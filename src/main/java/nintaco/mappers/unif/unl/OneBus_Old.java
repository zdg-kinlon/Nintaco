package nintaco.mappers.unif.unl;

import java.util.Arrays;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.apu.APU.*;

public class OneBus_Old extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[] midX = {0, 1, 2, 0, 3, 4, 5, 0};

    private final int[] cpu410x = new int[16];
    private final int[] ppu201x = new int[16];
    private final int[] apu40xx = new int[64];

    private final int pcmClock = 0xF6;

    private int irqCounter;
    private boolean irqEnabled;
    private boolean irqReloadRequest;
    private int irqResetDelay;

    private int invertHack = 0;

    private boolean pcmEnable;
    private int pcmIRQ;
    private int pcmAddress;
    private int pcmSize;
    private int pcmLatch;

    public OneBus_Old(final CartFile cartFile) {
        super(cartFile, 8, 8);
        if (cartFile.getFileCRC() == 0x305FCDC3
                || cartFile.getFileCRC() == 0x6ABFCE8E) {
            invertHack = 0x0F;
        }
    }

    @Override
    public void init() {
        updateBanks();
    }

    @Override
    public void resetting() {
        irqReloadRequest = false;
        irqCounter = 0;
        irqEnabled = false;

        Arrays.fill(cpu410x, 0);
        Arrays.fill(ppu201x, 0);
        Arrays.fill(apu40xx, 0);

        updateBanks();
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            return prgROM[(chrBanks[address >> chrShift] | (address & chrAddressMask))
                    & prgRomSizeMask];
        } else {
            return vram[address];
        }
    }

    private void updatePrgBanks() {
        final int bankmode = cpu410x[0x0B] & 7;
        final int mask = (bankmode == 0x07) ? 0xFF : (0x3F >> bankmode);
        final int block = ((cpu410x[0x00] & 0xF0) << 4) + (cpu410x[0x0A] & ~mask);
        final int swap = (cpu410x[0x05] & 0x40) >> 5;

        final int bank0 = cpu410x[0x07 ^ invertHack];
        final int bank1 = cpu410x[0x08 ^ invertHack];
        final int bank2 = (cpu410x[0x0B] & 0x40) != 0 ? cpu410x[0x09] : ~1;
        final int bank3 = ~0;

        setPrgBank(4 ^ swap, block | (bank0 & mask));
        setPrgBank(5, block | (bank1 & mask));
        setPrgBank(6 ^ swap, block | (bank2 & mask));
        setPrgBank(7, block | (bank3 & mask));
    }

    private void updateChrBanks() {

        final int mask = 0xFF >> midX[ppu201x[0x0A] & 7];
        final int block = ((cpu410x[0x00] & 0x0F) << 11)
                + ((ppu201x[0x08] & 0x70) << 4) + (ppu201x[0x0A] & ~mask);
        final int swap = (cpu410x[0x05] & 0x80) >> 5;

        final int bank0 = ppu201x[0x06] & ~1;
        final int bank1 = ppu201x[0x06] | 1;
        final int bank2 = ppu201x[0x07] & ~1;
        final int bank3 = ppu201x[0x07] | 1;
        final int bank4 = ppu201x[0x02];
        final int bank5 = ppu201x[0x03];
        final int bank6 = ppu201x[0x04];
        final int bank7 = ppu201x[0x05];

        setChrBank(0 ^ swap, block | (bank0 & mask));
        setChrBank(1 ^ swap, block | (bank1 & mask));
        setChrBank(2 ^ swap, block | (bank2 & mask));
        setChrBank(3 ^ swap, block | (bank3 & mask));
        setChrBank(4 ^ swap, block | (bank4 & mask));
        setChrBank(5 ^ swap, block | (bank5 & mask));
        setChrBank(6 ^ swap, block | (bank6 & mask));
        setChrBank(7 ^ swap, block | (bank7 & mask));

        setNametableMirroring(cpu410x[0x06] & 1);
    }

    private void updateBanks() {
        updatePrgBanks();
        updateChrBanks();
    }

    @Override
    public int readCpuMemory(int address) {

        address &= 0xFFFF;

        if (address == REG_APU_STATUS) {
            int result = apu.readStatus();
            if ((apu40xx[0x30] & 0x10) != 0) {
                result = (result & 0x7F) | pcmIRQ;
            }
            return result;
        }
        return super.readCpuMemory(address);
    }

    @Override
    public void writeCpuMemory(int address, int value) {

        value &= 0xFF;
        address &= 0xFFFF;

        if ((address & 0xFFF0) == 0x2010) {
            ppu201x[address & 0x000F] = value;
            updateBanks();
        } else {
            if ((address & 0xFFF0) == 0x4100) {
                switch (address & 0x000F) {
                    case 0x01:
                        cpu410x[0x01] = value & 0xFE;
                        break;
                    case 0x02:
                        irqReloadRequest = true;
                        break;
                    case 0x03:
                        cpu.setMapperIrq(false);
                        irqEnabled = false;
                        break;
                    case 0x04:
                        irqEnabled = true;
                        break;
                    default:
                        cpu410x[address & 0x0F] = value;
                        updateBanks();
                }
            } else if ((address & 0xFFC0) == 0x4000) {
                apu40xx[address & 0x3F] = value;
                switch (address & 0x3F) {
                    case 0x12:
                        if ((apu40xx[0x30] & 0x10) != 0) {
                            pcmAddress = value << 6;
                        }
                        break;
                    case 0x13:
                        if ((apu40xx[0x30] & 0x10) != 0) {
                            pcmSize = (value << 4) + 1;
                        }
                        break;
                    case 0x15:
                        if ((apu40xx[0x30] & 0x10) != 0) {
                            pcmEnable = (value & 0x10) != 0;
                            if (pcmIRQ != 0) {
                                cpu.setMapperIrq(false);
                                pcmIRQ = 0;
                            }
                            if (pcmEnable) {
                                pcmLatch = pcmClock;
                            }
                            value &= 0xEF;
                        }
                        break;
                }
            }
            super.writeCpuMemory(address, value);
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xE001) {
            case 0x8000:
                cpu410x[0x05] = (cpu410x[0x05] & 0x38) | (value & 0xC7);
                updateBanks();
                break;
            case 0x8001:
                switch (cpu410x[0x05] & 7) {
                    case 0:
                        ppu201x[0x06] = value;
                        updateChrBanks();
                        break;
                    case 1:
                        ppu201x[0x07] = value;
                        updateChrBanks();
                        break;
                    case 2:
                        ppu201x[0x02] = value;
                        updateChrBanks();
                        break;
                    case 3:
                        ppu201x[0x03] = value;
                        updateChrBanks();
                        break;
                    case 4:
                        ppu201x[0x04] = value;
                        updateChrBanks();
                        break;
                    case 5:
                        ppu201x[0x05] = value;
                        updateChrBanks();
                        break;
                    case 6:
                        cpu410x[0x07] = value;
                        updatePrgBanks();
                        break;
                    case 7:
                        cpu410x[0x08] = value;
                        updatePrgBanks();
                        break;
                }
                break;
            case 0xA000:
                cpu410x[0x06] = value;
                updateChrBanks();
                break;
            case 0xC000:
                cpu410x[0x01] = value & 0xFE;
                break;
            case 0xC001:
                irqReloadRequest = true;
                break;
            case 0xE000:
                cpu.setMapperIrq(false);
                irqEnabled = false;
                break;
            case 0xE001:
                irqEnabled = true;
                break;
        }
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
                irqCounter = cpu410x[0x01];
            }
            if (irqReloadRequest) {
                irqReloadRequest = false;
                irqCounter = cpu410x[0x01];
            }
            if (irqCounter == 0 && irqEnabled) {
                cpu.setMapperIrq(true);
            }
        }
        if (a12) {
            irqResetDelay = 8;
        }
    }

    @Override
    public void update() {
        if (pcmEnable) {
            pcmLatch--;
            if (pcmLatch <= 0) {
                pcmLatch += pcmClock;
                pcmSize--;
                if (pcmSize < 0) {
                    pcmIRQ = 0x80;
                    pcmEnable = false;
                    cpu.setMapperIrq(true);
                } else {
                    final int rawPcm = readMemory(pcmAddress);
                    apu.dmc.writeDirectLoad(rawPcm);
                    pcmAddress++;
                    pcmAddress &= 0x7FFF;
                }
            }
        }
    }
}