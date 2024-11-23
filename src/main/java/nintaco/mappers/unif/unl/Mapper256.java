package nintaco.mappers.unif.unl;

// TODO IMPLEMENT ONE BUS PPU

import nintaco.files.*;

// Generic OneBus hardware, with submapper-controlled unmangling of 2012-2017
// 4107-410A and 8000 register addresses/numbers
public class Mapper256 extends OneBus {

    private static final long serialVersionUID = 0;

    // PPU Bank Register unmangling by Submapper
    private static final int[][] PPUMangle = {
            {0, 1, 2, 3, 4, 5},  // Submapper 0: Normal
            {1, 0, 5, 4, 3, 2},  // Submapper 1: Waixing
            {0, 1, 2, 3, 4, 5},  // Submapper 2: Trump Grand
            {5, 4, 3, 2, 0, 1},  // Submapper 3: Zechess
            {2, 5, 0, 4, 3, 1},  // Submapper 4: Qishenglong
            {0, 1, 2, 3, 4, 5},  // Submapper 5: unused so far
            {0, 1, 2, 3, 4, 5},  // Submapper 6: unused so far
            {0, 1, 2, 3, 4, 5},  // Submapper 7: unused so far
            {0, 1, 2, 3, 4, 5},  // Submapper 8: unused so far
            {0, 1, 2, 3, 4, 5},  // Submapper 9: unused so far
            {0, 1, 2, 3, 4, 5},  // Submapper A: unused so far
            {0, 1, 2, 3, 4, 5},  // Submapper B: unused so far
            {0, 1, 2, 3, 4, 5},  // Submapper C: unused so far
            {0, 1, 2, 3, 4, 5},  // Submapper D: unused so far
            {0, 1, 2, 3, 4, 5},  // Submapper E: unused so far
            {0, 1, 2, 3, 4, 5},  // Submapper F: unused so far
    };

    // CPU Bank Register unmangling by Submapper
    private static final int[][] CPUMangle = {
            {0, 1, 2, 3},  // Submapper 0: Normal
            {0, 1, 2, 3},  // Submapper 1: Waixing
            {1, 0, 2, 3},  // Submapper 2: Trump Grand
            {0, 1, 2, 3},  // Submapper 3: Zechess
            {0, 1, 2, 3},  // Submapper 4: Qishenglong
            {0, 1, 2, 3},  // Submapper 5: unused so far
            {0, 1, 2, 3},  // Submapper 6: unused so far
            {0, 1, 2, 3},  // Submapper 7: unused so far
            {0, 1, 2, 3},  // Submapper 8: unused so far
            {0, 1, 2, 3},  // Submapper 9: unused so far
            {0, 1, 2, 3},  // Submapper A: unused so far
            {0, 1, 2, 3},  // Submapper B: unused so far
            {0, 1, 2, 3},  // Submapper C: unused so far
            {0, 1, 2, 3},  // Submapper D: unused so far
            {0, 1, 2, 3},  // Submapper E: unused so far
            {0, 1, 2, 3},  // Submapper F: unused so far
    };

    // MMC3 Bank Register unmangling by Submapper.
    // Note that OneBus::WriteMMC3 calls its own WriteHandler for Banks 2 and 4, 
    // not the Mapper256's.  So if the 2012-2017 registers are mangled but the 
    // MMC3 registers are unmangled, MMC3Mangle is 0,1,2,3,4,5,6,7.
    private static final int[][] MMC3Mangle = {
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper 0: Normal
            {5, 4, 3, 2, 1, 0, 6, 7},  // Submapper 1: Waixing
            {0, 1, 2, 3, 4, 5, 7, 6},  // Submapper 2: Trump Grand
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper 3: Zechess
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper 4: Qishenglong
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper 5: unused so far
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper 6: unused so far
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper 7: unused so far
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper 8: unused so far
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper 9: unused so far
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper A: unused so far
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper B: unused so far
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper C: unused so far
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper D: unused so far
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper E: unused so far
            {0, 1, 2, 3, 4, 5, 6, 7},  // Submapper F: unused so far
    };

    private final int submapper;

    public Mapper256(final CartFile cartFile) {
        super(cartFile);
        submapper = cartFile.getSubmapperNumber();
    }

    @Override
    protected void write2(int address, final int value) {
        if (address >= 0x2012 && address <= 0x2017) {
            address = 0x2012 + PPUMangle[submapper][address - 0x2012];
        }
        super.write2(address, value);
    }

    @Override
    protected void write4(int address, final int value) {
        if (address >= 0x4107 && address <= 0x410A) {
            address = 0x4107 + CPUMangle[submapper][address - 0x4107];
        }
        super.write4(address, value);
    }

    @Override
    protected void writeMMC3(final int address, int value) {
        if ((address & 0xE001) == 0x8000) {
            value = (value & 0xF8) | MMC3Mangle[submapper][value & 7];
        }
        super.writeMMC3(address, value);
    }
}