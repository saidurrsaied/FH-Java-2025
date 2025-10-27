package taskManager;

import equipment.Robot;

public interface Task {
    String getID();
    String getDescription();
    void execute(Robot robot);
//    boolean isCompleted();
}
