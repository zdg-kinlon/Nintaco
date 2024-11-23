package nintaco.api.server;

import nintaco.api.ActivateListener;

import static nintaco.api.local.EventTypes.Activate;

public class ServerActivateListener extends ServerListener
        implements ActivateListener {

    public ServerActivateListener(final ListenerLocker locker,
                                  final DataStream stream, final int listenerID) {
        super(locker, stream, Activate, listenerID);
    }

    @Override
    public void apiEnabled() {
        listenerInvoked();
    }
}
