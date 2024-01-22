package engine.skins;

import engine.UIManager;
import engine.skins.impl.*;
import engine.utils.Utils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class SkinManager {
    private static final List<ISkin> skinClasses = new ArrayList<>();

    public static List<ISkin> getSkins() {
        return skinClasses;
    }

    public static void registerSkins() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        skinClasses.add(getInstanceFor(DefaultSkin.class));
        skinClasses.add(getInstanceFor(MoneySkin.class));
        skinClasses.add(getInstanceFor(StarsSkin.class));
        skinClasses.add(getInstanceFor(HeartsSkin.class));
        skinClasses.add(getInstanceFor(MysterySkin.class));
    }

    private static ISkin getInstanceFor(Class<? extends ISkin> toInstance) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return toInstance.getConstructor().newInstance();
    }

    // Method to save the selected skin to a file
    // From: https://stackoverflow.com/questions/10654236/java-save-object-data-to-a-file
    public static void saveSelectedSkinToFile(String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(UIManager.selectedSkin);
            System.out.println("Selected skin saved to file: " + filePath);
        } catch (IOException e) {
            Utils.Debug(Utils.exceptionToString(e));
        }
    }

    // Method to load the selected skin from a file
    // From: https://stackoverflow.com/questions/10654236/java-save-object-data-to-a-file
    public static void loadSelectedSkinFromFile(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            UIManager.selectedSkin = (ISkin) ois.readObject();
            System.out.println("Selected skin loaded from file: " + filePath);
        } catch (IOException | ClassNotFoundException ignored) {
        }
    }
}