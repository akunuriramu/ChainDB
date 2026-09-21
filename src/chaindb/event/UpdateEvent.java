package chaindb.event;

import chaindb.exception.ColumnNotFoundException;
import chaindb.exception.TableNotFoundException;
import chaindb.model.Database;
import chaindb.model.Table;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Event representing an update of every row matching {@code whereColumn == whereValue}.
 *
 * {@code previousRows} is a snapshot of the affected rows *before* the update was
 * applied. It is not needed to replay the event, but it allows the UNDO feature to
 * build a compensating event that restores the prior state without inventing a
 * separate "delete history" mechanism.
 */
public class UpdateEvent extends DatabaseEvent {

    private static final long serialVersionUID = 1L;

    private final String whereColumn;
    private final Object whereValue;
    private final LinkedHashMap<String, Object> newValues;
    private final List<LinkedHashMap<String, Object>> previousRows;

    public UpdateEvent(String tableName, String whereColumn, Object whereValue,
                        LinkedHashMap<String, Object> newValues,
                        List<LinkedHashMap<String, Object>> previousRows) {
        super(tableName, "UPDATE");
        this.whereColumn = whereColumn;
        this.whereValue = whereValue;
        this.newValues = newValues;
        this.previousRows = previousRows;
    }

    @Override
    public void apply(Database database) throws TableNotFoundException, ColumnNotFoundException {
        Table table = database.getTable(tableName);
        table.updateRows(whereColumn, whereValue, newValues);
    }

    @Override
    public String getPayload() {
        return "SET " + newValues + " WHERE " + whereColumn + "=" + whereValue;
    }

    public String getWhereColumn() {
        return whereColumn;
    }

    public Object getWhereValue() {
        return whereValue;
    }

    public LinkedHashMap<String, Object> getNewValues() {
        return newValues;
    }

    public List<LinkedHashMap<String, Object>> getPreviousRows() {
        return previousRows;
    }
}
