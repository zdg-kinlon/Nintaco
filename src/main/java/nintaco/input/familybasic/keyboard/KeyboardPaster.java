package nintaco.input.familybasic.keyboard;

import nintaco.input.DeviceDescriptor;
import nintaco.input.Ports;

import static nintaco.util.StringUtil.replaceNewlines;

public class KeyboardPaster {

    private static final int RELEASED = 0;
    private static final int CHAR_PRESSED = 1;
    private static final int KANA_PRESSED = 2;

    private static final int MAX_LINE_LENGTH = 28;

    private final String str;
    private final int shortDelay;
    private final int longDelay;

    private int state = RELEASED;
    private int index;
    private int lineLength;
    private int timer;
    private boolean kana;
    private boolean finished;

    public KeyboardPaster(final String str, final int shortDelay,
                          final int longDelay) {
        this.str = replaceNewlines(str);
        this.shortDelay = shortDelay;
        this.longDelay = longDelay;
    }

    public int type(final int bits, final int consoleType,
                    final int[] pressedValues) {
        if (timer > 0) {
            timer--;
        } else {
            timer = shortDelay;
            switch (state) {
                case RELEASED:
                    final char c = str.charAt(index);
                    if (c <= 0xFF || c == '\uFF1A') {
                        if (kana) {
                            DeviceDescriptor.Keyboard.setKanaEnabled(pressedValues, false);
                            state = KANA_PRESSED;
                            kana = false;
                        } else {
                            if (c == '\n' || ++lineLength == MAX_LINE_LENGTH) {
                                lineLength = 0;
                                timer = longDelay;
                            }
                            DeviceDescriptor.Keyboard.pressKey(pressedValues, c);
                            state = CHAR_PRESSED;
                        }
                    } else {
                        if (kana) {
                            if (++lineLength == MAX_LINE_LENGTH) {
                                lineLength = 0;
                                timer = longDelay;
                            }
                            DeviceDescriptor.Keyboard.pressKey(pressedValues, c);
                            state = CHAR_PRESSED;
                        } else {
                            DeviceDescriptor.Keyboard.setKanaEnabled(pressedValues, true);
                            state = KANA_PRESSED;
                            kana = true;
                        }
                    }
                    break;
                case CHAR_PRESSED:
                    final int newIndex = index + 1;
                    if (newIndex >= str.length()) {
                        DeviceDescriptor.Keyboard.setKanaEnabled(pressedValues, false);
                        finished = true;
                    } else {
                        index = newIndex;
                    }
                    state = RELEASED;
                    break;
                case KANA_PRESSED:
                    state = RELEASED;
                    break;
            }
        }

        return DeviceDescriptor.Keyboard.setButtonBits(bits, consoleType,
                Ports.ExpansionPort, pressedValues);
    }

    public boolean isFinished() {
        return finished;
    }
}