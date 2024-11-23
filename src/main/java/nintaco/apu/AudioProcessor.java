package nintaco.apu;

@FunctionalInterface
public interface AudioProcessor {
    void processOutputSample(int value);
}
