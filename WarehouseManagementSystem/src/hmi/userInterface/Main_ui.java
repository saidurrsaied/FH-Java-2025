package userinterface;
	
import java.io.IOException;
import storagemanagement.*;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;

public class Main_ui extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
            // Load the FXML file for the loading page
//        		FXMLLoader loader = new FXMLLoader(getClass().getResource("/Loading_Page.fxml"));
//            Scene loadingScene  = new Scene(loader.load());
//            Loading controller = loader.getController();
//            controller.setOnLoadComplete(() -> {
//                try {
//                    // Load the login page FXML after loading completes
//            		    FXMLLoader login = new FXMLLoader(getClass().getResource("/Login_Page.fxml"));
//                    Scene loginScene  = new Scene(login.load());
//                    primaryStage.setScene(loginScene);
//                    primaryStage.setTitle("Login");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//    	        
//    		WarehouseManager manager = new WarehouseManager(1000, 1000);
//        //Create inventory
//        Product apple = new Product("Apple", "P-001");
//        Product banana = new Product("Banana", "P-002");
//        manager.addProductToInventory(apple, 10, "SHELF-1");
//        manager.addProductToInventory(banana, 5, "SHELF-2");
        	FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main_Screen.fxml"));
        	Scene loadingScene = new Scene(loader.load()); // Set the initial window size to 800x600


            // Show the loading scene initially
            primaryStage.setScene(loadingScene);
            primaryStage.setTitle("Loading...");
            primaryStage.show();
        
        }
    public static void main(String[] args) {
        launch(args);
    }
}

//
//import equipmentcontrol.Robot;
//import equipmentcontrol.ChargingStation;
//import storagemanagement.Inventory;
//import logging.Logger;
//
//public class Main {
//    public static void main(String[] args) {
//        // Initialize the components
//        Robot robot1 = new Robot("Robot1");
//        ChargingStation chargingStation1 = new ChargingStation("ChargingStation1");
//        Inventory inventory = new Inventory();
//
//        // Simulate tasks and log actions
//        while(true) {
//            robot1.performTask();
//            chargingStation1.charge();
//            inventory.addItem("Item1");
//            inventory.orderItem("Item2");
//
//            // Simulate an error log for robot (optional)
//            Logger.log_print("ERROR", "robot", "Robot1 encountered an issue during task.");
////            Logger.log_print("ERROR", "robot", "message");
//        }
//
//    }
//}

