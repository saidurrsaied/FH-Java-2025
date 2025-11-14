package taskManager.unitTests;

import taskManager.Task;
import taskManager.TaskCreationException;
import taskManager.TaskManager;
import warehouse.*;
import warehouse.exceptions.InventoryException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TaskManager smoke tests: verify enqueue and validation paths.
 * Log format aligns with other tests: [PASS]/[FAIL] lines.
 */
public class TaskMangerTest {

	private static class Ctx {
		BlockingQueue<Task> q;
		WarehouseManager wm;
		TaskManager tm;
		String okProductId;
		LoadingStation loading;
		StorageShelf shelf;
	}

	public static void main(String[] args) {
		System.out.println("===== TASK MANAGER TESTS =====");
		Ctx ctx = setup();

		testCreateOrder_enqueues(ctx);
		testCreateOrder_nullProduct_chains(ctx);
		testCreateOrder_zeroQty_chains(ctx);
		testCreateOrder_missingProduct_inventoryException(ctx);
		testCreateStock_enqueues(ctx);
		testMixed_enqueues(ctx);

		System.out.println("===== ALL TESTS FINISHED =====");
	}

	private static Ctx setup() {
		Ctx c = new Ctx();
		c.q = new LinkedBlockingQueue<>();
		c.wm = new WarehouseManager(12, 12);

		c.shelf = new StorageShelf("S1", 5, 5, WahouseObjectType.StorageShelf);
		c.loading = new LoadingStation("L1", 2, 2, WahouseObjectType.LoadingStation);
		c.wm.addObjectToFloor(c.shelf);
		c.wm.addObjectToFloor(c.loading);

		Product prod = new Product("Widget", "PROD-OK");
		c.wm.addProductToInventory(prod, 5, c.shelf.getId());
		c.okProductId = prod.getProductID();

		c.tm = new TaskManager(c.q, c.wm);
		return c;
	}

	private static void testCreateOrder_enqueues(Ctx c) {
		System.out.print("Order valid → enqueue: ");
		try {
			int before = c.q.size();
			c.tm.createNewOrder(c.okProductId, 2);
			int after = c.q.size();
			logPass(after == before + 1);
				} catch (TaskCreationException e) { logFail("unexpected TaskCreationException: " + e.getMessage()); }
					catch (RuntimeException e) { logUnexpected(e); }
	}

	private static void testCreateOrder_nullProduct_chains(Ctx c) {
		System.out.print("Order null productID → TaskCreationException: ");
		try {
			c.tm.createNewOrder(null, 1);
			logFail("expected TaskCreationException");
		} catch (TaskCreationException e) {
			expectCauseContains(e, "Order ID and productID cannot be null or blank");
		} catch (Exception e) { logUnexpected(e); }
	}

	private static void testCreateOrder_zeroQty_chains(Ctx c) {
		System.out.print("Order qty=0 → TaskCreationException: ");
		try {
			c.tm.createNewOrder(c.okProductId, 0);
			logFail("expected TaskCreationException");
		} catch (TaskCreationException e) {
			expectCauseContains(e, "Quantity must be greater than zero");
		} catch (Exception e) { logUnexpected(e); }
	}

	private static void testCreateOrder_missingProduct_inventoryException(Ctx c) {
		System.out.print("Order missing product → InventoryException: ");
		try {
			c.tm.createNewOrder("MISSING", 1);
			logFail("expected InventoryException");
		} catch (InventoryException e) {
			logPass(true);
		} catch (TaskCreationException e) { logFail("expected InventoryException, got TaskCreationException"); }
		  catch (RuntimeException e) { logUnexpected(e); }
	}

	private static void testCreateStock_enqueues(Ctx c) {
		System.out.print("Stock valid → enqueue: ");
		try {
			int before = c.q.size();
			c.tm.createNewStock("LST01", c.okProductId, 2);
			int after = c.q.size();
			logPass(after == before + 1);
				} catch (TaskCreationException e) { logFail("unexpected TaskCreationException: " + e.getMessage()); }
					catch (RuntimeException e) { logUnexpected(e); }
	}

	private static void testMixed_enqueues(Ctx c) {
		System.out.print("Mixed (Order+Stock) → both enqueued: ");
		try {
			int before = c.q.size();
			c.tm.createNewOrder(c.okProductId, 1);
			c.tm.createNewStock("LST01", c.okProductId, 2);
			int after = c.q.size();
			logPass(after == before + 2);
				} catch (TaskCreationException e) { logFail("unexpected TaskCreationException: " + e.getMessage()); }
					catch (RuntimeException e) { logUnexpected(e); }
	}

	// ---- Helpers ----
	private static void logPass(boolean ok) { System.out.println(ok ? "[PASS]" : "[FAIL]"); }
	private static void logFail(String msg) { System.out.println("[FAIL] " + msg); }
	private static void logUnexpected(Exception e) { System.out.println("[FAIL] unexpected: " + e); }

	private static void expectCauseContains(TaskCreationException e, String text) {
		Throwable cause = e.getCause();
		String causeMsg = (cause != null ? cause.getMessage() : null);
		boolean ok = (causeMsg != null && causeMsg.contains(text));
		System.out.println(ok ? "[PASS]" : ("[FAIL] cause=" + causeMsg));
	}
}
