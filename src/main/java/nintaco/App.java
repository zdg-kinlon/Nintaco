package nintaco;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import nintaco.api.local.*;
import nintaco.api.server.*;
import nintaco.apu.*;
import nintaco.cartdb.*;
import nintaco.cheats.*;
import nintaco.disassembler.*;
import nintaco.files.*;
import nintaco.gui.*;
import nintaco.gui.api.local.*;
import nintaco.gui.api.server.*;
import nintaco.gui.asmdasm.*;
import nintaco.gui.barcodebattler.*;
import nintaco.gui.dipswitches.*;
import nintaco.gui.mapmaker.*;
import nintaco.gui.nametables.*;
import nintaco.gui.patterntables.*;
import nintaco.gui.historyeditor.preferences.*;
import nintaco.gui.image.*;
import nintaco.gui.image.preferences.*;
import nintaco.gui.cheats.search.*;
import nintaco.gui.debugger.*;
import nintaco.gui.familybasic.*;
import nintaco.gui.fds.*;
import nintaco.gui.glasses.*;
import nintaco.gui.hexeditor.*;
import nintaco.gui.historyeditor.*;
import nintaco.gui.historyeditor.change.*;
import nintaco.gui.ips.*;
import nintaco.gui.netplay.client.*;
import nintaco.gui.netplay.server.*;
import nintaco.gui.oam.*;
import nintaco.gui.ramsearch.*;
import nintaco.gui.ramwatch.*;
import nintaco.gui.rob.*;
import nintaco.gui.sound.volumemixer.*;
import nintaco.gui.spritesaver.*;
import nintaco.gui.userinterface.*;
import nintaco.gui.watchhistory.*;
import nintaco.input.*;
import nintaco.input.dipswitches.*;
import nintaco.input.other.*;
import nintaco.logger.*;
import nintaco.mappers.*;
import nintaco.mappers.nintendo.fds.*;
import nintaco.mappers.nintendo.vs.*;
import nintaco.mappers.nsf.*;
import nintaco.movie.*;
import nintaco.netplay.client.*;
import nintaco.netplay.server.*;
import nintaco.palettes.*;
import nintaco.preferences.*;
import nintaco.util.*;

import static java.awt.event.KeyEvent.*;
import static nintaco.files.FileType.*;
import static nintaco.netplay.protocol.MessageType.*;
import static nintaco.util.BitUtil.*;
import static nintaco.util.GuiUtil.*;
import static nintaco.files.FileUtil.*;
import static nintaco.tv.TVSystem.*;
import static nintaco.util.StreamUtil.*;
import static nintaco.util.StringUtil.*;

public final class App {

    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    private static volatile AppMode appMode = AppMode.Default;

    private static SystemAudioProcessor systemAudioProcessor;

    private static MachineRunner machineRunner;
    private static Machine machine;

    private static PatchFrame applyIpsPatchFrame;
    private static AsmDasmFrame asmDasmFrame;
    private static BackgroundEditorFrame backgroundEditorFrame;
    private static BarcodeBattlerFrame barcodeBattlerFrame;
    private static CheatSearchFrame cheatSearchFrame;
    private static PatchFrame createIpsPatchFrame;
    private static DebuggerFrame debuggerFrame;
    private static GlassesFrame glassesFrame;
    private static HexEditorFrame hexEditorFrame;
    private static HistoryEditorFrame historyEditorFrame;
    private static ImageFrame imageFrame;
    private static MapMakerFrame mapMakerFrame;
    private static NametablesFrame nametablesFrame;
    private static NetplayClientFrame netplayClientFrame;
    private static NetplayServerFrame netplayServerFrame;
    private static OamDataFrame oamDataFrame;
    private static PatternTablesFrame patternTablesFrame;
    private static ProgramFrame programFrame;
    private static ProgramServerFrame programServerFrame;
    private static RamSearchFrame ramSearchFrame;
    private static RamWatchFrame ramWatchFrame;
    private static RobFrame robFrame;
    private static SpriteSaverFrame spriteSaverFrame;
    private static VolumeMixerFrame volumeMixerFrame;
    private static SubMonitorFrame subMonitorFrame;
    private static WatchHistoryFrame watchHistoryFrame;

    private static NesFile nesFile;
    private static FdsFile fdsFile;
    private static UnifFile unifFile;
    private static NsfFile nsfFile;
    private static String entryFileName;

    private static String license;
    private static int[] fdsBIOS;

    private static volatile int rewindTimeValue;
    private static volatile int highSpeedValue;

    private static final ProgramServer programServer = new ProgramServer();
    private static final NetplayServer netplayServer = new NetplayServer();
    private static final NetplayClient netplayClient = new NetplayClient();

    private static volatile TraceLogger traceLogger;

    private static volatile LocalAPI localAPI;

    private static boolean fds;
    private static boolean diskActivity;
    private static DiskActivityIndicator diskActivityIndicator
            = DiskActivityIndicator.NUM_LOCK;
    private static volatile boolean diskActivityIndicatorChanged = true;

    private static String historyProjectName;
    private static boolean lastPlayFrameHasFocus = true;
    private static boolean lastApplicationHasFocus = true;
    private static boolean pausedOnFocusLoss;

    public static void init(final String... args) throws Throwable {
        ThreadUtil.forceHighResolutionTime();
        LogUtil.init(args);
        AppPrefs.load();
        if (!AppPrefs.getInstance().getUserInterfacePrefs()
                .isAllowMultipleInstances() && InstanceUtil.isAlreadyRunning()) {
            return;
        }
        systemAudioProcessor = new SystemAudioProcessor();
        loadLicense();
        CartDB.init();
        CheatsDB.init();
        PaletteUtil.init();
        EDT.sync(() -> {
            initApplicationFocusListener();
            imageFrame = new ImageFrame();
            imageFrame.init();
            imageFrame.getImagePane().setTVSystem(NTSC);
            requestVsync(imageFrame, true);
        });
        InputUtil.init();
        PPU.init();
        APU.init();
        AppPrefs.getInstance().getInputs().apply();

        for (int i = 0; i < args.length; ++i) {
            final String arg = args[i];
            if (!arg.startsWith("-")) {
                final StringBuilder sb = new StringBuilder(arg);
                for (++i; i < args.length; ++i) {
                    sb.append(' ').append(args[i]);
                }
                imageFrame.openFile(sb.toString());
                break;
            }
        }
    }

    public static void setAppMode(final AppMode appMode) {
        App.appMode = appMode;
        InputUtil.handleSettingsChange();
        imageFrame.appModeChanged(appMode);
    }

    public static AppMode getAppMode() {
        return appMode;
    }

    public static boolean isRobGame() {
        return isGyromite() || isStackUp();
    }

    public static boolean isGyromite() {
        final CartFile cartFile = getCartFile();
        return cartFile != null && (cartFile.getFileCRC() == 0x023A5A32
                || cartFile.getFileCRC() == 0x84EF2FF9);
    }

    public static boolean isStackUp() {
        final CartFile cartFile = getCartFile();
        return cartFile != null && (cartFile.getFileCRC() == 0xDF67DAA1
                || cartFile.getFileCRC() == 0x97B0F110);
    }

    private static void initApplicationFocusListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addPropertyChangeListener("activeWindow", _ -> EDT.async(
                        App::applicationFocusChanged));
    }

    private static void applicationFocusChanged() {

        final AppPrefs appPrefs = AppPrefs.getInstance();
        final UserInterfacePrefs uiPrefs = appPrefs.getUserInterfacePrefs();
        final Window activeWindow = KeyboardFocusManager
                .getCurrentKeyboardFocusManager().getActiveWindow();

        final boolean applicationHasFocus = activeWindow != null;
        if (lastApplicationHasFocus != applicationHasFocus) {
            lastApplicationHasFocus = applicationHasFocus;
            final boolean pause = !applicationHasFocus;
            if ((applicationHasFocus && pausedOnFocusLoss)
                    || (!uiPrefs.isRunInBackground() && (imageFrame
                    .isDisplayingImagePane() || !appPrefs.getNsfPrefs()
                    .isPlayInBackground()) && !(programServer.isRunning()
                    && appPrefs.getProgramServerPrefs()
                    .isRunInBackground()))) {
                pausedOnFocusLoss = pause;
                setNoStepPause(pause);
                if (applicationHasFocus) {
                    InputUtil.clearEventQueues();
                }
            }
        }

        final boolean playFrameHasFocus = activeWindow == imageFrame
                || (glassesFrame != null && activeWindow == glassesFrame)
                || (robFrame != null && activeWindow == robFrame)
                || (subMonitorFrame != null && activeWindow == subMonitorFrame);
        if (lastPlayFrameHasFocus != playFrameHasFocus) {
            lastPlayFrameHasFocus = playFrameHasFocus;
            if (playFrameHasFocus) {
                InputUtil.setInputDisabled(false);
            } else if (!uiPrefs.isAcceptBackgroundInput()) {
                InputUtil.setInputDisabled(true);
            }
        }
    }

    private static void loadLicense() {
        final StringBuilder sb = new StringBuilder();
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(
                FileUtil.getResourceAsStream("/license/lgpl-2.1.txt")))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (final Throwable t) {
            //t.printStackTrace();
        }
        license = sb.toString();
    }

    private static void addFrame(final List<JFrame> frames, final JFrame frame) {
        if (frame != null) {
            frames.add(frame);
        }
    }

    private static Machine getRunningMachine() {
        final Machine m = machine;
        final MachineRunner r = machineRunner;
        return (m != null && r != null && r.isRunning()) ? m : null;
    }

    public static void cpuKilled(final int opcode, final int address) {
        EDT.async(() -> {
            final FourButtonDialog dialog = new FourButtonDialog(imageFrame,
                    String.format("<html>The processor executed a KIL (<tt>$%02X</tt>) "
                            + "instruction at <tt>$%04X</tt>.</html>", opcode, address),
                    "CPU Killed", FourButtonDialog.IconType.ERROR);
            dialog.setButtonText(0, "Power Cycle", 'P');
            dialog.setButtonText(1, "Reset", 'R');
            dialog.setButtonText(2, "Close", 'C');
            dialog.setButtonText(3, "Ignore", 'I');
            dialog.setVisible(true);
            switch (dialog.getSelection()) {
                case 0:
                    powerCycle();
                    break;
                case 1:
                    reset();
                    break;
                case 2:
                    close();
                    break;
            }
        });
    }

    public static void importHistory() {
        final WatchHistoryFrame watchHistory = watchHistoryFrame;
        if (watchHistory != null) {
            watchHistory.getWatchHistoryPanel().pause();
        }
        final HistoryEditorFrame frame = historyEditorFrame;
        if (frame != null) {
            frame.load();
        } else {
            App.setNoStepPause(true);
            final Paths paths = AppPrefs.getInstance().getPaths();
            final String historiesDir = paths.getHistoriesDir();
            mkdir(historiesDir);

            final JFileChooser chooser = createFileChooser("Load History File",
                    (File) null, HistoryEditorFrame.historyFileExtensionFilter);
            if (isBlank(historyProjectName)) {
                chooser.setCurrentDirectory(new File(paths.getHistoriesDir()));
            } else {
                chooser.setSelectedFile(FileUtil.getFile(paths.getHistoriesDir(),
                        historyProjectName));
            }
            if (showOpenDialog(imageFrame, chooser, (p, d) -> p.setHistoriesDir(d))
                    == JFileChooser.APPROVE_OPTION) {
                final File selectedFile = chooser.getSelectedFile();
                final PleaseWaitDialog pleaseWaitDialog
                        = new PleaseWaitDialog(imageFrame);
                pleaseWaitDialog.setMessage("Loading history file...");
                new Thread(() -> loadHistoryProjectFile(pleaseWaitDialog, selectedFile,
                        entryFileName)).start();
                pleaseWaitDialog.showAfterDelay();
            } else {
                App.setNoStepPause(false);
            }
        }
    }

    private static void loadHistoryProjectFile(
            final PleaseWaitDialog pleaseWaitDialog, final File file,
            final String entryFileName) {

        boolean failed = false;
        HistoryProject project = null;
        try (final ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            project = (HistoryProject) in.readObject();
        } catch (final Throwable t) {
            //t.printStackTrace();
            failed = true;
        } finally {
            final boolean showError = failed;
            final HistoryProject p = project;
            pleaseWaitDialog.dispose();
            EDT.async(() -> {
                historyProjectName = file.getName();
                if (showError) {
                    displayError(imageFrame, "Failed to load history file.");
                    App.setNoStepPause(false);
                } else {
                    restoreHistoryProject(p, entryFileName, file);
                }
            });
        }
    }

    private static void restoreHistoryProject(final HistoryProject project,
                                              final String entryFileName, final File file) {

        final CartFile cartFile = App.getCartFile();
        if ((cartFile != null && cartFile.getFileCRC() != project.getEntryFileCRC())
                || (cartFile == null && project.getEntryFileCRC() != 0)
                || !entryFileName.equalsIgnoreCase(project.getEntryFileName())) {
            final YesNoDialog dialog = new YesNoDialog(imageFrame, String.format(
                    "History game file: <pre>%s</pre><br/>"
                            + "Current game file: <pre>%s</pre><br/>"
                            + "Load history anyway?", project.getEntryFileName(),
                    entryFileName), "Game File Mismatch");
            dialog.setVisible(true);
            if (!dialog.isYes()) {
                App.setNoStepPause(false);
                return;
            }
        }

        AppPrefs.getInstance().getPaths().addRecentHistoryProject(file.getPath());
        AppPrefs.save();

        imageFrame.movieLoaded(project.getMovie());

        App.setNoStepPause(false);
    }

    public static void exportHistory(final Movie movie) {
        if (movie == null) {
            return;
        }
        final HistoryEditorFrame frame = historyEditorFrame;
        if (frame != null) {
            frame.saveAs();
        } else {
            App.setNoStepPause(true);
            final Paths paths = AppPrefs.getInstance().getPaths();
            final String historiesDir = paths.getHistoriesDir();

            mkdir(historiesDir);
            final File file = showSaveAsDialog(imageFrame, historiesDir,
                    historyProjectName, "history",
                    HistoryEditorFrame.historyFileExtensionFilter,
                    true, "Save History File");
            if (file != null) {
                historyProjectName = file.getName();
                final String dir = file.getParent();
                paths.addRecentDirectory(dir);
                paths.setHistoriesDir(dir);
                AppPrefs.save();

                final HistoryEditorPrefs prefs = new HistoryEditorPrefs();
                prefs.setFastGeneration(true);
                prefs.setMerge(false);
                prefs.setRestorePosition(true);
                prefs.setTrackCursor(true);
                prefs.setRecordPlayers(new boolean[4]);
                prefs.setViewPlayers(new boolean[]{true, true, false, false});

                final HistoryProject project = new HistoryProject();
                final CartFile cartFile = App.getCartFile();
                project.setEntryFileCRC(cartFile != null ? cartFile.getFileCRC() : 0);
                project.setEntryFileName(getEntryFileName());
                project.setBookmarks(new ArrayList<>());
                final ArrayList<HistoryChange> historyChanges = new ArrayList<>();
                historyChanges.add(new InitializationChange());
                project.setChanges(historyChanges);
                project.setChangesIndex(1);
                project.setHistoryEditorPrefs(prefs);
                project.setLastClickedRowIndex(movie.frameIndex);
                project.setMovie(movie);
                project.setHeadIndex(movie.frameIndex);
                project.setLastIndex(-1);
                project.setTailIndex(movie.frameIndex);
                project.setHistoryScrollValues(new IntPoint());
                project.setBookmarksScrollValues(new IntPoint());
                project.setChangesScrollValues(new IntPoint());

                final PleaseWaitDialog pleaseWaitDialog
                        = new PleaseWaitDialog(imageFrame);
                pleaseWaitDialog.setMessage("Saving history file...");
                new Thread(() -> saveHistoryProjectFile(pleaseWaitDialog, file,
                        project)).start();
                pleaseWaitDialog.showAfterDelay();
            }
        }
    }

    private static void saveHistoryProjectFile(
            final PleaseWaitDialog pleaseWaitDialog, final File file,
            final HistoryProject project) {

        boolean failed = false;
        try (final ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeObject(project);
        } catch (final Throwable t) {
            //t.printStackTrace();
            failed = true;
        } finally {
            final boolean showError = failed;
            pleaseWaitDialog.dispose();
            EDT.async(() -> {
                if (showError) {
                    displayError(imageFrame, "Failed to save history file.");
                } else {
                    AppPrefs.getInstance().getPaths().addRecentHistoryProject(
                            file.getPath());
                    AppPrefs.save();
                }
                App.setNoStepPause(false);
            });
        }
    }

    public static int getFileIndex(final int address, final boolean cpuMemory) {
        final Machine m = machine;
        if (m == null || address < 0) {
            return -1;
        }
        final NesFile file = nesFile;
        if (file == null) {
            return -1;
        }
        final Mapper mapper = m.getMapper();
        if (mapper == null) {
            return -1;
        }
        int index = file.getHeaderSize() + file.getTrainerSize();
        if (cpuMemory) {
            final int offset = mapper.getPrgRomIndex(address);
            if (offset < 0) {
                return -1;
            }
            index += offset;
        } else {
            final int offset = mapper.getChrRomIndex(address);
            if (offset < 0) {
                return -1;
            }
            index += file.getPrgRomSize() + offset;
        }
        return index;
    }

    private static void setDiskActivity(final boolean activity,
                                        final DiskActivityIndicator indicator) {

        int keyCode = 0;
        switch (indicator) {
            case NUM_LOCK:
                keyCode = VK_NUM_LOCK;
                break;
            case CAPS_LOCK:
                keyCode = VK_CAPS_LOCK;
                break;
            case SCROLL_LOCK:
                keyCode = VK_SCROLL_LOCK;
                break;
            case KANA_LOCK:
                keyCode = VK_KANA_LOCK;
                break;
            default:
                return;
        }

        try {
            Toolkit.getDefaultToolkit().setLockingKeyState(keyCode, activity);
        } catch (final UnsupportedOperationException u) {
        } catch (final Throwable t) {
            t.printStackTrace();
        }
    }

    public static void setDiskActivity(final boolean diskActivity) {
        if (fds) {
            if (diskActivityIndicatorChanged) {
                diskActivityIndicatorChanged = false;
                App.diskActivity = false;
                setDiskActivity(App.diskActivity, App.diskActivityIndicator);
                App.diskActivityIndicator = AppPrefs.getInstance()
                        .getFamicomDiskSystemPrefs().getDiskActivityIndicator();
            }
            if (diskActivity) {
                if (!App.diskActivity) {
                    App.diskActivity = true;
                    setDiskActivity(App.diskActivity, App.diskActivityIndicator);
                }
            } else if (App.diskActivity) {
                App.diskActivity = false;
                setDiskActivity(App.diskActivity, App.diskActivityIndicator);
            }
        }
    }

    public static void fireDiskActivityIndicatorChanged() {
        diskActivityIndicatorChanged = true;
    }

    public static String getLicense() {
        return license;
    }

    public static boolean loadFdsBIOS() {
        if (isFdsBiosLoaded()) {
            return true;
        }
        try {
            InputStream bios = getResourceAsStream("/bios/disksys.rom");
            loadFdsBIOS(bios, 0x2000);
            FilePath filePath = AppPrefs.getInstance().getFamicomDiskSystemPrefs().getBiosFile();
//            if (filePath == null) {
//                return false;
//            }
//            FileUtil.getInputStream(filePath, (in, length) -> {
//                try {
//                    loadFdsBIOS(in, length);
//                } catch (final Throwable t) {
//                    //t.printStackTrace();
//                }
//            });
        } catch (final Throwable t) {
            //t.printStackTrace();
        }
        return isFdsBiosLoaded();
    }

    public static void loadFdsBIOS(final InputStream inputStream,
                                   final long fileLength) throws Throwable {
        if (fileLength != 0x2000) {
            throw new MessageException("Invalid FDS BIOS file length.");
        }
        final int[] bios = new int[0x2000];
        try (BufferedInputStream s = new BufferedInputStream(inputStream)) {
            readByteArray(s, bios);
            fdsBIOS = bios;
        }
    }

    public static boolean isEmulator() {
        return imageFrame != null;
    }

    public static boolean isFdsBiosLoaded() {
        return fdsBIOS != null;
    }

    public static int[] getFdsBIOS() {
        return fdsBIOS;
    }

    public static AsmDasmFrame getAsmDasmFrame() {
        return asmDasmFrame;
    }

    public static DebuggerFrame getDebuggerFrame() {
        return debuggerFrame;
    }

    public static RamSearchFrame getRamSearchFrame() {
        return ramSearchFrame;
    }

    public static RamWatchFrame getRamWatchFrame() {
        return ramWatchFrame;
    }

    public static CheatSearchFrame getCheatSearchFrame() {
        return cheatSearchFrame;
    }

    public static GlassesFrame getGlassesFrame() {
        return glassesFrame;
    }

    public static HexEditorFrame getHexEditorFrame() {
        return hexEditorFrame;
    }

    public static HistoryEditorFrame getHistoryEditorFrame() {
        return historyEditorFrame;
    }

    public static ImageFrame getImageFrame() {
        return imageFrame;
    }

    public static ProgramFrame getProgramFrame() {
        return programFrame;
    }

    public static ProgramServerFrame getProgramServerFrame() {
        return programServerFrame;
    }

    public static NetplayServerFrame getNetworkServerFrame() {
        return netplayServerFrame;
    }

    public static NetplayClientFrame getNetworkClientFrame() {
        return netplayClientFrame;
    }

    public static VolumeMixerFrame getVolumeMixerFrame() {
        return volumeMixerFrame;
    }

    public static SubMonitorFrame getSubMonitorFrame() {
        return subMonitorFrame;
    }

    public static WatchHistoryFrame getWatchHistoryFrame() {
        return watchHistoryFrame;
    }

    public static MachineRunner getMachineRunner() {
        return machineRunner;
    }

    public static Machine getMachine() {
        return machine;
    }

    public static Cart getCart() {
        final CartFile cartFile = getCartFile();
        return cartFile != null ? cartFile.getCart() : null;
    }

    public static CartFile getCartFile() {
        return nesFile != null ? nesFile : unifFile;
    }

    public static UnifFile getUnifFile() {
        return unifFile;
    }

    public static NesFile getNesFile() {
        return nesFile;
    }

    public static FdsFile getFdsFile() {
        return fdsFile;
    }

    public static NsfFile getNsfFile() {
        return nsfFile;
    }

    public static IFile getFile() {
        if (nesFile != null) {
            return nesFile;
        } else if (fdsFile != null) {
            return fdsFile;
        } else if (unifFile != null) {
            return unifFile;
        } else if (nsfFile != null) {
            return nsfFile;
        } else {
            return null;
        }
    }

    public static boolean isFileLoaded() {
        return nesFile != null || fdsFile != null || unifFile != null
                || nsfFile != null;
    }

    public static String getEntryFileName() {
        return entryFileName;
    }

    public static ProgramServer getProgramServer() {
        return programServer;
    }

    public static NetplayServer getNetplayServer() {
        return netplayServer;
    }

    public static NetplayClient getNetplayClient() {
        return netplayClient;
    }

    // Boolean for invokeAndWait()
    public static Boolean showDipSwitchesDialog() {
        if (EventQueue.isDispatchThread()) {
            boolean ok = false;
            App.setNoStepPause(true);
            final List<DipSwitch> dipSwitches = App.getDipSwitches();
            if (dipSwitches != null && !dipSwitches.isEmpty()) {
                final DipSwitchesDialog dialog = new DipSwitchesDialog(imageFrame,
                        dipSwitches);
                dialog.setVisible(true);
                ok = dialog.isOK();
            }
            App.setNoStepPause(false);
            return ok;
        } else {
            GamePrefs.load(entryFileName);
            return EDT.sync(App::showDipSwitchesDialog);
        }
    }

    public static boolean isVsSystem() {
        final NesFile file = nesFile;
        return file != null && file.isVsSystem();
    }

    public static boolean isVsUniSystem() {
        final NesFile file = nesFile;
        return file != null && file.isVsUniSystem();
    }

    public static boolean isVsDualSystem() {
        final NesFile file = nesFile;
        return file != null && file.isVsDualSystem();
    }

    public static void startProgramServer() {
        if (EventQueue.isDispatchThread()) {
            new Thread(App::startProgramServer).start();
        } else {
            programServer.start();
        }
    }

    public static void stopProgramServer() {
        if (EventQueue.isDispatchThread()) {
            new Thread(App::stopProgramServer).start();
        } else {
            programServer.stop();
        }
    }

    public static void startNetplayServer() {
        if (EventQueue.isDispatchThread()) {
            new Thread(App::startNetplayServer).start();
        } else {
            netplayServer.start();
        }
    }

    public static void stopNetplayServer() {
        if (EventQueue.isDispatchThread()) {
            new Thread(App::stopNetplayServer).start();
        } else {
            netplayServer.stop();
        }
    }

    public static void startClient(final char[] password) {
        if (EventQueue.isDispatchThread()) {
            new Thread(() -> startClient(password)).start();
        } else {
            netplayClient.start(password);
        }
    }

    public static void stopClient() {
        if (EventQueue.isDispatchThread()) {
            new Thread(App::stopClient).start();
        } else {
            netplayClient.stop();
        }
    }

    public static Mapper loadFile(final DataInputStream in, final long fileSize,
                                  final String fileName) throws Throwable {
        return loadFile(in, fileSize, fileName, null);
    }

    public static Mapper loadFile(final DataInputStream in, final long fileSize,
                                  final String entryFileName, final String archiveFileName)
            throws Throwable {

        close();

        int fileType = getFileType(in);
        if (fileType == UNKNOWN && !isBlank(entryFileName)) {
            switch (getFileExtension(entryFileName)) {
                case "fds":
                    fileType = FDS;
                    break;
                case "unf":
                case "unif":
                    fileType = UNIF;
                    break;
                case "nsf":
                case "nsfe":
                    fileType = NSF;
                    break;
                default:
                    fileType = NES;
                    break;
            }
        }

        Mapper mapper = null;
        switch (fileType) {
            case FDS:
                nesFile = null;
                fdsFile = new FdsFile(in, fileSize, entryFileName, archiveFileName, fdsBIOS);
                unifFile = null;
                nsfFile = null;
                mapper = new FdsMapper(fdsFile);
                break;
            case UNIF:
                fdsFile = null;
                nesFile = null;
                unifFile = new UnifFile(in, fileSize, entryFileName, archiveFileName);
                nsfFile = null;
                mapper = Mapper.create(unifFile);
                break;
            case NSF:
                fdsFile = null;
                nesFile = null;
                unifFile = null;
                nsfFile = new NsfFile(in, fileSize, entryFileName, archiveFileName);
                mapper = new NsfMapper(nsfFile);
                break;
            default:
                fdsFile = null;
                nesFile = new NesFile(in, fileSize, entryFileName, archiveFileName);
                unifFile = null;
                nsfFile = null;
                mapper = Mapper.create(nesFile);
                break;
        }
        if (mapper == null) {
            throw new MessageException("Mapper %s is not supported.",
                    nesFile != null ? nesFile.getMapperNumber() : unifFile.getMapper());
        }
        final CartFile cartFile = getCartFile();
        if (cartFile != null && cartFile.isVsSystem()
                && cartFile.getVsGame() == null) {
            throw new MessageException("This VS. System game is not recognized.");
        }

        if (isBlank(entryFileName)) {
            App.entryFileName = getFileName(archiveFileName);
            historyProjectName = getFileNameWithoutExtension(archiveFileName);
        } else {
            App.entryFileName = getFileName(entryFileName);
            historyProjectName = getFileNameWithoutExtension(entryFileName);
        }

        if (isBlank(historyProjectName)) {
            historyProjectName = "game";
        }
        historyProjectName += ".history";
        imageFrame.updateContentPane(mapper, nsfFile);

        return mapper;
    }

    public static String createMachine(final Mapper mapper,
                                       final Machine ejectedMachine) {

        if (ejectedMachine != null && mapper.isVsDualSystem()) {
            throw new MessageException(
                    "Hot Swap to VS. DualSystem is not supported.");
        }

        GamePrefs.load(entryFileName);
        GameCheats.load(entryFileName);

        AppPrefs.getInstance().getInputs().autoConfigure(getCartFile());

        final VsGame vsGame = nesFile != null ? nesFile.getVsGame() : null;
        machine = (ejectedMachine == null) ? new Machine(mapper, vsGame)
                : new Machine(mapper, ejectedMachine);
        machine.getPPU().setNoSpriteLimit(AppPrefs.getInstance().getView()
                .isNoSpriteLimit());
        final boolean paletteUpdated;
        if (vsGame != null) {
            paletteUpdated = PaletteUtil.setVsPPU(vsGame.getPPU());
        } else if (nesFile != null && nesFile.isPlaychoice10()) {
            paletteUpdated = PaletteUtil.usePlayChoice10PPU();
        } else {
            paletteUpdated = PaletteUtil.setPalettePPU(PalettePPU._2C02);
        }
        if (paletteUpdated) {
            imageFrame.createPaletteMenu();
        }
        imageFrame.setMachine(machine);
        mapper.setMachine(machine);
        if (vsGame != null) {
            mapper.setDipSwitchesValue(DipSwitch.evaluate(vsGame.getDipSwitches(),
                    GamePrefs.getInstance().getDipSwitchesGamePrefs()
                            .getDipSwitchValues()));
            InputUtil.setVsGame(vsGame);
        } else {
            InputUtil.setVsGame(null);
        }
        mapper.loadNonVolatilePrgRam();

        final PPU ppu = machine.getPPU();
        SetTVSystem.run(machine, mapper.getPreferredTVSystem());

        machine.getAPU().setAudioProcessor(systemAudioProcessor);
        ppu.setScreenRenderer(imageFrame.getImagePane());
        if (ejectedMachine == null) {
            machine.getCPU().init();
        }
        mapper.init();

        machineRunner = new MachineRunner(machine);
        App.setTrackHistory(AppPrefs.getInstance().getHistoryPrefs()
                .isTrackHistory());

        if (isGyromite()) {
            new SetupROB(new GyromiteController()).run(machine);
        } else if (isStackUp()) {
            new SetupROB(new StackUpController()).run(machine);
        } else if (isVsDualSystem()) {
            createSubMonitorFrame();
        }

        updateFrames(machineRunner);
        InputUtil.pollControllers(machine);
        netplayServer.setMachineRunner(machineRunner);
        new Thread(machineRunner, "Machine Runner Thread").start();

        final StringBuilder sb = new StringBuilder();
        sb.append(getFile());
        sb.append(System.lineSeparator());
        sb.append(String.format("PRG ROM banks: %d%n", mapper.getPrgBankCount()));
        sb.append(String.format("PRG ROM bank size: %d bytes%n",
                mapper.getPrgBankSize()));
        sb.append(String.format("CHR ROM banks: %d%n", mapper.getChrBankCount()));
        sb.append(String.format("CHR ROM bank size: %d bytes%n",
                mapper.getChrBankSize()));

        return sb.toString();
    }

    public static void saveState(final Window parent, final File file,
                                 final int slot) {
        saveState(parent, file, slot, null);
    }

    public static void saveState(final Window parent, final File file,
                                 final int slot, final Runnable saveListener) {
        final Machine m = machine;
        if (m != null) {
            setNoStepPause(true);
            try (final ByteArrayOutputStream baos = toByteArrayOutputStream(m)) {
                new Thread(() -> {
                    mkdir(file.getParent());
                    try (DataOutputStream out = new DataOutputStream(
                            new BufferedOutputStream(new FileOutputStream(file)))) {
                        writeByteArrayOutputStream(out, baos);
                        out.close();
                        if (saveListener != null) {
                            EDT.async(saveListener);
                        }
                        showMessage(slot < 0 ? "SAVED" : String.format("SAVED (%d)", slot));
                    } catch (Throwable t) {
                        t.printStackTrace();
                        displayError(parent, "Failed to save game state.");
                    }
                }).start();
            } catch (Throwable t) {
                t.printStackTrace();
                displayError(parent, "Failed to save game state.");
            } finally {
                setNoStepPause(false);
            }
        }
    }

    public static void loadState(final Window parent, final File file,
                                 final int slot) {
        if (file == null || !file.exists()) {
            displayError(parent, "Failed to load game state: File not found.");
            return;
        }
        new Thread(() -> {
            try (final DataInputStream in = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(file)))) {
                final MachineRunner m = new MachineRunner((Machine) readObject(in));
                loadState(nesFile, fdsFile, unifFile, nsfFile, m);
                m.getMachine().getPPU().setNoSpriteLimit(AppPrefs.getInstance()
                        .getView().isNoSpriteLimit());
                InputUtil.setMachine(m.getMachine());
                netplayServer.postSaveState(m);
                final HistoryEditorFrame historyEditor = historyEditorFrame;
                if (historyEditor != null) {
                    historyEditor.setMachineRunner(m);
                } else {
                    new Thread(m).start();
                }
                showMessage(slot < 0 ? "LOADED" : String.format("LOADED (%d)", slot));
            } catch (Throwable t) {
                //t.printStackTrace();
                displayError(parent, "Failed to load game state.");
            }
        }).start();
    }

    public static void loadState(final IFile file, final MachineRunner runner) {
        NesFile nesFile = null;
        FdsFile fdsFile = null;
        UnifFile unifFile = null;
        NsfFile nsfFile = null;
        switch (file.getFileType()) {
            case NES:
                nesFile = (NesFile) file;
                break;
            case FDS:
                fdsFile = (FdsFile) file;
                break;
            case UNIF:
                unifFile = (UnifFile) file;
                break;
            case NSF:
                nsfFile = (NsfFile) file;
                break;
        }
        loadState(nesFile, fdsFile, unifFile, nsfFile, runner);
    }

    private static void loadState(final NesFile nes, final FdsFile fds,
                                  final UnifFile unif, final NsfFile nsf, final MachineRunner r) {

        close(false, true);
        nesFile = nes;
        fdsFile = fds;
        unifFile = unif;
        nsfFile = nsf;

        machineRunner = r;
        if (machineRunner != null) {
            machine = machineRunner.getMachine();
            final Mapper mapper = machine.getMapper();
            mapper.restore(nesFile);
            mapper.restore(unifFile);
            mapper.restore(fdsFile);
            mapper.restore(nsfFile);
            imageFrame.setMachine(machine);
            updateFrames(machineRunner);
            machine.getAPU().setAudioProcessor(systemAudioProcessor);
            machine.getPPU().setScreenRenderer(imageFrame.getImagePane());
            final VsGame vsGame = nesFile != null ? nesFile.getVsGame() : null;
            if (vsGame != null) {
                InputUtil.setVsGame(vsGame);
                if (vsGame.isDualSystemGame()) {
                    final SubMonitorFrame subMonitor = subMonitorFrame;
                    if (subMonitor != null) {
                        ((MainCPU) machine.getCPU()).getSubPPU().setScreenRenderer(
                                subMonitor.getImagePane());
                    }
                }
            } else {
                InputUtil.setVsGame(null);
            }
        }
        App.setTrackHistory(AppPrefs.getInstance().getHistoryPrefs()
                .isTrackHistory());
    }

    public static void handleDipSwitchChange(final List<DipSwitch> dipSwitches) {
        final MachineRunner r = machineRunner;
        if (r != null) {
            InputUtil.addOtherInput(new ChangeDipSwitches(DipSwitch
                    .evaluate(dipSwitches, GamePrefs.getInstance()
                            .getDipSwitchesGamePrefs().getDipSwitchValues())));
        }
    }

    public static List<DipSwitch> getDipSwitches() {
        final NesFile file = getNesFile();
        if (file == null || !file.isVsSystem()) {
            return null;
        }
        final VsGame vsGame = file.getVsGame();
        return vsGame == null ? DipSwitch.createDefaultDipSwitches()
                : vsGame.getDipSwitches();
    }

    public static void handleFrameRendered(final MachineRunner machineRunner) {
        final GlassesFrame glasses = glassesFrame;
        if (glasses != null) {
            glasses.update(machineRunner);
        }
        final HexEditorFrame hexEditor = hexEditorFrame;
        if (hexEditor != null) {
            hexEditor.update();
        }
        final CheatSearchFrame cheatSearch = cheatSearchFrame;
        if (cheatSearch != null) {
            cheatSearch.update();
        }
        final RamSearchFrame ramSearch = ramSearchFrame;
        if (ramSearch != null) {
            ramSearch.update();
        }
        final RamWatchFrame ramWatch = ramWatchFrame;
        if (ramWatch != null) {
            ramWatch.update();
        }
        final DebuggerFrame debugger = debuggerFrame;
        if (debugger != null) {
            debugger.update();
        }
    }

    public static void scanlineRendered(final int scanline) {
        final OamDataFrame oamData = oamDataFrame;
        if (oamData != null) {
            oamData.update(scanline);
        }
        final PatternTablesFrame patternTables = patternTablesFrame;
        if (patternTables != null) {
            patternTables.update(scanline);
        }
        final NametablesFrame nametables = nametablesFrame;
        if (nametables != null) {
            nametables.update(scanline);
        }
        final MapMakerFrame mapMaker = mapMakerFrame;
        if (mapMaker != null) {
            mapMaker.update(scanline);
        }
        final SpriteSaverFrame spriteSaver = spriteSaverFrame;
        if (spriteSaver != null) {
            spriteSaver.update(scanline);
        }
        final LocalAPI api = localAPI;
        if (api != null) {
            api.scanlineRendered(scanline);
        }
    }

    public static void firePauseChanged(final boolean paused) {
        final DebuggerFrame debugger = debuggerFrame;
        if (debugger != null) {
            debugger.onPausedChanged(paused);
        }
    }

    public static void fireStepPausedChanged(final boolean stepPause) {
        EDT.async(() -> {
            final ImageFrame image = imageFrame;
            if (image != null) {
                image.onStepPausedChanged(stepPause);
            }
            final CheatSearchFrame cheatSearch = cheatSearchFrame;
            if (cheatSearch != null) {
                cheatSearch.onStepPausedChanged(stepPause);
            }
            final RamSearchFrame ramSearch = ramSearchFrame;
            if (ramSearch != null) {
                ramSearch.onStepPausedChanged(stepPause);
            }
        });
    }

    public static void createHistoryEditorFrame() {
        EDT.async(() -> {
            destroyWatchHistoryFrame();
            if (historyEditorFrame == null) {
                setAppMode(AppMode.HistoryEditor);
                historyEditorFrame = new HistoryEditorFrame(machineRunner != null
                        && machineRunner.isRunning() ? machineRunner : null);
                historyEditorFrame.setVisible(true);
            } else {
                GuiUtil.toFront(historyEditorFrame);
            }
        });
    }

    public static void destroyHistoryEditorFrame() {
        EDT.async(() -> {
            if (historyEditorFrame != null) {
                setAppMode(AppMode.Default);
                historyEditorFrame.destroy();
                historyEditorFrame = null;
            }
        });
    }

    public static void createProgramServerFrame() {
        EDT.async(() -> {
            if (programServerFrame == null) {
                programServerFrame = new ProgramServerFrame();
                programServerFrame.setVisible(true);
            } else {
                GuiUtil.toFront(programServerFrame);
            }
        });
    }

    public static void destroyProgramServerFrame() {
        EDT.async(() -> {
            if (programServerFrame != null) {
                programServerFrame.destroy();
                programServerFrame = null;
            }
        });
    }

    public static void createNetplayServerFrame() {
        EDT.async(() -> {
            if (netplayServerFrame == null) {
                setAppMode(AppMode.NetplayServer);
                destroyBarcodeBattlerFrame();
                destroyGlassesFrame();
                netplayServerFrame = new NetplayServerFrame();
                netplayServerFrame.setVisible(true);
            } else {
                GuiUtil.toFront(netplayServerFrame);
            }
        });
    }

    public static void destroyNetplayServerFrame() {
        EDT.async(() -> {
            if (netplayServerFrame != null) {
                setAppMode(AppMode.Default);
                netplayServerFrame.destroy();
                netplayServerFrame = null;
            }
        });
    }

    public static void createNetplayClientFrame() {
        EDT.async(() -> {
            if (netplayClientFrame == null) {
                setAppMode(AppMode.NetplayClient);
                netplayClientFrame = new NetplayClientFrame();
                netplayClientFrame.setVisible(true);
            } else {
                GuiUtil.toFront(netplayClientFrame);
            }
        });
    }

    public static void destroyNetplayClientFrame() {
        EDT.async(() -> {
            if (netplayClientFrame != null) {
                setAppMode(AppMode.Default);
                netplayClientFrame.destroy();
                netplayClientFrame = null;
            }
        });
    }

    public static void createAsmDasmFrame() {
        EDT.async(() -> {
            if (asmDasmFrame == null) {
                asmDasmFrame = new AsmDasmFrame();
                asmDasmFrame.setVisible(true);
            } else {
                GuiUtil.toFront(asmDasmFrame);
            }
        });
    }

    public static void destroyAsmDasmFrame() {
        EDT.async(() -> {
            if (asmDasmFrame != null) {
                asmDasmFrame.destroy();
                asmDasmFrame = null;
            }
        });
    }

    public static void createDebuggerFrame() {
        EDT.async(() -> {
            if (debuggerFrame == null) {
                debuggerFrame = new DebuggerFrame(machineRunner != null
                        && machineRunner.isRunning() ? machineRunner : null);
                debuggerFrame.setVisible(true);
            } else {
                GuiUtil.toFront(debuggerFrame);
            }
        });
    }

    public static void destroyDebuggerFrame() {
        EDT.async(() -> {
            if (debuggerFrame != null) {
                debuggerFrame.destroy();
                debuggerFrame = null;
            }
        });
    }

    public static void createRamWatchFrame() {
        EDT.async(() -> {
            if (ramWatchFrame == null) {
                ramWatchFrame = new RamWatchFrame(getRunningMachine());
                ramWatchFrame.setVisible(true);
            } else {
                GuiUtil.toFront(ramWatchFrame);
            }
        });
    }

    public static void destroyRamWatchFrame() {
        EDT.async(() -> {
            if (ramWatchFrame != null) {
                ramWatchFrame.destroy();
                ramWatchFrame = null;
            }
        });
    }

    public static void createRamSearchFrame() {
        EDT.async(() -> {
            if (ramSearchFrame == null) {
                ramSearchFrame = new RamSearchFrame(getRunningMachine());
                ramSearchFrame.setVisible(true);
            } else {
                GuiUtil.toFront(ramSearchFrame);
            }
        });
    }

    public static void destroyRamSearchFrame() {
        EDT.async(() -> {
            if (ramSearchFrame != null) {
                ramSearchFrame.destroy();
                ramSearchFrame = null;
            }
        });
    }

    public static void createSpriteSaverFrame() {
        EDT.async(() -> {
            if (spriteSaverFrame == null) {
                spriteSaverFrame = new SpriteSaverFrame(getRunningMachine());
                spriteSaverFrame.setVisible(true);
            } else {
                GuiUtil.toFront(spriteSaverFrame);
            }
        });
    }

    public static void destroySpriteSaverFrame() {
        EDT.async(() -> {
            if (spriteSaverFrame != null) {
                spriteSaverFrame.destroy();
                spriteSaverFrame = null;
            }
        });
    }

    public static void createMapMakerFrame() {
        EDT.async(() -> {
            if (mapMakerFrame == null) {
                final Machine m = machine;
                mapMakerFrame = new MapMakerFrame(getRunningMachine());
                mapMakerFrame.setVisible(true);
            } else {
                GuiUtil.toFront(mapMakerFrame);
            }
        });
    }

    public static void destroyMapMakerFrame() {
        EDT.async(() -> {
            if (mapMakerFrame != null) {
                mapMakerFrame.destroy();
                mapMakerFrame = null;
            }
        });
    }

    public static void createNametablesFrame() {
        EDT.async(() -> {
            if (nametablesFrame == null) {
                nametablesFrame = new NametablesFrame(machineRunner);
                nametablesFrame.setVisible(true);
            } else {
                GuiUtil.toFront(nametablesFrame);
            }
        });
    }

    public static void destroyNametablesFrame() {
        EDT.async(() -> {
            if (nametablesFrame != null) {
                nametablesFrame.destroy();
                nametablesFrame = null;
            }
        });
    }

    public static void createOamDataFrame() {
        EDT.async(() -> {
            if (oamDataFrame == null) {
                oamDataFrame = new OamDataFrame(machineRunner);
                oamDataFrame.setVisible(true);
            } else {
                GuiUtil.toFront(oamDataFrame);
            }
        });
    }

    public static void destroyOamDataFrame() {
        EDT.async(() -> {
            if (oamDataFrame != null) {
                oamDataFrame.destroy();
                oamDataFrame = null;
            }
        });
    }

    public static void createPatternTablesFrame() {
        EDT.async(() -> {
            if (patternTablesFrame == null) {
                patternTablesFrame = new PatternTablesFrame(machineRunner);
                patternTablesFrame.setVisible(true);
            } else {
                GuiUtil.toFront(patternTablesFrame);
            }
        });
    }

    public static void destroyPatternTablesFrame() {
        EDT.async(() -> {
            if (patternTablesFrame != null) {
                patternTablesFrame.destroy();
                patternTablesFrame = null;
            }
        });
    }

    public static void createBackgroundEditorFrame() {
        EDT.async(() -> {
            if (backgroundEditorFrame == null) {
                final Machine m = getRunningMachine();
                if (m != null) {
                    backgroundEditorFrame = new BackgroundEditorFrame(m);
                    backgroundEditorFrame.setVisible(true);
                }
            } else {
                GuiUtil.toFront(backgroundEditorFrame);
            }
        });
    }

    public static void destroyBackgroundEditorFrame() {
        EDT.async(() -> {
            if (backgroundEditorFrame != null) {
                backgroundEditorFrame.destroy();
                backgroundEditorFrame = null;
            }
        });
    }

    public static void createCheatSearchFrame() {
        EDT.async(() -> {
            if (cheatSearchFrame == null) {
                cheatSearchFrame = new CheatSearchFrame(getRunningMachine());
                cheatSearchFrame.setVisible(true);
            } else {
                GuiUtil.toFront(cheatSearchFrame);
            }
        });
    }

    public static void destroyCheatSearchFrame() {
        EDT.async(() -> {
            if (cheatSearchFrame != null) {
                cheatSearchFrame.destroy();
                cheatSearchFrame = null;
            }
        });
    }

    public static void createHexEditorFrame() {
        EDT.async(() -> {
            if (hexEditorFrame == null) {
                hexEditorFrame = new HexEditorFrame();
                hexEditorFrame.setMachine(getRunningMachine());
                hexEditorFrame.setVisible(true);
            } else {
                GuiUtil.toFront(hexEditorFrame);
            }
        });
    }

    public static void destroyHexEditorFrame() {
        EDT.async(() -> {
            if (hexEditorFrame != null) {
                hexEditorFrame.destroy();
                hexEditorFrame = null;
            }
        });
    }

    public static void updateRobFrame(final RobController rob) {
        if (rob != null) {
            updateRobFrame(rob.getState());
        }
    }

    public static void updateRobFrame(final RobState state) {
        final RobFrame frame = robFrame;
        if (frame != null) {
            frame.render(state);
        }
    }

    public static void createRobFrame(final int game) {
        EDT.async(() -> {
            if (game == RobGame.NONE) {
                destroyRobFrame();
            } else if (robFrame == null) {
                robFrame = new RobFrame(game);
                forwardKeyEvents(robFrame, imageFrame);
                robFrame.setVisible(true);
                GuiUtil.toFront(imageFrame);
            } else {
                robFrame.setGame(game);
                GuiUtil.toFront(robFrame);
                GuiUtil.toFront(imageFrame);
            }
        });
    }

    public static void destroyRobFrame() {
        EDT.async(() -> {
            InputUtil.setRob(null);
            if (robFrame != null) {
                robFrame.destroy();
                robFrame = null;
            }
        });
    }

    public static void updateGlassesFrame(final int[] screen) {
        final GlassesFrame frame = glassesFrame;
        if (frame != null) {
            frame.update(screen);
        }
    }

    public static void createGlassesFrame() {
        EDT.async(() -> {
            if (glassesFrame == null) {
                glassesFrame = new GlassesFrame();
                forwardKeyEvents(glassesFrame, imageFrame);
                glassesFrame.setVisible(true);
            } else {
                GuiUtil.toFront(glassesFrame);
            }
            GuiUtil.toFront(imageFrame);
        });
    }

    public static void destroyGlassesFrame() {
        EDT.async(() -> {
            if (glassesFrame != null) {
                glassesFrame.destroy();
                glassesFrame = null;
            }
        });
    }

    public static void createBarcodeBattlerFrame() {
        EDT.async(() -> {
            if (barcodeBattlerFrame == null) {
                barcodeBattlerFrame = new BarcodeBattlerFrame();
                barcodeBattlerFrame.setVisible(true);
            } else {
                GuiUtil.toFront(barcodeBattlerFrame);
            }
            GuiUtil.toFront(imageFrame);
        });
    }

    public static void destroyBarcodeBattlerFrame() {
        EDT.async(() -> {
            if (barcodeBattlerFrame != null) {
                barcodeBattlerFrame.destroy();
                barcodeBattlerFrame = null;
            }
        });
    }

    public static void createApplyIpsPatchFrame() {
        EDT.async(() -> {
            if (applyIpsPatchFrame == null) {
                applyIpsPatchFrame = new PatchFrame(true);
                applyIpsPatchFrame.setVisible(true);
            } else {
                GuiUtil.toFront(applyIpsPatchFrame);
            }
        });
    }

    public static void destroyApplyIpsPatchFrame() {
        EDT.async(() -> {
            if (applyIpsPatchFrame != null) {
                applyIpsPatchFrame.destroy();
                applyIpsPatchFrame = null;
            }
        });
    }

    public static void createCreateIpsPatchFrame() {
        EDT.async(() -> {
            if (createIpsPatchFrame == null) {
                createIpsPatchFrame = new PatchFrame(false);
                createIpsPatchFrame.setVisible(true);
            } else {
                GuiUtil.toFront(createIpsPatchFrame);
            }
        });
    }

    public static void destroyCreateIpsPatchFrame() {
        EDT.async(() -> {
            if (createIpsPatchFrame != null) {
                createIpsPatchFrame.destroy();
                createIpsPatchFrame = null;
            }
        });
    }

    public static void createWatchHistoryFrame() {
        EDT.async(() -> {
            destroyHistoryEditorFrame();
            if (watchHistoryFrame == null) {
                setAppMode(AppMode.WatchHistory);
                watchHistoryFrame = new WatchHistoryFrame();
                watchHistoryFrame.setVisible(true);
            } else {
                GuiUtil.toFront(watchHistoryFrame);
            }
        });
    }

    public static void destroyWatchHistoryFrame() {
        EDT.async(() -> {
            if (watchHistoryFrame != null) {
                setAppMode(AppMode.Default);
                watchHistoryFrame.destroy();
                watchHistoryFrame = null;
            }
        });
    }

    public static void createVolumeMixerFrame() {
        EDT.async(() -> {
            if (volumeMixerFrame == null) {
                volumeMixerFrame = new VolumeMixerFrame();
                volumeMixerFrame.setVisible(true);
            } else {
                GuiUtil.toFront(volumeMixerFrame);
            }
        });
    }

    public static void destroyVolumeMixerFrame() {
        EDT.async(() -> {
            if (volumeMixerFrame != null) {
                volumeMixerFrame.destroy();
                volumeMixerFrame = null;
            }
        });
    }

    public static void createSubMonitorFrame() {
        EDT.async(() -> {
            if (subMonitorFrame == null) {
                subMonitorFrame = new SubMonitorFrame();
                forwardKeyEvents(subMonitorFrame, imageFrame);
                subMonitorFrame.setVisible(true);
                EDT.async(() -> GuiUtil.toFront(imageFrame));
            } else {
                GuiUtil.toFront(subMonitorFrame);
            }
            subMonitorFrame.init();
        });
    }

    public static void destroySubMonitorFrame() {
        EDT.async(() -> {
            if (subMonitorFrame != null) {
                subMonitorFrame.destroy();
                subMonitorFrame = null;
            }
        });
    }

    public static void createProgramFrame() {
        EDT.async(() -> {
            if (programFrame == null) {
                programFrame = new ProgramFrame();
                programFrame.setVisible(true);
            } else {
                GuiUtil.toFront(programFrame);
            }
        });
    }

    public static void destroyProgramFrame() {
        EDT.async(() -> {
            if (programFrame != null) {
                programFrame.destroy();
                programFrame = null;
            }
        });
    }

    public static void runSubMonitorFrame(final SubMonitorFrameFunction function) {
        final SubMonitorFrame frame = subMonitorFrame;
        if (frame != null) {
            function.f(frame);
        }
    }

    public static void runVsDualImagePane(final ImagePane imagePane,
                                          final ImagePaneFunction function) {

        final SubMonitorFrame frame = subMonitorFrame;
        if (frame == null) {
            return;
        }
        final ImagePane pane = frame.getImagePane();
        if (pane == null || pane == imagePane) {
            return;
        }
        function.f(pane);
    }

    public static void setTrackHistory(final boolean trackHistory) {

        clearRewindTime();
        clearHighSpeed();

        final MachineRunner r = machineRunner;
        if (r != null) {
            if (trackHistory) {
                Movie movie = r.getMovie();
                if (movie == null) {
                    movie = new Movie(isVsDualSystem());
                    r.setMovie(movie);
                    r.getMapper().updateButtons(0);
                    SystemAudioProcessor.setMovie(movie);
                }
                imageFrame.setHistoryTracking(true);
            } else {
                r.setMovie(null);
                r.getMapper().updateButtons(0);
                SystemAudioProcessor.setMovie(null);
                imageFrame.setHistoryTracking(false);
            }
        } else {
            imageFrame.setHistoryTracking(false);
        }
    }

    public static void clearHighSpeed() {
        highSpeedValue = 0;
    }

    public static void requestHighSpeed(final int portIndex) {
        highSpeedValue = toggleBit(highSpeedValue, portIndex);
    }

    public static void requestHighSpeed(final int portIndex,
                                        final boolean highSpeed) {
        highSpeedValue = setBit(highSpeedValue, portIndex, highSpeed);
    }

    public static void updateHighSpeed() {
        if (netplayClient.isRunning()) {
            netplayClient.post(HighSpeed, highSpeedValue);
        } else {
            if (netplayServer.isRunning()) {
                highSpeedValue = netplayServer.mergeHighSpeed(highSpeedValue);
            } else {
                highSpeedValue &= 0x0F;
            }
            setHighSpeed(highSpeedValue != 0);
        }
    }

    public static void clearRewindTime() {
        rewindTimeValue = 0;
    }

    public static void requestRewindTime(final int portIndex) {
        rewindTimeValue = toggleBit(rewindTimeValue, portIndex);
    }

    public static void requestRewindTime(final int portIndex,
                                         final boolean rewindTime) {
        rewindTimeValue = setBit(rewindTimeValue, portIndex, rewindTime);
    }

    public static void updateRewindTime() {
        if (netplayClient.isRunning()) {
            netplayClient.post(Rewind, rewindTimeValue);
        } else {
            if (netplayServer.isRunning()) {
                rewindTimeValue = netplayServer.mergeRewindTime(rewindTimeValue);
            } else {
                rewindTimeValue &= 0x1F;
            }
            final boolean rewindTime = rewindTimeValue != 0;
            final MachineRunner r = machineRunner;
            if (r != null) {
                if (rewindTime) {
                    if (r.isForwardTime()) {
                        final Movie movie = r.getMovie();
                        if (movie != null && !movie.movieBlocks.isEmpty()) {
                            updateFrames(null);
                            imageFrame.setTimeRewinding(true);
                            r.setForwardTime(false);
                        }
                    }
                } else if (!r.isForwardTime()) {
                    imageFrame.setTimeRewinding(false);
                    updateFrames(machineRunner);
                    r.setForwardTime(true);
                }
            }
        }
    }

    public static void setNoStepPause(final boolean noStepPause) {
        final MachineRunner r = machineRunner;
        if (r != null) {
            r.setNoStepPause(noStepPause);
        }
        if (noStepPause) {
            SystemAudioProcessor.flush();
        }
    }

    public static void setStepPause(final boolean stepPause) {
        final MachineRunner r = machineRunner;
        if (r != null) {
            r.setStepPause(stepPause);
        }
        if (stepPause) {
            SystemAudioProcessor.flush();
        }
    }

    public static void step(final PauseStepType pauseStepType) {
        final MachineRunner r = machineRunner;
        if (r != null) {
            r.step(pauseStepType);
        }
    }

    public static void stepToAddress(final int address) {
        final MachineRunner r = machineRunner;
        if (r != null) {
            r.stepToAddress(address);
        }
    }

    public static void stepToScanline(final int scanline) {
        final MachineRunner r = machineRunner;
        if (r != null) {
            r.stepToScanline(scanline);
        }
    }

    public static void stepToDot(final int scanlineCycle) {
        final MachineRunner r = machineRunner;
        if (r != null) {
            r.stepToDot(scanlineCycle);
        }
    }

    public static void stepToOpcode(final int opcode) {
        final MachineRunner r = machineRunner;
        if (r != null) {
            r.stepToOpcode(opcode);
        }
    }

    public static void stepToInstructions(final int instructions) {
        final MachineRunner r = machineRunner;
        if (r != null) {
            r.stepToInstructions(instructions);
        }
    }

    public static void setBreakpoints(final List<Breakpoint> breakpoints) {
        final MachineRunner r = machineRunner;
        if (r != null) {
            r.setBreakpoints(breakpoints);
        }
    }

    public static void setHighSpeed(final boolean highSpeed) {
        if (TimeUtil.isHighSpeed() != highSpeed) {
            TimeUtil.setHighSpeed(highSpeed);
            APU.setNormalSpeed(TimeUtil.getSpeed() == 100 && !highSpeed);
        }
    }

    public static void setSpeed(int percent) {
        if (percent < 1) {
            percent = 0;
        }
        TimeUtil.setSpeed(percent);
        APU.setNormalSpeed(percent == 100 && !TimeUtil.isHighSpeed());
    }

    public static int getSpeed() {
        return TimeUtil.getSpeed();
    }

    public static void showMessage(final String message) {
        imageFrame.getImagePane().showMessage(message);
        if (netplayServer.isRunning()) {
            netplayServer.post(ShowMessage, message);
        }
    }

    public static void reset() {
        if (machineRunner != null) {
            InputUtil.addOtherInput(new Reset());
        }
    }

    // erase NVRAM, power off, power on
    public static String eraseBatterySave() {
        if (machineRunner != null) {
            GamePrefs.getInstance().eraseNonVolatileRam();
            GamePrefs.save();
        }
        return powerCycle(false, false);
    }

    // power off, power on
    public static String powerCycle() {
        return powerCycle(false, true);
    }

    // power off, power on
    public static String powerCycle(final boolean closeFrames,
                                    final boolean saveNonVolatileData) {

        final String entryFile = entryFileName;
        final FdsFile fds = fdsFile;
        final NesFile nes = nesFile;
        final UnifFile unif = unifFile;
        final NsfFile nsf = nsfFile;

        close(closeFrames, saveNonVolatileData);

        entryFileName = entryFile;
        fdsFile = fds;
        nesFile = nes;
        unifFile = unif;
        nsfFile = nsf;

        Mapper mapper = null;
        if (fds != null) {
            mapper = new FdsMapper(fds);
        } else if (nes != null) {
            mapper = Mapper.create(nes);
        } else if (unif != null) {
            mapper = Mapper.create(unif);
        } else if (nsf != null) {
            mapper = new NsfMapper(nsfFile);
        }

        return createMachine(mapper, null);
    }

    public static void startTraceLogger() {
        disposeTraceLogger();
        final MachineRunner r = machineRunner;
        if (r != null) {
            traceLogger = new TraceLogger();
            r.setTraceLogger(traceLogger);
        }
    }

    public static boolean isTraceLoggerRunning() {
        return traceLogger != null;
    }

    public static void flushTraceLogger() {
        final TraceLogger logger = traceLogger;
        if (logger != null) {
            logger.flush();
        }
    }

    public static void disposeTraceLogger() {
        final TraceLogger logger = traceLogger;
        if (logger != null) {
            traceLogger = null;
            final MachineRunner r = machineRunner;
            if (r != null) {
                r.setTraceLogger(null);
            }
            logger.dispose();
        }
    }

    public static void setMachineRunner(final MachineRunner machineRunner) {
        App.machineRunner = machineRunner;
        if (machineRunner == null) {
            App.machine = null;
            setDiskActivity(false);
            diskActivityIndicatorChanged = true;
            fds = false;
        } else {
            App.machine = machineRunner.getMachine();
            fds = App.machine.getMapper().isFdsMapper();
        }
        App.setTrackHistory(AppPrefs.getInstance().getHistoryPrefs()
                .isTrackHistory());
    }

    public static void updateFrames(final MachineRunner machineRunner) {
        final Machine m = machineRunner != null ? machineRunner.getMachine() : null;
        final ImageFrame image = imageFrame;
        if (image != null) {
            image.setMachine(m);
        }
        final HexEditorFrame hexEditor = hexEditorFrame;
        if (hexEditor != null) {
            hexEditor.setMachine(m);
        }
        final CheatSearchFrame cheatSearch = cheatSearchFrame;
        if (cheatSearch != null) {
            cheatSearch.setMachine(m);
        }
        final OamDataFrame oamData = oamDataFrame;
        if (oamData != null) {
            oamData.setMachineRunner(machineRunner);
        }
        final PatternTablesFrame patternTables = patternTablesFrame;
        if (patternTables != null) {
            patternTables.setMachineRunner(machineRunner);
        }
        final BackgroundEditorFrame backgroundEditor = backgroundEditorFrame;
        if (backgroundEditor != null) {
            backgroundEditor.setMachine(m);
        }
        final NametablesFrame nametables = nametablesFrame;
        if (nametables != null) {
            nametables.setMachineRunner(machineRunner);
        }
        final MapMakerFrame maperMaker = mapMakerFrame;
        if (maperMaker != null) {
            maperMaker.setMachine(m);
        }
        final SpriteSaverFrame spriteSaver = spriteSaverFrame;
        if (spriteSaver != null) {
            spriteSaver.setMachine(m);
        }
        final RamSearchFrame ramSearch = ramSearchFrame;
        if (ramSearch != null) {
            ramSearch.setMachine(m);
        }
        final RamWatchFrame ramWatch = ramWatchFrame;
        if (ramWatch != null) {
            ramWatch.setMachine(m);
        }
        final DebuggerFrame debugger = debuggerFrame;
        if (debugger != null) {
            debugger.setMachineRunner(machineRunner);
        }
        final LocalAPI api = localAPI;
        if (api != null) {
            api.setMachineRunner(machineRunner);
        }
    }

    public static void setLocalAPI(final LocalAPI localAPI) {
        App.localAPI = localAPI;
        if (localAPI != null) {
            localAPI.setMachineRunner(machineRunner);
        }
    }

    public static LocalAPI getLocalAPI() {
        return localAPI;
    }

    public static void close() {
        close(true, true);
    }

    public static void close(final boolean closeFrames,
                             final boolean saveNonVolatileData) {
        setStepPause(false);
        clearRewindTime();
        clearHighSpeed();
        final Machine m = machine;
        if (m != null) {
            m.getMapper().close(saveNonVolatileData);
        }
        if (closeFrames) {
            destroyBarcodeBattlerFrame();
            destroyGlassesFrame();
            destroyRobFrame();
            destroyWatchHistoryFrame();
            destroySubMonitorFrame();
        }
        imageFrame.updateContentPane(null, null);
        imageFrame.setHistoryTracking(false);
        dispose();
        netplayServer.setMachineRunner(null);
        setSpeed(100);
        imageFrame.getImagePane().setCursorType(CursorType.Default);
    }

    public static void dispose() {
        final MachineRunner r = machineRunner;
        if (r != null) {
            r.dispose();
        }
        imageFrame.getImagePane().clearScreen();
        updateFrames(null);
        AppPrefs.getInstance().getInputs().autoConfigure();
        AppPrefs.save();
        InputUtil.setVsGame(null);
        GamePrefs.dispose();
        setMachineRunner(null);
        SystemAudioProcessor.flush();
        nesFile = null;
        fdsFile = null;
        unifFile = null;
        nsfFile = null;
    }

    public static void destroyFrames() {
        destroyApplyIpsPatchFrame();
        destroyAsmDasmFrame();
        destroyBackgroundEditorFrame();
        destroyBarcodeBattlerFrame();
        destroyCheatSearchFrame();
        destroyCreateIpsPatchFrame();
        destroyDebuggerFrame();
        destroyGlassesFrame();
        destroyHexEditorFrame();
        destroyHistoryEditorFrame();
        destroyMapMakerFrame();
        destroyNametablesFrame();
        destroyNetplayClientFrame();
        destroyNetplayServerFrame();
        destroyOamDataFrame();
        destroyPatternTablesFrame();
        destroyProgramFrame();
        destroyProgramServerFrame();
        destroyRamSearchFrame();
        destroyRamWatchFrame();
        destroyRobFrame();
        destroySpriteSaverFrame();
        destroySubMonitorFrame();
        destroyVolumeMixerFrame();
        destroyWatchHistoryFrame();
    }

    public static SystemAudioProcessor getSystemAudioProcessor() {
        return systemAudioProcessor;
    }

    private App() {
    }
}
