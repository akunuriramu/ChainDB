package chaindb.exception;

/**
 * Thrown when a query references a table that does not exist in the database.
 */
public class TableNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public TableNotFoundException(String message) {
        super(message);
    }
}
