package nintaco.mappers.unif.unl.dripgame;

import java.io.*;
import java.util.*;

public class DripGameAudioChannel implements Serializable {

    private static final long serialVersionUID = 0;

    private final int[] buffer = new int[256];

    private int head;
    private int tail;
    private int period;
    private int volume;
    private int counter;
    private int size;
    private int sample = 0x80;

    public void reset() {
        head = 0;
        tail = 0;
        period = 0;
        volume = 0;
        counter = 0;
        size = 0;
        sample = 0x80;

        Arrays.fill(buffer, 0);
    }

    public int getStatus() {
        switch (size) {
            case 0:
                return 0x40;
            case 256:
                return 0x80;
            default:
                return 0;
        }
    }

    public int getSample() {
        return sample;
    }

    public void silence() {
        size = head = tail = 0;
    }

    public void enqueue(final int value) {
        if (size < 256) {
            buffer[head] = value;
            head = (head + 1) & 0xFF;
            size++;
        }
    }

    public void setLowPeriod(final int value) {
        period = (period & 0x0F00) | value;
    }

    public void setHighPeriod(final int value) {
        period = ((value & 0x0F) << 8) | (period & 0x00FF);
        volume = (value >> 4);
    }

    public void update() {
        if (counter > 0) {
            counter--;
        } else if (size > 0) {
            counter = period;
            sample = buffer[tail] * volume;
            tail = (tail + 1) & 0xFF;
            size--;
        } else {
            sample = 0x80;
        }
    }
}
