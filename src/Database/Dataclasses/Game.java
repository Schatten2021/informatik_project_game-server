package Database.Dataclasses;

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

    public Game(int id, int round, int player1ID, float player1HP, float player1MP, int player1FinishedTurns, int player2ID, float player2HP, float player2MP, int player2FinishedTurns, boolean finished, int result) {
        this.id = id;
        this.round = round;
        this.player1ID = player1ID;
        this.player1HP = player1HP;
        this.player1MP = player1MP;
        this.player1FinishedTurns = player1FinishedTurns;
        this.player2ID = player2ID;
        this.player2HP = player2HP;
        this.player2MP = player2MP;
        this.player2FinishedTurns = player2FinishedTurns;
        this.finished = finished;
        this.result = result;
    }
    public Game(String[] data) {
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
    }
}
