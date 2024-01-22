package engine.themes.impl;

import com.googlecode.lanterna.TextColor;
import engine.themes.BaseGameTheme;
import engine.utils.Utils;

import java.awt.*;

public class DefaultGameTheme extends BaseGameTheme {
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
}
