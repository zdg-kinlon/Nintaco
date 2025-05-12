package cn.kinlon.emu.mappers.ave;

import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Maxi15 extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean nina03;
    private int outer;
    private int inner;

    public Maxi15(NesFile nesFile) {
        super(nesFile, 2, 1);
    }

    @Override
    public void init() {
        writeOuterBankControl(0);
        writeInnerBankControl(0);
    }

    @Override
    public int readMemory(int address) {

        int value;
        if (address < 0x8000) {
            value = memory[address];
        } else {
            value = prgROM[(prgBanks[1] | (address & 0x7FFF)) & prgRomSizeMask];
            switch (address & 0xFFF8) {
                case 0xFF80:
                case 0xFF88:
                case 0xFF90:
                case 0xFF98:
                    writeOuterBankControl(value);
                    break;
                case 0xFFE8:
                case 0xFFF0:
                    writeInnerBankControl(value);
                    break;
            }
        }

        return value;
    }

    private void writeOuterBankControl(int value) {
        if (outer == 0) {
            outer = value;
            setNametableMirroring(getBit(value, 7));
            nina03 = getBitBool(value, 6);
            updateBanks();
        }
    }

    private void writeInnerBankControl(int value) {
        inner = value;
        updateBanks();
    }

    private void updateBanks() {
        if (nina03) {
            setPrgBank((outer & 0x0E) | (inner & 0x01));
            setChrBank(((outer & 0x0E) << 2) | ((inner & 0x70) >> 4));
        } else {
            setPrgBank(outer & 0x0F);
            setChrBank(((outer & 0x0F) << 2) | ((inner & 0x30) >> 4));
        }
    }
}
