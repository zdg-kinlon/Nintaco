package cn.kinlon.emu.mappers.jy;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

public class Mapper091 extends MMC3 {

    private static final long serialVersionUID = 0;

    public Mapper091(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        prgBanks[6] = prgROM.length - 0x4000;
        prgBanks[7] = prgROM.length - 0x2000;
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address >= 0x6000) {
            switch (address & 0xF003) {
                case 0x6000:
                    writeChrReg(0, value);
                    break;
                case 0x6001:
                    writeChrReg(2, value);
                    break;
                case 0x6002:
                    writeChrReg(4, value);
                    break;
                case 0x6003:
                    writeChrReg(6, value);
                    break;
                case 0x7000:
                    writePrgReg(4, value);
                    break;
                case 0x7001:
                    writePrgReg(5, value);
                    break;
                case 0x7002:
                    writeIrqDisable();
                    break;
                case 0x7003:
                    writeIrqLatch(0x07);
                    writeIrqReload();
                    writeIrqEnable();
                    break;
            }
        } else {
            memory[address] = value;
        }
    }

    protected void writePrgReg(final int register, final int value) {
        prgBanks[register] = value << 13;
    }

    protected void writeChrReg(final int register, final int value) {
        chrBanks[register] = value << 11;
        chrBanks[register + 1] = chrBanks[register] + 0x0400;
    }
}