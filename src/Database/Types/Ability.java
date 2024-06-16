package Database.Types;

import Database.DataBase;

import java.sql.SQLException;
import java.util.HashMap;

public class Ability {
    private static final HashMap<Integer, Ability> instances = new HashMap<>();

    public final int id;
    public final String name;
    public final float cost;
    public final Effect[] effects;

    private Ability(String[] data, DataBase db) throws SQLException {
        this.id = Integer.parseInt(data[0]);
        this.name = data[1];
        this.cost = Float.parseFloat(data[2]);
        this.effects = db.getEffects(this);
    }
    public static Ability load(String[] data, DataBase db) throws SQLException {
        Ability instance = new Ability(data, db);
        if (!instances.containsKey(instance.id) || !instance.equals(instances.get(instance.id))) {
            instances.put(instance.id, instance);
        }
        return instances.get(instance.id);
    }
    public static Ability load(int id, DataBase db) throws SQLException {
        return db.getAbility(id);
    }
    public boolean equals(Ability other) {
        if (other == null) return false;
        if (other.effects.length != effects.length) {
            return false;
        }
        for (int i = 0; i < effects.length; i++) {
            if (!effects[i].equals(other.effects[i])) {
                return false;
            }
        }
        return id == other.id && name.equals(other.name);
    }
    public Ability getUpdated(DataBase db) throws SQLException {
        return load(this.id, db);
    }
}
