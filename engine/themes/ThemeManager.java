package engine.themes;

import engine.UIManager;
import engine.themes.impl.DefaultGameTheme;
import engine.themes.impl.PurpleGameTheme;
import engine.utils.Utils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static List<GameTheme> gameThemeClasses = new ArrayList<>();

    public static List<GameTheme> getThemes() {
        return gameThemeClasses;
    }

    public static void registerThemes() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        gameThemeClasses.add(getInstanceFor(DefaultGameTheme.class));
        gameThemeClasses.add(getInstanceFor(PurpleGameTheme.class));
    }

    private static GameTheme getInstanceFor(Class<? extends GameTheme> toInstance) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return toInstance.getConstructor().newInstance();
    }

    // Method to save the selected Theme to a file
    // From: https://stackoverflow.com/questions/10654236/java-save-object-data-to-a-file
    public static void saveSelectedThemeToFile(String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(UIManager.selectedTheme);
            System.out.println("Selected Theme saved to file: " + filePath);
        } catch (IOException e) {
            Utils.Debug(Utils.exceptionToString(e));
        }
    }

    // Method to load the selected Theme from a file
    // From: https://stackoverflow.com/questions/10654236/java-save-object-data-to-a-file
    public static void loadSelectedThemeFromFile(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            UIManager.selectedTheme = (GameTheme) ois.readObject();
            System.out.println("Selected Theme loaded from file: " + filePath);
        } catch (IOException | ClassNotFoundException ignored) {}
    }
}