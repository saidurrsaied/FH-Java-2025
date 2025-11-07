package equipmentManager; // Assuming this is where EM resides

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition; // For more robust waiting
import java.util.concurrent.locks.Lock;      // For more robust waiting
import java.util.concurrent.locks.ReentrantLock; // For more robust waiting

import taskManager.*; // Import Task and specific task types
import warehouse.PackingStation;

/**
 * Central Brain using "Just-in-Time Locking".
 * Dispatches tasks based *only* on robot availability initially.
 * Robots request stations *during* task execution.
 * Uses Lock and Condition for safer waiting than synchronized/wait/notify.
 */
public class EquipmentManager implements Runnable {
	// --- Private values ---
    private static final int LOW_BATTERY_PERCENT = 30;       // <30% means low battery
    
    private final String ID = this.getClass().getName();
    
    // --- Resource Lists ---
    private final List<Robot> availableRobots; // Robots ready at idle spots
    private final List<PackingStation> allPackingStations;
    private final List<ChargingStation> allChargingStations;
    
    // --- Queues ---
    private final Deque<Task> pendingPickTasks = new ArrayDeque<>();
    private final BlockingQueue<Task> taskSubmissionQueue;

	private final BlockingQueue<Robot> robotsWaitingForCharge = new LinkedBlockingQueue<>();;
	
	private LinkedBlockingQueue<ChargingStation> availableChargeStations;
	private LinkedBlockingQueue<PackingStation> availablePackStations;
    // --- Concurrency Control ---   
    public EquipmentManager(List<Robot> robots,
                            List<ChargingStation> chargingStations,
                            List<PackingStation> packingStations,
                            BlockingQueue<Task> taskSubmissionQueue) {
        this.availableRobots = robots;
        this.allChargingStations = chargingStations;
        this.allPackingStations = packingStations;
        this.taskSubmissionQueue = taskSubmissionQueue;
        
        // Add all charging stations into availableChargingStationQueue
        this.availableChargeStations = new LinkedBlockingQueue<>(chargingStations);
        
        // Add all packing stations into availablePackingStationQueue
        this.availablePackStations = new LinkedBlockingQueue<>(packingStations);
    }
    
    @Override
    public void run() {
        System.out.println("[EquipmentManager] Thread started.");
        while (!Thread.currentThread().isInterrupted()) {
        	Task newTask;
			try {
				newTask = taskSubmissionQueue.take();
				synchronized (this) {
                // Handle the new task
                switch (newTask.getType()) {
	                    case PICK_ORDER: 
	                        // Received Pick Order Task from TaskManager
	                        Point itemLocation = ((OrderTask) newTask).getItemLocation(); // Use interface
	                            
	                        // Check available robots, find the nearest one
	                        Robot foundRobot = findClosestAvailableRobot(itemLocation);
	                        if (foundRobot == null) {
	                            // No fit robot, save in pending queue, assign this OrderTask later
	                            pendingPickTasks.addLast(newTask);
	                            System.out.printf("[EquipmentManager] No available robot for %s → Pending (%d)%n", 
	                                		     (OrderTask) newTask, pendingPickTasks.size());
	                        } else {
	                            // Robot found, dispatch this OrderTask immediately.
	                            System.out.printf("[EquipmentManager] DISPATCH %s -> %s%n", (OrderTask) newTask, foundRobot.getID());
	                            availableRobots.remove(foundRobot);
	                            foundRobot.assignTask(newTask); // Send task to robot
	                        }
	                        break;
	                    // (Add additional cases if needed)
	                    default:
	                        System.out.printf("[EquipmentManager] Unknown task type received: %s%n", newTask.getType());
	                        break;
	                    } // end switch
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
    }
    
    /**
     * (Public) Called by Robot thread when it finishes a task.
     * This is the *other* main entry point and MUST be thread-safe.
     * @throws InterruptedException 
     */
    public synchronized void reportFinishedTask(Robot robot, Task finishedTask, boolean taskStatus) throws InterruptedException {
            System.out.printf("[EquipmentManager] Robot %s finished task %s.%n", robot.getID(), finishedTask.getID());
            
			// No matter what the previous task, try to assign. There is battery check in the function then
            // no worry about low battery service
			boolean assignedPending = tryAssignPendingTaskTo(robot);
			
			if (assignedPending) {
			    // Robot received pending task, then return to do the task
			    return;
			}
			
			// --- No available pending task ---
			switch(finishedTask.getType()) {
			    case PICK_ORDER:
			    case STOCK_ITEM:
			        // Check battery requirement
			        if (robot.getBatteryPercentage() < LOW_BATTERY_PERCENT) {
			        	ChargingStation freeChargingStation = requestAvailableChargingStation(0); // try to find available charging station
			            if (freeChargingStation != null) {
		                	// Found available charging station
		                	System.out.printf("[EquipmentManager] Assign charging station %s to robot %s.%n", freeChargingStation.getID(), robot.getID());
		                	robot.assignTask(new ChargeTask(freeChargingStation, robot.getID()));
			            } else {
			            	// No free charging station, robot needs to wait -> Assign 'GoToChargingStationAndWaitTask'
	                        ChargingStation closest = findClosestChargingStation(robot.getCurrentPosition());
	                        
	                        if (closest != null) {
	                            System.out.printf("[EquipmentManager] %s battery low. All stations busy. Assigning 'GoToChargingStationAndWaitTask' (Target: %s)%n", 
	                                              robot.getID(), closest.getID());
	                            robot.assignTask(new GoToChargingStationAndWaitTask(closest));
	                        } else {
	                            System.out.println("[EquipmentManager] ERROR: No charging stations! Robot " + robot.getID() + " stuck!");
	                            availableRobots.add(robot); 
	                        }
			            }
			        } else {
			            // Battery is still high, go to starting point
			            System.out.printf("[EquipmentManager] No pending tasks. Sending %s to Start Point.%n", robot.getID());
			            robot.assignTask(new GoToStartTask(robot.getStartingPosition()));
			        }
			        break;
			        
			    case CHARGE_ROBOT:
			        // Just fully charged, no pending task -> Go to Starting point
			        System.out.printf("[EquipmentManager] %s finished charging. No pending tasks. Sending to Start Point.%n", robot.getID());
			        robot.assignTask(new GoToStartTask(robot.getStartingPosition()));
			        break;
			
			    case GO_TO_CHARGING_STATION_AND_WAIT:
			        System.out.printf("[EquipmentManager] Robot %s finished 'GoToWait'. %n", robot.getID());
			        if (taskStatus) {
			        	// Charge successfully
				        System.out.printf("[EquipmentManager] Robot %s is fully charged %n", robot.getID());
			        } else {
			        	// Can not get access to charging station, send to waiting queue for charging
			        	System.out.printf("[EquipmentManager] Robot %s can not be charged, battery now is %.1f %n", robot.getID(), robot.getBatteryPercentage());
			        	robotsWaitingForCharge.add(robot);
			        }
			        // Go to starting point in both cases
			        robot.assignTask(new GoToStartTask(robot.getStartingPosition()));
			        break;
			        
			    case GO_TO_START:
			        System.out.printf("[EquipmentManager] Robot %s is at Start Point. Now IDLE.%n", robot.getID());
			        availableRobots.add(robot);
			        break;
			        
			    default:
			         availableRobots.add(robot); // Safety purpose
			         break;
			}
    }
      
    /**
     * (Public) Called by Robot *during* its 'execute' method (Just-in-Time).
     */
    public PackingStation requestAvailablePackingStation(Robot robot) throws InterruptedException {
    	// Waiting for available packing station indefinitely, robot will be in WAITING state here
    	// until there will be available packing station.
        PackingStation availableStation = availablePackStations.take();
        
        System.out.printf("[%s] Found available Packing Station %s for Robot %s.%n", ID, availableStation.getID(), robot.getID());
        return availableStation;
    }
    
    /**
     * (Public) Called by Robot *during* its 'execute' method (Just-in-Time).
     */
    public ChargingStation requestAvailableChargingStation(long timeout) throws InterruptedException {
    	// Waiting for available packing station indefinitely, robot will be in WAITING state here
    	// until there will be available packing station.
        ChargingStation availableStation = availableChargeStations.poll(timeout, TimeUnit.SECONDS);
        return availableStation;
    }
    
 // --- Internal Logic (Called from locked methods) ---
    /**
     
     */
    private double calculateRequiredEnergy(Robot robot, Point orderPosition) {
        Point robotPos = robot.getCurrentPosition();
        
        // Find the farthest position of packing station to order position that can be available
        PackingStation farthestPackStation = findFarthestPackingStation(orderPosition);
        
        if (farthestPackStation == null) {
            System.out.printf("[%s] No packing station found%n", ID);
            return Double.MAX_VALUE;
        }
        Point packPos = farthestPackStation.getLocation();

        ChargingStation farthestChargeStation = findFarthestChargingStation(packPos);
        if (farthestChargeStation == null) {
             System.out.printf("[%s] No charging station found%n", ID);
             return Double.MAX_VALUE;
        }
        Point chargePos = farthestChargeStation.getLocation();
        
        // Calculate farthest distance that robot can move 
        // from starting point --> item position --> packing station --> charging station
        double distToPick = robotPos.distance(orderPosition);
        double distToPack = orderPosition.distance(packPos);
        double distToCharge = packPos.distance(chargePos);
        
        double totalDistance = distToPick + distToPack + distToCharge;

        double energyForTravel = totalDistance * Robot.getBatteryCosumedPerMeter();

        return energyForTravel;
    }
    
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
        System.out.println(farthestStation);
        return farthestStation;
    }
    
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
     * (Public) Called by a Task (e.g., ChargeTask, GoToChargingStationAndWaitTask)
     * to return a station to the pool of available stations.
     * * @param station The ChargingStation that is now free.
     */
    public void releaseChargeStation(ChargingStation station) {
        if (station == null) {
            System.err.printf("ERROR: Attempted to release a null station.%n");
            return;
        }
        Robot waitingRobot = null;
        waitingRobot = robotsWaitingForCharge.poll();
        
        if (waitingRobot != null) {
        	waitingRobot.assignTask(new ChargeTask(station, waitingRobot.getID()));
        } else {
            try {
                // put() will add the station back to the queue.
                // If the queue is full (which shouldn't happen with LinkedBlockingQueue
                // unless you set a capacity), it will wait.
            	availableChargeStations.put(station);
                
                System.out.printf("[EquipmentManager] Charging Station %s released back to queue. (Queue size: %d)%n", 
                                  station.getID(), availableChargeStations.size());
                                  
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Re-set the interrupt flag
                System.err.printf("ERROR: Interrupted while trying to release station %s. Station may be lost!%n", station.getID());
            }
        }

    }
    
    /**
     * (Public) Called by a Task (e.g., OrderTask)
     * to return a station to the pool of available stations.
     *
     * @param station The PackingStation that is now free.
     */
    public void releasePackingStation(PackingStation station) {
        if (station == null) {
            System.err.printf("ERROR: Attempted to release a null packing station.%n");
            return;
        }
        
        try {
            // put() will add the station back to the queue.
            availablePackStations.put(station);
            
            System.out.printf("[EquipmentManager] Packing Station %s released back to queue. (Queue size: %d)%n", 
                              station.getID(), availablePackStations.size());
                              
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Re-set the interrupt flag
            System.err.printf("ERROR: Interrupted while trying to release station %s. Station may be lost!%n", station.getID());
        }
    }
    
    /**
     * (Internal) Optimization: Tries to assign a pending task to a *specific* robot.
     * Must be called while holding the lock.
     */
    private boolean tryAssignPendingTaskTo(Robot robot) {
        if (pendingPickTasks.isEmpty()) {
            return false;
        }

        Iterator<Task> iterator = pendingPickTasks.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            
            if (task.getType() == TaskType.PICK_ORDER) {
                OrderTask order = (OrderTask) task;
                double requiredBattery = calculateRequiredEnergy(robot, order.getItemLocation());
                
                if (robot.getBatteryPercentage() >= requiredBattery) {
                    System.out.printf("[%s] Robot %s assigned PENDING task %s immediately.%n", 
                                      this.ID, robot.getID(), task.getID());
                    
                    iterator.remove();
                    robot.assignTask(task);
                    return true;
                }
            }
        }
        return false;
    }
    
 // --- Helper Functions (Finders) ---
    // (These are private and should be called from within a locked method)

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
        		System.out.printf("[EquipmentManager] Robot %s does not have enough battery %n", robot.getID());
        	}
        }
        return bestRobot;
    }
}