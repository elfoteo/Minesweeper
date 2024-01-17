package engine;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import engine.utils.Constants;
import engine.utils.GameInstance;
import engine.utils.MinesweeperDifficulty;
import engine.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class UIManager {
    private final String[] menu = new String[] {"Play", "Leaderboard", "Options", "About","Exit"};
    private int selectedIndex = 0;
    private final Terminal terminal;
    private final Screen screen;
    private final MultiWindowTextGUI gui;
    private final Panel mainPanel;
    private final TextGraphics textGraphics;
    public Leaderboard leaderboard;
    private final Game game;
    public UIManager(Terminal terminal) throws IOException {
        this.terminal = terminal;

        screen = new TerminalScreen(terminal);
        textGraphics = screen.newTextGraphics();
        leaderboard = new Leaderboard(screen, textGraphics);
        gui = new MultiWindowTextGUI(screen, TextColor.ANSI.BLACK);
        BasicWindow mainWindow = new BasicWindow();
        mainPanel = new Panel();
        mainWindow.setComponent(mainPanel);

        gui.addWindow(mainWindow);

        screen.startScreen();

        screen.clear();
        // To not show the panel
        mainWindow.setVisible(false);
        terminal.enterPrivateMode();
        game = new Game(this);
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

    public boolean showDataCollectionWarning() throws IOException {
        if (isDataCollectionRejected()) {
            final boolean[] accepted = {false};
            MenuPopupWindow window = new MenuPopupWindow(mainPanel);
            window.setTheme(Constants.windowDefaultTheme);
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
            acceptButton.setTheme(Constants.confirmButtonTheme);

            Button cancelButton = new Button("Exit", () -> {
                accepted[0] = false;
                window.close();
            });
            cancelButton.setTheme(Constants.cancelButtonTheme);

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
            // Handle the exception according to your needs
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
        boolean running = true;
        while (running){
            // Add logo
            int x = Utils.getMaxStringLength(Constants.logo);
            int y = 1;
            for (String logoLine : Constants.logo){
                textGraphics.putString(screen.getTerminalSize().getColumns()/2-x/2, y, logoLine);
                y++;
            }
            // Add creator text
            int xOffset = 0;

            for (String segment : Constants.creatorText.split("\\*")) {
                // Display the non-star segment without any modifiers
                textGraphics.putString(screen.getTerminalSize().getColumns() - Constants.creatorText.length() - 1 + xOffset,
                        terminal.getTerminalSize().getRows() - 1, segment);
                xOffset += segment.length();

                // Apply the blinking style to the next star
                textGraphics.setStyleFrom(Constants.blinkStyle);

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

            x = Utils.getMaxStringLength(menu)+2;
            y = Constants.logo.length+2;
            int counter = 0;
            for (String menuLine : menu){
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
                if (selectedIndex > menu.length-1){
                    selectedIndex = 0;
                }
            }
            else if (choice.getKeyType() == KeyType.ArrowUp){
                selectedIndex--;
                if (selectedIndex < 0){
                    selectedIndex = menu.length-1;
                }
            } else if (choice.getKeyType() == KeyType.Enter) {
                switch (menu[selectedIndex]){
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
                    case "About":
                        showAboutMenu();
                        break;
                    case "Options":
                        showOptions();
                        break;
                    case "Exit":
                        running = false;
                        break;
                }
            }
        }
    }

    public void showPlayAgainPopup(String message, GameInstance gameInstance) {
        MenuPopupWindow window = new MenuPopupWindow(mainPanel);
        window.setTheme(Constants.windowDefaultTheme);

        Button exitButton = new Button("Exit", window::close);
        exitButton.setTheme(Constants.cancelButtonTheme);
        exitButton.setPreferredSize(new TerminalSize(exitButton.getLabel().length()+2, 1));

        Button playAgainButton = new Button("Play Again", () -> {
            gameInstance.setPlayAgain(true);
            window.close();
        });
        playAgainButton.setTheme(Constants.confirmButtonTheme);

        Label textBox = new Label(message);

        Panel panel = new Panel();
        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        exitButton.setPosition(new TerminalPosition(0, 5));
        buttonsPanel.addComponent(exitButton);
        buttonsPanel.addComponent(playAgainButton);
        panel.addComponent(textBox);
        panel.addComponent(buttonsPanel);

        try {
            window.setComponent(panel);
            // Center the window
            window.setPosition(new TerminalPosition(
                    terminal.getTerminalSize().getColumns() / 2 - Utils.getMaxStringLength(message.split("\n")) / 2,
                    terminal.getTerminalSize().getRows() / 2 - 4));
            gui.addWindowAndWait(window);
        } catch (IOException ignore) {
            // Handle IOException
        }
    }

    public void showAboutMenu() throws IOException {
        screen.clear();
        // Hide cursor
        Utils.hideCursor(0, 0, textGraphics);

        // Center the "About" title
        String title = "About";
        textGraphics.putString(screen.getTerminalSize().getColumns() / 2 - title.length() / 2, 0, title);

        // Display information about the game and the developer
        textGraphics.putString(0, 2, "Welcome to Minesweeper, a console-based game.");
        textGraphics.putString(0, 3, "This game was coded by Matteo Ciocci as a school project.");

        // Instructions on how to navigate menus
        textGraphics.putString(0, 5, "How to navigate menus:");
        textGraphics.putString(0, 6, " - Use the arrow keys to move up and down.");
        textGraphics.putString(0, 7, " - Press the Enter key to choose an option.");
        textGraphics.putString(0, 8, " - To exit a menu, press Escape.");

        // How to start
        textGraphics.putString(0, 10, "How to start:");
        textGraphics.putString(0, 11, " - Navigate the grid with the 4 arrow keys.");
        textGraphics.putString(0, 12, " - Press Enter to uncover a cell.");
        textGraphics.putString(0, 13, " - Press \"F\" to flag a cell.");

        // Display the information
        screen.refresh();

        // Wait for Escape key to exit
        while (true) {
            KeyStroke choice = screen.readInput();
            if (choice.getKeyType() == KeyType.Escape) {
                break;
            }
        }
        screen.clear();
    }

    private void showOptions() throws IOException {
        screen.clear();
        MenuPopupWindow window = new MenuPopupWindow(mainPanel);
        window.setTheme(Constants.windowDefaultTheme);
        Panel container = new Panel();
        container.addComponent(new Label("Options"));

        // Username settings
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        Label usernameLabel = new Label("Change username:");
        Label currentUsernameLabel = new Label(String.format("\"%s\"", getUsername()));
        // usernameSpace is used to keep the space between currentUsernameLabel and changeUsernameButton the same
        EmptySpace usernameSpace = new EmptySpace(new TerminalSize(15-currentUsernameLabel.getText().length(), 1));
        Button chageUsernameButton = new Button("Change", () -> {
            try {
                // Force to show the username popup
                if (getUsername(true) != null){
                    currentUsernameLabel.setText(String.format("\"%s\"", getUsername()));
                    usernameSpace.setPreferredSize(new TerminalSize(15-currentUsernameLabel.getText().length(), 1));
                }
            } catch (IOException ignore) {

            }
        });

        container.addComponent(usernameLabel);
        Panel usernameOptions = new Panel(new LinearLayout(Direction.HORIZONTAL));
        usernameOptions.addComponent(currentUsernameLabel);
        usernameOptions.addComponent(usernameSpace); // Add space
        usernameOptions.addComponent(chageUsernameButton);
        container.addComponent(usernameOptions);
        // Exit button
        container.addComponent(new EmptySpace(new TerminalSize(1, 1))); // Add some space
        Button exitButton = new Button("Exit", window::close);
        exitButton.setTheme(Constants.cancelButtonTheme);
        container.addComponent(exitButton);


        window.setComponent(container);
        window.setPosition(
                new TerminalPosition(terminal.getTerminalSize().getColumns() / 2 - 18,
                        terminal.getTerminalSize().getRows() / 2 - 6
                )
        );

        gui.addWindowAndWait(window);

        screen.clear();
    }

    /**
     * Displays a menu for the user to select the game difficulty.
     *
     * @return The selected Minesweeper difficulty. Returns {@code null} if the user cancels the action.
     * @throws IOException If an I/O error occurs while interacting with the user interface.
     */
    private MinesweeperDifficulty getDifficulty() throws IOException {
        final MinesweeperDifficulty[] selectedDifficulty = {MinesweeperDifficulty.MEDIUM};
        MenuPopupWindow window = new MenuPopupWindow(mainPanel);
        Panel container = new Panel();
        window.setTheme(Constants.windowDefaultTheme);
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
        cancelButton.setTheme(Constants.cancelButtonTheme);

        container.addComponent(cancelButton);

        window.setComponent(container);
        window.setPosition(new TerminalPosition(terminal.getTerminalSize().getColumns() / 2 - 15,
                terminal.getTerminalSize().getRows() / 2 - 4));

        gui.addWindowAndWait(window);
        return selectedDifficulty[0];
    }

    public void displayPopupWindow(Window parentWindow, String message) {
        MenuPopupWindow popupWindow = new MenuPopupWindow(parentWindow.getComponent());
        popupWindow.setTheme(Constants.windowDefaultTheme);
        Panel popupContainer = new Panel();
        popupContainer.addComponent(new Label(message));
        Button okButton = new Button("Ok", popupWindow::close);
        okButton.setPreferredSize(new TerminalSize(4, 1));
        okButton.setTheme(Constants.confirmButtonTheme);
        popupContainer.addComponent(okButton);
        try {
            popupWindow.setComponent(popupContainer);
            popupWindow.setPosition(new TerminalPosition(terminal.getTerminalSize().getColumns() / 2 - message.length()/2,
                    terminal.getTerminalSize().getRows() / 2 - 4));
            gui.addWindowAndWait(popupWindow);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUsername() throws IOException {
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
     * @throws IOException If an I/O error occurs while interacting with the user interface.
     */
    private String getUsername(boolean force) throws IOException {
        final String[] username = {""};
        Path usernameFile = Path.of(Constants.usernameFile);
        if (Files.exists(usernameFile) && !force){
            try{
                // Try to read the username from the file
                username[0] = Files.readAllLines(usernameFile).get(0);
                return username[0];
            }
            catch (Exception ignore){

            }
        }

        MenuPopupWindow window = new MenuPopupWindow(mainPanel);
        window.setTheme(Constants.windowDefaultTheme);
        Panel container = new Panel();
        Panel textPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        textPanel.addComponent(new Label("Username: "));
        TextBox userBox = new TextBox("");

        Button enterButton = new Button("Confirm",
                () -> {
                    if (userBox.getText().length() <= 10){
                        // If username is of the correct length close the window
                        username[0] = userBox.getText();
                        window.close();
                    }
                    else{
                        // Show username too long popup
                        displayPopupWindow(window, "Error, username too long");
                    }
                });
        enterButton.setTheme(Constants.confirmButtonTheme);
        Button cancelButton = new Button("Cancel", () -> {
            username[0] = null;
            window.close();
        });
        cancelButton.setTheme(Constants.cancelButtonTheme);
        textPanel.addComponent(userBox);
        buttonsPanel.addComponent(enterButton);
        buttonsPanel.addComponent(cancelButton);
        container.addComponent(textPanel);
        container.addComponent(buttonsPanel);

        window.setComponent(container);
        window.setPosition(new TerminalPosition(terminal.getTerminalSize().getColumns() / 2 - 12,
                terminal.getTerminalSize().getRows() / 2 - 4));
        gui.addWindowAndWait(window);
        if (username[0] != null){
            try{
                // Let's try to save the username to the file
                Files.writeString(usernameFile, username[0],
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
            catch (Exception ignore){

            }
        }
        return username[0];
    }
}
