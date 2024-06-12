package Network;

import Abitur.List;
import Abitur.Queue;
import logging.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionHost {
    private final ServerSocket serverSocket;
    private final Queue<Socket> incomingConnections = new Queue<>();
    private final List<Connection> connections = new List<>();
    private final Logger logger = new Logger("Network.ConnectionHost");
    private Thread acceptorThread;
    public ConnectionHost(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }
    public boolean isAlive() {
        return this.acceptorThread != null && this.acceptorThread.isAlive() && this.serverSocket.isBound() && !this.serverSocket.isClosed();
    }
    public void start() {
        this.acceptorThread = new Thread(this::acceptThread);
        this.acceptorThread.start();
        this.logger.info("Starting Server socket on port " + this.serverSocket.getLocalPort());
    }
    private void acceptThread() {
        while (this.isAlive()) {
            try {
                Socket socket = this.serverSocket.accept();
                this.incomingConnections.enqueue(socket);
                this.logger.debug("Accepted Connection from " + socket.getRemoteSocketAddress());
            } catch (IOException e) {
                this.close();
            }
        }
    }
    public void stop() {
        this.close();
    }
    private void close() {
        // close the serverSocket
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            this.logger.error(e.getMessage());
        }
        this.connections.toFirst();
        // close all connections
        while (this.connections.hasAccess()) {
            this.connections.getContent().stop();
            this.connections.remove();
        }
        // close all incoming sockets
        while (!this.incomingConnections.isEmpty()) {
            try {
                this.incomingConnections.front().close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.incomingConnections.dequeue();
        }
    }
    public Queue<Connection> getConnectionsWithData() {
        Queue<Connection> connectionsWithData = new Queue<>();
        this.connections.toFirst();
        while (this.connections.hasAccess()) {
            Connection connection = this.connections.getContent();
            if (connection.incoming.isEmpty()) {
                this.connections.next();
                continue;
            }
            connectionsWithData.enqueue(connection);
            this.connections.next();
        }
        return connectionsWithData;
    }
    public Queue<Connection> getConnections() {
        this.connections.toFirst();
        Queue<Connection> connections = new Queue<>();
        while (this.connections.hasAccess()) {
            connections.enqueue(this.connections.getContent());
            this.connections.next();
        }
        return connections;
    }
    public void updateConnection() {
        while (!this.incomingConnections.isEmpty()) {
            try {
                Connection connection = new Connection(this.incomingConnections.front());
                this.connections.append(connection);
                this.logger.info("now also listening to connection " + connection);
            } catch (IOException e) {
                this.logger.error(e.getMessage());
            }
            this.incomingConnections.dequeue();
        }
        this.connections.toFirst();
        while (this.connections.hasAccess()) {
            Connection connection = this.connections.getContent();
            try {
                connection.update();
                this.connections.next();
            } catch (IOException e) {
                String msg = String.format("removing connection %s because of \"%s\"", connection, e.getMessage());
                this.logger.info(msg);
                connection.stop();
                this.connections.remove();
            }
        }
    }
}
