package Database.Types;

import Database.DataBase;
import logging.Logger;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

public class Player {
    private static final HashMap<Integer, Player> instances = new HashMap<>();
    public final int id;
    public final String name;
    public final float maxHP;
    public final float maxMP;
    public final String password;
    public boolean inGame;
    public final float defaultHP;
    public final float defaultMP;
    public final float regenHP;
    public final float regenMP;

    public Game currentGame;

    private final Logger logger = new Logger("Database.Dataclasses.Player");

    public Player(String[] dbData) {
        this.logger.debug("loading from data " + Arrays.toString(dbData));
        this.id = Integer.parseInt(dbData[0]);
        this.name = dbData[1];
        this.maxHP = Float.parseFloat(dbData[2]);
        this.maxMP = Float.parseFloat(dbData[3]);
        this.password = dbData[4];
        this.inGame = Integer.parseInt(dbData[5]) > 0;
        this.defaultHP = Float.parseFloat(dbData[6]);
        this.defaultMP = Float.parseFloat(dbData[7]);
        this.regenHP = Float.parseFloat(dbData[8]);
        this.regenMP = Float.parseFloat(dbData[9]);
    }
    public static Player load(String[] dbData, DataBase db) throws SQLException {
        Player instance = new Player(dbData);
        if (!instances.containsKey(instance.id) || !instances.get(instance.id).equals(instance)) {
            instances.put(instance.id, instance);
        }
        instance = instances.get(instance.id);
        instance.currentGame = db.getRunningGame(instance);
        return instance;
    }
    public static Player load(int id, DataBase db) throws SQLException {
        if (!instances.containsKey(id))
            return db.getPlayer(id);
        return instances.get(id);
    }
    public Player getUpdated(DataBase db) throws SQLException {
        return load(this.id, db);
    }
    public boolean equals(Player other) {
        if (other == null) return false;
        return this.id == other.id &&
                this.name.equals(other.name) &&
                this.maxHP == other.maxHP &&
                this.maxMP == other.maxMP &&
                this.defaultHP == other.defaultHP &&
                this.defaultMP == other.defaultMP &&
                this.regenHP == other.regenHP &&
                this.regenMP == other.regenMP &&
                this.inGame == other.inGame &&
                this.password.equals(other.password);
    }
}
