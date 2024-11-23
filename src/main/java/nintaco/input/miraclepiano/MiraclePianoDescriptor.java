package nintaco.input.miraclepiano;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceDescriptor;
import nintaco.input.InputDevices;
import nintaco.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;

public class MiraclePianoDescriptor extends DeviceDescriptor {

    public static final int KeyC2 = 0;
    public static final int KeyCs2 = 1;
    public static final int KeyD2 = 2;
    public static final int KeyDs2 = 3;
    public static final int KeyE2 = 4;
    public static final int KeyF2 = 5;
    public static final int KeyFs2 = 6;
    public static final int KeyG2 = 7;
    public static final int KeyGs2 = 8;
    public static final int KeyA2 = 9;
    public static final int KeyAs2 = 10;
    public static final int KeyB2 = 11;

    public static final int KeyC3 = 12;
    public static final int KeyCs3 = 13;
    public static final int KeyD3 = 14;
    public static final int KeyDs3 = 15;
    public static final int KeyE3 = 16;
    public static final int KeyF3 = 17;
    public static final int KeyFs3 = 18;
    public static final int KeyG3 = 19;
    public static final int KeyGs3 = 20;
    public static final int KeyA3 = 21;
    public static final int KeyAs3 = 22;
    public static final int KeyB3 = 23;

    public static final int KeyC4 = 24; // Middle C
    public static final int KeyCs4 = 25;
    public static final int KeyD4 = 26;
    public static final int KeyDs4 = 27;
    public static final int KeyE4 = 28;
    public static final int KeyF4 = 29;
    public static final int KeyFs4 = 30;
    public static final int KeyG4 = 31;
    public static final int KeyGs4 = 32;
    public static final int KeyA4 = 33;
    public static final int KeyAs4 = 34;
    public static final int KeyB4 = 35;

    public static final int KeyC5 = 36;
    public static final int KeyCs5 = 37;
    public static final int KeyD5 = 38;
    public static final int KeyDs5 = 39;
    public static final int KeyE5 = 40;
    public static final int KeyF5 = 41;
    public static final int KeyFs5 = 42;
    public static final int KeyG5 = 43;
    public static final int KeyGs5 = 44;
    public static final int KeyA5 = 45;
    public static final int KeyAs5 = 46;
    public static final int KeyB5 = 47;

    public static final int KeyC6 = 48;

    public static final int DamperPedal = 49;

    public static final int VolumePlus = 50;
    public static final int VolumeMinus = 51;

    public static final int Piano = 52;
    public static final int Harpsichord = 53;
    public static final int Organ = 54;
    public static final int Vibraphone = 55;
    public static final int ElectricPiano = 56;
    public static final int Synthesizer = 57;

    public static final int RewindTime = 58;

    public static final String[] NAMES = {
            "C2", "C#2", "D2", "D#2", "E2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2",
            "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3",
            "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4",
            "C5", "C#5", "D5", "D#5", "E5", "F5", "F#5", "G5", "G#5", "A5", "A#5", "B5",
            "C6",
            "Damper Pedal",
            "Volume +", "Volume -",
            "Piano", "Harpsichord", "Organ",
            "Vibraphone", "Electric Piano", "Synthesizer",
            "Rewind Time",
    };

    private static final Key[] DEFAULTS = {
            Z, X, C, V, B, N, M, COMMA, PERIOD, SLASH, RSHIFT, RCONTROL,
            A, S, D, F, G, H, J, K, L, SEMICOLON, APOSTROPHE, RETURN,
            Q, W, E, R, T, Y, U, I, O, P, LBRACKET, RBRACKET,
            _1, _2, _3, _4, _5, _6, _7, _8, _9, _0, MINUS, EQUALS,
            INSERT,
            SPACE,
            ADD, SUBTRACT,
            NUMPAD4, NUMPAD5, NUMPAD6,
            NUMPAD1, NUMPAD2, NUMPAD3,
            TAB,
    };

    private int keySet;

    public MiraclePianoDescriptor() {
        super(InputDevices.MiraclePiano);
    }

    @Override
    public String getDeviceName() {
        return "Miracle Piano";
    }

    @Override
    public int getButtonCount() {
        return 59;
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
    public int setButtonBits(int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        bits |= keySet << 30;
        final int offset = 18 * keySet;
        for (int i = 0, b = 1; i < 22; ++i) {
            if (pressedValues[offset + i] != 0) {
                bits |= b;
            }
            if (b == 0x00_00_00_80) {
                b = 0x00_01_00_00;
            } else {
                b <<= 1;
            }
        }

        if (++keySet == 3) {
            keySet = 0;
        }

        return bits;
    }
}