package nintaco.input.other;

import nintaco.Machine;
import nintaco.input.OtherInput;

public class ScreamIntoMicrophone implements OtherInput {

    private static final long serialVersionUID = 0;

    @Override
    public void run(final Machine machine) {
        machine.getMapper().screamIntoMicrophone();
    }
}
