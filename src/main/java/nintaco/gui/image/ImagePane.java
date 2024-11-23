package nintaco.gui.image;

import nintaco.App;
import nintaco.ScreenRenderer;
import nintaco.api.Colors;
import nintaco.gui.fonts.FontUtil;
import nintaco.gui.image.filters.VideoFilter;
import nintaco.gui.image.filters.VideoFilterDescriptor;
import nintaco.gui.image.preferences.View;
import nintaco.gui.overscan.OverscanPrefs;
import nintaco.gui.screenshots.ScreenshotSaver;
import nintaco.input.InputUtil;
import nintaco.palettes.PaletteUtil;
import nintaco.preferences.AppPrefs;
import nintaco.tv.PixelAspectRatio;
import nintaco.tv.ScreenBorders;
import nintaco.tv.TVSystem;
import nintaco.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static nintaco.tv.TVSystem.NTSC;
import static nintaco.util.GuiUtil.invokeAndWait;
import static nintaco.util.ThreadUtil.joinAll;
import static nintaco.util.ThreadUtil.threadWait;

public class ImagePane extends JComponent implements ScreenRenderer {

    public static final int IMAGE_WIDTH = 256;
    public static final int IMAGE_HEIGHT = 240;

    private static final int CURSOR_TIMEOUT = 120;
    private static final int MESSAGE_TIMEOUT = 120;

    private static final long FPS_INTERVAL = 1_250_000_000L;
    private static final String DEFAULT_FPS_STRING = "         ";

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
    private volatile int imageX;
    private volatile int imageY;
    private volatile int imageWidth;
    private volatile int imageHeight;
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
    private volatile ScreenBorders screenBorders = NTSC.getScreenBorders();
    private volatile PixelAspectRatio pixelAspectRatio = PixelAspectRatio.SQUARE;
    private volatile boolean useTvAspectRatio;
    private volatile boolean smoothScaling;
    private volatile boolean uniformPixelScaling;
    private volatile int screenScale = 2;
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
    private volatile boolean requestScreenshot;
    private volatile boolean showFPS;
    private volatile boolean showStatusMessages;
    private volatile boolean rewinding;
    private volatile boolean paused;
    private volatile String message;
    private volatile int messageTimer;
    private int generatedFrames;
    private int displayedFrames;
    private long frameTime;
    private String fpsStr = DEFAULT_FPS_STRING;
    private final Thread renderThread = new Thread(this::renderLoop,
            "Render Thread");

    public ImagePane() {

        final View view = AppPrefs.getInstance().getView();
        setShowFPS(view.isShowFPS());
        setShowStatusMessages(view.isShowStatusMessages());

        renderThread.start();
        createVideoFilterThreads();

        paintImage = image;
        for (int i = screens.length - 1; i >= 0; i--) {
            Arrays.fill(screens[i], PaletteUtil.getPalettePPU().getMap()[Colors
                    .BLACK]);
        }

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent e) {
                paneResized();
            }

            @Override
            public void componentResized(final ComponentEvent e) {
                paneResized();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(final MouseEvent e) {
                InputUtil.setMouseCoordinates(-1);
                showCursor();
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                showCursor();
            }
        });

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseMoved(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (x >= imageX && y >= imageY && x < imageX + imageWidth
                        && y < imageY + imageHeight) {
                    x = screenBorders.left + (x - imageX) * (IMAGE_WIDTH
                            - screenBorders.left - screenBorders.right) / imageWidth;
                    y = screenBorders.top + (y - imageY) * (IMAGE_HEIGHT
                            - screenBorders.top - screenBorders.bottom) / imageHeight;
                    InputUtil.setMouseCoordinates(IMAGE_WIDTH * y + x);
                } else {
                    InputUtil.setMouseCoordinates(-1);
                }
                showCursor();
            }
        });

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

    public boolean isShowFPS() {
        return showFPS;
    }

    public void setShowFPS(final boolean showFPS) {
        App.runVsDualImagePane(this, p -> p.setShowFPS(showFPS));
        this.showFPS = showFPS;
        frameTime = System.nanoTime();
        generatedFrames = 0;
        displayedFrames = 0;
        fpsStr = DEFAULT_FPS_STRING;
    }

    public boolean isShowStatusMessages() {
        return showStatusMessages;
    }

    public void setShowStatusMessages(final boolean showStatusMessages) {
        App.runVsDualImagePane(this, p -> p.setShowStatusMessages(showFPS));
        this.showStatusMessages = showStatusMessages;
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

    public void showMessage(final String message) {
        App.runVsDualImagePane(this, p -> p.showMessage(message));
        this.message = message;
        this.messageTimer = message == null ? 0 : MESSAGE_TIMEOUT;
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

    private void showCursor() {
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
            switch (tvSystem) {
                case NTSC:
                    screenBorders = prefs.getNtscBorders();
                    break;
                case PAL:
                    screenBorders = prefs.getPalBorders();
                    break;
                case Dendy:
                    screenBorders = prefs.getDendyBorders();
                    break;
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
                pixelAspectRatio = tvSystem.getPixelAspectRatio();
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

    public boolean isUniformPixelScaling() {
        return uniformPixelScaling;
    }

    public void setUniformPixelScaling(final boolean uniformPixelScaling) {
        App.runVsDualImagePane(this,
                p -> p.setUniformPixelScaling(uniformPixelScaling));
        this.uniformPixelScaling = uniformPixelScaling;
        repaint();
    }

    private void adjustPreferredSize() {
        if (EventQueue.isDispatchThread()) {
            final double aspectRatio = useTvAspectRatio ? (pixelAspectRatio.horizontal
                    / (double) pixelAspectRatio.vertical) : 1.0;
            setPreferredSize(new Dimension(
                    (int) Math.round(aspectRatio * screenScale
                            * (IMAGE_WIDTH - screenBorders.left - screenBorders.right)),
                    screenScale * (IMAGE_HEIGHT - screenBorders.top
                            - screenBorders.bottom)));
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

    public void render(final int[] screen) {
        synchronized (screenMonitor) {
            generatedFrames++;
            System.arraycopy(screen, 0, screens[writeIndex], 0, screen.length);
            lastWriteIndex = writeIndex;      // Commit current screen.
            do {
                ++writeIndex;
                writeIndex &= 3;
            } while (writeIndex == readIndex); // Obtain next available screen. 
            screenMonitor.notifyAll();
        }
    }

    // Returns writable screen.
    @Override
    public int[] render() {
        synchronized (screenMonitor) {
            generatedFrames++;
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

            if (requestScreenshot) {
                requestScreenshot = false;
                ScreenshotSaver.save(screen, tvSystem);
            }

            if (showStatusMessages) {
                if (rewinding) {
                    FontUtil.drawString(screen, "REWIND", screenBorders.left + 8,
                            screenBorders.top + 8, false);
                } else if (message != null) {
                    FontUtil.drawString(screen, message, screenBorders.left + 8,
                            screenBorders.top + 8, false);
                }
            }
            if (showFPS) {
                FontUtil.drawString(screen, fpsStr, 248 - screenBorders.right
                        - (fpsStr.length() << 3), screenBorders.top + 8, true);
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
            if (--messageTimer <= 0) {
                messageTimer = 0;
                message = null;
            }
        }
    }

    public void requestScreenshot() {
        App.runVsDualImagePane(this, ImagePane::requestScreenshot);
        if (paused) {
            ScreenshotSaver.save(screens[readIndex], tvSystem);
        } else {
            requestScreenshot = true;
        }
    }

    public void clearScreen() {
        final int BLACK = PaletteUtil.getPalettePPU().getMap()[Colors.BLACK];
        App.runVsDualImagePane(this, ImagePane::clearScreen);
        rewinding = false;
        messageTimer = 0;
        message = null;
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

        final double aspectRatio = useTvAspectRatio ? (pixelAspectRatio.horizontal
                / (double) pixelAspectRatio.vertical) : 1.0;
        final int IMG_WIDTH = (int) Math.round((IMAGE_WIDTH - screenBorders.left
                - screenBorders.right) * aspectRatio * filterScale);
        final int IMG_HEIGHT = (IMAGE_HEIGHT - screenBorders.top
                - screenBorders.bottom) * filterScale;

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

        imageTop = (int) Math.round(screenBorders.top * filterScaleY);
        imageBottom = (int) Math.round((IMAGE_HEIGHT - screenBorders.bottom)
                * filterScaleY);
        imageLeft = (int) Math.round(screenBorders.left * filterScaleX);
        imageRight = (int) Math.round((IMAGE_WIDTH - screenBorders.right)
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

        if (showFPS) {
            displayedFrames++;
            final long duration = System.nanoTime() - frameTime;
            if (duration > FPS_INTERVAL) {
                final double seconds = duration * 1.0E-9;
                fpsStr = String.format("%.1f/%.1f", displayedFrames / seconds,
                        generatedFrames / seconds);
                generatedFrames = 0;
                displayedFrames = 0;
                frameTime = System.nanoTime();
            }
        }
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