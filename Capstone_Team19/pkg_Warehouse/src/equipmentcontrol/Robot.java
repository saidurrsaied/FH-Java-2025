package equipmentcontrol;

import logging.Logger;

public class Robot {
    private String name;

    public Robot(String name) {
        this.name = name;
    }

    public void performTask() {
        // Log robot task start
        Logger.log_print("INFO", "robot", name + " started task.");

        // Simulate some task
        try {
            Thread.sleep(1000); // Simulate task duration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Log robot task completion
        Logger.log_print("INFO", "robot", name + " completed task.");
    }
}
