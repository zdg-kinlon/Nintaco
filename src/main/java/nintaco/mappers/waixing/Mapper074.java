package nintaco.mappers.waixing;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

public class Mapper074 extends MMC3 {

    private static final long serialVersionUID = 0;

    protected final int minAddress;
    protected final int maxAddress;
    protected final int mask;

    public Mapper074(final CartFile cartFile) {
        this(cartFile, 0x2000, 0x2800);
    }

    public Mapper074(final CartFile cartFile, final int minAddress,
                     final int maxAddress) {

        super(cartFile);
        this.minAddress = minAddress;
        this.maxAddress = maxAddress;
        this.mask = maxAddress - minAddress - 1;
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000) {
            final int addr = (chrBanks[address >> 10] | (address & 0x03FF))
                    & chrRomSizeMask;
            if (addr >= minAddress && addr < maxAddress) {
                vram[addr & mask] = value;
            }
        } else {
            super.writeVRAM(address, value);
        }
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            final int addr = (chrBanks[address >> 10] | (address & 0x03FF))
                    & chrRomSizeMask;
            if (addr >= minAddress && addr < maxAddress) {
                return vram[addr & mask];
            } else {
                return super.readVRAM(address);
            }
        } else {
            return super.readVRAM(address);
        }
    }
}