package nintaco.api.server;

import nintaco.App;
import nintaco.api.*;
import nintaco.api.local.LocalAPI;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static nintaco.api.local.EventTypes.*;
import static nintaco.api.remote.RemoteBase.*;
import static nintaco.api.server.DataStream.ARRAY_LENGTH;
import static nintaco.util.ThreadUtil.sleep;

public abstract class LocalBase {

    public static final int MAX_SIZE = 1024;
    public static final long HEARTBEAT_MILLIS = 13_001L;
    public static final long FLUSH_MILLIS = 17_011L;

    protected final Map<Integer, Object> listeners = new HashMap<>();

    protected final int[] as = new int[ARRAY_LENGTH];
    protected final int[] bs = new int[ARRAY_LENGTH];
    protected final char[] cs = new char[ARRAY_LENGTH];
    protected final int[] emptyArray = new int[0];

    protected LocalAPI api;
    protected DataStream stream;
    protected ListenerLocker locker;
    protected Socket socket;
    protected Thread heartbeatThread;
    protected Thread flushThread;
    protected boolean disposed;

    protected volatile boolean running = true;

    public void run(final Socket socket) {

        try {
            this.socket = socket;

            stream = new DataStream(new DataOutputStream(new BufferedOutputStream(
                    socket.getOutputStream())), new DataInputStream(
                    new BufferedInputStream(socket.getInputStream())));

            locker = new ListenerLocker();

            heartbeatThread = new Thread(this::sendHeartbeats,
                    "Program Server Heartbeat Thread");
            flushThread = new Thread(this::sendFlushes,
                    "Program Server Flush Thread");

            heartbeatThread.start();
            flushThread.start();

            api = new LocalAPI();
            while (running) {
                call(stream.readByte());
            }
        } catch (final Throwable t) {
            //t.printStackTrace();
        } finally {
            dispose();
        }
    }

    private void sendHeartbeats() {
        try {
            while (running) {
                sleep(HEARTBEAT_MILLIS);
                locker.waitForRequest(false);
                stream.writeByte(HEARTBEAT);
                stream.flush();
                locker.waitForResponse();
            }
        } catch (final Throwable t) {
            dispose();
        }
    }

    private void sendFlushes() {
        try {
            while (running) {
                sleep(FLUSH_MILLIS);
                stream.flush();
            }
        } catch (final Throwable t) {
            dispose();
        }
    }

    public abstract void callMethod(final int methodValue) throws Throwable;

    public void call(final int methodValue) throws Throwable {

        switch (methodValue) {
            case Activate: {
                final int listenerID = stream.readInt();
                final ActivateListener listener = new ServerActivateListener(locker,
                        stream, listenerID);
                listeners.put(listenerID, listener);
                api.addActivateListener(listener);
                break;
            }
            case 2: {
                final int listenerID = stream.readInt();
                final ActivateListener listener = (ActivateListener) listeners
                        .remove(listenerID);
                if (listener != null) {
                    api.removeActivateListener(listener);
                }
                break;
            }
            case Deactivate: {
                final int listenerID = stream.readInt();
                final DeactivateListener listener = new ServerDeactivateListener(locker,
                        stream, listenerID);
                listeners.put(listenerID, listener);
                api.addDeactivateListener(listener);
                break;
            }
            case 4: {
                final int listenerID = stream.readInt();
                final DeactivateListener listener = (DeactivateListener) listeners
                        .remove(listenerID);
                if (listener != null) {
                    api.removeDeactivateListener(listener);
                }
                break;
            }
            case Stop: {
                final int listenerID = stream.readInt();
                final StopListener listener = new ServerStopListener(locker, stream,
                        listenerID);
                listeners.put(listenerID, listener);
                api.addStopListener(listener);
                break;
            }
            case 6: {
                final int listenerID = stream.readInt();
                final StopListener listener = (StopListener) listeners
                        .remove(listenerID);
                if (listener != null) {
                    api.removeStopListener(listener);
                }
                break;
            }
            case 7:
            case 8:
            case Access: {
                final int listenerID = stream.readInt();
                final AccessPointListener listener = new ServerAccessPointListener(
                        locker, stream, listenerID);
                listeners.put(listenerID, listener);
                api.addAccessPointListener(listener, stream.readInt(), stream.readInt(),
                        stream.readInt(), stream.readInt());
                break;
            }
            case 10: {
                final int listenerID = stream.readInt();
                final AccessPointListener listener = (AccessPointListener) listeners
                        .remove(listenerID);
                if (listener != null) {
                    api.removeAccessPointListener(listener);
                }
                break;
            }
            case Controllers: {
                final int listenerID = stream.readInt();
                final ControllersListener listener = new ServerControllersListener(
                        locker, stream, listenerID);
                listeners.put(listenerID, listener);
                api.addControllersListener(listener);
                break;
            }
            case 12: {
                final int listenerID = stream.readInt();
                final ControllersListener listener = (ControllersListener) listeners
                        .remove(listenerID);
                if (listener != null) {
                    api.removeControllersListener(listener);
                }
                break;
            }
            case Frame: {
                final int listenerID = stream.readInt();
                final FrameListener listener = new ServerFrameListener(locker, stream,
                        listenerID);
                listeners.put(listenerID, listener);
                api.addFrameListener(listener);
                break;
            }
            case 14: {
                final int listenerID = stream.readInt();
                final FrameListener listener = (FrameListener) listeners
                        .remove(listenerID);
                if (listener != null) {
                    api.removeFrameListener(listener);
                }
                break;
            }
            case Scanline: {
                final int listenerID = stream.readInt();
                final ScanlineListener listener = new ServerScanlineListener(locker,
                        stream, listenerID);
                listeners.put(listenerID, listener);
                api.addScanlineListener(listener, stream.readInt());
                break;
            }
            case 16: {
                final int listenerID = stream.readInt();
                final ScanlineListener listener = (ScanlineListener) listeners
                        .remove(listenerID);
                if (listener != null) {
                    api.removeScanlineListener(listener);
                }
                break;
            }
            case ScanlineCycle: {
                final int listenerID = stream.readInt();
                final ScanlineCycleListener listener = new ServerScanlineCycleListener(
                        locker, stream, listenerID);
                listeners.put(listenerID, listener);
                api.addScanlineCycleListener(listener, stream.readInt(),
                        stream.readInt());
                break;
            }
            case 18: {
                final int listenerID = stream.readInt();
                final ScanlineCycleListener listener = (ScanlineCycleListener) listeners
                        .remove(listenerID);
                if (listener != null) {
                    api.removeScanlineCycleListener(listener);
                }
                break;
            }
            case SpriteZero: {
                final int listenerID = stream.readInt();
                final SpriteZeroListener listener = new ServerSpriteZeroListener(
                        locker, stream, listenerID);
                listeners.put(listenerID, listener);
                api.addSpriteZeroListener(listener);
                break;
            }
            case 20: {
                final int listenerID = stream.readInt();
                final SpriteZeroListener listener = (SpriteZeroListener) listeners
                        .remove(listenerID);
                if (listener != null) {
                    api.removeSpriteZeroListener(listener);
                }
                break;
            }
            case Status: {
                final int listenerID = stream.readInt();
                final StatusListener listener = new ServerStatusListener(locker, stream,
                        listenerID);
                listeners.put(listenerID, listener);
                api.addStatusListener(listener);
                break;
            }
            case 22: {
                final int listenerID = stream.readInt();
                final StatusListener listener = (StatusListener) listeners
                        .remove(listenerID);
                if (listener != null) {
                    api.removeStatusListener(listener);
                }
                break;
            }
            case 100:
                stream.readIntArray(as);
                stream.readIntArray(bs);
                api.drawPolygon(as, bs, stream.readInt());
                break;
            case 101:
                stream.readIntArray(as);
                stream.readIntArray(bs);
                api.drawPolyline(as, bs, stream.readInt());
                break;
            case 109:
                stream.readIntArray(as);
                stream.readIntArray(bs);
                api.fillPolygon(as, bs, stream.readInt());
                break;
            case 113:
                stream.readCharArray(cs);
                api.drawChars(cs, stream.readInt(), stream.readInt(), stream.readInt(),
                        stream.readInt(), stream.readBoolean());
                break;
            case 114: {
                final int length = stream.readCharArray(cs);
                api.drawChars(cs, 0, length, stream.readInt(), stream.readInt(),
                        stream.readBoolean());
                break;
            }
            case 115: {
                final int id = stream.readInt();
                final int width = stream.readInt();
                final int height = stream.readInt();
                if (width < 0 || height < 0 || width > MAX_SIZE || height > MAX_SIZE) {
                    throw new IOException("Invalid sprite dimensions.");
                }
                final int[] pixels = new int[width * height];
                stream.readIntArray(pixels);
                api.createSprite(id, width, height, pixels);
                break;
            }
            case 119: {
                final int[] screen = api.getScreen();
                stream.writeIntArray(screen == null ? emptyArray : screen);
                break;
            }
            case 125:
                api.showMessage(stream.readString());
                break;
            case 128:
                api.open(stream.readString());
                break;
            case 129:
                api.openArchiveEntry(stream.readString(), stream.readString());
                break;
            case 130:
                stream.writeStringArray(api.getArchiveEntries(stream.readString()));
                break;
            case 131:
                stream.writeString(api.getDefaultArchiveEntry(stream.readString()));
                break;
            case 132:
                api.openDefaultArchiveEntry(stream.readString());
                break;
            case 134:
                api.saveState(stream.readString());
                break;
            case 135:
                api.loadState(stream.readString());
                break;
            case 138:
                api.setTVSystem(stream.readString());
                break;
            case 151:
                api.addCheat(stream.readInt(), stream.readInt(), stream.readInt(),
                        stream.readString(), stream.readBoolean());
                break;
            case 153:
                api.addGameGenie(stream.readString(), stream.readString(),
                        stream.readBoolean());
                break;
            case 154:
                api.removeGameGenie(stream.readString());
                break;
            case 155:
                api.addProActionRocky(stream.readString(), stream.readString(),
                        stream.readBoolean());
                break;
            case 156:
                api.removeProActionRocky(stream.readString());
                break;
            case 163:
                stream.writeInt(api.getStringWidth(stream.readString(),
                        stream.readBoolean()));
                break;
            case 164:
                stream.readCharArray(cs);
                stream.writeInt(api.getCharsWidth(cs, stream.readBoolean()));
                break;
            case READY:
                LocalAPI.setLocalAPI(api);
                App.setLocalAPI(api);
                break;
            case HEARTBEAT:
                break;
            case EVENT_RESPONSE:
                locker.responseReceived();
                break;
            case EVENT_REQUEST:
                locker.requestReceived();
                break;
            default:
                callMethod(methodValue);
                break;
        }
        stream.flush();
    }

    public void dispose() {

        synchronized (this) {
            if (disposed) {
                return;
            } else {
                disposed = true;
            }
        }

        running = false;

        final ListenerLocker listenerLocker = locker;
        if (listenerLocker != null) {
            listenerLocker.dispose();
        }

        final Socket s = socket;
        if (s != null) {
            try {
                s.close();
            } catch (final Throwable t) {
            }
        }

        final Thread ht = heartbeatThread;
        if (ht != null) {
            ht.interrupt();
        }

        final Thread ft = flushThread;
        if (ft != null) {
            ft.interrupt();
        }

        final LocalAPI localAPI = api;
        if (localAPI != null) {
            try {
                localAPI.dispose();
            } catch (final Throwable t) {
            }
        }
        LocalAPI.setLocalAPI(null);
    }
}
