package taskManager;

public class OrderTaskException extends Exception {
	
    public OrderTaskException(String message) {
        super(message);
    }
    
    public OrderTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}


