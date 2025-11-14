package application.robot_screen;

import java.awt.Point;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import application.inventory_screen.InventoryManager;
import equipmentManager.EquipmentManager;
import taskManager.Task;
import taskManager.TaskManager;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import warehouse.WarehouseManager;
import warehouse.datamanager.InventoryDataPacket;

public class OrderController {
	    @FXML private ComboBox<String> itemComboBox;
	    @FXML private TextField qtyField; 
	    @FXML private Label feedback; 
	    @FXML private Label idfield; 

        WarehouseManager warehouseManager = new WarehouseManager(1000, 1000);
        BlockingQueue<Task> taskSubmissionQueue = new LinkedBlockingQueue<>();
        
        TaskManager taskManager;
        EquipmentManager equipmentManager;
        // Reverted logging

        public void setInventoryManager(List<InventoryDataPacket> inventoryData) {
	        populateItemComboBox(inventoryData);  // Update the table view whenever inventoryManager is set
	    }

        public void setManager(WarehouseManager warehouseManager,TaskManager taskManager, EquipmentManager equipmentManager) {
	        this.warehouseManager = warehouseManager;  // Update the table view whenever inventoryManager is set
	        this.taskManager = taskManager;
	        this.equipmentManager = equipmentManager;
	    }
        
        @FXML
        private void handleApply() {
            String selectedItem = itemComboBox.getValue();
            String qtyText = qtyField.getText();
            String idText = InventoryManager.getID(selectedItem);
            idfield.setText(idText);

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
                    taskManager.createNewOrder(idText, quantity);

                    TimeUnit.SECONDS.sleep(5);

                    javafx.application.Platform.runLater(() -> {
                        feedback.setText("Order created successfully.");
                    });

                } catch (Exception e) {
                    System.err.println("[robot] Order creation failed: " + e.getMessage());
                    javafx.application.Platform.runLater(() -> {
                        feedback.setText("Order creation failed.");
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
	    private void populateItemComboBox(List<InventoryDataPacket> inventoryData) {
	        // Clear current items in the combo box
	        itemComboBox.getItems().clear();

	        // Add item names to the combo box from the inventoryItems list
	        for (InventoryDataPacket item : inventoryData) {
	            itemComboBox.getItems().add(item.getProductName());
	        }
	    }

}
