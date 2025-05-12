package cn.kinlon.emu.input.partytap;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.*;

public class PartyTapDescriptor extends DeviceDescriptor {

    public static final int Unit1 = 0;
    public static final int Unit2 = 1;
    public static final int Unit3 = 2;
    public static final int Unit4 = 3;
    public static final int Unit5 = 4;
    public static final int Unit6 = 5;

    public static final int RewindTime = 6;

    private static final Key[] DEFAULTS = {Q, W, E, R, T, Y, EQUALS};

    public PartyTapDescriptor() {
        super(InputDevices.PartyTap);
    }

    @Override
    public String getDeviceName() {
        return "Party Tap";
    }

    @Override
    public int getButtonCount() {
        return 7;
    }

    @Override
    public int getRewindTimeButton() {
        return RewindTime;
    }

    @Override
    public String getButtonName(final int buttonIndex) {
        switch (buttonIndex) {
            case Unit1:
                return "Unit 1";
            case Unit2:
                return "Unit 2";
            case Unit3:
                return "Unit 3";
            case Unit4:
                return "Unit 4";
            case Unit5:
                return "Unit 5";
            case Unit6:
                return "Unit 6";
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

        updateRewindTime(pressedValues[RewindTime] != 0, portIndex);

        int buttons = 0;
        if (pressedValues[Unit6] != 0) {
            buttons |= 0x20;
        }
        if (pressedValues[Unit5] != 0) {
            buttons |= 0x10;
        }
        if (pressedValues[Unit4] != 0) {
            buttons |= 0x08;
        }
        if (pressedValues[Unit3] != 0) {
            buttons |= 0x04;
        }
        if (pressedValues[Unit2] != 0) {
            buttons |= 0x02;
        }
        if (pressedValues[Unit1] != 0) {
            buttons |= 0x01;
        }
        return bits | (buttons << 16);
    }
}
