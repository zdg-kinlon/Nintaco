package cn.kinlon.emu.gui.image.filters;

public final class Scale4x extends VideoFilter {

    private final int[] temp;

    public Scale4x() {
        super(4);
        temp = new int[512 * 480];
    }

    private static void scale(final int[] in, final int[] out, final int[] temp,
                              int yFirst, int yLast) {

        yLast--;
        for (int y = yLast; y >= yFirst; y--) {
            final int ys = y << 8;
            final int ym = y == 0 ? 0 : (y - 1) << 8;
            final int yp = y == 239 ? 61184 : (y + 1) << 8;
            final int Y0 = y << 10;
            final int Y1 = Y0 + 512;
            for (int x = 255; x >= 0; x--) {
                final int x2 = x << 1;
                final int y0 = Y0 | x2;
                final int y1 = Y1 | x2;
                final int xm = x == 0 ? 0 : x - 1;
                final int xp = x == 255 ? 255 : x + 1;
                final int b = in[ym | x];
                final int d = in[ys | xm];
                final int e = in[ys | x];
                final int f = in[ys | xp];
                final int h = in[yp | x];
                if (b != h && d != f) {
                    temp[y0] = d == b ? d : e;
                    temp[y0 | 1] = b == f ? f : e;
                    temp[y1] = d == h ? d : e;
                    temp[y1 | 1] = h == f ? f : e;
                } else {
                    temp[y0] = temp[y0 | 1] = temp[y1] = temp[y1 | 1] = e;
                }
            }
        }

        yFirst <<= 1;
        for (int y = (yLast << 1) + 1; y >= yFirst; y--) {
            final int ys = y << 9;
            final int ym = y == 0 ? 0 : (y - 1) << 9;
            final int yp = y == 479 ? 245248 : (y + 1) << 9;
            final int Y0 = y << 11;
            final int Y1 = Y0 + 1024;
            for (int x = 511; x >= 0; x--) {
                final int x2 = x << 1;
                final int y0 = Y0 | x2;
                final int y1 = Y1 | x2;
                final int xm = x == 0 ? 0 : x - 1;
                final int xp = x == 511 ? 511 : x + 1;
                final int b = temp[ym | x];
                final int d = temp[ys | xm];
                final int e = temp[ys | x];
                final int f = temp[ys | xp];
                final int h = temp[yp | x];
                if (b != h && d != f) {
                    out[y0] = d == b ? d : e;
                    out[y0 | 1] = b == f ? f : e;
                    out[y1] = d == h ? d : e;
                    out[y1 | 1] = h == f ? f : e;
                } else {
                    out[y0] = out[y0 | 1] = out[y1] = out[y1 | 1] = e;
                }
            }
        }
    }

    @Override
    public void filter(final int[] in, final int yFirst, final int yLast) {
        scale(in, out, temp, yFirst, yLast);
    }
}
