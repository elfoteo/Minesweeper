package engine.options;

import engine.utils.FontManager;
import engine.utils.JsonFont;

import java.util.Objects;

/**
 * Class that stores the game options as an instance.
 */
public class OptionsInstance {
    private String username;
    private boolean grayOutNearbyCells;
    private JsonFont font;

    /**
     * Constructs an instance of OptionsInstance with the specified parameters.
     *
     * @param username           The username for the player. Must be between 3 and 10 characters long.
     * @param grayOutNearbyCells Determines whether nearby cells are grayed out.
     * @param font               The font settings for the game.
     */
    public OptionsInstance(String username, boolean grayOutNearbyCells, JsonFont font) {
        if (username.length() < 10 && username.length() > 3){
            this.username = username;
        }
        else{
            this.username = "-";
        }
        this.grayOutNearbyCells = grayOutNearbyCells;
        // Intellij suggestion
        this.font = Objects.requireNonNullElseGet(font, () -> new JsonFont(FontManager.getDefaultFont()));
    }

    /**
     * Checks if nearby cells are grayed out.
     *
     * @return true if nearby cells are grayed out, otherwise false.
     */
    public boolean isGrayOutNearbyCells() {
        return grayOutNearbyCells;
    }

    /**
     * Sets whether nearby cells should be grayed out.
     *
     * @param grayOutNearbyCells Determines whether nearby cells are grayed out.
     */
    public void setGrayOutNearbyCells(boolean grayOutNearbyCells) {
        this.grayOutNearbyCells = grayOutNearbyCells;
    }

    /**
     * Gets the username of the player.
     *
     * @return The username of the player.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the player.
     *
     * @param newUsername The new username to be set.
     */
    public void setUsername(String newUsername) {
        // Invalid usernames cannot be set
        if (isUsernameValid(newUsername)){
            username = newUsername;
        }
    }

    /**
     * Checks if the current username is valid.
     *
     * @return true if the username is valid, otherwise false.
     */
    public boolean isUsernameValid(){
        return isUsernameValid(username);
    }

    /**
     * Checks if the provided username is valid.
     *
     * @param username The username to be checked.
     * @return true if the username is valid, otherwise false.
     */
    public static boolean isUsernameValid(String username){
        return username.length() < 10 && username.length() > 3;
    }

    /**
     * Gets the font settings for the game.
     *
     * @return The font settings for the game.
     */
    public JsonFont getJsonFont() {
        return font;
    }

    /**
     * Sets the font settings for the game.
     *
     * @param font The font settings to be set.
     */
    public void setFont(JsonFont font) {
        this.font = font;
    }
}
