package nintaco.logger;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.logging.Level;

// https://blogs.oracle.com/nickstephen/entry/java_redirecting_system_out_and

public final class StdOutErrLevel extends Level {

    public static final Level STDOUT = new StdOutErrLevel("STDOUT",
            Level.INFO.intValue() + 53);
    public static final Level STDERR = new StdOutErrLevel("STDERR",
            Level.INFO.intValue() + 54);

    private StdOutErrLevel(final String name, final int value) {
        super(name, value);
    }

    // avoid creating duplicates when deserializing
    private Object readResolve() throws ObjectStreamException {
        if (intValue() == STDOUT.intValue()) {
            return STDOUT;
        } else if (intValue() == STDERR.intValue()) {
            return STDERR;
        }
        throw new InvalidObjectException("Unknown intValue: " + this);
    }
}
