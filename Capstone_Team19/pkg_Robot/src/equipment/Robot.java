package equipment;

import taskManager.Task;
import warehouse.WarehousePosition;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import common.Position;

public class Robot implements Runnable {
    private static final int PICKING_TIME_MS = 300; 
    private static final int DROPPING_TIME_MS = 300;
    private static final int CHARGING_1_PERCENTAGE_TIME_MS = 100;
    private static final long MOVE_DELAY_PER_METER_MS = 100; //speed
    
    private final String ID;
	private double batteryPercentage = 100;
    private RobotState state = RobotState.IDLE;
    private Position startingPosition;
    private Position currentPosition;
    private final EquipmentManager equipmentManager;
    private final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();
    
    public Robot(String ID, Position startingPosition, EquipmentManager equipmentManager) {
        this.ID = ID;
        this.startingPosition = startingPosition;
        this.currentPosition = startingPosition;
        this.equipmentManager = equipmentManager;
    }
    
    @Override
    public void run() {
        System.out.println(ID + " thread started.");
        while (true) {
            Task task = null;
            try {
                task = taskQueue.take();
                task.execute(this); 
            } catch (InterruptedException e) {
                 System.err.println("Robot " + ID + " bị ngắt khi đang làm task!");
            } finally {
                if (task != null) {
                    equipmentManager.reportRobotAvailable(this, task);
                }
            }
        }
    }

	public void addTask(Task task) {
        taskQueue.add(task);
        System.out.println(ID + " received task: " + task.getDescription());
    }

    public void moveTo(Position targetPosition) {    	
        // Calculate battery percentage
        double distance = Position.distance(currentPosition, targetPosition);
        int consumed = (int) Math.ceil(distance);
        batteryPercentage = Math.max(0, batteryPercentage - consumed);
        
        try {
            Thread.sleep((long) (distance * MOVE_DELAY_PER_METER_MS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Reach destination
        this.currentPosition = targetPosition;
        System.out.printf("[%s] Arrived at %s (battery %d%%)%n", ID, currentPosition, batteryPercentage);
    }

    public void pickUpItem(String itemId) {
    	System.out.printf("[%s] Picking up item %s ... %n", ID, itemId);
        try { Thread.sleep(PICKING_TIME_MS); } catch (Exception e) {}
        System.out.printf("[%s] Picked up item %s %n", ID, itemId);
    }
    
    public void dropItem(String itemId) {
        System.out.printf("[%s] Dropping item %s", ID, itemId);
        try { Thread.sleep(DROPPING_TIME_MS); } catch (Exception e) {}
        System.out.printf("[%s] Dropped item %s", ID, itemId);
    }
    
    public void charge() {
        System.out.printf("[%s] Charging ... %n", ID);
        double neededPercentage = 100 - batteryPercentage;

        try {
            Thread.sleep((long) (CHARGING_1_PERCENTAGE_TIME_MS * neededPercentage));
        } catch (InterruptedException e) {
            System.out.printf("[%s] Charging interrupted! %n", ID);
            Thread.currentThread().interrupt();
            return;
        }

        batteryPercentage = 100;
        System.out.printf("[%s] Fully charged %n", ID);
    }
    
    public String getID() {
		return ID;
	}
    
    public double getBatteryPercentage() {
		return batteryPercentage;
	}

	public void setBatteryPercentage(double batteryPercentage) {
		this.batteryPercentage = batteryPercentage;
	}

	public Position getStartingPosition() {
		return startingPosition;
	}

	public void setStartingPosition(Position startingPosition) {
		this.startingPosition = startingPosition;
	}

	public Position getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Position currentPosition) {
		this.currentPosition = currentPosition;
	}

	public RobotState getState() {
		return state;
	}

	public void setState(RobotState state) {
		this.state = state;
	}

}
