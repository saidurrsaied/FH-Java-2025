package equipmentManager; // Assuming this is where EM resides

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import pathFinding.PathFinding;
import taskManager.*; // Import Task and specific task types
import warehouse.PackingStation;

/**
 * Central Brain using "Just-in-Time Locking".
 * Dispatches tasks based *only* on robot availability initially.
 * Robots request stations *during* task execution.
 * Uses Lock and Condition for safer waiting than synchronized/wait/notify.
 */
public class EquipmentManager implements Runnable {
	// --- Constants ---
    private static final int LOW_BATTERY_PERCENT = 30;        // <30% means low battery, robot needs to be charged
    private static final int HIGH_BATTERY_PERCENT = 90;       // >90% means high battery, no need to charge more
    
    private final String ID = "Equipment Manager";
    
    // --- Full Resource Lists ---
    private final List<Robot> availableRobots; // Robots currently idle at their start positions
    private final List<PackingStation> allPackingStations;
    private final List<ChargingStation> allChargingStations;
    
    // --- Queues ---
    private final Deque<Task> pendingPickTasks = new ArrayDeque<>(); // Pending work
    private final BlockingQueue<Task> taskSubmissionQueue; // Incoming new task
	private final BlockingQueue<Robot> robotsWaitingForCharge = new LinkedBlockingQueue<>();
	
	// Queues for available (free) stations
	private LinkedBlockingQueue<ChargingStation> availableChargeStations;
	private LinkedBlockingQueue<PackingStation> availablePackStations;
	
    // --- Other utilities  ---   	
	private final PathFinding pathFinding;
	
    public EquipmentManager(List<Robot> robots,
                            List<ChargingStation> chargingStations,
                            List<PackingStation> packingStations,
                            BlockingQueue<Task> taskSubmissionQueue,
                            PathFinding pathFinding) {
        this.availableRobots = robots;
        this.allChargingStations = chargingStations;
        this.allPackingStations = packingStations;
        this.taskSubmissionQueue = taskSubmissionQueue;
        this.pathFinding = pathFinding;
        
        // Initialize the free station queues from the master lists
        this.availableChargeStations = new LinkedBlockingQueue<>(chargingStations);
        this.availablePackStations = new LinkedBlockingQueue<>(packingStations);
    }
    
    @Override
    public void run() {
        System.out.printf("[%s] Thread started. %n", ID);
        while (!Thread.currentThread().isInterrupted()) {
        	Task newTask;
			try {
				// Wait for a new task from the TaskManager (blocks outside the lock)
				newTask = taskSubmissionQueue.take();
				
				// Must lock 'this' to safely access availableRobots & pendingPickTasks
				synchronized (this) {
                // Handle the new task
                switch (newTask.getType()) {
	                    case PICK_ORDER: 
	                        Point itemLocation = ((OrderTask) newTask).getItemLocation(); // Use interface
	                            
	                        // Find the best robot (closest, enough battery)
	                        Robot foundRobot = findClosestAvailableRobot(itemLocation);
	                        if (foundRobot == null) {
	                        	// No suitable robot, add to the pending queue
	                            pendingPickTasks.addLast(newTask);
	                            System.out.printf("[%s] No available robot for %s → Pending (%d)%n",
	                            		           ID, (OrderTask) newTask, pendingPickTasks.size());
	                        } else {
	                            // Robot found, dispatch this OrderTask immediately.
	                            System.out.printf("[%s] DISPATCH %s -> %s%n", ID, (OrderTask) newTask, foundRobot.getId());
	                            availableRobots.remove(foundRobot); // Now busy
	                            foundRobot.assignTask(newTask); // Send task to robot
	                        }
	                        break;
	                    // (Add additional cases if needed)
	                    default:
	                        System.out.printf("[%s] Unknown task type received: %s%n", ID, newTask.getType());
	                        break;
	                    } // end switch
					} // end synchronized block
			} catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
					e.printStackTrace();
			}
        }
    }
    
    /**
     * (Public) Callback from a Robot thread when it finishes a task.
     * This is a main entry point and MUST be thread-safe (synchronized).
     */
    public synchronized void reportFinishedTask(Robot robot, Task finishedTask, boolean taskStatus) throws InterruptedException {
            System.out.printf("[%s] Robot %s finished task %s.%n", ID, robot.getId(), finishedTask.getID());
            
            // Priority 1: Always try to assign a pending task first.
            // (This function already checks battery, so it's safe)
			boolean assignedPending = tryAssignPendingTaskTo(robot);
			
			if (assignedPending) {
				// Robot received a new task and is busy. Our work here is done.
			    return;
			}
			
			// --- No available pending task, decide what to do next ---
			switch(finishedTask.getType()) {
			    case PICK_ORDER:
			    case STOCK_ITEM:
			    case GO_TO_START: // Also check battery if robot just arrived at start
			    	
			        // Check battery requirement
			        if (robot.getBatteryPercentage() < LOW_BATTERY_PERCENT) {
			        	// Battery is low, robot MUST charge.
	                    // Try to poll(0) a free station.
			        	ChargingStation freeChargingStation = requestAvailableChargingStation(0);
			        	
			            if (freeChargingStation != null) {
		                	// Found available charging station
			            	freeChargingStation.setState(ObjectState.BUSY);
		                	System.out.printf("[%s] Assign charging station %s to robot %s.%n", ID, freeChargingStation.getId(), robot.getId());
		                	robot.assignTask(new ChargeTask(freeChargingStation, robot.getId()));
			            } else {
			            	// No free charging station, assign 'GoToChargingStationAndWaitTask'
	                        ChargingStation closest = findClosestChargingStation(robot.getCurrentPosition());
	                        
	                        if (closest != null) {
	                            System.out.printf("[%s] %s battery low. All stations busy. Assigning 'GoToChargingStationAndWaitTask' (Target: %s)%n", 
	                                              ID, robot.getId(), closest.getId());
	                            robot.assignTask(new GoToChargingStationAndWaitTask(closest));
	                        } else {
	                            System.out.println("ERROR: No charging stations! Robot " + robot.getId() + " stuck!");
	                            availableRobots.add(robot); 
	                        }
			            }
			        } else {
			        	// Battery is still high
				        if (finishedTask.getType() == TaskType.GO_TO_START) {
				        	// Robot is at Start Point, battery OK, no pending tasks.
					        System.out.printf("[EquipmentManager] Robot %s is at Start Point. Now IDLE.%n", robot.getId());
					        if (!availableRobots.contains(robot)) {
			                    availableRobots.add(robot);
			                    robot.setState(RobotState.IDLE); // (Ensure Robot state is updated)
			                }
				        } else {
				        	// Just finished Pick/Stock, battery OK, no pending tasks -> go to Starting point
				            System.out.printf("[EquipmentManager] No pending tasks. Sending %s to Start Point.%n", robot.getId());
				            robot.assignTask(new GoToStartTask(robot.getStartingPosition()));
				        }
			        }
			        break;
			        
			    case CHARGE_ROBOT:
			        // Just fully charged, no pending task -> Back to Starting point
			        System.out.printf("[EquipmentManager] %s finished charging. No pending tasks. Sending to Start Point.%n", robot.getId());
			        robot.assignTask(new GoToStartTask(robot.getStartingPosition()));
			        break;
			
			    case GO_TO_CHARGING_STATION_AND_WAIT:
			        System.out.printf("[EquipmentManager] Robot %s finished 'GoToWait'. %n", robot.getId());
			        
			        if (taskStatus) {
			        	// Charge successfully
				        System.out.printf("[EquipmentManager] Robot %s is fully charged %n", robot.getId());
			        } else {
			        	// Could not get access to charging station (timeout)
			        	System.out.printf("[EquipmentManager] Robot %s can not be charged, battery now is %.1f %n", robot.getId(), robot.getBatteryPercentage());
			        	robotsWaitingForCharge.add(robot); // Send to waiting queue
			        }
			        // Go to starting point in both cases
			        robot.assignTask(new GoToStartTask(robot.getStartingPosition()));
			        break;
			        
			    default:
			         availableRobots.add(robot);// Safety fallback
			         break;
			}
    }
     
    /**
     * NEW METHOD: Called by a robot that timed out (15 min) while idle.
     * Must be synchronized because it conflicts with 'reportFinishedTask'
     * and 'run' (when accessing 'availableRobots').
     */
    public synchronized void idleRobotRequestsCharge(Robot robot) {
        System.out.printf("[EquipmentManager] %s (IDLE) requested charge due to timeout.%n", robot.getId());
        
        // Remove the robot from the available list to prevent race conditions
        availableRobots.remove(robot); 
        
        // Check if battery actually needs charging
        if (robot.getBatteryPercentage() >= HIGH_BATTERY_PERCENT) {
            System.out.printf("[EquipmentManager] %s requested charge but is already full. Returning to IDLE.%n", robot.getId());
            availableRobots.add(robot); // Add it back
            return; // Do nothing
        }
        
        // Battery is not full -> Assign a charge task
        ChargingStation freeStation = availableChargeStations.poll();
        if (freeStation != null) {
        	// A free station is available
        	freeStation.setState(ObjectState.BUSY);
            System.out.printf("[EquipmentManager] Found free station %s. Assigning 'ChargeTask'.%n", freeStation.getId());
            robot.assignTask(new ChargeTask(freeStation, robot.getId())); // (Modify ChargeTask constructor if needed)
        } else {
        	// No free stations
            ChargingStation closest = findClosestChargingStation(robot.getCurrentPosition());
            if (closest != null) {
                System.out.printf("[EquipmentManager] All stations busy. Assigning 'GoToChargingStationAndWaitTask'.%n");
                robot.assignTask(new GoToChargingStationAndWaitTask(closest));
            } else {
                System.out.println("[EquipmentManager] ERROR: No charging stations! Robot " + robot.getId() + " stuck in IDLE!");
                availableRobots.add(robot); // Add it back
            }
        }
    }
    
    /**
     * (Public) Called by a Task to request a Packing Station.
     * This blocks indefinitely.
     */
    public PackingStation requestAvailablePackingStation(Robot robot) throws InterruptedException {
        PackingStation availableStation = availablePackStations.take();
        System.out.printf("[%s] Found available Packing Station %s for Robot %s.%n", ID, availableStation.getId(), robot.getId());
        return availableStation;
    }
    
    /**
     * (Public) Called by a Task to request a Charging Station with a timeout.
     */
    public ChargingStation requestAvailableChargingStation(long timeout) throws InterruptedException {
    	// This will block for 'timeout' seconds.
        ChargingStation availableStation = availableChargeStations.poll(timeout, TimeUnit.SECONDS);
        return availableStation;
    }
    
    /**
     * (Public) Called by a Task to find the most effective path.
     */
    public List<Point> requestPath(Robot robot, Point targetLocation) {
        return pathFinding.findPath(robot.getLocation(), targetLocation);
    }
    
    // --- Internal Logic (Called from locked methods) ---

    /**
     * Calculates the worst-case energy required for a robot to complete an order.
     * Path: Robot -> Pick -> Farthest_Pack -> Farthest_Charge
     */
    private double calculateRequiredEnergy(Robot robot, Point orderPosition) {
        Point robotPos = robot.getCurrentPosition();
        
        // Find the FARTHEST packing station (worst-case scenario)
        PackingStation farthestPackStation = findFarthestPackingStation(orderPosition);
        if (farthestPackStation == null) {
            System.out.printf("[%s] No packing station found%n", ID);
            return Double.MAX_VALUE;
        }
        Point packPos = farthestPackStation.getLocation();

        // Find the FARTHEST charging station from that packing station
        ChargingStation farthestChargeStation = findFarthestChargingStation(packPos);
        if (farthestChargeStation == null) {
             System.out.printf("[%s] No charging station found%n", ID);
             return Double.MAX_VALUE;
        }
        Point chargePos = farthestChargeStation.getLocation();
        
        // CALCULATE PATHS USING A-STAR
        
        List<Point> pathToPick = pathFinding.findPath(robotPos, orderPosition);
        List<Point> pathToPack = pathFinding.findPath(orderPosition, packPos);
        List<Point> pathToCharge = pathFinding.findPath(packPos, chargePos);
        
        if (pathToPick.isEmpty() || pathToPack.isEmpty() || pathToCharge.isEmpty()) {
            System.out.printf("[%s] Cannot find a valid path for task.%n", ID);
            return Double.MAX_VALUE; 
        }
        
        // The distance is the number of steps in the path
        double distToPick = pathToPick.size();
        double distToPack = pathToPack.size();
        double distToCharge = pathToCharge.size();
        double totalDistance = distToPick + distToPack + distToCharge;
        
        double energyForTravel = totalDistance * Robot.getBatteryCosumedPerMeter();

        System.out.printf("[%s] Energy calculation for robot %s (Dist: %.1f * Cost: %.1f) = Total: %.1f%n",
                ID, robot.getId(), totalDistance, Robot.getBatteryCosumedPerMeter(), energyForTravel);
        
        return energyForTravel;
    }
    
    /**
     * Helper to find the farthest packing station from a target point.
     */
    private PackingStation findFarthestPackingStation(Point target) {
        if (allPackingStations.isEmpty()) {
            return null;
        }
        
        PackingStation farthestStation = null;
        double maxDistance = 0.0;
        
        for (PackingStation station : allPackingStations) {
            double distance = station.getLocation().distance(target);
            if (distance > maxDistance) {
                maxDistance = distance;
                farthestStation = station;
            }
        }
        return farthestStation;
    }
    
    /**
     * Helper to find the farthest charging station from a target point.
     */
    private ChargingStation findFarthestChargingStation(Point target) {
        if (allChargingStations.isEmpty()) return null;

        ChargingStation farthestStation = null;
        double maxDistance = 0.0;
        
        for (ChargingStation station : allChargingStations) {
            double distance = station.getLocation().distance(target);
            if (distance > maxDistance) {
                maxDistance = distance;
                farthestStation = station;
            }
        }
        return farthestStation;
    }
    
    /**
     * Helper to find the closest charging station to a target point.
     */
    private ChargingStation findClosestChargingStation(Point target) {
        if (allChargingStations.isEmpty()) {
            return null;
        }
        
        ChargingStation closestChargingStation = null;
        double minDistance = Double.MAX_VALUE;
        
        for (ChargingStation station : allChargingStations) {
            double distance = station.getLocation().distance(target);
            if (distance < minDistance) {
                minDistance = distance;
                closestChargingStation = station;
            }
        }
        return closestChargingStation;
    }

    /**
     * (Public) Returns a station to the pool of available charging stations.
     * This is thread-safe and prioritizes waiting robots.
     */
    public void releaseChargeStation(ChargingStation station) {
        if (station == null) {
            System.err.printf("ERROR: Attempted to release a null station.%n");
            return;
        }
        Robot waitingRobot = null;
        waitingRobot = robotsWaitingForCharge.poll();
        
        if (waitingRobot != null) {
        	waitingRobot.assignTask(new ChargeTask(station, waitingRobot.getId()));
        } else {
            try {
                // put() will add the station back to the queue.
                // If the queue is full (which shouldn't happen with LinkedBlockingQueue
                // unless you set a capacity), it will wait.
            	availableChargeStations.put(station);
                station.setState(ObjectState.FREE);
                System.out.printf("[EquipmentManager] Charging Station %s released back to queue. (Queue size: %d)%n", 
                                  station.getId(), availableChargeStations.size());
                                  
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Re-set the interrupt flag
                System.err.printf("ERROR: Interrupted while trying to release station %s. Station may be lost!%n", station.getId());
            }
        }

    }
    
    /**
     * (Public) Returns a station to the pool of available packing stations.
     * This is thread-safe.
     */
    public void releasePackingStation(PackingStation station) {
        if (station == null) {
            System.err.printf("ERROR: Attempted to release a null packing station.%n");
            return;
        }
        
        try {
            // put() will add the station back to the queue.
            availablePackStations.put(station);
            station.setState(ObjectState.FREE);
            System.out.printf("[EquipmentManager] Packing Station %s released back to queue. (Queue size: %d)%n", 
                              station.getId(), availablePackStations.size());
                              
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Re-set the interrupt flag
            System.err.printf("ERROR: Interrupted while trying to release station %s. Station may be lost!%n", station.getId());
        }
    }
    
    /**
     * (Internal) Finds the best-matching pending task for a newly free robot.
     * Must be called while holding the 'synchronized (this)' lock.
     */
    private boolean tryAssignPendingTaskTo(Robot robot) {
        if (pendingPickTasks.isEmpty()) {
            return false;
        }

        Iterator<Task> iterator = pendingPickTasks.iterator();
        Task bestTask = null; // Store the best task found
        double minDistance = Double.MAX_VALUE;

        // Iterate through all pending tasks to find the *best* one
        while (iterator.hasNext()) {
            Task task = iterator.next();
            
            if (task.getType() == TaskType.PICK_ORDER) {
                OrderTask order = (OrderTask) task;
                double requiredBattery = calculateRequiredEnergy(robot, order.getItemLocation());
                
                if (robot.getBatteryPercentage() >= requiredBattery) {
                    // Battery check passed. Now check distance.
                    double distance = robot.getCurrentPosition().distance(order.getItemLocation());
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestTask = task;
                    }
                }
            }
        }
        
        // Now, assign the best task (if one was found)
        if (bestTask != null) {
             System.out.printf("[%s] Robot %s assigned PENDING task %s immediately.%n", 
                                this.ID, robot.getId(), bestTask.getID());
             pendingPickTasks.remove(bestTask); // Remove the chosen task
             robot.assignTask(bestTask);
             return true;
        }
        
        return false; // No suitable task found
    }
    
    /**
     * (Internal) Finds the best-matching robot for a new task.
     * Must be called while holding the 'synchronized (this)' lock.
     */
    private Robot findClosestAvailableRobot(Point target) {
    	if (availableRobots.isEmpty()) {
            return null;
        }
    	
    	Robot bestRobot = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Robot robot : availableRobots) {
        	// 1. Check battery requirements for this robot to finish tasks
        	double requiredBattery = calculateRequiredEnergy(robot, target);
        	if (robot.getBatteryPercentage() >= requiredBattery) {
        		// Battery check passed, get destination
                double distance = robot.getCurrentPosition().distance(target);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestRobot = robot;
                }        		
        	} else {
        		// Not enough battery
        		System.out.printf("[EquipmentManager] Robot %s does not have enough battery %n", robot.getId());
        	}
        }
        return bestRobot;
    }
}