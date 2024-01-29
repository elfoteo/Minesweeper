package engine.themes.impl;

import com.googlecode.lanterna.TextColor;
import engine.themes.BaseGameTheme;
import engine.utils.Utils;

import java.awt.*;

public class OceanGameTheme extends BaseGameTheme {
    private final Color foregroundColor = Utils.TextColorToAwtColor(new TextColor.RGB(44, 219, 232));
    private final Color backgroundColor = Utils.TextColorToAwtColor(new TextColor.RGB(8, 0, 56));

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
        return "Ocean";
    }

    @Override
    public TextColor getMinefieldFore() {
        return new TextColor.RGB(71, 159, 255);
    }

    @Override
    public TextColor getMinefieldBack() {
        return Utils.ColorToTextColor(backgroundColor);
    }
}
