package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class Mapper199 extends MMC3 {

    private static final long serialVersionUID = 0;

    private final int[] regs = {-2, -1, 1, 3};
    private final boolean[] chrRamBanks = new boolean[8];

    private int bankSelect;

    public Mapper199(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000 && chrAddressMask != 0) {
            return (chrRamBanks[address >> chrShift] ? vram : chrROM)
                    [(chrBanks[address >> chrShift] | (address & chrAddressMask))
                    & chrRomSizeMask];
        } else {
            return vram[address];
        }
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000 && chrAddressMask != 0) {
            if (chrRamBanks[address >> chrShift]) {
                vram[(chrBanks[address >> chrShift] | (address & chrAddressMask))
                        & chrRomSizeMask] = value;
            }
        } else {
            vram[address] = value;
        }
    }

    @Override
    protected void writeBankSelect(final int value) {
        super.writeBankSelect(value);
        bankSelect = value;
    }

    @Override
    protected void writeBankData(final int value) {
        if (getBitBool(bankSelect, 3)) {
            regs[bankSelect & 3] = value;
        } else {
            R[register] = value;
        }
        updateBanks();
    }

    @Override
    protected void writeMirroring(final int value) {
        setNametableMirroring(value & 3);
    }

    @Override
    protected void updatePrgBanks() {
        super.updatePrgBanks();
        setPrgBank(6, regs[0]);
        setPrgBank(7, regs[1]);
    }

    @Override
    protected void setChrBank(final int bank, final int value) {

        super.setChrBank(bank, value);
        super.setChrBank(0, R[0]);
        super.setChrBank(1, regs[2]);
        super.setChrBank(2, R[1]);
        super.setChrBank(3, regs[3]);

        chrRamBanks[bank] = value < 8;
        chrRamBanks[0] = R[0] < 8;
        chrRamBanks[1] = regs[2] < 8;
        chrRamBanks[2] = R[1] < 8;
        chrRamBanks[3] = regs[3] < 8;
    }
}