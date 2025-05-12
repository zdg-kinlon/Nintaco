package cn.kinlon.emu.input.multitap;

import cn.kinlon.emu.input.gamepad.LagDeviceMapper;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.Famicom4PlayersAdapter1;

public class Famicom4PlayersAdapterMapper extends LagDeviceMapper
        implements Serializable {

    private static final long serialVersionUID = 0;

    private final int shift0;
    private final int shift1;
    private final int portIndex;

    private int shiftRegister0;
    private int shiftRegister1;
    private boolean strobe;

    public Famicom4PlayersAdapterMapper(final int portIndex) {
        this.portIndex = portIndex;
        this.shift0 = portIndex << 3;
        this.shift1 = ((portIndex + 2) << 3) - 1;
    }

    @Override
    public int getInputDevice() {
        return Famicom4PlayersAdapter1;
    }

    @Override
    public void writePort(final int value) {
        strobe = (value & 1) == 1;
        if (strobe) {
            updateButtons();
            shiftRegister0 = 0xFFFFFF00 | ((buttons >> shift0) & 0x000000FF);
            shiftRegister1 = 0xFFFFFE00 | ((buttons >> shift1) & 0x000001FE);
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (this.portIndex == portIndex) {
            final int value = (shiftRegister1 & 0x02) | (shiftRegister0 & 0x01);
            if (!strobe) {
                shiftRegister0 >>= 1;
                shiftRegister1 >>= 1;
            }
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return this.portIndex == portIndex ? (shiftRegister1 & 0x02)
                | (shiftRegister0 & 0x01) : 0;
    }

    @Override
    public void render(final int[] screen) {
        NESFourScoreMapper.render(screen, buttons);
    }
}