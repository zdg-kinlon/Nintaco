package cn.kinlon.emu.preferences;

import cn.kinlon.emu.gui.hexeditor.preferences.HexEditorGamePrefs;
import cn.kinlon.emu.utils.ThreadUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.kinlon.emu.files.FileUtil.createGamePreferencesFile;
import static cn.kinlon.emu.files.FileUtil.mkdir;
import static cn.kinlon.emu.utils.StreamUtil.readSparseByteArray;
import static cn.kinlon.emu.utils.StreamUtil.writeSparseByteArray;
import static cn.kinlon.emu.utils.ThreadUtil.joinAll;
import static cn.kinlon.emu.utils.ThreadUtils.async_io;

public final class GamePrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private static final List<Thread> threads = Collections.synchronizedList(
            new ArrayList<>());

    private static GamePrefs instance;
    private static String fileName;

    private transient int[] nonVolatilePrgRam;
    private transient int[] storageUnitRam;
    private transient int[] nonVolatileXRam;

    private HexEditorGamePrefs hexEditorGamePrefs;

    public void eraseNonVolatileRam() {
        synchronized (GamePrefs.class) {
            nonVolatilePrgRam = storageUnitRam = nonVolatileXRam = null;
        }
    }

    public int[] getNonVolatileXRam() {
        synchronized (GamePrefs.class) {
            return nonVolatileXRam;
        }
    }

    public void setNonVolatileXRam(final int[] nonVolatileXRam) {
        synchronized (GamePrefs.class) {
            this.nonVolatileXRam = nonVolatileXRam;
        }
    }

    public int[] getNonVolatilePrgRam() {
        synchronized (GamePrefs.class) {
            return nonVolatilePrgRam;
        }
    }

    public void setNonVolatilePrgRam(final int[] nonVolatilePrgRam) {
        synchronized (GamePrefs.class) {
            this.nonVolatilePrgRam = nonVolatilePrgRam;
        }
    }

    public int[] getStorageUnitRam() {
        synchronized (GamePrefs.class) {
            return storageUnitRam;
        }
    }

    public void setStorageUnitRam(final int[] storageUnitRam) {
        synchronized (GamePrefs.class) {
            this.storageUnitRam = storageUnitRam;
        }
    }

    public HexEditorGamePrefs getHexEditorGamePrefs() {
        synchronized (GamePrefs.class) {
            if (hexEditorGamePrefs == null) {
                hexEditorGamePrefs = new HexEditorGamePrefs();
            }
            return hexEditorGamePrefs;
        }
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        nonVolatilePrgRam = readSparseByteArray(in);
        storageUnitRam = readSparseByteArray(in);
        nonVolatileXRam = readSparseByteArray(in);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        writeSparseByteArray(out, nonVolatilePrgRam);
        writeSparseByteArray(out, storageUnitRam);
        writeSparseByteArray(out, nonVolatileXRam);
    }

    public static void dispose() {
        joinAll(threads);
        synchronized (GamePrefs.class) {
            fileName = null;
            instance = null;
        }
    }

    public static void load(final String entryFileName) {
        dispose();
        synchronized (GamePrefs.class) {
            fileName = createGamePreferencesFile(entryFileName);
            final File file = new File(fileName);
            if (file.exists()) {
                try (final ObjectInputStream in = new ObjectInputStream(
                        new BufferedInputStream(new FileInputStream(fileName)))) {
                    instance = (GamePrefs) in.readObject();
                } catch (final Throwable t) {
                    //t.printStackTrace();
                }
            }
        }
    }

    public static void save() {
        final String name;
        final GamePrefs prefs;
        synchronized (GamePrefs.class) {
            if (fileName == null || instance == null) {
                return;
            }
            name = fileName;
            prefs = instance;
        }

        async_io(() -> {
            synchronized (GamePrefs.class) {
                mkdir(AppPrefs.getInstance().getPaths().getGamePreferencesDir());
                try (final ObjectOutputStream out = new ObjectOutputStream(
                        new BufferedOutputStream(new FileOutputStream(name)))) {
                    out.writeObject(prefs);
                } catch (Throwable t) {
                    //t.printStackTrace();
                }
            }
        });
    }

    public static synchronized GamePrefs getInstance() {
        if (instance == null) {
            instance = new GamePrefs();
        }
        return instance;
    }

    private GamePrefs() {
    }
}