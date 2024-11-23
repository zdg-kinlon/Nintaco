package nintaco.api.server;

import nintaco.api.ScanlineListener;

import static nintaco.api.local.EventTypes.Scanline;

public class ServerScanlineListener extends ServerListener
        implements ScanlineListener {

    public ServerScanlineListener(final ListenerLocker locker,
                                  final DataStream stream, final int listenerID) {
        super(locker, stream, Scanline, listenerID);
    }

    @Override
    public void scanlineRendered(final int scanline) {
        try {
            waitForRequest(false);
            stream.writeInt(scanline);
            waitForResponse();
        } catch (final Throwable t) {
        }
    }
}
