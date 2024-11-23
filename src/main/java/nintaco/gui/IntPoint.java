package nintaco.gui;

import java.io.Serializable;

public class IntPoint implements Serializable {

    private static final long serialVersionUID = 0;

    public int x;
    public int y;

    public IntPoint() {
    }

    public IntPoint(final IntPoint p) {
        this.x = p.x;
        this.y = p.y;
    }

    public IntPoint(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public void set(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public void translate(final IntPoint p) {
        translate(p.x, p.y);
    }

    public void translate(final int dx, final int dy) {
        this.x += dx;
        this.y += dy;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        final IntPoint p = (IntPoint) obj;
        return p.x == x && p.y == y;
    }

    @Override
    public int hashCode() {
        return 7 + x + 2017 * y;
    }
}
