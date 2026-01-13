package cn.kinlon.emu.gui.adapter;

import cn.kinlon.emu.gui.image.ImagePane;
import cn.kinlon.emu.input.InputUtil;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ImagePaneMouseAdapter extends MouseAdapter {
    private final ImagePane imagePane;

    public ImagePaneMouseAdapter(final ImagePane imagePane) {
        this.imagePane = imagePane;
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        InputUtil.setMouseCoordinates(-1);
        imagePane.showCursor();
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        imagePane.showCursor();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (x >= imagePane.imageX
                && y >= imagePane.imageY
                && x < imagePane.imageX + imagePane.imageWidth
                && y < imagePane.imageY + imagePane.imageHeight) {
            x = imagePane.screenBorders.left() + (x - imagePane.imageX) * (imagePane.IMAGE_WIDTH
                    - imagePane.screenBorders.left() - imagePane.screenBorders.right()) / imagePane.imageWidth;
            y = imagePane.screenBorders.top() + (y - imagePane.imageY) * (imagePane.IMAGE_HEIGHT
                    - imagePane.screenBorders.top() - imagePane.screenBorders.bottom()) / imagePane.imageHeight;
            InputUtil.setMouseCoordinates(imagePane.IMAGE_WIDTH * y + x);
        } else {
            InputUtil.setMouseCoordinates(-1);
        }
        imagePane.showCursor();
    }

}
