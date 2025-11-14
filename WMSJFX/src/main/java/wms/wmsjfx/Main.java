package wms.wmsjfx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Application;
import javafx.stage.Stage;

import wms.wmsjfx.logger.Logger;
import wms.wmsjfx.pathFinding.PathFinding;
import wms.wmsjfx.taskManager.Task;
import wms.wmsjfx.taskManager.TaskManager;
import wms.wmsjfx.warehouse.*;
import wms.wmsjfx.warehouse.datamanager.DataFile;
import wms.wmsjfx.warehouse.exceptions.DataFileException;
import wms.wmsjfx.warehouse.exceptions.InventoryException;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import wms.wmsjfx.application.Loading;
import wms.wmsjfx.application.Login;
import wms.wmsjfx.equipmentManager.EquipmentManager;
import wms.wmsjfx.equipmentManager.Robot;
import wms.wmsjfx.warehouse.datamanager.InventoryDataPacket;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException, InterruptedException {
        Logger log = new Logger();

        try {
            // Data files
            List<InventoryDataPacket> inventoryData;
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
                inventoryData = DataFile.loadInventoryFromCSV(inventoryCsv);


                // Print inventory (safe: log errors, continue)
                try {
                    warehouseManager.getInventory().forEach((key, value) ->
                            System.out.println(key + ": " + value.getProduct().getProductName() + ":  " + value.getQuantity() + " on : "+ value.getShelf().getId()));
                } catch (InventoryException ie) {
                    log.log_print("ERROR", "robot", "Failed to print inventory: " + ie.getMessage());
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
            availableRobots = warehouseManager.getAllRobots();

            List<WarehouseObject> warehouseObjects = warehouseManager.getAllWarehouseObjects();
            System.out.println(warehouseObjects);

            PathFinding pathFinding = new PathFinding(warehouseManager);

            // All needed class for equipment manager are created, now create equipmentManager and start its thread
            EquipmentManager equipmentManager = new EquipmentManager(
                    warehouseManager,
                    taskSubmissionQueue,
                    pathFinding
            );

            System.out.println("Updating Robot references to EquipmentManager...");
            for (Robot r : availableRobots) {
                r.setEquipmentManager(equipmentManager);
            }


            // Start EquipmentManager thread
            Thread emThread = new Thread(equipmentManager, "EM-Brain-Thread");
            emThread.start();

            // Create Task manager
            TaskManager taskManager = new TaskManager(taskSubmissionQueue, warehouseManager);

            // Load the FXML file for the loading page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Loading_Page.fxml"));
            Scene loadingScene  = new Scene(loader.load());
            Loading load = loader.getController();

            load.setOnLoadComplete(() -> {
                try {
                    // Load the login page FXML after loading completes
                    FXMLLoader login = new FXMLLoader(getClass().getResource("/Login_Page.fxml"));
                    Scene loginScene  = new Scene(login.load());
                    Login controller = login.getController();
                    controller.setMainData(warehouseManager, inventoryData, taskManager, equipmentManager);

                    primaryStage.setScene(loginScene);
                    primaryStage.setTitle("Login");
                } catch (Exception e) {
                    log.log_print("ERROR", "system", "Login Error");
                }
            });

            primaryStage.setScene(loadingScene);
            primaryStage.setTitle("Loading...");
            primaryStage.show();

        } catch (IllegalStateException fatal) {
            Logger logLocal = new Logger();
            logLocal.log_print("ERROR", "inventory", "Fatal error: " + fatal.getMessage());
        } catch (Exception unexpected) {
            Logger logLocal = new Logger();
            logLocal.log_print("ERROR", "inventory", "Unexpected error: " + unexpected.getMessage());
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}