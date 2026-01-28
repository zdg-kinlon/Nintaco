package nintaco.cpu;

import nintaco.Breakpoint;

import java.util.ArrayList;
import java.util.List;

import static nintaco.BreakpointType.*;
import static nintaco.util.CollectionsUtil.convertToArray;

public class BreakPointMonitor {
    private final CPU cpu;

    public BreakPointMonitor(CPU cpu) {
        this.cpu = cpu;
    }

    private volatile transient Breakpoint[] executeBreakpoints;
    private volatile transient Breakpoint[] readBreakpoints;
    private volatile transient Breakpoint[] writeBreakpoints;

    public void setBreakpoints(final Breakpoint[] breakpoints) {
        final List<Breakpoint> executes = new ArrayList<>();
        final List<Breakpoint> reads = new ArrayList<>();
        final List<Breakpoint> writes = new ArrayList<>();
        if (breakpoints != null) {
            for (final Breakpoint breakpoint : breakpoints) {
                if (breakpoint.isEnabled()) {
                    switch (breakpoint.getType()) {
                        case Execute:
                            executes.add(breakpoint);
                            break;
                        case Read:
                            reads.add(breakpoint);
                            break;
                        case Write:
                            writes.add(breakpoint);
                            break;
                        default:
                            executes.add(breakpoint);
                            reads.add(breakpoint);
                            writes.add(breakpoint);
                            break;
                    }
                }
            }
        }
        executeBreakpoints = convertToArray(Breakpoint.class, executes);
        readBreakpoints = convertToArray(Breakpoint.class, reads);
        writeBreakpoints = convertToArray(Breakpoint.class, writes);
    }

    public void executeBreakpoints(int pc) {
        final Breakpoint[] breakpoints = executeBreakpoints;
        if (breakpoints != null) {
            for (int i = breakpoints.length - 1; i >= 0; i--) {
                final Breakpoint breakpoint = breakpoints[i];
                if ((breakpoint.startAddress == pc || (breakpoint.range
                        && breakpoint.startAddress <= pc
                        && pc <= breakpoint.endAddress)) && (breakpoint.bank < 0
                        || breakpoint.bank == cpu.mapper().getPrgBank(pc))) {
                    breakpoint.hit = true;
                }
            }
        }
    }

    public void readBreakpoints(int address) {
        final Breakpoint[] breakpoints = readBreakpoints;
        if (breakpoints != null) {
            for (int i = breakpoints.length - 1; i >= 0; i--) {
                final Breakpoint breakpoint = breakpoints[i];
                if ((breakpoint.startAddress == address || (breakpoint.range
                        && breakpoint.startAddress <= address
                        && address <= breakpoint.endAddress)) && (breakpoint.bank < 0
                        || breakpoint.bank == cpu.mapper().getPrgBank(address))) {
                    breakpoint.hit = true;
                }
            }
        }
    }
    
   
}
