package nintaco.mappers.waixing;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class Education extends Mapper {

    private static final long serialVersionUID = 0;

    private int low;
    private int low1;
    private int high;
    private int xramBank;
    private boolean setInvidiualBanks;
    private boolean useLow1;

    public Education(final CartFile cartFile) {
        super(cartFile, 4, 1);
        xram = new int[0x8000];
    }

    @Override
    public void init() {
        low = 0;
        low1 = 0;
        high = 0;
        xramBank = 0;
        setInvidiualBanks = false;
        useLow1 = false;
        updateBanks();
        setChrBank(0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xE000) == 0x6000) {
            return xram[xramBank | (address & 0x1FFF)];
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if ((address & 0xE000) == 0x6000) {
            xram[xramBank | (address & 0x1FFF)] = value;
        } else if ((address & 0xF800) == 0x4800) {
            switch (address & 3) {
                case 0:
                    setInvidiualBanks = getBitBool(value, 1);
                    useLow1 = getBitBool(value, 2);
                    setNametableMirroring(value & 1);
                    updateBanks();
                    break;
                case 1:
                    low = value & 7;
                    low1 = 0x06 | (value & 1);
                    updateBanks();
                    break;
                case 2:
                    high = value << 3;
                    updateBanks();
                    break;
                case 3:
                    xramBank = (value & 3) << 13;
                    break;
            }
        }
    }

    private void updateBanks() {
        if (setInvidiualBanks) {
            setPrgBank(2, high | low);
            if (useLow1) {
                setPrgBank(3, high | low1);
            } else {
                setPrgBank(3, high | 0x07);
            }
        } else {
            final int bank = high | low;
            if (useLow1) {
                setPrgBank(2, bank);
                setPrgBank(3, bank);
            } else {
                setPrgBanks(2, 2, bank);
            }
        }
    }
}