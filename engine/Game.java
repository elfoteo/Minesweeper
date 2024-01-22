package engine;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import engine.themes.IGameTheme;
import engine.utils.*;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The {@code Game} class represents a Minesweeper game instance. It has the logic
 * for playing the Minesweeper game and handles user interactions.
 *
 * @version 1.0
 * @since 2024-01-14
 */
public class Game {
    private final Screen screen;
    private final TextGraphics textGraphics;
    private final Terminal terminal;
    private final Panel mainPanel;
    private final MultiWindowTextGUI gui;
    private ScheduledExecutorService timer;
    private ScheduledFuture<?> timerTask;
    private long startTime;
    private final UIManager uiManager;

    public Game(UIManager uiManager) {
        this.uiManager = uiManager;
        this.screen = uiManager.getScreen();
        this.textGraphics = uiManager.getTextGraphics();
        this.terminal = uiManager.getTerminal();
        this.mainPanel = uiManager.getMainPanel();
        this.gui = uiManager.getGui();
    }

    /**
     * Starts a Minesweeper game with the specified username and difficulty level.
     *
     * The method initializes and starts a Minesweeper game for the provided username and difficulty level.
     * It returns a boolean indicating whether the player wants to play again after completing the session.
     * If an I/O error occurs during the gameplay or user interaction, it is thrown as an IOException.
     *
     * @param username      The username of the player.
     * @param difficulty    The difficulty level of the Minesweeper game.
     * @return              true if the player wants to play again, false otherwise.
     * @throws IOException  If an I/O error occurs during gameplay or user interaction.
     */
    public boolean start(String username, MinesweeperDifficulty difficulty) throws IOException {
        screen.clear();
        String title = "Minesweeper";
        textGraphics.putString(screen.getTerminalSize().getColumns() / 2 - title.length() / 2, 0, title);
        screen.refresh();
        // Prepare game
        GameInstance gameInstance = new GameInstance(screen, difficulty);
        Minesweeper minesweeper = gameInstance.getMinesweeper();
        IGameTheme gameTheme = uiManager.getTheme();
        // Start timer
        startTime = System.currentTimeMillis();
        timer = Executors.newSingleThreadScheduledExecutor();
        // Schedule timer update every 500 milliseconds (half second)
        timerTask = timer.scheduleAtFixedRate(this::updateTimer, 0, 500, TimeUnit.MILLISECONDS);
        String[] field;
        while (gameInstance.getRunning()) {
            // Change theme colors to the theme the user selected
            uiManager.applyThemeColors(textGraphics);
            // Display sidebar messages
            Utils.displaySidebarMessage(textGraphics, 1, "Score: %s", String.valueOf(gameInstance.getScore()));
            int mines = minesweeper.getRemainingMines();
            String message = mines < 0 ? "Mines: %s (Too many cells flagged)" : "Mines: %s";
            Utils.displaySidebarMessage(textGraphics, 2, message, String.valueOf(mines));
            // Message to help the user
            textGraphics.putString(0, terminal.getTerminalSize().getRows()-1, "Press 'F' to flag a mine");
            // Change the cursor position to let the user move it around with the 4 arrows
            screen.setCursorPosition(new TerminalPosition(gameInstance.getCursor()[0], gameInstance.getCursor()[1]));

            field = minesweeper.getFieldAsString().split("\n");
            textGraphics.setForegroundColor(gameTheme.getMinefieldFore());
            textGraphics.setBackgroundColor(gameTheme.getMinefieldBack());
            int centerX = screen.getTerminalSize().getColumns() / 2 - Utils.getMaxStringLength(field) / 2;
            int centerY = screen.getTerminalSize().getRows() / 2 - field.length / 2;
            int offsetX = 0;

            for (int col = 0; col < minesweeper.getFieldWidth(); col++) {
                for (int row = 0; row < minesweeper.getFieldHeight(); row++) {
                    Cell cell = minesweeper.getCell(col, row);
                    String cellContent = String.valueOf(cell.getChar());

                    if (minesweeper.isCellHighlighted(col, row)) {
                        // Highlight the cell if needed
                        textGraphics.setForegroundColor(Constants.cellHighlightColor);
                        cellContent = String.valueOf(minesweeper.getCell(col, row, true).getChar());
                    } else if (minesweeper.isUncovered(col, row) && cell.type == CellType.NUMBER) {
                        // Color cell numbers
                        int number = cell.getNumber();

                        // Set color based on conditions
                        if (number == minesweeper.getNumbersOfFlaggedCells(col, row) && uiManager.getOptions().isGrayOutNearbyCells()) {
                            // Give a hint only if the options allow it
                            textGraphics.setForegroundColor(gameTheme.getWarningColor(number, true));
                        } else {
                            textGraphics.setForegroundColor(gameTheme.getWarningColor(number, false));
                        }
                    }
                    else{
                        textGraphics.setForegroundColor(gameTheme.getMinefieldFore());
                        textGraphics.setBackgroundColor(gameTheme.getMinefieldBack());
                    }
                    // Can happen if the player decides to continue the game, loosing score
                    if (cell.type == CellType.MINE){
                        // If the cell is a mine, then color it red
                        textGraphics.setForegroundColor(Constants.dangerColor);
                    }

                    // Display the cell content
                    textGraphics.putString(centerX + col + offsetX, centerY + row, cellContent);

                    // Reset foreground color to default
                    textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
                }
                offsetX += 1;
            }
            uiManager.applyThemeColors(textGraphics);
            // Draw rectangle around the game
            Rectangle bounds = gameInstance.getGameBounds();
            Utils.drawRect(bounds.x-1, bounds.y-1,
                    bounds.width+2,
                    bounds.height+2, textGraphics);


            screen.refresh();

            KeyStroke choice = screen.readInput();

            switch (choice.getKeyType()) {
                // Handle arrow movement
                case ArrowUp -> handleArrowMovement(gameInstance, bounds, -1, 0);
                case ArrowDown -> handleArrowMovement(gameInstance, bounds, 1, 0);
                case ArrowLeft -> handleArrowMovement(gameInstance, bounds, 0, -2);
                case ArrowRight -> handleArrowMovement(gameInstance, bounds, 0, 2);
                case Character -> handleKeypress(choice, minesweeper, gameInstance);
                case Enter -> handleEnter(username, difficulty, minesweeper, gameInstance);
                case EOF, Escape -> handleEOFOrEscape(choice, gameInstance);
            }
            // If the user wants to play again, then the game has ended
            if (gameInstance.isGameEnded()){
                // Stop the game
                gameInstance.setRunning(false);
                break;
            }
        }
        // Stop timer
        stopTimer();
        // Clear screen
        screen.setCursorPosition(new TerminalPosition(0, 0));
        screen.clear();
        return gameInstance.getPlayAgain();
    }

    private void handleArrowMovement(GameInstance gameInstance, Rectangle bounds, int deltaY, int deltaX) {
        if (bounds.contains(gameInstance.getCursor()[0] + deltaX, gameInstance.getCursor()[1] + deltaY)) {
            gameInstance.setCursor(
                    new int[]{
                            gameInstance.getCursor()[0] + deltaX,
                            gameInstance.getCursor()[1] + deltaY
                    }
            );
            gameInstance.setTruePos(
                    new int[]{
                            gameInstance.getTruePos()[0]+Math.max(Math.min(deltaX, 1), -1),
                            gameInstance.getTruePos()[1]+Math.max(Math.min(deltaY, 1), -1)
                    }
            );
        }
    }

    private void handleKeypress(KeyStroke choice, Minesweeper minesweeper, GameInstance gameInstance) {
        if (choice.getCharacter().toString().equalsIgnoreCase("f")) {
            minesweeper.toggleHighlightCell(gameInstance.getTruePos()[0], gameInstance.getTruePos()[1]);
        }
    }

    private void handleEnter(String username, MinesweeperDifficulty difficulty, Minesweeper minesweeper, GameInstance gameInstance) {
        boolean wasUncovered = minesweeper.isUncovered(gameInstance.getTruePos()[0], gameInstance.getTruePos()[1]);
        Tuple<CellType, Tuple<Integer, Boolean>> minedTile = minesweeper.uncover(gameInstance.getTruePos()[0],
                gameInstance.getTruePos()[1]);

        if (minedTile.first() == null) {
            return;
        }

        if (minedTile.second().second()) {
            // Player has won
            // Stop the timer
            stopTimer();
            // Add the score
            gameInstance.setScore(gameInstance.getScore()+minedTile.second().first());
            // Send data async to the leaderboard
            uiManager.getLeaderboard().sendPlayerDataAsync(new Leaderboard.User(username, gameInstance.getScore(), getTimerRemainingTime(), difficulty));
            // Show win popup
            uiManager.showGameEndPopup(String.format(Constants.winMessage, gameInstance.getScore()), gameInstance, false, 0);
            gameInstance.setGameEnded(true);
        } else if (minedTile.first() == CellType.MINE && !wasUncovered) {
            // If the tile that the player has mined is a mine, and it wasn't yet mined, The player has lost
            // Stop the timer
            stopTimer();
            // Show lose popup
            long sysTime = pauseTimer();
            int rt = gameInstance.getRespawnTimes();
            boolean continueButtonPress;
            // The player can continue playing only if it hasn't already respawned more than 3 times and
            // has more then 9 score
            if (rt < 3 && gameInstance.getScore() >= 10){
                continueButtonPress = uiManager.showGameEndPopup(String.format(Constants.lossMessage, gameInstance.getScore()), gameInstance, true, -10*(rt+1));
            }
            else{
                continueButtonPress = uiManager.showGameEndPopup(String.format(Constants.lossMessage, gameInstance.getScore()), gameInstance, false, 0);
            }

            if (continueButtonPress){
                // Subtract score and let the game continue
                gameInstance.setScore(gameInstance.getScore()-10);
                // Add 1 to respawn times
                gameInstance.setRespawnTimes(rt+1);
                resumeTimer(sysTime);
            }
            else {
                stopTimer();
                gameInstance.setGameEnded(true);
            }
        } else {
            // If the tile wasn't already mined
            gameInstance.setScore(gameInstance.getScore()+minedTile.second().first());
        }
    }

    private void updateTimer() {
        // Calculate and display the elapsed time
        long elapsedTime = System.currentTimeMillis() - startTime;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        String text = Utils.getGameTimerText(seconds, minutes, elapsedTime);
        uiManager.applyThemeColors(textGraphics);
        textGraphics.putString(0, 3, text);
        // Refresh the screen to see the changes in real time
        try {
            screen.refresh();
        } catch (Exception ignored) {
        }
    }

    private String getTimerRemainingTime(){
        long elapsedTime = System.currentTimeMillis() - startTime;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private void stopTimer(){
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel(true);
        }
        timer.shutdown();
    }

    /**
     * Resumes the timer and schedules calls every 500ms.
     *
     * @param sysTime The system time when the timer was paused.
     */
    private void resumeTimer(long sysTime) {
        // Ensure the timer is not null and not shut down
        if (timer == null || timer.isShutdown()) {
            timer = Executors.newSingleThreadScheduledExecutor();
        }

        startTime = startTime + (System.currentTimeMillis() - sysTime);
        timerTask = timer.scheduleAtFixedRate(this::updateTimer, 0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Pauses the timer by canceling the existing timer task and returning the current system time.
     *
     * @return The system time when the timer is paused.
     */
    private long pauseTimer() {
        // Ensure the timer is not null and not shut down
        if (timer == null || timer.isShutdown()) {
            return System.currentTimeMillis();
        }

        if (timerTask != null) {
            timerTask.cancel(false);
        }
        return System.currentTimeMillis();
    }

    private void handleEOFOrEscape(KeyStroke choice, GameInstance gameInstance) {
        if (choice.getKeyType() == KeyType.Escape) {
            // TODO: Implement a pause menu
            showPauseMenu(gameInstance);
        } else {
            gameInstance.setRunning(false);
        }
    }

    private void showPauseMenu(GameInstance gameInstance) {
        long sysTime = pauseTimer();
        String warningMessage = "Pause";

        MenuPopupWindow popupWindow = new MenuPopupWindow(mainPanel);
        popupWindow.setTheme(uiManager.getWindowTheme());
        Panel popupContainer = new Panel();
        Panel titleContainer = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Panel buttonContainer = new Panel(new LinearLayout(Direction.HORIZONTAL));
        titleContainer.addComponent(new EmptySpace(uiManager.getThemeBackgroundColor(),
                new TerminalSize(9-warningMessage.length()/2, 1)));
        titleContainer.addComponent(new Label(warningMessage));
        popupContainer.addComponent(titleContainer);
        String minesMessage = gameInstance.getMinesweeper().getRemainingMines() < 0 ? "Mines: %s (Too many cells flagged)" : "Mines: %s";
        popupContainer.addComponent(new Label(String.format("Score: %s\n"+minesMessage, gameInstance.getScore(), gameInstance.getMinesweeper().getRemainingMines())));

        Button resumeButton = new Button("Resume", () -> {
            resumeTimer(sysTime);
            popupWindow.close();
        });
        resumeButton.setPreferredSize(new TerminalSize(resumeButton.getLabel().length()+2, 1));
        resumeButton.setTheme(uiManager.getConfirmButtonTheme());
        buttonContainer.addComponent(resumeButton);

        Button exitButton = new Button("Exit", () -> {
            if (warningExitMessage(gameInstance)){
                gameInstance.setRunning(false);
                popupWindow.close();
            }
        });
        exitButton.setPreferredSize(new TerminalSize(exitButton.getLabel().length()+2, 1));
        exitButton.setTheme(uiManager.getCancelButtonTheme());
        buttonContainer.addComponent(exitButton);
        popupContainer.addComponent(new EmptySpace(uiManager.getThemeBackgroundColor(), new TerminalSize(20, 1)));
        popupContainer.addComponent(buttonContainer);

        try {
            popupWindow.setComponent(popupContainer);
            popupWindow.setPosition(new TerminalPosition(terminal.getTerminalSize().getColumns() / 2 - 10,
                    terminal.getTerminalSize().getRows() / 2 - 4));
            gui.addWindowAndWait(popupWindow);
        } catch (IOException ignore) {
            // Handle the exception as needed
        }
    }

    private boolean warningExitMessage(GameInstance gameInstance) {
        boolean[] res = new boolean[] {false};

        long sysTime = pauseTimer();
        String warningMessage = "Do you really want to exit?\nAll the progress will be lost";

        MenuPopupWindow popupWindow = new MenuPopupWindow(mainPanel);
        popupWindow.setTheme(uiManager.getWindowTheme());
        Panel popupContainer = new Panel();
        Panel buttonContainer = new Panel(new LinearLayout(Direction.HORIZONTAL));
        popupContainer.addComponent(new Label(warningMessage));

        Button cancelButton = new Button("No", () -> {
            res[0] = false;
            resumeTimer(sysTime);
            popupWindow.close();
        });
        cancelButton.setPreferredSize(new TerminalSize(4, 1));
        cancelButton.setTheme(uiManager.getConfirmButtonTheme());
        buttonContainer.addComponent(cancelButton);

        Button exitButton = new Button("Yes", () -> {
            res[0] = true;
            gameInstance.setRunning(false);
            popupWindow.close();
        });
        exitButton.setPreferredSize(new TerminalSize(5, 1));
        exitButton.setTheme(uiManager.getCancelButtonTheme());
        buttonContainer.addComponent(exitButton);

        popupContainer.addComponent(buttonContainer);

        try {
            popupWindow.setComponent(popupContainer);
            popupWindow.setPosition(new TerminalPosition(terminal.getTerminalSize().getColumns() / 2 - (Utils.getMaxStringLength(warningMessage.split("\n"))+2) / 2,
                    terminal.getTerminalSize().getRows() / 2 - 4));
            gui.addWindowAndWait(popupWindow);
        } catch (IOException ignore) {
            // Handle the exception as needed
        }
        return res[0];
    }
}
