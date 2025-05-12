package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Mapper117 extends Mapper {

    private static final long serialVersionUID = 0;

    private int irqCounter;
    private int irqReloadValue;
    private int irqResetDelay;
    private boolean irqEnabled;
    private boolean irqEnabledAlt;

    public Mapper117(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        setPrgBanks(4, 4, -4);
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {

        if (irqResetDelay > 0) {
            irqResetDelay--;
        }

        final boolean a12 = (address & 0x1000) != 0;
        if (a12 && irqResetDelay == 0 && irqEnabled && irqEnabledAlt
                && irqCounter > 0 && --irqCounter == 0) {
            cpu.setMapperIrq(true);
            irqEnabledAlt = false;
        }
        if (a12) {
            irqResetDelay = 8;
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address) {
            case 0x8000:
                setPrgBank(4, value);
                break;
            case 0x8001:
                setPrgBank(5, value);
                break;
            case 0x8002:
                setPrgBank(6, value);
                break;
            case 0x8003:
                setPrgBank(7, value);
                break;

            case 0xA000:
            case 0xA001:
            case 0xA002:
            case 0xA003:
            case 0xA004:
            case 0xA005:
            case 0xA006:
            case 0xA007:
                setChrBank(address & 0x07, value);
                break;

            case 0xC001:
                irqReloadValue = value;
                break;
            case 0xC002:
                cpu.setMapperIrq(false);
                break;
            case 0xC003:
                irqCounter = irqReloadValue;
                irqEnabledAlt = true;
                break;

            case 0xD000:
                setNametableMirroring(value & 1);
                break;

            case 0xE000:
                irqEnabled = getBitBool(value, 0);
                cpu.setMapperIrq(false);
                break;
        }
    }
}