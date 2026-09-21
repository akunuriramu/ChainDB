package chaindb.blockchain;

import chaindb.event.DatabaseEvent;
import chaindb.exception.BlockchainException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The append-only chain of {@link Block}s that backs ChainDB.
 *
 * The blockchain is ChainDB's *only* persistent storage - there is no
 * separate database file. Application state is always reconstructed by
 * replaying every event in the chain, in order, from the genesis block.
 */
public class Blockchain implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Number of leading zero hex characters required of every mined block hash. */
    public static final int DEFAULT_DIFFICULTY = 4;

    private final List<Block> chain;
    private final int difficulty;

    public Blockchain() {
        this(DEFAULT_DIFFICULTY);
    }

    public Blockchain(int difficulty) {
        this.difficulty = difficulty;
        this.chain = new ArrayList<>();
        this.chain.add(createGenesisBlock());
    }

    private Block createGenesisBlock() {
        Block genesis = new Block(0, null, "0");
        genesis.mineBlock(difficulty);
        return genesis;
    }

    /**
     * Wraps the given event in a new, mined block and appends it to the chain.
     * The block is linked to the current last block via previousHash.
     */
    public Block appendBlock(DatabaseEvent event) {
        Block previous = chain.get(chain.size() - 1);
        Block block = new Block(chain.size(), event, previous.getCurrentHash());
        block.mineBlock(difficulty);
        chain.add(block);
        return block;
    }

    public List<Block> getChain() {
        return chain;
    }

    public int getDifficulty() {
        return difficulty;
    }

    /**
     * Verifies the integrity of the entire chain: for every block, its stored
     * hash must match a fresh recalculation, and its previousHash must match
     * the actual hash of its predecessor. Returns a list of human-readable
     * error descriptions (empty if the chain is fully valid).
     */
    public List<String> verifyChain() {
        List<String> errors = new ArrayList<>();
        String zeroTarget = "0".repeat(difficulty);

        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);

            String recalculated = current.calculateHash();
            if (!current.getCurrentHash().equals(recalculated)) {
                errors.add("Block #" + current.getBlockNumber()
                        + ": stored hash does not match recalculated hash (data tampering detected)");
            }
            if (!current.getPreviousHash().equals(previous.getCurrentHash())) {
                errors.add("Block #" + current.getBlockNumber()
                        + ": previousHash does not match hash of Block #" + previous.getBlockNumber());
            }
            if (!current.getCurrentHash().startsWith(zeroTarget)) {
                errors.add("Block #" + current.getBlockNumber()
                        + ": hash does not satisfy proof-of-work difficulty (" + difficulty + ")");
            }
        }
        return errors;
    }

    public boolean isValid() {
        return verifyChain().isEmpty();
    }

    /** Strict variant of {@link #verifyChain()} that throws instead of returning errors. */
    public void verifyChainStrict() throws BlockchainException {
        List<String> errors = verifyChain();
        if (!errors.isEmpty()) {
            throw new BlockchainException(
                    "Blockchain integrity check failed with " + errors.size()
                            + " issue(s): " + String.join("; ", errors));
        }
    }
}
