package engine.themes;

import com.googlecode.lanterna.TextColor;
import engine.utils.Utils;

import java.awt.*;
import java.io.Serializable;

// Abstract because so it can't be instanced, implements IGameTheme because is a theme,
// Implements serializable because needs to be serialized
public abstract class BaseGameTheme implements IGameTheme, Serializable {
    private final Color foregroundColor = Utils.TextColorToAwtColor(TextColor.ANSI.WHITE);
    private final Color backgroundColor = Utils.TextColorToAwtColor(TextColor.ANSI.DEFAULT);

    @Override
    public TextColor getForegroundColor() {
        return Utils.ColorToTextColor(foregroundColor);
    }

    @Override
    public TextColor getBackgroundColor() {
        return Utils.ColorToTextColor(backgroundColor);
    }

    @Override
    public String getThemeName() {
        return "Default";
    }

    @Override
    public TextColor getMinefieldFore() {
        return TextColor.ANSI.WHITE;
    }

    @Override
    public TextColor getMinefieldBack() {
        return TextColor.ANSI.BLACK;
    }

    public double getSaturation(boolean grayedOut){
        // If grayedOut then 0.4 else 0.8
        return grayedOut? 0.3 : 0.8;
    }

    @Override
    public TextColor getWarningColor(int warningLevel, boolean grayedOut) {
        double saturation = getSaturation(grayedOut);
        Color c = switch (warningLevel) {
            case 1 -> new Color(0, 255, 0);
            case 2 -> new Color(128, 255, 0);
            case 3 -> new Color(191, 255, 0);
            case 4 -> new Color(255, 200, 50);
            case 5 -> new Color(255, 128, 0);
            case 6 -> new Color(255, 64, 0);
            case 7 -> new Color(255, 32, 0);
            case 8 -> new Color(200, 0, 0);
            default -> throw new IllegalArgumentException("The cell warning level ranges from 1 to 8, given \""+warningLevel+"\"");
        };
        return new TextColor.RGB((int) (c.getRed()*saturation), (int) (c.getGreen()*saturation), (int) (c.getBlue()*saturation));
    }
}
