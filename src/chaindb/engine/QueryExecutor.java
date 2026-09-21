package chaindb.engine;

import chaindb.event.DatabaseEvent;
import chaindb.event.EventFactory;
import chaindb.exception.ColumnNotFoundException;
import chaindb.exception.InvalidQueryException;
import chaindb.exception.TableNotFoundException;
import chaindb.model.Column;
import chaindb.model.Database;
import chaindb.model.Row;
import chaindb.model.Table;
import chaindb.parser.CommandType;
import chaindb.parser.ParsedCommand;

import java.util.EnumMap;
import java.util.Map;

/**
 * Dispatches parsed SQL-like commands to the appropriate {@link CommandHandler}
 * (Command Pattern), keeping each command family's logic isolated and free
 * of duplication. Mutating commands (CREATE TABLE / INSERT / UPDATE / DELETE)
 * are routed through the {@link chaindb.event.EventFactory} and committed to
 * the blockchain via {@link DatabaseEngine#commitEvent}; read-only commands
 * (SELECT / SHOW TABLES / DESCRIBE) query the live in-memory database directly.
 */
public class QueryExecutor {

    private final DatabaseEngine engine;
    private final Map<CommandType, CommandHandler> handlers = new EnumMap<>(CommandType.class);

    public QueryExecutor(DatabaseEngine engine) {
        this.engine = engine;
        registerHandlers();
    }

    private void registerHandlers() {
        CommandHandler mutationHandler = this::handleMutation;
        handlers.put(CommandType.CREATE_TABLE, mutationHandler);
        handlers.put(CommandType.INSERT, mutationHandler);
        handlers.put(CommandType.UPDATE, mutationHandler);
        handlers.put(CommandType.DELETE, mutationHandler);

        handlers.put(CommandType.SELECT_ALL, this::handleSelectAll);
        handlers.put(CommandType.SELECT_WHERE, this::handleSelectWhere);
        handlers.put(CommandType.SHOW_TABLES, this::handleShowTables);
        handlers.put(CommandType.DESCRIBE, this::handleDescribe);
    }

    public QueryResult execute(ParsedCommand command) {
        CommandHandler handler = handlers.get(command.getType());
        if (handler == null) {
            return QueryResult.error("Command type not handled by QueryExecutor: " + command.getType());
        }
        try {
            return handler.handle(command);
        } catch (TableNotFoundException | ColumnNotFoundException | InvalidQueryException e) {
            return QueryResult.error(e.getMessage());
        }
    }

    private QueryResult handleMutation(ParsedCommand command)
            throws TableNotFoundException, ColumnNotFoundException, InvalidQueryException {
        Database database = engine.getDatabase();
        if (command.getType() == CommandType.CREATE_TABLE && database.hasTable(command.getTableName())) {
            return QueryResult.error("Table '" + command.getTableName() + "' already exists");
        }
        DatabaseEvent event = EventFactory.createEvent(command, database);
        engine.commitEvent(event);
        return QueryResult.message(describeSuccess(command));
    }

    private QueryResult handleSelectAll(ParsedCommand command) throws TableNotFoundException {
        Table table = engine.getDatabase().getTable(command.getTableName());
        return QueryResult.rows(table.getColumns(), table.getAllRows());
    }

    private QueryResult handleSelectWhere(ParsedCommand command) throws TableNotFoundException, ColumnNotFoundException {
        Table table = engine.getDatabase().getTable(command.getTableName());
        java.util.List<Row> rows = table.selectWhere(command.getWhereColumn(), command.getWhereValue());
        return QueryResult.rows(table.getColumns(), rows);
    }

    private QueryResult handleShowTables(ParsedCommand command) {
        Database database = engine.getDatabase();
        StringBuilder builder = new StringBuilder();
        if (database.getTables().isEmpty()) {
            builder.append("No tables found.");
        } else {
            builder.append("Tables:\n");
            for (String name : database.getTables().keySet()) {
                builder.append("  - ").append(name).append("\n");
            }
        }
        return QueryResult.message(builder.toString().trim());
    }

    private QueryResult handleDescribe(ParsedCommand command) throws TableNotFoundException {
        Table table = engine.getDatabase().getTable(command.getTableName());
        StringBuilder builder = new StringBuilder("Table: ").append(table.getName()).append("\n");
        for (Column column : table.getColumns()) {
            builder.append("  ").append(column.getName()).append(" : ").append(column.getType()).append("\n");
        }
        return QueryResult.message(builder.toString().trim());
    }

    private String describeSuccess(ParsedCommand command) {
        switch (command.getType()) {
            case CREATE_TABLE:
                return "Table '" + command.getTableName() + "' created.";
            case INSERT:
                return "1 row inserted into '" + command.getTableName() + "'.";
            case UPDATE:
                return "Row(s) updated in '" + command.getTableName() + "'.";
            case DELETE:
                return "Row(s) deleted from '" + command.getTableName() + "'.";
            default:
                return "OK";
        }
    }
}
