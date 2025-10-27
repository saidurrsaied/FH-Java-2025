package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainScreen {

    @FXML private BorderPane mainContent;

    // Method to show Inventory content
    @FXML
    private void showInventory() {
        // Load and set content for Inventory
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Inventory_Screen.fxml"));
            Node inventoryScreen = loader.load();
            mainContent.setCenter(inventoryScreen);
        } catch (Exception e) {
            e.printStackTrace();
        }    }

    // Method to show Warehouse content
    @FXML
    private void showWarehouse() {
        // Load and set content for Warehouse
        mainContent.setCenter(new WarehouseScreen());  // Create a WarehouseScreen class
    }

    // Method to show Robot content
    @FXML
    private void showRobot() {
        // Load and set content for Robot
        mainContent.setCenter(new RobotScreen());  // Create a RobotScreen class
    }

    // Method to show Log content
    @FXML
    private void showLog() {
        // Load and set content for Log
        mainContent.setCenter(new LogScreen());  // Create a LogScreen class
    }
    @FXML private void showStock() {
        try {
            // Load Stock form from FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Stock_Form.fxml"));
            Parent stockForm = loader.load();

            // Create a new Stage (window)
            Stage stockStage = new Stage();
            stockStage.setTitle("Stock Management");
            stockStage.setScene(new Scene(stockForm, 400, 200)); // Set size for the new window
            stockStage.show(); // Show the new window

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
