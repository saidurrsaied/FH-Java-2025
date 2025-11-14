package wms.wmsjfx.taskManager;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.equipmentManager.EquipmentManager;
import wms.wmsjfx.pathFinding.PathFinding;
import wms.wmsjfx.warehouse.LoadingStation;
import wms.wmsjfx.warehouse.PackingStation;
import wms.wmsjfx.warehouse.Product;
import wms.wmsjfx.warehouse.StorageShelf;
import wms.wmsjfx.warehouse.WahouseObjectType;
import wms.wmsjfx.warehouse.WarehouseManager;
import wms.wmsjfx.warehouse.WarehouseObject;

/**
 * TaskManager + EquipmentManager integration with fewer robots than tasks.
 * We use ZERO robots to avoid background Robot threads. The EM dispatcher runs
 * and moves tasks to its pending queue because no robot can be assigned.
 */
class TaskManagerLimitedRobotsTest {

    private static class World {
        final WarehouseManager wm;
        final BlockingQueue<Task> queue;
        final TaskManager tm;
        final EquipmentManager em;
        final Thread emThread;

        World() {
            this.wm = new WarehouseManager(10, 10);
            // Floor objects
            StorageShelf shelf = new StorageShelf("S1", 2, 2, WahouseObjectType.StorageShelf);
            PackingStation ps = new PackingStation("P1", 0, 0, WahouseObjectType.PackingStation);
            LoadingStation ls = new LoadingStation("L1", 1, 0, WahouseObjectType.LoadingStation);
            wms.wmsjfx.equipmentManager.ChargingStation cs =
                    new wms.wmsjfx.equipmentManager.ChargingStation("C1", 3, 3, WahouseObjectType.ChargingStation);
            wm.addObjectToFloor(shelf);
            wm.addObjectToFloor(ps);
            wm.addObjectToFloor(ls);
            wm.addObjectToFloor(cs);

            // Inventory with one product located on shelf
            Product p = new Product("Widget", "P1");
            wm.addProductToInventory(p, 20, "S1");

            this.queue = new ArrayBlockingQueue<>(64);
            this.tm = new TaskManager(queue, wm);
            PathFinding path = new PathFinding(wm);
            this.em = new EquipmentManager(wm, queue, path); // 0 robots in wm, so no robot thread started
            this.emThread = new Thread(em, "EM-Dispatcher-Test");
        }
    }

    @Test
    @DisplayName("Multiple order and stock submitted with zero robots -> all tasks pending in EM")
    void multipleOrdersAndStocks_zeroRobots_allPending() throws Exception {
        World world = new World();
        world.emThread.start();

        // Submit tasks roughly at the same time
        world.tm.createNewOrder("P1", 3);
        world.tm.createNewStock("L1", "P1", 5);
        world.tm.createNewOrder("P1", 4);

        // Wait a short time for EM to dequeue and process submissions
        boolean settled = waitUntil(() -> world.em.getPendingTasks().size() == 3, 1000);
        assertTrue(settled, "Pending tasks should reach 3 within 1s");

        // Clean up dispatcher thread
        world.emThread.interrupt();
        world.emThread.join(1000);
        assertFalse(world.emThread.isAlive(), "EM dispatcher should stop after interrupt");
    }

    // Utility: wait until condition true or timeout
    private boolean waitUntil(Check c, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (c.ok()) return true;
            Thread.sleep(10);
        }
        return c.ok();
    }

    @FunctionalInterface
    private interface Check { boolean ok(); }
}
