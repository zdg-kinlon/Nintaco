package cn.kinlon.emu.input.familybasic.datarecorder;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.DataRecorder;

public class DataRecorderConfig extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public DataRecorderConfig() {
        super(DataRecorder);
    }

    public DataRecorderConfig(final DataRecorderConfig dataRecorderConfig) {
        super(dataRecorderConfig);
    }

    @Override
    public DataRecorderConfig copy() {
        return new DataRecorderConfig(this);
    }
}