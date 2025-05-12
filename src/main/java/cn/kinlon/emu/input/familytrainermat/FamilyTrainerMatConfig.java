package cn.kinlon.emu.input.familytrainermat;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.FamilyTrainerMat;

public class FamilyTrainerMatConfig
        extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public FamilyTrainerMatConfig() {
        super(FamilyTrainerMat);
    }

    public FamilyTrainerMatConfig(
            final FamilyTrainerMatConfig familyTrainerMatConfig) {
        super(familyTrainerMatConfig);
    }

    @Override
    public FamilyTrainerMatConfig copy() {
        return new FamilyTrainerMatConfig(this);
    }
}