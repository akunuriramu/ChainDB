package chaindb.model;

import java.io.Serializable;

/**
 * Represents a single column definition within a {@link Table}.
 * The type of a column is inferred dynamically from the first value
 * inserted into it, since ChainDB's CREATE TABLE syntax is type-free.
 */
public class Column implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private DataType type;

    public Column(String name) {
        this.name = name;
        this.type = DataType.ANY;
    }

    /**
     * Refines this column's type based on an observed value.
     * Once a concrete type has been inferred it is not changed again.
     */
    public void inferType(Object value) {
        if (this.type == DataType.ANY && value != null) {
            if (value instanceof Integer || value instanceof Long) {
                this.type = DataType.INT;
            } else if (value instanceof Double || value instanceof Float) {
                this.type = DataType.DOUBLE;
            } else {
                this.type = DataType.STRING;
            }
        }
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + ":" + type;
    }
}
