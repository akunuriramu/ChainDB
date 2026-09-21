package chaindb.ui;

import chaindb.engine.DatabaseEngine;
import chaindb.engine.QueryExecutor;
import chaindb.engine.QueryResult;
import chaindb.exception.InvalidQueryException;
import chaindb.parser.ParsedCommand;
import chaindb.parser.SqlParser;
import chaindb.service.ExplorerService;
import chaindb.service.JsonExportService;
import chaindb.service.ReportService;
import chaindb.service.UndoService;
import chaindb.util.ConsoleTablePrinter;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * The interactive terminal front-end for ChainDB. Reads a line at a time,
 * parses it into a {@link ParsedCommand}, and either dispatches it to the
 * {@link QueryExecutor} (for SQL-like data commands) or handles it directly
 * (for administrative/meta commands like REPORT or EXPLORER).
 */
public class ConsoleUI {

    private final DatabaseEngine engine;
    private final QueryExecutor executor;
    private final SqlParser parser;
    private final Scanner scanner;

    public ConsoleUI() {
        this.engine = DatabaseEngine.getInstance();
        this.executor = new QueryExecutor(engine);
        this.parser = new SqlParser();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printBanner();
        boolean running = true;

        while (running) {
            System.out.print("ChainDB > ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) {
                continue;
            }

            try {
                ParsedCommand command = parser.parse(line);
                switch (command.getType()) {
                    case EXIT:
                        System.out.println("Persisting blockchain and shutting down...");
                        engine.persist();
                        running = false;
                        break;
                    case HELP:
                        printHelp();
                        break;
                    case REPORT:
                        System.out.println(new ReportService(engine.getDatabase(), engine.getBlockchain())
                                .generateReport());
                        break;
                    case VERIFY_CHAIN:
                        runVerify();
                        break;
                    case EXPLORER:
                        runExplorer();
                        break;
                    case EXPORT_JSON:
                        runExport();
                        break;
                    case UNDO:
                        System.out.println(new UndoService(engine).undoLast());
                        break;
                    default:
                        render(executor.execute(command));
                }
            } catch (InvalidQueryException e) {
                System.out.println("Query error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    private void render(QueryResult result) {
        if (result.hasRows()) {
            ConsoleTablePrinter.print(result.getColumns(), result.getRows());
        } else if (result.isSuccess()) {
            System.out.println(result.getMessage());
        } else {
            System.out.println("Error: " + result.getMessage());
        }
    }

    private void runVerify() {
        List<String> errors = engine.getBlockchain().verifyChain();
        if (errors.isEmpty()) {
            System.out.println("Blockchain integrity check: VALID. All "
                    + (engine.getBlockchain().getChain().size() - 1) + " block(s) verified successfully.");
        } else {
            System.out.println("Blockchain integrity check: INVALID.");
            for (String error : errors) {
                System.out.println("  ! " + error);
            }
        }
    }

    private void runExplorer() {
        ExplorerService explorer = new ExplorerService(engine.getBlockchain());
        System.out.println("--- Blockchain Explorer ---");
        System.out.println(explorer.listAllBlocks());
        System.out.print("Enter a block number to inspect, 'search <keyword>', or press Enter to exit: ");
        String input = scanner.nextLine();
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        input = input.trim();
        if (input.toLowerCase(Locale.ROOT).startsWith("search ")) {
            String keyword = input.substring(7).trim();
            System.out.println(explorer.searchEvents(keyword));
        } else {
            try {
                int blockNumber = Integer.parseInt(input);
                System.out.println(explorer.describeBlock(blockNumber));
            } catch (NumberFormatException e) {
                System.out.println("Invalid block number.");
            }
        }
    }

    private void runExport() {
        try {
            String path = "blockchain_export.json";
            new JsonExportService(engine.getBlockchain()).exportToFile(path);
            System.out.println("Blockchain exported to " + path);
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    private void printBanner() {
        System.out.println("=====================================================");
        System.out.println("  ChainDB - Event-Sourced Database Engine");
        System.out.println("  with Blockchain Integrity Verification");
        System.out.println("=====================================================");
        System.out.println("Type HELP for a list of commands.");
        int events = engine.getBlockchain().getChain().size() - 1;
        System.out.println("Loaded existing blockchain with " + events + " event(s).");
        System.out.println();
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  CREATE TABLE <table>(<col1>,<col2>,...)");
        System.out.println("  INSERT INTO <table> VALUES(<v1>,<v2>,...)");
        System.out.println("  UPDATE <table> SET <col>=<val>[,<col>=<val>...] WHERE <col>=<val>");
        System.out.println("  DELETE FROM <table> WHERE <col>=<val>");
        System.out.println("  SELECT * FROM <table>");
        System.out.println("  SELECT * FROM <table> WHERE <col>=<val>");
        System.out.println("  SHOW TABLES");
        System.out.println("  DESCRIBE <table>");
        System.out.println("  REPORT            - generate a statistics report");
        System.out.println("  VERIFYCHAIN       - verify blockchain integrity");
        System.out.println("  EXPLORER          - browse blocks and search events");
        System.out.println("  EXPORT JSON       - export blockchain to blockchain_export.json");
        System.out.println("  UNDO              - undo the last operation (compensating event)");
        System.out.println("  HELP              - show this help message");
        System.out.println("  EXIT              - persist and quit");
    }
}
