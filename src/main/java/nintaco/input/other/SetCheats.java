package nintaco.input.other;

import nintaco.Machine;
import nintaco.cheats.Cheat;
import nintaco.input.OtherInput;

public class SetCheats implements OtherInput {

    private static final long serialVersionUID = 0;

    private final Cheat[] cheats;

    public SetCheats(final Cheat[] cheats) {
        this.cheats = cheats;
    }

    @Override
    public void run(final Machine machine) {
        machine.getCPU().setCheats(cheats);
    }
}
