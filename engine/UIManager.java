package engine;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import engine.gui.impl.MainMenuGUI;
import engine.music.MusicManager;
import engine.music.MusicPlayer;
import engine.options.Options;
import engine.options.OptionsInstance;
import engine.skins.ISkin;
import engine.skins.SkinManager;
import engine.skins.impl.DefaultSkin;
import engine.themes.IGameTheme;
import engine.themes.ThemeManager;
import engine.themes.impl.DefaultGameTheme;
import engine.utils.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class UIManager {
    public static ISkin selectedSkin = new DefaultSkin();
    public static IGameTheme selectedTheme = new DefaultGameTheme();
    private final Terminal terminal;
    private final Screen screen;
    private final MultiWindowTextGUI gui;
    private final Panel mainPanel;
    private final TextGraphics textGraphics;
    public Leaderboard leaderboard;
    private final OptionsInstance options;
    private MenuPopupWindow themesMenuWindow;
    private RadioBoxList<String> themesMenuRadioboxList;
    private final List<Window.Hint> hints = new ArrayList<>();
    public TerminalResizeEventHandler terminalResizeEventHandler;
    private MusicPlayer musicPlayer;
    public UIManager(Terminal terminal) throws IOException {
        this.terminal = terminal;
        terminalResizeEventHandler = new TerminalResizeEventHandler(terminal.getTerminalSize());
        terminal.addResizeListener(terminalResizeEventHandler);

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
        hints.add(Window.Hint.NO_POST_RENDERING);

        mainWindow.setHints(hints);

        gui.addWindow(mainWindow);

        screen.startScreen();

        screen.clear();
        // To not show the panel
        mainWindow.setVisible(false);
        terminal.enterPrivateMode();
        // Load the options
        options = Options.readOptionsFromFile();
        // Set up music
        musicPlayer = MusicManager.getMusicPlayer();
        try{
            musicPlayer.changeSoundtrack(options.getSoundtrackFile());
        }
        catch (Exception ignore){
            Utils.Debug(Utils.exceptionToString(ignore));
        }
        musicPlayer.setVolumeToPercentage(Utils.boostAudio((float)options.getMusicVolume()/100));
        musicPlayer.play();
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

    public SimpleTheme getDisabledButtonTheme(){
        return new SimpleTheme(TextColor.ANSI.BLACK_BRIGHT, selectedTheme.getBackgroundColor(), SGR.BOLD);
    }

    public void applyThemeColors(TextGraphics textGraphics){
        textGraphics.setForegroundColor(selectedTheme.getForegroundColor());
        textGraphics.setBackgroundColor(selectedTheme.getBackgroundColor());
    }

    public TextColor getThemeBackgroundColor() {
        return selectedTheme.getBackgroundColor();
    }
    public TextColor getThemeForeground() {
        return selectedTheme.getForegroundColor();
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

    public boolean isDataCollectionRejected() {
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
        MainMenuGUI mainMenuGUI = new MainMenuGUI(this);
        mainMenuGUI.show();
    }

    public void updateTheme() {
        // To update the window theme in real time
        try{
            List<IGameTheme> themes = ThemeManager.getThemes();
            // Necessary
            Thread.sleep(20);
            for (IGameTheme theme : themes){
                if (theme.getThemeName().equals(themesMenuRadioboxList.getCheckedItem())){
                    // Update the selected theme
                    selectedTheme = theme;
                }
            }
            // Apply the new theme to the window
            themesMenuWindow.setTheme(getWindowTheme());
            // If the window component is a panel
            if (themesMenuWindow.getComponent() instanceof Panel){
                // For each component in the window
                for (Component component : ((Panel)themesMenuWindow.getComponent()).getChildren()){
                    // Update the single component theme
                    component.setTheme(getWindowTheme());
                }
            }

        }
        catch (Exception ignored){
            // This is a secondary update theme thread, any exceptions can be safely ignored
        }
    }

    public void showThemesMenu() {
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

    public void showSkinsMenu() {
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

    public void showContinuePopup(String message) {
        MenuPopupWindow window = new MenuPopupWindow(mainPanel);
        window.setTheme(getWindowTheme());
        Panel panel = new Panel();
        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Button continueButton = new Button("Continue", window::close);
        continueButton.setTheme(getConfirmButtonTheme());
        continueButton.setPreferredSize(new TerminalSize(continueButton.getLabel().length()+2, 1));
        continueButton.setPosition(new TerminalPosition(0, 5));
        buttonsPanel.addComponent(continueButton);

        Label textBox = new Label(message);

        panel.addComponent(textBox);
        panel.addComponent(buttonsPanel);

        window.setComponent(panel);
        centerWindow(window);
        gui.addWindowAndWait(window);
    }

    public void showOptions() {
        final boolean[] menuOpen = {true};
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
        EmptySpace usernameSpace = new EmptySpace(new TerminalSize(25-currentUsernameLabel.getText().length(), 1));
        Button chageUsernameButton = new Button("Change", () -> {
            // Force to show the username popup
            if (getUsername(true) != null){
                currentUsernameLabel.setText(String.format("\"%s\"", getUsername()));
                // On username update, update the optionsInstance too
                options.setUsername(getUsername());
                usernameSpace.setPreferredSize(new TerminalSize(25-currentUsernameLabel.getText().length(), 1));
            }
        });
        chageUsernameButton.setTheme(getConfirmButtonTheme());

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

        // Font options
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        Label fontOptionsLabel = new Label("Font Options:");
        Label currentFontLabel = new Label(String.format("\"%s\"", options.getJsonFont().getFont().getName()));
        if (options.getJsonFont().getFile() != null){
            currentFontLabel.setText(String.format("\"%s\"", options.getJsonFont().getFile()));
        }
        // Store the current font to check later if the font has changed
        String oldFont = currentFontLabel.getText();
        EmptySpace selectedFontSpace = new EmptySpace(new TerminalSize(25-currentFontLabel.getText().length(), 1));
        Button changeFontButton = new Button("Change", () -> {
            // Show the font selection popup, showing the font popup will automatically update
            // the font with the one selected by the user
            String newFontName = showFontSelectionPopup();
            if (newFontName.length() >= 20){
                newFontName = String.format("\"%s...\"", newFontName.substring(0, 15));
            }
            else {
                newFontName = String.format("\"%s\"", newFontName);
            }
            currentFontLabel.setText(newFontName);
            selectedFontSpace.setPreferredSize(new TerminalSize(25-currentFontLabel.getText().length(), 1));
        });
        changeFontButton.setTheme(getConfirmButtonTheme());

        container.addComponent(fontOptionsLabel);
        Panel fontNameOptions = new Panel(new LinearLayout(Direction.HORIZONTAL));
        fontNameOptions.addComponent(currentFontLabel);
        fontNameOptions.addComponent(selectedFontSpace); // Add space
        fontNameOptions.addComponent(changeFontButton);
        container.addComponent(fontNameOptions);


        Panel fontSizeOptionsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        fontSizeOptionsPanel.addComponent(new Label("Font size:"));
        int previousFontSize = options.getJsonFont().getFont().getSize();
        // Set the initial content to the previous font size
        TextBox fontOptionsSize = new TextBox(String.valueOf(previousFontSize));
        // Accept only up to 2 digits, and the number must be divisible by 2
        fontOptionsSize.setValidationPattern(Pattern.compile("\\d{1,2}"));
        fontSizeOptionsPanel.addComponent(fontOptionsSize);
        container.addComponent(fontSizeOptionsPanel);
        container.addComponent(new Label("Font size must be divisible by 2 and bigger then 10 smaller then 99"));

        // Music options
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        Panel musicVolumeOptions = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Label musicVolumeLabel = new Label("Music volume:");
        TextBox musicVolumeTextBox = new TextBox(String.valueOf(options.getMusicVolume()));
        // Match numbers from 1 to 100
        musicVolumeTextBox.setValidationPattern(Pattern.compile("^[0-9][0-9]?$|^100$"));
        musicVolumeTextBox.setPreferredSize(new TerminalSize(4, 1));
        // Soundtrack options
        Label currentSoundtrackLabel = new Label(String.format("\"%s\"", options.getSoundtrackFile().getName()));

        // Store the current soundtrack to check later if the soundtrack has changed
        EmptySpace selectedSoundtrackSpace = new EmptySpace(new TerminalSize(25-currentSoundtrackLabel.getText().length(), 1));
        Button changeSoundtrackButton = new Button("Change", () -> {
            // Show the soundtrack selection popup, showing the soundtrack popup will automatically update
            // the soundtrack with the one selected by the user
            String newSoundtrackName = showSoundtrackSelectionPopup();
            if (newSoundtrackName.length() >= 20){
                newSoundtrackName = String.format("\"%s...\"", newSoundtrackName.substring(0, 15));
            }
            else {
                newSoundtrackName = String.format("\"%s\"", newSoundtrackName);
            }
            currentSoundtrackLabel.setText(newSoundtrackName);
            selectedSoundtrackSpace.setPreferredSize(new TerminalSize(25-currentSoundtrackLabel.getText().length(), 1));
        });
        changeSoundtrackButton.setTheme(getConfirmButtonTheme());

        
        // Thread to update the music volume in real time
        new Thread(() -> {
            while (menuOpen[0]){
                try{
                    Utils.waitFor(10);
                    musicPlayer.setVolumeToPercentage(Utils.boostAudio((float) Integer.parseInt(musicVolumeTextBox.getText()) / 100));
                }
                catch (Exception ignore){

                }

            }
        }).start();


        // Add all the music volume options
        musicVolumeOptions.addComponent(musicVolumeLabel);
        musicVolumeOptions.addComponent(musicVolumeTextBox);
        musicVolumeOptions.addComponent(new Label("\u266B"));

        // Add all the soundtrack options
        Panel soundTrackOptions = new Panel(new LinearLayout(Direction.HORIZONTAL));
        soundTrackOptions.addComponent(currentSoundtrackLabel);
        soundTrackOptions.addComponent(selectedSoundtrackSpace); // Add space
        soundTrackOptions.addComponent(changeSoundtrackButton);

        container.addComponent(musicVolumeOptions);
        container.addComponent(new Label("Music volume ranges from 0 to 100"));
        container.addComponent(soundTrackOptions);
        // Exit button
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        Label hintLabel = new Label("Use tab to move around");
        hintLabel.addStyle(SGR.ITALIC);
        container.addComponent(hintLabel);
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        Button exitButton = new Button("Exit", window::close);
        exitButton.setTheme(getCancelButtonTheme());
        container.addComponent(exitButton);

        window.setComponent(container);
        centerWindow(window);
        gui.addWindowAndWait(window);
        // After the gui is closed
        // Update options values
        options.setGrayOutNearbyCells(grayNearbyCells.isChecked(0));
        // Get the current font from options
        Font currentFont = options.getJsonFont().getFont();

        // Get the font name of the current font
        String fontName = currentFont.getName();

        // Get the font size specified in the fontOptionsSize text field
        // and parse it to an integer.
        // If fails set to the previous size
        int newSize;
        try{
            newSize = Integer.parseInt(fontOptionsSize.getText());
        }
        catch (NumberFormatException ignore){
            newSize = previousFontSize;
        }

        // Create a new font with the same font name, with Plain style, and the new size
        Font newFont = new Font(fontName, Font.PLAIN, newSize);

        // Create a new JsonFont object with the new font
        JsonFont jsonFont = new JsonFont(newFont);

        // Set the option font to the new JsonFont object with the updated font
        if (newSize < 10 || newSize > 99 || newSize % 2 != 0) {
            String message = "Font size must be divisible by 2 and bigger then 10 smaller then 99.\nNo changes will be made to the font size.";
            if (!Objects.equals(oldFont, currentFontLabel.getText())){
                // Display a popup telling the user that to update the font they need to restart the app
                message += "\nOnly the font style will be changed.\nTo make the changes take effect, please restart the game.";
            }
            showContinuePopup(message);
        }
        else{
            if (options.getJsonFont().getFile() != null){
                jsonFont.setFromFile(options.getJsonFont().getFile());
            }
            options.setFont(jsonFont);

            if (newSize != previousFontSize || !Objects.equals(oldFont, currentFontLabel.getText())){
                // Display a popup telling the user that to update the font they need to restart the app
                showContinuePopup("To make the changes take effect, please restart the game.");
            }
        }

        menuOpen[0] = false;
        options.setMusicVolume(Integer.parseInt(musicVolumeTextBox.getText()));
        musicPlayer.setVolumeToPercentage(Utils.boostAudio((float) Integer.parseInt(musicVolumeTextBox.getText()) / 100));
        screen.clear();
    }

    private String showFontSelectionPopup() {
        MenuPopupWindow popupWindow = new MenuPopupWindow(mainPanel);
        popupWindow.setTheme(getWindowTheme());
        Panel popupContainer = new Panel();
        popupContainer.addComponent(new Label("Select a font:"));
        TerminalSize size = new TerminalSize(
                26,
                4
        );
        RadioBoxList<String> availableFonts = new RadioBoxList<>(size);
        for (String item : FontManager.getFonts()){
            // Add all the fonts
            availableFonts.addItem(item);
            // Check the selected one
            if (item.equals(options.getJsonFont().getFont().getName()) || item.equals(options.getJsonFont().getFile())){
                availableFonts.setCheckedItem(item);
            }
        }

        popupContainer.addComponent(availableFonts);

        Button okButton = new Button("Ok", popupWindow::close);
        okButton.setPreferredSize(new TerminalSize(4, 1));
        okButton.setTheme(getConfirmButtonTheme());
        popupContainer.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        Label hintLabel = new Label("Use tab to move around");
        hintLabel.addStyle(SGR.ITALIC);
        popupContainer.addComponent(hintLabel);
        popupContainer.addComponent(okButton);
        popupWindow.setComponent(popupContainer);
        centerWindow(popupWindow);
        gui.addWindowAndWait(popupWindow);
        // Window got closed
        try{
            for (String item : FontManager.getFonts()){
                if (availableFonts.isChecked(item)){
                    int fontSize = options.getJsonFont().getFont().getSize();
                    Font font;
                    if (item.endsWith(".ttf")){
                        font = Font.createFont(
                                Font.TRUETYPE_FONT, new File(Constants.fontsDir+item)).deriveFont((float)fontSize);
                    }
                    else{
                        font = new Font(item, Font.PLAIN, fontSize);
                    }
                    JsonFont jsonFont = new JsonFont(font);
                    if (item.endsWith(".ttf")){
                        jsonFont.setFromFile(item);
                    }
                    options.setFont(jsonFont);
                    return item;
                }
            }
        }
        catch (Exception ignore){

        }
        return "Courier New";
    }

    private String showSoundtrackSelectionPopup() {
        MenuPopupWindow popupWindow = new MenuPopupWindow(mainPanel);
        popupWindow.setTheme(getWindowTheme());
        Panel popupContainer = new Panel();
        popupContainer.addComponent(new Label("Select soundtrack:"));
        TerminalSize size = new TerminalSize(
                26,
                4
        );
        RadioBoxList<String> availableFonts = new RadioBoxList<>(size);
        for (File item : MusicManager.getSoundtracks()){
            String soundtrackName = item.getName();
            // Add all the fonts
            availableFonts.addItem(soundtrackName);
            // Check the selected one
            if (soundtrackName.equals(options.getSoundtrackFile().getName())){
                availableFonts.setCheckedItem(soundtrackName);
            }
        }

        popupContainer.addComponent(availableFonts);

        Button okButton = new Button("Ok", popupWindow::close);
        okButton.setPreferredSize(new TerminalSize(4, 1));
        okButton.setTheme(getConfirmButtonTheme());
        popupContainer.addComponent(new EmptySpace(new TerminalSize(1, 1)));
        Label hintLabel = new Label("Use tab to move around");
        hintLabel.addStyle(SGR.ITALIC);
        popupContainer.addComponent(hintLabel);
        popupContainer.addComponent(okButton);
        popupWindow.setComponent(popupContainer);
        centerWindow(popupWindow);
        gui.addWindowAndWait(popupWindow);
        // Window got closed
        try{
            for (File item : MusicManager.getSoundtracks()){
                String soundtrackName = item.getName();
                if (availableFonts.isChecked(soundtrackName)){
                    options.setSoundtrackFilePath(item.getName());

                    // Restart music
                    musicPlayer.changeSoundtrack(options.getSoundtrackFile());
                    musicPlayer.setVolumeToPercentage(Utils.boostAudio((float)options.getMusicVolume()/100));
                    musicPlayer.play();
                    return soundtrackName;
                }
            }
        }
        catch (Exception ignore){

        }
        return Constants.soundsDir+"sleepy.wav";
    }

    /**
     * Displays a menu for the user to select the game difficulty.
     *
     * @return The selected Minesweeper difficulty. Returns null if the user cancels the action.
     */
    public MinesweeperDifficulty getDifficulty() {
        // Default value isn't important
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
                case EASY -> button.setTheme(new SimpleTheme(TextColor.ANSI.GREEN, getThemeBackgroundColor(), SGR.BOLD));
                case MEDIUM -> button.setTheme(new SimpleTheme(new TextColor.RGB(255, 115, 0), getThemeBackgroundColor(), SGR.BOLD));
                case HARD -> button.setTheme(new SimpleTheme(new TextColor.RGB(180, 0, 0), getThemeBackgroundColor(), SGR.BOLD));
                case CUSTOM -> button.setTheme(new SimpleTheme(new TextColor.RGB(200, 200, 200), getThemeBackgroundColor(), SGR.BOLD));
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

    public String getUsername() {
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
        if (options.isUsernameValid() && !force){
            return options.getUsername();
        }

        MenuPopupWindow window = new MenuPopupWindow(mainPanel);
        window.setTheme(getWindowTheme());
        Panel container = new Panel();
        Panel textPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        textPanel.addComponent(new Label("Username:"));
        TextBox userBox = new TextBox("");

        Button enterButton = new Button("Confirm",
                () -> {
                    if (OptionsInstance.isUsernameValid(userBox.getText())){
                        // If the username is of the correct length, close the window and save the username
                        options.setUsername(userBox.getText());
                        username[0] = userBox.getText();
                        window.close();
                    }
                    else{
                        // Show username too long popup
                        showContinuePopup("Error, invalid username.\nThe username must be shorter then 10 characters and longer then 3");
                    }
                });
        enterButton.setTheme(getConfirmButtonTheme());
        Button cancelButton = new Button("Cancel", () -> {
            username[0] = null;
            window.close();
        });
        // Disable the cancel button if the username is invalid
        cancelButton.setEnabled(options.isUsernameValid());
        if (!options.isUsernameValid()){
            cancelButton.setTheme(getDisabledButtonTheme());
        }
        else {
            cancelButton.setTheme(getCancelButtonTheme());
        }
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

    public Tuple<Integer, Tuple<Integer, Integer>> askCustomDifficulty() {
        // Arg 1: how many mines
        // Arg 2: Tuple containing grid bounds (w, h)
        final int[] result = {-1, -1, -1};

        MenuPopupWindow window = new MenuPopupWindow(mainPanel);
        window.setTheme(getWindowTheme());
        Panel container = new Panel();
        Panel minesTextPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Panel gridSizeTextPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        minesTextPanel.addComponent(new Label("Enter how many mines:"));
        TextBox mines = new TextBox("");
        gridSizeTextPanel.addComponent(new Label("Grid size:"));
        TextBox gridSizeX = new TextBox("");
        TextBox gridSizeY = new TextBox("");
        // Make size inputs smaller
        gridSizeX.setPreferredSize(new TerminalSize(3, 1));
        gridSizeY.setPreferredSize(new TerminalSize(3, 1));
        mines.setValidationPattern(Pattern.compile("\\d+"));
        gridSizeX.setValidationPattern(Pattern.compile("\\d+"));
        gridSizeY.setValidationPattern(Pattern.compile("\\d+"));


        Button enterButton = new Button("Confirm",
                () -> {
                    if (mines.getText().length() <= 10){
                        // If the username is of the correct length, close the window and save the username
                        options.setUsername(mines.getText());
                        try{
                            result[0] = Integer.parseInt(mines.getText());
                            result[1] = Integer.parseInt(gridSizeX.getText());
                            result[2] = Integer.parseInt(gridSizeY.getText());
                            // Do all checks to verify that user input is valid values
                            if (result[1]*2+4 > terminal.getTerminalSize().getColumns() || result[2]+5 > terminal.getTerminalSize().getRows()){
                                // Check if the terminal window is big enough to create a game of that size
                                showInvalidLevelDataPopup("The terminal is too small to create a game with the specified size.\nPlease resize your terminal.");
                            }
                            else if (result[1] * result[2] < result[0]){
                                // Check that the gridWidth * gridHeight > mines
                                // If the user did give invalid data
                                // show the invalid data popup
                                showInvalidLevelDataPopup("Too many mines.\nThe number of mines must be smaller then the area of the grid.");
                            }
                            else if (result[0] <= 0 || result[1] <= 0 || result[2] <= 0){
                                showInvalidLevelDataPopup("Please give a valid grid size.");
                            }
                            else{
                                window.close();
                            }
                        }
                        catch (Exception ignore){
                            showInvalidLevelDataPopup("Invalid data provided");
                        }
                    }
                    else{
                        // Show username too long popup
                        displayPopupWindow(window, "Error, username too long");
                    }
                });
        enterButton.setTheme(getConfirmButtonTheme());
        Button cancelButton = new Button("Cancel", () -> {
            result[0] = -1;
            result[1] = -1;
            result[2] = -1;
            window.close();
        });
        cancelButton.setTheme(getCancelButtonTheme());
        minesTextPanel.addComponent(mines);
        gridSizeTextPanel.addComponent(gridSizeX);
        gridSizeTextPanel.addComponent(new Label("x"));
        gridSizeTextPanel.addComponent(gridSizeY);
        buttonsPanel.addComponent(enterButton);
        buttonsPanel.addComponent(cancelButton);
        container.addComponent(minesTextPanel);
        container.addComponent(gridSizeTextPanel);
        container.addComponent(buttonsPanel);

        window.setComponent(container);
        centerWindow(window);
        gui.addWindowAndWait(window);
        return new Tuple<>(result[0], new Tuple<>(result[1], result[2]));
    }

    public void showInvalidLevelDataPopup(String warningMessage) {
        MenuPopupWindow popupWindow = new MenuPopupWindow(mainPanel);
        popupWindow.setTheme(getWindowTheme());
        Panel popupContainer = new Panel();
        popupContainer.addComponent(new Label(warningMessage));

        Button cancelButton = new Button("Close", popupWindow::close);
        cancelButton.setPreferredSize(new TerminalSize(7, 1));
        cancelButton.setTheme(getCancelButtonTheme());

        popupContainer.addComponent(cancelButton);
        popupWindow.setComponent(popupContainer);
        centerWindow(popupWindow);
        gui.addWindowAndWait(popupWindow);
    }


    public void waitForTerminalResize(String message, TerminalSize goal) {
        AtomicBoolean running = new AtomicBoolean(true);

        MenuPopupWindow popupWindow = new MenuPopupWindow(mainPanel);
        popupWindow.setTheme(getWindowTheme());
        Panel popupContainer = new Panel();
        Label messageLabel = new Label(String.format(message, terminalResizeEventHandler.getLastKnownSize().getColumns(), terminalResizeEventHandler.getLastKnownSize().getRows()));
        popupContainer.addComponent(messageLabel);

        Thread updateThread = new Thread(() -> {
            while (running.get()) {
                messageLabel.setText(String.format(message, terminalResizeEventHandler.getLastKnownSize().getColumns(), terminalResizeEventHandler.getLastKnownSize().getRows()));
                Utils.waitFor(100);
                // If the terminal is equal or bigger of the goal size, then exit the function
                if (terminalResizeEventHandler.getLastKnownSize().getColumns() >= goal.getColumns() && terminalResizeEventHandler.getLastKnownSize().getRows() >= goal.getRows()){
                    running.set(false);
                    popupWindow.close();
                }
            }
        });
        updateThread.start();

        popupWindow.setComponent(popupContainer);
        centerWindow(popupWindow);
        gui.addWindowAndWait(popupWindow);
        screen.clear();
        running.set(false);
    }
}
