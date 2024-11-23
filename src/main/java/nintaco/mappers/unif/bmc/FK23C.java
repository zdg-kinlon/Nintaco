package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;
import static nintaco.mappers.NametableMirroring.*;

public class FK23C extends MMC3 {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[8];
    private final boolean[] chrRamBank = new boolean[8];

    private final int prgBonus;
    private final int prgMask;
    private final boolean FK23CA;

    private int unromCHR;
    private int dipswitch;
    private boolean bankSelectBit3;

    public FK23C(final CartFile cartFile, final boolean FK23CA) {
        super(cartFile);
        this.FK23CA = FK23CA;
        prgBonus = 0;
        prgMask = 0x7F >> prgBonus;
    }

    @Override
    public void init() {
        unromCHR = 0;
        bankSelectBit3 = false;
        regs[0] = regs[1] = regs[2] = regs[3] = 0;
        regs[4] = regs[5] = regs[6] = regs[7] = 0xFF;
        writeMirroring(VERTICAL);
        super.init();
    }

    @Override
    public void resetting() {
        dipswitch = (dipswitch + 1) & 7;
        init();
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000 && chrRamBank[address >> chrShift]
                && chrAddressMask != 0) {
            vram[(chrBanks[address >> chrShift] | (address & chrAddressMask))
                    & 0x1FFF] = value;
        } else {
            vram[address] = value;
        }
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000 && chrAddressMask != 0) {
            if (chrRamBank[address >> chrShift]) {
                return vram[(chrBanks[address >> chrShift] | (address & chrAddressMask))
                        & 0x1FFF];
            } else {
                return chrROM[(chrBanks[address >> chrShift]
                        | (address & chrAddressMask)) & chrRomSizeMask];
            }
        } else {
            return vram[address];
        }
    }

    @Override
    public void setChrBank(final int bank, final int value) {
        if (getBitBool(regs[0], 6)) {
            final int B = (regs[2] | unromCHR) << 3;
            for (int i = 7; i >= 0; i--) {
                chrRamBank[i] = false;
                super.setChrBank(i, B | i);
            }
        } else if (getBitBool(regs[0], 5)) {
            chrRamBank[bank] = true;
            super.setChrBank(bank, value);
        } else {
            final int B = (regs[2] & 0x7F) << 3;
            if (getBitBool(regs[3], 1)) {
                super.setChrBank(bank, B | value);
                chrRamBank[bank] = false;
                if (chrMode) {
                    super.setChrBank(4, B | R[0]);
                    super.setChrBank(5, B | regs[6]);
                    super.setChrBank(6, B | R[1]);
                    super.setChrBank(7, B | regs[7]);
                    chrRamBank[4] = false;
                    chrRamBank[5] = false;
                    chrRamBank[6] = false;
                    chrRamBank[7] = false;
                } else {
                    super.setChrBank(0, B | R[0]);
                    super.setChrBank(1, B | regs[6]);
                    super.setChrBank(2, B | R[1]);
                    super.setChrBank(3, B | regs[7]);
                    chrRamBank[0] = false;
                    chrRamBank[1] = false;
                    chrRamBank[2] = false;
                    chrRamBank[3] = false;
                }
            } else {
                super.setChrBank(bank, B | value);
                chrRamBank[bank] = false;
            }
        }
    }

    @Override
    public void setPrgBank(final int bank, final int value) {

        switch (regs[0] & 7) {
            case 3: {
                final int B = regs[1] << 1;
                super.setPrgBank(4, B);
                super.setPrgBank(5, B | 1);
                super.setPrgBank(6, B);
                super.setPrgBank(7, B | 1);
                break;
            }
            case 4: {
                final int B = (regs[1] & 0xFE) << 1;
                super.setPrgBank(4, B);
                super.setPrgBank(5, B | 1);
                super.setPrgBank(6, B | 2);
                super.setPrgBank(7, B | 3);
                break;
            }
            default:
                if ((regs[0] & 3) != 0) {
                    final int blocksize = (6) - (regs[0] & 3);
                    final int mask = (1 << blocksize) - 1;
                    super.setPrgBank(bank, (value & mask) | (regs[1] << 1));
                } else {
                    super.setPrgBank(bank, value & prgMask);
                }
                if (getBitBool(regs[3], 1)) {
                    super.setPrgBank(6, regs[4]);
                    super.setPrgBank(7, regs[5]);
                }
                break;
        }
    }

    @Override
    protected void writePrgRamProtect(final int value) {
        super.writePrgRamProtect(value);
        super.setPrgBank(3, value & 3);
    }

    @Override
    protected void writeBankSelect(final int value) {
        bankSelectBit3 = getBitBool(value, 3);
        super.writeBankSelect(value);
    }

    @Override
    public void writeRegister(final int address, int value) {
        if (getBitBool(regs[0], 6)) {
            if ((regs[0] & 0x30) != 0) {
                unromCHR = 0;
            } else {
                unromCHR = value & 3;
                updateChrBanks();
            }
        } else {
            if (address == 0x8001 && getBitBool(regs[3], 1) && bankSelectBit3) {
                regs[4 | (register & 3)] = value;
                updateBanks();
            } else {
                if (address < 0xC000) {
                    if (FK23CA) {
                        if ((address == 0x8000) && (value == 0x46)) {
                            value = 0x47;
                        } else if ((address == 0x8000) && (value == 0x47)) {
                            value = 0x46;
                        }
                    }
                }
                super.writeRegister(address, value);
            }
        }
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x6000) {
            return prgROM[(prgBanks[address >> prgShift] | (address & prgAddressMask))
                    & prgRomSizeMask];
        } else {
            return memory[address];
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xF000) == 0x5000) {
            if (getBitBool(address, dipswitch + 4)) {
                regs[address & 3] = value;
                if ((regs[0] & 0xF0) == 0x20 || (address & 3) == 1
                        || (address & 3) == 2) {
                    updateBanks();
                }
            }
            if (FK23CA && getBitBool(regs[3], 1)) {
                regs[0] &= ~7;
            }
        }
        super.writeMemory(address, value);
    }
}