package chaindb.event;

import chaindb.exception.ColumnNotFoundException;
import chaindb.exception.TableNotFoundException;
import chaindb.model.Database;
import chaindb.model.Row;
import chaindb.model.Table;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Event representing the insertion of a single row into a table.
 */
public class InsertEvent extends DatabaseEvent {

    private static final long serialVersionUID = 1L;

    private final LinkedHashMap<String, Object> values;

    public InsertEvent(String tableName, LinkedHashMap<String, Object> values) {
        super(tableName, "INSERT");
        this.values = values;
    }

    @Override
    public void apply(Database database) throws TableNotFoundException, ColumnNotFoundException {
        Table table = database.getTable(tableName);
        table.insertRow(new Row(values));
    }

    @Override
    public String getPayload() {
        return values.toString();
    }

    public Map<String, Object> getValues() {
        return values;
    }
}
