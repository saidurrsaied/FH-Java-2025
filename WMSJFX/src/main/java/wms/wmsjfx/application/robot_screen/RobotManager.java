package application.robot_screen;

import java.util.List;

import equipmentManager.Robot;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RobotManager {
	private static final ObservableList<Robot> ROBOTS = FXCollections.observableArrayList();

    public static void initializeRobot(List<Robot> avaiRobot) {
    	ROBOTS.clear(); 
        for (Robot robot : avaiRobot) {
            ROBOTS.add(robot);
        }
    }

    public static ObservableList<Robot> getRobots() {
        return ROBOTS;
    }
}