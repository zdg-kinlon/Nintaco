package nintaco.mappers.nintendo;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.mappers.NametableMirroring.*;
import static nintaco.util.BitUtil.getBitBool;

public class MMC1 extends Mapper {

    protected static final int[] MIRRORING
            = {ONE_SCREEN_A, ONE_SCREEN_B, VERTICAL, HORIZONTAL};
    private static final long serialVersionUID = 0;
    protected final int[] chrBankRegs = new int[2];

    protected final boolean largePrgROM;
    protected final boolean prgBanking;

    protected long lastCycleCount;
    protected int lastUpdatedChrBankReg;
    protected int shiftRegister = 0x10;
    protected int controlRegister;
    protected int prgBankMode;
    protected int prgBankReg;
    protected boolean chrBankMode;
    protected boolean prgRamEnabled;

    public MMC1(final CartFile cartFile) {
        super(cartFile, 4, 2);
        largePrgROM = cartFile.getPrgRomLength() == 0x80000;
        prgBanking = cartFile.getSubmapperNumber() != 5;
    }

    @Override
    public void init() {
        prgRamEnabled = true;
        writeControl(0x0C);
        writeChrBankReg(0, 0);
        writeChrBankReg(1, 0);
        setPrgBank(2, 0);
        setPrgBank(3, prgBanking ? -1 : 1);
        updateBanks();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xE000) != 0x6000 || prgRamEnabled) {
            super.writeMemory(address, value);
        }
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xE000) != 0x6000 || prgRamEnabled) {
            return super.readMemory(address);
        } else {
            return 0;
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {

        final long cycleCount = cpu.getCycleCounter();
        if (cycleCount - lastCycleCount >= 2) {
            if (getBitBool(value, 7)) {
                shiftRegister = 0x10;
                writeControl(controlRegister | 0x0C);
            } else {
                final int register = address & 0xE000;
                final boolean write = getBitBool(shiftRegister, 0);
                shiftRegister = (shiftRegister >> 1) | ((value & 1) << 4);
                if (write) {
                    switch (register) {
                        case 0x8000:
                            writeControl(shiftRegister);
                            break;
                        case 0xA000:
                            writeChrBankReg(0, shiftRegister);
                            break;
                        case 0xC000:
                            writeChrBankReg(1, shiftRegister);
                            break;
                        case 0xE000:
                            writePrgBankReg(shiftRegister);
                            break;
                    }
                    shiftRegister = 0x10;
                }
            }
        }
        lastCycleCount = cycleCount;
    }

    protected void writeControl(final int value) {
        this.controlRegister = value;
        setNametableMirroring(MIRRORING[value & 3]);
        prgBankMode = (value >> 2) & 3;
        chrBankMode = getBitBool(value, 4);
        updateBanks();
    }

    protected void writeChrBankReg(final int bank, final int value) {
        chrBankRegs[bank] = value & 0x1F;
        lastUpdatedChrBankReg = bank;
        updateBanks();
    }

    protected void writePrgBankReg(final int value) {
        prgBankReg = value & 0x0F;
        prgRamEnabled = !getBitBool(value, 4);
        updateBanks();
    }

    protected void updateBanks() {

        if (prgBanking) {
            final int prgBlock = largePrgROM ? (chrBankRegs[(chrBankMode
                    && lastUpdatedChrBankReg == 1) ? 1 : 0] & 0x10) : 0;
            switch (prgBankMode) {
                case 0:
                case 1: {
                    final int bank = prgBlock | (prgBankReg & 0xFE);
                    setPrgBank(2, bank);
                    setPrgBank(3, bank | 1);
                    break;
                }
                case 2:
                    setPrgBank(2, prgBlock);
                    setPrgBank(3, prgBlock | prgBankReg);
                    break;
                case 3:
                    setPrgBank(2, prgBlock | prgBankReg);
                    setPrgBank(3, prgBlock | 0x0F);
                    break;
            }
        }

        if (chrBankMode) {
            setChrBank(0, chrBankRegs[0]);
            setChrBank(1, chrBankRegs[1]);
        } else {
            setChrBank(0, chrBankRegs[0] & 0x1E);
            setChrBank(1, (chrBankRegs[0] & 0x1E) | 0x01);
        }
    }
}