package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



import static cn.kinlon.emu.utils.BitUtil.*;

public class Mapper182 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] REGISTERS = {0, 3, 1, 5, 6, 7, 2, 4};

    public Mapper182(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address < 0x8000) {
            memory[address] = value;
        } else {
            switch (address & 0xE001) {
                case 0x8001:
                    writeMirroring(value);
                    break;
                case 0xA000:
                    writeBankSelect(value);
                    break;
                case 0xC000:
                    writeBankData(value);
                    break;
                case 0xC001:
                    writeIrqLatch(value);
                    writeIrqReload();
                    break;
                case 0xE000:
                    writeIrqDisable();
                    break;
                case 0xE001:
                    writeIrqEnable();
                    break;
            }
        }
    }

    @Override
    protected void writeBankSelect(int value) {
        chrMode = getBitBool(value, 7);
        prgMode = getBitBool(value, 6);
        register = REGISTERS[value & 7];
        updateBanks();
    }
}
