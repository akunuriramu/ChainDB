package chaindb.event;

import chaindb.exception.ColumnNotFoundException;
import chaindb.exception.InvalidQueryException;
import chaindb.exception.TableNotFoundException;
import chaindb.model.Column;
import chaindb.model.Database;
import chaindb.model.Row;
import chaindb.model.Table;
import chaindb.parser.CommandType;
import chaindb.parser.ParsedCommand;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Factory Pattern: translates a parsed, table-name-and-values-only
 * {@link ParsedCommand} into the concrete {@link DatabaseEvent} subtype
 * that represents it, resolving positional INSERT values against the
 * target table's declared column order along the way.
 */
public final class EventFactory {

    private EventFactory() {
        // static factory class
    }

    public static DatabaseEvent createEvent(ParsedCommand command, Database database)
            throws TableNotFoundException, ColumnNotFoundException, InvalidQueryException {
        switch (command.getType()) {
            case CREATE_TABLE:
                return new CreateTableEvent(command.getTableName(), command.getColumnNames());

            case INSERT:
                return buildInsertEvent(command, database);

            case UPDATE:
                return buildUpdateEvent(command, database);

            case DELETE:
                return buildDeleteEvent(command, database);

            default:
                throw new InvalidQueryException("Cannot create a database event for command type: " + command.getType());
        }
    }

    private static DatabaseEvent buildInsertEvent(ParsedCommand command, Database database)
            throws TableNotFoundException, InvalidQueryException {
        Table table = database.getTable(command.getTableName());
        List<Column> columns = new ArrayList<>(table.getColumns());
        LinkedHashMap<String, Object> positionalValues = command.getValues();

        if (positionalValues.size() != columns.size()) {
            throw new InvalidQueryException(
                    "Table '" + command.getTableName() + "' expects " + columns.size()
                            + " value(s) but " + positionalValues.size() + " were given");
        }

        LinkedHashMap<String, Object> mapped = new LinkedHashMap<>();
        int index = 0;
        for (Object value : positionalValues.values()) {
            mapped.put(columns.get(index).getName(), value);
            index++;
        }
        return new InsertEvent(command.getTableName(), mapped);
    }

    private static DatabaseEvent buildUpdateEvent(ParsedCommand command, Database database)
            throws TableNotFoundException, ColumnNotFoundException {
        Table table = database.getTable(command.getTableName());
        List<LinkedHashMap<String, Object>> previousRows = snapshot(
                table.selectWhere(command.getWhereColumn(), command.getWhereValue()));
        return new UpdateEvent(command.getTableName(), command.getWhereColumn(), command.getWhereValue(),
                command.getSetValues(), previousRows);
    }

    private static DatabaseEvent buildDeleteEvent(ParsedCommand command, Database database)
            throws TableNotFoundException, ColumnNotFoundException {
        Table table = database.getTable(command.getTableName());
        List<LinkedHashMap<String, Object>> previousRows = snapshot(
                table.selectWhere(command.getWhereColumn(), command.getWhereValue()));
        return new DeleteEvent(command.getTableName(), command.getWhereColumn(), command.getWhereValue(), previousRows);
    }

    private static List<LinkedHashMap<String, Object>> snapshot(List<Row> rows) {
        List<LinkedHashMap<String, Object>> snapshot = new ArrayList<>();
        for (Row row : rows) {
            snapshot.add(new LinkedHashMap<>(row.getValues()));
        }
        return snapshot;
    }
}
