//
//	
//import java.awt.Point;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.TimeUnit;
//import javafx.application.Application;
//import javafx.stage.Stage;
//import logger.Logger;
//import pathFinding.NodeType;
//import pathFinding.PathFinding;
//import pathFinding.WarehouseMap;
//import taskManager.Task;
//import taskManager.TaskManager;
//import warehouse.*;
//import warehouse.datamanager.DataFile;
//import warehouse.exceptions.DataFileException;
//import warehouse.exceptions.InventoryException;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.layout.AnchorPane;
//import javafx.fxml.FXMLLoader;
//import application.MainScreen;
//import equipmentManager.EquipmentManager;
//import equipmentManager.ChargingStation;
//import equipmentManager.Robot;
//import warehouse.datamanager.InventoryDataPacket;
//
//public class Main extends Application {
//
//    @Override
//    public void start(Stage primaryStage) throws IOException, InterruptedException {
//        
//        File dataDir = new File("data");
//        if (!dataDir.exists() && !dataDir.mkdirs()) {
//            throw new IllegalStateException("Cannot create data directory at: " + dataDir.getAbsolutePath());
//        }
//        String inventoryCsv = new File(dataDir, "inventory.csv").getPath();
//        String floorCsv = new File(dataDir, "warehouse_floor.csv").getPath();
//
//        // --- Initialize managers and queues ---
//        WarehouseManager warehouseManager = new WarehouseManager(1000, 1000);
//        BlockingQueue<Task> taskSubmissionQueue = new LinkedBlockingQueue<>();
//        List<Robot> availableRobots = Collections.synchronizedList(new ArrayList<>());
//        List<ChargingStation> chargingStations = new ArrayList<>();
//
//        // Will be filled after floor init
//        List<PackingStation> packingStations = new ArrayList<>();
//        
//        // EquipmentManager depends on lists above
//        EquipmentManager equipmentManager = new EquipmentManager(
//                availableRobots,
//                chargingStations,
//                new ArrayList<>(), // temp, replaced after floor init
//                taskSubmissionQueue,
//                new PathFinding(null)
//        );
//           
//        DataFile.initializeFloor(warehouseManager, equipmentManager, floorCsv);
//        DataFile.initializeInventory(warehouseManager, inventoryCsv);
//        
//        List<InventoryDataPacket> inventoryData = DataFile.loadInventoryFromCSV(inventoryCsv);
//        
//        // Refresh packing stations list after floor load
//        packingStations = warehouseManager.getAllPackingStations();
//        
//        // Collect robots created during floor initialization (type Robot in equipmentManager package)
//        for (var obj : warehouseManager.getFloorObjects()) {
//            if (obj.getObjectType() == WahouseObjectType.Robot && obj instanceof Robot r) {
//                availableRobots.add(r);
//                System.out.println(r);
//
//            }
//        }
//
//        chargingStations.add(new ChargingStation("CS-01", 15, 13, WahouseObjectType.ChargingStation));
//        chargingStations.add(new ChargingStation("CS-02", 17, 13, WahouseObjectType.ChargingStation));
//        
//        TaskManager taskManager = new TaskManager(taskSubmissionQueue);
//        List<StorageShelf> storageShelves = warehouseManager.getAllStorageShelves();
//
//        // Add pathFinding algorithm
//        WarehouseMap warehouseMap = new WarehouseMap(40, 40);
//        for (Robot r : availableRobots) {
//        	warehouseMap.addWarehouseObject(NodeType.Robot, true, r.getLocation());	
//        }
//        
//        for (PackingStation p : packingStations) {
//        	warehouseMap.addWarehouseObject(NodeType.PackingStation, false, p.getLocation());	
//        }
//        
//        System.out.println(packingStations);
//        for (ChargingStation c : chargingStations) {
//        	warehouseMap.addWarehouseObject(NodeType.ChargingStation, false, c.getLocation());	
//        }            
//        
//        for (StorageShelf s : storageShelves) {
//        	warehouseMap.addWarehouseObject(NodeType.Shelf, false, s.getLocation());	
//        }
//        
//		for (int x = 0 ; x < warehouseMap.mapSizeX; x++) {
//			for (int y = 0; y < warehouseMap.mapSizeY; y++) {
//				
//				if (warehouseMap.getWarehouseObject(new Point(x,y)) == null) {
//					warehouseMap.addWarehouseObject(NodeType.None, true, new Point(x,y));
//				}
//			}
//		}
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
//
//        System.out.println("Updating Robot references to Final EquipmentManager...");
//        for (Robot r : availableRobots) {
//            r.setEquipmentManager(finalEquipmentManager); 
//        }
//        
//        // Start EquipmentManager thread
//        Thread emThread = new Thread(finalEquipmentManager, "EM-Brain-Thread");
//        emThread.setDaemon(true);
//        emThread.start();
//       
//        // Start robot threads
//        for (Robot r : availableRobots) {
//            Thread t = new Thread(r, "Robot-" + r.getId());
//            t.setDaemon(true);
//            t.start();
//        }
//
//        	FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_interface/Main_Screen.fxml"));
//        	Scene loadingScene = new Scene(loader.load()); // Set the initial window size to 800x600
//        	
//        	// Inject inventory data into main screen
//        MainScreen controller = loader.getController();            
//        controller.setMainData(warehouseManager, inventoryData, taskManager, finalEquipmentManager);
//                
//        // Show the loading scene initially          
//        primaryStage.setScene(loadingScene);            
//        primaryStage.setTitle("Loading...");            
//        primaryStage.show();
//        
//        // Wait indefinitely
////        emThread.join();    
//        }
//    public static void main(String[] args) {
//
//
//	
//        launch(args);
//    }
//}
//
////
////import equipmentcontrol.Robot;
////import equipmentcontrol.ChargingStation;
////import storagemanagement.Inventory;
////import logging.Logger;
////
////public class Main {
////    public static void main(String[] args) {
////        // Initialize the components
////        Robot robot1 = new Robot("Robot1");
////        ChargingStation chargingStation1 = new ChargingStation("ChargingStation1");
////        Inventory inventory = new Inventory();
////
////        // Simulate tasks and log actions
////        while(true) {
////            robot1.performTask();
////            chargingStation1.charge();
////            inventory.addItem("Item1");
////            inventory.orderItem("Item2");
////
////            // Simulate an error log for robot (optional)
////            Logger.log_print("ERROR", "robot", "Robot1 encountered an issue during task.");
//////            Logger.log_print("ERROR", "robot", "message");
////        }
////
////    }
////}
//
