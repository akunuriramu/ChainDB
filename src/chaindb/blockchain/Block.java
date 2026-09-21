package chaindb.blockchain;

import chaindb.event.DatabaseEvent;
import chaindb.util.HashUtil;

import java.io.Serializable;

/**
 * A single block in the ChainDB blockchain. Each block wraps exactly one
 * {@link DatabaseEvent} (except the genesis block, whose event is null)
 * and is linked to its predecessor by {@code previousHash}.
 *
 * Blocks are mined with a simple proof-of-work scheme purely for tamper
 * resistance - there is no cryptocurrency, mining reward, or peer network.
 */
public class Block implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int blockNumber;
    private final long timestamp;
    private final DatabaseEvent event;
    private final String previousHash;
    private String currentHash;
    private long nonce;

    public Block(int blockNumber, DatabaseEvent event, String previousHash) {
        this.blockNumber = blockNumber;
        this.timestamp = System.currentTimeMillis();
        this.event = event;
        this.previousHash = previousHash;
        this.nonce = 0;
        this.currentHash = calculateHash();
    }

    /** Recomputes this block's hash from its current fields (including nonce). */
    public String calculateHash() {
        String data = blockNumber
                + Long.toString(timestamp)
                + String.valueOf(event)
                + previousHash
                + nonce;
        return HashUtil.sha256(data);
    }

    /**
     * Simple proof-of-work: increments the nonce until the block's hash
     * begins with {@code difficulty} leading zero hex characters.
     */
    public void mineBlock(int difficulty) {
        String target = "0".repeat(difficulty);
        while (currentHash == null || !currentHash.startsWith(target)) {
            nonce++;
            currentHash = calculateHash();
        }
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public DatabaseEvent getEvent() {
        return event;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getCurrentHash() {
        return currentHash;
    }

    public long getNonce() {
        return nonce;
    }
}
