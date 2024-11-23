package nintaco.gui.spritesaver;

import nintaco.Machine;
import nintaco.palettes.PaletteUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static nintaco.files.FileUtil.appendSeparator;
import static nintaco.files.FileUtil.mkdir;
import static nintaco.gui.spritesaver.SpriteSearcher.TRANSPARENT;
import static nintaco.util.GuiUtil.isAlphaFormat;
import static nintaco.util.MathUtil.clamp;

public class MetaspriteSet {

    private final Metasprite[] metasprites = new Metasprite[999983];

    private int sweeperSize;
    private int sweepIndex;
    private int metaspriteID;
    private int minOccurrences;

    public MetaspriteSet() {
        setSweepSeconds(10);
    }

    public final void setMinOccurrences(final int minOccurrences) {
        this.minOccurrences = minOccurrences < 1 ? 1 : minOccurrences;
    }

    public final void setSweepSeconds(final int seconds) {
        sweeperSize = metasprites.length / (2 * 60 * clamp(seconds, 1, 300));
    }

    public void sweep() {
        for (int i = sweeperSize - 1; i >= 0; i--) {
            while (metasprites[sweepIndex] != null
                    && metasprites[sweepIndex].occurrences < minOccurrences) {
                metasprites[sweepIndex] = metasprites[sweepIndex].next;
            }
            Metasprite parent = metasprites[sweepIndex];
            if (parent != null) {
                Metasprite metasprite = parent.next;
                while (metasprite != null) {
                    if (metasprite.occurrences < minOccurrences) {
                        parent.next = metasprite.next;
                    }
                    parent = metasprite;
                    metasprite = metasprite.next;
                }
            }

            if (++sweepIndex >= metasprites.length) {
                sweepIndex = 0;
            }
        }
    }

    public int save(final Machine machine, final String outputDir,
                    final String filePrefix, final String fileFormat, final int imageScale,
                    final int startIndex) {

        if (machine == null) {
            return 0;
        }

        final List<Metasprite> list = new ArrayList<>();
        for (int i = metasprites.length - 1; i >= 0; i--) {
            Metasprite metasprite = metasprites[i];
            metasprites[i] = null;
            while (metasprite != null) {
                if (metasprite.occurrences >= minOccurrences) {
                    list.add(metasprite);
                }
                metasprite = metasprite.next;
            }
        }
        Collections.sort(list);

        mkdir(outputDir);
        final String formatStr = appendSeparator(outputDir) + filePrefix + "-%0"
                + clamp(1 + (int) Math.log10(list.size() + startIndex), 1, 9) + "d."
                + fileFormat;
        int index = startIndex;
        for (final Metasprite metasprite : list) {
            save(machine, metasprite, formatStr, fileFormat, index++, imageScale);
        }

        metaspriteID = 0;
        return list.size();
    }

    private void save(final Machine machine, final Metasprite metasprite,
                      final String formatStr, final String fileFormat, final int fileIndex,
                      int imageScale) {

        final boolean alphaFormat = isAlphaFormat(fileFormat);
        final int imageWidth = imageScale * metasprite.width;
        final BufferedImage image = new BufferedImage(imageWidth,
                imageScale * metasprite.height, alphaFormat
                ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        final int transparentColor = alphaFormat ? 0 : 0x007F7F7F;
        final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer())
                .getData();
        final int[] palette = PaletteUtil.getExtendedPalette(machine);
        final byte[] paletteIndices = metasprite.paletteIndices;

        if (imageScale == 1) {
            for (int i = pixels.length - 1; i >= 0; i--) {
                final int index = paletteIndices[i] & 0x7F;
                pixels[i] = index == TRANSPARENT ? transparentColor
                        : (0xFF000000 | palette[index]);
            }
        } else {
            final int imageScale1 = imageScale - 1;
            for (int y = metasprite.height - 1; y >= 0; y--) {
                final int paletteY = y * metasprite.width;
                final int py = y * imageScale;
                for (int x = metasprite.width - 1; x >= 0; x--) {
                    final int pixelX = x * imageScale;
                    final int index = paletteIndices[paletteY + x] & 0x7F;
                    final int pixel = index == TRANSPARENT
                            ? transparentColor : (0xFF000000 | palette[index]);
                    for (int i = imageScale1; i >= 0; i--) {
                        final int pixelOffset = (py + i) * imageWidth + pixelX;
                        for (int j = imageScale1; j >= 0; j--) {
                            pixels[pixelOffset + j] = pixel;
                        }
                    }
                }
            }
        }

        try {
            ImageIO.write(image, fileFormat, new File(String.format(formatStr,
                    fileIndex)));
        } catch (Throwable t) {
            //t.printStackTrace();
        }
    }

    public boolean add(final byte[][] screen, final int x, final int y,
                       final int width, final int height, final int hash) {

        int index = (hash & 0x7FFFFFFF) % metasprites.length;
        Metasprite metasprite = metasprites[index];
        if (metasprite == null) {
            metasprites[index] = createMetasprite(screen, x, y, width, height, hash);
            return minOccurrences == 1;
        } else {
            Metasprite parent = null;
            while (true) {
                if (metasprite.hash == hash && metasprite.width == width
                        && metasprite.height == height) {
                    final byte[] paletteIndices = metasprite.paletteIndices;
                    outer:
                    {
                        for (int i = height - 1; i >= 0; i--) {
                            final byte[] row = screen[y + i];
                            final int offset = i * width;
                            for (int j = width - 1; j >= 0; j--) {
                                if (row[j + x] != paletteIndices[offset + j]) {
                                    break outer;
                                }
                            }
                        }
                        if (parent != null) {
                            parent.next = metasprite.next;
                            metasprite.next = metasprites[index].next;
                            metasprites[index] = metasprite;
                        }
                        if (metasprite.occurrences < minOccurrences) {
                            return ++metasprite.occurrences == minOccurrences;
                        }
                        return false;
                    }
                }
                if (metasprite.next == null) {
                    metasprite.next = createMetasprite(screen, x, y, width, height, hash);
                    return minOccurrences == 1;
                } else {
                    parent = metasprite;
                    metasprite = metasprite.next;
                }
            }
        }
    }

    private Metasprite createMetasprite(final byte[][] screen, final int x,
                                        final int y, final int width, final int height, final int hash) {
        final byte[] paletteIndices = new byte[width * height];
        for (int i = height - 1; i >= 0; i--) {
            System.arraycopy(screen[y + i], x, paletteIndices, i * width, width);
        }
        return new Metasprite(paletteIndices, width,
                height, hash, metaspriteID++);
    }
}
