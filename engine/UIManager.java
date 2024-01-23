package engine;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import engine.options.Options;
import engine.options.OptionsInstance;
import engine.skins.ISkin;
import engine.skins.SkinManager;
import engine.skins.impl.DefaultSkin;
import engine.themes.IGameTheme;
import engine.themes.ThemeManager;
import engine.themes.impl.DefaultGameTheme;
import engine.utils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UIManager {
    private final String[] menuOptions = new String[] {"Play", "Leaderboard", "Settings", "About", "Exit"};
    public static ISkin selectedSkin = new DefaultSkin();
    public static IGameTheme selectedTheme = new DefaultGameTheme();
    private int selectedIndex = 0;
    private final Terminal terminal;
    private final Screen screen;
    private final MultiWindowTextGUI gui;
    private final Panel mainPanel;
    private final TextGraphics textGraphics;
    public Leaderboard leaderboard;
    private final Game game;
    private final OptionsInstance options;
    private MenuPopupWindow themesMenuWindow;
    private RadioBoxList<String> themesMenuRadioboxList;
    private final List<Window.Hint> hints = new ArrayList<>();
    public UIManager(Terminal terminal) throws IOException {
        this.terminal = terminal;

        screen = new TerminalScreen(terminal);
        textGraphics = screen.newTextGraphics();
        leaderboard = new Leaderboard(screen, textGraphics);
        Panel guiBackground = new Panel();
        guiBackground.setTheme(getWindowTheme());
        gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), guiBackground);

        BasicWindow mainWindow = new BasicWindow();
        mainPanel = new Panel();
        mainWindow.setComponent(mainPanel);
        hints.add(Window.Hint.CENTERED);

        mainWindow.setHints(hints);

        gui.addWindow(mainWindow);

        screen.startScreen();

        screen.clear();
        // To not show the panel
        mainWindow.setVisible(false);
        terminal.enterPrivateMode();
        game = new Game(this);
        OptionsInstance oi = Options.readOptionsFromFile();
        // Intellij suggestion
        options = Objects.requireNonNullElseGet(oi, () ->
                new OptionsInstance("null", true)
        );
    }
    public Terminal getTerminal() {
        return terminal;
    }

    public Screen getScreen() {
        return screen;
    }

    public MultiWindowTextGUI getGui() {
        return gui;
    }

    public Panel getMainPanel() {
        return mainPanel;
    }

    public TextGraphics getTextGraphics() {
        return textGraphics;
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    public OptionsInstance getOptions(){
        return options;
    }

    public SimpleTheme getWindowTheme(){
        return new SimpleTheme(selectedTheme.getForegroundColor(), selectedTheme.getBackgroundColor());
    }

    public void centerWindow(Window window){
        window.setHints(hints);
    }

    public SimpleTheme getConfirmButtonTheme(){
        return new SimpleTheme(TextColor.ANSI.GREEN, selectedTheme.getBackgroundColor(), SGR.BOLD);
    }

    public SimpleTheme getWarningButtonTheme(){
        return new SimpleTheme(new TextColor.RGB(235, 144, 52), selectedTheme.getBackgroundColor(), SGR.BOLD);
    }

    public SimpleTheme getCancelButtonTheme(){
        return new SimpleTheme(TextColor.ANSI.RED, selectedTheme.getBackgroundColor(), SGR.BOLD);
    }

    public void applyThemeColors(TextGraphics textGraphics){
        textGraphics.setForegroundColor(selectedTheme.getForegroundColor());
        textGraphics.setBackgroundColor(selectedTheme.getBackgroundColor());
    }

    public TextColor getThemeBackgroundColor() {
        return selectedTheme.getBackgroundColor();
    }

    public IGameTheme getTheme(){
        return selectedTheme;
    }

    public boolean showDataCollectionWarning() throws IOException {
        if (isDataCollectionRejected()) {
            final boolean[] accepted = {false};
            MenuPopupWindow window = new MenuPopupWindow(mainPanel);
            window.setTheme(getWindowTheme());
            Panel container = new Panel();
            Panel buttonContainer = new Panel(new LinearLayout(Direction.HORIZONTAL));
            container.addComponent(new Label("Data Collection Warning:"));
            container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space

            Label warningLabel = new Label(Constants.dataCollectionWarning);
            container.addComponent(warningLabel);
            container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space

            Button acceptButton = new Button("Accept", () -> {
                accepted[0] = true;
                window.close();
            });
            acceptButton.setTheme(getConfirmButtonTheme());

            Button cancelButton = new Button("Exit", () -> {
                accepted[0] = false;
                window.close();
            });
            cancelButton.setTheme(getCancelButtonTheme());

            buttonContainer.addComponent(acceptButton);
            buttonContainer.addComponent(cancelButton);
            container.addComponent(buttonContainer);

            window.setComponent(container);
            window.setPosition(
                    new TerminalPosition(terminal.getTerminalSize().getColumns() / 2 - (Utils.getMaxStringLength(Constants.dataCollectionWarning.split("\n"))+2)/2,
                            terminal.getTerminalSize().getRows() / 2 - 6
                    )
            );

            gui.addWindowAndWait(window);

            // Set the flag according to the user decision
            setUserAcceptedDataCollection(accepted[0]);
            return accepted[0];
        }
        return true;
    }

    private boolean isDataCollectionRejected() {
        try {
            Path filePath = Path.of(Constants.dataCollectionAcceptedFile);
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath)){
                Files.createFile(filePath);
            }
            List<String> lines = Files.readAllLines(filePath);
            if (!lines.isEmpty() && "accepted=true".equals(lines.get(0))) {
                return false;
            }
        } catch (IOException e) {
            Utils.Debug(e.toString());
        }
        return true;
    }

    private void setUserAcceptedDataCollection(boolean accepted) {
        try {
            // delete existing data and crete the file if it doesn't exist
            Files.write(Path.of(Constants.dataCollectionAcceptedFile),
                    ("accepted=" + accepted).getBytes(),
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException ignore) {
            // ignore
        }
    }

    public void showMainScreen() throws IOException {
        // If the data collection isn't accepted yet
        if (isDataCollectionRejected()){
            // If the user denies the data collection exit
            if (!showDataCollectionWarning()){
                return;
            }
        }
        try{
            SkinManager.loadSelectedSkinFromFile(Constants.skinFile);
        } catch (Exception ignore){}
        try{
            ThemeManager.loadSelectedThemeFromFile(Constants.themeFile);
        } catch (Exception ignore){}
        boolean running = true;
        while (running){
            applyThemeColors(textGraphics);
            // Add logo
            int x = Utils.getMaxStringLength(Constants.minesweeperLogo);
            int y = 1;
            for (String logoLine : Constants.minesweeperLogo){
                textGraphics.putString(screen.getTerminalSize().getColumns()/2-x/2, y, logoLine);
                y++;
            }
            // Add creator text
            int xOffset = 0;

            for (String segment : Constants.creatorText.split("\\*")) {
                // Display the non-star segment without any modifiers
                applyThemeColors(textGraphics);
                textGraphics.putString(screen.getTerminalSize().getColumns() - Constants.creatorText.length() - 1 + xOffset,
                        terminal.getTerminalSize().getRows() - 1, segment);
                xOffset += segment.length();

                // Apply the blinking style to the next star
                textGraphics.setStyleFrom(Constants.blinkStyle);
                applyThemeColors(textGraphics);
                // Display the star with the blinking style
                textGraphics.putString(screen.getTerminalSize().getColumns() - Constants.creatorText.length() - 1 + xOffset,
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
            applyThemeColors(textGraphics);

            x = Utils.getMaxStringLength(menuOptions)+2;
            y = Constants.minesweeperLogo.length+2;
            int counter = 0;
            for (String menuLine : menuOptions){
                if (selectedIndex == counter){
                    textGraphics.putString(screen.getTerminalSize().getColumns()/2-x/2, y, "o "+menuLine);
                }
                else{
                    textGraphics.putString(screen.getTerminalSize().getColumns()/2-x/2, y, "- "+menuLine);
                }
                y++;
                counter++;
            }
            screen.refresh();

            KeyStroke choice = screen.readInput();
            if (choice.getKeyType() == KeyType.EOF){
                break;
            }

            if (choice.getKeyType() == KeyType.ArrowDown){
                selectedIndex++;
                if (selectedIndex > menuOptions.length-1){
                    selectedIndex = 0;
                }
            }
            else if (choice.getKeyType() == KeyType.ArrowUp){
                selectedIndex--;
                if (selectedIndex < 0){
                    selectedIndex = menuOptions.length-1;
                }
            } else if (choice.getKeyType() == KeyType.Enter) {
                switch (menuOptions[selectedIndex]){
                    case "Play":
                        String username = getUsername();
                        if (username == null){
                            break;
                        }
                        MinesweeperDifficulty difficulty;
                        boolean playAgain;
                        do {
                            difficulty = getDifficulty();
                            // The difficulty will only be null if the user decides to cancel
                            if (difficulty == null){
                                break;
                            }
                            playAgain = game.start(username, difficulty);
                        }
                        while (playAgain);
                        break;
                    case "Leaderboard":
                        leaderboard.displayLeaderboard();
                        break;
                    case "Settings":
                        showSettings();
                        break;
                    case "About":
                        showAboutMenu();
                        break;
                    case "Exit":
                        running = false;
                        break;
                }
            }
        }
        // App ends, save options
        Options.saveOptionsToFile(options);
    }

    private void showSettings() throws IOException {
        int localIndex = 0;
        String[] localOptions = new String[] {"Skins", "Themes", "Options", "Back"};
        screen.clear();
        boolean running = true;
        while (running) {
            applyThemeColors(textGraphics);
            // Add logo
            int x = Utils.getMaxStringLength(Constants.settingsLogo);
            int y = 1;
            for (String logoLine : Constants.settingsLogo) {
                textGraphics.putString(screen.getTerminalSize().getColumns() / 2 - x / 2, y, logoLine);
                y++;
            }

            // Hide cursor
            Utils.hideCursor(0, 0, textGraphics);
            // Clear any modifiers after the loop
            textGraphics.clearModifiers();
            screen.refresh();
            applyThemeColors(textGraphics);

            x = Utils.getMaxStringLength(localOptions) + 2;
            y = Constants.minesweeperLogo.length + 2;
            int counter = 0;
            for (String menuLine : localOptions) {
                if (localIndex == counter) {
                    textGraphics.putString(screen.getTerminalSize().getColumns() / 2 - x / 2, y, "o " + menuLine);
                } else {
                    textGraphics.putString(screen.getTerminalSize().getColumns() / 2 - x / 2, y, "- " + menuLine);
                }
                y++;
                counter++;
            }
            screen.refresh();

            KeyStroke choice = screen.readInput();
            if (choice.getKeyType() == KeyType.EOF) {
                break;
            }

            if (choice.getKeyType() == KeyType.ArrowDown) {
                localIndex++;
                if (localIndex > localOptions.length - 1) {
                    localIndex = 0;
                }
            } else if (choice.getKeyType() == KeyType.ArrowUp) {
                localIndex--;
                if (localIndex < 0) {
                    localIndex = localOptions.length - 1;
                }
            } else if (choice.getKeyType() == KeyType.Escape || choice.getKeyType() == KeyType.EOF) {
                return;
            } else if (choice.getKeyType() == KeyType.Enter) {
                switch (localOptions[localIndex]) {
                    case "Skins":
                        showSkinsMenu();
                        break;
                    case "Themes":
                        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
                        // Schedule a timer task to update the theme every 100 milliseconds
                        ScheduledFuture<?> timerTask = timer.scheduleAtFixedRate(this::updateTheme, 0, 100, TimeUnit.MILLISECONDS);
                        try {
                            showThemesMenu();
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
                        break;
                    case "Options":
                        // TODO: Music options
                        showOptions();
                        break;
                    case "Back":
                        running = false;
                        break;
                }
            }
        }
        screen.clear();
    }

    private void updateTheme() {
        try{
            List<IGameTheme> themes = ThemeManager.getThemes();
            // Necessary
            Thread.sleep(20);
            for (IGameTheme theme : themes){
                if (theme.getThemeName().equals(themesMenuRadioboxList.getCheckedItem())){
                    selectedTheme = theme;
                }
            }
            themesMenuWindow.setTheme(getWindowTheme());
        }
        catch (Exception ignored){
            // This is a secondary update theme thread, any exceptions can be safely ignored
        }
    }

    private void showThemesMenu() {
        screen.clear();
        themesMenuWindow = new MenuPopupWindow(mainPanel);
        themesMenuWindow.setTheme(getWindowTheme());
        Panel container = new Panel();
        container.addComponent(new Label("Themes"));

        // Username settings
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        // Themes list
        List<IGameTheme> themes = ThemeManager.getThemes();
        themesMenuRadioboxList = getRadiobuttonThemeList(themes);

        container.addComponent(themesMenuRadioboxList);

        // Exit button
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        Button exitButton = new Button("Exit", themesMenuWindow::close);
        exitButton.setTheme(getCancelButtonTheme());
        container.addComponent(exitButton);


        themesMenuWindow.setComponent(container);
        centerWindow(themesMenuWindow);

        gui.addWindowAndWait(themesMenuWindow);
        // The user exited the menu
        for (IGameTheme theme : themes){
            if (theme.getThemeName().equals(themesMenuRadioboxList.getCheckedItem())){
                selectedTheme = theme;
                try{
                    ThemeManager.saveSelectedThemeToFile(Constants.themeFile);
                }
                catch (Exception ignore){
                    // Probably file errors
                }
            }
        }
        screen.clear();
    }

    private void showSkinsMenu() {
        screen.clear();
        MenuPopupWindow window = new MenuPopupWindow(mainPanel);
        window.setTheme(getWindowTheme());
        Panel container = new Panel();
        container.addComponent(new Label("Skins"));

        // Username settings
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        // Skin list
        List<ISkin> skins = SkinManager.getSkins();
        RadioBoxList<String> radioBoxList = getRadiobuttonSkinList(skins);

        container.addComponent(radioBoxList);

        // Exit button
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        Button exitButton = new Button("Exit", window::close);
        exitButton.setTheme(getCancelButtonTheme());
        container.addComponent(exitButton);


        window.setComponent(container);
        centerWindow(window);

        gui.addWindowAndWait(window);
        // The user exited the menu
        for (ISkin skin : skins){
            if (skin.getSkinName().equals(radioBoxList.getCheckedItem())){
                selectedSkin = skin;
                try{
                    SkinManager.saveSelectedSkinToFile(Constants.skinFile);
                }
                catch (Exception ignore){
                    // Probably file errors
                }
            }
        }
        screen.clear();
    }

    private static RadioBoxList<String> getRadiobuttonSkinList(List<ISkin> skins) {
        TerminalSize size = new TerminalSize(20, 10);
        RadioBoxList<String> radioBoxList = new RadioBoxList<>(size);
        // For each item in the list, cast it to a Skin that extends the skin method
        for (ISkin skin : skins){
            radioBoxList.addItem(skin.getSkinName());
            // Set the selected radiobutton to the selected skin
            if (skin.getClass() == selectedSkin.getClass()){
                radioBoxList.setCheckedItem(skin.getSkinName());
            }
        }
        return radioBoxList;
    }

    private static RadioBoxList<String> getRadiobuttonThemeList(List<IGameTheme> themes) {
        TerminalSize size = new TerminalSize(20, 10);
        RadioBoxList<String> radioBoxList = new RadioBoxList<>(size);
        // For each item in the list, cast it to a Skin that extends the skin method
        for (IGameTheme theme : themes){
            radioBoxList.addItem(theme.getThemeName());
            // Set the selected radiobutton to the selected skin
            if (theme.getClass() == selectedTheme.getClass()){
                radioBoxList.setCheckedItem(theme.getThemeName());
            }
        }
        return radioBoxList;
    }

    /**
     * Displays a popup window with a message and options for the player to play again or exit.
     * Optionally includes a "Continue" button with a penalty to the score.
     *
     * @param message       The message to be displayed in the popup window.
     * @param gameInstance  The GameInstance object representing the current game state.
     * @param showContinueButton  If true, includes a "Continue" button; otherwise, only "Play Again" and "Exit" buttons are shown.
     * @return              True if the "Continue" button was pressed, false otherwise.
     */
    public boolean showGameEndPopup(String message, GameInstance gameInstance, boolean showContinueButton, int subtractScore) {
        boolean[] continuePressed = new boolean[] {false};
        MenuPopupWindow window = new MenuPopupWindow(mainPanel);
        window.setTheme(getWindowTheme());
        Panel panel = new Panel();
        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Button playAgainButton = new Button("Play Again", () -> {
            gameInstance.setPlayAgain(true);
            window.close();
        });
        playAgainButton.setTheme(getConfirmButtonTheme());
        buttonsPanel.addComponent(playAgainButton);

        if (showContinueButton){
            Button continueButton = new Button(String.format("Continue [%s score]", subtractScore), () -> {
                gameInstance.setPlayAgain(true);
                continuePressed[0] = true;
                window.close();
            });
            continueButton.setTheme(getWarningButtonTheme());
            buttonsPanel.addComponent(continueButton);
        }

        Button exitButton = new Button("Exit", window::close);
        exitButton.setTheme(getCancelButtonTheme());
        exitButton.setPreferredSize(new TerminalSize(exitButton.getLabel().length()+2, 1));
        exitButton.setPosition(new TerminalPosition(0, 5));
        buttonsPanel.addComponent(exitButton);

        Label textBox = new Label(message);

        panel.addComponent(textBox);
        panel.addComponent(buttonsPanel);

        window.setComponent(panel);
        centerWindow(window);
        gui.addWindowAndWait(window);
        return continuePressed[0];
    }

    public void showAboutMenu() throws IOException {
        boolean[] running = new boolean[]{true};
        boolean[] rgbEnabled = new boolean[]{true};
        screen.clear();
        Utils.hideCursor(0, 0, textGraphics);

        long startTime = System.currentTimeMillis();
        new Thread(() -> {
            while (running[0]) {
                KeyStroke choice = null;
                try {
                    choice = screen.readInput();
                } catch (IOException ignored) {
                }
                if (choice != null) {
                    if (choice.getKeyType() == KeyType.EOF || choice.getKeyType() == KeyType.Escape) {
                        running[0] = false;
                    } else if (choice.getKeyType() == KeyType.Tab) {
                        rgbEnabled[0] = !rgbEnabled[0];
                    }
                }
            }

        }).start();

        while (running[0]) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            int offsetY = 0;
            int offsetX = 0;
            if (rgbEnabled[0]){
                textGraphics.enableModifiers(SGR.BOLD);
                for (int i = 0; i < Constants.aboutText.length(); i++) {
                    int[] rgb = Utils.getRainbow(elapsedTime, i);

                    textGraphics.setForegroundColor(new TextColor.RGB(rgb[0], rgb[1], rgb[2]));

                    textGraphics.putString(offsetX, offsetY, String.valueOf(Constants.aboutText.charAt(i)));
                    offsetX++;
                    if (Constants.aboutText.charAt(i) == '\n'){
                        offsetY++;
                        offsetX = 0;
                    }
                }
            }
            else {
                for (String line : Constants.aboutText.split("\n")){
                    textGraphics.putString(0, offsetY, line);
                    offsetY++;
                }
            }
            textGraphics.disableModifiers(SGR.BOLD);
            textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
            textGraphics.putString(0, screen.getTerminalSize().getRows()-1, "Press \"Tab\" to toggle rgb");
            screen.refresh();

            // RGB needs a higher frame-rate, a static text doesn't need lots of updates
            if (rgbEnabled[0]){
                waitFor(30);
            }
            else {
                waitFor(200);
            }

        }
        screen.clear();
    }

    private void waitFor(int millis){
        try {
            Thread.sleep(millis);
        }catch (InterruptedException ignored) {

        }
    }


    private void showOptions() {
        screen.clear();
        MenuPopupWindow window = new MenuPopupWindow(mainPanel);
        window.setTheme(getWindowTheme());
        Panel container = new Panel();
        container.addComponent(new Label("Options"));

        // Username settings
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        Label usernameLabel = new Label("Change username:");
        Label currentUsernameLabel = new Label(String.format("\"%s\"", getUsername()));
        // usernameSpace is used to keep the space between currentUsernameLabel and changeUsernameButton the same
        EmptySpace usernameSpace = new EmptySpace(new TerminalSize(15-currentUsernameLabel.getText().length(), 1));
        Button chageUsernameButton = new Button("Change", () -> {
            // Force to show the username popup
            if (getUsername(true) != null){
                currentUsernameLabel.setText(String.format("\"%s\"", getUsername()));
                // On username update, update the optionsInstance too
                options.setUsername(getUsername());
                usernameSpace.setPreferredSize(new TerminalSize(15-currentUsernameLabel.getText().length(), 1));
            }
        });

        container.addComponent(usernameLabel);
        Panel usernameOptions = new Panel(new LinearLayout(Direction.HORIZONTAL));
        usernameOptions.addComponent(currentUsernameLabel);
        usernameOptions.addComponent(usernameSpace); // Add space
        usernameOptions.addComponent(chageUsernameButton);
        container.addComponent(usernameOptions);
        // Gray out nearby cells
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        CheckBoxList<String> grayNearbyCells = new CheckBoxList<>();
        grayNearbyCells.addItem("Gray out nearby cells", options.isGrayOutNearbyCells());
        container.addComponent(grayNearbyCells);
        // Exit button
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        Button exitButton = new Button("Exit", window::close);
        exitButton.setTheme(getCancelButtonTheme());
        container.addComponent(exitButton);


        window.setComponent(container);
        centerWindow(window);
        gui.addWindowAndWait(window);
        options.setGrayOutNearbyCells(grayNearbyCells.isChecked(0));
        screen.clear();
    }

    /**
     * Displays a menu for the user to select the game difficulty.
     *
     * @return The selected Minesweeper difficulty. Returns {@code null} if the user cancels the action.
     */
    private MinesweeperDifficulty getDifficulty() {
        final MinesweeperDifficulty[] selectedDifficulty = {MinesweeperDifficulty.MEDIUM};
        MenuPopupWindow window = new MenuPopupWindow(mainPanel);
        Panel container = new Panel();
        window.setTheme(getWindowTheme());
        container.addComponent(new Label("Select the game difficulty:"));
        for (MinesweeperDifficulty difficulty : MinesweeperDifficulty.values()){
            String name = difficulty.name();
            String camelCase = Utils.toCamelCase(name);
            Button button = new Button(camelCase, () -> {
                selectedDifficulty[0] = difficulty;
                window.close();
            });
            switch (difficulty){
                // Set different colors depending on the difficulty
                case EASY -> button.setTheme(new SimpleTheme(TextColor.ANSI.GREEN, TextColor.ANSI.DEFAULT, SGR.BOLD));
                case MEDIUM -> button.setTheme(new SimpleTheme(new TextColor.RGB(255, 115, 0), TextColor.ANSI.DEFAULT, SGR.BOLD));
                case HARD -> button.setTheme(new SimpleTheme(new TextColor.RGB(180, 0, 0), TextColor.ANSI.DEFAULT, SGR.BOLD));
            }
            button.setPreferredSize(new TerminalSize(27, 1));
            container.addComponent(button);
        }

        Button cancelButton = new Button("Cancel", () -> {
            selectedDifficulty[0] = null;
            window.close();
        });
        cancelButton.setTheme(getCancelButtonTheme());

        container.addComponent(cancelButton);

        window.setComponent(container);
        centerWindow(window);
        gui.addWindowAndWait(window);
        return selectedDifficulty[0];
    }

    public void displayPopupWindow(Window parentWindow, String message) {
        MenuPopupWindow popupWindow = new MenuPopupWindow(parentWindow.getComponent());
        popupWindow.setTheme(getWindowTheme());
        Panel popupContainer = new Panel();
        popupContainer.addComponent(new Label(message));
        Button okButton = new Button("Ok", popupWindow::close);
        okButton.setPreferredSize(new TerminalSize(4, 1));
        okButton.setTheme(getConfirmButtonTheme());
        popupContainer.addComponent(okButton);
        popupWindow.setComponent(popupContainer);
        centerWindow(popupWindow);
    }

    private String getUsername() {
        return getUsername(false);
    }
    /**
     * Displays a menu for the user to input their username.
     *
     * <p>The method displays a UI to let the user enter their username.
     * It returns the entered username as a String. If the user cancels the action or an I/O error occurs during the interaction,
     * the method returns {@code null}.</p>
     *
     * @param force Forces to ask the username
     * @return The entered username. Returns {@code null} if the user cancels the action or an I/O error occurs.
     */
    private String getUsername(boolean force) {
        final String[] username = {""};
        if (!Objects.equals(options.getUsername(), "null") && !force){
            return options.getUsername();
        }

        MenuPopupWindow window = new MenuPopupWindow(mainPanel);
        window.setTheme(getWindowTheme());
        Panel container = new Panel();
        Panel textPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        textPanel.addComponent(new Label("Username: "));
        TextBox userBox = new TextBox("");

        Button enterButton = new Button("Confirm",
                () -> {
                    if (userBox.getText().length() <= 10){
                        // If the username is of the correct length, close the window and save the username
                        options.setUsername(userBox.getText());
                        username[0] = userBox.getText();
                        window.close();
                    }
                    else{
                        // Show username too long popup
                        displayPopupWindow(window, "Error, username too long");
                    }
                });
        enterButton.setTheme(getConfirmButtonTheme());
        Button cancelButton = new Button("Cancel", () -> {
            username[0] = null;
            window.close();
        });
        cancelButton.setTheme(getCancelButtonTheme());
        textPanel.addComponent(userBox);
        buttonsPanel.addComponent(enterButton);
        buttonsPanel.addComponent(cancelButton);
        container.addComponent(textPanel);
        container.addComponent(buttonsPanel);

        window.setComponent(container);
        centerWindow(window);
        gui.addWindowAndWait(window);
        return username[0];
    }
}
