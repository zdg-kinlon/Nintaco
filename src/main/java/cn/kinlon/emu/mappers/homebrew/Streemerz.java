package cn.kinlon.emu.mappers.homebrew;

import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.mappers.NametableMirroring.*;
import static cn.kinlon.emu.utils.BitUtil.getBit;

public class Streemerz extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[] bankSizeMasks = new int[4];

    static {
        for (int i = 0; i < bankSizeMasks.length; i++) {
            bankSizeMasks[i] = (2 << i) - 1;
        }
    }

    private int register;
    private int mirroring;
    private int bankMode;
    private int innerPrg = 0x0F;
    private int outerPrg = 0x3F;

    public Streemerz(NesFile nesFile) {
        super(nesFile, 4, 1);
        xram = new int[0x8000];
        updatePrgBanks();
    }

    @Override
    public int readVRAM(int address) {
        if (address < 0x2000) {
            return xram[chrBanks[0] | address];
        } else {
            return vram[address];
        }
    }

    @Override
    public void writeVRAM(int address, int value) {
        if (address < 0x2000) {
            xram[chrBanks[0] | address] = value;
        } else {
            vram[address] = value;
        }
    }

    @Override
    public void writeMemory(int address, int value) {
        if ((address & 0xF000) == 0x5000) {
            writeRegisterIndex(value);
        } else if (address >= 0x8000) {
            writeRegisterValue(value);
        } else {
            memory[address] = value;
        }
    }

    private void writeRegisterIndex(int value) {
        register = value & 0x81;
    }

    private void writeRegisterValue(int value) {
        value &= 0x3F;
        switch (register) {
            case 0x00:
                writeChrRegister(value);
                break;
            case 0x01:
                writePrgRegister(value);
                break;
            case 0x80:
                writeMode(value);
                break;
            case 0x81:
                writeOuterPrgRegister(value);
                break;
        }
    }

    private void writeChrRegister(int value) {
        setChrBank(value & 0x03);
        overrideNametableMirroring(value);
    }

    private void writePrgRegister(int value) {
        innerPrg = value & 0x0F;
        overrideNametableMirroring(value);
        updatePrgBanks();
    }

    private void writeMode(int value) {
        bankMode = (value >> 2) & 0x0F;
        setNametableMirroring(value);
        updatePrgBanks();
    }

    private void writeOuterPrgRegister(int value) {
        outerPrg = (value & 0x3F) << 1;
        updatePrgBanks();
    }

    private void updatePrgBanks() {
        updatePrgBank(2, bankMode, outerPrg, innerPrg);
        updatePrgBank(3, bankMode, outerPrg, innerPrg);
    }

    private void updatePrgBank(int bank, int bankMode, int outerBank,
                               int innerBank) {
        int cpuA14 = bank - 2;
        if (((bankMode ^ cpuA14) & 0x03) == 0x02) {
            bankMode = 0;
        }
        if ((bankMode & 0x02) == 0) {
            innerBank = (innerBank << 1) | cpuA14;
        }
        setPrgBank(bank, ((innerBank ^ outerBank)
                & bankSizeMasks[(bankMode >> 2) & 3]) ^ outerBank);
    }

    private void overrideNametableMirroring(int value) {
        if (mirroring < 2) {
            setNametableMirroring(getBit(value, 4));
        }
    }

    @Override
    public void setNametableMirroring(int nametableMirroring) {
        mirroring = nametableMirroring & 0x03;
        switch (mirroring) {
            case 0:
                super.setNametableMirroring(ONE_SCREEN_A);
                break;
            case 1:
                super.setNametableMirroring(ONE_SCREEN_B);
                break;
            case 2:
                super.setNametableMirroring(VERTICAL);
                break;
            case 3:
                super.setNametableMirroring(HORIZONTAL);
                break;
        }
    }
}
