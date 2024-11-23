package nintaco.mappers.unif.bmc;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class BMC8157 extends Mapper {

    private static final long serialVersionUID = 0;

    private int command;
    private int resetData;

    public BMC8157(final CartFile cartFile) {
        super(cartFile, 4, 0);
    }

    @Override
    public void init() {
        resetData = 0xFFFD;
        updateBanks();
    }

    @Override
    public void resetting() {
        command = 0;
        resetData ^= 0x0002;
        updateBanks();
    }

    @Override
    public int readMemory(int address) {
        if (address >= 0x8000 && getBitBool(command, 8)
                && prgRomLength < 0x100000) {
            address &= resetData;
        }
        return super.readMemory(address);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        command = address;
        updateBanks();
    }

    private void updateBanks() {
        final int base = ((command & 0x060) | ((command & 0x100) >> 1)) >> 2;
        final int bank = (command & 0x01C) >> 2;
        final int lBank = getBitBool(command, 9)
                ? 7 : (getBitBool(command, 7) ? bank : 0);
        setPrgBank(2, base | bank);
        setPrgBank(3, base | lBank);
        setNametableMirroring(getBit(command, 1));
    }
}