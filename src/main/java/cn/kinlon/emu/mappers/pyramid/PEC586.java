package cn.kinlon.emu.mappers.pyramid;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.mappers.NametableMirroring.*;
import static cn.kinlon.emu.utils.BitUtil.*;

// TODO IMPLEMENT PEC KEYBOARD

public class PEC586 extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[] BankLUT = {
            0x03, 0x13, 0x23, 0x33, 0x03, 0x13, 0x23, 0x33,
            0x03, 0x13, 0x23, 0x33, 0x03, 0x13, 0x23, 0x33, // $00
            0x45, 0x67, 0x45, 0x67, 0x45, 0x67, 0x45, 0x67,
            0x45, 0x67, 0x45, 0x67, 0x45, 0x67, 0x45, 0x67, // $10
            0x03, 0x13, 0x23, 0x33, 0x03, 0x13, 0x23, 0x33,
            0x03, 0x13, 0x23, 0x33, 0x03, 0x13, 0x23, 0x33, // $20
            0x47, 0x67, 0x47, 0x67, 0x47, 0x67, 0x47, 0x67,
            0x47, 0x67, 0x47, 0x67, 0x47, 0x67, 0x47, 0x67, // $30
            0x02, 0x12, 0x22, 0x32, 0x02, 0x12, 0x22, 0x32,
            0x02, 0x12, 0x22, 0x32, 0x02, 0x12, 0x22, 0x32, // $40
            0x45, 0x67, 0x45, 0x67, 0x45, 0x67, 0x45, 0x67,
            0x45, 0x67, 0x45, 0x67, 0x45, 0x67, 0x45, 0x67, // $50
            0x02, 0x12, 0x22, 0x32, 0x02, 0x12, 0x22, 0x32,
            0x02, 0x12, 0x22, 0x32, 0x00, 0x10, 0x20, 0x30, // $60
            0x47, 0x67, 0x47, 0x67, 0x47, 0x67, 0x47, 0x67,
            0x47, 0x67, 0x47, 0x67, 0x47, 0x67, 0x47, 0x67, // $70 
    };

    private static final int[] ProtLUT = {
            0x00, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20,
            0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02,
    };

    private final boolean is512KPrgROM;

    private int mode;
    private int index;
    private int read5300;
    private int lastNTAddress;

    public PEC586(final CartFile cartFile) {
        super(cartFile, 4, 1);
        is512KPrgROM = (prgRomLength == 0x80000);
    }

    @Override
    public void init() {
        index = 0;
        mode = is512KPrgROM ? 0x00 : 0x0E;
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            if ((mode & 0x80) != 0) {
                // If the 1bpp mode is active, convert 1bpp to the normal 2bpp format.
                // Substitute A3 with NTRAM A0 (odd tile position)
                // Substitute A12 with NTRAM A9 (scanline 128+)
                return super.readVRAM((address & 0x0FF7)
                        | ((lastNTAddress & 0x0201) << 3));
            }
        } else if (address < 0x3F00 && (address & 0x03FF) < 0x03C0) {
            lastNTAddress = address;
        }
        return super.readVRAM(address);
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x8000) {
            if (is512KPrgROM && (mode & 0x10) == 0
                    && ((mode & 0x40) == 0 || address >= 0xA000)) {
                return prgROM[0x41C00 | ((address << 3) & 0x3E000)
                        | (address & 0x03FF)];
            }
        } else {
            switch (address & 0xF700) {
                case 0x5300:
                    read5300 ^= 0x04;
                    return read5300;
                case 0x5500:
                    return 0xD8 | ProtLUT[index >> 4];
            }
        }
        return super.readMemory(address);
    }

    @Override
    public void writeMemory(final int address, final int value) {
        switch (address & 0xF700) {
            case 0x5000:
                mode = value;
                updateState();
                break;
            case 0x5400:
                index = value;
                updateState();
                break;
            default:
                memory[address] = value;
                break;
        }
    }

    private void updateState() {
        if (is512KPrgROM) {
            set2PrgBanks(2, (mode & 7) << 1);
            if (getBitBool(mode, 6) && !getBitBool(mode, 4)) {
                setPrgBank(2, 0x20 | ((mode & 0x20) >> 1) | (mode & 0x0F));
            }
            setNametableMirroring((mode & 0x18) == 0x18 ? HORIZONTAL : VERTICAL);
        } else {
            final int bank = BankLUT[mode & 0x7F];
            setPrgBank(2, (bank >> 4));
            setPrgBank(3, bank & 0x0F);
            setNametableMirroring(VERTICAL);
        }
    }
}