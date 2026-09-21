package chaindb.event;

import chaindb.exception.ColumnNotFoundException;
import chaindb.exception.TableNotFoundException;
import chaindb.model.Database;
import chaindb.model.Table;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Event representing the deletion of every row matching {@code whereColumn == whereValue}.
 *
 * {@code deletedRows} snapshots the rows that were removed, so that the UNDO
 * feature can re-insert them via a compensating {@link InsertEvent} without
 * needing to reconstruct deleted state from earlier in the chain.
 */
public class DeleteEvent extends DatabaseEvent {

    private static final long serialVersionUID = 1L;

    private final String whereColumn;
    private final Object whereValue;
    private final List<LinkedHashMap<String, Object>> deletedRows;

    public DeleteEvent(String tableName, String whereColumn, Object whereValue,
                        List<LinkedHashMap<String, Object>> deletedRows) {
        super(tableName, "DELETE");
        this.whereColumn = whereColumn;
        this.whereValue = whereValue;
        this.deletedRows = deletedRows;
    }

    @Override
    public void apply(Database database) throws TableNotFoundException, ColumnNotFoundException {
        Table table = database.getTable(tableName);
        table.deleteRows(whereColumn, whereValue);
    }

    @Override
    public String getPayload() {
        return "WHERE " + whereColumn + "=" + whereValue + " (" + deletedRows.size() + " row(s))";
    }

    public String getWhereColumn() {
        return whereColumn;
    }

    public Object getWhereValue() {
        return whereValue;
    }

    public List<LinkedHashMap<String, Object>> getDeletedRows() {
        return deletedRows;
    }
}
