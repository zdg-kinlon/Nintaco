package cn.kinlon.emu.mappers.jaleco;

// TODO AUDIO PLAYBACK CONTROL REGISTER

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class JF17 extends Mapper {

    private static final long serialVersionUID = 0;

    protected final int prgBankIndex = 2;
    protected boolean P;
    protected boolean C;

    public JF17(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        super.init();
        setPrgBank(3, -1);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        boolean p = getBitBool(value, 7);
        boolean c = getBitBool(value, 6);
        if (!P && p) {
            setPrgBank(prgBankIndex, value & 0x07);
        }
        if (!C && c) {
            setChrBank(0, value & 0x0F);
        }
        P = p;
        C = c;
    }
}
