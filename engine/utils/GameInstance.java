package engine.utils;

import com.googlecode.lanterna.screen.Screen;
import engine.Minesweeper;

import java.awt.*;

public class GameInstance{
    private int respawnTimes;
    private int[] cursor;
    private int[] truePos;
    private int score;
    private boolean running;
    private boolean playAgain;
    private boolean gameEnded;
    private Minesweeper minesweeper;
    private final Rectangle gameBounds;
    private final MinesweeperDifficulty difficulty;
    private final String username;
    public GameInstance(Screen screen, MinesweeperDifficulty difficulty, String username) {
        // Get the information for the difficulty
        Tuple<Integer, Tuple<Integer, Integer>> difficultyInfo = Utils.getDifficultyInfo(difficulty);
        minesweeper = new Minesweeper(difficultyInfo.second().first(), difficultyInfo.second().second(), difficultyInfo.first());
        String[] field = minesweeper.getFieldAsString().split("\n");
        cursor = new int[] {
                screen.getTerminalSize().getColumns() / 2 - Utils.getMaxStringLength(field) / 2, // cursor x
                screen.getTerminalSize().getRows() / 2 - field.length / 2 // cursor y
        };
        gameBounds = new Rectangle(
                screen.getTerminalSize().getColumns() / 2 - Utils.getMaxStringLength(field) / 2,
                screen.getTerminalSize().getRows() / 2 - field.length / 2, field[0].length(), field.length
        );
        truePos = new int[] {0, 0};
        score = 0;

        running = true;
        playAgain = false;
        gameEnded = false;
        respawnTimes = 0;
        this.difficulty = difficulty;
        this.username =  username;
    }

    // Getters
    public int[] getCursor() {
        return cursor;
    }

    public int[] getTruePos() {
        return truePos;
    }

    public int getScore() {
        return score;
    }

    public boolean getRunning() {
        return running;
    }

    public boolean getPlayAgain() {
        return playAgain;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    // Setters
    public void setCursor(int[] cursor) {
        this.cursor = cursor;
    }

    public void setTruePos(int[] truePos) {
        this.truePos = truePos;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setPlayAgain(boolean playAgain) {
        this.playAgain = playAgain;
    }

    public void setGameEnded(boolean gameEnded) {
        this.gameEnded = gameEnded;
    }

    public Minesweeper getMinesweeper() {
        return minesweeper;
    }

    public void setMinesweeper(Minesweeper minesweeper) {
        this.minesweeper = minesweeper;
    }

    public Rectangle getGameBounds() {
        return gameBounds;
    }

    public int getRespawnTimes() {
        return respawnTimes;
    }

    public void setRespawnTimes(int respawnTimes) {
        this.respawnTimes = respawnTimes;
    }

    public MinesweeperDifficulty getDifficulty() {
        return difficulty;
    }

    public String getUsername() {
        return username;
    }
}