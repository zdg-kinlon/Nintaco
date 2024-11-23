package nintaco.gui;

import java.io.Serializable;

public class DoublePoint implements Serializable {

    private static final long serialVersionUID = 0;

    public double x;
    public double y;

    public DoublePoint() {
    }

    public DoublePoint(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public void set(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public void translate(final DoublePoint p) {
        translate(p.x, p.y);
    }

    public void translate(final double dx, final double dy) {
        this.x += dx;
        this.y += dy;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("(%f, %f)", x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        final DoublePoint p = (DoublePoint) obj;
        return p.x == x && p.y == y;
    }

    @Override
    public int hashCode() {
        return (int) (7 + x + 2017 * y);
    }
}