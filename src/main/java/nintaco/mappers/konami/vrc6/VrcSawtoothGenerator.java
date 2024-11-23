package nintaco.mappers.konami.vrc6;

public class VrcSawtoothGenerator extends VrcChannel {

    private static final long serialVersionUID = 0;

    private int accumulator;
    private int accumulatorRate;
    private int accumulatorClocks;
    private boolean clockAccumulator;

    public void reset() {
        accumulator = 0;
        accumulatorRate = 0;
        accumulatorClocks = 0;
        clockAccumulator = false;
    }

    public void writeAccumulatorRate(int value) {
        accumulatorRate = 0x3F & value;
    }

    public void update() {
        if (runOscillator) {
            if (frequency == 0) {
                frequency = (frequencyReload >> frequencyShift);
                if (clockAccumulator && enabled) {
                    if (++accumulatorClocks == 7) {
                        accumulatorClocks = accumulator = 0;
                    } else {
                        accumulator = 0xFF & (accumulator + accumulatorRate);
                    }
                    outputLevel = accumulator >> 3;
                }
                clockAccumulator = !clockAccumulator;
            } else {
                frequency--;
            }
        }
    }

    @Override
    public void writeFrequencyHigh(int value) {
        super.writeFrequencyHigh(value);
        if (!enabled) {
            outputLevel = accumulator = 0;
        }
    }
}
