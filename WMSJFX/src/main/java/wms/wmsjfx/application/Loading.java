package wms.wmsjfx.application;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.concurrent.Task;

public class Loading {

    @FXML
    private ProgressBar progressBar;

    private Runnable onLoadComplete; // callback when loading is done

    @FXML
    public void initialize() {
        Task<Void> loadingTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i <= 50; i++) {
                    Thread.sleep(40);
                    updateProgress(i, 50);
                }
                return null;
            }
        };

        progressBar.progressProperty().bind(loadingTask.progressProperty());

        new Thread(loadingTask).start();
        loadingTask.setOnSucceeded(e -> {
            if (onLoadComplete != null) onLoadComplete.run();
        });
    }

    public void setOnLoadComplete(Runnable onLoadComplete) {
        this.onLoadComplete = onLoadComplete;
    }
}