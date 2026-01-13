package cn.kinlon.emu.utils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PathUtils {

    private static Path appPath;
    private static Path tempPath;

    public static List<Path> getPathsFromApp(String extension, String... paths) {
        Path path = getPathFromApp(paths);
        final String ext = extension.charAt(0) == '.' ? extension.substring(1) : extension;
        try (Stream<Path> stream = Files.walk(path)) {
            return stream.filter(Files::isRegularFile).filter(p -> getFileExtension(p).equalsIgnoreCase(ext)).collect(Collectors.toList());
        } catch (IOException _) {
            return Collections.emptyList();
        }
    }

    public static String removeExtension(Path path) {
        if (path == null) {
            return "";
        }
        String fileName = path.getFileName().toString();
        int i = getFileExtensionIndex(fileName);
        return i <= 0 ? "" : fileName.substring(0, i - 1);
    }

    public static String getFileExtension(Path path) {
        if (path == null) {
            return "";
        }
        String fileName = path.getFileName().toString();
        int i = getFileExtensionIndex(fileName);
        return i < 0 ? "" : fileName.substring(i).toLowerCase();
    }

    public static int getFileExtensionIndex(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return -1;
        }
        int last = fileName.lastIndexOf('.');
        if (last < 0 || last == fileName.length() - 1) {
            return -1;
        }
        char c;
        for (int i = fileName.length() - 1; 0 <= i; i--) {
            c = fileName.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                return c == '.' ? i + 1 : -1;
            }
        }
        return -1;
    }

    public static Path getPathFromTemp(String... paths) {
        return getPathFromBase(getTempPath(), paths);
    }

    public static Path getPathFromApp(String... paths) {
        return getPathFromBase(getAppPath(), paths);
    }

    public static Path getPathFromBase(Path base, String... paths) {
        if (ArrayUtils.isEmpty(paths)) {
            return base;
        }
        Path r = base;
        for (String p : paths) {
            if (!StringUtils.isBlank(p)) {
                r = r.resolve(p);
            }
        }
        r = r.normalize();
        return r;
    }

    public static Path getTempPath() {
        if (tempPath != null) {
            return tempPath;
        }
        tempPath = Paths.get(System.getProperty("java.io.tmpdir"));
        return tempPath;
    }

    public static Path getAppPath() {
        if (appPath != null) {
            return appPath;
        }
        ClassLoader classLoader = PathUtils.class.getClassLoader();
        URL resourceUrl = classLoader.getResource("");
        if (resourceUrl == null) {
            throw new IllegalStateException("Unable to determine the root directory of the class path.");
        }
        String protocol = resourceUrl.getProtocol();
        Path path;
        if ("jar".equals(protocol)) {
            URL location = PathUtils.class.getProtectionDomain().getCodeSource().getLocation();
            try {
                Path jarPath = Paths.get(location.toURI());
                path = jarPath.getParent();
            } catch (Exception e) {
                throw new IllegalStateException("An error occurred while parsing the JAR file path: " + location.getPath(), e);
            }
        } else {
            String projectRoot = System.getProperty("user.dir");
            path = Paths.get(projectRoot, "src", "main", "resources");
        }
        appPath = path.toAbsolutePath().normalize();
        return appPath;
    }

    private PathUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }
}
