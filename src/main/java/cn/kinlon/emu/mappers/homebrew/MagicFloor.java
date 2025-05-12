package cn.kinlon.emu.mappers.homebrew;

import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.mappers.NametableMirroring.*;
import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class MagicFloor extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] ntMappings = new int[8];

    public MagicFloor(final NesFile nesFile) {
        super(nesFile, 2, 1);
        int mirroring = nesFile.getMirroring();
        if (mirroring == FOUR_SCREEN) {
            mirroring = getBitBool(nesFile.getHeader()[6], 0) ? ONE_SCREEN_B
                    : ONE_SCREEN_A;
        }
        int bit = 0;
        switch (mirroring) {
            case VERTICAL:
                bit = 0;
                break;
            case HORIZONTAL:
                bit = 1;
                break;
            case ONE_SCREEN_A:
                bit = 2;
                break;
            case ONE_SCREEN_B:
                bit = 3;
                break;
        }
        for (int i = 7; i >= 0; i--) {
            ntMappings[i] = 0x2000 | (getBit(i, bit) << 10);
        }
    }

    @Override
    public void init() {
        setPrgBank(0);
    }

    @Override
    public int maskVRAMAddress(int address) {

        address &= vramMask;

        if (address >= 0x2000 && address <= 0x3EFF) {
            address = ntMappings[(address >> 10) & 7] | (address & 0x03FF);
        }

        return address;
    }
}
