package cn.kinlon.emu.input.mahjong;

import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.icons.InputIcons;

import java.io.Serializable;

import static cn.kinlon.emu.input.mahjong.MahjongDescriptor.*;

public class MahjongMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private int buttons;
    private int shiftRegister;

    @Override
    public int getInputDevice() {
        return InputDevices.Mahjong;
    }

    @Override
    public void update(final int buttons) {
        this.buttons = buttons >> 16;
    }

    @Override
    public void writePort(final int value) {
        int bits = buttons;
        shiftRegister = 0;
        switch (value & 0x06) {
            case 0x02:
                for (int i = 2; i >= 0; i--, bits >>= 5) {
                    switch (bits & 0x1F) {
                        case KeyI:
                            shiftRegister |= 0x100;
                            break;
                        case KeyJ:
                            shiftRegister |= 0x080;
                            break;
                        case KeyK:
                            shiftRegister |= 0x040;
                            break;
                        case KeyL:
                            shiftRegister |= 0x020;
                            break;
                        case KeyM:
                            shiftRegister |= 0x010;
                            break;
                        case KeyN:
                            shiftRegister |= 0x008;
                            break;
                        case 0x1F:
                            return;
                    }
                }
                break;
            case 0x04:
                for (int i = 2; i >= 0; i--, bits >>>= 5) {
                    switch (bits & 0x1F) {
                        case KeyA:
                            shiftRegister |= 0x100;
                            break;
                        case KeyB:
                            shiftRegister |= 0x080;
                            break;
                        case KeyC:
                            shiftRegister |= 0x040;
                            break;
                        case KeyD:
                            shiftRegister |= 0x020;
                            break;
                        case KeyE:
                            shiftRegister |= 0x010;
                            break;
                        case KeyF:
                            shiftRegister |= 0x008;
                            break;
                        case KeyG:
                            shiftRegister |= 0x004;
                            break;
                        case KeyH:
                            shiftRegister |= 0x002;
                            break;
                        case 0x1F:
                            return;
                    }
                }
                break;
            case 0x06:
                for (int i = 2; i >= 0; i--, bits >>>= 5) {
                    switch (bits & 0x1F) {
                        case KeySelect:
                            shiftRegister |= 0x100;
                            break;
                        case KeyStart:
                            shiftRegister |= 0x080;
                            break;
                        case KeyKan:
                            shiftRegister |= 0x040;
                            break;
                        case KeyPon:
                            shiftRegister |= 0x020;
                            break;
                        case KeyChi:
                            shiftRegister |= 0x010;
                            break;
                        case KeyReach:
                            shiftRegister |= 0x008;
                            break;
                        case KeyRon:
                            shiftRegister |= 0x004;
                            break;
                        case 0x1F:
                            return;
                    }
                }
                break;
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 1) {
            final int value = shiftRegister & 0x02;
            shiftRegister >>= 1;
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return (portIndex == 1) ? (shiftRegister & 0x02) : 0;
    }

    @Override
    public void render(final int[] screen) {
        final int x = 169;
        final int y = 211;
        InputIcons.Mahjong.render(screen, x, y);
        int bits = buttons;
        for (int i = 2; i >= 0; i--, bits >>= 5) {
            final int b = bits & 0x1F;
            if (b == 0x1F) {
                return;
            } else if (b <= KeyM) {
                InputIcons.FamilyBasicKeyboardKey.render(screen, x + 2 + 3 * b, y + 3);
            } else if (b == KeyN) {
                InputIcons.GamepadAB.render(screen, x + 41, y + 2);
            } else {
                InputIcons.FamilyBasicKeyboardKey.render(screen, x + 17
                        + 3 * (b - KeySelect), y + 6);
            }
        }
    }
}