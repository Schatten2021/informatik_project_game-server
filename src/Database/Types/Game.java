package Database.Types;

import Abitur.List;
import Database.DataBase;

import java.sql.SQLException;
import java.util.HashMap;

public class Game {
    private static final HashMap<Integer, Game> instances = new HashMap<>();

    public final int id;
    public final int round;
    public final int player1ID;
    public final float player1HP;
    public final float player1MP;
    public final int player1FinishedTurns;
    public final int player2ID;
    public final float player2HP;
    public final float player2MP;
    public final int player2FinishedTurns;
    public final boolean finished;
    public final int result;
    public Player player1;
    public Player player2;
    public final List<AbilityUsed> abilitiesUsed = new List<>();

    private Game(String[] data, DataBase db) throws SQLException {
        this.id = Integer.parseInt(data[0]);
        this.round = Integer.parseInt(data[1]);
        this.player1ID = Integer.parseInt(data[2]);
        this.player1HP = Float.parseFloat(data[3]);
        this.player1MP = Float.parseFloat(data[4]);
        this.player1FinishedTurns = Integer.parseInt(data[5]);
        this.player2ID = Integer.parseInt(data[6]);
        this.player2HP = Float.parseFloat(data[7]);
        this.player2MP = Float.parseFloat(data[8]);
        this.player2FinishedTurns = Integer.parseInt(data[9]);
        this.finished = data[10].equals("1");
        this.result = Integer.parseInt(data[11]);

        this.player1 = Player.load(this.player1ID, db);
        this.player2 = Player.load(this.player2ID, db);
        AbilityUsed[] abilitiesUsed = db.getAbilitiesUsed(this);
        for (AbilityUsed abilityUsed : abilitiesUsed) {
            this.abilitiesUsed.append(abilityUsed);
        }

        this.player1.currentGame = this;
        this.player2.currentGame = this;
    }
    public static Game load(String[] data, DataBase db) throws SQLException {
        Game instance = new Game(data, db);
        if (!instances.containsKey(instance.id) || !instances.get(instance.id).equals(instance)) {
            instances.put(instance.id, instance);
        }
        instance = instances.get(instance.id);
        instance.player1 = instance.player1.getUpdated(db);
        instance.player2 = instance.player2.getUpdated(db);
        return instance;
    }
    public static Game load(int id, DataBase db) throws SQLException {
        return db.getGame(id);
    }
    public Game getUpdated(DataBase db) throws SQLException {
        return load(this.id, db);
    }
    public boolean equals(Game other) {
        if (other == null) return false;
        this.abilitiesUsed.toFirst();
        other.abilitiesUsed.toFirst();
        while (!this.abilitiesUsed.isEmpty()) {
            if (other.abilitiesUsed.isEmpty()) {
                return false;
            }
            if (!this.abilitiesUsed.getContent().equals(other.abilitiesUsed.getContent())) {
                return false;
            }
            this.abilitiesUsed.next();
            other.abilitiesUsed.next();
        }
        return id == other.id &&
                round == other.round &&
                player1.id == other.player1.id &&
                player1HP == other.player1HP &&
                player1MP == other.player1MP &&
                player1FinishedTurns == other.player1FinishedTurns &&
                player2.id == other.player2.id &&
                player2HP == other.player2HP &&
                player2MP == other.player2MP &&
                player2FinishedTurns == other.player2FinishedTurns &&
                finished == other.finished &&
                result == other.result;
    }
}
