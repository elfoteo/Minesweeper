package engine.utils;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;

public class InputHandler {
    private final Screen screen;
    private volatile KeyStroke lastChar;
    private Thread inputThread;
    private boolean[] handled = new boolean[] {false};

    public InputHandler(Screen screen) {
        this.screen = screen;
    }

    public void startThread() {
        inputThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    lastChar = screen.readInput();
                    handled[0] = false;
                } catch (IOException e) {

                }
            }
        });
        inputThread.start();
    }

    public void stopThread() {
        inputThread.interrupt(); // Interrupt the input thread
        try {
            inputThread.join(); // Wait for inputThread to finish
        } catch (InterruptedException e) {

        }
    }

    public KeyStroke handleInput() {
        if (!handled[0]){
            handled[0] = true;
            return lastChar;
        }
        return null;
    }
}
