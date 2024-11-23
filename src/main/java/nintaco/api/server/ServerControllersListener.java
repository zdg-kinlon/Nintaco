package nintaco.api.server;

import nintaco.api.ControllersListener;

import static nintaco.api.local.EventTypes.Controllers;

public class ServerControllersListener extends ServerListener
        implements ControllersListener {

    public ServerControllersListener(final ListenerLocker locker,
                                     final DataStream stream, final int listenerID) {
        super(locker, stream, Controllers, listenerID);
    }

    @Override
    public void controllersProbed() {
        listenerInvoked();
    }
}
