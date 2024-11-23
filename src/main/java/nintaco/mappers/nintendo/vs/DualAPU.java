package nintaco.mappers.nintendo.vs;

import nintaco.apu.APU;
import nintaco.apu.AudioProcessor;

// VS. DualSystem
public class DualAPU extends APU {

    private static final long serialVersionUID = 0;

    // When SubMonitorFrame is in focus, it affects all instances of DualAPU.
    private static volatile boolean mainUpdateEnabled;
    private final DualAPU dualAPU;

    public DualAPU() {
        this(null);
    }

    public DualAPU(final DualAPU dualAPU) {
        this.dualAPU = dualAPU;
    }

    public static boolean isMainUpdateEnabled() {
        return mainUpdateEnabled;
    }

    public static void setMainUpdateEnabled(final boolean mainUpdateEnabled) {
        DualAPU.mainUpdateEnabled = mainUpdateEnabled;
    }

    @Override
    public void update(final boolean apuCycle) {
        if (dualAPU == null) {
            // Sub APU
            if (!mainUpdateEnabled) {
                super.update(apuCycle);
            }
        } else if (mainUpdateEnabled) {
            // Main APU
            super.update(apuCycle);
        }
    }

    @Override
    public void setAudioProcessor(final AudioProcessor audioProcessor) {
        super.setAudioProcessor(audioProcessor);
        if (dualAPU != null) {
            dualAPU.setAudioProcessor(audioProcessor);
        }
    }

    @Override
    public void reset() {
        super.reset();
        if (dualAPU != null) {
            dualAPU.reset();
        }
    }
}