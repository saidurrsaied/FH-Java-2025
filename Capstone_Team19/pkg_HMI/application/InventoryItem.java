package application;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class InventoryItem {

    private SimpleStringProperty itemName;
    private SimpleIntegerProperty quantity;

    public InventoryItem(String itemName, int quantity) {
        this.itemName = new SimpleStringProperty(itemName);  // SimpleStringProperty for item name
        this.quantity = new SimpleIntegerProperty(quantity);  // SimpleIntegerProperty for quantity
    }

    // Getter for item name
    public String getItemName() {
        return itemName.get();
    }

    // Getter for quantity
    public int getQuantity() {
        return quantity.get();
    }

    // Property for item name, so TableView can bind to it
    public SimpleStringProperty itemNameProperty() {
        return itemName;
    }

    // Property for quantity, so TableView can bind to it
    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    // Setter for quantity to update the quantity value
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }
}
