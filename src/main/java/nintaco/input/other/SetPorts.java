package nintaco.input.other;

import nintaco.App;
import nintaco.Machine;
import nintaco.input.InputUtil;
import nintaco.input.OtherInput;
import nintaco.input.Ports;
import nintaco.preferences.AppPrefs;

public class SetPorts implements OtherInput {

    private static final long serialVersionUID = 0;

    private final Ports ports;

    public SetPorts(final Ports ports) {
        this.ports = ports;
    }

    @Override
    public void run(final Machine machine) {
        if (App.getNetplayClient().isRunning()) {
//      InputUtil.setOverrides(ports.getConsoleType(), ports.isMultitap());
        } else {
            AppPrefs.getInstance().getInputs().setPorts(ports);
            AppPrefs.save();
        }
        InputUtil.handleSettingsChange();
    }
}
