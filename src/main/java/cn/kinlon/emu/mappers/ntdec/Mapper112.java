package cn.kinlon.emu.mappers.ntdec;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Mapper112 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] reg = new int[8];
    private int mirroring;
    private int register;
    private int bank;

    public Mapper112(final CartFile cartFile) {
        super(cartFile, 8, 8, 0x4020, 0x8000);
    }

    @Override
    public void init() {
        bank = 0;
        setPrgBanks(6, 2, -2);
    }

    private void updateBanks() {
        setNametableMirroring(mirroring);

        setPrgBank(4, reg[0]);
        setPrgBank(5, reg[1]);

        setChrBanks(0, 2, reg[2] & 0xFE);
        setChrBanks(2, 2, reg[3] & 0xFE);

        setChrBank(4, ((bank & 0x10) << 4) | reg[4]);
        setChrBank(5, ((bank & 0x20) << 3) | reg[5]);
        setChrBank(6, ((bank & 0x40) << 2) | reg[6]);
        setChrBank(7, ((bank & 0x80) << 1) | reg[7]);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address < 0x6000 || address >= 0x8000) {
            switch (address) {
                case 0x8000:
                    register = value & 7;
                    break;
                case 0xA000:
                    reg[register] = value;
                    updateBanks();
                    break;
                case 0xC000:
                    bank = value;
                    updateBanks();
                    break;
                case 0xE000:
                    mirroring = value & 1;
                    updateBanks();
                    break;
            }
        }
    }
}