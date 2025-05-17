package cn.kinlon.emu.apu;

import cn.kinlon.emu.gui.sound.SoundPrefs;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.utils.ThreadUtil;
import org.monte.media.av.Movie;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Math.clamp;
import static java.lang.Math.log10;
import static cn.kinlon.emu.utils.StringUtil.*;

public class SystemAudioProcessor implements AudioProcessor {

    public static final double OUTPUT_SAMPLING_FREQUENCY = 48000.0;
    public static final int FLUSH_LATENCY = 5;
    public static final int LINE_BUFFER_SIZE = 0x20000;
    public static final AudioFormat AUDIO_FORMAT = new AudioFormat(
            (float) OUTPUT_SAMPLING_FREQUENCY, 16, 1, true, true);
    private static final String[] DEFAULT_DEVICE_KEYWORDS
            = {"primary", "default", "main"};
    private static final ExecutorService flushExecutor = Executors
            .newSingleThreadExecutor();
    private static AudioFileFormat.Type[] supportedAudioFileTypes;
    private static SourceDataLine sourceDataLine;
    private static FloatControl masterGainControl;
    private static int masterVolume;
    private static byte[] buffer;
    private static byte[] flushBuffer;
    private static int bufferSize;
    private static int flushSize;
    private static int overrun;
    private static int underrun;
    private static int lineBufferSize;
    private static int bufferIndex;
    private static int lastSample;
    private static volatile Movie movie;

    public static void init() {
        final AppPrefs appPrefs = AppPrefs.getInstance();
        masterVolume = appPrefs.getVolumeMixerPrefs().getMasterVolume();
        applySoundPrefs(appPrefs.getSoundPrefs());
    }

    public static void applySoundPrefs(final SoundPrefs prefs) {
        disposeLine();
        final String[] audioDevices = getAudioDevices();
        final String audioDevice = prefs.getAudioDevice();
        int deviceIndex = -1;
        if (!isBlank(audioDevice)) {
            deviceIndex = findMatch(audioDevices, audioDevice);
        }
        if (deviceIndex < 0) {
            deviceIndex = getDefaultAudioDevice(audioDevices);
        }
        if (deviceIndex > 0) {
            try {
                final Mixer.Info info = getMixerInfo(audioDevices[deviceIndex]);
                if (info != null) {
                    sourceDataLine = getSourceDataLine(info);
                    if (sourceDataLine != null) {
                        sourceDataLine.open(AUDIO_FORMAT, LINE_BUFFER_SIZE);
                        masterGainControl = (FloatControl) sourceDataLine.getControl(
                                FloatControl.Type.MASTER_GAIN);
                        setMasterVolume(masterVolume);
                        sourceDataLine.start();
                    }
                }
            } catch (final Throwable t) {
                //t.printStackTrace();
            }
        }

        lineBufferSize = sourceDataLine.getBufferSize();
        bufferSize = computeBufferLength(prefs.getLatencyMillis());
        flushSize = computeBufferLength(FLUSH_LATENCY);
        buffer = new byte[bufferSize];
        overrun = lineBufferSize - bufferSize - (flushSize << 2);
        underrun = lineBufferSize - flushSize;
        bufferIndex = 0;
        flushBuffer = new byte[flushSize];
    }

    private static int computeBufferLength(final int milliseconds) {
        return ((int) Math.round(OUTPUT_SAMPLING_FREQUENCY * milliseconds / 1000.0))
                << 1;
    }

    private static void disposeLine() {
        try {
            final SourceDataLine line = sourceDataLine;
            sourceDataLine = null;
            masterGainControl = null;
            if (line != null) {
                flush(line, lineBufferSize, true);
            }
        } catch (final Throwable t) {
        }
    }

    private static Mixer.Info getMixerInfo(final String name) {
        for (final Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (info.getName().equalsIgnoreCase(name)) {
                return info;
            }
        }
        return null;
    }

    public static String[] getAudioDevices() {
        final List<String> devices = new ArrayList<>();
        devices.add("None");
        try {
            for (final Mixer.Info info : AudioSystem.getMixerInfo()) {
                if (isLineAssignable(info)) {
                    devices.add(info.getName());
                }
            }
        } catch (final Throwable t) {
        }
        return devices.toArray(new String[devices.size()]);
    }

    public static int getDefaultAudioDevice(final String[] audioDevices) {
        for (final String keyword : DEFAULT_DEVICE_KEYWORDS) {
            final int index = findContaining(audioDevices, keyword, 1,
                    audioDevices.length - 1);
            if (index >= 0) {
                return index;
            }
        }
        return audioDevices.length >= 2 ? 1 : 0;
    }

    private static boolean isLineAssignable(final Mixer.Info info) {
        return getSourceDataLine(info) != null;
    }

    private static SourceDataLine getSourceDataLine(final Mixer.Info info) {
        try {
            return (SourceDataLine) AudioSystem.getMixer(info).getLine(
                    new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT));
        } catch (final Throwable t) {
        }
        return null;
    }

    public static int getMasterVolume() {
        return masterVolume;
    }

    public static void setMasterVolume(final int volume) {
        masterVolume = volume;
        try {
            final FloatControl masterGain = masterGainControl;
            if (masterGain != null) {
                masterGain.setValue((float) clamp(20.0 * log10(volume / 100.0),
                        masterGain.getMinimum(), masterGain.getMaximum()));
            }
        } catch (final Throwable t) {
        }
    }

    public static void setMovie(final Movie movie) {
        SystemAudioProcessor.movie = movie;
    }

    public static void flush() {
        flush(sourceDataLine, lineBufferSize, false);
    }

    private static void flush(final SourceDataLine sourceDataLine,
                              final int lineBufferSize, final boolean close) {

        try {
            if (sourceDataLine != null) {
                float value = lastSample;
                bufferIndex = 0;
                lastSample = 0;
                float dv = -value / (flushBuffer.length >> 1);
                for (int i = 0; i < flushBuffer.length; i += 2) {
                    value += dv;
                    final int v = (int) value;
                    flushBuffer[i] = (byte) (v >> 8);
                    flushBuffer[i | 1] = (byte) v;
                }
                sourceDataLine.write(flushBuffer, 0, flushBuffer.length);
                flushExecutor.submit(new FlushThread(sourceDataLine, lineBufferSize,
                        close));
            }
        } catch (final Throwable t) {
        }
    }

    @Override
    public void processOutputSample(final int value) {

        lastSample = value;

        final SourceDataLine line = sourceDataLine;
        if (line != null) {

            buffer[bufferIndex] = (byte) (value >> 8);
            buffer[bufferIndex + 1] = (byte) value;
            bufferIndex += 2;

            if ((bufferIndex == flushSize && line.available() < underrun)
                    || bufferIndex == bufferSize) {
                if (line.available() < overrun) {
//          System.out.println("-- audio buffer overrun --");
                } else {
                    if (line.available() == lineBufferSize) {
//            System.out.println("-- audio buffer underrun --");
                        float warmup = 0;
                        final float dw = 1f / (bufferIndex >> 1);
                        for (int i = 0; i < bufferIndex; i += 2) {
                            warmup += dw;
                            final int v = (int) (warmup * (short) (((buffer[i] & 0xFF) << 8)
                                    | (buffer[i | 1] & 0xFF)));
                            buffer[i] = (byte) (v >> 8);
                            buffer[i | 1] = (byte) v;
                        }
                    }
                    line.write(buffer, 0, bufferIndex);
                }
                bufferIndex = 0;
            }
        }
    }

    private record FlushThread(SourceDataLine sourceDataLine, int lineBufferSize, boolean close) implements Runnable {

        @Override
            public void run() {
                try {
                    if (sourceDataLine != null && lineBufferSize > 0) {
                        for (int i = 0; i < 1000; i++) {
                            if (sourceDataLine.available() == lineBufferSize) {
                                break;
                            } else {
                                ThreadUtil.sleep(1);
                            }
                        }
                        if (close) {
                            sourceDataLine.stop();
                            sourceDataLine.flush();
                            sourceDataLine.close();
                        } else {
                            sourceDataLine.flush();
                        }
                    }
                } catch (final Throwable t) {
                    //t.printStackTrace();
                }
            }
        }
}
