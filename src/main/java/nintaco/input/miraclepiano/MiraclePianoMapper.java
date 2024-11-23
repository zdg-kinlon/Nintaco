package nintaco.input.miraclepiano;

import nintaco.CPU;
import nintaco.Machine;
import nintaco.apu.APU;
import nintaco.apu.SystemAudioProcessor;
import nintaco.input.DeviceMapper;
import nintaco.input.InputDevices;
import nintaco.input.icons.InputIcons;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static nintaco.input.miraclepiano.MiraclePianoDescriptor.*;
import static nintaco.util.BitUtil.reverseBits;
import static nintaco.util.MathUtil.clamp;

public class MiraclePianoMapper extends DeviceMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private static final int[] GENERAL_MIDI_PATCHES = {
            // x0   x1   x2   x3   x4   x5   x6   x7  
            // y8   y9   yA   yB   yC   yD   yE   yF
            0, 3, 2, 1, 6, 7, 18, 19, // 0x
            25, 25, 24, 105, 107, 107, 26, 27, // 0y
            29, 30, 28, 46, 6, 32, 36, 33, // 1x
            4, 34, 99, 35, 33, 11, 11, 13, // 1y
            12, 9, 108, 14, 114, 116, 48, 45, // 2x
            49, 40, 46, 46, 60, 69, 57, 57, // 2y
            59, 62, 63, 81, 58, 22, 73, 75, // 3x
            82, 77, 71, 68, 70, 64, 112, 14, // 3y
            112, 26, 27, 28, 4, 6, 2, 5, // 4x
            127, 119, 117, 115, 113, 16, 17, 20, // 4y
            6, 88, 89, 90, 91, 92, 93, 94, // 5x
            95, 80, 82, 83, 84, 85, 86, 87, // 5y
            14, 101, 105, 77, 0, 11, 2, 10, // 6x
            6, 7, 18, 19, 25, 33, 24, 13, // 6y
            12, 57, 56, 62, 63, 81, 14, 1, // 7x
            11, 41, 44, 10, 34, 4, 6, 2, // 7y
    };

    private static final int[] FIRMWARE_RESPONSE = {
            0xF0, 0x00, 0x00, 0x42, 0x01, 0x05, 0x01, 0x00, 0xF7,
    };

    private static final int[][] KEY_COORDINATES = {
            {1, 0}, {2, 2}, {4, 0}, {5, 2}, {7, 1}, {10, 0},
            {11, 2}, {13, 0}, {14, 2}, {16, 0}, {17, 2}, {19, 1},
    };

    private static final int PERCUSSION_CHANNEL = 9;

    private static final int VELOCITY = 100;

    private static final int CONTROLLER_VOLUME = 7;
    private static final int CONTROLLER_DAMPER_PEDAL = 64;

    private static final int STATE_IDLE = 0;
    private static final int STATE_STROBED = 1;
    private static final int STATE_TRANSMIT = 2;
    private static final int STATE_RECEIVE = 3;

    private static final int TRANSMIT_DELAY = 40;
    private static final int QUEUE_CAPACITY = 1024;

    private static final Object SYNTHESIZER_MONITOR = new Object();
    private static volatile Synthesizer synthesizer;
    private static volatile MidiChannel[] channels;
    private final int[] queue = new int[QUEUE_CAPACITY];
    private int queueSize;
    private int head;
    private int tail;
    private int state;
    private int data;
    private int bitsTransmitted;
    private int bytesTransmitted;
    private int command;
    private int d1;
    private int d2;
    private int d3;
    private int mainVolume = 100;
    private int masterVolume = -1;
    private long strobeCycle;
    private long keysPressed;
    private boolean localControlEnabled = true;
    private volatile CPU cpu;

    public MiraclePianoMapper() {
        initMidi();
        adjustVolume();
    }

    private static void initMidi() {
        synchronized (SYNTHESIZER_MONITOR) {
            try {
                if (synthesizer == null) {
                    synthesizer = MidiSystem.getSynthesizer();
                    if (synthesizer != null) {
                        synthesizer.open();
                        channels = synthesizer.getChannels();
                    }
                }
            } catch (final Throwable t) {
            }
        }
    }

    @Override
    public int getInputDevice() {
        return InputDevices.MiraclePiano;
    }

    @Override
    public void setMachine(final Machine machine) {
        if (machine == null) {
            cpu = null;
        } else {
            cpu = machine.getCPU();
        }
    }

    @Override
    public void update(final int buttons) {

        final int offset = 18 * ((buttons >> 30) & 3);
        long pressed = keysPressed;
        pressed &= ~(0x3FFFFFL << offset);
        pressed |= ((long) (((buttons >> 8) & 0x3FFF00) | (buttons & 0xFF)))
                << offset;
        long delta = pressed ^ keysPressed;
        keysPressed = pressed;

        if (localControlEnabled) {
            if (((delta >> DamperPedal) & 1) != 0) {
                final boolean sustain = ((keysPressed >> DamperPedal) & 1) != 0;
                sustain(sustain);
                sendSustain(sustain);
            }
            if (((delta >> Piano) & 1) != 0) {
                final boolean p = ((keysPressed >> Piano) & 1) != 0;
                if (p) {
                    patch(0);
                }
                sendButton(0, p);
            }
            if (((delta >> Harpsichord) & 1) != 0) {
                final boolean p = ((keysPressed >> Harpsichord) & 1) != 0;
                if (p) {
                    patch(4);
                }
                sendButton(1, p);
            }
            if (((delta >> Organ) & 1) != 0) {
                final boolean p = ((keysPressed >> Organ) & 1) != 0;
                if (p) {
                    patch(6);
                }
                sendButton(2, p);
            }
            if (((delta >> Vibraphone) & 1) != 0) {
                final boolean p = ((keysPressed >> Vibraphone) & 1) != 0;
                if (p) {
                    patch(29);
                }
                sendButton(3, p);
            }
            if (((delta >> ElectricPiano) & 1) != 0) {
                final boolean p = ((keysPressed >> ElectricPiano) & 1) != 0;
                if (p) {
                    patch(2);
                }
                sendButton(4, p);
            }
            if (((delta >> Synthesizer) & 1) != 0) {
                final boolean p = ((keysPressed >> Synthesizer) & 1) != 0;
                if (p) {
                    patch(68);
                }
                sendButton(5, p);
            }
            if (((delta >> VolumePlus) & 1) != 0) {
                final boolean p = ((keysPressed >> VolumePlus) & 1) != 0;
                if (p) {
                    increaseMainVolumeLevel();
                }
                sendButton(6, p);
            }
            if (((delta >> VolumeMinus) & 1) != 0) {
                final boolean p = ((keysPressed >> VolumeMinus) & 1) != 0;
                if (p) {
                    decreaseMainVolumeLevel();
                }
                sendButton(7, p);
            }
            int key = 0x24;
            while (delta != 0 && key <= 0x54) {
                if ((delta & 1) != 0) {
                    if ((pressed & 1) != 0) {
                        playNote(key);
                        sendNoteOn(key);
                    } else {
                        stopNote(key);
                        sendNoteOff(key);
                    }
                }
                delta >>= 1;
                pressed >>= 1;
                ++key;
            }
        }
    }

    @Override
    public void writePort(int value) {
        value &= 1;
        switch (state) {
            case STATE_IDLE:
            case STATE_RECEIVE:
                if (value == 1) {
                    strobeCycle = getCycleCounter();
                    state = STATE_STROBED;
                }
                break;
            case STATE_STROBED:
                if (getCycleCounter() - strobeCycle >= TRANSMIT_DELAY) {
                    state = STATE_TRANSMIT;
                    data = bitsTransmitted = 0;
                    writePort(value);
                } else if (value == 0) {
                    state = STATE_RECEIVE;
                    if (queueSize == 0) {
                        data = 0;
                    } else {
                        data = ((0xFF ^ reverseBits(queue[tail])) << 1) | 1;
                        --queueSize;
                        if (++tail == QUEUE_CAPACITY) {
                            tail = 0;
                        }
                    }
                }
                break;
            case STATE_TRANSMIT:
                data = (data << 1) | value;
                ++bitsTransmitted;
                if (bitsTransmitted == 8) {
                    handleTransmit();
                } else if (bitsTransmitted == 9) {
                    state = STATE_IDLE;
                }
                break;
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (portIndex == 0) {
            if (state == STATE_RECEIVE) {
                final int value = data & 1;
                data >>= 1;
                return value;
            }
        } else {
            state = STATE_IDLE;
        }
        return 0;
    }

    @Override
    public int peekPort(final int portIndex) {
        return (portIndex == 0 && state == STATE_RECEIVE) ? (data & 1) : 0;
    }

    @Override
    public void render(final int[] screen) {
        final int x = 137;
        final int y = 196;
        InputIcons.Miracle.render(screen, x, y);
        long k = keysPressed;
        int o = 0;
        int offset = 0;
        for (int i = 0; i < 48; i++, k >>= 1) {
            if ((k & 1) == 1) {
                final int T = KEY_COORDINATES[o][1];
                (T == 0 ? InputIcons.DoremikkoWhite1 : T == 1
                        ? InputIcons.DoremikkoWhite2 : InputIcons.DoremikkoBlack).render(
                        screen, x + offset + KEY_COORDINATES[o][0], y + 13);
            }
            if (o == 11) {
                o = 0;
                offset += 21;
            } else {
                o++;
            }
        }
        if ((k & 1) == 1) {
            InputIcons.DoremikkoWhite2.render(screen, x + 85, y + 13);
        }
        k >>= 1;
        if ((k & 1) == 1) {
            InputIcons.MiraclePedal.render(screen, x + 46, y + 2);
        }
        k >>= 1;
        if ((k & 1) == 1) {
            InputIcons.MiracleUp.render(screen, x + 34, y + 2);
        }
        k >>= 1;
        if ((k & 1) == 1) {
            InputIcons.MiracleDown.render(screen, x + 34, y + 7);
        }
        k >>= 1;
        if ((k & 1) == 1) {
            InputIcons.MiracleButton.render(screen, x + 19, y + 2);
        }
        k >>= 1;
        if ((k & 1) == 1) {
            InputIcons.MiracleButton.render(screen, x + 24, y + 2);
        }
        k >>= 1;
        if ((k & 1) == 1) {
            InputIcons.MiracleButton.render(screen, x + 29, y + 2);
        }
        k >>= 1;
        if ((k & 1) == 1) {
            InputIcons.MiracleButton.render(screen, x + 19, y + 7);
        }
        k >>= 1;
        if ((k & 1) == 1) {
            InputIcons.MiracleButton.render(screen, x + 24, y + 7);
        }
        k >>= 1;
        if ((k & 1) == 1) {
            InputIcons.MiracleButton.render(screen, x + 29, y + 7);
        }
    }

    @Override
    public void close(final boolean saveNonVolatileData) {
        synchronized (SYNTHESIZER_MONITOR) {
            try {
                if (synthesizer != null) {
                    synthesizer.close();
                    synthesizer = null;
                }
                channels = null;
            } catch (final Throwable t) {
            }
        }
    }

    private void playNote(final int key) {
        final MidiChannel[] cs = channels;
        if (cs != null) {
            final int channel = (key >= 60) ? 0 : 8;
            if (channel < cs.length) {
                final MidiChannel c = cs[channel];
                if (c != null) {
                    adjustVolume();
                    c.noteOn(key, VELOCITY);
                }
            }
        }
    }

    private void stopNote(final int key) {
        final MidiChannel[] cs = channels;
        if (cs != null) {
            final int channel = (key >= 60) ? 0 : 8;
            if (channel < cs.length) {
                final MidiChannel c = cs[channel];
                if (c != null) {
                    c.noteOff(key);
                }
            }
        }
    }

    private void sendNoteOn(final int key) {
        enqueue(0x90);
        enqueue(key);
        enqueue(VELOCITY);
    }

    private void sendNoteOff(final int key) {
        enqueue(0x90);
        enqueue(key);
        enqueue(0);
    }

    private void enqueue(final int value) {
        if (queueSize < QUEUE_CAPACITY) {
            ++queueSize;
            queue[head] = value;
            if (++head == QUEUE_CAPACITY) {
                head = 0;
            }
        }
    }

    private long getCycleCounter() {
        final CPU cpu = this.cpu;
        return (cpu != null) ? cpu.getCycleCounter() : 0L;
    }

    private void adjustVolume() {
        final int volume = APU.isSoundEnabled()
                ? SystemAudioProcessor.getMasterVolume() : 0;
        if (volume != masterVolume) {
            masterVolume = volume;
            setMainVolumeLevel(mainVolume);
        }
    }

    private void handleTransmit() {
        ++bytesTransmitted;
        if (bytesTransmitted == 1) {
            if (data < 0x80 && (command & 0xF0) == 0x90) {
                bytesTransmitted = 2;
                d1 = data;
            } else {
                command = data;
                if (command == 0xFF) {
                    bytesTransmitted = 0;
                } else {
                    switch (command & 0xF0) {
                        case 0x80:
                        case 0x90:
                        case 0xB0:
                        case 0xC0:
                            break;
                        default:
                            if (command != 0xF0) {
                                bytesTransmitted = 0;
                            }
                            break;
                    }
                }
            }
        } else {
            switch (command & 0xF0) {
                case 0x80:
                case 0x90:
                    if (bytesTransmitted == 2) {
                        d1 = data;
                    } else {
                        bytesTransmitted = 0;
                        handleToggleNote();
                    }
                    break;
                case 0xB0:
                    if (bytesTransmitted == 2) {
                        d1 = data;
                    } else {
                        bytesTransmitted = 0;
                        switch (d1) {
                            case 0x07:
                                handleMainVolumeLevel();
                                break;
                            case 0x40:
                                handleToggleSustain();
                                break;
                            case 0x7A:
                                handleToggleLocalControl();
                                break;
                            case 0x7B:
                                handleDisableNotes();
                                break;
                        }
                    }
                    break;
                case 0xC0:
                    bytesTransmitted = 0;
                    handlePatchChange();
                    break;
                case 0xF0:
                    switch (bytesTransmitted) {
                        case 2:
                        case 3:
                            if (data != 0x00) {
                                bytesTransmitted = 0;
                            }
                            break;
                        case 4:
                            if (data != 0x42) {
                                bytesTransmitted = 0;
                            }
                            break;
                        case 5:
                            if (data != 0x01) {
                                bytesTransmitted = 0;
                            }
                            break;
                        case 6:
                            d1 = data;
                            break;
                        case 7:
                            if (d1 == 0x06) {
                                d1 = data;
                            } else {
                                bytesTransmitted = 0;
                                if (d1 == 0x04) {
                                    handleFirmwareVersionRequest();
                                }
                            }
                            break;
                        case 8:
                            d2 = data;
                            break;
                        case 9:
                            d3 = data;
                            break;
                        case 10:
                            bytesTransmitted = 0;
                            handlePatchSplit();
                            break;
                    }
                    break;
            }
        }
    }

    private void handlePatchSplit() {
        handlePatchSplit(d1, d2, d3);
    }

    private void handlePatchSplit(final int channel, final int lowerPatch,
                                  final int upperPatch) {

        final MidiChannel[] cs = channels;
        if (cs != null) {
            final int lowerChannel = channel & 0x07;
            if (lowerChannel < cs.length) {
                final MidiChannel c = cs[lowerChannel];
                if (c != null) {
                    c.programChange(GENERAL_MIDI_PATCHES[lowerPatch & 0x7F]);
                }
            }
            final int upperChannel = lowerChannel | 0x08;
            if (upperChannel != PERCUSSION_CHANNEL && upperChannel < cs.length) {
                final MidiChannel c = cs[upperChannel];
                if (c != null) {
                    c.programChange(GENERAL_MIDI_PATCHES[upperPatch & 0x7F]);
                }
            }
        }
    }

    private void patch(final int patch) {
        handlePatchSplit(0, patch, patch);
    }

    private void sendButton(int button, final boolean pressed) {
        if (pressed) {
            button |= 0x08;
        }
        enqueue(0xF0);
        enqueue(0x00);
        enqueue(0x00);
        enqueue(0x42);
        enqueue(0x01);
        enqueue(0x01);
        enqueue(button);
        enqueue(0xF7);
    }

    private void handleFirmwareVersionRequest() {
        for (int i = 0; i < FIRMWARE_RESPONSE.length; ++i) {
            enqueue(FIRMWARE_RESPONSE[i]);
        }
    }

    private void handleDisableNotes() {
        int channel = command & 0x07;

        final MidiChannel[] cs = channels;
        if (cs != null) {
            if (channel < cs.length) {
                final MidiChannel c = cs[channel];
                if (c != null) {
                    c.allNotesOff();
                }
            }
            channel |= 0x08;
            if (channel != PERCUSSION_CHANNEL && channel < cs.length) {
                final MidiChannel c = cs[channel];
                if (c != null) {
                    c.allNotesOff();
                }
            }
        }
    }

    private void handleToggleLocalControl() {
        localControlEnabled = data != 0;
    }

    private void handleToggleSustain() {
        sustain(command, data);
    }

    private void sustain(final boolean sustain) {
        sustain(0, sustain ? 0x7F : 0x00);
    }

    private void sustain(int channel, int sustain) {
        channel &= 0x07;
        sustain &= 0x7F;

        final MidiChannel[] cs = channels;
        if (cs != null) {
            if (channel < cs.length) {
                final MidiChannel c = cs[channel];
                if (c != null) {
                    c.controlChange(CONTROLLER_DAMPER_PEDAL, sustain);
                }
            }
            channel |= 0x08;
            if (channel != PERCUSSION_CHANNEL && channel < cs.length) {
                final MidiChannel c = cs[channel];
                if (c != null) {
                    c.controlChange(CONTROLLER_DAMPER_PEDAL, sustain);
                }
            }
        }
    }

    private void sendSustain(final boolean sustain) {
        enqueue(0xB0);
        enqueue(0x40);
        enqueue(sustain ? 0x7F : 0x00);
    }

    private void handleMainVolumeLevel() {
        setMainVolumeLevel(data);
    }

    private void increaseMainVolumeLevel() {
        adjustMainVolumeLevel(13);
    }

    private void decreaseMainVolumeLevel() {
        adjustMainVolumeLevel(-13);
    }

    private void adjustMainVolumeLevel(final int deltaVolume) {
        setMainVolumeLevel(clamp(mainVolume + deltaVolume, 0, 0x7F));
    }

    private void setMainVolumeLevel(final int volume) {
        mainVolume = volume & 0x7F;
        final int v = clamp((int) (mainVolume * Math.sqrt(masterVolume / 100.0)),
                0, 0x7F);

        final MidiChannel[] cs = channels;
        if (cs != null) {
            for (int i = cs.length - 1; i >= 0; --i) {
                final MidiChannel c = cs[i];
                if (c != null) {
                    c.controlChange(CONTROLLER_VOLUME, v);
                }
            }
        }
    }

    private void handleToggleNote() {
        final int key = d1;
        final int velocity = data;

        int channel = command & 0x07;
        if (key >= 60 && channel != (PERCUSSION_CHANNEL & 0x07)) {
            channel |= 0x08;
        }

        final MidiChannel[] cs = channels;
        if (cs != null && channel < cs.length) {
            final MidiChannel c = cs[channel];
            if (c != null) {
                if (velocity == 0 || (command & 0xF0) == 0x80) {
                    c.noteOff(key);
                } else {
                    adjustVolume();
                    c.noteOn(key, velocity);
                }
            }
        }
    }

    private void handlePatchChange() {
        handlePatchSplit(command, data, data);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        tail = 0;
        queueSize = readQueue(in, queue);
        head = (queueSize == QUEUE_CAPACITY) ? 0 : queueSize;
        initMidi();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        writeQueue(out, queue, tail, queueSize);
    }

    private int readQueue(final ObjectInputStream in, final int[] queue)
            throws IOException {

        final int size = in.readInt();
        for (int i = 0; i < size; ++i) {
            queue[i] = in.read();
        }
        return size;
    }

    private void writeQueue(final ObjectOutputStream out, final int[] queue,
                            int tail, int size) throws IOException {

        out.writeInt(size);
        while (size > 0) {
            --size;
            out.write(queue[tail]);
            if (++tail == QUEUE_CAPACITY) {
                tail = 0;
            }
        }
    }
}