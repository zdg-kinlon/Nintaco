package nintaco.files.vs.goodnes;

import nintaco.files.ArchiveEntry;
import nintaco.files.FileUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static nintaco.files.FileUtil.InputStreamListener;
import static nintaco.util.StringUtil.isBlank;

public final class GoodNesFileUtil {

    private static final String HDR_PATH = "/vs/hdr/";

    private static final String ENTRIES = "/vs/goodnes/entries.txt";

    private static final String[] HEADER_FILES = {
            "balonfgt", // 0
            "vsbball",  // 1
            "vsbball",  // 2
            "vsbball",  // 3
            "iceclmrd", // 4
            "vsmahjng_pad", // 5
            "vstennis", // 6
            "wrecking", // 7
    };

    private static final ArchiveEntry[][] archiveEntries;
    private static final Set<String>[] patterns;

    static {
        ArchiveEntry[][] entries = null;
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(
                FileUtil.getResourceAsStream(ENTRIES)))) {
            final int archives = Integer.parseInt(br.readLine());
            entries = new ArchiveEntry[archives][];
            for (int i = 0; i < archives; ++i) {
                final int archiveLength = Integer.parseInt(br.readLine());
                final ArchiveEntry[] es = entries[i] = new ArchiveEntry[archiveLength];
                for (int j = 0; j < archiveLength; ++j) {
                    es[j] = new ArchiveEntry(br.readLine(),
                            Long.parseLong(br.readLine()));
                }
            }
        } catch (final Throwable t) {
            t.printStackTrace();
        } finally {
            archiveEntries = entries;
            patterns = new Set[archiveEntries.length];
            for (int i = patterns.length - 1; i >= 0; --i) {
                final Set<String> set = patterns[i] = new HashSet<>();
                final ArchiveEntry[] es = archiveEntries[i];
                for (int j = es.length - 1; j >= 0; --j) {
                    set.add(es[j].getName());
                }
            }
        }
    }

    private GoodNesFileUtil() {
    }

    public static int identifyCandidate(final String archiveFileName,
                                        final String entryFileName) {

        if (isBlank(archiveFileName) || isBlank(entryFileName)) {
            return -1;
        }

        final String _entryFileName = entryFileName.trim()
                .toLowerCase(Locale.ENGLISH);

        int candidateID = -1;
        for (int i = patterns.length - 1; i >= 0; --i) {
            final Set<String> pattern = patterns[i];
            if (pattern.contains(_entryFileName)) {
                candidateID = i;
                break;
            }
        }

        return candidateID;
    }

    public static int confirmCandidate(final String archiveFileName,
                                       final int candidateID, final List<ArchiveEntry> allEntries) {

        final Set<ArchiveEntry> set = new HashSet<>();
        for (int i = allEntries.size() - 1; i >= 0; --i) {
            final ArchiveEntry entry = allEntries.get(i);
            set.add(new ArchiveEntry(entry.getName().trim().toLowerCase(
                    Locale.ENGLISH), entry.getSize()));
        }
        final ArchiveEntry[] entries = archiveEntries[candidateID];
        if (!(set.contains(entries[0]) && set.contains(entries[1]))) {
            return -1;
        }

        return candidateID;
    }

    private static ArchiveEntry findArchiveEntry(
            final List<ArchiveEntry> allEntries, final String name) {

        for (int i = allEntries.size() - 1; i >= 0; --i) {
            final ArchiveEntry e = allEntries.get(i);
            if (e.getName().trim().equalsIgnoreCase(name)) {
                return e;
            }
        }

        return null;
    }

    private static byte[] readArchiveEntry(final String archiveFileName,
                                           final List<ArchiveEntry> allEntries, final String name) throws Throwable {
        return FileUtil.readArchiveEntry(archiveFileName, findArchiveEntry(allEntries, name));
    }

    private static void readHeader(final String fileName, final byte[] data)
            throws Throwable {
        try (final DataInputStream in = new DataInputStream(FileUtil.getResourceAsStream(HDR_PATH + fileName + ".hdr"))) {
            in.readFully(data, 0, 16);
        }
    }

    public static void getArchiveInputStream(final String archiveFileName,
                                             final List<ArchiveEntry> allEntries, final int archiveID,
                                             final InputStreamListener listener) throws Throwable {

        final ArchiveEntry[] entries = archiveEntries[archiveID];
        final byte[] entry0 = readArchiveEntry(archiveFileName, allEntries,
                entries[0].getName());
        final byte[] entry1 = readArchiveEntry(archiveFileName, allEntries,
                entries[1].getName());

        final byte[] data = new byte[98320];
        readHeader(HEADER_FILES[archiveID], data);
        System.arraycopy(entry0, 16, data, 16, 32768);
        System.arraycopy(entry1, 16, data, 16 + 32768, 32768);
        if (archiveID == 6) {
            // Tennis
            System.arraycopy(entry0, 16 + 32768, data, 16 + 65536, 16384);
            System.arraycopy(entry1, 16 + 32768, data, 16 + 65536 + 16384,
                    16384);
        } else {
            System.arraycopy(entry1, 16 + 32768, data, 16 + 65536,
                    (archiveID == 5) ? 8192 : 16384);
        }

        listener.handleInputStream(new ByteArrayInputStream(data), data.length);
    }
}
