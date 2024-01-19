package engine.themes.impl;

import com.googlecode.lanterna.TextColor;
import engine.themes.IGameTheme;
import engine.utils.Utils;

import java.awt.*;
import java.io.Serializable;

public class DefaultGameTheme implements IGameTheme, Serializable {
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
}
