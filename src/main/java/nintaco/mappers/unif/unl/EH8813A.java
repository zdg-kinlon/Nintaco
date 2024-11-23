package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

// TODO DIP SWITCHES

public class EH8813A extends Mapper {

    private static final long serialVersionUID = 0;

    private boolean locked;
    private boolean dipMode;
    private int chr;
    private int dip;

    public EH8813A(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        locked = false;
        chr = 0;
        super.init();
    }

    @Override
    public void resetting() {
        dip = (dip + 1) & 0xF;
        init();
    }

    @Override
    public int readMemory(int address) {
        if (dipMode && (address & 0x8040) == 0x8040) {
            address = (address & 0xFFF0) | dip;
        } else if ((address & 0xF000) == 0x5000) {
            if ((address & 0x0800) == 0) {
                return 0xFF;
            } else {
                address &= 0xF003;
            }
        }
        return super.readMemory(address);
    }

    @Override
    public void writeMemory(int address, int value) {
        if ((address & 0xF000) == 0x5000) {
            address &= 0xF003;
            value &= 0x0F;
        }
        super.writeMemory(address, value);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (!locked) {
            locked = getBitBool(address, 8);
            dipMode = getBitBool(address, 6);
            if (getBitBool(address, 7)) {
                setPrgBank(2, address & 0x003F);
                setPrgBank(3, address & 0x003F);
            } else {
                set2PrgBanks(2, address & 0x003E);
            }
            setNametableMirroring(getBit(value, 7));
            chr = value & 0x7C;
        }
        setChrBank(chr | (value & 0x03));
    }
}