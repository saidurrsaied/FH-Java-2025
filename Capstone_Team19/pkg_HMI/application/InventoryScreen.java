package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InventoryScreen {

    private static final ObservableList<InventoryItem> ITEMS = FXCollections.observableArrayList();

    static {
        // Seed a few items so you can test
        ITEMS.addAll(
            new InventoryItem("Item 1", 10),
            new InventoryItem("Item 2", 15),
            new InventoryItem("Item 3", 5)
        );
    }

    public static ObservableList<InventoryItem> getItems() {
        return ITEMS;
    }

    /** Returns the InventoryItem by exact name, or null if not found */
    public static InventoryItem findByName(String name) {
        if (name == null) return null;
        for (InventoryItem it : ITEMS) {
            if (name.equals(it.getItemName())) return it;
        }
        return null;
    }

    /** Update quantity to an absolute value. Returns true if item existed and was updated. */
    public static boolean incrementQuantity(String name, int increment) {
        InventoryItem it = findByName(name);
        if (it == null) return false;
        int newQuantity = it.getQuantity() + increment;  // Increment the current quantity
        it.setQuantity(newQuantity);  // Update the quantity in the InventoryItem
        return true;
    }
}
