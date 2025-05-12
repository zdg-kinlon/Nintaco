package cn.kinlon.emu.input.familybasic.datarecorder;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;

public class DataRecorderDescriptor extends DeviceDescriptor {

    public DataRecorderDescriptor() {
        super(InputDevices.DataRecorder);
    }

    @Override
    public String getDeviceName() {
        return "Data Recorder";
    }

    @Override
    public int getButtonCount() {
        return 0;
    }

    @Override
    public String getButtonName(final int buttonIndex) {
        return null;
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return null;
    }

    @Override
    public int setButtonBits(final int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {
        return bits;
    }
}
