package nintaco.input.doremikkokeyboard;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceDescriptor;
import nintaco.input.InputDevices;
import nintaco.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;

public class DoremikkoKeyboardDescriptor extends DeviceDescriptor {

    public static final int KeyF2 = 0;
    public static final int KeyFs2 = 1;
    public static final int KeyG2 = 2;
    public static final int KeyGs2 = 3;
    public static final int KeyA2 = 4;
    public static final int KeyAs2 = 5;
    public static final int KeyB2 = 6;

    public static final int KeyC3 = 7;
    public static final int KeyCs3 = 8;
    public static final int KeyD3 = 9;
    public static final int KeyDs3 = 10;
    public static final int KeyE3 = 11;
    public static final int KeyF3 = 12;
    public static final int KeyFs3 = 13;
    public static final int KeyG3 = 14;
    public static final int KeyGs3 = 15;
    public static final int KeyA3 = 16;
    public static final int KeyAs3 = 17;
    public static final int KeyB3 = 18;

    public static final int KeyC4 = 19; // Middle C
    public static final int KeyCs4 = 20;
    public static final int KeyD4 = 21;
    public static final int KeyDs4 = 22;
    public static final int KeyE4 = 23;
    public static final int KeyF4 = 24;
    public static final int KeyFs4 = 25;
    public static final int KeyG4 = 26;
    public static final int KeyGs4 = 27;
    public static final int KeyA4 = 28;
    public static final int KeyAs4 = 29;
    public static final int KeyB4 = 30;

    public static final int KeyC5 = 31;
    public static final int KeyCs5 = 32;
    public static final int KeyD5 = 33;
    public static final int KeyDs5 = 34;
    public static final int KeyE5 = 35;

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