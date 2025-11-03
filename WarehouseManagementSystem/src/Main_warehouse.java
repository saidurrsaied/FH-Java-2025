import logger.Logger;
import equipmentManager.ChargingStation;
import equipmentManager.EquipmentManager;
import equipmentManager.Robot;
import taskManager.Task;
import taskManager.TaskCreationException;
import taskManager.TaskManager;
import taskManager.OrderTask;
import warehouse.PackingStation;
import warehouse.WahouseObjectType;
import warehouse.WarehouseManager;
import warehouse.datamanager.DataFile;
import warehouse.exceptions.DataFileException;
import warehouse.exceptions.InventoryException;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main_warehouse {
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

            // --- Initialize managers and queues ---
            WarehouseManager warehouseManager = new WarehouseManager(1000, 1000);
            BlockingQueue<Task> taskSubmissionQueue = new LinkedBlockingQueue<>();
            List<Robot> availableRobots = Collections.synchronizedList(new ArrayList<>());
            List<ChargingStation> chargingStations = new ArrayList<>();


            // Will be filled after floor init
            List<PackingStation> packingStations;

            // EquipmentManager depends on lists above
            EquipmentManager equipmentManager = new EquipmentManager(
                    availableRobots,
                    chargingStations,
                    new ArrayList<>(), // temp, replaced after floor init
                    taskSubmissionQueue
            );

            // --- Initialize warehouse from CSV files ---
            try {
                DataFile.initializeFloor(warehouseManager, equipmentManager, floorCsv);
                warehouseManager.getInventory().forEach((key, value) -> System.out.println(key + ": " + value.getProduct().getProductName() + ":  " + value.getQuantity()));

                DataFile.initializeInventory(warehouseManager, inventoryCsv);
                // Print inventory (safe: log errors, continue)
                try {
                    warehouseManager.getInventory().forEach((key, value) ->
                            System.out.println(key + ": " + value.getProduct().getProductName() + ":  " + value.getQuantity() + " on : "+ value.getShelf().getId()));
                } catch (InventoryException ie) {
                    log.log_print("ERROR", "inventory", "Failed to print inventory: " + ie.getMessage());
                }


                log.log_print("INFO", "inventory", "Loaded warehouse floor and inventory from CSV.");
            } catch (DataFileException dfe) {
                log.log_print("ERROR", "inventory", "Data file error: " + dfe.getMessage());
                return;
            } catch (Exception initEx) {
                log.log_print("ERROR", "inventory", "Initialization error: " + initEx.getMessage());
                return;
            }

            // Refresh packing stations list after floor load
            packingStations = warehouseManager.getAllPackingStations();

            // Recreate EquipmentManager with real packing stations
            EquipmentManager finalEquipmentManager = new EquipmentManager(
                    availableRobots,
                    chargingStations,
                    packingStations,
                    taskSubmissionQueue
            );

            // Collect robots created during floor initialization (type Robot in equipmentManager package)
            for (var obj : warehouseManager.getFloorObjects()) {
                if (obj.getObjectType() == WahouseObjectType.Robot && obj instanceof Robot r) {
                    availableRobots.add(r);
                }
            }

            // Start EquipmentManager thread
            Thread emThread = new Thread(finalEquipmentManager, "EM-Brain-Thread");
            emThread.setDaemon(true);
            emThread.start();

            // Start robot threads
            for (Robot r : availableRobots) {
                Thread t = new Thread(r, "Robot-" + r.getID());
                t.setDaemon(true);
                t.start();
            }



            // Submit a demo order via TaskManager
            TaskManager taskManager = new TaskManager(taskSubmissionQueue);
            try {
                Point pickFrom = warehouseManager.getStorageShelf("SHELF2").getLocation();
                // Use OrderTask to pick an item by name, delivery handled by EquipmentManager at runtime
                taskManager.createNewOrder(warehouseManager.getProductLocationByProductID("P-002"), "P-002", 2);
                log.log_print("INFO", "robot", "Submitted demo order for Banana x2.");
            } catch (TaskCreationException tce) {
                log.log_print("ERROR", "robot", "Order creation failed: " + tce.getMessage());
            }

            // Allow some time for processing (optional simple wait)
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            // --- Export snapshot again ---
            try {
                String postRunCsv = new File(dataDir, "inventory_post_run.csv").getPath();
                List<warehouse.datamanager.InventoryDataPacket> after = warehouseManager.exportInventoryData();
                DataFile.exportInventoryToCSV(after, postRunCsv);
                log.log_print("INFO", "inventory", "Exported post-run inventory to " + postRunCsv);
            } catch (DataFileException dfe) {
                log.log_print("ERROR", "inventory", "Export failed: " + dfe.getMessage());
            }

            System.out.println("Simulation finished. Check Logging/ and data/ folders.");
        } catch (IllegalStateException fatal) {
            Logger logLocal = new Logger();
            logLocal.log_print("ERROR", "inventory", "Fatal error: " + fatal.getMessage());
        } catch (Exception unexpected) {
            Logger logLocal = new Logger();
            logLocal.log_print("ERROR", "inventory", "Unexpected error: " + unexpected.getMessage());
        }
    }
}