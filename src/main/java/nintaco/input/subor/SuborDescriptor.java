package nintaco.input.subor;

import net.java.games.input.Component;
import nintaco.input.ButtonMapping;
import nintaco.input.DeviceDescriptor;
import nintaco.input.InputDevices;
import nintaco.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;
import static nintaco.util.MathUtil.clamp;

public class SuborDescriptor extends DeviceDescriptor {

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
    public static final int KeyComma = 47;
    public static final int KeyPeriod = 48;
    public static final int KeyApostrophe = 49;
    public static final int KeyMinus = 50;
    public static final int KeyEquals = 51;
    public static final int KeyForwardSlash = 52;
    public static final int KeyBackslash = 53;

    public static final int KeyUp = 54;
    public static final int KeyDown = 55;
    public static final int KeyLeft = 56;
    public static final int KeyRight = 57;

    public static final int KeyEnter = 58;
    public static final int KeySpace = 59;
    public static final int KeyTab = 60;
    public static final int KeyBackspace = 61;
    public static final int KeyInsert = 62;
    public static final int KeyDelete = 63;
    public static final int KeyHome = 64;
    public static final int KeyEnd = 65;
    public static final int KeyPageUp = 66;
    public static final int KeyPageDown = 67;
    public static final int KeyEscape = 68;

    public static final int KeyCapsLock = 69;
    public static final int KeyLeftShift = 70;
    public static final int KeyRightShift = 71;
    public static final int KeyLeftControl = 72;
    public static final int KeyRightControl = 73;

    public static final int MouseLeft = 74;
    public static final int MouseRight = 75;

    public static final int RewindTime = 76;

    public static final String[] NAMES = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8",
            "[", "]", ";", ",", ".", "'", "-", "=", "/", "\\",
            "Up", "Down", "Left", "Right",
            "Enter", "Space", "Tab", "Backspace", "Insert", "Delete", "Home", "End",
            "Page Up", "Page Down", "Escape",
            "Caps Lock", "Left Shift", "Right Shift", "Left Ctrl", "Right Ctrl",
            "(Mouse) Left", "(Mouse) Right",
            "Rewind Time"
    };
    public static final int[] KEY_MAP = new int[74];
    private static final Component.Identifier[] DEFAULTS = {
            A, B, C, D, E, F, G, H, I, J, K, L, M,
            N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
            _0, _1, _2, _3, _4, _5, _6, _7, _8, _9,
            F1, F2, F3, F4, F5, F6, F7, F8,
            LBRACKET, RBRACKET, SEMICOLON, COMMA, PERIOD, APOSTROPHE, MINUS, EQUALS,
            SLASH, BACKSLASH,
            UP, DOWN, LEFT, RIGHT,
            RETURN, SPACE, TAB, BACK, INSERT, DELETE, HOME, END,
            PAGEUP, PAGEDOWN, ESCAPE,
            CAPITAL, LSHIFT, RSHIFT, LCONTROL, RCONTROL,
            Button.LEFT, Button.RIGHT,
            GRAVE,
    };

    static {
        KEY_MAP[Key4] = 0;
        KEY_MAP[KeyG] = 1;
        KEY_MAP[KeyF] = 2;
        KEY_MAP[KeyC] = 3;

        KEY_MAP[KeyF2] = 4;
        KEY_MAP[KeyE] = 5;
        KEY_MAP[Key5] = 6;
        KEY_MAP[KeyV] = 7;

        KEY_MAP[Key2] = 8;
        KEY_MAP[KeyD] = 9;
        KEY_MAP[KeyS] = 10;
        KEY_MAP[KeyEnd] = 11;

        KEY_MAP[KeyF1] = 12;
        KEY_MAP[KeyW] = 13;
        KEY_MAP[Key3] = 14;
        KEY_MAP[KeyX] = 15;

        KEY_MAP[KeyInsert] = 16;
        KEY_MAP[KeyBackspace] = 17;
        KEY_MAP[KeyPageDown] = 18;
        KEY_MAP[KeyRight] = 19;

        KEY_MAP[KeyF8] = 20;
        KEY_MAP[KeyPageUp] = 21;
        KEY_MAP[KeyDelete] = 22;
        KEY_MAP[KeyHome] = 23;

        KEY_MAP[Key9] = 24;
        KEY_MAP[KeyI] = 25;
        KEY_MAP[KeyL] = 26;
        KEY_MAP[KeyComma] = 27;

        KEY_MAP[KeyF5] = 28;
        KEY_MAP[KeyO] = 29;
        KEY_MAP[Key0] = 30;
        KEY_MAP[KeyPeriod] = 31;

        KEY_MAP[KeyRightBracket] = 32;
        KEY_MAP[KeyEnter] = 33;
        KEY_MAP[KeyUp] = 34;
        KEY_MAP[KeyLeft] = 35;

        KEY_MAP[KeyF7] = 36;
        KEY_MAP[KeyLeftBracket] = 37;
        KEY_MAP[KeyBackslash] = 38;
        KEY_MAP[KeyDown] = 39;

        KEY_MAP[KeyQ] = 40;
        KEY_MAP[KeyCapsLock] = 41;
        KEY_MAP[KeyZ] = 42;
        KEY_MAP[KeyTab] = 43;

        KEY_MAP[KeyEscape] = 44;
        KEY_MAP[KeyA] = 45;
        KEY_MAP[Key1] = 46;
        KEY_MAP[KeyLeftControl] = KEY_MAP[KeyRightControl] = 47;

        KEY_MAP[Key7] = 48;
        KEY_MAP[KeyY] = 49;
        KEY_MAP[KeyK] = 50;
        KEY_MAP[KeyM] = 51;

        KEY_MAP[KeyF4] = 52;
        KEY_MAP[KeyU] = 53;
        KEY_MAP[Key8] = 54;
        KEY_MAP[KeyJ] = 55;

        KEY_MAP[KeyMinus] = 56;
        KEY_MAP[KeySemicolon] = 57;
        KEY_MAP[KeyApostrophe] = 58;
        KEY_MAP[KeyForwardSlash] = 59;

        KEY_MAP[KeyF6] = 60;
        KEY_MAP[KeyP] = 61;
        KEY_MAP[KeyEquals] = 62;
        KEY_MAP[KeyLeftShift] = KEY_MAP[KeyRightShift] = 63;

        KEY_MAP[KeyT] = 64;
        KEY_MAP[KeyH] = 65;
        KEY_MAP[KeyN] = 66;
        KEY_MAP[KeySpace] = 67;

        KEY_MAP[KeyF3] = 68;
        KEY_MAP[KeyR] = 69;
        KEY_MAP[Key6] = 70;
        KEY_MAP[KeyB] = 71;
    }

    private boolean keyboard;

    public SuborDescriptor() {
        super(InputDevices.Subor);
    }

    @Override
    public String getDeviceName() {
        return "Subor Keyboard and Mouse";
    }

    @Override
    public int getButtonCount() {
        return 77;
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
        return getDefaultButtonMapping((buttonIndex == MouseLeft
                || buttonIndex == MouseRight) ? InputUtil.getDefaultMouse()
                : InputUtil.getDefaultKeyboard(), buttonIndex, DEFAULTS);
    }

    @Override
    public int setButtonBits(int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        if (keyboard) {
            keyboard = false;
            if (pressedValues[KeyLeftShift] != 0
                    || pressedValues[KeyRightShift] != 0) {
                bits |= 0x80_00_00_00;
            }
            if (pressedValues[KeyLeftControl] != 0
                    || pressedValues[KeyRightControl] != 0) {
                bits |= 0x40_00_00_00;
            }

            {
                int pressed = 0;
                for (int i = KeyCapsLock; i >= 0; i--) {
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
        } else {
            keyboard = true;
            bits |= 0x0000_0100;

            if (pressedValues[MouseLeft] != 0) {
                bits |= 0x0000_0800;
            }
            if (pressedValues[MouseRight] != 0) {
                bits |= 0x0000_0400;
            }

            final int dx = clamp(((int) InputUtil.getMouseDeltaX()) >> 1, -128, 127);
            final int dy = clamp(((int) InputUtil.getMouseDeltaY()) >> 1, -128, 127);

            return ((dy & 0xFF) << 24) | ((dx & 0xFF) << 16) | bits;
        }
    }

    public void pressKey(final int[] pressedValues, final char c) {
        switch (c) {
            case 'A':
                pressedValues[KeyLeftShift] = 1;
            case 'a':
                pressedValues[KeyA] = 1;
                break;

            case 'B':
                pressedValues[KeyLeftShift] = 1;
            case 'b':
                pressedValues[KeyB] = 1;
                break;

            case 'C':
                pressedValues[KeyLeftShift] = 1;
            case 'c':
                pressedValues[KeyC] = 1;
                break;

            case 'D':
                pressedValues[KeyLeftShift] = 1;
            case 'd':
                pressedValues[KeyD] = 1;
                break;

            case 'E':
                pressedValues[KeyLeftShift] = 1;
            case 'e':
                pressedValues[KeyE] = 1;
                break;

            case 'F':
                pressedValues[KeyLeftShift] = 1;
            case 'f':
                pressedValues[KeyF] = 1;
                break;

            case 'G':
                pressedValues[KeyLeftShift] = 1;
            case 'g':
                pressedValues[KeyG] = 1;
                break;

            case 'H':
                pressedValues[KeyLeftShift] = 1;
            case 'h':
                pressedValues[KeyH] = 1;
                break;

            case 'I':
                pressedValues[KeyLeftShift] = 1;
            case 'i':
                pressedValues[KeyI] = 1;
                break;

            case 'J':
                pressedValues[KeyLeftShift] = 1;
            case 'j':
                pressedValues[KeyJ] = 1;
                break;

            case 'K':
                pressedValues[KeyLeftShift] = 1;
            case 'k':
                pressedValues[KeyK] = 1;
                break;

            case 'L':
                pressedValues[KeyLeftShift] = 1;
            case 'l':
                pressedValues[KeyL] = 1;
                break;

            case 'M':
                pressedValues[KeyLeftShift] = 1;
            case 'm':
                pressedValues[KeyM] = 1;
                break;

            case 'N':
                pressedValues[KeyLeftShift] = 1;
            case 'n':
                pressedValues[KeyN] = 1;
                break;

            case 'O':
                pressedValues[KeyLeftShift] = 1;
            case 'o':
                pressedValues[KeyO] = 1;
                break;

            case 'P':
                pressedValues[KeyLeftShift] = 1;
            case 'p':
                pressedValues[KeyP] = 1;
                break;

            case 'Q':
                pressedValues[KeyLeftShift] = 1;
            case 'q':
                pressedValues[KeyQ] = 1;
                break;

            case 'R':
                pressedValues[KeyLeftShift] = 1;
            case 'r':
                pressedValues[KeyR] = 1;
                break;

            case 'S':
                pressedValues[KeyLeftShift] = 1;
            case 's':
                pressedValues[KeyS] = 1;
                break;

            case 'T':
                pressedValues[KeyLeftShift] = 1;
            case 't':
                pressedValues[KeyT] = 1;
                break;

            case 'U':
                pressedValues[KeyLeftShift] = 1;
            case 'u':
                pressedValues[KeyU] = 1;
                break;

            case 'V':
                pressedValues[KeyLeftShift] = 1;
            case 'v':
                pressedValues[KeyV] = 1;
                break;

            case 'W':
                pressedValues[KeyLeftShift] = 1;
            case 'w':
                pressedValues[KeyW] = 1;
                break;

            case 'X':
                pressedValues[KeyLeftShift] = 1;
            case 'x':
                pressedValues[KeyX] = 1;
                break;

            case 'Y':
                pressedValues[KeyLeftShift] = 1;
            case 'y':
                pressedValues[KeyY] = 1;
                break;

            case 'Z':
                pressedValues[KeyLeftShift] = 1;
            case 'z':
                pressedValues[KeyZ] = 1;
                break;

            case ')':
                pressedValues[KeyLeftShift] = 1;
            case '0':
                pressedValues[Key0] = 1;
                break;

            case '!':
                pressedValues[KeyLeftShift] = 1;
            case '1':
                pressedValues[Key1] = 1;
                break;

            case '@':
                pressedValues[KeyLeftShift] = 1;
            case '2':
                pressedValues[Key2] = 1;
                break;

            case '#':
                pressedValues[KeyLeftShift] = 1;
            case '3':
                pressedValues[Key3] = 1;
                break;

            case '$':
                pressedValues[KeyLeftShift] = 1;
            case '4':
                pressedValues[Key4] = 1;
                break;

            case '%':
                pressedValues[KeyLeftShift] = 1;
            case '5':
                pressedValues[Key5] = 1;
                break;

            case '^':
                pressedValues[KeyLeftShift] = 1;
            case '6':
                pressedValues[Key6] = 1;
                break;

            case '&':
                pressedValues[KeyLeftShift] = 1;
            case '7':
                pressedValues[Key7] = 1;
                break;

            case '*':
                pressedValues[KeyLeftShift] = 1;
            case '8':
                pressedValues[Key8] = 1;
                break;

            case '(':
                pressedValues[KeyLeftShift] = 1;
            case '9':
                pressedValues[Key9] = 1;
                break;

            case '{':
                pressedValues[KeyLeftShift] = 1;
            case '[':
                pressedValues[KeyLeftBracket] = 1;
                break;

            case '}':
                pressedValues[KeyLeftShift] = 1;
            case ']':
                pressedValues[KeyRightBracket] = 1;
                break;

            case ':':
                pressedValues[KeyLeftShift] = 1;
            case ';':
                pressedValues[KeySemicolon] = 1;
                break;

            case '<':
                pressedValues[KeyLeftShift] = 1;
            case ',':
                pressedValues[KeyComma] = 1;
                break;

            case '>':
                pressedValues[KeyLeftShift] = 1;
            case '.':
                pressedValues[KeyPeriod] = 1;
                break;

            case '"':
                pressedValues[KeyLeftShift] = 1;
            case '\'':
                pressedValues[KeyMinus] = 1;
                break;

            case '_':
                pressedValues[KeyLeftShift] = 1;
            case '-':
                pressedValues[KeyMinus] = 1;
                break;

            case '+':
                pressedValues[KeyLeftShift] = 1;
            case '=':
                pressedValues[KeyEquals] = 1;
                break;

            case '?':
                pressedValues[KeyLeftShift] = 1;
            case '/':
                pressedValues[KeyForwardSlash] = 1;
                break;

            case '|':
                pressedValues[KeyLeftShift] = 1;
            case '\\':
                pressedValues[KeyBackslash] = 1;
                break;

            case '\n':
                pressedValues[KeyEnter] = 1;
                break;
            case '\t':
                pressedValues[KeyTab] = 1;
                break;
            case ' ':
                pressedValues[KeySpace] = 1;
                break;
        }
    }
}