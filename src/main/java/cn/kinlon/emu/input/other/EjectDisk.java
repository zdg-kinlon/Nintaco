package cn.kinlon.emu.input.other;

import cn.kinlon.emu.Machine;
import cn.kinlon.emu.input.OtherInput;

public class EjectDisk implements OtherInput {

    private static final long serialVersionUID = 0;

    @Override
    public void run(final Machine machine) {
        machine.getMapper().ejectDisk();
    }
}
