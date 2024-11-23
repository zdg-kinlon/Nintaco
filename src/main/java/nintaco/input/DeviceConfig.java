package nintaco.input;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    protected final int inputDevice;

    protected List<ButtonMapping> buttonMappings;

    protected transient DeviceDescriptor descriptor;

    public DeviceConfig(final int inputDevice) {
        this(inputDevice, new ArrayList<>());
        final int buttonCount = descriptor.getButtonCount();
        for (int i = 0; i < buttonCount; i++) {
            buttonMappings.add(descriptor.getDefaultButtonMapping(i));
        }
    }

    public DeviceConfig(final int inputDevice,
                        final List<ButtonMapping> buttonMappings) {
        this.inputDevice = inputDevice;
        this.buttonMappings = buttonMappings;
        initDeviceDescriptor();
    }

    public DeviceConfig(final DeviceConfig deviceConfig) {
        inputDevice = deviceConfig.getInputDevice();
        buttonMappings = new ArrayList<>();
        for (final ButtonMapping buttonMapping : deviceConfig.getButtonMappings()) {
            buttonMappings.add(buttonMapping.copy());
        }
        initDeviceDescriptor();
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        initDeviceDescriptor();
    }

    public abstract DeviceConfig copy();

    private void initDeviceDescriptor() {
        if (descriptor == null) {
            descriptor = DeviceDescriptor.getDescriptor(inputDevice);
        }
    }

    public DeviceDescriptor getDeviceDescriptor() {
        initDeviceDescriptor();
        return descriptor;
    }

    public List<ButtonMapping> getButtonMappings() {
        return buttonMappings;
    }

    public void setButtonMappings(final List<ButtonMapping> buttonMappings) {
        this.buttonMappings = buttonMappings;
    }

    public int getInputDevice() {
        return inputDevice;
    }

    public void clear() {
        final int buttonCount = descriptor.getButtonCount();
        for (int i = 0; i < buttonCount; i++) {
            buttonMappings.set(i, descriptor.getNoButtonMapping(i));
        }
    }
}