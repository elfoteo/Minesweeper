import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import engine.UIManager;
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
			terminal = new DefaultTerminalFactory().createTerminal();
			terminal.enterPrivateMode();

            UIManager uiManager = new UIManager(terminal);
			uiManager.showMainScreen();
		}
		catch (Exception ex){
			// Construct the crash report message with stack trace
			StringBuilder crashReport = new StringBuilder("Crash Report:\n\n");
			crashReport.append("An unexpected error occurred.\n");
			crashReport.append("Error details:\n");
			crashReport.append(ex);
			crashReport.append("\n\nStack Trace:\n");

			// Append each line of the stack trace to the crash report
			for (StackTraceElement element : ex.getStackTrace()) {
				crashReport.append(element.toString()).append("\n");
			}

			// Show the crash report in a message box
			Utils.Debug(crashReport.toString());
			return; // Immediately stop the program
		}

		terminal.exitPrivateMode();
		terminal.close();
	}
}