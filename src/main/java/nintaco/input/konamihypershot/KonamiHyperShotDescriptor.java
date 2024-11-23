package nintaco.input.konamihypershot;

import nintaco.input.*;
import nintaco.input.gamepad.Autofire;

import static net.java.games.input.Component.Identifier.Key.*;

public class KonamiHyperShotDescriptor extends DeviceDescriptor {

    public static final int Run1 = 0;
    public static final int Jump1 = 1;
    public static final int AutofireRun1 = 2;
    public static final int AutofireJump1 = 3;
    public static final int ToggleAutofire1 = 4;

    public static final int Run2 = 5;
    public static final int Jump2 = 6;
    public static final int AutofireRun2 = 7;
    public static final int AutofireJump2 = 8;
    public static final int ToggleAutofire2 = 9;

    public static final int RewindTime = 10;

    private static final Key[] DEFAULTS = {
            X, Z, S, Key.A, Q,
            V, C, F, D, E,
            EQUALS,
    };

    private final Autofire[] autofires;

    public KonamiHyperShotDescriptor() {
        super(InputDevices.KonamiHyperShot);
        autofires = new Autofire[]{new Autofire(2), new Autofire(2)};
    }

    @Override
    public void handleSettingsChange(final Inputs inputs) {
        super.handleSettingsChange(inputs);
        autofires[0].setRate(inputs.getAutofireRate() - 1);
        autofires[1].setRate(inputs.getAutofireRate() - 1);
    }

    @Override
    public String getDeviceName() {
        return "Konami Hyper Shot";
    }

    @Override
    public int getButtonCount() {
        return 11;
    }

    @Override
    public int getRewindTimeButton() {
        return RewindTime;
    }

    @Override
    public String getButtonName(final int buttonIndex) {
        switch (buttonIndex) {
            case Run1:
                return "Run 1";
            case Jump1:
                return "Jump 1";
            case AutofireRun1:
                return "Autofire Run 1";
            case AutofireJump1:
                return "Autofire Jump 1";
            case ToggleAutofire1:
                return "Toggle Autofire 1";
            case Run2:
                return "Run 2";
            case Jump2:
                return "Jump 2";
            case AutofireRun2:
                return "Autofire Run 2";
            case AutofireJump2:
                return "Autofire Jump 2";
            case ToggleAutofire2:
                return "Toggle Autofire 2";
            case RewindTime:
                return "Rewind Time";
            default:
                return "Unknown";
        }
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(InputUtil.getDefaultKeyboard(), buttonIndex,
                DEFAULTS);
    }

    @Override
    public int setButtonBits(final int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        autofires[0].setToggle(pressedValues[ToggleAutofire1] != 0);
        autofires[0].buttonStates[0].update(pressedValues[AutofireRun1] != 0,
                pressedValues[Run1] != 0);
        autofires[0].buttonStates[1].update(pressedValues[AutofireJump1] != 0,
                pressedValues[Jump1] != 0);

        autofires[1].setToggle(pressedValues[ToggleAutofire2] != 0);
        autofires[1].buttonStates[0].update(pressedValues[AutofireRun2] != 0,
                pressedValues[Run2] != 0);
        autofires[1].buttonStates[1].update(pressedValues[AutofireJump2] != 0,
                pressedValues[Jump2] != 0);

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        int buttons = 0;
        if (autofires[1].buttonStates[1].asserted
                || (!autofires[0].enabled && pressedValues[Jump2] != 0)) {
            buttons |= 0x08;
        }
        if (autofires[1].buttonStates[0].asserted
                || (!autofires[0].enabled && pressedValues[Run2] != 0)) {
            buttons |= 0x04;
        }
        if (autofires[0].buttonStates[1].asserted
                || (!autofires[0].enabled && pressedValues[Jump1] != 0)) {
            buttons |= 0x02;
        }
        if (autofires[0].buttonStates[0].asserted
                || (!autofires[0].enabled && pressedValues[Run1] != 0)) {
            buttons |= 0x01;
        }

        return bits | (buttons << 16);
    }
}