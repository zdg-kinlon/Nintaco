package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

import static cn.kinlon.emu.utils.BitUtil.*;

public class Mapper014 extends MMC3 {

    private static final long serialVersionUID = 0;

    private final int[] vrcChrRegs = new int[8];

    private int mode;

    public Mapper014(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void updateChrBanks() {
        final int page0 = (mode & 0x08) << 5;
        final int page1 = (mode & 0x20) << 3;
        final int page2 = (mode & 0x80) << 1;
        if (chrMode) {
            setChrBank(0, page1 | R[2]);
            setChrBank(1, page1 | R[3]);
            setChrBank(2, page2 | R[4]);
            setChrBank(3, page2 | R[5]);
            setChrBank(4, page0 | (R[0] & 0xFE));
            setChrBank(5, page0 | (R[0] | 0x01));
            setChrBank(6, page0 | (R[1] & 0xFE));
            setChrBank(7, page0 | (R[1] | 0x01));
        } else {
            setChrBank(0, page0 | (R[0] & 0xFE));
            setChrBank(1, page0 | (R[0] | 0x01));
            setChrBank(2, page0 | (R[1] & 0xFE));
            setChrBank(3, page0 | (R[1] | 0x01));
            setChrBank(4, page1 | R[2]);
            setChrBank(5, page1 | R[3]);
            setChrBank(6, page2 | R[4]);
            setChrBank(7, page2 | R[5]);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address >= 0x4100) {
            if (address == 0xA131) {
                mode = value;
            }
            if (getBitBool(mode, 1)) {
                updateBanks();
                writeRegister(address, value);
            } else {
                if ((address >= 0xB000) && (address <= 0xE003)) {
                    final int index = ((((address & 2) | (address >> 10)) >> 1) + 2) & 7;
                    final int shift = ((address & 1) << 2);
                    vrcChrRegs[index] = (vrcChrRegs[index] & (0xF0 >> shift))
                            | ((value & 0x0F) << shift);
                    setChrBank(index, vrcChrRegs[index]);
                } else {
                    switch (address & 0xF003) {
                        case 0x8000:
                            setPrgBank(4, value);
                            break;
                        case 0xA000:
                            setPrgBank(5, value);
                            break;
                        case 0x9000:
                            setNametableMirroring(value & 1);
                            break;
                    }
                }
                setPrgBank(6, -2);
                setPrgBank(7, -1);
            }
        }
    }
}