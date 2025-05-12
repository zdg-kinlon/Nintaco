package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class BMCCTC12IN1 extends Mapper {

    private static final long serialVersionUID = 0;

    private int reg;

    public BMCCTC12IN1(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        reg = 0;
        setPrgBank(3, 1);
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {
        switch (reg & 0xC0) {
            case 0x40:
                set4PrgBanks(4, (reg & 0x1F) << 1);
                break;
            case 0x80:
            case 0xC0:
                set2PrgBanks(4, (reg & 0x3F) << 1);
                set2PrgBanks(6, ((reg & 0x38) | 0x07) << 1);
                break;
            default:
                set2PrgBanks(4, (reg & 0x1F) << 1);
                set2PrgBanks(6, (reg & 0x1F) << 1);
                break;
        }
        setNametableMirroring(getBit(reg, 5));
    }

    private void writeOuterBankRegister(final int value) {
        reg = (value & 0xF8) | (reg & 0x07);
        updateState();
    }

    private void writeInnerBankRegister(final int value) {
        reg = (reg & 0xF8) | (value & 0x07);
        updateState();
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address >= 0x2000 || (reg & 0x80) != 0) {
            vram[address] = value;
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address >= 0xC000) {
            writeInnerBankRegister(value);
        } else {
            writeOuterBankRegister(value);
        }
    }
}