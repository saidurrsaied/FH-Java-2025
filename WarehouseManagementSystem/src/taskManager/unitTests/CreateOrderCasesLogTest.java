package taskManager.unitTests;

import taskManager.Task;
import taskManager.TaskManager;
import taskManager.TaskCreationException;
import warehouse.WarehouseManager;
import warehouse.StorageShelf;
import warehouse.Product;
import warehouse.WahouseObjectType;
import warehouse.exceptions.InventoryException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Log-based tests for TaskManager.createNewOrder(productID, quantity)
 * Validations come from OrderTask constructor and Inventory.
 */
public class CreateOrderCasesLogTest {

    private static class Ctx {
        BlockingQueue<Task> q;
        WarehouseManager wm;
        TaskManager tm;
        String okProductId;
    }

    public static void main(String[] args) {
        System.out.println("===== CREATE ORDER TESTS =====");
        Ctx ctx = setup();

        case1_nullProduct_shouldChain(ctx);
        case2_blankProduct_shouldChain(ctx);
        case3_quantityZero_shouldChain(ctx);
        case4_productNotFound_runtimeInventory(ctx);
        case5_quantityTooLarge_shouldChain(ctx);
        case6_validOrder_enqueued(ctx);

        System.out.println("===== ALL TESTS FINISHED =====");
    }

    private static Ctx setup() {
        Ctx c = new Ctx();
        c.q = new LinkedBlockingQueue<>();
        c.wm = new WarehouseManager(12, 12);

        // Create one shelf and one product in inventory
        StorageShelf shelf = new StorageShelf("S1", 5, 5, WahouseObjectType.StorageShelf);
        c.wm.addObjectToFloor(shelf);
        Product prod = new Product("Widget", "PROD-OK");
        c.wm.addProductToInventory(prod, 5, shelf.getId());
        c.okProductId = prod.getProductID();

        c.tm = new TaskManager(c.q, c.wm);
        return c;
    }

    private static void case1_nullProduct_shouldChain(Ctx c) {
        System.out.print("Case 1 (productID=null): ");
        try {
            c.tm.createNewOrder(null, 1);
            System.out.println("[FAIL] expected TaskCreationException");
        } catch (TaskCreationException e) {
            expectCauseContains(e, "Order ID and productID cannot be null or blank");
        } catch (Exception e) {
            System.out.println("[FAIL] unexpected: " + e);
        }
    }

    private static void case2_blankProduct_shouldChain(Ctx c) {
        System.out.print("Case 2 (productID blank): ");
        try {
            c.tm.createNewOrder("  \t", 1);
            System.out.println("[FAIL] expected TaskCreationException");
        } catch (TaskCreationException e) {
            expectCauseContains(e, "Order ID and productID cannot be null or blank");
        } catch (Exception e) {
            System.out.println("[FAIL] unexpected: " + e);
        }
    }

    private static void case3_quantityZero_shouldChain(Ctx c) {
        System.out.print("Case 3 (quantity=0): ");
        try {
            c.tm.createNewOrder(c.okProductId, 0);
            System.out.println("[FAIL] expected TaskCreationException");
        } catch (TaskCreationException e) {
            expectCauseContains(e, "Quantity must be greater than zero");
        } catch (Exception e) {
            System.out.println("[FAIL] unexpected: " + e);
        }
    }

    private static void case4_productNotFound_runtimeInventory(Ctx c) {
        System.out.print("Case 4 (product not found): ");
        try {
            c.tm.createNewOrder("MISSING", 1);
            System.out.println("[FAIL] expected InventoryException");
        } catch (InventoryException ex) {
            System.out.println("[PASS] " + ex.getMessage());
        } catch (Exception e) {
            System.out.println("[FAIL] unexpected: " + e);
        }
    }

    private static void case5_quantityTooLarge_shouldChain(Ctx c) {
        System.out.print("Case 5 (quantity too large): ");
        try {
            c.tm.createNewOrder(c.okProductId, 999);
            System.out.println("[FAIL] expected TaskCreationException");
        } catch (TaskCreationException e) {
            expectCauseContains(e, "Quantity is not enough");
        } catch (Exception e) {
            System.out.println("[FAIL] unexpected: " + e);
        }
    }

    private static void case6_validOrder_enqueued(Ctx c) {
        System.out.print("Case 6 (valid order): ");
        try {
            int before = c.q.size();
            c.tm.createNewOrder(c.okProductId, 2);
            int after = c.q.size();
            System.out.println(after == before + 1 ? "[PASS]" : "[FAIL] queue not incremented");
        } catch (Exception e) {
            System.out.println("[FAIL] unexpected: " + e);
        }
    }

    private static void expectCauseContains(TaskCreationException e, String text) {
        String causeMsg = (e.getCause() != null ? e.getCause().getMessage() : null);
        boolean ok = (causeMsg != null && causeMsg.contains(text));
        System.out.println(ok ? "[PASS]" : ("[FAIL] cause=" + causeMsg));
    }
}
