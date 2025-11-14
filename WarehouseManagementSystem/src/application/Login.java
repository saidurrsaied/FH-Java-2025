package application;

import java.util.List;

import equipmentManager.EquipmentManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import logger.Logger;
import taskManager.TaskManager;
import warehouse.WarehouseManager;
import warehouse.datamanager.InventoryDataPacket;
import javafx.collections.FXCollections;

public class Login {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;
    @FXML private Label feedback;
    
    private List<InventoryDataPacket> inventoryData = FXCollections.observableArrayList();
    private TaskManager taskManager;
    private WarehouseManager warehousemanager;
    private EquipmentManager equipmentManager;
    private Logger log = new Logger();
    @FXML
    private void initialize() {
        // Disable the Login button until both fields are filled
        loginButton.disableProperty().bind(
            usernameField.textProperty().isEmpty()
                .or(passwordField.textProperty().isEmpty())
        );
        
        // Handle Enter key on password field (for login)
        passwordField.setOnAction(e -> {
			try {
				handleLogin();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
        			log.log_print("ERROR", "system", "Login Screen error");
			}
		});
    }
    @FXML
    private void handleLogin() throws Exception {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.equals("admin") && password.equals("1234")) {
            feedback.setText("Login successful!");
            feedback.setStyle("-fx-text-fill: green;");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_interface/Main_Screen.fxml"));
           	Scene loadingScene = new Scene(loader.load());
            MainScreen controller = loader.getController();            
            controller.setMainData(warehousemanager, inventoryData, taskManager, equipmentManager);            
            Stage mainStage = new Stage();
	        mainStage.setScene(loadingScene);
	        mainStage.setTitle("Welcome!");
	        mainStage.show();
            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();
        } else {
            feedback.setText("Invalid credentials.");
            feedback.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleCancel() {
        usernameField.clear();
        passwordField.clear();
        feedback.setText("");
    }
    
    public void setMainData(WarehouseManager warehousemanager, List<InventoryDataPacket> data, TaskManager taskManager, EquipmentManager equipmentManager) {
        this.inventoryData = data;
        this.taskManager = taskManager;
        this.warehousemanager = warehousemanager;
        this.equipmentManager = equipmentManager;
    }
}
