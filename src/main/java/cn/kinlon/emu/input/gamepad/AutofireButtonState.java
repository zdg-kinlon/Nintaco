package cn.kinlon.emu.input.gamepad;

public class AutofireButtonState {

    private final Autofire autofire;
    public boolean asserted;
    private int autofireCounter;

    public AutofireButtonState(final Autofire autofire) {
        this.autofire = autofire;
    }

    public void update(boolean autofireHeld, final boolean buttonHeld) {

        if (autofire.enabled) {
            autofireHeld |= buttonHeld;
        }

        if (autofireHeld) {
            if (autofireCounter == 0) {
                autofireCounter = autofire.rate;
                asserted = !asserted;
            } else {
                autofireCounter--;
            }
        } else {
            autofireCounter = 0;
            asserted = false;
        }
    }

}
