package chaindb.storage;

import chaindb.blockchain.Blockchain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Persists the blockchain to a single local file using plain Java
 * serialization. This file (blockchain.dat by default) is ChainDB's only
 * persistent storage - there is no separate database file of any kind.
 */
public class FileBlockchainRepository implements BlockchainRepository {

    private final String filePath;

    public FileBlockchainRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void save(Blockchain blockchain) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(blockchain);
        } catch (IOException e) {
            System.err.println("Warning: failed to persist blockchain to '" + filePath + "': " + e.getMessage());
        }
    }

    @Override
    public Blockchain load() {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (Blockchain) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Warning: failed to load existing blockchain from '" + filePath
                    + "', starting fresh: " + e.getMessage());
            return null;
        }
    }
}
