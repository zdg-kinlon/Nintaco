package nintaco.gui.rob;

import nintaco.util.EDT;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;

import static java.awt.RenderingHints.*;

public abstract class RobPane extends JComponent {

    private static final int IDEAL_WIDTH = 1260;
    private static final int IDEAL_HEIGHT = 554;

    private static final Color BACKGROUND = Color.WHITE;
    private static final Color ROB_FACE = Color.BLACK;
    private static final Color ROB_LINES = new Color(0xAA8B90);
    private static final Color ROB_EYES = new Color(0x971836);
    private static final Color ROB_DARK_EYES = new Color(0x811626);
    private static final Color ROB_REFLECTION = new Color(0xE0A9A6);
    private static final Color ROB_LIT_EYES = new Color(0xFF7F7F);
    private static final Color ROB_LIT_DARK_EYES = Color.WHITE;
    private static final Color ROB_HIGHLIGHT = new Color(0xEDDEE5);

    private static final Stroke STROKE = new BasicStroke(2f);

    private static final int[][][] HIGHLIGHT_POLYGONS = {
            {{5, 7, 20, 13,},
                    {3, 2, 4, 8,},},
            {{97, 95, 82, 89,},
                    {3, 2, 4, 8,},},
            {{5, 7, 20, 13,},
                    {41, 42, 41, 37,},},
            {{97, 95, 82, 89,},
                    {41, 42, 41, 37,},},
    };

    protected final RobState state = new RobState();

    protected double translateX;
    protected double translateY;
    protected double scale;
    protected int paneWidth;
    protected int paneHeight;

    public RobPane() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                paneResized();
            }
        });
        setPreferredSize(new Dimension(IDEAL_WIDTH >> 1, IDEAL_HEIGHT >> 1));
    }

    private void paneResized() {
        paneWidth = getWidth();
        paneHeight = getHeight();
        if (IDEAL_HEIGHT * paneWidth >= IDEAL_WIDTH * paneHeight) {
            final double width = paneHeight * IDEAL_WIDTH
                    / (double) IDEAL_HEIGHT;
            translateX = (paneWidth - width) / 2.0;
            translateY = 0.0;
            scale = width / IDEAL_WIDTH;
        } else {
            final double height = paneWidth * IDEAL_HEIGHT / IDEAL_WIDTH;
            translateX = 0.0;
            translateY = (paneHeight - height) / 2.0;
            scale = height / IDEAL_HEIGHT;
        }
        repaint();
    }

    protected double convertX(final double x) {
        return 630.0 + 230.0 * x;
    }

    protected double convertY(final double y) {
        return 310.0 + 40.0 * y;
    }

    public void render(final RobState state) {
        if (this.state.modifications != state.modifications) {
            this.state.init(state);
            EDT.async(this::repaint);
        }
    }

    protected void drawROB(final Graphics2D g, final double x, final double y,
                           final boolean lit) {
        final AffineTransform transform = g.getTransform();
        g.translate(x - 102, y);
        g.scale(2, 2);
        g.setColor(ROB_FACE);
        g.fillRoundRect(0, 0, 102, 44, 18, 18);
        g.setColor(ROB_HIGHLIGHT);
        for (int i = HIGHLIGHT_POLYGONS.length - 1; i >= 0; i--) {
            g.fillPolygon(HIGHLIGHT_POLYGONS[i][0], HIGHLIGHT_POLYGONS[i][1], 4);
        }
        g.setColor(ROB_LINES);
        g.fillRoundRect(9, 5, 84, 35, 35, 35);
        g.setColor(ROB_FACE);
        for (int i = 4; i >= 0; i--) {
            g.fillRect(28, 11 + 5 * i, 48, 4);
        }
        g.setColor(lit ? ROB_LIT_EYES : ROB_EYES);
        g.fillOval(12, 7, 30, 30);
        g.fillOval(60, 7, 30, 30);
        g.setColor(lit ? ROB_LIT_DARK_EYES : ROB_DARK_EYES);
        g.fillOval(17, 12, 20, 20);
        g.fillOval(65, 12, 20, 20);
        g.setColor(ROB_FACE);
        g.drawOval(12, 7, 30, 30);
        g.drawOval(60, 7, 30, 30);
        g.setColor(ROB_LINES);
        g.drawOval(11, 6, 32, 32);
        g.drawOval(59, 6, 32, 32);
        if (!lit) {
            g.setColor(ROB_REFLECTION);
            g.fillRoundRect(21, 16, 12, 12, 6, 6);
            g.fillRoundRect(69, 16, 12, 12, 6, 6);
        }
        g.setTransform(transform);
    }

    @Override
    protected void paintComponent(final Graphics G) {
        final Graphics2D g = (Graphics2D) G;
        final AffineTransform transform = g.getTransform();
        final RenderingHints hints = g.getRenderingHints();
        final Stroke stroke = g.getStroke();
        g.setStroke(STROKE);
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);

        g.setColor(BACKGROUND);
        g.fillRect(0, 0, paneWidth, paneHeight);

        g.translate(translateX, translateY);
        g.scale(scale, scale);

        draw(g);

        g.setTransform(transform);
        g.setRenderingHints(hints);
        g.setStroke(stroke);
    }

    protected abstract void draw(final Graphics2D g);
}
