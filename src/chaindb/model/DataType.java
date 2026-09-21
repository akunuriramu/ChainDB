package chaindb.model;

/**
 * Represents the inferred data type of a column.
 * Columns start as ANY and are refined once the first value is inserted,
 * since CREATE TABLE statements in ChainDB do not declare explicit types.
 */
public enum DataType {
    ANY,
    INT,
    DOUBLE,
    STRING
}
