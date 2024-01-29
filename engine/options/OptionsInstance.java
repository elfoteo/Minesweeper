package engine.options;

import engine.utils.FontManager;
import engine.utils.JsonFont;

import java.util.Objects;

public class OptionsInstance {
    private String username;
    private boolean grayOutNearbyCells;
    private JsonFont font;

    public OptionsInstance(String username, boolean grayOutNearbyCells, JsonFont font) {
        if (username.length() < 10){
            this.username = username;
        }
        else{
            this.username = "null";
        }
        this.grayOutNearbyCells = grayOutNearbyCells;
        // Intellij suggestion
        this.font = Objects.requireNonNullElseGet(font, () -> new JsonFont(FontManager.getDefaultFont()));
    }

    public boolean isGrayOutNearbyCells() {
        return grayOutNearbyCells;
    }

    public void setGrayOutNearbyCells(boolean grayOutNearbyCells) {
        this.grayOutNearbyCells = grayOutNearbyCells;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public JsonFont getJsonFont() {
        return font;
    }

    public void setFont(JsonFont font) {
        this.font = font;
    }
}
