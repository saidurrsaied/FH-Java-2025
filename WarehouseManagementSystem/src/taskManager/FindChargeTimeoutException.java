package taskManager;

/**
 * Custom exception thrown when a Robot
 * waits for a charging station for the maximum time (e.g., 15 minutes) and times out.
 * Inherits from OrderTaskException (or Exception) so Robot.run() can catch it.
 */
public class FindChargeTimeoutException extends Exception {
	public FindChargeTimeoutException(String message) {
        super(message); // Call parent constructor
    }
    
    public FindChargeTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
