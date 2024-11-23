package nintaco.input.gamepad;

public class Autofire {

    public final AutofireButtonState[] buttonStates;

    public boolean toggle;
    public boolean enabled;
    public volatile int rate;

    public Autofire(final int buttonCount) {
        buttonStates = new AutofireButtonState[buttonCount];
        for (int i = buttonCount - 1; i >= 0; i--) {
            buttonStates[i] = new AutofireButtonState(this);
        }
    }

    public boolean isToggle() {
        return toggle;
    }

    public void setToggle(final boolean toggle) {
        if (this.toggle != toggle) {
            this.toggle = toggle;
            if (toggle) {
                enabled = !enabled;
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(final int rate) {
        this.rate = rate;
    }
}
