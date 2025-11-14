package wms.wmsjfx.application;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class WarehouseScreen extends StackPane {

    public WarehouseScreen() {
        // Add a label to show this is the Warehouse screen
        Label label = new Label("This is the Warehouse screen");
        getChildren().add(label);  // Add the label to the StackPane (which is a Node)
    }
}
