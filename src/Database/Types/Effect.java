package Database.Types;

import Database.DataBase;

import java.sql.SQLException;
import java.util.HashMap;

public class Effect {
    private static final HashMap<Integer, Effect> instances = new HashMap<>();

    public final int id;
    public final int time;
    public final String name;
    public final int valueEffected;
    public final float min;
    public final float max;
    public final boolean relative;
    public final boolean hitSelf;

    private Effect(String[] data) {
        this.id = Integer.parseInt(data[0]);
        this.time = Integer.parseInt(data[1]);
        this.name = data[2];
        this.valueEffected = Integer.parseInt(data[3]);
        this.min = Float.parseFloat(data[4]);
        this.max = Float.parseFloat(data[5]);
        this.relative = data[6].equals("1");
        this.hitSelf = data[7].equals("1");
    }
    public static Effect load(String[] data) {
        Effect effect = new Effect(data);
        if (!instances.containsKey(effect.id) || !instances.get(effect.id).equals(effect)) {
            instances.put(effect.id, effect);
        }
        return instances.get(effect.id);
    }
    public static Effect load(int id, DataBase db) throws SQLException {
        return db.getEffect(id);
    }
    public Effect getUpdated(DataBase db) throws SQLException {
        return load(this.id, db);
    }
    public boolean equals(Effect other) {
        if (other == null) return false;
        return (this.id == other.id) &&
                (this.time == other.time) &&
                this.name.equals(other.name) &&
                (this.valueEffected == other.valueEffected) &&
                (this.min == other.min) &&
                (this.max == other.max) &&
                (this.relative == other.relative);
    }
    public float value() {
        // generate a value between min and max
        float value = (float) (Math.random() * (max - min) + min);
        // round to 2 digits
        double multiplier = Math.pow(10, 2);
        return (float) ((float) Math.round(value * multiplier) / multiplier);
    }
}
