package cn.kinlon.emu.input.snesmouse;

import cn.kinlon.emu.input.DeviceMapper;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.SnesMouse;

public class SnesMouseMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private final int shift;
    private final int portIndex;

    private int buttons;
    private int shiftRegister;
    private int sensitivity;
    private boolean strobe;

    public SnesMouseMapper(final int portIndex) {
        this.shift = portIndex << 3;
        this.portIndex = portIndex;
    }

    @Override
    public int getInputDevice() {
        return SnesMouse;
    }

    @Override
    public void update(final int buttons) {
        this.buttons = buttons;
    }

    @Override
    public void writePort(final int value) {
        strobe = (value & 1) == 1;
        if (strobe) {
            shiftRegister = (((buttons >> shift) & 0xFF) | (sensitivity << 4) | 0x01)
                    << 16;
            int deltaY = ((byte) (buttons >> 24)) << sensitivity;
            int deltaX = ((byte) (buttons >> 16)) << sensitivity;
            if (deltaY >= 0) {
                shiftRegister |= (deltaY & 0x7F) << 8;
            } else {
                shiftRegister |= (0x80 | (-deltaY & 0x7F)) << 8;
            }
            if (deltaX >= 0) {
                shiftRegister |= deltaX & 0x7F;
            } else {
                shiftRegister |= 0x80 | (-deltaX & 0x7F);
            }
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (this.portIndex == portIndex) {
            final int value = (shiftRegister >> 31) & 1;
            if (strobe) {
                if (sensitivity == 2) {
                    sensitivity = 0;
                } else {
                    sensitivity++;
                }
            } else {
                shiftRegister <<= 1;
            }
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return this.portIndex == portIndex ? ((shiftRegister >> 31) & 1) : 0;
    }
}