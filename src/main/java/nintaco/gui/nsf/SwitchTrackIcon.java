package nintaco.gui.nsf;

import nintaco.gui.VectorIcon;

import java.awt.*;

public class SwitchTrackIcon extends VectorIcon {

    public SwitchTrackIcon(final boolean flippedHorizontally) {
        super(1.0, flippedHorizontally, false);
    }

    @Override
    protected void paintIcon(final boolean enabled, final Graphics2D g) {
        renderPath(enabled, g, PlayIcon.Path, 0, 0);
        renderPath(enabled, g, PauseIcon.Path, 17, 0);
    }

    @Override
    protected double getPaintWidth() {
        return 23;
    }

    @Override
    protected double getPaintHeight() {
        return 20;
    }
}
