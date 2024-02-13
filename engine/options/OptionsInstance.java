package engine.options;

import engine.utils.Constants;
import engine.utils.FontManager;
import engine.utils.JsonFont;

import java.io.File;
import java.util.Objects;

/**
 * Class that stores the game options as an instance.
 */
public class OptionsInstance {
    private String username;
    private boolean grayOutNearbyCells;
    private JsonFont font;
    private int musicVolume;
    private String soundtrackFilePath;

    /**
     * Constructs an instance of OptionsInstance with the specified parameters.
     *
     * @param username           The username for the player. Must be between 3 and 10 characters long.
     * @param musicVolume        The music volume for the game. Should be between 0 and 100.
     * @param grayOutNearbyCells Determines whether nearby cells are grayed out.
     * @param font               The font settings for the game.
     */
    public OptionsInstance(String username, int musicVolume, boolean grayOutNearbyCells, JsonFont font, String soundtrackFilePath) {
        this.soundtrackFilePath = soundtrackFilePath;
        setUsername(username);
        setMusicVolume(musicVolume);
        this.grayOutNearbyCells = grayOutNearbyCells;
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
     * If the provided username is invalid, "-" will be set as the username.
     *
     * @param newUsername The new username to be set.
     */
    public void setUsername(String newUsername) {
        if (isUsernameValid(newUsername)) {
            username = newUsername;
        } else {
            username = "-";
        }
    }

    /**
     * Checks if the current username is valid.
     *
     * @return true if the username is valid (between 3 and 10 characters long), otherwise false.
     */
    public boolean isUsernameValid(){
        return isUsernameValid(username);
    }

    /**
     * Checks if the provided username is valid.
     *
     * @param username The username to be checked.
     * @return true if the username is valid (between 3 and 10 characters long), otherwise false.
     */
    public static boolean isUsernameValid(String username){
        return username.length() >= 3 && username.length() <= 10;
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

    /**
     * Gets the value of the music volume.
     *
     * @return the music volume.
     */
    public int getMusicVolume() {
        return musicVolume;
    }

    /**
     * Sets the music volume to a value.
     * The volume will be clamped between 0 and 100.
     *
     * @param musicVolume the new volume.
     */
    public void setMusicVolume(int musicVolume) {
        this.musicVolume = Math.max(0, Math.min(100, musicVolume));
    }

    public File getSoundtrackFile() {
        return new File(Constants.soundsDir+soundtrackFilePath);
    }

    public String getSoundtrackFilePath() {
        return soundtrackFilePath;
    }

    public void setSoundtrackFilePath(String soundtrackFilePath) {
        this.soundtrackFilePath = soundtrackFilePath;
    }
}
