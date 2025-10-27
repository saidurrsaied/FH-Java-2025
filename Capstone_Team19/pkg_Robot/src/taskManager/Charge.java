package taskManager;

import common.Position;
import equipment.Robot;

public class Charge implements Task{

	private String ID;
	private Position chargingStation;
	
	public Charge(String ID, Position chargingPosition) {
		this.ID = ID;
		this.chargingStation = chargingPosition;
	}
	
	@Override
	public void execute(Robot robot) {
		robot.moveTo(chargingStation);
		robot.charge();
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Charging ...";
	}
}
