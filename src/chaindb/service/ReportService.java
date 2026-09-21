package chaindb.service;

import chaindb.blockchain.Block;
import chaindb.blockchain.Blockchain;
import chaindb.event.CreateTableEvent;
import chaindb.event.DatabaseEvent;
import chaindb.event.DeleteEvent;
import chaindb.event.InsertEvent;
import chaindb.event.UpdateEvent;
import chaindb.model.Database;
import chaindb.model.Table;

import java.util.List;

/**
 * Computes and formats the ChainDB statistics report: table/row counts,
 * blockchain size, operation-type breakdown, and integrity status.
 */
public class ReportService {

    private final Database database;
    private final Blockchain blockchain;

    public ReportService(Database database, Blockchain blockchain) {
        this.database = database;
        this.blockchain = blockchain;
    }

    public String generateReport() {
        int totalTables = database.getTables().size();
        int totalRows = 0;
        for (Table table : database.getTables().values()) {
            totalRows += table.getAllRows().size();
        }

        int totalBlocks = blockchain.getChain().size();
        int createTableOps = 0;
        int insertOps = 0;
        int updateOps = 0;
        int deleteOps = 0;

        for (Block block : blockchain.getChain()) {
            DatabaseEvent event = block.getEvent();
            if (event == null) {
                continue;
            }
            if (event instanceof InsertEvent) {
                insertOps++;
            } else if (event instanceof UpdateEvent) {
                updateOps++;
            } else if (event instanceof DeleteEvent) {
                deleteOps++;
            } else if (event instanceof CreateTableEvent) {
                createTableOps++;
            }
        }

        List<String> errors = blockchain.verifyChain();

        StringBuilder report = new StringBuilder();
        report.append("========== ChainDB Report ==========\n");
        report.append(String.format("Total Tables:            %d%n", totalTables));
        report.append(String.format("Total Rows:               %d%n", totalRows));
        report.append(String.format("Total Blocks:             %d%n", totalBlocks));
        report.append(String.format("Total CREATE_TABLE ops:   %d%n", createTableOps));
        report.append(String.format("Total INSERT operations:  %d%n", insertOps));
        report.append(String.format("Total UPDATE operations:  %d%n", updateOps));
        report.append(String.format("Total DELETE operations:  %d%n", deleteOps));
        report.append(String.format("Blockchain Valid:         %s%n",
                errors.isEmpty() ? "YES" : "NO (" + errors.size() + " issue(s))"));
        if (!errors.isEmpty()) {
            for (String error : errors) {
                report.append("  ! ").append(error).append("\n");
            }
        }
        report.append("=====================================");
        return report.toString();
    }
}
