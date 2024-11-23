package nintaco.input.other;

import nintaco.Machine;
import nintaco.input.OtherInput;

public class SetNoSpriteLimit implements OtherInput {

    private static final long serialVersionUID = 0;

    private final boolean noSpriteLimit;

    public SetNoSpriteLimit(final boolean noSpriteLimit) {
        this.noSpriteLimit = noSpriteLimit;
    }

    @Override
    public void run(final Machine machine) {
        machine.getPPU().setNoSpriteLimit(noSpriteLimit);
    }
}
