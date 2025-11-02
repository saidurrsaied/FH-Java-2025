package application;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class InventoryDetailController {

    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> itemColumn;
    @FXML private TableColumn<InventoryItem, Integer> quantityColumn;

    @FXML
    public void initialize() {
        // Bind the columns to the InventoryItem properties
        itemColumn.setCellValueFactory(cd -> cd.getValue().itemNameProperty());
        quantityColumn.setCellValueFactory(cd -> cd.getValue().quantityProperty().asObject());
        
        // Bind the TableView to the InventoryScreen's ObservableList
        inventoryTable.setItems(InventoryScreen.getItems()); // This will make the table show all items in the list
    }
}
