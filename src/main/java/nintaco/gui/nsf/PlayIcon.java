package nintaco.gui.nsf;

import nintaco.gui.VectorIcon;

import java.awt.*;
import java.awt.geom.Path2D;

public class PlayIcon extends VectorIcon {

    public static final Path2D.Double Path = new Path2D.Double();

    static {
        Path.moveTo(0, 0);
        Path.lineTo(14, 10);
        Path.lineTo(0, 20);
        Path.closePath();
    }

    public PlayIcon() {
        super(1.5);
    }

    @Override
    public void paintIcon(final boolean enabled, final Graphics2D g) {
        renderPath(enabled, g, Path, 0, 0);
    }

    @Override
    protected double getPaintWidth() {
        return 15;
    }

    @Override
    protected double getPaintHeight() {
        return 20;
    }
}
