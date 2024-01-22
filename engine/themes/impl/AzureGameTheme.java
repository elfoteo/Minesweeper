package engine.themes.impl;

import com.googlecode.lanterna.TextColor;
import engine.themes.BaseGameTheme;
import engine.utils.Utils;

import java.awt.*;

public class AzureGameTheme extends BaseGameTheme {
    private final Color foregroundColor = Utils.TextColorToAwtColor(new TextColor.RGB(0, 255, 234));
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
        return "Azure";
    }

}
