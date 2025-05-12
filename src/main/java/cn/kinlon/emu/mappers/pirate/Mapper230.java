package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;
import cn.kinlon.emu.mappers.NametableMirroring;

import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Mapper230 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean multiMode;

    public Mapper230(final CartFile cartFile) {
        super(cartFile, 4, 0);
    }

    @Override
    public void init() {
        if (multiMode) {
            setPrgBank(2, 8);
            setPrgBank(3, 9);
            setNametableMirroring(NametableMirroring.HORIZONTAL);
        } else {
            setPrgBank(2, 0);
            setPrgBank(3, 7);
            setNametableMirroring(NametableMirroring.VERTICAL);
        }
    }

    @Override
    public void resetting() {
        multiMode ^= true;
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (multiMode) {
            writeMultiRegister(value);
        } else {
            writeContraRegister(value);
        }
    }

    private void writeMultiRegister(final int value) {
        if (getBitBool(value, 5)) {
            final int bank = (value & 0x1F) + 8;
            setPrgBank(2, bank);
            setPrgBank(3, bank);
        } else {
            final int bank = (value & 0x1E) + 8;
            setPrgBank(2, bank);
            setPrgBank(3, bank + 1);
        }
        setNametableMirroring(getBit(value, 6) ^ 1);
    }

    private void writeContraRegister(final int value) {
        setPrgBank(2, value & 7);
        setPrgBank(3, 7);
        setNametableMirroring(NametableMirroring.VERTICAL);
    }
}