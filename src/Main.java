import java.io.IOException;
import java.sql.SQLException;

import logging.Level;
import logging.Logger;


public class Main {
    public static void main(String[] args) throws SQLException, IOException {
        Logger root = new Logger("root");
        root.setLevel(Level.ALL);
        root.addHandler(new logging.ConsoleLogger());
        new Logger("Network.Connection").setLevel(Level.INFO);
        Server server = new Server(8080, "127.0.0.1", 3306, "informatik_game", "zugf5bVMC7&hVa43HQglV?3W0CIPYGFbR09!nT#", "informatik_game");
        server.start();
    }
}