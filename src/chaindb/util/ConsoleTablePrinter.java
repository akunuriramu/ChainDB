package chaindb.util;

import chaindb.model.Column;
import chaindb.model.Row;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Renders query results as an aligned ASCII table on the console,
 * similar to the output of traditional SQL client tools.
 */
public final class ConsoleTablePrinter {

    private ConsoleTablePrinter() {
        // static utility class
    }

    public static void print(Collection<Column> columns, List<Row> rows) {
        List<String> columnNames = new ArrayList<>();
        for (Column column : columns) {
            columnNames.add(column.getName());
        }

        int[] widths = new int[columnNames.size()];
        for (int i = 0; i < columnNames.size(); i++) {
            widths[i] = columnNames.get(i).length();
        }
        for (Row row : rows) {
            for (int i = 0; i < columnNames.size(); i++) {
                Object value = row.getValues().get(columnNames.get(i));
                int length = (value == null) ? 4 : value.toString().length();
                widths[i] = Math.max(widths[i], length);
            }
        }

        printSeparator(widths);
        printRow(columnNames, widths);
        printSeparator(widths);
        for (Row row : rows) {
            List<String> cellValues = new ArrayList<>();
            for (String columnName : columnNames) {
                Object value = row.getValues().get(columnName);
                cellValues.add(value == null ? "NULL" : value.toString());
            }
            printRow(cellValues, widths);
        }
        printSeparator(widths);
        System.out.println(rows.size() + " row(s) returned.");
    }

    private static void printSeparator(int[] widths) {
        StringBuilder line = new StringBuilder("+");
        for (int width : widths) {
            line.append("-".repeat(width + 2)).append("+");
        }
        System.out.println(line);
    }

    private static void printRow(List<String> values, int[] widths) {
        StringBuilder line = new StringBuilder("|");
        for (int i = 0; i < values.size(); i++) {
            line.append(" ").append(pad(values.get(i), widths[i])).append(" |");
        }
        System.out.println(line);
    }

    private static String pad(String value, int width) {
        StringBuilder padded = new StringBuilder(value);
        while (padded.length() < width) {
            padded.append(' ');
        }
        return padded.toString();
    }
}
