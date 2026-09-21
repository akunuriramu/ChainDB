package chaindb.service;

import chaindb.blockchain.Block;
import chaindb.blockchain.Blockchain;
import chaindb.event.DatabaseEvent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Bonus feature: exports the entire blockchain to a human-readable JSON
 * file, hand-built with a StringBuilder since no external JSON library
 * is permitted.
 */
public class JsonExportService {

    private final Blockchain blockchain;

    public JsonExportService(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public void exportToFile(String path) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"chain\": [\n");

        List<Block> chain = blockchain.getChain();
        for (int i = 0; i < chain.size(); i++) {
            Block block = chain.get(i);
            DatabaseEvent event = block.getEvent();

            json.append("    {\n");
            json.append("      \"blockNumber\": ").append(block.getBlockNumber()).append(",\n");
            json.append("      \"timestamp\": ").append(block.getTimestamp()).append(",\n");
            json.append("      \"previousHash\": \"").append(block.getPreviousHash()).append("\",\n");
            json.append("      \"currentHash\": \"").append(block.getCurrentHash()).append("\",\n");
            json.append("      \"nonce\": ").append(block.getNonce()).append(",\n");
            json.append("      \"event\": ")
                    .append(event == null ? "null" : "\"" + escape(event.toString()) + "\"")
                    .append("\n");
            json.append("    }").append(i < chain.size() - 1 ? "," : "").append("\n");
        }

        json.append("  ]\n}\n");

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)) {
            writer.write(json.toString());
        }
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
