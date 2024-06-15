package Database.Dataclasses;

import Database.Connection;

public class Game {
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
    public final Player player1;
    public final Player player2;

    public Game(String[] data, Player player1, Player player2) {
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
        this.finished = Boolean.parseBoolean(data[10]);
        this.result = Integer.parseInt(data[11]);
        this.player1 = player1;
        this.player2 = player2;
    }
}
