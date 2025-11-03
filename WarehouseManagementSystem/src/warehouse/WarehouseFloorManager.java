package warehouse;

import warehouse.datamanager.WarehouseDataPacket;
import warehouse.exceptions.FloorException;

import java.awt.Rectangle;
import java.util.*;


/**
 * WarehouseFloorManager
 * Methods:
 *  addObject: Add an asset to the warehouse floor if it is not overlapping with any other asset.
 *  removeObject: Remove an asset from the warehouse floor.
 *  getObjectById: Get an asset from the warehouse floor by its ID.
 *  getAllObjects: Get all assets from the warehouse floor.
 *  getStorageShelf: Get a storage shelf from the warehouse floor by its ID.
 *
 */


public class WarehouseFloorManager {
    private final Rectangle warehouseFloor;
    private final Map<String, WarehouseObject> objectMap = new HashMap<>();
    private final List<WarehouseObject> objectList = new ArrayList<>();

    public WarehouseFloorManager(int width, int length) {
        this.warehouseFloor = new Rectangle(0, 0, width, length);
    }

    /**
    * Add an asset to the warehouse floor if it is not overlapping with any other asset.
    * */
    public boolean addObject(WarehouseObject asset) {
//        Rectangle area = asset.getOccupiedArea();
//        if (!warehouseFloor.contains(area)) {
//            System.out.println("Out of warehouse boundaries");
//        return false;}
//
//        for (WarehouseObject existingObject : objectList) {
//            if (existingObject.getOccupiedArea().intersects(area)) {
//                System.out.println("Overlap detected: " + asset.getId() + " overlaps with " + existingObject.getId());
//                return false;
//            }
//        }

        objectList.add(asset);
        objectMap.put(asset.getId(), asset);
        System.out.println("Added object: " + asset.getId());
        return true;
    }

    public boolean removeObject(String objectId) {
        WarehouseObject obj = objectMap.remove(objectId);
        if (obj == null) return false;
        else { objectList.remove(obj);
        return true;}
    }

    public Optional<WarehouseObject> getObjectById(String objectId) {
        return Optional.ofNullable(objectMap.get(objectId));
    }

    public Collection<WarehouseObject> getAllObjects() {
        return Collections.unmodifiableCollection(objectMap.values());
    }

    public StorageShelf getStorageShelf(String shelfID) {
        WarehouseObject obj = objectMap.get(shelfID);
        if (!(obj instanceof StorageShelf)) {
            throw new FloorException("Shelf not found: " + shelfID);
        }
        else {
            return (StorageShelf) obj;
        }


    }

    /**
     * Export the warehouse data as a list of WarehouseDataPackets
     * */

    public List<WarehouseDataPacket> exportWarehouseData() {
        List<WarehouseDataPacket> dataPacket = new ArrayList<>();
        for (WarehouseObject obj : objectMap.values()) {
            dataPacket.add(
                    new WarehouseDataPacket(
                        obj.getId(),
                        obj.getObjectType().toString(),
                        obj.getLocation().x,
                        obj.getLocation().y)
                        );
        }
        return dataPacket;
    }

}
