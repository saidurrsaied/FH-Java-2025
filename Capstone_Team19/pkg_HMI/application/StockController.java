package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class StockController {

    @FXML private TextField nameField;
    @FXML private TextField qtyField;
    @FXML private Label feedback;

    @FXML
    private void handleApply() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String qtyText = qtyField.getText() == null ? "" : qtyField.getText().trim();

        if (name.isEmpty() || qtyText.isEmpty()) {
            showError("Please fill in both fields.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyText);
            if (qty < 0) {
                showError("Quantity must be >= 0.");
                return;
            }
        } catch (NumberFormatException ex) {
            showError("Quantity must be a number.");
            return;
        }

        boolean updated = InventoryScreen.incrementQuantity(name, qty);
        if (!updated) {
            showError("Item \"" + name + "\" does not exist.");
        } else {
            showOk("Updated \"" + name + "\" to quantity " + qty + ".");
        }
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        qtyField.clear();
        feedback.setText("");
    }

    private void showError(String msg) {
        feedback.setStyle("-fx-text-fill:#d64545;");
        feedback.setText(msg);
    }

    private void showOk(String msg) {
        feedback.setStyle("-fx-text-fill:#2dbf71;");
        feedback.setText(msg);
    }
}
