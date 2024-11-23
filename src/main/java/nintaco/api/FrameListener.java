package nintaco.api;

/**
 * The listener interface for receiving render events. The class
 * that is interested in processing render events implements this
 * interface and the object created with that class is registered with
 * {@link API#addFrameListener(nintaco.api.FrameListener)
 * API.addFrameListener()}. Immediately after a full frame was rendered, that
 * object's {@code frameRendered} method will be invoked.
 */
@FunctionalInterface
public interface FrameListener {

    /**
     * Invoked immediately after a full frame was rendered, just before the frame
     * is displayed to the user. This method provides the opportunity to inspect
     * and modify the frame. If the {@link API} drawing methods are called prior
     * to this listener, the changes will actually be delayed and they will be
     * applied as if the drawing methods were called within this listener.
     *
     * @see API#clipRect(int, int, int, int) clipRect
     * @see API#copyArea(int, int, int, int, int, int) copyArea
     * @see API#createSprite(int, int, int, int[]) createSprite
     * @see API#draw3DRect(int, int, int, int, boolean) draw3DRect
     * @see API#drawArc(int, int, int, int, int, int) drawArc
     * @see API#drawChar(char, int, int) drawChar
     * @see API#drawChars(char[], int, int, int, int, boolean) drawChars
     * @see API#drawLine(int, int, int, int) drawLine
     * @see API#drawOval(int, int, int, int) drawOval
     * @see API#drawPolygon(int[], int[], int) drawPolygon
     * @see API#drawPolyline(int[], int[], int) drawPolyline
     * @see API#drawRect(int, int, int, int) drawRect
     * @see API#drawRoundRect(int, int, int, int, int, int) drawRoundRect
     * @see API#drawSprite(int, int, int) drawSprite
     * @see API#drawString(java.lang.String, int, int, boolean) drawString
     * @see API#fill3DRect(int, int, int, int, boolean) fill3DRect
     * @see API#fillArc(int, int, int, int, int, int) fillArc
     * @see API#fillOval(int, int, int, int) fillOval
     * @see API#fillPolygon(int[], int[], int) fillPolygon
     * @see API#fillRect(int, int, int, int) fillRect
     * @see API#fillRoundRect(int, int, int, int, int, int) fillRoundRect
     */
    void frameRendered();
}
