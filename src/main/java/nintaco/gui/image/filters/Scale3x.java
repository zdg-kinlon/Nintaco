package nintaco.gui.image.filters;

public final class Scale3x extends VideoFilter {

    public Scale3x() {
        super(3);
    }

    private static void scale(final int[] in, final int[] out,
                              final int yFirst, final int yLast) {

        for (int y = yLast - 1; y >= yFirst; y--) {
            final int ys = y << 8;
            final int ym = y == 0 ? 0 : (y - 1) << 8;
            final int yp = y == 239 ? 61184 : (y + 1) << 8;
            final int Y0 = 2304 * y;
            final int Y1 = Y0 + 768;
            final int Y2 = Y1 + 768;
            for (int x = 255; x >= 0; x--) {
                final int x2 = 3 * x;
                final int y0 = Y0 + x2;
                final int y1 = Y1 + x2;
                final int y2 = Y2 + x2;
                final int xm = x == 0 ? 0 : x - 1;
                final int xp = x == 255 ? 255 : x + 1;
                final int a = in[ym + xm];
                final int b = in[ym + x];
                final int c = in[ym + xp];
                final int d = in[ys + xm];
                final int e = in[ys + x];
                final int f = in[ys + xp];
                final int g = in[yp + xm];
                final int h = in[yp + x];
                final int i = in[yp + xp];
                if (b != h && d != f) {
                    out[y0] = d == b ? d : e;
                    out[y0 + 1] = (d == b && e != c) || (b == f && e != a) ? b : e;
                    out[y0 + 2] = b == f ? f : e;
                    out[y1] = (d == b && e != g) || (d == h && e != a) ? d : e;
                    out[y1 + 1] = e;
                    out[y1 + 2] = (b == f && e != i) || (h == f && e != c) ? f : e;
                    out[y2] = d == h ? d : e;
                    out[y2 + 1] = (d == h && e != i) || (h == f && e != g) ? h : e;
                    out[y2 + 2] = h == f ? f : e;
                } else {
                    out[y0] = out[y0 + 1] = out[y0 + 2] = out[y1] = out[y1 + 1]
                            = out[y1 + 2] = out[y2] = out[y2 + 1] = out[y2 + 2] = e;
                }
            }
        }
    }

    @Override
    public void filter(final int[] in, final int yFirst, final int yLast) {
        scale(in, out, yFirst, yLast);
    }
}
