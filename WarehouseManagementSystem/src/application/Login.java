package application;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

public class Login {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;
    @FXML private Label feedback;

    @FXML
    private void initialize() {
        // Disable the Login button until both fields are filled
        loginButton.disableProperty().bind(
            usernameField.textProperty().isEmpty()
                .or(passwordField.textProperty().isEmpty())
        );
        
        // Handle Enter key on password field (for login)
        passwordField.setOnAction(e -> {
			try {
				handleLogin();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
    }
    @FXML
    private void handleLogin() throws Exception {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.equals("admin") && password.equals("1234")) {
            feedback.setText("Login successful!");
            feedback.setStyle("-fx-text-fill: green;");
            // Wait for 1 second before closing the login window
            PauseTransition pause = new PauseTransition(Duration.seconds(0.7));
            pause.setOnFinished(event -> {
                // Load the Main Screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main_Screen.fxml"));
                Scene mainScene = null;
				try {
					mainScene = new Scene(loader.load());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // Set the initial window size to 800x600

                // Create a new Stage for the Main Screen
                Stage mainStage = new Stage();
                mainStage.setScene(mainScene);
                mainStage.setTitle("Welcome!");
                mainStage.show();

                // Close the login window
                Stage loginStage = (Stage) usernameField.getScene().getWindow();
                loginStage.close();
            });
            pause.play();
        } else {
            feedback.setText("Invalid credentials.");
            feedback.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleCancel() {
        usernameField.clear();
        passwordField.clear();
        feedback.setText("");
    }
}
