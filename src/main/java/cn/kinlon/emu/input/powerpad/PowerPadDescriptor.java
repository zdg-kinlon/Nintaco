package cn.kinlon.emu.input.powerpad;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;

public class PowerPadDescriptor extends DeviceDescriptor {

    public static final int SideB1 = 0;
    public static final int SideB2 = 1;
    public static final int SideB3 = 2;
    public static final int SideB4 = 3;
    public static final int SideB5 = 4;
    public static final int SideB6 = 5;
    public static final int SideB7 = 6;
    public static final int SideB8 = 7;
    public static final int SideB9 = 8;
    public static final int SideB10 = 9;
    public static final int SideB11 = 10;
    public static final int SideB12 = 11;

    public static final int SideA3 = 12;
    public static final int SideA2 = 13;
    public static final int SideA8 = 14;
    public static final int SideA7 = 15;
    public static final int SideA6 = 16;
    public static final int SideA5 = 17;
    public static final int SideA11 = 18;
    public static final int SideA10 = 19;

    public static final int RewindTime = 20;

    private static final Key[] DEFAULTS
            = {Q, W, E, R,
            A, S, D, F,
            Z, X, C, V,

            Y, U,
            G, H, J, K,
            N, M,

            EQUALS};

    public PowerPadDescriptor() {
        super(InputDevices.PowerPad);
    }

    @Override
    public String getDeviceName() {
        return "Power Pad";
    }

    @Override
    public int getButtonCount() {
        return 21;
    }

    @Override
    public int getRewindTimeButton() {
        return RewindTime;
    }

    @Override
    public String getButtonName(final int buttonIndex) {
        switch (buttonIndex) {
            case SideB1:
                return "Side B 1";
            case SideB2:
                return "Side B 2";
            case SideB3:
                return "Side B 3";
            case SideB4:
                return "Side B 4";
            case SideB5:
                return "Side B 5";
            case SideB6:
                return "Side B 6";
            case SideB7:
                return "Side B 7";
            case SideB8:
                return "Side B 8";
            case SideB9:
                return "Side B 9";
            case SideB10:
                return "Side B 10";
            case SideB11:
                return "Side B 11";
            case SideB12:
                return "Side B 12";

            case SideA3:
                return "Side A 3";
            case SideA2:
                return "Side A 2";
            case SideA8:
                return "Side A 8";
            case SideA7:
                return "Side A 7";
            case SideA6:
                return "Side A 6";
            case SideA5:
                return "Side A 5";
            case SideA11:
                return "Side A 11";
            case SideA10:
                return "Side A 10";

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

        if (pressedValues[SideB2] != 0 || pressedValues[SideA2] != 0) {
            bits |= 0x00010000;
        }
        if (pressedValues[SideB4] != 0) {
            bits |= 0x00020000;
        }
        if (pressedValues[SideB1] != 0) {
            bits |= 0x00040000;
        }
        if (pressedValues[SideB3] != 0 || pressedValues[SideA3] != 0) {
            bits |= 0x00080000;
        }
        if (pressedValues[SideB5] != 0 || pressedValues[SideA5] != 0) {
            bits |= 0x00100000;
        }
        if (pressedValues[SideB12] != 0) {
            bits |= 0x00200000;
        }
        if (pressedValues[SideB9] != 0) {
            bits |= 0x00400000;
        }
        if (pressedValues[SideB8] != 0 || pressedValues[SideA8] != 0) {
            bits |= 0x00800000;
        }
        if (pressedValues[SideB6] != 0 || pressedValues[SideA6] != 0) {
            bits |= 0x01000000;
        }
        bits |= 0x02000000;
        if (pressedValues[SideB10] != 0 || pressedValues[SideA10] != 0) {
            bits |= 0x04000000;
        }
        bits |= 0x08000000;
        if (pressedValues[SideB11] != 0 || pressedValues[SideA11] != 0) {
            bits |= 0x10000000;
        }
        bits |= 0x20000000;
        if (pressedValues[SideB7] != 0 || pressedValues[SideA7] != 0) {
            bits |= 0x40000000;
        }
        bits |= 0x80000000;

        return bits;
    }
}