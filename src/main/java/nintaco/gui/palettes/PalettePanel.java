package nintaco.gui.palettes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PalettePanel extends JPanel {

    private static final int PREFERRED_SQUARE_SIZE = 42;
    private static final Dimension PREFERRED_SIZE
            = new Dimension(16 * PREFERRED_SQUARE_SIZE, 4 * PREFERRED_SQUARE_SIZE);

    private final PaletteOptionsDialog paletteOptionsDialog;

    private int[] palette = new int[64];

    private int panelWidth;
    private int panelHeight;
    private int squareSize;
    private int imageX;
    private int imageY;
    private int imageWidth;
    private int imageHeight;

    private int selectedX;
    private int selectedY;
    private int selectedIndex = -1;

    public PalettePanel(final PaletteOptionsDialog paletteOptionsDialog) {

        this.paletteOptionsDialog = paletteOptionsDialog;

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                panelResized();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                updateSelectedIndex(e.getX(), e.getY());
                if (selectedIndex >= 0) {
                    final Color newColor = JColorChooser.showDialog(PalettePanel.this,
                            "Select Palette Color", new Color(palette[selectedIndex]));
                    if (newColor != null) {
                        palette[selectedIndex] = newColor.getRGB() & 0xFFFFFF;
                        paletteOptionsDialog.paletteSelectionChanged(selectedIndex,
                                palette[selectedIndex]);
                    }
                }
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selectedIndex = -1;
                paletteOptionsDialog.paletteSelectionChanged(-1, -1);
                repaint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(final MouseEvent e) {
                updateSelectedIndex(e.getX(), e.getY());
                repaint();
            }
        });
    }

    private void updateSelectedIndex(final int mouseX, final int mouseY) {
        final int x = mouseX - imageX;
        final int y = mouseY - imageY;
        if (x < 0 || y < 0 || x >= imageWidth || y >= imageHeight) {
            selectedIndex = -1;
        } else {
            final int X = x / squareSize;
            final int Y = y / squareSize;
            selectedIndex = (Y << 4) | X;
            selectedX = imageX + squareSize * X;
            selectedY = imageY + squareSize * Y;
        }
        paletteOptionsDialog.paletteSelectionChanged(selectedIndex,
                selectedIndex < 0 ? -1 : palette[selectedIndex]);
    }

    private void panelResized() {
        panelWidth = getWidth();
        panelHeight = getHeight();
        squareSize = Math.min(panelWidth >> 4, panelHeight >> 2);
        imageWidth = squareSize << 4;
        imageHeight = squareSize << 2;
        imageX = (panelWidth - imageWidth) >> 1;
        imageY = (panelHeight - imageHeight) >> 1;
    }

    @Override
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }

    public int[] getPalette() {
        return palette;
    }

    public void setPalette(final int[] palette) {
        if (palette == null) {
            this.palette = new int[64];
        } else {
            this.palette = palette;
        }
        repaint();
    }

    public void getPalette(final int[] target) {
        System.arraycopy(palette, 0, target, 0, palette.length);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        panelResized();
        super.paintComponent(g);
        for (int y = 3; y >= 0; y--) {
            for (int x = 15; x >= 0; x--) {
                g.setColor(new Color(palette[(y << 4) | x]));
                g.fillRect(imageX + squareSize * x, imageY + squareSize * y, squareSize,
                        squareSize);
            }
        }
        g.setColor(Color.BLACK);
        g.drawRect(imageX, imageY, imageWidth - 1, imageHeight - 1);
        if (selectedIndex >= 0) {
            g.setXORMode(Color.WHITE);
            g.drawRect(selectedX, selectedY, squareSize - 1, squareSize - 1);
        }
    }
}
