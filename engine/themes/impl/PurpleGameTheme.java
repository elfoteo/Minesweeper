package engine.themes.impl;

import com.googlecode.lanterna.TextColor;
import engine.themes.IGameTheme;
import engine.utils.Utils;

import java.awt.*;
import java.io.Serializable;

public class PurpleGameTheme implements IGameTheme, Serializable {
    private final Color foregroundColor = Utils.TextColorToAwtColor(new TextColor.RGB(180, 40, 255));
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
