package cn.kinlon.emu.mappers.sunsoft.fme7;

import cn.kinlon.emu.mappers.Audio;
import cn.kinlon.emu.tv.TVSystem;

import static cn.kinlon.emu.mappers.sunsoft.fme7.Emu2149.*;

public final class Sunsoft5BAudio extends Audio {

    private static final long serialVersionUID = 0;

    private static final double RATE = 48000;
    private static final int MIX_RANGE = 0x0800;

    private static float volume;

    static {
        setVolume(100);
    }

    public static void setVolume(final int volume) {
        Sunsoft5BAudio.volume = volume / 100f;
    }

    private PSG psg;
    private float divider;
    private float dividerStep;
    private int soundSample;
    private TVSystem tvSystem;

    @Override
    public void init() {
    }

    @Override
    public void reset() {
        tvSystemChanged();
    }

    public void setTVSystem(final TVSystem tvSystem) {
        if (this.tvSystem != tvSystem) {
            this.tvSystem = tvSystem;
            tvSystemChanged();
        }
    }

    private void tvSystemChanged() {

        divider = 0f;
        soundSample = 0;

        if (tvSystem == null) {
            tvSystem = TVSystem.NTSC;
        }

        final double cyclesPerSecond = tvSystem.cyclesPerSecond();
        dividerStep = (float) (cyclesPerSecond / RATE);
        psg = PSG_new((int) cyclesPerSecond, (int) RATE);

        for (int i = 0; i < 16; i++) { // blank all registers
            writeRegister(0xC000, i);
            writeRegister(0xE000, 0);
        }
        writeRegister(0xC000, 0x07); // disable all tones
        writeRegister(0xE000, 0x3F);

        PSG_set_quality(psg, true);
        PSG_reset(psg);
    }

    @Override
    public boolean writeRegister(final int address, final int value) {
        switch (address & 0xE000) {
            case 0xC000:
                writeAudioRegisterIndex(value);
                return true;
            case 0xE000:
                writeAudioRegisterValue(value);
                return true;
            default:
                return false;
        }
    }

    private void writeAudioRegisterIndex(final int value) {
        PSG_writeIO(psg, 0, value);
    }

    private void writeAudioRegisterValue(final int value) {
        PSG_writeIO(psg, 1, value);
    }

    @Override
    public void update() {
        while (++divider >= dividerStep) {
            divider -= dividerStep;
            soundSample = PSG_calc(psg) << 3;
        }
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