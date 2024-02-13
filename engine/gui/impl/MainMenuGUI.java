package engine.gui.impl;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import engine.UIManager;
import engine.gui.AbstractTerminalGUI;
import engine.options.Options;
import engine.skins.SkinManager;
import engine.themes.ThemeManager;
import engine.utils.Constants;
import engine.utils.MinesweeperDifficulty;
import engine.utils.Utils;

import java.io.IOException;

/**
 * Represents the main menu GUI.
 */
public class MainMenuGUI extends AbstractTerminalGUI {

    private final Terminal terminal;
    private int selectedIndex = 0;

    /**
     * Constructor for the MainMenuGUI.
     *
     * @param uiManager The UIManager giving access to the terminal and screen.
     */
    public MainMenuGUI(UIManager uiManager) {
        super(uiManager.getTerminal());
        this.uiManager = uiManager;
        this.terminal = uiManager.getTerminal();
        this.screen = uiManager.getScreen();
        this.textGraphics = uiManager.getTextGraphics();
    }

    @Override
    public void show() throws IOException {
        // Call super method to register all the necessary stuff
        super.show();
        // If the data collection isn't accepted yet
        if (uiManager.isDataCollectionRejected()) {
            // If the user denies the data collection exit
            if (!uiManager.showDataCollectionWarning()) {
                return;
            }
        }
        try {
            SkinManager.loadSelectedSkinFromFile(Constants.skinFile);
        } catch (Exception ignore) {
        }
        try {
            ThemeManager.loadSelectedThemeFromFile(Constants.themeFile);
        } catch (Exception ignore) {
        }
        boolean running = true;

        while (running) {
            draw();

            KeyStroke choice = screen.readInput();
            if (choice.getKeyType() == KeyType.EOF) {
                break;
            }

            if (choice.getKeyType() == KeyType.ArrowDown) {
                selectedIndex++;
                if (selectedIndex > Constants.mainMenuOptions.length - 1) {
                    selectedIndex = 0;
                }
            } else if (choice.getKeyType() == KeyType.ArrowUp) {
                selectedIndex--;
                if (selectedIndex < 0) {
                    selectedIndex = Constants.mainMenuOptions.length - 1;
                }
            } else if (choice.getKeyType() == KeyType.Enter) {
                switch (Constants.mainMenuOptions[selectedIndex]) {
                    case "Play":
                        String username = uiManager.getUsername();
                        if (username == null) {
                            break;
                        }
                        MinesweeperDifficulty difficulty;
                        boolean playAgain;
                        do {
                            difficulty = uiManager.getDifficulty();
                            // The difficulty will only be null if the user decides to cancel
                            if (difficulty == null) {
                                break;
                            }
                            GameGUI game = new GameGUI(uiManager, difficulty, username);
                            openGUI(game);
                            playAgain = game.playAgain();
                        }
                        while (playAgain);
                        break;
                    case "Leaderboard":
                        openGUI(new LeaderboardGUI(uiManager));
                        break;
                    case "Settings":
                        openGUI(new SettingsGUI(uiManager));
                        break;
                    case "About":
                        openGUI(new AboutGUI(uiManager));
                        break;
                    case "Exit":
                        running = false;
                        break;
                }
            }
        }
        // GUI got closed
        onClose();
    }

    @Override
    public void onClose() {
        // Call super method
        super.onClose();
        // GUI Closes, save options
        Options.saveOptionsToFile(uiManager.getOptions());
    }

    @Override
    public void draw() throws IOException {
        super.draw();
        // screen.doResizeIfNecessary() returns size if the screen has been resized, null if not
        // if the screen has been resized clear the screen
        if (screen.doResizeIfNecessary() != null) {
            screen.clear();
        }
        uiManager.applyThemeColors(textGraphics);
        // Add logo
        int x = Utils.getMaxStringLength(Constants.minesweeperLogo);
        int y = 1;
        for (String logoLine : Constants.minesweeperLogo) {
            textGraphics.putString(getTerminalWidth() / 2 - x / 2, y, logoLine);
            y++;
        }
        // Add creator text
        int xOffset = 0;

        for (String segment : Constants.creatorText.split("\\*")) {
            // Display the non-star segment without any modifiers
            uiManager.applyThemeColors(textGraphics);
            textGraphics.putString(getTerminalWidth() - Constants.creatorText.length() - 1 + xOffset,
                    terminal.getTerminalSize().getRows() - 1, segment);
            xOffset += segment.length();

            // Apply the blinking style to the next star
            textGraphics.setStyleFrom(Constants.blinkStyle);
            uiManager.applyThemeColors(textGraphics);
            // Display the star with the blinking style
            textGraphics.putString(getTerminalWidth() - Constants.creatorText.length() - 1 + xOffset,
                    terminal.getTerminalSize().getRows() - 1, "*");

            // Move the xOffset to the next position after the star
            textGraphics.clearModifiers();
            xOffset++;
            screen.refresh();
        }

        // Hide cursor
        Utils.hideCursor(0, 0, textGraphics);
        // Clear any modifiers after the loop
        textGraphics.clearModifiers();
        screen.refresh();
        uiManager.applyThemeColors(textGraphics);

        x = Utils.getMaxStringLength(Constants.mainMenuOptions) + 2;
        y = Constants.minesweeperLogo.length + 2;
        int counter = 0;
        for (String menuLine : Constants.mainMenuOptions) {
            if (selectedIndex == counter) {
                textGraphics.putString(getTerminalWidth() / 2 - x / 2, y, "o " + menuLine);
            } else {
                textGraphics.putString(getTerminalWidth() / 2 - x / 2, y, "- " + menuLine);
            }
            y++;
            counter++;
        }
        screen.refresh();
    }
}
