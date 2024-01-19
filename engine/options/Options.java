package engine.options;

import engine.utils.Constants;
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
                    json.getBoolean("grayOutNearbyCells")
            );
        } catch (IOException e) {
            return null;
        }
    }

    public static void saveOptionsToFile(OptionsInstance optionsInstance) {
        JSONObject json = new JSONObject();
        json.put("username", optionsInstance.getUsername());
        json.put("grayOutNearbyCells", optionsInstance.isGrayOutNearbyCells());

        try {
            Files.write(Paths.get(Constants.optionsFile), json.toString().getBytes());
        } catch (IOException ignored) {}
    }
}
