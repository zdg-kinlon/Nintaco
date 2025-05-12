package cn.kinlon.emu.input.doremikkokeyboard;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;

public class DoremikkoKeyboardDescriptor extends DeviceDescriptor {

    public static final int RewindTime = 36;

    public static final String[] NAMES = {
            "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2",
            "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3",
            "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4",
            "C5", "C#5", "D5", "D#5", "E5",
            "Rewind Time",
    };

    private static final Key[] DEFAULTS = {
            Z, S, X, D, C, F, V,
            B, H, N, J, M, COMMA, L, PERIOD, SEMICOLON, SLASH, APOSTROPHE, RSHIFT,
            Q, _2, W, _3, E, R, _5, T, _6, Y, _7, U,
            I, _9, O, _0, P,
            EQUALS,
    };

    private boolean upperKeys;

    public DoremikkoKeyboardDescriptor() {
        super(InputDevices.DoremikkoKeyboard);
    }

    @Override
    public String getDeviceName() {
        return "Doremikko Keyboard";
    }

    @Override
    public int getButtonCount() {
        return 37;
    }

    @Override
    public int getRewindTimeButton() {
        return RewindTime;
    }

    @Override
    public String getButtonName(final int buttonIndex) {
        return NAMES[buttonIndex];
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(InputUtil.getDefaultKeyboard(), buttonIndex,
                DEFAULTS);
    }

    @Override
    public int setButtonBits(final int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        final int offset;
        int value = 0;
        if (upperKeys) {
            offset = 18;
        } else {
            offset = 0;
        }
        if (pressedValues[offset] != 0) {
            value |= 0x0000_0400;
        }
        if (pressedValues[1 + offset] != 0) {
            value |= 0x0000_0800;
        }
        for (int i = 2, j = 0x0001_0000; i < 18; i++, j <<= 1) {
            if (pressedValues[i + offset] != 0) {
                value |= j;
            }
        }

        if (upperKeys) {
            upperKeys = false;
            return (~value & 0xFFFF_0C00) | bits;
        } else {
            upperKeys = true;
            return value | bits;
        }
    }
}