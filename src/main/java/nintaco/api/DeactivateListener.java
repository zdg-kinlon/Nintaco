package nintaco.api;

/**
 * <p>The listener interface for API disabled events. The class
 * that is interested in processing API disabled events implements this
 * interface and the object created with that class is registered with
 * {@link API#addDeactivateListener(nintaco.api.DeactivateListener)
 * API.addDeactivateListener()}.</p>
 *
 * <p>Implementations of this type will be called back when a file that is
 * actively running in the emulator is closed. In addition, the Remote API is
 * disabled if the socket connection is disrupted.</p>
 */
@FunctionalInterface
public interface DeactivateListener {

    /**
     * Invoked when the API transitions from enabled to disabled.
     */
    void apiDisabled();
}
