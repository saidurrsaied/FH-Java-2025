import logger.Logger;
import warehouse.*;
import equipments.ChargingStation;
import equipments.Robot;
import taskManager.Order;

import java.awt.Point;

public class Main {
    public static void main(String[] args) {

        WarehouseManager manager = new WarehouseManager(1000, 1000);
        Logger dataLogger = new Logger();

        // Create warehouse floor objects
        StorageShelf shelfA = new StorageShelf("SHELF-1", 50, 50, 40, 20);
        StorageShelf shelfB = new StorageShelf("SHELF-2", 120, 50, 40, 20);
        Station packStation = new Station("Packing Station 1", "PS1", 300, 60, 60, 40);

        manager.addObjectToFloor(shelfA);
        manager.addObjectToFloor(shelfB);
        manager.addObjectToFloor(packStation);

        //Create inventory
        Product apple = new Product("Apple", "P-001");
        Product banana = new Product("Banana", "P-002");
        manager.addProductToInventory(apple, 10, "SHELF-1");
        manager.addProductToInventory(banana, 5, "SHELF-2");


        System.out.println("Apple available: " + manager.getProductQuantity("P-001"));
        System.out.println("Banana available: " + manager.getProductQuantity("P-002"));
        System.out.println("Apple in stock? " + manager.isProductInStock("P-001"));

        // Increase/decrease
        manager.increaseProductQuantity("P-001", 3);
        dataLogger.log_print("INFO","inventory", "Inventory Added New Product");

        manager.decreaseProductQuantity("P-002", 2);
        System.out.println("Apple available: " + manager.getProductQuantity("P-001")
                + ", Banana available: " + manager.getProductQuantity("P-002"));



        // View read-only collections
        System.out.println("Inventory overview: " + manager.getInventory());
        System.out.println("Floor overview: " + manager.getFloorOverview());


        ChargingStation station = new ChargingStation();
        Robot robot = new Robot("R-1", station);

        // Use WarehouseManager to fetch floor objects and pass their locations to the Order
        //Point itemLocation = manager.getStorageShelf("SHELF-2").getLocation();
        //Point packingLocation = manager.getFloorObjectByID("PS1").get().getLocation();
        Order pickBanana = new Order("Banana", manager.getStorageShelf("SHELF-2").getLocation(), 2, manager.getFloorObjectByID("PS1").get().getLocation());

        dataLogger.log_print("INFO", "robot", "Assigned task to robot: " + pickBanana.getDescription());
        pickBanana.execute(robot);
        dataLogger.log_print("INFO", "robot", "Task completed: " + pickBanana.isCompleted());

    }
}