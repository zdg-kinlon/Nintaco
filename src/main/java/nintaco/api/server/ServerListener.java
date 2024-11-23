package nintaco.api.server;

public class ServerListener {

    protected final ListenerLocker locker;
    protected final DataStream stream;
    protected final int eventType;
    protected final int listenerID;

    public ServerListener(final ListenerLocker locker,
                          final DataStream stream, final int eventType,
                          final int listenerID) {
        this.locker = locker;
        this.stream = stream;
        this.eventType = eventType;
        this.listenerID = listenerID;
    }

    protected void waitForRequest(final boolean generatesResult)
            throws Throwable {
        locker.waitForRequest(generatesResult);
        stream.writeByte(eventType);
        stream.writeInt(listenerID);
    }

    protected void waitForResponse() throws Throwable {
        stream.flush();
        locker.waitForResponse();
    }

    protected void listenerInvoked() {
        try {
            waitForRequest(false);
            waitForResponse();
        } catch (final Throwable t) {
        }
    }
}
