package nintaco.api;

/**
 * The listener interface for status change messages. The class that is
 * interested in processing status change messages implements this
 * interface and the object created with that class is registered with
 * {@link API#addStatusListener(nintaco.api.StatusListener)
 * API.addStatusListener()}. In the Remote API, implementations of this type
 * will be called back when the network connection is established or disrupted.
 */
public interface StatusListener {

    /**
     * Invoked to report a change in status.
     *
     * @param message Describes the new status.
     */
    void statusChanged(String message);
}
