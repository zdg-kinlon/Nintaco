package cn.kinlon.emu.input.familybasic.transformer;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;

public class TransformerDescriptor extends DeviceDescriptor {

    public static final String[] NAMES = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "Num 0", "Num 1", "Num 2", "Num 3", "Num 4", "Num 5", "Num 6", "Num 7",
            "Num 8", "Num 9", "Num -", "Num +", "Num .", "Num =", "Num ,", "Num /",
            "Num Enter",
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12",
            "F13", "F14", "F15",
            "-", "=", "[", "]", ";", "'", "~", "\\", "/", ",", ".", ":", "@", "^",
            "Space", "Backspace", "Tab", "Enter", "Escape",
            "Up", "Down", "Left", "Right",
            "Page Up", "Page Down", "Home", "End", "Insert", "Delete",
            "Left Control", "Right Control", "Left Shift", "Right Shift",
            "Left Alt", "Right Alt", "Left Windows", "Right Windows",
            "Caps Lock", "Num Lock", "Scroll Lock", "SysRq", "Pause",
            "Multiply", "Kana", "Convert", "Noconvert", "Yen", "Underline",
            "Kanji", "Stop", "Ax", "Apps", "Power", "Sleep",
    };

    public static final Key[] DEFAULTS = {
            A, B, C, D, E, F, G, H, I, J, K, L, M,
            N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
            _0, _1, _2, _3, _4, _5, _6, _7, _8, _9,
            NUMPAD0, NUMPAD1, NUMPAD2, NUMPAD3, NUMPAD4, NUMPAD5, NUMPAD6, NUMPAD7,
            NUMPAD8, NUMPAD9, SUBTRACT, ADD, DECIMAL, NUMPADEQUAL, NUMPADCOMMA,
            DIVIDE, NUMPADENTER,
            F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12,
            F13, F14, F15,
            MINUS, EQUALS, LBRACKET, RBRACKET, SEMICOLON, APOSTROPHE, GRAVE, BACKSLASH,
            SLASH, COMMA, PERIOD, COLON, AT, CIRCUMFLEX,
            SPACE, BACK, TAB, RETURN, ESCAPE,
            UP, DOWN, LEFT, RIGHT,
            PAGEUP, PAGEDOWN, HOME, END, INSERT, DELETE,
            LCONTROL, RCONTROL, LSHIFT, RSHIFT,
            LALT, RALT, LWIN, RWIN,
            CAPITAL, NUMLOCK, SCROLL, SYSRQ, PAUSE,
            MULTIPLY, KANA, CONVERT, NOCONVERT, YEN, UNDERLINE,
            KANJI, STOP, AX, APPS, POWER, SLEEP,
    };

    public static final int[] SCAN_CODES = {
            0x1E, 0x30, 0x2E, 0x20, 0x12, 0x21, 0x22, 0x23, 0x17, 0x24, 0x25, 0x26,
            0x32, 0x31, 0x18, 0x19, 0x10, 0x13, 0x1F, 0x14, 0x16, 0x2F, 0x11, 0x2D,
            0x15, 0x2C,
            0x0B, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A,
            0x52, 0x4F, 0x50, 0x51, 0x4B, 0x4C, 0x4D, 0x47,
            0x48, 0x49, 0x4A, 0x4E, 0x53, 0x8D, 0xB3,
            0xB5, 0x9C,
            0x3B, 0x3C, 0x3D, 0x3E, 0x3F, 0x40, 0x41, 0x42, 0x43, 0x44, 0x57, 0x58,
            0x64, 0x65, 0x66,
            0x0C, 0x0D, 0x1A, 0x1B, 0x27, 0x28, 0x29, 0x2B,
            0x35, 0x33, 0x34, 0x92, 0x91, 0x90,
            0x39, 0x0E, 0x0F, 0x1C, 0x01,
            0xC8, 0xD0, 0xCB, 0xCD,
            0xC9, 0xD1, 0xC7, 0xCF, 0xD2, 0xD3,
            0x1D, 0x9D, 0x2A, 0x36,
            0x38, 0xB8, 0xDB, 0xDC,
            0x3A, 0x45, 0x46, 0xB7, 0xC5,
            0x37, 0x70, 0x79, 0x7B, 0x7D, 0x93,
            0x94, 0x95, 0x96, 0xDD, 0xDE, 0xDF,
    };

    public TransformerDescriptor() {
        super(InputDevices.TransformerKeyboard);
    }

    @Override
    public String getDeviceName() {
        return "Transformer Keyboard";
    }

    @Override
    public int getButtonCount() {
        return SCAN_CODES.length;
    }

    @Override
    public int getRewindTimeButton() {
        return -1;
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

        int value = 0;
        for (int i = SCAN_CODES.length - 1, count = 0; i >= 0; i--) {
            if (pressedValues[i] != 0) {
                value = (value << 8) | SCAN_CODES[i];
                if (++count == 3) {
                    break;
                }
            }
        }
        return (value << 8) | bits;
    }
}