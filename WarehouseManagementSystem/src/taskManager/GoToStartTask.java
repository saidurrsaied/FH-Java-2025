package taskManager;

import java.awt.Point;
import java.util.List;

import equipmentManager.*;

public class GoToStartTask implements Task{

	private final Point startingPosition;
    private final TaskType taskType = TaskType.GO_TO_START;
    private final String ID;
    
	public GoToStartTask(Point startingPosition) {
		super();
		this.startingPosition = startingPosition;
		this.ID = "GoToStart(" + startingPosition.x + "," + startingPosition.y + ")";
	}

	@Override
	public String getID() {
		return this.ID;
	}

	@Override
	public String getDescription() {
		return String.format("Task: Return to Starting Point (%d, %d)",
                startingPosition.x,
                startingPosition.y);
	}

	@Override
	public void execute(Robot robot, EquipmentManager manager) throws InterruptedException {
		System.out.printf("[%s] Executing %s...%n", robot.getID(), this.ID);
		List<Point> steps = manager.requestPath(robot, startingPosition);
		robot.stepMove(steps);
//		robot.moveTo(startingPosition);
	}

	@Override
	public TaskType getType() {
		return taskType;
	}
}
