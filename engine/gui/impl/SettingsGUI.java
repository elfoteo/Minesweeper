package engine.gui.impl;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import engine.UIManager;
import engine.gui.AbstractTerminalGUI;
import engine.utils.Constants;
import engine.utils.Utils;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SettingsGUI extends AbstractTerminalGUI {
    private int selectedIndex = 0;

    /**
     * Constructor for the SettingsGUI.
     *
     * @param uiManager The UIManager giving access to the terminal and screen.
     */
    public SettingsGUI(UIManager uiManager) {
        super(uiManager.getTerminal());
        this.uiManager = uiManager;
        this.screen = uiManager.getScreen();
        this.textGraphics = uiManager.getTextGraphics();
    }

    @Override
    public void show() throws IOException {
        super.show();
        screen.clear();
        boolean running = true;
        while (running) {
            draw();

            KeyStroke choice = screen.readInput();
            if (choice.getKeyType() == KeyType.EOF) {
                break;
            }

            if (choice.getKeyType() == KeyType.ArrowDown) {
                selectedIndex++;
                if (selectedIndex > Constants.settingsMenuOptions.length - 1) {
                    selectedIndex = 0;
                }
            } else if (choice.getKeyType() == KeyType.ArrowUp) {
                selectedIndex--;
                if (selectedIndex < 0) {
                    selectedIndex = Constants.settingsMenuOptions.length - 1;
                }
            } else if (choice.getKeyType() == KeyType.Escape || choice.getKeyType() == KeyType.EOF) {
                break;
            } else if (choice.getKeyType() == KeyType.Enter) {
                switch (Constants.settingsMenuOptions[selectedIndex]) {
                    case "Skins":
                        resizePaused = true;
                        uiManager.showSkinsMenu();
                        resizePaused = false;
                        break;
                    case "Themes":
                        resizePaused = true;
                        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
                        // Schedule a timer task to update the theme every 100 milliseconds
                        ScheduledFuture<?> timerTask = timer.scheduleAtFixedRate(uiManager::updateTheme, 0, 100, TimeUnit.MILLISECONDS);
                        try {
                            uiManager.showThemesMenu();
                        } catch (Exception ex) {
                            // If an exception occurs during menu display, stop the timer task and shutdown the timer
                            timerTask.cancel(true);
                            timer.shutdown();
                            // Propagate the exception up the stack
                            throw ex;
                        }
                        // If all is fine, cancel the timer
                        timerTask.cancel(true);
                        timer.shutdown();
                        resizePaused = false;
                        break;
                    case "Options":
                        // TODO: Music options
                        resizePaused = true;
                        uiManager.showOptions();
                        resizePaused = false;
                        break;
                    case "Back":
                        running = false;
                        break;
                }
            }
        }
        onClose();
    }

    @Override
    public void onClose() {
        super.onClose();
        screen.clear();
    }

    @Override
    public void draw() throws IOException {
        super.draw();

        uiManager.applyThemeColors(textGraphics);
        // Add logo
        int x = Utils.getMaxStringLength(Constants.settingsLogo);
        int y = 1;
        for (String logoLine : Constants.settingsLogo) {
            textGraphics.putString(getTerminalWidth() / 2 - x / 2, y, logoLine);
            y++;
        }

        // Hide cursor
        Utils.hideCursor(0, 0, textGraphics);
        // Clear any modifiers after the loop
        textGraphics.clearModifiers();
        screen.refresh();
        uiManager.applyThemeColors(textGraphics);

        x = Utils.getMaxStringLength(Constants.settingsMenuOptions) + 2;
        y = Constants.minesweeperLogo.length + 2;
        int counter = 0;
        for (String menuLine : Constants.settingsMenuOptions) {
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
