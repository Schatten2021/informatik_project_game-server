package Database;


import Database.Dataclasses.*;

import java.sql.SQLException;

public class Connection {
    private final MySQLConnection connection;

    public Connection(String host, int port, String username, String password, String databaseName) throws SQLException {
        this.connection = new MySQLConnection(host, port, databaseName, username, password);
        this.connection.prepareStatement("login", "SELECT * FROM player WHERE name=? AND password=?;");
        this.connection.prepareStatement("register", "INSERT INTO player VALUES(NULL,?,100,100,?, FALSE, DEFAULT, DEFAULT);");
        this.connection.prepareStatement("newGame", "INSERT INTO game VALUES(NULL, DEFAULT, ?, ?, ?, DEFAULT, ?, ?, ?, DEFAULT, DEFAULT, DEFAULT);");
        this.connection.prepareStatement("inGame", "UPDATE player SET InGame=? WHERE id=?");
        this.connection.prepareStatement("getRunningGames", "SELECT game.* FROM player, game WHERE (player1ID=player.id OR player2ID=player.id) AND NOT game.finished AND player.id=?;");
        this.connection.prepareStatement("getPlayer", "SELECT * FROM player WHERE id=?");
    }
    public Player login(String username, String password) throws SQLException {
        QueryResult loginResult = this.connection.runPreparedStatement("login", username, password);
        if (loginResult == null || loginResult.getRowCount() != 1) {
            return null;
        }
        String[] data = loginResult.getData()[0];
        return new Player(data);
    }
    public void register(String username, String password) throws SQLException {
        this.connection.runPreparedStatement("register", username, password);
    }
    public void newGame(Player player1, Player player2) throws SQLException {
        this.connection.runPreparedStatement("newGame", player1.id, player1.defaultHP, player1.defaultMP, player2.id, player2.defaultHP, player2.defaultMP);
        this.connection.runPreparedStatement("inGame", true, player1.id);
        this.connection.runPreparedStatement("inGame", true, player2.id);
        player1.inGame = true;
        player2.inGame = true;
    }
    public Game getRunningGame(Player player) throws SQLException {
        QueryResult games = this.connection.runPreparedStatement("getRunningGames", player.id);
        if (games == null || games.getRowCount() < 1) {
            return null;
        }
        return new Game(games.getData()[0]);
    }
    public Player getPlayer(int id) throws SQLException {
        QueryResult result = this.connection.runPreparedStatement("getPlayer", id);
        if (result.getRowCount() != 1) {
            return null;
        }
        return new Player(result.getData()[0]);
    }
}
