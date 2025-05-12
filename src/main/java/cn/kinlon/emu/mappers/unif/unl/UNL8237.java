package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;



import static cn.kinlon.emu.utils.BitUtil.*;

public class UNL8237 extends MMC3 {

    private static final long serialVersionUID = 0;

    protected static final int[][] registerLUT = {
            {0, 1, 2, 3, 4, 5, 6, 7},
            {0, 2, 6, 1, 7, 3, 4, 5},
            {0, 5, 4, 1, 7, 2, 6, 3},
            {0, 6, 3, 7, 5, 2, 4, 1},
            {0, 2, 5, 3, 6, 1, 7, 4},
            {0, 1, 2, 3, 4, 5, 6, 7},
            {0, 1, 2, 3, 4, 5, 6, 7},
            {0, 1, 2, 3, 4, 5, 6, 7},
    };

    protected static final int[][] addressLUT = {
            {0, 1, 2, 3, 4, 5, 6, 7},
            {3, 2, 0, 4, 1, 5, 6, 7},
            {0, 1, 2, 3, 4, 5, 6, 7},
            {5, 0, 1, 2, 3, 7, 6, 4},
            {3, 1, 0, 5, 2, 4, 6, 7},
            {0, 1, 2, 3, 4, 5, 6, 7},
            {0, 1, 2, 3, 4, 5, 6, 7},
            {0, 1, 2, 3, 4, 5, 6, 7},
    };

    protected final int[] regs = new int[3];

    public UNL8237(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        regs[0] = 0;
        regs[1] = 3;
        regs[2] = 0;
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address >= 0x8000) {
            final int adr = addressLUT[regs[2]][((address >> 12) & 6)
                    | (address & 1)];
            address = (adr & 1) | ((adr & 6) << 12) | 0x8000;
            if (adr == 0) {
                value = (value & 0xC0) | registerLUT[regs[2]][value & 7];
            }
        } else {
            switch (address) {
                case 0x5000:
                    regs[0] = value;
                    updateBanks();
                    break;
                case 0x5001:
                    regs[1] = value;
                    updateBanks();
                    break;
                case 0x5007:
                    regs[2] = value;
                    break;
            }
        }
        super.writeMemory(address, value);
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        if (getBitBool(regs[0], 6)) {
            final int sbank = regs[1] & 0x10;
            if (getBitBool(regs[0], 7)) {
                final int b = ((regs[1] & 3) << 4) | (regs[0] & 7) | (sbank >> 1);
                if (getBitBool(regs[0], 5)) {
                    final int B = (b >> 1) << 2;
                    super.setPrgBank(4, B);
                    super.setPrgBank(5, B | 1);
                    super.setPrgBank(6, B | 2);
                    super.setPrgBank(7, B | 3);
                } else {
                    final int B = b << 1;
                    super.setPrgBank(4, B);
                    super.setPrgBank(5, B | 1);
                    super.setPrgBank(6, B);
                    super.setPrgBank(7, B | 1);
                }
            } else {
                super.setPrgBank(bank, ((regs[1] & 3) << 5) | sbank
                        | (value & 0x0F));
            }
        } else {
            if (getBitBool(regs[0], 7)) {
                final int b = ((regs[1] & 3) << 4) | (regs[0] & 0x0F);
                if (getBitBool(regs[0], 5)) {
                    final int B = (b >> 1) << 2;
                    super.setPrgBank(4, B);
                    super.setPrgBank(5, B | 1);
                    super.setPrgBank(6, B | 2);
                    super.setPrgBank(7, B | 3);
                } else {
                    final int B = b << 1;
                    super.setPrgBank(4, B);
                    super.setPrgBank(5, B | 1);
                    super.setPrgBank(6, B);
                    super.setPrgBank(7, B | 1);
                }
            } else {
                super.setPrgBank(bank, ((regs[1] & 3) << 5) | (value & 0x1F));
            }
        }
    }

    @Override
    protected void setChrBank(final int bank, int value) {
        if (getBitBool(regs[0], 6)) {
            value = ((regs[1] & 0x0C) << 6) | ((regs[1] & 0x20) << 2)
                    | (value & 0x7F);
        } else {
            value |= (regs[1] & 0x0C) << 6;
        }

        super.setChrBank(bank, value);
    }
}