package nintaco.gui;

import nintaco.util.EDT;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImagePanel extends JPanel {

    public final int IMAGE_WIDTH;
    public final int IMAGE_HEIGHT;
    private final Dimension preferredSize;
    private final BufferedImage buffer;
    private final int[] screen;
    private final List<MouseMotionProxy> mouseMotionProxies = new ArrayList<>();
    private final List<MouseProxy> mouseProxies = new ArrayList<>();
    private volatile int imageX;
    private volatile int imageY;
    private volatile int imageWidth;
    private volatile int imageHeight;
    private volatile int paneWidth;
    private volatile int paneHeight;
    private volatile boolean barsOnSides;
    private MouseEvent lastMouseEvent;
    private boolean blackBars;
    private boolean centered = true;
    private boolean minimalSizeIsPreferredSize;
    public ImagePanel(final int width, final int height) {
        this(width, height, width, height);
    }
    public ImagePanel(final int width, final int height, final int scale) {
        this(width, height, scale * width, scale * height);
    }

    public ImagePanel(final int width, final int height,
                      final int preferredWidth, final int preferredHeight) {

        this.IMAGE_WIDTH = width;
        this.IMAGE_HEIGHT = height;
        preferredSize = new Dimension(preferredWidth, preferredHeight);
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        screen = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();

        setDoubleBuffered(false);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                paneResized();
            }
        });

        super.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseUpdated(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseUpdated(e);
            }
        });
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                fireMouseExited(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                mouseUpdated(e);
            }
        });
    }

    private void mouseUpdated(MouseEvent e) {
        final MouseEvent mouseEvent = adjustMouseEvent(e);
        if (lastMouseEvent == null && mouseEvent != null) {
            fireMouseEntered(mouseEvent);
        } else if (lastMouseEvent != null && mouseEvent == null) {
            fireMouseExited(lastMouseEvent);
        }
        lastMouseEvent = mouseEvent;
    }

    private void fireMouseEntered(final MouseEvent e) {
        for (MouseProxy proxy : mouseProxies) {
            proxy.getMouseListener().mouseEntered(e);
        }
    }

    private void fireMouseExited(final MouseEvent e) {
        for (MouseProxy proxy : mouseProxies) {
            proxy.getMouseListener().mouseExited(e);
        }
    }

    private MouseEvent adjustMouseEvent(MouseEvent e) {
        final int imgX = centered ? imageX : 0;
        final int imgY = centered ? imageY : 0;
        int x = e.getX();
        int y = e.getY();
        if (x >= imgX && y >= imgY && x < imgX + imageWidth
                && y < imgY + imageHeight) {
            x = (x - imgX) * IMAGE_WIDTH / imageWidth;
            y = (y - imgY) * IMAGE_HEIGHT / imageHeight;
            return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(),
                    e.getModifiersEx(), x, y, 0, 0, e.getClickCount(), e.isPopupTrigger(),
                    e.getButton());
        } else {
            return null;
        }
    }

    @Override
    public void addMouseMotionListener(final MouseMotionListener listener) {
        final MouseMotionProxy proxy = new MouseMotionProxy(listener);
        mouseMotionProxies.add(proxy);
        super.addMouseMotionListener(proxy);
    }

    @Override
    public void removeMouseMotionListener(final MouseMotionListener listener) {
        for (int i = mouseMotionProxies.size() - 1; i >= 0; i--) {
            final MouseMotionProxy proxy = mouseMotionProxies.get(i);
            if (proxy.getMouseMotionListener().equals(listener)) {
                super.removeMouseMotionListener(proxy);
                mouseMotionProxies.remove(i);
            }
        }
    }

    @Override
    public void addMouseListener(final MouseListener listener) {
        final MouseProxy proxy = new MouseProxy(listener);
        mouseProxies.add(proxy);
        super.addMouseListener(proxy);
    }

    @Override
    public void removeMouseListener(final MouseListener listener) {
        for (int i = mouseProxies.size() - 1; i >= 0; i--) {
            final MouseProxy proxy = mouseProxies.get(i);
            if (proxy.getMouseListener().equals(listener)) {
                super.removeMouseListener(proxy);
                mouseProxies.remove(i);
            }
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return minimalSizeIsPreferredSize ? getPreferredSize()
                : super.getMinimumSize();
    }

    @Override
    public void setMinimumSize(Dimension minimumSize) {
    }

    @Override
    public void setMaximumSize(Dimension maximumSize) {
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
    }

    public boolean isMinimalSizeIsPreferredSize() {
        return minimalSizeIsPreferredSize;
    }

    public void setMinimalSizeIsPreferredSize(
            final boolean minimalSizeIsPreferredSize) {
        this.minimalSizeIsPreferredSize = minimalSizeIsPreferredSize;
    }

    public boolean isBlackBars() {
        return blackBars;
    }

    public void setBlackBars(final boolean blackBars) {
        this.blackBars = blackBars;
    }

    public boolean isCentered() {
        return centered;
    }

    public void setCentered(final boolean centered) {
        this.centered = centered;
    }

    public int[] getScreen() {
        return screen;
    }

    public void clearScreen() {
        Arrays.fill(screen, 0);
        EDT.async(this::repaint);
    }

    public void render() {
        final Graphics g = getGraphics();
        if (g != null) {
            render(g, buffer);
            g.dispose();
        }
    }

    private void paneResized() {
        paneWidth = getWidth();
        paneHeight = getHeight();
        if (IMAGE_HEIGHT * paneWidth >= IMAGE_WIDTH * paneHeight) {
            barsOnSides = true;
            imageHeight = paneHeight;
            imageY = 0;
            imageWidth = imageHeight * IMAGE_WIDTH / IMAGE_HEIGHT;
            imageX = (paneWidth - imageWidth) / 2;
        } else {
            barsOnSides = false;
            imageWidth = paneWidth;
            imageHeight = imageWidth * IMAGE_HEIGHT / IMAGE_WIDTH;
            imageY = (paneHeight - imageHeight) / 2;
            imageX = 0;
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        paneResized();
        super.paintComponent(g);
        render(g, buffer);
    }

    private void render(final Graphics g, final BufferedImage buffer) {
        if (centered) {
            if (blackBars) {
                g.setColor(Color.BLACK);
                if (barsOnSides) {
                    g.fillRect(0, 0, imageX, paneHeight);
                    g.fillRect(imageX + imageWidth, 0,
                            paneWidth - (imageX + imageWidth), paneHeight);
                } else {
                    g.fillRect(0, 0, paneWidth, imageY);
                    g.fillRect(0, imageY + imageHeight,
                            paneWidth, paneHeight - (imageY + imageHeight));
                }
            }
            g.drawImage(buffer, imageX, imageY, imageWidth, imageHeight, null);
        } else {
            if (blackBars) {
                g.setColor(Color.BLACK);
                if (barsOnSides) {
                    g.fillRect(imageWidth, 0, paneWidth - imageWidth, paneHeight);
                } else {
                    g.fillRect(0, imageHeight, paneWidth, paneHeight - imageHeight);
                }
            }
            g.drawImage(buffer, 0, 0, imageWidth, imageHeight, null);
        }
    }

    private class MouseMotionProxy implements MouseMotionListener {

        private final MouseMotionListener listener;

        public MouseMotionProxy(final MouseMotionListener listener) {
            this.listener = listener;
        }

        public MouseMotionListener getMouseMotionListener() {
            return listener;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            final MouseEvent event = adjustMouseEvent(e);
            if (event != null) {
                listener.mouseDragged(event);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            final MouseEvent event = adjustMouseEvent(e);
            if (event != null) {
                listener.mouseMoved(event);
            }
        }
    }

    private class MouseProxy implements MouseListener {

        private final MouseListener listener;

        public MouseProxy(final MouseListener listener) {
            this.listener = listener;
        }

        public MouseListener getMouseListener() {
            return listener;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            final MouseEvent event = adjustMouseEvent(e);
            if (event != null) {
                listener.mouseClicked(event);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            final MouseEvent event = adjustMouseEvent(e);
            if (event != null) {
                listener.mousePressed(event);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            final MouseEvent event = adjustMouseEvent(e);
            if (event != null) {
                listener.mouseReleased(event);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            final MouseEvent event = adjustMouseEvent(e);
            if (event != null) {
                listener.mouseEntered(event);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            final MouseEvent event = adjustMouseEvent(e);
            if (event != null) {
                listener.mouseExited(event);
            }
        }
    }
}

