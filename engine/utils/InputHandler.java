package engine.utils;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;

public class InputHandler {
    private final Screen screen;
    private volatile KeyStroke lastChar;
    private boolean[] threadRunning = {true};
    private boolean[] handled = {false};

    public InputHandler(Screen screen){
        this.screen = screen;
    }

    public void startThread(){
        threadRunning[0] = true;
        new Thread(() -> {
            while (threadRunning[0]){
                try {
                    handled[0] = false;
                    lastChar = screen.readInput();
                } catch (IOException ignore) {
                }
            }
        }).start();
    }

    public void stopThread(){
        threadRunning[0] = false;
    }

    public KeyStroke handleInput(){
        if (!handled[0]){
            
            handled[0] = true;
            return lastChar;
        }
        return null;
    }
}
