package nintaco.mappers.bandai;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class OekaKidsTablet extends Mapper {

    private static final long serialVersionUID = 0;

    private int outer;
    private int inner;

    public OekaKidsTablet(final CartFile cartFile) {
        super(cartFile, 2, 2);
        xram = new int[0x8000];
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            return xram[chrBanks[address >> 12] | (address & 0x0FFF)];
        } else {
            return vram[address];
        }
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000) {
            xram[chrBanks[address >> 12] | (address & 0x0FFF)] = value;
        } else {
            vram[address] = value;
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {

        outer = value & 0x04;

        setPrgBank(1, value & 0x03);
        setChrBank(1, outer | 0x03);

        updateChrBank0();
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {

        if ((address & 0x3000) == 0x2000 && (address & 0x03FF) < 0x03C0) {
            inner = (address >> 8) & 0x03;
            updateChrBank0();
        }
    }

    private void updateChrBank0() {
        setChrBank(0, outer | inner);
    }
}