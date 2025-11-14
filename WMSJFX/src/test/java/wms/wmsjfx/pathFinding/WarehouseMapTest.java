package wms.wmsjfx.pathFinding;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.equipmentManager.ChargingStation;
import wms.wmsjfx.warehouse.LoadingStation;
import wms.wmsjfx.warehouse.PackingStation;
import wms.wmsjfx.warehouse.StorageShelf;
import wms.wmsjfx.warehouse.WahouseObjectType;
import wms.wmsjfx.warehouse.WarehouseObject;

class WarehouseMapTest {

    private List<WarehouseObject> sampleObjects() {
        List<WarehouseObject> list = new ArrayList<>();
        list.add(new StorageShelf("S1", 1, 1, WahouseObjectType.StorageShelf));
        list.add(new StorageShelf("S2", 3, 3, WahouseObjectType.StorageShelf));
        list.add(new PackingStation("P1", 0, 4, WahouseObjectType.PackingStation));
        list.add(new LoadingStation("L1", 4, 0, WahouseObjectType.LoadingStation));
        list.add(new ChargingStation("C1", 2, 2, WahouseObjectType.ChargingStation));
        return list;
    }

    @Test
    @DisplayName("createWarehouseMap places objects and fills remaining as walkable None")
    void mapInitialization_placesObjectsAndDefaults() {
        WarehouseMap map = new WarehouseMap(5, 5, sampleObjects());

        // Specific object placements
        assertEquals(NodeType.Shelf, map.getWarehouseObject(new Point(1, 1)).nodeType);
        assertEquals(NodeType.Shelf, map.getWarehouseObject(new Point(3, 3)).nodeType);
        assertEquals(NodeType.PackingStation, map.getWarehouseObject(new Point(0, 4)).nodeType);
        assertEquals(NodeType.LoadingStation, map.getWarehouseObject(new Point(4, 0)).nodeType);
        assertEquals(NodeType.ChargingStation, map.getWarehouseObject(new Point(2, 2)).nodeType);

        // Default cell
        Node n = map.getWarehouseObject(new Point(0, 0));
        assertNotNull(n);
        assertEquals(NodeType.None, n.nodeType);
        assertTrue(n.getWalkable());
    }

    @Test
    @DisplayName("getNeighbors returns 8 for center, 5 for edge, 3 for corner (with diagonals)")
    void neighborsCounts() {
        WarehouseMap map = new WarehouseMap(5, 5, sampleObjects());

        // center (2,2) should have 8 neighbors
        Node center = map.getWarehouseObject(new Point(2, 2));
        assertEquals(8, map.getNeighbors(center).size());

        // edge (2,0) should have 5 neighbors
        Node edge = map.getWarehouseObject(new Point(2, 0));
        assertEquals(5, map.getNeighbors(edge).size());

        // corner (0,0) should have 3 neighbors
        Node corner = map.getWarehouseObject(new Point(0, 0));
        assertEquals(3, map.getNeighbors(corner).size());
    }
}
