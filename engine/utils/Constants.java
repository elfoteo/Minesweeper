package engine.utils;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.StyleSet;
import com.googlecode.lanterna.graphics.Theme;

import java.util.EnumSet;

public class Constants {
    public static final SimpleTheme cancelButtonTheme = new SimpleTheme(TextColor.ANSI.RED, TextColor.ANSI.DEFAULT, SGR.BOLD);
    public static final SimpleTheme confirmButtonTheme = new SimpleTheme(TextColor.ANSI.GREEN, TextColor.ANSI.DEFAULT, SGR.BOLD);
    public static final SimpleTheme windowDefaultTheme = new SimpleTheme(TextColor.ANSI.WHITE, TextColor.ANSI.DEFAULT);
    public static final String[] logo = """
 __  __ _                                                  \s
|  \\/  (_)                                                 \s
| \\  / |_ _ __   ___  _____      _____  ___ _ __   ___ _ __\s
| |\\/| | | '_ \\ / _ \\/ __\\ \\ /\\ / / _ \\/ _ \\ '_ \\ / _ \\ '__|
| |  | | | | | |  __/\\__ \\\\ V  V /  __/  __/ |_) |  __/ |  \s
|_|  |_|_|_| |_|\\___||___/ \\_/\\_/ \\___|\\___| .__/ \\___|_|  \s
										     | |             \s
										     |_|             \s
            """.split("\n");
    public static final String[] leaderboardLogo = """
 _                    _           _                         _\s
| |                  | |         | |                       | |
| |     ___  __ _  __| | ___ _ __| |__   ___   __ _ _ __ __| |
| |    / _ \\/ _` |/ _` |/ _ \\ '__| '_ \\ / _ \\ / _` | '__/ _` |
| |___|  __/ (_| | (_| |  __/ |  | |_) | (_) | (_| | | | (_| |
|______\\___|\\__,_|\\__,_|\\___|_|  |_.__/ \\___/ \\__,_|_|  \\__,_|""".split("\n");
    public static final String creatorText = "Game made by *Matteo Ciocci*";
    public static final StyleSet<StyleSet.Set> blinkStyle = (new StyleSet.Set()).setModifiers(EnumSet.of(SGR.BLINK));
    public static final String apiUrl = "https://minesweeperapi.cyclic.app/";
    public static final String dataCollectionWarning = "By playing this game, you agree that your game data, including:\n" +
            " - username\n" +
            " - score\n" +
            " - game time\n" +
            "may be sent to the leaderboard for competition purposes.\n" +
            "Your privacy is important for us, and your data will be handled securely.";
    public static final String dataCollectionAcceptedFile = "data/data_collection.txt";
    public static final String usernameFile = "data/username.txt";
}
