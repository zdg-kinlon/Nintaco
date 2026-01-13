package cn.kinlon.emu.gui.adapter;

import cn.kinlon.emu.gui.image.ImagePane;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ImagePaneComponentAdapter extends ComponentAdapter {
    private final ImagePane imagePane;

    public ImagePaneComponentAdapter(final ImagePane imagePane) {
        this.imagePane = imagePane;
    }

    @Override
    public void componentShown(final ComponentEvent e) {
        imagePane.paneResized();
    }

    @Override
    public void componentResized(final ComponentEvent e) {
        imagePane.paneResized();
    }
}
