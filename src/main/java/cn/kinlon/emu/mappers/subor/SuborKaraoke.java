package cn.kinlon.emu.mappers.subor;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class SuborKaraoke extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean switchMode;

    public SuborKaraoke(final CartFile cartFile) {
        super(cartFile, 2, 2);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switchMode = getBitBool(value, 7);
        if (switchMode) {
            setChrBank(1, 1);
        } else {
            set2ChrBanks(0, 0);
        }
        setNametableMirroring(getBit(value, 6));
        setPrgBank(value & 0x3F);
    }

    @Override
    public int readVRAM(final int address) {
        if (switchMode && (address >= 0x2000 && address <= 0x3EFF)) {
            setChrBank(0, getBit(address, (nametableMappings[2] == 0x2000)
                    ? 11 : 10));
        }
        return super.readVRAM(address);
    }
}
