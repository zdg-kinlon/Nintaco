package cn.kinlon.emu.mappers.sachen;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;
import static cn.kinlon.emu.mappers.NametableMirroring.*;

import cn.kinlon.emu.tv.TVSystem;

public abstract class Sachen8259 extends Mapper {

    private static final long serialVersionUID = 0;

    protected static final int[] NAMETABLE_MIRRORING = {
            VERTICAL,
            HORIZONTAL,
            L_SHAPED,
            ONE_SCREEN_A,
    };

    protected final int[] chrRegs = new int[4];

    protected int register;
    protected boolean simpleMode;

    public Sachen8259(final CartFile cartFile) {
        super(cartFile, 2, 4);
    }

    public Sachen8259(final CartFile cartFile, final int prgBanksSize,
                      final int chrBanksSize) {
        super(cartFile, prgBanksSize, chrBanksSize);
    }

    @Override
    public void init() {
        preferredTVSystem = TVSystem.NTSC;
    }

    @Override
    public void writeMemory(int address, int value) {
        switch (address & 0xC101) {
            case 0x4100:
                writeRegisterSelect(value);
                break;
            case 0x4101:
                writeRegisterData(value);
                break;
            default:
                memory[address] = value;
                break;
        }
    }

    protected void writeRegisterSelect(int value) {
        register = value & 7;
    }

    protected abstract void writeRegisterData(int value);

    protected void writeModeAndMirroringSelect(int value) {
        if (nametableMirroring != FOUR_SCREEN) {
            simpleMode = getBitBool(value, 0);
            if (simpleMode) {
                setNametableMirroring(VERTICAL);
            } else {
                setNametableMirroring(NAMETABLE_MIRRORING[value >> 1]);
            }
        }
    }

    protected void writeChrSelect(final int bank, final int value) {
        chrRegs[bank] = (chrRegs[bank] & 0xF8) | value;
        updateChrBanks();
    }

    protected abstract void updateChrBanks();
}
