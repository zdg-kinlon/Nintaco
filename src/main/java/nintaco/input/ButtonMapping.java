package nintaco.input;

import java.io.Serializable;

import static nintaco.util.CollectionsUtil.compareArrays;

public class ButtonMapping implements Serializable {

    private static final long serialVersionUID = 0;

    protected final int inputDevice;
    protected final int buttonIndex;
    protected final ButtonID[] buttonIds;

    protected transient String buttonName;
    protected transient String description;

    public ButtonMapping(final ButtonMapping buttonMapping) {
        this(buttonMapping.getInputDevice(), buttonMapping.getButtonIndex(),
                buttonMapping.getButtonIds());
    }

    public ButtonMapping(final int inputDevice, final int buttonIndex) {
        this(inputDevice, buttonIndex, new ButtonID[]{new ButtonID()});
    }

    public ButtonMapping(final int inputDevice, final int buttonIndex,
                         final ButtonID[] buttonIds) {
        this.inputDevice = inputDevice;
        this.buttonIndex = buttonIndex;
        this.buttonIds = buttonIds;
    }

    public ButtonMapping copy() {
        return new ButtonMapping(this);
    }

    public int getInputDevice() {
        return inputDevice;
    }

    public String getButtonName() {
        if (buttonName == null) {
            buttonName = DeviceDescriptor.getDescriptor(inputDevice)
                    .getButtonName(buttonIndex);
        }
        return buttonName;
    }

    public int getButtonIndex() {
        return buttonIndex;
    }

    public ButtonID[] getButtonIds() {
        return buttonIds;
    }

    public String getDescription() {
        if (description == null) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < buttonIds.length; i++) {
                if (i > 0) {
                    sb.append(" + ");
                }
                sb.append(buttonIds[i]);
            }
            description = sb.toString();
        }
        return description;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public boolean equals(Object obj) {
        final ButtonMapping b = (ButtonMapping) obj;
        return b.buttonIndex == buttonIndex
                && compareArrays(buttonIds, b.buttonIds);
    }
}
