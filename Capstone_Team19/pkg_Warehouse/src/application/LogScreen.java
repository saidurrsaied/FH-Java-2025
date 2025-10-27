package application;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class LogScreen extends StackPane {

    public LogScreen() {
        // Add a label to show this is the Log screen
        Label label = new Label("This is the Log screen");
        getChildren().add(label);  // Add the label to the StackPane (which is a Node)
    }
}
