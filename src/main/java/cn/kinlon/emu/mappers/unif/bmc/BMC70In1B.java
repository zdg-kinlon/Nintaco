package cn.kinlon.emu.mappers.unif.bmc;

import cn.kinlon.emu.files.CartFile;

public class BMC70In1B extends BMC70In1 {

    private static final long serialVersionUID = 0;

    public BMC70In1B(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    protected void initHardwareSwitch() {
        hardwareSwitch = 0x06;
    }

    @Override
    protected void updateChrBanks() {
    }

    @Override
    protected void setLargeBank(final int address) {
        largeBank = (address & 3) << 3;
    }
}
