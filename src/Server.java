

import java.io.IOException;
import java.sql.SQLException;

import Abitur.Queue;
import Database.Dataclasses.Game;
import Database.Dataclasses.Player;
import Network.Connection;
import Network.Packets.Downstream.GameStart;
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

    private final Queue<Connection> availablePlayers = new Queue<>();

    public Server(int programPort, String dataBaseHost, int dbPort, String dbUser, String dbPassword, String dbName) throws IOException, SQLException {
        this.connectionHost = new Network.ConnectionHost(programPort);
        this.db = new Database.Connection(dataBaseHost, dbPort, dbUser, dbPassword, dbName);
    }
    public void start() {
        if (thread != null) {
            this.logger.warn("Already started");
            return;
        }
        this.connectionHost.start();
        this.thread = new Thread(this::readThread);
        this.thread.start();
    }
    public void stop() {
        this.thread.interrupt();
        this.connectionHost.stop();
        while (!this.availablePlayers.isEmpty())
            this.availablePlayers.dequeue();
    }
    private void readThread() {
        while (this.connectionHost.isAlive() && !this.thread.isInterrupted()) {
            this.connectionHost.updateConnection();
            this.handleIncomingData();
            this.checkWaitingConnections();
            this.checkNewGames();
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
            connection.player = player;
            this.availablePlayers.enqueue(connection);
            this.logger.info("Registered player " + player.name);
            this.logger.info("Player + \"" + player.name + "\" now waiting for a game.");
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
            connection.player = player;
            this.logger.debug("Logged in player \"" + player.name + "\" in game: " + player.inGame);
            if (!player.inGame) {
                this.availablePlayers.enqueue(connection);
                this.logger.info("Player \"" + player.name + "\" now waiting for a game.");
            } else {
                this.playerReenterGame(connection);
            }
        } catch (SQLException | IOException e) {
            connection.markForDeletion();
            String msg = String.format("Login failed on connection %s with username \"%s\" because of \"%s\"", connection, packet.username, e.getMessage());
            logger.error(msg);
        }
    }
    private void playerReenterGame(Connection connection) throws SQLException, IOException {
        Player player = connection.player;
        Game game = this.db.getRunningGame(player);
        this.sendGame(connection, game);
        String msg = String.format("Player \"%s\" has continued playing", player.name);
        this.logger.info(msg);

    }
    private void handleError(Error packet, Connection connection) {
        connection.markForDeletion();
        logger.error(connection + " had an error: " + packet.code);
    }
    private void handleOutgoingData() {
        Queue<Connection> connections = this.connectionHost.getConnections();
        while (!connections.isEmpty()) {
            Connection connection = connections.front();
            connections.dequeue();
        }
    }
    private void checkNewGames() {
        while (!this.availablePlayers.isEmpty()) {
            // get first player
            Connection connection1 = this.availablePlayers.front();
            Player player1 = connection1.player;
            this.availablePlayers.dequeue();
            // only one player in queue
            if (this.availablePlayers.isEmpty()) {
                this.availablePlayers.enqueue(connection1);
                break;
            }

            // get second player
            Connection connection2 = this.availablePlayers.front();
            Player player2 = connection2.player;
            this.availablePlayers.dequeue();

            // add the game in the database
            try {
                this.db.newGame(player1, player2);
            } catch (SQLException e) {
                String msg = String.format("Couldn't pair player \"%s\" to player \"%s\" because of SQLException \"%s\".", player1.name, player2.name, e.getMessage());
                this.logger.error(msg);
                connection1.markForDeletion();
                connection2.markForDeletion();
                return;
            }
            Game game;
            try {
                game = this.db.getRunningGame(player1);
            } catch (SQLException e) {
                this.logger.fatal("Game not registered (" + e.getMessage() + ")");
                return;
            }

            // tell the players that they are in a game
            try {
                this.sendGame(connection1, game);
                this.sendGame(connection2, game);
                String msg = String.format("Player \"%s\" now playing against \"%s\".", player1.name, player2.name);
                this.logger.info(msg);
            } catch (IOException e) {
                String msg = String.format("Couldn't pair player \"%s\" to player \"%s\" because of IOException \"%s\".", player1.name, player2.name, e.getMessage());
                this.logger.error(msg);
                connection1.markForDeletion();
                connection2.markForDeletion();
            }
        }
    }
    private void sendGame(Connection connection, Game game) throws IOException {
        Player player = connection.player;
        Player other;
        if (player.id == game.player1ID) {
            other = game.player2;
        } else {
            other = game.player1;
        }
        connection.send(new GameStart(
                player.defaultHP,
                player.regenHP,
                player.defaultMP,
                player.regenMP,
                other.name,
                other.defaultHP,
                other.regenHP,
                other.defaultMP,
                other.regenMP
        ));
    }
    private void checkWaitingConnections() {
        Queue<Connection> previouslyWaitingConnections = new Queue<>();
        while (!this.availablePlayers.isEmpty()) {
            previouslyWaitingConnections.enqueue(this.availablePlayers.front());
            this.availablePlayers.dequeue();
        }
        while (!previouslyWaitingConnections.isEmpty()) {
            Connection connection = previouslyWaitingConnections.front();
            previouslyWaitingConnections.dequeue();
            if (!connection.isDeleted()) {
                this.availablePlayers.enqueue(connection);
                continue;
            }
            String msg = String.format("Player \"%s\" now stopped waiting for a connection due to a disconnect.", connection.player);
            this.logger.info(msg);
        }
    }
}
