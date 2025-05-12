package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class BMC12In1 extends Mapper {

    private static final long serialVersionUID = 0;

    private int reg0;
    private int reg1;
    private int mode;

    public BMC12In1(final CartFile cartFile) {
        super(cartFile, 4, 2);
    }

    @Override
    public void init() {
        updateBanks();
    }

    private void updateBanks() {
        final int bank = (mode & 3) << 3;
        setChrBank(0, (reg0 >> 3) | (bank << 2));
        setChrBank(1, (reg1 >> 3) | (bank << 2));
        if (getBitBool(mode, 3)) {
            setPrgBanks(2, 2, bank | (reg0 & 0x06));
        } else {
            setPrgBank(2, bank | (reg0 & 0x07));
            setPrgBank(3, bank | 0x07);
        }
        setNametableMirroring(getBit(mode, 2));
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xE000) {
            case 0xA000:
                reg0 = value;
                updateBanks();
                break;
            case 0xC000:
                reg1 = value;
                updateBanks();
                break;
            case 0xE000:
                mode = value & 0x0F;
                updateBanks();
                break;
        }
    }
}