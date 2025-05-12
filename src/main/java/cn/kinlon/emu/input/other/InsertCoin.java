package cn.kinlon.emu.input.other;

import cn.kinlon.emu.Machine;
import cn.kinlon.emu.input.OtherInput;

public class InsertCoin implements OtherInput {

    private static final long serialVersionUID = 0;

    private final int vsSystem;
    private final int coinSlot;

    public InsertCoin(final int vsSystem, final int coinSlot) {
        this.vsSystem = vsSystem;
        this.coinSlot = coinSlot;
    }

    @Override
    public void run(final Machine machine) {
        machine.getMapper().insertCoin(vsSystem, coinSlot);
    }
}
