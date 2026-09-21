package chaindb.storage;

import chaindb.blockchain.Blockchain;

/**
 * Repository Pattern: abstracts how the blockchain is persisted and
 * loaded, so the engine does not need to know about file I/O directly.
 */
public interface BlockchainRepository {

    void save(Blockchain blockchain);

    /** Returns the persisted blockchain, or {@code null} if none exists yet. */
    Blockchain load();
}
