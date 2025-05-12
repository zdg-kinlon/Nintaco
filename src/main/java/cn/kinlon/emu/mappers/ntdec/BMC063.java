package cn.kinlon.emu.mappers.ntdec;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class BMC063 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean openBus;

    public BMC063(final CartFile cartFile) {
        super(cartFile, 8, 1);
    }

    @Override
    public void resetting() {
        openBus = false;
    }

    @Override
    public int readMemory(final int address) {
        if (openBus && (address & 0xC000) == 0x8000) {
            return 0;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {

        if (openBus && address < 0xC000) {
            return;
        }

        openBus = ((address & 0x0300) == 0x0300);

        if (!openBus) {
            setPrgBank(4, ((address >> 1) & 0x01FC)
                    | (getBitBool(address, 1) ? 0 : ((address >> 1) & 0x0002)));
            setPrgBank(5, ((address >> 1) & 0x01FC)
                    | (getBitBool(address, 1) ? 1 : ((address >> 1) & 0x0002) | 1));
        }
        setPrgBank(6, ((address >> 1) & 0x01FC)
                | (getBitBool(address, 1) ? 0x2 : ((address >> 1) & 0x0002)));
        setPrgBank(7, getBitBool(address, 11) ? ((address & 0x07C)
                | ((address & 0x06) != 0 ? 3 : 1)) : (((address >> 1) & 0x01FC)
                | (getBitBool(address, 1) ? 0x03 : ((address >> 1 & 0x02) | 0x01))));

        setNametableMirroring(address & 1);
    }
}