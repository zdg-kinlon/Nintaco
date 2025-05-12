package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;


import static cn.kinlon.emu.utils.BitUtil.*;

public class UNL8237A extends UNL8237 {

    private static final long serialVersionUID = 0;

    public UNL8237A(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        if (getBitBool(regs[0], 6)) {
            final int sbank = regs[1] & 0x10;
            if (getBitBool(regs[0], 7)) {
                final int b = ((regs[1] & 3) << 4) | ((regs[1] & 8) << 3)
                        | (regs[0] & 7) | (sbank >> 1);
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
                super.setPrgBank(bank, ((regs[1] & 3) << 5) | ((regs[1] & 8) << 4)
                        | sbank | (value & 0x0F));
            }
        } else {
            if (getBitBool(regs[0], 7)) {
                final int b = ((regs[1] & 3) << 4) | ((regs[1] & 8) << 3)
                        | (regs[0] & 0x0F);
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
                super.setPrgBank(bank, ((regs[1] & 3) << 5)
                        | ((regs[1] & 8) << 4) | (value & 0x1F));
            }
        }
    }

    @Override
    protected void setChrBank(final int bank, int value) {
        if (getBitBool(regs[0], 6)) {
            value = ((regs[1] & 0x0E) << 7) | ((regs[1] & 0x20) << 2)
                    | (value & 0x7F);
        } else {
            value |= (regs[1] & 0x0E) << 7;
        }

        super.setChrBank(bank, value);
    }
}
