package cn.kinlon.emu.gui.image;

import cn.kinlon.emu.App;
import cn.kinlon.emu.ScreenRenderer;
import cn.kinlon.emu.Colors;
import cn.kinlon.emu.gui.adapter.ImagePaneMouseAdapter;
import cn.kinlon.emu.gui.image.filters.VideoFilter;
import cn.kinlon.emu.gui.image.filters.VideoFilterDescriptor;
import cn.kinlon.emu.gui.adapter.ImagePaneComponentAdapter;
import cn.kinlon.emu.gui.overscan.OverscanPrefs;
import cn.kinlon.emu.palettes.PaletteUtil;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.tv.PixelAspectRatio;
import cn.kinlon.emu.tv.ScreenBorders;
import cn.kinlon.emu.tv.TVSystem;
import cn.kinlon.emu.utils.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static cn.kinlon.emu.tv.TVSystem.NTSC;
import static cn.kinlon.emu.utils.GuiUtil.invokeAndWait;
import static cn.kinlon.emu.utils.ThreadUtil.joinAll;
import static cn.kinlon.emu.utils.ThreadUtil.threadWait;

public class ImagePane extends JComponent implements ScreenRenderer {

    public static final int IMAGE_WIDTH = 256;
    public static final int IMAGE_HEIGHT = 240;

    private static final int CURSOR_TIMEOUT = 120;
    private static final int MESSAGE_TIMEOUT = 120;

    private final Toolkit toolkit = Toolkit.getDefaultToolkit();
    private final Cursor crosshairsCursor;
    private final Cursor blankCursor;

    private final Object screenMonitor = new Object();
    private final int[][] screens = new int[4][IMAGE_WIDTH * IMAGE_HEIGHT];
    private final BufferedImage image = new BufferedImage(IMAGE_WIDTH,
            IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
    private final int[] data = ((DataBufferInt) image.getRaster().getDataBuffer())
            .getData();
    private CursorType cursorType = CursorType.Default;
    private volatile boolean cursorVisible = true;
    private volatile int cursorCounter = CURSOR_TIMEOUT;
    private volatile boolean hideInactiveMouseCursor;
    private volatile boolean hideFullscreenMouseCursor;
    private int writeIndex;
    private int lastWriteIndex;
    private int readIndex;
    private volatile BufferStrategy bufferStrategy;
    public volatile int imageX;
    public volatile int imageY;
    public volatile int imageWidth;
    public volatile int imageHeight;
    private volatile int imageTop;
    private volatile int imageBottom;
    private volatile int imageLeft;
    private volatile int imageRight;
    private volatile int paneWidth;
    private volatile int paneHeight;
    private volatile Bars bars = Bars.NONE;
    private volatile BufferedImage paintImage;
    private volatile int[] palette = PaletteUtil.getExtendedPalette(NTSC);
    private volatile TVSystem tvSystem;
    public volatile ScreenBorders screenBorders = NTSC.screenBorders();
    private volatile PixelAspectRatio pixelAspectRatio = PixelAspectRatio.SQUARE;
    private volatile boolean useTvAspectRatio;
    private volatile boolean smoothScaling;
    private volatile boolean uniformPixelScaling;
    private volatile int screenScale = 1;
    private int lastFrameColor;
    private volatile Color frameColor = Color.BLACK;
    private volatile VideoFilterDescriptor videoFilterDescriptor
            = VideoFilterDescriptor.NoFilter;
    private volatile VideoFilter[] videoFilters;
    private volatile FilterThread[] filterThreads = new FilterThread[0];
    private int runningThreads;
    private int filterScale = 1;
    private double filterScaleX = 1;
    private double filterScaleY = 1;
    private boolean repaintRequested;
    private volatile boolean rendering = true;
    private volatile boolean rewinding;
    private volatile boolean paused;

    private final Thread renderThread = new Thread(this::renderLoop,
            "Render Thread");

    public ImagePane() {
        renderThread.start();
        createVideoFilterThreads();

        paintImage = image;
        for (int i = screens.length - 1; i >= 0; i--) {
            Arrays.fill(screens[i], PaletteUtil.getPalettePPU().getMap()[Colors
                    .BLACK]);
        }

        ImagePaneComponentAdapter imagePaneComponentAdapter = new ImagePaneComponentAdapter(this);
        ImagePaneMouseAdapter imagePaneMouseAdapter = new ImagePaneMouseAdapter(this);
        addComponentListener(imagePaneComponentAdapter);
        addMouseListener(imagePaneMouseAdapter);
        addMouseMotionListener(imagePaneMouseAdapter);

        adjustPreferredSize();

        crosshairsCursor = GuiUtil.getCursor("/images/crosshairs.png",
                new Point(15, 15));
        blankCursor = toolkit.createCustomCursor(new BufferedImage(16, 16,
                BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");
    }

    public void createVideoFilterThreads() {
        App.runVsDualImagePane(this, ImagePane::createVideoFilterThreads);
        createVideoFilterThreads(AppPrefs.getInstance().getUserInterfacePrefs()
                .isUseMulticoreFiltering() ? max(1, Runtime.getRuntime()
                .availableProcessors() - 1) : 1);
    }

    private void createVideoFilterThreads(final int count) {
        synchronized (FilterThread.class) {
            if (count != filterThreads.length) {
                destroyVideoFilterThreads();
                filterThreads = new FilterThread[count];
                final int scanlines = 240 / count;
                for (int i = count - 1; i >= 0; i--) {
                    filterThreads[i] = new FilterThread(i, scanlines * i, i == count - 1
                            ? 240 : scanlines * (i + 1));
                }
                createVideoFilters();
                for (int i = count - 1; i >= 0; i--) {
                    filterThreads[i].start();
                }
            }
        }
    }

    public void destroy() {
        rendering = false;
        destroyVideoFilterThreads();
    }

    private void destroyVideoFilterThreads() {
        synchronized (FilterThread.class) {
            for (int i = filterThreads.length - 1; i >= 0; i--) {
                filterThreads[i].dispose();
            }
            joinAll(filterThreads);
        }
    }

    public VideoFilterDescriptor getVideoFilterDescriptor() {
        return videoFilterDescriptor;
    }

    public void setVideoFilterDescriptor(
            final VideoFilterDescriptor videoFilterDescriptor) {
        App.runVsDualImagePane(this,
                p -> p.setVideoFilterDescriptor(videoFilterDescriptor));
        if (this.videoFilterDescriptor != videoFilterDescriptor) {
            this.videoFilterDescriptor = videoFilterDescriptor;
            createVideoFilters();
            paneResized();
            repaint();
        }
    }

    private void createVideoFilters() {
        synchronized (FilterThread.class) {
            if (videoFilters != null) {
                for (int i = videoFilters.length - 1; i >= 0; i--) {
                    videoFilters[i].dispose();
                }
            }
            videoFilters = videoFilterDescriptor.createFilters(
                    filterThreads.length, PaletteUtil.getExtendedPalettes());
            if (videoFilters == null) {
                filterScale = 1;
                filterScaleX = filterScaleY = 1.0;
                paintImage = image;
            } else {
                filterScale = videoFilterDescriptor.getScale();
                filterScaleX = videoFilterDescriptor.getWidth() / (double) IMAGE_WIDTH;
                filterScaleY = videoFilterDescriptor.getHeight()
                        / (double) IMAGE_HEIGHT;
                paintImage = videoFilters[0].getImage();
            }
        }
    }

    public void setExtendedPalettes(final int[][] extendedPalettes) {
        App.runVsDualImagePane(this,
                p -> p.setExtendedPalettes(extendedPalettes));
        synchronized (FilterThread.class) {
            if (videoFilters != null) {
                videoFilters[0].setExtendedPalettes(extendedPalettes);
            }
        }
    }

    public boolean isRewinding() {
        return rewinding;
    }

    public void setRewinding(final boolean rewinding) {
        App.runVsDualImagePane(this, p -> p.setRewinding(rewinding));
        this.rewinding = rewinding;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(final boolean paused) {
        App.runVsDualImagePane(this, p -> p.setPaused(paused));
        this.paused = paused;
    }

    public CursorType getCursorType() {
        return cursorType;
    }

    public void setCursorType(final CursorType cursorType) {
        if (EventQueue.isDispatchThread()) {
            App.runVsDualImagePane(this, p -> p.setCursorType(cursorType));
            if (this.cursorType != cursorType) {
                this.cursorType = cursorType;
                updateCursor();
            }
        } else {
            EventQueue.invokeLater(() -> setCursorType(cursorType));
        }
    }

    public boolean isHideInactiveMouseCursor() {
        return hideInactiveMouseCursor;
    }

    public void setHideInactiveMouseCursor(
            final boolean hideInactiveMouseCursor) {
        App.runVsDualImagePane(this,
                p -> p.setHideInactiveMouseCursor(hideInactiveMouseCursor));
        this.hideInactiveMouseCursor = hideInactiveMouseCursor;
    }

    public boolean isHideFullscreenMouseCursor() {
        return hideFullscreenMouseCursor;
    }

    public void setHideFullscreenMouseCursor(
            final boolean hideFullscreenMouseCursor) {
        App.runVsDualImagePane(this,
                p -> p.setHideFullscreenMouseCursor(hideFullscreenMouseCursor));
        this.hideFullscreenMouseCursor = hideFullscreenMouseCursor;
    }

    public boolean isCursorVisible() {
        return cursorVisible;
    }

    public void setCursorVisible(final boolean cursorVisible) {
        App.runVsDualImagePane(this, p -> p.setCursorVisible(cursorVisible));
        if (this.cursorVisible != cursorVisible) {
            if (EventQueue.isDispatchThread()) {
                this.cursorVisible = cursorVisible;
                updateCursor();
            } else {
                EventQueue.invokeLater(() -> setCursorVisible(cursorVisible));
            }
        }
    }

    public void showCursor() {
        cursorCounter = CURSOR_TIMEOUT;
        if (bufferStrategy == null || !hideFullscreenMouseCursor) {
            setCursorVisible(true);
        }
    }

    private void updateCursor() {
        if (cursorVisible || cursorType == CursorType.Crosshairs) {
            switch (cursorType) {
                case Crosshairs:
                    setCursor(crosshairsCursor);
                    break;
                case Blank:
                    setCursor(blankCursor);
                    break;
                default:
                    setCursor(Cursor.getDefaultCursor());
                    break;
            }
        } else {
            setCursor(blankCursor);
        }
    }

    public void updateScreenBorders() {
        App.runVsDualImagePane(this, ImagePane::updateScreenBorders);
        final AppPrefs appPrefs = AppPrefs.getInstance();
        if (appPrefs.getView().isUnderscan()) {
            screenBorders = ScreenBorders.EMPTY_BORDERS;
        } else {
            final OverscanPrefs prefs = appPrefs.getOverscanPrefs();
            if (tvSystem == TVSystem.NTSC) {
                screenBorders = prefs.getNtscBorders();
            } else if (tvSystem == TVSystem.PAL) {
                screenBorders = prefs.getPalBorders();
            } else if (tvSystem == TVSystem.Dendy) {
                screenBorders = prefs.getDendyBorders();
            }
        }
        adjustPreferredSize();
        App.getImageFrame().adjustSize();
        repaint();
    }

    public TVSystem getTVSystem() {
        return tvSystem;
    }

    public void setTVSystem(final TVSystem tvSystem) {
        if (EventQueue.isDispatchThread()) {
            App.runVsDualImagePane(this, p -> p.setTVSystem(tvSystem));
            if (this.tvSystem != tvSystem) {
                this.tvSystem = tvSystem;
                palette = PaletteUtil.getExtendedPalette(tvSystem);
                pixelAspectRatio = tvSystem.pixelAspectRatio();
                updateScreenBorders();
            }
        } else {
            EventQueue.invokeLater(() -> setTVSystem(tvSystem));
        }
    }

    public boolean isUseTvAspectRatio() {
        return useTvAspectRatio;
    }

    public void setUseTvAspectRatio(final boolean useTvAspectRatio) {
        this.useTvAspectRatio = useTvAspectRatio;
        adjustPreferredSize();
        repaint();
    }

    public int getScreenScale() {
        return screenScale;
    }

    public void setScreenScale(final int screenScale) {
        this.screenScale = screenScale;
        adjustPreferredSize();
        repaint();
    }

    public boolean isSmoothScaling() {
        return smoothScaling;
    }

    public void setSmoothScaling(final boolean smoothScaling) {
        App.runVsDualImagePane(this, p -> p.setSmoothScaling(smoothScaling));
        this.smoothScaling = smoothScaling;
        repaint();
    }

    public void setUniformPixelScaling(final boolean uniformPixelScaling) {
        App.runVsDualImagePane(this,
                p -> p.setUniformPixelScaling(uniformPixelScaling));
        this.uniformPixelScaling = uniformPixelScaling;
        repaint();
    }

    private void adjustPreferredSize() {
        if (EventQueue.isDispatchThread()) {
            final double aspectRatio = useTvAspectRatio ? (pixelAspectRatio.horizontal()
                    / (double) pixelAspectRatio.vertical()) : 1.0;
            setPreferredSize(new Dimension(
                    (int) Math.round(aspectRatio * screenScale
                            * (IMAGE_WIDTH - screenBorders.left() - screenBorders.right())),
                    screenScale * (IMAGE_HEIGHT - screenBorders.top()
                            - screenBorders.bottom())));
            invalidate();
        } else {
            invokeAndWait(this::adjustPreferredSize);
        }
    }

    public BufferStrategy getBufferStrategy() {
        return bufferStrategy;
    }

    public void setBufferStrategy(final BufferStrategy bufferStrategy) {
        this.bufferStrategy = bufferStrategy;
        if (bufferStrategy != null && hideFullscreenMouseCursor) {
            setCursorVisible(false);
        }
        EventQueue.invokeLater(this::redraw);
    }

    public void redraw() {
        // Do not redraw the VsDualImagePane
        paneResized();
        repaint();
    }

    public int getFrameColor() {
        return lastFrameColor;
    }

    public void setFrameColor(final int color) {
        App.runVsDualImagePane(this, p -> setFrameColor(color));
        if (lastFrameColor != color) {
            lastFrameColor = color;
            frameColor = new Color(color);
            final Graphics g = getGraphics();
            if (g == null) {
                return;
            }
            paintComponent(g);
            g.dispose();
        }
    }

    // Returns writable screen.
    @Override
    public int[] render() {
        synchronized (screenMonitor) {
            lastWriteIndex = writeIndex;      // Commit current screen.
            do {
                ++writeIndex;
                writeIndex &= 3;
            } while (writeIndex == readIndex); // Obtain next available screen. 
            screenMonitor.notifyAll();
            return screens[writeIndex];
        }
    }

    private void renderLoop() {
        while (rendering) {
            final int[] screen;
            synchronized (screenMonitor) {
                while (lastWriteIndex == readIndex) {
                    threadWait(screenMonitor);
                }
                readIndex = lastWriteIndex;
                screen = screens[readIndex];
            }

            if (videoFilterDescriptor == VideoFilterDescriptor.Ntsc) {
                System.arraycopy(screen, 0, data, 0, screen.length);
            } else {
                PaletteUtil.applyPalette(screen, data, palette);
            }

            synchronized (FilterThread.class) {
                if (videoFilters != null) {
                    runningThreads = filterThreads.length;
                    for (int i = filterThreads.length - 1; i >= 0; i--) {
                        filterThreads[i].execute();
                    }
                    while (runningThreads != 0) {
                        threadWait(FilterThread.class);
                    }
                    paintImage = videoFilters[0].getImage();
                } else {
                    paintImage = image;
                }
            }

            try {
                final BufferStrategy strategy = bufferStrategy;
                if (strategy != null) {
                    final Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
                    if (g != null) {
                        render(g, paintImage);
                        g.dispose();
                        if (!strategy.contentsLost()) {
                            strategy.show();
                        }
                        toolkit.sync();
                    }
                } else {
                    EventQueue.invokeAndWait(
                            () -> paintImmediately(0, 0, paneWidth, paneHeight));
                }
            } catch (final Throwable t) {
            }

            if (--cursorCounter <= 0) {
                cursorCounter = 0;
                if (hideInactiveMouseCursor) {
                    setCursorVisible(false);
                }
            }
        }
    }

    public void clearScreen() {
        final int BLACK = PaletteUtil.getPalettePPU().getMap()[Colors.BLACK];
        App.runVsDualImagePane(this, ImagePane::clearScreen);
        rewinding = false;
        for (int i = screens.length - 1; i >= 0; i--) {
            Arrays.fill(render(), BLACK);
        }
        Arrays.fill(data, 0);
        for (int i = screens.length - 1; i >= 0; i--) {
            Arrays.fill(screens[i], BLACK);
        }
        final VideoFilter[] filters = videoFilters;
        if (filters != null) {
            filters[0].reset();
        }
        invokeAndWait((Runnable) this::repaint);
    }

    public void requestRepaint() {
        App.runVsDualImagePane(this, ImagePane::requestRepaint);
        repaintRequested = true;
    }

    public void paneResized() {

        paneWidth = getWidth();
        paneHeight = getHeight();

        final double aspectRatio = useTvAspectRatio ? (pixelAspectRatio.horizontal()
                / (double) pixelAspectRatio.vertical()) : 1.0;
        final int IMG_WIDTH = (int) Math.round((IMAGE_WIDTH - screenBorders.left()
                - screenBorders.right()) * aspectRatio * filterScale);
        final int IMG_HEIGHT = (IMAGE_HEIGHT - screenBorders.top()
                - screenBorders.bottom()) * filterScale;

        if (uniformPixelScaling
                && paneWidth >= IMG_WIDTH && paneHeight >= IMG_HEIGHT) {
            int xs = 1;
            while (IMG_WIDTH * (xs + 1) <= paneWidth) {
                ++xs;
            }
            int ys = 1;
            while (IMG_HEIGHT * (ys + 1) <= paneHeight) {
                ++ys;
            }
            final int scale = min(xs, ys);
            imageWidth = IMG_WIDTH * scale;
            imageHeight = IMG_HEIGHT * scale;
            imageX = (paneWidth - imageWidth) / 2;
            imageY = (paneHeight - imageHeight) / 2;
            if (imageWidth == paneWidth) {
                bars = (imageHeight == paneHeight) ? Bars.NONE : Bars.TOP_BOTTOM;
            } else if (imageHeight == paneHeight) {
                bars = Bars.LEFT_RIGHT;
            } else {
                bars = Bars.ALL_SIDES;
            }
        } else {
            if (IMG_HEIGHT * paneWidth >= IMG_WIDTH * paneHeight) {
                imageHeight = paneHeight;
                imageY = 0;
                imageWidth = IMG_WIDTH * imageHeight / IMG_HEIGHT;
                imageX = (paneWidth - imageWidth) / 2;
                bars = (paneWidth == imageWidth) ? Bars.NONE : Bars.LEFT_RIGHT;
            } else {
                imageWidth = paneWidth;
                imageHeight = IMG_HEIGHT * imageWidth / IMG_WIDTH;
                imageY = (paneHeight - imageHeight) / 2;
                imageX = 0;
                bars = (paneHeight == imageHeight) ? Bars.NONE : Bars.TOP_BOTTOM;
            }
        }

        imageTop = (int) Math.round(screenBorders.top() * filterScaleY);
        imageBottom = (int) Math.round((IMAGE_HEIGHT - screenBorders.bottom())
                * filterScaleY);
        imageLeft = (int) Math.round(screenBorders.left() * filterScaleX);
        imageRight = (int) Math.round((IMAGE_WIDTH - screenBorders.right())
                * filterScaleX);

        if (bufferStrategy == null && App.getImageFrame().isVisible()) {
            setPreferredSize(new Dimension(paneWidth, paneHeight));
        }

        if (repaintRequested) {
            repaintRequested = false;
            repaint();
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        render((Graphics2D) g, paintImage);
    }

    private void render(final Graphics2D g, final BufferedImage buffer) {

        if (smoothScaling) {
            g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
        }

        switch (bars) {
            case LEFT_RIGHT:
                g.setColor(frameColor);
                g.fillRect(0, 0, imageX, paneHeight);
                g.fillRect(imageX + imageWidth, 0,
                        paneWidth - (imageX + imageWidth), paneHeight);
                break;
            case TOP_BOTTOM:
                g.setColor(frameColor);
                g.fillRect(0, 0, paneWidth, imageY);
                g.fillRect(0, imageY + imageHeight,
                        paneWidth, paneHeight - (imageY + imageHeight));
                break;
            case ALL_SIDES:
                g.setColor(frameColor);
                g.fillRect(0, 0, imageX, paneHeight);
                g.fillRect(imageX + imageWidth, 0,
                        paneWidth - (imageX + imageWidth), paneHeight);
                g.fillRect(imageX, 0, imageWidth, imageY);
                g.fillRect(imageX, imageY + imageHeight, imageWidth,
                        paneHeight - (imageY + imageHeight));
                break;
        }

        g.drawImage(buffer,
                imageX, imageY, imageX + imageWidth, imageY + imageHeight,
                imageLeft, imageTop, imageRight, imageBottom,
                null);

//        if (showFPS) {
//            displayedFrames++;
//            final long duration = System.nanoTime() - frameTime;
//            if (duration > FPS_INTERVAL) {
//                final double seconds = duration * 1.0E-9;
//                fpsStr = String.format("%.1f/%.1f", displayedFrames / seconds,
//                        generatedFrames / seconds);
//                generatedFrames = 0;
//                displayedFrames = 0;
//                frameTime = System.nanoTime();
//            }
//        }
    }

    private enum Bars {
        NONE,
        LEFT_RIGHT,
        TOP_BOTTOM,
        ALL_SIDES,
    }

    private class FilterThread extends Thread {

        private final int index;
        private final int yFirst;
        private final int yLast;

        private boolean execute;

        private volatile boolean running = true;

        public FilterThread(final int index, final int yFirst, final int yLast) {
            this.index = index;
            this.yFirst = yFirst;
            this.yLast = yLast;
        }

        public synchronized void execute() {
            if (running) {
                execute = true;
                notifyAll();
            } else {
                synchronized (FilterThread.class) {
                    runningThreads--;
                    FilterThread.class.notifyAll();
                }
            }
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
                    final VideoFilter[] filters = videoFilters;
                    if (filters != null && index < filters.length) {
                        filters[index].filter(data, yFirst, yLast);
                    }
                    synchronized (FilterThread.class) {
                        runningThreads--;
                        FilterThread.class.notifyAll();
                    }
                }
            }
        }

        public synchronized void dispose() {
            running = false;
            notifyAll();
        }
    }
}