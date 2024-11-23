package nintaco.mappers;

import java.io.*;

public abstract class Audio implements Serializable {

    private static final long serialVersionUID = 0;

    public void init() {
    }

    public void reset() {
    }

    public int readRegister(final int address) {
        return -1;
    }

    public boolean writeRegister(final int address, final int value) {
        return false;
    }

    public void update() {
    }

    public int getAudioMixerScale() {
        return 0xFFFF;
    }

    public float getAudioSample() {
        return 0;
    }
}
