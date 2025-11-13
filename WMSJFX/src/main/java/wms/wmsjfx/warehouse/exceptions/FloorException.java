package wms.wmsjfx.warehouse.exceptions;

public class FloorException extends RuntimeException {
    public FloorException(String message) { super(message); }
    public FloorException(String message, Throwable cause) { super(message, cause); }
}