package nintaco.mappers.txc;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public abstract class TxcLatch extends Mapper {

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
    protected boolean Y;

    public TxcLatch(final CartFile cartFile, final int prgBanksSize,
                    final int chrBanksSize) {
        super(cartFile, prgBanksSize, chrBanksSize);
    }

    @Override
    public void init() {
        output = invert = staging = accumulator = inverter = 0;
        Y = X = A = false;
        B = true;
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    protected abstract void updateState();

    protected int readLatch(final int address) {
        int result = 0xFF;
        if ((address & 0xE103) == 0x4100) {
            result = ((inverter ^ invert) & 0xF8) | (accumulator & 0x07);
            Y = X | getBitBool(result, 4);
            updateState();
        }
        return result;
    }

    protected void writeLatch(final int address, final int value) {
        switch (address & 0xE103) {
            case 0x4100:
                if (increase) {
                    ++accumulator;
                } else {
                    accumulator = ((accumulator & 0xF8) | (staging & 0x07)) ^ invert;
                }
                break;
            case 0x4101:
                invert = getBitBool(value, 0) ? 0xFF : 0x00;
                break;
            case 0x4102:
                staging = value & 0x07;
                inverter = value & 0xF8;
                break;
            case 0x4103:
                increase = getBitBool(value, 0);
                break;
            default:
                if ((address & 0x8000) != 0) {
                    output = ((inverter & 0x08) << 1) | (accumulator & 0x0F);
                }
                break;
        }

        X = (invert != 0) ? A : B;
        Y = X | ((value & 0x10) != 0);
        updateState();
    }
}