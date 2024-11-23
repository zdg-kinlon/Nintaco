package nintaco.preferences;

import java.io.*;
import java.util.*;

import nintaco.gui.api.server.*;
import nintaco.gui.archive.*;
import nintaco.gui.cheats.*;
import nintaco.gui.debugger.logger.*;
import nintaco.gui.dipswitches.*;
import nintaco.gui.debugger.*;
import nintaco.gui.exportmedia.preferences.*;
import nintaco.gui.familybasic.*;
import nintaco.gui.fds.*;
import nintaco.gui.hexeditor.preferences.*;
import nintaco.gui.historyeditor.preferences.*;
import nintaco.gui.ips.*;
import nintaco.gui.image.preferences.*;
import nintaco.gui.mapmaker.*;
import nintaco.gui.nametables.*;
import nintaco.gui.netplay.client.*;
import nintaco.gui.netplay.server.*;
import nintaco.gui.nsf.*;
import nintaco.gui.oam.*;
import nintaco.gui.overscan.*;
import nintaco.gui.patterntables.*;
import nintaco.gui.ramsearch.*;
import nintaco.gui.sound.*;
import nintaco.gui.sound.volumemixer.*;
import nintaco.gui.spritesaver.*;
import nintaco.gui.userinterface.*;
import nintaco.input.*;
import nintaco.palettes.*;

import static nintaco.files.FileUtil.*;
import static nintaco.util.ThreadUtil.*;

public final class AppPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private static final File PREFERENCES_FILE = new File(
            getWorkingDirectory("nintaco.preferences"));

    private static AppPrefs instance;
    private static final List<Thread> threads = Collections.synchronizedList(
            new ArrayList<>());

    private Paths paths;
    private View view;
    private Inputs inputs;
    private Palettes palettes;
    private HexEditorAppPrefs hexEditorPrefs;
    private CheatPrefs cheatPrefs;
    private PatternTablesPrefs patternTablesPrefs;
    private NametablesPrefs nametablesPrefs;
    private MapMakerAppPrefs mapMakerAppPrefs;
    private SpriteSaverAppPrefs spriteSaverAppPrefs;
    private RamSearchPrefs ramSearchPrefs;
    private LoggerAppPrefs loggerAppPrefs;
    private HistoryEditorPrefs historyEditorPrefs;
    private ProgramServerPrefs programSreverPrefs;
    private NetplayServerPrefs netplayServerPrefs;
    private NetplayClientPrefs netplayClientPrefs;
    private FamicomDiskSystemPrefs famicomDiskSystemPrefs;
    private DipSwitchesAppPrefs dipSwitchesAppPrefs;
    private SoundPrefs soundPrefs;
    private VolumeMixerPrefs volumeMixerPrefs;
    private ExportMediaFilePrefs exportMediaFilePrefs;
    private ExportMediaFilePrefs screenshotPrefs;
    private HistoryPrefs historyPrefs;
    private OverscanPrefs overscanPrefs;
    private ArchivePrefs archivePrefs;
    private UserInterfacePrefs userInterfacePrefs;
    private IpsPatchPrefs applyIpsPatchPrefs;
    private IpsPatchPrefs createIpsPatchPrefs;
    private OamDataPrefs oamDataPrefs;
    private NsfPrefs nsfPrefs;
    private FamilyBasicPrefs familyBasicPrefs;
    private DebuggerAppPrefs debuggerAppPrefs;

    public synchronized DebuggerAppPrefs getDebuggerAppPrefs() {
        if (debuggerAppPrefs == null) {
            debuggerAppPrefs = new DebuggerAppPrefs();
        }
        return debuggerAppPrefs;
    }

    public synchronized FamilyBasicPrefs getFamilyBasicPrefs() {
        if (familyBasicPrefs == null) {
            familyBasicPrefs = new FamilyBasicPrefs();
        }
        return familyBasicPrefs;
    }

    public synchronized NsfPrefs getNsfPrefs() {
        if (nsfPrefs == null) {
            nsfPrefs = new NsfPrefs();
        }
        return nsfPrefs;
    }

    public synchronized OamDataPrefs getOamDataPrefs() {
        if (oamDataPrefs == null) {
            oamDataPrefs = new OamDataPrefs();
        }
        return oamDataPrefs;
    }

    public synchronized IpsPatchPrefs getApplyIpsPatchPrefs() {
        if (applyIpsPatchPrefs == null) {
            applyIpsPatchPrefs = new IpsPatchPrefs();
        }
        return applyIpsPatchPrefs;
    }

    public synchronized IpsPatchPrefs getCreateIpsPatchPrefs() {
        if (createIpsPatchPrefs == null) {
            createIpsPatchPrefs = new IpsPatchPrefs();
        }
        return createIpsPatchPrefs;
    }

    public synchronized UserInterfacePrefs getUserInterfacePrefs() {
        if (userInterfacePrefs == null) {
            userInterfacePrefs = new UserInterfacePrefs();
        }
        return userInterfacePrefs;
    }

    public synchronized ArchivePrefs getArchivePrefs() {
        if (archivePrefs == null) {
            archivePrefs = new ArchivePrefs();
        }
        return archivePrefs;
    }

    public synchronized OverscanPrefs getOverscanPrefs() {
        if (overscanPrefs == null) {
            overscanPrefs = new OverscanPrefs();
        }
        return overscanPrefs;
    }

    public synchronized Palettes getPalettes() {
        if (palettes == null) {
            palettes = new Palettes();
        }
        return palettes;
    }

    public synchronized HistoryPrefs getHistoryPrefs() {
        if (historyPrefs == null) {
            historyPrefs = new HistoryPrefs();
        }
        return historyPrefs;
    }

    public synchronized ExportMediaFilePrefs getScreenshotPrefs() {
        if (screenshotPrefs == null) {
            screenshotPrefs = new ExportMediaFilePrefs();
            screenshotPrefs.setFileType(3);
        }
        return screenshotPrefs;
    }

    public synchronized ExportMediaFilePrefs getExportMediaFilePrefs() {
        if (exportMediaFilePrefs == null) {
            exportMediaFilePrefs = new ExportMediaFilePrefs();
        }
        return exportMediaFilePrefs;
    }

    public synchronized VolumeMixerPrefs getVolumeMixerPrefs() {
        if (volumeMixerPrefs == null) {
            volumeMixerPrefs = new VolumeMixerPrefs();
        }
        return volumeMixerPrefs;
    }

    public synchronized SoundPrefs getSoundPrefs() {
        if (soundPrefs == null) {
            soundPrefs = new SoundPrefs();
        }
        return soundPrefs;
    }

    public synchronized DipSwitchesAppPrefs getDipSwitchesAppPrefs() {
        if (dipSwitchesAppPrefs == null) {
            dipSwitchesAppPrefs = new DipSwitchesAppPrefs();
        }
        return dipSwitchesAppPrefs;
    }

    public synchronized FamicomDiskSystemPrefs getFamicomDiskSystemPrefs() {
        if (famicomDiskSystemPrefs == null) {
            famicomDiskSystemPrefs = new FamicomDiskSystemPrefs();
        }
        return famicomDiskSystemPrefs;
    }

    public synchronized ProgramServerPrefs getProgramServerPrefs() {
        if (programSreverPrefs == null) {
            programSreverPrefs = new ProgramServerPrefs();
        }
        return programSreverPrefs;
    }

    public synchronized NetplayServerPrefs getNetplayServerPrefs() {
        if (netplayServerPrefs == null) {
            netplayServerPrefs = new NetplayServerPrefs();
        }
        return netplayServerPrefs;
    }

    public synchronized NetplayClientPrefs getNetplayClientPrefs() {
        if (netplayClientPrefs == null) {
            netplayClientPrefs = new NetplayClientPrefs();
        }
        return netplayClientPrefs;
    }

    public synchronized HistoryEditorPrefs getHistoryEditorPrefs() {
        if (historyEditorPrefs == null) {
            historyEditorPrefs = new HistoryEditorPrefs();
        }
        return historyEditorPrefs;
    }

    public synchronized LoggerAppPrefs getLoggerAppPrefs() {
        if (loggerAppPrefs == null) {
            loggerAppPrefs = new LoggerAppPrefs();
        }
        return loggerAppPrefs;
    }

    public synchronized RamSearchPrefs getRamSearchAppPrefs() {
        if (ramSearchPrefs == null) {
            ramSearchPrefs = new RamSearchPrefs();
        }
        return ramSearchPrefs;
    }

    public synchronized SpriteSaverAppPrefs getSpriteSaverAppPrefs() {
        if (spriteSaverAppPrefs == null) {
            spriteSaverAppPrefs = new SpriteSaverAppPrefs();
        }
        return spriteSaverAppPrefs;
    }

    public synchronized MapMakerAppPrefs getMapMakerAppPrefs() {
        if (mapMakerAppPrefs == null) {
            mapMakerAppPrefs = new MapMakerAppPrefs();
        }
        return mapMakerAppPrefs;
    }

    public synchronized NametablesPrefs getNametablesPrefs() {
        if (nametablesPrefs == null) {
            nametablesPrefs = new NametablesPrefs();
        }
        return nametablesPrefs;
    }

    public synchronized PatternTablesPrefs getPatternTablesPrefs() {
        if (patternTablesPrefs == null) {
            patternTablesPrefs = new PatternTablesPrefs();
        }
        return patternTablesPrefs;
    }

    public synchronized CheatPrefs getCheatPrefs() {
        if (cheatPrefs == null) {
            cheatPrefs = new CheatPrefs();
        }
        return cheatPrefs;
    }

    public synchronized HexEditorAppPrefs getHexEditorPrefs() {
        if (hexEditorPrefs == null) {
            hexEditorPrefs = new HexEditorAppPrefs();
        }
        return hexEditorPrefs;
    }

    public synchronized Paths getPaths() {
        if (paths == null) {
            paths = new Paths();
        }
        return paths;
    }

    public synchronized View getView() {
        if (view == null) {
            view = new View();
        }
        return view;
    }

    public synchronized Inputs getInputs() {
        if (inputs == null) {
            inputs = new Inputs();
        }
        return inputs;
    }

    public static synchronized AppPrefs getInstance() {
        return instance;
    }

    public static void flush() {
        joinAll(threads);
    }

    private static void dispose() {
        joinAll(threads);
        synchronized (AppPrefs.class) {
            instance = null;
        }
    }

    public static void load() {
        dispose();
        synchronized (AppPrefs.class) {
            try (final ObjectInputStream in = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(PREFERENCES_FILE)))) {
                instance = (AppPrefs) in.readObject();
            } catch (final Throwable t) {
            }
            if (instance == null) {
                instance = new AppPrefs();
            }
            instance.getPaths().init();
        }
    }

    public static void save() {
        final AppPrefs prefs;
        synchronized (AppPrefs.class) {
            if (instance == null) {
                return;
            }
            prefs = instance;
        }

        final Thread thread = new Thread(() -> {
            synchronized (AppPrefs.class) {
                try (final ObjectOutputStream out = new ObjectOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(PREFERENCES_FILE)))) {
                    out.writeObject(prefs);
                } catch (Throwable t) {
                    //t.printStackTrace();
                }
            }
            threads.remove(Thread.currentThread());
        }, "AppPrefs Save Thread");

        synchronized (threads) {
            thread.start();
            threads.add(thread);
        }
    }

    private AppPrefs() {
    }
}