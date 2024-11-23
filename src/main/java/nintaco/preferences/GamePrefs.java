package nintaco.preferences;

import java.io.*;
import java.util.*;

import nintaco.gui.api.local.*;
import nintaco.gui.debugger.*;
import nintaco.gui.debugger.logger.*;
import nintaco.gui.dipswitches.*;
import nintaco.gui.hexeditor.preferences.*;
import nintaco.gui.mapmaker.*;
import nintaco.gui.ramwatch.*;
import nintaco.gui.spritesaver.*;

import static nintaco.files.FileUtil.*;
import static nintaco.util.StreamUtil.*;
import static nintaco.util.ThreadUtil.*;

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
    private MapMakerGamePrefs mapMakerGamePrefs;
    private SpriteSaverGamePrefs spriteSaverGamePrefs;
    private RamWatchGamePrefs ramWatchGamePrefs;
    private DebuggerGamePrefs debuggerGamePrefs;
    private LoggerGamePrefs loggerGamePrefs;
    private DipSwitchesGamePrefs dipSwitchesGamePrefs;
    private ProgramGamePrefs programGamePrefs;

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

    public LoggerGamePrefs getLoggerGamePrefs() {
        synchronized (GamePrefs.class) {
            if (loggerGamePrefs == null) {
                loggerGamePrefs = new LoggerGamePrefs();
            }
            return loggerGamePrefs;
        }
    }

    public DebuggerGamePrefs getDebuggerGamePrefs() {
        synchronized (GamePrefs.class) {
            if (debuggerGamePrefs == null) {
                debuggerGamePrefs = new DebuggerGamePrefs();
            }
            return debuggerGamePrefs;
        }
    }

    public RamWatchGamePrefs getRamWatchGamePrefs() {
        synchronized (GamePrefs.class) {
            if (ramWatchGamePrefs == null) {
                ramWatchGamePrefs = new RamWatchGamePrefs();
            }
            return ramWatchGamePrefs;
        }
    }

    public SpriteSaverGamePrefs getSpriteSaverGamePrefs() {
        synchronized (GamePrefs.class) {
            if (spriteSaverGamePrefs == null) {
                spriteSaverGamePrefs = new SpriteSaverGamePrefs();
            }
            return spriteSaverGamePrefs;
        }
    }

    public MapMakerGamePrefs getMapMakerGamePrefs() {
        synchronized (GamePrefs.class) {
            if (mapMakerGamePrefs == null) {
                mapMakerGamePrefs = new MapMakerGamePrefs();
            }
            return mapMakerGamePrefs;
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

    public DipSwitchesGamePrefs getDipSwitchesGamePrefs() {
        synchronized (GamePrefs.class) {
            if (dipSwitchesGamePrefs == null) {
                dipSwitchesGamePrefs = new DipSwitchesGamePrefs();
            }
            return dipSwitchesGamePrefs;
        }
    }

    public ProgramGamePrefs getProgramGamePrefs() {
        synchronized (GamePrefs.class) {
            if (programGamePrefs == null) {
                programGamePrefs = new ProgramGamePrefs();
            }
            return programGamePrefs;
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

        final Thread thread = new Thread(() -> {
            synchronized (GamePrefs.class) {
                mkdir(AppPrefs.getInstance().getPaths().getGamePreferencesDir());
                try (final ObjectOutputStream out = new ObjectOutputStream(
                        new BufferedOutputStream(new FileOutputStream(name)))) {
                    out.writeObject(prefs);
                } catch (Throwable t) {
                    //t.printStackTrace();
                }
            }
            threads.remove(Thread.currentThread());
        }, "GamePrefs Save Thread");

        synchronized (threads) {
            thread.start();
            threads.add(thread);
        }
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