/**
 * Author: Anh Phuc Dang
 * */



import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Import your custom classes
import equipments.ChargingStation;
import equipments.EquipmentManager;
import equipments.PackingStation;
import equipments.Robot;
import taskManager.Task;
import taskManager.TaskCreationException;
import taskManager.TaskManager;

// Import your warehouse data classes (if needed for setup)
// import warehouse.WarehouseManager;
// import warehouse.StorageShelf;
// import warehouse.Station;



public class Main {

    public static void main(String[] args) {

        System.out.println("[Main] --- Autonomous Warehouse Management System ---");

        // --- 1. Initialize Communication Queue ---
        // This is the shared "inbox" that TaskManager WRITES to
        // and EquipmentManager READS from.
        BlockingQueue<Task> taskSubmissionQueue = new LinkedBlockingQueue<>();

        // --- 2. Initialize Physical Resources (The Stations) ---

        // Create Charging Stations
        List<ChargingStation> chargingStations = new ArrayList<>();
        chargingStations.add(new ChargingStation("CS-01", new Point(5, 5)));

        // Create Packing Stations
        List<PackingStation> packingStations = new ArrayList<>();
        packingStations.add(new PackingStation("PS-01", new Point(100, 50)));
        packingStations.add(new PackingStation("PS-02", new Point(100, 55)));

        // (HoldingStations are removed based on our last logic change)

        // --- 3. Initialize Robot List ---
        // This list holds robots that are currently IDLE and at their STARTING_POS.
        // We use a synchronizedList for safety, although the EM-Thread model
        // (if implemented) wouldn't strictly need it.
        List<Robot> availableRobots = Collections.synchronizedList(new ArrayList<>());

        // --- 4. Initialize the "Brain" (EquipmentManager) ---
        // Pass all the physical resources to the manager.
        EquipmentManager manager = new EquipmentManager(
                availableRobots,
                chargingStations,
                packingStations,
                taskSubmissionQueue
        );

        // --- 5. Initialize the "Task Creator" (TaskManager) ---
        // Give it the shared queue to submit tasks to.
        TaskManager taskManager = new TaskManager(taskSubmissionQueue);

        // --- 6. Initialize the "Workers" (Robots) ---
        // Create robots, passing their ID, Start Pos, and a reference to the Manager
        Robot robot1 = new Robot("R-01", new Point(0, 0), manager);
        Robot robot2 = new Robot("R-02", new Point(0, 1), manager);

        // Add the new robots to the "available" list so the EM knows they exist
        availableRobots.add(robot1);
        availableRobots.add(robot2);

        // --- 7. START ALL THREADS ---

        // Start the EquipmentManager thread. It will now wait for tasks.
        new Thread(manager, "EM-Brain-Thread").start();

        // Start the Robot threads. They will now wait for tasks.
        new Thread(robot1, "Robot-R-01-Thread").start();
        new Thread(robot2, "Robot-R-02-Thread").start();

        System.out.println("[Main] All Threads started. Waiting 1 second for startup...");
        try {
            Thread.sleep(1000); // Give threads time to initialize
        } catch (InterruptedException e) {}

        // --- 8. SIMULATE WORK ---
        System.out.println("[Main] --- Sending 3 Orders to the queue ---");

        // Use TaskManager to create and submit new orders to the shared queue.
        // The EquipmentManager will pick these up automatically.
        try {
            taskManager.createNewOrder(new Point(10, 10), "Item-A", 5); // Task 1
            taskManager.createNewOrder(new Point(50, 20), "Item-B", 2); // Task 2
            // This task will likely go to the pending queue
            // as both robots will be busy.
            taskManager.createNewOrder(new Point(30, 30), "Item-C", 1); // Task 3
        } catch (TaskCreationException  e) {
            System.err.println("❌ Order creation failed: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("→ Root cause: " + e.getCause().getMessage());
            }
        }

        System.out.println("[Main] --- 3 Orders submitted. Program is running. ---");
        // The main thread will now exit, but the Robot and EM threads
        // will continue running indefinitely.
    }
}