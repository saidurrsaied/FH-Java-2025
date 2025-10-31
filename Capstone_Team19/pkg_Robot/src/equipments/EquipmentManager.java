package equipments; // Assuming this is where EM resides

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition; // For more robust waiting
import java.util.concurrent.locks.Lock;      // For more robust waiting
import java.util.concurrent.locks.ReentrantLock; // For more robust waiting

import taskManager.*; // Import Task and specific task types

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
    // Queue for robots holding an item, waiting for a packing station
    private final Queue<Robot> robotsWaitingForStation = new LinkedList<>();
    // Queue for robots holding an item, waiting for a charging station
    private final Queue<Robot> robotsWaitingForCharge = new LinkedList<>();

    // --- Concurrency Control ---
    private final Lock managerLock = new ReentrantLock(true);
    private final Condition stationAvailableCondition = managerLock.newCondition();
    
    public EquipmentManager(List<Robot> robots, 
                            List<ChargingStation> chargingStations, 
                            List<PackingStation> packingStations,
                            BlockingQueue<Task> taskSubmissionQueue) {
        this.availableRobots = robots;
        this.allChargingStations = chargingStations;
        this.allPackingStations = packingStations;
        this.taskSubmissionQueue = taskSubmissionQueue;
    }
    
    @Override
    public void run() {
        System.out.println("[EquipmentManager] Thread started.");
        while (!Thread.currentThread().isInterrupted()) {
        	Task newTask;
			try {
                // 1. Wait for a new task. This blocks *outside* the lock.
				newTask = taskSubmissionQueue.take();
	        	
                // Lock the manager to safely access shared lists
                managerLock.lock();
                try {
                    // 2. Handle the new task
                    switch (newTask.getType()) {
                        case PICK_ORDER: {
                            Point itemLocation = ((Order) newTask).getItemLocation(); // Use interface
                            
                            // Check available robots, find the nearest one
                            Robot foundRobot = findClosestAvailableRobot(itemLocation);
                            
                            if (foundRobot == null) {
                                // No fit robot, save in pending queue
                                pendingPickTasks.addLast(newTask);
                                System.out.printf("[EquipmentManager] No available robot for %s → Pending (%d)%n", 
                                		          (Order) newTask, pendingPickTasks.size());
                                break; // Exit switch, loop continues
                            } else {
                                // Robot found! Dispatch immediately.
                                System.out.printf("[EquipmentManager] DISPATCH %s -> %s%n", (Order) newTask, foundRobot.getID());
                                availableRobots.remove(foundRobot);
                                foundRobot.assignTask(newTask); // Send task to robot
                            }
                            break; // Exit switch
                        }
                        // (Add additional cases if needed)
                        default:
                            System.out.printf("[EquipmentManager] Unknown task type received: %s%n", newTask.getType());
                            break;
                    } // end switch
                } finally {
                    managerLock.unlock(); // Always unlock after handling
                }
			} catch (InterruptedException e) {
				System.out.println("EquipmentManager thread interrupted.");
                Thread.currentThread().interrupt(); // Preserve interrupt status
			}
        }
        System.out.println("EquipmentManager thread stopped.");
    }
    
    /**
     * (Public) Called by Robot thread when it finishes a task.
     * This is the *other* main entry point and MUST be thread-safe.
     */
    public void reportRobotAvailable(Robot robot, Task finishedTask) {
        managerLock.lock(); // Lock to safely modify shared lists
        try {
            System.out.printf("[EquipmentManager] Robot %s finished task %s.%n", robot.getID(), finishedTask.getID());

            // 1. Release resources (e.g., Packing Station)
            releaseResourcesFrom(robot, finishedTask);

            // 2. Check battery (highest priority)
            if (robot.getBatteryPercentage() < LOW_BATTERY_PERCENT) {
                System.out.printf("[EquipmentManager] Robot %s needs to be charged.%n", robot.getID());
                if (dispatchChargeTask(robot)) {
                    return; // Robot is now busy charging
                } else {
                	// Can not find any available charging station, add robot to waiting queue for charging station
                    System.out.printf("[%s] WARNING: Robot %s needs charge, but no stations free!%n", this.ID, robot.getID());
                    
                    if (!robotsWaitingForCharge.contains(robot)) {
                        robotsWaitingForCharge.add(robot);
                    }
                    
                    if ((finishedTask.getType() != TaskType.GO_TO_START) && 
                        !robot.getCurrentPosition().equals(robot.getStartingPosition())) {
                    	// Robot is not at starting point, send task to ask it go to starting point
                        System.out.printf("[%s] Sending Robot %s to Starting Point to wait for charge.%n", this.ID, robot.getID());
                        robot.assignTask(new GoToStartTask(robot.getStartingPosition()));
                        return;
                        } else {
                        // Robot is already at starting point, still waiting for charging station be available
                        System.out.printf("[%s] Robot %s is at Start Point, waiting for charge. Will not be assigned tasks.%n", this.ID, robot.getID());
                        return; // return here, then will not be added to available list
                    }
                }
            }

            // 3. Check if robot just finished its "go to start" task
            if (finishedTask.getType() == TaskType.GO_TO_START) { // Assuming GO_TO_START type
                System.out.printf("[%s] Robot %s has arrived at Start Position. Now Available.%n", this.ID, robot.getID());
                availableRobots.add(robot); 
                // A robot is free, check if it can take a pending task
                dispatchPendingTasks(); 
                return;
            }

            // 4. (Robot just finished Order or Charge)
            // Try to assign a pending task *immediately*
            if (tryAssignPendingTaskTo(robot)) {
                System.out.printf("[%s] Robot %s assigned a new pending task immediately.%n", this.ID, robot.getID());
                return; // Robot is busy again
            }

            // 5. No new task. Send robot to its Starting Position to be idle.
            System.out.printf("[%s] No pending tasks for Robot %s. Sending to Starting Point.%n", this.ID, robot.getID());
            robot.assignTask(new GoToStartTask(robot.getStartingPosition())); // Assuming GoToStartTask exists

        } finally {
            managerLock.unlock(); // Always release lock
        }
    }
      
    /**
     * (Public) Called by Robot *during* its 'execute' method (Just-in-Time).
     */
    public PackingStation requestAvailablePackingStation(Robot robot) throws InterruptedException {
        managerLock.lock(); // Lock to check stations and wait
        try {
            PackingStation availableStation = findAnyAvailableStation();

            // Loop while no station is available
            while (availableStation == null) {
                System.out.printf("[%s] All Packing Stations busy. Robot %s must wait...%n", ID, robot.getID());
                if (!robotsWaitingForStation.contains(robot)) {
                    robotsWaitingForStation.add(robot); 
                }

                // Wait for a signal (from releaseResourcesFrom)
                stationAvailableCondition.await(); 

                // Woken up! Try again to find a station
                availableStation = findAnyAvailableStation();
            }
            
            // Success!
            robotsWaitingForStation.remove(robot); // Remove from waiting list
            availableStation.tryAcquire(); // Reserve the station
            System.out.printf("[%s] Allocating Station %s to Robot %s.%n", ID, availableStation.getID(), robot.getID());
            return availableStation;

        } catch (InterruptedException e) {
             // If interrupted while waiting
             robotsWaitingForStation.remove(robot); // Ensure robot is removed from wait queue
             System.out.printf("[%s] Robot %s was interrupted while waiting for a station.%n", ID, robot.getID());
             Thread.currentThread().interrupt(); // Re-set interrupt flag
             return null; // Signal to the Order task that it failed
        } finally {
            managerLock.unlock(); // Always release lock
        }
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

        // Calculate farthest distance that robot can move 
        // from starting point --> item position --> packing position
        double distToPick = robotPos.distance(orderPosition);
        double distToPack = orderPosition.distance(packPos);
        double totalDistance = distToPick + distToPack;

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
    
    /**
     * (Internal) Scans the pending queue and dispatches tasks if robots are available.
     * Must be called while holding the lock.
     */
    private void dispatchPendingTasks() {
        if (availableRobots.isEmpty() || pendingPickTasks.isEmpty()) {
            return; // No resources or no work
        }

        System.out.printf("[%s] Brain: Checking %d pending tasks for %d available robots...%n", ID, pendingPickTasks.size(), availableRobots.size());
        Iterator<Task> iterator = pendingPickTasks.iterator();

        while (iterator.hasNext()) {
            if (availableRobots.isEmpty()) break; // No more robots
            
            Task task = iterator.next();
            
            if (task instanceof Order) {
                // Find a robot for this task
                Robot bestRobot = findClosestAvailableRobot(((Order) task).getItemLocation());
                
                if (bestRobot != null) {
                    // Found a robot! Dispatch.
                    System.out.printf("[%s] Brain: Dispatching PENDING task %s -> Robot %s%n", 
                                      this.ID, task.getID(), bestRobot.getID());
                                       
                    iterator.remove(); // Remove task from pending
                    availableRobots.remove(bestRobot); // Robot is no longer available
                    
                    // Note: Station is NOT locked here. It will be requested Just-in-Time.
                    bestRobot.assignTask(task);
                }
            }
            // If no robot, loop to next task (maybe a closer one exists for another robot)
        }
    }
    
    /**
     * (Internal) Releases resources and signals waiting threads.
     * Must be called while holding the lock.
     */
    private void releaseResourcesFrom(Robot robot, Task finishedTask) {
        boolean stationReleased = false;
        boolean chargeStationReleased = false;
        
        switch(finishedTask.getType()) {
        case PICK_ORDER:
        	// release packing station
            PackingStation packingStation = findPackingStationAt(robot.getCurrentPosition());
            if (packingStation != null) {
            	packingStation.release();
                stationReleased = true;
                System.out.printf("[%s] Released Packing Station %s.%n", ID, packingStation.getID());
            }
        	break;
        case CHARGE_ROBOT:
            ChargingStation chargingStation = findChargingStationAt(robot.getCurrentPosition());
            if (chargingStation != null) {
            	chargingStation.release();
            	chargeStationReleased = true;
                // (Signal a different condition if you have robots waiting for charge)
                System.out.printf("[%s] Released Charging Station %s.%n", ID, chargingStation.getID());
            }
        	break;
		default:
			break;
        }

        // If a packing station was freed AND robots are waiting...
        if (stationReleased && !robotsWaitingForStation.isEmpty()) {
            System.out.printf("[%s] Signaling %d robots waiting for a station.%n", ID, robotsWaitingForStation.size());
            stationAvailableCondition.signalAll(); // Wake up waiting robots
        }
        // If a charging station is freed and there is robot waiting for it
        if (chargeStationReleased && !robotsWaitingForCharge.isEmpty()) {
            System.out.printf("[%s] A charge station is free. Checking %d robots waiting for charge...%n", ID, robotsWaitingForCharge.size());
            Robot robotToCharge = null;
            Iterator<Robot> iterator = robotsWaitingForCharge.iterator();
            while(iterator.hasNext()) {
                 Robot waitingRobot = iterator.next();
                 
                 // Check if there is need-charge robots at home, priority them
                 if (waitingRobot.getState() == RobotState.IDLE && 
                     waitingRobot.getCurrentPosition().equals(waitingRobot.getStartingPosition())) 
                 {
                     robotToCharge = waitingRobot;
                     iterator.remove();
                     break;
                 }
            }

            if (robotToCharge != null) {
                System.out.printf("[%s] Found idle robot %s at Start Point. Sending to charge.%n", this.ID, robotToCharge.getID());
                dispatchChargeTask(robotToCharge); 
            } else {
                 System.out.printf("[%s] No robots are ready at Start Point to take the free charge station.%n", this.ID);
            }
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
                Order order = (Order) task;
                
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
    
    private PackingStation findAnyAvailableStation() {
        for (PackingStation station : allPackingStations) {
            if (station.isAvailable()) return station;
        }
        return null;
    }
    
    private PackingStation findPackingStationAt(Point location) {
        for (PackingStation station : allPackingStations) {
            if (station.getLocation().equals(location)) return station;
        }
        return null;
    }

    private ChargingStation findAnyAvailableChargeStation() {
        for (ChargingStation station : allChargingStations) {
            if (station.isAvailable()) return station;
        }
        return null;
    }
    
    private ChargingStation findChargingStationAt(Point location) {
         for (ChargingStation station : allChargingStations) {
            if (station.getLocation().equals(location)) return station;
        }
        return null;
    }
    
    private boolean dispatchChargeTask(Robot robot) {
        ChargingStation freeStation = findAnyAvailableChargeStation();
        if (freeStation != null) {
            freeStation.tryAcquire();
            robot.assignTask(new Charge(freeStation.getLocation(), robot.getID())); // Assuming ChargeTask exists
            return true;
        }
        return false;
    }
}