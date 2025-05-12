package cn.kinlon.emu.input.doremikkokeyboard;

import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.icons.InputIcons;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.DoremikkoKeyboard;

public class DoremikkoKeyboardMapper
        extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int[][] KEYS = {
            {1, 0}, {2, 2}, {4, 0}, {5, 2},
            {7, 0}, {8, 2}, {10, 1}, {13, 0},
            {14, 2}, {16, 0}, {17, 2}, {19, 1}
    };

    private long keys;
    private int register;
    private int part;
    private boolean mode;

    @Override
    public int getInputDevice() {
        return DoremikkoKeyboard;
    }

    @Override
    public void update(final int buttons) {
        final int value = ((buttons & 0xFFFF_0000) >>> 14)
                | ((buttons & 0x0000_0C00) >>> 10);
        if (Integer.bitCount(value) > 8) {
            keys = (keys & 0x00_0003_FFFFL) | (((long) (~value & 0x0003_FFFF)) << 18);
        } else {
            keys = (keys & 0x0F_FFFC_0000L) | value;
        }
    }

    @Override
    public void writePort(final int value) {
        if ((value & 0x02) > (register & 0x02)) {
            part = 0;
            mode = false;
        }
        if ((value & 0x01) > (register & 0x01)) {
            part++;
            mode = false;
        }
        register = value;
    }


    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 1) {
            final int value = peekPort(1);
            mode = !mode;
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        if (portIndex == 1) {
            final int value;
            switch (part) {
                case 1:
                    if (mode) {
                        value = (int) ((keys & 0x0_0000_0003L) << 1);
                    } else {
                        value = 0;
                    }
                    break;
                case 2:
                    if (mode) {
                        value = (int) ((keys & 0x0_0000_00C0L) >> 5);
                    } else {
                        value = (int) ((keys & 0x0_0000_003CL) >> 1);
                    }
                    break;
                case 3:
                    if (mode) {
                        value = (int) ((keys & 0x0_0000_3000L) >> 11);
                    } else {
                        value = (int) ((keys & 0x0_0000_0F00L) >> 7);
                    }
                    break;
                case 4:
                    if (mode) {
                        value = (int) ((keys & 0x0_000C_0000L) >> 17);
                    } else {
                        value = (int) ((keys & 0x0_0003_C000L) >> 13);
                    }
                    break;
                case 5:
                    if (mode) {
                        value = (int) ((keys & 0x0_0300_0000L) >> 23);
                    } else {
                        value = (int) ((keys & 0x0_00F0_0000L) >> 19);
                    }
                    break;
                case 6:
                    if (mode) {
                        value = (int) ((keys & 0x0_C000_0000L) >> 29);
                    } else {
                        value = (int) ((keys & 0x0_3C00_0000L) >> 25);
                    }
                    break;
                case 7:
                    if (mode) {
                        value = 0;
                    } else {
                        value = (int) ((keys & 0xF_0000_0000L) >> 31);
                    }
                    break;
                default:
                    value = 0;
                    break;
            }
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public void render(final int[] screen) {
        final int x = 160;
        final int y = 208;
        InputIcons.Doremikko.render(screen, x, y);
        long k = keys;
        int o = 0;
        int offset = 0;
        for (int i = 0; i < 36; i++, k >>= 1) {
            if ((k & 1) == 1) {
                final int T = KEYS[o][1];
                (T == 0 ? InputIcons.DoremikkoWhite1 : T == 1
                        ? InputIcons.DoremikkoWhite2 : InputIcons.DoremikkoBlack).render(
                        screen, x + offset + KEYS[o][0], y + 1);
            }
            if (o == 11) {
                o = 0;
                offset += 21;
            } else {
                o++;
            }
        }
    }
}