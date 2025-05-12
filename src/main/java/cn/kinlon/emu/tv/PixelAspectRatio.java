package cn.kinlon.emu.tv;

import java.io.*;

public class PixelAspectRatio implements Serializable {

    private static final long serialVersionUID = 0;

    public static final PixelAspectRatio SQUARE = new PixelAspectRatio(1, 1);

    public final int horizontal;
    public final int vertical;

    public PixelAspectRatio(final int horizontal, final int vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

}
