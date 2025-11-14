package wms.wmsjfx.application.robot_screen;


import java.util.List;
import java.util.concurrent.BlockingQueue;

import wms.wmsjfx.equipmentManager.Robot;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import wms.wmsjfx.taskManager.Task;


public class RobotManager {
    private static final ObservableList<Robot> ROBOTS = FXCollections.observableArrayList();

    public static void initializeRobot(List<Robot> avaiRobot, List<Task> getPendingTasks) {
        ROBOTS.clear();
        for (Robot robot : avaiRobot) {
            ROBOTS.add(robot);
        }

    }

    public static ObservableList<Robot> getRobots() {
        return ROBOTS;
    }

}