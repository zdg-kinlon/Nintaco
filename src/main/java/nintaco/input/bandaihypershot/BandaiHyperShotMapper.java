package nintaco.input.bandaihypershot;

import nintaco.input.icons.InputIcons;
import nintaco.input.zapper.ZapperMapper;

import static nintaco.input.InputDevices.BandaiHyperShot;
import static nintaco.input.bandaihypershot.BandaiHyperShotDescriptor.*;
import static nintaco.util.BitUtil.getBit;
import static nintaco.util.BitUtil.getBitBool;

public class BandaiHyperShotMapper extends ZapperMapper {

    private int buttons;
    private int shiftRegister;
    private boolean strobe;

    public BandaiHyperShotMapper() {
        super(1);
    }

    @Override
    public int getInputDevice() {
        return BandaiHyperShot;
    }

    @Override
    public void handleScanline() {
        if (photoSensor > 0) {
            photoSensor--;
        }
        updatePortValue();
    }

    @Override
    public void update(final int buttons) {
        this.buttons = buttons;
        trigger = getBit(buttons, 8);
        coordinates = (buttons >> 16) & 0xFFFF;
        offscreen = (coordinates == 0xFFFF);
        updatePortValue();
    }

    @Override
    public void writePort(final int value) {
        strobe = getBitBool(value, 0);
        if (strobe) {
            shiftRegister = 0xFFFFFE00 | ((buttons >> 7) & 0x1FE);
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 0) {
            final int value = shiftRegister & 0x02;
            if (!strobe) {
                shiftRegister >>= 1;
            }
            return value;
        } else {
            return portValue;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return (portIndex == 0) ? (shiftRegister & 0x02) : portValue;
    }

    @Override
    public void render(final int[] screen) {
        final int bs = buttons >> 8;
        final int x = 168;
        final int y = 202;
        InputIcons.BandaiHyperShot.render(screen, x, y);
        if (getBitBool(bs, Trigger)) {
            InputIcons.BandaiHyperShotTrigger.render(screen, x + 12, y + 15);
        }
        if (getBitBool(bs, Grenade)) {
            InputIcons.BandaiHyperShotTrigger.render(screen, x + 28, y + 14);
        }
        if (getBitBool(bs, Select)) {
            InputIcons.GamepadStart.render(screen, x + 19, y + 9);
        }
        if (getBitBool(bs, Start)) {
            InputIcons.GamepadStart.render(screen, x + 24, y + 9);
        }
        if (getBitBool(bs, Up)) {
            InputIcons.GamepadDPad.render(screen, x + 10, y + 4);
        }
        if (getBitBool(bs, Down)) {
            InputIcons.GamepadDPad.render(screen, x + 10, y + 10);
        }
        if (getBitBool(bs, Left)) {
            InputIcons.GamepadDPad.render(screen, x + 7, y + 7);
        }
        if (getBitBool(bs, Right)) {
            InputIcons.GamepadDPad.render(screen, x + 13, y + 7);
        }
        final int X = coordinates & 0xFF;
        final int Y = (coordinates >> 8) & 0xFF;
        if (Y < 240) {
            InputIcons.ZapperTarget.renderSafe(screen, X - 7, Y - 7);
        }
    }
}
