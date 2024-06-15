import java.io.IOException;
import java.sql.SQLException;

import logging.Level;
import logging.Logger;


public class Main {
    public static void main(String[] args) throws SQLException, IOException {
        Logger root = new Logger("root");
        root.setLevel(Level.ALL);
        root.addHandler(new logging.ConsoleLogger());
        Server server = new Server(8080, "127.0.0.1", 3306, "informatik_game", "zugf5bVMC7&hVa43HQglV?3W0CIPYGFbR09!nT#", "informatik_game");
        server.start();

/*
        Connection connection = new Connection("127.0.0.1", 3306, "informatik_game", "zugf5bVMC7&hVa43HQglV?3W0CIPYGFbR09!nT#", "informatik_game");
        ConnectionHost networkHost = new ConnectionHost(8080);
        networkHost.start();
        while (networkHost.isAlive()) {
            Queue<Network.Connection> connections = networkHost.getConnectionsWithData();
            while (!connections.isEmpty()) {
                Network.Connection currentConnection = connections.front();
                while (!currentConnection.incoming.isEmpty()) {
                    Network.Packets.Packet packet = currentConnection.incoming.front();
                    if (packet instanceof Network.Packets.Upstream.Login) {
                        Database.Dataclasses.Player player = connection.login(((Login) packet).username, ((Login) packet).password);
                        if (player == null) {
                            root.error("Login failed");
                        } else {
                            root.info("Login successful for player " + player.name);
                        }
                    } else {
                        System.out.println("Packet received " + packet.getClass().getName());
                    }
                    currentConnection.incoming.dequeue();
                }
            }
        }
        root.error("Server stopped");
*/
    }
}