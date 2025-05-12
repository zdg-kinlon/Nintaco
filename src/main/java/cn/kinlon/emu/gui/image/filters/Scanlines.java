package cn.kinlon.emu.gui.image.filters;

import static java.lang.Math.*;

public final class Scanlines extends VideoFilter {

    private final int SCALE;
    private final int W;
    private final int[][] kernels;
    private final float I_SCALE;

    public Scanlines(final int scale) {
        super(scale);
        SCALE = scale;
        I_SCALE = 1f / SCALE;
        W = SCALE << 8;
        kernels = new int[256][SCALE];

        for (int i = 255; i >= 0; --i) {
            double sum = 0.0;
            final double e = 1.0 - 0.95 * pow(i / 255.0, 0.5);
            for (int j = SCALE - 1; j >= 0; --j) {
                sum += i * pow((1 + cos(PI * (2 * ((0.5 + j) / SCALE) - 1))) / 2, e);
            }
            final double intensity = i * i * SCALE / sum;
            final double E = 1.0 - 0.95 * pow(intensity / 255.0, 0.5);
            final int[] kernel = kernels[i];
            for (int j = SCALE - 1; j >= 0; --j) {
                kernel[j] = min(255, (int) round(intensity * pow((1 + cos(PI
                        * (2 * ((0.5 + j) / SCALE) - 1))) / 2, E)));
            }
        }
    }

    public Scanlines(final Scanlines scanlines) {
        super(scanlines.getImage(), scanlines.getImageData());
        SCALE = scanlines.SCALE;
        W = scanlines.W;
        kernels = scanlines.kernels;
        I_SCALE = scanlines.I_SCALE;
    }

    @Override
    public void filter(final int[] in, final int yFirst, final int yLast) {

        final int[] out = this.out;

        for (int o = W * (SCALE * yLast - 1), first = W * SCALE * yFirst;
             o >= first; o -= W) {
            for (int j = W - 1; j >= 0; --j) {
                out[o + j] = 0;
            }
        }

        for (int y = yLast - 1; y >= yFirst; y--) {

            final int sy = SCALE * y;
            final int oy = y << 8;

            for (int x = 255; x >= 0; --x) {
                final int sx = SCALE * x;
                final int q = (x == 0) ? in[oy + x] : in[oy + x - 1];
                final int p = in[oy + x];

                for (int s = 16; s >= 0; s -= 8) {
                    final int vq = (q >> s) & 0xFF;
                    final int vp = (p >> s) & 0xFF;
                    if (vq == vp) {
                        final int[] kernel = kernels[vq];
                        for (int i = SCALE - 1; i >= 0; --i) {
                            final int k = kernel[i];
                            final int o = (sy + i) * W + sx;
                            for (int j = SCALE - 1; j >= 0; --j) {
                                out[o + j] |= k << s;
                            }
                        }
                    } else {
                        float delta = vp - vq;
                        for (int j = SCALE - 1; j >= 0; --j) {
                            final int o = sx + j;
                            final int[] kernel = kernels[(int) (vq + delta * (j + 1)
                                    * I_SCALE)];
                            for (int i = SCALE - 1; i >= 0; --i) {
                                out[(sy + i) * W + o] |= kernel[i] << s;
                            }
                        }
                    }
                }
            }
        }
    }
}