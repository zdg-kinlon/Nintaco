package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

import static cn.kinlon.emu.utils.BitUtil.getBit;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Mapper226 extends Mapper {

    private static final long serialVersionUID = 0;

    protected final int[] registers = new int[2];

    public Mapper226(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        registers[0] = 0;
        registers[1] = 0;
        setPrgBank(2, 0);
        setPrgBank(3, 1);
        setChrBank(0);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0x8001) {
            case 0x8000:
                registers[0] = value;
                break;
            case 0x8001:
                registers[1] = value;
                break;
        }

        updatePrg();
        setNametableMirroring(getBit(registers[0], 6) ^ 1);
    }

    protected int getPrgPage() {
        return (registers[0] & 0x1F) | ((registers[0] & 0x80) >> 2)
                | ((registers[1] & 0x01) << 6);
    }

    protected void updatePrg() {
        final int prgPage = getPrgPage();
        if (getBitBool(registers[0], 5)) {
            setPrgBank(2, prgPage);
            setPrgBank(3, prgPage);
        } else {
            setPrgBank(2, prgPage & 0xFE);
            setPrgBank(3, (prgPage & 0xFE) + 1);
        }
    }
}
