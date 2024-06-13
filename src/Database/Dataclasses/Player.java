package Database.Dataclasses;

public class Player {
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

    public Player(int id, String name, float maxHP, float maxMP, String password, boolean inGame, float defaultHP, float defaultMP, float regenHP, float regenMP) {
        this.id = id;
        this.name = name;
        this.maxHP = maxHP;
        this.maxMP = maxMP;
        this.password = password;
        this.inGame = inGame;
        this.defaultHP = defaultHP;
        this.defaultMP = defaultMP;
        this.regenHP = regenHP;
        this.regenMP = regenMP;
    }
    public Player(String[] dbData) {
        this.id = Integer.parseInt(dbData[0]);
        this.name = dbData[1];
        this.maxHP = Float.parseFloat(dbData[2]);
        this.maxMP = Float.parseFloat(dbData[3]);
        this.password = dbData[4];
        this.inGame = Boolean.parseBoolean(dbData[5]);
        this.defaultHP = Float.parseFloat(dbData[6]);
        this.defaultMP = Float.parseFloat(dbData[7]);
        this.regenHP = Float.parseFloat(dbData[8]);
        this.regenMP = Float.parseFloat(dbData[9]);
    }
}
