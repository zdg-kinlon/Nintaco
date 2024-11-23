package nintaco.mappers.ntdec;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class AsderPC95 extends Mapper {

    private static final long serialVersionUID = 0;

    private int keyboardRow;

    public AsderPC95(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    private int readKeyboardButtonState() {
        int state = 0;
        writeOutputPort(0x05);
        for (int i = 0; i <= keyboardRow; ++i) {
            writeOutputPort(0x04);
            state = (readInputPort(1) >> 1) & 0x0F;
            writeOutputPort(0x06);
            state |= (readInputPort(1) << 3) & 0xF0;
        }
        return state;
    }

    @Override
    public int readMemory(final int address) {
        return (address == 0x4906) ? readKeyboardButtonState()
                : super.readMemory(address);
    }

    private void writePrgRomBank(final int address, int value) {
        final int bank = 4 | (address & 3);
        if (bank == 7) {
            value |= 1;
        }
        setPrgBank(bank, value);
    }

    private void writeChrRamBank(final int address, final int value) {
        setChrBank(address & 7, value);
    }

    private void writeKeyboardRow(final int value) {
        keyboardRow = value;
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address == 0x4904) {
            writeKeyboardRow(value);
        } else {
            switch (address & 0xE000) {
                case 0x8000:
                    writePrgRomBank(address, value);
                    break;
                case 0x9000:
                    writeChrRamBank(address, value);
                    break;
            }
        }
    }
}
