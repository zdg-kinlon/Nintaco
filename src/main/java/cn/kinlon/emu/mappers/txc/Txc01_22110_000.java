package cn.kinlon.emu.mappers.txc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC1;



import static cn.kinlon.emu.mappers.NametableMirroring.*;
import static cn.kinlon.emu.utils.BitUtil.*;

public class Txc01_22110_000 extends MMC1 {

    private static final long serialVersionUID = 0;

    private int mode;
    private int latch;

    public Txc01_22110_000(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        mode = latch = 0;
        setNametableMirroring(VERTICAL);
        updateState();
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {
        if (getBitBool(mode, 0)) {
            updateBanks();
        } else {
            final int upper = (mode & 2) << 1;
            setPrgBank(2, upper | ((latch >> 4) & 3));
            setPrgBank(3, upper | 3);
            set2ChrBanks(0, latch & 0x0E);
        }
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        if (getBitBool(mode, 0)) {
            super.setPrgBank(bank, 0x08 | (value & 0x07));
        } else {
            super.setPrgBank(bank, value);
        }
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        if (getBitBool(mode, 0)) {
            super.setChrBank(bank, 0x20 | (value & 0x1F));
        } else {
            super.setChrBank(bank, value);
        }
    }

    private void writeMode(final int value) {
        mode = value;
        updateState();
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if ((address & 0xE100) == 0x4100) {
            writeMode(value);
        } else if (getBitBool(mode, 0)) {
            super.writeMemory(address, value);
        } else if (address >= 0x8000) {
            latch = value;
            updateState();
        } else {
            memory[address] = value;
        }
    }

    @Override
    protected void writeControl(final int value) {
        this.controlRegister = value;
        setNametableMirroring(VERTICAL);
        prgBankMode = (value >> 2) & 3;
        chrBankMode = getBitBool(value, 4);
        updateBanks();
    }
}