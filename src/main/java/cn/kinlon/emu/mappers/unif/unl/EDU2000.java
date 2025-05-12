package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.mappers.NametableMirroring.*;

public class EDU2000 extends Mapper {

    private static final long serialVersionUID = 0;

    private int xramOffset;

    public EDU2000(final CartFile cartFile) {
        super(cartFile, 2, 0);
        xram = new int[0x8000];
        setNametableMirroring(ONE_SCREEN_A);
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xE000) == 0x6000) {
            return xram[xramOffset | (address & 0x1FFF)];
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xE000) == 0x6000) {
            xram[xramOffset | (address & 0x1FFF)] = value;
        } else {
            super.writeMemory(address, value);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        xramOffset = (value & 0xC0) << 7;
        setPrgBank(1, value & 0x1F);
    }
}