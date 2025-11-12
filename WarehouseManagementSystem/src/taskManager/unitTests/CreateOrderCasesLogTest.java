//package taskManager.unitTests;
//
//import taskManager.Task;
//import taskManager.TaskManager;
//import taskManager.TaskCreationException;
//
//import java.awt.Point;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
///**
// * 5 log-based test cases for TaskManager.createNewOrder(...)
// * Demonstrates chained exceptions (OrderTaskException -> TaskCreationException).
// *
// * Assumptions in Order constructor validations (no enum, message-only):
// * - itemLocation == null                -> "Item location cannot be null"
// * - itemName == null/blank              -> "Item name cannot be null or blank"
// * - quantity <= 0                       -> "Quantity must be greater than zero"
// * - itemName equals "Item-Z" (example)  -> "Item-Z is out of stock"
// *
// * Note: orderId is generated inside TaskManager ("Order-N"), so we do NOT test blank orderId here.
// */
//public class CreateOrderCasesLogTest {
//
//    public static void main(String[] args) {
//        System.out.println("===== Running 5 Create-Order Cases (log-based) =====");
//
//        // Shared unbounded queue; we don't need a consumer for these tests
//        BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
//        TaskManager tm = new TaskManager(queue);
//
//        case1_nullPickupLocation_shouldChain(tm);
//        case2_blankItemName_shouldChain(tm);
//        case3_quantityZero_shouldChain(tm);
//        case4_nullItemName_shouldChain(tm);
//        case5_validOrder_shouldPass(tm);
//
//        System.out.println("===== All cases executed =====");
//    }
//
//    // Case 1: orderPosition == null -> expect TaskCreationException; cause msg "Item location cannot be null"
//    private static void case1_nullPickupLocation_shouldChain(TaskManager tm) {
//        System.out.print("Case 1 (null pickup location): ");
//        try {
//            tm.createNewOrder(null, "Item-A", 1);
//            System.out.println("❌ FAIL (expected TaskCreationException)");
//        } catch (TaskCreationException e) {
//            printChained(e, "Item location cannot be null");
//        } catch (Exception e) {
//            System.out.println("❌ FAIL (unexpected exception): " + e);
//        }
//    }
//
//    // Case 2: itemName blank -> expect TaskCreationException; cause msg "Item name cannot be null or blank"
//    private static void case2_blankItemName_shouldChain(TaskManager tm) {
//        System.out.print("Case 2 (blank item name): ");
//        try {
//            tm.createNewOrder(new Point(1, 1), "   ", 1);
//            System.out.println("❌ FAIL (expected TaskCreationException)");
//        } catch (TaskCreationException e) {
//            printChained(e, "Item name cannot be null or blank");
//        } catch (Exception e) {
//            System.out.println("❌ FAIL (unexpected exception): " + e);
//        }
//    }
//
//    // Case 3: quantity <= 0 -> expect TaskCreationException; cause msg "Quantity must be greater than zero"
//    private static void case3_quantityZero_shouldChain(TaskManager tm) {
//        System.out.print("Case 3 (quantity = 0): ");
//        try {
//            tm.createNewOrder(new Point(2, 2), "Item-B", 0);
//            System.out.println("❌ FAIL (expected TaskCreationException)");
//        } catch (TaskCreationException e) {
//            printChained(e, "Quantity must be greater than zero");
//        } catch (Exception e) {
//            System.out.println("❌ FAIL (unexpected exception): " + e);
//        }
//    }
//
//    // Case 4: out of stock sentinel (e.g., "Item-Z") -> expect TaskCreationException; cause msg "Item-Z is out of stock"
//    private static void case4_nullItemName_shouldChain(TaskManager tm) {
//        System.out.print("Case 4 (null item name): ");
//        try {
//            tm.createNewOrder(new Point(3, 3), null, 1);
//            System.out.println("❌ FAIL (expected TaskCreationException)");
//        } catch (TaskCreationException e) {
//            printChained(e, "Item name cannot be null or blank");
//        } catch (Exception e) {
//            System.out.println("❌ FAIL (unexpected exception): " + e);
//        }
//    }
//
//    // Case 5: valid order -> should NOT throw; success log printed by TaskManager
//    private static void case5_validOrder_shouldPass(TaskManager tm) {
//        System.out.print("Case 5 (valid order): ");
//        try {
//            tm.createNewOrder(new Point(10, 10), "Item-OK", 2);
//            System.out.println("✅ PASS");
//        } catch (Exception e) {
//            System.out.println("❌ FAIL (unexpected exception): " + e);
//        }
//    }
//
//    // Helper to print chained messages and verify expected root-cause text
//    private static void printChained(TaskCreationException e, String expectedCauseSnippet) {
//        String msg = e.getMessage();
//        String causeMsg = (e.getCause() != null ? e.getCause().getMessage() : null);
//
//        boolean hasCause = (e.getCause() != null);
//        boolean causeMatch = (causeMsg != null && causeMsg.contains(expectedCauseSnippet));
//
//        System.out.println((hasCause && causeMatch ? "✅ PASS" : "❌ FAIL")
//                + " | TaskCreationException: \"" + msg + "\""
//                + " | cause: " + (hasCause
//                    ? (e.getCause().getClass().getSimpleName() + " - \"" + causeMsg + "\"")
//                    : "null"));
//    }
//}
