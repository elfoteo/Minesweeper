import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;
import engine.UIManager;
import engine.skins.SkinManager;
import engine.themes.ThemeManager;
import engine.utils.Utils;

import java.io.IOException;


/**
 * Minesweeper game using java and lanterna
 * 
 * @author Matteo Ciocci
 *
 */
public class Program {
    public static void main(String[] args) throws IOException {
        Terminal terminal;
        try{
			// Create factory
			DefaultTerminalFactory factory = new DefaultTerminalFactory();
			factory.setTerminalEmulatorTitle("Minesweeper");
			// Create terminal
			terminal = factory.createTerminal();
			terminal.enterPrivateMode();

			// Register the skins
			SkinManager.registerSkins();
			// Register the themes
			ThemeManager.registerThemes();

            UIManager uiManager = new UIManager(terminal);
			uiManager.showMainScreen();
		}
		catch (Exception ex){
			String crashReport = Utils.exceptionToString(ex);

			// Show the crash report in a message box
			Utils.Debug(crashReport);
			return; // Immediately stop the program
		}

		terminal.exitPrivateMode();
		terminal.close();
	}
}