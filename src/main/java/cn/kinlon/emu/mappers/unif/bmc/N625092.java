package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class N625092 extends Mapper {

    private static final long serialVersionUID = 0;

    private int cmd;
    private int bank;
    private int ass;

    public N625092(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        cmd = 0;
        bank = 0;
        updateBanks();
    }

    @Override
    public void resetting() {
        init();
        ass++;
    }

    private void updateBanks() {
        setNametableMirroring(cmd & 1);
        setChrBank(0);
        final int high = (cmd & 0xFC) >> 2;
        if (getBitBool(cmd, 1)) {
            if (getBitBool(cmd, 8)) {
                setPrgBank(2, high | bank);
                setPrgBank(3, high | 7);
            } else {
                setPrgBank(2, high | (bank & 6));
                setPrgBank(3, high | ((bank & 6) | 1));
            }
        } else {
            setPrgBank(2, high | bank);
            setPrgBank(3, high | bank);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address >= 0xC000) {
            bank = address & 7;
            updateBanks();
        } else {
            cmd = address;
            if (address == 0x80F8) {
                setPrgBank(2, ass);
                setPrgBank(3, ass);
            } else {
                updateBanks();
            }
        }
    }
}
