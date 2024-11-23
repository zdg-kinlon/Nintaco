package nintaco.mappers.unif.unl.dripgame;

import nintaco.mappers.*;

public class DripGameAudio extends Audio {

    private static final long serialVersionUID = 0;

    private final DripGameAudioChannel[] channels = new DripGameAudioChannel[2];

    public DripGameAudio() {
        for (int i = channels.length - 1; i >= 0; i--) {
            channels[i] = new DripGameAudioChannel();
        }
    }

    @Override
    public void reset() {
        channels[0].reset();
        channels[1].reset();
    }

    public int getStatus(final int channel) {
        return channels[channel].getStatus();
    }

    @Override
    public boolean writeRegister(final int address, final int value) {
        switch (address & 0xF00F) {
            case 0x8000:
                channels[0].silence();
                return true;
            case 0x8001:
                channels[0].enqueue(value);
                return true;
            case 0x8002:
                channels[0].setLowPeriod(value);
                return true;
            case 0x8003:
                channels[0].setHighPeriod(value);
                return true;
            case 0x8004:
                channels[1].silence();
                return true;
            case 0x8005:
                channels[1].enqueue(value);
                return true;
            case 0x8006:
                channels[1].setLowPeriod(value);
                return true;
            case 0x8007:
                channels[1].setHighPeriod(value);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void update() {
        channels[0].update();
        channels[1].update();
    }

    @Override
    public int getAudioMixerScale() {
        return 0;
    }

    @Override
    public float getAudioSample() {
        return 8.56666f * (channels[0].getSample() + channels[1].getSample());
    }
}