package engine.skins;

import engine.UIManager;
import engine.skins.impl.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the registration and loading of skins for the game.
 */
public class SkinManager {
    private static final List<ISkin> registeredSkins = new ArrayList<>();

    /**
     * Returns the list of registered skins.
     *
     * @return The list of registered skins.
     */
    public static List<ISkin> getSkins() {
        return registeredSkins;
    }

    /**
     * Registers the default set of skins for the game.
     */
    public static void registerSkins() {
        register(DefaultSkin.class);
        register(MoneySkin.class);
        register(StarsSkin.class);
        register(HeartsSkin.class);
        register(MysterySkin.class);
        register(ASCIISkin.class);
        register(DiamondSkin.class);
        register(PixelSkin.class);
    }

    /**
     * Registers a skin class.
     *
     * @param skin The skin class to register.
     */
    private static void register(Class<? extends ISkin> skin) {
        try {
            registeredSkins.add(getInstanceFor(skin));
        } catch (Exception ignored) {
            // If a skin fails registering just ignore
        }
    }

    /**
     * Instantiates a skin class and returns an instance of it.
     *
     * @param toInstance The skin class to instantiate.
     * @return An instance of the specified skin class.
     * @throws NoSuchMethodException If the constructor of the skin class is not found.
     */
    private static ISkin getInstanceFor(Class<? extends ISkin> toInstance) throws NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        return toInstance.getConstructor().newInstance();
    }

    /**
     * Saves the selected skin to a file.
     *
     * @param filePath The file path where the selected skin will be saved.
     */
    public static void saveSelectedSkinToFile(String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(UIManager.selectedSkin);
        } catch (Exception ignored) {
        }
    }

    /**
     * Loads a skin from a given file.
     *
     * @param filePath The file path from where the selected skin will be loaded.
     */
    public static void loadSelectedSkinFromFile(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            UIManager.selectedSkin = (ISkin) ois.readObject();
        } catch (Exception ignored) {
        }
    }
}
