package wms.wmsjfx.taskManager;

import java.awt.Point;
import java.util.concurrent.BlockingQueue; // Dùng queue chung
import java.util.concurrent.atomic.AtomicInteger;

//import warehouse.WarehousePosition;

public class TaskManager {

    private final BlockingQueue<Task> taskSubmissionQueue;
    private final AtomicInteger orderIdCounter = new AtomicInteger(0);
    private final AtomicInteger stockIdCounter = new AtomicInteger(0);
    
    // Queue for sending tasks to equipment manager
    public TaskManager(BlockingQueue<Task> taskSubmissionQueue) {
        this.taskSubmissionQueue = taskSubmissionQueue;
    }

    // Method for creating Order task
    public void createNewOrder(Point orderPosition, String itemId, int quantity) throws TaskCreationException {

        int newId = orderIdCounter.incrementAndGet();
        String taskId = "Order-" + newId;

        // 1) Create a new Order (may throw OrderTaskException if invalid)
        final Task newTask;
        
        try {
            newTask = new OrderTask(taskId, itemId, orderPosition, quantity);
        } catch (OrderTaskException e) {
            // Chain the validation error from Order into a higher-level exception
            throw new TaskCreationException("Order validation failed for " + taskId, e);
        }

        // 2) Submit the task to the shared queue (may throw InterruptedException)
        try {
            taskSubmissionQueue.put(newTask);
            System.out.printf("[TaskManager] Submitted %s (%s x%d)%n", taskId, itemId, quantity);
        } catch (InterruptedException e) {
            // Restore the thread's interrupted status
            Thread.currentThread().interrupt();
            System.err.println("[TaskManager] Interrupted while submitting " + taskId);

            // Optionally rethrow as unchecked if you want to escalate this failure
            // throw new RuntimeException("Interrupted while enqueuing " + taskId, e);
        }
    }
    
 // Method for creating Stock task
    public void createNewStock(String itemName, Point unloadingArea, Point shelfLocation) {
    	int newId = stockIdCounter.incrementAndGet();
    	String taskId = "Stock-" + newId;
        Task newTask = new StockTask(taskId, itemName, unloadingArea, shelfLocation);
        
        try {
            // Push new task into queue
            taskSubmissionQueue.put(newTask);
            System.out.println("[TaskManager] Submitted new task " + itemName + " to shared queue.");
        } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             System.err.println("[TaskManager] Failed to submit task " + itemName);
        }
    }
}