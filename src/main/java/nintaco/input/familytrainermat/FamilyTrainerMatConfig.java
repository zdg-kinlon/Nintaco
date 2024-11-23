package nintaco.input.familytrainermat;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.FamilyTrainerMat;

public class FamilyTrainerMatConfig
        extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public FamilyTrainerMatConfig() {
        super(FamilyTrainerMat);
    }

    public FamilyTrainerMatConfig(final List<ButtonMapping> buttonMappings) {
        super(FamilyTrainerMat, buttonMappings);
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