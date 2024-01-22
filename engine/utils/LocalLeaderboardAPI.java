package engine.utils;

import engine.Leaderboard;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class LocalLeaderboardAPI {
    private JSONObject data;

    public LocalLeaderboardAPI(){
        // Init data json by reading to the file
        loadData();
    }

    public void saveChanges() throws IOException {
        Files.write(Paths.get(Constants.localLeaderboardFile), data.toString().getBytes(),
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    private void loadData(){
        try {
            String content = new String(Files.readAllBytes(Paths.get(Constants.localLeaderboardFile)));
            data = new JSONObject(content);
            // Check if "users" is an array
            if (!(data.has("users") && data.get("users") instanceof JSONArray))
            {
                // "Users" is not an array or does not exist.
                // Throws an exception and gets handled by the block below
                throw new JSONException("Value 'users' is not an array.");
            }
        } catch (IOException | JSONException e) {
            data = new JSONObject();
            // Set users to empty json if data loading fails
            data.put("users", new JSONArray());
        }
    }

    public void addUser(Leaderboard.User user) {
        // Create an empty JSONArray or get the existing one
        JSONArray usersArray;
        if (data.has("users") && data.get("users") instanceof JSONArray) {
            usersArray = data.getJSONArray("users");
        } else {
            usersArray = new JSONArray();
            data.put("users", usersArray);
        }

        // Create a JSONObject representing a user
        JSONObject jsonUser = new JSONObject();
        jsonUser.put("username", user.username());
        jsonUser.put("score", user.score());
        jsonUser.put("time", user.time());
        jsonUser.put("difficulty", user.difficulty().toString());

        // Add the new user to the "users" array
        usersArray.put(jsonUser);
        // Keep the list small, keep only the best results
        List<Leaderboard.User> userList = jsonArrayToUserList();
        userList = Utils.sortUsers(userList);
        // Remove all items after the fifth
        userList = userList.subList(0, 5);
        // Convert back to json
        data.put("users", UserListToJsonArray(userList));
        try{
            saveChanges();
        }
        catch (IOException ignore){}
    }

    public static JSONArray UserListToJsonArray(List<Leaderboard.User> list) {
        JSONArray jsonArray = new JSONArray();

        for (Leaderboard.User user : list) {
            JSONObject userObject = new JSONObject();
            userObject.put("username", user.username());
            userObject.put("score", user.score());
            userObject.put("time", user.time());
            userObject.put("difficulty", user.difficulty().name());

            jsonArray.put(userObject);
        }

        return jsonArray;
    }

    public List<Leaderboard.User> jsonArrayToUserList() {
        List<Leaderboard.User> userList = new ArrayList<>();

        JSONArray usersArray = data.getJSONArray("users");

        for (int i = 0; i < usersArray.length(); i++) {
            JSONObject userObject = usersArray.getJSONObject(i);

            String username = userObject.getString("username");
            int score = userObject.getInt("score");
            String time = userObject.getString("time");
            MinesweeperDifficulty difficulty = MinesweeperDifficulty.valueOf((String) userObject.get("difficulty"));
            userList.add(new Leaderboard.User(username, score, time, difficulty));
        }

        return userList;
    }
    public String getRaw(){
        // Return all user's data as raw json
        return data.toString();
    }
}
