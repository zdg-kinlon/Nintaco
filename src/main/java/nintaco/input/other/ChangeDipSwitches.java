package nintaco.input.other;

import nintaco.Machine;
import nintaco.input.OtherInput;

public class ChangeDipSwitches implements OtherInput {

    private static final long serialVersionUID = 0;

    private final int dipSwitchesValue;

    public ChangeDipSwitches(final int dipSwitchesValue) {
        this.dipSwitchesValue = dipSwitchesValue;
    }

    @Override
    public void run(final Machine machine) {
        machine.getMapper().setDipSwitchesValue(dipSwitchesValue);
    }
}
