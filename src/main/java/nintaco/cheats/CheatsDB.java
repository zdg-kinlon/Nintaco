package nintaco.cheats;

import nintaco.files.FileUtil;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CheatsDB {

    private static final Map<Integer, CheatsDBEntry[]> cheats = new HashMap<>();

    private CheatsDB() {
    }

    public static void init() {
        try (final DataInputStream in = new DataInputStream(new BufferedInputStream(
                FileUtil.getResourceAsStream("/db/cheats-db.dat")))) {
            final List<Integer> crcs = new ArrayList<>();
            while (true) {
                try {
                    crcs.clear();
                    for (int i = in.readShort() - 1; i >= 0; i--) {
                        crcs.add(in.readInt());
                    }
                    final CheatsDBEntry[] entries
                            = new CheatsDBEntry[in.readUnsignedByte()];
                    for (int i = 0; i < entries.length; i++) {
                        final String description = in.readUTF();
                        final String[][] codes = new String[in.readUnsignedByte()][];
                        for (int j = 0; j < codes.length; j++) {
                            codes[j] = new String[in.readUnsignedByte()];
                            for (int k = 0; k < codes[j].length; k++) {
                                int address = in.readUnsignedShort();
                                final boolean hasCompareValue = address >= 0x8000;
                                address |= 0x8000;
                                final int dataValue = in.readUnsignedByte();
                                final int compareValue = in.readUnsignedByte();
                                codes[j][k] = GameGenie.convert(address, dataValue,
                                        compareValue, hasCompareValue);
                            }
                        }
                        entries[i] = new CheatsDBEntry(description, codes);
                    }
                    for (int i = 0; i < crcs.size(); i++) {
                        cheats.put(crcs.get(i), entries);
                    }
                } catch (final EOFException eof) {
                    break;
                }
            }
        } catch (final Throwable t) {
            //t.printStackTrace();
        }
    }

    public static synchronized CheatsDBEntry[] getCheats(final int fileCRC) {
        return cheats.get(fileCRC);
    }
}
