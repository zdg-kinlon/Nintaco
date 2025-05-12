package cn.kinlon.emu.mappers.kaiser;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.mappers.NametableMirroring.VERTICAL;

public class Kaiser7013B extends Mapper {

    private static final long serialVersionUID = 0;

    public Kaiser7013B(final CartFile cartFile) {
        super(cartFile, 4, 1, 0x6000, 0x8000);
    }

    @Override
    public void init() {
        setPrgBank(2, 0);
        setPrgBank(3, -1);
        setChrBank(0, 0);
        setNametableMirroring(VERTICAL);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address < 0x8000) {
            setPrgBank(2, value);
        } else {
            setNametableMirroring(value & 1);
        }
    }
}
