package chaindb.model;

import chaindb.exception.ColumnNotFoundException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an in-memory table: an ordered set of {@link Column} definitions
 * plus the {@link Row} data currently held in that table.
 *
 * Table never persists itself directly - its state is always a product of
 * replaying {@code DatabaseEvent}s from the blockchain.
 */
public class Table implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final LinkedHashMap<String, Column> columns;
    private final List<Row> rows;

    public Table(String name, List<String> columnNames) {
        this.name = name;
        this.columns = new LinkedHashMap<>();
        for (String columnName : columnNames) {
            columns.put(columnName, new Column(columnName));
        }
        this.rows = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Collection<Column> getColumns() {
        return columns.values();
    }

    public boolean hasColumn(String columnName) {
        return columns.containsKey(columnName);
    }

    /** Returns a defensive copy of all rows currently in the table. */
    public List<Row> getAllRows() {
        return new ArrayList<>(rows);
    }

    /**
     * Inserts a new row, validating that every supplied column exists,
     * and refines column type metadata from the inserted values.
     */
    public void insertRow(Row row) throws ColumnNotFoundException {
        for (String key : row.getValues().keySet()) {
            requireColumn(key);
        }
        for (Map.Entry<String, Object> entry : row.getValues().entrySet()) {
            columns.get(entry.getKey()).inferType(entry.getValue());
        }
        rows.add(row);
    }

    /** Returns all rows where {@code column == value}. */
    public List<Row> selectWhere(String column, Object value) throws ColumnNotFoundException {
        requireColumn(column);
        List<Row> result = new ArrayList<>();
        for (Row row : rows) {
            if (valuesEqual(row.getValues().get(column), value)) {
                result.add(row);
            }
        }
        return result;
    }

    /**
     * Updates every row matching {@code whereColumn == whereValue} with the
     * supplied new values. Returns the rows that were updated (post-update state).
     */
    public List<Row> updateRows(String whereColumn, Object whereValue, Map<String, Object> newValues)
            throws ColumnNotFoundException {
        requireColumn(whereColumn);
        for (String key : newValues.keySet()) {
            requireColumn(key);
        }
        List<Row> updated = new ArrayList<>();
        for (Row row : rows) {
            if (valuesEqual(row.getValues().get(whereColumn), whereValue)) {
                row.getValues().putAll(newValues);
                for (Map.Entry<String, Object> entry : newValues.entrySet()) {
                    columns.get(entry.getKey()).inferType(entry.getValue());
                }
                updated.add(row);
            }
        }
        return updated;
    }

    /** Removes every row matching {@code whereColumn == whereValue}. Returns the removed rows. */
    public List<Row> deleteRows(String whereColumn, Object whereValue) throws ColumnNotFoundException {
        requireColumn(whereColumn);
        List<Row> removed = new ArrayList<>();
        Iterator<Row> iterator = rows.iterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            if (valuesEqual(row.getValues().get(whereColumn), whereValue)) {
                removed.add(row);
                iterator.remove();
            }
        }
        return removed;
    }

    private void requireColumn(String columnName) throws ColumnNotFoundException {
        if (!columns.containsKey(columnName)) {
            throw new ColumnNotFoundException(
                    "Column '" + columnName + "' not found in table '" + name + "'");
        }
    }

    /** Loosely compares two values, coercing numeric types so 101 == 101.0. */
    public static boolean valuesEqual(Object a, Object b) {
        if (a == null || b == null) {
            return Objects.equals(a, b);
        }
        if (a instanceof Number && b instanceof Number) {
            return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        }
        return a.toString().equals(b.toString());
    }
}
