package cn.kinlon.emu.input.familytrainermat;

import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.icons.InputIcons;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.FamilyTrainerMat;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class FamilyTrainerMatMapper
        extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private int buttons;
    private int output;

    @Override
    public int getInputDevice() {
        return FamilyTrainerMat;
    }

    @Override
    public void update(final int buttons) {
        this.buttons = buttons;
    }

    @Override
    public void writePort(final int value) {
        if (!getBitBool(value, 2)) {
            output = (~buttons >> 23) & 0x1E;
        } else if (!getBitBool(value, 1)) {
            output = (~buttons >> 19) & 0x1E;
        } else if (!getBitBool(value, 0)) {
            output = (~buttons >> 15) & 0x1E;
        } else {
            output = 0;
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 1) {
            return output;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return readPort(portIndex);
    }

    @Override
    public void render(final int[] screen) {
        final int x = 180;
        final int y = 205;
        InputIcons.PowerPad.render(screen, x, y);
        if ((buttons & 0x0800_0000) != 0) { //  1
            InputIcons.GamepadAB.render(screen, x + 2, y + 2);
        }
        if ((buttons & 0x0400_0000) != 0) { //  2
            InputIcons.GamepadAB.render(screen, x + 7, y + 2);
        }
        if ((buttons & 0x0200_0000) != 0) { //  3
            InputIcons.GamepadAB.render(screen, x + 13, y + 2);
        }
        if ((buttons & 0x0100_0000) != 0) { //  4
            InputIcons.GamepadAB.render(screen, x + 18, y + 2);
        }
        if ((buttons & 0x0080_0000) != 0) { //  5
            InputIcons.GamepadAB.render(screen, x + 2, y + 8);
        }
        if ((buttons & 0x0040_0000) != 0) { //  6
            InputIcons.GamepadAB.render(screen, x + 7, y + 8);
        }
        if ((buttons & 0x0020_0000) != 0) { //  7
            InputIcons.GamepadAB.render(screen, x + 13, y + 8);
        }
        if ((buttons & 0x0010_0000) != 0) { //  8
            InputIcons.GamepadAB.render(screen, x + 18, y + 8);
        }
        if ((buttons & 0x0008_0000) != 0) { //  9
            InputIcons.GamepadAB.render(screen, x + 2, y + 14);
        }
        if ((buttons & 0x0004_0000) != 0) { // 10
            InputIcons.GamepadAB.render(screen, x + 7, y + 14);
        }
        if ((buttons & 0x0002_0000) != 0) { // 11
            InputIcons.GamepadAB.render(screen, x + 13, y + 14);
        }
        if ((buttons & 0x0001_0000) != 0) { // 12
            InputIcons.GamepadAB.render(screen, x + 18, y + 14);
        }
    }
}