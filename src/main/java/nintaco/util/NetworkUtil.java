package nintaco.util;

import java.net.*;
import java.util.*;

public final class NetworkUtil {

    private NetworkUtil() {
    }

    public static InetAddress[] toArray(final List<InetAddress> addresses) {
        final InetAddress[] array = new InetAddress[addresses.size()];
        addresses.toArray(array);
        return array;
    }

    public static List<InetAddress> sortAddresses(
            final List<InetAddress> addresses) {
        Collections.sort(addresses, (a, b) -> Objects.compare(a.getHostAddress(),
                b.getHostAddress(), String.CASE_INSENSITIVE_ORDER));
        return addresses;
    }

    public static List<InetAddress> getNetworkInterfaces() {
        List<InetAddress> addresses = new ArrayList<>();

        try {
            for (final Enumeration<NetworkInterface> nis = NetworkInterface
                    .getNetworkInterfaces(); nis.hasMoreElements(); ) {
                final NetworkInterface ni = nis.nextElement();
                if (ni.isUp()) {
                    for (final Enumeration<InetAddress> e = ni.getInetAddresses();
                         e.hasMoreElements(); ) {
                        addresses.add(e.nextElement());
                    }
                }
            }
        } catch (final Throwable t) {
            //t.printStackTrace();
        }

        return addresses;
    }
}
