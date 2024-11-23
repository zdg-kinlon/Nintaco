package nintaco.gui.debugger.logger;

import nintaco.disassembler.*;
import nintaco.preferences.AppPrefs;

import java.io.Serializable;

public class LoggerAppPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Integer maxLines;
    private LogPrefs logPrefs;

    public LogPrefs getLogPrefs() {
        synchronized (AppPrefs.class) {
            if (logPrefs == null) {
                logPrefs = new LogPrefs();

                logPrefs.frameCounter = false;
                logPrefs.cpuCounter = false;
                logPrefs.instructionCounter = false;
                logPrefs.scanline = true;
                logPrefs.dot = true;

                logPrefs.logBeforeExecute = false;
                logPrefs.bank = false;
                logPrefs.machineCode = true;
                logPrefs.instruction = true;
                logPrefs.inspections = true;

                logPrefs.logPCType = LogPCType.PC;
                logPrefs.A = true;
                logPrefs.X = true;
                logPrefs.Y = true;
                logPrefs.logPType = LogPType.HH;
                logPrefs.logSType = LogSType.SP;

                logPrefs.v = false;
                logPrefs.t = false;
                logPrefs.x = false;
                logPrefs.w = false;

                logPrefs.addressLabels = true;
                logPrefs.tabBySP = false;
                logPrefs.branchesType = BranchesType.AbsoluteBranches;
            }
            return logPrefs;
        }
    }

    public void setLogPrefs(LogPrefs logPrefs) {
        synchronized (AppPrefs.class) {
            this.logPrefs = logPrefs;
        }
    }

    public int getMaxLines() {
        synchronized (AppPrefs.class) {
            if (maxLines == null) {
                maxLines = 10_000_000;
            }
            return maxLines;
        }
    }

    public void setMaxLines(final int maxLines) {
        synchronized (AppPrefs.class) {
            this.maxLines = maxLines;
        }
    }
}
