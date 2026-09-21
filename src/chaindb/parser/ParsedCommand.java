package chaindb.parser;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * An immutable, structured representation of a single parsed user command.
 * Built via the nested {@link Builder} since different command types
 * populate very different subsets of fields.
 */
public class ParsedCommand {

    private final CommandType type;
    private final String tableName;
    private final List<String> columnNames;
    private final LinkedHashMap<String, Object> values;
    private final LinkedHashMap<String, Object> setValues;
    private final String whereColumn;
    private final Object whereValue;

    private ParsedCommand(Builder builder) {
        this.type = builder.type;
        this.tableName = builder.tableName;
        this.columnNames = builder.columnNames;
        this.values = builder.values;
        this.setValues = builder.setValues;
        this.whereColumn = builder.whereColumn;
        this.whereValue = builder.whereValue;
    }

    public CommandType getType() {
        return type;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public LinkedHashMap<String, Object> getValues() {
        return values;
    }

    public LinkedHashMap<String, Object> getSetValues() {
        return setValues;
    }

    public String getWhereColumn() {
        return whereColumn;
    }

    public Object getWhereValue() {
        return whereValue;
    }

    public static class Builder {
        private CommandType type;
        private String tableName;
        private List<String> columnNames;
        private LinkedHashMap<String, Object> values;
        private LinkedHashMap<String, Object> setValues;
        private String whereColumn;
        private Object whereValue;

        public Builder type(CommandType type) {
            this.type = type;
            return this;
        }

        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder columnNames(List<String> columnNames) {
            this.columnNames = columnNames;
            return this;
        }

        public Builder values(LinkedHashMap<String, Object> values) {
            this.values = values;
            return this;
        }

        public Builder setValues(LinkedHashMap<String, Object> setValues) {
            this.setValues = setValues;
            return this;
        }

        public Builder whereColumn(String whereColumn) {
            this.whereColumn = whereColumn;
            return this;
        }

        public Builder whereValue(Object whereValue) {
            this.whereValue = whereValue;
            return this;
        }

        public ParsedCommand build() {
            return new ParsedCommand(this);
        }
    }
}
