package chaindb.engine;

import chaindb.blockchain.Block;
import chaindb.blockchain.Blockchain;
import chaindb.event.DatabaseEvent;
import chaindb.exception.ColumnNotFoundException;
import chaindb.exception.TableNotFoundException;
import chaindb.model.Database;
import chaindb.storage.BlockchainRepository;
import chaindb.storage.FileBlockchainRepository;

/**
 * Singleton Pattern: the single central engine that owns the in-memory
 * {@link Database} and the {@link Blockchain} backing it, and coordinates
 * loading, replaying, committing, and persisting state.
 *
 * There is intentionally only ever one DatabaseEngine per running
 * application, since there is only one blockchain file and one
 * reconstructed database state.
 */
public final class DatabaseEngine {

    private static DatabaseEngine instance;

    private final Database database;
    private final Blockchain blockchain;
    private final BlockchainRepository repository;

    private DatabaseEngine() {
        this.repository = new FileBlockchainRepository("blockchain.dat");
        Blockchain loaded = repository.load();
        this.blockchain = (loaded != null)
                ? loaded
                : new Blockchain(Integer.getInteger("chaindb.difficulty", Blockchain.DEFAULT_DIFFICULTY));
        this.database = new Database("ChainDB");
        replayEvents();
    }

    public static synchronized DatabaseEngine getInstance() {
        if (instance == null) {
            instance = new DatabaseEngine();
        }
        return instance;
    }

    /** Rebuilds the in-memory database purely by replaying every event in the chain. */
    private void replayEvents() {
        for (Block block : blockchain.getChain()) {
            DatabaseEvent event = block.getEvent();
            if (event == null) {
                continue; // genesis block carries no event
            }
            try {
                event.apply(database);
            } catch (TableNotFoundException | ColumnNotFoundException e) {
                throw new IllegalStateException(
                        "Failed to replay event at block #" + block.getBlockNumber() + ": " + e.getMessage(), e);
            }
        }
    }

    public Database getDatabase() {
        return database;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    /**
     * Applies the event to the live database, mines and appends a new block
     * for it, and persists the updated chain. If applying the event fails,
     * no block is appended and nothing is persisted (atomic behavior).
     */
    public Block commitEvent(DatabaseEvent event) throws TableNotFoundException, ColumnNotFoundException {
        event.apply(database);
        Block block = blockchain.appendBlock(event);
        repository.save(blockchain);
        return block;
    }

    public void persist() {
        repository.save(blockchain);
    }
}
