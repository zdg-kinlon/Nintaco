package nintaco.api.server;

import nintaco.api.ScanlineCycleListener;

import static nintaco.api.local.EventTypes.ScanlineCycle;

public class ServerScanlineCycleListener extends ServerListener
        implements ScanlineCycleListener {

    public ServerScanlineCycleListener(final ListenerLocker locker,
                                       final DataStream stream, final int listenerID) {
        super(locker, stream, ScanlineCycle, listenerID);
    }

    @Override
    public void cyclePerformed(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {
        try {
            waitForRequest(false);
            stream.writeInt(scanline);
            stream.writeInt(scanlineCycle);
            stream.writeInt(address);
            stream.writeBoolean(rendering);
            waitForResponse();
        } catch (final Throwable t) {
        }
    }
}
