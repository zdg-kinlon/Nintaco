package cn.kinlon.emu.mappers.konami.vrc7;

import cn.kinlon.emu.mappers.Audio;

import static cn.kinlon.emu.mappers.konami.vrc7.Emu2413.*;
import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public final class VRC7Audio extends Audio {

    private static final long serialVersionUID = 0;

    private static final int MIX_RANGE = 0x7FFF;
    private static final int AUDIO_SCALE = MIX_RANGE >> 9;

    private static float volume;

    static {
        setVolume(100);
    }

    private boolean soundEnabled;
    private int soundSample;
    private OPLL opll;
    private int divider; // clock divider

    public static void setVolume(final int volume) {
        VRC7Audio.volume = volume / 100f;
    }

    @Override
    public void init() {
        OPLL_init();
        opll = OPLL_new();
        reset();
    }

    @Override
    public void reset() {
        soundSample = 0;
        divider = 0;
        for (int i = 0; i < 0x40; i++) {
            OPLL_writeIO(opll, 0, i);
            OPLL_writeIO(opll, 1, 0);
        }
        OPLL_reset_patch(opll);
        OPLL_reset(opll);
    }

    @Override
    public boolean writeRegister(final int address, final int value) {
        switch (address) {
            case 0x9010:
                OPLL_writeIO(opll, 0, value);
                return true;
            case 0x9030:
                OPLL_writeIO(opll, 1, value);
                return true;
            case 0xE000:
                soundEnabled = !getBitBool(value, 6);
                return false;
            default:
                return false;
        }
    }

    @Override
    public void update() {
        while (++divider >= 36) {
            divider -= 36;
            OPLL_calc(opll);
        }

        soundSample = 0;
        for (int i = 0; i < 6; i++) {
            soundSample += opll.slot[(i << 1) | 1].output[1];
        }
        soundSample = 0x8000 + soundSample * AUDIO_SCALE;
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
