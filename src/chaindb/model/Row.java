package chaindb.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a single row of data within a {@link Table}.
 * Values are keyed by column name and stored in insertion order.
 */
public class Row implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LinkedHashMap<String, Object> values;

    public Row(Map<String, Object> values) {
        this.values = new LinkedHashMap<>(values);
    }

    public LinkedHashMap<String, Object> getValues() {
        return values;
    }

    /**
     * Produces a copy of this row backed by a new map.
     * Used when snapshotting rows for undo/compensating events.
     */
    public Row copy() {
        return new Row(new LinkedHashMap<>(values));
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
