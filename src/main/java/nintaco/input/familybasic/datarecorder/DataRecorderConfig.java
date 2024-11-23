package nintaco.input.familybasic.datarecorder;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceConfig;

import java.io.Serializable;
import java.util.List;

import static nintaco.input.InputDevices.DataRecorder;

public class DataRecorderConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public DataRecorderConfig() {
        super(DataRecorder);
    }

    public DataRecorderConfig(final List<ButtonMapping> buttonMappings) {
        super(DataRecorder, buttonMappings);
    }

    public DataRecorderConfig(final DataRecorderConfig dataRecorderConfig) {
        super(dataRecorderConfig);
    }

    @Override
    public DataRecorderConfig copy() {
        return new DataRecorderConfig(this);
    }
}