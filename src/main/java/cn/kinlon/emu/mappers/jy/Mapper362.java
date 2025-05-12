package cn.kinlon.emu.mappers.jy;

// TODO WIP

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.konami.VRC2And4;

public class Mapper362 extends VRC2And4 {

    private static final long serialVersionUID = 0;

    protected int prgBlockOffset;
    protected int chrBlockOffset;

    public Mapper362(final CartFile cartFile) {
        super(cartFile);
        variant = VRC4f;
        useHeuristics = false;
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            final int bank = address >> 10;
            final int value = ((chrHigh[bank] << 4) | chrLow[bank]) & 0x180;
            prgBlockOffset = value >> 3;
            chrBlockOffset = value;
            updateBanks();
        }
        return super.readVRAM(address);
    }

    @Override
    protected void writeIrqAcknowledge() {
        cpu.setMapperIrq(false);
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        super.setPrgBank(bank, prgBlockOffset | (0x0F & value));
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        super.setChrBank(bank, chrBlockOffset | (0x7F & value));
    }
}
