package engine.gui;

import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import engine.TerminalResizeEventHandler;
import engine.utils.Utils;

import java.io.IOException;

public class AbstractTerminalGUI implements ITerminalGUI {
    protected Screen screen;
    private TerminalResizeEventHandler terminalResizeEventHandler;
    protected boolean resizePaused = false;

    /**
     * Constructor for AbstractTerminalGUI.
     *
     * @param terminal The terminal instance to associate with this GUI.
     */
    public AbstractTerminalGUI(Terminal terminal) {
        setup(terminal);
    }

    /**
     * Initialize the GUI setup with the given terminal.
     *
     * @param terminal The terminal instance to associate with this GUI.
     */
    private void setup(Terminal terminal) {
        try {
            terminalResizeEventHandler = new TerminalResizeEventHandler(terminal.getTerminalSize());
            terminal.addResizeListener(terminalResizeEventHandler);
            // If an exception is thrown, the terminal will not be resizable,
            // as .subscribe will never be called. Consider logging the exception.
            // Register the onResize event
            subscribe();
        } catch (Exception ignored) {

        }
    }

    private void resize() {
        new Thread(() -> {
            try{
                if (!resizePaused) {
                    // Clear the screen
                    screen.clear();
                    // Resize screen
                    screen.doResizeIfNecessary();

                    try{
                        onResize();

                    }
                    catch (Exception ignore){

                    }

                    try {
                        // Redraw everything
                        draw();
                    } catch (Exception ignore) {

                    }
                }
            }
            catch (Exception ex){
                Utils.Debug(Utils.exceptionToString(ex));
            }
        }).start();
    }

    @Override
    public void onResize() {

    }

    @Override
    public void draw() throws IOException {

    }

    @Override
    public void onClose() {
        resizePaused = true;
        unsubscribe();
        terminalResizeEventHandler = null;
    }

    @Override
    public void openGUI(ITerminalGUI gui) throws IOException {
        // While the new GUI is open, we need to stop the current resize event handler
        resizePaused = true;
        // Open the new GUI
        gui.show();
        // Then add it back
        resizePaused = false;
    }

    /**
     * Unsubscribe from resize events.
     */
    private void unsubscribe() {
        terminalResizeEventHandler.unsubscribe(this::resize);
    }

    /**
     * Subscribe to resize events.
     */
    private void subscribe() {
        terminalResizeEventHandler.subscribe(this::resize);
    }

    @Override
    public void show() throws IOException {

    }

    /**
     * Get the height of the terminal.
     *
     * @return The height of the terminal.
     */
    public int getTerminalHeight() {
        return terminalResizeEventHandler.getLastKnownSize().getRows();
    }

    /**
     * Get the width of the terminal.
     *
     * @return The width of the terminal.
     */
    public int getTerminalWidth() {
        return terminalResizeEventHandler.getLastKnownSize().getColumns();
    }
}
