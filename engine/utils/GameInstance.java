package engine.utils;

import com.googlecode.lanterna.screen.Screen;

public class GameInstance{
    private int[] cursor;
    private int[] truePos;
    private int score;

    // Intellij suggestion
    private boolean running;
    private boolean playAgain;
    private boolean gameEnded;
    public GameInstance(Screen screen, String[] field) {
        cursor = new int[] {
                screen.getTerminalSize().getColumns() / 2 - Utils.getMaxStringLength(field) / 2, // cursor x
                screen.getTerminalSize().getRows() / 2 - field.length / 2 // cursor y
        };
        truePos = new int[] {0, 0};
        score = 0;

        // Intellij suggestion
        running = true;
        playAgain = false;
        gameEnded = false;
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
}