package engine.options;

import engine.utils.Constants;
import engine.utils.FontManager;
import engine.utils.JsonFont;
import engine.utils.Utils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Options {
    public static OptionsInstance readOptionsFromFile() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(Constants.optionsFile)));
            JSONObject json = new JSONObject(content);
            return new OptionsInstance(
                    json.getString("username"),
                    json.getBoolean("grayOutNearbyCells"),
                    JsonFont.fromString(json.get("font").toString())
            );
        } catch (Exception e) {
            // Any error loading the config create a new one from default
            OptionsInstance oi = new OptionsInstance(
                    "null",
                    true,
                    new JsonFont(FontManager.getDefaultFont())
            );
            // Save it
            saveOptionsToFile(oi);
            // Return it
            return oi;
        }
    }

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
