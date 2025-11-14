package wms.wmsjfx.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class Logger {

    private static String logFolderPath;
    private static String logSystemPath;
    private static File folder_robot;
    private static File folder_robot1;
    private static File folder_robot2;
    private static File folder_robot3;
    private static File folder_equipment_manager;
    private static File folder_charging;
    private static File folder_inventory;
    private static File folder_system;

    // Static block to initialize log folder based on current date
    static {
        File log = new File("Logging");
        if (!log.exists()) {
            log.mkdir();
        }
        // Create the folder for today's date if it doesn't exist
        folder_robot = new File("Logging", "Robot");
        if (!folder_robot.exists()) {
            folder_robot.mkdir();
        }
        folder_robot1 = new File("Logging/Robot", "Robot1");
        if (!folder_robot1.exists()) {
            folder_robot1.mkdir();
        }
        folder_robot2 = new File("Logging/Robot", "Robot2");
        if (!folder_robot2.exists()) {
            folder_robot2.mkdir();
        }
        folder_robot3 = new File("Logging/Robot", "Robot3");
        if (!folder_robot3.exists()) {
            folder_robot3.mkdir();
        }
        folder_equipment_manager = new File("Logging", "Equipment_Manager");
        if (!folder_equipment_manager.exists()) {
            folder_equipment_manager.mkdir();
        }
        folder_charging = new File("Logging", "Charging_Station");
        if (!folder_charging.exists()) {
            folder_charging.mkdir();
        }
        folder_inventory = new File("Logging", "Inventory");
        if (!folder_inventory.exists()) {
            folder_inventory.mkdir();
        }
        folder_system = new File("Logging", "System");
        if (!folder_system.exists()) {
            folder_system.mkdir();
        }
        logSystemPath = folder_system.getPath();  // Set the path for the log folder
    }

    /* General log print function that handles info, warning, error, etc.
     * @param String type
     * @param String component
     * @param String message
     */
    public void log_print(String type, String component, String message) {

        // Get the current time formatted as "hour.minute.second"
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yy"));

        // Format the log level: [INFO], [WARNING], [ERROR]
        String logLevel = "[" + type + "]";

        String system = null;
        switch(component) {
            case "robot":
                logFolderPath = folder_robot.getPath();
                system = "Robot";
                break;
            case "R1":
                logFolderPath = folder_robot1.getPath();
                system = "Robot1";
                break;
            case "R2":
                logFolderPath = folder_robot2.getPath();
                system = "Robot2";
                break;
            case "R3":
                logFolderPath = folder_robot3.getPath();
                system = "Robot3";
                break;
            case "equipment_manager":
                logFolderPath = folder_equipment_manager.getPath();
                system = "Equipment_Manager";
                break;
            case "inventory":
                logFolderPath = folder_inventory.getPath();
                system = "Inventory";
                break;
            case "charging":
                logFolderPath = folder_charging.getPath();
                system = "Charging";
                break;
            default:
                logFolderPath = folder_system.getPath();
        }

        // Format the log message
        String logMessage = String.format("%s[%s][%s][%s] %s", logLevel, dateString, timestamp, system, message);

        // Determine file name based on origin ("robot.txt", "charging.txt")
        String fileName = dateString + ".txt";

        // Ensure parent directories exist
        if (logFolderPath != null) new File(logFolderPath).mkdirs();
        if (logSystemPath != null) new File(logSystemPath).mkdirs();

        // Define the full file path inside the date-based folder
        File logFile = new File(logFolderPath, fileName);
        File systemFile = new File(logSystemPath, fileName);

        // Write the log message to both files
        try (FileWriter logWriter = new FileWriter(logFile, true);
             FileWriter systemWriter = new FileWriter(systemFile, true)) {

            // Write to the system log file
            systemWriter.write(logMessage + "\n");
            systemWriter.flush(); 	// Ensure the log is written immediately

            // Write to the main log file
            logWriter.write(logMessage + "\n");
            logWriter.flush(); 		// Ensure the log is written immediately

        } catch (IOException e) {
            handleException(e); 	// Handle any IOException
        }
    }
    /** Handles IOException by writing to a fallback error log file. */
    private void handleException(IOException e) {
        try (FileWriter fallback = new FileWriter("logger_error.txt", true)) {
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            fallback.write("[" + timestamp + "] " + e.toString() + System.lineSeparator());
        } catch (IOException ignored) {
        }
    }
}