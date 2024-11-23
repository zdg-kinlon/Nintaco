package nintaco.api;

/**
 * <p>The listener interface for access point events. The class
 * that is interested in processing access point events implements this
 * interface and the object created with that class is registered with one of
 * the {@link API#addAccessPointListener(nintaco.api.AccessPointListener, int,
 * int, int, int) API.addAccessPointListener()} methods.</p>
 *
 * <p>An access point is a specified CPU Memory read or write, or instruction
 * execution point. When an access point is defined, it is associated with an
 * implementation of this listener, providing a callback when the access point
 * is hit.</p>
 *
 * <p>The CPU Memory read and write methods of {@link API} do not trigger
 * access points. Consequentially, they can be called within this listener
 * without causing recursive loops.</p>
 */
@FunctionalInterface
public interface AccessPointListener {

    /**
     * Invoked when an access point is encountered.
     *
     * @param accessPointType One of the {@link AccessPointType} constants,
     *                        indicating the type of access point that was encountered.
     * @param address         The CPU Memory address where the access point was
     *                        encountered.
     * @param value           {@link AccessPointType#PostRead PostRead},
     *                        {@link AccessPointType#PreWrite PreWrite} and
     *                        {@link AccessPointType#PostWrite PostWrite} provide the read value, the
     *                        value to be written and the written value respectively. The remaining
     *                        access point types use {@code -1} to indicate no value.
     * @return {@link AccessPointType#PostRead PreRead},
     * {@link AccessPointType#PostRead PostRead} and
     * {@link AccessPointType#PostRead PreWrite} handlers can optionally return
     * a substitute byte value in place of the value to be read, the read value
     * and the value to be written respectively, in the lower 8 bits. Or, they can
     * return {@code -1} to indicate no modified behavior. The returned value of
     * the remaining access point types is ignored.
     * @see AccessPointType
     */
    int accessPointHit(int accessPointType, int address, int value);
}
