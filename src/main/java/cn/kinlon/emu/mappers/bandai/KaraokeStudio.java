package cn.kinlon.emu.mappers.bandai;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class KaraokeStudio extends Mapper {

    private static final long serialVersionUID = 0;

    private final boolean expansionRomPresent;

    private boolean internalRomSelected;

    public KaraokeStudio(final CartFile cartFile) {
        super(cartFile, 4, 1);
        expansionRomPresent = cartFile.getPrgRomLength() >= 0x40000;
    }

    @Override
    public void init() {
        setPrgBank(2, 0);
        setPrgBank(3, 7);
        setChrBank(0);
        internalRomSelected = true;
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xE000) == 0x6000) {
            return readMicrophone();
        } else if (!(expansionRomPresent || internalRomSelected)
                && (address & 0xC000) == 0x8000) {
            return 0;
        } else {
            return super.readMemory(address);
        }
    }

    private int readMicrophone() {
        return 0x03; // TODO IMPLEMENT MICROPHONE AND BUTTONS
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        internalRomSelected = getBitBool(value, 4);
        if (internalRomSelected) {
            setPrgBank(2, value & 7);
        } else if (expansionRomPresent) {
            setPrgBank(2, 8 | (value & 7));
        }
        setNametableMirroring(getBit(value, 5));
    }
}