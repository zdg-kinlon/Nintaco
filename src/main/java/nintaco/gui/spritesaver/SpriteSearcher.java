package nintaco.gui.spritesaver;

import nintaco.Machine;
import nintaco.PPU;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static nintaco.util.BitUtil.getBitBool;
import static nintaco.util.BitUtil.reverseBits;
import static nintaco.util.MathUtil.hash;

public class SpriteSearcher {

    public static final byte TRANSPARENT = 0x40;

    private final byte[][] screen = new byte[272][264];
    private final MetaspriteSet metaspriteSet = new MetaspriteSet();
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int marginMinX;
    private int marginMinY;
    private int marginMaxX;
    private int marginMaxY;

    public SpriteSearcher() {
        setEdgeMargin(16);
    }

    public final void setMinOccurrences(final int minOccurrences) {
        metaspriteSet.setMinOccurrences(minOccurrences);
    }

    public final void setSweepSeconds(final int seconds) {
        metaspriteSet.setSweepSeconds(seconds);
    }

    public final void setEdgeMargin(final int margin) {
        marginMinX = margin;
        marginMinY = margin;
        marginMaxX = 255 - margin;
        marginMaxY = 239 - margin;
    }

    public int save(final Machine machine, final String outputDir,
                    final String filePrefix, final String fileFormat, final int imageScale,
                    final int startIndex) {
        return metaspriteSet.save(machine, outputDir, filePrefix, fileFormat,
                imageScale, startIndex);
    }

    public int search(final PPU ppu) {

        if (ppu == null) {
            return 0;
        }

        metaspriteSet.sweep();
        clearScreen();
        renderSpriteTiles(ppu);
        return findMetasprites();
    }

    private int findMetasprites() {
        int spritesFound = 0;
        for (int i = 255; i >= 0; i--) {
            final byte[] row = screen[i];
            for (int j = 255; j >= 0; j--) {
                if ((row[j] & 0xFF) < TRANSPARENT) {
                    minX = j;
                    maxX = j;
                    minY = i;
                    maxY = i;
                    sprawl(j, i);

                    if (minX >= marginMinX && minY >= marginMinY && maxX <= marginMaxX
                            && maxY <= marginMaxY) {
                        final int width = maxX - minX + 1;
                        final int height = maxY - minY + 1;
                        final int hash = hash(screen, minX, minY, width, height);
                        if (metaspriteSet.add(screen, minX, minY, width, height, hash)) {
                            spritesFound++;
                        }
                    }
                }
            }
        }
        return spritesFound;
    }

    private void sprawl(int x, int y) {
        byte[] row = screen[y];
        row[x] |= 0x80;

        int e = x - 1;
        while (e >= 0 && (row[e] & 0xFF) < TRANSPARENT) {
            row[e] |= 0x80;
            e--;
        }
        e++;

        int w = x + 1;
        while (w <= 255 && (row[w] & 0xFF) < TRANSPARENT) {
            row[w] |= 0x80;
            w++;
        }
        w--;

        minX = min(minX, e);
        maxX = max(maxX, w);
        minY = min(minY, y);
        maxY = max(maxY, y);

        y--;
        if (y >= 0) {
            row = screen[y];
            for (int i = e; i <= w; i++) {
                if ((row[i] & 0xFF) < TRANSPARENT) {
                    sprawl(i, y);
                }
            }
        }
        y += 2;
        if (y <= 255) {
            row = screen[y];
            for (int i = e; i <= w; i++) {
                if ((row[i] & 0xFF) < TRANSPARENT) {
                    sprawl(i, y);
                }
            }
        }
    }

    private void renderSpriteTiles(final PPU ppu) {

        final int[] OAM = ppu.getOAM();
        final int[] paletteRAM = ppu.getPaletteRAM();
        final int patternTableAddress = ppu.getSpritePatternTableAddress();
        final boolean size8x16 = ppu.isSpriteSize8x16();
        for (int i = 0xFC; i >= 0; i -= 4) {
            final int y = OAM[i];
            final int tileIndex = OAM[i + 1];
            final int attribute = OAM[i + 2];
            final int x = OAM[i + 3];
            final int paletteIndex = 0x10 | ((attribute & 0x03) << 2);
            final boolean flipHorizontally = getBitBool(attribute, 6);
            final boolean flipVertically = getBitBool(attribute, 7);

            if (size8x16) {
                int address0 = ((tileIndex & 1) << 12) | ((tileIndex & 0xFE) << 4);
                int address1 = address0 | 0x0010;
                if (flipVertically) {
                    final int temp = address0;
                    address0 = address1;
                    address1 = temp;
                }
                renderTile(ppu, address0, paletteIndex, paletteRAM, x, y,
                        flipHorizontally, flipVertically);
                renderTile(ppu, address1, paletteIndex, paletteRAM, x, y + 8,
                        flipHorizontally, flipVertically);
            } else {
                renderTile(ppu, patternTableAddress | (OAM[i + 1] << 4), paletteIndex,
                        paletteRAM, x, y, flipHorizontally, flipVertically);
            }
        }
    }

    private void renderTile(final PPU ppu, final int patternTableAddress,
                            final int paletteIndex, final int[] paletteRAM, final int x, final int y,
                            final boolean flipHorizontally, final boolean flipVertically) {
        for (int i = 7; i >= 0; i--) {
            final int address = patternTableAddress | i;
            int b0 = ppu.peekVRAM(address);
            int b1 = ppu.peekVRAM(address | 0x08);
            if (flipHorizontally) {
                b0 = reverseBits(b0);
                b1 = reverseBits(b1);
            }
            final byte[] row = screen[y + (flipVertically ? (7 - i) : i)];
            for (int j = 7; j >= 0; j--) {
                final int v = ((b1 & 1) << 1) | (b0 & 1);
                b0 >>= 1;
                b1 >>= 1;
                final int index = paletteIndex | v;
                if ((index & 0x03) != 0) {
                    row[x + j] = (byte) paletteRAM[index & 0x1F];
                }
            }
        }
    }

    private void clearScreen() {
        for (int i = 255; i >= 0; i--) {
            final byte[] row = screen[i];
            for (int j = 255; j >= 0; j--) {
                row[j] = TRANSPARENT;
            }
        }
    }
}