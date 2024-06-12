

import java.io.IOException;
import java.sql.SQLException;

import Abitur.Queue;
import Database.Dataclasses.Player;
import Network.Connection;
import Network.Packets.Error;
import Network.Packets.Heartbeat;
import Network.Packets.Packet;
import Network.Packets.Upstream.Login;
import Network.Packets.Upstream.SignUP;
import logging.Logger;

public class Server {
    private final Network.ConnectionHost connectionHost;
    private final Database.Connection db;
    private final Logger logger = new Logger("Server");
    private Thread thread;

    public Server(int programPort, String dataBaseHost, int dbPort, String dbUser, String dbPassword, String dbName) throws IOException, SQLException {
        this.connectionHost = new Network.ConnectionHost(programPort);
        this.db = new Database.Connection(dataBaseHost, dbPort, dbUser, dbPassword, dbName);
    }
    public void start() {
        this.connectionHost.start();
        this.thread = new Thread(this::readThread);
        this.thread.start();
    }
    public void stop() {
        this.thread.interrupt();
        this.connectionHost.stop();
    }
    private void readThread() {
        while (this.connectionHost.isAlive() && !this.thread.isInterrupted()) {
            this.connectionHost.updateConnection();
            this.handleIncomingData();
            this.handleOutgoingData();
        }
        logger.info("exiting readThread");
    }
    private void handleIncomingData() {
        Queue<Connection> connections = this.connectionHost.getConnectionsWithData();
        while (!connections.isEmpty()) {
            Connection connection = connections.front();
            Queue<Packet> packets = connection.incoming;
            while (!packets.isEmpty()) {
                Packet packet = packets.front();
                this.handlePacket(packet, connection);
                packets.dequeue();
            }
            connections.dequeue();
        }
    }
    private void handlePacket(Packet packet, Connection connection) {
        switch (packet) {
            case SignUP signUP -> this.handleSignUp(signUP, connection);
            case Login login -> this.handleLogin(login, connection);
            case Heartbeat _ -> {}
            case Error error -> this.handleError(error, connection);
            case null -> logger.error("Got null packet from " + connection);
            default -> logger.debug("Got unhandled packet: " + packet.getClass().getName() + " from connection: " + connection);
        }
    }
    private void handleSignUp(SignUP packet, Connection connection) {
        try {
            this.db.register(packet.username.value, packet.password.value);
            Player player = this.db.login(packet.username.value, packet.password.value);
            connection.playerID = player.id;
            this.logger.info("Registered player " + player.name);
        } catch (SQLException e) {
            String msg = String.format("Registration failed on connection %s with username \"%s\" because of \"%s\"", connection, packet.username.value, e.getMessage());
            this.logger.error(msg);
            connection.markForDeletion();
        }
    }
    private void handleLogin(Login packet, Connection connection) {
        try {
            Player player = this.db.login(packet.username, packet.password);
            if (player == null) {
                String msg = String.format("User \"%s\" couldn't login with hash: \"%s\"", packet.username, packet.password);
                this.logger.error(msg);
                connection.markForDeletion();
                return;
            }
            connection.playerID = player.id;
            this.logger.debug("Logged in player " + player.name);
        } catch (SQLException e) {
            connection.markForDeletion();
            String msg = String.format("Login failed on connection %s with username \"%s\" because of \"%s\"", connection, packet.username, e.getMessage());
            logger.error(msg);
        }
    }
    private void handleError(Error packet, Connection connection) {
        connection.markForDeletion();
        logger.error(connection + " had an error: " + packet.code);
    }
    private void handleOutgoingData() {
        Queue<Connection> connections = this.connectionHost.getConnections();
        while (!connections.isEmpty()) {
            connections.dequeue();
        }
    }
}
