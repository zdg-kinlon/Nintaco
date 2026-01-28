package nintaco.mappers.taito;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class TC0690 extends Mapper {

    private static final long serialVersionUID = 0;

    private int irqReloadValue;
    private boolean irqEnabled;
    private int irqCounter;
    private int irqResetDelay;
    private boolean irqReloadRequest;
    private boolean isTC0690;

    public TC0690(final CartFile cartFile) {
        super(cartFile, 8, 6);
    }

    @Override
    public void init() {
        setPrgBank(6, -2);
        setPrgBank(7, -1);
    }

    @Override
    public int readVRAM(int address) {
        if (address < 0x1000) {
            return chrROM[(chrBanks[address >> 11] | (address & 0x07FF))
                    & chrRomSizeMask];
        } else if (address < 0x2000) {
            return chrROM[(chrBanks[2 + ((address >> 10) & 3)] | (address & 0x03FF))
                    & chrRomSizeMask];
        } else {
            return vram[address];
        }
    }

    @Override
    public void writeMemory(int address, int value) {
        switch (address & 0xE003) {
            case 0x8000:
                setPrgBank(4, value);
                if (!isTC0690) {
                    setNametableMirroring(getBit(value, 6));
                }
                break;
            case 0x8001:
                setPrgBank(5, value);
                break;
            case 0x8002:
                writeChrBank(0, value, 11);
                break;
            case 0x8003:
                writeChrBank(1, value, 11);
                break;
            case 0xA000:
                writeChrBank(2, value, 10);
                break;
            case 0xA001:
                writeChrBank(3, value, 10);
                break;
            case 0xA002:
                writeChrBank(4, value, 10);
                break;
            case 0xA003:
                writeChrBank(5, value, 10);
                break;
            case 0xC000:
                writeIrqReload(value);
                isTC0690 = true;
                break;
            case 0xC001:
                writeIrqClear();
                isTC0690 = true;
                break;
            case 0xC002:
                writeIrqEnable();
                isTC0690 = true;
                break;
            case 0xC003:
                writeIrqAcknowledge();
                isTC0690 = true;
                break;
            case 0xE000:
                setNametableMirroring(getBit(value, 6));
                isTC0690 = true;
                break;
            default:
                memory[address] = value;
                break;
        }
    }

    private void writeIrqReload(final int value) {
        irqReloadValue = value ^ 0xFF;
    }

    private void writeIrqClear() {
        irqCounter = 0;
        irqReloadRequest = true;
    }

    private void writeIrqEnable() {
        irqEnabled = true;
    }

    private void writeIrqAcknowledge() {
        irqEnabled = false;
        cpu.interrupt().setMapperIrq(false);
    }

    private void writeChrBank(final int bank, final int value, final int shift) {
        chrBanks[bank] = value << shift;
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {

        if (irqResetDelay > 0) {
            irqResetDelay--;
        }

        final boolean a12 = (address & 0x1000) != 0;
        if (a12 && irqResetDelay == 0) {
            if (irqCounter > 0) {
                irqCounter--;
            } else {
                irqCounter = irqReloadValue;
            }
            if (irqReloadRequest) {
                irqReloadRequest = false;
                irqCounter = irqReloadValue;
            }
            if (irqCounter == 0 && irqEnabled) {
                cpu.interrupt().setMapperIrq(true);
            }
        }
        if (a12) {
            irqResetDelay = 8;
        }
    }
}