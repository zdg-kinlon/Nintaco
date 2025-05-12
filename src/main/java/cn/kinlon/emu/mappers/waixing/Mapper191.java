package cn.kinlon.emu.mappers.waixing;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



import static cn.kinlon.emu.utils.BitUtil.*;

public class Mapper191 extends MMC3 {

    private static final long serialVersionUID = 0;

    protected final int bit;
    protected final int vramMask;

    public Mapper191(final CartFile cartFile) {
        this(cartFile, 17, 0x07FF);
    }

    public Mapper191(final CartFile cartFile, final int bit, final int vramMask) {
        super(cartFile);
        this.bit = bit;
        this.vramMask = vramMask;
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000) {
            final int addr = chrBanks[address >> 10] | (address & 0x03FF);
            if (getBitBool(addr, bit)) {
                vram[addr & vramMask] = value;
            } else {
                super.writeVRAM(address, value);
            }
        } else {
            super.writeVRAM(address, value);
        }
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            final int addr = chrBanks[address >> 10] | (address & 0x03FF);
            if (getBitBool(addr, bit)) {
                return vram[addr & vramMask];
            } else {
                return super.readVRAM(address);
            }
        } else {
            return super.readVRAM(address);
        }
    }
}
