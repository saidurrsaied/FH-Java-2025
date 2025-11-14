package application.robot_screen;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import equipmentManager.Robot;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import taskManager.Task;

public class RobotManager {
	private static final ObservableList<Robot> ROBOTS = FXCollections.observableArrayList();
    private static ObservableList<Task> taskSubmissionQueue = FXCollections.observableArrayList();

    public static void initializeRobot(List<Robot> avaiRobot, List<Task> getPendingTasks) {
    		ROBOTS.clear(); 
        for (Robot robot : avaiRobot) {
            ROBOTS.add(robot);
        }
        
        taskSubmissionQueue.clear(); 
        for (Task task : getPendingTasks) {
        		taskSubmissionQueue.add(task);
        }
    }

    public static ObservableList<Robot> getRobots() {
        return ROBOTS;
    }
    
    public static ObservableList<Task>  getTasks() {
        return taskSubmissionQueue;
    }
       
}