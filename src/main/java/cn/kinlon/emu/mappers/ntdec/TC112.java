package cn.kinlon.emu.mappers.ntdec;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class TC112 extends Mapper {

    private static final long serialVersionUID = 0;

    public TC112(final CartFile cartFile) {
        super(cartFile, 8, 4);
        setPrgBank(5, -3);
        setPrgBank(6, -2);
        setPrgBank(7, -1);
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address >= 0x6000 && address < 0x8000) {
            switch (address & 0x6003) {
                case 0x6000:
                    writeChrBank(0, value);
                    writeChrBank(1, value + 2);
                    break;
                case 0x6001:
                    writeChrBank(2, value);
                    break;
                case 0x6002:
                    writeChrBank(3, value);
                    break;
                case 0x6003:
                    setPrgBank(4, value);
                    break;
            }
        } else {
            memory[address] = value;
        }
    }

    private void writeChrBank(int bank, int value) {
        chrBanks[bank] = value << 10;
    }
}
