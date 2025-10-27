package equipment;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import taskManager.*; // Assuming Task, Order, ChargeTask, GoToHoldingTask, GoToStartTask are here
import common.Position; // Assuming you have a common Position class

/**
 * The central "Brain" 🧠 of the warehouse.
 * This class is responsible for managing all equipment (Robots, Stations)
 * and dispatching tasks in an optimal, thread-safe manner.
 *
 * This implementation uses "Eager Pre-booking":
 * It reserves *both* the Robot and the required Station *before*
 * dispatching the task, preventing any physical conflicts for resources.
 */
public class EquipmentManager {
    
    // A unique ID for logging
    private final String ID = this.getClass().getName();
    
    // --- Resource Lists ---
    
    // Tracks robots that are currently idle (in a holding spot) and ready for work
    private final List<Robot> availableRobots; 
    
    // Lists of ALL stations in the warehouse
    private final List<PackingStation> allPackingStations;
    private final List<HoldingStation> allHoldingStations;
    private final List<ChargingStation> allChargingStations;
    
    // Queue for tasks submitted by TaskManager, waiting for resources
    private final Queue<Task> pendingTasks = new LinkedList<>();
    
    /**
     * Constructs the EquipmentManager.
     * @param robots A list of ALL robots (assumed to be idle at start).
     * @param chargingStations List of all charging stations.
     * @param packingStations List of all packing stations.
     * @param holdingStations List of all holding/parking stations.
     */
    public EquipmentManager(List<Robot> robots, 
                            List<ChargingStation> chargingStations, 
                            List<PackingStation> packingStations,
                            List<HoldingStation> holdingStations) {
        
        // We use the passed-in list directly. 
        // Assumes the list itself is thread-safe (e.g., created with Collections.synchronizedList)
        this.availableRobots = robots; 
        this.allChargingStations = chargingStations;
        this.allPackingStations = packingStations;
        this.allHoldingStations = holdingStations;
    }
    
    /**
     * Entry Point 1: Called by TaskManager when a new business task arrives.
     * This method is synchronized to ensure thread-safety.
     */
    public synchronized void assignTask(Task task) {
        System.out.printf("[%s] Received task %s. Adding to pending queue.%n", this.ID, task.getID());
        pendingTasks.add(task);
        // Attempt to dispatch a task immediately
        dispatchPendingTasks();
    }
    
    /**
     * Entry Point 2: Callback from a Robot when it finishes its current task.
     * This method is synchronized to prevent race conditions.
     */
    public synchronized void reportRobotAvailable(Robot robot, Task finishedTask) {
        System.out.printf("[%s] Robot %s finished task %s.%n", this.ID, robot.getID(), finishedTask.getID());

        // 1. Release resources used by the *finished* task
        releaseResourcesFrom(robot, finishedTask);

        // 2. Check battery (highest priority)
        if (robot.getBatteryPercentage() < 20) {
            System.out.printf("[%s] Robot %s battery is low (%f%%).%n", this.ID, robot.getID(), robot.getBatteryPercentage());
            if (dispatchChargeTask(robot)) {
                return; // Robot is now busy charging
            } else {
                System.out.printf("[%s] WARNING: Robot %s needs charge, but no stations are free!%n", this.ID, robot.getID());
                // (Allowing robot to go to idle, will try to charge again later)
            }
        }

        // 3. Robot just finished its "go to idle" task. It is now truly available.
        if (finishedTask instanceof GotoHoldingTask || finishedTask instanceof GotoStartTask) {
            System.out.printf("[%s] Robot %s has arrived at its idle spot. Adding to available list.%n", this.ID, robot.getID());
            
            // --- THIS IS WHERE ROBOTS ARE ADDED TO THE AVAILABLE LIST ---
            availableRobots.add(robot); 
            // -----------------------------------------------------------
            
            dispatchPendingTasks(); // Check if this newly-available robot can take a pending task
            return;
        }

        // 4. (Robot just finished a work task, e.g., Order)
        // Try to assign a new pending task *immediately*
        if (tryAssignPendingTaskTo(robot)) {
            System.out.printf("[%s] Robot %s assigned a new task immediately.%n", this.ID, robot.getID());
            return; // Robot is busy again
        }

        // 5. No new task available. Robot must go to an idle spot (to clear work areas).
        System.out.printf("[%s] No pending tasks for Robot %s. Sending to idle spot.%n", this.ID, robot.getID());
        HoldingStation freeSpot = findClosestAvailableHoldingStation(robot.getCurrentPosition());
        
        if (freeSpot != null) {
            // 5a. Holding slot available
            System.out.printf("[%s] Sending Robot %s to Holding Spot %s.%n", this.ID, robot.getID(), freeSpot.getID());
            freeSpot.tryAcquire(); // Reserve the slot
            //TODO
            robot.addTask(new GotoHoldingTask(freeSpot)); 
        } else {
            // 5b. No holding slot available (fallback)
            System.out.printf("[%s] All Holding Spots are full. Sending Robot %s to Starting Point.%n", this.ID, robot.getID());
            robot.addTask(new GotoStartTask(robot.getStartingPosition())); 
        }
    }
    
    /**
     * Entry Point 3: Callback from a Robot when it *starts* a new task.
     * This is crucial for releasing idle spots.
     */
    public synchronized void reportRobotLeavingIdle(Robot robot, HoldingStation station) {
        if (station != null) {
            System.out.printf("[%s] Robot %s is leaving Holding Spot %s.%n", this.ID, robot.getID(), station.getID());
            station.release(); // The slot is now free
        }
    }

    /**
     * The "Brain" 🧠. Scans the pending task queue and tries to match
     * tasks with available Robots and Stations.
     * This method is *private* and *not* synchronized,
     * as it's only called from within synchronized public methods.
     */
    private void dispatchPendingTasks() {
        if (availableRobots.isEmpty() || pendingTasks.isEmpty()) {
            return; // No resources or no work
        }

        System.out.printf("[%s] Brain: Scanning %d tasks for %d available robots...%n", this.ID, pendingTasks.size(), availableRobots.size());
        Iterator<Task> iterator = pendingTasks.iterator();

        while (iterator.hasNext()) {
            if (availableRobots.isEmpty()) {
                System.out.printf("[%s] Brain: No more available robots. Stopping scan.%n", this.ID);
                break; // Stop if we run out of robots
            }
            
            Task task = iterator.next();
            
            // Check if the task is an Order
            if (task instanceof Order) {
                Order orderTask = (Order) task;
                
                // 1. Find the best *available* packing station for this task
                //    (Optimal logic: closest to the *pickup* location)
                PackingStation bestStation = findClosestAvailableStation(orderTask.getItemPosition());
                
                if (bestStation == null) {
                    // No stations available for this task.
                    // Continue scanning queue for other tasks (e.g., ScanTask)
                    continue; 
                }

                // 2. Find the best *available* robot for this task
                //    (Optimal logic: closest to the *pickup* location)
                Robot bestRobot = findClosestAvailableRobot(orderTask.getItemPosition());
                
                if (bestRobot == null) {
                     // This means no robot is available, which contradicts the check at the top.
                     // But for safety, we stop.
                    break; 
                }

                // 3. SUCCESS: We found a match!
                System.out.printf("[%s] Brain: Match found! Task %s -> Robot %s -> Station %s.%n", 
                                  this.ID, task.getID(), bestRobot.getID(), bestStation.getID());
                                   
                // Lock resources
                iterator.remove(); // Remove task from pending queue
                availableRobots.remove(bestRobot); // Robot is no longer available
               
                // Dispatch!
                bestRobot.addTask(task);
            } 
            // (else if (task instanceof ScanTask) { ... })
        }
    }
        
     // --- HELPER METHODS (Private) ---

    /**
     * Releases resources (Packing/Charging stations) when a task is finished.
     */
    private void releaseResourcesFrom(Robot robot, Task finishedTask) {
        if (finishedTask instanceof Order) {
            PackingStation station = ((Order) finishedTask).getReservedStation();
            if (station != null) {
                System.out.printf("[%s] Releasing Packing Station %s.%n", this.ID, station.getID());
                station.release();
            }
        } else if (finishedTask instanceof ChargeTask) {
            ChargingStation station = ((ChargeTask) finishedTask).getReservedStation();
            if (station != null) {
                System.out.printf("[%s] Releasing Charging Station %s.%n", this.ID, station.getID());
                station.release();
            }
        }
        // Holding spots are released when *leaving*, not when finishing a task.
    }

    /**
     * Tries to assign a pending task to a *specific* robot that just finished a job.
     * This is an optimization to reduce travel to/from holding spots.
     * @return true if a task was successfully assigned.
     */
    private boolean tryAssignPendingTaskTo(Robot robot) {
        // This is a complex optimization (a "mini-dispatch").
        // For simplicity, we return false to force the robot to go to an idle spot first.
        // The main dispatchPendingTasks() will handle it.
        return false;
    }
         
    /**
     * Finds the closest *available* Packing Station to a target location.
     * @return A station, or null if all are busy.
     */
    private PackingStation findClosestAvailableStation(Position target) {
        PackingStation bestStation = null;
        double minDistance = Double.MAX_VALUE;

        for (PackingStation station : allPackingStations) {
            if (station.isAvailable()) { // Only check available stations
                double distance = Position.distance(station.getPosition(), target);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestStation = station;
                }
            }
        }
        return bestStation;
    }

    /**
     * Finds the closest *available* Robot to a target location.
     * @return A robot, or null if all are busy.
     */
    private Robot findClosestAvailableRobot(Position target) {
        Robot bestRobot = null;
        double minDistance = Double.MAX_VALUE;

        // Note: We iterate over 'availableRobots', not 'allRobots'
        for (Robot robot : availableRobots) { 
        	double distance = Position.distance(robot.getCurrentPosition(), target);
            if (distance < minDistance) {
                minDistance = distance;
                bestRobot = robot;
            }
        }
        return bestRobot;
    }
    
    /**
     * Finds *any* available Holding Station.
     * @return A station, or null if all are full.
     */
    private HoldingStation findAvailableHoldingStation() {
        // (Could be optimized to find closest)
        for (HoldingStation station : allHoldingStations) {
            if (station.isAvailable()) {
                return station;
            }
        }
        return null;
    }
    
    /**
     * Finds the closest *available* Charging Station to a robot.
     * @return A station, or null if all are busy.
     */
    private ChargingStation findClosestAvailableChargeStation(Position robotLocation) {
        ChargingStation bestStation = null;
        double minDistance = Double.MAX_VALUE;

        for (ChargingStation station : allChargingStations) {
            if (station.isAvailable()) { // Only check available stations
            	double distance = Position.distance(station.getPosition(), robotLocation);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestStation = station;
                }
            }
        }
        return bestStation;
    }
    
    /**
     * Creates and dispatches a ChargeTask to a specific robot.
     * @return true if a station was found and the task was dispatched.
     */
    private boolean dispatchChargeTask(Robot robot) {
        ChargingStation freeStation = findClosestAvailableChargeStation(robot.getCurrentPosition());
        
        if (freeStation != null) {
            System.out.printf("[%s] Sending Robot %s to Charge Station %s.%n", this.ID, robot.getID(), freeStation.getID());
            freeStation.tryAcquire(); // Reserve the charging spot
            //TODO
//            ChargeTask chargeTask = new ChargeTask(freeStation);
            // We need to set the reserved station on the task so it can be released later
//            chargeTask.setReservedStation(freeStation); 
//            robot.assignTask(chargeTask);
            return true;
        } else {
            return false; // No stations available
        }
    }
    
    /**
     * Finds the closest *available* Holding Station to a target location.
     * @param target The location to measure distance from (usually the robot's current location).
     * @return A station, or null if all are full.
     */
    private HoldingStation findClosestAvailableHoldingStation(Position target) {
        HoldingStation bestStation = null;
        double minDistance = Double.MAX_VALUE;

        for (HoldingStation station : allHoldingStations) {
            // Check if the station has an available slot
            if (station.isAvailable()) { 
            	double distance = Position.distance(station.getPosition(), target);
                // If this station is closer than the previous best, update it
                if (distance < minDistance) {
                    minDistance = distance;
                    bestStation = station;
                }
            }
        }
}