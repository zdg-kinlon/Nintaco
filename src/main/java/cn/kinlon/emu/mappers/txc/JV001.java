package cn.kinlon.emu.mappers.txc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public abstract class JV001 extends Mapper {

    private static final long serialVersionUID = 0;

    protected boolean increase;
    protected int output;
    protected int invert;
    protected int staging;
    protected int accumulator;
    protected int inverter;
    protected boolean A;
    protected boolean B;
    protected boolean X;

    public JV001(final CartFile cartFile, final int prgBanksSize,
                 final int chrBanksSize) {
        super(cartFile, prgBanksSize, chrBanksSize);
    }

    @Override
    public void init() {
        output = staging = accumulator = inverter = 0;
        invert = 0xFF;
        X = A = false;
        B = true;
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    protected abstract void updateState();

    protected int readJV001(final int address) {
        int result = 0xFF;
        if ((address & 0xE103) == 0x4100) {
            result = ((inverter ^ invert) & 0xF0) | (accumulator & 0x0F);
            updateState();
        }
        return result;
    }

    protected void writeJV001(final int address, final int value) {
        switch (address & 0xE103) {
            case 0x4100:
                if (increase) {
                    ++accumulator;
                } else {
                    accumulator = ((accumulator & 0xF0) | (staging & 0x0F)) ^ invert;
                }
                break;
            case 0x4101:
                invert = getBitBool(value, 0) ? 0xFF : 0x00;
                break;
            case 0x4102:
                staging = value & 0x0F;
                inverter = value & 0xF0;
                break;
            case 0x4103:
                increase = getBitBool(value, 0);
                break;
            default:
                if ((address & 0x8000) != 0) {
                    output = (inverter & 0xF0) | (accumulator & 0x0F);
                }
                break;
        }

        X = (invert != 0) ? A : B;
        updateState();
    }
}
