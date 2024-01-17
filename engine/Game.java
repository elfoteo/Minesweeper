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
import engine.utils.*;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The {@code Game} class represents a Minesweeper game instance. It encapsulates the logic
 * for playing the Minesweeper game and handles user interactions.
 * <p>
 * The class provides methods for starting and managing Minesweeper games, handling input events,
 * updating the timer, and displaying popup windows.
 * </p>
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
     * <p>The method initializes and starts a Minesweeper game for the provided username and difficulty level.
     * It returns a boolean indicating whether the player wants to play again after completing the session.
     * If an I/O error occurs during the gameplay or user interaction, it is thrown as an IOException.</p>
     *
     * @param username   The username of the player.
     * @param difficulty The difficulty level of the Minesweeper game.
     * @return true if the player wants to play again, false otherwise.
     * @throws IOException If an I/O error occurs during gameplay or user interaction.
     * @since 2024-01-14
     */
    public boolean start(String username, MinesweeperDifficulty difficulty) throws IOException {
        screen.clear();
        String title = "Minesweeper";
        textGraphics.putString(screen.getTerminalSize().getColumns() / 2 - title.length() / 2, 0, title);
        screen.refresh();
        GameInstance gameInstance = new GameInstance(screen, difficulty);

        startTime = System.currentTimeMillis();
        timer = Executors.newSingleThreadScheduledExecutor();
        // Schedule timer update every 500 milliseconds (half second)
        timerTask = timer.scheduleAtFixedRate(this::updateTimer, 0, 500, TimeUnit.MILLISECONDS);
        String[] field;
        while (gameInstance.getRunning()) {
            // Display sidebar messages
            Utils.displaySidebarMessage(textGraphics, 1, "Score: %s", String.valueOf(gameInstance.getScore()));
            int mines = gameInstance.getMinesweeper().getRemainingMines();
            String message = mines < 0 ? "Mines: %s (Too many cells flagged)" : "Mines: %s";
            Utils.displaySidebarMessage(textGraphics, 2, message, String.valueOf(mines));
            // Message to help the user
            textGraphics.putString(0, terminal.getTerminalSize().getRows()-1, "Press 'F' to flag a mine");
            // Change the cursor position to let the user move it around with the 4 arrows
            screen.setCursorPosition(new TerminalPosition(gameInstance.getCursor()[0], gameInstance.getCursor()[1]));

            field = gameInstance.getMinesweeper().getFieldAsString().split("\n");

            int centerX = screen.getTerminalSize().getColumns() / 2 - Utils.getMaxStringLength(field) / 2;
            int centerY = screen.getTerminalSize().getRows() / 2 - field.length / 2;
            int offsetX;

            for (int row = 0; row < gameInstance.getMinesweeper().getFieldWidth(); row++) {
                offsetX = 0;

                for (int col = 0; col < gameInstance.getMinesweeper().getFieldHeight(); col++) {
                    // TODO: Finish skin implementation
                    String cellContent = String.valueOf(gameInstance.getMinesweeper().getCell(row, col));

                    // Highlight the cell if needed
                    if (gameInstance.getMinesweeper().isCellHighlighted(col, row)) {
                        textGraphics.setForegroundColor(new TextColor.RGB(235, 128, 52));
                    }

                    // Display the cell content
                    textGraphics.putString(centerX + col + offsetX, centerY + row, cellContent);

                    // Reset foreground color to default
                    textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);

                    // Add extra space at the end
                    offsetX += 1;
                }
            }

            screen.refresh();

            KeyStroke choice = screen.readInput();

            switch (choice.getKeyType()) {
                // Handle arrow movement
                case ArrowUp -> handleArrowMovement(gameInstance, gameInstance.getGameBounds(), -1, 0);
                case ArrowDown -> handleArrowMovement(gameInstance, gameInstance.getGameBounds(), 1, 0);
                case ArrowLeft -> handleArrowMovement(gameInstance, gameInstance.getGameBounds(), 0, -2);
                case ArrowRight -> handleArrowMovement(gameInstance, gameInstance.getGameBounds(), 0, 2);
                case Character -> handleKeypress(choice, gameInstance.getMinesweeper(), gameInstance);
                case Enter -> handleEnter(username, difficulty, gameInstance.getMinesweeper(), gameInstance);
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
        if (choice.getCharacter() == 'f') {
            minesweeper.toggleHighlightCell(gameInstance.getTruePos()[0], gameInstance.getTruePos()[1]);
        }
    }

    private void handleEnter(String username, MinesweeperDifficulty difficulty, Minesweeper minesweeper, GameInstance gameInstance) {
        Tuple<Character, Tuple<Integer, Boolean>> minedTile = minesweeper.uncover(gameInstance.getTruePos()[0],
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
            uiManager.showPlayAgainPopup(String.format("Congratulations! You've successfully cleared the minefield!\nScore: %d\nPress \"Play Again\" to start again or \"Exit\" to exit", gameInstance.getScore()), gameInstance);
            gameInstance.setGameEnded(true);
        } else if (minedTile.first() == '*') {
            // Player has lost
            // Stop the timer
            stopTimer();
            // Show lose popup
            uiManager.showPlayAgainPopup(String.format("Oh no! You've uncovered a mine!\nScore: %d\nPress \"Play Again\" to start again or \"Exit\" to exit", gameInstance.getScore()), gameInstance);
            gameInstance.setGameEnded(true);
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

    private void handleEOFOrEscape(KeyStroke choice, GameInstance gameInstance) {
        if (choice.getKeyType() == KeyType.Escape) {
            long sysTime = System.currentTimeMillis();
            timerTask.cancel(false);
            String warningMessage = "Do you really want to exit?";

            MenuPopupWindow popupWindow = new MenuPopupWindow(mainPanel);
            popupWindow.setTheme(Constants.windowDefaultTheme);
            Panel popupContainer = new Panel();
            Panel buttonContainer = new Panel(new LinearLayout(Direction.HORIZONTAL));
            popupContainer.addComponent(new Label(warningMessage));
            Button cancelButton = new Button("No", () -> {
                startTime = startTime+(System.currentTimeMillis()-sysTime);
                timerTask = timer.scheduleAtFixedRate(this::updateTimer, 0, 500, TimeUnit.MILLISECONDS);
                popupWindow.close();
            });
            cancelButton.setPreferredSize(new TerminalSize(4, 1));
            cancelButton.setTheme(Constants.confirmButtonTheme);
            buttonContainer.addComponent(cancelButton);
            Button exitButton = new Button("Yes", () -> {
                gameInstance.setRunning(false);
                popupWindow.close();
            });
            exitButton.setPreferredSize(new TerminalSize(5, 1));
            exitButton.setTheme(Constants.cancelButtonTheme);
            buttonContainer.addComponent(exitButton);
            popupContainer.addComponent(buttonContainer);
            try {
                popupWindow.setComponent(popupContainer);
                popupWindow.setPosition(new TerminalPosition(terminal.getTerminalSize().getColumns() / 2 - warningMessage.length() / 2,
                        terminal.getTerminalSize().getRows() / 2 - 4));
                gui.addWindowAndWait(popupWindow);
            } catch (IOException ignore) {

            }
        }
        else{
            gameInstance.setRunning(false);
        }
    }
}
