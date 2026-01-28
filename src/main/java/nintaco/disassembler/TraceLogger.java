package nintaco.disassembler;

import nintaco.App;
import nintaco.cpu.CPU;
import nintaco.PPU;
import nintaco.gui.debugger.DebuggerFrame;
import nintaco.gui.debugger.logger.LoggerAppPrefs;
import nintaco.mappers.Mapper;
import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;
import nintaco.util.EDT;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import static nintaco.files.FileUtil.mkdir;
import static nintaco.util.ThreadUtil.join;
import static nintaco.util.ThreadUtil.threadWait;

public class TraceLogger {

    private static final int LOG_RECORDS = 4096;
    final StringBuilder sb = new StringBuilder();
    private final Object MONITOR = new Object();
    private final LogPrefs logPrefs;
    private final LogRecord[] logRecords = new LogRecord[LOG_RECORDS];
    private final int maxLines;

    private volatile PrintWriter out;
    private volatile Thread thread;
    private volatile boolean running = true;
    private volatile int writeHead;
    private volatile int readHead;
    private volatile int size;

    private boolean beforeCycle = true;

    private int lines;

    public TraceLogger() {
        final LoggerAppPrefs appPrefs = AppPrefs.getInstance().getLoggerAppPrefs();
        maxLines = appPrefs.getMaxLines();
        logPrefs = appPrefs.getLogPrefs();
        final int[] addresses = logPrefs.addresses;
        for (int i = logRecords.length - 1; i >= 0; i--) {
            logRecords[i] = new LogRecord();
            if (addresses != null) {
                logRecords[i].values = new int[addresses.length];
            }
        }

        try {
            final File file = new File(GamePrefs.getInstance().getLoggerGamePrefs()
                    .getFileName());
            mkdir(file.getParent());
            out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        } catch (final Throwable t) {
            t.printStackTrace();
            return;
        }

        thread = new Thread(this::readLoop, "Trace Logger Thread");
        thread.start();
    }

    public void log(final boolean beforeExecute, final CPU cpu, final PPU ppu,
                    final Mapper mapper) {

        if (!running || beforeExecute != beforeCycle) {
            return;
        }
        beforeCycle = !beforeCycle;

        if (beforeExecute) {
            synchronized (MONITOR) {
                while (running && size == LOG_RECORDS) {
                    threadWait(MONITOR);
                }
            }
            if (!running) {
                return;
            }

            if (++writeHead == LOG_RECORDS) {
                writeHead = 0;
            }

            Disassembler.captureInstruction(logRecords[writeHead], cpu, ppu, mapper);
        }

        if (beforeExecute == logPrefs.logBeforeExecute) {
            Disassembler.captureRegisters(logPrefs, logRecords[writeHead], cpu, ppu,
                    mapper);
        }

        if (!beforeExecute) {
            synchronized (MONITOR) {
                size++;
                MONITOR.notifyAll();
            }
        }
    }

    private void readLoop() {
        while (running) {
            synchronized (MONITOR) {
                while (running && size == 0) {
                    threadWait(MONITOR);
                }
            }
            if (!running) {
                return;
            }
            if (++readHead >= LOG_RECORDS) {
                readHead = 0;
            }
            persistLogRecord(logRecords[readHead]);
            synchronized (MONITOR) {
                size--;
                MONITOR.notifyAll();
            }
        }
        close();
    }

    private void persistLogRecord(final LogRecord logRecord) {
        try {
            final PrintWriter o = out;
            if (o != null) {
                sb.setLength(0);
                Disassembler.appendLogRecord(sb, logRecord, logPrefs);
                out.println(sb);
                if (++lines == maxLines) {
                    running = false;
                    App.disposeTraceLogger();
                    final DebuggerFrame debugger = App.getDebuggerFrame();
                    if (debugger != null) {
                        EDT.async(debugger::updateLoggerButton);
                    }
                }
            }
        } catch (final Throwable t) {
            running = false;
        }
    }

    public void flush() {
        try {
            final PrintWriter o = out;
            if (o != null) {
                o.flush();
            }
        } catch (final Throwable t) {
        }
    }

    private void close() {
        try {
            final PrintWriter o = out;
            out = null;
            if (o != null) {
                o.close();
            }
        } catch (final Throwable t) {
        }
    }

    public void dispose() {
        close();
        final Thread t = thread;
        if (t != null) {
            thread = null;
            synchronized (MONITOR) {
                running = false;
                MONITOR.notifyAll();
            }
            join(t);
        }
    }
}
