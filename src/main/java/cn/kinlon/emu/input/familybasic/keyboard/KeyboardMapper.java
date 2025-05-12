package cn.kinlon.emu.input.familybasic.keyboard;

import cn.kinlon.emu.Machine;
import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.familybasic.datarecorder.DataRecorderMapper;
import cn.kinlon.emu.input.icons.InputIcons;

import java.io.Serializable;

import static cn.kinlon.emu.input.familybasic.keyboard.KeyboardDescriptor.*;
import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class KeyboardMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int[][] KEYS = {
            {39, 2, 1,}, //  0
            {41, 8, 1,}, //  1
            {38, 8, 0,}, //  2
            {39, 11, 0,}, //  3

            {42, 11, 0,}, //  4
            {40, 14, 1,}, //  5
            {39, 5, 0,}, //  6
            {42, 5, 0,}, //  7

            {34, 2, 1,}, //  8
            {35, 8, 0,}, //  9
            {36, 11, 0,}, // 10
            {33, 11, 0,}, // 11

            {37, 14, 0,}, // 12
            {34, 14, 0,}, // 13
            {33, 5, 0,}, // 14
            {36, 5, 0,}, // 15

            {29, 2, 1,}, // 16
            {29, 8, 0,}, // 17
            {30, 11, 0,}, // 18
            {27, 11, 0,}, // 19

            {31, 14, 0,}, // 20
            {28, 14, 0,}, // 21
            {32, 8, 0,}, // 22
            {30, 5, 0,}, // 23

            {24, 2, 1,}, // 24
            {26, 8, 0,}, // 25
            {23, 8, 0,}, // 26
            {24, 11, 0,}, // 27

            {25, 14, 0,}, // 28
            {22, 14, 0,}, // 29
            {27, 5, 0,}, // 30
            {24, 5, 0,}, // 31

            {19, 2, 1,}, // 32
            {20, 8, 0,}, // 33
            {18, 11, 0,}, // 34
            {15, 11, 0,}, // 35

            {19, 14, 0,}, // 36
            {16, 14, 0,}, // 37
            {21, 5, 0,}, // 38
            {18, 5, 0,}, // 39

            {14, 2, 1,}, // 40
            {17, 8, 0,}, // 41
            {14, 8, 0,}, // 42
            {12, 11, 0,}, // 43

            {15, 11, 0,}, // 44
            {13, 14, 0,}, // 45
            {15, 5, 0,}, // 46
            {12, 5, 0,}, // 47

            {9, 2, 1,}, // 48
            {8, 8, 0,}, // 49
            {9, 11, 0,}, // 50
            {6, 11, 0,}, // 51

            {10, 14, 0,}, // 52
            {7, 14, 0,}, // 53
            {11, 8, 0,}, // 54
            {9, 5, 0,}, // 55

            {4, 2, 1,}, // 56
            {2, 8, 0,}, // 57
            {5, 8, 0,}, // 58
            {3, 11, 0,}, // 59    

            {2, 14, 1,}, // 60
            {10, 17, 0,}, // 61
            {3, 5, 0,}, // 62
            {6, 5, 0,}, // 63

            {48, 6, 0,}, // 64
            {50, 9, 1,}, // 65
            {53, 12, 1,}, // 66
            {47, 12, 1,}, // 67

            {50, 15, 1,}, // 68
            {13, 17, 2,}, // 69
            {54, 6, 0,}, // 70
            {51, 6, 0,}, // 71
    };

    private final int[][] KeyMatrix = new int[10][2];
    private final boolean[] PressedKeys = new boolean[72];
    private final DataRecorderMapper dataRecorderMapper
            = new DataRecorderMapper();

    private int row;
    private int column;
    private boolean enabled;

    @Override
    public int getInputDevice() {
        return InputDevices.Keyboard;
    }

    @Override
    public void setMachine(final Machine machine) {
        dataRecorderMapper.setMachine(machine);
    }

    public DataRecorderMapper getDataRecorder() {
        return dataRecorderMapper;
    }

    @Override
    public void update(final int buttons) {
        dataRecorderMapper.update(buttons);
        for (int i = 9; i >= 0; i--) {
            KeyMatrix[i][0] = 0x1E;
            KeyMatrix[i][1] = 0x1E;
        }
        for (int i = 71; i >= 0; i--) {
            PressedKeys[i] = false;
        }
        setKeyBit((buttons >> 16) & 0x7F);
        setKeyBit((buttons >> 23) & 0x7F);
        if ((buttons & 0x40_00_00_00) != 0) {
            setKeyBit(KEY_MAP[KeyLeftShift]);
        }
        if ((buttons & 0x80_00_00_00) != 0) {
            setKeyBit(KEY_MAP[KeyRightShift]);
        }
        if ((buttons & 0x00_00_04_00) != 0) {
            setKeyBit(KEY_MAP[KeyControl]);
        }
        if ((buttons & 0x00_00_08_00) != 0) {
            setKeyBit(KEY_MAP[KeyKana]);
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
        dataRecorderMapper.writePort(value);
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
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 1) {
            if (enabled) {
                return KeyMatrix[row][column];
            } else {
                return 0x1E;
            }
        } else {
            return dataRecorderMapper.readPort(0);
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return portIndex == 1 ? readPort(1) : dataRecorderMapper.peekPort(0);
    }

    @Override
    public void render(final int[] screen) {
        final int x = 163;
        final int y = 205;
        InputIcons.FamilyBasicKeyboard.render(screen, x, y);
        for (int i = 71; i >= 0; i--) {
            if (PressedKeys[i]) {
                final int[] K = KEYS[i];
                (K[2] == 0 ? InputIcons.FamilyBasicKeyboardKey : K[2] == 1
                        ? InputIcons.FamilyBasicKeyboardShift
                        : InputIcons.FamilyBasicKeyboardSpace).render(screen, x + K[0],
                        y + K[1]);
            }
        }
    }
}