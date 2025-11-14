package wms.wmsjfx.equipmentManager;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.pathFinding.PathFinding;
import wms.wmsjfx.taskManager.Task;
import wms.wmsjfx.warehouse.StorageShelf;
import wms.wmsjfx.warehouse.WahouseObjectType;
import wms.wmsjfx.warehouse.WarehouseManager;
import wms.wmsjfx.warehouse.Product;

/**
 * Verifies that EquipmentManager.stop(...) interrupts and joins dispatcher and robot threads.
 * This is a minimal integration test that does not rely on JavaFX lifecycle.
 */
class EquipmentManagerShutdownTest {

    @Test
    @DisplayName("EquipmentManager.stop interrupts dispatcher and robot threads and they terminate")
    void stopInterruptsAndJoinsThreads() throws Exception {
        // Arrange a small world with 1 robot so EM starts a robot thread
        WarehouseManager wm = new WarehouseManager(6, 6);
        // Floor: one shelf and one robot
        StorageShelf shelf = new StorageShelf("S1", 0, 0, WahouseObjectType.StorageShelf);
        wm.addObjectToFloor(shelf);
        Robot robot = new Robot("R-Stop", new Point(0, 0), null, WahouseObjectType.Robot);
        wm.addObjectToFloor(robot);

        // Add product so robot environment is valid if it starts processing later
        wm.addProductToInventory(new Product("Widget", "P1"), 1, "S1");

        BlockingQueue<Task> queue = new ArrayBlockingQueue<>(16);
        PathFinding path = new PathFinding(wm);
        EquipmentManager em = new EquipmentManager(wm, queue, path);

        // Start dispatcher in a dedicated thread
        Thread dispatcher = new Thread(em, "EM-Dispatcher-Test-Stop");
        em.registerDispatcherThread(dispatcher);
        dispatcher.start();

        // Sanity: robot thread should have started
        assertTrue(em.getAliveRobotThreadCount() >= 1);

        // Act: request stop and wait a short timeout
        em.stop(1500);

        // Assert: dispatcher terminated and robot threads are not alive
        dispatcher.join(100); // should already be done
        assertFalse(dispatcher.isAlive(), "Dispatcher thread should be terminated after stop()");
        assertEquals(0, em.getAliveRobotThreadCount(), "All robot threads should be terminated after stop()");
    }
}
