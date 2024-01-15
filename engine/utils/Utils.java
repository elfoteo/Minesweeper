package engine.utils;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

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
     * <p>The cursor is hidden by placing a character at the cursor position.
     * This causes the cursor to invert the color, making it black and effectively hiding it.</p>
     *
     * @param cursorX       The x-coordinate of the cursor position.
     * @param cursorY       The y-coordinate of the cursor position.
     * @param textGraphics  The TextGraphics object used for rendering on the screen.
     */
    public static void hideCursor(int cursorX, int cursorY, TextGraphics textGraphics) {
        textGraphics.setCharacter(
                cursorX,
                cursorY,
                new TextCharacter(textGraphics.getCharacter(cursorX, cursorY).getCharacter(), TextColor.ANSI.BLACK, TextColor.ANSI.WHITE)
        );
    }

    public static void drawRect(int x, int y, int width, int height, TextGraphics textGraphics) {
        // Draw top and bottom borders
        textGraphics.putString(x, y, "+"+"-".repeat(width-2)+"+");
        textGraphics.putString(x, y + height - 1, "+"+"-".repeat(width-2)+"+");

        // Draw left and right borders
        String symbol = "|";
        for (int i = 1; i < height - 1; i++) {
            textGraphics.putString(x, y + i, symbol);
            textGraphics.putString(x + width - 1, y + i, symbol);
        }
    }

    public static String toCamelCase(String input){
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public static String getGameTimerText(long seconds, long minutes, long elapsedTime) {
        long remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes);

        boolean showColon = (elapsedTime % 1000 < 500);

        String title;
        // String format pattern: "%02d %02d"
        // - %02d: Represents an integer with a minimum width of 2 digits.
        // - Space: Adds a space between the two numbers.
        if (showColon) {
            title = "Time: " + String.format("%02d:%02d", minutes, remainingSeconds);
        } else {
            title = "Time: " + String.format("%02d %02d", minutes, remainingSeconds);
        }
        return title;
    }

    public static void displaySidebarMessage(TextGraphics textGraphics, int line, String label, String content) {
        int width = textGraphics.getSize().getColumns();
        String message = String.format(label, content) + " ".repeat(width - (label.length() + content.length()));
        textGraphics.putString(0, line, message);
    }
}
