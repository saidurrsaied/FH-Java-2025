package application.inventory_screen;

import java.util.List;
import java.util.concurrent.TimeUnit;

import application.MainScreen;
import equipmentManager.EquipmentManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import taskManager.TaskManager;
import warehouse.InventoryItem;
import warehouse.LoadingStation;
import warehouse.WarehouseManager;
import warehouse.datamanager.InventoryDataPacket;

public class StockController {

    @FXML private ComboBox<String> itemComboBox;
    @FXML private ComboBox<String> loadingComboBox;
    @FXML private TextField qtyField; 
    @FXML private Label feedback; 
    
    private WarehouseManager warehouseManager;  
    private TaskManager taskManager;
    EquipmentManager equipmentManager;

    public void setInventoryManager(WarehouseManager warehousemanager) {
        populateItemComboBox(InventoryManager.getItems(), warehousemanager);
    }
    
    public void setManager(WarehouseManager warehouseManager,TaskManager taskManager, EquipmentManager equipmentManager) {
        this.warehouseManager = warehouseManager;  
        this.taskManager = taskManager;
        this.equipmentManager = equipmentManager;
    }
    
    // Handle Apply button click: Update the inventory quantity
    @FXML
    private void handleApply() {
        String selectedItem = itemComboBox.getValue();
        String selectedStation = loadingComboBox.getValue();
        String qtyText = qtyField.getText();
        String idText = InventoryManager.getID(selectedItem);

        if (selectedItem == null || selectedItem.isEmpty()) {
            feedback.setText("Please select an item.");
            return;
        }

        if (qtyText == null || qtyText.isEmpty()) {
            feedback.setText("Please enter a quantity.");
            return;
        }
        
        int quantity;
        try {
            quantity = Integer.parseInt(qtyText);
            if (quantity <= 0) {
                feedback.setText("Quantity must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            feedback.setText("Invalid quantity.");
            return;
        }

        new Thread(() -> {
            try {
                taskManager.createNewStock(selectedStation, idText, quantity);

                TimeUnit.SECONDS.sleep(5);
                javafx.application.Platform.runLater(() -> {
                    feedback.setText("Stock created successfully.");
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    feedback.setText("Stock creation failed.");
                });
            }
        }).start();
    }

    // Handle Clear button click: Reset the form
    @FXML
    private void handleClear() {
        itemComboBox.setValue(null); // Clear selected item in ComboBox
        qtyField.clear();            // Clear the quantity field
        feedback.setText("");        // Clear the feedback message
    }
    
    // Populate the ComboBox with item names
    private void populateItemComboBox(ObservableList<InventoryItem> inventoryData, WarehouseManager warehousemanager) {
        itemComboBox.getItems().clear();
        loadingComboBox.getItems().clear();
        for (InventoryItem item : inventoryData) {
            itemComboBox.getItems().add(item.getProduct().getProductName());
        }
        for(LoadingStation station : warehousemanager.getAllLoadingStations()) {
            loadingComboBox.getItems().add(station.getId());           
        }
    }
}
