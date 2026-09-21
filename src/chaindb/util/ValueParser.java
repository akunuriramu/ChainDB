package chaindb.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts raw SQL-like literal tokens ("101", "\"Alice\"", "8.4") into
 * their corresponding Java types (Integer, String, Double), and splits
 * comma-separated value lists while respecting quoted strings.
 */
public final class ValueParser {

    private ValueParser() {
        // static utility class
    }

    /** Parses a single literal token into an Integer, Double, or String. */
    public static Object parse(String token) {
        String trimmed = token.trim();
        if (trimmed.length() >= 2 && isQuoted(trimmed)) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException notAnInt) {
            try {
                return Double.parseDouble(trimmed);
            } catch (NumberFormatException notADouble) {
                return trimmed;
            }
        }
    }

    private static boolean isQuoted(String s) {
        char first = s.charAt(0);
        char last = s.charAt(s.length() - 1);
        return (first == '"' && last == '"') || (first == '\'' && last == '\'');
    }

    /**
     * Splits a comma-separated list of literals, ignoring commas that
     * appear inside single or double quoted strings.
     */
    public static List<String> splitRespectingQuotes(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;

        for (char c : input.toCharArray()) {
            if (inQuotes) {
                current.append(c);
                if (c == quoteChar) {
                    inQuotes = false;
                }
            } else if (c == '"' || c == '\'') {
                inQuotes = true;
                quoteChar = c;
                current.append(c);
            } else if (c == ',') {
                tokens.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            tokens.add(current.toString().trim());
        }
        return tokens;
    }
}
