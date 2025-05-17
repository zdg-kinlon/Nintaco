package cn.kinlon.emu.palettes;

import cn.kinlon.emu.App;
import cn.kinlon.emu.files.FileUtil;
import cn.kinlon.emu.gui.fonts.FontUtil;
import cn.kinlon.emu.gui.image.ImageFrame;
import cn.kinlon.emu.input.icons.InputIcons;
import cn.kinlon.emu.mappers.nintendo.vs.VsPPU;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.tv.TVSystem;

import java.io.*;
import java.util.*;

import static cn.kinlon.emu.palettes.PalettePPU.*;
import static cn.kinlon.emu.tv.TVSystem.NTSC;
import static cn.kinlon.emu.tv.TVSystem.PAL;
import static cn.kinlon.emu.utils.MathUtil.clamp;

public final class PaletteUtil {

    private static final double ATTENUATION_SCALE = 0.79399;
    private static final double ATTENUATION_OFFSET = 0.0782838;

    private static final int[] TINTS = {0, 6, 10, 8, 2, 4, 0, 0};
    private static final double[] LO_LEVELS = {-0.12, 0.00, 0.31, 0.72};
    private static final double[] HI_LEVELS = {0.40, 0.68, 1.00, 1.00};

    private static final double[] PHASES = new double[19];

    static {
        for (int i = PHASES.length - 1; i >= 0; i--) {
            PHASES[i] = -Math.cos(i * Math.PI / 6.0);
        }
    }

    private static final int[][] EXTENDED_PALETTES = new int[2][512];
    public static final boolean[] ZAPPER_COLORS = new boolean[64];
    public static final boolean[] ROB_COLORS = new boolean[64];

    private static final Set<String> standardPaletteNames = Collections
            .synchronizedSet(new HashSet<>());
    public static final Map<PalettePPU, String> defaultMapping = Collections
            .synchronizedMap(new HashMap<>());
    public static final Map<PalettePPU, boolean[]> zapperColors = Collections
            .synchronizedMap(new HashMap<>());
    public static final Map<PalettePPU, boolean[]> robColors = Collections
            .synchronizedMap(new HashMap<>());

    static {
        defaultMapping.put(_2C02, PaletteNames.SMOOTH_FBX);
        defaultMapping.put(_2C03_2C05, PaletteNames._2C03_2C05);
        defaultMapping.put(RP2C04_0001, PaletteNames.RP2C04_0001);
        defaultMapping.put(RP2C04_0002, PaletteNames.RP2C04_0002);
        defaultMapping.put(RP2C04_0003, PaletteNames.RP2C04_0003);
        defaultMapping.put(RP2C04_0004, PaletteNames.RP2C04_0004);
    }

    private static PalettePPU palettePPU;

    public static void init() throws Throwable {

        final Palettes prefs = AppPrefs.getInstance().getPalettes();

        synchronized (defaultMapping) {
            for (final Map.Entry<PalettePPU, String> entry
                    : defaultMapping.entrySet()) {
                prefs.ppuPaletteMapping.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        final Map<String, int[]> pals = prefs.originalPalettes;

        loadPalette(pals, "2C03_2C05", PaletteNames._2C03_2C05);
        loadPalette(pals, "3DS_VC", PaletteNames._3DS_VC);
        loadPalette(pals, "ASQ_Reality_C", PaletteNames.ASQ_REALITY_C);
        loadPalette(pals, "AV_Famicom", PaletteNames.AV_FAMICOM);
        loadPalette(pals, "BMF_Final_3", PaletteNames.BMF_FINAL_3);
        loadPalette(pals, "Composite Direct (FBX)",
                PaletteNames.COMPOSITE_DIRECT_FBX);
        loadPalette(pals, "Consumer", PaletteNames.CONSUMER);
        loadPalette(pals, "Drag 3", PaletteNames.DRAG_3);
        loadPalette(pals, "Dougeff", PaletteNames.DOUGEFF);
        loadPalette(pals, "FCEUX", PaletteNames.FCEUX);
        loadPalette(pals, "FCEUX-15", PaletteNames.FCEUX_15);
        loadPalette(pals, "Game_Boy", PaletteNames.GAME_BOY);
        loadPalette(pals, "Grayscale", PaletteNames.GRAYSCALE);
        loadPalette(pals, "Kizul", PaletteNames.KIZUL);
        loadPalette(pals, "NES Classic (FBX)", PaletteNames.NES_CLASSIC_FBX);
        loadPalette(pals, "nesticle", PaletteNames.NESTICLE);
        loadPalette(pals, "Nestopia_RGB", PaletteNames.NESTOPIA_RGB);
        loadPalette(pals, "Nestopia_YUV", PaletteNames.NESTOPIA_YUV);
        loadPalette(pals, "Nintendulator-NTSC", PaletteNames.NINTENDULATOR_NTSC);
        loadPalette(pals, "PVM Style D93 (FBX)", PaletteNames.PVM_STYLE_D93_FBX);
        loadPalette(pals, "Rinao", PaletteNames.RINAO);
        loadPalette(pals, "Rockman 9", PaletteNames.ROCKMAN_9);
        loadPalette(pals, "RP2C04-0001", PaletteNames.RP2C04_0001);
        loadPalette(pals, "RP2C04-0002", PaletteNames.RP2C04_0002);
        loadPalette(pals, "RP2C04-0003", PaletteNames.RP2C04_0003);
        loadPalette(pals, "RP2C04-0004", PaletteNames.RP2C04_0004);
        loadPalette(pals, "Smooth (FBX)", PaletteNames.SMOOTH_FBX);
        loadPalette(pals, "Sony CXA", PaletteNames.SONY_CXA);
        loadPalette(pals, "terratec-cinergy", PaletteNames.TERRATEC_CINERGY);
        loadPalette(pals, "Trebor", PaletteNames.TREBOR);
        loadPalette(pals, "Wavebeam", PaletteNames.WAVEBEAM);
        loadPalette(pals, "Wii_VC", PaletteNames.WII_VC);

        setLightSensorColors(_2C02, pals.get(PaletteNames.SMOOTH_FBX));
        setLightSensorColors(_2C03_2C05, pals.get(PaletteNames._2C03_2C05));
        setLightSensorColors(RP2C04_0001, pals.get(PaletteNames.RP2C04_0001));
        setLightSensorColors(RP2C04_0002, pals.get(PaletteNames.RP2C04_0002));
        setLightSensorColors(RP2C04_0003, pals.get(PaletteNames.RP2C04_0003));
        setLightSensorColors(RP2C04_0004, pals.get(PaletteNames.RP2C04_0004));

        setPalettePPU(_2C02);
    }

    public static int[] getExtendedPalette(final TVSystem tvSystem) {
        return EXTENDED_PALETTES[tvSystem == NTSC ? 0 : 1];
    }

    public static int[][] getExtendedPalettes() {
        return EXTENDED_PALETTES;
    }

    private static void loadPalette(final Map<String, int[]> palettes,
                                    final String fileName, final String displayName) throws Throwable {
        standardPaletteNames.add(displayName);
        palettes.put(displayName, loadPaletteResource(fileName));
    }

    public static int[] loadPaletteResource(final String fileName) throws Throwable {
        return loadPalette(FileUtil.getResourceAsStream("/palettes/" + fileName + ".pal"));
    }

    private static void setLightSensorColors(final PalettePPU palettePPU, final int[] palette) {
        final boolean[] zs = new boolean[palette.length];
        final boolean[] rs = new boolean[palette.length];
        for (int i = palette.length - 1; i >= 0; i--) {
            final int c = palette[i];
            final int R = (c >> 16) & 0xFF;
            final int G = (c >> 8) & 0xFF;
            final int B = c & 0xFF;
            zs[i] = (R + G + B) > 0xFF;
            rs[i] = G > 0x7F;
        }
        zapperColors.put(palettePPU, zs);
        robColors.put(palettePPU, rs);
    }

    public static int[] loadPalette(final InputStream in) throws Throwable {
        try (BufferedInputStream bin = new BufferedInputStream(in)) {
            final int[] palette = new int[64];
            for (int i = 0; i < palette.length; i++) {
                final int r = bin.read();
                final int g = bin.read();
                final int b = bin.read();
                palette[i] = (r << 16) | (g << 8) | b;
            }
            return palette;
        }
    }

    public static String getDefaultName() {
        return getDefaultName(getPalettePPU());
    }

    public static String getDefaultName(final PalettePPU palettePPU) {
        return defaultMapping.get(palettePPU);
    }

    public static synchronized PalettePPU getPalettePPU() {
        return palettePPU == null ? PalettePPU._2C02 : palettePPU;
    }

    public static synchronized boolean setPalettePPU(final PalettePPU palettePPU) {
        if (PaletteUtil.palettePPU != palettePPU) {
            PaletteUtil.palettePPU = palettePPU;
            update();
            FontUtil.setPalettePPU(palettePPU);
            InputIcons.setPalettePPU(palettePPU);
            return true;
        } else {
            return false;
        }
    }

    public static boolean usePlayChoice10PPU() {
        return setPalettePPU(_2C03_2C05);
    }

    public static boolean setVsPPU(final int vsPPU) {
        switch (vsPPU) {
            case VsPPU.RP2C04_0001:
                return setPalettePPU(RP2C04_0001);
            case VsPPU.RP2C04_0002:
                return setPalettePPU(RP2C04_0002);
            case VsPPU.RP2C04_0003:
                return setPalettePPU(RP2C04_0003);
            case VsPPU.RP2C04_0004:
                return setPalettePPU(RP2C04_0004);
            case VsPPU.RP2C03B:
            case VsPPU.RP2C03G:
            case VsPPU.RC2C03B:
            case VsPPU.RC2C03C:
            case VsPPU.RC2C05_01:
            case VsPPU.RC2C05_02:
            case VsPPU.RC2C05_03:
            case VsPPU.RC2C05_04:
            case VsPPU.RC2C05_05:
                return setPalettePPU(_2C03_2C05);
            default:
                return setPalettePPU(_2C02);
        }
    }

    public static synchronized void update() {
        final int[] palette = new int[64];
        AppPrefs.getInstance().getPalettes().getPalette(palettePPU, palette);
        extendPalette(palette, EXTENDED_PALETTES);
        System.arraycopy(zapperColors.get(palettePPU), 0, ZAPPER_COLORS, 0,
                ZAPPER_COLORS.length);
        System.arraycopy(robColors.get(palettePPU), 0, ROB_COLORS, 0,
                ROB_COLORS.length);
        final ImageFrame imageFrame = App.getImageFrame();
        if (imageFrame != null) {
            imageFrame.getImagePane().setExtendedPalettes(EXTENDED_PALETTES);
        }
    }

    public static void applyPalette(final int[] source, final int[] destination,
                                    final int[] palette) {
        for (int i = destination.length - 1; i >= 0; i--) {
            destination[i] = palette[source[i]];
        }
    }

    private static double toSinAngle(final int color) {
        return PHASES[color];
    }

    private static double toCosAngle(final int color) {
        return PHASES[color + 3];
    }

    public static void extendPalette(final int[] palette,
                                     final int[][] extendedPalettes) {
        extendPalette(palette, extendedPalettes[0], NTSC);
        extendPalette(palette, extendedPalettes[1], PAL);
    }

    public static void extendPalette(final int[] palette, final int[] extended,
                                     final TVSystem tvSystem) {

        final boolean ntsc = tvSystem == NTSC;

        for (int i = 7; i >= 0; i--) {
            final int offset = i << 6;
            final int emphasis = ntsc ? i
                    : ((i & 4) | ((i & 1) << 1) | ((i >> 1) & 1));
            for (int j = 63; j >= 0; j--) {
                final int color = j & 0x0F;
                final int RGB = palette[j];
                int R = (RGB >> 16) & 0xFF;
                int G = (RGB >> 8) & 0xFF;
                int B = RGB & 0xFF;
                if (i > 0 && color <= 0x0D) {
                    double r = R / 255.0;
                    double g = G / 255.0;
                    double b = B / 255.0;

                    double Y;
                    double I;
                    double Q;

                    Y = 0.299 * r + 0.587 * g + 0.144 * b;
                    I = 0.596 * r - 0.274 * g - 0.322 * b;
                    Q = 0.211 * r - 0.523 * g + 0.312 * b;

                    final int level = (j >> 4) & 0x03;
                    double lo = LO_LEVELS[level];
                    double hi = HI_LEVELS[level];

                    if (color == 0) {
                        lo = hi;
                    } else if (color == 0x0D) {
                        hi = lo;
                    }

                    if (emphasis == 7) {
                        Y = 1.13 * (Y * ATTENUATION_SCALE - ATTENUATION_OFFSET);
                    } else {
                        final int tintColor = TINTS[emphasis];
                        double saturation = (hi * (1 - ATTENUATION_SCALE)
                                + ATTENUATION_OFFSET) / 2.0;
                        Y -= saturation / 2.0;
                        if (emphasis >= 3 && emphasis != 4) {
                            saturation *= 0.6f;
                            Y -= saturation;
                        }
                        I += toSinAngle(tintColor) * saturation;
                        Q += toCosAngle(tintColor) * saturation;
                    }

                    r = Y + 0.956 * I + 0.621 * Q;
                    g = Y - 0.272 * I - 0.647 * Q;
                    b = Y - 1.106 * I + 1.703 * Q;

                    R = (int) (255.0 * clamp(r, 0, 1));
                    G = (int) (255.0 * clamp(g, 0, 1));
                    B = (int) (255.0 * clamp(b, 0, 1));
                }

                extended[offset | j] = (R << 16) | (G << 8) | B;
            }
        }
    }

    private PaletteUtil() {
    }
}
