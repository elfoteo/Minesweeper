package engine.gui.impl;

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
import com.googlecode.lanterna.terminal.Terminal;
import engine.Leaderboard;
import engine.Minesweeper;
import engine.UIManager;
import engine.gui.AbstractTerminalGUI;
import engine.themes.IGameTheme;
import engine.utils.*;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * GUI implementation for the Minesweeper game.
 */
public class GameGUI extends AbstractTerminalGUI {
    private final Terminal terminal;
    private final Panel mainPanel;
    private final MultiWindowTextGUI gui;
    private ScheduledExecutorService timer;
    private long startTime;
    private boolean playAgain = false;
    private final MinesweeperDifficulty difficulty;
    private final String username;
    private GameInstance gameInstance;
    private Minesweeper minesweeper;
    private IGameTheme gameTheme;
    private InputHandler inputHandler;

    /**
     * Constructor for the GameGUI.
     *
     * @param uiManager The UIManager giving access to the terminal and screen.
     * @param difficulty The difficulty level of the Minesweeper game.
     * @param username The username of the player.
     */
    public GameGUI(UIManager uiManager, MinesweeperDifficulty difficulty, String username) {
        super(uiManager.getTerminal());
        this.uiManager = uiManager;
        this.screen = uiManager.getScreen();
        this.textGraphics = uiManager.getTextGraphics();
        this.terminal = uiManager.getTerminal();
        this.mainPanel = uiManager.getMainPanel();
        this.gui = uiManager.getGui();
        this.difficulty = difficulty;
        this.username = username;
    }

    @Override
    public void show() throws IOException {
        super.show();
        screen.clear();
        if (Objects.equals(username, "null")){
            uiManager.getUsername();
        }
        // Prepare game
        // Get the information for the difficulty
        Tuple<Integer, Tuple<Integer, Integer>> difficultyInfo;
        if (difficulty == MinesweeperDifficulty.CUSTOM){
            // The user has selected a custom difficulty,
            // ask if it wants to continue as this won't count towards the leaderboard
            if (showCustomDifficultyWarning()){
                playAgain = true;
                return;
            }
            // Show the dialog asking for grid size and mines in the custom difficulty
            difficultyInfo = uiManager.askCustomDifficulty();
            // The user has chosen to cancel, so we return true to display again the difficulty selection menu
            if (difficultyInfo.first() <= -1 ||
                    difficultyInfo.second().first() <= -1 ||
                    difficultyInfo.second().second() <= -1){
                playAgain = true;
                return;
            }
        }
        else{
            difficultyInfo = Utils.getDifficultyInfo(difficulty);
            if (difficultyInfo.second().first()*2+4 > terminal.getTerminalSize().getColumns() || difficultyInfo.second().second()+5 > terminal.getTerminalSize().getRows()){
                // Check if the terminal window is big enough to create a game of that size
                uiManager.waitForTerminalResize(
                        "Current size: %sx%s\nRequired size: " + (difficultyInfo.second().first()*2 + 4) + "x" + (difficultyInfo.second().second() + 5)+"\nThe terminal is too small to create a game with the specified size.\nPlease resize your terminal.",
                        new TerminalSize(difficultyInfo.second().first()*2+4, difficultyInfo.second().second()+5)
                );
            }
        }
        // Prepare game
        gameInstance = new GameInstance(screen, difficulty, difficultyInfo, username);
        minesweeper = gameInstance.getMinesweeper();
        gameTheme = uiManager.getTheme();
        inputHandler = new InputHandler(screen);
        // Start timer
        startTime = System.currentTimeMillis();
        timer = Executors.newSingleThreadScheduledExecutor();
        
        inputHandler.startThread();
        while (gameInstance.isRunning()) {
            draw();
            TerminalSize goalSize = new TerminalSize(difficultyInfo.second().first()*2 + 22, difficultyInfo.second().second() + 5);
            if (goalSize.getColumns() > getTerminalWidth() || goalSize.getRows() > getTerminalHeight()){
                // If the terminal is too small, we need to ask the user to make it bigger to continue to play
                long sysTime = pauseTimer();
                resizePaused = true;
                inputHandler.stopThread();
                uiManager.waitForTerminalResize(
                        "Current size: %sx%s\nRequired size: " + goalSize.getColumns() + "x" + goalSize.getRows()+"\nThe terminal is too small to play a game with the specified size.\nPlease resize your terminal.",
                        new TerminalSize(goalSize.getColumns(), goalSize.getRows())
                );
                inputHandler.startThread();
                resizePaused = false;
                resumeTimer(sysTime);
                // The screen got resized, but the resize listener was paused,
                // so we manually call the resize event to resize the game grid
                onResize();
            }


            KeyStroke choice = inputHandler.handleInput();
            if (choice != null){
                Rectangle bounds = gameInstance.getGameBounds();
                switch (choice.getKeyType()) {
                    // Handle arrow movement
                    case ArrowUp -> handleArrowMovement(gameInstance, bounds, -1, 0);
                    case ArrowDown -> handleArrowMovement(gameInstance, bounds, 1, 0);
                    case ArrowLeft -> handleArrowMovement(gameInstance, bounds, 0, -2);
                    case ArrowRight -> handleArrowMovement(gameInstance, bounds, 0, 2);
                    case Character -> handleKeypress(choice, minesweeper, gameInstance);
                    case Enter -> handleEnter(minesweeper, gameInstance);
                    case EOF, Escape -> handleEOFOrEscape(choice, gameInstance);
                }
                // After processing witch move to make let's check if the game ended
                gameInstance.setGameStage(minesweeper.getGameStage());

                if (gameInstance.getGameStage() == GameStage.WON){
                    inputHandler.stopThread();
                    showGameWonMessage(gameInstance, gameInstance.getUsername(), gameInstance.getDifficulty());
                    inputHandler.startThread();
                }
                // If the user wants to play again, then the game has ended
                if (gameInstance.isGameEnded()){
                    // Stop the game
                    gameInstance.setRunning(false);
                    break;
                }
            }
            Utils.waitFor(10);
        }
        onClose();
    }

    @Override
    public void onClose() {
        super.onClose();
        // Stop timer
        stopTimer();
        // Clear screen
        screen.setCursorPosition(new TerminalPosition(0, 0));
        screen.clear();
        inputHandler.stopThread();
        playAgain = gameInstance.getPlayAgain();
    }

    boolean playAgain(){
        return playAgain;
    }

    @Override
    public void onResize() {
        super.onResize();
        gameInstance.recalculateGameBounds(uiManager.terminalResizeEventHandler.getLastKnownSize());
    }

    private int getScreenWidth(){
        return uiManager.terminalResizeEventHandler.getLastKnownSize().getColumns();
    }

    private int getScreenHeight(){
        return uiManager.terminalResizeEventHandler.getLastKnownSize().getRows();
    }

    @Override
    public void draw() throws IOException {
        super.draw();
        // Draw title
        String title = "Minesweeper";
        textGraphics.putString(getScreenWidth() / 2 - title.length() / 2, 0, title);
        // Change theme colors to the theme the user selected
        uiManager.applyThemeColors(textGraphics);
        // Display sidebar messages
        Utils.displaySidebarMessage(textGraphics, 1, "Score: %s", String.valueOf(gameInstance.getScore()));
        int mines = minesweeper.getRemainingMines();
        String message = mines < 0 ? "Mines: %s (Too many cells flagged)" : "Mines: %s";
        Utils.displaySidebarMessage(textGraphics, 2, message, String.valueOf(mines));
        // Message to help the user
        if (gameInstance.getGameStage() == GameStage.MINES_NOT_FLAGGED){
            textGraphics.setForegroundColor(Constants.cellHighlightColor);
            textGraphics.putString(0, getScreenHeight()-2, "To win flag all mines");
            textGraphics.setForegroundColor(uiManager.getThemeForeground());
        }
        textGraphics.putString(0, getScreenHeight()-1, "Press 'F' to flag a mine or 'Escape' to pause");
        // Change the cursor position to let the user move it around with the 4 arrows
        screen.setCursorPosition(new TerminalPosition(gameInstance.getCursor()[0], gameInstance.getCursor()[1]));

        textGraphics.setForegroundColor(gameTheme.getMinefieldFore());
        textGraphics.setBackgroundColor(gameTheme.getMinefieldBack());
        int centerX = gameInstance.getGameBounds().x;
        int centerY = gameInstance.getGameBounds().y;
        int offsetX = 0;

        for (int col = 0; col < minesweeper.getFieldWidth(); col++) {
            for (int row = 0; row < minesweeper.getFieldHeight(); row++) {
                Cell cell = minesweeper.getCell(col, row);
                String cellContent = String.valueOf(cell.getChar());

                if (minesweeper.isFlagged(col, row)) {
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
        updateTimer();
        screen.refresh();
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

    private void showGameWonMessage(GameInstance gameInstance, String username, MinesweeperDifficulty difficulty) {
        // Stop the timer
        stopTimer();

        // Send data async to the leaderboard if difficulty is not custom
        if (difficulty != MinesweeperDifficulty.CUSTOM){
            uiManager.getLeaderboard().sendPlayerDataAsync(
                    new Leaderboard.User(username, gameInstance.getScore(), getTimerRemainingTime(), difficulty)
            );
        }
        // Show win popup
        uiManager.showGameEndPopup(
                String.format(
                        Constants.winMessage,
                        gameInstance.getScore()
                ),
                gameInstance,
                false,
                0
        );
        gameInstance.setGameEnded(true);
    }

    private void handleEnter(Minesweeper minesweeper, GameInstance gameInstance) {
        boolean wasUncovered = minesweeper.isUncovered(gameInstance.getTruePos()[0], gameInstance.getTruePos()[1]);
        Tuple<CellType, Tuple<Integer, Boolean>> minedTile = minesweeper.uncover(gameInstance.getTruePos()[0],
                gameInstance.getTruePos()[1]);

        if (minedTile.first() == null) {
            return;
        }

        if (minedTile.second().second()) {
            // Player has won
            // Add the score of the last mined tile
            gameInstance.setScore(gameInstance.getScore()+minedTile.second().first());
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
                inputHandler.stopThread();
                continueButtonPress = uiManager.showGameEndPopup(String.format(Constants.lossMessage, gameInstance.getScore()), gameInstance, true, -10*(rt+1));
                inputHandler.startThread();
            }
            else{
                inputHandler.stopThread();
                continueButtonPress = uiManager.showGameEndPopup(String.format(Constants.lossMessage, gameInstance.getScore()), gameInstance, false, 0);
                inputHandler.startThread();
            }

            if (continueButtonPress){
                // Subtract score and let the game continue
                gameInstance.setScore(gameInstance.getScore()-10);
                // Add 1 to respawn times
                gameInstance.setRespawnTimes(rt+1);
                resumeTimer(sysTime);
            }
            else {
                inputHandler.stopThread();
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
    }

    private String getTimerRemainingTime(){
        long elapsedTime = System.currentTimeMillis() - startTime;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private void stopTimer(){
        //if (timerTask != null && !timerTask.isCancelled()) {
        //    timerTask.cancel(true);
        //}
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
        //timerTask = timer.scheduleAtFixedRate(this::updateTimer, 0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Pauses the timer by canceling the existing timer task and returning the current system time.
     *
     * @return The system time when the timer is paused.
     */
    private long pauseTimer() {
        return System.currentTimeMillis();
    }

    private void handleEOFOrEscape(KeyStroke choice, GameInstance gameInstance) {
        if (choice.getKeyType() == KeyType.Escape) {
            inputHandler.stopThread();
            showPauseMenu(gameInstance);
            inputHandler.startThread();
        } else {
            gameInstance.setRunning(false);
            inputHandler.stopThread();
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
        titleContainer.addComponent(new com.googlecode.lanterna.gui2.Label(warningMessage));
        popupContainer.addComponent(titleContainer);
        String minesMessage = gameInstance.getMinesweeper().getRemainingMines() < 0 ? "Mines: %s (Too many cells flagged)" : "Mines: %s";
        popupContainer.addComponent(new com.googlecode.lanterna.gui2.Label(String.format("Score: %s\n"+minesMessage, gameInstance.getScore(), gameInstance.getMinesweeper().getRemainingMines())));

        com.googlecode.lanterna.gui2.Button resumeButton = new com.googlecode.lanterna.gui2.Button("Resume", () -> {
            resumeTimer(sysTime);
            popupWindow.close();
        });
        resumeButton.setPreferredSize(new TerminalSize(resumeButton.getLabel().length()+2, 1));
        resumeButton.setTheme(uiManager.getConfirmButtonTheme());
        buttonContainer.addComponent(resumeButton);

        com.googlecode.lanterna.gui2.Button exitButton = new com.googlecode.lanterna.gui2.Button("Exit", () -> {
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


        popupWindow.setComponent(popupContainer);
        uiManager.centerWindow(popupWindow);
        gui.addWindowAndWait(popupWindow);
    }

    private boolean warningExitMessage(GameInstance gameInstance) {
        boolean[] res = new boolean[] {false};

        String warningMessage = "Do you really want to exit?\nAll the progress will be lost";

        MenuPopupWindow popupWindow = new MenuPopupWindow(mainPanel);
        popupWindow.setTheme(uiManager.getWindowTheme());
        Panel popupContainer = new Panel();
        Panel buttonContainer = new Panel(new LinearLayout(Direction.HORIZONTAL));
        popupContainer.addComponent(new com.googlecode.lanterna.gui2.Label(warningMessage));

        com.googlecode.lanterna.gui2.Button cancelButton = new com.googlecode.lanterna.gui2.Button("No", () -> {
            res[0] = false;
            popupWindow.close();
        });
        cancelButton.setPreferredSize(new TerminalSize(4, 1));
        cancelButton.setTheme(uiManager.getConfirmButtonTheme());
        buttonContainer.addComponent(cancelButton);

        com.googlecode.lanterna.gui2.Button exitButton = new com.googlecode.lanterna.gui2.Button("Yes", () -> {
            res[0] = true;
            gameInstance.setRunning(false);
            popupWindow.close();
        });
        exitButton.setPreferredSize(new TerminalSize(5, 1));
        exitButton.setTheme(uiManager.getCancelButtonTheme());
        buttonContainer.addComponent(exitButton);

        popupContainer.addComponent(buttonContainer);


        popupWindow.setComponent(popupContainer);
        uiManager.centerWindow(popupWindow);
        gui.addWindowAndWait(popupWindow);

        return res[0];
    }

    private boolean showCustomDifficultyWarning() {
        boolean[] quit = new boolean[] {false};

        String warningMessage = "You have selected a custom difficulty.\nNote that this won't count in the leaderboard.\nDo you wish to continue?";

        MenuPopupWindow popupWindow = new MenuPopupWindow(mainPanel);
        popupWindow.setTheme(uiManager.getWindowTheme());
        Panel popupContainer = new Panel();
        Panel buttonContainer = new Panel(new LinearLayout(Direction.HORIZONTAL));
        popupContainer.addComponent(new Label(warningMessage));

        com.googlecode.lanterna.gui2.Button confirmButton = new com.googlecode.lanterna.gui2.Button("Yes", () -> {
            quit[0] = false;
            popupWindow.close();
        });
        confirmButton.setPreferredSize(new TerminalSize(5, 1));
        confirmButton.setTheme(uiManager.getConfirmButtonTheme());
        buttonContainer.addComponent(confirmButton);

        com.googlecode.lanterna.gui2.Button cancelButton = new Button("No", () -> {
            quit[0] = true;
            popupWindow.close();
        });
        cancelButton.setPreferredSize(new TerminalSize(4, 1));
        cancelButton.setTheme(uiManager.getCancelButtonTheme());
        buttonContainer.addComponent(cancelButton);

        popupContainer.addComponent(buttonContainer);


        popupWindow.setComponent(popupContainer);
        uiManager.centerWindow(popupWindow);
        gui.addWindowAndWait(popupWindow);

        return quit[0];
    }
}
