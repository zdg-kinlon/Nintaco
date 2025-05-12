package cn.kinlon.emu.input.konamihypershot;

import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.icons.InputIcons;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.KonamiHyperShot;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class KonamiHyperShotMapper
        extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private int buttons;
    private int output;
    private boolean strobe;

    @Override
    public int getInputDevice() {
        return KonamiHyperShot;
    }

    @Override
    public void update(final int buttons) {
        this.buttons = (buttons >> 15) & 0x1E;
    }

    @Override
    public void writePort(final int value) {
        final boolean priorStrobe = strobe;
        strobe = getBitBool(value, 0);
        if (priorStrobe && !strobe) {
            output = buttons;
        }
    }

    @Override
    public int readPort(final int portIndex) {
        return (portIndex == 1) ? output : 0;
    }

    @Override
    public int peekPort(final int portIndex) {
        return readPort(portIndex);
    }

    @Override
    public void render(final int[] screen) {
        final int x1 = 140;
        final int x2 = 204;
        final int y = 202;
        InputIcons.KonamiHyperShot.render(screen, x1, y);
        InputIcons.KonamiHyperShot.render(screen, x2, y);
        if ((buttons & 0x02) != 0) {
            InputIcons.KonamiHyperShotButton.render(screen, x1 + 25, y + 10);
        }
        if ((buttons & 0x04) != 0) {
            InputIcons.KonamiHyperShotButton.render(screen, x1 + 8, y + 10);
        }
        if ((buttons & 0x08) != 0) {
            InputIcons.KonamiHyperShotButton.render(screen, x2 + 25, y + 10);
        }
        if ((buttons & 0x10) != 0) {
            InputIcons.KonamiHyperShotButton.render(screen, x2 + 8, y + 10);
        }
    }
}