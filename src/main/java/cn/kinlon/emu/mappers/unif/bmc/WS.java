package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class WS extends Mapper {

    private int reg0;
    private int reg1;

    public WS(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x6000, 0x8000);
    }

    @Override
    public void init() {
        reg0 = reg1 = 0;
        updateBanks();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateBanks() {
        setChrBank(reg1 & 7);
        if (getBitBool(reg0, 3)) {
            final int bank = (reg0 & 7) << 1;
            setPrgBanks(4, 2, bank);
            setPrgBanks(6, 2, bank);
        } else {
            setPrgBanks(4, 4, (reg0 & 6) << 1);
        }

        setNametableMirroring(getBit(reg0, 4));
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (!getBitBool(reg0, 5)) {
            if (getBitBool(address, 0)) {
                reg1 = value;
            } else {
                reg0 = value;
            }
            updateBanks();
        }
    }
}