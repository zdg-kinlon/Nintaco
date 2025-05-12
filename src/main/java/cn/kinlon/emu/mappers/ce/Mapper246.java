package cn.kinlon.emu.mappers.ce;

import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.mappers.Mapper;

public class Mapper246 extends Mapper {

    private static final long serialVersionUID = 0;

    public Mapper246(NesFile nesFile) {
        super(nesFile, 8, 4);
        setPrgBank(7, -1);
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address >= 0x6000 && address <= 0x67FF) {
            switch (address & 0x6007) {
                case 0x6000:
                    setPrgBank(4, value);
                    break;
                case 0x6001:
                    setPrgBank(5, value);
                    break;
                case 0x6002:
                    setPrgBank(6, value);
                    break;
                case 0x6003:
                    setPrgBank(7, value);
                    break;
                case 0x6004:
                    setChrBank(0, value);
                    break;
                case 0x6005:
                    setChrBank(1, value);
                    break;
                case 0x6006:
                    setChrBank(2, value);
                    break;
                case 0x6007:
                    setChrBank(3, value);
                    break;
            }
        } else {
            memory[address] = value;
        }
    }
}
