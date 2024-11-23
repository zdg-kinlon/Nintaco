package nintaco.input;

import java.io.Serializable;

import static nintaco.input.ConsoleType.NES;
import static nintaco.input.InputDevices.Gamepad1;
import static nintaco.input.InputDevices.Gamepad2;

public class Ports implements Serializable {

    public static final int Port1 = 0;
    public static final int Port2 = 1;
    public static final int Tap1 = 0;
    public static final int Tap2 = 1;
    public static final int Tap3 = 2;
    public static final int Tap4 = 3;
    public static final int Main1 = 0;
    public static final int Main2 = 1;
    public static final int Sub1 = 2;
    public static final int Sub2 = 3;
    public static final int ExpansionPort = 4;
    public static final Ports DEFAULTS = new Ports(
            new int[][]{{Port1, Gamepad1}, {Port2, Gamepad2}}, false, NES);
    private static final long serialVersionUID = 0;
    private final int[][] portDevices;
    private final boolean multitap;
    private final int consoleType;

    public Ports(final int[][] portDevices, final boolean multitap,
                 final int consoleType) {
        this.portDevices = portDevices;
        this.multitap = multitap;
        this.consoleType = consoleType;
    }

    public static boolean hasDevice(final int[][] portDevices, final int device) {
        for (int i = portDevices.length - 1; i >= 0; i--) {
            if (portDevices[i][1] == device) {
                return true;
            }
        }
        return false;
    }

    public int[][] getPortDevices() {
        return portDevices;
    }

    public boolean isMultitap() {
        return multitap;
    }

    public int getConsoleType() {
        return consoleType;
    }

    public boolean hasDevice(final int device) {
        return hasDevice(portDevices, device);
    }

    public Integer getDevice(final int port) {
        for (int i = portDevices.length - 1; i >= 0; i--) {
            if (portDevices[i][0] == port) {
                return portDevices[i][1];
            }
        }
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        }
        final Ports ports = (Ports) obj;
        if (!(portDevices.length == ports.portDevices.length
                && multitap == ports.multitap && consoleType == ports.consoleType)) {
            return false;
        }
        for (int i = portDevices.length - 1; i >= 0; i--) {
            if (!(portDevices[i][0] == ports.portDevices[i][0]
                    && portDevices[i][1] == ports.portDevices[i][1])) {
                return false;
            }
        }
        return true;
    }
}