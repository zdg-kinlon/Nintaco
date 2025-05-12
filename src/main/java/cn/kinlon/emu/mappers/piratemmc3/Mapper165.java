package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



public class Mapper165 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final boolean[] latches = new boolean[2];

    public Mapper165(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public int readVRAM(final int address) {
        final int value;
        if (address < 0x2000) {
            final int bankAddress = (chrBanks[address >> chrShift]
                    | (address & chrAddressMask)) & chrRomSizeMask;
            if (bankAddress < 0x1000) {
                value = vram[bankAddress];
            } else {
                value = chrROM[bankAddress];
            }
        } else {
            value = vram[address];
        }
        switch (address & 0xFFF8) {
            case 0x0FD8:
            case 0x1FD8:
                latches[0] = latches[1] = false;
                updateChrBanks();
                break;
            case 0x0FE8:
                latches[0] = true;
                updateChrBanks();
                break;
            case 0x1FE8:
                latches[1] = true;
                updateChrBanks();
                break;
        }
        return value;
    }

    @Override
    protected void updateChrBanks() {
        setBanks(chrBanks, 0, (latches[0] ? R[1] : R[0]) << 10, 4, 0x0400);
        setBanks(chrBanks, 4, (latches[1] ? R[4] : R[2]) << 10, 4, 0x0400);
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000) {
            final int bankAddress = (chrBanks[address >> chrShift]
                    | (address & chrAddressMask)) & chrRomSizeMask;
            if (bankAddress < 0x1000) {
                vram[bankAddress] = value;
            }
        } else {
            vram[address] = value;
        }
    }
}