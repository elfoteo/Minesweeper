package engine.utils;

import org.json.JSONObject;

import java.awt.*;

public record JsonFont(Font font) {
    public static Font jsonToFont(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        String family = json.getString("family");
        int size = json.getInt("size");
        return new Font(family, Font.PLAIN, size);
    }

    public static JsonFont fromString(String font) {
        return new JsonFont(jsonToFont(font));
    }

    public JSONObject fontToJson() {
        JSONObject json = new JSONObject();
        json.put("family", font.getFamily());
        json.put("size", font.getSize());
        return json;
    }
}
