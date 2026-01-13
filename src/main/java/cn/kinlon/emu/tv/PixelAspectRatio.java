package cn.kinlon.emu.tv;

import java.io.*;

public record PixelAspectRatio(int horizontal, int vertical) implements Serializable {

    private static final long serialVersionUID = 0;

    public static final PixelAspectRatio SQUARE = new PixelAspectRatio(1, 1);

}
