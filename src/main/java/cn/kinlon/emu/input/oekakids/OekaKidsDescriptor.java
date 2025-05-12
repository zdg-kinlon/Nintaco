package cn.kinlon.emu.input.oekakids;

import net.java.games.input.Component;
import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import static net.java.games.input.Component.Identifier.Button.Button;
import static net.java.games.input.Component.Identifier.Button.Key;

public class OekaKidsDescriptor extends DeviceDescriptor {

    public static final int Touch = 0;
    public static final int Click = 1;

    public static final int RewindTime = 2;

    private static final Component.Identifier[] DEFAULTS
            = {Key.X, Button.LEFT, Key.BACK};

    public OekaKidsDescriptor() {
        super(InputDevices.OekaKids);
    }

    @Override
    public String getDeviceName() {
        return "Oeka Kids Tablet";
    }

    @Override
    public int getButtonCount() {
        return 3;
    }

    @Override
    public int getRewindTimeButton() {
        return RewindTime;
    }

    @Override
    public String getButtonName(final int buttonIndex) {
        switch (buttonIndex) {
            case Touch:
                return "Touch";
            case Click:
                return "Click";
            case RewindTime:
                return "Rewind Time";
            default:
                return "Unknown";
        }
    }

    @Override
    public ButtonMapping getDefaultButtonMapping(final int buttonIndex) {
        return getDefaultButtonMapping(buttonIndex == Click
                        ? InputUtil.getDefaultMouse() : InputUtil.getDefaultKeyboard(),
                buttonIndex, DEFAULTS);
    }

    @Override
    public int setButtonBits(final int bits, final int consoleType,
                             final int portIndex, final int[] pressedValues) {

        updateRewindTime(pressedValues[RewindTime] != 0, 0);

        final int mouseCoordinates = InputUtil.getMouseCoordinates();

        int buttons = bits | (mouseCoordinates << 16);
        return (mouseCoordinates == 0xFFFF) ? buttons : buttons
                | (pressedValues[Touch] != 0 ? 0x00000800 : 0)
                | (pressedValues[Click] != 0 ? 0x00000400 : 0);
    }
}
