package taskManager;

import common.Position;
import equipment.HoldingStation;
import equipment.Robot;

public class GotoHoldingTask implements Task{
	private final HoldingStation holdingPosition;
	
	public GotoHoldingTask(HoldingStation freeSpot) {
		super();
		this.holdingPosition = freeSpot;
	}
	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute(Robot robot) {
		robot.moveTo(holdingPosition.getPosition());
	}

}
