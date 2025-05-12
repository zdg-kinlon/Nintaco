package cn.kinlon.emu.input.turbofile;

import cn.kinlon.emu.input.ButtonMapping;
import cn.kinlon.emu.input.DeviceDescriptor;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;

import static net.java.games.input.Component.Identifier.Key.EQUALS;
import static net.java.games.input.Component.Identifier.Key.Key;

public class TurboFileDescriptor extends DeviceDescriptor {

    public static final int RewindTime = 0;

    private static final Key[] DEFAULTS = {EQUALS,};

    public TurboFileDescriptor() {
        super(InputDevices.TurboFile);
    }

    @Override
    public String getDeviceName() {
        return "Turbo File";
    }

    @Override
    public int getButtonCount() {
        return 1;
    }

    @Override
    public int getRewindTimeButton() {
        return RewindTime;
    }

    @Override
    public String getButtonName(final int buttonIndex) {
        return "Rewind Time";
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

        return bits;
    }
}