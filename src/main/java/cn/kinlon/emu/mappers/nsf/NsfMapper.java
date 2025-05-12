package cn.kinlon.emu.mappers.nsf;

import cn.kinlon.emu.files.NsfFile;
import cn.kinlon.emu.gui.nsf.NsfPrefs;
import cn.kinlon.emu.mappers.Audio;
import cn.kinlon.emu.mappers.Mapper;
import cn.kinlon.emu.mappers.konami.vrc6.VRC6Audio;
import cn.kinlon.emu.mappers.konami.vrc7.VRC7Audio;
import cn.kinlon.emu.mappers.namco.Namco163Audio;
import cn.kinlon.emu.mappers.nintendo.fds.FdsAudio;
import cn.kinlon.emu.mappers.nintendo.mmc5.MMC5Audio;
import cn.kinlon.emu.mappers.sunsoft.fme7.Sunsoft5BAudio;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.tv.TVSystem;

import java.util.Arrays;

import static java.lang.Math.max;
import static java.lang.Math.round;

public class NsfMapper extends Mapper {

    public static final int DEFAULT_FADE_SECONDS = 8;
    private static final long serialVersionUID = 0;
    private static final int STATE_NOT_PLAYING = 0;
    private static final int STATE_SONG_REQUESTED = 1;
    private static final int STATE_PLAYING_SONG = 2;

    private final int chipCount;
    private final int audioMixerScale;
    private final Audio[] audios;
    private final FdsAudio fdsAudio;
    private final MMC5Audio mmc5Audio;
    private final Sunsoft5BAudio sunsoft5BAudio;

    private int state = STATE_NOT_PLAYING;
    private int requestedSong;
    private boolean songPaused;

    private volatile boolean automaticallyAdvanceTrack;
    private volatile int silenceSeconds;
    private volatile boolean defaultTrackLength;
    private volatile int trackLengthMinutes;

    private volatile long songCpuCycles;
    private boolean fixedLengthTrack;
    private long trackCycles = Long.MAX_VALUE;
    private long fadeCycles;
    private float volume = 1;
    private float deltaVolume;

    private transient NsfFile nsfFile;

    public NsfMapper(final NsfFile nsfFile) {
        super(nsfFile);
        this.nsfFile = nsfFile;

        final NsfPrefs prefs = AppPrefs.getInstance().getNsfPrefs();
        automaticallyAdvanceTrack = prefs.isAutomaticallyAdvanceTrack();
        silenceSeconds = prefs.getSilenceSeconds();
        defaultTrackLength = prefs.isDefaultTrackLength();
        trackLengthMinutes = prefs.getTrackLengthMinutes();

        chipCount = nsfFile.getChipCount();
        audios = new Audio[chipCount];

        int i = 0;
        if (nsfFile.usesFdsAudio()) {
            audios[i++] = fdsAudio = new FdsAudio();
        } else {
            fdsAudio = null;
        }
        if (nsfFile.usesMMC5Audio()) {
            audios[i++] = mmc5Audio = new MMC5Audio(getTVSystem());
        } else {
            mmc5Audio = null;
        }
        if (nsfFile.usesNamco163Audio()) {
            audios[i++] = new Namco163Audio();
        }
        if (nsfFile.usesSunsoft5BAudio()) {
            audios[i++] = sunsoft5BAudio = new Sunsoft5BAudio();
        } else {
            sunsoft5BAudio = null;
        }
        if (nsfFile.usesVRC6Audio()) {
            audios[i++] = new VRC6Audio();
        }
        if (nsfFile.usesVRC7Audio()) {
            audios[i++] = new VRC7Audio();
        }

        int sum = 0;
        for (i = chipCount - 1; i >= 0; i--) {
            audios[i].init();
            sum += audios[i].getAudioMixerScale();
        }
        audioMixerScale = chipCount == 0 ? 0xFFFF : (sum / chipCount);

        requestSong(nsfFile.getStartingSong());
    }

    @Override
    public void setTVSystem(final TVSystem tvSystem) {
        super.setTVSystem(tvSystem);
        if (mmc5Audio != null) {
            mmc5Audio.setTVSystem(tvSystem);
        }
        if (sunsoft5BAudio != null) {
            sunsoft5BAudio.setTVSystem(tvSystem);
        }
    }

    @Override
    public void setNsfOptions(final boolean automaticallyAdvanceTrack,
                              final int idleSeconds, final boolean defaultTrackLength,
                              final int trackLengthMinutes) {
        this.automaticallyAdvanceTrack = automaticallyAdvanceTrack;
        this.silenceSeconds = idleSeconds;
        this.defaultTrackLength = defaultTrackLength;
        this.trackLengthMinutes = trackLengthMinutes;
    }

    @Override
    public boolean isNsfMapper() {
        return true;
    }

    public boolean isSongPaused() {
        return songPaused;
    }

    @Override
    public void setSongPaused(final boolean songPaused) {
        this.songPaused = songPaused;
        apu.setFadeVolume(songPaused ? 0 : volume);
        if (!songPaused) {
            apu.clearInactiveSeconds();
        }
    }

    @Override
    public void requestSong(final int songNumber) {
        requestedSong = songNumber;
        trackCycles = Long.MAX_VALUE;
        songCpuCycles = 0L;
        volume = 1f;
        state = STATE_SONG_REQUESTED;
    }

    public int getRequestedSong() {
        return requestedSong;
    }

    private void initSong(final int songNumber) {
        initBanks();
        for (int i = 0x4013; i >= 0x4000; i--) {
            writeCpuMemory(i, 0x00);
        }
        writeCpuMemory(0x4015, 0x0F);
        writeCpuMemory(0x4017, 0x40);
        for (int i = chipCount - 1; i >= 0; i--) {
            audios[i].reset();
        }
        reg.sp(0xFF);
        reg.a(songNumber);
        reg.x(ntsc ? 0 : 1);
    }

    private void initBanks() {
        Arrays.fill(memory, 0x00);
        if (nsfFile.isBankSwitched()) {
            final int[] initBanks = nsfFile.getInitBanks();
            for (int i = 7; i >= 0; i--) {
                setPrgBank(i + 8, initBanks[i]);
            }
            if (fdsAudio != null) {
                setPrgBank(6, initBanks[6]);
                setPrgBank(7, initBanks[7]);
            }
        } else {
            for (int bank = nsfFile.getLoadAddress() >> 12, value = 0; bank < 16;
                 bank++, value++) {
                setPrgBank(bank, value);
            }
        }
    }

    public void jumpSubroutine(final int address) {
        memory[0x01FE] = 0xFC;
        memory[0x01FF] = 0x4F;
        reg.sp(0xFD);
        reg.i(true);
        reg.pc(address);
    }

    @Override
    public int readCpuMemory(int address) {
        switch (address) {
            case 0x4FFD:
                return 0x4C;
            case 0x4FFE:
                return 0xFD;
            case 0x4FFF:
                return 0x4F;
            default:
                return super.readCpuMemory(address);
        }
    }

    @Override
    public int readMemory(final int address) {
        if (mmc5Audio != null && address >= 0x6000) {
            mmc5Audio.updatePcmValue(address, memory[address]);
        }
        for (int i = audios.length - 1; i >= 0; i--) {
            final int value = audios[i].readRegister(address);
            if (value >= 0) {
                return value;
            }
        }
        switch (address) {
            case 0xFFFC:
                return 0xFD;
            case 0xFFFD:
                return 0x4F;
            default:
                return memory[address];
        }
    }

    @Override
    public void setPrgBank(final int bank, final int value) {
        final int address = bank << 12;
        final int offset = value << 12;
        if (offset >= prgROM.length) {
            Arrays.fill(memory, address, address + 0x1000, 0);
        } else {
            System.arraycopy(prgROM, offset, memory, address, 0x1000);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (address < 0x8000 || (fdsAudio != null && address < 0xE000)) {
            memory[address] = value;
        }
        if (address >= 0x5FF6 && address < 0x6000) {
            setPrgBank(address & 0x000F, value);
        }
        for (int i = audios.length - 1; i >= 0; i--) {
            if (audios[i].writeRegister(address, value)) {
                break;
            }
        }
    }

    @Override
    public void handleFrameRendered() {
        if (reg.pc() == 0x4FFD) {
            switch (state) {
                case STATE_SONG_REQUESTED: {
                    volume = 1f;
                    songCpuCycles = 0L;
                    fixedLengthTrack = false;
                    apu.setFadeVolume(1);
                    apu.clearInactiveSeconds();
                    initSong(requestedSong);
                    jumpSubroutine(nsfFile.getInitAddress());

                    final double cyclesPerMilli = tvSystem.getCyclesPerSecond() / 1000.0;
                    final long fadeMillis = nsfFile.getFadeMillis()[requestedSong];
                    if (fadeMillis >= 0) {
                        fadeCycles = max(1, round(fadeMillis * cyclesPerMilli));
                    } else {
                        fadeCycles = (long) (DEFAULT_FADE_SECONDS
                                * tvSystem.getCyclesPerSecond());
                    }
                    final long trackMillis = nsfFile.getTrackMillis()[requestedSong];
                    if (trackMillis >= 0) {
                        fixedLengthTrack = true;
                        trackCycles = max(1, round(trackMillis * cyclesPerMilli));
                    } else if (defaultTrackLength) {
                        trackCycles = max(1, round(60 * trackLengthMinutes
                                * tvSystem.getCyclesPerSecond()) - fadeCycles);
                    } else {
                        trackCycles = Long.MAX_VALUE;
                    }
                    volume = 1f;
                    songCpuCycles = 0L;
                    deltaVolume = 1f / fadeCycles;
                    apu.setFadeVolume(1);
                    apu.clearInactiveSeconds();

                    state = STATE_PLAYING_SONG;
                    break;
                }
                case STATE_PLAYING_SONG:
                    if (!songPaused) {
                        jumpSubroutine(nsfFile.getPlayAddress());
                    }
                    break;
            }
        }
    }

    @Override
    public void restore(final NsfFile nsfFile) {
        super.restore(nsfFile);
        this.nsfFile = nsfFile;
    }

    @Override
    public void update() {
        if (!songPaused) {
            for (int i = chipCount - 1; i >= 0; i--) {
                audios[i].update();
            }
            if (state == STATE_PLAYING_SONG) {
                songCpuCycles++;
                if (trackCycles == 0) {
                    if (fadeCycles > 0) {
                        fadeCycles--;
                        volume -= deltaVolume;
                        apu.setFadeVolume(volume);
                    } else {
                        volume = 0;
                        apu.setFadeVolume(0);
                    }
                } else {
                    trackCycles--;
                }
            }
        }
    }

    public boolean isAudioActive() {
        if (songPaused || !automaticallyAdvanceTrack) {
            return true;
        } else if (state == STATE_PLAYING_SONG) {
            if (trackCycles <= 0 && fadeCycles <= 0) {
                return false;
            } else if (fixedLengthTrack) {
                return true;
            } else {
                return apu.getInactiveSeconds() < silenceSeconds;
            }
        } else {
            return true;
        }
    }

    public long getSongCpuCycles() {
        return songCpuCycles;
    }

    @Override
    public int getAudioMixerScale() {
        return audioMixerScale;
    }

    @Override
    public float getAudioSample() {
        switch (chipCount) {
            case 0:
                return 0;
            case 1:
                return audios[0].getAudioSample();
            default:
                int sum = 0;
                for (int i = chipCount - 1; i >= 0; i--) {
                    sum += audios[i].getAudioSample();
                }
                return sum / chipCount;
        }
    }
}
