package nintaco.gui.rob;

import java.awt.*;
import java.awt.geom.AffineTransform;

import static nintaco.gui.rob.RobGame.GYROMITE;

//Gyromite:
//
//R 000101 110 1010
//O 000101 110 1110
//C 000101 011 1110
//L 000101 011 1010
//U 000101 011 1011
//D 000101 111 1011
//
//T 000101 110 1011 1111 1
//
//R 6A
//O 6E
//C 3E
//L 3A
//U 3B
//D 7B
//T 6B F
//
//Stack up:
//
//C 000101 011 1110
//L 000101 011 1010
//R 000101 110 1010
//U 000101 111 1010
//D 000101 010 1110
//O 000101 110 1110
//T 000101 110 1011 0000 0
//
//C 3E
//O 6E
//R 6A
//L 3A
//U 7A
//D 2E
//T 6B 0

public class GyromitePane extends RobPane {

    private static final Color GYRO_SHAFT = new Color(0xB4B6A9);
    private static final Color GYRO_BASE = new Color(0xD7D8CD);
    private static final Color GYRO_HOLDER = Color.BLACK;
    private static final Color A_BUTTON = new Color(0x1E9AFF);
    private static final Color B_BUTTON = new Color(0xFF2A16);
    private static final Color HANDS = new Color(0x7A707E);

    private static final int[][] GYRO_POLYGON = {
            {0, 11, 16, 16, 18, 18, -18, -18, -16, -16, -11,},
            {0, 5, 44, 149, 149, 152, 152, 149, 149, 44, 5,},
    };
    private static final int[][] GYRO_HOLDER_POLYGON = {
            {0, 10, 18, 66, 66, -66, -66, -18, -10,},
            {0, 0, 76, 76, 83, 83, 76, 76, 0},
    };
    private static final int[][] LEFT_HAND_POLYGON = {
            {-76, -16, -16, 16, 16, -16, -16, -76,},
            {-149, -149, -136, -132, -126, -122, -109, -109,},
    };
    private static final int[][] RIGHT_HAND_POLYGON = {
            {16, -16, -16, 16, 16, -16, -16, 16, 76, 76,},
            {-109, -115, -122, -126, -132, -136, -142, -149, -149, -109,},
    };

    public GyromitePane() {
        state.game = GYROMITE;
        state.reset();
    }

    @Override
    protected double convertY(final double y) {
        return 343.0 + 40.0 * y;
    }

    private void drawHands(final Graphics2D g, double x, double y, double open) {
        x = convertX(x);
        y = convertY(y);
        open *= 40.0;
        final AffineTransform transform = g.getTransform();
        g.setColor(HANDS);
        g.translate(x - open, y);
        g.fillPolygon(LEFT_HAND_POLYGON[0], LEFT_HAND_POLYGON[1],
                LEFT_HAND_POLYGON[0].length);
        g.setTransform(transform);
        g.translate(x + open, y);
        g.fillPolygon(RIGHT_HAND_POLYGON[0], RIGHT_HAND_POLYGON[1],
                RIGHT_HAND_POLYGON[0].length);
        g.setTransform(transform);
    }

    private void drawButton(final Graphics2D g, final double x,
                            final double y, final Color color) {
        final AffineTransform transform = g.getTransform();
        g.translate(convertX(x), convertY(y));
        g.setColor(color);
        g.fillRect(-42, -5, 84, 25);
        g.setTransform(transform);
    }

    private void drawSpinner(final Graphics2D g, final double x,
                             final double y) {
        final AffineTransform transform = g.getTransform();
        g.translate(x, y + 34);
        g.setColor(GYRO_HOLDER);
        g.fillRect(-48, 0, 96, 12);
        g.fillRect(-40, 12, 80, 4);
        g.setColor(GYRO_SHAFT);
        g.fillRect(-48, 16, 96, 82);
        g.fillRect(-56, 82, 112, 81);
        g.setTransform(transform);
    }

    private void drawGyroHolder(final Graphics2D g, final double x,
                                final double y) {
        final AffineTransform transform = g.getTransform();
        g.translate(x, y + 117);
        g.scale(1, -1);
        g.setColor(GYRO_HOLDER);
        g.fillPolygon(GYRO_HOLDER_POLYGON[0], GYRO_HOLDER_POLYGON[1],
                GYRO_HOLDER_POLYGON[0].length);
        g.setTransform(transform);
    }

    private void drawGyro(final Graphics2D g, final double x, final double y) {
        final AffineTransform transform = g.getTransform();
        g.translate(convertX(x), convertY(y));
        g.scale(1, -1);
        g.setColor(GYRO_SHAFT);
        g.fillPolygon(GYRO_POLYGON[0], GYRO_POLYGON[1], GYRO_POLYGON[0].length);
        g.setColor(GYRO_BASE);
        g.fillRect(-111, 44, 222, 25);
        g.setTransform(transform);
    }

    @Override
    protected void draw(final Graphics2D g) {
        drawROB(g, 630, 10, state.testing);
        for (int i = 2; i >= 0; i--) {
            drawGyro(g, state.pieces[i][0], state.pieces[i][1]);
        }
        drawGyroHolder(g, 170, 425.0);
        drawGyroHolder(g, 400, 425.0);
        drawSpinner(g, 1090.0, 345.0);
        drawButton(g, 0, state.aButtonY, A_BUTTON);
        drawButton(g, 1, state.bButtonY, B_BUTTON);
        drawHands(g, state.handsX, state.handsY, state.handsOpen);
    }
}
