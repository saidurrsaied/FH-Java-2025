package logger;



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
    private static File folder_charging;
    private static File folder_inventory;
    private static File folder_system;

    public Logger() {}

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

    // General log print function that handles info, warning, error, etc.
    public void log_print(String type, String component, String message) {
        // Get the current time formatted as "hour.minute.second"
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yy"));

        // Format the log level prefix
        String logLevel = "[" + type + "]";  // Example: [INFO], [WARNING], [ERROR]

        String system = null;
        switch(component) {
            case "robot":
                logFolderPath = folder_robot.getPath();
                system = "Robot";
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
        }

        // Format the log message
        String logMessage = String.format("%s[%s][%s][%s] %s", logLevel, dateString, timestamp, system, message);

        // Determine file name based on origin (robot, charging, etc.)

        String fileName = dateString + "_" + timestamp.replace(":","-") + ".txt";  // Example: "robot.txt", "charging.txt"

        // Define the full file path inside the date-based folder
        File logFile = new File(logFolderPath, fileName);
        File systemFile = new File(logSystemPath, fileName);
        // Write the log message to both files
        try (FileWriter logWriter = new FileWriter(logFile, true);
             FileWriter systemWriter = new FileWriter(systemFile, true)) {

            // Write to the system log file
            systemWriter.write(logMessage + "\n");
            systemWriter.flush(); // Ensure the log is written immediately

            // Write to the main log file
            logWriter.write(logMessage + "\n");
            logWriter.flush(); // Ensure the log is written immediately

        } catch (IOException e) {
            e.printStackTrace(); // Handle any IOException
        }
    }
}