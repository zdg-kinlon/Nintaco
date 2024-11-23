package nintaco.api;

/**
 * The listener interface for stop events. The class
 * that is interested in processing stop events implements this
 * interface and the object created with that class is registered with
 * {@link API#addStopListener(nintaco.api.StopListener) API.addStopListener()}.
 * Implementations of this type are invoked when the Stop button is pressed in
 * the Run Program dialog. It is only applicable to the Local API.
 */
@FunctionalInterface
public interface StopListener {

    /**
     * <p>Invoked when the Stop button is pressed in the Run Program dialog. It
     * provides an opportunity to perform cleanup operations such as
     * terminating spawned threads, closing open streams and freeing allocated
     * resources. However, it is not necessary to remove listeners; all added
     * listeners will be automatically removed after this method returns.</p>
     *
     * <p>Pressing the Stop button does not disable the API. Consequentially, the
     * Stop button will not trigger {@link DeactivateListener}s. And, this method
     * can make API calls as part of the shutdown process.</p>
     *
     * @see DeactivateListener
     */
    void dispose();
}
