package engine.options;

import engine.utils.Constants;
import engine.utils.FontManager;
import engine.utils.JsonFont;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility class for managing game options.
 */
public class Options {

    /**
     * Reads game options from a JSON file.
     *
     * @return The options instance read from the file, or a default options instance if an error occurs.
     */
    public static OptionsInstance readOptionsFromFile() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(Constants.optionsFile)));
            JSONObject json = new JSONObject(content);
            return new OptionsInstance(
                    json.getString("username"),
                    json.getBoolean("grayOutNearbyCells"),
                    JsonFont.fromString(json.get("font").toString())
            );
        } catch (IOException | org.json.JSONException e) {
            // Any error loading the config create a new one from default
            OptionsInstance oi = getDefaultOptions();
            // Save it
            saveOptionsToFile(oi);
            // Return it
            return oi;
        }
    }

    /**
     * Gets the default game options.
     *
     * @return The default options instance.
     */
    private static OptionsInstance getDefaultOptions(){
        return new OptionsInstance(
                "-",
                true,
                new JsonFont(FontManager.getDefaultFont())
        );
    }

    /**
     * Saves the given options instance to a JSON file.
     *
     * @param optionsInstance The option instance to be saved.
     */
    public static void saveOptionsToFile(OptionsInstance optionsInstance) {
        JSONObject json = new JSONObject();
        json.put("username", optionsInstance.getUsername());
        json.put("grayOutNearbyCells", optionsInstance.isGrayOutNearbyCells());
        json.put("font", optionsInstance.getJsonFont().fontToJson());
        try {
            Files.write(Paths.get(Constants.optionsFile), json.toString().getBytes());
        } catch (IOException ignored) {

        }
    }
}
