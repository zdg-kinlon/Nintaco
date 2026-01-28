package nintaco.mappers.bitcorp;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.mappers.NametableMirroring.HORIZONTAL;
import static nintaco.mappers.NametableMirroring.VERTICAL;

public class Mapper357 extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[][] BANKS = {
            {4, 3, 5, 3, 6, 3, 7, 3},
            {1, 1, 5, 1, 4, 1, 5, 1},
    };

    private int dips;
    private int irqCounter;
    private int bankSMB2J;
    private int bankUNROM;
    private boolean irqEnabled;
    private boolean bankSwap;

    public Mapper357(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x4000, 0x6000);
    }

    @Override
    public void init() {
        bankSMB2J = 3;
        irqCounter = 0;
        bankSwap = irqEnabled = false;
        updateState();
    }

    @Override
    public void resetting() {
        dips = (dips + 0x08) & 0x18;
        init();
    }

    private void updateState() {
        if (dips == 0) {
            if (bankSwap) {
                setPrgBank(3, 0);
                setPrgBank(4, 0);
                setPrgBank(5, 0);
                setPrgBank(6, BANKS[1][bankSMB2J]);
                setPrgBank(7, 8);
            } else {
                setPrgBank(3, 2);
                setPrgBank(4, 1);
                setPrgBank(5, 0);
                setPrgBank(6, BANKS[0][bankSMB2J]);
                setPrgBank(7, 10);
            }
        } else {
            set2PrgBanks(4, (dips | bankUNROM) << 1);
            set2PrgBanks(6, (dips | 7) << 1);
        }
        setNametableMirroring((dips == 0x18) ? HORIZONTAL : VERTICAL);
    }

    @Override
    public void update() {
        if (irqEnabled && (++irqCounter & 0xFFF) == 0) {
            cpu.interrupt().setMapperIrq(true);
        }
    }

    @Override
    public void writeRegister(final int address, final int value) {
        if (address >= 0x8000) {
            bankUNROM = value & 7;
            updateState();
        }
        switch (address & 0x71FF) {
            case 0x4022:
                bankSMB2J = value & 7;
                updateState();
                break;
            case 0x4120:
                bankSwap = (value & 1) != 0;
                updateState();
                break;
        }
        if ((address & 0xF1FF) == 0x4122) {
            irqEnabled = (value & 1) != 0;
            irqCounter = 0;
            cpu.interrupt().setMapperIrq(false);
        }
    }
}
