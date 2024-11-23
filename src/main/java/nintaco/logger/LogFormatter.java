package nintaco.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

// http://stackoverflow.com/questions/194765/how-do-i-get-java-logging-output-to-appear-on-a-single-line

public final class LogFormatter extends Formatter {

    private static final String LINE_SEPARATOR
            = System.getProperty("line.separator");

    private static final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyyMMdd-HH:mm:ss.SSS");

    @Override
    public String format(final LogRecord record) {
        final StringBuilder sb = new StringBuilder();

        sb.append(formatter.format(Instant.ofEpochMilli(record.getMillis())
                        .atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .append(" ")
                .append(record.getLevel().getLocalizedName())
                .append(": ")
                .append(formatMessage(record))
                .append(LINE_SEPARATOR);

        if (record.getThrown() != null) {
            try {
                final StringWriter stringWriter = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(stringWriter);
                record.getThrown().printStackTrace(printWriter);
                printWriter.close();
                sb.append(stringWriter);
            } catch (final Throwable t) {
            }
        }

        return sb.toString();
    }
}
