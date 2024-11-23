package nintaco.gui.image.filters;

import java.util.Arrays;

public final class Retro3x extends VideoFilter {

    private static final int[][] GAUSSIAN = {
            {1, 2, 1},
            {2, 4, 2},
            {1, 2, 1},
    };

    private static final int[][] PRECEDENCE = {
            {1, 1}, {0, 1}, {1, 2}, {2, 0},
            {1, 1}, {2, 1}, {1, 0}, {2, 2},
            {1, 1}, {0, 1}, {1, 2}, {0, 2},
            {1, 1}, {2, 1}, {1, 0}, {0, 0},
    };

    private static final int[][][] BLURS = new int[256][3][3];

    static {
        initBlurs();
    }

    private final int[] SCREEN;
    private final int[] RANDOM_VALUES = new int[3001];
    private final int[] row = new int[256];
    private int randomIndex;
    private int jag;
    private boolean ghosting = true;
    public Retro3x() {
        super(3);
        SCREEN = new int[256 * 240];
        initRandomValues();
    }
    public Retro3x(final Retro3x retro3x) {
        super(retro3x.getImage(), retro3x.getImageData());
        this.SCREEN = retro3x.SCREEN;
        initRandomValues();
    }

    private static void initBlurs() {
        for (int i = 255; i >= 0; i--) {
            distributeRemainder(distributeIntensity(9 * i, BLURS[i]), BLURS[i]);
        }
    }

    private static int distributeIntensity(final int intensity,
                                           final int[][] blur) {
        int sum = 0;
        for (int i = 2; i >= 0; i--) {
            for (int j = 2; j >= 0; j--) {
                blur[i][j] = (intensity * GAUSSIAN[i][j]) >> 4;
                if (blur[i][j] > 255) {
                    blur[i][j] = 255;
                }
                sum += blur[i][j];
            }
        }
        return intensity - sum;
    }

    private static void distributeRemainder(int remainder, final int[][] blur) {
        while (remainder > 0) {
            for (int i = 0; i < PRECEDENCE.length; i++) {
                final int[] p = PRECEDENCE[i];
                if (blur[p[1]][p[0]] < 255) {
                    blur[p[1]][p[0]]++;
                    if (--remainder == 0) {
                        return;
                    }
                }
            }
        }
    }

    private static void scale(final int[] in, final int[] out, final int yFirst,
                              final int yLast, final int[][][] blurs, final int[] screen,
                              final int[] randomValues, int rand, final int[] row,
                              final int jag, final boolean ghosting) {

        for (int y = yLast - 1; y >= yFirst; --y) {
            final int ys = y << 8;
            final int Y0 = 2304 * y;
            final int Y1 = Y0 + 768;
            final int Y2 = Y1 + 768;
            row[0] = in[ys];
            for (int x = 1, i = (y + jag) % 3; x < 256; ++x, i = i == 2 ? 0 : i + 1) {
                final int yx = ys + x;
                final int p1 = in[yx - 1];
                final int p2 = in[yx];
                if (p1 == p2) {
                    row[x] = p2;
                } else {
                    final int r1 = (p1 >> 16) & 0xFF;
                    final int g1 = (p1 >> 8) & 0xFF;
                    final int b1 = p1 & 0xFF;
                    final int r2 = (p2 >> 16) & 0xFF;
                    final int g2 = (p2 >> 8) & 0xFF;
                    final int b2 = p2 & 0xFF;
                    final int r;
                    final int g;
                    final int b;
                    switch (i) {
                        case 0:
                            r = (r1 + 3 * r2) >> 2;
                            g = g2;
                            b = b2;
                            break;
                        case 1:
                            r = r2;
                            g = (g1 + 3 * g2) >> 2;
                            b = b2;
                            break;
                        default:
                            r = r2;
                            g = g2;
                            b = (b1 + 3 * b2) >> 2;
                            break;
                    }
                    row[x] = (r << 16) | (g << 8) | b;
                }
            }
            final int[] pixels;
            final int offset;
            if (ghosting) {
                pixels = screen;
                offset = ys;
                for (int x = 255; x >= 0; x--) {
                    final int yx = ys + x;
                    int r = ((3 * ((screen[yx] >> 16) & 0xFF)
                            + 5 * ((row[x] >> 16) & 0xFF)) >> 3) + randomValues[rand];
                    if (++rand == 3001) {
                        rand = 0;
                    }
                    if (r < 0) {
                        r = 0;
                    } else if (r > 255) {
                        r = 255;
                    }
                    int g = ((3 * ((screen[yx] >> 8) & 0xFF)
                            + 5 * ((row[x] >> 8) & 0xFF)) >> 3) + randomValues[rand];
                    if (++rand == 3001) {
                        rand = 0;
                    }
                    if (g < 0) {
                        g = 0;
                    } else if (g > 255) {
                        g = 255;
                    }
                    int b = ((3 * (screen[yx] & 0xFF)
                            + 5 * (row[x] & 0xFF)) >> 3) + randomValues[rand];
                    if (++rand == 3001) {
                        rand = 0;
                    }
                    if (b < 0) {
                        b = 0;
                    } else if (b > 255) {
                        b = 255;
                    }
                    screen[yx] = (r << 16) | (g << 8) | b;
                }
            } else {
                pixels = row;
                offset = 0;
            }
            for (int x = 255; x >= 0; x--) {
                final int yx = offset + x;
                final int x3 = x * 3;
                final int y0 = Y0 + x3;
                final int y1 = Y1 + x3;
                final int y2 = Y2 + x3;
                final int p = pixels[yx];
                final int[][] R = blurs[(p >> 16) & 0xFF];
                final int[][] G = blurs[(p >> 8) & 0xFF];
                final int[][] B = blurs[p & 0xFF];

                out[y0] = (R[0][1] << 16) | (G[0][0] << 8);
                out[y0 + 1] = (R[0][2] << 16) | (G[0][1] << 8) | B[0][0];
                out[y0 + 2] = (G[0][2] << 8) | B[0][1];

                out[y1] = (R[1][1] << 16) | (G[1][0] << 8);
                out[y1 + 1] = (R[1][2] << 16) | (G[1][1] << 8) | B[1][0];
                out[y1 + 2] = (G[1][2] << 8) | B[1][1];

                out[y2] = (R[2][1] << 16) | (G[2][0] << 8);
                out[y2 + 1] = (R[2][2] << 16) | (G[2][1] << 8) | B[2][0];
                out[y2 + 2] = (G[2][2] << 8) | B[2][1];

                if (x != 0) {
                    final int[][] b = blurs[pixels[yx - 1] & 0xFF];
                    out[y0] |= b[0][2];
                    out[y1] |= b[1][2];
                    out[y2] |= b[2][2];
                }

                if (x != 255) {
                    final int[][] r = blurs[(pixels[yx + 1] >> 16) & 0xFF];
                    out[y0 + 2] |= r[0][0] << 16;
                    out[y1 + 2] |= r[1][0] << 16;
                    out[y2 + 2] |= r[2][0] << 16;
                }
            }
        }
    }

    public void setGhosting(final boolean ghosting) {
        this.ghosting = ghosting;
    }

    private void initRandomValues() {
        for (int i = RANDOM_VALUES.length - 1; i >= 0; --i) {
            RANDOM_VALUES[i] = (int) (13 * Math.random()) - 6;
        }
    }

    @Override
    public void reset() {
        super.reset();
        Arrays.fill(SCREEN, 0);
    }

    @Override
    public void filter(final int[] in, final int yFirst,
                       final int yLast) {
        scale(in, out, yFirst, yLast, BLURS, SCREEN, RANDOM_VALUES, randomIndex,
                row, jag, ghosting);
        randomIndex = (randomIndex + 61440) % 3001;
        ++jag;
    }
}
