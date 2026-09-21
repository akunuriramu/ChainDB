package chaindb.exception;

/**
 * Thrown when the blockchain's integrity cannot be verified -
 * i.e. tampering or corruption has been detected.
 */
public class BlockchainException extends Exception {
    private static final long serialVersionUID = 1L;

    public BlockchainException(String message) {
        super(message);
    }
}
