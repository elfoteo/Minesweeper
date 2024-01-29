package engine.themes.impl;

import com.googlecode.lanterna.TextColor;
import engine.themes.BaseGameTheme;
import engine.utils.Utils;

import java.awt.*;

public class BloodyGameTheme extends BaseGameTheme {
    private final Color foregroundColor = Utils.TextColorToAwtColor(new TextColor.RGB(192, 0, 0));
    private final Color backgroundColor = Utils.TextColorToAwtColor(new TextColor.RGB(12, 0, 0));

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
        return "Bloody";
    }

    @Override
    public TextColor getMinefieldFore() {
        return new TextColor.RGB(160, 0, 0);
    }

    @Override
    public TextColor getMinefieldBack() {
        return Utils.ColorToTextColor(backgroundColor);
    }

    @Override
    public TextColor getWarningColor(int warningLevel, boolean grayedOut) {
        double saturation = getSaturation(grayedOut);
        Color c = switch (warningLevel) {
            case 1 -> new Color(127, 3, 1);
            case 2 -> new Color(165, 4, 1);
            case 3 -> new Color(178, 4, 2);
            case 4 -> new Color(204, 5, 2);
            case 5 -> new Color(216, 5, 2);
            case 6 -> new Color(229, 6, 2);
            case 7 -> new Color(242, 6, 2);
            case 8 -> new Color(255, 7, 3);
            default -> throw new IllegalArgumentException("The cell warning level ranges from 1 to 8, given \"" + warningLevel + "\"");
        };
        return new TextColor.RGB((int) (c.getRed()*saturation), (int) (c.getGreen()*saturation), (int) (c.getBlue()*saturation));
    }
}
