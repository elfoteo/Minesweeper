package engine.utils;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import engine.options.Options;
import engine.options.OptionsInstance;
import org.json.JSONObject;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class FontManager {
    private static final String[] preinstalledFonts = new String[] {
            "Courier New",
            "Cascadia Code",
            "Cascadia Mono",
            "Consolas",
            "Lucida Sans Typewriter"
    };
    private static final List<String> fonts = new ArrayList<>();
    public static SwingTerminalFontConfiguration getFontConfiguration() throws IOException, FontFormatException {
        OptionsInstance oi = Options.readOptionsFromFile();
        JsonFont jsonFont = oi.getJsonFont();
        Font font = jsonFont.getFont();


        if (fonts.contains(font.getName())){
            // If the font name ends with a ".ttf" it is a file, so we load it from disk
            if (font.getName().endsWith(".ttf")){
                font = Font.createFont(Font.TRUETYPE_FONT, new File(Constants.fontsDir+font.getName())).deriveFont((float)font.getSize());
            }

            return new SwingTerminalFontConfiguration(true,
                    AWTTerminalFontConfiguration.BoldMode.EVERYTHING_BUT_SYMBOLS,
                    font,
                    new Font("Monospaced", Font.PLAIN, font.getSize()) // Load monospaced font for symbols support
            );
        }
        else{
            // Load default fonts
            return new SwingTerminalFontConfiguration(true,
                    AWTTerminalFontConfiguration.BoldMode.EVERYTHING_BUT_SYMBOLS,
                    new Font("Courier New", Font.PLAIN, font.getSize()),
                    new Font("Monospaced", Font.PLAIN, font.getSize()) // Load monospaced font for symbols support
            );
        }
    }

    public static void registerFonts() {
        // Add all the preinstalled fonts
        fonts.addAll(List.of(preinstalledFonts));

        // Add all the .ttf fonts in the fonts directory
        Path folder = Paths.get(Constants.fontsDir);
        // Try with resources
        try (Stream<Path> files = Files.walk(folder)) {
            files.filter(Files::isRegularFile) // If it is a file
                    .filter(path -> path.toString().toLowerCase().endsWith(".ttf")) // If it is a file ending with .ttf
                    .filter(path -> loadFontAndCheckMonospace(String.valueOf(path))) // Try loading the font and check if it is monospace
                    .forEach(path -> fonts.add(path.getFileName().toString())); // Add it to the fonts list
        } catch (IOException ignored) {}
    }

    // from AWTTerminalFontConfiguration
    private static boolean isFontMonospaced(Font font) {
        FontRenderContext frc = new FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
        Rectangle2D iBounds = font.getStringBounds("i", frc);
        Rectangle2D mBounds = font.getStringBounds("W", frc);
        return iBounds.getWidth() == mBounds.getWidth();
    }

    // from AWTTerminalFontConfiguration
    private static int getFontSize() {
        int baseFontSize = 14;
        String[] javaVersion = System.getProperty("java.version", "1").split("\\.");
        return System.getProperty("os.name", "").startsWith("Windows") && Integer.parseInt(javaVersion[0]) >= 9 ? baseFontSize : getHPIAdjustedFontSize(baseFontSize);
    }

    // from AWTTerminalFontConfiguration
    private static int getHPIAdjustedFontSize(int baseFontSize) {
        if (Toolkit.getDefaultToolkit().getScreenResolution() >= 110) {
            return Toolkit.getDefaultToolkit().getScreenResolution() / (baseFontSize / 2) + 1;
        } else {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (ge.getMaximumWindowBounds().getWidth() > 4096.0) {
                return baseFontSize * 4;
            } else {
                return ge.getMaximumWindowBounds().getWidth() > 2048.0 ? baseFontSize * 2 : baseFontSize;
            }
        }
    }

    private static boolean loadFontAndCheckMonospace(String font){
        try{
            return isFontMonospaced(Font.createFont(
                    Font.TRUETYPE_FONT, new File(font)
            ).deriveFont((float)16));
        }
        catch (Exception ignore){
            return false;
        }
    }

    public static Font getDefaultFont() {
        return new Font("Courier New", Font.PLAIN, getFontSize());
    }

    public static List<String> getFonts(){
        return fonts;
    }
}
