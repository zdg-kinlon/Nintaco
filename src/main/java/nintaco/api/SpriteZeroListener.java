package nintaco.api;

/**
 * The listener interface for sprite zero hit events. The class that is
 * interested in processing sprite zero hit events implements this
 * interface and the object created with that class is registered with
 * {@link API#addSpriteZeroListener(nintaco.api.SpriteZeroListener)
 * API.addSpriteZeroListener()}. Implementations of this type are called back
 * immediately after the sprite zero flag is set.
 */
public interface SpriteZeroListener {

    /**
     * Invoked when the sprite zero flag is set.
     *
     * @param scanline      The scanline in which the sprite zero hit occurred.
     * @param scanlineCycle The dot within the scanline in which the sprite zero
     *                      hit occurred.
     * @see API#setSpriteZeroHit(boolean)
     * @see API#isSpriteZeroHit()
     */
    void spriteZeroHit(int scanline, int scanlineCycle);
}
