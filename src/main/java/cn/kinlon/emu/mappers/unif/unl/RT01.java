package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



public class RT01 extends Mapper {

    private static final long serialVersionUID = 0;

    private int random; // LFSR borrowed from Pitfall! for 8-bit PRNG

    public RT01(final CartFile cartFile) {
        super(cartFile, 4, 4);
    }

    @Override
    public void init() {
        random = 0xC4;
        set2PrgBanks(2, 0);
        set4ChrBanks(0, 0);
    }

    private int nextInt() {
        return (random = 0xFF & ((random << 1) | ((1 & (random >> 3))
                ^ (1 & (random >> 4)) ^ (1 & (random >> 5))
                ^ (1 & (random >> 7)))));
    }

    @Override
    public int readMemory(final int address) {
        return (((address >= 0xCE80) && (address < 0xCF00))
                || ((address >= 0xFE80) && (address < 0xFF00)))
                ? (0xF2 | (nextInt() & 0x0D)) : super.readMemory(address);
    }
}