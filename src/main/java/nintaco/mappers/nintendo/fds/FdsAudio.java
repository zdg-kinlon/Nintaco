package nintaco.mappers.nintendo.fds;

import nintaco.mappers.Audio;

import java.util.Arrays;

import static nintaco.tv.TVSystem.NTSC;
import static nintaco.util.BitUtil.getBitBool;

public final class FdsAudio extends Audio {

    private static final long serialVersionUID = 0;

    private static final double CUTOFF_FREQUENCY = 2000;
    private static final float SMOOTH;
    
    static {
        final double SAMPLE_PERIOD = NTSC.getSecondsPerCycle();
        final double X = 2.0 * Math.PI * SAMPLE_PERIOD * CUTOFF_FREQUENCY;
        SMOOTH = (float) (X / (X + 1));
    }
    
    private static final int FDS_AUDIO_MAX = 0x4F1B;
    private static final int APU_AUDIO_MAX = 0xFFFF - FDS_AUDIO_MAX;
    private static final float INV_SMOOTH = 1f - SMOOTH;
    private static final int[] MOD_ADJUSTMENTS = {0, 1, 2, 4, 0, -4, -2, -1};
    private static final float MAX_OUTPUT = 32 * 63;
    private static final float[] MASTER_VOLUMES = {
            SMOOTH * FDS_AUDIO_MAX / MAX_OUTPUT,
            2 * SMOOTH * FDS_AUDIO_MAX / (3 * MAX_OUTPUT),
            SMOOTH * FDS_AUDIO_MAX / (2 * MAX_OUTPUT),
            2 * SMOOTH * FDS_AUDIO_MAX / (5 * MAX_OUTPUT),
    };
    private static float volume;
    
    static {
        setVolume(100);
    }

    private final int[] modTable = new int[64];
    private final int[] volumeTable = new int[64];
    private float masterVolume;
    private float audioSample;
    private int volumeOutput;
    private int volumeSpeed;
    private int volumeGain;
    private int modSpeed;
    private int modGain;
    private int volumeFrequency;
    private int modCounter;
    private int modFrequency;
    private int envelopeSpeed;
    private int volumeOutputPosition;
    private int volumeTimer;
    private int modOutputPosition;
    private int modTimer;
    private int modAccumulator;
    private int volumeAccumulator;
    private boolean volumeAndSweepEnabled;
    private boolean volumeEnabled;
    private boolean volumeIncrease;
    private boolean volumeGainEnabled;
    private boolean modIncrease;
    private boolean modGainEnabled;
    private boolean volumeTableWriteEnabled;
    private boolean modEnabled;
    private boolean soundIOEnabled; // ignored

    public static void setVolume(final int volume) {
        FdsAudio.volume = volume / 100f;
    }

    @Override
    public void reset() {
        masterVolume = 0f;
        audioSample = 0f;
        volumeOutput = 0;
        volumeSpeed = 0;
        volumeGain = 0;
        modSpeed = 0;
        modGain = 0;
        volumeFrequency = 0;
        modCounter = 0;
        modFrequency = 0;
        envelopeSpeed = 0;
        volumeOutputPosition = 0;
        volumeTimer = 0;
        modOutputPosition = 0;
        modTimer = 0;
        modAccumulator = 0;
        volumeAccumulator = 0;
        volumeAndSweepEnabled = false;
        volumeEnabled = false;
        volumeIncrease = false;
        volumeGainEnabled = false;
        modIncrease = false;
        modGainEnabled = false;
        volumeTableWriteEnabled = false;
        modEnabled = false;
        soundIOEnabled = false;

        Arrays.fill(modTable, 0);
        Arrays.fill(volumeTable, 0);
    }

    @Override
    public boolean writeRegister(final int address, final int value) {

        if ((address & 0xFFC0) == 0x4040) {
            writeVolumeTable(address, value);
            return true;
        }

        switch (address) {
            case 0x4023:
                writeMasterIOEnable(value);
                return false;
            case 0x4080:
                writeVolumeEnvelope(value);
                return true;
            case 0x4082:
                writeVolumeFrequencyLow(value);
                return true;
            case 0x4083:
                writeVolumeFrequencyHigh(value);
                return true;
            case 0x4084:
                writeModEnvelope(value);
                return true;
            case 0x4085:
                writeModCounter(value);
                return true;
            case 0x4086:
                writeModFrequencyLow(value);
                return true;
            case 0x4087:
                writeModFrequencyHigh(value);
                return true;
            case 0x4088:
                writeModTable(value);
                return true;
            case 0x4089:
                writeMasterVolume(value);
                return true;
            case 0x408A:
                writeEnvelopeSpeed(value);
                return true;
            default:
                return false;
        }
    }

    @Override
    public int readRegister(final int address) {
        if (address == 0x4090) {
            return readVolumeGain();
        } else if (address == 0x4092) {
            return readModGain();
        } else if ((address & 0xFFC0) == 0x4040) {
            return readVolumeTable(address);
        } else {
            return -1;
        }
    }

    @Override
    public void update() {
        if (volumeAndSweepEnabled && volumeEnabled && envelopeSpeed != 0) {
            if (volumeGainEnabled && --volumeTimer < 0) {
                tickVolumeUnit();
            }
            if (modGainEnabled && --modTimer < 0) {
                tickModUnit();
            }
        }

        if (modEnabled) {
            modAccumulator += modFrequency;
            while (modAccumulator >= 0x10000) {
                modAccumulator -= 0x10000;
                int modValue = modTable[modOutputPosition];
                modOutputPosition = (modOutputPosition + 1) & 0x3F;
                if (modValue == 4) {
                    modCounter = 0;
                } else {
                    modCounter = ((modCounter + MOD_ADJUSTMENTS[modValue]) << 25) >> 25;
                }
            }
        }

        if (volumeEnabled) {
            int modulatedPitch;
            if (modGain != 0) {
                modulatedPitch = modCounter * modGain;
                int remainder = modulatedPitch & 0x0F;
                modulatedPitch >>= 4;
                if (remainder > 0 && (modulatedPitch & 0x80) == 0) {
                    if (modCounter < 0) {
                        modulatedPitch--;
                    } else {
                        modulatedPitch += 2;
                    }
                }

                modulatedPitch = (((modulatedPitch + 64) & 0xFF) - 64)
                        * volumeFrequency;
                remainder = modulatedPitch & 0x3F;
                modulatedPitch >>= 6;
                if (remainder >= 32) {
                    modulatedPitch++;
                }
            } else {
                modulatedPitch = 0;
            }

            volumeAccumulator += volumeFrequency + modulatedPitch;
            while (volumeAccumulator >= 0x10000) {
                volumeAccumulator -= 0x10000;
                volumeOutputPosition = (volumeOutputPosition + 1) & 0x3F;
            }
        }

        if (!volumeTableWriteEnabled) {
            volumeOutput = volumeGain;
            if (volumeOutput > 32) {
                volumeOutput = 32;
            }
            volumeOutput *= volumeTable[volumeOutputPosition];
        }

        audioSample = volumeOutput * masterVolume + INV_SMOOTH * audioSample;
    }

    private void writeMasterIOEnable(final int value) {
        soundIOEnabled = getBitBool(value, 1);
    }

    private void writeVolumeTable(final int address, final int value) {
        if (volumeTableWriteEnabled) {
            volumeTable[address & 0x003f] = value & 0x3F;
        }
    }

    private int readVolumeTable(final int address) {
        return volumeTable[address & 0x003f];
    }

    private void writeVolumeEnvelope(final int value) {
        volumeSpeed = value & 0x3F;
        volumeIncrease = getBitBool(value, 6);
        volumeGainEnabled = !getBitBool(value, 7);
        resetVolumeTimer();

        if (!volumeGainEnabled) {
            volumeGain = volumeSpeed;
        }
    }

    private void writeVolumeFrequencyLow(final int value) {
        volumeFrequency = (volumeFrequency & 0xF00) | value;
    }

    private void writeVolumeFrequencyHigh(final int value) {
        volumeFrequency = (volumeFrequency & 0x0FF) | ((value & 0x0F) << 8);
        volumeAndSweepEnabled = !getBitBool(value, 6);
        volumeEnabled = !getBitBool(value, 7);

        if (!volumeEnabled) {
            volumeAccumulator = 0;
        }

        if (!volumeAndSweepEnabled) {
            resetVolumeTimer();
            resetModTimer();
        }
    }

    private void writeModEnvelope(final int value) {
        modSpeed = value & 0x3F;
        modIncrease = getBitBool(value, 6);
        modGainEnabled = !getBitBool(value, 7);
        resetModTimer();

        if (!modGainEnabled) {
            modGain = modSpeed;
        }
    }

    private void writeModCounter(final int value) {
        modAccumulator = 0;
        modCounter = ((value & 0x7F) << 25) >> 25;
    }

    private void writeModFrequencyLow(final int value) {
        modFrequency = (modFrequency & 0xF00) | value;
    }

    private void writeModFrequencyHigh(final int value) {
        modFrequency = (modFrequency & 0x0FF) | ((value & 0x0F) << 8);
        modEnabled = !getBitBool(value, 7);
        if (!modEnabled) {
            modAccumulator = 0;
        }
    }

    private void writeModTable(final int value) {
        if (!modEnabled) {
            modTable[(modOutputPosition + 1) & 0x3F]
                    = modTable[modOutputPosition] = value & 0x07;
            modOutputPosition = (modOutputPosition + 2) & 0x3F;
        }
    }

    private void writeMasterVolume(final int value) {
        masterVolume = MASTER_VOLUMES[value & 0x03];
        volumeTableWriteEnabled = getBitBool(value, 7);
    }

    private void writeEnvelopeSpeed(int value) {
        envelopeSpeed = value;
        resetVolumeTimer();
        resetModTimer();
    }

    private int readVolumeGain() {
        return 0x40 | volumeGain;
    }

    private int readModGain() {
        return 0x40 | modGain;
    }

    private void resetVolumeTimer() {
        volumeTimer = ((volumeSpeed + 1) << 3) * envelopeSpeed;
    }

    private void resetModTimer() {
        modTimer = ((modSpeed + 1) << 3) * envelopeSpeed;
    }

    private void tickVolumeUnit() {
        if (volumeIncrease) {
            if (volumeGain < 32) {
                volumeGain++;
            }
        } else {
            if (volumeGain > 0) {
                volumeGain--;
            }
        }
        resetVolumeTimer();
    }

    private void tickModUnit() {
        if (modIncrease) {
            if (modGain < 32) {
                modGain++;
            }
        } else {
            if (modGain > 0) {
                modGain--;
            }
        }
        resetModTimer();
    }

    @Override
    public float getAudioSample() {
        return volume * audioSample;
    }

    @Override
    public int getAudioMixerScale() {
        return APU_AUDIO_MAX;
    }
}