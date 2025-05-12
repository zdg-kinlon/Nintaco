package cn.kinlon.emu.input.other;

import cn.kinlon.emu.Machine;
import cn.kinlon.emu.input.OtherInput;
import cn.kinlon.emu.mappers.Mapper;

public class FlipDiskSide implements OtherInput {

    private static final long serialVersionUID = 0;

    @Override
    public void run(final Machine machine) {
        final Mapper mapper = machine.getMapper();
        final int sides = mapper.getDiskSideCount();
        if (sides > 1) {
            mapper.setDiskSide(mapper.getDiskSide() ^ 1);
        }
    }
}
