package cn.kinlon.emu.input.familybasic.keyboard;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;

public class KeyboardDescriptor extends DeviceDescriptor {

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
    public static final int KeyO = 14;
    public static final int KeyP = 15;
    public static final int KeyQ = 16;
    public static final int KeyR = 17;
    public static final int KeyS = 18;
    public static final int KeyT = 19;
    public static final int KeyU = 20;
    public static final int KeyV = 21;
    public static final int KeyW = 22;
    public static final int KeyX = 23;
    public static final int KeyY = 24;
    public static final int KeyZ = 25;

    public static final int Key0 = 26;
    public static final int Key1 = 27;
    public static final int Key2 = 28;
    public static final int Key3 = 29;
    public static final int Key4 = 30;
    public static final int Key5 = 31;
    public static final int Key6 = 32;
    public static final int Key7 = 33;
    public static final int Key8 = 34;
    public static final int Key9 = 35;

    public static final int KeyF1 = 36;
    public static final int KeyF2 = 37;
    public static final int KeyF3 = 38;
    public static final int KeyF4 = 39;
    public static final int KeyF5 = 40;
    public static final int KeyF6 = 41;
    public static final int KeyF7 = 42;
    public static final int KeyF8 = 43;

    public static final int KeyLeftBracket = 44;
    public static final int KeyRightBracket = 45;
    public static final int KeySemicolon = 46;
    public static final int KeyColon = 47;
    public static final int KeyComma = 48;
    public static final int KeyPeriod = 49;
    public static final int KeyAtSign = 50;
    public static final int KeyCaret = 51;
    public static final int KeyMinus = 52;
    public static final int KeyForwardSlash = 53;
    public static final int KeyUnderscore = 54;
    public static final int KeyYen = 55;

    public static final int KeyUp = 56;
    public static final int KeyDown = 57;
    public static final int KeyLeft = 58;
    public static final int KeyRight = 59;

    public static final int KeyReturn = 60;
    public static final int KeySpace = 61;
    public static final int KeyInsert = 62;
    public static final int KeyBackspace = 63;
    public static final int KeyHome = 64;
    public static final int KeyEscape = 65;
    public static final int KeyGraph = 66;
    public static final int KeyStop = 67;

    public static final int KeyLeftShift = 68;
    public static final int KeyRightShift = 69;
    public static final int KeyControl = 70;
    public static final int KeyKana = 71;

    public static final int RewindTime = 72;

    public static final String[] NAMES = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8",
            "[", "]", ";", ":", ",", ".", "@", "^", "-", "/", "_", "\u00A5",
            "Up", "Down", "Left", "Right",
            "Return", "Space", "Insert", "Backspace", "Home", "Escape", "Graph", "Stop",
            "Left Shift", "Right Shift", "Ctrl", "Kana",
            "Rewind Time"
    };
    public static final int[] KEY_MAP = new int[72];
    private static final Key[] DEFAULTS = {
            A, B, C, D, E, F, G, H, I, J, K, L, M,
            N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
            _0, _1, _2, _3, _4, _5, _6, _7, _8, _9,
            F1, F2, F3, F4, F5, F6, F7, F8,
            LBRACKET, RBRACKET, SEMICOLON, APOSTROPHE, COMMA, PERIOD, GRAVE, EQUALS,
            MINUS, SLASH, END, BACKSLASH,
            UP, DOWN, LEFT, RIGHT,
            RETURN, SPACE, INSERT, BACK, HOME, ESCAPE, TAB, PAUSE,
            LSHIFT, RSHIFT, LCONTROL, RCONTROL,
            DELETE
    };

    static {
        KEY_MAP[KeyF8] = 0;
        KEY_MAP[KeyReturn] = 1;
        KEY_MAP[KeyLeftBracket] = 2;
        KEY_MAP[KeyRightBracket] = 3;

        KEY_MAP[KeyKana] = 4;
        KEY_MAP[KeyRightShift] = 5;
        KEY_MAP[KeyYen] = 6;
        KEY_MAP[KeyStop] = 7;

        KEY_MAP[KeyF7] = 8;
        KEY_MAP[KeyAtSign] = 9;
        KEY_MAP[KeyColon] = 10;
        KEY_MAP[KeySemicolon] = 11;

        KEY_MAP[KeyUnderscore] = 12;
        KEY_MAP[KeyForwardSlash] = 13;
        KEY_MAP[KeyMinus] = 14;
        KEY_MAP[KeyCaret] = 15;

        KEY_MAP[KeyF6] = 16;
        KEY_MAP[KeyO] = 17;
        KEY_MAP[KeyL] = 18;
        KEY_MAP[KeyK] = 19;

        KEY_MAP[KeyPeriod] = 20;
        KEY_MAP[KeyComma] = 21;
        KEY_MAP[KeyP] = 22;
        KEY_MAP[Key0] = 23;

        KEY_MAP[KeyF5] = 24;
        KEY_MAP[KeyI] = 25;
        KEY_MAP[KeyU] = 26;
        KEY_MAP[KeyJ] = 27;

        KEY_MAP[KeyM] = 28;
        KEY_MAP[KeyN] = 29;
        KEY_MAP[Key9] = 30;
        KEY_MAP[Key8] = 31;

        KEY_MAP[KeyF4] = 32;
        KEY_MAP[KeyY] = 33;
        KEY_MAP[KeyG] = 34;
        KEY_MAP[KeyH] = 35;

        KEY_MAP[KeyB] = 36;
        KEY_MAP[KeyV] = 37;
        KEY_MAP[Key7] = 38;
        KEY_MAP[Key6] = 39;

        KEY_MAP[KeyF3] = 40;
        KEY_MAP[KeyT] = 41;
        KEY_MAP[KeyR] = 42;
        KEY_MAP[KeyD] = 43;

        KEY_MAP[KeyF] = 44;
        KEY_MAP[KeyC] = 45;
        KEY_MAP[Key5] = 46;
        KEY_MAP[Key4] = 47;

        KEY_MAP[KeyF2] = 48;
        KEY_MAP[KeyW] = 49;
        KEY_MAP[KeyS] = 50;
        KEY_MAP[KeyA] = 51;

        KEY_MAP[KeyX] = 52;
        KEY_MAP[KeyZ] = 53;
        KEY_MAP[KeyE] = 54;
        KEY_MAP[Key3] = 55;

        KEY_MAP[KeyF1] = 56;
        KEY_MAP[KeyEscape] = 57;
        KEY_MAP[KeyQ] = 58;
        KEY_MAP[KeyControl] = 59;

        KEY_MAP[KeyLeftShift] = 60;
        KEY_MAP[KeyGraph] = 61;
        KEY_MAP[Key1] = 62;
        KEY_MAP[Key2] = 63;

        KEY_MAP[KeyHome] = 64;
        KEY_MAP[KeyUp] = 65;
        KEY_MAP[KeyRight] = 66;
        KEY_MAP[KeyLeft] = 67;

        KEY_MAP[KeyDown] = 68;
        KEY_MAP[KeySpace] = 69;
        KEY_MAP[KeyBackspace] = 70;
        KEY_MAP[KeyInsert] = 71;
    }

    public KeyboardDescriptor() {
        super(InputDevices.Keyboard);
    }

    @Override
    public String getDeviceName() {
        return "Family Keyboard and Recorder";
    }

    @Override
    public int getButtonCount() {
        return 73;
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
    public int setButtonBits(int bits, final int consoleType, final int portIndex,
                             final int[] pressedValues) {

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        if (pressedValues[KeyLeftShift] != 0) {
            bits |= 0x40_00_00_00;
        }
        if (pressedValues[KeyRightShift] != 0) {
            bits |= 0x80_00_00_00;
        }
        if (pressedValues[KeyControl] != 0) {
            bits |= 0x00_00_04_00;
        }
        if (pressedValues[KeyKana] != 0) {
            bits |= 0x00_00_08_00;
        }

        {
            int pressed = 0;
            for (int i = KeyStop; i >= 0; i--) {
                if (pressedValues[i] != 0) {
                    if (pressed == 0) {
                        bits |= KEY_MAP[i] << 16;
                        pressed++;
                    } else {
                        bits |= KEY_MAP[i] << 23;
                        pressed++;
                        break;
                    }
                }
            }
            if (pressed == 0) {
                bits |= 0x3F_FF_00_00;
            } else if (pressed == 1) {
                bits |= 0x3F_80_00_00;
            }
        }

        return bits;
    }

    public void setKanaEnabled(final int[] pressedValues,
                               final boolean kanaEnabled) {
        pressedValues[KeyControl] = 1;
        pressedValues[kanaEnabled ? KeyV : KeyW] = 1;
    }

    public void pressKey(final int[] pressedValues, final char c) {
        switch (c) {
            case 'A':
            case 'a':
                pressedValues[KeyA] = 1;
                break;
            case 'B':
            case 'b':
                pressedValues[KeyB] = 1;
                break;
            case 'C':
            case 'c':
                pressedValues[KeyC] = 1;
                break;
            case 'D':
            case 'd':
                pressedValues[KeyD] = 1;
                break;
            case 'E':
            case 'e':
                pressedValues[KeyE] = 1;
                break;
            case 'F':
            case 'f':
                pressedValues[KeyF] = 1;
                break;
            case 'G':
            case 'g':
                pressedValues[KeyG] = 1;
                break;
            case 'H':
            case 'h':
                pressedValues[KeyH] = 1;
                break;
            case 'I':
            case 'i':
                pressedValues[KeyI] = 1;
                break;
            case 'J':
            case 'j':
                pressedValues[KeyJ] = 1;
                break;
            case 'K':
            case 'k':
                pressedValues[KeyK] = 1;
                break;
            case 'L':
            case 'l':
                pressedValues[KeyL] = 1;
                break;
            case 'M':
            case 'm':
                pressedValues[KeyM] = 1;
                break;
            case 'N':
            case 'n':
                pressedValues[KeyN] = 1;
                break;
            case 'O':
            case 'o':
                pressedValues[KeyO] = 1;
                break;
            case 'P':
            case 'p':
                pressedValues[KeyP] = 1;
                break;
            case 'Q':
            case 'q':
                pressedValues[KeyQ] = 1;
                break;
            case 'R':
            case 'r':
                pressedValues[KeyR] = 1;
                break;
            case 'S':
            case 's':
                pressedValues[KeyS] = 1;
                break;
            case 'T':
            case 't':
                pressedValues[KeyT] = 1;
                break;
            case 'U':
            case 'u':
                pressedValues[KeyU] = 1;
                break;
            case 'V':
            case 'v':
                pressedValues[KeyV] = 1;
                break;
            case 'W':
            case 'w':
                pressedValues[KeyW] = 1;
                break;
            case 'X':
            case 'x':
                pressedValues[KeyX] = 1;
                break;
            case 'Y':
            case 'y':
                pressedValues[KeyY] = 1;
                break;
            case 'Z':
            case 'z':
                pressedValues[KeyZ] = 1;
                break;

            case '0':
                pressedValues[Key0] = 1;
                break;
            case '1':
                pressedValues[Key1] = 1;
                break;
            case '2':
                pressedValues[Key2] = 1;
                break;
            case '3':
                pressedValues[Key3] = 1;
                break;
            case '4':
                pressedValues[Key4] = 1;
                break;
            case '5':
                pressedValues[Key5] = 1;
                break;
            case '6':
                pressedValues[Key6] = 1;
                break;
            case '7':
                pressedValues[Key7] = 1;
                break;
            case '8':
                pressedValues[Key8] = 1;
                break;
            case '9':
                pressedValues[Key9] = 1;
                break;

            case '[':
                pressedValues[KeyLeftBracket] = 1;
                break;
            case ']':
                pressedValues[KeyRightBracket] = 1;
                break;
            case ';':
                pressedValues[KeySemicolon] = 1;
                break;
            case ':':
            case '\uFF1A':
                pressedValues[KeyColon] = 1;
                break;
            case ',':
                pressedValues[KeyComma] = 1;
                break;
            case '.':
                pressedValues[KeyPeriod] = 1;
                break;
            case '@':
                pressedValues[KeyAtSign] = 1;
                break;
            case '^':
                pressedValues[KeyCaret] = 1;
                break;
            case '-':
                pressedValues[KeyMinus] = 1;
                break;
            case '/':
                pressedValues[KeyForwardSlash] = 1;
                break;
            case '_':
                pressedValues[KeyUnderscore] = 1;
                break;
            case '\\':
                pressedValues[KeyYen] = 1;
                break;

            case '\n':
                pressedValues[KeyReturn] = 1;
                break;
            case ' ':
                pressedValues[KeySpace] = 1;
                break;

            case '!':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key1] = 1;
                break;
            case '"':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key2] = 1;
                break;
            case '#':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key3] = 1;
                break;
            case '$':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key4] = 1;
                break;
            case '%':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key5] = 1;
                break;
            case '&':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key6] = 1;
                break;
            case '\'':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key7] = 1;
                break;
            case '(':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key8] = 1;
                break;
            case ')':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key9] = 1;
                break;
            case '=':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyMinus] = 1;
                break;
            case '+':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeySemicolon] = 1;
                break;
            case '*':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyColon] = 1;
                break;
            case '<':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyComma] = 1;
                break;
            case '>':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyPeriod] = 1;
                break;
            case '?':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyForwardSlash] = 1;
                break;

            case '\u30a2':
                pressedValues[Key1] = 1;
                break;
            case '\u30a4':
                pressedValues[Key2] = 1;
                break;
            case '\u30a6':
                pressedValues[Key3] = 1;
                break;
            case '\u30a8':
                pressedValues[Key4] = 1;
                break;
            case '\u30aa':
                pressedValues[Key5] = 1;
                break;
            case '\u30ca':
                pressedValues[Key6] = 1;
                break;
            case '\u30cb':
                pressedValues[Key7] = 1;
                break;
            case '\u30cc':
                pressedValues[Key8] = 1;
                break;
            case '\u30cd':
                pressedValues[Key9] = 1;
                break;
            case '\u30ce':
                pressedValues[Key0] = 1;
                break;
            case '\u30e9':
                pressedValues[KeyMinus] = 1;
                break;
            case '\u30ea':
                pressedValues[KeyCaret] = 1;
                break;
            case '\u30eb':
                pressedValues[KeyYen] = 1;
                break;

            case '\u30ab':
                pressedValues[KeyQ] = 1;
                break;
            case '\u30ad':
                pressedValues[KeyW] = 1;
                break;
            case '\u30af':
                pressedValues[KeyE] = 1;
                break;
            case '\u30b1':
                pressedValues[KeyR] = 1;
                break;
            case '\u30b3':
                pressedValues[KeyT] = 1;
                break;
            case '\u30cf':
                pressedValues[KeyY] = 1;
                break;
            case '\u30d2':
                pressedValues[KeyU] = 1;
                break;
            case '\u30d5':
                pressedValues[KeyI] = 1;
                break;
            case '\u30d8':
                pressedValues[KeyO] = 1;
                break;
            case '\u30db':
                pressedValues[KeyP] = 1;
                break;
            case '\u30ec':
                pressedValues[KeyAtSign] = 1;
                break;
            case '\u30ed':
                pressedValues[KeyLeftBracket] = 1;
                break;

            case '\u30b5':
                pressedValues[KeyA] = 1;
                break;
            case '\u30b7':
                pressedValues[KeyS] = 1;
                break;
            case '\u30b9':
                pressedValues[KeyD] = 1;
                break;
            case '\u30bb':
                pressedValues[KeyF] = 1;
                break;
            case '\u30bd':
                pressedValues[KeyG] = 1;
                break;
            case '\u30de':
                pressedValues[KeyH] = 1;
                break;
            case '\u30df':
                pressedValues[KeyJ] = 1;
                break;
            case '\u30e0':
                pressedValues[KeyK] = 1;
                break;
            case '\u30e1':
                pressedValues[KeyL] = 1;
                break;
            case '\u30e2':
                pressedValues[KeySemicolon] = 1;
                break;
            case '\u30fc':
                pressedValues[KeyColon] = 1;
                break;
            case '\u3002':
                pressedValues[KeyRightBracket] = 1;
                break;

            case '\u30bf':
                pressedValues[KeyZ] = 1;
                break;
            case '\u30c1':
                pressedValues[KeyX] = 1;
                break;
            case '\u30c4':
                pressedValues[KeyC] = 1;
                break;
            case '\u30c6':
                pressedValues[KeyV] = 1;
                break;
            case '\u30c8':
                pressedValues[KeyB] = 1;
                break;
            case '\u30e4':
                pressedValues[KeyN] = 1;
                break;
            case '\u30e6':
                pressedValues[KeyM] = 1;
                break;
            case '\u30e8':
                pressedValues[KeyComma] = 1;
                break;
            case '\u30ef':
                pressedValues[KeyPeriod] = 1;
                break;
            case '\u30f2':
                pressedValues[KeyForwardSlash] = 1;
                break;
            case '\u30f3':
                pressedValues[KeyUnderscore] = 1;
                break;

            case '\u30a1':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key1] = 1;
                break;
            case '\u30a3':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key2] = 1;
                break;
            case '\u30a5':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key3] = 1;
                break;
            case '\u30a7':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key4] = 1;
                break;
            case '\u30a9':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[Key5] = 1;
                break;

            case '\u30d1':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyY] = 1;
                break;
            case '\u30d4':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyU] = 1;
                break;
            case '\u30d7':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyI] = 1;
                break;
            case '\u30da':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyO] = 1;
                break;
            case '\u30dd':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyP] = 1;
                break;
            case '\u300c':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyLeftBracket] = 1;
                break;

            case '\u300d':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyRightBracket] = 1;
                break;

            case '\u2423':
                pressedValues[KeyLeftShift] = 1;
                pressedValues[KeyUnderscore] = 1;
                break;
        }
    }
}