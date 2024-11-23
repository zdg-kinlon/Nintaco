package nintaco.input.dongda;

import net.java.games.input.Component;
import nintaco.input.*;

import static net.java.games.input.Component.Identifier.Key.*;

public class DongdaPEC586KeyboardDescriptor extends DeviceDescriptor {

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

    public static final int KeyNum0 = 36;
    public static final int KeyNum1 = 37;
    public static final int KeyNum2 = 38;
    public static final int KeyNum3 = 39;
    public static final int KeyNum4 = 40;
    public static final int KeyNum5 = 41;
    public static final int KeyNum6 = 42;
    public static final int KeyNum7 = 43;
    public static final int KeyNum8 = 44;
    public static final int KeyNum9 = 45;

    public static final int KeyNumEnter = 46;
    public static final int KeyNumAdd = 47;
    public static final int KeyNumSubtract = 48;
    public static final int KeyNumMultiply = 49;
    public static final int KeyNumDivide = 50;
    public static final int KeyNumDecimal = 51;

    public static final int KeyF1 = 52;
    public static final int KeyF2 = 53;
    public static final int KeyF3 = 54;
    public static final int KeyF4 = 55;
    public static final int KeyF5 = 56;
    public static final int KeyF6 = 57;
    public static final int KeyF7 = 58;
    public static final int KeyF8 = 59;
    public static final int KeyF9 = 60;
    public static final int KeyF10 = 61;
    public static final int KeyF11 = 62;
    public static final int KeyF12 = 63;

    public static final int KeyLeftBracket = 64;
    public static final int KeyRightBracket = 65;
    public static final int KeySemicolon = 66;
    public static final int KeyComma = 67;
    public static final int KeyPeriod = 68;
    public static final int KeyApostrophe = 69;
    public static final int KeyMinus = 70;
    public static final int KeyEquals = 71;
    public static final int KeyForwardSlash = 72;
    public static final int KeyBackslash = 73;
    public static final int KeyGraveAccent = 74;

    public static final int KeyUp = 75;
    public static final int KeyDown = 76;
    public static final int KeyLeft = 77;
    public static final int KeyRight = 78;

    public static final int KeyEnter = 79;
    public static final int KeySpace = 80;
    public static final int KeyTab = 81;
    public static final int KeyBackspace = 82;
    public static final int KeyInsert = 83;
    public static final int KeyDelete = 84;
    public static final int KeyHome = 85;
    public static final int KeyEnd = 86;
    public static final int KeyPageUp = 87;
    public static final int KeyPageDown = 88;
    public static final int KeyEscape = 89;
    public static final int KeyPause = 90;

    public static final int KeyCapsLock = 91;
    public static final int KeyNumLock = 92;
    public static final int KeyLeftShift = 93;
    public static final int KeyRightShift = 94;
    public static final int KeyLeftControl = 95;
    public static final int KeyRightControl = 96;

    public static final int RewindTime = 97;

    public static final String[] NAMES = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "Num 0", "Num 1", "Num 2", "Num 3", "Num 4", "Num 5", "Num 6", "Num 7",
            "Num 8", "Num 9", "Num Enter", "Num +", "Num -", "Num *", "Num /", "Num .",
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12",
            "[", "]", ";", ",", ".", "'", "-", "=", "/", "\\", "`",
            "Up", "Down", "Left", "Right",
            "Enter", "Space", "Tab", "Backspace", "Insert", "Delete", "Home", "End",
            "Page Up", "Page Down", "Escape", "Pause | Break",
            "Caps Lock", "Num Lock", "Left Shift", "Right Shift", "Left Ctrl",
            "Right Ctrl",
            "Rewind Time"
    };
    public static final int[] KEY_MAP = new int[97];
    private static final Component.Identifier[] DEFAULTS = {
            A, B, C, D, E, F, G, H, I, J, K, L, M,
            N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
            _0, _1, _2, _3, _4, _5, _6, _7, _8, _9,
            NUMPAD0, NUMPAD1, NUMPAD2, NUMPAD3, NUMPAD4, NUMPAD5, NUMPAD6, NUMPAD7,
            NUMPAD8, NUMPAD9,
            NUMPADENTER, ADD, SUBTRACT, MULTIPLY, DIVIDE, DECIMAL,
            F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12,
            LBRACKET, RBRACKET, SEMICOLON, COMMA, PERIOD, APOSTROPHE, MINUS, EQUALS,
            SLASH, BACKSLASH, GRAVE,
            UP, DOWN, LEFT, RIGHT,
            RETURN, SPACE, TAB, BACK, INSERT, DELETE, HOME, END,
            PAGEUP, PAGEDOWN, ESCAPE, PAUSE,
            CAPITAL, NUMLOCK, LSHIFT, RSHIFT, LCONTROL, RCONTROL,
    };

    static {
        KEY_MAP[KeyEscape] = 0;
        KEY_MAP[KeySpace] = 1;
        KEY_MAP[KeyCapsLock] = 3;
        KEY_MAP[KeyLeftControl] = KEY_MAP[KeyRightControl] = 4;
        KEY_MAP[KeyGraveAccent] = 5;
        KEY_MAP[KeyTab] = 6;
        KEY_MAP[KeyLeftShift] = KEY_MAP[KeyRightShift] = 7;

        KEY_MAP[KeyF6] = 8;
        KEY_MAP[KeyF7] = 9;
        KEY_MAP[KeyF5] = 10;
        KEY_MAP[KeyF4] = 11;
        KEY_MAP[KeyF8] = 12;
        KEY_MAP[KeyF2] = 13;
        KEY_MAP[KeyF1] = 14;
        KEY_MAP[KeyF3] = 15;

        KEY_MAP[KeyEquals] = 16;
        KEY_MAP[KeyNum0] = 17;
        KEY_MAP[KeyNumDecimal] = 18;
        KEY_MAP[KeyA] = 19;
        KEY_MAP[KeyEnter] = 20;
        KEY_MAP[Key1] = 21;
        KEY_MAP[KeyQ] = 22;
        KEY_MAP[KeyZ] = 23;

        KEY_MAP[KeyNum3] = 25;
        KEY_MAP[KeyNum6] = 26;
        KEY_MAP[KeyS] = 27;
        KEY_MAP[KeyNum9] = 28;
        KEY_MAP[Key2] = 29;
        KEY_MAP[KeyW] = 30;
        KEY_MAP[KeyX] = 31;

        KEY_MAP[KeyForwardSlash] = 32;
        KEY_MAP[KeyNum2] = 33;
        KEY_MAP[KeyNum5] = 34;
        KEY_MAP[KeyD] = 35;
        KEY_MAP[KeyNum8] = 36;
        KEY_MAP[Key3] = 37;
        KEY_MAP[KeyE] = 38;
        KEY_MAP[KeyC] = 39;

        KEY_MAP[KeyPause] = 40;
        KEY_MAP[KeyNum1] = 41;
        KEY_MAP[KeyNum4] = 42;
        KEY_MAP[KeyF] = 43;
        KEY_MAP[KeyNum7] = 44;
        KEY_MAP[Key4] = 45;
        KEY_MAP[KeyR] = 46;
        KEY_MAP[KeyV] = 47;

        KEY_MAP[KeyBackspace] = 48;
        KEY_MAP[KeyBackslash] = 49;
        KEY_MAP[KeyNumEnter] = 50;
        KEY_MAP[KeyG] = 51;
        KEY_MAP[KeyRightBracket] = 52;
        KEY_MAP[Key5] = 53;
        KEY_MAP[KeyT] = 54;
        KEY_MAP[KeyB] = 55;

        KEY_MAP[Key9] = 56;
        KEY_MAP[KeyPeriod] = 57;
        KEY_MAP[KeyL] = 58;
        KEY_MAP[KeyK] = 59;
        KEY_MAP[KeyO] = 60;
        KEY_MAP[Key8] = 61;
        KEY_MAP[KeyI] = 62;
        KEY_MAP[KeyComma] = 63;

        KEY_MAP[Key0] = 64;
        KEY_MAP[KeySemicolon] = 66;
        KEY_MAP[KeyJ] = 67;
        KEY_MAP[KeyP] = 68;
        KEY_MAP[Key7] = 69;
        KEY_MAP[KeyU] = 70;
        KEY_MAP[KeyM] = 71;

        KEY_MAP[KeyMinus] = 72;
        KEY_MAP[KeyApostrophe] = 74;
        KEY_MAP[KeyH] = 75;
        KEY_MAP[KeyLeftBracket] = 76;
        KEY_MAP[Key6] = 77;
        KEY_MAP[KeyY] = 78;
        KEY_MAP[KeyN] = 79;

        KEY_MAP[KeyF11] = 80;
        KEY_MAP[KeyF12] = 81;
        KEY_MAP[KeyF10] = 82;
        KEY_MAP[KeyF9] = 85;

        KEY_MAP[KeyUp] = 88;
        KEY_MAP[KeyRight] = 89;
        KEY_MAP[KeyDown] = 90;
        KEY_MAP[KeyPageDown] = 91;
        KEY_MAP[KeyLeft] = 92;
        KEY_MAP[KeyNumMultiply] = 93;
        KEY_MAP[KeyNumSubtract] = 94;
        KEY_MAP[KeyNumAdd] = 95;

        KEY_MAP[KeyInsert] = 96;
        KEY_MAP[KeyPageUp] = 98;
        KEY_MAP[KeyHome] = 99;
        KEY_MAP[KeyDelete] = 100;
        KEY_MAP[KeyEnd] = 101;
        KEY_MAP[KeyNumDivide] = 102;
        KEY_MAP[KeyNumLock] = 103;
    }

    public DongdaPEC586KeyboardDescriptor() {
        super(InputDevices.DongdaPEC586Keyboard);
    }

    @Override
    public String getDeviceName() {
        return "Dongda PEC-586 Keyboard";
    }

    @Override
    public int getButtonCount() {
        return 98;
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
        if (buttonIndex == RewindTime) {
            return getButtonMapping(buttonIndex, new ButtonID[]{
                    new ButtonID(InputUtil.getDefaultKeyboard(), RSHIFT.getName(), 1),
                    new ButtonID(InputUtil.getDefaultKeyboard(), BACK.getName(), 1)});
        } else {
            return getDefaultButtonMapping(InputUtil.getDefaultKeyboard(),
                    buttonIndex, DEFAULTS);
        }
    }

    @Override
    public int setButtonBits(int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        if (pressedValues[KeyLeftShift] != 0
                || pressedValues[KeyRightShift] != 0) {
            bits |= 0x80_00_00_00;
        }
        if (pressedValues[KeyLeftControl] != 0
                || pressedValues[KeyRightControl] != 0) {
            bits |= 0x40_00_00_00;
        }

        int pressed = 0;
        for (int i = KeyNumLock; i >= 0; --i) {
            if (pressedValues[i] != 0) {
                if (pressed == 0) {
                    bits |= KEY_MAP[i] << 16;
                    ++pressed;
                } else {
                    bits |= KEY_MAP[i] << 23;
                    ++pressed;
                    break;
                }
            }
        }
        if (pressed == 0) {
            bits |= 0x3F_FF_00_00;
        } else if (pressed == 1) {
            bits |= 0x3F_80_00_00;
        }

        return bits;
    }
}
