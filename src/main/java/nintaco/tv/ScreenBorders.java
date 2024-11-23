package nintaco.tv;

import java.io.*;

public class ScreenBorders implements Serializable {

    private static final long serialVersionUID = 0;

    public static final ScreenBorders EMPTY_BORDERS
            = new ScreenBorders(0, 0, 0, 0);

    public final int top;
    public final int bottom;
    public final int left;
    public final int right;

    public ScreenBorders(final int top, final int bottom, final int left,
                         final int right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }
}
