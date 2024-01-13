package engine;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import engine.utils.Constants;
import engine.utils.MinesweeperDifficulty;
import engine.utils.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Leaderboard {
    private final TextGraphics textGraphics;
    private final Screen screen;

    public Leaderboard(Screen screen, TextGraphics textGraphics){
        this.screen = screen;
        this.textGraphics = textGraphics;
    }
    public void displayLeaderboard(){
        String raw = getRawData();
        List<User> users = parseJson(raw);
        for (User user : users){
            Utils.Debug(user.toString());
        }
    }
    private static List<User> parseJson(String jsonString) {
        List<User> userList = new ArrayList<>();

        // Trim whitespace and remove curly braces
        jsonString = jsonString.trim().replaceAll("\\[\\{}]", "");

        // Split into key-value pairs
        String[] keyValuePairs = jsonString.split(",");

        String username = null;
        int score = 0;
        String time = null;
        MinesweeperDifficulty difficulty = null;

        for (String pair : keyValuePairs) {
            String[] entry = pair.split(":");

            String key = entry[0].trim().replaceAll("\"", "");
            String value = entry[1].trim().replaceAll("\"", "");

            switch (key) {
                case "username":
                    username = value;
                    break;
                case "score":
                    score = Integer.parseInt(value);
                    break;
                case "time":
                    time = value;
                    break;
                case "difficulty":
                    difficulty = MinesweeperDifficulty.valueOf(value.toUpperCase());
                    break;
            }
        }

        // Create a new User object and add it to the list
        userList.add(new User(username, score, time, difficulty));

        return userList;
    }
    private String getRawData(){
        try {
            // Create a URL object
            URL url = new URL(Constants.apiUrl);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Get the response code
            int responseCode = connection.getResponseCode();

            // Check if the request was successful (HTTP status code 200)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response from the input stream
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Close the reader
                reader.close();

                // Return the raw JSON data
                return response.toString();
            }

            // Close the connection
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static class User{
        public final String username;
        public final int score;
        public final String time;
        public final MinesweeperDifficulty difficulty;

        User(String username, int score, String time, MinesweeperDifficulty difficulty){
            this.username = username;
            this.score = score;
            this.time = time;
            this.difficulty = difficulty;
        }
        @Override
        public String toString() {
            return "User{" +
                    "username='" + username + '\'' +
                    ", score=" + score +
                    ", time='" + time + '\'' +
                    ", difficulty=" + difficulty +
                    '}';
        }
    }
}
