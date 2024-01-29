package engine.utils;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import engine.options.Options;
import engine.options.OptionsInstance;
import org.json.JSONObject;
import java.awt.*;
import java.util.Objects;

public class FontManager {
    public static SwingTerminalFontConfiguration getFontConfiguration(){
        OptionsInstance oi = Options.readOptionsFromFile();
        JsonFont jsonFont = oi.getJsonFont();
        Font font = jsonFont.font();

        return new SwingTerminalFontConfiguration(true, AWTTerminalFontConfiguration.BoldMode.EVERYTHING_BUT_SYMBOLS, font, new Font("Monospaced", Font.PLAIN, font.getSize()));
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

    public static Font getDefaultFont() {
        return new Font("Courier New", Font.PLAIN, getFontSize());
    }
}
