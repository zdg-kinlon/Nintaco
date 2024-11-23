package nintaco.api;

/**
 * Contains a set of constants used as an argument in {@link
 * API#addAccessPointListener(nintaco.api.AccessPointListener, int, int, int,
 * int) API.addAccessPointListener()} to indicate the type of access points that
 * trigger the associated {@link AccessPointListener}.
 *
 * @see AccessPointListener
 */
public interface AccessPointType {

    /**
     * Occurs just before a CPU Memory read, providing an opportunity to skip
     * the read and to substitute a value in its place.
     */
    int PreRead = 0;

    /**
     * Occurs just after a CPU Memory read, providing an opportunity to substitute
     * the read value.
     */
    int PostRead = 1;

    /**
     * Occurs just before a CPU Memory write, providing an opportunity to
     * substitute the value to be written.
     */
    int PreWrite = 2;

    /**
     * Occurs just after a CPU Memory write, too late to affect the behavior of
     * the write.
     */
    int PostWrite = 3;

    /**
     * Occurs just before an instruction is executed.
     */
    int PreExecute = 4;

    /**
     * Occurs just after an instruction is executed.
     */
    int PostExecute = 5;
}
