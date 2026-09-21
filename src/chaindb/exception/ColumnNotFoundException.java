package chaindb.exception;

/**
 * Thrown when a query references a column that does not exist on the target table.
 */
public class ColumnNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public ColumnNotFoundException(String message) {
        super(message);
    }
}
