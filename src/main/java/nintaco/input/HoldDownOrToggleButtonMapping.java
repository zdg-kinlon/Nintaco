package nintaco.input;

import java.io.Serializable;

public class HoldDownOrToggleButtonMapping extends ButtonMapping
        implements Serializable {

    private static final long serialVersionUID = 0;

    private boolean holdDown;

    public HoldDownOrToggleButtonMapping(
            final HoldDownOrToggleButtonMapping buttonMapping) {
        this(buttonMapping.getInputDevice(), buttonMapping.getButtonIndex(),
                buttonMapping.getButtonIds(), buttonMapping.isHoldDown());
    }

    public HoldDownOrToggleButtonMapping(final int inputDevice,
                                         final int buttonIndex) {
        this(inputDevice, buttonIndex, new ButtonID[]{new ButtonID()},
                false);
    }

    public HoldDownOrToggleButtonMapping(final int inputDevice,
                                         final int buttonIndex, final ButtonID[] buttonIds,
                                         final boolean holdDown) {
        super(inputDevice, buttonIndex, buttonIds);
        this.holdDown = holdDown;
    }

    @Override
    public String getDescription() {
        if (description == null) {
            description = super.getDescription();
            if (!"...".equals(description)) {
                description = String.format("%s (%s)", description,
                        holdDown ? "hold" : "toggle");
            }
        }
        return description;
    }

    @Override
    public ButtonMapping copy() {
        return new HoldDownOrToggleButtonMapping(this);
    }

    public boolean isHoldDown() {
        return holdDown;
    }

    public void setHoldDown(boolean holdDown) {
        this.holdDown = holdDown;
    }
}