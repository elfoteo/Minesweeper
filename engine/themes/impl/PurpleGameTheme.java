package engine.themes.impl;

import com.googlecode.lanterna.TextColor;
import engine.themes.BaseGameTheme;
import engine.utils.Utils;

import java.awt.*;

public class PurpleGameTheme extends BaseGameTheme {
    private final Color foregroundColor = Utils.TextColorToAwtColor(new TextColor.RGB(184, 51, 255));
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
        return "Purple";
    }

    @Override
    public TextColor getMinefieldFore() {
        return TextColor.ANSI.BLUE_BRIGHT;
    }

    @Override
    public TextColor getWarningColor(int warningLevel, boolean grayedOut) {
        double saturation = getSaturation(grayedOut);
        Color c = switch (warningLevel) {
            case 1 -> new Color(174, 33, 255);
            case 2 -> new Color(139, 26, 204);
            case 3 -> new Color(121, 23, 178);
            case 4 -> new Color(104, 19, 153);
            case 5 -> new Color(87, 16, 127);
            case 6 -> new Color(78, 14, 114);
            case 7 -> new Color(69, 13, 102);
            case 8 -> new Color(60, 11, 89);
            default -> throw new IllegalArgumentException("The cell warning level ranges from 1 to 8, given \"" + warningLevel + "\"");
        };
        return new TextColor.RGB((int) (c.getRed()*saturation), (int) (c.getGreen()*saturation), (int) (c.getBlue()*saturation));
    }
}
