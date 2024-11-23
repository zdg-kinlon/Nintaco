package nintaco.mappers.bandai;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.mappers.NametableMirroring.*;
import static nintaco.util.BitUtil.getBitBool;

public class Bandai74161_7432 extends Mapper {

    private static final long serialVersionUID = 0L;

    private boolean controlledMirroring;

    public Bandai74161_7432(final CartFile cartFile,
                            final boolean controlledMirroring) {
        super(cartFile, 4, 1);
        this.controlledMirroring = controlledMirroring;
    }

    @Override
    public void init() {
        setPrgBank(2, 0);
        setPrgBank(3, -1);
        setChrBank(0);
        setNametableMirroring(VERTICAL);
    }

    @Override
    public void writeRegister(final int address, final int value) {
        final boolean mirroring = getBitBool(value, 7);
        if (mirroring) {
            controlledMirroring = true;
        }
        if (controlledMirroring) {
            setNametableMirroring(mirroring ? ONE_SCREEN_B : ONE_SCREEN_A);
        }
        setPrgBank(2, (value >> 4) & 0x07);
        setChrBank(value & 0x0F);
    }
}
