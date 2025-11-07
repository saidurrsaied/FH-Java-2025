import equipmentManager.ChargingStation;
import equipmentManager.EquipmentManager;
import equipmentManager.Robot;
import equipmentManager.RobotState;
import taskManager.Task;
import taskManager.TaskManager;
import taskManager.FindChargeTimeoutException;
import taskManager.OrderTask;
import taskManager.TaskType;
import warehouse.PackingStation;
import warehouse.WahouseObjectType;
import warehouse.WarehouseManager;
import warehouse.datamanager.DataFile;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main_ChargingTest {

	/**
     * Helper: Creates a fake 'Order' Task to report completion.
     * (We only need its Type and ID for the report).
     *
     * This version correctly implements the 'Task' interface.
     */
    private static Task createFakeOrderTask() {
        
        // Create an anonymous inner class that implements the Task interface
        return new Task() {
            
            @Override
            public TaskType getType() {
                // This is the most important part for the 'switch' statement
                return TaskType.PICK_ORDER; 
            }

            @Override
            public String getID() {
                return "FakeOrderTask-999";
            }

            @Override
            public String getDescription() {
                return "A fake task used for testing reports.";
            }

            @Override
            public void execute(Robot robot, EquipmentManager manager) 
                    throws InterruptedException, FindChargeTimeoutException {
                // This method will never be called in this test,
                // because we pass the task directly to 'reportFinishedTask'.
                /* Do nothing */
            }
        };
    }

    public static void main(String[] args) throws Exception {

        System.out.println("--- INITIALIZING TEST ENVIRONMENT ---");

        // --- 1. Initialize core components ---
        BlockingQueue<Task> taskSubmissionQueue = new LinkedBlockingQueue<>();
        List<Robot> allRobots = Collections.synchronizedList(new ArrayList<>());
        
        // === CREATE RESOURCES ===
        // ONLY 2 CHARGING STATIONS
        List<ChargingStation> chargingStations = new ArrayList<>();
        ChargingStation cs1 = new ChargingStation("CS01", 50, 50, WahouseObjectType.ChargingStation);
        ChargingStation cs2 = new ChargingStation("CS02", 50, 55, WahouseObjectType.ChargingStation);
        chargingStations.add(cs1);
        chargingStations.add(cs2);

        // 2 PACKING STATIONS (for testing)
        List<PackingStation> packingStations = new ArrayList<>();
        packingStations.add(new PackingStation("PST01", 1, 1, WahouseObjectType.PackingStation));
        packingStations.add(new PackingStation("PST02", 1, 2 , WahouseObjectType.PackingStation));

        // --- 2. Initialize Manager and Robots ---
        EquipmentManager manager = new EquipmentManager(
                allRobots, // Starts empty, robots will be added later
                chargingStations,
                packingStations,
                taskSubmissionQueue
        );

        Thread emThread = new Thread(manager, "EM-Brain-Thread");
        emThread.setDaemon(false); // Important: Keep the JVM alive
        emThread.start();

        // Create 4 Robots for testing
        Robot r1 = new Robot("R1", new Point(0, 0), manager, WahouseObjectType.Robot);
        Robot r2 = new Robot("R2", new Point(0, 1), manager, WahouseObjectType.Robot);
        Robot r3 = new Robot("R3", new Point(0, 2), manager, WahouseObjectType.Robot);
        Robot r4 = new Robot("R4", new Point(0, 3), manager, WahouseObjectType.Robot);
        
        allRobots.add(r1);
        allRobots.add(r2);
        allRobots.add(r3);
        allRobots.add(r4);

        // Update references (fixes the deadlock bug)
        for (Robot r : allRobots) {
            r.setEquipmentManager(manager);
            Thread t = new Thread(r, "Robot-" + r.getID());
            t.setDaemon(true);
            t.start();
        }

        System.out.println("--- SETUP COMPLETE. Starting Scenarios... ---");
        TimeUnit.SECONDS.sleep(2); // Wait for threads to start

        // ================================================================
        // SCENARIO 1: 3 ROBOTS, 2 STATIONS (FIFO TEST)
        // ================================================================
        System.out.println("\n--- SCENARIO 1: 3 ROBOTS, 2 STATIONS (FIFO TEST) ---");
        
        // Set 3 robots' batteries to a low state
        r1.setBatteryPercentage(20);
        r2.setBatteryPercentage(20);
        r3.setBatteryPercentage(20);

        // Simulate 3 robots finishing tasks sequentially (1 sec apart)
        System.out.println("[TEST] R1 finishing task... (Should get ChargeTask)");
        manager.reportFinishedTask(r1, createFakeOrderTask(), true);
        TimeUnit.SECONDS.sleep(1);

        System.out.println("[TEST] R2 finishing task... (Should get ChargeTask)");
        manager.reportFinishedTask(r2, createFakeOrderTask(), true);
        TimeUnit.SECONDS.sleep(1);

        System.out.println("[TEST] R3 finishing task... (Should get GoToChargingStationAndWaitTask)");
        manager.reportFinishedTask(r3, createFakeOrderTask(), true);
        
        System.out.println("\n[TEST] --- Waiting 30 seconds for Scenario 1 to resolve... ---");
        // Wait 30 seconds for the 3 robots to charge and resolve
        // (Assuming robot.charge() takes 5-10 seconds)
        // YOU SHOULD SEE:
        // 1. R1 and R2 receive 'ChargeTask' (since 2 stations are free)
        // 2. R3 receives 'GoToChargingStationAndWaitTask' (all stations are busy)
        // 3. R3 will move to its station and 'poll' (wait)
        // 4. When R1 (or R2) finishes, R3 will acquire the station and start charging.
        TimeUnit.SECONDS.sleep(30);

        // ================================================================
        // SCENARIO 2: 15-SECOND TIMEOUT TEST
        // ================================================================
        System.out.println("\n--- SCENARIO 2: 15-SECOND TIMEOUT TEST ---");
        
        // 1. Manually acquire both stations to ensure the queue is empty
        System.out.println("[TEST] Manually acquiring both stations (CS01, CS02)...");
        ChargingStation takenCS1 = manager.requestAvailableChargingStation(0);
        ChargingStation takenCS2 = manager.requestAvailableChargingStation(0);
        
        if (takenCS1 == null || takenCS2 == null) {
            System.err.println("[TEST] FAILED TO SETUP SCENARIO 2! Stations were not available.");
            return;
        }
        System.out.println("[TEST] All stations are now busy.");
        
        // 2. Set R4's battery to low and report task completion
        r4.setBatteryPercentage(20);
        System.out.println("[TEST] R4 finishing task... (Should get GoToChargingStationAndWaitTask)");
        manager.reportFinishedTask(r4, createFakeOrderTask(), true);

        // 3. Observe
        // R4 will receive 'GoToChargingStationAndWaitTask'
        // R4's thread will execute and call poll(15 SECONDS)
        System.out.println("\n[TEST] --- Waiting 20 seconds (longer than 15s timeout)... ---");
        TimeUnit.SECONDS.sleep(20);
        
        // YOU SHOULD SEE (IN THE LOGS):
        // 1. [R4] Arrived at ... Waiting (15 sec)...
        // 2. (After 15 seconds)
        // 3. [R4] 15-SEC TIMEOUT. Returning to Start.
        // 4. [EquipmentManager] Robot R4 finished task GO_TO_CHARGING_STATION_AND_WAIT (Status: false)
        // 5. [EquipmentManager] ... Sending to Start Point.
        // 6. [R4] Received task: GoToStartTask
        
        System.out.println("\n[TEST] --- Test complete. Releasing stations. ---");
        // Cleanup
        manager.releaseChargeStation(takenCS1);
        manager.releaseChargeStation(takenCS2);
        
        System.out.println("[TEST] Simulation will now run indefinitely. Press CTRL+C to stop.");
        
        // Wait indefinitely
        emThread.join();
    }
}