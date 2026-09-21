package chaindb.parser;

/**
 * Enumerates every command ChainDB's console can recognize, spanning
 * both data-mutating SQL-like statements and administrative/meta commands.
 */
public enum CommandType {
    CREATE_TABLE,
    INSERT,
    UPDATE,
    DELETE,
    SELECT_ALL,
    SELECT_WHERE,
    SHOW_TABLES,
    DESCRIBE,
    REPORT,
    VERIFY_CHAIN,
    EXPLORER,
    EXPORT_JSON,
    UNDO,
    HELP,
    EXIT
}
