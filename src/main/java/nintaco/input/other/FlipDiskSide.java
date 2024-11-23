package nintaco.input.other;

import nintaco.Machine;
import nintaco.input.OtherInput;
import nintaco.mappers.Mapper;

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
