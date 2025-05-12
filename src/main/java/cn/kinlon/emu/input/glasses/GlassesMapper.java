package cn.kinlon.emu.input.glasses;

import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.InputDevices;

import java.io.Serializable;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class GlassesMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private boolean shutter;

    @Override
    public int getInputDevice() {
        return InputDevices.Glasses;
    }

    @Override
    public void update(final int buttons) {
    }

    @Override
    public void writePort(final int value) {
        shutter = getBitBool(value, 1);
    }

    @Override
    public int readPort(final int portIndex) {
        return 0;
    }

    @Override
    public int peekPort(final int portIndex) {
        return 0;
    }
}