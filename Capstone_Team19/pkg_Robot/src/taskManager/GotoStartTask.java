package taskManager;

import common.Position;
import equipment.Robot;

public class GotoStartTask implements Task{

	private final Position startingPosition;
	
	public GotoStartTask(Position startingPosition) {
		super();
		this.startingPosition = startingPosition;
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
		robot.moveTo(startingPosition);
	}

}
