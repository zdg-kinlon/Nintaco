package nintaco.apu;

import nintaco.CPU;
import nintaco.Machine;
import nintaco.gui.sound.volumemixer.VolumeMixerPrefs;
import nintaco.mappers.Mapper;
import nintaco.mappers.konami.vrc6.VRC6Audio;
import nintaco.mappers.konami.vrc7.VRC7Audio;
import nintaco.mappers.namco.Namco163Audio;
import nintaco.mappers.nintendo.fds.FdsAudio;
import nintaco.mappers.nintendo.mmc5.MMC5Audio;
import nintaco.mappers.sunsoft.fme7.Sunsoft5BAudio;
import nintaco.preferences.AppPrefs;
import nintaco.tv.TVSystem;

import java.io.Serializable;

import static nintaco.tv.TVSystem.PAL;
import static nintaco.util.BitUtil.getBitBool;
import static nintaco.util.MathUtil.isOdd;

public class APU implements Serializable {

    public static final int REG_APU_PULSE1_ENVELOPE = 0x4000;
    public static final int REG_APU_PULSE1_SWEEP = 0x4001;
    public static final int REG_APU_PULSE1_TIMER_RELOAD_LOW = 0x4002;
    public static final int REG_APU_PULSE1_TIMER_RELOAD_HIGH = 0x4003;
    public static final int REG_APU_PULSE2_ENVELOPE = 0x4004;
    public static final int REG_APU_PULSE2_SWEEP = 0x4005;
    public static final int REG_APU_PULSE2_TIMER_RELOAD_LOW = 0x4006;
    public static final int REG_APU_PULSE2_TIMER_RELOAD_HIGH = 0x4007;
    public static final int REG_APU_TRIANGLE_LINEAR_COUNTER = 0x4008;
    public static final int REG_APU_TRIANGLE_TIMER_RELOAD_LOW = 0x400A;
    public static final int REG_APU_TRIANGLE_TIMER_RELOAD_HIGH = 0x400B;
    public static final int REG_APU_NOISE_ENVELOPE = 0x400C;
    public static final int REG_APU_NOISE_MODE_AND_PERIOD = 0x400E;
    public static final int REG_APU_NOISE_LENGTH_COUNTER = 0x400F;
    public static final int REG_APU_DMC_FLAGS_AND_FREQUENCY = 0x4010;
    public static final int REG_APU_DMC_DIRECT_LOAD = 0x4011;
    public static final int REG_APU_DMC_SAMPLE_ADDRESS = 0x4012;
    public static final int REG_APU_DMC_SAMPLE_LENGTH = 0x4013;
    public static final int REG_APU_STATUS = 0x4015;
    public static final int REG_APU_FRAME_COUNTER = 0x4017;
    public static final int[] lengths = {
            10, 254, 20, 2, 40, 4, 80, 6, 160, 8, 60, 10, 14, 12, 26, 14,
            12, 16, 24, 18, 48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30};
    private static final long serialVersionUID = 0;
    private static boolean enabled;
    private static boolean soundEnabled;
    private static boolean normalSpeed;
    private static boolean smoothDMC;
    private static float pulse1Volume;
    private static float pulse2Volume;
    private static float triangleVolume;
    private static float noiseVolume;
    private static float dmcVolume;
    private static int masterVolume;

    static {
        setSoundEnabled(true);
        setNormalSpeed(true);
        setSmoothDMC(true);
        setMasterVolume(100);
        setPulse1Volume(100);
        setPulse2Volume(100);
        setTriangleVolume(100);
        setNoiseVolume(100);
        setDmcVolume(100);
    }

    public final PulseGenerator pulse1 = new PulseGenerator(true);
    public final PulseGenerator pulse2 = new PulseGenerator(false);
    public final NoiseGenerator noise = new NoiseGenerator();
    public final TriangleGenerator triangle = new TriangleGenerator();
    public final DeltaModulationChannel dmc = new DeltaModulationChannel();
    private CPU cpu;
    private boolean fiveStep;
    private boolean irqEnabled;
    private int timer;
    private int writeDelay;
    private int quarterFrameDelay;
    private int halfFrameDelay;
    private int irqDelay;
    private Mapper mapper;
    private Decimator decimator;
    private boolean pal;
    private transient AudioProcessor audioProcessor;

    private static void updateEnabled() {
        enabled = soundEnabled && normalSpeed && masterVolume > 0;
    }

    public static void setMasterVolume(final int masterVolume) {
        APU.masterVolume = masterVolume;
        updateEnabled();
    }

    public static boolean isSoundEnabled() {
        return APU.soundEnabled;
    }

    public static void setSoundEnabled(final boolean soundEnabled) {
        APU.soundEnabled = soundEnabled;
        updateEnabled();
    }

    public static void setNormalSpeed(final boolean normalSpeed) {
        APU.normalSpeed = normalSpeed;
        updateEnabled();
    }

    public static void setSmoothDMC(final boolean smoothDMC) {
        APU.smoothDMC = smoothDMC;
    }

    public static void setPulse1Volume(final int pulse1Volume) {
        APU.pulse1Volume = pulse1Volume / 100f;
    }

    public static void setPulse2Volume(final int pulse2Volume) {
        APU.pulse2Volume = pulse2Volume / 100f;
    }

    public static void setTriangleVolume(final int triangleVolume) {
        APU.triangleVolume = 2.75167f * triangleVolume / 100f;
    }

    public static void setNoiseVolume(final int noiseVolume) {
        APU.noiseVolume = 1.84936f * noiseVolume / 100f;
    }

    public static void setDmcVolume(final int dmcVolume) {
        APU.dmcVolume = dmcVolume / 100f;
    }

    public static void setVolumeMixerPrefs(final VolumeMixerPrefs prefs) {
        setMasterVolume(prefs.getMasterVolume());
        setSoundEnabled(prefs.isSoundEnabled());
        setSmoothDMC(prefs.isSmoothDMC());
        SystemAudioProcessor.setMasterVolume(prefs.getMasterVolume());
        setPulse1Volume(prefs.getSquare1Volume());
        setPulse2Volume(prefs.getSquare2Volume());
        setTriangleVolume(prefs.getTriangleVolume());
        setNoiseVolume(prefs.getNoiseVolume());
        setDmcVolume(prefs.getDmcVolume());
        VRC6Audio.setVolume(prefs.getVrc6Volume());
        VRC7Audio.setVolume(prefs.getVrc7Volume());
        Namco163Audio.setVolume(prefs.getN163Volume());
        FdsAudio.setVolume(prefs.getFdsVolume());
        MMC5Audio.setVolume(prefs.getMmc5Volume());
        Sunsoft5BAudio.setVolume(prefs.getS5bVolume());
    }

    public static void init() {
        SystemAudioProcessor.init();
        setVolumeMixerPrefs(AppPrefs.getInstance().getVolumeMixerPrefs());
    }

    public void reset() {
        irqEnabled = false;
        cpu.setApuIrq(false);
        writeStatus(0);
    }

    public void setMachine(final Machine machine) {
        this.cpu = machine.getCPU();
        this.mapper = machine.getMapper();
    }

    public void setCPU(final CPU cpu) {
        this.cpu = cpu;
    }

    public Mapper getMapper() {
        return mapper;
    }

    public void setMapper(final Mapper mapper) {
        this.mapper = mapper;
    }

    public DeltaModulationChannel getDMC() {
        return dmc;
    }

    public void clearInactiveSeconds() {
        decimator.clearInactiveSeconds();
    }

    public int getInactiveSeconds() {
        return decimator.getInactiveSeconds();
    }

    public void setTVSystem(final TVSystem tvSystem) {

        this.pal = tvSystem == PAL;
        decimator = new Decimator(tvSystem,
                SystemAudioProcessor.OUTPUT_SAMPLING_FREQUENCY);
        decimator.setAudioProcessor(audioProcessor);

        noise.setPAL(pal);
        dmc.setPAL(pal);
    }

    public void setAudioProcessor(final AudioProcessor audioProcessor) {
        this.audioProcessor = audioProcessor;
        if (decimator != null) {
            decimator.setAudioProcessor(audioProcessor);
        }
    }

    public void setFadeVolume(final float volume) {
        decimator.setVolume(volume);
    }

    public void writeFrameCounter(final int value) {

        fiveStep = getBitBool(value, 7);
        irqEnabled = !getBitBool(value, 6);
        writeDelay = isOdd(cpu.getCycleCounter()) ? 3 : 2;

        if (!irqEnabled) {
            cpu.setApuIrq(false);
        }
    }

    public void writeStatus(final int value) {
        dmc.setEnabled(getBitBool(value, 4));
        noise.setEnabled(getBitBool(value, 3));
        triangle.setEnabled(getBitBool(value, 2));
        pulse2.setEnabled(getBitBool(value, 1));
        pulse1.setEnabled(getBitBool(value, 0));
    }

    public int readStatus() {
        final int value = peekStatus();
        cpu.setApuIrq(false);
        return value;
    }

    public int peekStatus() {
        int value = 0;
        if (cpu.getDmcIrq()) {
            value |= 0x80;
        }
        if (cpu.getApuIrq()) {
            value |= 0x40;
        }
        if (dmc.getBytesRemaining() != 0) {
            value |= 0x10;
        }
        if (noise.getLengthCounter() != 0) {
            value |= 0x08;
        }
        if (triangle.getLengthCounter() != 0) {
            value |= 0x04;
        }
        if (pulse2.getLengthCounter() != 0) {
            value |= 0x02;
        }
        if (pulse1.getLengthCounter() != 0) {
            value |= 0x01;
        }
        return value;
    }

    public void update(final boolean apuCycle) {

        if (apuCycle) {
            pulse1.update();
            pulse2.update();
            noise.update();
            dmc.update();
        }
        triangle.update();

        if (pal) {
            switch (timer) {
                case 8312:
                    quarterFrameDelay = 2;
                    break;
                case 16626:
                    quarterFrameDelay = 2;
                    halfFrameDelay = 2;
                    break;
                case 24938:
                    quarterFrameDelay = 2;
                    break;
                case 33252:
                    if (!fiveStep) {
                        quarterFrameDelay = 2;
                        halfFrameDelay = 2;
                        irqDelay = 3;
                        timer = -2;
                    }
                    break;
                case 41560:
                    quarterFrameDelay = 2;
                    halfFrameDelay = 2;
                    timer = -2;
                    break;
            }
        } else {
            switch (timer) {
                case 7456:
                    quarterFrameDelay = 2;
                    break;
                case 14912:
                    quarterFrameDelay = 2;
                    halfFrameDelay = 2;
                    break;
                case 22370:
                    quarterFrameDelay = 2;
                    break;
                case 29828:
                    if (!fiveStep) {
                        quarterFrameDelay = 2;
                        halfFrameDelay = 2;
                        irqDelay = 3;
                        timer = -2;
                    }
                    break;
                case 37280:
                    quarterFrameDelay = 2;
                    halfFrameDelay = 2;
                    timer = -2;
                    break;
            }
        }

        timer++;

        if (quarterFrameDelay > 0 && --quarterFrameDelay == 0) {
            updateQuarterFrame();
        }
        if (halfFrameDelay > 0 && --halfFrameDelay == 0) {
            updateHalfFrame();
        }
        if (irqDelay > 0) {
            irqDelay--;
            if (!fiveStep && irqEnabled) {
                cpu.setApuIrq(true);
            }
        }
        if (writeDelay > 0 && --writeDelay == 0) {
            if (fiveStep) {
                quarterFrameDelay = 2;
                halfFrameDelay = 2;
            }
            timer = 0;
        }

        if (enabled) {
            final float pulse = pulse1Volume * pulse1.getValue() + pulse2Volume
                    * pulse2.getValue();
            int dmcValue = dmc.getOutputLevel();
            if (smoothDMC) {
                dmcValue += dmc.getSmoothLevel();
                if (dmcValue < 0) {
                    dmcValue = 0;
                } else if (dmcValue > 127) {
                    dmcValue = 127;
                }
            }
            decimator.addInputSample(mapper.getAudioMixerScale() * ((0.9588f * pulse)
                    / (pulse + 81.28f) - 361.733f / (triangleVolume * triangle.getValue()
                    + noiseVolume * noise.getValue() + dmcVolume * dmcValue
                    + 226.38f) + 1.5979f) + mapper.getAudioSample() - 0x8000);
        }
    }

    private void updateQuarterFrame() {
        pulse1.updateEnvelopeGenerator();
        pulse2.updateEnvelopeGenerator();
        noise.updateEnvelopeGenerator();
        triangle.updateLinearCounter();
    }

    private void updateHalfFrame() {
        pulse1.updateLengthCounterAndSweepGenerator();
        pulse2.updateLengthCounterAndSweepGenerator();
        noise.updateLengthCounter();
        triangle.updateLengthCounter();
    }
}