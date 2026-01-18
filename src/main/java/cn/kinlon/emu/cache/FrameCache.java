package cn.kinlon.emu.cache;

import cn.kinlon.emu.gui.userinterface.InitialRamState;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class FrameCache implements WritableFrame, ReadableFrame {

    private final Lock lock = new ReentrantLock();

    private final int[][] cache;
    private int writePosition;
    private int readPosition;
    private int lastWritePosition;

    public FrameCache(int size) {
        size = Math.max(2, size);
        cache = new int[size][];
    }

    public void reset(final int frameWidth, final int frameHeight, final int defaultValue) {
        Arrays.setAll(cache, _ -> IntStream.range(0, frameWidth * frameHeight).map(_ -> defaultValue).toArray());
    }

    @Override
    public int[] getReadFrame() {
        lock.lock();
        try {
            while (readPosition == lastWritePosition) {
                lock.unlock();
                Thread.onSpinWait();
                lock.lock();
            }
            readPosition = lastWritePosition;
            return cache[readPosition];
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int[] getWriteFrame() {
        lock.lock();
        try {
            lastWritePosition = writePosition;
            do {
                writePosition = ++writePosition % cache.length;
            } while (writePosition == readPosition);
            return cache[writePosition];
        } finally {
            lock.unlock();
        }
    }
}
