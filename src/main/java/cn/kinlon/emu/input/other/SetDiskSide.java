package cn.kinlon.emu.input.other;

import cn.kinlon.emu.Machine;
import cn.kinlon.emu.input.OtherInput;

public class SetDiskSide implements OtherInput {

    private static final long serialVersionUID = 0;

    private final int side;

    public SetDiskSide(final int side) {
        this.side = side;
    }

    @Override
    public void run(final Machine machine) {
        machine.getMapper().setDiskSide(side);
    }
}
