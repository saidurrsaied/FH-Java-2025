package application;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.concurrent.Task;

public class Loading {

    @FXML
    private ProgressBar progressBar;

    private Runnable onLoadComplete; // callback when loading is done

    @FXML
    public void initialize() {
        // Task to simulate loading
        Task<Void> loadingTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i <= 50; i++) {
                    Thread.sleep(40); // speed of progress (lower = faster)
                    updateProgress(i, 50);
                }
                return null;
            }
        };

        // Bind progress bar to task progress
        progressBar.progressProperty().bind(loadingTask.progressProperty());

        // Start task in a background thread
        new Thread(loadingTask).start();

        // Once loading is complete, trigger the callback
        loadingTask.setOnSucceeded(e -> {
            if (onLoadComplete != null) onLoadComplete.run();
        });
    }

    // Allow `Main.java` to define what happens after loading finishes
    public void setOnLoadComplete(Runnable onLoadComplete) {
        this.onLoadComplete = onLoadComplete;
    }
}
