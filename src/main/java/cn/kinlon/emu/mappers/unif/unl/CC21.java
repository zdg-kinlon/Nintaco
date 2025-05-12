package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.mappers.NametableMirroring.ONE_SCREEN_A;

public class CC21 extends Mapper {

    private static final long serialVersionUID = 0;

    private final boolean chr8K;

    private int register;

    public CC21(final CartFile cartFile) {
        super(cartFile, 2, 2);
        chr8K = cartFile.getChrRomLength() == 8192;
    }

    private void updateBanks() {
        setPrgBank(1, 0);
        if (chr8K) {
            setChrBank(0, register);
            setChrBank(1, register);
        } else {
            setChrBanks(0, 2, register << 1);
        }
        setNametableMirroring(ONE_SCREEN_A + register);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        register = ((address == 0x8000) ? value : address) & 1;
        updateBanks();
    }
}