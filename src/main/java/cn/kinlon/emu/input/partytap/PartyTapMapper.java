package cn.kinlon.emu.input.partytap;

import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.icons.InputIcons;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.PartyTap;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class PartyTapMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private int mode = 0xE0;
    private int buttons;
    private int shiftRegister;
    private boolean strobe;

    @Override
    public int getInputDevice() {
        return PartyTap;
    }

    @Override
    public void update(final int buttons) {
        this.buttons = (buttons >> 14) & 0xFC;
    }

    @Override
    public void writePort(final int value) {
        mode = 0xA0 | ((value & 0x04) << 4);
        final boolean priorStrobe = strobe;
        strobe = getBitBool(value, 0);
        if (priorStrobe && !strobe) {
            shiftRegister = buttons;
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 1) {
            final int value = shiftRegister & 0x1C;
            shiftRegister = (shiftRegister >> 3) | mode;
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return (portIndex == 1) ? (shiftRegister & 0x1C) : 0;
    }

    @Override
    public void render(final int[] screen) {
        final int x = 128;
        final int y = 208;
        int b = buttons >> 2;
        for (int i = 0; i < 6; i++, b >>= 1) {
            final int X = x + 21 * i;
            InputIcons.PartyTap.render(screen, X, y);
            if (getBitBool(b, 0)) {
                InputIcons.PartyTapButton.render(screen, X + 2, y + 2);
            }
        }
    }
}