package chaindb.ui;

/**
 * Application entry point. Boots the interactive console UI, which in turn
 * initializes the {@link chaindb.engine.DatabaseEngine} singleton, loading
 * and replaying any existing blockchain.dat before accepting commands.
 */
public class Main {
    public static void main(String[] args) {
        new ConsoleUI().start();
    }
}
