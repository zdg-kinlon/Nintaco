package cn.kinlon.emu.mappers.irem;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.mappers.NametableMirroring.ONE_SCREEN_A;
import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class G101 extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] prgRegs = new int[2];

    private final boolean G101B;

    private boolean prgMode;

    public G101(final CartFile cartFile) {
        super(cartFile, 8, 8);
        if (cartFile.getFileCRC() == 0x243A8735 // Major League
                || cartFile.getSubmapperNumber() == 1) {
            G101B = true;
            setNametableMirroring(ONE_SCREEN_A);
        } else {
            G101B = false;
        }
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF000) {
            case 0x8000:
                writePrgRegister(0, value);
                break;
            case 0x9000:
                writePrgMode(value);
                break;
            case 0xA000:
                writePrgRegister(1, value);
                break;
            case 0xB000:
                setChrBank(address & 7, value);
                break;
        }
    }

    private void writePrgRegister(final int register, final int value) {
        prgRegs[register] = value & 0x1F;
        updatePrgBanks();
    }

    private void writePrgMode(final int value) {
        prgMode = !G101B && getBitBool(value, 1);
        setNametableMirroring(getBit(value, 0));
        updatePrgBanks();
    }

    private void updatePrgBanks() {
        if (prgMode) {
            setPrgBank(4, -2);
            setPrgBank(6, prgRegs[0]);
        } else {
            setPrgBank(4, prgRegs[0]);
            setPrgBank(6, -2);
        }
        setPrgBank(5, prgRegs[1]);
        setPrgBank(7, -1);
    }
}