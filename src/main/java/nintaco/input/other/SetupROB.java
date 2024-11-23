package nintaco.input.other;

import nintaco.App;
import nintaco.Machine;
import nintaco.gui.rob.RobController;
import nintaco.input.InputUtil;
import nintaco.input.OtherInput;

public class SetupROB implements OtherInput {

    private static final long serialVersionUID = 0;

    private final RobController rob;

    public SetupROB(final RobController rob) {
        this.rob = rob;
    }

    @Override
    public void run(final Machine machine) {
        InputUtil.setRob(rob);
        if (rob == null) {
            App.destroyRobFrame();
        } else {
            App.createRobFrame(rob.getState().game);
        }
    }
}
