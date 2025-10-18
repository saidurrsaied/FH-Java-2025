package taskManager;

import equipments.Robot;

public interface Task {
    void execute(Robot robot);
    String getDescription();
    boolean isCompleted();
}
