package cn.kinlon.emu.mappers.namco;

import cn.kinlon.emu.files.NesFile;

public class NAMCOT3425 extends DxROM {

    private static final long serialVersionUID = 0;

    public NAMCOT3425(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    protected void writeChrBank(final int bank, final int value) {
        setChrBank(bank, value & 0x1F);
    }

    @Override
    protected void writeChrBank2K(final int bank, final int value) {
        final int val = value & 0x1E;
        setChrBank(bank, val);
        setChrBank(bank + 1, val | 1);
        nametableMappings[bank] = nametableMappings[bank + 1]
                = 0x2000 | ((value & 0x20) << 5);
    }
}
