package taskManager;

import equipments.Robot;
import warehouse.WarehousePosition;

public class Stock implements Task {
    private final String itemName;
    private final WarehousePosition unloadingArea;
    private final WarehousePosition shelfLocation;
    //ADD Quantity
    private boolean completed = false;

    public Stock(String itemName, WarehousePosition unloadingArea, WarehousePosition shelfLocation) {
        this.itemName = itemName;
        this.unloadingArea = unloadingArea;
        this.shelfLocation = shelfLocation;
    }

    @Override
    public void execute(Robot robot) {
        robot.setStatus("Stocking item: " + itemName);
        System.out.println(robot.getName() + " is now " + robot.getStatus());

        robot.moveTo(unloadingArea);
        System.out.println(robot.getName() + " picking up stock for " + itemName);
        try {
            Thread.sleep(2000); // simulate pickup
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        robot.moveTo(shelfLocation);
        System.out.println(robot.getName() + " stocked " + itemName + " on shelf at " + shelfLocation);
        robot.setStatus("Idle");
        completed = true;
    }

    @Override
    public String getDescription() {
        return "StockItem[" + itemName + " from unloading area to " + shelfLocation + "]";
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }
}
