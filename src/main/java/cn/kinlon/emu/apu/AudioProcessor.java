package cn.kinlon.emu.apu;

@FunctionalInterface
public interface AudioProcessor {
    void processOutputSample(int value);
}
