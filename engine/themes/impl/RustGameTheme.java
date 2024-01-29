package engine.themes.impl;

import com.googlecode.lanterna.TextColor;
import engine.themes.BaseGameTheme;
import engine.utils.Utils;

import java.awt.*;

public class RustGameTheme extends BaseGameTheme {
    private final Color foregroundColor = Utils.TextColorToAwtColor(new TextColor.RGB(227, 112, 30));
    private final Color backgroundColor = Utils.TextColorToAwtColor(new TextColor.RGB(8, 4, 1));

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
        return "Rust";
    }

    @Override
    public TextColor getMinefieldFore() {
        return new TextColor.RGB(166, 85, 27);
    }

    @Override
    public TextColor getMinefieldBack() {
        return Utils.ColorToTextColor(backgroundColor);
    }
}
