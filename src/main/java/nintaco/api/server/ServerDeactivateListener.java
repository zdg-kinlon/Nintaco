package nintaco.api.server;

import nintaco.api.DeactivateListener;

import static nintaco.api.local.EventTypes.Deactivate;

public class ServerDeactivateListener extends ServerListener
        implements DeactivateListener {

    public ServerDeactivateListener(final ListenerLocker locker,
                                    final DataStream stream, final int listenerID) {
        super(locker, stream, Deactivate, listenerID);
    }

    @Override
    public void apiDisabled() {
        listenerInvoked();
    }
}
