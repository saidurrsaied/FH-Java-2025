//import logger.Logger;
//import pathFinding.NodeType;
//import pathFinding.PathFinding;
//import pathFinding.WarehouseMap;
//import equipmentManager.ChargingStation;
//import equipmentManager.EquipmentManager;
//import equipmentManager.Robot;
//import taskManager.Task;
//import taskManager.TaskCreationException;
//import taskManager.TaskManager;
//import taskManager.OrderTask;
//import warehouse.PackingStation;
//import warehouse.StorageShelf;
//import warehouse.WahouseObjectType;
//import warehouse.WarehouseManager;
//import warehouse.datamanager.DataFile;
//import warehouse.exceptions.DataFileException;
//import warehouse.exceptions.InventoryException;
//
//import java.awt.Point;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.TimeUnit;
//
//public class Main_warehouse {
//    public static void main(String[] args) {
//        Logger log = new Logger();
//
//        try {
//            // Data files
//            File dataDir = new File("data");
//            if (!dataDir.exists() && !dataDir.mkdirs()) {
//                throw new IllegalStateException("Cannot create data directory at: " + dataDir.getAbsolutePath());
//            }
//            String inventoryCsv = new File(dataDir, "inventory.csv").getPath();
//            String floorCsv = new File(dataDir, "warehouse_floor.csv").getPath();
//
//            // --- Initialize managers and queues ---
//            WarehouseManager warehouseManager = new WarehouseManager(1000, 1000);
//            BlockingQueue<Task> taskSubmissionQueue = new LinkedBlockingQueue<>();
//            List<Robot> availableRobots = Collections.synchronizedList(new ArrayList<>());
//            List<ChargingStation> chargingStations = new ArrayList<>();
//
//            // Will be filled after floor init
//            List<PackingStation> packingStations;
//
//            // EquipmentManager depends on lists above
//            EquipmentManager equipmentManager = new EquipmentManager(
//                    availableRobots,
//                    chargingStations,
//                    new ArrayList<>(), // temp, replaced after floor init
//                    taskSubmissionQueue,
//                    new PathFinding(null)
//            );
// 
//            // --- Initialize warehouse from CSV files ---
//            try {
//                DataFile.initializeFloor(warehouseManager, equipmentManager, floorCsv);
//                warehouseManager.getInventory().forEach((key, value) -> System.out.println(key + ": " + value.getProduct().getProductName() + ":  " + value.getQuantity()));
//
//                DataFile.initializeInventory(warehouseManager, inventoryCsv);
//                // Print inventory (safe: log errors, continue)
//                try {
//                    warehouseManager.getInventory().forEach((key, value) ->
//                            System.out.println(key + ": " + value.getProduct().getProductName() + ":  " + value.getQuantity() + " on : "+ value.getShelf().getId()));
//                } catch (InventoryException ie) {
//                    log.log_print("ERROR", "inventory", "Failed to print inventory: " + ie.getMessage());
//                }
//                log.log_print("INFO", "inventory", "Loaded warehouse floor and inventory from CSV.");
//            } catch (DataFileException dfe) {
//                log.log_print("ERROR", "inventory", "Data file error: " + dfe.getMessage());
//                return;
//            } catch (Exception initEx) {
//                log.log_print("ERROR", "inventory", "Initialization error: " + initEx.getMessage());
//                return;
//            }
//
//            // Refresh packing stations list after floor load
//            packingStations = warehouseManager.getAllPackingStations();
//
//            // Collect robots created during floor initialization (type Robot in equipmentManager package)
//            for (var obj : warehouseManager.getFloorObjects()) {
//                if (obj.getObjectType() == WahouseObjectType.Robot && obj instanceof Robot r) {
//                    availableRobots.add(r);
//                }
//            }
//            
//          chargingStations.add(new ChargingStation("CS-01", 11, 0, WahouseObjectType.ChargingStation));
//          chargingStations.add(new ChargingStation("CS-02", 10, 0, WahouseObjectType.ChargingStation));
//
//          // Submit a demo order via TaskManager
//          TaskManager taskManager = new TaskManager(taskSubmissionQueue);
//          List<StorageShelf> storageShelves = warehouseManager.getAllStorageShelves();
//          
//          // Add pathFinding algorithm
//          WarehouseMap warehouseMap = new WarehouseMap(40, 40);
//          for (Robot r : availableRobots) {
//          	warehouseMap.addWarehouseObject(NodeType.Robot, true, r.getLocation());	
//          }
//          
//          for (PackingStation p : packingStations) {
//          	warehouseMap.addWarehouseObject(NodeType.PackingStation, false, p.getLocation());	
//          }
//          
//          System.out.println(packingStations);
//          for (ChargingStation c : chargingStations) {
//          	warehouseMap.addWarehouseObject(NodeType.ChargingStation, false, c.getLocation());	
//          }            
//          
//          for (StorageShelf s : storageShelves) {
//          	warehouseMap.addWarehouseObject(NodeType.Shelf, false, s.getLocation());	
//          }
//          
//  		for (int x = 0 ; x < warehouseMap.mapSizeX; x++) {
//  			for (int y = 0; y < warehouseMap.mapSizeY; y++) {
//  				
//  				if (warehouseMap.getWarehouseObject(new Point(x,y)) == null) {
//  					warehouseMap.addWarehouseObject(NodeType.None, true, new Point(x,y));
//  				}
//  			}
//  		}
//  		
//  		warehouseMap.showMap();
//  		PathFinding pathFinding = new PathFinding(warehouseMap);
//  		
//        // Recreate EquipmentManager with real packing stations
//        EquipmentManager finalEquipmentManager = new EquipmentManager(availableRobots,
//                                                                      chargingStations,
//                                                                      packingStations,
//                                                                      taskSubmissionQueue,
//                                                                      pathFinding);
//        // Start EquipmentManager thread
//        Thread emThread = new Thread(finalEquipmentManager, "EM-Brain-Thread");
//        emThread.setDaemon(true);
//        emThread.start();
//
//        System.out.println("Updating Robot references to Final EquipmentManager...");
//        for (Robot r : availableRobots) {
//            r.setEquipmentManager(finalEquipmentManager); 
//        }
//        
//        // Start robot threads
//        for (Robot r : availableRobots) {
//            Thread t = new Thread(r, "Robot-" + r.getID());
//            t.setDaemon(true);
//            t.start();
//        }
//
//        System.out.println(availableRobots);
//        try {           
//            int i = 0;     
//            System.out.printf("--- Submitting Order %d ---%n", i);
//            taskManager.createNewOrder( warehouseManager.getProductLocationByProductID("P-001"), "Apple", 1);
//            
//            System.out.printf("--- Submitting Order %d ---%n", i++);
//            taskManager.createNewOrder( warehouseManager.getProductLocationByProductID("P-002"), "Banana", 1);
//            
//            System.out.printf("--- Submitting Order %d ---%n", i++);
//            taskManager.createNewOrder( warehouseManager.getProductLocationByProductID("P-003"), "Orange", 1);
//            
//        } catch (TaskCreationException tce) {
//            log.log_print("ERROR", "robot", "Order creation failed: " + tce.getMessage());
//        }
//
////            // Allow some time for processing (optional simple wait)
////            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
////
////            // --- Export snapshot again ---
////            try {
////                String postRunCsv = new File(dataDir, "inventory_post_run.csv").getPath();
////                List<warehouse.datamanager.InventoryDataPacket> after = warehouseManager.exportInventoryData();
////                DataFile.exportInventoryToCSV(after, postRunCsv);
////                log.log_print("INFO", "inventory", "Exported post-run inventory to " + postRunCsv);
////            } catch (DataFileException dfe) {
////                log.log_print("ERROR", "inventory", "Export failed: " + dfe.getMessage());
////            }
//        System.out.println("Simulation running... Press CTRL + C to stop.");
//        emThread.join();
////            System.out.println("Simulation finished. Check Logging/ and data/ folders.");
//        } catch (IllegalStateException fatal) {
//            Logger logLocal = new Logger();
//            logLocal.log_print("ERROR", "inventory", "Fatal error: " + fatal.getMessage());
//        } catch (Exception unexpected) {
//            Logger logLocal = new Logger();
//            logLocal.log_print("ERROR", "inventory", "Unexpected error: " + unexpected.getMessage());
//        }
//    }
//}