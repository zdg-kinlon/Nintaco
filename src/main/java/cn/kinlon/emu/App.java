package cn.kinlon.emu;

import cn.kinlon.emu.apu.APU;
import cn.kinlon.emu.apu.SystemAudioProcessor;
import cn.kinlon.emu.cartdb.Cart;
import cn.kinlon.emu.cartdb.CartDB;
import cn.kinlon.emu.files.*;
import cn.kinlon.emu.gui.image.*;
import cn.kinlon.emu.gui.FourButtonDialog;
import cn.kinlon.emu.gui.fds.DiskActivityIndicator;
import cn.kinlon.emu.gui.hexeditor.HexEditorFrame;
import cn.kinlon.emu.gui.sound.volumemixer.VolumeMixerFrame;
import cn.kinlon.emu.gui.userinterface.UserInterfacePrefs;
import cn.kinlon.emu.input.InputUtil;
import cn.kinlon.emu.input.other.Reset;
import cn.kinlon.emu.input.other.SetTVSystem;
import cn.kinlon.emu.mappers.Mapper;
import cn.kinlon.emu.mappers.nintendo.fds.FdsMapper;
import cn.kinlon.emu.mappers.nintendo.vs.VsGame;
import cn.kinlon.emu.mappers.nsf.NsfMapper;
import cn.kinlon.emu.palettes.PalettePPU;
import cn.kinlon.emu.palettes.PaletteUtil;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.preferences.GamePrefs;
import cn.kinlon.emu.utils.GuiUtil;
import cn.kinlon.emu.utils.InstanceUtil;
import cn.kinlon.emu.utils.ThreadUtil;
import cn.kinlon.emu.utils.TimeUtil;

import java.awt.*;
import java.io.*;
import java.security.MessageDigest;

import static java.awt.event.KeyEvent.*;
import static cn.kinlon.emu.files.FileType.*;
import static cn.kinlon.emu.files.FileUtil.*;
import static cn.kinlon.emu.tv.TVSystem.NTSC;
import static cn.kinlon.emu.utils.BitUtil.setBit;
import static cn.kinlon.emu.utils.BitUtil.toggleBit;
import static cn.kinlon.emu.utils.GuiUtil.*;
import static cn.kinlon.emu.utils.StreamUtil.readByteArray;
import static cn.kinlon.emu.utils.StringUtil.isBlank;

public final class App {

    private static volatile AppMode appMode = AppMode.Default;

    private static SystemAudioProcessor systemAudioProcessor;

    private static MachineRunner machineRunner;
    private static Machine machine;

    private static HexEditorFrame hexEditorFrame;
    private static ImageFrame imageFrame;
    private static VolumeMixerFrame volumeMixerFrame;
    private static SubMonitorFrame subMonitorFrame;

    private static NesFile nesFile;
    private static FdsFile fdsFile;
    private static UnifFile unifFile;
    private static NsfFile nsfFile;
    private static String entryFileName;
    
    private static int[] fdsBIOS;

    private static volatile int rewindTimeValue;
    private static volatile int highSpeedValue;

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
        AppPrefs.load();
        if (!AppPrefs.getInstance().getUserInterfacePrefs()
                .isAllowMultipleInstances() && InstanceUtil.isAlreadyRunning()) {
            return;
        }
        systemAudioProcessor = new SystemAudioProcessor();
        CartDB.init();
        PaletteUtil.init();
        EventQueue.invokeAndWait(() -> {
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
                .addPropertyChangeListener("activeWindow", e -> EventQueue.invokeLater(
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
                    .isPlayInBackground()))) {
                pausedOnFocusLoss = pause;
                setNoStepPause(pause);
                if (applicationHasFocus) {
                    InputUtil.clearEventQueues();
                }
            }
        }

        final boolean playFrameHasFocus = activeWindow == imageFrame
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

    private static Machine getRunningMachine() {
        final Machine m = machine;
        final MachineRunner r = machineRunner;
        return (m != null && r != null && r.isRunning()) ? m : null;
    }

    public static void cpuKilled(final int opcode, final int address) {
        if (EventQueue.isDispatchThread()) {
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
        } else {
            EventQueue.invokeLater(() -> cpuKilled(opcode, address));
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

    public static boolean isFdsBiosLoaded() {
        return fdsBIOS != null;
    }

    public static ImageFrame getImageFrame() {
        return imageFrame;
    }

    public static VolumeMixerFrame getVolumeMixerFrame() {
        return volumeMixerFrame;
    }

    public static SubMonitorFrame getSubMonitorFrame() {
        return subMonitorFrame;
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
        InputUtil.setVsGame(null);
        mapper.loadNonVolatilePrgRam();

        final PPU ppu = machine.getPPU();
        SetTVSystem.run(machine, mapper.getPreferredTVSystem());

        machine.getAPU().setAudioProcessor(systemAudioProcessor);
        ppu.setScreenRenderer(imageFrame.getImagePane());
        if (ejectedMachine == null) {
            machine.getCPU().reset();
        }
        mapper.init();

        machineRunner = new MachineRunner(machine);
        App.setTrackHistory(AppPrefs.getInstance().getHistoryPrefs()
                .isTrackHistory());

        if (isVsDualSystem()) {
            createSubMonitorFrame();
        }

        updateFrames(machineRunner);
        InputUtil.pollControllers(machine);
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

    public static void handleFrameRendered(final MachineRunner machineRunner) {
        final HexEditorFrame hexEditor = hexEditorFrame;
        if (hexEditor != null) {
            hexEditor.update();
        }
    }

    public static void fireStepPausedChanged(final boolean stepPause) {
        EventQueue.invokeLater(() -> {
            final ImageFrame image = imageFrame;
            if (image != null) {
                image.onStepPausedChanged(stepPause);
            }
        });
    }

    public static void createHexEditorFrame() {
        if (EventQueue.isDispatchThread()) {
            if (hexEditorFrame == null) {
                hexEditorFrame = new HexEditorFrame();
                hexEditorFrame.setMachine(getRunningMachine());
                hexEditorFrame.setVisible(true);
            } else {
                GuiUtil.toFront(hexEditorFrame);
            }
        } else {
            EventQueue.invokeLater(App::createHexEditorFrame);
        }
    }

    public static void destroyHexEditorFrame() {
        if (EventQueue.isDispatchThread()) {
            if (hexEditorFrame != null) {
                hexEditorFrame.destroy();
                hexEditorFrame = null;
            }
        } else {
            EventQueue.invokeLater(App::destroyHexEditorFrame);
        }
    }

    public static void destroyVolumeMixerFrame() {
        if (EventQueue.isDispatchThread()) {
            if (volumeMixerFrame != null) {
                volumeMixerFrame.destroy();
                volumeMixerFrame = null;
            }
        } else {
            EventQueue.invokeLater(App::destroyVolumeMixerFrame);
        }
    }

    public static void createSubMonitorFrame() {
        if (EventQueue.isDispatchThread()) {
            if (subMonitorFrame == null) {
                subMonitorFrame = new SubMonitorFrame();
                forwardKeyEvents(subMonitorFrame, imageFrame);
                subMonitorFrame.setVisible(true);
                EventQueue.invokeLater(() -> GuiUtil.toFront(imageFrame));
            } else {
                GuiUtil.toFront(subMonitorFrame);
            }
            subMonitorFrame.init();
        } else {
            EventQueue.invokeLater(App::createSubMonitorFrame);
        }
    }

    public static void destroySubMonitorFrame() {
        if (EventQueue.isDispatchThread()) {
            if (subMonitorFrame != null) {
                subMonitorFrame.destroy();
                subMonitorFrame = null;
            }
        } else {
            EventQueue.invokeLater(App::destroySubMonitorFrame);
        }
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
                r.getMapper().updateButtons(0);
                SystemAudioProcessor.setMovie(null);
                imageFrame.setHistoryTracking(false);
            
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
        highSpeedValue &= 0x0F;
        setHighSpeed(highSpeedValue != 0);
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
        //APU.setNormalSpeed(percent == 100 && !TimeUtil.isHighSpeed());
    }

    public static int getSpeed() {
        return TimeUtil.getSpeed();
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
            destroySubMonitorFrame();
        }
        imageFrame.updateContentPane(null, null);
        imageFrame.setHistoryTracking(false);
        invokeAndWait(() -> {
        });
        dispose();
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
        destroyHexEditorFrame();
        destroySubMonitorFrame();
        destroyVolumeMixerFrame();
    }

    private App() {
    }
}
