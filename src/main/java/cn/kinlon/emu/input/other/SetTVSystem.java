package cn.kinlon.emu.input.other;

import cn.kinlon.emu.App;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.input.OtherInput;
import cn.kinlon.emu.tv.TVSystem;

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
