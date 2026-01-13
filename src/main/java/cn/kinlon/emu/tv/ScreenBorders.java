package cn.kinlon.emu.tv;

import java.io.*;

public record ScreenBorders(int top, int bottom, int left, int right) implements Serializable {

    private static final long serialVersionUID = 0;

    public static final ScreenBorders EMPTY_BORDERS
            = new ScreenBorders(0, 0, 0, 0);

}
