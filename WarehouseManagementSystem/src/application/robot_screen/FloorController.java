package application.robot_screen;

import application.inventory_screen.InventoryManager;
import equipmentManager.Robot;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import warehouse.InventoryItem;
import javafx.scene.layout.GridPane;

public class FloorController {

    @FXML private GridPane gridPane;
    @FXML private Group robot1;  
    @FXML private Group robot2;  
    @FXML private Group robot3;  
    @FXML private Label shelf1;  
    @FXML private Label shelf2;  
    @FXML private Label shelf3; 
    @FXML private Label shelf4; 
    @FXML private Label shelf5; 
    @FXML private Label shelf6; 
    @FXML private Label shelf7; 
    @FXML private Label shelf8; 
    @FXML private Label shelf9; 
    private Label[] shelves;
    private Group[] robots;
    
    private ObservableList<Robot> ROBOTS = FXCollections.observableArrayList();
    private ObservableList<InventoryItem> ITEMS = FXCollections.observableArrayList();

    public void initialize() {
        shelves = new Label[] {shelf1, shelf2, shelf3, shelf4, shelf5, shelf6, shelf7, shelf8, shelf9};
        robots = new Group[] {robot1,robot2,robot3};
        ROBOTS = RobotManager.getRobots(); 
        ITEMS  = InventoryManager.getItems();
        SetShelfitem();
        new AnimationTimer() {
            @Override
            public void handle(long now) {
            		for(int i=0;i<ROBOTS.size();i++) {
            			Movement(robots[i], ROBOTS.get(i));
            			State_control(robots[i], ROBOTS.get(i));
            		}
            }
        }.start();
    }

    public void Movement(Group robot_index, Robot robot) {
        double targetX = robot.getLocation().getX();
        double targetY = robot.getLocation().getY();

        gridPane.getChildren().remove(robot_index);
        gridPane.add(robot_index, (int) targetX, (int) targetY);
    }
    
    public void State_control(Group robot_index, Robot robot) {
    		Circle circle = (Circle) robot_index.getChildren().get(0);
    		if(robot.getBatteryPercentage() < 20) {
        		circle.setFill(javafx.scene.paint.Color.RED);
    		}
    		else if(robot.getBatteryPercentage() >= 20 && robot.getBatteryPercentage() < 50) {
        		circle.setFill(javafx.scene.paint.Color.YELLOW);
    		}
    		else {
        		circle.setFill(javafx.scene.paint.Color.GREEN);   			
    		}
    }
    
    public void SetShelfitem() {
        for (Label shelf : shelves) {
        		for(InventoryItem item : ITEMS) {
        				if(shelf.getText().equals(item.getShelf().getId())) {
        					shelf.setText(item.getProduct().getProductName());
        				}
        		}
        }
    }
    
}