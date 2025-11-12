package main;
import logger.Logger;
import pathFinding.NodeType;
import pathFinding.PathFinding;
import pathFinding.WarehouseMap;
import equipmentManager.ChargingStation;
import equipmentManager.EquipmentManager;
import equipmentManager.Robot;
import taskManager.Task;
import taskManager.TaskCreationException;
import taskManager.TaskManager;
import taskManager.OrderTask;
import warehouse.PackingStation;
import warehouse.StorageShelf;
import warehouse.WahouseObjectType;
import warehouse.WarehouseManager;
import warehouse.WarehouseObject;
import warehouse.datamanager.DataFile;
import warehouse.exceptions.DataFileException;
import warehouse.exceptions.InventoryException;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
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

            // --- Initialize managers and queues ---
            WarehouseManager warehouseManager = new WarehouseManager(1000, 1000);
            
            // --- Initialize warehouse from CSV files ---
            try {
                String inventoryCsv = new File(dataDir, "inventory.csv").getPath();
                String floorCsv = new File(dataDir, "warehouse_floor.csv").getPath();
                
                DataFile.initializeFloor(warehouseManager, null, floorCsv);
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
            
            BlockingQueue<Task> taskSubmissionQueue = new LinkedBlockingQueue<>();
            
            List<Robot> availableRobots = new ArrayList<>();
            List<ChargingStation> chargingStations = new ArrayList<>();
            List<PackingStation> packingStations = new ArrayList<>();;
            
            packingStations = warehouseManager.getAllPackingStations();
            chargingStations = warehouseManager.getAllChargingStations();
            availableRobots = warehouseManager.getAllRobots();
            
            List<WarehouseObject> warehouseObjects = warehouseManager.getAllWarehouseObjects();
            System.out.println(warehouseObjects);
            
            // Create A* map
            WarehouseMap warehouseMap = new WarehouseMap(20, 15, warehouseObjects);
            warehouseMap.showMap();

            PathFinding pathFinding = new PathFinding(warehouseMap);
            
            EquipmentManager equipmentManager = new EquipmentManager(
                    availableRobots,
                    chargingStations,
                    packingStations,
                    taskSubmissionQueue,
                    pathFinding
            );
            
	        System.out.println("Updating Robot references to EquipmentManager...");
	        for (Robot r : availableRobots) {
	            r.setEquipmentManager(equipmentManager); 
	        }
            
            TaskManager taskManager = new TaskManager(taskSubmissionQueue, warehouseManager);
          
            // Start EquipmentManager thread
	        Thread emThread = new Thread(equipmentManager, "EM-Brain-Thread");
	        emThread.setDaemon(true);
	        emThread.start();
	        
	        // Start robot threads
	        for (Robot r : availableRobots) {
	            Thread t = new Thread(r, "Robot-" + r.getId());
	            t.setDaemon(true);
	            t.start();
	        }
	
	        try {           
	            int i = 0;     
	            System.out.printf("--- Submitting Order %d ---%n", i);
	            taskManager.createNewOrder("P-001", 1);
	            
	            System.out.printf("--- Submitting Order %d ---%n", ++i);
	            taskManager.createNewOrder("P-002", 1);
	            
	            System.out.printf("--- Submitting Order %d ---%n", ++i);
	            taskManager.createNewOrder("P-003", 1);
	            
	        } catch (TaskCreationException tce) {
	            log.log_print("ERROR", "robot", "Order creation failed: " + tce.getMessage());
	        }
	
	        System.out.println("Simulation running... Press CTRL + C to stop.");
	        emThread.join();
	        } catch (IllegalStateException fatal) {
	            Logger logLocal = new Logger();
	            logLocal.log_print("ERROR", "inventory", "Fatal error: " + fatal.getMessage());
	        } catch (Exception unexpected) {
	            Logger logLocal = new Logger();
	            logLocal.log_print("ERROR", "inventory", "Unexpected error: " + unexpected.getMessage());
	        }
	    }
}