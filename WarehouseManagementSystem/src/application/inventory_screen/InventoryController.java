package application.inventory_screen;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import warehouse.InventoryItem;

public class InventoryController {

    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> colId;
    @FXML private TableColumn<InventoryItem, String> colName;
    @FXML private TableColumn<InventoryItem, Number> colQty;
    @FXML private TableColumn<InventoryItem, String> colShelf;
    @FXML private TableColumn<InventoryItem, Double> colPosX;
    @FXML private TableColumn<InventoryItem, Double> colPosY;
    
    private Timeline refreshTimeline;

    public void initialize() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProduct().getProductID()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProduct().getProductName()));
        colShelf.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getShelf().getId()));
        colQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()));
        colPosX.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getShelf().getLocation().getX()).asObject());
        colPosY.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getShelf().getLocation().getY()).asObject());
        
        inventoryTable.setItems(InventoryManager.getItems());  // Set updated data to the table
        inventoryTable.setFixedCellSize(40); 

        refreshTimeline = new Timeline(
                new KeyFrame(Duration.millis(200), e -> inventoryTable.refresh())
            );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }
}
