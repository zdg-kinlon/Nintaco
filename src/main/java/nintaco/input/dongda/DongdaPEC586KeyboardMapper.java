package nintaco.input.dongda;

// TODO TAPE DRIVE NOT IMPLEMENTED

import nintaco.Machine;
import nintaco.input.DeviceMapper;
import nintaco.input.InputDevices;
import nintaco.input.icons.InputIcons;
import nintaco.mappers.Mapper;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.Serializable;

import static nintaco.input.dongda.DongdaPEC586KeyboardDescriptor.*;
import static nintaco.util.BitUtil.getBitBool;

public class DongdaPEC586KeyboardMapper
        extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int[][][] KEYS = {
            {{2, 2, 0},}, //   0 Esc
            {{14, 18, 2},}, //   1 Space
            null,               //   2 ?
            {{2, 12, 4},}, //   3 Caps Lock
            {{2, 18, 3}, {43, 18, 3},}, //   4 Left Control / Right Control    
            {{2, 6, 0},}, //   5 Grave accent
            {{2, 9, 3},}, //   6 Tab    
            {{2, 15, 5}, {41, 15, 5},}, //   7 Left Shift / Right Shift

            {{24, 2, 0},}, //   8 F6
            {{27, 2, 0},}, //   9 F7
            {{21, 2, 0},}, //  10 F5
            {{16, 2, 0},}, //  11 F4
            {{30, 2, 0},}, //  12 F8
            {{10, 2, 0},}, //  13 F2
            {{7, 2, 0},}, //  14 F1
            {{13, 2, 0},}, //  15 F3

            {{41, 6, 0},}, //  16 =
            {{58, 18, 5},}, //  17 Num 0
            {{64, 18, 0},}, //  18 Num .
            {{7, 12, 0},}, //  19 A
            {{40, 9, 1},}, //  20 Enter
            {{5, 6, 0},}, //  21 1
            {{6, 9, 0},}, //  22 Q
            {{8, 15, 0},}, //  23 Z

            null,               //  24 ?
            {{64, 15, 0},}, //  25 Num 3
            {{64, 12, 0},}, //  26 Num 6
            {{10, 12, 0},}, //  27 S
            {{64, 9, 0},}, //  28 Num 9
            {{8, 6, 0},}, //  29 2
            {{9, 9, 0},}, //  30 W
            {{11, 15, 0},}, //  31 X

            {{35, 15, 0},}, //  32 /
            {{61, 15, 0},}, //  33 Num 2
            {{61, 12, 0},}, //  34 Num 5
            {{13, 12, 0},}, //  35 D
            {{61, 9, 0},}, //  36 Num 8
            {{11, 6, 0},}, //  37 3
            {{12, 9, 0},}, //  38 E
            {{14, 15, 0},}, //  39 C

            {{54, 2, 0},}, //  40 Pause
            {{58, 15, 0},}, //  41 Num 1
            {{58, 12, 0},}, //  42 Num 4
            {{16, 12, 0},}, //  43 F
            {{58, 9, 0},}, //  44 Num 7
            {{14, 6, 0},}, //  45 4
            {{15, 9, 0},}, //  46 R
            {{17, 15, 0},}, //  47 V

            {{44, 6, 0},}, //  48 Backspace
            {{41, 6, 0},}, //  49 Backslash
            {{67, 15, 6},}, //  50 Num Enter
            {{19, 12, 0},}, //  51 G
            {{39, 9, 0},}, //  52 ]
            {{17, 6, 0},}, //  53 5
            {{18, 9, 0},}, //  54 T
            {{20, 15, 0},}, //  55 B

            {{29, 6, 0},}, //  56 9
            {{32, 15, 0},}, //  57 .
            {{31, 12, 0},}, //  58 L
            {{28, 12, 0},}, //  59 K
            {{30, 9, 0},}, //  60 O
            {{26, 6, 0},}, //  61 8
            {{27, 9, 0},}, //  62 I
            {{29, 15, 0},}, //  63 ,

            {{32, 6, 0},}, //  64 0
            null,               //  65 ?
            {{34, 12, 0},}, //  66 ;
            {{25, 12, 0},}, //  67 J
            {{33, 9, 0},}, //  68 P
            {{23, 6, 0},}, //  69 7
            {{24, 9, 0},}, //  70 U
            {{26, 15, 0},}, //  71 M

            {{35, 6, 0},}, //  72 -
            null,               //  73 ?
            {{37, 12, 0},}, //  74 '
            {{22, 12, 0},}, //  75 H
            {{36, 9, 0},}, //  76 [
            {{20, 6, 0},}, //  77 6
            {{21, 9, 0},}, //  78 Y
            {{23, 15, 0},}, //  79 N

            {{41, 2, 0},}, //  80 F11
            {{44, 2, 0},}, //  81 F12
            {{38, 2, 0},}, //  82 F10
            null,               //  83 ?
            null,               //  84 ?
            {{35, 2, 0},}, //  85 F9    
            null,               //  86 ?
            null,               //  87 ?

            {{51, 15, 0},}, //  88 Up
            {{54, 18, 0},}, //  89 Right
            {{51, 18, 0},}, //  90 Down
            {{54, 9, 0},}, //  91 Page Down
            {{48, 18, 0},}, //  92 Left
            {{64, 6, 0},}, //  93 Num *
            {{67, 6, 0},}, //  94 Num -
            {{67, 9, 6},}, //  95 Num +

            {{48, 6, 0},}, //  96 Insert
            null,               //  97 ?
            {{54, 6, 0},}, //  98 Page Up
            {{51, 6, 0},}, //  98 Home    
            {{48, 9, 0},}, // 100 Delete
            {{51, 9, 0},}, // 101 End
            {{61, 6, 0},}, // 102 Num /    
            {{58, 6, 0},}, // 103 Num Lock
    };

    private static final int CAPS_LOCK_INDICATOR_ADDRESS = 0x00EA;

    private final int[] Keys = new int[13];
    private final boolean[] PressedKeys = new boolean[104];

    private int strobe;
    private int row;
    private int column;

    private boolean capsLockInitialized;
    private boolean capsLockOn;

    private volatile Mapper mapper;

    @Override
    public int getInputDevice() {
        return InputDevices.DongdaPEC586Keyboard;
    }

    @Override
    public void setMachine(final Machine machine) {
        if (machine == null) {
            mapper = null;
        } else {
            mapper = machine.getMapper();
        }
    }

    @Override
    public void update(final int buttons) {
        for (int i = 12; i >= 0; --i) {
            Keys[i] = 0;
        }
        for (int i = 103; i >= 0; --i) {
            PressedKeys[i] = false;
        }

        setKeyBit((buttons >> 16) & 0x7F);
        setKeyBit((buttons >> 23) & 0x7F);
        if ((buttons & 0x80_00_00_00) != 0) {
            setKeyBit(KEY_MAP[KeyLeftShift]);
        }
        if ((buttons & 0x40_00_00_00) != 0) {
            setKeyBit(KEY_MAP[KeyLeftControl]);
        }

        final boolean capsOn = getBitBool(mapper.readMemory(
                CAPS_LOCK_INDICATOR_ADDRESS), 7);
        if (capsOn != capsLockOn || !capsLockInitialized) {
            capsLockInitialized = true;
            capsLockOn = capsOn;
            setCapsLockIndicator(capsLockOn);
        }
    }

    private void setCapsLockIndicator(final boolean lit) {
        try {
            Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_CAPS_LOCK,
                    lit);
        } catch (final UnsupportedOperationException u) {
        } catch (final Throwable t) {
            t.printStackTrace();
        }
    }

    private void setKeyBit(final int key) {
        if (key < 104) {
            Keys[key >> 3] |= 1 << (key & 7);
            PressedKeys[key] = true;
        }
    }

    @Override
    public void writePort(final int value) {
        if ((strobe & 2) == 0 && (value & 2) != 0) {
            row = 0;
        }
        if ((strobe & 1) != 0 && (value & 1) == 0) {
            column = 0;
        }
        if ((strobe & 4) != 0 && (value & 4) == 0 && ++row == 13) {
            row = 0;
        }
        strobe = value;
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 1) {
            int value = 0;
            if ((Keys[row] & (1 << (7 - column))) != 0) {
                value = 2;
            }
            ++column;
            column &= 7;
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        if (portIndex == 1) {
            int value = 0;
            if ((Keys[row] & (1 << (7 - column))) != 0) {
                value = 2;
            }
            return value;
        } else {
            return 0;
        }
    }

    private void render(final int[] screen, final int x, final int y,
                        final int[] K) {

        final InputIcons icon;
        switch (K[2]) {
            case 1:
                icon = InputIcons.SuborKeyboardEnter;
                break;
            case 2:
                icon = InputIcons.SuborKeyboardSpace;
                break;
            case 3:
                icon = InputIcons.SuborKeyboard3;
                break;
            case 4:
                icon = InputIcons.FamilyBasicKeyboardShift;
                break;
            case 5:
                icon = InputIcons.SuborKeyboard5;
                break;
            case 6:
                icon = InputIcons.SuborKeyboardVertical;
                break;
            default:
                icon = InputIcons.FamilyBasicKeyboardKey;
                break;
        }
        icon.render(screen, x + K[0], y + K[1]);
    }

    @Override
    public void render(final int[] screen) {
        final int x = 157;
        final int y = 205;
        InputIcons.SuborKeyboard.render(screen, x, y);
        for (int i = 103; i >= 0; i--) {
            if (PressedKeys[i]) {
                final int[][] K = KEYS[i];
                if (K != null) {
                    if (K.length == 2) {
                        render(screen, x, y, K[1]);
                    }
                    render(screen, x, y, K[0]);
                }
            }
        }
    }

    @Override
    public void close(final boolean saveNonVolatileData) {
        setCapsLockIndicator(false);
    }
}