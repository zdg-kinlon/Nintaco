package nintaco.movie;

import nintaco.apu.*;
import nintaco.gui.rob.*;

import static nintaco.gui.image.ImagePane.*;

public class MovieFrame implements AudioProcessor {

    public final int[] screen = new int[IMAGE_WIDTH * IMAGE_HEIGHT];
    public final int[] screen2;
    public final int[] audioSamples = new int[2048];
    public final RobState robState = new RobState();

    public int audioLength;
    public int frameIndex = -1;

    public MovieFrame(final boolean vsDualSystem) {
        screen2 = vsDualSystem ? new int[IMAGE_WIDTH * IMAGE_HEIGHT] : null;
    }

    public boolean isVsDualSystem() {
        return screen2 != null;
    }

    @Override
    public void processOutputSample(final int value) {
        if (audioLength < audioSamples.length) {
            audioSamples[audioLength++] = value;
        }
    }
}
