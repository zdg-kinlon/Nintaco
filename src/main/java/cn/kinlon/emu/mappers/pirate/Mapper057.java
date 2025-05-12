package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Mapper057 extends Mapper {

    private static final long serialVersionUID = 0;

    private int AAA;
    private int BBB;

    public Mapper057(NesFile nesFile) {
        super(nesFile, 4, 1);
    }

    @Override
    protected void writeRegister(int address, int value) {
        switch (address & 0x8800) {
            case 0x8000:
                writeA(value);
                break;
            case 0x8800:
                writeB(value);
                break;
        }
    }

    private void writeA(int value) {
        AAA = ((value & 0x40) >> 3) | (value & 7);
        updateChrBank();
    }

    private void writeB(int value) {
        int PPP = value >> 5;
        if (getBitBool(value, 4)) {
            setPrgBank(2, PPP & 6);
            setPrgBank(3, PPP | 1);
        } else {
            setPrgBank(2, PPP);
            setPrgBank(3, PPP);
        }
        BBB = value & 7;
        updateChrBank();
        setNametableMirroring(getBit(value, 3));
    }

    private void updateChrBank() {
        setChrBank(0, AAA | BBB);
    }
}
