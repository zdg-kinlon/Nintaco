package cn.kinlon.emu.mappers.namco;

import cn.kinlon.emu.mappers.Audio;

import java.util.Arrays;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public final class Namco163Audio extends Audio {

    private static final long serialVersionUID = 0;

    private static final int MIX_RANGE = 5 * 8438;

    private static float volume;

    static {
        setVolume(100);
    }

    private final int[] soundRAM = new int[0x80];
    private final int[] outputs = new int[8];
    private int soundSample;
    private int soundCpuCycleCount;
    private int soundChannelIndex;
    private int soundAddress;
    private int enabledChannels = 1;
    private boolean soundEnabled;
    private boolean incrementSoundAddress;

    public static void setVolume(final int volume) {
        Namco163Audio.volume = volume / 100f;
    }

    @Override
    public void reset() {
        soundSample = 0;
        soundCpuCycleCount = 0;
        soundChannelIndex = 0;
        soundAddress = 0;
        enabledChannels = 1;
        soundEnabled = false;
        incrementSoundAddress = false;

        Arrays.fill(soundRAM, 0);
        Arrays.fill(outputs, 0);
    }

    @Override
    public int readRegister(final int address) {
        return (address & 0xF800) == 0x4800 ? readSoundData() : -1;
    }

    @Override
    public boolean writeRegister(final int address, final int value) {
        switch (address & 0xF800) {
            case 0x4800:
                writeSoundData(value);
                break;
            case 0xE000:
                soundEnabled = !getBitBool(value, 6);
                break;
            case 0xF800:
                soundAddress = value & 0x7F;
                incrementSoundAddress = getBitBool(value, 7);
                break;
        }
        return false;
    }

    private void writeSoundData(final int value) {
        soundRAM[soundAddress] = value;
        if (soundAddress == 0x7F) {
            enabledChannels = ((value >> 4) & 0x07) + 1;
        }
        if (incrementSoundAddress) {
            soundAddress = (soundAddress + 1) & 0x7F;
        }
    }

    private void updateSoundChannel() {
        final int addr = 0x40 + (soundChannelIndex << 3);
        final int freq = ((soundRAM[addr + 4] & 0x03) << 16)
                | (soundRAM[addr + 2] << 8) | soundRAM[addr];
        int phase = (soundRAM[addr + 5] << 16) | (soundRAM[addr + 3] << 8)
                | soundRAM[addr + 1];
        final int length = 256 - (soundRAM[addr + 4] & 0xFC);
        final int offset = soundRAM[addr + 6];
        final int volume = soundRAM[addr + 7] & 0x0F;

        phase = (phase + freq) % (length << 16);
        outputs[soundChannelIndex] = (8 - sample(((phase >> 16) + offset) & 0xFF))
                * volume;

        // sample - 8   ==>  [-8,7]
        // [-8,7] * 15  ==>  [-120,105]

        // sample ==> [0,15]
        // [0, 15] * 15 ==> [0,225]

        soundRAM[addr + 1] = phase & 0xFF;
        soundRAM[addr + 3] = (phase >> 8) & 0xFF;
        soundRAM[addr + 5] = (phase >> 16) & 0xFF;

        soundSample = 0;
        for (int i = 8 - enabledChannels; i < 8; i++) {
            soundSample += outputs[i];
        }

        soundSample = 0x8000 + (soundSample * MIX_RANGE)
                / (225 * (enabledChannels <= 6 ? enabledChannels : 6));
    }

    private int sample(final int x) {
        return (soundRAM[x >> 1] >> ((x & 1) << 2)) & 0x0F;
    }

    private int readSoundData() {
        final int value = soundRAM[soundAddress];
        if (incrementSoundAddress) {
            soundAddress = (soundAddress + 1) & 0x7F;
        }
        return value;
    }

    @Override
    public void update() {
        if (++soundCpuCycleCount == 15) {
            soundCpuCycleCount = 0;
            if (--soundChannelIndex < 8 - enabledChannels) {
                soundChannelIndex = 7;
            }
            updateSoundChannel();
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    @Override
    public int getAudioMixerScale() {
        return 0xFFFF - MIX_RANGE;
    }

    @Override
    public float getAudioSample() {
        return volume * soundSample;
    }
}