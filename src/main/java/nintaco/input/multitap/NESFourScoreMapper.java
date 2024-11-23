package nintaco.input.multitap;

import nintaco.input.gamepad.GamepadMapper;
import nintaco.input.gamepad.LagDeviceMapper;

import java.io.Serializable;

import static nintaco.input.InputDevices.NESFourScore1;

// NES Four Score / Satellite
public class NESFourScoreMapper extends LagDeviceMapper
        implements Serializable {

    private static final long serialVersionUID = 0;

    private final int shift0;
    private final int shift1;
    private final int signature;
    private final int portIndex;

    private int shiftRegister;
    private boolean strobe;

    public NESFourScoreMapper(final int portIndex) {
        this.portIndex = portIndex;
        this.shift0 = portIndex << 3;
        this.shift1 = (portIndex + 1) << 3;
        this.signature = portIndex == 0 ? 0x00080000 : 0x00040000;
    }

    public static void render(final int[] screen, final int buttons) {
        GamepadMapper.render(screen, 0, buttons & 0xFF);
        GamepadMapper.render(screen, 1, (buttons >> 8) & 0xFF);
        GamepadMapper.render(screen, 2, (buttons >> 16) & 0xFF);
        GamepadMapper.render(screen, 3, (buttons >> 24) & 0xFF);
    }

    @Override
    public int getInputDevice() {
        return NESFourScore1;
    }

    @Override
    public void writePort(final int value) {
        strobe = (value & 1) == 1;
        if (strobe) {
            updateButtons();
            shiftRegister = signature
                    | ((buttons >> shift1) & 0x0000FF00)
                    | ((buttons >> shift0) & 0x000000FF);
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (this.portIndex == portIndex) {
            final int value = shiftRegister & 1;
            if (!strobe) {
                shiftRegister >>= 1;
            }
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return this.portIndex == portIndex ? (shiftRegister & 1) : 0;
    }

    @Override
    public void render(final int[] screen) {
        render(screen, buttons);
    }
}