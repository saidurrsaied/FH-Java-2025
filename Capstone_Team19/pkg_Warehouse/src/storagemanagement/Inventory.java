package storagemanagement;

import logging.Logger;

public class Inventory {

    public void addItem(String item) {
        // Log item added
        Logger.log_print("INFO", "inventory", "Item " + item + " added to inventory.");
    }

    public void orderItem(String item) {
        // Log item ordered for restocking
        Logger.log_print("INFO", "inventory", "Item " + item + " ordered for restocking.");
    }
}
