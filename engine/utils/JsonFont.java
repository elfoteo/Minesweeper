package engine.utils;

import org.json.JSONObject;

import java.awt.*;
import java.io.File;

public class JsonFont {
    private final Font font;
    private String fromFile = null;

    public JsonFont(Font font) {
        this.font = font;
    }

    public void setFromFile(String file){
        fromFile = file;
    }

    public Font getFont(){
        return font;
    }

    public String getFile(){
        return fromFile;
    }

    public static Font jsonToFont(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        String family = json.getString("family");
        String fromFile = null;
        try{
            fromFile = json.getString("from-file");
        }
        catch (Exception ignore){

        }
        int size = json.getInt("size");
        try{
            // If there is a file load it from the file
            if (fromFile != null){
                return Font.createFont(Font.TRUETYPE_FONT, new File(Constants.fontsDir+fromFile)).deriveFont((float)size);
            }
        }
        catch (Exception ignore){

        }
        return new Font(family, Font.PLAIN, size);
    }

    public static JsonFont fromString(String jsonStr) {
        Font font;
        JSONObject json = new JSONObject(jsonStr);
        String family = json.getString("family");
        String fromFile = null;
        JsonFont jsonFont;
        try{
            fromFile = json.getString("from-file");
        }
        catch (Exception ignore){

        }
        int size = json.getInt("size");
        try{
            // If there is a file load it from the file
            if (fromFile != null){
                font = Font.createFont(Font.TRUETYPE_FONT, new File(Constants.fontsDir+fromFile)).deriveFont((float)size);
            }
            else{
                font = new Font(family, Font.PLAIN, size);
            }
        }
        catch (Exception ignore){
            font = new Font(family, Font.PLAIN, size);
        }
        jsonFont = new JsonFont(font);
        jsonFont.setFromFile(fromFile);

        //return new JsonFont(jsonToFont(font));
        return jsonFont;
    }

    public JSONObject fontToJson() {
        JSONObject json = new JSONObject();
        json.put("family", font.getName());
        json.put("from-file", fromFile);
        json.put("size", font.getSize());
        return json;
    }
}
