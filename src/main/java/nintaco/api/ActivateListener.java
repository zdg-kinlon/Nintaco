package nintaco.api;

/**
 * <p>The listener interface for API enabled events. The class
 * that is interested in processing API enabled events implements this
 * interface and the object created with that class is registered with
 * {@link API#addActivateListener(nintaco.api.ActivateListener)
 * API.addActivateListener()}. It provides an implementation with the
 * opportunity to perform initialization tasks.</p>
 *
 * <p>A listener of this type is called back immediately after the
 * {@link API#run()} method is invoked if the emulator is actively running a
 * file; otherwise, it will be triggered when a file is opened. The Remote API
 * is not enabled until a socket connection is established.</p>
 */
@FunctionalInterface
public interface ActivateListener {

    /**
     * Invoked when the API is enabled.
     */
    void apiEnabled();
}
