package Database;

import Database.Types.*;
import logging.Logger;

import java.sql.SQLException;

public class DataBase {
    private final MySQLConnection connection;
    private final Logger logger = new Logger("Database");

    public DataBase(String host, int port, String username, String password, String databaseName) throws SQLException {
        this.connection = new MySQLConnection(host, port, databaseName, username, password);
        this.connection.prepareStatement("getAbility", "SELECT * FROM abilities WHERE ID=?;");
        this.connection.prepareStatement("getEffectsForAbility", "SELECT effects.* FROM effects, abilities, abilityEffects WHERE effects.id=abilityEffects.effectId AND abilities.ID=abilityEffects.abilityId AND abilities.ID=?");
        this.connection.prepareStatement("getEffect", "SELECT * FROM effects WHERE id=?;");
        this.connection.prepareStatement("getAbility", "SELECT * FROM abilities WHERE ID=?;");
        this.connection.prepareStatement("getPlayer", "SELECT * FROM player WHERE id=?;");
        this.connection.prepareStatement("getGame", "SELECT * FROM game WHERE id=?;");
        this.connection.prepareStatement("getAbilityUsed", "SELECT * FROM abilitiesUsed WHERE id=?;");
        this.connection.prepareStatement("getAbilitiesUsedInGame", "SELECT * FROM abilitiesUsed WHERE gameId=?;");
        this.connection.prepareStatement("login", "SELECT * FROM player WHERE name=? AND password=?;");
        this.connection.prepareStatement("signUp", "INSERT INTO player VALUES (DEFAULT, ?, DEFAULT, DEFAULT, ?, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT)");
        this.connection.prepareStatement("getRunningGamesForPlayer", "SELECT game.* FROM player, game WHERE (game.player1ID=player.id OR game.player2ID=player.id) AND player.id=? AND game.finished=FALSE;");
        this.connection.prepareStatement("newGame", "INSERT INTO game VALUES (DEFAULT, DEFAULT, ?, ?, ?, ?, ?, DEFAULT, ?, ?, ?, ?, ?, DEFAULT, DEFAULT, DEFAULT);");
        this.connection.prepareStatement("getLastInsertedGame", "SELECT * FROM game WHERE id=(SELECT MAX(id) FROM game)");
        this.connection.prepareStatement("abilityUsed", "INSERT INTO abilitiesUsed VALUES (DEFAULT, ?, ?, ?, ?, ?);");
        this.connection.prepareStatement("getLastAbilityUsed", "SELECT * FROM abilitiesUsed WHERE id=(SELECT MAX(id) FROM abilitiesUsed)");
        this.connection.prepareStatement("newRound", "UPDATE game SET round = round + 1 WHERE id=?;");
        this.connection.prepareStatement("player1finishedRound", "UPDATE game SET player1FinishedTurns = round WHERE id=?");
        this.connection.prepareStatement("player2finishedRound", "UPDATE game SET player2FinishedTurns = round WHERE id=?");
        this.connection.prepareStatement("updateGame", "UPDATE game SET player1HP=?, player1HPRegen=?, player1MP=?, player1MPRegen=?, player2HP=?, player2HPRegen=?, player2MP=?, player2MPRegen=? WHERE id=?");
        this.connection.prepareStatement("gameOver", "UPDATE game SET finished=TRUE, result=? WHERE id=?");
        this.connection.prepareStatement("getAllAbilities", "SELECT * FROM abilities;");
        this.connection.prepareStatement("getAllEffects", "SELECT * FROM effects;");
        this.connection.prepareStatement("getAllRunningGames", "SELECT * FROM game WHERE finished=FALSE");
        this.connection.prepareStatement("getAllAbilitiesUsed", "SELECT * FROM abilitiesUsed WHERE gameId=?");
        this.connection.prepareStatement("inGame", "UPDATE player SET InGame=? WHERE id=?");
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
        this.connection.runPreparedStatement("newGame", player1.id, player1.defaultHP, player1.regenHP, player1.defaultMP, player1.regenMP, player2.id, player2.defaultHP, player2.regenHP, player2.defaultMP, player2.regenMP);
        this.connection.runPreparedStatement("inGame", true, player1.id);
        this.connection.runPreparedStatement("inGame", true, player2.id);
        QueryResult queryResult = this.connection.runPreparedStatement("getLastInsertedGame");
        String[][] data = queryResult.getData();
        if (data.length != 1) {
            return null;
        }
        Game game = Game.load(data[0], this);
        player1.currentGame = game;
        player2.currentGame = game;
        player1.inGame = true;
        player2.inGame = true;
        return game;
    }
    public void abilityUsed(Ability ability, Player player, Game game) throws SQLException {
        boolean player1 = player.id == game.player1ID;
        this.connection.runPreparedStatement("abilityUsed", game.id, ability.id, game.round, player1, Math.random());
        QueryResult queryResult = this.connection.runPreparedStatement("getLastAbilityUsed");
        String[][] data = queryResult.getData();
        if (data.length != 1) {
            this.logger.fatal("got no data for freshly inserted \"abilityUsed\"");
            throw new SQLException();
        }
        game.abilitiesUsed.append(AbilityUsed.load(data[0], this));
        if (player1) {
            game.player1MP -= ability.cost;
        } else {
            game.player2MP -= ability.cost;
        }
        this.updateDB(game);
    }
    public void newRound(Game game) throws SQLException {
        game.player1HP += game.player1HPRegen;
        game.player1MP += game.player1MPRegen;
        game.player2HP += game.player2HPRegen;
        game.player2MP += game.player2MPRegen;
        game.round++;
        this.updateDB(game);
    }
    public void playerFinishedRound(Player player) throws SQLException {
        player.currentGame = player.currentGame.getUpdated(this);
        Game game = player.currentGame;
        if (game == null) {
            this.logger.error("player has no running game");
            return;
        }
        if (game.player1ID == player.id) {
            this.connection.runPreparedStatement("player1finishedRound", game.id);
            game.player1FinishedTurns++;
        } else {
            this.connection.runPreparedStatement("player2finishedRound", game.id);
            game.player2FinishedTurns++;
        }
    }
    public AbilityUsed[] getAllAbilitiesUsed(Game game) throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("getAllAbilitiesUsed", game.id);
        String[][] data = queryResult.getData();
        AbilityUsed[] usedAbilities = new AbilityUsed[data.length];
        for (int i = 0; i < data.length; i++) {
            usedAbilities[i] = AbilityUsed.load(data[i], this);
        }
        return usedAbilities;
    }
    public void updateDB(Game game) throws SQLException {
        if (game.player1HP <= 0 && game.player2HP <= 0) {
            this.connection.runPreparedStatement("gameOver", 0, game.id);
            game.finished = true;
            game.result = 0;
        } else if (game.player1HP <= 0 || game.player2HP <= 0) {
            int result = (game.player1HP <= 0) ? -1 : 1;
            this.connection.runPreparedStatement("gameOver", result, game.id);
            game.finished = true;
            game.result = result;
        }
        this.connection.runPreparedStatement("updateGame", game.player1HP, game.player1HPRegen, game.player1MP, game.player1MPRegen, game.player2HP, game.player2HPRegen, game.player2MP, game.player2MPRegen, game.id);
        if (game.finished) {
            this.connection.runPreparedStatement("inGame", false, game.player1.id);
            this.connection.runPreparedStatement("inGame", false, game.player2.id);
        }
    }
    public Ability[] allAbilities() throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("getAllAbilities");
        String[][] data = queryResult.getData();
        Ability[] abilities = new Ability[data.length];
        for (int i = 0; i < abilities.length; i++) {
            abilities[i] = Ability.load(data[i], this);
        }
        return abilities;
    }
    public Effect[] allEffects() throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("getAllEffects");
        String[][] data = queryResult.getData();
        Effect[] effects = new Effect[data.length];
        for (int i = 0; i < effects.length; i++) {
            effects[i] = Effect.load(data[i]);
        }
        return effects;
    }
    public Game[] getRunningGames() throws SQLException {
        QueryResult queryResult = this.connection.runPreparedStatement("getAllRunningGames");
        String[][] data = queryResult.getData();
        Game[] games = new Game[data.length];
        for (int i = 0; i < data.length; i++) {
            games[i] = Game.load(data[i], this);
        }
        return games;
    }
    public void playerNotInGame(Player p) throws SQLException {
        p.inGame = false;
        this.connection.runPreparedStatement("inGame", false, p.id);
    }
}
