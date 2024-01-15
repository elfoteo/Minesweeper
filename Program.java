import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import engine.UIManager;

import java.io.IOException;


/**
 * Minesweeper game using java and lanterna
 * 
 * @author Matteo Ciocci
 *
 */
public class Program {
	private static Terminal terminal;
	private static UIManager uiManager;

	public static void main(String[] args) throws IOException {
		terminal = new DefaultTerminalFactory().createTerminal();
		uiManager = new UIManager(terminal);
		uiManager.showMainScreen();
	}
}