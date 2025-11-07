package taskManager;

import java.util.concurrent.BlockingQueue;

import equipmentManager.ChargingStation;
import equipmentManager.EquipmentManager;
import equipmentManager.Robot;

public class GoToChargingStationAndWaitTask implements Task {
    private final ChargingStation targetChargingStation;
    private final TaskType taskType = TaskType.GO_TO_CHARGING_STATION_AND_WAIT;
    private final String ID;
    
	public GoToChargingStationAndWaitTask(ChargingStation targetChargingStation) {
		super();
		this.targetChargingStation = targetChargingStation;
		this.ID = "GoToChargingStatuinAndWait-";
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getDescription() {
		return String.format("Task: Move to (%.1f, %.1f) and waiting for available charging station.", 
				targetChargingStation.getLocation().getX(), 
				targetChargingStation.getLocation().getY());
	}

	@Override
	public TaskType getType() {
		return taskType;
	}

	@Override
	public void execute(Robot robot, EquipmentManager manager) throws InterruptedException, FindChargeTimeoutException {
		// 1. Robot move to nearest charging station to wait
		robot.moveTo(targetChargingStation.getLocation());
		
		// 2. Wait for 15 minutes until previous Robot finishes charging
		ChargingStation foundChargingStation = manager.requestAvailableChargingStation(15);
		if (foundChargingStation != null) {
		    System.out.printf("[%s] Found available Charging Station %s for Robot %s.%n", this.getClass().getName(), foundChargingStation.getID(), robot.getID());
		    // Check if this charging station is the charging station robot stands
		    if (foundChargingStation.getLocation().equals(robot.getCurrentPosition())) {
		    	robot.charge();
		    } else {
		    	robot.moveTo(foundChargingStation.getLocation());
		    	robot.charge();
		    }
		    // Finally remove charging station when robot finishes charging
		    manager.releaseChargeStation(foundChargingStation);
		} else {
            // Can not found available, go to starting point and wait
			throw new FindChargeTimeoutException("Can not find charge in 15 minutes %n");
		}
	}
    
    
    
}
