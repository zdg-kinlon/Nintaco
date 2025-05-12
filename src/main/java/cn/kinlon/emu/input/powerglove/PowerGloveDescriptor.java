package cn.kinlon.emu.input.powerglove;

import net.java.games.input.Component;
import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import static net.java.games.input.Component.Identifier.Button;
import static net.java.games.input.Component.Identifier.Key;

public class PowerGloveDescriptor extends DeviceDescriptor {

    public static final int PointFinger = 0;
    public static final int MakeFist = 1;
    public static final int Select = 2;
    public static final int Start = 3;
    public static final int MoveIn = 4;
    public static final int MoveOut = 5;
    public static final int RollLeft = 6;
    public static final int RollRight = 7;

    public static final int RewindTime = 8;

    private static final Component.Identifier[] DEFAULTS = {
            Button.LEFT, Button.RIGHT,
            Key.APOSTROPHE, Key.RETURN, Key.W, Key.S, Key.A, Key.D,
            Key.BACK,
    };

    public PowerGloveDescriptor() {
        super(InputDevices.PowerGlove);
    }

    @Override
    public String getDeviceName() {
        return "Power Glove";
    }

    @Override
    public int getButtonCount() {
        return 9;
    }

    @Override
    public int getRewindTimeButton() {
        return RewindTime;
    }

    @Override
    public String getButtonName(final int buttonIndex) {
        switch (buttonIndex) {
            case PointFinger:
                return "Point Finger";
            case MakeFist:
                return "Make Fist";
            case Select:
                return "Select";
            case Start:
                return "Start";
            case MoveIn:
                return "Move In";
            case MoveOut:
                return "Move Out";
            case RollLeft:
                return "Roll Left";
            case RollRight:
                return "Roll Right";
            case RewindTime:
                return "Rewind Time";
            default:
                return "Unknown";
        }
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping((buttonIndex == PointFinger
                || buttonIndex == MakeFist) ? InputUtil.getDefaultMouse()
                : InputUtil.getDefaultKeyboard(), buttonIndex, DEFAULTS);
    }

    @Override
    public int setButtonBits(final int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        int buttons = 0;
        if (pressedValues[PointFinger] != 0) {
            buttons |= 0x01;
        }
        if (pressedValues[MakeFist] != 0) {
            buttons |= 0x02;
        }
        if (pressedValues[Select] != 0) {
            buttons |= 0x04;
        }
        if (pressedValues[Start] != 0) {
            buttons |= 0x08;
        }
        if (pressedValues[MoveIn] != 0) {
            buttons |= 0x10;
        }
        if (pressedValues[MoveOut] != 0) {
            buttons |= 0x20;
        }
        if (pressedValues[RollLeft] != 0) {
            buttons |= 0x40;
        }
        if (pressedValues[RollRight] != 0) {
            buttons |= 0x80;
        }

        return bits
                | (InputUtil.getMouseCoordinates() << 16)
                | (portIndex == 0 ? buttons : (buttons << 8));
    }
}