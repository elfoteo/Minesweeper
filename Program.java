import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import engine.UIManager;
import engine.music.MusicManager;
import engine.skins.SkinManager;
import engine.themes.ThemeManager;
import engine.utils.FontManager;
import engine.utils.Utils;

import java.awt.*;
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
			// List of fonts:
			// Courier New
			// Cascadia Code
			// Cascadia Mono
			// Consolas
			// Lucida Sans Typewriter


			// Create factory
			DefaultTerminalFactory factory = new DefaultTerminalFactory();
			factory.setTerminalEmulatorTitle("Minesweeper");

			// Register the fonts
			FontManager.registerFonts();
			// Load the font configuration
			factory.setTerminalEmulatorFontConfiguration(FontManager.getFontConfiguration());

			// Create terminal
			terminal = factory.createTerminal();
			terminal.enterPrivateMode();

			// Register the skins
			SkinManager.registerSkins();
			// Register the themes
			ThemeManager.registerThemes();
			// Register music
			boolean success = MusicManager.register();
			if (!success){
				terminal.exitPrivateMode();
				terminal.close();
				return;
			}

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