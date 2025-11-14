package wms.wmsjfx;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.equipmentManager.ChargingStation;
import wms.wmsjfx.equipmentManager.ObjectState;
import wms.wmsjfx.pathFinding.NodeType;
import wms.wmsjfx.pathFinding.WarehouseMap;
import wms.wmsjfx.warehouse.Inventory;
import wms.wmsjfx.warehouse.LoadingStation;
import wms.wmsjfx.warehouse.PackingStation;
import wms.wmsjfx.warehouse.Product;
import wms.wmsjfx.warehouse.StorageShelf;
import wms.wmsjfx.warehouse.WahouseObjectType;
import wms.wmsjfx.warehouse.WarehouseObject;
import wms.wmsjfx.warehouse.datamanager.InventoryDataPacket;

/**
 * Additional end-to-end scenarios focused on system behavior without UI/threads (except one
 * bounded contention test on LoadingStation's semaphore).
 */
class SystemIntegrationScenariosTest {

    private List<WarehouseObject> createObjects(StorageShelf s1, StorageShelf s2,
                                                PackingStation ps, LoadingStation ls,
                                                ChargingStation cs) {
        List<WarehouseObject> list = new ArrayList<>();
        list.add(s1);
        list.add(s2);
        list.add(ps);
        list.add(ls);
        list.add(cs);
        return list;
    }

    @Test
    @DisplayName("Sequential multiple orders for the same product across a single shelf")
    void sequentialOrders_sameProduct_singleShelf() {
        // Arrange map and stations
        StorageShelf shelf = new StorageShelf("S1", 2, 2, WahouseObjectType.StorageShelf);
        StorageShelf spareShelf = new StorageShelf("S2", 4, 4, WahouseObjectType.StorageShelf);
        PackingStation ps = new PackingStation("P1", 0, 0, WahouseObjectType.PackingStation);
        LoadingStation ls = new LoadingStation("L1", 5, 0, WahouseObjectType.LoadingStation);
        ChargingStation cs = new ChargingStation("C1", 1, 5, WahouseObjectType.ChargingStation);
        WarehouseMap map = new WarehouseMap(6, 6, createObjects(shelf, spareShelf, ps, ls, cs));

        // Verify placements
        assertEquals(NodeType.Shelf, map.getWarehouseObject(new Point(2, 2)).nodeType);
        assertEquals(NodeType.Shelf, map.getWarehouseObject(new Point(4, 4)).nodeType);

        // Inventory setup
        Inventory inv = new Inventory();
        Product widget = new Product("Widget", "P1");
        inv.addProduct(widget, 20, shelf);
        assertFalse(shelf.isAvailable());

        // Sequential orders (simulate back-to-back orders)
        inv.decreaseProductQuantity("P1", 3);
        inv.decreaseProductQuantity("P1", 4);
        inv.decreaseProductQuantity("P1", 5);
        assertEquals(8, inv.getProductQuantity("P1"));

        // Restock after orders
        inv.increaseProductQuantity("P1", 7);
        assertEquals(15, inv.getProductQuantity("P1"));

        // Drive to zero then remove item -> shelf becomes available
        inv.decreaseProductQuantity("P1", 15);
        assertEquals(0, inv.getProductQuantity("P1"));
        inv.removeProduct("P1");
        assertTrue(shelf.isAvailable());
        assertNull(shelf.getStoredProduct());

        // Now add a different product to the same shelf should succeed
        Product gadget = new Product("Gadget", "P2");
        inv.addProduct(gadget, 5, shelf);
        assertEquals(5, inv.getProductQuantity("P2"));
        assertFalse(shelf.isAvailable());

        // Export and verify shelf coordinates align with map
        List<InventoryDataPacket> packets = inv.exportInventoryData();
        assertEquals(1, packets.size());
        InventoryDataPacket dp = packets.get(0);
        assertEquals("S1", dp.getShelfId());
        assertEquals(2, dp.getX());
        assertEquals(2, dp.getY());
        assertEquals(NodeType.Shelf, map.getWarehouseObject(new Point(dp.getX(), dp.getY())).nodeType);
    }

    @Test
    @DisplayName("Interleaved restocking and ordering across two shelves/products (serialized model)")
    void interleavedRestockAndOrders_serialized() {
        StorageShelf s1 = new StorageShelf("S1", 1, 1, WahouseObjectType.StorageShelf);
        StorageShelf s2 = new StorageShelf("S2", 3, 1, WahouseObjectType.StorageShelf);
        PackingStation ps = new PackingStation("P1", 0, 0, WahouseObjectType.PackingStation);
        LoadingStation ls = new LoadingStation("L1", 5, 0, WahouseObjectType.LoadingStation);
        ChargingStation cs = new ChargingStation("C1", 2, 4, WahouseObjectType.ChargingStation);
        WarehouseMap map = new WarehouseMap(7, 5, createObjects(s1, s2, ps, ls, cs));

        Inventory inv = new Inventory();
        Product pA = new Product("Apple", "A");
        Product pB = new Product("Banana", "B");

        inv.addProduct(pA, 10, s1); // A:10 on S1
        inv.addProduct(pB, 5, s2);  // B:5 on S2

        // Interleave operations (as would happen over time)
        inv.decreaseProductQuantity("A", 3); // A:7
        inv.increaseProductQuantity("B", 4); // B:9
        inv.decreaseProductQuantity("B", 2); // B:7
        inv.increaseProductQuantity("A", 6); // A:13
        inv.decreaseProductQuantity("A", 8); // A:5

        assertEquals(5, inv.getProductQuantity("A"));
        assertEquals(7, inv.getProductQuantity("B"));

        // Export and verify both entries map to correct shelf nodes
        List<InventoryDataPacket> packets = inv.exportInventoryData();
        assertEquals(2, packets.size());
        for (InventoryDataPacket dp : packets) {
            NodeType nt = map.getWarehouseObject(new Point(dp.getX(), dp.getY())).nodeType;
            assertEquals(NodeType.Shelf, nt);
            if (dp.getProductId().equals("A")) {
                assertEquals("S1", dp.getShelfId());
                assertEquals(5, dp.getQuantity());
            } else if (dp.getProductId().equals("B")) {
                assertEquals("S2", dp.getShelfId());
                assertEquals(7, dp.getQuantity());
            } else {
                fail("Unexpected product in export: " + dp.getProductId());
            }
        }
    }

    @Test
    @DisplayName("LoadingStation contention: two actors acquire/release with semaphore exclusivity")
    void loadingStationContention_bounded() throws Exception {
        LoadingStation ls = new LoadingStation("L1", 5, 0, WahouseObjectType.LoadingStation);

        assertEquals(ObjectState.FREE.toString(), ls.getState());

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        final StringBuilder order = new StringBuilder();

        Runnable worker = () -> {
            try {
                start.await(1, TimeUnit.SECONDS);
                ls.acquire();
                // Record entry order; only one thread should be BUSY at a time
                synchronized (order) {
                    order.append("[").append(Thread.currentThread().getName()).append("]");
                }
                // Simulate short critical section
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                ls.release();
            } catch (InterruptedException e) {
                fail("Thread interrupted");
            } finally {
                done.countDown();
            }
        };

        Thread t1 = new Thread(worker, "T1");
        Thread t2 = new Thread(worker, "T2");
        t1.start();
        t2.start();
        start.countDown();

        assertTrue(done.await(2, TimeUnit.SECONDS), "Workers did not finish in time");

        // After both complete, station must be FREE
        assertEquals(ObjectState.FREE.toString(), ls.getState());
        // Order string must contain both threads in some sequence (exclusive access)
        String seq = order.toString();
        assertTrue(seq.contains("[T1]"));
        assertTrue(seq.contains("[T2]"));
    }
}
