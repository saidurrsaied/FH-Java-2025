package wms.wmsjfx.equipmentManager; // Assuming this is where EM resides

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import wms.wmsjfx.pathFinding.PathFinding;
import wms.wmsjfx.taskManager.*; // Import Task and specific task types
import wms.wmsjfx.warehouse.PackingStation;
import wms.wmsjfx.warehouse.WarehouseManager;
import wms.wmsjfx.logger.Logger;

/**
 * Central Brain using "Just-in-Time Locking".
 * Dispatches tasks based *only* on robot availability initially.
 * Robots request stations *during* task execution.
 * Uses Lock and Condition for safer waiting than synchronized/wait/notify.
 */

public class EquipmentManager implements Runnable {
    // --- Constants ---
    private static final int LOW_BATTERY_PERCENT = 30;        // <30% means low battery, robot needs to be charged
    private static final int HIGH_BATTERY_PERCENT = 70;       // >90% means high battery, no need to charge more
    private static final double ENERGY_UNAVAILABLE = -1.0;    // Sentinel for path/energy calculation failure

    private final String ID = "Equipment Manager";

    // --- Full Resource Lists ---
    private final List<Robot> availableRobots; // Robots currently idle at their start positions
    private final List<PackingStation> allPackingStations;
    private final List<ChargingStation> allChargingStations;

    // --- Queues ---
    private final Queue<Task> pendingPickTasks = new ArrayDeque<>(); // Pending work
    private final BlockingQueue<Task> taskSubmissionQueue; // Incoming new task
    private final BlockingQueue<Robot> robotsWaitingForCharge = new LinkedBlockingQueue<>();

    // Queues for available (free) stations
    private LinkedBlockingQueue<ChargingStation> availableChargeStations;
    private LinkedBlockingQueue<PackingStation> availablePackStations;

    // Thread tracking for graceful shutdown (minimal addition)
    private volatile boolean stopping = false;
    private final List<Thread> robotThreads = new ArrayList<>();
    private volatile Thread dispatcherThread; // optional: register by caller

    // --- Other utilities  ---
    private final PathFinding pathFinding;
    Logger logger = new Logger();

    public EquipmentManager(WarehouseManager warehouseManager,
                            BlockingQueue<Task> taskSubmissionQueue,
                            PathFinding pathFinding) {
        this.availableRobots = warehouseManager.getAllRobots();
        this.allChargingStations = warehouseManager.getAllChargingStations();
        this.allPackingStations = warehouseManager.getAllPackingStations();
        this.taskSubmissionQueue = taskSubmissionQueue;
        this.pathFinding = pathFinding;

        // Initialize the free station queues from the master lists
        this.availableChargeStations = new LinkedBlockingQueue<>(this.allChargingStations);
        this.availablePackStations = new LinkedBlockingQueue<>(this.allPackingStations);
        System.out.println(this.availableRobots);

        for (Robot r : availableRobots) {
            r.setEquipmentManager(this);
        }

        // Start robot threads and track them for later interruption
        for (Robot r : availableRobots) {
            Thread t = new Thread(r, "Robot-" + r.getId());
            t.start();
            robotThreads.add(t);
        }
    }

    public List<Robot> getRobot(){
        return this.availableRobots;
    }
    @Override
    public void run() {
        // If someone runs EM as a dispatcher in a thread, allow stop() to find it
        if (dispatcherThread == null) {
            dispatcherThread = Thread.currentThread();
        }
        logger.log_print("info", "equipment_manager", " Dispatcher started.");

        while (!Thread.currentThread().isInterrupted() && !stopping) {
            try {
                // Original blocking behavior: wait until a task arrives
                Task newTask = taskSubmissionQueue.take();

                synchronized (this) {
                    switch (newTask.getType()) {
                        case PICK_ORDER -> {
                            Point itemLocation = ((OrderTask) newTask).getItemLocation();
                            Robot foundRobot = findClosestAvailableRobot(itemLocation);
                            if (foundRobot == null) {
                                pendingPickTasks.offer(newTask);
                                logger.log_print("info", "equipment_manager", " No available robot for " + newTask + " → Pending (" + pendingPickTasks.size() + ")");
                            } else {
                                logger.log_print("info", "equipment_manager", " DISPATCH " + newTask + " -> " + foundRobot.getId());
                                availableRobots.remove(foundRobot);
                                foundRobot.assignTask(newTask);
                            }
                        }
                        case STOCK_ITEM -> {
                            StockTask stockTask = (StockTask) newTask;
                            Robot foundRobot = findAvailableRobotForStocking(stockTask);
                            if (foundRobot == null) {
                                pendingPickTasks.offer(newTask);
                                logger.log_print("info", "equipment_manager", " No available robot for " + newTask + " → Pending (" + pendingPickTasks.size() + ")");
                            } else {
                                logger.log_print("info", "equipment_manager", " DISPATCH " + newTask + " -> " + foundRobot.getId());
                                availableRobots.remove(foundRobot);
                                foundRobot.assignTask(newTask);
                            }
                        }
                        default -> logger.log_print("error", "equipment_manager", " Unknown task type received: " + newTask.getType());
                    }
                }
            } catch (InterruptedException e) {
                logger.log_print("info", "equipment_manager", " Interrupt received. Stopping dispatcher...");
                Thread.currentThread().interrupt();
                break;
            }
        }
        logger.log_print("info", "equipment_manager", " Dispatcher stopped.");
    }

    /**
     * Register the dispatcher thread so stop() can interrupt it later.
     * This is optional; if not set, it will be auto-set when run() starts.
     */
    public void registerDispatcherThread(Thread t) {
        this.dispatcherThread = t;
    }

    /**
     * Gracefully stop: interrupt dispatcher and all robot threads and await termination.
     * Minimal, non-breaking addition for JavaFX Application.stop() to call.
     */
    public void stop(long timeoutMs) throws InterruptedException {
        stopping = true;
        // Interrupt dispatcher if known
        Thread dt = this.dispatcherThread;
        if (dt != null) {
            dt.interrupt();
        }
        // Interrupt robots
        for (Thread t : robotThreads) {
            t.interrupt();
        }

        long deadline = System.currentTimeMillis() + Math.max(0, timeoutMs);
        // Join dispatcher
        if (dt != null) {
            long remaining = Math.max(0, deadline - System.currentTimeMillis());
            if (remaining > 0) dt.join(remaining);
        }
        // Join robots
        for (Thread t : robotThreads) {
            long remaining = Math.max(0, deadline - System.currentTimeMillis());
            if (remaining <= 0) break;
            try {
                t.join(remaining);
            } catch (InterruptedException ie) {
                // Preserve interrupt and stop waiting further
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Testing/monitoring helper: returns number of alive robot threads tracked by EM.
     */
    public int getAliveRobotThreadCount() {
        int alive = 0;
        for (Thread t : robotThreads) {
            if (t.isAlive()) alive++;
        }
        return alive;
    }

    /**
     * (Public) Callback from a Robot thread when it finishes a task.
     * This is a main entry point and MUST be thread-safe (synchronized).
     */
    public synchronized void reportFinishedTask(Robot robot, Task finishedTask, boolean taskStatus) throws InterruptedException {
        logger.log_print("info", "equipment_manager", " Robot " + robot.getId() + " finished task " + finishedTask.getID() + ".");

        // Priority 1: Always try to assign a pending task first.
        // (This function already checks battery, so it's safe)
        boolean assignedPending = tryAssignPendingTaskTo(robot);

        if (assignedPending) {
            // Robot received a new task and is busy. Our work here is done.
            return;
        }

        // --- No available pending task, decide what to do next ---
        switch (finishedTask.getType()) {
            case PICK_ORDER, STOCK_ITEM, GO_TO_START -> {
                if (robot.getBatteryPercentage() < LOW_BATTERY_PERCENT) {
                    ChargingStation freeChargingStation = requestAvailableChargingStation(0);
                    if (freeChargingStation != null) {
                        freeChargingStation.setState(ObjectState.BUSY);
                        logger.log_print("info", "equipment_manager", " Assign charging station " + freeChargingStation.getId() + " to robot " + robot.getId() + ".");
                        robot.assignTask(new ChargeTask(freeChargingStation, robot.getId()));
                    } else {
                        ChargingStation closest = findClosestChargingStation(robot.getCurrentPosition());
                        if (closest != null) {
                            logger.log_print("info", "equipment_manager", " " + robot.getId() + " battery low. All stations busy. Assigning 'GoToChargingStationAndWaitTask' (Target: " + closest.getId() + ")");
                            robot.assignTask(new GoToChargingStationAndWaitTask(closest));
                        } else {
                            logger.log_print("error", "equipment_manager", " No charging stations! Robot " + robot.getId() + " stuck!");
                            availableRobots.add(robot);
                        }
                    }
                } else {
                    if (finishedTask.getType() == TaskType.GO_TO_START) {
                        logger.log_print("info", "equipment_manager", " Robot " + robot.getId() + " is at Start Point. Now IDLE.");
                        if (!availableRobots.contains(robot)) {
                            availableRobots.add(robot);
                            robot.setState(RobotState.IDLE);
                        }
                    } else {
                        logger.log_print("info", "equipment_manager", "No pending tasks. Sending " + robot.getId() + " to Start Point.");
                        robot.assignTask(new GoToStartTask(robot.getStartingPosition()));
                    }
                }
            }
            case CHARGE_ROBOT -> {
                logger.log_print("info", "equipment_manager", " Robot " + robot.getId() + " finished charging. Sending to Start Point.");
                robot.assignTask(new GoToStartTask(robot.getStartingPosition()));
            }
            case GO_TO_CHARGING_STATION_AND_WAIT -> {
                logger.log_print("info", "equipment_manager", " Robot " + robot.getId() + " finished 'GoToWait'.");
                if (taskStatus) {
                    logger.log_print("info", "equipment_manager", " Robot " + robot.getId() + " is fully charged.");
                } else {
                    logger.log_print("info", "equipment_manager", " Robot " + robot.getId() + " cannot be charged, battery now is " + robot.getBatteryPercentage());
                    robotsWaitingForCharge.add(robot);
                }
                robot.assignTask(new GoToStartTask(robot.getStartingPosition()));
            }
            default -> availableRobots.add(robot);
        }
    }

    /**
     * NEW METHOD: Called by a robot that timed out (15 min) while idle.
     * Must be synchronized because it conflicts with 'reportFinishedTask'
     * and 'run' (when accessing 'availableRobots').
     */
    public synchronized void idleRobotRequestsCharge(Robot robot) {
        logger.log_print("info", "equipment_manager", " " + robot.getId() + " (IDLE) requested charge due to timeout.");

        // Remove the robot from the available list to prevent race conditions
        availableRobots.remove(robot);

        // Check if battery actually needs charging
        if (robot.getBatteryPercentage() >= HIGH_BATTERY_PERCENT) {
            logger.log_print("info", "equipment_manager", " " + robot.getId() + " requested charge but is already full. Returning to IDLE.");
            availableRobots.add(robot); // Add it back
            return; // Do nothing
        }

        // Battery is not full -> Assign a charge task
        ChargingStation freeStation = availableChargeStations.poll();
        if (freeStation != null) {
            // A free station is available
            freeStation.setState(ObjectState.BUSY);
            logger.log_print("info", "equipment_manager", " Found free station " + freeStation.getId() + ". Assigning ChargeTask.");
            robot.assignTask(new ChargeTask(freeStation, robot.getId())); // (Modify ChargeTask constructor if needed)
        } else {
            // No free stations
            ChargingStation closest = findClosestChargingStation(robot.getCurrentPosition());
            if (closest != null) {
                logger.log_print("info", "equipment_manager", " All stations busy. Assigning GoToChargingStationAndWaitTask.");
                robot.assignTask(new GoToChargingStationAndWaitTask(closest));
            } else {
                logger.log_print("error", "equipment_manager", " No charging stations! Robot " + robot.getId() + " stuck!");
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
        logger.log_print("info", "equipment_manager", " Found available Packing Station " + availableStation.getId() + " for Robot " + robot.getId() + ".");
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
            logger.log_print("info", "equipment_manager", " No packing station found – cannot evaluate energy.");
            return ENERGY_UNAVAILABLE;
        }
        Point packPos = farthestPackStation.getLocation();

        // Find the FARTHEST charging station from that packing station
        ChargingStation farthestChargeStation = findFarthestChargingStation(packPos);
        if (farthestChargeStation == null) {
            logger.log_print("info", "equipment_manager", " No charging station found – cannot evaluate energy.");
            return ENERGY_UNAVAILABLE;
        }
        Point chargePos = farthestChargeStation.getLocation();

        // CALCULATE PATHS USING A-STAR
        List<Point> pathToPick = pathFinding.findPath(robotPos, orderPosition);
        List<Point> pathToPack = pathFinding.findPath(orderPosition, packPos);
        List<Point> pathToCharge = pathFinding.findPath(packPos, chargePos);

        if (pathToPick.isEmpty() || pathToPack.isEmpty() || pathToCharge.isEmpty()) {
            logger.log_print("info", "equipment_manager", " Cannot find a valid path for task – energy unavailable.");
            return ENERGY_UNAVAILABLE;
        }

        // Use geometric distance along each path instead of raw step count
        double distToPick = computePathDistance(robotPos, pathToPick);
        double distToPack = computePathDistance(orderPosition, pathToPack);
        double distToCharge = computePathDistance(packPos, pathToCharge);
        double totalDistance = distToPick + distToPack + distToCharge;

        double energyForTravel = totalDistance * Robot.getBatteryCosumedPerMeter();

//        System.out.printf("[%s] Energy calculation for robot %s (Dist: %.2f * Cost: %.2f) = Total: %.2f",
//            ID, robot.getId(), totalDistance, Robot.getBatteryCosumedPerMeter(), energyForTravel);
        logger.log_print("info", "equipment_manager", String.format("[%s] Energy calc for %s (Dist: %.2f) = Total: %.2f",
                ID, robot.getId(), totalDistance, energyForTravel));
        return energyForTravel;
    }

    /**
     * Calculates the worst-case energy required for a robot to complete an stock task.
     * Path: Robot -> Stock location -> Shelf Location -> Farthest_Charge
     */
    private double calculateRequiredEnergyForStock(Robot robot, StockTask stockTask) {
        Point robotPos = robot.getCurrentPosition();

        // Get Stock Position and Shelf Location
        Point loadingPos = stockTask.getLoadingStationLocation();
        Point shelfPos = stockTask.getShelfLocation();

        // Find the farthest charging station (Worst case)
        ChargingStation farthestChargeStation = findFarthestChargingStation(shelfPos);
        if (farthestChargeStation == null) {
            logger.log_print("info", "equipment_manager", " No charging station found – cannot evaluate stock energy.");
            return ENERGY_UNAVAILABLE;
        }
        Point chargePos = farthestChargeStation.getLocation();

        // Calculate distance based on A*
        List<Point> pathToLoad = pathFinding.findPath(robotPos, loadingPos);
        List<Point> pathToShelf = pathFinding.findPath(loadingPos, shelfPos);
        List<Point> pathToCharge = pathFinding.findPath(shelfPos, chargePos);

        if (pathToLoad.isEmpty() || pathToShelf.isEmpty() || pathToCharge.isEmpty()) {
            logger.log_print("info", "equipment_manager", " Cannot find a valid path for StockTask – energy unavailable.");
            return ENERGY_UNAVAILABLE;
        }

        double totalDistance = computePathDistance(robotPos, pathToLoad) +
                computePathDistance(loadingPos, pathToShelf) +
                computePathDistance(shelfPos, pathToCharge);

        double energyForTravel = totalDistance * Robot.getBatteryCosumedPerMeter();

        logger.log_print("info", "equipment_manager", String.format("[%s] Energy calc (Stock) for %s (Dist: %.2f) = Total: %.2f",
                ID, robot.getId(), totalDistance, energyForTravel));

        return energyForTravel;
    }

    /**
     * Computes Euclidean distance traveled when following a path list of points, starting from startPos.
     */
    private double computePathDistance(Point startPos, List<Point> path) {
        if (path == null || path.isEmpty()) return 0.0;
        double d = 0.0;
        Point prev = startPos;
        for (Point step : path) {
            d += prev.distance(step);
            prev = step;
        }
        return d;
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
            logger.log_print("error", "equipment_manager", " Attempted to release a null charging station.");
            return;
        }
        Robot waitingRobot = robotsWaitingForCharge.poll();

        if (waitingRobot != null) {
            waitingRobot.assignTask(new ChargeTask(station, waitingRobot.getId()));
        } else {
            try {
                // put() will add the station back to the queue.
                // If the queue is full (which shouldn't happen with LinkedBlockingQueue
                // unless you set a capacity), it will wait.
                availableChargeStations.put(station);
                station.setState(ObjectState.FREE);
                logger.log_print("info", "equipment_manager", String.format("Charging Station %s released back to queue. (Queue size: %d)",
                        station.getId(), availableChargeStations.size()));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Re-set the interrupt flag
                logger.log_print("error", "equipment_manager", String.format("Interrupted while trying to release station %s. Station may be lost!", station.getId()));
            }
        }

    }

    /**
     * (Public) Returns a station to the pool of available packing stations.
     * This is thread-safe.
     */
    public void releasePackingStation(PackingStation station) {
        if (station == null) {
            logger.log_print("error", "equipment_manager", " Attempted to release a null packing station.");
            return;
        }

        try {
            // put() will add the station back to the queue.
            availablePackStations.put(station);
            station.setState(ObjectState.FREE);
            logger.log_print("info", "equipment_manager", String.format("Packing Station %s released back to queue. (Queue size: %d)",
                    station.getId(), availablePackStations.size()));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Re-set the interrupt flag
            logger.log_print("error", "equipment_manager", String.format("Interrupted while trying to release station %s. Station may be lost!", station.getId()));
        }
    }

    /**
     * (Public) Return current pending tasks.
     * This is thread-safe.
     */
    public synchronized List<Task> getPendingTasks() {
        // We must synchronize on 'this' to safely access the pendingPickTasks deque,
        // matching the lock used by run() and reportFinishedTask().

        // Return a new ArrayList, which is a snapshot (copy) of the deque.
        return new ArrayList<>(pendingPickTasks);
    }

    /**
     * (Internal) Finds the best-matching pending task for a newly free robot.
     * Must be called while holding the 'synchronized (this)' lock.
     */
    private boolean tryAssignPendingTaskTo(Robot robot) {
        if (pendingPickTasks.isEmpty()) return false;
        double requiredBattery;
        for (Task task : pendingPickTasks) {
            TaskType type = task.getType();
            switch (type) {
                case PICK_ORDER -> {
                    OrderTask order = (OrderTask) task;
                    requiredBattery = calculateRequiredEnergy(robot, order.getItemLocation());
                }
                case STOCK_ITEM -> {
                    StockTask stock = (StockTask) task;
                    requiredBattery = calculateRequiredEnergyForStock(robot, stock);
                }
                default -> {
                    continue; // unsupported types not handled here
                }
            }
            if (requiredBattery == ENERGY_UNAVAILABLE) {
                logger.log_print("info", "equipment_manager", String.format("[%s] Skipping task %s – energy unavailable (path/station missing).", ID, task.getID()));
                continue;
            }
            if (robot.getBatteryPercentage() >= requiredBattery) {
                pendingPickTasks.remove(task);
                robot.assignTask(task);
                logger.log_print("info", "equipment_manager", String.format("[%s] Robot %s assigned pending task %s", this.ID, robot.getId(), task.getID()));
                return true;
            }
        }

        return false;
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
            double requiredBattery = calculateRequiredEnergy(robot, target);
            if (requiredBattery == ENERGY_UNAVAILABLE) {
                continue; // skip un-evaluable
            }
            if (robot.getBatteryPercentage() >= requiredBattery) {
                double distance = robot.getCurrentPosition().distance(target);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestRobot = robot;
                }
            } else {
                logger.log_print("info", "equipment_manager", String.format("Robot %s battery %.2f < needed %.2f", robot.getId(), robot.getBatteryPercentage(), requiredBattery));
            }
        }
        return bestRobot;
    }

    /**
     * (Internal) Finds the best-matching robot for a new task.
     * Must be called while holding the 'synchronized (this)' lock.
     */
    private Robot findAvailableRobotForStocking(StockTask stockTask) {
        if (availableRobots.isEmpty()) {
            return null;
        }

        Robot bestRobot = null;
        double minDistance = Double.MAX_VALUE;
        Point target = stockTask.getLoadingStationLocation();

        for (Robot robot : availableRobots) {
            double requiredBattery = calculateRequiredEnergyForStock(robot, stockTask);
            if (requiredBattery == ENERGY_UNAVAILABLE) {
                continue; // cannot evaluate, skip
            }
            if (robot.getBatteryPercentage() >= requiredBattery) {
                List<Point> path = pathFinding.findPath(robot.getCurrentPosition(), target);
                if (!path.isEmpty()) {
                    double distance = path.size();
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestRobot = robot;
                    }
                }
            } else {
                logger.log_print("info", "equipment_manager", String.format("[%s] Robot %s (%.2f%%) not enough battery for StockTask (needs %.2f%%)",
                        ID, robot.getId(), robot.getBatteryPercentage(), requiredBattery));
            }
        }
        return bestRobot;
    }

}