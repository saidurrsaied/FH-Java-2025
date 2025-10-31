import logger.Logger;
import warehouse.*;
import equipments.ChargingStation;
import equipments.Robot;
import taskManager.Order;
import warehouse.datamanager.DataFile;
import warehouse.exceptions.DataFileException;
import warehouse.exceptions.InventoryException;
import warehouse.exceptions.FloorException;

import java.awt.Point;
import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Logger log = new Logger();

        try {
            // Data files
            File dataDir = new File("data");
            if (!dataDir.exists() && !dataDir.mkdirs()) {
                throw new IllegalStateException("Cannot create data directory at: " + dataDir.getAbsolutePath());
            }
            String inventoryCsv = new File(dataDir, "inventory.csv").getPath();
            String floorCsv = new File(dataDir, "warehouse_floor.csv").getPath();

            // --- Initialize warehouse from CSV files ---
            WarehouseManager manager = new WarehouseManager(1000, 1000);
            try {
                DataFile.initializeFloor(manager, floorCsv);
                DataFile.initializeInventory(manager, inventoryCsv);
                log.log_print("INFO", "inventory", "Loaded warehouse floor and inventory from CSV.");
            } catch (DataFileException dfe) {
                log.log_print("ERROR", "inventory", "Data file error: " + dfe.getMessage());
                return; // cannot proceed without data
            } catch (Exception initEx) {
                log.log_print("ERROR", "inventory", "Initialization error: " + initEx.getMessage());
                return;
            }

            // Print inventory (safe: log errors, continue)
            try {
                manager.getInventory().forEach((key, value) ->
                        System.out.println(key + ": " + value.getProduct().getProductName() + ":  " + value.getQuantity()));
            } catch (InventoryException ie) {
                log.log_print("ERROR", "inventory", "Failed to print inventory: " + ie.getMessage());
            }

            // --- Robot and task simulation ---
            ChargingStation station = new ChargingStation();
            Robot robot = new Robot("R-1", station);
            Thread robotThread = new Thread(robot, "Robot-R-1");
            robotThread.setDaemon(true);
            robotThread.start();

            // Create order picking task: pick bananas from SHELF-2 to packing station
            Point pickFrom;
            Point deliverTo;
            try {
                pickFrom = manager.getStorageShelf("SHELF2").getLocation();
                deliverTo = manager.getFloorObjectByID("ST01")
                        .orElseThrow(() -> new FloorException("Station not found: ST01"))
                        .getLocation();
            } catch (FloorException fe) {
                log.log_print("ERROR", "inventory", "Floor lookup failed: " + fe.getMessage());
                return;
            }

            Order pickBanana = new Order("Banana", pickFrom, 2, deliverTo);

            log.log_print("INFO", "robot", "Assigning: " + pickBanana.getDescription());
            try {
                pickBanana.execute(robot);
                log.log_print("INFO", "robot", "Task completed: " + pickBanana.isCompleted());
            } catch (RuntimeException taskEx) {
                log.log_print("ERROR", "robot", "Task execution failed: " + taskEx.getMessage());
            }

            // --- Update inventory after task completion ---
            try {
                manager.decreaseProductQuantity("P-002", 2);
                log.log_print("INFO", "inventory", "Decreased Banana by 2 after pick.");
            } catch (InventoryException ie) {
                log.log_print("ERROR", "inventory", "Inventory update failed: " + ie.getMessage());
            }

            // --- Export snapshot again ---
            try {
                String postRunCsv = new File(dataDir, "inventory_post_run.csv").getPath();
                List<warehouse.datamanager.InventoryDataPacket> after = manager.exportInventoryData();
                DataFile.exportInventoryToCSV(after, postRunCsv);
                log.log_print("INFO", "inventory", "Exported post-run inventory to " + postRunCsv);
            } catch (DataFileException dfe) {
                log.log_print("ERROR", "inventory", "Export failed: " + dfe.getMessage());
            }

            System.out.println("Simulation finished. Check Logging/ and data/ folders.");
        } catch (IllegalStateException fatal) {
            // Truly fatal pre-initialization error
            log.log_print("ERROR", "inventory", "Fatal error: " + fatal.getMessage());
        } catch (Exception unexpected) {
            log.log_print("ERROR", "inventory", "Unexpected error: " + unexpected.getMessage());
        }
    }
}