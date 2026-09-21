package chaindb.service;

import chaindb.blockchain.Block;
import chaindb.blockchain.Blockchain;
import chaindb.event.DatabaseEvent;

import java.util.Date;
import java.util.Locale;

/**
 * Bonus feature: a lightweight blockchain explorer allowing the user to
 * list all blocks, inspect a specific block's full details, or search
 * events by a free-text keyword.
 */
public class ExplorerService {

    private final Blockchain blockchain;

    public ExplorerService(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public String listAllBlocks() {
        StringBuilder builder = new StringBuilder();
        for (Block block : blockchain.getChain()) {
            String hash = block.getCurrentHash();
            String shortHash = hash.length() > 12 ? hash.substring(0, 12) + "..." : hash;
            builder.append("#").append(block.getBlockNumber()).append(" ")
                    .append(block.getEvent() == null ? "GENESIS" : block.getEvent().toString())
                    .append(" | hash=").append(shortHash)
                    .append("\n");
        }
        return builder.toString().trim();
    }

    public String describeBlock(int blockNumber) {
        for (Block block : blockchain.getChain()) {
            if (block.getBlockNumber() == blockNumber) {
                return formatBlock(block);
            }
        }
        return "Block #" + blockNumber + " not found. Valid range: 0-" + (blockchain.getChain().size() - 1);
    }

    private String formatBlock(Block block) {
        StringBuilder builder = new StringBuilder();
        builder.append("Block #").append(block.getBlockNumber()).append("\n");
        builder.append("  Timestamp     : ").append(new Date(block.getTimestamp())).append("\n");
        builder.append("  Event         : ")
                .append(block.getEvent() == null ? "GENESIS" : block.getEvent().toString()).append("\n");
        builder.append("  Previous Hash : ").append(block.getPreviousHash()).append("\n");
        builder.append("  Current Hash  : ").append(block.getCurrentHash()).append("\n");
        builder.append("  Nonce         : ").append(block.getNonce());
        return builder.toString();
    }

    public String searchEvents(String keyword) {
        StringBuilder builder = new StringBuilder();
        String needle = keyword.toLowerCase(Locale.ROOT);
        int matches = 0;
        for (Block block : blockchain.getChain()) {
            DatabaseEvent event = block.getEvent();
            if (event == null) {
                continue;
            }
            String haystack = (event.getTableName() + " " + event.getOperation() + " " + event.getPayload())
                    .toLowerCase(Locale.ROOT);
            if (haystack.contains(needle)) {
                builder.append("Block #").append(block.getBlockNumber()).append(": ").append(event).append("\n");
                matches++;
            }
        }
        if (matches == 0) {
            return "No matching events found for '" + keyword + "'.";
        }
        return builder.toString().trim();
    }
}
