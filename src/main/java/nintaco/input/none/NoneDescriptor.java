package nintaco.input.none;

import nintaco.input.ButtonMapping;
import nintaco.input.DeviceDescriptor;
import nintaco.input.InputDevices;

public class NoneDescriptor extends DeviceDescriptor {

    public NoneDescriptor() {
        super(InputDevices.None);
    }

    @Override
    public String getDeviceName() {
        return "<none>";
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
