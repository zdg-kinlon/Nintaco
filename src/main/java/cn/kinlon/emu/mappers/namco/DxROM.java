package cn.kinlon.emu.mappers.namco;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.mappers.nintendo.vs.VsHardware.*;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class DxROM extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[][] VS_SECURITY_DATA = {
            {0xFF, 0xBF, 0xB7, 0x97, 0x97, 0x17, 0x57, 0x4F,
                    0x6F, 0x6B, 0xEB, 0xA9, 0xB1, 0x90, 0x94, 0x14,
                    0x56, 0x4E, 0x6F, 0x6B, 0xEB, 0xA9, 0xB1, 0x90,
                    0xD4, 0x5C, 0x3E, 0x26, 0x87, 0x83, 0x13, 0x00},
            {0x00, 0x00, 0x00, 0x00, 0xB4, 0x00, 0x00, 0x00,
                    0x00, 0x6F, 0x00, 0x00, 0x00, 0x00, 0x94, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
    };
    protected int register;
    private int vsSecurityTable = -1;
    private int vsSecurityIndex;
    private final boolean vsSecurityEnabled;
    private boolean vsSuperXevious;
    private boolean xeviousSelect;

    public DxROM(final CartFile cartFile) {
        super(cartFile, 8, 8);

        switch (cartFile.getVsHardware()) {
            case VS_UNISYSTEM_TKO_BOXING:
                vsSecurityTable = 0;
                break;
            case VS_UNISYSTEM_RBI_BASEBALL:
                vsSecurityTable = 1;
                break;
            case VS_UNISYSTEM_SUPER_XEVIOUS:
                vsSuperXevious = true;
                break;
        }
        vsSecurityEnabled = vsSecurityTable >= 0;

        setPrgBank(4, 0);
        setPrgBank(5, 1);
        setPrgBank(6, -2);
        setPrgBank(7, -1);
    }

    @Override
    public int readMemory(final int address) {
        if (vsSecurityEnabled && (address & 0xFFFE) == 0x5E00) {
            if (getBitBool(address, 0)) {
                return VS_SECURITY_DATA[vsSecurityTable][vsSecurityIndex++ & 0x1F];
            } else {
                vsSecurityIndex = 0;
                return 0x5E;
            }
        } else if (vsSuperXevious && (address & 0xFC00) == 0x5400) {
            switch (address) {
                case 0x54FF:
                    return 0x05;
                case 0x5678:
                    return xeviousSelect ? 0x00 : 0x01;
                case 0x578F:
                    return xeviousSelect ? 0xD1 : 0x89;
                case 0x5567:
                    xeviousSelect ^= true;
                    return xeviousSelect ? 0x37 : 0x3E;
                default:
                    return 0x00;
            }
        }
        return super.readMemory(address);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (getBitBool(address, 0)) {
            writeRegisterValue(value);
        } else {
            writeRegisterIndex(value);
        }
    }

    protected void writeRegisterIndex(final int value) {
        register = value & 7;
    }

    protected void writeRegisterValue(final int value) {
        switch (register) {
            case 0:
                writeChrBank2K(0, value);
                break;
            case 1:
                writeChrBank2K(2, value);
                break;
            case 2:
                writeChrBank(4, value);
                break;
            case 3:
                writeChrBank(5, value);
                break;
            case 4:
                writeChrBank(6, value);
                break;
            case 5:
                writeChrBank(7, value);
                break;
            case 6:
                writePrgBank(4, value);
                break;
            case 7:
                writePrgBank(5, value);
                break;
        }
    }

    protected void writePrgBank(final int bank, final int value) {
        setPrgBank(bank, value & 0x0F);
    }

    protected void writeChrBank(final int bank, final int value) {
        setChrBank(bank, value & 0x3F);
    }

    protected void writeChrBank2K(final int bank, int value) {
        value &= 0x3E;
        setChrBank(bank, value);
        setChrBank(bank + 1, value | 1);
    }

    @Override
    public int readVRAM(int address) {
        if (address < 0x2000) {
            return readChr(address);
        } else {
            return vram[address];
        }
    }

    protected int readChr(int address) {
        return chrROM[(chrBanks[address >> 10] | (address & 0x03FF))
                & chrRomSizeMask];
    }
}

