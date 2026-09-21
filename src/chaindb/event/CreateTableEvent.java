package chaindb.event;

import chaindb.model.Database;
import chaindb.model.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Event representing the creation of a new table.
 */
public class CreateTableEvent extends DatabaseEvent {

    private static final long serialVersionUID = 1L;

    private final List<String> columnNames;

    public CreateTableEvent(String tableName, List<String> columnNames) {
        super(tableName, "CREATE_TABLE");
        this.columnNames = new ArrayList<>(columnNames);
    }

    @Override
    public void apply(Database database) {
        Table table = new Table(tableName, columnNames);
        database.addTable(table);
    }

    @Override
    public String getPayload() {
        return "columns=" + columnNames;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }
}
