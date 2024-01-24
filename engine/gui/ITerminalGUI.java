package engine.gui;

import java.io.IOException;

public interface ITerminalGUI {
    void onResize();
    void draw() throws IOException;
    void show() throws IOException;
    void onClose();
    void openGUI(ITerminalGUI terminalGUI) throws IOException;
}
