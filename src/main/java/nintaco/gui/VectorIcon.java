package nintaco.gui;

import nintaco.preferences.AppPrefs;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.lang.Math.ceil;

public abstract class VectorIcon implements Icon {

    private static final int SIZE = 16;

    private final double sizeScale;
    private final boolean flippedHorizontally;
    private final boolean flippedVertically;

    public VectorIcon() {
        this(1.0, false, false);
    }

    public VectorIcon(final double sizeScale) {
        this(sizeScale, false, false);
    }

    public VectorIcon(final double sizeScale, final boolean flippedHorizontally,
                      final boolean flippedVertically) {
        this.sizeScale = sizeScale;
        this.flippedHorizontally = flippedHorizontally;
        this.flippedVertically = flippedVertically;
    }

    @Override
    public void paintIcon(final Component c, final Graphics G, final int x,
                          final int y) {

        final Graphics2D g = (Graphics2D) G;
        final RenderingHints renderingHints = g.getRenderingHints();
        final AffineTransform transform = g.getTransform();

        final double paintWidth = getPaintWidth();
        final double paintHeight = getPaintHeight();

        final double scale;
        final double side = getIconWidth();
        double sx;
        double sy;
        double tx;
        double ty;
        if (paintHeight > paintWidth) {
            scale = side / paintHeight;
            tx = x + (side - paintWidth * scale) / 2.0;
            ty = y;
        } else {
            scale = side / paintWidth;
            tx = x;
            ty = y + (side - paintHeight * scale) / 2.0;
        }
        if (flippedHorizontally) {
            sx = -scale;
            tx += side;
        } else {
            sx = scale;
        }
        if (flippedVertically) {
            sy = -scale;
            ty += side;
        } else {
            sy = scale;
        }

        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g.translate(tx, ty);
        g.scale(sx, sy);

        paintIcon(c.isEnabled(), g);

        g.setTransform(transform);
        g.setRenderingHints(renderingHints);
    }

    protected abstract double getPaintWidth();

    protected abstract double getPaintHeight();

    protected abstract void paintIcon(boolean enabled, Graphics2D g);

    protected void renderPath(final boolean enabled, final Graphics2D g,
                              final Path2D.Double path, final double x, final double y) {
        final AffineTransform transform = g.getTransform();
        g.translate(x, y);
        if (enabled) {
            g.setColor(Color.WHITE);
            g.fill(path);
        }
        g.setColor(enabled ? Color.BLACK : Color.GRAY);
        g.draw(path);
        g.setTransform(transform);
    }

    @Override
    public int getIconWidth() {
        return (int) ceil(SIZE * sizeScale
                * AppPrefs.getInstance().getView().getFontScale());
    }

    @Override
    public int getIconHeight() {
        return getIconWidth();
    }
}
