package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class BMC70In1 extends Mapper {

    private static final long serialVersionUID = 0;

    protected int hardwareSwitch;
    protected int largeBank;
    protected int prgBank;
    protected int chrBank;
    protected int bankMode;
    protected int mirroring;

    public BMC70In1(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        initHardwareSwitch();
        setChrBank(0);
        bankMode = 0;
        largeBank = 0;
        updateBanks();
    }

    protected void initHardwareSwitch() {
        hardwareSwitch = 0x0D;
    }

    @Override
    public void resetting() {
        init();
        hardwareSwitch++;
        hardwareSwitch &= 0x0F;
    }

    private void updateBanks() {
        switch (bankMode) {
            case 0x00:
            case 0x10:
                setPrgBank(2, largeBank | prgBank);
                setPrgBank(3, largeBank | 7);
                break;
            case 0x20: {
                final int b = (largeBank | prgBank) & 0xFE;
                setPrgBank(2, b);
                setPrgBank(3, b | 1);
                break;
            }
            case 0x30:
                setPrgBank(2, largeBank | prgBank);
                setPrgBank(3, largeBank | prgBank);
                break;
        }
        setNametableMirroring(mirroring);
        updateChrBanks();
    }

    protected void updateChrBanks() {
        setChrBank(chrBank);
    }

    @Override
    public int readMemory(int address) {
        if (address >= 0x8000 && bankMode == 0x10) {
            address = (address & 0xFFF0) | hardwareSwitch;
        }
        return super.readMemory(address);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (getBitBool(address, 14)) {
            bankMode = address & 0x30;
            prgBank = address & 7;
        } else {
            mirroring = (address & 0x20) >> 5;
            setLargeBank(address);
        }
        updateBanks();
    }

    protected void setLargeBank(final int address) {
        chrBank = address & 7;
    }
}
