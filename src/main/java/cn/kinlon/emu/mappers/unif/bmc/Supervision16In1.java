package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class Supervision16In1 extends Mapper {

    private static final long serialVersionUID = 0;

    private int game;
    private int data;

    public Supervision16In1(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x6000, 0x6000);
    }

    private void updateBanks() {
        setPrgBank(3, ((game & 0x0F) << 4) | 0x0F);
        if (getBitBool(game, 4)) {
            setPrgBanks(4, 2, (((game & 0x0F) << 3) | data) << 1);
            setPrgBanks(6, 2, (((game & 0x0F) << 3) | 0x07) << 1);
        } else {
            setPrgBanks(4, 4, 0x100);
        }
        setChrBank(0);
        setNametableMirroring(getBit(game, 5));
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (!getBitBool(game, 4)) {
            game = value & 0x3F;
        }
        data = value & 7;
        updateBanks();
    }
}