package chaindb.model;

import chaindb.exception.TableNotFoundException;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the entire in-memory database: a named collection of tables.
 * A Database instance is always reconstructed by replaying blockchain events
 * from genesis - it is never itself the source of truth.
 */
public class Database implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final LinkedHashMap<String, Table> tables;

    public Database(String name) {
        this.name = name;
        this.tables = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public void addTable(Table table) {
        tables.put(table.getName(), table);
    }

    public boolean hasTable(String tableName) {
        return tables.containsKey(tableName);
    }

    public Table getTable(String tableName) throws TableNotFoundException {
        Table table = tables.get(tableName);
        if (table == null) {
            throw new TableNotFoundException("Table '" + tableName + "' does not exist");
        }
        return table;
    }

    public Map<String, Table> getTables() {
        return tables;
    }
}
