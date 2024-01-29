package engine.utils;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.StyleSet;

import java.util.EnumSet;

public class Constants {
    public static final TextColor cellHighlightColor = new TextColor.RGB(235, 128, 52);
    public static final TextColor dangerColor = new TextColor.RGB(200, 10, 13);
    public static final String[] mainMenuOptions = new String[] {"Play", "Leaderboard", "Settings", "About", "Exit"};
    public static final String[] settingsMenuOptions = new String[] {"Skins", "Themes", "Options", "Back"};
    public static final String[] minesweeperLogo = """
 __  __ _                                                  
|  \\/  (_)                                                 
| \\  / |_ _ __   ___  _____      _____  ___ _ __   ___ _ __
| |\\/| | | '_ \\ / _ \\/ __\\ \\ /\\ / / _ \\/ _ \\ '_ \\ / _ \\ '__|
| |  | | | | | |  __/\\__ \\\\ V  V /  __/  __/ |_) |  __/ | 
|_|  |_|_|_| |_|\\___||___/ \\_/\\_/ \\___|\\___| .__/ \\___|_|
                                           | |
                                           |_|
            """.split("\n");
    public static final String[] leaderboardLogo = """
 _                    _           _                         _
| |                  | |         | |                       | |
| |     ___  __ _  __| | ___ _ __| |__   ___   __ _ _ __ __| |
| |    / _ \\/ _` |/ _` |/ _ \\ '__| '_ \\ / _ \\ / _` | '__/ _` |
| |___|  __/ (_| | (_| |  __/ |  | |_) | (_) | (_| | | | (_| |
|______\\___|\\__,_|\\__,_|\\___|_|  |_.__/ \\___/ \\__,_|_|  \\__,_|""".split("\n");
    public static final String[] settingsLogo = """
   _____      _   _   _                
  / ____|    | | | | (_)               
 | (___   ___| |_| |_ _ _ __   __ _ ___
  \\___ \\ / _ \\ __| __| | '_ \\ / _` / __|
  ____) |  __/ |_| |_| | | | | (_| \\__ \\
 |_____/ \\___|\\__|\\__|_|_| |_|\\__, |___/
                               __/ |   
                              |___/    """.split("\n");
    public static final String winMessage = "Congratulations! You've successfully cleared the minefield!\nScore: %d\nPress \"Play Again\" to start again or \"Exit\" to exit";
    public static final String lossMessage = "Oh no! You've uncovered a mine!\nScore: %d\nPress \"Play Again\" to start again or \"Exit\" to exit";
    public static final String creatorText = "Game made by *Matteo Ciocci*";
    public static final StyleSet<StyleSet.Set> blinkStyle = (new StyleSet.Set()).setModifiers(EnumSet.of(SGR.BLINK));
    public static final String apiUrl = "https://minesweeperapi.cyclic.app/";
    public static final String dataCollectionWarning = """
 By playing this game, you agree that your game data, including:
 - username
 - score
 - game time
may be sent to the leaderboard for competition purposes.
Your privacy is important for us, and your data will be handled securely.""";
    public static final String aboutText = """
Welcome to Minesweeper, a console-based game.
This game was coded by Matteo Ciocci as a school project.

How to navigate menus:
 - Use the arrow keys to move up and down.
 - Press the Enter key to choose an option.
 - To exit a menu, press Escape.

How to play:
 - Navigate the grid with the 4 arrow keys.
 - Press Enter to uncover a cell.
 - Press "F" to flag a cell.
 - To win you need to flag all mines correctly
 
If you uncover a mine you can respawn up to 3 times if you have enough score""";
    public static final String appDataDir = "data/";
    public static final String dataCollectionAcceptedFile = appDataDir+"data_collection.txt";
    public static final String skinFile = appDataDir+"skin.dat";
    public static final String optionsFile = appDataDir+"options.json";
    public static final String themeFile = appDataDir+"theme.dat";
    public static final String localLeaderboardFile = appDataDir+"local_leaderboard.json";
    public static final String fontsDir = appDataDir+"fonts/";
}
