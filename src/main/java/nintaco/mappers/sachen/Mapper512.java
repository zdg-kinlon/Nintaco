package nintaco.mappers.sachen;

// TODO WIP

import nintaco.files.*;
import nintaco.mappers.nintendo.*;
//import static nintaco.mappers.NametableMirroring.*;

public class Mapper512 extends MMC3 {

    private static final long serialVersionUID = 0;

    private final int[] CIRAM = new int[0x1000]; // TODO SERIALIZATION CONSIDERATIONS

    //  private int mirroring;
    private int mode;

    public Mapper512(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (mode == 1 && address >= 0x2000 && address < 0x3F00) {
            CIRAM[address & 0x0FFF] = value;
        } else {
            super.writeVRAM(address, value);
        }
    }

    @Override
    public int readVRAM(final int address) {
        if (mode == 1 && address >= 0x2000 && address < 0x3F00) {
            return CIRAM[address & 0x0FFF];
        } else {
            return super.readVRAM(address);
        }
    }

    private void writeMode(final int value) {
        System.out.println(value); // TODO REMOVE
        mode = value & 3;
//    super.setNametableMirroring((mode == 1) ? FOUR_SCREEN : mirroring);
        chrRamPresent = mode != 0;
        updateChrBanks();
    }

//  @Override public void setNametableMirroring(final int nametableMirroring) {
//    mirroring = nametableMirroring;
//    if (mode != 1) {
//      super.setNametableMirroring(nametableMirroring);
//    }
//  }

    @Override
    protected void updateChrBanks() {
        if (mode >= 2) {
            set4ChrBanks(0, 0);
            set4ChrBanks(4, 0);
        } else {
            super.updateChrBanks();
        }
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address == 0x4100) {
            writeMode(value);
        }
        super.writeMemory(address, value);
    }
}