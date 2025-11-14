package wms.wmsjfx;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.equipmentManager.ChargingStation;
import wms.wmsjfx.equipmentManager.ObjectState;
import wms.wmsjfx.pathFinding.Node;
import wms.wmsjfx.pathFinding.NodeType;
import wms.wmsjfx.pathFinding.WarehouseMap;
import wms.wmsjfx.warehouse.LoadingStation;
import wms.wmsjfx.warehouse.PackingStation;
import wms.wmsjfx.warehouse.Product;
import wms.wmsjfx.warehouse.StorageShelf;
import wms.wmsjfx.warehouse.WahouseObjectType;
import wms.wmsjfx.warehouse.WarehouseObject;
import wms.wmsjfx.warehouse.Inventory;
import wms.wmsjfx.warehouse.datamanager.InventoryDataPacket;

/**
 * A lightweight integration test tying together multiple subsystems without UI/threads.
 *
 * Verifies that:
 * - Warehouse objects appear correctly on the map.
 * - Inventory data references shelf coordinates that match the map.
 * - Stations can transition states and remain consistent.
 */
class SystemIntegrationTest {

    private List<WarehouseObject> createObjects(StorageShelf shelf, PackingStation ps, LoadingStation ls, ChargingStation cs) {
        List<WarehouseObject> list = new ArrayList<>();
        list.add(shelf);
        list.add(ps);
        list.add(ls);
        list.add(cs);
        return list;
    }

    @Test
    @DisplayName("End-to-end: map placement aligns with inventory export; station states toggle")
    void endToEnd_basicFlow() throws Exception {
        // Arrange: create warehouse elements
        StorageShelf shelf = new StorageShelf("S1", 2, 3, WahouseObjectType.StorageShelf);
        PackingStation ps = new PackingStation("P1", 0, 0, WahouseObjectType.PackingStation);
        LoadingStation ls = new LoadingStation("L1", 4, 1, WahouseObjectType.LoadingStation);
        ChargingStation cs = new ChargingStation("C1", 1, 4, WahouseObjectType.ChargingStation);

        WarehouseMap map = new WarehouseMap(6, 6, createObjects(shelf, ps, ls, cs));

        // Verify map placements for each object
        assertEquals(NodeType.Shelf, map.getWarehouseObject(new Point(2, 3)).nodeType);
        assertEquals(NodeType.PackingStation, map.getWarehouseObject(new Point(0, 0)).nodeType);
        assertEquals(NodeType.LoadingStation, map.getWarehouseObject(new Point(4, 1)).nodeType);
        assertEquals(NodeType.ChargingStation, map.getWarehouseObject(new Point(1, 4)).nodeType);

        // Act: add inventory at the shelf and export
        Inventory inv = new Inventory();
        Product p = new Product("Widget", "P1");
        inv.addProduct(p, 7, shelf);
        List<InventoryDataPacket> packets = inv.exportInventoryData();

        // Assert: exported coordinates and shelf id match the shelf/map positions
        assertEquals(1, packets.size());
        InventoryDataPacket dp = packets.get(0);
        assertEquals("S1", dp.getShelfId());
        assertEquals(2, dp.getX());
        assertEquals(3, dp.getY());

        Node shelfNode = map.getWarehouseObject(new Point(dp.getX(), dp.getY()));
        assertNotNull(shelfNode);
        assertEquals(NodeType.Shelf, shelfNode.nodeType);

        // Stations: toggle states to ensure they work together in a flow
        assertEquals(ObjectState.FREE.toString(), cs.getState());
        cs.setState(ObjectState.BUSY);
        assertEquals(ObjectState.BUSY.toString(), cs.getState());
        cs.setState(ObjectState.FREE);
        assertEquals(ObjectState.FREE.toString(), cs.getState());

        assertEquals(ObjectState.FREE.toString(), ls.getState());
        ls.acquire();
        assertEquals(ObjectState.BUSY.toString(), ls.getState());
        ls.release();
        assertEquals(ObjectState.FREE.toString(), ls.getState());
    }
}
