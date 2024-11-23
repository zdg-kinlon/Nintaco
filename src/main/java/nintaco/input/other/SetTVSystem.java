package nintaco.input.other;

import nintaco.App;
import nintaco.Machine;
import nintaco.input.OtherInput;
import nintaco.tv.TVSystem;

public class SetTVSystem implements OtherInput {

    private static final long serialVersionUID = 0;

    private final TVSystem tvSystem;

    public SetTVSystem(final TVSystem tvSystem) {
        this.tvSystem = tvSystem;
    }

    public static void run(final Machine machine, final TVSystem tvSystem) {
        machine.getMapper().setTVSystem(tvSystem);
        machine.getPPU().setTVSystem(tvSystem);
        machine.getAPU().setTVSystem(tvSystem);
        if (App.getMachine() == machine) {
            App.getImageFrame().getImagePane().setTVSystem(tvSystem);
        }
    }

    @Override
    public void run(final Machine machine) {
        run(machine, tvSystem);
    }
}
