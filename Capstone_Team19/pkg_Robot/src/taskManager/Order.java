package taskManager;

import equipments.Robot;
import equipments.PackingStation;

import java.awt.Point;

import equipments.EquipmentManager; 

/**
 * Represents a task to pick an item and deliver it to a packing station.
 * The packing station is requested *during* execution (Just-in-Time).
 */
public class Order implements Task {
	private final String orderId;
    private final String itemName;
    private final Point itemLocation;
    private final int quantity;
    private final TaskType taskType = TaskType.PICK_ORDER;
    
    // No reservedStation field needed here anymore.

    public Order(String orderId, String itemName, Point itemLocation, int quantity) {
		super();
		this.orderId = orderId;
		this.itemName = itemName;
		this.itemLocation = itemLocation;
		this.quantity = quantity;
	}

	/**
     * Execution script for the Order task.
     * @param robot The Robot performing the task.
     * @param manager The EquipmentManager to request resources from.
     * @throws InterruptedException If the robot is interrupted.
     */
    @Override
    public void execute(Robot robot, EquipmentManager manager) throws InterruptedException { 
    	System.out.printf("[%s] Executing %s (%s)%n", robot.getID(), this.orderId, this.itemName);

        // 1. Go to item location
        robot.moveTo(itemLocation);

        // 2. Pick up item
        robot.pickUpItem(itemName); // Using Order ID as Item ID for simplicity

        // 3. --- JUST-IN-TIME STATION REQUEST ---
        System.out.printf("[%s] Item picked up. Requesting available Packing Station...%n", robot.getID());
        
        // This call might block the robot's thread if all stations are busy
        PackingStation assignedStation = manager.requestAvailablePackingStation(robot); 
        
        // Check if interrupted while waiting (request might return null if interrupted)
        if (assignedStation == null) {
             System.out.printf("[%s] Interrupted while waiting for Packing Station for Order %s. Aborting.%n", robot.getID(), this.itemName);
             // Optionally, robot could try to return the item or go to a safe spot.
             // For now, we just let the task end here. The finally block in Robot.run() will report.
             return; 
        }
        
        System.out.printf("[%s] Assigned Packing Station %s. Moving to drop off...%n", 
                          robot.getID(), assignedStation.getID());

        // 4. Go to the assigned station
        robot.moveTo(assignedStation.getLocation());

        // 5. Drop the item
        robot.dropItem(this.itemName);

        System.out.printf("[%s] Completed Order at %s%n", robot.getID(), assignedStation.getID());
        // DO NOT report completion here. Robot's run() loop handles that.
    }

    public String getItemName() {
		return itemName;
	}

	public Point getItemLocation() {
		return itemLocation;
	}

	public TaskType getTaskType() {
		return taskType;
	}
    
	@Override
	public String getID() {
		return this.orderId;
	}

	@Override
	public String getDescription() {
		return "Pick " + this.quantity + "x " + this.itemName + " [ID: " + this.orderId + "]";
	}

	@Override
	public TaskType getType() {
		return taskType;
	}

	@Override
	public String toString() {
		return "Task [taskType=" + taskType + ", " + "itemName=" + itemName + "]";
	}
	
	
}