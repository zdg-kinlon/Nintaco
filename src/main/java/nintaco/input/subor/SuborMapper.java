package nintaco.input.subor;

import nintaco.input.DeviceMapper;
import nintaco.input.InputDevices;
import nintaco.input.icons.InputIcons;

import java.io.Serializable;

import static java.lang.Math.min;
import static nintaco.input.subor.SuborDescriptor.*;
import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class SuborMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int[][][] KEYS = {
            {{14, 6, 0}, {58, 12, 0},}, //  0
            {{19, 12, 0},}, //  1
            {{16, 12, 0},}, //  2
            {{14, 15, 0},}, //  3

            {{10, 2, 0},}, //  4
            {{12, 9, 0},}, //  5
            {{17, 6, 0}, {61, 12, 0},}, //  6
            {{17, 15, 0},}, //  7

            {{8, 6, 0}, {61, 15, 0},}, //  8
            {{13, 12, 0},}, //  9
            {{10, 12, 0},}, // 10
            {{51, 9, 0},}, // 11

            {{7, 2, 0},}, // 12
            {{9, 9, 0},}, // 13
            {{11, 6, 0}, {64, 15, 0},}, // 14
            {{11, 15, 0},}, // 15

            {{48, 6, 0},}, // 16
            {{44, 6, 0},}, // 17
            {{54, 9, 0},}, // 18
            {{54, 18, 0},}, // 19

            {{30, 2, 0},}, // 20
            {{54, 6, 0},}, // 21
            {{48, 9, 0}, {64, 18, 0},}, // 22
            {{51, 6, 0},}, // 23

            {{29, 6, 0}, {64, 9, 0},}, // 24
            {{27, 9, 0},}, // 25
            {{31, 12, 0},}, // 26
            {{29, 15, 0},}, // 27

            {{21, 2, 0},}, // 28
            {{30, 9, 0},}, // 29
            {{32, 6, 0}, {58, 18, 5},}, // 30
            {{32, 15, 0},}, // 31

            {{39, 9, 0},}, // 32
            {{40, 9, 1}, {67, 15, 6},}, // 33
            {{51, 15, 0},}, // 34
            {{48, 18, 0},}, // 35

            {{27, 2, 0},}, // 36
            {{36, 9, 0},}, // 37
            {{41, 6, 0},}, // 38
            {{51, 18, 0},}, // 39

            {{6, 9, 0},}, // 40
            {{2, 12, 4},}, // 41
            {{8, 15, 0},}, // 42
            {{2, 9, 3},}, // 43

            {{2, 2, 0},}, // 44
            {{7, 12, 0},}, // 45
            {{5, 6, 0}, {58, 15, 0},}, // 46
            {{2, 18, 3}, {43, 18, 3},}, // 47

            {{23, 6, 0}, {58, 9, 0},}, // 48
            {{21, 9, 0},}, // 49
            {{28, 12, 0},}, // 50
            {{26, 15, 0},}, // 51

            {{16, 2, 0},}, // 52
            {{24, 9, 0},}, // 53
            {{26, 6, 0}, {61, 9, 0},}, // 54
            {{25, 12, 0},}, // 55

            {{35, 6, 0}, {67, 6, 0},}, // 56
            {{34, 12, 0},}, // 57
            {{37, 12, 0},}, // 58
            {{35, 15, 0}, {61, 6, 0},}, // 59

            {{24, 2, 0},}, // 60
            {{33, 9, 0},}, // 61
            {{38, 6, 0},}, // 62
            {{2, 15, 5}, {41, 15, 5},}, // 63

            {{18, 9, 0},}, // 64
            {{22, 12, 0},}, // 65
            {{23, 15, 0},}, // 66
            {{14, 18, 2},}, // 67

            {{13, 2, 0},}, // 68
            {{15, 9, 0},}, // 69
            {{20, 6, 0}, {64, 12, 0},}, // 70
            {{20, 15, 0},}, // 71 
    };

    private final int[][] KeyMatrix = new int[10][2];
    private final boolean[] PressedKeys = new boolean[72];

    private int row;
    private int column;
    private boolean enabled;

    private int mouseButtons;
    private int deltaX;
    private int deltaY;
    private int dx;
    private int sx;
    private int dy;
    private int sy;
    private int byteIndex;
    private int shiftRegister;
    private boolean strobe;

    @Override
    public int getInputDevice() {
        return InputDevices.Subor;
    }

    @Override
    public void update(final int buttons) {
        if ((buttons & 0x0000_0100) != 0) {
            mouseButtons = (buttons & 0x0C00) >> 4;
            deltaX += (byte) (buttons >> 16);
            deltaY += (byte) (buttons >> 24);
        } else {
            for (int i = 9; i >= 0; i--) {
                KeyMatrix[i][0] = 0x1E;
                KeyMatrix[i][1] = 0x1E;
            }
            for (int i = 71; i >= 0; i--) {
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
        }
    }

    private void setKeyBit(final int key) {
        if (key < 72) {
            KeyMatrix[key >> 3][(key >> 2) & 1] &= ~(1 << (1 + (key & 3)));
            PressedKeys[key] = true;
        }
    }

    @Override
    public void writePort(final int value) {
        enabled = getBitBool(value, 2);
        if (enabled) {
            final int col = getBit(value, 1);
            if (column == 1 && col == 0) {
                if (++row == 10) {
                    row = 0;
                }
            }
            column = col;

            if (getBitBool(value, 0)) {
                row = 0;
            }
        }

        if (strobe) {
            if ((value & 0x06) != 0) {
                strobe = false;

                switch (byteIndex) {
                    case 0:
                        dx = 0;
                        sx = 0;
                        if (deltaX != 0) {
                            if (deltaX > 0) {
                                dx = min(31, deltaX);
                                deltaX -= dx;
                            } else {
                                dx = min(31, -deltaX);
                                deltaX += dx;
                                sx = 0x20;
                            }
                        }

                        dy = 0;
                        sy = 0;
                        if (deltaY != 0) {
                            if (deltaY > 0) {
                                dy = min(31, deltaY);
                                deltaY -= dy;
                            } else {
                                dy = min(31, -deltaY);
                                deltaY += dy;
                                sy = 0x08;
                            }
                        }

                        if (dx <= 1 && dy <= 1) {
                            shiftRegister = mouseButtons | sx | (dx << 4) | sy | (dy << 2);
                        } else {
                            shiftRegister = 0x01
                                    | mouseButtons | sx | (dx & 0x10) | sy | ((dy & 0x10) >> 2);
                            byteIndex = 1;
                        }
                        break;

                    case 1:
                        shiftRegister = 0x02 | mouseButtons | ((dx & 0x0F) << 2);
                        byteIndex = 2;
                        break;

                    case 2:
                        shiftRegister = 0x03 | mouseButtons | ((dy & 0x0F) << 2);
                        byteIndex = 0;
                        break;
                }
            }
        } else {
            strobe = getBitBool(value, 0);
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 1) {
            int value = 0;

            if (enabled) {
                value = KeyMatrix[row][column];
            } else {
                value = 0x1E;
            }

            value |= getBit(shiftRegister, 7);
            shiftRegister <<= 1;

            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        if (portIndex == 1) {
            int value;

            if (enabled) {
                value = KeyMatrix[row][column];
            } else {
                value = 0x1E;
            }

            value |= getBit(shiftRegister, 7);

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
        for (int i = 71; i >= 0; i--) {
            if (PressedKeys[i]) {
                final int[][] K = KEYS[i];
                if (K.length == 2) {
                    render(screen, x, y, K[1]);
                }
                render(screen, x, y, K[0]);
            }
        }
    }
}
