package cn.kinlon.emu.input.snesmouse;

import net.java.games.input.Component;
import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import static net.java.games.input.Component.Identifier.Button.*;

public class SnesMouseDescriptor extends DeviceDescriptor {

    public static final int LeftButton = 0;
    public static final int RightButton = 1;

    public static final int RewindTime = 2;

    private static final Component.Identifier.Button[] DEFAULTS
            = {LEFT, RIGHT, MIDDLE};

    public SnesMouseDescriptor() {
        super(InputDevices.SnesMouse);
    }

    @Override
    public String getDeviceName() {
        return "SNES Mouse";
    }

    @Override
    public int getButtonCount() {
        return 3;
    }

    @Override
    public int getRewindTimeButton() {
        return RewindTime;
    }

    @Override
    public String getButtonName(int buttonIndex) {
        switch (buttonIndex) {
            case LeftButton:
                return "Left";
            case RightButton:
                return "Right";
            case RewindTime:
                return "Rewind Time";
            default:
                return "Unknown";
        }
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(InputUtil.getDefaultMouse(), buttonIndex,
                DEFAULTS);
    }

    @Override
    public int setButtonBits(final int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        final boolean rewindTime = pressedValues[RewindTime] != 0;

        updateRewindTime(rewindTime, portIndex);

        int buttons = 0;
        if (pressedValues[LeftButton] != 0) {
            buttons |= 0x40;
        }
        if (pressedValues[RightButton] != 0) {
            buttons |= 0x80;
        }


        return bits
                | ((((int) InputUtil.getMouseDeltaY()) & 0xFF) << 24)
                | ((((int) InputUtil.getMouseDeltaX()) & 0xFF) << 16)
                | (portIndex == 0 ? buttons : (buttons << 8));
    }
}
