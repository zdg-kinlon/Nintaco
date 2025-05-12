package cn.kinlon.emu.mappers.piratemmc3;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

import static java.util.Arrays.*;
import static cn.kinlon.emu.utils.BitUtil.*;

public class BMCHPxx extends MMC3 {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[4];

    private int dipSwitches;
    private int unromChr;
    private boolean locked;

    public BMCHPxx(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        fill(regs, 0);
        unromChr = 0;
        locked = false;
        super.init();
    }

    @Override
    public void resetting() {
        init();
        dipSwitches = (dipSwitches + 1) & 0x0F;
    }

    @Override
    protected void setChrBank(final int bank, int value) {
        if (getBitBool(regs[0], 2)) {
            switch (regs[0] & 3) {
                case 2:
                    value = (regs[2] & 0x3E) | (unromChr & 0x01);
                    break;
                case 3:
                    value = (regs[2] & 0x3C) | (unromChr & 0x03);
                    break;
                default:
                    value = regs[2] & 0x3F;
                    break;
            }
            value <<= 3;
            for (int i = 7; i >= 0; --i) {
                super.setChrBank(i, value | i);
            }
        } else if (getBitBool(regs[0], 0)) {
            super.setChrBank(bank, ((regs[2] & 0x30) << 3) | (value & 0x7F));
        } else {
            super.setChrBank(bank, ((regs[2] & 0x20) << 3) | (value & 0xFF));
        }
    }

    @Override
    protected void setPrgBank(final int bank, int value) {
        if (getBitBool(regs[0], 2)) {
            if ((regs[0] & 0x0F) == 0x04) {
                value = (regs[1] & 0x1F) << 1;
                super.setPrgBank(4, value);
                super.setPrgBank(5, value | 1);
                super.setPrgBank(6, value);
                super.setPrgBank(7, value | 1);
            } else {
                value = (regs[1] & 0x1E) << 1;
                super.setPrgBank(4, value);
                super.setPrgBank(5, value | 1);
                super.setPrgBank(6, value | 2);
                super.setPrgBank(7, value | 3);
            }
        } else if (getBitBool(regs[0], 1)) {
            super.setPrgBank(bank, (value & 0x0F) | ((regs[1] & 0x18) << 1));
        } else {
            super.setPrgBank(bank, (value & 0x1F) | ((regs[1] & 0x10) << 1));
        }
    }

    @Override
    protected void writeMirroring(final int value) {
        if (getBitBool(regs[0], 2)) {
            setNametableMirroring(getBit(unromChr, 2));
        } else {
            super.writeMirroring(value & 1);
        }
    }

    @Override
    public int readMemory(final int address) {
        return ((address & 0xF000) == 0x5000) ? dipSwitches
                : super.readMemory(address);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        super.writeMemory(address, value);
        if ((address & 0xF000) == 0x5000) {
            if (!locked) {
                regs[address & 0x03] = value;
                locked = getBitBool(value, 7);
                updateBanks();
            }
        } else if (address >= 0x8000) {
            if (getBitBool(regs[0], 2)) {
                unromChr = value;
                updateChrBanks();
            }
        }
    }
}