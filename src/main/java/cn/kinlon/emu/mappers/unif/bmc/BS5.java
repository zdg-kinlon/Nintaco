package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.mappers.NametableMirroring.*;

public class BS5 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] prgRegs = new int[4];
    private final int[] chrRegs = new int[4];

    private int resetData;

    public BS5(final CartFile cartFile) {
        super(cartFile, 8, 4);
    }

    @Override
    public void init() {
        prgRegs[0] = prgRegs[1] = prgRegs[2] = prgRegs[3] = -1;
        updateBanks();
    }

    @Override
    public void resetting() {
        resetData = (resetData + 1) & 3;
        prgRegs[0] = prgRegs[1] = prgRegs[2] = prgRegs[3] = -1;
        updateBanks();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        final int register = (address & 0x0C00) >> 10;
        switch (address & 0xF000) {
            case 0x8000:
                chrRegs[register] = address & 0x001F;
                break;
            case 0xA000:
                if ((address & (1 << (resetData + 4))) != 0) {
                    prgRegs[register] = address & 0x000F;
                }
                break;
        }
        updateBanks();
    }

    private void updateBanks() {
        setPrgBank(4, prgRegs[0]);
        setPrgBank(5, prgRegs[1]);
        setPrgBank(6, prgRegs[2]);
        setPrgBank(7, prgRegs[3]);
        setChrBank(0, chrRegs[0]);
        setChrBank(1, chrRegs[1]);
        setChrBank(2, chrRegs[2]);
        setChrBank(3, chrRegs[3]);
        setNametableMirroring(VERTICAL);
    }
}
