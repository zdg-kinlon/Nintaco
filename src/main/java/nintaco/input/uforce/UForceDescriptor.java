package nintaco.input.uforce;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceDescriptor;
import nintaco.input.InputDevices;
import nintaco.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;
import static nintaco.util.MathUtil.clamp;

public class UForceDescriptor extends DeviceDescriptor {

    public static final int Top = 0;
    public static final int TopFieldUpperLeft = 1;
    public static final int TopFieldUpperRight = 2;
    public static final int TopFieldLowerLeft = 3;
    public static final int TopFieldLowerRight = 4;
    public static final int BottomFieldUpperLeft = 5;
    public static final int BottomFieldLowerLeft = 6;
    public static final int BottomFieldLowerRight = 7;
    public static final int Bottom = 8;
    public static final int Start = 9;
    public static final int Select = 10;

    public static final int RewindTime = 11;
    public static final int HighSpeed = 12;

    private static final Key[] DEFAULTS = {
            ADD,         // Top  
            DIVIDE,      // Top-Field Upper-Left
            MULTIPLY,    // Top-Field Upper-Right
            NUMPAD8,     // Top-Field Lower-Left
            NUMPAD9,     // Top-Field Lower-Right
            NUMPAD5,     // Bottom-Field Upper-Left
            NUMPAD2,     // Bottom-Field Lower-Left
            NUMPAD3,     // Bottom-Field Lower-Right
            NUMPAD0,     // Bottom
            RETURN,      // Start
            APOSTROPHE,  // Select
            BACK,        // Rewind Time
            GRAVE,       // High Speed
    };

    public UForceDescriptor() {
        super(InputDevices.UForce);
    }

    @Override
    public String getDeviceName() {
        return "U-Force";
    }

    @Override
    public int getButtonCount() {
        return 13;
    }

    @Override
    public int getRewindTimeButton() {
        return RewindTime;
    }

    @Override
    public int getHighSpeedButton() {
        return HighSpeed;
    }

    @Override
    public String getButtonName(final int buttonIndex) {
        switch (buttonIndex) {
            case Top:
                return "Top";
            case TopFieldUpperLeft:
                return "Top-Field Upper-Left";
            case TopFieldUpperRight:
                return "Top-Field Upper-Right";
            case TopFieldLowerLeft:
                return "Top-Field Lower-Left";
            case TopFieldLowerRight:
                return "Top-Field Lower-Right";
            case BottomFieldUpperLeft:
                return "Bottom-Field Upper-Left";
            case BottomFieldLowerLeft:
                return "Bottom-Field Lower-Left";
            case BottomFieldLowerRight:
                return "Bottom-Field Lower-Right";
            case Bottom:
                return "Bottom";
            case Start:
                return "Start";
            case Select:
                return "Select";
            case RewindTime:
                return "Rewind Time";
            case HighSpeed:
                return "High Speed";
            default:
                return "Unknown";
        }
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(InputUtil.getDefaultKeyboard(), buttonIndex,
                DEFAULTS);
    }

    @Override
    public int setButtonBits(int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);
        updateHighSpeed(pressedValues[HighSpeed] != 0, portIndex);

        bits = 0;
        if (pressedValues[Select] != 0) {
            bits |= 0x0400;
        }
        if (pressedValues[Start] != 0) {
            bits |= 0x0200;
        }
        if (pressedValues[Bottom] != 0) {
            bits |= 0x0100;
        }
        if (pressedValues[BottomFieldLowerRight] != 0) {
            bits |= 0x0080;
        }
        if (pressedValues[BottomFieldLowerLeft] != 0) {
            bits |= 0x0040;
        }
        if (pressedValues[BottomFieldUpperLeft] != 0) {
            bits |= 0x0020;
        }
        if (pressedValues[TopFieldLowerRight] != 0) {
            bits |= 0x0010;
        }
        if (pressedValues[TopFieldLowerLeft] != 0) {
            bits |= 0x0008;
        }
        if (pressedValues[TopFieldUpperRight] != 0) {
            bits |= 0x0004;
        }
        if (pressedValues[TopFieldUpperLeft] != 0) {
            bits |= 0x0002;
        }
        if (pressedValues[Top] != 0) {
            bits |= 0x0001;
        }

        final int dx = clamp(((int) InputUtil.getMouseDeltaX()) >> 1, -128, 127);
        final int dy = clamp(((int) InputUtil.getMouseDeltaY()) >> 1, -128, 127);

        return ((dy & 0xFF) << 24) | ((dx & 0xFF) << 16) | bits;
    }
}