package warehouse;

import warehouse.datapackets.InventoryDataPacket;
import warehouse.datapackets.WarehouseDataPacket;

import java.util.*;



/**
 * WarehouseManager
 */

public class WarehouseManager {
    private final WarehouseFloorManager floorManager;
    private final Inventory inventory;


    public WarehouseManager(int width, int length) {
        this.floorManager = new WarehouseFloorManager(width, length);
        this.inventory = new Inventory();
    }

/**  use provided mutation methods to access inventory and floor */
    public Map<String, InventoryItem> getInventory() {
    return Collections.unmodifiableMap(inventory.getAllItems());
    }


    public Collection<WarehouseObject> getFloorOverview() {
        return Collections.unmodifiableCollection(floorManager.getAllObjects());
    }


    /**
     * Inventory Management Mutation Methods
     * */


    public InventoryItem getInventoryItem(String productID) {
        return inventory.geInventoryItem(productID);
    }

    public void removeInventoryItem(String productID) {
        inventory.removeProduct(productID);
    }


    public void addProductToInventory (Product product, int quantity, String shelfID){
        inventory.addProduct(product, quantity, floorManager.getStorageShelf(shelfID));
    }

    public void removeProductFromInventory (String productID){
        inventory.removeProduct(productID);
    }

    public boolean isProductInStock (String productID){
        return inventory.isProductInStock(productID);
    }

    public int getProductQuantity (String productID){
        return inventory.getProductQuantity(productID);
    }

    public void increaseProductQuantity (String productID, int amount){
        inventory.increaseProductQuantity(productID, amount);
    }

    public void decreaseProductQuantity (String productID, int amount){
        inventory.decreaseProductQuantity(productID, amount);
    }

    public List<InventoryDataPacket> exportInventoryData(){
        return inventory.exportInventoryData();
    }

    /**
     * Floor Management Mutation Methods
     * */


    public boolean addObjectToFloor (WarehouseObject object){
        return floorManager.addObject(object);
    }
    public boolean removeObjectFromFloor (String objectID){
        return floorManager.removeObject(objectID);
    }
    public Optional<WarehouseObject> getObjectFromFloor (String objectID){
        return floorManager.getObjectById(objectID);
    }

    public Optional<WarehouseObject> getFloorObjectByID(String objectID){
        return floorManager.getObjectById(objectID);
    }

    public Collection<WarehouseObject> getFloorObjects(){
        return floorManager.getAllObjects();
    }

    public StorageShelf getStorageShelf(String shelfID){
        return floorManager.getStorageShelf(shelfID);
    }


    public List<WarehouseDataPacket> exportFloorData(){
        return floorManager.exportWarehouseData();
    }
}