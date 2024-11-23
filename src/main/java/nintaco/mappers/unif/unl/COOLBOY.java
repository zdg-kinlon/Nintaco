package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.nintendo.*;

import static nintaco.util.BitUtil.*;

public class COOLBOY extends MMC3 {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[4];
    private final int xramMask;

    private boolean registerWritesEnabled = true;
    private boolean weirdMode;

    public COOLBOY(final CartFile cartFile) {
        super(cartFile);
        xram = new int[0x40000];
        xramMask = xram.length - 1;
    }

    @Override
    public void init() {
        sync();
    }

    @Override
    public int readVRAM(final int address) {
        if (address < 0x2000) {
            return xram[(chrBanks[address >> chrShift] | (address & chrAddressMask))
                    & xramMask];
        } else {
            return vram[address];
        }
    }

    @Override
    public void writeVRAM(final int address, final int value) {
        if (address < 0x2000) {
            xram[(chrBanks[address >> chrShift] | (address & chrAddressMask))
                    & xramMask] = value;
        } else {
            vram[address] = value;
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (registerWritesEnabled && (0xE000 & address) == 0x6000) {
            regs[address & 3] = value;
            sync();
        }
        super.writeMemory(address, value);
    }

    @Override
    protected void updatePrgBanks() {
        if (!prgMode && weirdMode) {
            setPrgBank(4, R[6]);
            setPrgBank(5, R[7]);
            setPrgBank(6, 0);
            setPrgBank(7, 0);
        } else {
            super.updatePrgBanks();
        }
    }

    @Override
    protected void updateChrBanks() {
        if (weirdMode) {
            if (chrMode) {
                setChrBank(0, R[2]);
                setChrBank(1, R[3]);
                setChrBank(2, R[4]);
                setChrBank(3, R[5]);
                setChrBank(4, R[0]);
                setChrBank(5, 0);
                setChrBank(6, R[1]);
                setChrBank(7, 0);
            } else {
                setChrBank(0, R[0]);
                setChrBank(1, 0);
                setChrBank(2, R[1]);
                setChrBank(3, 0);
                setChrBank(4, R[2]);
                setChrBank(5, R[3]);
                setChrBank(6, R[4]);
                setChrBank(7, R[5]);
            }
        } else {
            super.updateChrBanks();
        }
    }

    private void sync() {

        weirdMode = getBitBool(regs[3], 6);

        final int CCKKJEEE = (((regs[0] & 0x30) | (regs[1] & 0x0C)) << 2)
                | ((regs[1] & 0x10) >> 1) | (regs[0] & 0x07);

        if (getBitBool(regs[3], 4)) {
            final int CCKKJEEEQQR = (CCKKJEEE << 3) | ((regs[3] & 0x0E) >> 1);
            if (getBitBool(regs[1], 1)) {
                prgBlockOffset = (CCKKJEEEQQR & -2) << 1;
                prgBlockMask = 0x03;
            } else {
                prgBlockOffset = CCKKJEEEQQR << 1;
                prgBlockMask = 0x01;
            }
            if (getBitBool(regs[0], 7)) {
                chrBlockOffset = moveBit(regs[0], 3, 7) | ((regs[2] & 0x0F) << 3);
                chrBlockMask = 0x07;
            } else {
                chrBlockOffset = (regs[2] & 0x0F) << 3;
                chrBlockMask = 0x87;
            }
            registerWritesEnabled = true;
        } else {
            final int IHGB = moveBit(regs[1], 5, 3) | moveBit(regs[1], 6, 2)
                    | moveBit(regs[1], 7, 1, true) | moveBit(regs[0], 6, 0, true);
            prgBlockOffset = (CCKKJEEE & ~IHGB) << 4;
            prgBlockMask = (IHGB << 4) | 0x0F;
            if (getBitBool(regs[0], 7)) {
                chrBlockOffset = moveBit(regs[0], 3, 7);
                chrBlockMask = 0x7F;
            } else {
                chrBlockOffset = 0x00;
                chrBlockMask = 0xFF;
            }
            registerWritesEnabled = !getBitBool(regs[3], 7);
        }

        updateBanks();
    }
}