package nintaco.mappers.fukutakeshoten;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

// TODO Tape drive not fully implemented

public class StudyBox extends Mapper {

    private static final long serialVersionUID = 0;

    private int xramBank;
    private int tapeReady;
    private int tapeReadyDelay;
    private int reg4202;

    public StudyBox(final CartFile cartFile) {
        super(cartFile, 4, 1);
        xram = new int[0x8000];
    }

    @Override
    public void init() {
        xramBank = 0;
        setPrgBank(2, 0);
        setPrgBank(3, 0);
        setChrBank(0);
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xE000) == 0x6000) {
            return xram[xramBank | (address & 0x1FFF)];
        } else if ((address & 0xFE00) == 0x4200) {
            switch (address) {
                case 0x4200:
                case 0x4203:
                    return 0x00;
                case 0x4201:
                    return 0x10;
                case 0x4202:
                    return tapeReady;
                default:
                    return 0xFF;
            }
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if ((address & 0xE000) == 0x6000) {
            xram[xramBank | (address & 0x1FFF)] = value;
        } else if ((address & 0xFE00) == 0x4200) {
            switch (address & 3) {
                case 0:
                    xramBank = (value & 0xC0) << 7;
                    break;
                case 1:
                    setPrgBank(2, value);
                    break;
                case 2:
                    reg4202 = value;
                    tapeReadyDelay = 100;
                    break;
            }
        }
    }

    @Override
    public void update() {
        if (tapeReadyDelay > 0 && --tapeReadyDelay == 0) {
            tapeReady = (reg4202 & 0x10) << 2;
        }
    }
}