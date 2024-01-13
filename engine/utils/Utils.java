package engine.utils;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

import javax.swing.*;
import java.io.IOException;

public class Utils {
    public static int getMaxStringLength(String[] array) {
        if (array == null || array.length == 0) {
            return 0; // or throw an exception, depending on your requirements
        }

        int maxLength = array[0].length();

        for (String logo : array) {
            int currentLength = logo.length();
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        return maxLength;
    }

    public static void Debug(String message){
        JOptionPane.showMessageDialog(null, message);
    }

    public static void WriteDebug(TextGraphics graphics, Screen screen, String string) throws IOException {
        graphics.putString(0, screen.getTerminalSize().getRows()-1, string);
        screen.refresh();
    }

    public static void WriteDebug(TextGraphics graphics, Screen screen, char string) throws IOException {
        graphics.putString(0, screen.getTerminalSize().getRows()-1, string+"");
        screen.refresh();
    }

    /**
     * Converts a given difficulty in to information
     *
     * @param difficulty The difficulty level of the Minesweeper game.
     * @return A tuple containing the mine count and the grid dimensions (rows x columns).
     */
    public static Tuple<Integer, Tuple<Integer, Integer>> getDifficultyInfo(MinesweeperDifficulty difficulty){
        switch (difficulty){
            case EASY -> {
                return new Tuple<>(5, new Tuple<>(8, 8));
            }
            case MEDIUM -> {
                return new Tuple<>(14, new Tuple<>(12, 12));
            }
            case HARD -> {
                return new Tuple<>(52, new Tuple<>(18, 18));
            }
        }
        return new Tuple<>(5, new Tuple<>(8, 8));
    }

    /**
     * Hides the cursor at the specified position on the screen.
     *
     * <p>The cursor is hidden by placing a white space character at the specified position.
     * This causes the cursor to invert the color, making it black and effectively hiding it.</p>
     *
     * @param cursorX       The x-coordinate of the cursor position.
     * @param cursorY       The y-coordinate of the cursor position.
     * @param textGraphics  The TextGraphics object used for rendering on the screen.
     */
    public static void hideCursor(int cursorX, int cursorY, TextGraphics textGraphics) {
        textGraphics.setBackgroundColor(TextColor.ANSI.WHITE_BRIGHT);
        textGraphics.putString(cursorX, cursorY, " ");
        textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
    }
}
