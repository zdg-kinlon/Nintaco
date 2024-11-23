package nintaco.disassembler;

import java.io.Serializable;

public class LogPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    public boolean frameCounter;
    public boolean cpuCounter;
    public boolean instructionCounter;
    public boolean scanline;
    public boolean dot;

    public boolean logBeforeExecute;
    public boolean bank;
    public boolean machineCode;
    public boolean instruction;
    public boolean inspections;

    public int logPCType;
    public boolean A;
    public boolean X;
    public boolean Y;
    public int logPType;
    public int logSType;

    public boolean v;
    public boolean t;
    public boolean x;
    public boolean w;

    public boolean addressLabels;
    public boolean tabBySP;
    public int branchesType;

    public int[] addresses;

    public LogPrefs() {
    }

    public LogPrefs(final LogPrefs logPrefs) {
        frameCounter = logPrefs.frameCounter;
        cpuCounter = logPrefs.cpuCounter;
        instructionCounter = logPrefs.instructionCounter;
        scanline = logPrefs.scanline;
        dot = logPrefs.dot;

        logBeforeExecute = logPrefs.logBeforeExecute;
        bank = logPrefs.bank;
        machineCode = logPrefs.machineCode;
        instruction = logPrefs.instruction;
        inspections = logPrefs.inspections;

        logPCType = logPrefs.logPCType;
        A = logPrefs.A;
        X = logPrefs.X;
        Y = logPrefs.Y;
        logPType = logPrefs.logPType;
        logSType = logPrefs.logSType;

        addressLabels = logPrefs.addressLabels;
        tabBySP = logPrefs.tabBySP;
        branchesType = logPrefs.branchesType;

        if (logPrefs.addresses == null) {
            addresses = null;
        } else {
            if (addresses == null || addresses.length != logPrefs.addresses.length) {
                addresses = new int[logPrefs.addresses.length];
            }
            System.arraycopy(logPrefs.addresses, 0, addresses, 0, addresses.length);
        }
    }
}
