package cn.kinlon.emu.mappers.jaleco;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.mappers.NametableMirroring.*;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class SS88006 extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[][] IRQ_MASKS = {
            {0xFFFF, 0x0000},
            {0x0FFF, 0xF000},
            {0x00FF, 0xFF00},
            {0x000F, 0xFFF0},
    };
    private int irqSize;
    private int irqCounter;
    private int irqReload;
    private boolean irqEnabled;
    public SS88006(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        setPrgBank(7, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address >= 0xA000 && address < 0xE000) {
            writeChrBank(address, value);
        } else {
            switch (address & 0xF003) {
                case 0x8000:
                    writePrgBankLow(4, value);
                    break;
                case 0x8001:
                    writePrgBankHigh(4, value);
                    break;
                case 0x8002:
                    writePrgBankLow(5, value);
                    break;
                case 0x8003:
                    writePrgBankHigh(5, value);
                    break;
                case 0x9000:
                    writePrgBankLow(6, value);
                    break;
                case 0x9001:
                    writePrgBankHigh(6, value);
                    break;
                case 0xE000:
                case 0xE001:
                case 0xE002:
                case 0xE003:
                    writeIrqReload(address, value);
                    break;
                case 0xF000:
                    writeIrqReset();
                    break;
                case 0xF001:
                    writeIrqControl(value);
                    break;
                case 0xF002:
                    writeMirroring(value);
                    break;
                case 0xF003:
                    // TODO uPD7756C ADPCM sound IC play sound
                    break;
            }
        }
    }

    private void writeMirroring(final int value) {
        switch (value & 3) {
            case 0:
                setNametableMirroring(HORIZONTAL);
                break;
            case 1:
                setNametableMirroring(VERTICAL);
                break;
            case 2:
                setNametableMirroring(ONE_SCREEN_A);
                break;
            case 3:
                setNametableMirroring(ONE_SCREEN_B);
                break;
        }
    }

    private void writeIrqReset() {
        irqCounter = irqReload;
        cpu.setMapperIrq(false);
    }

    private void writeIrqControl(final int value) {
        irqEnabled = getBitBool(value, 0);
        if (getBitBool(value, 3)) {
            irqSize = 3;
        } else if (getBitBool(value, 2)) {
            irqSize = 2;
        } else if (getBitBool(value, 1)) {
            irqSize = 1;
        } else {
            irqSize = 0;
        }
        cpu.setMapperIrq(false);
    }

    private void writeIrqReload(final int address, final int value) {
        int shift = (address & 3) << 2;
        irqReload = (irqReload & ~(0x0F << shift)) | ((value & 0x0F) << shift);
    }

    private void writeChrBank(final int address, final int value) {
        int bank = (((address >> 12) - 0x0A) << 1) | ((address >> 1) & 1);
        int shift = (address & 1) << 2;
        chrBanks[bank] = (chrBanks[bank] & (0x03C000 >> shift))
                | ((value & 0x0F) << (10 + shift));
    }

    private void writePrgBankLow(final int bank, final int value) {
        prgBanks[bank] = (prgBanks[bank] & 0x1E0000) | ((value & 0x0F) << 13);
    }

    private void writePrgBankHigh(final int bank, final int value) {
        prgBanks[bank] = (prgBanks[bank] & 0x01E000) | ((value & 0x0F) << 17);
    }

    @Override
    public void update() {
        if (irqEnabled) {
            if ((irqCounter & IRQ_MASKS[irqSize][0]) == 0) {
                cpu.setMapperIrq(true);
            }
            irqCounter = (irqCounter & IRQ_MASKS[irqSize][1])
                    | ((irqCounter - 1) & IRQ_MASKS[irqSize][0]);
        }
    }
}
