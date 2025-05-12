package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class MrMary2 extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[] LUT = {4, 3, 5, 3, 6, 3, 7, 3};

    private int bank6;
    private int irqCounter;
    private boolean irqEnabled;
    private boolean swap;

    public MrMary2(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x4020, 0x6000);
    }

    @Override
    public void init() {
        bank6 = 0;
        irqCounter = 0;
        irqEnabled = false;
        swap = false;

        updateBanks();
        setPrgBank(4, 1);
        setPrgBank(5, 0);
        setChrBank(0);
    }

    @Override
    public void update() {
        if (irqEnabled) {
            irqCounter++;
            if (irqCounter >= 4096) {
                irqEnabled = false;
                cpu.setMapperIrq(true);
            }
        }
    }

    private void updateBanks() {
        setPrgBank(3, swap ? 0 : 2);
        setPrgBank(6, bank6);
        setPrgBank(7, swap ? 8 : 9);
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xF000) == 0x5000) {
            return prgROM[(0x10000 | (address & prgAddressMask))
                    & prgRomSizeMask];
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        switch (address & 0xF1FF) {
            case 0x4022:
                bank6 = LUT[value & 7];
                updateBanks();
                break;
            case 0x4120:
                swap = getBitBool(value, 0);
                updateBanks();
                break;

            case 0x4122:
            case 0x8122:
                irqEnabled = getBitBool(value, 0);
                cpu.setMapperIrq(false);
                irqCounter = 0;
                break;
        }
    }
}
