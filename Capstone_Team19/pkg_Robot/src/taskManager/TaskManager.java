package taskManager;

import java.awt.Point;
import java.util.concurrent.BlockingQueue; // Dùng queue chung
import java.util.concurrent.atomic.AtomicInteger;

import warehouse.WarehousePosition;

public class TaskManager {

    private final BlockingQueue<Task> taskSubmissionQueue;
    private final AtomicInteger orderIdCounter = new AtomicInteger(0);
    private final AtomicInteger stockIdCounter = new AtomicInteger(0);
    
    // Queue for sending tasks to equipment manager
    public TaskManager(BlockingQueue<Task> taskSubmissionQueue) {
        this.taskSubmissionQueue = taskSubmissionQueue;
    }

    // Method for creating Order task
    public void createNewOrder(Point orderPosition, String itemId, int quantity) {
    	int newId = orderIdCounter.incrementAndGet();
    	String taskId = "Order-" + newId;
        Task newTask = new Order(taskId, itemId, orderPosition, quantity);

        try {
            // Push new task into queue
            taskSubmissionQueue.put(newTask);
            System.out.println("[TaskManager] Submitted new task " + itemId + " to shared queue.");
        } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             System.err.println("[TaskManager] Failed to submit task " + itemId);
        }
    }
    
 // Method for creating Stock task
    public void createNewStock(String itemName, WarehousePosition unloadingArea, WarehousePosition shelfLocation) {
    	int newId = stockIdCounter.incrementAndGet();
    	String taskId = "Stock-" + newId;
        Task newTask = new Stock(taskId, itemName, unloadingArea, shelfLocation);
        
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