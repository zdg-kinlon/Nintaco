package nintaco.api.server;

import nintaco.api.SpriteZeroListener;

import static nintaco.api.local.EventTypes.SpriteZero;

public class ServerSpriteZeroListener extends ServerListener
        implements SpriteZeroListener {

    public ServerSpriteZeroListener(final ListenerLocker locker,
                                    final DataStream stream, final int listenerID) {
        super(locker, stream, SpriteZero, listenerID);
    }

    @Override
    public void spriteZeroHit(final int scanline, final int scanlineCycle) {
        try {
            waitForRequest(false);
            stream.writeInt(scanline);
            stream.writeInt(scanlineCycle);
            waitForResponse();
        } catch (final Throwable t) {
        }
    }
}
