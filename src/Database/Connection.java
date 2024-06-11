package Database;


import Database.Dataclasses.*;

import java.sql.SQLException;

public class Connection {
    private final MySQLConnection connection;

    public Connection(String host, int port, String username, String password, String databaseName) throws SQLException {
        this.connection = new MySQLConnection(host, port, databaseName, username, password);
        this.connection.prepareStatement("login", "SELECT * FROM player WHERE name=? AND password=?");
    }
    public Player login(String username, String password) throws SQLException {
        QueryResult loginResult = this.connection.runPreparedStatement("login", username, password);
        if (loginResult == null || loginResult.getRowCount() != 1) {
            return null;
        }
        String[] data = loginResult.getData()[0];
        return new Player(Integer.parseInt(data[0]), data[1], Float.parseFloat(data[2]), Float.parseFloat(data[3]), data[4]);
    }
    public void close() throws SQLException {
        this.connection.close();
    }
}
