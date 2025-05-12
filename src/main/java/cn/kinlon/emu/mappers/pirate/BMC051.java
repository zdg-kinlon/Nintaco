package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.mappers.NametableMirroring.HORIZONTAL;
import static cn.kinlon.emu.mappers.NametableMirroring.VERTICAL;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class BMC051 extends Mapper {

    private static final long serialVersionUID = 0;

    private int bank;
    private int mode;

    public BMC051(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x6000, 0x6000);
    }

    @Override
    public void init() {
        bank = 0;
        mode = 1;
        updateBanks();
    }

    private void updateBanks() {
        if (getBitBool(mode, 0)) {
            setPrgBank(3, (0x23 | (bank << 2)));
            setPrgBanks(4, 4, bank << 2);
        } else {
            setPrgBank(3, (0x2F | (bank << 2)));
            setPrgBanks(4, 2, (bank << 2) | mode);
            setPrgBanks(6, 2, (bank << 2) | 0x0E);
        }
        setNametableMirroring(mode == 0x03 ? HORIZONTAL : VERTICAL);
    }

    @Override
    protected void writeRegister(int address, int value) {
        if (address < 0x8000) {
            mode = ((value >> 3) & 0x02) | ((value >> 1) & 0x01);
        } else if (address >= 0xC000 && address < 0xE000) {
            bank = value & 0x0F;
            mode = ((value >> 3) & 0x02) | (mode & 0x01);
        } else {
            bank = value & 0x0F;
        }
        updateBanks();
    }
}