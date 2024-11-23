package nintaco.input.glasses;

import nintaco.input.DeviceMapper;
import nintaco.input.InputDevices;

import java.io.Serializable;

import static nintaco.util.BitUtil.getBitBool;

public class GlassesMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private boolean shutter;

    @Override
    public int getInputDevice() {
        return InputDevices.Glasses;
    }

    public boolean isShutter() {
        return shutter;
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