package nintaco.gui.image.filters;

// xBRZ: "Scale by rules" - high quality image upscaling filter by Zenju
// https://sourceforge.net/projects/xbrz/

// Ported to Java by Nasher the Intrepidis
// http://intrepidis.blogspot.com/2014/02/xbrz-in-java.html

// GNU General Public License

public final class xBRZ extends VideoFilter {

    private static final int redMask = 0xff0000;
    private static final int greenMask = 0x00ff00;
    private static final int blueMask = 0x0000ff;
    private static final int maxRots = 4; // Number of 90 degree rotations
    private static final int maxScale = 5; // Highest possible scale
    private static final int maxScaleSq = maxScale * maxScale;
    private static final IntPair[] matrixRotation;
    private static final Scaler Scaler2x = new Scaler() {
        private static final int scale = 2;

        public int scale() {
            return scale;
        }

        public void blendLineShallow(int col, OutputMatrix out) {
            alphaBlend(1, 4, out.ref(scale - 1, 0), col);
            alphaBlend(3, 4, out.ref(scale - 1, 1), col);
        }

        public void blendLineSteep(int col, OutputMatrix out) {
            alphaBlend(1, 4, out.ref(0, scale - 1), col);
            alphaBlend(3, 4, out.ref(1, scale - 1), col);
        }

        public void blendLineSteepAndShallow(int col, OutputMatrix out) {
            alphaBlend(1, 4, out.ref(1, 0), col);
            alphaBlend(1, 4, out.ref(0, 1), col);
            alphaBlend(5, 6, out.ref(1, 1), col); //[!] fixes 7/8 used in xBR
        }

        public void blendLineDiagonal(int col, OutputMatrix out) {
            alphaBlend(1, 2, out.ref(1, 1), col);
        }

        public void blendCorner(int col, OutputMatrix out) {
            //model a round corner
            alphaBlend(21, 100, out.ref(1, 1), col); //exact: 1 - pi/4 = 0.2146018366
        }
    };
    private static final Scaler Scaler3x = new Scaler() {
        private static final int scale = 3;

        public int scale() {
            return scale;
        }

        public void blendLineShallow(int col, OutputMatrix out) {
            alphaBlend(1, 4, out.ref(scale - 1, 0), col);
            alphaBlend(1, 4, out.ref(scale - 2, 2), col);
            alphaBlend(3, 4, out.ref(scale - 1, 1), col);
            out.ref(scale - 1, 2).set(col);
        }

        public void blendLineSteep(int col, OutputMatrix out) {
            alphaBlend(1, 4, out.ref(0, scale - 1), col);
            alphaBlend(1, 4, out.ref(2, scale - 2), col);
            alphaBlend(3, 4, out.ref(1, scale - 1), col);
            out.ref(2, scale - 1).set(col);
        }

        public void blendLineSteepAndShallow(int col, OutputMatrix out) {
            alphaBlend(1, 4, out.ref(2, 0), col);
            alphaBlend(1, 4, out.ref(0, 2), col);
            alphaBlend(3, 4, out.ref(2, 1), col);
            alphaBlend(3, 4, out.ref(1, 2), col);

            out.ref(2, 2).set(col);
        }

        public void blendLineDiagonal(int col, OutputMatrix out) {
            alphaBlend(1, 8, out.ref(1, 2), col);
            alphaBlend(1, 8, out.ref(2, 1), col);
            alphaBlend(7, 8, out.ref(2, 2), col);
        }

        public void blendCorner(int col, OutputMatrix out) {
            //model a round corner
            alphaBlend(45, 100, out.ref(2, 2), col); //exact: 0.4545939598
            //alphaBlend(14, 1000, out.ref(2, 1), col); //0.01413008627 -> negligable
            //alphaBlend(14, 1000, out.ref(1, 2), col); //0.01413008627
        }
    };
    private static final Scaler Scaler4x = new Scaler() {
        private static final int scale = 4;

        public int scale() {
            return scale;
        }

        public void blendLineShallow(int col, OutputMatrix out) {
            alphaBlend(1, 4, out.ref(scale - 1, 0), col);
            alphaBlend(1, 4, out.ref(scale - 2, 2), col);
            alphaBlend(3, 4, out.ref(scale - 1, 1), col);
            alphaBlend(3, 4, out.ref(scale - 2, 3), col);
            out.ref(scale - 1, 2).set(col);
            out.ref(scale - 1, 3).set(col);
        }

        public void blendLineSteep(int col, OutputMatrix out) {
            alphaBlend(1, 4, out.ref(0, scale - 1), col);
            alphaBlend(1, 4, out.ref(2, scale - 2), col);
            alphaBlend(3, 4, out.ref(1, scale - 1), col);
            alphaBlend(3, 4, out.ref(3, scale - 2), col);
            out.ref(2, scale - 1).set(col);
            out.ref(3, scale - 1).set(col);
        }

        public void blendLineSteepAndShallow(int col, OutputMatrix out) {
            alphaBlend(3, 4, out.ref(3, 1), col);
            alphaBlend(3, 4, out.ref(1, 3), col);
            alphaBlend(1, 4, out.ref(3, 0), col);
            alphaBlend(1, 4, out.ref(0, 3), col);
            alphaBlend(1, 3, out.ref(2, 2), col); //[!] fixes 1/4 used in xBR
            out.ref(3, 3).set(col);
            out.ref(3, 2).set(col);
            out.ref(2, 3).set(col);
        }

        public void blendLineDiagonal(int col, OutputMatrix out) {
            alphaBlend(1, 2, out.ref(scale - 1, scale / 2), col);
            alphaBlend(1, 2, out.ref(scale - 2, scale / 2 + 1), col);
            out.ref(scale - 1, scale - 1).set(col);
        }

        public void blendCorner(int col, OutputMatrix out) {
            //model a round corner
            alphaBlend(68, 100, out.ref(3, 3), col); //exact: 0.6848532563
            alphaBlend(9, 100, out.ref(3, 2), col); //0.08677704501
            alphaBlend(9, 100, out.ref(2, 3), col); //0.08677704501
        }
    };
    private static final Scaler Scaler5x = new Scaler() {
        private static final int scale = 5;

        public int scale() {
            return scale;
        }

        public void blendLineShallow(int col, OutputMatrix out) {
            alphaBlend(1, 4, out.ref(scale - 1, 0), col);
            alphaBlend(1, 4, out.ref(scale - 2, 2), col);
            alphaBlend(1, 4, out.ref(scale - 3, 4), col);
            alphaBlend(3, 4, out.ref(scale - 1, 1), col);
            alphaBlend(3, 4, out.ref(scale - 2, 3), col);
            out.ref(scale - 1, 2).set(col);
            out.ref(scale - 1, 3).set(col);
            out.ref(scale - 1, 4).set(col);
            out.ref(scale - 2, 4).set(col);
        }

        public void blendLineSteep(int col, OutputMatrix out) {
            alphaBlend(1, 4, out.ref(0, scale - 1), col);
            alphaBlend(1, 4, out.ref(2, scale - 2), col);
            alphaBlend(1, 4, out.ref(4, scale - 3), col);
            alphaBlend(3, 4, out.ref(1, scale - 1), col);
            alphaBlend(3, 4, out.ref(3, scale - 2), col);
            out.ref(2, scale - 1).set(col);
            out.ref(3, scale - 1).set(col);
            out.ref(4, scale - 1).set(col);
            out.ref(4, scale - 2).set(col);
        }

        public void blendLineSteepAndShallow(int col, OutputMatrix out) {
            alphaBlend(1, 4, out.ref(0, scale - 1), col);
            alphaBlend(1, 4, out.ref(2, scale - 2), col);
            alphaBlend(3, 4, out.ref(1, scale - 1), col);
            alphaBlend(1, 4, out.ref(scale - 1, 0), col);
            alphaBlend(1, 4, out.ref(scale - 2, 2), col);
            alphaBlend(3, 4, out.ref(scale - 1, 1), col);
            out.ref(2, scale - 1).set(col);
            out.ref(3, scale - 1).set(col);
            out.ref(scale - 1, 2).set(col);
            out.ref(scale - 1, 3).set(col);
            out.ref(4, scale - 1).set(col);
            alphaBlend(2, 3, out.ref(3, 3), col);
        }

        public void blendLineDiagonal(int col, OutputMatrix out) {
            alphaBlend(1, 8, out.ref(scale - 1, scale / 2), col);
            alphaBlend(1, 8, out.ref(scale - 2, scale / 2 + 1), col);
            alphaBlend(1, 8, out.ref(scale - 3, scale / 2 + 2), col);
            alphaBlend(7, 8, out.ref(4, 3), col);
            alphaBlend(7, 8, out.ref(3, 4), col);
            out.ref(4, 4).set(col);
        }

        public void blendCorner(int col, OutputMatrix out) {
            //model a round corner
            alphaBlend(86, 100, out.ref(4, 4), col); //exact: 0.8631434088
            alphaBlend(23, 100, out.ref(4, 3), col); //0.2306749731
            alphaBlend(23, 100, out.ref(3, 4), col); //0.2306749731
            //alphaBlend(8, 1000, out.ref(4, 2), col); //0.008384061834 -> negligable
            //alphaBlend(8, 1000, out.ref(2, 4), col); //0.008384061834
        }
    };

    //calculate input matrix coordinates after rotation at program startup
    static {
        matrixRotation = new IntPair[(maxScale - 1) * maxScaleSq * maxRots];
        for (int n = 2; n < maxScale + 1; n++) {
            for (int r = 0; r < maxRots; r++) {
                final int nr = (n - 2) * (maxRots * maxScaleSq) + r * maxScaleSq;
                for (int i = 0; i < maxScale; i++) {
                    for (int j = 0; j < maxScale; j++) {
                        matrixRotation[nr + i * maxScale + j]
                                = buildMatrixRotation(r, i, j, n);
                    }
                }
            }
        }
    }

    private final BlendResult blendResult = new BlendResult();
    private final ScalerCfg cfg;
    private final ScaleSize scaleSize;
    private OutputMatrix outputMatrix;
    /*
    input kernel area naming convention:
    -----------------
    | A | B | C | D |
    ----|---|---|---|
    | E | F | G | H | //evalute the four corners between F, G, J, K
    ----|---|---|---| //input pixel is at position F
    | I | J | K | L |
    ----|---|---|---|
    | M | N | O | P |
    -----------------
     */
    private IColorDist preProcessCorners_colorDist;
    private IColorEq scalePixel_colorEq;
    private IColorDist scalePixel_colorDist;

    public xBRZ(xBRZ xbrz) {
        super(xbrz.getImage(), xbrz.getImageData());
        this.scaleSize = xbrz.scaleSize;
        this.cfg = new ScalerCfg();
    }

    public xBRZ(final ScaleSize scaleSize) {
        super(scaleSize.size);
        this.scaleSize = scaleSize;
        this.cfg = new ScalerCfg();
    }

    private static void alphaBlend(
            final int n,
            final int m,
            final IntPtr dstPtr,
            final int col) {
        //this works because 8 upper bits are free
        final int dst = dstPtr.get();
        final int redComponent = blendComponent(redMask, n, m, dst, col);
        final int greenComponent = blendComponent(greenMask, n, m, dst, col);
        final int blueComponent = blendComponent(blueMask, n, m, dst, col);
        final int blend = (redComponent | greenComponent | blueComponent);
        dstPtr.set(blend | 0xff000000);
    }

    private static int blendComponent(
            final int mask,
            final int n,
            final int m,
            final int inPixel,
            final int setPixel) {
        final int inChan = inPixel & mask;
        final int setChan = setPixel & mask;
        final int blend = setChan * n + inChan * (m - n);
        final int component = mask & (blend / m);
        return component;
    }

    //fill block with the given color
    private static void fillBlock(
            final int[] trg,
            int trgi,
            final int pitch,
            final int col,
            final int blockSize) {
        for (int y = 0; y < blockSize; ++y, trgi += pitch) {
            for (int x = 0; x < blockSize; ++x) {
                trg[trgi + x] = col;
            }
        }
    }

    private static double square(final double value) {
        return value * value;
    }

    private static double distYCbCr(
            final int pix1,
            final int pix2,
            final double lumaWeight) {

        // http://en.wikipedia.org/wiki/YCbCr#ITU-R_BT.601_conversion
        // YCbCr conversion is a matrix multiplication => take advantage of 
        // linearity by subtracting first!

        // we may delay division by 255 to after matrix multiplication
        // subtraction for int is noticeable faster than for double
        final int r_diff = ((pix1 & redMask) - (pix2 & redMask)) >> 16;
        final int g_diff = ((pix1 & greenMask) - (pix2 & greenMask)) >> 8;
        final int b_diff = (pix1 & blueMask) - (pix2 & blueMask);

        final double k_b = 0.0722; //ITU-R BT.709 conversion
        final double k_r = 0.2126; //
        final double k_g = 1 - k_b - k_r;

        final double scale_b = 0.5 / (1 - k_b);
        final double scale_r = 0.5 / (1 - k_r);

        //[!], analog YCbCr!
        final double y = k_r * r_diff + k_g * g_diff + k_b * b_diff;
        final double c_b = scale_b * (b_diff - y);
        final double c_r = scale_r * (r_diff - y);

        // Skip division by 255.
        // Also skip square root here by pre-squaring the
        // config option equalColorTolerance.
        //return Math.sqrt(square(lumaWeight * y) + square(c_b) + square(c_r));
        return square(lumaWeight * y) + square(c_b) + square(c_r);
    }

    private static double colorDist(
            final int pix1,
            final int pix2,
            final double luminanceWeight) {
        if (pix1 == pix2) {
            return 0;
        }
        return distYCbCr(pix1, pix2, luminanceWeight);
    }

    private static IntPair buildMatrixRotation(
            final int rotDeg,
            final int I,
            final int J,
            final int N) {

        final int I_old, J_old;

        if (rotDeg == 0) {
            I_old = I;
            J_old = J;
        } else {
            //old coordinates before rotation!
            final IntPair old = buildMatrixRotation(rotDeg - 1, I, J, N);
            I_old = N - 1 - old.J;
            J_old = old.I;
        }

        return new IntPair(I_old, J_old);
    }

    @Override
    public void filter(final int[] in, final int yFirst, final int yLast) {
        scaleImage(in, out, 256, 240, yFirst, yLast);
    }

    //detect blend direction
    private void preProcessCorners(final Kernel_4x4 ker) {
        blendResult.reset();

        if ((ker.f == ker.g && ker.j == ker.k)
                || (ker.f == ker.j && ker.g == ker.k)) {
            return;
        }

        final IColorDist dist = preProcessCorners_colorDist;

        final int weight = 4;
        final double jg = dist.D(ker.i, ker.f) + dist.D(ker.f, ker.c)
                + dist.D(ker.n, ker.k) + dist.D(ker.k, ker.h)
                + weight * dist.D(ker.j, ker.g);
        final double fk = dist.D(ker.e, ker.j) + dist.D(ker.j, ker.o)
                + dist.D(ker.b, ker.g) + dist.D(ker.g, ker.l)
                + weight * dist.D(ker.f, ker.k);

        if (jg < fk) {
            final boolean dominantGradient = cfg.dominantDirectionThreshold * jg < fk;
            if (ker.f != ker.g && ker.f != ker.j) {
                blendResult.f = dominantGradient ? BlendType.BLEND_DOMINANT
                        : BlendType.BLEND_NORMAL;
            }
            if (ker.k != ker.j && ker.k != ker.g) {
                blendResult.k = dominantGradient ? BlendType.BLEND_DOMINANT
                        : BlendType.BLEND_NORMAL;
            }
        } else if (fk < jg) {
            final boolean dominantGradient = cfg.dominantDirectionThreshold * fk < jg;
            if (ker.j != ker.f && ker.j != ker.k) {
                blendResult.j = dominantGradient ? BlendType.BLEND_DOMINANT
                        : BlendType.BLEND_NORMAL;
            }
            if (ker.g != ker.f && ker.g != ker.k) {
                blendResult.g = dominantGradient ? BlendType.BLEND_DOMINANT
                        : BlendType.BLEND_NORMAL;
            }
        }
    }

    /*
    input kernel area naming convention:
    -------------
    | A | B | C |
    ----|---|---|
    | D | E | F | //input pixel is at position E
    ----|---|---|
    | G | H | I |
    -------------
     */
    private void scalePixel(
            final Scaler scaler,
            final int rotDeg,
            final Kernel_3x3 ker,
            final int[] trg,
            final int trgi,
            final int trgWidth,
            final char blendInfo) {

        // blendInfo - result of preprocessing all four corners of pixel "e"

        //final int a = ker._[Rot._[(0 << 2) + rotDeg]];
        final int b = ker.K[Rot.R[(1 << 2) + rotDeg]];
        final int c = ker.K[Rot.R[(2 << 2) + rotDeg]];
        final int d = ker.K[Rot.R[(3 << 2) + rotDeg]];
        final int e = ker.K[Rot.R[(4 << 2) + rotDeg]];
        final int f = ker.K[Rot.R[(5 << 2) + rotDeg]];
        final int g = ker.K[Rot.R[(6 << 2) + rotDeg]];
        final int h = ker.K[Rot.R[(7 << 2) + rotDeg]];
        final int i = ker.K[Rot.R[(8 << 2) + rotDeg]];

        final char blend = BlendInfo.rotate(blendInfo, rotDeg);

        if (BlendInfo.getBottomR(blend) == BlendType.BLEND_NONE) {
            return;
        }

        final IColorEq eq = scalePixel_colorEq;
        final IColorDist dist = scalePixel_colorDist;

        boolean doLineBlend;

        if (BlendInfo.getBottomR(blend) >= BlendType.BLEND_DOMINANT) {
            doLineBlend = true;
        } // make sure there is no second blending in an adjacent
        // rotation for this pixel: handles insular pixels, mario eyes
        // but support double-blending for 90 degree corners
        else if (BlendInfo.getTopR(blend) != BlendType.BLEND_NONE && !eq.E(e, g)) {
            doLineBlend = false;
        } else if (BlendInfo.getBottomL(blend) != BlendType.BLEND_NONE
                && !eq.E(e, c)) {
            doLineBlend = false;
        } // no full blending for L-shapes; blend corner only (handles 
        // "mario mushroom eyes")
        else doLineBlend = !eq.E(g, h) || !eq.E(h, i) || !eq.E(i, f) || !eq.E(f, c)
                    || eq.E(e, i);

        //choose most similar color
        final int px = dist.D(e, f) <= dist.D(e, h) ? f : h;

        final OutputMatrix out = outputMatrix;
        out.move(rotDeg, trgi);

        if (!doLineBlend) {
            scaler.blendCorner(px, out);
            return;
        }

        //test sample: 70% of values max(fg, hc) / min(fg, hc)
        //are between 1.1 and 3.7 with median being 1.9
        final double fg = dist.D(f, g);
        final double hc = dist.D(h, c);

        final boolean haveShallowLine = cfg.steepDirectionThreshold * fg <= hc
                && e != g && d != g;
        final boolean haveSteepLine = cfg.steepDirectionThreshold * hc <= fg
                && e != c && b != c;

        if (haveShallowLine) {
            if (haveSteepLine) {
                scaler.blendLineSteepAndShallow(px, out);
            } else {
                scaler.blendLineShallow(px, out);
            }
        } else if (haveSteepLine) {
            scaler.blendLineSteep(px, out);
        } else {
            scaler.blendLineDiagonal(px, out);
        }
    }

    // scaler policy: see "Scaler2x" reference implementation
    private void scaleImage(
            final int[] src,
            final int[] trg,
            final int srcWidth,
            final int srcHeight,
            int yFirst,
            int yLast) {
        yFirst = Math.max(yFirst, 0);
        yLast = Math.min(yLast, srcHeight);

        if (yFirst >= yLast || srcWidth <= 0) {
            return;
        }

        final int trgWidth = srcWidth * scaleSize.size;

        //temporary buffer for "on the fly preprocessing"
        final char[] preProcBuffer = new char[srcWidth];

        final Kernel_4x4 ker4 = new Kernel_4x4();

        preProcessCorners_colorDist = new IColorDist() {
            public double D(final int col1, final int col2) {
                return colorDist(col1, col2, cfg.luminanceWeight);
            }
        };

        // initialize preprocessing buffer for first row:
        // detect upper left and right corner blending
        // this cannot be optimized for adjacent processing
        // stripes; we must not allow for a memory race condition!
        if (yFirst > 0) {
            final int y = yFirst - 1;

            final int s_m1 = srcWidth * Math.max(y - 1, 0);
            final int s_0 = srcWidth * y; //center line
            final int s_p1 = srcWidth * Math.min(y + 1, srcHeight - 1);
            final int s_p2 = srcWidth * Math.min(y + 2, srcHeight - 1);

            for (int x = 0; x < srcWidth; ++x) {
                final int x_m1 = Math.max(x - 1, 0);
                final int x_p1 = Math.min(x + 1, srcWidth - 1);
                final int x_p2 = Math.min(x + 2, srcWidth - 1);

                //read sequentially from memory as far as possible
                ker4.a = src[s_m1 + x_m1];
                ker4.b = src[s_m1 + x];
                ker4.c = src[s_m1 + x_p1];
                ker4.d = src[s_m1 + x_p2];

                ker4.e = src[s_0 + x_m1];
                ker4.f = src[s_0 + x];
                ker4.g = src[s_0 + x_p1];
                ker4.h = src[s_0 + x_p2];

                ker4.i = src[s_p1 + x_m1];
                ker4.j = src[s_p1 + x];
                ker4.k = src[s_p1 + x_p1];
                ker4.l = src[s_p1 + x_p2];

                ker4.m = src[s_p2 + x_m1];
                ker4.n = src[s_p2 + x];
                ker4.o = src[s_p2 + x_p1];
                ker4.p = src[s_p2 + x_p2];

                preProcessCorners(ker4); // writes to blendResult
        /*
          preprocessing blend result:
          ---------
          | F | G | //evalute corner between F, G, J, K
          ----|---| //input pixel is at position F
          | J | K |
          ---------
         */
                preProcBuffer[x] = BlendInfo.setTopR(preProcBuffer[x], blendResult.j);

                if (x + 1 < srcWidth) {
                    preProcBuffer[x + 1] = BlendInfo.setTopL(preProcBuffer[x + 1],
                            blendResult.k);
                }
            }
        }

        final double eqColorThres = square(cfg.equalColorTolerance);

        scalePixel_colorEq = new IColorEq() {
            public boolean E(final int col1, final int col2) {
                return colorDist(col1, col2, cfg.luminanceWeight) < eqColorThres;
            }
        };

        scalePixel_colorDist = new IColorDist() {
            public double D(final int col1, final int col2) {
                return colorDist(col1, col2, cfg.luminanceWeight);
            }
        };

        outputMatrix = new OutputMatrix(scaleSize.size, trg, trgWidth);

        char blend_xy = 0;
        char blend_xy1 = 0;

        final Kernel_3x3 ker3 = new Kernel_3x3();

        for (int y = yFirst; y < yLast; y++) {
            //consider MT "striped" access
            int trgi = scaleSize.size * y * trgWidth;

            final int s_m1 = srcWidth * Math.max(y - 1, 0);
            final int s_0 = srcWidth * y; //center line
            final int s_p1 = srcWidth * Math.min(y + 1, srcHeight - 1);
            final int s_p2 = srcWidth * Math.min(y + 2, srcHeight - 1);

            blend_xy1 = 0; //corner blending for current (x, y + 1) position

            for (int x = 0; x < srcWidth; ++x, trgi += scaleSize.size) {
                final int x_m1 = Math.max(x - 1, 0);
                final int x_p1 = Math.min(x + 1, srcWidth - 1);
                final int x_p2 = Math.min(x + 2, srcWidth - 1);

                //evaluate the four corners on bottom-right of current pixel
                //blend_xy for current (x, y) position
                {
                    //read sequentially from memory as far as possible
                    ker4.a = src[s_m1 + x_m1];
                    ker4.b = src[s_m1 + x];
                    ker4.c = src[s_m1 + x_p1];
                    ker4.d = src[s_m1 + x_p2];

                    ker4.e = src[s_0 + x_m1];
                    ker4.f = src[s_0 + x];
                    ker4.g = src[s_0 + x_p1];
                    ker4.h = src[s_0 + x_p2];

                    ker4.i = src[s_p1 + x_m1];
                    ker4.j = src[s_p1 + x];
                    ker4.k = src[s_p1 + x_p1];
                    ker4.l = src[s_p1 + x_p2];

                    ker4.m = src[s_p2 + x_m1];
                    ker4.n = src[s_p2 + x];
                    ker4.o = src[s_p2 + x_p1];
                    ker4.p = src[s_p2 + x_p2];

                    preProcessCorners(ker4); // writes to blendResult

          /*
          preprocessing blend result:
          ---------
          | F | G | //evaluate corner between F, G, J, K
          ----|---| //current input pixel is at position F
          | J | K |
          ---------
           */
                    //all four corners of (x, y) have been determined at
                    //this point due to processing sequence!
                    blend_xy = BlendInfo.setBottomR(preProcBuffer[x], blendResult.f);

                    //set 2nd known corner for (x, y + 1)
                    blend_xy1 = BlendInfo.setTopR(blend_xy1, blendResult.j);
                    //store on current buffer position for use on next row
                    preProcBuffer[x] = blend_xy1;

                    //set 1st known corner for (x + 1, y + 1) and
                    //buffer for use on next column
                    blend_xy1 = BlendInfo.setTopL((char) 0, blendResult.k);

                    if (x + 1 < srcWidth) //set 3rd known corner for (x + 1, y)
                    {
                        preProcBuffer[x + 1] = BlendInfo.setBottomL(preProcBuffer[x + 1],
                                blendResult.g);
                    }
                }

                // fill block of size scale * scale with the given color
                // place *after* preprocessing step, to not overwrite the
                // results while processing the the last pixel!
                fillBlock(trg, trgi, trgWidth, src[s_0 + x], scaleSize.size);

                //blend four corners of current pixel
                if (blend_xy == 0) {
                    continue;
                }

                final int a = 0, b = 1, c = 2, d = 3, e = 4, f = 5, g = 6, h = 7, i = 8;

                //read sequentially from memory as far as possible
                ker3.K[a] = src[s_m1 + x_m1];
                ker3.K[b] = src[s_m1 + x];
                ker3.K[c] = src[s_m1 + x_p1];

                ker3.K[d] = src[s_0 + x_m1];
                ker3.K[e] = src[s_0 + x];
                ker3.K[f] = src[s_0 + x_p1];

                ker3.K[g] = src[s_p1 + x_m1];
                ker3.K[h] = src[s_p1 + x];
                ker3.K[i] = src[s_p1 + x_p1];

                scalePixel(scaleSize.scaler, RotationDegree.ROT_0, ker3, trg, trgi,
                        trgWidth, blend_xy);
                scalePixel(scaleSize.scaler, RotationDegree.ROT_90, ker3, trg, trgi,
                        trgWidth, blend_xy);
                scalePixel(scaleSize.scaler, RotationDegree.ROT_180, ker3, trg, trgi,
                        trgWidth, blend_xy);
                scalePixel(scaleSize.scaler, RotationDegree.ROT_270, ker3, trg, trgi,
                        trgWidth, blend_xy);
            }
        }
    }

    public enum ScaleSize {
        Times2(Scaler2x),
        Times3(Scaler3x),
        Times4(Scaler4x),
        Times5(Scaler5x);

        public final int size;
        private final Scaler scaler;

        ScaleSize(final Scaler scaler) {
            this.scaler = scaler;
            this.size = scaler.scale();
        }

        public static final ScaleSize cast(final int ordinal) {
            final int ord1 = Math.max(ordinal, 0);
            final int ord2 = Math.min(ord1, values().length - 1);
            return values()[ord2];
        }
    }

    private enum BlendType {
        ;

        // These blend types must fit into 2 bits.
        public static final char BLEND_NONE = 0; //do not blend
        public static final char BLEND_NORMAL = 1;//a normal indication to blend
        public static final char BLEND_DOMINANT = 2; //a strong indication to blend
    }
    private enum Rot {
        ;
        // Cache the 4 rotations of the 9 positions, a to i.
        public static int[] R = new int[9 * 4];

        static {
            final int a = 0, b = 1, c = 2,
                    d = 3, e = 4, f = 5,
                    g = 6, h = 7, i = 8;

            final int[] deg0 = new int[]{
                    a, b, c,
                    d, e, f,
                    g, h, i};

            final int[] deg90 = new int[]{
                    g, d, a,
                    h, e, b,
                    i, f, c};

            final int[] deg180 = new int[]{
                    i, h, g,
                    f, e, d,
                    c, b, a};

            final int[] deg270 = new int[]{
                    c, f, i,
                    b, e, h,
                    a, d, g};

            final int[][] rotation = new int[][]{
                    deg0, deg90, deg180, deg270
            };

            for (int rotDeg = 0; rotDeg < 4; rotDeg++) {
                for (int x = 0; x < 9; x++) {
                    R[(x << 2) + rotDeg] = rotation[rotDeg][x];
                }
            }
        }
    }
    private enum BlendInfo {
        ;

        public static final char getTopL(final char b) {
            return (char) ((b) & 0x3);
        }

        public static final char getTopR(final char b) {
            return (char) ((b >> 2) & 0x3);
        }

        public static final char getBottomR(final char b) {
            return (char) ((b >> 4) & 0x3);
        }

        public static final char getBottomL(final char b) {
            return (char) ((b >> 6) & 0x3);
        }

        public static final char setTopL(final char b, final char bt) {
            return (char) (b | bt);
        }

        public static final char setTopR(final char b, final char bt) {
            return (char) (b | (bt << 2));
        }

        public static final char setBottomR(final char b, final char bt) {
            return (char) (b | (bt << 4));
        }

        public static final char setBottomL(final char b, final char bt) {
            return (char) (b | (bt << 6));
        }

        public static final char rotate(
                final char b,
                final int rotDeg) {
            final int l = rotDeg << 1;
            final int r = 8 - l;
            return (char) (b << l | b >> r);
        }
    }
    //clock-wise
    private enum RotationDegree {
        ;
        public static final int ROT_0 = 0;
        public static final int ROT_90 = 1;
        public static final int ROT_180 = 2;
        public static final int ROT_270 = 3;
    }

    private interface IColorEq {
        boolean E(int col1, int col2);
    }

    private interface IColorDist {
        double D(int col1, int col2);
    }

    private interface Scaler {

        int scale();

        void blendLineSteep(int col, OutputMatrix out);

        void blendLineSteepAndShallow(int col, OutputMatrix out);

        void blendLineShallow(int col, OutputMatrix out);

        void blendLineDiagonal(int col, OutputMatrix out);

        void blendCorner(int col, OutputMatrix out);
    }

    public static class ScalerCfg {
        // These are the default values:

        public double luminanceWeight = 1;
        public double equalColorTolerance = 30;
        public double dominantDirectionThreshold = 3.6;
        public double steepDirectionThreshold = 2.2;
    }

    private static final class BlendResult {

        public char f;
        public char g;
        public char j;
        public char k;

        public void reset() {
            f = g = j = k = 0;
        }
    }

    private static final class Kernel_3x3 {
        public final int[] K = new int[3 * 3];
    }

    private static final class Kernel_4x4 {
        public int a, b, c, d;
        public int e, f, g, h;
        public int i, j, k, l;
        public int m, n, o, p;
    }

    // access matrix area, top-left at position "out" for image with given width
    private static final class OutputMatrix {

        private final IntPtr out;
        private final int outWidth;
        private final int n;
        private int outi;
        private int nr;

        public OutputMatrix(
                final int scale,
                final int[] out,
                final int outWidth) {
            this.n = (scale - 2) * (maxRots * maxScaleSq);
            this.out = new IntPtr(out);
            this.outWidth = outWidth;
        }

        public void move(
                final int rotDeg,
                final int outi) {
            this.nr = n + rotDeg * maxScaleSq;
            this.outi = outi;
        }

        public IntPtr ref(final int i, final int j) {
            final IntPair rot = matrixRotation[nr + i * maxScale + j];
            out.position(outi + rot.J + rot.I * outWidth);
            return out;
        }
    }

    private static final class IntPair {

        public final int I;
        public final int J;

        public IntPair(final int i, final int j) {
            I = i;
            J = j;
        }
    }

    private static final class IntPtr {

        private final int[] arr;
        private int ptr;

        public IntPtr(final int[] intArray) {
            this.arr = intArray;
        }

        public void position(final int position) {
            ptr = position;
        }

        public int get() {
            return arr[ptr];
        }

        public void set(final int val) {
            arr[ptr] = val;
        }
    }
}