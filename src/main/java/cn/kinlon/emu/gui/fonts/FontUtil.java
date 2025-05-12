package cn.kinlon.emu.gui.fonts;

import cn.kinlon.emu.files.FileUtil;
import cn.kinlon.emu.palettes.PalettePPU;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedInputStream;
import java.io.DataInputStream;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static cn.kinlon.emu.utils.CollectionsUtil.isBlank;
import static cn.kinlon.emu.utils.GuiUtil.drawHorizontalLine;
import static cn.kinlon.emu.utils.GuiUtil.drawVerticalLine;
import static cn.kinlon.emu.utils.MathUtil.isEven;

public final class FontUtil {

    private static final String FONT_FILE = "font.png";
    private static final String KERNING_FILE = "kerning.dat";

    private static final BufferedImage[] tiles = new BufferedImage[96];
    private static final int[][] pixels = new int[96][];
    private static final int[][] kernings = new int[96][96];

    private static final int DARK_BLUE = 0x01;
    private static final int BLUE = 0x11;
    private static final int WHITE = 0x30;

    private static int darkBlue;
    private static int blue;
    private static int white;

    private static PalettePPU palettePPU = PalettePPU._2C02;

    private static int requestColor = WHITE;
    private static int drawColor = 0xFF000000 | WHITE;

    static {
        try {
            createTiles(ImageIO.read(FileUtil.getResourceAsStream("/fonts/" + FONT_FILE)));
            createKerningTable();
        } catch (final Throwable t) {
            //t.printStackTrace();
        }
    }

    private FontUtil() {
    }

    private static void createKerningTable() throws Throwable {
        try (final DataInputStream in = new DataInputStream(
                new BufferedInputStream(
                        FileUtil.getResourceAsStream("/fonts/" + KERNING_FILE)
                ))
        ) {
            int value = 0;
            for (int i = 0; i < 96; i++) {
                for (int j = 0; j < 96; j++) {
                    final int v;
                    if (isEven(j)) {
                        value = in.readUnsignedByte();
                        v = value >> 4;
                    } else {
                        v = value & 0x0F;
                    }
                    kernings[i][j] = v + 1;
                }
            }
        }
    }

    private static void createTiles(final BufferedImage image) {
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 16; x++) {
                createTile(image, x, y);
            }
        }
    }

    private static void createTile(final BufferedImage image, final int x,
                                   final int y) {

        final int index = (y << 4) | x;

        tiles[index] = new BufferedImage(8, 8, TYPE_INT_ARGB);
        final int[] ps = pixels[index] = ((DataBufferInt) tiles[index].getRaster()
                .getDataBuffer()).getData();

        image.getRGB(x << 3, y << 3, 8, 8, ps, 0, 8);

        for (int i = 63; i >= 0; i--) {
            ps[i] = (ps[i] & 0x00FFFFFF) == 0x00FFFFFF ? drawColor : 0;
        }
    }

    private static void updateColor() {
        final int color = 0xFF000000 | (requestColor & 0x1C0)
                | palettePPU.getMap()[requestColor & 0x03F];
        if (drawColor != color) {
            updateColor(color);
        }
    }

    private static void updateColor(final int color) {
        drawColor = color;
        for (int i = 95; i >= 0; i--) {
            final int[] ps = pixels[i];
            for (int j = 63; j >= 0; j--) {
                if (ps[j] != 0) {
                    ps[j] = drawColor;
                }
            }
        }
    }

    public static void drawChar(final Graphics2D g, final char c, final int x,
                                final int y) {

        updateColor();
        g.drawImage(tiles[(c & 0x7F) - ' '], x, y, null);
    }

    public static void drawChar(final int[] screen, final char c, final int x,
                                int y, final boolean opaque) {

        if ((y + 7 < 0) || (x + 7 < 0) || (y > 239) || (x > 255)) {
            return;
        }

        final int yMin = y < 0 ? -y : 0;
        final int yMax = y > 232 ? (239 - y) : 7;
        final int xMin = x < 0 ? -x : 0;
        final int xMax = x > 248 ? (255 - x) : 7;

        final int[] ps = pixels[(c & 0x7F) - ' '];
        for (int i = yMin; i <= yMax; i++) {
            final int so = ((i + y) << 8) + x;
            final int po = i << 3;
            if (opaque) {
                for (int j = xMin; j <= xMax; j++) {
                    screen[so + j] = ps[po + j] == 0 ? darkBlue : white;
                }
            } else {
                for (int j = xMin; j <= xMax; j++) {
                    if (ps[po + j] != 0) {
                        screen[so + j] = white;
                    }
                }
            }
        }
    }

    private static void fillRect(final int[] screen, int x1, int y1, int x2,
                                 int y2, final int color) {

        if (x1 > x2) {
            final int t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y1 > y2) {
            final int t = y1;
            y1 = y2;
            y2 = t;
        }
        if (y2 < 0 || x2 < 0 || y2 > 239 || x2 > 255) {
            return;
        }
        final int yMin = y1 < 0 ? 0 : y1;
        final int yMax = y2 > 239 ? 239 : y2;
        final int xMin = x1 < 0 ? 0 : x1;
        final int xMax = x2 > 255 ? 255 : x2;
        for (int i = yMin; i <= yMax; i++) {
            final int so = i << 8;
            for (int j = xMin; j <= xMax; j++) {
                screen[so + j] = color;
            }
        }
    }

    public static int getWidth(final String str, final boolean monospaced) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        final int length = str.length();
        if (monospaced) {
            return length << 3;
        } else {
            int width = 0;
            int a;
            int b = 0;
            for (int i = 0; i < length; i++) {
                a = b;
                b = (str.charAt(i) & 0x7F) - ' ';
                if (i > 0) {
                    width += kernings[a][b];
                }
            }
            return width + kernings[b]['M' - ' '];
        }
    }

    public static void drawString(final int[] screen, final String str,
                                  final int x, final int y, final boolean monospaced) {

        if (str == null || str.length() == 0) {
            return;
        }

        if (!monospaced) {
            fillRect(screen, x - 1, y - 1, x + getWidth(str, monospaced), y + 7,
                    darkBlue);
        }

        final int length = str.length();
        int X = x;
        int a;
        int b = 0;
        for (int i = 0; i < length; i++) {
            final char c = str.charAt(i);
            if (monospaced) {
                if (i > 0) {
                    X += 8;
                }
            } else {
                a = b;
                b = (c & 0x7F) - ' ';
                if (i > 0) {
                    X += kernings[a][b];
                }
            }
            drawChar(screen, c, X, y, monospaced);
        }
        if (monospaced) {
            drawVerticalLine(screen, x - 1, y, y + 7, darkBlue);
            drawHorizontalLine(screen, x - 1, X + 7, y - 1, darkBlue);
            X += 8;
        } else {
            X += kernings[b]['M' - ' '] + 1;
        }

        drawVerticalLine(screen, x - 2, y - 1, y + 7, blue);
        drawVerticalLine(screen, X, y - 1, y + 7, blue);
        drawHorizontalLine(screen, x - 2, X, y - 2, blue);
        drawHorizontalLine(screen, x - 2, X, y + 8, blue);
    }

    public static void setPalettePPU(final PalettePPU palettePPU) {
        FontUtil.palettePPU = palettePPU;
        final int[] map = palettePPU.getMap();
        darkBlue = map[DARK_BLUE];
        blue = map[BLUE];
        white = map[WHITE];
        updateColor();
    }
}
