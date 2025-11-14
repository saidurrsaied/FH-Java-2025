package wms.wmsjfx.taskManager;

import wms.wmsjfx.equipmentManager.EquipmentManager;
import wms.wmsjfx.equipmentManager.Robot;
import wms.wmsjfx.warehouse.LoadingStation; // <-- Import
import wms.wmsjfx.warehouse.StorageShelf;
import wms.wmsjfx.warehouse.WarehouseManager;

import java.awt.Point;
import java.util.List;

public class StockTask implements Task {

    private final String stockId;
    private final String productID;
    private final LoadingStation loadingStation;
    private final Point shelfLocation;
    private final int quantity;
    private final WarehouseManager warehouseManager;
    private final TaskType taskType = TaskType.STOCK_ITEM;
    // ...

    public StockTask(String stockId,
                     String productID,
                     LoadingStation loadingStation,
                     int quantity,
                     WarehouseManager warehouseManager) throws OrderTaskException {
        // ... (Validation)
        this.stockId = stockId;
        this.productID = productID;
        this.quantity = quantity;
        this.loadingStation = loadingStation;
        this.warehouseManager = warehouseManager;
        this.shelfLocation = warehouseManager.getProductLocationByProductID(productID);
    }

    @Override
    public void execute(Robot robot, EquipmentManager manager) throws InterruptedException {
        System.out.printf("[%s] Executing %s. Moving to %s...%n",
                robot.getId(), this.stockId, this.loadingStation.getId());
        // 1. Go to Loading Station
        List<Point> stepsToLoading = manager.requestPath(robot, this.loadingStation.getLocation());
        robot.stepMove(stepsToLoading);

        // 2. Lock the loading station
        System.out.printf("[%s] Arrived at %s. Waiting for access...%n",
                robot.getId(), this.loadingStation.getId());

        try {
            // Robot is waiting until the loading station is free
            this.loadingStation.acquire();

            // 3. Pick the order
            System.out.printf("[%s] Acquired %s. Picking up item...%n",
                    robot.getId(), this.loadingStation.getId());
            robot.pickUpItem(this.productID);

        } finally {
            // 4. Return Lock
            System.out.printf("[%s] Releasing %s.%n",
                    robot.getId(), this.loadingStation.getId());
            this.loadingStation.release();
        }

        // 5. Go to shelf to load item
        List<Point> stepsToShelf = manager.requestPath(robot, this.shelfLocation);
        robot.stepMove(stepsToShelf);

        // 6. Loading
        robot.dropItem(this.productID);
        System.out.printf("[%s] Successfully stocked %s.%n", robot.getId(), this.productID);

        // 7. Increase Item
        warehouseManager.increaseProductQuantity(productID, quantity);

    }

    public Point getLoadingStationLocation() {
        return this.loadingStation.getLocation();
    }

    public Point getShelfLocation() {
        return this.shelfLocation.getLocation();
    }

    @Override
    public String getID() {
        return this.stockId;
    }

    @Override
    public String getDescription() {
        return "Stock " + this.quantity + "x " + this.productID + " [ID: " + this.stockId + "]";
    }

    @Override
    public TaskType getType() {
        return taskType;
    }
}