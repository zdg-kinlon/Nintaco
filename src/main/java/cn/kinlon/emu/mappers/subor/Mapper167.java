package cn.kinlon.emu.mappers.subor;

import cn.kinlon.emu.files.CartFile;


public class Mapper167 extends Mapper166 {

    private static final long serialVersionUID = 0;

    public Mapper167(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void updateUNROM(final int bank) {
        setPrgBank(2, bank);
        setPrgBank(3, 0x20);
    }

    @Override
    protected void updateNROM(final int bank) {
        setPrgBank(2, bank | 0x01);
        setPrgBank(3, bank & 0xFE);
    }
}
