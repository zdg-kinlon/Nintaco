package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



import static cn.kinlon.emu.utils.BitUtil.*;

public class Super24In1SC03 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] PRG_BLOCK_MASKS
            = {0x3F, 0x1F, 0x0F, 0x01, 0x03, 0x00, 0x00, 0x00};

    private boolean chrRamMode;
    private int bankSize = 0x24;
    private int prgBank = 0x9F;
    private int chrBank;

    public Super24In1SC03(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        updateBlocks();
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000 && !chrRamMode && chrAddressMask != 0) {
            return chrROM[(chrBanks[address >> chrShift] | (address & chrAddressMask))
                    & chrRomSizeMask];
        } else {
            return vram[address];
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        super.writeMemory(address, value);
        switch (address) {
            case 0x5FF0:
                bankSize = value;
                updateBlocks();
                break;
            case 0x5FF1:
                prgBank = value;
                updateBlocks();
                break;
            case 0x5FF2:
                chrBank = value;
                updateBlocks();
                break;
        }
    }

    private void updateBlocks() {
        final int chrOffset;
        final int chrMask;
        if (getBitBool(bankSize, 5)) {
            chrRamMode = true;
            chrOffset = 0;
            chrMask = 0x07;
        } else {
            chrRamMode = false;
            chrOffset = chrBank << 3;
            chrMask = 0xFF;
        }
        setBlock(prgBank << 1, PRG_BLOCK_MASKS[bankSize & 7], chrOffset, chrMask);
    }
}
