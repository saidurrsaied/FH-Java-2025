package wms.wmsjfx.taskManager;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.warehouse.LoadingStation;
import wms.wmsjfx.warehouse.Product;
import wms.wmsjfx.warehouse.StorageShelf;
import wms.wmsjfx.warehouse.WahouseObjectType;
import wms.wmsjfx.warehouse.WarehouseManager;
import wms.wmsjfx.warehouse.exceptions.InventoryException;

/**
 * Tests for TaskManager task creation validation paths.
 */
class TaskCreationValidationTest {

    @Test
    @DisplayName("Order creation: requesting quantity greater than available throws TaskCreationException and enqueues nothing")
    void orderQuantityGreaterThanAvailable_throws() {
        // Arrange: world with one shelf and product quantity 5
        WarehouseManager wm = new WarehouseManager(6, 6);
        StorageShelf shelf = new StorageShelf("S1", 1, 1, WahouseObjectType.StorageShelf);
        wm.addObjectToFloor(shelf);
        Product p = new Product("Widget", "P1");
        wm.addProductToInventory(p, 5, "S1");

        BlockingQueue<Task> queue = new ArrayBlockingQueue<>(16);
        TaskManager tm = new TaskManager(queue, wm);

        // Act + Assert: creating order for 10 (>5) must fail with TaskCreationException
        assertThrows(TaskCreationException.class, () -> tm.createNewOrder("P1", 10));
        assertEquals(0, queue.size(), "No task should be enqueued on failed order creation");
    }

    @Test
    @DisplayName("Stock creation: product/shelf does not exist -> InventoryException thrown; nothing enqueued")
    void stockForNonExistingProductShelf_throwsInventoryException() {
        // Arrange: floor with LoadingStation only; NO product in inventory, so no shelf mapping
        WarehouseManager wm = new WarehouseManager(6, 6);
        LoadingStation ls = new LoadingStation("L1", 0, 0, WahouseObjectType.LoadingStation);
        wm.addObjectToFloor(ls);

        BlockingQueue<Task> queue = new ArrayBlockingQueue<>(16);
        TaskManager tm = new TaskManager(queue, wm);

        // Act + Assert: creating stock task for unknown product should bubble up InventoryException
        assertThrows(InventoryException.class, () -> tm.createNewStock("L1", "UNKNOWN", 3));
        assertEquals(0, queue.size(), "No task should be enqueued on failed stock creation");
    }
}
