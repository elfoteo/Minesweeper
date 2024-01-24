package engine.gui.impl;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import engine.Leaderboard;
import engine.UIManager;
import engine.gui.AbstractTerminalGUI;
import engine.utils.Constants;
import engine.utils.Utils;

import java.io.IOException;
import java.util.List;

public class LeaderboardGUI extends AbstractTerminalGUI {
    private final Leaderboard leaderboard;
    private final TextGraphics textGraphics;
    // uiShowingLocal, false if showing global leaderboard, true if showing local leaderboard
    private boolean uiShowingLocal = false;
    private List<Leaderboard.User> globalUsers;
    private List<Leaderboard.User> localUsers;

    /**
     * Constructor for the LeaderboardGUI.
     *
     * @param uiManager The UIManager giving access to the terminal and screen.
     */
    public LeaderboardGUI(UIManager uiManager){
        super(uiManager.getTerminal());
        this.leaderboard = uiManager.leaderboard;
        this.screen = uiManager.getScreen();
        this.textGraphics = uiManager.getTextGraphics();
    }

    @Override
    public void show() throws IOException {
        super.show();
        loadData();

        while (true) {
            // Draw
            draw();
            // Handle inputs
            KeyStroke choice = screen.readInput();
            if (choice.getKeyType() == KeyType.Escape || choice.getKeyType() == KeyType.EOF) {
                break;
            }
            if (choice.getKeyType() == KeyType.Tab){
                uiShowingLocal = !uiShowingLocal;
            }
            screen.clear();
        }
        onClose();
        screen.clear();
    }

    @Override
    public void draw() throws IOException {
        super.draw();
        // Add logo
        int x = Utils.getMaxStringLength(Constants.leaderboardLogo);
        int y = 1;
        for (String logoLine : Constants.leaderboardLogo) {
            textGraphics.putString(getTerminalWidth() / 2 - x / 2, y, logoLine);
            y++;
        }
        // Add border
        textGraphics.putString(0, 0, "#".repeat(getTerminalWidth()));
        textGraphics.putString(0, getTerminalHeight()-1, "#".repeat(getTerminalWidth()));
        for (int i = 0;i < getTerminalHeight();i++){
            textGraphics.putString(0, i, "#");
            textGraphics.putString(getTerminalWidth()-1, i, "#");
        }
        // Add helper text
        textGraphics.putString(1, getTerminalHeight()-2, "Use Tab to toggle between Local and Global leaderboards");
        // Hide cursor
        try{
            screen.setCursorPosition(new TerminalPosition(1, 1));
            Utils.hideCursor(screen.getCursorPosition().getColumn(), screen.getCursorPosition().getRow(), textGraphics);
        }
        catch (NullPointerException ignore){
            // Sometimes screen.getCursorPosition().getColumn()/.getRow() returns null
        }

        int width = 40;
        int height = 12;
        x = getTerminalWidth()/2-width/2;
        y = getTerminalHeight()/2-height/2+3;

        Utils.drawRect(x, y, width, height, textGraphics);

        if (uiShowingLocal){
            showLocalLeaderboard(localUsers, x, y, width, height);
        }
        else {
            showGlobalLeaderboard(globalUsers, x, y, width, height);
        }
        screen.refresh();
    }

    private void loadData() throws IOException {
        screen.clear();
        textGraphics.putString(0, 0, "Loading data...");
        screen.refresh();
        globalUsers = leaderboard.getGlobalUsers();
        localUsers = leaderboard.getLocalUsers();
        screen.clear();
    }

    private void displayEmptyLeaderboardMessage(int x, int y){
        textGraphics.putString(x+2, y+3, "No one has won a game yet.");
        textGraphics.putString(x+2, y+4, "Be the first!");
        textGraphics.putString(x+2, y+5, "To play go to the main menu and click");
        textGraphics.putString(x+2, y+6, "\"Play\"");
    }

    private void showGlobalLeaderboard(List<Leaderboard.User> globalUsers, int x, int y, int width, int height){
        String title = "Global Leaderboard";
        textGraphics.putString(x+width/2-title.length()/2, y+1, title);
        if (globalUsers == null){
            String subtitle = "Connection error";
            textGraphics.putString(x+width/2-subtitle.length()/2, y+height/2-1, subtitle);
        }
        else if (globalUsers.isEmpty()){
            displayEmptyLeaderboardMessage(x, y);
        }
        else{
            String fs = "   " + String.format("%-" + 12 + "s%-" + 7 + "s%-" + 7 + "s%s", "Username", "Score", "Time", "Level");
            textGraphics.putString(x+2, y+2, fs);
            int n = 1;

            for (Leaderboard.User user : globalUsers){
                String formattedString = n + ") " + String.format("%-" + 12 + "s%-" + 7 + "d%-" + 7 + "s%s",
                        user.username(), user.score(), user.time(), Utils.toCamelCase(user.difficulty().name()));

                textGraphics.putString(x+2, y+n*2+2, formattedString);
                // Only show top 3 players
                if (n > 3){
                    break;
                }
                n++;
            }
        }
    }

    private void showLocalLeaderboard(List<Leaderboard.User> localUsers, int x, int y, int width, int height){
        String title = "Local Leaderboard";
        textGraphics.putString(x+width/2-title.length()/2, y+1, title);
        if (localUsers == null){
            // This shouldn't happen
            String subtitle = "Unexpected error";
            textGraphics.putString(x+width/2-subtitle.length()/2, y+height/2-1, subtitle);
        }
        else if (localUsers.isEmpty()){
            displayEmptyLeaderboardMessage(x, y);
        }
        else{
            String fs = "   " + String.format("%-" + 12 + "s%-" + 7 + "s%-" + 7 + "s%s", "Username", "Score", "Time", "Level");
            textGraphics.putString(x+2, y+2, fs);
            int n = 1;

            for (Leaderboard.User user : localUsers){
                String formattedString = n + ") " + String.format("%-" + 12 + "s%-" + 7 + "d%-" + 7 + "s%s",
                        user.username(), user.score(), user.time(), Utils.toCamelCase(user.difficulty().name()));

                textGraphics.putString(x+2, y+n*2+2, formattedString);
                // Only show top 3 players
                if (n > 3){
                    break;
                }
                n++;
            }
        }
    }
}
