import engine.Leaderboard;
import engine.Minesweeper;
import engine.utils.Constants;
import engine.utils.MinesweeperDifficulty;
import engine.utils.Tuple;
import engine.utils.Utils;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Minesweeper using java and lanterna
 * 
 * @author Matteo Ciocci
 *
 */
public class Program {
	private static final String[] menu = new String[] {"Play", "About","Exit"};
	private static int selectedIndex = 0;
	private static Terminal terminal;
	private static Screen screen;
	private static MultiWindowTextGUI gui;
    private static Panel mainPanel;
	private static TextGraphics textGraphics;
    private static long startTime;
	private static ScheduledExecutorService timer;
	private static ScheduledFuture<?> timerTask;

	public static void main(String[] args) throws IOException {
		terminal = new DefaultTerminalFactory().createTerminal();
		
		screen = new TerminalScreen(terminal);
		textGraphics = screen.newTextGraphics();
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

		try {
			Leaderboard l = new Leaderboard(screen, textGraphics);
			l.displayLeaderboard();
			showMainScreen();
		}
		catch (Exception ex){
			stopTimer();
			// Construct the crash report message with stack trace
			StringBuilder crashReport = new StringBuilder("Crash Report:\n\n");
			crashReport.append("An unexpected error occurred.\n");
			crashReport.append("Error details:\n");
			crashReport.append(ex);
			crashReport.append("\n\nStack Trace:\n");

			// Append each line of the stack trace to the crash report
			for (StackTraceElement element : ex.getStackTrace()) {
				crashReport.append(element.toString()).append("\n");
			}

			// Show the crash report in a message box
			Utils.Debug(crashReport.toString());
		}

		terminal.exitPrivateMode();
		screen.stopScreen();
		terminal.close();
	}

	private static void showMainScreen() throws IOException {
		boolean running = true;
		while (running){
			// Add logo
			int x = Utils.getMaxStringLength(Constants.logo);
			int y = 0;
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
							playAgain = play(username, difficulty);
						}
						while (playAgain);
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
	}
	/**
	 * Displays a menu for the user to select the game difficulty.
	 *
	 * @return The selected Minesweeper difficulty. Returns {@code null} if the user cancels the action.
	 * @throws IOException If an I/O error occurs while interacting with the user interface.
	 */
	private static MinesweeperDifficulty getDifficulty() throws IOException {
		final MinesweeperDifficulty[] selectedDifficulty = {MinesweeperDifficulty.MEDIUM};
		MenuPopupWindow window = new MenuPopupWindow(mainPanel);
		Panel container = new Panel();
		container.addComponent(new Label("Select the game difficulty:"));
		for (MinesweeperDifficulty difficulty : MinesweeperDifficulty.values()){
			String name = difficulty.name();
			String camelCase = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
			Button button = new Button(camelCase, () -> {
				selectedDifficulty[0] = difficulty;
				window.close();
			});
			switch (difficulty){
				case EASY -> button.setTheme(new SimpleTheme(TextColor.ANSI.GREEN, TextColor.ANSI.WHITE));
				case MEDIUM -> button.setTheme(new SimpleTheme(new TextColor.RGB(255, 115, 0), TextColor.ANSI.WHITE));
				case HARD -> button.setTheme(new SimpleTheme(TextColor.ANSI.RED, TextColor.ANSI.WHITE));
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

	/**
	 * Displays a menu for the user to input their username.
	 *
	 * <p>The method displays a UI to let the user enter their username.
	 * It returns the entered username as a String. If the user cancels the action or an I/O error occurs during the interaction,
	 * the method returns {@code null}.</p>
	 *
	 * @return The entered username. Returns {@code null} if the user cancels the action or an I/O error occurs.
	 * @throws IOException If an I/O error occurs while interacting with the user interface.
	 */
	private static String getUsername() throws IOException {
		final String[] username = {""};
		MenuPopupWindow window = new MenuPopupWindow(mainPanel);
		Panel container = new Panel();
		Panel textPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
		Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
		textPanel.addComponent(new Label("Username: "));
		TextBox userBox = new TextBox("");
		Button enterButton = new Button("Confirm",
				() -> {
					username[0] = userBox.getText();
					window.close();
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
		return username[0];
	}

	private static void updateTimer() {
		// Calculate and display the elapsed time
		long elapsedTime = System.currentTimeMillis() - startTime;
		long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
		long minutes = TimeUnit.SECONDS.toMinutes(seconds);
		long remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes);

		boolean showColon = (elapsedTime % 1000 < 500);

		String title;
		// String format pattern: "%02d %02d"
		// - %02d: Represents an integer with a minimum width of 2 digits.
		// - Space: Adds a space between the two numbers.
		if (showColon) {
			title = "Time: " + String.format("%02d:%02d", minutes, remainingSeconds);
		} else {
			title = "Time: " + String.format("%02d %02d", minutes, remainingSeconds);
		}

		textGraphics.putString(0, 3, title);
		// Refresh the screen to see the changes in real time
		try {
			screen.refresh();
		} catch (Exception ignored) {
		}
	}

	private static void stopTimer(){
		if (timerTask != null && !timerTask.isCancelled()) {
			timerTask.cancel(true);
		}
		timer.shutdown();
	}

	private static void displaySidebarMessage(TextGraphics textGraphics, int line, String label, String content) {
		int width = textGraphics.getSize().getColumns();
		String message = String.format(label, content) + " ".repeat(width - (label.length() + content.length()));
		textGraphics.putString(0, line, message);
	}

	/**
	 * Initiates a Minesweeper game with a specified username and difficulty level.
	 *
	 * <p>The method starts a Minesweeper game for the provided username and difficulty level.
	 * It returns a boolean indicating whether the player wants to play again after completing the session.
	 * If an I/O error occurs during the gameplay or user interaction, it is thrown as an IOException.</p>
	 *
	 * @param username   The username of the player.
	 * @param difficulty The difficulty level of the Minesweeper game.
	 * @return {@code true} if the player wants to play again, {@code false} otherwise.
	 * @throws IOException If an I/O error occurs during gameplay or user interaction.
	 */
	private static boolean play(String username, MinesweeperDifficulty difficulty) throws IOException {
		screen.clear();
		String title = "Minesweeper";
		textGraphics.putString(screen.getTerminalSize().getColumns() / 2 - title.length() / 2, 0, title);
		screen.refresh();
		Tuple<Integer, Tuple<Integer, Integer>> difficultyInfo = Utils.getDifficultyInfo(difficulty);
		Minesweeper minesweeper = new Minesweeper(difficultyInfo.second().first(), difficultyInfo.second().second(), difficultyInfo.first());
		String[] field = minesweeper.getFieldAsString().split("\n");
		Rectangle bounds = new Rectangle(
				screen.getTerminalSize().getColumns()/2-Utils.getMaxStringLength(field)/2,
				screen.getTerminalSize().getRows()/2-field.length/2, field[0].length(), field.length);
		startTime = System.currentTimeMillis();
		timer = Executors.newSingleThreadScheduledExecutor();
		// Schedule timer update every 500 milliseconds (half second)
		timerTask = timer.scheduleAtFixedRate(Program::updateTimer, 0, 500, TimeUnit.MILLISECONDS);

		int cursorX = screen.getTerminalSize().getColumns()/2-Utils.getMaxStringLength(field)/2;
		int cursorY = screen.getTerminalSize().getRows()/2-field.length/2;
		int trueX = 0;
		int trueY = 0;
		int score = 0;

		boolean running = true;
		final boolean[] playAgain = {false};

		while (running) {
			displaySidebarMessage(textGraphics, 1, "Score: %s", String.valueOf(score));
			int mines = minesweeper.getRemainingMines();
			String message = mines < 0 ? "Mines: %s (Too many cells flagged)" : "Mines: %s";
			displaySidebarMessage(textGraphics, 2, message, String.valueOf(mines));
			screen.setCursorPosition(new TerminalPosition(cursorX, cursorY));
			textGraphics.putString(0, terminal.getTerminalSize().getRows()-1, "Press 'F' to flag a mine");
			field = minesweeper.getFieldAsString().split("\n");

			int centerX = screen.getTerminalSize().getColumns() / 2 - Utils.getMaxStringLength(field) / 2;
			int centerY = screen.getTerminalSize().getRows() / 2 - field.length / 2;
			int offsetX;

			for (int row = 0; row < minesweeper.getFieldWidth(); row++) {
				offsetX = 0;

				for (int col = 0; col < minesweeper.getFieldHeight(); col++) {
					String cellContent;

					if (minesweeper.isUncovered(row, col)) {
						cellContent = String.valueOf(minesweeper.getCell(row, col));
					} else {
						cellContent = "#";
					}

					// Highlight the cell if needed
					if (minesweeper.isCellHighlighted(col, row)) {
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
				case ArrowUp:
					if (bounds.contains(cursorX, cursorY-1)){
						cursorY--;
						trueY--;
					}
					break;
				case ArrowDown:
					if (bounds.contains(cursorX, cursorY+1)){
						cursorY++;
						trueY++;
					}
					break;
				case ArrowLeft:
					if (bounds.contains(cursorX-2, cursorY)) {
						cursorX -= 2;
						trueX--;
					}
					break;
				case ArrowRight:
					if (bounds.contains(cursorX+2, cursorY)) {
						cursorX += 2;
						trueX++;
					}
					break;
				case Character:
					if (choice.getCharacter() == 'f'){
						minesweeper.toggleHighlightCell(trueX, trueY);
					}
					break;
				case Enter:
					Tuple<Character, Tuple<Integer, Boolean>> minedTile = minesweeper.uncover(trueX, trueY);
					// Character: the character that got mined
					// Integer: score
					// Boolean: player has won (game ended)

					// If the char is null the cell that the user mined is currently locked
					if (minedTile.first() == null){
						break;
					}

					// if the game ended
					if (minedTile.second().second()) {
						// Player has won
						// Stop the timer
						stopTimer();
						// Add the score
						score += minedTile.second().first();
						// Show popup
						MenuPopupWindow window = new MenuPopupWindow(mainPanel);

						// Close button
						Button exitButton = new Button("Exit", window::close);

						Button playAgainButton = new Button("Play Again",
								() -> {
									playAgain[0] = true;
									window.close();
								});

						Label textBox = new Label(String.format("Congratulations! You've successfully cleared the minefield!\nScore: %d\nPress \"Play Again\" to play again or \"Exit\" to exit", score));
						playAgainButton.setTheme(Constants.confirmButtonTheme);
						exitButton.setTheme(Constants.cancelButtonTheme);

						Panel panel = new Panel();
						Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
						exitButton.setPosition(new TerminalPosition(0, 5));
						buttonsPanel.addComponent(playAgainButton);
						buttonsPanel.addComponent(exitButton);
						panel.addComponent(textBox);
						panel.addComponent(buttonsPanel);

						// Window can hold only one component, we add a panel to hold everything
						window.setComponent(panel);
						window.setPosition(new TerminalPosition(terminal.getTerminalSize().getColumns() / 2 - 35,
								terminal.getTerminalSize().getRows() / 2 - 4));
						gui.addWindowAndWait(window);

						running = false;
					}
					else if (minedTile.first() == '*') {
						// Player has lost
						// Stop the timer
						stopTimer();
                        // Show popup
						MenuPopupWindow window = new MenuPopupWindow(mainPanel);


                        // Close button
                        Button exitButton = new Button("Exit", window::close);
						exitButton.setTheme(Constants.cancelButtonTheme);
                        Button playAgainButton = new Button("Play Again",
								() -> {
									playAgain[0] = true;
									window.close();
								});
						playAgainButton.setTheme(Constants.confirmButtonTheme);
                        Label textBox = new Label(String.format("Oh no! You've uncovered a mine!\nScore: %d\nPress \"Play Again\" to play again or \"Exit\" to exit", score));
                        Panel panel = new Panel();
                        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
						exitButton.setPosition(new TerminalPosition(0, 5));
                        buttonsPanel.addComponent(exitButton);
                        buttonsPanel.addComponent(playAgainButton);
                        panel.addComponent(textBox);
                        panel.addComponent(buttonsPanel);


                        // Window can hold only one component, we add a panel to hold everything
                        window.setComponent(panel);
                        window.setPosition(new TerminalPosition(terminal.getTerminalSize().getColumns()/2-25,
								terminal.getTerminalSize().getRows()/2-4));
						gui.addWindowAndWait(window);

                        running = false;
                    } else {// if the tile wasn't already mined
						score += minedTile.second().first();
                    }
					break;
				case EOF, Escape:
					running = false;
					break;
            }

		}
		// Stop timer
		stopTimer();
		// Clear screen
		screen.setCursorPosition(new TerminalPosition(0, 0));
		screen.clear();
		return playAgain[0];
	}

	public static void showAboutMenu() throws IOException {
		screen.clear();
		// Hide cursor
		Utils.hideCursor(0, 0, textGraphics);

		// Center the "About" title
		String title = "About";
		textGraphics.putString(screen.getTerminalSize().getColumns() / 2 - title.length() / 2, 0, title);

		// Display information about the game and the developer
		textGraphics.putString(0, 2, "Welcome to Minesweeper, a console-based game.");
		textGraphics.putString(0, 3, "This game was coded by Matteo Ciocci as a school project.");

		// Provide instructions on how to navigate menus
		textGraphics.putString(0, 5, "How to navigate menus:");
		textGraphics.putString(0, 6, " - Use the arrow keys to move up and down.");
		textGraphics.putString(0, 7, " - Press the Enter key to choose an option.");
		textGraphics.putString(0, 8, " - To exit a menu, press Escape.");

		// Provide instructions on how to play
		textGraphics.putString(0, 10, "How to play:");
		textGraphics.putString(0, 11, " - Navigate the grid with the 4 arrow keys.");
		textGraphics.putString(0, 12, " - Press Enter to select a cell.");

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
}