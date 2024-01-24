package engine;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;

import java.util.ArrayList;
import java.util.List;

public class TerminalResizeEventHandler implements TerminalResizeListener {
    private boolean wasResized = false;
    private TerminalSize lastKnownSize;

    // List of runnables to be executed on resize events
    private List<Runnable> resizeRunnables = new ArrayList<>();

    public TerminalResizeEventHandler(TerminalSize initialSize) {
        this.lastKnownSize = initialSize;
    }

    public synchronized boolean isTerminalResized() {
        if (this.wasResized) {
            this.wasResized = false;
            return true;
        } else {
            return false;
        }
    }

    public TerminalSize getLastKnownSize() {
        return this.lastKnownSize;
    }

    public synchronized void onResized(Terminal terminal, TerminalSize newSize) {
        this.wasResized = true;
        this.lastKnownSize = newSize;

        // Execute all runnables on resize
        for (Runnable runnable : resizeRunnables) {
            runnable.run();
        }
    }

    // Subscribe a runnable to be executed on resize events
    public void subscribe(Runnable runnable) {
        resizeRunnables.add(runnable);
    }

    // Unsubscribe a runnable for resize events
    public void unsubscribe(Runnable runnable) {
        resizeRunnables.remove(runnable);
    }
}
