package engine.themes;

import com.googlecode.lanterna.TextColor;

import java.awt.*;
import java.io.Serializable;

public interface GameTheme extends Serializable {
    TextColor getForegroundColor();

    TextColor getBackgroundColor();
    String getThemeName();
}
