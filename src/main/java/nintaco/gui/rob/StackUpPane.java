package nintaco.gui.rob;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

import static nintaco.gui.rob.RobGame.STACK_UP;

public class StackUpPane extends RobPane {

    public static final Color[] BLOCK_COLORS = {
            new Color(0xFF1F57),
            new Color(0xCCC5CA),
            new Color(0x1D66AD),
            new Color(0xFBB846),
            new Color(0x4EAA6D),
    };

    private static final Color PLATFORM = new Color(0xB2A8A6);
    private static final Color HANDS = new Color(0xA39DB5);

    private static final int[][] BLOCK_POLYGON = {
            {50, 50, -50, -50, -25, -12, 12, 25,},
            {0, -40, -40, 0, 0, 20, 20, 0,},
    };

    private static final boolean[] notDrawn = new boolean[5];

    public StackUpPane() {
        state.game = STACK_UP;
        state.reset();
    }

    private void drawHands(final Graphics2D g) {
        final AffineTransform transform = g.getTransform();
        g.translate(convertX(state.handsX), convertY(state.handsY));
        final int open = (int) (50f + state.handsOpen * 30f);
        g.setColor(Color.BLACK);
        g.fillRect(open, -33, 6, 26);
        g.fillRect(-open - 6, -33, 6, 26);
        g.setColor(HANDS);
        g.fillRect(open + 6, -35, 63, 30);
        g.fillRect(-open - 69, -35, 63, 30);
        g.setTransform(transform);
    }

    private void drawPlatform(final Graphics2D g, final double x) {
        g.setColor(PLATFORM);
        final int X = (int) convertX(x);
        final int Y = (int) convertY(5);
        g.fillRect(X - 63, Y, 126, 6);
        g.fillRect(X - 50, Y, 100, 30);
    }

    private void drawPlatforms(final Graphics2D g) {
        for (int i = 4; i >= 0; i--) {
            drawPlatform(g, i - 2);
        }
    }

    private void drawBlock(final Graphics2D g, final double x, final double y,
                           final Color color) {
        final AffineTransform transform = g.getTransform();
        g.translate(convertX(x), convertY(y));

        g.setColor(color);
        g.fillPolygon(BLOCK_POLYGON[0], BLOCK_POLYGON[1], BLOCK_POLYGON[0].length);

        g.setTransform(transform);
    }

    private void drawBlocks(final Graphics2D g) {
        Arrays.fill(notDrawn, true);
        final float[][] blocks = state.pieces;
        for (int i = 4; i >= 0; i--) {
            float minValue = Float.MAX_VALUE;
            int minIndex = 0;
            for (int j = 4; j >= 0; j--) {
                if (notDrawn[j] && blocks[j][1] < minValue) {
                    minValue = blocks[j][1];
                    minIndex = j;
                }
            }
            drawBlock(g, blocks[minIndex][0], blocks[minIndex][1],
                    BLOCK_COLORS[minIndex]);
            notDrawn[minIndex] = false;
        }
    }

    @Override
    protected void draw(final Graphics2D g) {
        drawROB(g, 630, 10, state.testing);
        drawBlocks(g);
        drawPlatforms(g);
        drawHands(g);
    }
}
