package equipmentManager;

import taskManager.FindChargeTimeoutException;
import taskManager.Task;
import warehouse.WahouseObjectType;
import warehouse.WarehouseObject;

import java.awt.Point;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Robot worker that runs on its own thread.
 * It fetches tasks from an internal queue, executes them,
 * and reports completion back to the EquipmentManager.
 * * This version does NOT use HoldingStations. Its only idle spot
 * is its StartingPosition.
 */
public class Robot extends WarehouseObject implements Runnable {
    
    // --- Constants ---
    private static final int PICKING_TIME_MS = 50; 
    private static final int DROPPING_TIME_MS = 50;
    private static final int CHARGING_1_PERCENTAGE_TIME_MS = 10;
    private static final long MOVE_DELAY_PER_METER_MS = 250; // Speed simulation
    private static final double BATTERY_COSUMED_PER_METER = 1;
    private static final long IDLE_CHARGE_TIMEOUT_SECONDS = 30; // IDLE status timeout for charging
    private static final double FULL_BATTERY = 100;
    
	// --- State ---
	private double batteryPercentage = FULL_BATTERY;
    private RobotState state = RobotState.IDLE;
    private Point startingPosition; // The "home" base
    private Point currentPosition; // Current physical location
    
    // Internal "inbox" for tasks assigned by the EquipmentManager
    private final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();
    
    // Reference to the central manager to report completion and pass to tasks
    private EquipmentManager equipmentManager;

	/**
     * Creates a new Robot.
     * @param ID Unique identifier (e.g., "R-01").
     * @param startingPosition The "home" position to return to when idle.
     * @param manager A reference to the central EquipmentManager.
     */
    public Robot(String id, Point startingPosition, EquipmentManager manager, WahouseObjectType object_TYPE) {
        super(id, startingPosition.x, startingPosition.y, object_TYPE);
        this.startingPosition = startingPosition;
        this.currentPosition = startingPosition;
        this.equipmentManager = manager; // Store the reference to the "Brain"
    }

    /**
     * Called by EquipmentManager to assign a new task to this robot.
     * @param task The Task (script) for the robot to execute.
     */
    public void assignTask(Task task) {
        taskQueue.add(task);
        System.out.printf("[%s] Received task: %s %n", super.getId(), task.getDescription());
    }

    /**
     * The main run loop for the Robot's dedicated thread.
     */
    @Override
    public void run() {
    	System.out.printf("[%s] Robot thread started at its starting position (%d, %d)%n", super.getId(), currentPosition.x, currentPosition.y);
        Task currentTask = null; // Track the task being executed
        boolean taskStatus = false; // Report success/failure
        
        while (!Thread.currentThread().isInterrupted()) { // Infinite loop
            try {
                currentTask = null; // Reset task holder
                taskStatus = false; // Reset status

                // Waiting for tasks in IDLE_CHARGE_TIMEOUT_SECONDS, if not Robot go for charging if battery percentage is below 90%
                if (equipmentManager != null) { // Only wait if manager is set
                     System.out.printf("[%s] Is IDLE. Waiting for task (%d sec timeout)...%n", super.getId(), IDLE_CHARGE_TIMEOUT_SECONDS);
                    
                    // Wait for a task, with a timeout
                    currentTask = taskQueue.poll(IDLE_CHARGE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } else {
                    // Fallback if manager isn't set, wait forever
                    currentTask = taskQueue.take();
                }

                if (currentTask != null) {
                    // --- TASK RECEIVED ---
                    try {
                        // Execute the task
                        currentTask.execute(this, this.equipmentManager);
                        taskStatus = true; // Mark as success
                    // --- ADDED EXCEPTION CATCHING ---
                    } catch (FindChargeTimeoutException e) { 
                        taskStatus = false; // Mark as failure
                        System.err.printf("[%s] Task %s FAILED: %s%n", super.getId(), currentTask.getType(), e.getMessage());
                    } catch (Exception e) { // Catch unexpected bugs (like NullPointerException)
                        taskStatus = false;
                        System.err.printf("[%s] Task %s CRASHED (Unexpected): %s%n", super.getId(), currentTask.getType(), e.getMessage());
                        e.printStackTrace();
                    }
                
                } else {
                    // --- TIMEOUT (NO TASK) ---
                    // currentTask is null, so the IDLE_CHARGE_TIMEOUT_SECONDS timeout expired
                    System.out.printf("[%s] IDLE TIMEOUT (%d sec). Requesting charge...%n", super.getId(), IDLE_CHARGE_TIMEOUT_SECONDS);
                    
                    // Call the manager to handle this idle charge request
                    equipmentManager.idleRobotRequestsCharge(this);
                    
                    // The manager will assign a new task (ChargeTask or GoToWaitTask).
                    // We 'continue' the loop to go back and poll() that new task immediately.
                    continue; 
                }
            } catch (InterruptedException e) {
                // The poll() or a task's execute() was interrupted
                System.out.printf("[%s] Thread interrupted. Exiting run loop...%n", super.getId());
                Thread.currentThread().interrupt(); // Re-set the interrupt flag
                break; // Exit the while(true) loop
                
            } finally {
                // REPORT (Only if a task was actually processed)
                if (currentTask != null) {
                    // This block runs for both success (true) and failure (false)                    
                    try {
                        // Report the final status to the manager
                        equipmentManager.reportFinishedTask(this, currentTask, taskStatus);
                    } catch (InterruptedException e) {
                        System.err.printf("[%s] CRITICAL: Interrupted while reporting task!%n", super.getId());
                        Thread.currentThread().interrupt();
                    }
                }
                // If currentTask is null (from timeout), we *don't* report.
                // We just loop back to poll().
            }
        } // End of while(true)
        
        System.out.printf("[%s] Thread stopped.%n", super.getId());
    }

    // --- PRIMITIVE ACTIONS ---
    
    /**
     * Simulates moving the robot to a target position.
     * Updates battery and current position.
     * @param targetPosition The destination.
     * @throws InterruptedException if the thread is interrupted during sleep (movement).
     */
    public void moveTo(Point targetPosition) throws InterruptedException {    	
        double distance = targetPosition.distance(currentPosition);
        double consumed =  Math.ceil(distance * BATTERY_COSUMED_PER_METER); // Simple battery consumption model
        batteryPercentage = Math.max(0, batteryPercentage - consumed);
        long moveTimeMs = (long) (distance * MOVE_DELAY_PER_METER_MS);
                          
        if (moveTimeMs > 0) {                     
            Thread.sleep(moveTimeMs); // Simulate travel time
        }
        
        // Check if thread was interrupted during sleep
        if(Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("MoveTo interrupted for Robot " + super.getId());
        }

        this.currentPosition = targetPosition;
    }

    public void stepMove(List<Point> steps) throws InterruptedException {
    	// Optional: Log the start of the entire multi-step move
        System.out.printf("[%s] Starting multi-step path from (%d, %d). Following %d steps.%n",
        		          super.getId(), currentPosition.x, currentPosition.y, steps.size());
        
        this.state = RobotState.MOVING;
    	// Iterate through each Point (step) in the list
    	for (Point nextStep : steps) {
    		// Call the existing moveTo function for the next step.
    		// moveTo() will handle:
    		// 1. Calculating distance from currentPosition to nextStep
    		// 2. Consuming battery
    		// 3. Simulating travel time (Thread.sleep)
    		// 4. Checking for InterruptedException
    		// 5. Updating this.currentPosition to nextStep
    		moveTo(nextStep);
    	}

        // Optional: Log the completion of the entire path
        System.out.printf("[%s] Finished multi-step path. Final location: (%d, %d)%n",
        		          super.getId(), currentPosition.x, currentPosition.y);    	
    }
    
    /**
     * Simulates picking up an item.
     * @param itemId The ID of the item (for logging).
     * @throws InterruptedException if the thread is interrupted.
     */
    public void pickUpItem(String itemId) throws InterruptedException {
    	System.out.printf("[%s] Picking up item %s at (%d, %d)... %n", super.getId(), itemId, currentPosition.x, currentPosition.y);
    	this.state = RobotState.PICKING;
        Thread.sleep(PICKING_TIME_MS); 
        if(Thread.currentThread().isInterrupted()) throw new InterruptedException("PickUp interrupted");
        System.out.printf("[%s] Picked up item %s%n", super.getId(), itemId);
    }
    
    /**
     * Simulates dropping off an item.
     * @param itemId The ID of the item (for logging).
     * @throws InterruptedException if the thread is interrupted.
     */
    public void dropItem(String itemId) throws InterruptedException {
        System.out.printf("[%s] Dropping item %s at (%d, %d)...%n", super.getId(), itemId, currentPosition.x, currentPosition.y);
        this.state = RobotState.PACKING;
        Thread.sleep(DROPPING_TIME_MS); 
        if(Thread.currentThread().isInterrupted()) throw new InterruptedException("DropItem interrupted");
        System.out.printf("[%s] Dropped item %s%n", super.getId(), itemId);
    }
    
    /**
     * Simulates charging the battery to 100%.
     * Calculates the time needed based on the missing percentage.
     * @throws InterruptedException if the thread is interrupted during sleep (charging).
     */
    public void charge() throws InterruptedException {
        System.out.printf("[%s] Charging at (%d, %d) (Current: %.0f%%)... %n", super.getId(), currentPosition.x, currentPosition.y, batteryPercentage);
        
        // Calculate how much charge is needed
        double neededPercentage = 100.0 - batteryPercentage;
        
        // Check if charging is necessary
        if (neededPercentage <= 0) {
             System.out.printf("[%s] Already fully charged.%n", super.getId());
             return; // No need to charge
        }

        // Calculate total charging time
        long chargeTimeMs = (long) (CHARGING_1_PERCENTAGE_TIME_MS * neededPercentage);
        
        // Set robot state to charging
        this.state = RobotState.CHARGING;
        System.out.printf("[%s] Charging for %dms...%n", super.getId(), chargeTimeMs);
        
        // Simulate the charging time
        Thread.sleep(chargeTimeMs);
        
        // Check if thread was interrupted during sleep
        if(Thread.currentThread().isInterrupted()) {
             System.out.printf("[%s] Charging interrupted! Battery might not be full.%n", super.getId());
             // Propagate the interrupt to be caught by the run() loop
             throw new InterruptedException("Charge interrupted for Robot " + super.getId());
        }

        // If successful, set battery to 100%
        this.batteryPercentage = FULL_BATTERY;
        System.out.printf("[%s] Fully charged.%n", super.getId());
    }
    
    
    public void setEquipmentManager(EquipmentManager manager) {
        this.equipmentManager = manager;
    }
    
    public static double getBatteryCosumedPerMeter() {
		return BATTERY_COSUMED_PER_METER;
	}
    
    public double getBatteryPercentage() {
		return batteryPercentage;
	}

	public void setBatteryPercentage(double batteryPercentage) {
		this.batteryPercentage = batteryPercentage;
	}

	public String getState() {
		return state.toString();
	}

	public void setState(RobotState newState) {
		this.state = newState;
	}

	public Point getStartingPosition() {
		return startingPosition;
	}

	public void setStartingPosition(Point startingPosition) {
		this.startingPosition = startingPosition;
	}

	public Point getCurrentPosition() {
		return currentPosition;
	}

	public Point getLocation() {return this.currentPosition;}

    @Override
    public String toString() {
        return String.format("Robot [ID=%s, state=%s, startingPosition=%s, %s, currentPosition=%s, %s]",
        		super.getId(), state, startingPosition.x, startingPosition.y, currentPosition.x, currentPosition.y);
    }
}