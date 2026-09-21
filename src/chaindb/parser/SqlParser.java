package chaindb.parser;

import chaindb.exception.InvalidQueryException;
import chaindb.util.ValueParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses raw console input into a structured {@link ParsedCommand}.
 *
 * Recognizes the SQL-like grammar described in the ChainDB specification
 * (CREATE TABLE / INSERT / UPDATE / DELETE / SELECT / SHOW TABLES / DESCRIBE)
 * as well as ChainDB's administrative commands (REPORT, VERIFYCHAIN,
 * EXPLORER, EXPORT JSON, UNDO, HELP, EXIT).
 */
public class SqlParser {

    private static final Pattern CREATE_TABLE =
            Pattern.compile("(?i)^CREATE\\s+TABLE\\s+(\\w+)\\s*\\((.+)\\)\\s*$");
    private static final Pattern INSERT =
            Pattern.compile("(?i)^INSERT\\s+INTO\\s+(\\w+)\\s+VALUES\\s*\\((.+)\\)\\s*$");
    private static final Pattern UPDATE =
            Pattern.compile("(?i)^UPDATE\\s+(\\w+)\\s+SET\\s+(.+?)\\s+WHERE\\s+(.+)$");
    private static final Pattern DELETE =
            Pattern.compile("(?i)^DELETE\\s+FROM\\s+(\\w+)\\s+WHERE\\s+(.+)$");
    private static final Pattern SELECT_WHERE =
            Pattern.compile("(?i)^SELECT\\s+\\*\\s+FROM\\s+(\\w+)\\s+WHERE\\s+(.+)$");
    private static final Pattern SELECT_ALL =
            Pattern.compile("(?i)^SELECT\\s+\\*\\s+FROM\\s+(\\w+)\\s*$");
    private static final Pattern SHOW_TABLES =
            Pattern.compile("(?i)^SHOW\\s+TABLES\\s*$");
    private static final Pattern DESCRIBE =
            Pattern.compile("(?i)^DESCRIBE\\s+(\\w+)\\s*$");

    public ParsedCommand parse(String rawInput) throws InvalidQueryException {
        String input = rawInput.trim();
        if (input.endsWith(";")) {
            input = input.substring(0, input.length() - 1).trim();
        }
        if (input.isEmpty()) {
            throw new InvalidQueryException("Empty command");
        }

        String upper = input.toUpperCase(Locale.ROOT);
        switch (upper) {
            case "EXIT":
            case "QUIT":
                return new ParsedCommand.Builder().type(CommandType.EXIT).build();
            case "HELP":
                return new ParsedCommand.Builder().type(CommandType.HELP).build();
            case "REPORT":
            case "GENERATE REPORT":
            case "GENERATE REPORTS":
                return new ParsedCommand.Builder().type(CommandType.REPORT).build();
            case "VERIFYCHAIN":
            case "VERIFY CHAIN":
                return new ParsedCommand.Builder().type(CommandType.VERIFY_CHAIN).build();
            case "EXPLORER":
                return new ParsedCommand.Builder().type(CommandType.EXPLORER).build();
            case "EXPORT JSON":
            case "EXPORTJSON":
                return new ParsedCommand.Builder().type(CommandType.EXPORT_JSON).build();
            case "UNDO":
                return new ParsedCommand.Builder().type(CommandType.UNDO).build();
            default:
                break;
        }

        Matcher matcher;

        matcher = CREATE_TABLE.matcher(input);
if (matcher.matches()) {
    List<String> columns = new ArrayList<>();

    for (String column : matcher.group(2).split(",")) {

        column = column.trim();

        // Extract only the column name
        String[] parts = column.split("\\s+");

        columns.add(parts[0]);
    }

    return new ParsedCommand.Builder()
            .type(CommandType.CREATE_TABLE)
            .tableName(matcher.group(1))
            .columnNames(columns)
            .build();
}

        matcher = INSERT.matcher(input);
        if (matcher.matches()) {
            List<String> tokens = ValueParser.splitRespectingQuotes(matcher.group(2));
            LinkedHashMap<String, Object> positionalValues = new LinkedHashMap<>();
            for (int i = 0; i < tokens.size(); i++) {
                positionalValues.put("__pos" + i, ValueParser.parse(tokens.get(i)));
            }
            return new ParsedCommand.Builder()
                    .type(CommandType.INSERT)
                    .tableName(matcher.group(1))
                    .values(positionalValues)
                    .build();
        }

        matcher = UPDATE.matcher(input);
        if (matcher.matches()) {
            LinkedHashMap<String, Object> setValues = parseAssignments(matcher.group(2));
            String[] whereParts = splitEquality(matcher.group(3));
            return new ParsedCommand.Builder()
                    .type(CommandType.UPDATE)
                    .tableName(matcher.group(1))
                    .setValues(setValues)
                    .whereColumn(whereParts[0])
                    .whereValue(ValueParser.parse(whereParts[1]))
                    .build();
        }

        matcher = DELETE.matcher(input);
        if (matcher.matches()) {
            String[] whereParts = splitEquality(matcher.group(2));
            return new ParsedCommand.Builder()
                    .type(CommandType.DELETE)
                    .tableName(matcher.group(1))
                    .whereColumn(whereParts[0])
                    .whereValue(ValueParser.parse(whereParts[1]))
                    .build();
        }

        matcher = SELECT_WHERE.matcher(input);
        if (matcher.matches()) {
            String[] whereParts = splitEquality(matcher.group(2));
            return new ParsedCommand.Builder()
                    .type(CommandType.SELECT_WHERE)
                    .tableName(matcher.group(1))
                    .whereColumn(whereParts[0])
                    .whereValue(ValueParser.parse(whereParts[1]))
                    .build();
        }

        matcher = SELECT_ALL.matcher(input);
        if (matcher.matches()) {
            return new ParsedCommand.Builder()
                    .type(CommandType.SELECT_ALL)
                    .tableName(matcher.group(1))
                    .build();
        }

        matcher = SHOW_TABLES.matcher(input);
        if (matcher.matches()) {
            return new ParsedCommand.Builder().type(CommandType.SHOW_TABLES).build();
        }

        matcher = DESCRIBE.matcher(input);
        if (matcher.matches()) {
            return new ParsedCommand.Builder()
                    .type(CommandType.DESCRIBE)
                    .tableName(matcher.group(1))
                    .build();
        }

        throw new InvalidQueryException("Unrecognized or malformed command: " + rawInput);
    }

    private LinkedHashMap<String, Object> parseAssignments(String clause) throws InvalidQueryException {
        LinkedHashMap<String, Object> assignments = new LinkedHashMap<>();
        for (String part : ValueParser.splitRespectingQuotes(clause)) {
            String[] keyValue = splitEquality(part);
            assignments.put(keyValue[0], ValueParser.parse(keyValue[1]));
        }
        return assignments;
    }

    private String[] splitEquality(String clause) throws InvalidQueryException {
        int equalsIndex = clause.indexOf('=');
        if (equalsIndex < 0) {
            throw new InvalidQueryException("Expected 'column=value' in: " + clause);
        }
        String column = clause.substring(0, equalsIndex).trim();
        String value = clause.substring(equalsIndex + 1).trim();
        return new String[]{column, value};
    }
}
