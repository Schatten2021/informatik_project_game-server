package Database;

import Database.Types.*;

import java.sql.SQLException;

public class DataBase {
    private final MySQLConnection connection;

    public DataBase(String host, int port, String username, String password, String databaseName) throws SQLException {
        this.connection = new MySQLConnection(host, port, databaseName, username, password);
        this.connection.prepareStatement("getAbility", "SELECT * FROM abilities WHERE ID=?;");
        this.connection.prepareStatement("getEffectsForAbility", "SELECT effects.* FROM effects, abilities, abilityEffects WHERE effects.id=abilityEffects.effectId AND abilities.ID=abilityEffects.abilityId AND abilities.ID=?");
        this.connection.prepareStatement("getEffect", "SELECT * FROM effects WHERE id=?;");
        this.connection.prepareStatement("getAbility", "SELECT * FROM abilities WHERE ID=?;");
        this.connection.prepareStatement("getPlayer", "SELECT * FROM player WHERE id=?;");
        this.connection.prepareStatement("getGame", "SELECT * FROM game WHERE id=?;");
        this.connection.prepareStatement("getAbilityUsed", "SELECT * FROM abilitiesUsed WHERE id=?;");
        this.connection.prepareStatement("getAbilitiesUsedInGame", "SELECT abilitiesUsed.* FROM game, abilitiesUsed WHERE abilitiesUsed.gameId=game.id && game.id=?;");
        this.connection.prepareStatement("login", "SELECT * FROM player WHERE name=? AND password=?;");
        this.connection.prepareStatement("signUp", "INSERT INTO player VALUES (DEFAULT, ?, DEFAULT, DEFAULT, ?, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT)");
        this.connection.prepareStatement("getRunningGamesForPlayer", "SELECT game.* FROM player, game WHERE (game.player1ID=player.id OR game.player2ID=player.id) AND player.id=?;");
        this.connection.prepareStatement("newGame", "INSERT INTO game VALUES (DEFAULT, DEFAULT, ?, ?, ?, DEFAULT, ?, ?, ?, DEFAULT, DEFAULT, DEFAULT);SELECT * FROM game WHERE LAST_INSERT_ID()=id;");
    }
    public Ability getAbility(int id) throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("getAbility", id);
        String[][] data = queryResult.getData();
        if (data.length != 1) {
            return null;
        }
        return Ability.load(data[0], this);
    }
    public Effect[] getEffects(Ability ability) throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("getEffectsForAbility", ability.id);
        String[][] data = queryResult.getData();
        Effect[] effects = new Effect[data.length];
        for (int i = 0; i < data.length; i++) {
            effects[i] = Effect.load(data[i]);
        }
        return effects;
    }
    public Effect getEffect(int id) throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("getEffect", id);
        String[][] data = queryResult.getData();
        if (data.length != 1) {
            return null;
        }
        return Effect.load(data[0]);
    }
    public Game getGame(int id) throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("getGame", id);
        String[][] data = queryResult.getData();
        if (data.length != 1) {
            return null;
        }
        return Game.load(data[0], this);
    }
    public Player getPlayer(int id) throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("getPlayer", id);
        String[][] data = queryResult.getData();
        if (data.length != 1) {
            return null;
        }
        return Player.load(data[0], this);
    }
    public AbilityUsed getAbilityUsed(int id) throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("getAbilityUsed", id);
        String[][] data = queryResult.getData();
        if (data.length != 1) {
            return null;
        }
        return AbilityUsed.load(data[0], this);
    }
    public AbilityUsed[] getAbilitiesUsed(Game game) throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("getAbilitiesUsedInGame", game.id);
        String[][] data = queryResult.getData();
        AbilityUsed[] abilitiesUsed = new AbilityUsed[data.length];
        for (int i = 0; i < data.length; i++) {
            abilitiesUsed[i] = AbilityUsed.load(data[i], this);
        }
        return abilitiesUsed;
    }
    public Player login(String username, String password) throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("login", username, password);
        String[][] data = queryResult.getData();
        if (data.length != 1) {
            return null;
        }
        return Player.load(data[0], this);
    }
    public Player signUp(String username, String password) throws SQLException {
        this.connection.runPreparedStatement("signUp", username, password);
        return this.login(username, password);
    }
    public Game getRunningGame(Player player) throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("getRunningGamesForPlayer", player.id);
        String[][] data = queryResult.getData();
        if (data.length != 1) {
            return null;
        }
        return Game.load(data[0], this);
    }
    public Game newGame(Player player1, Player player2) throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("newGame", player1.id, player1.defaultHP, player1.defaultMP, player2.id, player2.defaultHP, player2.defaultMP);
        String[][] data = queryResult.getData();
        if (data.length != 1) {
            return null;
        }
        return Game.load(data[0], this);
    }
}
