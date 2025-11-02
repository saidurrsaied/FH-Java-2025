package taskManager;

import java.awt.Point;

import equipmentManager.EquipmentManager;
import equipmentManager.Robot;

public class ChargeTask implements Task{
	private Point chargingStationLocation;
	private final String ID;
	private final TaskType taskType = TaskType.CHARGE_ROBOT;
	
	public ChargeTask(Point chargingPosition, String robotId) {
		this.chargingStationLocation = chargingPosition;
		this.ID = "Charge-" + robotId;
	}
	
	@Override
	public String getID() {
		return this.ID;
	}

	@Override
	public String getDescription() {
		return String.format("Task: Move to (%d, %d) and charge.", 
                chargingStationLocation.x, 
                chargingStationLocation.y);
	}

	@Override
	public TaskType getType() {
		return taskType;
	}

	@Override
	public void execute(Robot robot, EquipmentManager manager) throws InterruptedException {
		System.out.printf("[%s] Executing %s...%n", robot.getID(), this.ID);
		robot.moveTo(chargingStationLocation);
		robot.charge();
	}
}
