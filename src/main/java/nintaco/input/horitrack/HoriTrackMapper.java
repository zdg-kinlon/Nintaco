package nintaco.input.horitrack;

import nintaco.input.DeviceMapper;
import nintaco.input.icons.InputIcons;

import java.io.Serializable;

import static nintaco.input.InputDevices.HoriTrack;
import static nintaco.util.BitUtil.getBitBool;

public class HoriTrackMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private int buttons;
    private int shiftRegister;
    private boolean strobe;

    @Override
    public int getInputDevice() {
        return HoriTrack;
    }

    @Override
    public void update(final int buttons) {
        this.buttons = buttons;
    }

    @Override
    public void writePort(final int value) {
        final boolean priorStrobe = strobe;
        strobe = getBitBool(value, 0);
        if (priorStrobe && !strobe) {
            shiftRegister = 0x100000 | ((buttons & 0x000C00) << 7)
                    | ((buttons >> 15) & 0x01FFFE);
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 0) {
            final int value = shiftRegister;
            shiftRegister >>= 1;
            return value & 0x02;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return (portIndex == 0) ? (shiftRegister & 0x02) : 0;
    }

    @Override
    public void render(final int[] screen) {
        final int x = 172;
        final int y = 188;
        InputIcons.HoriTrack.render(screen, x, y);
        if ((buttons & 0x0001_0000) != 0) {
            InputIcons.GamepadAB.render(screen, x + 27, y + 20);
        }
        if ((buttons & 0x0002_0000) != 0) {
            InputIcons.GamepadAB.render(screen, x + 20, y + 27);
        }
        if ((buttons & 0x0004_0000) != 0) {
            InputIcons.GamepadStart.render(screen, x + 30, y + 9);
        }
        if ((buttons & 0x0008_0000) != 0) {
            InputIcons.GamepadStart.render(screen, x + 24, y + 6);
        }
        if ((buttons & 0x0010_0000) != 0) {
            InputIcons.GamepadDPad.render(screen, x + 7, y + 24);
        }
        if ((buttons & 0x0020_0000) != 0) {
            InputIcons.GamepadDPad.render(screen, x + 7, y + 30);
        }
        if ((buttons & 0x0040_0000) != 0) {
            InputIcons.GamepadDPad.render(screen, x + 4, y + 27);
        }
        if ((buttons & 0x0080_0000) != 0) {
            InputIcons.GamepadDPad.render(screen, x + 10, y + 27);
        }
    }
}