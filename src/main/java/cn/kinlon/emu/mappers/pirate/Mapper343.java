package cn.kinlon.emu.mappers.pirate;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Mapper343 extends Mapper {

    private static final long serialVersionUID = 0;

    private int bank;

    public Mapper343(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    public void init() {
        setPrgBank(bank);
        setChrBank(bank);
    }

    @Override
    public void resetting() {
        bank ^= 1;
        init();
    }
}