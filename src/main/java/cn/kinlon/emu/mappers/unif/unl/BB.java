
package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class BB extends Mapper {

    private static final long serialVersionUID = 0;

    private int prgReg;
    private int chrReg;

    public BB(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        prgReg = -1;
        chrReg = 0;
        setPrgBanks(4, 4, -4);
        updateBanks();
    }

    private void updateBanks() {
        setPrgBank(3, prgReg & 3);
        setChrBank(chrReg);
    }

    @Override
    public void writeRegister(final int address, final int value) {
        if ((address & 0x9000) == 0x8000) {
            prgReg = chrReg = value;
        } else {
            chrReg = value & 1;
        }
        updateBanks();
    }
}