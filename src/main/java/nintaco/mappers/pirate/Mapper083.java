package nintaco.mappers.pirate;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBitBool;

public class Mapper083 extends Mapper {

    private static final long serialVersionUID = 0;

    protected final int[] regs = new int[11];
    protected final int[] exRegs = new int[4];

    protected boolean is2kBank;
    protected boolean isNot2kBank;

    protected int mode;
    protected int bank;
    protected int resetBit;

    protected int irqCounter;
    protected boolean irqEnabled;

    public Mapper083(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        updateBanks();
    }

    @Override
    public void resetting() {
        resetBit ^= 1;
        updateBanks();
    }

    @Override
    public void update() {
        if (irqEnabled) {
            irqCounter = (irqCounter - 1) & 0xFFFF;
            if (irqCounter == 0) {
                irqEnabled = false;
                irqCounter = 0xFFFF;
                cpu.setMapperIrq(true);
            }
        }
    }

    protected void updateBanks() {

        setNametableMirroring(mode & 3);

        if (is2kBank && !isNot2kBank) {
            setChrBanks(0, 2, regs[0] << 1);
            setChrBanks(2, 2, regs[1] << 1);
            setChrBanks(4, 2, regs[6] << 1);
            setChrBanks(6, 2, regs[7] << 1);
        } else {
            for (int i = 7; i >= 0; i--) {
                setChrBank(i, regs[i] | ((bank & 0x30) << 4));
            }
        }

        if (getBitBool(mode, 6)) {
            setPrgBanks(4, 2, (bank & 0x3F) << 1);
            setPrgBanks(6, 2, ((bank & 0x30) | 0x0F) << 1);
        } else {
            setPrgBank(4, regs[8]);
            setPrgBank(5, regs[9]);
            setPrgBank(6, regs[10]);
            setPrgBank(7, -1);
        }
    }

    @Override
    public int readMemory(final int address) {
        switch (address) {
            case 0x5000:
                return resetBit;
            case 0x5100:
                return exRegs[0];
            case 0x5101:
                return exRegs[1];
            case 0x5102:
                return exRegs[2];
            case 0x5103:
                return exRegs[3];
            default:
                return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        switch (address) {
            case 0x5100:
                exRegs[0] = value;
                break;
            case 0x5101:
                exRegs[1] = value;
                break;
            case 0x5102:
                exRegs[2] = value;
                break;
            case 0x5103:
                exRegs[3] = value;
                break;
            case 0x8000:
                is2kBank = true;
                bank = value;
                mode |= 0x40;
                updateBanks();
                break;
            case 0x8100:
                mode = (mode & 0x40) | value;
                updateBanks();
                break;
            case 0x8200:
                irqCounter = (irqCounter & 0xFF00) | value;
                cpu.setMapperIrq(false);
                break;
            case 0x8201:
                irqEnabled = getBitBool(mode, 7);
                irqCounter = (value << 8) | (irqCounter & 0x00FF);
                break;
            case 0xB000:
            case 0xB0FF:
            case 0xB1FF:
                bank = value;
                mode |= 0x40;
                updateBanks();
                break;
            default:
                if (address >= 0x8300 && address <= 0x8302) {
                    mode &= 0xBF;
                    regs[address - 0x82F8] = value;
                    updateBanks();
                } else if (address >= 0x8310 && address <= 0x8317) {
                    regs[address - 0x8310] = value;
                    if (address >= 0x8312 && address <= 0x8315) {
                        isNot2kBank = true;
                    }
                    updateBanks();
                }
                break;
        }
    }
}