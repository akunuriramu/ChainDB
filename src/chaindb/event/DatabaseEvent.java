package chaindb.event;

import chaindb.exception.ColumnNotFoundException;
import chaindb.exception.TableNotFoundException;
import chaindb.model.Database;

import java.io.Serializable;

/**
 * The base type for every mutation that can happen to a ChainDB database.
 *
 * Every event is immutable once created, is stored inside a {@link chaindb.blockchain.Block},
 * and knows how to {@link #apply(Database)} itself to reconstruct database state.
 * The database is never the source of truth - it is always derived by replaying
 * these events, in order, from the blockchain.
 */
public abstract class DatabaseEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final long timestamp;
    protected final String tableName;
    protected final String operation;

    protected DatabaseEvent(String tableName, String operation) {
        this.timestamp = System.currentTimeMillis();
        this.tableName = tableName;
        this.operation = operation;
    }

    /** Applies this event's effect onto the given database (mutates it in place). */
    public abstract void apply(Database database) throws TableNotFoundException, ColumnNotFoundException;

    /** A short human-readable summary of this event's data, used for display and search. */
    public abstract String getPayload();

    public long getTimestamp() {
        return timestamp;
    }

    public String getTableName() {
        return tableName;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return String.format("%s %s -> %s", operation, tableName, getPayload());
    }
}
