package cn.kinlon.emu.input.mahjong;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;

public class MahjongDescriptor extends DeviceDescriptor {

    public static final int KeyA = 0;
    public static final int KeyB = 1;
    public static final int KeyC = 2;
    public static final int KeyD = 3;
    public static final int KeyE = 4;
    public static final int KeyF = 5;
    public static final int KeyG = 6;
    public static final int KeyH = 7;
    public static final int KeyI = 8;
    public static final int KeyJ = 9;
    public static final int KeyK = 10;
    public static final int KeyL = 11;
    public static final int KeyM = 12;
    public static final int KeyN = 13;

    public static final int KeySelect = 14;
    public static final int KeyStart = 15;
    public static final int KeyKan = 16;
    public static final int KeyPon = 17;
    public static final int KeyChi = 18;
    public static final int KeyReach = 19;
    public static final int KeyRon = 20;

    public static final int RewindTime = 21;

    public static final String[] NAMES = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
            "Select", "Start", "Kan", "Pon", "Chi", "Reach", "Ron",
            "Rewind Time",
    };

    private static final Key[] DEFAULTS = {
            Q, W, E, R, T, Y, U, I, O, P, LBRACKET, RBRACKET, BACKSLASH, DELETE,
            APOSTROPHE, RETURN, G, H, J, K, L,
            EQUALS,
    };

    public MahjongDescriptor() {
        super(InputDevices.Mahjong);
    }

    @Override
    public String getDeviceName() {
        return "Mahjong";
    }

    @Override
    public int getButtonCount() {
        return 22;
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

        int presses = 0;
        int value = 0xFFFF;
        for (int i = KeyRon; i >= KeyA; i--) {
            if (pressedValues[i] != 0) {
                value = (value << 5) | i;
                if (++presses == 3) {
                    break;
                }
            }
        }

        return (value << 16) | bits;
    }
}