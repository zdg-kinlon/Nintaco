package nintaco.gui.exportmedia;

import nintaco.App;
import nintaco.gui.exportmedia.preferences.ExportMediaFilePrefs;
import nintaco.gui.exportmedia.preferences.FramesOption;
import nintaco.gui.image.filters.VideoFilter;
import nintaco.gui.image.filters.VideoFilterDescriptor;
import nintaco.palettes.PaletteNames;
import nintaco.palettes.PaletteUtil;
import nintaco.preferences.AppPrefs;
import nintaco.tv.PixelAspectRatio;
import nintaco.tv.ScreenBorders;
import nintaco.tv.TVSystem;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.lang.Math.max;
import static java.lang.Math.round;
import static nintaco.gui.exportmedia.preferences.FramesOption.*;
import static nintaco.gui.exportmedia.preferences.VideoType.ANIMATED_GIF;
import static nintaco.tv.TVSystem.NTSC;
import static nintaco.util.ThreadUtil.threadWait;

public class ImageConverter {

    private final ExportMediaFilePrefs prefs;
    private final TVSystem tvSystem;
    private final int[][] palettes = new int[2][512];
    private final boolean applyPalette;
    private final VideoFilter[] videoFilters;
    private final FilterThread[] filterThreads;
    private final BufferedImage scaleImage;
    private final int[] scaleData;
    private final Graphics2D scaleGraphics;
    private final BufferedImage mergeImage;
    private final int[] mergeData;
    private final FramesOption framesOption;
    private final int sx1;
    private final int sy1;
    private final int sx2;
    private final int sy2;
    private int runningThreads;
    private boolean oddFrame = true;
    private volatile BufferedImage image;
    private volatile int[] screen;
    private volatile boolean running = true;
    public ImageConverter(final ExportMediaFilePrefs prefs,
                          final TVSystem tvSystem, final boolean multithreaded) {
        this.prefs = new ExportMediaFilePrefs(prefs);
        this.tvSystem = tvSystem;
        this.framesOption = prefs.getFileType() == ANIMATED_GIF ? SaveAll
                : prefs.getFramesOption();

        final boolean crop = prefs.isCropBorders();
        final ScreenBorders borders = tvSystem.getScreenBorders();
        final PixelAspectRatio pixelAspectRatio = tvSystem.getPixelAspectRatio();
        final double aspectRatio = prefs.isUseTvAspectRatio()
                ? (pixelAspectRatio.horizontal / (double) pixelAspectRatio.vertical)
                : 1.0;

        final int scale;
        VideoFilterDescriptor filterDescriptor = prefs.getVideoFilter();
        if (filterDescriptor == VideoFilterDescriptor.Current) {
            filterDescriptor = App.getImageFrame().getImagePane()
                    .getVideoFilterDescriptor();
            if (filterDescriptor == null) {
                filterDescriptor = VideoFilterDescriptor.NoFilter;
                scale = 1;
            } else {
                scale = filterDescriptor.getScale();
            }
        } else {
            scale = prefs.getScale();
        }
        applyPalette = filterDescriptor != VideoFilterDescriptor.Ntsc;

        final String paletteName = prefs.getPalette();
        if (PaletteNames.CURRENT.equals(paletteName)) {
            final int[][] extendedPalettes = PaletteUtil.getExtendedPalettes();
            System.arraycopy(extendedPalettes[0], 0, palettes[0], 0,
                    palettes[0].length);
            System.arraycopy(extendedPalettes[1], 0, palettes[1], 0,
                    palettes[1].length);
        } else {
            final int[] pal = new int[64];
            AppPrefs.getInstance().getPalettes().getPalette(paletteName, pal);
            PaletteUtil.extendPalette(pal, palettes);
        }

        final double filterScaleX;
        final double filterScaleY;
        if (filterDescriptor != VideoFilterDescriptor.NoFilter) {
            videoFilters = filterDescriptor.createFilters(max(1,
                            multithreaded ? Runtime.getRuntime().availableProcessors() : 1),
                    palettes);
            if (!multithreaded) {
                videoFilters[0].setGhosting(false);
            }
            filterScaleX = filterDescriptor.getWidth() / 256.0;
            filterScaleY = filterDescriptor.getHeight() / 240.0;
            if (multithreaded) {
                filterThreads = new FilterThread[videoFilters.length];
                final int scanlines = 240 / filterThreads.length;
                for (int i = filterThreads.length - 1; i >= 0; i--) {
                    filterThreads[i] = new FilterThread(videoFilters[i], i * scanlines,
                            i == filterThreads.length - 1 ? 240 : (i + 1) * scanlines);
                    filterThreads[i].start();
                }
            } else {
                filterThreads = null;
            }
        } else {
            videoFilters = null;
            filterThreads = null;
            filterScaleX = filterScaleY = 1.0;
        }

        if (crop) {
            sx1 = (int) round(filterScaleX * borders.getLeft());
            sy1 = (int) round(filterScaleY * borders.getTop());
            sx2 = (int) round(filterScaleX * (256 - borders.getRight()));
            sy2 = (int) round(filterScaleY * (240 - borders.getBottom()));
        } else {
            sx1 = 0;
            sy1 = 0;
            sx2 = (int) round(filterScaleX * 256);
            sy2 = (int) round(filterScaleY * 240);
        }

        if (scale != 1 || (filterDescriptor != VideoFilterDescriptor.NoFilter
                && (filterScaleX != scale || filterScaleY != scale))
                || aspectRatio != 1.0 || crop) {
            final int width;
            final int height;
            if (crop) {
                width = (int) Math.round(aspectRatio * scale * (256 - borders.getLeft()
                        - borders.getRight()));
                height = scale * (240 - borders.getTop() - borders.getBottom());
            } else {
                width = (int) Math.round(aspectRatio * scale * 256);
                height = scale * 240;
            }
            scaleImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            scaleGraphics = scaleImage.createGraphics();
            if (prefs.isSmoothScaling()) {
                scaleGraphics.setRenderingHint(KEY_INTERPOLATION,
                        VALUE_INTERPOLATION_BILINEAR);
            }
        } else {
            scaleImage = null;
            scaleGraphics = null;
        }

        if (framesOption == Interlace || framesOption == Merge) {
            final int width;
            final int height;
            if (scaleImage != null) {
                width = scaleImage.getWidth();
                height = scaleImage.getHeight();
            } else if (videoFilters != null) {
                final BufferedImage img = videoFilters[0].getImage();
                width = img.getWidth();
                height = img.getHeight();
            } else {
                width = 256;
                height = 240;
            }
            mergeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            mergeData = ((DataBufferInt) mergeImage.getRaster().getDataBuffer())
                    .getData();
            scaleData = ((DataBufferInt) scaleImage.getRaster().getDataBuffer())
                    .getData();
        } else {
            mergeImage = null;
            mergeData = null;
            scaleData = null;
        }
    }

    public TVSystem getTvSystem() {
        return tvSystem;
    }

    public void setImage(final BufferedImage image) {
        this.image = image;
        this.screen = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public BufferedImage convert() {

        oddFrame = !oddFrame;

        if (applyPalette) {
            PaletteUtil.applyPalette(screen, palettes[tvSystem == NTSC ? 0 : 1]);
        }

        BufferedImage img = image;
        if (videoFilters != null) {
            if (filterThreads == null) {
                videoFilters[0].filter(screen, 0, 240);
            } else {
                synchronized (FilterThread.class) {
                    runningThreads = filterThreads.length;
                    for (int i = filterThreads.length - 1; i >= 0; i--) {
                        filterThreads[i].execute();
                    }
                    while (runningThreads != 0 && running) {
                        threadWait(FilterThread.class);
                    }
                    if (!running) {
                        return img;
                    }
                }
            }
            img = videoFilters[0].getImage();
        }
        if (scaleGraphics != null) {
            try {
                scaleGraphics.drawImage(img, 0, 0, scaleImage.getWidth(),
                        scaleImage.getHeight(), sx1, sy1, sx2, sy2, null);
                img = scaleImage;
            } catch (final Throwable t) {
            }
        }
        if (framesOption == Interlace) {
            for (int y = oddFrame ? 1 : 0; y < scaleImage.getHeight(); y += 2) {
                final int offset = y * scaleImage.getWidth();
                System.arraycopy(scaleData, offset, mergeData, offset,
                        scaleImage.getWidth());
            }
            img = mergeImage;
        } else if (framesOption == Merge) {
            if (oddFrame) {
                for (int i = mergeData.length - 1; i >= 0; i--) {
                    final int m = mergeData[i];
                    final int rm = (m >> 16) & 0xFF;
                    final int gm = (m >> 8) & 0xFF;
                    final int bm = m & 0xFF;
                    final int s = scaleData[i];
                    final int rs = (s >> 16) & 0xFF;
                    final int gs = (s >> 8) & 0xFF;
                    final int bs = s & 0xFF;
                    mergeData[i] = (((rm + rs) >> 1) << 16) | (((gm + gs) >> 1) << 8)
                            | ((bm + bs) >> 1);
                }
                img = mergeImage;
            } else {
                System.arraycopy(scaleData, 0, mergeData, 0, scaleData.length);
            }
        }

        return (framesOption == SaveAll || oddFrame) ? img : null;
    }

    public int getWidth() {
        if (scaleImage != null) {
            return scaleImage.getWidth();
        }
        if (videoFilters != null) {
            return videoFilters[0].getImage().getWidth();
        }
        return image.getWidth();
    }

    public int getHeight() {
        if (scaleImage != null) {
            return scaleImage.getHeight();
        }
        if (videoFilters != null) {
            return videoFilters[0].getImage().getHeight();
        }
        return image.getHeight();
    }

    public ExportMediaFilePrefs getPrefs() {
        return prefs;
    }

    public void dispose() {
        synchronized (FilterThread.class) {
            running = false;
            FilterThread.class.notifyAll();
        }
        if (filterThreads != null) {
            for (int i = filterThreads.length - 1; i >= 0; i--) {
                filterThreads[i].dispose();
            }
        }
        if (videoFilters != null) {
            for (int i = videoFilters.length - 1; i >= 0; i--) {
                videoFilters[i].dispose();
            }
        }
        if (scaleGraphics != null) {
            scaleGraphics.dispose();
        }
        if (scaleImage != null) {
            scaleImage.flush();
        }
    }

    private class FilterThread extends Thread {

        private final VideoFilter videoFilter;
        private final int yFirst;
        private final int yLast;

        private boolean execute;

        public FilterThread(final VideoFilter videoFilter, final int yFirst,
                            int yLast) {
            this.videoFilter = videoFilter;
            this.yFirst = yFirst;
            this.yLast = yLast;
        }

        public synchronized void execute() {
            execute = true;
            notifyAll();
        }

        private synchronized void waitForExecute() {
            while (!execute && running) {
                threadWait(this);
            }
            execute = false;
        }

        @Override
        public void run() {
            while (running) {
                waitForExecute();
                if (running) {
                    videoFilter.filter(screen, yFirst, yLast);
                    synchronized (FilterThread.class) {
                        runningThreads--;
                        FilterThread.class.notifyAll();
                    }
                }
            }
        }

        public synchronized void dispose() {
            notifyAll();
        }
    }
}