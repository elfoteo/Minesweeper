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
    private GameStage gameStage;
    private final String username;
    public GameInstance(Screen screen, MinesweeperDifficulty difficulty, Tuple<Integer, Tuple<Integer, Integer>> difficultyInfo, String username) {
        minesweeper = new Minesweeper(difficultyInfo.second().first(), difficultyInfo.second().second(), difficultyInfo.first());
        // Create a Rectangle for game bounds, centered on the screen
        gameBounds = new Rectangle(
                screen.getTerminalSize().getColumns() / 2 - (minesweeper.getFieldWidth()*2-1) / 2,
                screen.getTerminalSize().getRows() / 2 - minesweeper.getFieldHeight() / 2,
                minesweeper.getFieldWidth()*2-1,
                minesweeper.getFieldHeight()
        );
        // Set the cursor in the top left corner
        cursor = new int[] {
                gameBounds.x, // cursor x
                gameBounds.y // cursor y
        };

        truePos = new int[] {0, 0};
        score = 0;

        running = true;
        playAgain = false;
        gameEnded = false;
        respawnTimes = 0;
        gameStage = GameStage.IN_PROGRESS;
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

    public GameStage getGameStage() {
        return gameStage;
    }

    public void setGameStage(GameStage gameStage) {
        this.gameStage = gameStage;
    }
}