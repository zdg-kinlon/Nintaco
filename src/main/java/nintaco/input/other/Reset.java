package nintaco.input.other;

import nintaco.Machine;
import nintaco.input.OtherInput;

public class Reset implements OtherInput {

    private static final long serialVersionUID = 0;

    @Override
    public void run(final Machine machine) {
        machine.getMapper().resetting();
        machine.getMapper().setVramMask(0x3FFF);
        machine.getCPU().reset();
    }
}
