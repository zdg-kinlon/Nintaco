package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper272 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] prgRegs = new int[2];
    private final int[] chrRegs = new int[8];

    private int hvMirroring;
    private int oneScreenMirroring;
    private int lastBit13;
    private int irqCounter;
    private boolean irqCounterEnabled;

    public Mapper272(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        lastBit13 = irqCounter = 0;
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {

        setPrgBank(4, prgRegs[0]);
        setPrgBank(5, prgRegs[1]);
        setPrgBank(6, -2);
        setPrgBank(7, -1);

        for (int i = 7; i >= 0; --i) {
            setChrBank(i, chrRegs[i]);
        }

        switch (oneScreenMirroring) {
            case 2:
            case 3:
                setNametableMirroring(oneScreenMirroring);
                break;
            default:
                setNametableMirroring(hvMirroring);
                break;
        }
    }

    @Override
    public int readVRAM(final int address) {
        final int bit13 = (address >> 13) & 1;
        if ((lastBit13 == 1) && (bit13 == 0) && irqCounterEnabled
                && ++irqCounter == 84) {
            irqCounter = 0;
            cpu.setMapperIrq(true);
        }
        lastBit13 = bit13;
        return super.readVRAM(address);
    }

    @Override
    protected void writeRegister(final int address, final int value) {

        switch (0xF003 & address) {
            case 0x8000:
            case 0x8001:
            case 0x8002:
            case 0x8003:
                prgRegs[0] = value;
                break;
            case 0xA000:
            case 0xA001:
            case 0xA002:
            case 0xA003:
                prgRegs[1] = value;
                break;
            case 0x9000:
            case 0x9001:
            case 0x9002:
            case 0x9003:
                hvMirroring = value & 1;
                break;
            case 0xB000:
                chrRegs[0] = (chrRegs[0] & 0xF0) | (value & 0x0F);
                break;
            case 0xB001:
                chrRegs[0] = ((value & 0x0F) << 4) | (chrRegs[0] & 0x0F);
                break;
            case 0xB002:
                chrRegs[1] = (chrRegs[1] & 0xF0) | (value & 0x0F);
                break;
            case 0xB003:
                chrRegs[1] = ((value & 0x0F) << 4) | (chrRegs[1] & 0x0F);
                break;
            case 0xC000:
                chrRegs[2] = (chrRegs[2] & 0xF0) | (value & 0x0F);
                break;
            case 0xC001:
                chrRegs[2] = ((value & 0x0F) << 4) | (chrRegs[2] & 0x0F);
                break;
            case 0xC002:
                chrRegs[3] = (chrRegs[3] & 0xF0) | (value & 0x0F);
                break;
            case 0xC003:
                chrRegs[3] = ((value & 0x0F) << 4) | (chrRegs[3] & 0x0F);
                break;
            case 0xD000:
                chrRegs[4] = (chrRegs[4] & 0xF0) | (value & 0x0F);
                break;
            case 0xD001:
                chrRegs[4] = ((value & 0x0F) << 4) | (chrRegs[4] & 0x0F);
                break;
            case 0xD002:
                chrRegs[5] = (chrRegs[5] & 0xF0) | (value & 0x0F);
                break;
            case 0xD003:
                chrRegs[5] = ((value & 0x0F) << 4) | (chrRegs[5] & 0x0F);
                break;
            case 0xE000:
                chrRegs[6] = (chrRegs[6] & 0xF0) | (value & 0x0F);
                break;
            case 0xE001:
                chrRegs[6] = ((value & 0x0F) << 4) | (chrRegs[6] & 0x0F);
                break;
            case 0xE002:
                chrRegs[7] = (chrRegs[7] & 0xF0) | (value & 0x0F);
                break;
            case 0xE003:
                chrRegs[7] = ((value & 0x0F) << 4) | (chrRegs[7] & 0x0F);
                break;
        }

        switch (0xC00C & address) {
            case 0x8004:
                oneScreenMirroring = value & 3;
                break;
            case 0x800C:
                cpu.setMapperIrq(true);
                break;
            case 0xC004:
                cpu.setMapperIrq(false);
                break;
            case 0xC008:
                irqCounterEnabled = true;
                break;
            case 0xC00C:
                irqCounterEnabled = false;
                irqCounter = 0;
                cpu.setMapperIrq(false);
                break;
        }

        updateState();
    }
}
