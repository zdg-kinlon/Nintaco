package nintaco.mappers.jaleco;

// TODO AUDIO PLAYBACK CONTROL REGISTER

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.getBitBool;

public class JF17 extends Mapper {

    private static final long serialVersionUID = 0;

    protected int prgBankIndex = 2;
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
