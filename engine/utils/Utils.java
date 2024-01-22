package engine.utils;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import engine.Leaderboard;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Utils {
    /**
     * Returns the maximum string length of the array.
     *
     * <p>If the input array is or empty, returns 0.</p>
     *
     * @param array An array of strings.
     * @return The maximum length among the strings in the array, or 0 if the array is {@code null} or empty.
     */
    public static int getMaxStringLength(String[] array) {
        if (array == null || array.length == 0) {
            return 0;
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

    /**
     * Displays a debug message in a pop-up window.
     *
     * <p>This method shows a debug message in a pop-up using a JOptionPane.</p>
     *
     * @param message The debugging message to be displayed.
     */
    public static void Debug(Object message) {
        JOptionPane.showMessageDialog(null, message.toString());
    }
    public static String exceptionToString(Exception ex){
        // Construct the crash report message with stack trace
        StringBuilder crashReport = new StringBuilder("Crash Report:\n\n");
        crashReport.append("An unexpected error occurred.\n");
        crashReport.append("Error details:\n");
        crashReport.append(ex);
        crashReport.append("\n\nStack Trace:\n");

        // Append each line of the stack trace to the crash report
        for (StackTraceElement element : ex.getStackTrace()) {
            crashReport.append(element.toString()).append("\n");
        }
        return crashReport.toString();
    }

    /**
     * Get RGB values for a shifting rainbow effect based on elapsed time and position.
     *
     * @param elapsedTime  The elapsed time in milliseconds.
     * @param i            The position index for adjusting the effect individually.
     * @return An array of RGB values representing the color.
     */
    public static int[] getRainbow(long elapsedTime, int i) {
        // How rainbow effect works.
        // We have a "hue" variable that is the angle as HSB works with an angle, saturation, lightness
        // Rotating the angle
        // and keeping constant the saturation and the brightness
        // we can achieve a simple rainbow effect

        // Calculate the hue based on elapsed time and position
        float hue = (float) ((elapsedTime / 5000.0 - i * 0.05) % 360.0);

        // Set saturation and brightness
        // final because they don't get changed
        final float saturation = 1F;
        final float brightness = 1F;

        // Create a Color object using HSB color model
        Color c = Color.getHSBColor(hue, saturation, brightness);

        // Extract RGB values from the Color object and return them
        return new int[]{c.getRed(), c.getGreen(), c.getBlue()};
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
     * The cursor is hidden by placing a character at the cursor position with inverted colors.
     * This causes the cursor to invert the colors again, so the cursor is hidden.
     *
     * @param cursorX       The x-coordinate of the cursor position.
     * @param cursorY       The y-coordinate of the cursor position.
     * @param textGraphics  The TextGraphics object used for rendering on the screen.
     */
    public static void hideCursor(int cursorX, int cursorY, TextGraphics textGraphics) {
        TextCharacter tc = textGraphics.getCharacter(cursorX, cursorY);
        textGraphics.setCharacter(
                cursorX,
                cursorY,
                new TextCharacter(
                        tc.getCharacter(),
                        tc.getBackgroundColor(),
                        tc.getForegroundColor())
        );
    }

    public static Color calculateGradientColor(Color color1, Color color2, Color color3, double percentage) {
        // Ensure percentage is within the valid range [0, 1]
        percentage = Math.max(0, Math.min(1, percentage));

        // Calculate the individual components of the resulting color
        int red = (int) (color1.getRed() + percentage * (color2.getRed() - color1.getRed()) + percentage * (color3.getRed() - color2.getRed()));
        int green = (int) (color1.getGreen() + percentage * (color2.getGreen() - color1.getGreen()) + percentage * (color3.getGreen() - color2.getGreen()));
        int blue = (int) (color1.getBlue() + percentage * (color2.getBlue() - color1.getBlue()) + percentage * (color3.getBlue() - color2.getBlue()));

        // Create and return the resulting color
        return new Color(red, green, blue);
    }

    /**
     * Sorts a list of Users based on difficulty, score, and time.
     * Sorting order:
     * 1. Difficulties are sorted from "Hard" to "Easy."
     * 2. Scores are sorted in descending order (from highest to lowest).
     * 3. Times are sorted in ascending order (from lowest to highest).
     *
     * @param users The list of Leaderboard.Users to be sorted.
     * @return A new list containing the sorted Leaderboard.Users.
     */
    public static java.util.List<Leaderboard.User> sortUsers(List<Leaderboard.User> users){
        return users.stream()
                .sorted(
                        // Difficulties are stored from Easy to Hard.
                        // To sort them from "Hard" to "Easy" multiply by -1 them so "Easy" becomes bigger than "Hard"
                        Comparator.comparingInt(
                                        (Leaderboard.User u) -> u.difficulty().ordinal()*-1
                                )
                                .thenComparingInt(u -> u.score()*-1) // Descending score order
                                .thenComparing(Leaderboard.User::time)).collect(Collectors.toList()); // Ascending time order
    }

    public static TextColor ColorToTextColor(Color color){
        return new TextColor.RGB(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color TextColorToAwtColor(TextColor textColor){
        return new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue());
    }

    public static void drawRect(int x, int y, int width, int height, TextGraphics textGraphics) {
        // Draw top and bottom borders
        textGraphics.putString(x, y, "\u250C"+"\u2500".repeat(width-2)+"\u2510");
        textGraphics.putString(x, y + height - 1, "\u2514"+"\u2500".repeat(width-2)+"\u2518");

        // Draw left and right borders
        String symbol = "\u2502";
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

    /**
     * Normalizes a double value within a specific range.
     *
     * @param n   The input value to be normalized.
     * @param min The minimum value of the range.
     * @param max The maximum value of the range.
     * @return The normalized value within the specified range.
     */
    public static double normalize(double n, double min, double max) {
        return Math.max(min, Math.min(max, n));
    }
}
