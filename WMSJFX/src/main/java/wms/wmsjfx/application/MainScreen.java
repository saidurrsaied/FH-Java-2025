package wms.wmsjfx.application;

import java.io.File;
import java.util.List;

import wms.wmsjfx.application.inventory_screen.*;
import wms.wmsjfx.application.robot_screen.OrderController;
import wms.wmsjfx.application.robot_screen.RobotManager;
import wms.wmsjfx.equipmentManager.EquipmentManager;
import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import wms.wmsjfx.logger.Logger;
import javafx.stage.FileChooser;
import wms.wmsjfx.taskManager.TaskManager;

import wms.wmsjfx.warehouse.WarehouseManager;
import wms.wmsjfx.warehouse.datamanager.DataFile;
import wms.wmsjfx.warehouse.datamanager.InventoryDataPacket;


public class MainScreen {

    @FXML private BorderPane mainContent;

    private List<InventoryDataPacket> inventoryData = FXCollections.observableArrayList();
    private TaskManager taskManager;
    private WarehouseManager warehousemanager;
    private EquipmentManager equipmentManager;

    public void setMainData(WarehouseManager warehousemanager, List<InventoryDataPacket> data, TaskManager taskManager, EquipmentManager equipmentManager) {
        this.inventoryData = data;
        this.taskManager = taskManager;
        this.warehousemanager = warehousemanager;
        this.equipmentManager = equipmentManager;
        InventoryManager.initializeInventory(inventoryData, this.warehousemanager);
        RobotManager.initializeRobot(this.equipmentManager.getRobot(), this.equipmentManager.getPendingTasks());
    }

    private Logger log = new Logger();

    // Method to show Inventory content
    @FXML
    public void showInventory() {
        // Load and set content for Inventory
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Inventory_Screen.fxml"));
            Node screen = loader.load();
            mainContent.setCenter(screen);
        } catch (Exception e) {
            log.log_print("ERROR", "system", "Inventory Screen error");
        }
    }

    // Method to show Warehouse content
    @FXML
    private void showFloor() {
        // Load and set content for Floor
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Floor_Screen.fxml"));
            Node floorScreen = loader.load();
            mainContent.setCenter(floorScreen);

        } catch (Exception e) {
            log.log_print("ERROR", "system", "Floor Screen error");
        }
    }

    // Method to show Robot content
    @FXML
    private void showRobot() {
        // Load and set content for Robot
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Robot_Screen.fxml"));
            Parent robotScreen = loader.load();

            Stage robotStage = new Stage();
            robotStage.setTitle("Robot Management");
            robotStage.setScene(new Scene(robotScreen, 400, 600)); // Set size for the new window
            robotStage.show();


        } catch (Exception e) {
            log.log_print("ERROR", "system", "Robot Screen error");
        }
    }

    // Method to show Log content
    @FXML
    private void showLog() {
        // Load and set content for Log
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Log_Screen.fxml"));
            Node inventoryScreen = loader.load();
            mainContent.setCenter(inventoryScreen);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private void showStock() {
        try {
            // Load the Stock screen FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Stock_Form.fxml"));
            Parent stockForm = loader.load();

            // Get the controller of Inventory Screen
            StockController stockController = loader.getController();
            stockController.setInventoryManager(warehousemanager);
            stockController.setManager(warehousemanager, taskManager, equipmentManager);

            Stage stockStage = new Stage();
            stockStage.setTitle("Stock Management");
            stockStage.setScene(new Scene(stockForm, 400, 250));
            stockStage.show();
        } catch (Exception e) {
            log.log_print("ERROR", "system", "Stock Screen error");
        }
    }

    @FXML private void showOrder() {
        try {
            // Load the Stock screen FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Order_Form.fxml"));
            Parent orderForm = loader.load();

            // Get the controller of Inventory Screen
            OrderController orderController = loader.getController();
            orderController.setInventoryManager();
            orderController.setManager(warehousemanager, taskManager, equipmentManager);

            // Create a new Stage (window)
            Stage orderStage = new Stage();
            orderStage.setTitle("Order Management");
            orderStage.setScene(new Scene(orderForm, 400, 200)); // Set size for the new window
            orderStage.show(); // Show the new window
            // Create a new Stage (window)

        } catch (Exception e) {
            log.log_print("ERROR", "system", "Order Screen error");
        }
    }

    @FXML
    private void handleExportCSV() {
        // Open a file chooser to select where to save the CSV
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {

            // Call the exportInventoryToCSV method with the selected file path
            DataFile.exportInventoryToCSV(inventoryData, file.getAbsolutePath());
            showAlert("Success", "Inventory has been exported to: " + file.getAbsolutePath(), AlertType.INFORMATION);
        }
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}