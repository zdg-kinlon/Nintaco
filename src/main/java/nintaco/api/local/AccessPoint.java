package nintaco.api.local;

import nintaco.api.AccessPointListener;

public class AccessPoint {

    public final AccessPointListener listener;
    public final int type;
    public final int minAddress;
    public final int maxAddress;
    public final int bank;

    public AccessPoint(final AccessPointListener listener, final int type,
                       final int minAddress) {
        this(listener, type, minAddress, -1, -1);
    }

    public AccessPoint(final AccessPointListener listener, final int type,
                       final int minAddress, final int maxAddress) {
        this(listener, type, minAddress, maxAddress, -1);
    }

    public AccessPoint(final AccessPointListener listener, final int type,
                       final int minAddress, final int maxAddress, final int bank) {

        this.listener = listener;
        this.type = type;
        this.bank = bank;

        if (maxAddress < 0) {
            this.minAddress = this.maxAddress = minAddress;
        } else if (minAddress <= maxAddress) {
            this.minAddress = minAddress;
            this.maxAddress = maxAddress;
        } else {
            this.minAddress = maxAddress;
            this.maxAddress = minAddress;
        }
    }

    public AccessPointListener getListener() {
        return listener;
    }

    public int getType() {
        return type;
    }

    public int getMinAddress() {
        return minAddress;
    }

    public int getMaxAddress() {
        return maxAddress;
    }

    public int getBank() {
        return bank;
    }
}
