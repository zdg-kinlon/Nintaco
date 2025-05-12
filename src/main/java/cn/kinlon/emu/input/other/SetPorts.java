package cn.kinlon.emu.input.other;

import cn.kinlon.emu.Machine;
import cn.kinlon.emu.input.InputUtil;
import cn.kinlon.emu.input.OtherInput;
import cn.kinlon.emu.input.Ports;
import cn.kinlon.emu.preferences.AppPrefs;

public class SetPorts implements OtherInput {

    private static final long serialVersionUID = 0;

    private final Ports ports;

    public SetPorts(final Ports ports) {
        this.ports = ports;
    }

    @Override
    public void run(final Machine machine) {
        AppPrefs.getInstance().getInputs().setPorts(ports);
        AppPrefs.save();
        InputUtil.handleSettingsChange();
    }
}
