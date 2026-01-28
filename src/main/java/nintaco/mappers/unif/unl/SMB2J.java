package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class SMB2J extends Mapper {

    private static final long serialVersionUID = 0;

    private int irqCounter;
    private boolean irqEnabled;

    public SMB2J(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        irqCounter = 0;
        irqEnabled = false;
        writeBankSelect(0);
        set4PrgBanks(4, 0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public void update() {
        if (irqEnabled) {
            if (irqCounter < 5750) {
                ++irqCounter;
            } else {
                irqEnabled = false;
                cpu.interrupt().setMapperIrq(true);
            }
        }
    }

    private void writeBankSelect(final int value) {
        setPrgBank(3, 4 | (value & 1));
    }

    private void writeIrqControl(final int value) {
        irqEnabled = getBitBool(value, 0);
        irqCounter = 0;
        cpu.interrupt().setMapperIrq(false);
    }

    @Override
    public int readMemory(final int address) {
        return (address >= 0x4042 && address <= 0x4055) ? 0xFF
                : super.readMemory(address);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        super.writeMemory(address, value);
        switch (address) {
            case 0x4027:
                writeBankSelect(value);
                break;
            case 0x4068:
                writeIrqControl(value);
                break;
        }
    }
}