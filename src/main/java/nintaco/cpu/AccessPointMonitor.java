package nintaco.cpu;

import nintaco.api.local.AccessPoint;

import java.util.ArrayList;
import java.util.List;

import static nintaco.api.AccessPointType.*;
import static nintaco.api.AccessPointType.PostRead;
import static nintaco.api.AccessPointType.PostWrite;
import static nintaco.api.AccessPointType.PreWrite;
import static nintaco.util.CollectionsUtil.convertToArray;
import static nintaco.util.CollectionsUtil.isBlank;

public class AccessPointMonitor {
    private final CPU cpu;

    public AccessPointMonitor(CPU cpu) {
        this.cpu = cpu;
    }

    private volatile transient AccessPoint[] preReadAccessPoints;
    private volatile transient AccessPoint[] postReadAccessPoints;
    private volatile transient AccessPoint[] preWriteAccessPoints;
    private volatile transient AccessPoint[] postWriteAccessPoints;
    private volatile transient AccessPoint[] preExecuteAccessPoints;
    private volatile transient AccessPoint[] postExecuteAccessPoints;

    public void setAccessPoints(final AccessPoint[] accessPoints) {
        final List[] as = new List[6];
        if (!isBlank(accessPoints)) {
            for (int i = as.length - 1; i >= 0; i--) {
                as[i] = new ArrayList<>();
            }
            for (final AccessPoint accessPoint : accessPoints) {
                if (accessPoint.getMinAddress() >= 0x0000
                        && accessPoint.getMinAddress() <= 0xFFFF) {
                    as[accessPoint.getType()].add(accessPoint);
                }
            }
        }
        preReadAccessPoints = convertToArray(AccessPoint.class, as[PreRead]);
        postReadAccessPoints = convertToArray(AccessPoint.class, as[PostRead]);
        preWriteAccessPoints = convertToArray(AccessPoint.class, as[PreWrite]);
        postWriteAccessPoints = convertToArray(AccessPoint.class, as[PostWrite]);
        preExecuteAccessPoints = convertToArray(AccessPoint.class, as[PreExecute]);
        postExecuteAccessPoints = convertToArray(AccessPoint.class, as[PostExecute]);
    }

    public void preExecuteAccessPoints(int pc) {
        final AccessPoint[] preAccessPoints = preExecuteAccessPoints;
        if (preAccessPoints != null) {
            for (int i = preAccessPoints.length - 1; i >= 0; i--) {
                final AccessPoint accessPoint = preAccessPoints[i];
                if (accessPoint.minAddress <= pc
                        && pc <= accessPoint.maxAddress && (accessPoint.bank < 0
                        || accessPoint.bank == cpu.mapper().getPrgBank(pc))) {
                    accessPoint.listener.accessPointHit(PreExecute, pc, -1);
                }
            }
        }
    }

    public void postExecuteAccessPoints(int pc) {
        final AccessPoint[] postAccessPoints = postExecuteAccessPoints;
        if (postAccessPoints != null) {
            for (int i = postAccessPoints.length - 1; i >= 0; i--) {
                final AccessPoint accessPoint = postAccessPoints[i];
                if (accessPoint.minAddress <= pc
                        && pc <= accessPoint.maxAddress && (accessPoint.bank < 0
                        || accessPoint.bank == cpu.mapper().getPrgBank(pc))) {
                    accessPoint.listener.accessPointHit(PostExecute, pc, -1);
                }
            }
        }
    }

    public int preWriteAccessPoints(int address, int value) {
        final AccessPoint[] preAccessPoints = preWriteAccessPoints;
        if (preAccessPoints != null) {
            for (int i = preAccessPoints.length - 1; i >= 0; i--) {
                final AccessPoint accessPoint = preAccessPoints[i];
                if (accessPoint.minAddress <= address
                        && address <= accessPoint.maxAddress && (accessPoint.bank < 0
                        || accessPoint.bank == cpu.mapper().getPrgBank(address))) {
                    final int v = accessPoint.listener.accessPointHit(PreWrite,
                            address, value);
                    if (v >= 0) {
                        value = v & 0xFF;
                    }
                }
            }
        }
        return value;
    }

    public void postWriteAccessPoints(int address, int value) {
        final AccessPoint[] postAccessPoints = postWriteAccessPoints;
        if (postAccessPoints != null) {
            for (int i = postAccessPoints.length - 1; i >= 0; i--) {
                final AccessPoint accessPoint = postAccessPoints[i];
                if (accessPoint.minAddress <= address
                        && address <= accessPoint.maxAddress && (accessPoint.bank < 0
                        || accessPoint.bank == cpu.mapper().getPrgBank(address))) {
                    accessPoint.listener.accessPointHit(PostWrite, address, value);
                }
            }
        }
    }

    public int preReadAccessPoints(int address) {
        final AccessPoint[] preAccessPoints = preReadAccessPoints;
        if (preAccessPoints != null) {
            for (int i = preAccessPoints.length - 1; i >= 0; i--) {
                final AccessPoint accessPoint = preAccessPoints[i];
                if (accessPoint.minAddress <= address
                        && address <= accessPoint.maxAddress && (accessPoint.bank < 0
                        || accessPoint.bank == cpu.mapper().getPrgBank(address))) {
                    final int v = accessPoint.listener.accessPointHit(PreRead,
                            address, -1);
                    if (v >= 0) {
                        return v & 0xFF;
                    }
                }
            }
        }
        return -1;
    }

    public int postReadAccessPoints(int address, int value) {
        final AccessPoint[] postAccessPoints = postReadAccessPoints;
        if (postAccessPoints != null) {
            for (int i = postAccessPoints.length - 1; i >= 0; i--) {
                final AccessPoint accessPoint = postAccessPoints[i];
                if (accessPoint.minAddress <= address
                        && address <= accessPoint.maxAddress && (accessPoint.bank < 0
                        || accessPoint.bank == cpu.mapper().getPrgBank(address))) {
                    final int v = accessPoint.listener.accessPointHit(PostRead,
                            address, value);
                    if (v >= 0) {
                        return v & 0xFF;
                    }
                }
            }
        }
        return -1;
    }
}
