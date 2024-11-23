package nintaco.api.server;

import nintaco.api.AccessPointListener;

import static nintaco.api.local.EventTypes.Access;

public class ServerAccessPointListener extends ServerListener
        implements AccessPointListener {

    public ServerAccessPointListener(final ListenerLocker locker,
                                     final DataStream stream, final int listenerID) {
        super(locker, stream, Access, listenerID);
    }

    @Override
    public int accessPointHit(final int type, final int address,
                              final int value) {
        try {
            waitForRequest(true);
            stream.writeInt(type);
            stream.writeInt(address);
            stream.writeInt(value);
            waitForResponse();
            return stream.readInt();
        } catch (final Throwable t) {
            return -1;
        } finally {
            locker.resultReceived();
        }
    }
}
