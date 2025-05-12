package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.mappers.NametableMirroring.ONE_SCREEN_A;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class BMC235 extends Mapper {

    private static final long serialVersionUID = 0;

    private int register;

    public BMC235(final NesFile nesFile) {
        super(nesFile, 4, 1);
    }

    @Override
    public void init() {
        register = 0;
        setChrBank(0);
        updateBanks();
    }

    private void updateBanks() {
        if (getBitBool(register, 10)) {
            setNametableMirroring(ONE_SCREEN_A);
        } else {
            setNametableMirroring((register >> 13) & 1);
        }
        if (getBitBool(register, 11)) {
            setPrgBank(2, ((register & 0x300) >> 3) | ((register & 0x1F) << 1)
                    | ((register >> 12) & 1));
            setPrgBank(3, ((register & 0x300) >> 3) | ((register & 0x1F) << 1)
                    | ((register >> 12) & 1));
        } else {
            final int b = (((register & 0x300) >> 4) | (register & 0x1F)) << 1;
            setPrgBank(2, b);
            setPrgBank(3, b | 1);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        register = address;
        updateBanks();
    }
}