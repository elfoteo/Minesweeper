package engine.themes.impl;

import com.googlecode.lanterna.TextColor;
import engine.themes.BaseGameTheme;
import engine.utils.Utils;

import java.awt.*;

public class WhiteGameTheme extends BaseGameTheme {
    private final Color foregroundColor = Utils.TextColorToAwtColor(new TextColor.RGB(0, 0, 0));
    private final Color backgroundColor = Utils.TextColorToAwtColor(new TextColor.RGB(255, 255, 255));

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
        return "White";
    }

    @Override
    public TextColor getMinefieldFore() {
        return new TextColor.RGB(0, 0, 0);
    }

    @Override
    public TextColor getMinefieldBack() {
        return Utils.ColorToTextColor(backgroundColor);
    }
}
