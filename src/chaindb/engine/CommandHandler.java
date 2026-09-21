package chaindb.engine;

import chaindb.exception.ColumnNotFoundException;
import chaindb.exception.InvalidQueryException;
import chaindb.exception.TableNotFoundException;
import chaindb.parser.ParsedCommand;

/**
 * Command Pattern: represents a single executable handler for one family
 * of {@link chaindb.parser.CommandType}. Implementations encapsulate the
 * logic needed to turn a parsed command into a {@link QueryResult}.
 */
public interface CommandHandler {

    QueryResult handle(ParsedCommand command)
            throws TableNotFoundException, ColumnNotFoundException, InvalidQueryException;
}
