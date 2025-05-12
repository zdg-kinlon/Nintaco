package cn.kinlon.emu.input.taptapmat;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;

// Super Mogura Tataki!! - Pokkun Moguraa (J) (bundled with mat & hammer)

public class TapTapMatDescriptor extends DeviceDescriptor {

    public static final int BUTTON_1_1 = 0;
    public static final int BUTTON_1_2 = 1;
    public static final int BUTTON_1_3 = 2;
    public static final int BUTTON_1_4 = 3;
    public static final int BUTTON_2_1 = 4;
    public static final int BUTTON_2_2 = 5;
    public static final int BUTTON_2_3 = 6;
    public static final int BUTTON_2_4 = 7;
    public static final int BUTTON_3_1 = 8;
    public static final int BUTTON_3_2 = 9;
    public static final int BUTTON_3_3 = 10;
    public static final int BUTTON_3_4 = 11;

    public static final int RewindTime = 12;

    private static final Key[] DEFAULTS
            = {T, Y, U, I,
            G, H, J, K,
            B, N, M, COMMA,

            EQUALS};

    public TapTapMatDescriptor() {
        super(InputDevices.TapTapMat);
    }

    @Override
    public String getDeviceName() {
        return "Tap-tap Mat";
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
    public String getButtonName(final int buttonIndex) {
        switch (buttonIndex) {
            case BUTTON_1_1:
                return "(1, 1)";
            case BUTTON_1_2:
                return "(1, 2)";
            case BUTTON_1_3:
                return "(1, 3)";
            case BUTTON_1_4:
                return "(1, 4)";
            case BUTTON_2_1:
                return "(2, 1)";
            case BUTTON_2_2:
                return "(2, 2)";
            case BUTTON_2_3:
                return "(2, 3)";
            case BUTTON_2_4:
                return "(2, 4)";
            case BUTTON_3_1:
                return "(3, 1)";
            case BUTTON_3_2:
                return "(3, 2)";
            case BUTTON_3_3:
                return "(3, 3)";
            case BUTTON_3_4:
                return "(3, 4)";
            case RewindTime:
                return "Rewind Time";
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
    public int setButtonBits(int bits, final int consoleType, final int portIndex,
                             final int[] pressedValues) {

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        if (pressedValues[BUTTON_1_1] != 0) {
            bits |= 0x0100_0000;
        }
        if (pressedValues[BUTTON_1_2] != 0) {
            bits |= 0x0200_0000;
        }
        if (pressedValues[BUTTON_1_3] != 0) {
            bits |= 0x0400_0000;
        }
        if (pressedValues[BUTTON_1_4] != 0) {
            bits |= 0x0800_0000;
        }

        if (pressedValues[BUTTON_2_1] != 0) {
            bits |= 0x0010_0000;
        }
        if (pressedValues[BUTTON_2_2] != 0) {
            bits |= 0x0020_0000;
        }
        if (pressedValues[BUTTON_2_3] != 0) {
            bits |= 0x0040_0000;
        }
        if (pressedValues[BUTTON_2_4] != 0) {
            bits |= 0x0080_0000;
        }

        if (pressedValues[BUTTON_3_1] != 0) {
            bits |= 0x0001_0000;
        }
        if (pressedValues[BUTTON_3_2] != 0) {
            bits |= 0x0002_0000;
        }
        if (pressedValues[BUTTON_3_3] != 0) {
            bits |= 0x0004_0000;
        }
        if (pressedValues[BUTTON_3_4] != 0) {
            bits |= 0x0008_0000;
        }

        return bits;
    }
}
