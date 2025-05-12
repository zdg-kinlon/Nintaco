package cn.kinlon.emu.input.miraclepiano;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;

public class MiraclePianoDescriptor extends DeviceDescriptor {

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