package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;



import static cn.kinlon.emu.utils.BitUtil.*;

public class BMC64In1NoRepeat extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[4];

    public BMC64In1NoRepeat(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        regs[0] = 0x80;
        regs[1] = 0x43;
        regs[2] = regs[3] = 0x00;
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {
        if (getBitBool(regs[0], 7)) {
            if (getBitBool(regs[1], 7)) {
                final int b = (regs[1] & 0x1F) << 1;
                setPrgBank(2, b);
                setPrgBank(3, b | 1);
            } else {
                final int bank = ((regs[1] & 0x1f) << 1) | ((regs[1] >> 6) & 1);
                setPrgBank(2, bank);
                setPrgBank(3, bank);
            }
        } else {
            setPrgBank(3, ((regs[1] & 0x1f) << 1) | ((regs[1] >> 6) & 1));
        }
        setNametableMirroring(getBit(regs[0], 5));
        setChrBank((regs[2] << 2) | ((regs[0] >> 1) & 3));
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address >= 0x8000) {
            regs[3] = value;
            updateState();
        } else if (address >= 0x5000 && address <= 0x5003) {
            regs[address & 3] = value;
            updateState();
        }
        memory[address] = value;
    }
}