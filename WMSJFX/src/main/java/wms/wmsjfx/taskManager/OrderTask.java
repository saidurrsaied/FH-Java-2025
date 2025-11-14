package taskManager;

import equipmentManager.Robot;
import equipmentManager.RobotState;
import warehouse.PackingStation;
import warehouse.WarehouseManager;

import java.awt.Point;
import java.util.List;

import equipmentManager.EquipmentManager; 

/**
 * Represents a task to pick an item and deliver it to a packing station.
 * The packing station is requested *during* execution (Just-in-Time).
 */
public class OrderTask implements Task {
	private final String orderId;
    private final String productID;
    private final Point itemLocation;
    private final int quantity;
    private final TaskType taskType = TaskType.PICK_ORDER;
    private final WarehouseManager warehouseManager;
    
    public OrderTask(String orderId, String productID, int quantity, WarehouseManager warehouseManager) throws OrderTaskException {
		super();
		this.orderId = orderId;
		this.productID = productID;
		this.quantity = quantity;
		this.warehouseManager = warehouseManager;
		this.itemLocation = warehouseManager.getProductLocationByProductID(productID);
		
        // ========== VALIDATION ==========
	    if (orderId == null || orderId.isBlank() || productID == null || productID.isBlank()) {
	        throw new OrderTaskException("Order ID and productID cannot be null or blank");
	    }

	    if (quantity <= 0) {
	        throw new OrderTaskException("Quantity must be greater than zero");
	    }
	    
	    if (quantity > warehouseManager.getProductQuantity(productID)) {
	        throw new OrderTaskException("Quantity is not enough");
	    }
	}

	/**
     * Execution script for the Order task.
     * @param robot The Robot performing the task.
     * @param manager The EquipmentManager to request resources from.
     * @throws InterruptedException If the robot is interrupted.
     */
    @Override
	public void execute(Robot robot, EquipmentManager manager) throws InterruptedException { 
		System.out.printf("[robot][%s] Executing %s (%s)%n", robot.getId(), this.orderId, this.productID);
		
        // 1. Go to item location
		List<Point> steps = manager.requestPath(robot, itemLocation);
		robot.stepMove(steps);
		
        // 2. Pick up item
        robot.pickUpItem(productID); // Using Order ID as Item ID for simplicity

        // 3. --- JUST-IN-TIME STATION REQUEST ---
		System.out.printf("[robot][%s] Item picked up. Requesting available Packing Station...%n", robot.getId());
        
        // This call might block the robot's thread if all stations are busy
        robot.setState(RobotState.WAITING_FOR_AVAILABLE_PACKING_STATION);
        PackingStation assignedStation = manager.requestAvailablePackingStation(robot); 
        
        // Check if interrupted while waiting (request might return null if interrupted)
	 	   if (assignedStation == null) {
			   System.err.printf("[robot][%s] Interrupted while waiting for Packing LoadingStation for Order %s. Aborting.%n", robot.getId(), this.productID);
             // Optionally, robot could try to return the item or go to a safe spot.
             // For now, we just let the task end here. The finally block in Robot.run() will report.
        }
        
		System.out.printf("[robot][%s] Assigned Packing Station %s. Moving to drop off...%n",
                          robot.getId(), assignedStation.getId());

        // 4. Go to the assigned station
        steps = manager.requestPath(robot, assignedStation.getLocation());
		robot.stepMove(steps);
		
        // 5. Drop the item
        robot.dropItem(this.productID);

		System.out.printf("[robot][%s] Completed Order at %s%n", robot.getId(), assignedStation.getId());
        
        // 6. Release packing station
        manager.releasePackingStation(assignedStation);
        
        // 7. Reduce the quantity of ordered product
        warehouseManager.decreaseProductQuantity(productID, quantity);
		System.out.printf("[inventory][%s] Decrease product. Now quantity for %s is %d%n", robot.getId(), productID, warehouseManager.getProductQuantity(productID));
        
        // DO NOT report completion here. Robot's run() loop handles that.
    }

    public String getItemName() {
		return productID;
	}

	public Point getItemLocation() {
		return itemLocation;
	}
    
	@Override
	public String getID() {
		return this.orderId;
	}

	@Override
	public String getDescription() {
		return "Pick " + this.quantity + "x " + this.productID + " [ID: " + this.orderId + "]";
	}

	@Override
	public TaskType getType() {
		return taskType;
	}

	@Override
	public String toString() {
		return "Task [taskType=" + taskType + ", " + "itemName=" + productID + "]";
	}
	
	
}