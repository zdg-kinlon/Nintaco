package cn.kinlon.emu.input.racermate;

import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.icons.InputIcons;

import java.io.Serializable;

import static java.lang.Math.min;
import static cn.kinlon.emu.input.InputDevices.RacerMate1;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;
import static cn.kinlon.emu.utils.BitUtil.reverseBits;
import static cn.kinlon.emu.utils.MathUtil.clamp;

public class RacerMateMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;
    private static final float AVERAGE_PEDAL_WEIGHT = 0.0625f;

    private static final float MAX_PEDAL_TIME = 30f;
    private static final float PEDAL_COEFFICIENT = 0.001f;
    private static final float WIND_COEFFICIENT = 1f / 99f;
    private static final float DRAG_COEFFICIENT = 0.007f;
    private static final float GRADE_COEFFICIENT = 0.0008f / 150f;
    private static final float FRICTION_COEFFICIENT = 0.0001f / 140f;
    private static final float WATTS_SCALE = 1000f / 140f;
    private final int shift;
    private final int portIndex;
    private int buttons;
    private int out;
    private int writeValue;
    private int readValue;
    private int reads;
    private int readIndex;
    private int grade;
    private int wind;
    private int weight;
    private int pedalTimer = (int) MAX_PEDAL_TIME;
    private float averagePedalTime = MAX_PEDAL_TIME;
    private float speed;
    private boolean leftPedalActive;
    private boolean leftPedalPressed;
    private boolean rightPedalPressed;
    private ReceiveType receiveType = ReceiveType.SPEED;

    public RacerMateMapper(final int portIndex) {
        this.shift = portIndex << 3;
        this.portIndex = portIndex;
    }

    private void write(final int index, int data) {

        switch (index) {
            case 1: // Grade x 10 (-49 to 150)
            case 2: // Wind (-99 to 99)
                if (data >= 0x800) {
                    data |= 0xFFFFF000;
                }
                if (index == 1) {
                    grade = data;
                } else {
                    wind = data;
                }
                break;
            case 3: // Weight (70 to 325)
                weight = data;
                break;
            case 4: // Pulse Target Min (0 to 220)
            case 5: // Pulse Target Max (0 to 220)
                data -= 0x800;
                break;
        }
    }

    @Override
    public int getInputDevice() {
        return RacerMate1;
    }

    @Override
    public void update(int buttons) {

        buttons = (buttons >> shift) & 0xFF;

        int keys = buttons & 0x3F;
        if (keys == 0) {
            this.buttons = 0x80; // No buttons pressed is indicated by setting bit 7.
        } else {
            // Multiple buttons cannot be pressed simultaneously. The loop below
            // finds and applies only the right-most set bit.
            this.buttons = 1;
            while ((keys & 1) == 0) {
                keys >>= 1;
                this.buttons <<= 1;
            }
        }

        leftPedalPressed = getBitBool(buttons, 7);
        rightPedalPressed = getBitBool(buttons, 6);
        updateSpeed();
    }

    private void updateSpeed() {

        if (leftPedalActive) {
            if (rightPedalPressed && !leftPedalPressed) {
                leftPedalActive = false;
                pedalTimer = 0;
            } else {
                ++pedalTimer;
            }
        } else {
            if (leftPedalPressed && !rightPedalPressed) {
                leftPedalActive = true;
                pedalTimer = 0;
            } else {
                ++pedalTimer;
            }
        }
        averagePedalTime = AVERAGE_PEDAL_WEIGHT * pedalTimer
                + (1f - AVERAGE_PEDAL_WEIGHT) * averagePedalTime;
        if (averagePedalTime > MAX_PEDAL_TIME) {
            averagePedalTime = MAX_PEDAL_TIME;
        }
        speed += PEDAL_COEFFICIENT * (1f - averagePedalTime / MAX_PEDAL_TIME);

        final float windSpeed = speed + WIND_COEFFICIENT * wind;
        final float drag = DRAG_COEFFICIENT * windSpeed * windSpeed;
        if (windSpeed < 0) {
            speed += drag;
        } else {
            speed -= drag;
        }

        speed -= GRADE_COEFFICIENT * grade;

        speed -= FRICTION_COEFFICIENT * weight;

        if (speed > 1f) {
            speed = 1f;
        } else if (speed < 1E-6f) {
            speed = 0f;
        }
    }

    @Override
    public void writePort(final int value) {
        out = value;
    }

    @Override
    public int readPort(final int portIndex) {

        if (portIndex == 0) {
            if (reads >= 24
                    && (out == 0xBB || out == 0xEE || out == 0x33 || out == 0xCC)) {

                reads = 0;
                if ((out & 1) == 0) {

                    write(reverseBits((writeValue >> 1) & 0x0F) >> 4,
                            (reverseBits((writeValue >> 5) & 0x7F) << 4)
                                    | (reverseBits((writeValue >> (13 + this.portIndex))
                                    & 0x1F) >> 3));

                    writeValue = readIndex = 0;

                    switch (receiveType) {
                        case SPEED:
                            receiveType = ReceiveType.WATTS;
                            readValue = 0x0100000 | (((int) (0xFFF * speed)) << 8) | buttons;
                            break;
                        case WATTS:
                            receiveType = ReceiveType.PULSE;
                            readValue = 0x0200000 | ((min(0xFFF,
                                    (int) (WATTS_SCALE * speed * weight))) << 8) | buttons;
                            break;
                        case PULSE:
                            receiveType = ReceiveType.RPM;
                            readValue = 0x1300000 | (clamp((int) (450 * speed), 60, 255)
                                    << 8) | buttons;
                            break;
                        case RPM:
                            receiveType = ReceiveType.SPEED;
                            readValue = 0x0680000 | (((int) (0xFF * speed)) << 8)
                                    | buttons;
                            break;
                    }

                } else {
                    readIndex = 24;
                }

                // Player 1 data is delayed by 1 read; player 2 by 2 reads.
                readIndex -= this.portIndex;

                return 0;
            } else {
                ++reads;
            }
        }

        if (this.portIndex == portIndex) {

            int b = (readValue >> (readIndex >> 1)) & 1;
            if ((readIndex & 1) == 0) {
                b ^= 1;
            }

            if (readIndex < 48 && (readIndex & 1) == 1) {
                writeValue = (writeValue << 1) | (out & 1);
            }

            ++readIndex;

            return b;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        if (this.portIndex == portIndex) {
            int b = (readValue >> (readIndex >> 1)) & 1;
            if ((readIndex & 1) == 0) {
                b ^= 1;
            }
            return b;
        } else {
            return 0;
        }
    }

    @Override
    public void render(final int[] screen) {
        final int x = (portIndex == 0) ? 16 : 136;
        final int y = 12;
        InputIcons.RacerMate.render(screen, x, y);
        if (getBitBool(buttons, 0)) {
            InputIcons.RacerMateButton.render(screen, x + 36, y + 32);
        }
        if (getBitBool(buttons, 1)) {
            InputIcons.RacerMateButton.render(screen, x + 36, y + 37);
        }
        if (getBitBool(buttons, 2)) {
            InputIcons.RacerMateButton.render(screen, x + 47, y + 37);
        }
        if (getBitBool(buttons, 3)) {
            InputIcons.RacerMateButton.render(screen, x + 58, y + 32);
        }
        if (getBitBool(buttons, 4)) {
            InputIcons.RacerMateButton.render(screen, x + 47, y + 32);
        }
        if (getBitBool(buttons, 5)) {
            InputIcons.RacerMateButton.render(screen, x + 58, y + 37);
        }
        if (leftPedalPressed) {
            InputIcons.RacerMateLeftPedal.render(screen, x + 1, y + 15);
        }
        if (rightPedalPressed) {
            InputIcons.RacerMateRightPedal.render(screen, x + 73, y + 15);
        }
    }

    private enum ReceiveType {SPEED, WATTS, PULSE, RPM}
}