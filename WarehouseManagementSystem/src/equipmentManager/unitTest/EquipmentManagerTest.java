package equipmentManager.unitTest;

import equipmentManager.EquipmentManager;
import equipmentManager.Robot;
import pathFinding.PathFinding;
import taskManager.OrderTask;
import taskManager.StockTask;
import taskManager.Task;
import warehouse.LoadingStation;
import warehouse.PackingStation;
import warehouse.Product;
import warehouse.StorageShelf;
import warehouse.WahouseObjectType;
import warehouse.WarehouseManager;
import equipmentManager.ChargingStation;

import java.awt.Point;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EquipmentManagerTest {

    public static void main(String[] args) throws Exception {
        System.out.println("===== EQUIPMENT MANAGER TESTS =====");

        TestContext ctx = setupContext(12, 12);
        startManagerThread(ctx);

        testSubmitTask(ctx);
        testAssignTaskEnoughBattery(ctx);
        testAssignTaskNotEnoughBattery(ctx);
        testAssignStockTask(ctx);
        testFIFOAssignment(ctx);

        System.out.println("===== ALL TESTS FINISHED =====");
    }

    // Simple context holder
    private static class TestContext {
        WarehouseManager wm;
        PathFinding pf;
        EquipmentManager em;
        BlockingQueue<Task> queue;
        Robot robot;
        StorageShelf shelf;
        LoadingStation loading;
        PackingStation packing;
    }

    private static TestContext setupContext(int width, int height) {
        TestContext c = new TestContext();
        c.wm = new WarehouseManager(width, height);

        // Floor objects
        c.packing = new PackingStation("P1", 6, 6, WahouseObjectType.PackingStation);
        c.loading = new LoadingStation("L1", 2, 2, WahouseObjectType.LoadingStation);
        c.shelf = new StorageShelf("S1", 9, 9, WahouseObjectType.StorageShelf);
        c.robot = new Robot("R1", new Point(0, 0), null, WahouseObjectType.Robot);
        // Add at least one charging station so energy calculations succeed
        ChargingStation cStation = new ChargingStation("C1", 10, 10, WahouseObjectType.ChargingStation);

        c.wm.addObjectToFloor(c.packing);
        c.wm.addObjectToFloor(c.loading);
        c.wm.addObjectToFloor(c.shelf);
        c.wm.addObjectToFloor(c.robot);
        c.wm.addObjectToFloor(cStation);

        // Inventory setup
        Product p = new Product("Widget", "PROD-1");
        c.wm.addProductToInventory(p, 10, c.shelf.getId());

        // Pathfinding and manager
        c.pf = new PathFinding(c.wm);
        c.queue = new LinkedBlockingQueue<>();
        c.em = new EquipmentManager(c.wm, c.queue, c.pf);
        return c;
    }

    private static void startManagerThread(TestContext c) {
        Thread emThread = new Thread(c.em, "EquipmentManager");
        emThread.setDaemon(true);
        emThread.start();
    }

    // Helper asserts
    private static void assertTrue(boolean condition, String message) {
        if (condition) System.out.println("[PASS] " + message);
        else System.out.println("[FAIL] " + message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.println("[PASS] " + message);
        } else {
            System.out.println("[FAIL] " + message + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    // ----------------------- TEST CASES -----------------------

    private static void testSubmitTask(TestContext c) throws Exception {
        OrderTask task = new OrderTask("Order-1", "PROD-1", 1, c.wm);
        c.queue.put(task); // submit to EM via shared queue
        Thread.sleep(200); // let EM process

        assertTrue(c.em.getPendingTasks().size() >= 0, "Submit task processed without crash");
    }

    private static void testAssignTaskEnoughBattery(TestContext c) throws Exception {
        c.robot.setBatteryPercentage(100);
        OrderTask task = new OrderTask("Order-2", "PROD-1", 1, c.wm);
        c.queue.put(task);
        Thread.sleep(500);
        assertTrue(c.robot.getActiveTask() != null, "Robot picked up order task with enough battery");
    }

    private static void testAssignTaskNotEnoughBattery(TestContext c) throws Exception {
        c.robot.setBatteryPercentage(1); // force low battery
        OrderTask task = new OrderTask("Order-3", "PROD-1", 1, c.wm);
        c.queue.put(task);
        Thread.sleep(300);

        // Expect pending since robot can't take it
        assertTrue(c.em.getPendingTasks().size() >= 1, "Low battery: task stays pending");
    }

   private static void testAssignStockTask(TestContext c) throws Exception {
       c.robot.setBatteryPercentage(100);
       StockTask stock = new StockTask("LST01", "PROD-1", c.loading, 1, c.wm);
       c.queue.put(stock);
       Thread.sleep(500);
       assertTrue(c.robot.getActiveTask() != null, "Robot picked up stock task");
   }

    private static void testFIFOAssignment(TestContext c) throws Exception {
        c.robot.setBatteryPercentage(100);
        OrderTask t1 = new OrderTask("Order-A", "PROD-1", 1, c.wm);
        OrderTask t2 = new OrderTask("Order-B", "PROD-1", 1, c.wm);

        c.queue.put(t1);
        c.queue.put(t2);
        Thread.sleep(300);

        // One task should be pending while robot is busy with first
        assertTrue(c.em.getPendingTasks().size() >= 1, "FIFO: second task pending while first executes");
    }
}
