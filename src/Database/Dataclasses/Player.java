package Database.Dataclasses;

public class Player {
    public final int id;
    public final String name;
    public final float maxHP;
    public final float maxMP;
    public final String password;
    public Player(int id, String name, float maxHP, float maxMP, String password) {
        this.id = id;
        this.name = name;
        this.maxHP = maxHP;
        this.maxMP = maxMP;
        this.password = password;
    }
}
