package cn.kinlon.emu.input.powerglove;

import cn.kinlon.emu.input.DeviceMapper;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.PowerGlove;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;
import static cn.kinlon.emu.utils.MathUtil.clamp;

public class PowerGloveMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private final int[] buffer = {
            0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3F, 0xFF, 0xFF};

    private final int portIndex;
    private final int shift;

    private int latch;
    private int stream = -1;
    private int output;
    private int counter;
    private int depth = 32;
    private int roll = 32;

    private int mouseX;
    private int mouseY;
    private int gesture;
    private boolean select;
    private boolean start;
    private boolean moveIn;
    private boolean moveOut;
    private boolean rollLeft;
    private boolean rollRight;

    public PowerGloveMapper(final int portIndex) {
        this.portIndex = portIndex;
        shift = portIndex == 0 ? 0 : 8;
    }

    @Override
    public int getInputDevice() {
        return PowerGlove;
    }

    @Override
    public void update(final int buttons) {
        int y = (buttons >> 24) & 0xFF;
        if (y < 240) {
            mouseX = (clamp((buttons >> 16) & 0xFF, 24, 235) - 24) * 112 / 211 - 54;
            mouseY = 34 - (clamp(y, 21, 158) - 21) * 73 / 137;
        }
        final int bits = buttons >> shift;
        if (getBitBool(bits, 0)) {
            gesture = 0x0F;
        } else if (getBitBool(bits, 1)) {
            gesture = 0xFF;
        } else {
            gesture = 0x00;
        }
        select = getBitBool(bits, 2);
        start = getBitBool(bits, 3);
        moveIn = getBitBool(bits, 4);
        moveOut = getBitBool(bits, 5);
        rollLeft = getBitBool(bits, 6);
        rollRight = getBitBool(bits, 7);
    }

    @Override
    public void writePort(final int value) {
        latch = ((latch << 1) & 0xFE) | (value & 0x01);

        if (latch == 0x06 && counter == 0) {
            stream = -1;
        } else if (latch == 0xFF) {
            stream = -1;
            counter = 1;
        } else if (counter != 0 && counter++ == 11) {
            stream = 0;
            counter = 0;
        }
    }

    private void updateBuffer() {
        buffer[1] = mouseX;
        buffer[2] = mouseY;

        if (moveOut) {
            if (depth < 127) {
                depth++;
            }
        } else if (moveIn) {
            if (depth > 0) {
                depth--;
            }
        }

        buffer[3] = ((depth >> 2) - 16) & 0xFF;

        if (rollLeft) {
            if (roll < 63) {
                roll++;
            }
        } else if (rollRight) {
            if (roll > 0) {
                roll--;
            }
        } else if (roll < 32) {
            roll++;
        } else if (roll > 32) {
            roll--;
        }

        buffer[4] = ((roll >> 1) - 16) & 0xFF;
        buffer[5] = gesture;

        if (start) {
            buffer[6] = 0x82;
        } else if (select) {
            buffer[6] = 0x83;
        } else {
            buffer[6] = 0xFF;
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (this.portIndex == portIndex) {
            int data = 0;

            if (stream != -1) {
                data = stream++;

                if ((data & 0x07) == 0) {
                    updateBuffer();
                    output = buffer[data >> 3] ^ 0xFF;
                } else if (data == 0x5F) {
                    stream = 0;
                }

                data = output >> 7;
                output = (output << 1) & 0xFF;
            }

            return data;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        if (this.portIndex == portIndex) {
            return stream != -1 ? (output >> 7) : 0;
        } else {
            return 0;
        }
    }
}
