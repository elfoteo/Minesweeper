import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.StyleSet;

import java.util.EnumSet;

public class Constants {
    public static final SimpleTheme cancelButtonTheme = new SimpleTheme(TextColor.ANSI.RED, TextColor.ANSI.WHITE, SGR.BOLD);
    public static final SimpleTheme confirmButtonTheme = new SimpleTheme(TextColor.ANSI.GREEN, TextColor.ANSI.WHITE, SGR.BOLD);
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
    public static final String creatorText = "Game made by *Matteo Ciocci*";
    public static final StyleSet<StyleSet.Set> blinkStyle = (new StyleSet.Set()).setModifiers(EnumSet.of(SGR.BLINK));
}
