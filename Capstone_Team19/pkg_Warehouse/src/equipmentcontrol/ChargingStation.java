package equipmentcontrol;

import logging.Logger;

public class ChargingStation {

    private String name;

    public ChargingStation(String name) {
        this.name = name;
    }

    public void charge() {
        // Log charging station start
        Logger.log_print("INFO", "charging", "Charging station " + name + " started charging.");

        // Simulate charging process
        try {
            Thread.sleep(2000); // Simulate charging duration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Log charging station completion
        Logger.log_print("INFO", "charging", "Charging station " + name + " completed charging.");
    }
}
