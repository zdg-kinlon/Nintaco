package nintaco.gui.watchhistory;

import nintaco.gui.historyeditor.tasks.FrameRenderer;
import nintaco.palettes.PaletteUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import static nintaco.gui.image.ImagePane.IMAGE_HEIGHT;
import static nintaco.gui.image.ImagePane.IMAGE_WIDTH;
import static nintaco.tv.TVSystem.NTSC;

public class PreviewPane extends JComponent implements FrameRenderer {

    private final BufferedImage image = new BufferedImage(IMAGE_WIDTH,
            IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
    private final int[] screen = ((DataBufferInt) image.getRaster()
            .getDataBuffer()).getData();
    private final Dimension preferredSize = new Dimension(IMAGE_WIDTH,
            IMAGE_HEIGHT);

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int[] getScreen() {
        return screen;
    }

    @Override
    public void render(final int[] screen) {
        final int[] palette = PaletteUtil.getExtendedPalette(NTSC);
        for (int i = screen.length - 1; i >= 0; i--) {
            this.screen[i] = palette[screen[i]];
        }
    }

    public void drawRectangle() {
        for (int i = 255; i >= 0; i--) {
            screen[i] = 0;
            screen[61184 | i] = 0;
        }
        for (int i = 238; i > 0; i--) {
            screen[i << 8] = 0;
            screen[(i << 8) | 255] = 0;
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        g.drawImage(image, 0, 0, null);
    }

    public void dispose() {
        image.flush();
    }
}
