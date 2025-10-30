package logging;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.*;
import java.util.Optional;

public class LogManager {

    @FXML private TextField equipmentField;       // TextField for system name or date
    @FXML private Button filterLogsButton;        // Button for filtering logs
    @FXML private Button moveLogButton;           // Button for moving logs
    @FXML private Button deleteLogButton;         // Button for deleting logs
    @FXML private Button archiveLogButton;        // Button for archiving logs
    @FXML private Button openLogButton;           // Button for opening logs
    @FXML private Button refreshlogbutton; 	     // Button for refresh logs
    @FXML private Button chooseFolderButton;      // Button for selecting the log folder
    @FXML private Label folderPathLabel;          // Label to show selected folder path
    @FXML private ListView<String> logListView;   // ListView for displaying log file names
    @FXML private TextArea logViewerArea;         // TextArea for viewing log file content
    private File selectedLogFolder = new File("Logging");  // Default folder path

    // Method to load the list of logs into the ListView, with optional filter
    private void loadLogList(String filterText) {
        File logDirectory = selectedLogFolder;

        // Get the list of log files, optionally filtered by name
        String[] logFiles = logDirectory.list((_, name) -> {
            if (filterText.isEmpty()) {
            		return name.endsWith(".txt");
            }
            return name.contains(filterText) && name.endsWith(".txt");
        });

        if (logFiles != null) {
            logListView.getItems().clear();  			// Clear the existing list
            logListView.getItems().addAll(logFiles);     // Add the log files to the ListView
        }
    }
    
    // Load the list of available log files when the HMI starts
    @FXML
    public void initialize() {
        loadLogList(""); 
    }

    // Method to handle folder selection
    @FXML
    private void handleChooseFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Log Folder");

        // Set initial directory if it exists
        if (selectedLogFolder.exists()) {
            directoryChooser.setInitialDirectory(selectedLogFolder);
        }

        Stage stage = (Stage) chooseFolderButton.getScene().getWindow();
        File chosenDir = directoryChooser.showDialog(stage);

        if (chosenDir != null) {
            selectedLogFolder = chosenDir;
            folderPathLabel.setText("Current Folder: " + chosenDir.getAbsolutePath());
            showMessage("Log folder changed to:\n" + chosenDir.getAbsolutePath());
            loadLogList(""); // Reload logs from the new folder
        }
    }

    // Method to filter and load logs based on equipment name or date
    @FXML
    private void handleFilterLogs() {
        String filterText = equipmentField.getText().trim();
        loadLogList(filterText + ".txt");
    }

    // Method to move a log file to another folder
    @FXML
    private void handleMoveLog() {
        String logFileName = logListView.getSelectionModel().getSelectedItem();
        if (logFileName != null) {
            Path sourcePath = selectedLogFolder.toPath().resolve(logFileName);
            System.out.println(sourcePath);
            System.out.println(selectedLogFolder);

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Log Files", "*.txt"));
//            File destinationFolder = fileChooser.showSaveDialog(null);

            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Destination Folder");
            
            // Set initial directory if it exists
            if (selectedLogFolder.exists()) {
                directoryChooser.setInitialDirectory(selectedLogFolder);
            }

            Stage stage = (Stage) chooseFolderButton.getScene().getWindow();
            File destinationFolder = directoryChooser.showDialog(stage);
            System.out.println(destinationFolder);
            
            if (destinationFolder != null) {
                try {
                    Path destinationPath = destinationFolder.toPath().resolve(logFileName);
                    Files.move(sourcePath, destinationPath);
                    showMessage("Log file moved.");
                } catch (IOException e) {
                    e.printStackTrace();
                    showMessage("Error moving log file.");
                }
            }
        }
    }

    // Method to delete a log file
    @FXML
    private void handleDeleteLog() {
        String logFileName = logListView.getSelectionModel().getSelectedItem();
        if (logFileName != null) {
        	
            // Confirm with the user before deleting
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Delete Log");
            alert.setHeaderText("Are you sure you want to delete this log?");
            alert.setContentText("This action cannot be undone.");
            
            // Show the dialog and capture the user's response
            Optional<ButtonType> result = alert.showAndWait();
            
            // If the user clicks "OK", proceed with deletion
            if (result.isPresent() && result.get() == ButtonType.OK) {
                File logFile = new File(selectedLogFolder, logFileName);
                if (logFile.delete()) {
                    showMessage("Log file deleted.");
                    loadLogList("");  //Reload the log list after deletion
                } else {
                    showMessage("Error deleting log file.");
                }
            } else {
                logViewerArea.setText("Delete operation canceled.");
            }
        }
    }

    // Method to open and display a log file's contents
    @FXML
    private void handleOpenLog() {
        String logFileName = logListView.getSelectionModel().getSelectedItem();
        if (logFileName != null) {
            File logFile = new File(selectedLogFolder, logFileName);
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                logViewerArea.clear();  // Clear the previous log content
                while ((line = reader.readLine()) != null) {
                    logViewerArea.appendText(line + "\n");  // Append the log content to the TextArea
                }
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error opening log file.");
            }
        }
    }

    // Method to open and display a log file's contents
    @FXML
    private void handleRefreshLog() {
        loadLogList(""); 
    }
    
    // Method to show messages to the user
    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Log Manager");
        alert.setHeaderText(null);  		 // No header text
        alert.setContentText(message);  	 // Display the passed message
        alert.showAndWait();  		     // Show the message and wait for the user to close it
    }
}
