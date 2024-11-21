import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Handles the formatting of log messages for log files.
 */
public class LogFormatter extends Formatter {
    // Defines the format for displaying dates in logs
    public static DateTimeFormatter d_t_fmt_J = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    /**
     * Formats the log record into a specific format.
     * @param r_J - The log record to be formatted
     * @return - Formatted log message as a string
     */
    @Override
    public String format(LogRecord r_J) {
        return getFormattedMessage(r_J.getMessage());
    }

    /**
     * Defines the specific format in which log records should be written.
     * @param msg_J - log message to be formatted
     * @return Formatted log message as a string
     */
    public static String getFormattedMessage(String msg_J) {
        StringBuilder sb = new StringBuilder();
        sb.append(d_t_fmt_J.format(LocalDateTime.now()));
        sb.append(": ");
        sb.append("Peer ");
        sb.append(msg_J);
        sb.append("\n");
        return sb.toString();
    }
}
