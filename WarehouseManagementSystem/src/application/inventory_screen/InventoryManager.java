package application.inventory_screen;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import warehouse.InventoryItem;
import warehouse.Product;
import warehouse.StorageShelf;
import warehouse.datamanager.InventoryDataPacket;
import warehouse.WahouseObjectType;
import warehouse.WarehouseManager;
public class InventoryManager {

	private static final ObservableList<InventoryItem> ITEMS = FXCollections.observableArrayList();

    public static void initializeInventory(List<InventoryDataPacket> inventoryDataList, WarehouseManager warehousemanager) {
        ITEMS.clear(); 
        for (InventoryDataPacket dataPacket : inventoryDataList) {
            ITEMS.add(warehousemanager.getInventoryItem(dataPacket.getProductId()));            
        }
    }

    public static ObservableList<InventoryItem> getItems() {
        return ITEMS;
    }

    public static InventoryItem findByName(String name) {
            for (InventoryItem item : ITEMS) {
            	String check_name=item.getProduct().getProductName();
                if (check_name == name) {
                    return item;
                }
            }
            return null; 
        }

    public static String getID(String name) {
        InventoryItem it = findByName(name);
        return it.getProduct().getProductID();  // Update the quantity in the InventoryItem
    }

    public static void incrementQuantity(String name, int increment) {
        InventoryItem it = findByName(name);
        it.addQuantity(increment);  // Update the quantity in the InventoryItem
    }
    
    
    public static InventoryItem convertToInventoryItem(InventoryDataPacket dataPacket) {
        Product product = new Product(dataPacket.getProductName(), dataPacket.getProductId());
        StorageShelf shelf = new StorageShelf(dataPacket.getShelfId(), dataPacket.getX(), dataPacket.getY(), WahouseObjectType.StorageShelf);
        int quantity = dataPacket.getQuantity();

        return new InventoryItem(product, quantity, shelf);
    }
    
    public static List<InventoryDataPacket> convertToInventoryDataPacketList(List<InventoryItem> inventoryItems) {
        List<InventoryDataPacket> dataPacketList = new ArrayList<>();
        
        for (InventoryItem item : inventoryItems) {
            InventoryDataPacket packet = convertToInventoryDataPacket(item);
            dataPacketList.add(packet);
        }       
        return dataPacketList;
    }
    
    public static InventoryDataPacket convertToInventoryDataPacket(InventoryItem inventoryItem) {
        String productId = inventoryItem.getProduct().getProductID();
        String productName = inventoryItem.getProduct().getProductName();
        String shelfId = inventoryItem.getShelf().getId();
        int posX = (int)inventoryItem.getShelf().getLocation().getX();
        int posY = (int)inventoryItem.getShelf().getLocation().getY();
        int quantity = inventoryItem.getQuantity();
        
        return new InventoryDataPacket(productId, productName, quantity, shelfId, posX, posY);
    }    
}
