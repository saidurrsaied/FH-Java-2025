package wms.wmsjfx.application.inventory_screen;

import java.util.List;
import wms.wmsjfx.application.MainScreen;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import wms.wmsjfx.warehouse.datamanager.InventoryDataPacket;

public class StockController {

    @FXML private ComboBox<String> itemComboBox;
    @FXML private TextField qtyField; 
    @FXML private Label feedback; 
    private MainScreen mainScreen;  

    public void setInventoryManager(List<InventoryDataPacket> inventoryData) {
        populateItemComboBox(inventoryData);
    }
    
    // Handle Apply button click: Update the inventory quantity
    @FXML
    private void handleApply() {
        String selectedItem = itemComboBox.getValue();
        String qtyText = qtyField.getText();

        if (selectedItem == null || selectedItem.isEmpty()) {
            feedback.setText("Please select an item.");
            return;
        }

        if (qtyText == null || qtyText.isEmpty()) {
            feedback.setText("Please enter a quantity.");
            return;
        }
        int quantity = 0;
        try {
            quantity = Integer.parseInt(qtyText);
            if (quantity <= 0) {
                feedback.setText("Quantity must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            feedback.setText("Invalid quantity. Please enter a valid number.");
        }
        InventoryManager.incrementQuantity(selectedItem, quantity);
        feedback.setText("Quantity updated successfully.");
        showInventory(InventoryManager.convertToInventoryDataPacketList(InventoryManager.getItems()));
    }

    // Handle Clear button click: Reset the form
    @FXML
    private void handleClear() {
        itemComboBox.setValue(null); // Clear selected item in ComboBox
        qtyField.clear();            // Clear the quantity field
        feedback.setText("");        // Clear the feedback message
    }
    
    // Setter to allow injecting MainScreen reference
    public void setMainScreen(MainScreen mainScreen) {
        this.mainScreen = mainScreen;
    }
    
    public void showInventory(List<InventoryDataPacket> inventoryData) {
        mainScreen.showInventory();
        mainScreen.updateinventoryData(inventoryData);
    }
    
    // Populate the ComboBox with item names
    private void populateItemComboBox(List<InventoryDataPacket> inventoryData) {
        itemComboBox.getItems().clear();
        for (InventoryDataPacket item : inventoryData) {
            itemComboBox.getItems().add(item.getProductName());
        }
    }
}
