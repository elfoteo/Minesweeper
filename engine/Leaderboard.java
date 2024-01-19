package engine;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import engine.utils.Constants;
import engine.utils.MinesweeperDifficulty;
import engine.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Leaderboard {
    private final TextGraphics textGraphics;
    private final Screen screen;

    public Leaderboard(Screen screen, TextGraphics textGraphics){
        this.screen = screen;
        this.textGraphics = textGraphics;
        // If the computer has a proxy set, we need to use that proxy
        System.setProperty("java.net.useSystemProxies", "true");
    }
    public void displayLeaderboard() throws IOException {
        screen.clear();
        textGraphics.putString(0, 0, "Loading data...");
        screen.refresh();
        String raw;
        List<User> users;
        try{
            raw = getRawData();
            users = parseJson(raw);
            if (users != null){
                users = users.stream()
                        .sorted(
                                // Difficulties are stored from Easy to Hard.
                                // To sort them from Hard to Easy multiply by -1 them so Easy becomes bigger then Hard
                                Comparator.comparingInt(
                                    (User u) -> u.difficulty.ordinal()*-1
                                )
                                .thenComparingInt(u -> u.score*-1) // Descending score order
                                .thenComparing(u -> u.time)).collect(Collectors.toList()); // Ascending time order
            }
        }
        catch (Exception ignore){
            // Catch any exception because it will probably be an internet error
            // Set users to null to display internet error
            users = null;
        }
        screen.clear();

        boolean running = true;
        while (running) {
            // Add logo
            int x = Utils.getMaxStringLength(Constants.logo);
            int y = 1;
            for (String logoLine : Constants.leaderboardLogo) {
                textGraphics.putString(screen.getTerminalSize().getColumns() / 2 - x / 2, y, logoLine);
                y++;
            }
            // Add border
            textGraphics.putString(0, 0, "#".repeat(screen.getTerminalSize().getColumns()));
            textGraphics.putString(0, screen.getTerminalSize().getRows()-1, "#".repeat(screen.getTerminalSize().getColumns()));
            for (int i = 0;i < screen.getTerminalSize().getRows();i++){
                textGraphics.putString(0, i, "#");
                textGraphics.putString(screen.getTerminalSize().getColumns()-1, i, "#");
            }
            // Hide cursor
            Utils.hideCursor(screen.getCursorPosition().getColumn(), screen.getCursorPosition().getRow(), textGraphics);

            int width = 40;
            int height = 12;
            x = screen.getTerminalSize().getColumns()/2-width/2;
            y = screen.getTerminalSize().getRows()/2-height/2+3;

            Utils.drawRect(x, y, width, height, textGraphics);

            if (users == null){
                String title = "Connection error";
                textGraphics.putString(x+width/2-title.length()/2, y+height/2-1, title);
            }
            else{
                String fs = "   " + String.format("%-" + 12 + "s%-" + 7 + "s%-" + 7 + "s%s", "Username", "Score", "Time", "Level");
                textGraphics.putString(x+2, y+1, fs);
                int n = 1;
                for (User user : users){
                    String formattedString = n + ") " + String.format("%-" + 12 + "s%-" + 7 + "d%-" + 7 + "s%s",
                            user.username, user.score, user.time, Utils.toCamelCase(user.difficulty.name()));

                    textGraphics.putString(x+2, y+n*2+1, formattedString);
                    // Only show top 3 players
                    if (n > 3){
                        break;
                    }
                    n++;
                }
            }

            screen.refresh();
            KeyStroke choice = screen.readInput();
            if (choice.getKeyType() == KeyType.Escape || choice.getKeyType() == KeyType.EOF) {
                break;
            }
        }
        screen.clear();
    }

    private static List<User> parseJson(String jsonString) {
        // Parse the JSON string
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray usersArray = jsonObject.getJSONArray("users");

        // Create an array to store User objects
        User[] users = new User[usersArray.length()];

        // Iterate through the JSON array and create User objects
        for (int i = 0; i < usersArray.length(); i++) {
            JSONObject userObject = usersArray.getJSONObject(i);
            String username = userObject.getString("username");
            int score = userObject.getInt("score");
            String time = userObject.getString("time");
            String difficultyStr = userObject.getString("difficulty");
            MinesweeperDifficulty difficulty = MinesweeperDifficulty.valueOf(difficultyStr.toUpperCase());

            users[i] = new User(username, score, time, difficulty);
        }
        return List.of(users);
    }
    private String getRawData(){
        try {
            // Create a URL object
            URL url = new URL(Constants.apiUrl+"/raw");

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

    public void sendPlayerDataAsync(User playerData){
        // From: https://www.geeksforgeeks.org/completablefuture-in-java/
        CompletableFuture.runAsync(() -> sendPlayerData(playerData));
    }

    public void sendPlayerData(User playerData){
        try{
            // URL of the API
            URL url = new URL(Constants.apiUrl+"/submit");

            // Open a connection to the URL
            HttpURLConnection connection = sendPlayerDataToAPI(url, playerData);

            // Close the connection
            connection.disconnect();
        }
        catch (Exception ex){
            // Probably internet error, ignore
        }
    }
    /**
     * Sends player data to the specified API using HTTP POST.
     *
     * @param url        The URL of the API endpoint.
     * @param playerData The player data to be sent.
     * @return           The HttpURLConnection object representing the connection to the API.
     * @throws IOException If an I/O exception occurs while making the connection.
     */
    private static HttpURLConnection sendPlayerDataToAPI(URL url, User playerData) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method to POST !important
        connection.setRequestMethod("POST");

        // Set the content type to JSON !important
        connection.setRequestProperty("Content-Type", "application/json");

        // Enable input/output streams
        connection.setDoOutput(true);

        // Set the request payload (JSON data)
        String jsonInputString = String.format(
                "{\"username\":\"%s\",\"score\":%d,\"time\":\"%s\",\"difficulty\":\"%s\"}",
                playerData.username, playerData.score, playerData.time, playerData.difficulty);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Response must be read or else the server won't respond
        try (InputStream is = connection.getInputStream()) {
            String responseMessage = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("Response from the server: " + responseMessage);
        } catch (IOException e) {
            System.out.println("Error reading the response: " + e.getMessage());
        }

        return connection;
    }


    public record User(String username, int score, String time, MinesweeperDifficulty difficulty) {
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
