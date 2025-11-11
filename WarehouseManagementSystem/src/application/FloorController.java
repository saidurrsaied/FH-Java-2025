package application;

import application.robot_screen.RobotManager;
import equipmentManager.Robot;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.layout.GridPane;

public class FloorController {

    @FXML private GridPane gridPane;
    @FXML private Group robot1;  
    @FXML private Group robot2;  
    @FXML private Group robot3;  

    @FXML private Circle robotCircle; // Circle representing robot

    private ObservableList<Robot> ROBOTS = FXCollections.observableArrayList();
   
    public void initialize() {
        ROBOTS = RobotManager.getRobots();     
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                moveTo(robot1, ROBOTS.get(0));
                moveTo(robot2, ROBOTS.get(1));
                moveTo(robot3, ROBOTS.get(2));
            }
        }.start();
    }

    public void moveTo(Group robot_index, Robot robot) {
        double targetX = robot.getLocation().getX();
        double targetY = robot.getLocation().getY();

        gridPane.getChildren().remove(robot_index);
        gridPane.add(robot_index, (int) targetX, (int) targetY);
    }
}