package nintaco.api.remote;

import nintaco.api.*;
import nintaco.api.local.AccessPoint;
import nintaco.api.local.ScanlineCyclePoint;
import nintaco.api.local.ScanlinePoint;
import nintaco.api.server.DataStream;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import static nintaco.api.local.EventTypes.*;
import static nintaco.util.ThreadUtil.sleep;

public abstract class RemoteBase implements API {

    public static final int EVENT_REQUEST = 0xFF;
    public static final int EVENT_RESPONSE = 0xFE;
    public static final int HEARTBEAT = 0xFD;
    public static final int READY = 0xFC;
    public static final long RETRY_MILLIS = 1000L;
    private static final int[] EVENT_TYPES = {Activate, Deactivate, Stop, Access,
            Controllers, Frame, Scanline, ScanlineCycle, SpriteZero, Status};
    // listener -> listenerID
    protected final Map<Object, Integer> listenerIDs = new IdentityHashMap<>();

    // eventType -> listenerID -> listenerObject(listener)
    protected final Map<Integer, Map<Integer, Object>> listenerObjects
            = new HashMap<>();

    protected String host;
    protected int port;
    protected DataStream stream;
    protected int nextID;
    protected boolean running;

    public RemoteBase(final String host, final int port) {
        this.host = host;
        this.port = port;
        for (final int eventType : EVENT_TYPES) {
            listenerObjects.put(eventType, new HashMap<>());
        }
    }

    @Override
    public void run() {
        if (running) {
            return;
        } else {
            running = true;
        }
        while (true) {
            fireStatusChanged("Connecting to %s:%d...", host, port);
            final Socket socket;
            try {
                socket = new Socket(host, port);
                stream = new DataStream(new DataOutputStream(new BufferedOutputStream(
                        socket.getOutputStream())), new DataInputStream(
                        new BufferedInputStream(socket.getInputStream())));
            } catch (final Throwable t) {
                fireStatusChanged("Failed to establish connection.");
            }
            if (stream != null) {
                try {
                    fireStatusChanged("Connection established.");
                    sendListeners();
                    sendReady();
                    while (true) {
                        probeEvents();
                    }
                } catch (final EOFException e) {
                    fireDeactivated();
                    fireStatusChanged("Disconnected.");
                } catch (final Throwable t) {
                    t.printStackTrace();
                    fireDeactivated();
                    fireStatusChanged("Disconnected.");
                } finally {
                    stream = null;
                }
            }
            sleep(RETRY_MILLIS);
        }
    }

    protected void fireDeactivated() {
        for (final Object obj : new ArrayList<>(listenerObjects.get(Deactivate)
                .values())) {
            ((DeactivateListener) obj).apiDisabled();
        }
    }

    protected void fireStatusChanged(final String message,
                                     final Object... params) {
        final String msg = String.format(message, params);
        for (final Object obj : new ArrayList<>(listenerObjects.get(Status)
                .values())) {
            ((StatusListener) obj).statusChanged(msg);
        }
    }

    protected void sendReady() {
        if (stream != null) {
            try {
                stream.writeByte(READY);
                stream.flush();
            } catch (final Throwable t) {
            }
        }
    }

    protected void sendListeners() {
        for (Map.Entry<Integer, Map<Integer, Object>> e1
                : listenerObjects.entrySet()) {
            for (Map.Entry<Integer, Object> e2 : e1.getValue().entrySet()) {
                sendListener(e2.getKey(), e1.getKey(), e2.getValue());
            }
        }
    }

    protected void probeEvents() throws Throwable {

        stream.writeByte(EVENT_REQUEST);
        stream.flush();

        final int eventType = stream.readByte();

        if (eventType == HEARTBEAT) {
            stream.writeByte(EVENT_RESPONSE);
            stream.flush();
            return;
        }

        final int listenerID = stream.readInt();
        final Object obj = listenerObjects.get(eventType).get(listenerID);

        if (obj != null) {
            if (eventType == Access) {
                final int type = stream.readInt();
                final int address = stream.readInt();
                final int value = stream.readInt();
                final int result = ((AccessPoint) obj).getListener().accessPointHit(
                        type, address, value);
                stream.writeByte(EVENT_RESPONSE);
                stream.writeInt(result);
            } else {
                switch (eventType) {
                    case Activate:
                        ((ActivateListener) obj).apiEnabled();
                        break;
                    case Deactivate:
                        ((DeactivateListener) obj).apiDisabled();
                        break;
                    case Stop:
                        ((StopListener) obj).dispose();
                        break;
                    case Controllers:
                        ((ControllersListener) obj).controllersProbed();
                        break;
                    case Frame:
                        ((FrameListener) obj).frameRendered();
                        break;
                    case Scanline:
                        ((ScanlinePoint) obj).getListener().scanlineRendered(
                                stream.readInt());
                        break;
                    case ScanlineCycle:
                        ((ScanlineCyclePoint) obj).getListener().cyclePerformed(
                                stream.readInt(), stream.readInt(), stream.readInt(),
                                stream.readBoolean());
                        break;
                    case SpriteZero:
                        ((SpriteZeroListener) obj).spriteZeroHit(stream.readInt(),
                                stream.readInt());
                        break;
                    case Status:
                        ((StatusListener) obj).statusChanged(stream.readString());
                        break;
                    default:
                        throw new IOException("Unknown listener type: " + eventType);
                }
                stream.writeByte(EVENT_RESPONSE);
            }
        }

        stream.flush();
    }

    protected void sendListener(final int listenerID, final int eventType,
                                final Object listenerObject) {
        if (stream != null) {
            try {
                stream.writeByte(eventType);
                stream.writeInt(listenerID);
                switch (eventType) {
                    case Access: {
                        final AccessPoint point = (AccessPoint) listenerObject;
                        stream.writeInt(point.type);
                        stream.writeInt(point.minAddress);
                        stream.writeInt(point.maxAddress);
                        stream.writeInt(point.bank);
                        break;
                    }
                    case Scanline: {
                        final ScanlinePoint point = (ScanlinePoint) listenerObject;
                        stream.writeInt(point.scanline);
                        break;
                    }
                    case ScanlineCycle: {
                        final ScanlineCyclePoint point = (ScanlineCyclePoint) listenerObject;
                        stream.writeInt(point.scanline);
                        stream.writeInt(point.scanlineCycle);
                        break;
                    }
                }
                stream.flush();
            } catch (final Throwable t) {
            }
        }
    }

    protected void addListener(final Object listener, final int eventType) {
        if (listener != null) {
            sendListener(addListenerObject(listener, eventType), eventType,
                    listener);
        }
    }

    public void removeListener(final Object listener, final int eventType,
                               final int methodValue) {
        if (listener != null) {
            final int listenerID = removeListenerObject(listener, eventType);
            if (listenerID >= 0 && stream != null) {
                try {
                    stream.writeByte(methodValue);
                    stream.writeInt(listenerID);
                    stream.flush();
                } catch (final Throwable t) {
                }
            }
        }
    }

    protected int addListenerObject(final Object listener,
                                    final int eventType) {
        return addListenerObject(listener, eventType, listener);
    }

    protected int addListenerObject(final Object listener,
                                    final int eventType, final Object listenerObject) {
        final Integer listenerID = nextID++;
        listenerIDs.put(listener, listenerID);
        listenerObjects.get(eventType).put(listenerID, listenerObject);
        return listenerID;
    }

    protected int removeListenerObject(final Object listener,
                                       final int eventType) {
        final Integer listenerID = listenerIDs.remove(listener);
        if (listenerID != null) {
            listenerObjects.get(eventType).remove(listenerID);
            return listenerID;
        } else {
            return -1;
        }
    }

    @Override
    public void addActivateListener(final ActivateListener listener) {
        addListener(listener, Activate);
    }

    @Override
    public void removeActivateListener(final ActivateListener listener) {
        removeListener(listener, Activate, 2);
    }

    @Override
    public void addDeactivateListener(final DeactivateListener listener) {
        addListener(listener, Deactivate);
    }

    @Override
    public void removeDeactivateListener(final DeactivateListener listener) {
        removeListener(listener, Deactivate, 4);
    }

    @Override
    public void addStopListener(final StopListener listener) {
        addListener(listener, Stop);
    }

    @Override
    public void removeStopListener(final StopListener listener) {
        removeListener(listener, Stop, 6);
    }

    @Override
    public void addAccessPointListener(final AccessPointListener listener,
                                       final int accessPointType, final int address) {
        addAccessPointListener(listener, accessPointType, address, -1, -1);
    }

    @Override
    public void addAccessPointListener(final AccessPointListener listener,
                                       final int accessPointType, final int minAddress, final int maxAddress) {
        addAccessPointListener(listener, accessPointType, minAddress, maxAddress,
                -1);
    }

    @Override
    public void addAccessPointListener(final AccessPointListener listener,
                                       final int accessPointType, final int minAddress, final int maxAddress,
                                       final int bank) {

        if (listener != null) {
            final AccessPoint point = new AccessPoint(listener, accessPointType,
                    minAddress, maxAddress, bank);
            sendListener(addListenerObject(listener, Access, point), Access, point);
        }
    }

    @Override
    public void removeAccessPointListener(final AccessPointListener listener) {
        removeListener(listener, Access, 10);
    }

    @Override
    public void addControllersListener(final ControllersListener listener) {
        addListener(listener, Controllers);
    }

    @Override
    public void removeControllersListener(final ControllersListener listener) {
        removeListener(listener, Controllers, 12);
    }

    @Override
    public void addFrameListener(final FrameListener listener) {
        addListener(listener, Frame);
    }

    @Override
    public void removeFrameListener(final FrameListener listener) {
        removeListener(listener, Frame, 14);
    }

    @Override
    public void addScanlineListener(final ScanlineListener listener,
                                    final int scanline) {

        if (listener != null) {
            final ScanlinePoint point = new ScanlinePoint(listener, scanline);
            sendListener(addListenerObject(listener, Scanline, point), Scanline,
                    point);
        }
    }

    @Override
    public void removeScanlineListener(final ScanlineListener listener) {
        removeListener(listener, Scanline, 16);
    }

    @Override
    public void addScanlineCycleListener(final ScanlineCycleListener listener,
                                         final int scanline, final int scanlineCycle) {

        if (listener != null) {
            final ScanlineCyclePoint point = new ScanlineCyclePoint(listener,
                    scanline, scanlineCycle);
            sendListener(addListenerObject(listener, ScanlineCycle, point),
                    ScanlineCycle, point);
        }
    }

    @Override
    public void removeScanlineCycleListener(
            final ScanlineCycleListener listener) {
        removeListener(listener, ScanlineCycle, 18);
    }

    @Override
    public void addSpriteZeroListener(final SpriteZeroListener listener) {
        addListener(listener, SpriteZero);
    }

    @Override
    public void removeSpriteZeroListener(final SpriteZeroListener listener) {
        removeListener(listener, SpriteZero, 20);
    }

    @Override
    public void addStatusListener(final StatusListener listener) {
        addListener(listener, Status);
    }

    @Override
    public void removeStatusListener(final StatusListener listener) {
        removeListener(listener, Status, 22);
    }

    @Override
    public void getPixels(final int[] pixels) {
        try {
            stream.writeByte(119);
            stream.flush();
            stream.readIntArray(pixels);
        } catch (final Throwable t) {
        }
    }
}