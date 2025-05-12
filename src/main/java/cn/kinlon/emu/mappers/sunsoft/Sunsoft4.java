package cn.kinlon.emu.mappers.sunsoft;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class Sunsoft4 extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int LICENSING_TIMER_RESET = 107520;

    private final int[] nametableBanks = new int[2];

    private int licensingTimer;
    private boolean licensingEnabled;
    private boolean wramEnabled;
    private boolean internalRomEnabled;
    private boolean chrRomNametablesEnabled;

    public Sunsoft4(final CartFile cartFile) {
        super(cartFile, 4, 4);
    }

    @Override
    public void init() {
        setPrgBank(3, 7);
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            return chrROM[(chrBanks[address >> 11] | (address & 0x07FF))
                    & chrRomSizeMask];
        } else if (chrRomNametablesEnabled && address < 0x2800) {
            return chrROM[(nametableBanks[address < 0x2400 ? 0 : 1]
                    | (address & 0x03FF)) & chrRomSizeMask];
        } else {
            return vram[address];
        }
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x8000) {
            if (address >= 0xC000) {
                return prgROM[(prgBanks[3] | (address & 0x3FFF)) & prgRomSizeMask];
            } else if (internalRomEnabled || !licensingEnabled
                    || licensingTimer > 0) {
                return prgROM[(prgBanks[2] | (address & 0x3FFF)) & prgRomSizeMask];
            } else {
                return 0;
            }
        } else if (address >= 0x6000) {
            return wramEnabled ? memory[address] : 0;
        } else {
            return memory[address];
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address >= 0x6000) {
            switch (address & 0xF000) {
                case 0x6000:
                case 0x7000:
                    writeLicensingIC(address, value);
                    break;
                case 0x8000:
                    setChrBank(0, value);
                    break;
                case 0x9000:
                    setChrBank(1, value);
                    break;
                case 0xA000:
                    setChrBank(2, value);
                    break;
                case 0xB000:
                    setChrBank(3, value);
                    break;
                case 0xC000:
                    writeNametableBank(0, value);
                    break;
                case 0xD000:
                    writeNametableBank(1, value);
                    break;
                case 0xE000:
                    writeNametableMirroring(value);
                    break;
                case 0xF000:
                    writePrgBank(value);
                    break;
            }
        } else {
            memory[address] = value;
        }
    }

    private void writeNametableMirroring(final int value) {
        setNametableMirroring(value & 3);
        chrRomNametablesEnabled = getBitBool(value, 4);
    }

    private void writePrgBank(final int value) {
        setPrgBank(2, value & 7);
        internalRomEnabled = getBitBool(value, 3);
        wramEnabled = getBitBool(value, 4);
    }

    private void writeNametableBank(final int bank, final int value) {
        nametableBanks[bank] = (0x80 | value) << 10;
    }

    private void writeLicensingIC(final int address, final int value) {
        if (wramEnabled) {
            memory[address] = value;
        } else {
            licensingTimer = LICENSING_TIMER_RESET;
            licensingEnabled = true;
        }
    }

    @Override
    public void update() {
        if (licensingTimer > 0) {
            licensingTimer--;
        }
    }
}