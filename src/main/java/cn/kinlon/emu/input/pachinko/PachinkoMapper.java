package cn.kinlon.emu.input.pachinko;

import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.icons.InputIcons;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Pachinko;
import static cn.kinlon.emu.input.pachinko.PachinkoDescriptor.*;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class PachinkoMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private int throttle = 255;
    private int throttleDirection;
    private int buttons;
    private int shiftRegister = 0xFF0000;
    private boolean strobe;

    @Override
    public int getInputDevice() {
        return Pachinko;
    }

    @Override
    public void update(final int buttons) {
        throttleDirection = 0;
        if (getBitBool(buttons, 25)) {
            if (throttle > 159) {
                throttleDirection = -1;
                throttle -= 4;
            }
        }
        if (getBitBool(buttons, 24)) {
            if (throttle < 255) {
                throttleDirection = 1;
                throttle += 4;
            }
        }
        this.buttons = (buttons >> 16) & 0xFF;
    }

    @Override
    public void writePort(final int value) {
        final boolean prior = strobe;
        strobe = getBitBool(value, 0);
        if (prior && !strobe) {
            final int t
                    = (throttle >> 7 & 0x01)
                    | (throttle >> 5 & 0x02)
                    | (throttle >> 3 & 0x04)
                    | (throttle >> 1 & 0x08)
                    | (throttle << 1 & 0x10)
                    | (throttle << 3 & 0x20)
                    | (throttle << 5 & 0x40)
                    | (throttle << 7 & 0x80);
            shiftRegister = 0xFF0000 | (t << 8) | buttons;
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 0) {
            final int value = (shiftRegister & 1) << 1;
            shiftRegister >>= 1;
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return (portIndex == 0) ? ((shiftRegister & 1) << 1) : 0x00;
    }

    @Override
    public void render(final int[] screen) {
        final int x = 172;
        final int y = 196;
        InputIcons.Pachinko.render(screen, x, y);
        if (getBitBool(buttons, A)) {
            InputIcons.GamepadAB.render(screen, x + 31, y + 14);
        }
        if (getBitBool(buttons, B)) {
            InputIcons.GamepadAB.render(screen, x + 26, y + 14);
        }
        if (getBitBool(buttons, Select)) {
            InputIcons.GamepadStart.render(screen, x + 4, y + 24);
        }
        if (getBitBool(buttons, Start)) {
            InputIcons.GamepadStart.render(screen, x + 9, y + 24);
        }
        if (getBitBool(buttons, Up)) {
            InputIcons.GamepadDPad.render(screen, x + 6, y + 10);
        }
        if (getBitBool(buttons, Down)) {
            InputIcons.GamepadDPad.render(screen, x + 6, y + 16);
        }
        if (getBitBool(buttons, Left)) {
            InputIcons.GamepadDPad.render(screen, x + 3, y + 13);
        }
        if (getBitBool(buttons, Right)) {
            InputIcons.GamepadDPad.render(screen, x + 9, y + 13);
        }
        if (throttleDirection < 0) {
            InputIcons.PachinkoDown.render(screen, x + 16, y + 7);
        }
        if (throttleDirection > 0) {
            InputIcons.PachinkoUp.render(screen, x + 23, y + 1);
        }
    }
}