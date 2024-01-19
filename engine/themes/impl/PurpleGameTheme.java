package engine.themes.impl;

import com.googlecode.lanterna.TextColor;
import engine.themes.GameTheme;
import engine.utils.Utils;

import java.awt.*;
import java.io.Serializable;

public class PurpleGameTheme implements GameTheme, Serializable {
    private final Color foregroundColor = Utils.TextColorToAwtColor(new TextColor.RGB(120, 4, 180));
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
}
