package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class Mapper208 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] LUT = {
            0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59,
            0x59, 0x49, 0x19, 0x09, 0x59, 0x49, 0x19, 0x09,
            0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59,
            0x51, 0x41, 0x11, 0x01, 0x51, 0x41, 0x11, 0x01,
            0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59,
            0x59, 0x49, 0x19, 0x09, 0x59, 0x49, 0x19, 0x09,
            0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59,
            0x51, 0x41, 0x11, 0x01, 0x51, 0x41, 0x11, 0x01,
            0x00, 0x10, 0x40, 0x50, 0x00, 0x10, 0x40, 0x50,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x08, 0x18, 0x48, 0x58, 0x08, 0x18, 0x48, 0x58,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x10, 0x40, 0x50, 0x00, 0x10, 0x40, 0x50,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x08, 0x18, 0x48, 0x58, 0x08, 0x18, 0x48, 0x58,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59,
            0x58, 0x48, 0x18, 0x08, 0x58, 0x48, 0x18, 0x08,
            0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59,
            0x50, 0x40, 0x10, 0x00, 0x50, 0x40, 0x10, 0x00,
            0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59,
            0x58, 0x48, 0x18, 0x08, 0x58, 0x48, 0x18, 0x08,
            0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59, 0x59,
            0x50, 0x40, 0x10, 0x00, 0x50, 0x40, 0x10, 0x00,
            0x01, 0x11, 0x41, 0x51, 0x01, 0x11, 0x41, 0x51,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x09, 0x19, 0x49, 0x59, 0x09, 0x19, 0x49, 0x59,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x11, 0x41, 0x51, 0x01, 0x11, 0x41, 0x51,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x09, 0x19, 0x49, 0x59, 0x09, 0x19, 0x49, 0x59,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    };

    private final int[] regs = new int[6];

    public Mapper208(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        regs[5] = 3;
        super.init();
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        final int b = regs[5] << 2;
        super.setPrgBank(4, b);
        super.setPrgBank(5, b | 1);
        super.setPrgBank(6, b | 2);
        super.setPrgBank(7, b | 3);
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xF800) == 0x5800) {
            return regs[address & 3];
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if ((address & 0xF000) == 0x5000) {
            if (address <= 0x57FF) {
                regs[4] = value;
            } else {
                regs[address & 3] = value ^ LUT[regs[4]];
            }
        } else if ((address & 0xD800) == 0x4800) {
            regs[5] = ((value >> 3) & 2) | (value & 1);
            super.updatePrgBanks();
        } else if (address >= minRegisterAddress) {
            writeRegister(address, value);
        }
    }
}