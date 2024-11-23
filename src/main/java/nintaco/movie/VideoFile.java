package nintaco.movie;

import java.awt.image.*;
import java.io.*;
import java.nio.*;

import org.monte.media.av.*;
import org.monte.media.av.Buffer;
import org.monte.media.math.*;

import static org.monte.media.av.codec.audio.AudioFormatKeys.*;
import static org.monte.media.av.codec.video.VideoFormatKeys.*;
import static nintaco.apu.SystemAudioProcessor.*;

public class VideoFile {

    private final MovieWriter movieWriter;
    private final int videoTrack;
    private final int audioTrack;
    private final Buffer videoBuffer;
    private final Buffer audioBuffer;
    private final double secondsPerFrame;
    private final byte[] audioData;
    private final boolean recordAudio;

    private int videoFrame;
    private int audioFrame;
    private int audioIndex;
    private boolean closed;

    public VideoFile(final String fileName, final boolean avi,
                     final double framesPerSecond, final int width, final int height,
                     final boolean recordAudio) throws Throwable {
        this(new File(fileName), avi, framesPerSecond, width, height, recordAudio);
    }

    public VideoFile(final File file, final boolean avi,
                     final double framesPerSecond, final int width, final int height,
                     final boolean recordAudio) throws Throwable {

        this.secondsPerFrame = 1.0 / framesPerSecond;

        movieWriter = Registry.getInstance().getWriter(new Format(MediaTypeKey,
                FormatKeys.MediaType.FILE, MimeTypeKey,
                avi ? MIME_AVI : MIME_QUICKTIME), file);

        videoTrack = movieWriter.addTrack(new Format(
                MediaTypeKey, MediaType.VIDEO,
                EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                WidthKey, width,
                HeightKey, height,
                DepthKey, 24,
                FrameRateKey, Rational.valueOf(framesPerSecond),
                QualityKey, 1f,
                KeyFrameIntervalKey, (int) Math.round(framesPerSecond * 60)));
        videoBuffer = new Buffer();
        videoBuffer.format = new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey,
                ENCODING_BUFFERED_IMAGE);
        videoBuffer.track = videoTrack;
        videoBuffer.sampleDuration = Rational.valueOf(secondsPerFrame);

        this.recordAudio = recordAudio;
        if (recordAudio) {
            audioBuffer = new Buffer();
            audioBuffer.format = new Format(
                    MediaTypeKey, MediaType.AUDIO,
                    SampleRateKey, Rational.valueOf(OUTPUT_SAMPLING_FREQUENCY),
                    SampleSizeInBitsKey, 16,
                    ByteOrderKey, ByteOrder.LITTLE_ENDIAN);
            audioBuffer.data = audioData
                    = new byte[((int) OUTPUT_SAMPLING_FREQUENCY) << 1];
            audioTrack = movieWriter.addTrack(audioBuffer.format);
            audioBuffer.track = audioTrack;
        } else {
            audioBuffer = null;
            audioTrack = 0;
            audioData = null;
        }
    }

    public synchronized void writeImage(final BufferedImage image)
            throws Throwable {
        if (closed) {
            return;
        }
        videoBuffer.data = image;
        videoBuffer.timeStamp = Rational.valueOf(secondsPerFrame * videoFrame);
        videoBuffer.sequenceNumber = videoFrame++;
        movieWriter.write(videoTrack, videoBuffer);
    }

    public synchronized void writeAudioSample(final int sample) throws Throwable {
        if (closed || !recordAudio) {
            return;
        }
        audioData[audioIndex++] = (byte) (sample & 0xFF);
        audioData[audioIndex++] = (byte) ((sample >> 8) & 0xFF);
        if (audioIndex == audioData.length) {
            audioIndex = 0;
            audioBuffer.length = audioData.length;
            audioBuffer.sampleCount = audioData.length >> 1;
            audioBuffer.sampleDuration = Rational.ONE;
            audioBuffer.timeStamp = Rational.valueOf(audioFrame);
            audioBuffer.sequenceNumber = audioFrame++;
            movieWriter.write(audioTrack, audioBuffer);
        }
    }

    public synchronized void close() throws Throwable {
        if (closed) {
            return;
        }
        closed = true;
        if (recordAudio && audioIndex > 0) {
            audioIndex = 0;
            audioBuffer.length = audioIndex;
            audioBuffer.sampleCount = audioIndex >> 1;
            audioBuffer.sampleDuration = Rational.valueOf(audioIndex,
                    audioBuffer.length);
            audioBuffer.timeStamp = Rational.valueOf(audioFrame);
            audioBuffer.sequenceNumber = audioFrame;
            movieWriter.write(audioTrack, audioBuffer);
        }
        movieWriter.close();
    }
}
