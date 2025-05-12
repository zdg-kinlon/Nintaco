package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



import static cn.kinlon.emu.utils.BitUtil.*;

public class BMC1024CA1 extends MMC3 {

    private static final long serialVersionUID = 0;

    private boolean outerBankLocked;

    public BMC1024CA1(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        writeOuterBank(0);
    }

    private void writeOuterBank(final int address) {
        final int outerBank = address & 7;
        if (outerBank > 0) {
            outerBankLocked = true;
        }
        if (getBitBool(address, 3)) {
            setPrgBlock(outerBank << 4, 0x1F);
        } else {
            setPrgBlock(outerBank << 4, 0x0F);
        }
        chrRamPresent = getBitBool(address, 4);
        if (chrRamPresent) {
            setChrBlock(0, -1);
        } else if (getBitBool(address, 5)) {
            setChrBlock(outerBank << 7, 0xFF);
        } else {
            setChrBlock(outerBank << 7, 0x7F);
        }
        updateBanks();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (!outerBankLocked && prgRamChipEnabled && prgRamWritesEnabled
                && (address & 0xE000) == 0x6000) {
            writeOuterBank(address);
        }
        super.writeMemory(address, value);
    }
}