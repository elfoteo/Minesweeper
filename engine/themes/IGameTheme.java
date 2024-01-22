package engine.themes;

import com.googlecode.lanterna.TextColor;

import java.io.Serializable;

public interface IGameTheme extends Serializable {
    TextColor getForegroundColor();

    TextColor getBackgroundColor();
    String getThemeName();

    TextColor getMinefieldFore();
    TextColor getMinefieldBack();
    TextColor getWarningColor(int warningLevel, boolean grayedOut);
}
