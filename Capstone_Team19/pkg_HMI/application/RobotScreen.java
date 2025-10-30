package application;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class RobotScreen extends StackPane {

    public RobotScreen() {
        // Add a label to show this is the Robot screen
        Label label = new Label("This is the Robot screen");
        getChildren().add(label);  // Add the label to the StackPane (which is a Node)
    }
}
