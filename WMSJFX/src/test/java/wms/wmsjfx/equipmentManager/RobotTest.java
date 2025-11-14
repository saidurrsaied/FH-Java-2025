package wms.wmsjfx.equipmentManager;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wms.wmsjfx.pathFinding.PathFinding;
import wms.wmsjfx.taskManager.ChargeTask;
import wms.wmsjfx.taskManager.OrderTask;
import wms.wmsjfx.taskManager.Task;
import wms.wmsjfx.taskManager.TaskManager;
import wms.wmsjfx.taskManager.TaskType;
import wms.wmsjfx.warehouse.LoadingStation;
import wms.wmsjfx.warehouse.PackingStation;
import wms.wmsjfx.warehouse.Product;
import wms.wmsjfx.warehouse.StorageShelf;
import wms.wmsjfx.warehouse.WahouseObjectType;
import wms.wmsjfx.warehouse.WarehouseManager;

/**
 * Robot end-to-end execution tests using real EquipmentManager
 * with a minimal world and zero robots registered on the floor
 * (so EM won't auto-start any robot threads).
 *
 * We keep all coordinates the same so move paths are empty, making tests fast and deterministic.
 */
class RobotTest {

    private Thread robotThread; // ensure cleanup

    @AfterEach
    void tearDown() throws Exception {
        if (robotThread != null && robotThread.isAlive()) {
            robotThread.interrupt();
            robotThread.join(1000);
        }
    }

    private static class World {
        final WarehouseManager wm;
        final EquipmentManager em;
        final PackingStation packing;
        final LoadingStation loading;
        final ChargingStation charging;
        final StorageShelf shelf;

        World(Point p) {
            this.wm = new WarehouseManager(6, 6);
            this.shelf = new StorageShelf("S1", p.x, p.y, WahouseObjectType.StorageShelf);
            this.packing = new PackingStation("P1", p.x, p.y, WahouseObjectType.PackingStation);
            this.loading = new LoadingStation("L1", p.x, p.y, WahouseObjectType.LoadingStation);
            this.charging = new ChargingStation("C1", p.x, p.y, WahouseObjectType.ChargingStation);
            wm.addObjectToFloor(shelf);
            wm.addObjectToFloor(packing);
            wm.addObjectToFloor(loading);
            wm.addObjectToFloor(charging);

            // Inventory: add one product located at shelf
            Product product = new Product("Widget", "P1");
            wm.addProductToInventory(product, 10, "S1");

            BlockingQueue<Task> q = new ArrayBlockingQueue<>(16);
            PathFinding path = new PathFinding(wm);
            this.em = new EquipmentManager(wm, q, path); // 0 robots on floor
        }
    }

    private Thread startRobot(Robot r) {
        Thread t = new Thread(r, "Robot-Thread-Test");
        t.start();
        this.robotThread = t;
        return t;
    }

    private boolean waitUntil(Check c, long timeoutMs) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            if (c.ok()) return true;
            Thread.sleep(5);
        }
        return c.ok();
    }

    @FunctionalInterface
    private interface Check { boolean ok(); }

    @Test
    @DisplayName("Robot executes OrderTask: picks, gets packing station, drops, inventory decreases")
    void robotExecutesOrderTask_endToEnd() throws Exception {
        World world = new World(new Point(0, 0));

        // Robot starts at same position => no movement delay
        Robot robot = new Robot("R-1", new Point(0, 0), null, WahouseObjectType.Robot);
        robot.setEquipmentManager(world.em);

        startRobot(robot);

        int initialQty = world.wm.getProductQuantity("P1");

        OrderTask order = new OrderTask("Order-1", "P1", 2, world.wm);
        robot.assignTask(order);

        // Wait until quantity decreased or timeout
        assertTrue(waitUntil(() -> world.wm.getProductQuantity("P1") == initialQty - 2, 2000),
                "Inventory should decrease by order quantity");
    }

    @Test
    @DisplayName("Robot executes StockTask: acquires LoadingStation, drops to shelf, inventory increases")
    void robotExecutesStockTask_endToEnd() throws Exception {
        World world = new World(new Point(1, 1));
        Robot robot = new Robot("R-2", new Point(1, 1), null, WahouseObjectType.Robot);
        robot.setEquipmentManager(world.em);
        startRobot(robot);

        int initialQty = world.wm.getProductQuantity("P1");

        wms.wmsjfx.taskManager.StockTask stock = new wms.wmsjfx.taskManager.StockTask(
                "Stock-1", "P1", world.loading, 3, world.wm);
        robot.assignTask(stock);

        assertTrue(waitUntil(() -> world.wm.getProductQuantity("P1") == initialQty + 3, 2000),
                "Inventory should increase by stock quantity");

        // LoadingStation should be FREE again after task
        assertEquals(ObjectState.FREE.toString(), world.loading.getState());
    }

    @Test
    @DisplayName("ChargeTask sets battery to 100% and releases station")
    void robotChargesToFull() throws Exception {
        World world = new World(new Point(2, 2));
        Robot robot = new Robot("R-3", new Point(2, 2), null, WahouseObjectType.Robot);
        robot.setEquipmentManager(world.em);
        startRobot(robot);

        // Set battery to 95% so sleep ~50ms
        robot.setBatteryPercentage(95);

        ChargeTask charge = new ChargeTask(world.charging, robot.getId());
        robot.assignTask(charge);

        assertTrue(waitUntil(() -> Math.abs(robot.getBatteryPercentage() - 100.0) < 0.0001, 2000),
                "Robot should be fully charged");

        // Charging station should be FREE again
        assertEquals(ObjectState.FREE.toString(), world.charging.getState());
    }

    @Test
    @DisplayName("Low battery after order -> EM assigns auto ChargeTask then GoToStart")
    void lowBatteryTriggersAutoChargeFlow() throws Exception {
        World world = new World(new Point(3, 3));
        Robot robot = new Robot("R-4", new Point(3, 3), null, WahouseObjectType.Robot);
        robot.setEquipmentManager(world.em);
        startRobot(robot);

        // Make battery low so EM will schedule a ChargeTask in reportFinishedTask
        robot.setBatteryPercentage(10);

        int initialQty = world.wm.getProductQuantity("P1");
        OrderTask order = new OrderTask("Order-2", "P1", 1, world.wm);
        robot.assignTask(order);

        // 1) Wait inventory decreased
        assertTrue(waitUntil(() -> world.wm.getProductQuantity("P1") == initialQty - 1, 3000));
        // 2) Wait battery becomes 100 (means ChargeTask executed)
        assertTrue(waitUntil(() -> Math.abs(robot.getBatteryPercentage() - 100.0) < 0.0001, 4000));
        // 3) Eventually robot should process GoToStart too; we can at least assert it doesn't have an active task for a moment
        assertTrue(waitUntil(() -> robot.getActiveTask() == null, 1000));
    }
}
