import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

//Handles log file generation and message logging functionalities.
 
public class LogHelper {
    // Handler to write log messages into a specified file
    public FileHandler f_H_J;

    // Logger instance used for logging messages
    public static Logger log_J = Logger.getLogger(LogHelper.class.getName());

    /**
     * Initializes the logging configuration.
     * Creates a new log file and sets up a file handler to write messages into it.
     * @param present_PID_J - PeerID for which the log file needs to be created
     */
    public void initializeLogger(String present_PID_J) {
        try {
            f_H_J = new FileHandler("log_peer_" + present_PID_J + ".log");
            f_H_J.setFormatter(new LogFormatter());
            log_J.addHandler(f_H_J);
            log_J.setUseParentHandlers(false);
        } catch (IOException e) {

        }
    }

    /**
     * Logs a message in a log file and displays it in the console.
     * @param m_J - message to be logged and displayed in console
     */
    public static void logAndShowInConsole(String m_J) {
        log_J.info(m_J);
        System.out.println(LogFormatter.getFormattedMessage(m_J));
    }
}
