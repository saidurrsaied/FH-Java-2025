import logger.Logger;
import warehouse.*;
import equipments.ChargingStation;
import equipments.Robot;
import taskManager.Order;
import warehouse.datamanager.DataFile;

import java.awt.Point;
import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        Logger log = new Logger();

        // Data files
        File dataDir = new File("data");
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            throw new IllegalStateException("Cannot create data directory at: " + dataDir.getAbsolutePath());
        }
        String inventoryCsv = new File(dataDir, "inventory.csv").getPath();
        String floorCsv = new File(dataDir, "warehouse_floor.csv").getPath();

        // --- Initialize warehouse from CSV files ---
        WarehouseManager manager = new WarehouseManager(1000, 1000);
        DataFile.initializeFloor(manager, floorCsv);
        DataFile.initializeInventory(manager, inventoryCsv);
        log.log_print("INFO", "inventory", "Loaded warehouse floor and inventory from CSV.");

        // Print inventory
        manager.getInventory().forEach((key, value) -> System.out.println(key + ": "+ value.getProduct().getProductName() + ":  " + value.getQuantity()));


        // --- Robot and task simulation ---
        ChargingStation station = new ChargingStation();
        Robot robot = new Robot("R-1", station);
        Thread robotThread = new Thread(robot, "Robot-R-1");
        robotThread.setDaemon(true);
        robotThread.start();

        // Create order picking task: pick bananas from SHELF-2 to packing station
        Point pickFrom = manager.getStorageShelf("SHELF2").getLocation();
        Point deliverTo = manager.getFloorObjectByID("ST01").get().getLocation();
        Order pickBanana = new Order("Banana", pickFrom, 2, deliverTo);

        log.log_print("INFO", "robot", "Assigning: " + pickBanana.getDescription());
        pickBanana.execute(robot);
        log.log_print("INFO", "robot", "Task completed: " + pickBanana.isCompleted());

        // --- Update inventory after task completion ---
        manager.decreaseProductQuantity("P-002", 2);
        log.log_print("INFO", "inventory", "Decreased Banana by 2 after pick.");

        // --- Export snapshot again ---
        String postRunCsv = new File(dataDir, "inventory_post_run.csv").getPath();
        List<warehouse.datamanager.InventoryDataPacket> after = manager.exportInventoryData();
        DataFile.exportInventoryToCSV(after, postRunCsv);
        log.log_print("INFO", "inventory", "Exported post-run inventory to " + postRunCsv);

        System.out.println("Simulation finished. Check Logging/ and data/ folders.");
    }
}