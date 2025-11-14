package main;
import pathFinding.PathFinding;
import equipmentManager.EquipmentManager;
import taskManager.Task;
import taskManager.TaskCreationException;
import taskManager.TaskManager;
import warehouse.WarehouseManager;
import warehouse.datamanager.DataFile;
import warehouse.exceptions.DataFileException;
import warehouse.exceptions.InventoryException;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main_warehouse {
    public static void main(String[] args) {
        // Reverted: use direct prints

        try {
            // Data files
            File dataDir = new File("data");
            if (!dataDir.exists() && !dataDir.mkdirs()) {
                throw new IllegalStateException("Cannot create data directory at: " + dataDir.getAbsolutePath());
            }

            // --- Initialize managers and queues ---
            WarehouseManager warehouseManager = new WarehouseManager(20, 20);
            
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
                    System.err.println("[inventory] Failed to print inventory: " + ie.getMessage());
                }
                System.out.println("[inventory] Loaded warehouse floor and inventory from CSV.");
            } catch (DataFileException dfe) {
                System.err.println("[inventory] Data file error: " + dfe.getMessage());
                return;
            } catch (Exception initEx) {
                System.err.println("[inventory] Initialization error: " + initEx.getMessage());
                return;
            }
            
            BlockingQueue<Task> taskSubmissionQueue = new LinkedBlockingQueue<>();
            
            // Create path finding based on A* algorithm
            PathFinding pathFinding = new PathFinding(warehouseManager);
            
            // All needed class for equipment manager are created, now create equipmentManager and start its thread
            EquipmentManager equipmentManager = new EquipmentManager(
            		warehouseManager,
                    taskSubmissionQueue,
                    pathFinding
            );
            Thread emThread = new Thread(equipmentManager, "EM-Brain-Thread");
            emThread.start();
            
            // Create Task manager
            TaskManager taskManager = new TaskManager(taskSubmissionQueue, warehouseManager);
          
	        try {           
	            int i = 0;     
	            System.out.printf("--- Submitting Order %d ---%n", i);
	            taskManager.createNewOrder("P-001", 1);
	            
	            System.out.printf("--- Submitting Order %d ---%n", ++i);
	            taskManager.createNewOrder("P-002", 1);
	            
	            System.out.printf("--- Submitting Order %d ---%n", ++i);
	            taskManager.createNewOrder("P-003", 1);
	            
	            taskManager.createNewStock("LST01", "P-003", 5);
	            
	        } catch (TaskCreationException tce) {
                System.err.println("[robot] Order creation failed: " + tce.getMessage());
	        }

            // Simplified message (removed obsolete graceful shutdown hint)
            System.out.println("[inventory] Simulation running...");

        // Keep main thread alive so user can press CTRL+C
        try {
            emThread.join(); // Wait for dispatcher to finish
        } catch (InterruptedException ie) {
            System.out.println("[Main] Interrupted, exiting...");
        }
	        } catch (IllegalStateException fatal) {
                System.err.println("[inventory] Fatal error: " + fatal.getMessage());
	        } catch (Exception unexpected) {
                System.err.println("[inventory] Unexpected error: " + unexpected.getMessage());
	        }
	    }
}