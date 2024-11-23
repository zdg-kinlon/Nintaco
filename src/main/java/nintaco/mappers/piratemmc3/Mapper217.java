package nintaco.mappers.piratemmc3;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class Mapper217 extends MMC3 {

    private static final long serialVersionUID = 0;

    private static final int[] LUT = {0, 6, 3, 7, 5, 2, 4, 1};

    private final int[] regs = new int[4];

    public Mapper217(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        regs[0] = 0;
        regs[1] = 0xFF;
        regs[2] = 0x03;
        regs[3] = 0;
        updateBanks();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void setChrBank(final int bank, int value) {
        if (!getBitBool(regs[1], 3)) {
            value = ((regs[1] << 3) & 0x80) | (value & 0x7F);
        }
        super.setChrBank(bank, ((regs[1] << 8) & 0x0300) | value);
    }

    @Override
    protected void setPrgBank(final int bank, int value) {
        if (getBitBool(regs[1], 3)) {
            value &= 0x1F;
        } else {
            value = (value & 0x0F) | (regs[1] & 0x10);
        }
        super.setPrgBank(bank, ((regs[1] << 5) & 0x60) | value);
    }

    @Override
    public void writeMemory(final int address, int value) {
        memory[address] = value;
        if (address < 0x8000) {
            switch (address) {
                case 0x5000:
                    regs[0] = value;
                    if (getBitBool(value, 7)) {
                        value = (value & 0x0F) | ((regs[1] << 4) & 0x30);
                        value <<= 1;
                        setPrgBank(4, value);
                        setPrgBank(5, value + 1);
                        setPrgBank(6, value);
                        setPrgBank(7, value + 1);
                    } else {
                        updatePrgBanks();
                    }
                    break;

                case 0x5001:
                    if (regs[1] != value) {
                        regs[1] = value;
                        updatePrgBanks();
                    }
                    break;

                case 0x5007:
                    regs[2] = value;
                    break;
            }
        } else {
            switch (address & 0xE001) {
                case 0x8000:
                    if (regs[2] != 0) {
                        writeIrqLatch(value);
                    } else {
                        writeBankSelect(value);
                    }
                    break;

                case 0x8001:
                    if (regs[2] != 0) {
                        regs[3] = 1;
                        writeBankSelect((value & 0xC0) | LUT[value & 0x07]);
                    } else {
                        writeBankData(value);
                    }
                    break;

                case 0xA000:
                    if (regs[2] != 0) {
                        if (regs[3] != 0 && (!getBitBool(regs[0], 7) || register < 6)) {
                            regs[3] = 0;
                            writeBankData(value);
                        }
                    } else {
                        setNametableMirroring(value & 1);
                    }
                    break;

                case 0xA001:
                    if (regs[2] != 0) {
                        setNametableMirroring(value & 1);
                    } else {
                        writePrgRamProtect(value);
                    }
                    break;

                default:
                    super.writeRegister(address, value);
                    break;
            }
        }
    }
}
