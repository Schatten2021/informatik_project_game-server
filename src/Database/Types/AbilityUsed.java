package Database.Types;

import Database.DataBase;

import java.sql.SQLException;
import java.util.HashMap;

public class AbilityUsed {
    private static final HashMap<Integer, AbilityUsed> instances = new HashMap<>();

    public final int id;
    public final int gameId;
    public final int abilityId;
    public final int round;
    public final boolean player1;

    public final Ability ability;

    private AbilityUsed(String[] data, DataBase db) throws SQLException {
        this.id = Integer.parseInt(data[0]);
        this.gameId = Integer.parseInt(data[1]);
        this.abilityId = Integer.parseInt(data[2]);
        this.round = Integer.parseInt(data[3]);
        this.player1 = data[4].equals("1");
        this.ability = db.getAbility(this.abilityId);
    }
    public static AbilityUsed load(String[] data, DataBase db) throws SQLException {
        AbilityUsed abilityUsed = new AbilityUsed(data, db);
        if (!instances.containsKey(abilityUsed.id) || !instances.get(abilityUsed.id).equals(abilityUsed)) {
            instances.put(abilityUsed.id, abilityUsed);
        }
        return instances.get(abilityUsed.id);
    }
    public static AbilityUsed load(int id, DataBase db) throws SQLException {
        return db.getAbilityUsed(id);
    }
    public AbilityUsed getUpdated(DataBase db) throws SQLException {
        return load(this.id, db);
    }
    public boolean equals(AbilityUsed other) {
        if (other == null) return false;
        return this.id == other.id &&
                this.gameId == other.gameId &&
                this.round == other.round &&
                this.ability.equals(other.ability);
    }
}
