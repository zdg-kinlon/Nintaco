package nintaco.files.vs.mame;

import nintaco.files.ArchiveEntry;
import nintaco.files.FileUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Arrays.fill;
import static nintaco.files.FileUtil.InputStreamListener;
import static nintaco.files.FileUtil.getFileName;
import static nintaco.util.CollectionsUtil.isBlank;
import static nintaco.util.StreamUtil.copy;

public final class MameFileUtil {

    private static final String HDR_PATH = "/vs/hdr/";

    private static final String ENTRIES = "/vs/mame/entries.txt";

    private static final String PAD_8K = "pad.8k";
    private static final byte[] PAD_8K_DATA = new byte[0x2000];

    private static final int GUMSHOE_ID = 40;

    private static final ArchiveEntry[][] archiveEntries;
    private static final Set<ArchiveEntry>[] patterns;

    static {
        fill(PAD_8K_DATA, (byte) 0xFF);

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
                final Set<ArchiveEntry> set = patterns[i] = new HashSet<>();
                final ArchiveEntry[] es = archiveEntries[i];
                for (int j = es.length - 1; j > 0; --j) { // do not include hdr file
                    final ArchiveEntry entry = es[j];
                    if (!PAD_8K.equals(entry.getName())) { // do not include pad files
                        set.add(entry);
                    }
                }
            }
        }
    }

    private MameFileUtil() {
    }

    public static int identifyArchive(final List<ArchiveEntry> archiveEntries) {
        if (isBlank(archiveEntries)) {
            return -1;
        }
        final Set<ArchiveEntry> entries = new HashSet<>();
        for (int i = archiveEntries.size() - 1; i >= 0; --i) {
            final ArchiveEntry entry = archiveEntries.get(i);
            entries.add(new ArchiveEntry(getFileName(entry.getName()).trim()
                    .toLowerCase(Locale.ENGLISH), entry.getSize()));
        }
        for (int i = patterns.length - 1; i >= 0; --i) {
            if (entries.containsAll(patterns[i])) {
                return i;
            }
        }
        return -1;
    }

    public static void getArchiveInputStream(final String archiveFileName,
                                             final List<ArchiveEntry> allEntries, final int archiveID,
                                             final InputStreamListener listener) throws Throwable {

        final ArchiveEntry[] entries = archiveEntries[archiveID];
        final String[] files = new String[entries.length];
        for (int i = allEntries.size() - 1; i >= 0; --i) {
            final ArchiveEntry _entry = allEntries.get(i);
            final ArchiveEntry entry = new ArchiveEntry(getFileName(_entry.getName())
                    .trim().toLowerCase(Locale.ENGLISH), _entry.getSize());
            for (int j = entries.length - 1; j > 0; --j) { // do not include hdr file
                if (entries[j].equals(entry)) {
                    files[j] = _entry.getName();
                    break;
                }
            }
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buffer = new byte[64 * 1024];

        // Copy hdr file
        copy(FileUtil.getResourceAsStream(HDR_PATH + entries[0].getName()), out, buffer);

        // Copy archive files
        for (int i = 1; i < entries.length; ++i) {
            if (PAD_8K.equals(entries[i].getName())) {
                out.write(PAD_8K_DATA);
            } else {
                FileUtil.getArchiveInputStream(archiveFileName, files[i],
                        (in, fileSize) -> copy(in, out, buffer), null);
            }
        }

        out.close();
        final byte[] data = out.toByteArray();

        if (archiveID == GUMSHOE_ID) {
            System.arraycopy(data, 0x2010, buffer, 0, 0x2000);
            System.arraycopy(data, 0x4010, data, 0x2010, 0x6000);
            System.arraycopy(buffer, 0, data, 0x8010, 0x2000);
        }

        listener.handleInputStream(new ByteArrayInputStream(data), data.length);
    }
}