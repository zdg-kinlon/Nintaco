package nintaco.api.server;

import nintaco.api.StatusListener;

import static nintaco.api.local.EventTypes.Status;

public class ServerStatusListener extends ServerListener
        implements StatusListener {

    public ServerStatusListener(final ListenerLocker locker,
                                final DataStream stream, final int listenerID) {
        super(locker, stream, Status, listenerID);
    }

    @Override
    public void statusChanged(final String message) {
        try {
            waitForRequest(false);
            stream.writeString(message);
            waitForResponse();
        } catch (final Throwable t) {
        }
    }
}