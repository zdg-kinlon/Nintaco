package cn.kinlon.emu.input.icons;

import cn.kinlon.emu.files.FileUtil;
import cn.kinlon.emu.palettes.PalettePPU;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public enum InputIcons {

    Arkanoid("arkanoid", false),
    BandaiHyperShot("bandaihypershot", false),
    BandaiHyperShotTrigger("bandaihypershot-trigger", true),
    Doremikko("doremikko", false),
    DoremikkoBlack("doremikko-black", true),
    DoremikkoWhite1("doremikko-white1", true),
    DoremikkoWhite2("doremikko-white2", true),
    ExcitingBoxing("excitingboxing", false),
    ExcitingBoxingLeft("excitingboxing-left", true),
    ExcitingBoxingRight("excitingboxing-right", true),
    FamilyBasicKeyboard("familybasickeyboard", false),
    FamilyBasicKeyboardKey("familybasickeyboard-key", true),
    FamilyBasicKeyboardShift("familybasickeyboard-shift", true),
    FamilyBasicKeyboardSpace("familybasickeyboard-space", true),
    Gamepad("gamepad", false),
    GamepadAB("gamepad-ab", true),
    GamepadDPad("gamepad-dpad", true),
    GamepadStart("gamepad-start", true),
    HoriTrack("horitrack", false),
    KonamiHyperShot("konamihypershot", false),
    KonamiHyperShotButton("konamihypershot-button", true),
    Mahjong("mahjong", false),
    Miracle("miracle", false),
    MiracleButton("miracle-button", true),
    MiracleDown("miracle-down", true),
    MiraclePedal("miracle-pedal", true),
    MiracleUp("miracle-up", true),
    Pachinko("pachinko", false),
    PachinkoDown("pachinko-down", true),
    PachinkoUp("pachinko-up", true),
    PartyTap("partytap", false),
    PartyTapButton("partytap-button", true),
    PowerPad("powerpad", false),
    RacerMate("racermate", false),
    RacerMateButton("racermate-button", true),
    RacerMateLeftPedal("racermate-left-pedal", true),
    RacerMateRightPedal("racermate-right-pedal", true),
    SuborKeyboard("suborkeyboard", false),
    SuborKeyboard3("suborkeyboard-3", true),
    SuborKeyboard5("suborkeyboard-5", true),
    SuborKeyboardEnter("suborkeyboard-enter", true),
    SuborKeyboardSpace("suborkeyboard-space", true),
    SuborKeyboardVertical("suborkeyboard-vertical", true),
    TapTapMat("taptapmat", false),
    TopRider("toprider", false),
    TopRiderBrake("toprider-brake", true),
    TopRiderHandle("toprider-handle", true),
    TopRiderShift("toprider-shift", true),
    Transformer("transformer", false),
    UForce("uforce", false),
    Zapper("zapper", false),
    ZapperTarget("zapper-target", false),
    ZapperTrigger("zapper-trigger", true);

    private static final int TRANSPARENT = -1;
    private static final int DARK_GRAY = 0x00;
    private static final int GRAY = 0x10;
    private static final int BLACK = 0x0F;
    private static final int WHITE = 0x30;

    private static int paletteIndex;
    private final int width;
    private final int height;
    private final int[][] pixels;
    InputIcons(final String file, final boolean button) {
        int w = 0;
        int h = 0;
        int[] ps = null;
        try {
            final BufferedImage image = ImageIO.read(FileUtil.getResourceAsStream("/images/" + file + ".png"));
            w = image.getWidth();
            h = image.getHeight();
            ps = new int[w * h];
            image.getRGB(0, 0, w, h, ps, 0, w);
            for (int i = ps.length - 1; i >= 0; i--) {
                if ((ps[i] & 0xFF000000) == 0) {
                    ps[i] = TRANSPARENT;
                } else if (button) {
                    ps[i] = WHITE;
                } else if ((ps[i] & 0x00FFFFFF) == 0) {
                    ps[i] = BLACK;
                } else if ((ps[i] & 0x00FFFFFF) == 0x00666666) {
                    ps[i] = DARK_GRAY;
                } else {
                    ps[i] = GRAY;
                }
            }
        } catch (final Throwable t) {
            //t.printStackTrace();
        }

        width = w;
        height = h;

        final PalettePPU[] values = PalettePPU.values();
        pixels = new int[values.length][ps.length];
        for (final PalettePPU palettePPU : values) {
            final int[] pix = pixels[palettePPU.getIndex()];
            final int[] map = palettePPU.getMap();
            for (int i = ps.length - 1; i >= 0; --i) {
                final int color = ps[i];
                pix[i] = (color == TRANSPARENT) ? TRANSPARENT : map[ps[i]];
            }
        }
    }

    public static void setPalettePPU(final PalettePPU palettePPU) {
        paletteIndex = palettePPU.getIndex();
    }

    public void render(final int[] screen, final int x, final int y) {
        final int[] pix = pixels[paletteIndex];
        for (int i = 0, k = 0, s = (y << 8) | x; i < height; i++, s += 256) {
            for (int j = 0; j < width; j++, k++) {
                final int p = pix[k];
                if (p >= 0) {
                    screen[s + j] = p;
                }
            }
        }
    }

    public void renderSafe(final int[] screen, final int x, final int y) {
        final int[] pix = pixels[paletteIndex];
        final int x2 = x + width - 1;
        final int y2 = y + height - 1;
        if (x > 255 || y > 239 || x2 < 0 || y2 < 0) {
            return;
        }
        final int xMin = x < 0 ? -x : 0;
        final int yMin = y < 0 ? -y : 0;
        final int xMax = x2 > 255 ? 255 - x : width - 1;
        final int yMax = y2 > 239 ? 239 - y : height - 1;
        for (int i = yMin; i <= yMax; i++) {
            final int po = width * i;
            final int s = ((y + i) << 8) + x;
            for (int j = xMin; j <= xMax; j++) {
                final int p = pix[po + j];
                if (p >= 0) {
                    screen[s + j] = p;
                }
            }
        }
    }
}