package cn.kinlon.emu.mappers.ntdec;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

// TODO ZAPPER NOT FULLY WORKING

public class TF1201 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] chrRegs = new int[8];

    private int prgReg0;
    private int prgReg1;
    private boolean prgMode;

    private int irqCounter;
    private boolean irqEnabled;

    public TF1201(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        updatePrgBanks();
        updateChrBanks();
    }

    private void updatePrgBanks() {
        if (prgMode) {
            setPrgBank(4, -2);
            setPrgBank(6, prgReg0);
        } else {
            setPrgBank(4, prgReg0);
            setPrgBank(6, -2);
        }
        setPrgBank(5, prgReg1);
        setPrgBank(7, -1);
    }

    private void updateChrBanks() {
        for (int i = 0; i < 8; i++) {
            setChrBank(i, chrRegs[i]);
        }
    }

    @Override
    protected void writeRegister(int address, final int value) {
        address = (address & 0xF003) | ((address & 0x000C) >> 2);
        if (address >= 0xB000 && address <= 0xE003) {
            final int register = (((address >> 11) - 6) | (address & 1)) & 7;
            final int shift = ((address & 2) << 1);
            chrRegs[register] = (chrRegs[register] & (0xF0 >> shift))
                    | ((value & 0x0F) << shift);
            updateChrBanks();
        } else {
            switch (address & 0xF003) {
                case 0x8000:
                    prgReg0 = value;
                    updatePrgBanks();
                    break;
                case 0xA000:
                    prgReg1 = value;
                    updatePrgBanks();
                    break;
                case 0x9000:
                    setNametableMirroring(value & 1);
                    break;
                case 0x9001:
                    prgMode = (value & 3) != 0;
                    updatePrgBanks();
                    break;
                case 0xF000:
                    irqCounter = ((irqCounter & 0xF0) | (value & 0xF));
                    break;
                case 0xF002:
                    irqCounter = ((irqCounter & 0x0F) | ((value & 0xF) << 4));
                    break;
                case 0xF001:
                case 0xF003:
                    irqEnabled = (value & 2) != 0;
                    cpu.setMapperIrq(false);
                    if (ppu.getScanline() < 240) {
                        irqCounter -= 8;
                    }
                    break;
            }
        }
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {
        if (irqEnabled && rendering && scanlineCycle == 0 && ++irqCounter == 237) {
            cpu.setMapperIrq(true);
        }
    }
}