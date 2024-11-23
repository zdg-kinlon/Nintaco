
package nintaco.util;

import java.awt.*;
import java.net.URI;

public final class BrowserUtil {

    private static final String[] BROWSERS = {"epiphany", "firefox", "mozilla",
            "konqueror", "netscape", "opera", "links", "lynx"};

    public static void openBrowser(final String url) {
        if (!openBrowserWithDesktop(url)) {
            openBrowserWithRuntime(url);
        }
    }

    private static boolean openBrowserWithRuntime(final String url) {
        final String os = System.getProperty("os.name").toLowerCase();
        final Runtime runtime = Runtime.getRuntime();
        try {
            if (os.contains("win")) {
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                runtime.exec("open" + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                final StringBuffer sb = new StringBuffer();
                for (int i = 0; i < BROWSERS.length; i++) {
                    if (i > 0) {
                        sb.append(" || ");
                    }
                    sb.append(BROWSERS[i]).append(" \"").append(url).append("\" ");
                }
                runtime.exec(new String[]{"sh", "-c", sb.toString()});
            } else {
                return false;
            }
        } catch (final Throwable t) {
            return false;
        }
        return true;
    }

    private static boolean openBrowserWithDesktop(final String url) {
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop()
                : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(url));
            } catch (final Throwable t) {
                return false;
            }
            return true;
        }
        return false;
    }

    private BrowserUtil() {
    }
}