package chaindb.exception;

/**
 * Thrown when user input cannot be parsed as a recognized ChainDB command,
 * or is structurally malformed (wrong number of values, bad WHERE clause, etc).
 */
public class InvalidQueryException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidQueryException(String message) {
        super(message);
    }
}
