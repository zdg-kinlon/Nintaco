package cn.kinlon.emu.gui.nsf;

import cn.kinlon.emu.gui.VectorIcon;

import java.awt.*;
import java.awt.geom.Path2D;

public class PauseIcon extends VectorIcon {

    public static final Path2D.Double Path = new Path2D.Double();

    static {
        Path.moveTo(0, 0);
        Path.lineTo(5, 0);
        Path.lineTo(5, 20);
        Path.lineTo(0, 20);
        Path.closePath();
    }

    public PauseIcon() {
        super(1.5);
    }

    @Override
    protected void paintIcon(final boolean enabled, final Graphics2D g) {
        renderPath(enabled, g, Path, 0, 0);
        renderPath(enabled, g, Path, 9, 0);
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
