package taskManager;

import java.awt.Point;

import equipmentManager.EquipmentManager;
import equipmentManager.Robot;
//import warehouse.WarehousePosition;

public class StockTask implements Task {
	private final String stockId;
    private final String itemName;
    private final Point unloadingArea;
    private final Point shelfLocation;
    
    private final TaskType taskType = TaskType.STOCK_ITEM;

    public StockTask(String stockId, String itemName, Point unloadingArea, Point shelfLocation) {
    	super();
    	this.stockId = stockId;
        this.itemName = itemName;
        this.unloadingArea = unloadingArea;
        this.shelfLocation = shelfLocation;
    }

    @Override
    public void execute(Robot robot, EquipmentManager manager) throws InterruptedException {
        System.out.printf("[%s] Executing %s (%s)%n", robot.getID(), this.stockId, this.itemName);
        
        // 1. Go to item location
        robot.moveTo(new Point(unloadingArea.x, unloadingArea.y));
        
        // 2. Pick item
        robot.pickUpItem(itemName);

        // 3. Move to shelf location
        robot.moveTo(new Point(shelfLocation.x, shelfLocation.y));
        
        // 4. Drop item at shelf
        robot.dropItem(itemName);
            
        System.out.printf("[%s] Stocked %s at shelf %s%n", robot.getID(), this.itemName, this.shelfLocation);

    }

    @Override
    public String getDescription() {
        return "StockItem[" + itemName + " from " + unloadingArea + " to " + shelfLocation + "]";
    }

	@Override
	public String getID() {
		return this.stockId;
	}

	@Override
	public TaskType getType() {
		return this.taskType;
	}
    
    @Override
	public String toString() {
		return "Task [taskType=" + taskType + ", " + "itemName=" + itemName + "]";
	}
}