package nintaco.api;

/**
 * The listener interface for receiving controller events. The class
 * that is interested in processing controller events implements this
 * interface and the object created with that class is registered with
 * {@link API#addControllersListener(nintaco.api.ControllersListener)
 * API.addControllersListener()}. Immediately after the controllers were probed
 * for data, that object's {@code controllersProbed} method will be invoked.
 */
@FunctionalInterface
public interface ControllersListener {

    /**
     * Invoked immediately after the controllers were probed for data, just
     * before the probed data is exposed to the machine. This method provides the
     * opportunity to inspect and modify the cached controller values that will
     * be available during the subsequent frame generation period. If the
     * {@link API} controller modifiers are called prior to this listener,
     * the changes will actually be delayed and they will be applied as if the
     * modifiers were called within this listener.
     *
     * @see API#readGamepad(int, int) readGamepad
     * @see API#writeGamepad(int, int, boolean) writeGamepad
     * @see API#isZapperTrigger() isZapperTrigger
     * @see API#setZapperTrigger(boolean) setZapperTrigger
     * @see API#getZapperX() getZapperX
     * @see API#setZapperX(int) setZapperX
     * @see API#getZapperY() getZapperY
     * @see API#setZapperY(int) setZapperY
     */
    void controllersProbed();
}
