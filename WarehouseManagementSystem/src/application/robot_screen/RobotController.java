package application.robot_screen;

import equipmentManager.Robot;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import taskManager.Task;
import taskManager.TaskManager;

public class RobotController {

    @FXML private TableColumn<Robot, String> robotid;
    @FXML private TableColumn<Robot, Double> battery;
    @FXML private TableColumn<Robot, String> robotstate;
    @FXML private TableView<Robot> tablerobot;

    private Timeline refreshTimeline_1, refreshTimeline_2;

    public void initialize() {
        robotid.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        battery.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getBatteryPercentage()).asObject());
        robotstate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getState()));

        robotid.setCellFactory(this::centeredCell);
        battery.setCellFactory(this::batteryCell);
        robotstate.setCellFactory(this::stateCell);

        tablerobot.setItems(RobotManager.getRobots());
        

        tablerobot.setFixedCellSize(50); 
        tablerobot.prefHeightProperty().bind(
                Bindings.size(tablerobot.getItems())
                        .multiply(tablerobot.getFixedCellSize())
                        .add(28) 
        );
        tablerobot.minHeightProperty().bind(tablerobot.prefHeightProperty());
        tablerobot.maxHeightProperty().bind(tablerobot.prefHeightProperty());
        
        refreshTimeline_1 = new Timeline(
            new KeyFrame(Duration.millis(200), e -> tablerobot.refresh())
        );
               
        refreshTimeline_1.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline_2.setCycleCount(Timeline.INDEFINITE);
        
        refreshTimeline_1.play();
    }

    private <T> TableCell<Robot, T> centeredCell(TableColumn<Robot, T> col) {
        return new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-alignment: CENTER; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Consolas';");
                }
            }
        };
    }

    private TableCell<Robot, Double> batteryCell(TableColumn<Robot, Double> col) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    return;
                }

                setText(String.format("%.1f%%", value));
                String color = value > 50 ? "#4CAF50" : (value >= 20 ? "#FFC107" : "#F44336");
                setStyle("-fx-alignment: CENTER; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            }
        };
    }

    private TableCell<Robot, String> stateCell(TableColumn<Robot, String> col) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String state, boolean empty) {
                super.updateItem(state, empty);
                if (empty || state == null) {
                    setText(null);
                    return;
                }

                setText(state.toUpperCase());
                String color;
                	if(state.toUpperCase().equals("IDLE")) {
                		color = "#4CAF50";
                	}
                	else {
                		color = "#2196F3";               		
                	}
                setStyle("-fx-alignment: CENTER; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            }
        };
    }
}
