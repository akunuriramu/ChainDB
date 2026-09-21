package chaindb.engine;

import chaindb.model.Column;
import chaindb.model.Row;

import java.util.Collection;
import java.util.List;

/**
 * The outcome of executing a single {@link chaindb.parser.ParsedCommand}.
 * Either carries a plain status message, a set of result rows (SELECT),
 * or an error message.
 */
public class QueryResult {

    private final boolean success;
    private final String message;
    private final Collection<Column> columns;
    private final List<Row> rows;

    private QueryResult(boolean success, String message, Collection<Column> columns, List<Row> rows) {
        this.success = success;
        this.message = message;
        this.columns = columns;
        this.rows = rows;
    }

    public static QueryResult message(String message) {
        return new QueryResult(true, message, null, null);
    }

    public static QueryResult error(String message) {
        return new QueryResult(false, message, null, null);
    }

    public static QueryResult rows(Collection<Column> columns, List<Row> rows) {
        return new QueryResult(true, null, columns, rows);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Collection<Column> getColumns() {
        return columns;
    }

    public List<Row> getRows() {
        return rows;
    }

    public boolean hasRows() {
        return rows != null;
    }
}
