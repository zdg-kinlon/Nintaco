package cn.kinlon.emu.mappers.kaiser;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBit;

public class Kaiser7022 extends Mapper {

    private static final long serialVersionUID = 0;

    private int reg;

    public Kaiser7022(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        reg = 0;
        setPrgBank(2, 0);
    }

    @Override
    public void resetting() {
        reg = 0;
        readMemory(0xFFFC);
    }

    @Override
    public int readMemory(final int address) {
        if (address == 0xFFFC) {
            setChrBank(reg);
            setPrgBank(2, reg);
            setPrgBank(3, reg);
        }
        return super.readMemory(address);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address) {
            case 0x8000:
                setNametableMirroring(getBit(value, 2));
                break;
            case 0xA000:
                reg = value & 0x0F;
                break;
        }
    }
}
