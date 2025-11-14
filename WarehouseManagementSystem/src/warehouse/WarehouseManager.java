package warehouse;

import equipmentManager.ChargingStation;
import equipmentManager.Robot;
import warehouse.datamanager.InventoryDataPacket;
import warehouse.datamanager.WarehouseDataPacket;

import java.awt.*;
import java.util.*;
import java.util.List;


/**
 * WarehouseManager
 */

public class WarehouseManager {
    public final WarehouseFloorManager floorManager;
    public final Inventory inventory;


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
//    public boolean removeObjectFromFloor (String objectID){
//        return floorManager.removeObject(objectID);
//    }
    public WarehouseObject getObjectFromFloor (String objectID){
        return floorManager.getObjectById(objectID);
    }

//    public Optional<WarehouseObject> getFloorObjectByID(String objectID){
//        return floorManager.getObjectById(objectID);
//    }

    public Collection<WarehouseObject> getFloorObjects(){
        return floorManager.getAllObjects();
    }

    public StorageShelf getStorageShelf(String shelfID){
        return floorManager.getStorageShelf(shelfID);
    }

    public Point getProductLocationByProductID(String productID){return inventory.geInventoryItem(productID).getShelf().getLocation();}

    public StorageShelf getStorageShelfByProductID(String productID){return inventory.geInventoryItem(productID).getShelf();}

    public List<WarehouseDataPacket> exportFloorData(){
        return floorManager.exportWarehouseData();
    }

    // Return all packing stations on the floor
    public List<PackingStation> getAllPackingStations() {
        List<PackingStation> stations = new ArrayList<>();
        for (WarehouseObject obj : WarehouseManager.this.floorManager.getAllObjects()) {
            if (obj.getObjectType().equals(WahouseObjectType.PackingStation)) {
                stations.add((PackingStation) obj);
            }
        }
        return stations;
    }

    // Return all storage shelves on the floor
    public List<StorageShelf> getAllStorageShelves() {
        List<StorageShelf> shelves = new ArrayList<>();
        for (WarehouseObject obj : WarehouseManager.this.floorManager.getAllObjects()) {
            if (obj instanceof StorageShelf shelf) {
                shelves.add(shelf);
            }
        }
        return shelves;
    }

    // Return all charging stations on the floor
    public List<ChargingStation> getAllChargingStations() {
        List<ChargingStation> stations = new ArrayList<>();
        for (WarehouseObject obj : WarehouseManager.this.floorManager.getAllObjects()) {
            if (obj.getObjectType().equals(WahouseObjectType.ChargingStation)) {
                stations.add((ChargingStation) obj);
            }
        }
        return stations;
    }

    public List<LoadingStation> getAllLoadingStations() {
        List<LoadingStation> stations = new ArrayList<>();
        for (WarehouseObject obj : WarehouseManager.this.floorManager.getAllObjects()) {
            if (obj.getObjectType().equals(WahouseObjectType.LoadingStation)) {
                stations.add((LoadingStation) obj);
            }
        }
        return stations;
    }
    
    public List<Robot> getAllRobots() {
        List<Robot> robots = new ArrayList<>();
        for (WarehouseObject obj : WarehouseManager.this.floorManager.getAllObjects()) {
            if (obj.getObjectType().equals(WahouseObjectType.Robot)) {
                robots.add((Robot) obj);
            }
        }
        return robots;
    }

    public List<WarehouseObject> getAllWarehouseObjects() {
    	List<WarehouseObject> objects =  new ArrayList<>();
        for (WarehouseObject obj : WarehouseManager.this.floorManager.getAllObjects()) {
            objects.add(obj);
        }
    	return objects;
    }

    public Rectangle getWarehouseArea() {
        return this.floorManager.getWarehouseFloor();
    }

}
