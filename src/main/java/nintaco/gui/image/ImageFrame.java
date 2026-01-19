package nintaco.gui.image;

import nintaco.*;
import nintaco.apu.SystemAudioProcessor;
import nintaco.files.*;
import nintaco.files.vs.goodnes.GoodNesFileUtil;
import nintaco.files.vs.mame.MameFileUtil;
import nintaco.gui.*;
import nintaco.gui.about.AboutDialog;
import nintaco.gui.archive.ArchiveFileChooser;
import nintaco.gui.archive.ArchiveOptionsDialog;
import nintaco.gui.cheats.CheatsDialog;
import nintaco.gui.contentdirectory.ContentDirectoryDialog;
import nintaco.gui.debugger.logger.LoggerDialog;
import nintaco.gui.familybasic.BackgroundEditorFrame;
import nintaco.gui.familybasic.FamilyBasicOptionsDialog;
import nintaco.gui.fds.FamicomDiskSystemOptionsDialog;
import nintaco.gui.image.filters.VideoFilterDescriptor;
import nintaco.gui.image.preferences.Paths;
import nintaco.gui.image.preferences.View;
import nintaco.gui.input.buttonmapping.ButtonMappingDialog;
import nintaco.gui.input.ports.PortsDialog;
import nintaco.gui.input.settings.InputSettingsDialog;
import nintaco.gui.nsf.NsfOptionsDialog;
import nintaco.gui.nsf.NsfPanel;
import nintaco.gui.overscan.OverscanDialog;
import nintaco.gui.palettes.PaletteOptionsDialog;
import nintaco.gui.rob.RobController;
import nintaco.gui.screenshots.ScreenshotOptionsDialog;
import nintaco.gui.sound.SoundOptionsDialog;
import nintaco.gui.userinterface.UserInterfaceDialog;
import nintaco.gui.userinterface.UserInterfacePrefs;
import nintaco.gui.watchhistory.WatchHistoryFrame;
import nintaco.gui.watchhistory.WatchHistoryPanel;
import nintaco.input.DeviceMapper;
import nintaco.input.InputDevices;
import nintaco.input.InputUtil;
import nintaco.input.familybasic.FamilyBasicUtil;
import nintaco.input.familybasic.datarecorder.DataRecorderMapper;
import nintaco.input.familybasic.datarecorder.DataRecorderMode;
import nintaco.input.other.*;
import nintaco.mappers.Mapper;
import nintaco.mappers.nintendo.vs.CoinSlot;
import nintaco.mappers.nintendo.vs.DualAPU;
import nintaco.mappers.nintendo.vs.VsSystem;
import nintaco.movie.Movie;
import nintaco.netplay.client.NetplayClient;
import nintaco.palettes.PalettePPU;
import nintaco.palettes.PaletteUtil;
import nintaco.palettes.Palettes;
import nintaco.preferences.AppPrefs;
import nintaco.tv.TVSystem;
import nintaco.util.BrowserUtil;
import nintaco.util.CollectionsUtil;
import nintaco.util.EDT;
import nintaco.util.GuiUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.*;

import static java.awt.event.KeyEvent.*;
import static javax.swing.UIManager.*;
import static nintaco.files.ArchiveEntry.toNames;
import static nintaco.files.FileUtil.*;
import static nintaco.files.vs.mame.MameFileUtil.identifyArchive;
import static nintaco.netplay.protocol.MessageType.QuickLoad;
import static nintaco.netplay.protocol.MessageType.QuickSave;
import static nintaco.tv.TVSystem.*;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.isBlank;

public class ImageFrame extends javax.swing.JFrame implements StyleListener {

    public static final long HIDE_MENU_BAR_DELAY = 20_000L;

    public static final FileExtensionFilter[] FileExtensionFilters = {
            null,
            new FileExtensionFilter(1, "iNES and NES 2.0 files (*.nes)", "nes", "nez"),
            new FileExtensionFilter(2, "Famicom Disk System files (*.fds)", "fds"),
            new FileExtensionFilter(3, "UNIF files (*.unf, *.unif)", "unf", "unif"),
            new FileExtensionFilter(4, "NES Sound Files (*.nsf, *.nsfe)", "nsf",
                    "nsfe"),
            new FileExtensionFilter(5, "Archives (*.zip, *.rar, *.7z, *.tar*)",
                    "zip", "rar", "7z", "tar.bz2", "tb2", "tbz", "tbz2", "tar.gz", "tgz",
                    "tar.lzma", "tlz", "tar.xz", "txz", "tar.Z", "tZ"),
            new FileExtensionFilter(6, "All files (*.*)"),
    };
    public static final FileExtensionFilter[] BasicFileExtensionFilters = {
            new FileExtensionFilter(0, "BASIC files (*.bas)", "bas"),
            new FileExtensionFilter(1, "All files (*.*)"),
    };
    public static final FileExtensionFilter[] TapeFileExtensionFilters = {
            new FileExtensionFilter(0, "Tape files (*.tape)", "tape"),
            new FileExtensionFilter(1, "All files (*.*)"),
    };
    public static final String[] SpeedLabels = {"Max", "400%", "300%", "200%",
            "Normal", "75%", "50%", "25%", "10%", "5%", "1%"};
    public static final int[] SpeedValues = {0, 400, 300, 200, 100, 75, 50, 25,
            10, 5, 1};
    private static final KeyEventDispatcher DisableKeyEventsDispatcher
            = e -> true;

    static {
        final List<String> extensions = new ArrayList<>();
        extensions.add("All supported files");
        for (final FileExtensionFilter filter : FileExtensionFilters) {
            if (filter != null) {
                Collections.addAll(extensions, filter.getExtensions());
            }
        }
        FileExtensionFilters[0] = new FileExtensionFilter(0,
                extensions.toArray(new String[extensions.size()]));
    }

    private final JRadioButtonMenuItem[] speedMenuItems
            = new JRadioButtonMenuItem[SpeedValues.length];    private final KeyListener NoMenuBarKeyListener = new KeyAdapter() {
        @Override
        public void keyPressed(final KeyEvent e) {
            e.consume();
            final int code = e.getKeyCode();
            if (e.isShiftDown()) {
                if (!(e.isAltDown() || e.isControlDown())) {
                    if (code >= VK_0 && code <= VK_9) {
                        quickSaveState(code - VK_0);
                    } else {
                        switch (code) {
                            case VK_B:
                                flipDiskSideMenuItemActionPerformed(null);
                                break;
                            case VK_F:
                                nextFrameMenuItemActionPerformed(null);
                                break;
                            case VK_P:
                                if (isPauseable()) {
                                    pauseMenuItem.setSelected(!pauseMenuItem.isSelected());
                                    pause();
                                }
                                break;
                            case VK_F2:
                                insertCoinSubLeftMenuItemActionPerformed(null);
                                break;
                            case VK_F3:
                                insertCoinSubRightMenuItemActionPerformed(null);
                                break;
                        }
                    }
                }
            } else if (e.isControlDown()) {
                if (!e.isAltDown()) {
                    if (code == VK_R) {
                        resetMenuItemActionPerformed(null);
                    }
                }
            } else if (e.isAltDown()) {
                if (code >= VK_1 && code <= VK_5) {
                    setScreenScale(code - VK_0);
                } else {
                    switch (e.getKeyCode()) {
                        case VK_ENTER:
                            toggleFullscreenMode();
                            break;
                        case VK_S:
                            maximize(ImageFrame.this);
                            break;
                    }
                }
            } else {
                if (code >= VK_0 && code <= VK_9) {
                    quickLoadState(code - VK_0);
                } else {
                    switch (code) {
                        case VK_F2:
                            insertCoinMainLeftMenuItemActionPerformed(null);
                            break;
                        case VK_F3:
                            insertCoinMainRightMenuItemActionPerformed(null);
                            break;
                        case VK_F12:
                            saveScreenshotMenuItemActionPerformed(null);
                            break;
                        case VK_ESCAPE:
                            setFullscreenMode(false);
                            setMenuBarVisible(true);
                            break;
                    }
                }
            }
        }

        @Override
        public void keyReleased(final KeyEvent e) {
            e.consume();
        }

        @Override
        public void keyTyped(final KeyEvent e) {
            e.consume();
        }
    };
    private final ImagePane imagePane = new ImagePane();
    private final NsfPanel nsfPanel = new NsfPanel();
    private final List<QuickSaveStateInfo> quickSaveStateInfos
            = new ArrayList<>();
    private final List<QuickSaveListener> quickSaveListeners = new ArrayList<>();
    private boolean displayingImagePane;
    private Machine machine;
    private boolean historyTracking;
    private boolean timeRewinding;
    private boolean keyEventsEnabled;
    private boolean smoothScaling;
    private boolean useTvAspectRatio;
    private boolean uniformPixelScaling;
    private String saveFileName;
    private File lastSaveFile;
    private volatile QuickSaveStateInfo newestQuickSaveStateInfo;
    private volatile QuickSaveStateInfo oldestQuickSaveStateInfo;
    private volatile String fileInfo;
    private volatile File basicProgramFile;
    private volatile File tapeFile;
    private ButtonGroup diskSideButtonGroup = new ButtonGroup();
    private JCheckBoxMenuItem[] diskSideMenuItems = new JCheckBoxMenuItem[0];
    private boolean wasMaximized;
    private long exitFullscreenModeTime;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem applyIPSMenuItem;
    private javax.swing.JMenuItem archiveFileOptionsMenuItem;
    private javax.swing.JMenuItem asmDasmMenuItem;
    private javax.swing.JCheckBoxMenuItem backgroundCheckBoxMenuItem;
    private javax.swing.JMenuItem barcodeBattlerMenuItem;
    private javax.swing.JMenuItem buttonMappingMenuItem;
    private javax.swing.JMenuItem clearHistoryMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem connectToNetplayServerMenuItem;
    private javax.swing.JMenuItem contentDirectoryMenuItem;
    private javax.swing.JMenuItem createIPSMenuItem;
    private javax.swing.JMenu debugMenu;
    private javax.swing.JMenuItem debuggerMenuItem;
    private javax.swing.JRadioButtonMenuItem dendyRegionRadioButtonMenuItem;
    private javax.swing.JMenuItem dipSwitchesMenuItem;
    private javax.swing.JMenuItem editFileHeaderMenuItem;
    private javax.swing.JMenuItem editHistoryMenuItem;
    private javax.swing.JMenuItem ejectDiskMenuItem;
    private javax.swing.JMenuItem eraseBatterySaveMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem exportHistoryMenuItem;
    private javax.swing.JMenuItem exportVideoAudioMenuItem;
    private javax.swing.JMenuItem famicom3dGlassesMenuItem;
    private javax.swing.JMenuItem famicomDiskSystemOptionsMenuItem;
    private javax.swing.JMenuItem familyBasicCopyProgramMenuItem;
    private javax.swing.JMenuItem familyBasicEditBackgroundMenuItem;
    private javax.swing.JMenuItem familyBasicEraseTapeMenuItem;
    private javax.swing.JMenuItem familyBasicLoadBackgroundMenuItem;
    private javax.swing.JMenuItem familyBasicLoadProgramMenuItem;
    private javax.swing.JMenuItem familyBasicLoadTapeMenuItem;
    private javax.swing.JMenu familyBasicMenu;
    private javax.swing.JMenuItem familyBasicOptionsMenuItem;
    private javax.swing.JMenuItem familyBasicPasteProgramMenuItem;
    private javax.swing.JRadioButtonMenuItem familyBasicPlayTapeMenuItem;
    private javax.swing.JRadioButtonMenuItem familyBasicRecordTapeMenuItem;
    private javax.swing.JMenuItem familyBasicSaveBackgroundMenuItem;
    private javax.swing.JMenuItem familyBasicSaveProgramMenuItem;
    private javax.swing.JMenuItem familyBasicSaveTapeMenuItem;
    private javax.swing.JRadioButtonMenuItem familyBasicStopTapeMenuItem;
    private javax.swing.ButtonGroup familyBasicTapeButtonGroup;
    private javax.swing.JMenuItem familyBasicTypePasteMenuItem;
    private javax.swing.JMenuItem fileInfoMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem flipDiskSideMenuItem;
    private javax.swing.JMenuItem fontSizeMenuItem;
    private javax.swing.JCheckBoxMenuItem fpsCheckBoxMenuItem;
    private javax.swing.JMenuItem fullscreenMenuItem;
    private javax.swing.JMenuItem glitchMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JMenuItem hexEditorMenuItem;
    private javax.swing.JMenuItem hideMenuBarMenuItem;
    private javax.swing.JMenuItem hotSwapMenuItem;
    private javax.swing.JMenuItem importHistoryMenuItem;
    private javax.swing.JCheckBoxMenuItem inputDevicesCheckBoxMenuItem;
    private javax.swing.JMenuItem inputSettingsMenuItem;
    private javax.swing.JMenuItem insertCoinMainLeftMenuItem;
    private javax.swing.JMenuItem insertCoinMainRightMenuItem;
    private javax.swing.JMenu insertCoinMenu;
    private javax.swing.JMenuItem insertCoinSubLeftMenuItem;
    private javax.swing.JMenuItem insertCoinSubRightMenuItem;
    private javax.swing.JMenu insertDiskMenu;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JPopupMenu.Separator jSeparator17;
    private javax.swing.JPopupMenu.Separator jSeparator18;
    private javax.swing.JPopupMenu.Separator jSeparator19;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator20;
    private javax.swing.JPopupMenu.Separator jSeparator21;
    private javax.swing.JPopupMenu.Separator jSeparator22;
    private javax.swing.JPopupMenu.Separator jSeparator23;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JMenuItem licenseMenuItem;
    private javax.swing.JMenuItem loadStateMenuItem;
    private javax.swing.ButtonGroup lookAndFeelButtonGroup;
    private javax.swing.JMenu lookAndFeelMenu;
    private javax.swing.JMenu machineMenu;
    private javax.swing.JMenuItem manageCheatsMenuItem;
    private javax.swing.JMenuItem mapMakerMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem nametablesMenuItem;
    private javax.swing.JMenuItem newestSlotMenuItem;
    private javax.swing.JMenuItem nextFrameMenuItem;
    private javax.swing.JCheckBoxMenuItem noSpriteLimitCheckBoxMenuItem;
    private javax.swing.JMenuItem nsfOptionsMenuItem;
    private javax.swing.JRadioButtonMenuItem ntscRegionRadioButtonMenuItem;
    private javax.swing.JMenuItem oamDataMenuItem;
    private javax.swing.JMenuItem oldestSlotMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenu openRecentArchiveMenu;
    private javax.swing.JMenu openRecentDirectoryMenu;
    private javax.swing.JMenu openRecentFileMenu;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JMenuItem overscanMenuItem;
    private javax.swing.JRadioButtonMenuItem palRegionRadioButtonMenuItem;
    private javax.swing.JMenu paletteMenu;
    private javax.swing.JMenuItem palettesMenuItem;
    private javax.swing.JMenuItem patternTablesMenuItem;
    private javax.swing.JCheckBoxMenuItem pauseMenuItem;
    private javax.swing.JMenuItem portsMenuItem;
    private javax.swing.JMenuItem powerCycleMenuItem;
    private javax.swing.JPopupMenu.Separator quickLoadSeparator;
    private javax.swing.JMenu quickLoadStateMenu;
    private javax.swing.JPopupMenu.Separator quickSaveSeparator;
    private javax.swing.JMenu quickSaveStateMenu;
    private javax.swing.JMenuItem ramSearchMenuItem;
    private javax.swing.JMenuItem ramWatchMenuItem;
    private javax.swing.JMenuItem recentArchivesClearMenuItem;
    private javax.swing.JCheckBoxMenuItem recentArchivesLockCheckBoxMenuItem;
    private javax.swing.JPopupMenu.Separator recentArchivesSeparator;
    private javax.swing.JMenuItem recentDirectoriesClearMenuItem;
    private javax.swing.JCheckBoxMenuItem recentDirectoriesLockCheckBoxMenuItem;
    private javax.swing.JPopupMenu.Separator recentDirectoriesSeparator;
    private javax.swing.JMenuItem recentFilesClearMenuItem;
    private javax.swing.JCheckBoxMenuItem recentFilesLockCheckBoxMenuItem;
    private javax.swing.JPopupMenu.Separator recentFilesSeparator;
    private javax.swing.ButtonGroup regionButtonGroup;
    private javax.swing.JMenuItem resetMenuItem;
    private javax.swing.JCheckBoxMenuItem rewindTimeCheckBoxMenuItem;
    private javax.swing.JMenuItem robMenuItem;
    private javax.swing.JMenuItem runProgramMenuItem;
    private javax.swing.JMenuItem saveScreenshotMenuItem;
    private javax.swing.JMenuItem saveStateMenuItem;
    private javax.swing.JMenuItem screamIntoMicrophoneMenuItem;
    private javax.swing.JMenuItem screenSize1XMenuItem;
    private javax.swing.JMenuItem screenSize2XMenuItem;
    private javax.swing.JMenuItem screenSize3XMenuItem;
    private javax.swing.JMenuItem screenSize4XMenuItem;
    private javax.swing.JMenuItem screenSize5XMenuItem;
    private javax.swing.ButtonGroup screenSizeButtonGroup;
    private javax.swing.JMenuItem screenSizeMaxMenuItem;
    private javax.swing.JMenu screenSizeMenu;
    private javax.swing.JMenuItem screenshotOptionsMenuItem;
    private javax.swing.JMenuItem searchCheatsMenuItem;
    private javax.swing.JPopupMenu.Separator separator1;
    private javax.swing.JPopupMenu.Separator separator15;
    private javax.swing.JPopupMenu.Separator separator16;
    private javax.swing.JPopupMenu.Separator separator2;
    private javax.swing.JPopupMenu.Separator separator3;
    private javax.swing.JPopupMenu.Separator separator5;
    private javax.swing.JPopupMenu.Separator separator7;
    private javax.swing.JPopupMenu.Separator separator8;
    private javax.swing.JMenuItem serviceButtonMainMenuItem;
    private javax.swing.JMenu serviceButtonMenu;
    private javax.swing.JMenuItem serviceButtonSubMenuItem;
    private javax.swing.JMenu showMenu;
    private javax.swing.JCheckBoxMenuItem smoothScalingCheckBoxMenuItem;
    private javax.swing.JMenuItem soundMenuItem;
    private javax.swing.JMenu speedMenu;
    private javax.swing.JCheckBoxMenuItem spriteBoxesCheckBoxMenuItem;
    private javax.swing.JMenuItem spriteSaverMenuItem;
    private javax.swing.JCheckBoxMenuItem spritesCheckBoxMenuItem;
    private javax.swing.JMenuItem startNetplayServerMenuItem;
    private javax.swing.JMenuItem startProgramServerMenuItem;
    private javax.swing.JMenuItem startTraceLoggerMenuItem;
    private javax.swing.JCheckBoxMenuItem statusMessagesCheckBoxMenuItem;
    private javax.swing.JMenuItem subMonitorMenuItem;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenuItem traceLoggerOptionsMenuItem;
    private javax.swing.JCheckBoxMenuItem trackHistoryCheckBoxMenuItem;
    private javax.swing.JCheckBoxMenuItem tvAspectCheckBoxMenuItem;
    private javax.swing.JMenu tvSystemMenu;
    private javax.swing.JCheckBoxMenuItem underscanCheckBoxMenuItem;
    private javax.swing.JCheckBoxMenuItem uniformPixelScalingCheckBoxMenuItem;
    private javax.swing.JMenuItem userInterfaceMenuItem;
    private javax.swing.JMenu videoFilterMenu;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem volumeMixerMenuItem;
    private javax.swing.JMenuItem watchHistoryMenuItem;

    public ImageFrame() {
        initComponents();
        initMenuBar();
        createSpeedMenu();
        updateContentPane(null, null);
        initFileDragAndDrop();
    }

    public void init() {
        AppPrefs.getInstance().getUserInterfacePrefs().apply();
        setLocationRelativeTo(null);
        setVisible(true);
        requestFocus();
    }

    private void initFileDragAndDrop() {
        new FileDrop(imagePane, this::filesDropped);
        new FileDrop(nsfPanel, this::filesDropped);
    }

    private void initMenuBar() {
        createPaletteMenu();
        createLookAndFeelMenu();
        createRecentFilesMenu();
        trackHistoryCheckBoxMenuItem.setSelected(AppPrefs.getInstance()
                .getHistoryPrefs().isTrackHistory());
        PauseMenuListener.addPauseMenuListener(menuBar);
        initTimeRewinding();
    }

    private void initTimeRewinding() {
        setTimeRewinding(timeRewinding);
        updateMenus();
    }

    private void initShowMenu() {
        final View view = AppPrefs.getInstance().getView();
        fpsCheckBoxMenuItem.setSelected(view.isShowFPS());
        inputDevicesCheckBoxMenuItem.setSelected(view.isShowInputDevices());
        backgroundCheckBoxMenuItem.setSelected(view.isBackgroundEnabled());
        spritesCheckBoxMenuItem.setSelected(view.isSpritesEnabled());
        spriteBoxesCheckBoxMenuItem.setSelected(view.isSpriteBoxesEnabled());
    }

    private void initScreenSizeMenu() {
        final View view = AppPrefs.getInstance().getView();
        useTvAspectRatio = view.isTvAspect();
        tvAspectCheckBoxMenuItem.setSelected(useTvAspectRatio);
        smoothScaling = view.isSmoothScaling();
        smoothScalingCheckBoxMenuItem.setSelected(smoothScaling);
        uniformPixelScaling = view.isUniformPixelScaling();
        uniformPixelScalingCheckBoxMenuItem.setSelected(uniformPixelScaling);
        underscanCheckBoxMenuItem.setSelected(view.isUnderscan());

        EDT.async(() -> { // TODO REVIEW THIS
            setUseTvAspectRatio(useTvAspectRatio);
            imagePane.setSmoothScaling(smoothScaling);
            imagePane.setUniformPixelScaling(uniformPixelScaling);
        });
    }

    private void filesDropped(final File[] files) {
        if (!CollectionsUtil.isBlank(files)) {
            fileOpened(files[0], null, false, null, null);
        }
    }

    private void showWatchHistoryFrame(final boolean showSave) {
        App.createWatchHistoryFrame();
        final WatchHistoryFrame watchHistoryFrame = App.getWatchHistoryFrame();
        if (watchHistoryFrame != null) {
            final WatchHistoryPanel watchHistoryPanel = watchHistoryFrame
                    .getWatchHistoryPanel();
            watchHistoryPanel.init();
            watchHistoryPanel.setMovie(null);
            watchHistoryPanel.setShowSave(showSave);
            watchHistoryFrame.pack();
        }
    }

    public void movieLoaded(final Movie movie) {
        App.createWatchHistoryFrame();
        final WatchHistoryFrame watchHistoryFrame = App.getWatchHistoryFrame();
        if (watchHistoryFrame != null) {
            final WatchHistoryPanel watchHistoryPanel = watchHistoryFrame
                    .getWatchHistoryPanel();
            watchHistoryPanel.init();
            watchHistoryPanel.setMovie(movie);
            watchHistoryPanel.setShowSave(false);
            watchHistoryFrame.pack();
        }
    }

    public void setHistoryTracking(final boolean historyTracking) {
        EDT.async(() -> {
            this.historyTracking = historyTracking;
            trackHistoryCheckBoxMenuItem.setSelected(historyTracking);
            setTimeRewinding(timeRewinding && historyTracking);
        });
    }

    public void setTimeRewinding(final boolean timeRewinding) {
        EDT.async(() -> this.timeRewinding = timeRewinding);
    }

    private void fireQuickSaveListeners() {
        for (final QuickSaveListener quickSaveListener : quickSaveListeners) {
            quickSaveListener.onQuickSaveChanged(quickSaveStateInfos);
        }
    }

    public void handleQuickSaveStateMenuNames(
            final String[] quickSaveStateMenuNames, final boolean enabled) {

        final boolean menusEnabled = quickSaveStateMenuNames != null;
        final NetplayClient client = App.getNetplayClient();

        quickLoadStateMenu.removeAll();
        quickSaveStateMenu.removeAll();
        quickLoadStateMenu.setEnabled(menusEnabled);
        quickSaveStateMenu.setEnabled(menusEnabled);
        quickSaveStateInfos.clear();
        if (!menusEnabled) {
            return;
        }

        for (int i = 0; i < quickSaveStateMenuNames.length; i++) {
            final int index = i + 1;
            final String name = quickSaveStateMenuNames[i];
            final char key = Character.forDigit(i + 1, 10);

            final JMenuItem loadMenuItem = new JMenuItem(name, key);
            scaleMenuItemFont(loadMenuItem);
            loadMenuItem.setAccelerator(KeyStroke.getKeyStroke(key));
            loadMenuItem.setEnabled(enabled && !name.contains("..."));
            loadMenuItem.addActionListener(e -> client.post(QuickLoad, index));
            quickLoadStateMenu.add(loadMenuItem);

            final JMenuItem saveMenuItem = new JMenuItem(name, key);
            saveMenuItem.setEnabled(enabled);
            scaleMenuItemFont(saveMenuItem);
            saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.getExtendedKeyCodeForChar(key), InputEvent.SHIFT_MASK));
            saveMenuItem.addActionListener(e -> client.post(QuickSave, index));
            quickSaveStateMenu.add(saveMenuItem);
        }

        removeAllActionListeners(newestSlotMenuItem);
        newestSlotMenuItem.addActionListener(e -> client.post(QuickLoad, 0));
        newestSlotMenuItem.setEnabled(enabled);

        removeAllActionListeners(oldestSlotMenuItem);
        oldestSlotMenuItem.addActionListener(e -> client.post(QuickSave, 0));
        oldestSlotMenuItem.setEnabled(enabled);

        quickLoadStateMenu.add(quickLoadSeparator);
        quickLoadStateMenu.add(newestSlotMenuItem);
        quickSaveStateMenu.add(quickSaveSeparator);
        quickSaveStateMenu.add(oldestSlotMenuItem);
    }

    private void createVideoFiltersMenu() {
        videoFilterMenu.removeAll();
        final VideoFilterDescriptor videoFilter = AppPrefs.getInstance().getView()
                .getVideoFilter();
        final ButtonGroup buttonGroup = new ButtonGroup();
        for (final VideoFilterDescriptor descriptor
                : VideoFilterDescriptor.values()) {
            if (descriptor != VideoFilterDescriptor.Current) {
                final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(
                        descriptor.getName());
                videoFilterMenu.add(menuItem);
                buttonGroup.add(menuItem);
                menuItem.addActionListener(e -> {
                    AppPrefs.getInstance().getView().setVideoFilter(descriptor);
                    AppPrefs.save();
                    imagePane.setVideoFilterDescriptor(descriptor);
                    final boolean tvAspect = descriptor.isUseTvAspectRatio()
                            || useTvAspectRatio;
                    tvAspectCheckBoxMenuItem.setSelected(tvAspect);
                    setUseTvAspectRatio(tvAspect);
                    final boolean scaling = descriptor.isSmoothScaling() || smoothScaling;
                    smoothScalingCheckBoxMenuItem.setSelected(scaling);
                    imagePane.setSmoothScaling(scaling);
                    setScreenScale(descriptor.getScale());
                });
                if (descriptor == videoFilter) {
                    menuItem.setSelected(true);
                    menuItem.doClick();
                }
            }
        }
    }

    public void createPaletteMenu() {
        EDT.async(()->{
            paletteMenu.removeAll();
            final ButtonGroup buttonGroup = new ButtonGroup();
            final Palettes prefs = AppPrefs.getInstance().getPalettes();
            final List<String> names = new ArrayList<>();
            final Map<PalettePPU, String> ppuPaletteMapping = new HashMap<>();
            prefs.getPaletteNames(names);
            prefs.getPpuPaletteMapping(ppuPaletteMapping);
            final PalettePPU palettePPU = PaletteUtil.getPalettePPU();
            final String selectedName = ppuPaletteMapping.get(palettePPU);
            for (final String name : names) {
                final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(name);
                buttonGroup.add(menuItem);
                menuItem.addActionListener(e -> paletteSelected(palettePPU, name));
                if (name.equals(selectedName)) {
                    menuItem.setSelected(true);
                }
                paletteMenu.add(menuItem);
            }
            paletteMenu.add(new JSeparator());
            final JMenuItem defaultMenuItem = new JMenuItem("Default");
            defaultMenuItem.addActionListener(arg -> {
                final String defaultName = PaletteUtil.getDefaultName();
                for (final Enumeration<AbstractButton> e = buttonGroup.getElements();
                     e.hasMoreElements(); ) {
                    final AbstractButton button = e.nextElement();
                    if (defaultName.equals(button.getText())) {
                        button.setSelected(true);
                        button.doClick();
                        break;
                    }
                }
            });
            paletteMenu.add(defaultMenuItem);
        });
    }

    private void paletteSelected(final PalettePPU palettePPU, final String name) {
        final Palettes prefs = AppPrefs.getInstance().getPalettes();
        final Map<PalettePPU, String> ppuPaletteMapping = new HashMap<>();
        prefs.getPpuPaletteMapping(ppuPaletteMapping);
        ppuPaletteMapping.put(palettePPU, name);
        prefs.setPpuPaletteMapping(ppuPaletteMapping);
        AppPrefs.save();
        PaletteUtil.update();
    }

    private void createSpeedMenu() {
        speedMenu.removeAll();
        final ButtonGroup buttonGroup = new ButtonGroup();
        for (int i = 0; i < SpeedLabels.length; i++) {
            final String label = SpeedLabels[i];
            final int value = SpeedValues[i];
            final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(label);
            speedMenuItems[i] = menuItem;
            if (value == 100) {
                menuItem.setSelected(true);
            }
            buttonGroup.add(menuItem);
            final char c = label.charAt(0);
            if (Character.isAlphabetic(c)) {
                menuItem.setMnemonic(c);
            }
            menuItem.addActionListener(e -> App.setSpeed(value));
            speedMenu.add(menuItem);
        }
    }

    private void createQuickMenus() {

        quickLoadStateMenu.removeAll();
        quickSaveStateMenu.removeAll();
        quickSaveStateInfos.clear();

        for (int i = 1; i <= 9; i++) {
            final int slot = i;
            final File file = new File(String.format("%s-%d", saveFileName, i));
            final boolean fileExists = file.exists();
            final String name = fileExists ? getFileTimestamp(i, file)
                    : String.format("%d  ...                     ", i);
            final char key = Character.forDigit(i, 10);

            final JMenuItem loadMenuItem = new JMenuItem(name, key);
            scaleMenuItemFont(loadMenuItem);
            loadMenuItem.setAccelerator(KeyStroke.getKeyStroke(key));
            loadMenuItem.setEnabled(fileExists);
            loadMenuItem.addActionListener(e -> {
                if ((e.getModifiers() & Event.ALT_MASK) == 0) {
                    quickLoadState(slot, file);
                }
            });
            quickLoadStateMenu.add(loadMenuItem);

            final JMenuItem saveMenuItem = new JMenuItem(name, key);
            scaleMenuItemFont(saveMenuItem);
            saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.getExtendedKeyCodeForChar(key), InputEvent.SHIFT_MASK));
            final int index = i;
            saveMenuItem.addActionListener(e -> quickSaveState(index, file,
                    loadMenuItem, saveMenuItem));
            quickSaveStateMenu.add(saveMenuItem);

            quickSaveStateInfos.add(new QuickSaveStateInfo(i, file,
                    file.lastModified(), loadMenuItem, saveMenuItem));
        }

        quickLoadStateMenu.add(quickLoadSeparator);
        quickLoadStateMenu.add(newestSlotMenuItem);
        quickSaveStateMenu.add(quickSaveSeparator);
        quickSaveStateMenu.add(oldestSlotMenuItem);

        findNewestAndOldestSaveSlots();
    }

    private void findNewestAndOldestSaveSlots() {
        QuickSaveStateInfo newest = null;
        QuickSaveStateInfo oldest = null;
        for (final QuickSaveStateInfo info : quickSaveStateInfos) {
            if (newest == null) {
                newest = info;
                oldest = info;
            } else {
                if (info.getModifiedTime() < oldest.getModifiedTime()) {
                    oldest = info;
                }
                if (info.getModifiedTime() > newest.getModifiedTime()) {
                    newest = info;
                }
            }
        }

        removeAllActionListeners(newestSlotMenuItem);
        if (newest.getModifiedTime() > 0) {
            newestSlotMenuItem.setEnabled(true);
            final QuickSaveStateInfo info = newest;
            newestSlotMenuItem.addActionListener(e -> quickLoadState(
                    info.getSlot(), info.getFile()));
        } else {
            newest = null;
            newestSlotMenuItem.setEnabled(false);
        }

        removeAllActionListeners(oldestSlotMenuItem);
        final QuickSaveStateInfo info = oldest;
        oldestSlotMenuItem.addActionListener(e -> quickSaveState(info.getSlot(),
                info.getFile(), info.getLoadMenuItem(), info.getSaveMenuItem()));

        newestQuickSaveStateInfo = newest;
        oldestQuickSaveStateInfo = oldest;

        fireQuickSaveListeners();
    }

    public void createLookAndFeelMenu() {
        final View view = AppPrefs.getInstance().getView();
        final String lookAndFeelClassName = view.getLookAndFeelClassName() == null
                ? getSystemLookAndFeelClassName() : view.getLookAndFeelClassName();
        final String metalLookAndFeelClassName = MetalLookAndFeel.class
                .getCanonicalName();
        LookAndFeelInfo[] lookAndFeelInfos = getInstalledLookAndFeels();
        Arrays.sort(lookAndFeelInfos,
                (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        for (final LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos) {
            if (lookAndFeelInfo.getClassName().equals(metalLookAndFeelClassName)) {
                addMetalLookAndFeelMenuItems(view);
            } else {
                addLookAndFeelMenuItem(lookAndFeelInfo, lookAndFeelClassName);
            }
        }
    }

    private void addMetalLookAndFeelMenuItems(View view) {

        final String themeClassName = view.getThemeClassName();

        addMetalLookAndFeelMenuItem("Metal Ocean",
                () -> setMetalLookAndFeel(new OceanTheme()),
                OceanTheme.class.getCanonicalName().equals(themeClassName));
        addMetalLookAndFeelMenuItem("Metal Steel",
                () -> setMetalLookAndFeel(new DefaultMetalTheme()),
                DefaultMetalTheme.class.getCanonicalName().equals(themeClassName));
    }

    private void addMetalLookAndFeelMenuItem(String label, Runnable run,
                                             boolean isSelectedTheme) {
        final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(label);
        lookAndFeelButtonGroup.add(menuItem);
        if (isSelectedTheme) {
            menuItem.setSelected(true);
            EDT.async(run);
        }
        menuItem.addActionListener(e -> EDT.async(run));
        lookAndFeelMenu.add(menuItem);
    }

    private void addLookAndFeelMenuItem(final LookAndFeelInfo lookAndFeelInfo,
                                        final String lookAndFeelClassName) {
        final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(
                lookAndFeelInfo.getName());
        lookAndFeelButtonGroup.add(menuItem);
        if (lookAndFeelInfo.getClassName().equals(lookAndFeelClassName)) {
            menuItem.setSelected(true);
            EventQueue.invokeLater(() -> GuiUtil.setLookAndFeel(lookAndFeelClassName));
        }
        menuItem.addActionListener(_ -> EDT.async(() -> GuiUtil.setLookAndFeel(lookAndFeelInfo.getClassName())));
        lookAndFeelMenu.add(menuItem);
    }

    @Override
    public void styleChanged() {
        imagePane.updateUI();
        nsfPanel.updateUI();
        recentArchivesClearMenuItem.updateUI();
        recentArchivesLockCheckBoxMenuItem.updateUI();
        recentArchivesSeparator.updateUI();
        recentFilesClearMenuItem.updateUI();
        recentFilesLockCheckBoxMenuItem.updateUI();
        recentFilesSeparator.updateUI();
        recentDirectoriesClearMenuItem.updateUI();
        recentDirectoriesLockCheckBoxMenuItem.updateUI();
        recentDirectoriesSeparator.updateUI();
        imagePane.paneResized();
    }

    public void updateContentPane(final Mapper mapper, final NsfFile nsfFile) {
        EDT.async(() -> {
            final WatchHistoryFrame watchHistoryFrame = App.getWatchHistoryFrame();
            if (watchHistoryFrame != null) {
                watchHistoryFrame.getWatchHistoryPanel().resume(false);
            }
            nsfPanel.init(mapper, nsfFile);
            final boolean useImagePane = nsfFile == null;
            if (useImagePane != displayingImagePane) {
                displayingImagePane = useImagePane;
                if (useImagePane) {
                    InputUtil.setRewindTimeDisabled(false);
                    setContentPane(imagePane);
                } else {
                    InputUtil.setRewindTimeDisabled(true);
                    setContentPane(nsfPanel);
                }
                SwingUtilities.updateComponentTreeUI(this);
                maxipack(this);
            }
        });
    }

    public boolean isDisplayingImagePane() {
        return displayingImagePane;
    }

    private void removeAllActionListeners(final AbstractButton button) {
        for (final ActionListener listener : button.getActionListeners()) {
            button.removeActionListener(listener);
        }
    }

    public void quickLoadState(final int slot) {
        final QuickSaveStateInfo info = slot < 1 ? newestQuickSaveStateInfo
                : quickSaveStateInfos.get(slot - 1);
        if (info != null) {
            quickLoadState(slot, info.getFile());
        }
    }

    public void quickSaveState(final int slot) {
        final QuickSaveStateInfo info = slot < 1 ? oldestQuickSaveStateInfo
                : quickSaveStateInfos.get(slot - 1);
        if (info != null) {
            quickSaveState(info.getSlot(), info.getFile(), info.getLoadMenuItem(),
                    info.getSaveMenuItem());
        }
    }

    private void quickLoadState(final int slot, final File file) {
        if (isSaveable()) {
            App.setStepPause(false);
            App.loadState(this, file, slot);
        }
    }

    private void quickSaveState(final int slot, final File file,
                                final JMenuItem loadMenuItem, final JMenuItem saveMenuItem) {
        if (isSaveable()) {
            App.saveState(this, file, slot, () -> {
                final Date date = new java.util.Date();
                final String name = getFileTimestamp(slot, date);
                loadMenuItem.setEnabled(true);
                loadMenuItem.setText(name);
                saveMenuItem.setText(name);

                quickSaveStateInfos.set(slot - 1, new QuickSaveStateInfo(slot, file,
                        date.getTime(), loadMenuItem, saveMenuItem));
                findNewestAndOldestSaveSlots();
            });
        }
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(final Machine machine) {
        this.machine = machine;
        nsfPanel.setMachine(machine);
        EDT.async(this::initTimeRewinding);
    }

    public void appModeChanged(final AppMode appMode) {
        EDT.async(() -> {
            nsfPanel.appModeChanged(appMode);
            updateMenus();
        });
    }

    public String getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(final String fileInfo) {
        this.fileInfo = fileInfo;
    }

    public ImagePane getImagePane() {
        return imagePane;
    }

    public NsfPanel getNsfPanel() {
        return nsfPanel;
    }

    public void adjustSize() {
        EDT.sync(() -> {
            if (imagePane.getBufferStrategy() == null) {
                maxipack(this);
            }
        });
    }

    private void toggleFullscreenMode() {
        final SubMonitorFrame subMonitorFrame = App.getSubMonitorFrame();
        if (imagePane.getBufferStrategy() != null || (subMonitorFrame != null
                && subMonitorFrame.getImagePane().getBufferStrategy() != null)) {
            if (subMonitorFrame != null) {
                subMonitorFrame.exitFullscreenMode();
            }
            exitFullscreenMode();
        } else {
            if (subMonitorFrame != null) {
                subMonitorFrame.enterFullscreenMode();
            }
            enterFullscreenMode();
        }
    }

    private void enterFullscreenMode() {
        if (imagePane.getBufferStrategy() == null) {
            wasMaximized = isMaximized(this);
            final GraphicsDevice device = getGraphicsConfiguration().getDevice();
            dispose();
            requestVsync(this, false);
            setUndecorated(true);
            setMenuBarVisible(false);
            setIgnoreRepaint(true);
            device.setFullScreenWindow(this);
            createBufferStrategy(3);
            imagePane.setBufferStrategy(getBufferStrategy());
            EDT.async(() -> {
                setVisible(true);
                imagePane.redraw();
            });
        }
    }

    private void exitFullscreenMode() {
        if (imagePane.getBufferStrategy() != null) {
            exitFullscreenModeTime = System.currentTimeMillis();
            imagePane.getBufferStrategy().dispose();
            imagePane.setBufferStrategy(null);
            dispose();
            setUndecorated(false);
            setIgnoreRepaint(false);
            setMenuBarVisible(true);
            if (wasMaximized) {
                maximize(this);
            }
            requestVsync(this, true);
            EDT.async(() -> {
                setVisible(true);
                imagePane.redraw();
            });
        }
    }

    public void setFullscreenMode(final boolean fullscreenMode) {
        EDT.async(() -> {
            final SubMonitorFrame subMonitorFrame = App.getSubMonitorFrame();
            if (fullscreenMode) {
                if (subMonitorFrame != null) {
                    subMonitorFrame.enterFullscreenMode();
                }
                enterFullscreenMode();
            } else {
                if (subMonitorFrame != null) {
                    subMonitorFrame.exitFullscreenMode();
                }
                exitFullscreenMode();
            }
        });
    }

    private void openArchiveFile(final String archiveFileName,
                                 final String entryFileName, PleaseWaitDialog pleaseWaitDialog,
                                 final boolean editHeader, final Machine ejectedMachine) {

        if (!validateFdsBIOS(entryFileName)) {
            pleaseWaitDialog.dispose();
            if (EDT.sync(() -> checkFdsBIOS(entryFileName))) {
                pleaseWaitDialog = new PleaseWaitDialog(this);
                pleaseWaitDialog.setMessage("Reading archive file...");
                pleaseWaitDialog.showAfterDelay();
            } else {
                return;
            }
        }
        loadArchiveFile(archiveFileName, entryFileName, pleaseWaitDialog,
                editHeader, ejectedMachine);
    }

    private void openArchiveFile(final String archiveFileName,
                                 PleaseWaitDialog pleaseWaitDialog, final boolean editHeader,
                                 final Machine ejectedMachine) {

        java.util.List<String> entries = null;
        try {
            entries = toNames(getArchiveEntries(archiveFileName));
        } catch (final Throwable t) {
            //t.printStackTrace();
        }
        final java.util.List<String> files = entries;

        if (files == null) {
            pleaseWaitDialog.dispose();
            displayError(this, "Failed to open archive file.");
            App.setNoStepPause(false);
        } else if (files.isEmpty()) {
            List<ArchiveEntry> allEntries = null;
            if (!editHeader) {
                try {
                    allEntries = getArchiveEntries(archiveFileName, null);
                } catch (final Throwable t) {
                }
            }
            final int archiveID = identifyArchive(allEntries);
            if (archiveID >= 0) {
                loadMameFile(archiveFileName, allEntries, archiveID, pleaseWaitDialog,
                        ejectedMachine);
            } else {
                pleaseWaitDialog.dispose();
                displayError(this, "The archive does not contain any supported files.");
                App.setNoStepPause(false);
            }
        } else {
            final int defaultEntry = getDefaultArchiveEntry(archiveFileName, entries);
            if (files.size() == 1 || (defaultEntry >= 0 && AppPrefs.getInstance()
                    .getArchivePrefs().isOpenDefaultArchiveEntry())) {
                openArchiveFile(archiveFileName, files.get(defaultEntry),
                        pleaseWaitDialog, editHeader, ejectedMachine);
            } else {
                pleaseWaitDialog.dispose();
                EDT.async(() -> showArchiveFileChooser(archiveFileName,
                        files, defaultEntry, editHeader, ejectedMachine));
            }
        }
    }

    private void showArchiveFileChooser(final String archiveFileName,
                                        final java.util.List<String> files, final int defaultEntry,
                                        final boolean editHeader, final Machine ejectedMachine) {
        final ArchiveFileChooser archiveChooser = new ArchiveFileChooser(this,
                files, defaultEntry);
        archiveChooser.setVisible(true);

        final String entryFileName = archiveChooser.getSelectedFile();
        if (entryFileName == null) {
            App.setNoStepPause(false);
        } else if (checkFdsBIOS(entryFileName)) {
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            pleaseWaitDialog.setMessage("Reading archive file...");
            new Thread(() -> loadArchiveFile(archiveFileName, entryFileName,
                    pleaseWaitDialog, editHeader, ejectedMachine)).start();
            pleaseWaitDialog.showAfterDelay();
        }
    }

    private void loadMameFile(final String archiveFileName,
                              final List<ArchiveEntry> allEntries, final int archiveID,
                              final PleaseWaitDialog pleaseWaitDialog,
                              final Machine ejectedMachine) {

        String error = null;
        try {
            MameFileUtil.getArchiveInputStream(archiveFileName, allEntries,
                    archiveID, (stream, size) -> handleInputStream(archiveFileName, null,
                            pleaseWaitDialog, false, ejectedMachine, stream, size));
        } catch (final FileNotFoundException e) {
            error = "The selected file was not found.";
        } catch (final MessageException e) {
            //e.printStackTrace();
            error = e.getMessage();
        } catch (final Throwable t) {
            //t.printStackTrace();
            error = "Failed to load from archive.";
        }
        if (error != null) {
            pleaseWaitDialog.dispose();
            displayError("Invalid File Error", this, error);
        }
    }

    private void loadGoodNesFile(final String archiveFileName,
                                 final String entryFileName, final List<ArchiveEntry> allEntries,
                                 final int archiveID, final PleaseWaitDialog pleaseWaitDialog,
                                 final Machine ejectedMachine) {

        String error = null;
        try {
            GoodNesFileUtil.getArchiveInputStream(archiveFileName, allEntries,
                    archiveID, (stream, size) -> handleInputStream(archiveFileName,
                            entryFileName, pleaseWaitDialog, false, ejectedMachine, stream,
                            size));
        } catch (final FileNotFoundException e) {
            error = "The selected file was not found.";
        } catch (final MessageException e) {
            //e.printStackTrace();
            error = e.getMessage();
        } catch (final Throwable t) {
            //t.printStackTrace();
            error = "Failed to load from archive.";
        }
        if (error != null) {
            pleaseWaitDialog.dispose();
            displayError("Invalid File Error", this, error);
        }
    }

    private void loadArchiveFile(final String archiveFileName,
                                 final String entryFileName, final PleaseWaitDialog pleaseWaitDialog,
                                 final boolean editHeader, final Machine ejectedMachine) {

        if (!editHeader) {
            App.close();

            final int candidateID = GoodNesFileUtil.identifyCandidate(archiveFileName,
                    entryFileName);
            if (candidateID >= 0) {
                final List<ArchiveEntry> allEntries;
                try {
                    allEntries = FileUtil.getArchiveEntries(archiveFileName, null);
                } catch (final Throwable t) {
                    t.printStackTrace();
                    pleaseWaitDialog.dispose();
                    displayError("Invalid File Error", this,
                            "Failed to load from archive.");
                    return;
                }
                final int archiveID = GoodNesFileUtil.confirmCandidate(
                        archiveFileName, candidateID, allEntries);
                if (archiveID >= 0) {
                    loadGoodNesFile(archiveFileName, entryFileName, allEntries, archiveID,
                            pleaseWaitDialog, ejectedMachine);
                    return;
                }
            }
        }

        String error = null;
        try {
            IpsUtil.getArchiveInputStream(archiveFileName, entryFileName,
                    (stream, size) -> handleInputStream(archiveFileName, entryFileName,
                            pleaseWaitDialog, editHeader, ejectedMachine, stream, size));
        } catch (final FileNotFoundException e) {
            error = "The selected file was not found.";
        } catch (final MessageException e) {
            //e.printStackTrace();
            error = e.getMessage();
        } catch (final Throwable t) {
            t.printStackTrace();
            error = "Failed to load from archive.";
        }
        if (error != null) {
            pleaseWaitDialog.dispose();
            displayError("Invalid File Error", this, error);
        }
    }

    private void handleInputStream(final String archiveFileName,
                                   final String entryFileName, final PleaseWaitDialog pleaseWaitDialog,
                                   final boolean editHeader, final Machine ejectedMachine,
                                   final InputStream stream, final long size) {

        String errorMessage = null;
        try (final DataInputStream in = new DataInputStream(
                new BufferedInputStream(stream))) {
            if (editHeader) {
                AppPrefs.getInstance().getPaths().addRecentDirectory(
                        getDirectoryPath(archiveFileName));
                AppPrefs.save();
                editFileHeader(in, size, pleaseWaitDialog, entryFileName);
                EDT.async(this::createRecentFilesMenu);
            } else {
                App.setStepPause(false);
                final Mapper m = App.loadFile(in, size, entryFileName,
                        archiveFileName);
                if (App.isVsSystem() && AppPrefs.getInstance()
                        .getDipSwitchesAppPrefs().isDisplayDialogOnLoad()) {
                    pleaseWaitDialog.dispose();
                    App.showDipSwitchesDialog();
                    fileInfo = App.createMachine(m, ejectedMachine);
                } else {
                    fileInfo = App.createMachine(m, ejectedMachine);
                    pleaseWaitDialog.dispose();
                }
                AppPrefs.getInstance().getPaths().addRecentFile(entryFileName,
                        archiveFileName);
                AppPrefs.save();
                saveFileName = createSaveFile(isBlank(entryFileName) ? archiveFileName
                        : entryFileName);
                lastSaveFile = new File(saveFileName);
                EDT.async(this::finishLoad);
            }
        } catch (final FileNotFoundException e) {
            errorMessage = "The selected file was not found.";
        } catch (final MessageException e) {
            //e.printStackTrace();
            errorMessage = e.getMessage();
        } catch (final Throwable t) {
            t.printStackTrace();
            errorMessage = "Failed to load from archive.";
        }
        if (errorMessage != null) {
            pleaseWaitDialog.dispose();
            if (editHeader) {
                App.setNoStepPause(false);
            } else {
                App.close();
            }
            displayError("Invalid File Error", this, errorMessage);
        }
    }

    private void loadDiskFile(final String fileName,
                              final PleaseWaitDialog pleaseWaitDialog, final boolean editHeader,
                              final Machine ejectedMachine) {

        DataInputStream in = null;
        String errorMessage = null;
        try {
            final IpsUtil.OpenFileHandle handle = IpsUtil.getOpenFileHandle(fileName);
            in = new DataInputStream(new BufferedInputStream(handle
                    .getInputStream()));

            if (editHeader) {
                AppPrefs.getInstance().getPaths().addRecentDirectory(
                        getDirectoryPath(fileName));
                AppPrefs.save();
                editFileHeader(in, handle.getFileSize(), pleaseWaitDialog,
                        getFileName(fileName));
                EDT.async(this::createRecentFilesMenu);
            } else {
                App.setStepPause(false);
                final Mapper m = App.loadFile(in, handle.getFileSize(), fileName);
                if (App.isVsSystem() && AppPrefs.getInstance()
                        .getDipSwitchesAppPrefs().isDisplayDialogOnLoad()) {
                    pleaseWaitDialog.dispose();
                    App.showDipSwitchesDialog();
                    fileInfo = App.createMachine(m, ejectedMachine);
                } else {
                    fileInfo = App.createMachine(m, ejectedMachine);
                    pleaseWaitDialog.dispose();
                }
                AppPrefs.getInstance().getPaths().addRecentFile(fileName);
                AppPrefs.save();
                saveFileName = createSaveFile(fileName);
                lastSaveFile = new File(saveFileName);
                EDT.async(this::finishLoad);
            }
        } catch (final FileNotFoundException e) {
            errorMessage = "The selected file was not found.";
        } catch (final MessageException e) {
//      e.printStackTrace();
            errorMessage = e.getMessage();
        } catch (final Throwable t) {
            t.printStackTrace();
            errorMessage = "Unable to open file.";
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final Throwable t) {
                }
            }
        }

        if (errorMessage != null) {
            pleaseWaitDialog.dispose();
            if (editHeader) {
                App.setNoStepPause(false);
            } else {
                App.close();
            }
            displayError("Invalid File Error", this, errorMessage);
        }
    }

    private void finishLoad() {
        App.setSpeed(100);
        final UserInterfacePrefs prefs = AppPrefs.getInstance()
                .getUserInterfacePrefs();
        createQuickMenus();
        createRecentFilesMenu();
        updateMenus();
        if (prefs.isHideMenuBar()) {
            setMenuBarVisible(false);
        }
        if (prefs.isEnterFullscreen()) {
            enterFullscreenMode();
        }
    }

    private void updateMenus() {
        fileMenuMenuSelected(null);
        machineMenuMenuSelected(null);
        viewMenuMenuSelected(null);
        toolsMenuMenuSelected(null);
        debugMenuMenuSelected(null);
    }

    private boolean validateFdsBIOS(final String fileName) {
        return !isFamicomDiskSystemFile(fileName) || App.loadFdsBIOS();
    }

    // Boolean for invokeAndWait()
    private Boolean checkFdsBIOS(final String fileName) {
        if (!validateFdsBIOS(fileName)) {
            final YesNoDialog yesNoDialog = new YesNoDialog(this, AppPrefs
                    .getInstance().getFamicomDiskSystemPrefs().getBiosFile() == null ?
                    "<p>Famicom Disk System emulation requires a BIOS file.</p>"
                            + "<p></p><p>Set that up?</p>" :
                    "<p>The provided Famicom Disk System BIOS file is missing or "
                            + "invalid.</p><p></p><p>Update the configuration?</p>",
                    "BIOS File Required");
            yesNoDialog.setVisible(true);
            if (!yesNoDialog.isYes()) {
                return false;
            }
            final FamicomDiskSystemOptionsDialog fdsDialog
                    = new FamicomDiskSystemOptionsDialog(this);
            fdsDialog.setVisible(true);
            return App.isFdsBiosLoaded();
        }
        return true;
    }

    private void editFileHeader(final DataInputStream in, final long fileSize,
                                final PleaseWaitDialog pleaseWaitDialog, final String entryFileName)
            throws Throwable {
        final MutableNesFile nesFile = new MutableNesFile(in, fileSize);
        pleaseWaitDialog.dispose();
        EDT.async(() -> {
            final EditNesHeaderDialog editDialog
                    = new EditNesHeaderDialog(this, true);
            editDialog.setNesFile(nesFile);
            editDialog.setEntryFileName(entryFileName);
            editDialog.pack();
            editDialog.setLocationRelativeTo(this);
            editDialog.setVisible(true);
            if (editDialog.getSaveFile() == null) {
                App.setNoStepPause(false);
            } else {
                final PleaseWaitDialog pwDialog = new PleaseWaitDialog(this);
                new Thread(() -> saveEditedNesFile(editDialog, pwDialog)).start();
                pwDialog.showAfterDelay();
            }
        });
    }

    private void saveEditedNesFile(final EditNesHeaderDialog editDialog,
                                   final PleaseWaitDialog pleaseWaitDialog) {
        try {
            editDialog.getNesFile().write(editDialog.getSaveFile());
            pleaseWaitDialog.dispose();
        } catch (final Throwable t) {
            //t.printStackTrace();
            pleaseWaitDialog.dispose();
            displayError(this, "Failed to save NES file.");
        }
        App.setNoStepPause(false);
    }

    private void exit() {
        App.setNoStepPause(true);
        if (AppPrefs.getInstance().getUserInterfacePrefs().isConfirmExit()) {
            final YesNoDialog yesNoDialog = new YesNoDialog(this,
                    "Do you really want to exit?", "Confirm Exit");
            yesNoDialog.setVisible(true);
            if (yesNoDialog.isNo()) {
                App.setNoStepPause(false);
                return;
            }
        }

        try {
            App.close();
            App.destroyFrames();
            AppPrefs.flush();
            dispose();
        } catch (Throwable t) {
        }
        System.exit(0);
    }

    private void createRecentFilesMenu() {
        openRecentFileMenu.removeAll();
        final Paths paths = AppPrefs.getInstance().getPaths();
        final java.util.List<FilePath> filePaths = paths.getRecentFiles();
        if (filePaths.isEmpty()) {
            openRecentFileMenu.setEnabled(false);
        } else {
            openRecentFileMenu.setEnabled(App.getAppMode() == AppMode.Default);
            synchronized (AppPrefs.class) {
                for (final FilePath filePath : filePaths) {
                    final JMenuItem menuItem = new JMenuItem(filePath.toString());
                    scaleMenuItemFont(menuItem);
                    menuItem.addActionListener(e -> recentFileMenuItemPressed(filePath));
                    openRecentFileMenu.add(menuItem);
                }
            }
            openRecentFileMenu.add(recentFilesSeparator);
            openRecentFileMenu.add(recentFilesLockCheckBoxMenuItem);
            openRecentFileMenu.add(recentFilesClearMenuItem);
        }
        recentFilesLockCheckBoxMenuItem.setSelected(paths.isLockRecentFiles());
        createRecentArchivesMenu();
    }

    private void createRecentArchivesMenu() {
        openRecentArchiveMenu.removeAll();
        final Paths paths = AppPrefs.getInstance().getPaths();
        final java.util.List<String> archiveFiles = paths.getRecentArchives();
        if (archiveFiles.isEmpty()) {
            openRecentArchiveMenu.setEnabled(false);
        } else {
            openRecentArchiveMenu.setEnabled(true);
            synchronized (AppPrefs.class) {
                for (final String archiveFile : archiveFiles) {
                    final JMenuItem menuItem = new JMenuItem(getFileName(archiveFile));
                    scaleMenuItemFont(menuItem);
                    menuItem.addActionListener(
                            e -> recentArchiveMenuItemPressed(archiveFile));
                    openRecentArchiveMenu.add(menuItem);
                }
            }
            openRecentArchiveMenu.add(recentArchivesSeparator);
            openRecentArchiveMenu.add(recentArchivesLockCheckBoxMenuItem);
            openRecentArchiveMenu.add(recentArchivesClearMenuItem);
        }
        recentArchivesLockCheckBoxMenuItem.setSelected(
                paths.isLockRecentArchives());
    }

    private void createRecentDirectoriesMenu() {
        openRecentDirectoryMenu.removeAll();
        final Paths paths = AppPrefs.getInstance().getPaths();
        final java.util.List<String> directories = paths.getRecentDirectories();
        if (!directories.isEmpty()) {
            synchronized (AppPrefs.class) {
                for (final String directory : directories) {
                    final JMenuItem menuItem = new JMenuItem(directory);
                    scaleMenuItemFont(menuItem);
                    menuItem.addActionListener(e -> openFile(directory, false, null));
                    openRecentDirectoryMenu.add(menuItem);
                }
            }
            openRecentDirectoryMenu.add(recentDirectoriesSeparator);
            openRecentDirectoryMenu.add(recentDirectoriesLockCheckBoxMenuItem);
            openRecentDirectoryMenu.add(recentDirectoriesClearMenuItem);
        }
        recentDirectoriesLockCheckBoxMenuItem.setSelected(
                paths.isLockRecentDirectories());
    }

    public void open(final FilePath filePath) {
        EDT.async(() -> recentFileMenuItemPressed(filePath));
    }

    private void recentFileMenuItemPressed(final FilePath filePath) {

        if (!checkFdsBIOS(filePath.getEntryPath())) {
            return;
        }

        final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
        if (filePath.isArchivedEntry()) {
            pleaseWaitDialog.setMessage("Reading archive file...");
            new Thread(() -> loadArchiveFile(filePath.getArchivePath(),
                    filePath.getEntryPath(), pleaseWaitDialog, false, null)).start();
        } else {
            pleaseWaitDialog.setMessage("Reading file...");
            new Thread(() -> loadDiskFile(filePath.getEntryPath(), pleaseWaitDialog,
                    false, null)).start();
        }
        pleaseWaitDialog.showAfterDelay();
    }

    private void recentArchiveMenuItemPressed(final String archiveFile) {
        App.setNoStepPause(true);
        final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
        pleaseWaitDialog.setMessage("Reading archive file...");
        new Thread(() -> openArchiveFile(archiveFile, pleaseWaitDialog, false,
                null)).start();
        pleaseWaitDialog.showAfterDelay();
    }

    private void openFile(String directory, final boolean editHeader,
                          final Machine ejectedMachine) {

        App.setNoStepPause(true);

        final Paths paths = AppPrefs.getInstance().getPaths();
        if (directory == null) {
            directory = paths.getFilesDir();
        }

        final JFileChooser chooser = createFileChooser("Open", directory,
                FileExtensionFilters);
        chooser.setFileFilter(
                FileExtensionFilters[paths.getFileExtensionFilterIndex()]);
        if (showOpenDialog(this, chooser, null) == JFileChooser.APPROVE_OPTION) {
            final javax.swing.filechooser.FileFilter fileFilter
                    = chooser.getFileFilter();
            final File file = chooser.getSelectedFile();
            if (file == null || isBlank(file.toString())) {
                App.setNoStepPause(false);
                return;
            }

            fileOpened(chooser.getSelectedFile(), directory, editHeader,
                    ejectedMachine, fileFilter);
        } else {
            App.setNoStepPause(false);
        }
    }

    public void openFile(final String fileName) {
        EDT.async(() -> ImageFrame.this.openFile(new File(fileName)));
    }

    public void openFile(final File file) {
        EDT.async(() -> fileOpened(file, null, false, null, null));
    }

    private void fileOpened(final File file, String directory,
                            final boolean editHeader, final Machine ejectedMachine,
                            final javax.swing.filechooser.FileFilter fileFilter) {

        if (file == null || isBlank(file.toString())) {
            App.setNoStepPause(false);
            return;
        }

        final Paths paths = AppPrefs.getInstance().getPaths();
        if (directory == null) {
            directory = paths.getFilesDir();
        }

        final FilePath filePath = FilePath.fromLongString(file.toString());
        if (filePath == null) {
            displayError("Open File Error", this, "Invalid file name.");
            App.setNoStepPause(false);
            return;
        }

        final File outerFile = new File(filePath.getOuterPath());
        if (!outerFile.exists()) {
            if (isDirectory(outerFile)) {
                displayError("Open File Error", this,
                        "The specified directory does not exist.");
                App.setNoStepPause(false);
                final String dir = directory;
                EDT.async(() -> openFile(dir, editHeader,
                        ejectedMachine));
                return;
            } else {
                displayError("Open File Error", this,
                        "The specified file does not exist.");
                App.setNoStepPause(false);
                return;
            }
        } else if (!outerFile.isFile()) {
            displayError("Open File Error", this,
                    "The specified path is not a file or an existing directory.");
            App.setNoStepPause(false);
            return;
        }

        final String archiveFileName;
        final String entryFileName;
        if (filePath.isArchivedEntry()) {
            if (!isArchiveFile(filePath.getArchivePath())) {
                displayError("Open File Error", this,
                        "Unsupported archive file type.");
                App.setNoStepPause(false);
                return;
            } else {
                archiveFileName = filePath.getArchivePath();
                entryFileName = filePath.getEntryPath();
            }
        } else if (isArchiveFile(filePath.getEntryPath())) {
            archiveFileName = filePath.getEntryPath();
            entryFileName = null;
        } else {
            archiveFileName = null;
            entryFileName = filePath.getEntryPath();
        }

        paths.setFilesDir(getDirectoryPath(filePath.getOuterPath()));
        if (fileFilter instanceof FileExtensionFilter
                && fileFilter.accept(new File(filePath.getOuterPath()))) {
            paths.setFileExtensionFilterIndex(((FileExtensionFilter) fileFilter)
                    .getIndex());
        } else {
            paths.setFileExtensionFilterIndex(0);
        }
        AppPrefs.save();

        if (archiveFileName != null && entryFileName != null
                && !checkFdsBIOS(entryFileName)) {
            App.setNoStepPause(false);
            return;
        }

        if (archiveFileName != null || checkFdsBIOS(entryFileName)) {
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            if (archiveFileName != null) {
                pleaseWaitDialog.setMessage("Reading archive file...");
                if (entryFileName != null) {
                    new Thread(() -> openArchiveFile(archiveFileName, entryFileName,
                            pleaseWaitDialog, editHeader, ejectedMachine)).start();
                } else {
                    new Thread(() -> openArchiveFile(archiveFileName, pleaseWaitDialog,
                            editHeader, ejectedMachine)).start();
                }
            } else {
                pleaseWaitDialog.setMessage("Reading file...");
                new Thread(() -> loadDiskFile(entryFileName, pleaseWaitDialog,
                        editHeader, ejectedMachine)).start();
            }
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }

    private void saveOrOpenState(final int dialogType) {
        App.setNoStepPause(true);
        final String directory = AppPrefs.getInstance().getPaths()
                .getSaveStatesDir();
        mkdir(directory);
        final JFileChooser chooser = createFileChooser(
                dialogType == JFileChooser.SAVE_DIALOG ? "Save As" : "Open",
                directory, new FileNameExtensionFilter("Nintaco Save File (save)",
                        "save"));
        chooser.setSelectedFile(lastSaveFile);
        if (showFileChooser(this, chooser, null, dialogType)
                == JFileChooser.APPROVE_OPTION) {
            if (dialogType == JFileChooser.SAVE_DIALOG) {
                saveState(chooser.getSelectedFile());
                App.setNoStepPause(false);
            } else {
                loadState(chooser.getSelectedFile());
            }
        } else {
            App.setNoStepPause(false);
        }
    }

    public void saveState(final File stateFile) {
        lastSaveFile = stateFile;
        App.saveState(this, stateFile, -1);
    }

    public void loadState(final File stateFile) {
        lastSaveFile = stateFile;
        App.setStepPause(false);
        App.loadState(this, stateFile, -1);
    }

    public void addQuickSaveListener(final QuickSaveListener quickSaveListener) {
        quickSaveListeners.add(quickSaveListener);
        fireQuickSaveListeners();
    }

    public void removeQuickSaveListener(
            final QuickSaveListener quickSaveListener) {
        quickSaveListeners.remove(quickSaveListener);
    }

    public void onStepPausedChanged(final boolean paused) {
        imagePane.setPaused(paused);
        pauseMenuItem.setSelected(paused);
        nextFrameMenuItem.setEnabled(pauseMenuItem.isEnabled() && paused);
    }

    private void enableKeyEvents() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .removeKeyEventDispatcher(DisableKeyEventsDispatcher);
    }

    public void setKeyEventsEnabled(final boolean keyEventsEnabled) {
        EDT.async(() -> {
            this.keyEventsEnabled = keyEventsEnabled;
            final KeyboardFocusManager manager = KeyboardFocusManager
                    .getCurrentKeyboardFocusManager();
            manager.removeKeyEventDispatcher(DisableKeyEventsDispatcher);
            if (!keyEventsEnabled) {
                manager.addKeyEventDispatcher(DisableKeyEventsDispatcher);
            }
        });
    }

    private void loadBasicProgram(final File file,
                                  final PleaseWaitDialog pleaseWaitDialog) {

        if (executeMessageTask(this, pleaseWaitDialog,
                () -> FamilyBasicUtil.loadProgram(file))) {
            basicProgramFile = file;
        }
    }

    private void saveBasicProgram(final File file,
                                  final PleaseWaitDialog pleaseWaitDialog) {

        if (executeMessageTask(this, pleaseWaitDialog,
                () -> FamilyBasicUtil.saveProgram(file))) {
            basicProgramFile = file;
        }
    }

    private void loadTape(final File file,
                          final PleaseWaitDialog pleaseWaitDialog) {

        boolean loadError = false;
        try {
            machine.getMapper().getDataRecorder().loadTape(file);
        } catch (final Throwable t) {
            //t.printStackTrace();
            loadError = true;
        }
        pleaseWaitDialog.dispose();
        if (loadError) {
            displayError("Load Tape Error", this, "Failed to load tape file.");
        } else {
            displayInformation("Tape Loaded", this,
                    "The tape data was successfully retrieved.");
        }
        App.setNoStepPause(false);
    }

    private void saveTape(final File file,
                          final PleaseWaitDialog pleaseWaitDialog) {

        boolean saveError = false;
        try {
            machine.getMapper().getDataRecorder().saveTape(file);
        } catch (final Throwable t) {
            //t.printStackTrace();
            saveError = true;
        }
        pleaseWaitDialog.dispose();
        if (saveError) {
            displayError("Save Tape Error", this, "Failed to save tape file.");
        } else {
            displayInformation("Tape Saved", this,
                    "The tape data was successfully stored.");
        }
        App.setNoStepPause(false);
    }

    private void setScreenScale(final int scale) {
        App.runSubMonitorFrame(f -> f.setScreenScale(scale));
        setFullscreenMode(false);
        final Rectangle area = getScreenWorkingArea(this);
        if ((scale << 8) > area.width || 240 * scale > area.height) {
            screenSizeMaxMenuItemActionPerformed(null);
        } else {
            normalize(this);
            imagePane.setScreenScale(scale);
            maxipack(this);
        }
    }

    private void setMenuBarVisible(final boolean visible) {
        if (visible) {
            removeKeyListener(NoMenuBarKeyListener);
        } else {
            addKeyListener(NoMenuBarKeyListener);
        }
        menuBar.setVisible(visible);
        maxipack(this);
    }

    private void reset() {
        App.reset();
    }

    private void pause() {
        App.setStepPause(pauseMenuItem.isSelected());
    }

    private void nextFrame() {
        App.step(PauseStepType.Frame);
    }

    private void flipDisk() {
        InputUtil.addOtherInput(new FlipDiskSide());
    }

    private void insertCoin(final int vsSystem, final int coinSlot) {
        InputUtil.addOtherInput(new InsertCoin(vsSystem, coinSlot));
    }

    private void setUseTvAspectRatio(final boolean useTvAspectRatio) {
        App.runSubMonitorFrame(f -> f.setUseTvAspectRatio(useTvAspectRatio));
        if (imagePane.isUseTvAspectRatio() != useTvAspectRatio) {
            imagePane.setUseTvAspectRatio(useTvAspectRatio);
            maxipack(this);
        }
    }

    private void setTVSystem(final TVSystem tvSystem) {
        InputUtil.addOtherInput(new SetTVSystem(tvSystem));
    }

    private boolean isSaveable() {
        final AppMode appMode = App.getAppMode();
        return machine != null && appMode != AppMode.HistoryEditor
                && appMode != AppMode.WatchHistory
                && (appMode != AppMode.NetplayClient
                || App.getNetplayClient().getQuickSaveNames() != null);
    }

    private boolean isPauseable() {
        final AppMode appMode = App.getAppMode();
        return machine != null && displayingImagePane
                && appMode != AppMode.WatchHistory && appMode != AppMode.HistoryEditor
                && appMode != AppMode.NetplayClient;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        screenSizeButtonGroup = new javax.swing.ButtonGroup();
        lookAndFeelButtonGroup = new javax.swing.ButtonGroup();
        familyBasicTapeButtonGroup = new javax.swing.ButtonGroup();
        regionButtonGroup = new javax.swing.ButtonGroup();
        jMenuItem1 = new javax.swing.JMenuItem();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        openRecentFileMenu = new javax.swing.JMenu();
        recentFilesSeparator = new javax.swing.JPopupMenu.Separator();
        recentFilesLockCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        recentFilesClearMenuItem = new javax.swing.JMenuItem();
        openRecentArchiveMenu = new javax.swing.JMenu();
        recentArchivesSeparator = new javax.swing.JPopupMenu.Separator();
        recentArchivesLockCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        recentArchivesClearMenuItem = new javax.swing.JMenuItem();
        openRecentDirectoryMenu = new javax.swing.JMenu();
        recentDirectoriesSeparator = new javax.swing.JPopupMenu.Separator();
        recentDirectoriesLockCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        recentDirectoriesClearMenuItem = new javax.swing.JMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        separator3 = new javax.swing.JPopupMenu.Separator();
        loadStateMenuItem = new javax.swing.JMenuItem();
        saveStateMenuItem = new javax.swing.JMenuItem();
        separator1 = new javax.swing.JPopupMenu.Separator();
        quickLoadStateMenu = new javax.swing.JMenu();
        quickLoadSeparator = new javax.swing.JPopupMenu.Separator();
        newestSlotMenuItem = new javax.swing.JMenuItem();
        quickSaveStateMenu = new javax.swing.JMenu();
        quickSaveSeparator = new javax.swing.JPopupMenu.Separator();
        oldestSlotMenuItem = new javax.swing.JMenuItem();
        separator8 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        machineMenu = new javax.swing.JMenu();
        resetMenuItem = new javax.swing.JMenuItem();
        powerCycleMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        pauseMenuItem = new javax.swing.JCheckBoxMenuItem();
        nextFrameMenuItem = new javax.swing.JMenuItem();
        speedMenu = new javax.swing.JMenu();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        tvSystemMenu = new javax.swing.JMenu();
        ntscRegionRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        palRegionRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        dendyRegionRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        jSeparator20 = new javax.swing.JPopupMenu.Separator();
        insertDiskMenu = new javax.swing.JMenu();
        flipDiskSideMenuItem = new javax.swing.JMenuItem();
        ejectDiskMenuItem = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        familyBasicMenu = new javax.swing.JMenu();
        familyBasicPlayTapeMenuItem = new javax.swing.JRadioButtonMenuItem();
        familyBasicRecordTapeMenuItem = new javax.swing.JRadioButtonMenuItem();
        familyBasicStopTapeMenuItem = new javax.swing.JRadioButtonMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        familyBasicLoadTapeMenuItem = new javax.swing.JMenuItem();
        familyBasicSaveTapeMenuItem = new javax.swing.JMenuItem();
        familyBasicEraseTapeMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        familyBasicCopyProgramMenuItem = new javax.swing.JMenuItem();
        familyBasicPasteProgramMenuItem = new javax.swing.JMenuItem();
        familyBasicTypePasteMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        familyBasicLoadProgramMenuItem = new javax.swing.JMenuItem();
        familyBasicSaveProgramMenuItem = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        familyBasicEditBackgroundMenuItem = new javax.swing.JMenuItem();
        familyBasicLoadBackgroundMenuItem = new javax.swing.JMenuItem();
        familyBasicSaveBackgroundMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        insertCoinMenu = new javax.swing.JMenu();
        insertCoinMainLeftMenuItem = new javax.swing.JMenuItem();
        insertCoinMainRightMenuItem = new javax.swing.JMenuItem();
        insertCoinSubLeftMenuItem = new javax.swing.JMenuItem();
        insertCoinSubRightMenuItem = new javax.swing.JMenuItem();
        serviceButtonMenu = new javax.swing.JMenu();
        serviceButtonMainMenuItem = new javax.swing.JMenuItem();
        serviceButtonSubMenuItem = new javax.swing.JMenuItem();
        dipSwitchesMenuItem = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JPopupMenu.Separator();
        screamIntoMicrophoneMenuItem = new javax.swing.JMenuItem();
        jSeparator22 = new javax.swing.JPopupMenu.Separator();
        eraseBatterySaveMenuItem = new javax.swing.JMenuItem();
        hotSwapMenuItem = new javax.swing.JMenuItem();
        glitchMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        fileInfoMenuItem = new javax.swing.JMenuItem();
        separator15 = new javax.swing.JPopupMenu.Separator();
        barcodeBattlerMenuItem = new javax.swing.JMenuItem();
        famicom3dGlassesMenuItem = new javax.swing.JMenuItem();
        robMenuItem = new javax.swing.JMenuItem();
        subMonitorMenuItem = new javax.swing.JMenuItem();
        separator5 = new javax.swing.JPopupMenu.Separator();
        showMenu = new javax.swing.JMenu();
        statusMessagesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        fpsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        inputDevicesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator19 = new javax.swing.JPopupMenu.Separator();
        backgroundCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        spritesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        spriteBoxesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        noSpriteLimitCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        screenSizeMenu = new javax.swing.JMenu();
        screenSize1XMenuItem = new javax.swing.JMenuItem();
        screenSize2XMenuItem = new javax.swing.JMenuItem();
        screenSize3XMenuItem = new javax.swing.JMenuItem();
        screenSize4XMenuItem = new javax.swing.JMenuItem();
        screenSize5XMenuItem = new javax.swing.JMenuItem();
        screenSizeMaxMenuItem = new javax.swing.JMenuItem();
        separator16 = new javax.swing.JPopupMenu.Separator();
        tvAspectCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        smoothScalingCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        uniformPixelScalingCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        underscanCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        videoFilterMenu = new javax.swing.JMenu();
        paletteMenu = new javax.swing.JMenu();
        lookAndFeelMenu = new javax.swing.JMenu();
        fontSizeMenuItem = new javax.swing.JMenuItem();
        separator7 = new javax.swing.JPopupMenu.Separator();
        hideMenuBarMenuItem = new javax.swing.JMenuItem();
        fullscreenMenuItem = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        buttonMappingMenuItem = new javax.swing.JMenuItem();
        portsMenuItem = new javax.swing.JMenuItem();
        inputSettingsMenuItem = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        soundMenuItem = new javax.swing.JMenuItem();
        volumeMixerMenuItem = new javax.swing.JMenuItem();
        nsfOptionsMenuItem = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JPopupMenu.Separator();
        contentDirectoryMenuItem = new javax.swing.JMenuItem();
        archiveFileOptionsMenuItem = new javax.swing.JMenuItem();
        famicomDiskSystemOptionsMenuItem = new javax.swing.JMenuItem();
        familyBasicOptionsMenuItem = new javax.swing.JMenuItem();
        traceLoggerOptionsMenuItem = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JPopupMenu.Separator();
        userInterfaceMenuItem = new javax.swing.JMenuItem();
        screenshotOptionsMenuItem = new javax.swing.JMenuItem();
        overscanMenuItem = new javax.swing.JMenuItem();
        palettesMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        startNetplayServerMenuItem = new javax.swing.JMenuItem();
        connectToNetplayServerMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        mapMakerMenuItem = new javax.swing.JMenuItem();
        spriteSaverMenuItem = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        runProgramMenuItem = new javax.swing.JMenuItem();
        startProgramServerMenuItem = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        manageCheatsMenuItem = new javax.swing.JMenuItem();
        searchCheatsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        editFileHeaderMenuItem = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        saveScreenshotMenuItem = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        applyIPSMenuItem = new javax.swing.JMenuItem();
        createIPSMenuItem = new javax.swing.JMenuItem();
        jSeparator21 = new javax.swing.JPopupMenu.Separator();
        trackHistoryCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        watchHistoryMenuItem = new javax.swing.JMenuItem();
        exportVideoAudioMenuItem = new javax.swing.JMenuItem();
        editHistoryMenuItem = new javax.swing.JMenuItem();
        clearHistoryMenuItem = new javax.swing.JMenuItem();
        importHistoryMenuItem = new javax.swing.JMenuItem();
        exportHistoryMenuItem = new javax.swing.JMenuItem();
        rewindTimeCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        debugMenu = new javax.swing.JMenu();
        debuggerMenuItem = new javax.swing.JMenuItem();
        oamDataMenuItem = new javax.swing.JMenuItem();
        patternTablesMenuItem = new javax.swing.JMenuItem();
        nametablesMenuItem = new javax.swing.JMenuItem();
        hexEditorMenuItem = new javax.swing.JMenuItem();
        asmDasmMenuItem = new javax.swing.JMenuItem();
        ramSearchMenuItem = new javax.swing.JMenuItem();
        ramWatchMenuItem = new javax.swing.JMenuItem();
        jSeparator23 = new javax.swing.JPopupMenu.Separator();
        startTraceLoggerMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpMenuItem = new javax.swing.JMenuItem();
        licenseMenuItem = new javax.swing.JMenuItem();
        separator2 = new javax.swing.JPopupMenu.Separator();
        aboutMenuItem = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Nintaco");
        setMaximumSize(null);
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowStateListener(new java.awt.event.WindowStateListener() {
            public void windowStateChanged(java.awt.event.WindowEvent evt) {
                formWindowStateChanged(evt);
            }
        });
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }

            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }

            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");
        fileMenu.setName(""); // NOI18N
        fileMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                fileMenuMenuSelected(evt);
            }
        });

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setMnemonic('O');
        openMenuItem.setText("Open...");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        openRecentFileMenu.setMnemonic('F');
        openRecentFileMenu.setText("Open Recent File");
        openRecentFileMenu.add(recentFilesSeparator);

        recentFilesLockCheckBoxMenuItem.setMnemonic('L');
        recentFilesLockCheckBoxMenuItem.setText("Lock");
        recentFilesLockCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentFilesLockCheckBoxMenuItemActionPerformed(evt);
            }
        });
        openRecentFileMenu.add(recentFilesLockCheckBoxMenuItem);

        recentFilesClearMenuItem.setMnemonic('C');
        recentFilesClearMenuItem.setText("Clear");
        recentFilesClearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentFilesClearMenuItemActionPerformed(evt);
            }
        });
        openRecentFileMenu.add(recentFilesClearMenuItem);

        fileMenu.add(openRecentFileMenu);

        openRecentArchiveMenu.setMnemonic('A');
        openRecentArchiveMenu.setText("Open Recent Archive");
        openRecentArchiveMenu.add(recentArchivesSeparator);

        recentArchivesLockCheckBoxMenuItem.setMnemonic('L');
        recentArchivesLockCheckBoxMenuItem.setSelected(true);
        recentArchivesLockCheckBoxMenuItem.setText("Lock");
        recentArchivesLockCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentArchivesLockCheckBoxMenuItemActionPerformed(evt);
            }
        });
        openRecentArchiveMenu.add(recentArchivesLockCheckBoxMenuItem);

        recentArchivesClearMenuItem.setMnemonic('C');
        recentArchivesClearMenuItem.setText("Clear");
        recentArchivesClearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentArchivesClearMenuItemActionPerformed(evt);
            }
        });
        openRecentArchiveMenu.add(recentArchivesClearMenuItem);

        fileMenu.add(openRecentArchiveMenu);

        openRecentDirectoryMenu.setMnemonic('D');
        openRecentDirectoryMenu.setText("Open Recent Directory");
        openRecentDirectoryMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                openRecentDirectoryMenuMenuSelected(evt);
            }
        });
        openRecentDirectoryMenu.add(recentDirectoriesSeparator);

        recentDirectoriesLockCheckBoxMenuItem.setMnemonic('L');
        recentDirectoriesLockCheckBoxMenuItem.setText("Lock");
        recentDirectoriesLockCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentDirectoriesLockCheckBoxMenuItemActionPerformed(evt);
            }
        });
        openRecentDirectoryMenu.add(recentDirectoriesLockCheckBoxMenuItem);

        recentDirectoriesClearMenuItem.setMnemonic('C');
        recentDirectoriesClearMenuItem.setText("Clear");
        recentDirectoriesClearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentDirectoriesClearMenuItemActionPerformed(evt);
            }
        });
        openRecentDirectoryMenu.add(recentDirectoriesClearMenuItem);

        fileMenu.add(openRecentDirectoryMenu);

        closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        closeMenuItem.setMnemonic('C');
        closeMenuItem.setText("Close");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeMenuItem);
        fileMenu.add(separator3);

        loadStateMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, 0));
        loadStateMenuItem.setMnemonic('L');
        loadStateMenuItem.setText("Load State...");
        loadStateMenuItem.setEnabled(false);
        loadStateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadStateMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadStateMenuItem);

        saveStateMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        saveStateMenuItem.setText("Save State...");
        saveStateMenuItem.setEnabled(false);
        saveStateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveStateMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveStateMenuItem);
        fileMenu.add(separator1);

        quickLoadStateMenu.setMnemonic('a');
        quickLoadStateMenu.setText("Quick Load State");
        quickLoadStateMenu.setEnabled(false);
        quickLoadStateMenu.add(quickLoadSeparator);

        newestSlotMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, 0));
        newestSlotMenuItem.setMnemonic('N');
        newestSlotMenuItem.setText("Newest Slot");
        quickLoadStateMenu.add(newestSlotMenuItem);

        fileMenu.add(quickLoadStateMenu);

        quickSaveStateMenu.setMnemonic('v');
        quickSaveStateMenu.setText("Quick Save State");
        quickSaveStateMenu.setEnabled(false);
        quickSaveStateMenu.add(quickSaveSeparator);

        oldestSlotMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, java.awt.event.InputEvent.SHIFT_MASK));
        oldestSlotMenuItem.setMnemonic('O');
        oldestSlotMenuItem.setText("Oldest Slot");
        quickSaveStateMenu.add(oldestSlotMenuItem);

        fileMenu.add(quickSaveStateMenu);
        fileMenu.add(separator8);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        machineMenu.setMnemonic('M');
        machineMenu.setText("Machine");
        machineMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                machineMenuMenuSelected(evt);
            }
        });

        resetMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        resetMenuItem.setMnemonic('R');
        resetMenuItem.setText("Reset");
        resetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetMenuItemActionPerformed(evt);
            }
        });
        machineMenu.add(resetMenuItem);

        powerCycleMenuItem.setMnemonic('C');
        powerCycleMenuItem.setText("Power Cycle");
        powerCycleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                powerCycleMenuItemActionPerformed(evt);
            }
        });
        machineMenu.add(powerCycleMenuItem);
        machineMenu.add(jSeparator1);

        pauseMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.SHIFT_MASK));
        pauseMenuItem.setMnemonic('P');
        pauseMenuItem.setText("Pause");
        pauseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseMenuItemActionPerformed(evt);
            }
        });
        machineMenu.add(pauseMenuItem);

        nextFrameMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.SHIFT_MASK));
        nextFrameMenuItem.setMnemonic('N');
        nextFrameMenuItem.setText("Next Frame");
        nextFrameMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextFrameMenuItemActionPerformed(evt);
            }
        });
        machineMenu.add(nextFrameMenuItem);

        speedMenu.setMnemonic('S');
        speedMenu.setText("Speed");
        speedMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                speedMenuMenuSelected(evt);
            }
        });
        machineMenu.add(speedMenu);
        machineMenu.add(jSeparator5);

        tvSystemMenu.setMnemonic('T');
        tvSystemMenu.setText("TV System");
        tvSystemMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                tvSystemMenuMenuSelected(evt);
            }
        });

        regionButtonGroup.add(ntscRegionRadioButtonMenuItem);
        ntscRegionRadioButtonMenuItem.setText("NTSC");
        ntscRegionRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ntscRegionRadioButtonMenuItemActionPerformed(evt);
            }
        });
        tvSystemMenu.add(ntscRegionRadioButtonMenuItem);

        regionButtonGroup.add(palRegionRadioButtonMenuItem);
        palRegionRadioButtonMenuItem.setText("PAL");
        palRegionRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                palRegionRadioButtonMenuItemActionPerformed(evt);
            }
        });
        tvSystemMenu.add(palRegionRadioButtonMenuItem);

        regionButtonGroup.add(dendyRegionRadioButtonMenuItem);
        dendyRegionRadioButtonMenuItem.setText("Dendy");
        dendyRegionRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dendyRegionRadioButtonMenuItemActionPerformed(evt);
            }
        });
        tvSystemMenu.add(dendyRegionRadioButtonMenuItem);

        machineMenu.add(tvSystemMenu);
        machineMenu.add(jSeparator20);

        insertDiskMenu.setMnemonic('D');
        insertDiskMenu.setText("Insert Disk");
        machineMenu.add(insertDiskMenu);

        flipDiskSideMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.SHIFT_MASK));
        flipDiskSideMenuItem.setMnemonic('F');
        flipDiskSideMenuItem.setText("Flip Disk Side");
        flipDiskSideMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flipDiskSideMenuItemActionPerformed(evt);
            }
        });
        machineMenu.add(flipDiskSideMenuItem);

        ejectDiskMenuItem.setMnemonic('E');
        ejectDiskMenuItem.setText("Eject Disk");
        ejectDiskMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ejectDiskMenuItemActionPerformed(evt);
            }
        });
        machineMenu.add(ejectDiskMenuItem);
        machineMenu.add(jSeparator13);

        familyBasicMenu.setMnemonic('B');
        familyBasicMenu.setText("Family BASIC");
        familyBasicMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                familyBasicMenuMenuSelected(evt);
            }
        });

        familyBasicTapeButtonGroup.add(familyBasicPlayTapeMenuItem);
        familyBasicPlayTapeMenuItem.setText("Play Tape");
        familyBasicPlayTapeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicPlayTapeMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicPlayTapeMenuItem);

        familyBasicTapeButtonGroup.add(familyBasicRecordTapeMenuItem);
        familyBasicRecordTapeMenuItem.setText("Record Tape");
        familyBasicRecordTapeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicRecordTapeMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicRecordTapeMenuItem);

        familyBasicTapeButtonGroup.add(familyBasicStopTapeMenuItem);
        familyBasicStopTapeMenuItem.setSelected(true);
        familyBasicStopTapeMenuItem.setText("Stop Tape");
        familyBasicStopTapeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicStopTapeMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicStopTapeMenuItem);
        familyBasicMenu.add(jSeparator8);

        familyBasicLoadTapeMenuItem.setText("Load Tape...");
        familyBasicLoadTapeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicLoadTapeMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicLoadTapeMenuItem);

        familyBasicSaveTapeMenuItem.setText("Save Tape...");
        familyBasicSaveTapeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicSaveTapeMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicSaveTapeMenuItem);

        familyBasicEraseTapeMenuItem.setText("Erase Tape");
        familyBasicEraseTapeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicEraseTapeMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicEraseTapeMenuItem);
        familyBasicMenu.add(jSeparator9);

        familyBasicCopyProgramMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        familyBasicCopyProgramMenuItem.setMnemonic('C');
        familyBasicCopyProgramMenuItem.setText("Copy Program");
        familyBasicCopyProgramMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicCopyProgramMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicCopyProgramMenuItem);

        familyBasicPasteProgramMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        familyBasicPasteProgramMenuItem.setMnemonic('P');
        familyBasicPasteProgramMenuItem.setText("Paste Program");
        familyBasicPasteProgramMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicPasteProgramMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicPasteProgramMenuItem);

        familyBasicTypePasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        familyBasicTypePasteMenuItem.setMnemonic('T');
        familyBasicTypePasteMenuItem.setText("Type Paste");
        familyBasicTypePasteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicTypePasteMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicTypePasteMenuItem);
        familyBasicMenu.add(jSeparator6);

        familyBasicLoadProgramMenuItem.setMnemonic('L');
        familyBasicLoadProgramMenuItem.setText("Load Program...");
        familyBasicLoadProgramMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicLoadProgramMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicLoadProgramMenuItem);

        familyBasicSaveProgramMenuItem.setMnemonic('S');
        familyBasicSaveProgramMenuItem.setText("Save Program...");
        familyBasicSaveProgramMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicSaveProgramMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicSaveProgramMenuItem);
        familyBasicMenu.add(jSeparator7);

        familyBasicEditBackgroundMenuItem.setMnemonic('E');
        familyBasicEditBackgroundMenuItem.setText("Edit Background...");
        familyBasicEditBackgroundMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicEditBackgroundMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicEditBackgroundMenuItem);

        familyBasicLoadBackgroundMenuItem.setMnemonic('d');
        familyBasicLoadBackgroundMenuItem.setText("Load Background...");
        familyBasicLoadBackgroundMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicLoadBackgroundMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicLoadBackgroundMenuItem);

        familyBasicSaveBackgroundMenuItem.setMnemonic('v');
        familyBasicSaveBackgroundMenuItem.setText("Save Background...");
        familyBasicSaveBackgroundMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicSaveBackgroundMenuItemActionPerformed(evt);
            }
        });
        familyBasicMenu.add(familyBasicSaveBackgroundMenuItem);

        machineMenu.add(familyBasicMenu);
        machineMenu.add(jSeparator3);

        insertCoinMenu.setMnemonic('I');
        insertCoinMenu.setText("Insert Coin");

        insertCoinMainLeftMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        insertCoinMainLeftMenuItem.setText("Main Left");
        insertCoinMainLeftMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertCoinMainLeftMenuItemActionPerformed(evt);
            }
        });
        insertCoinMenu.add(insertCoinMainLeftMenuItem);

        insertCoinMainRightMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        insertCoinMainRightMenuItem.setText("Main Right");
        insertCoinMainRightMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertCoinMainRightMenuItemActionPerformed(evt);
            }
        });
        insertCoinMenu.add(insertCoinMainRightMenuItem);

        insertCoinSubLeftMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, java.awt.event.InputEvent.SHIFT_MASK));
        insertCoinSubLeftMenuItem.setText("Sub Left");
        insertCoinSubLeftMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertCoinSubLeftMenuItemActionPerformed(evt);
            }
        });
        insertCoinMenu.add(insertCoinSubLeftMenuItem);

        insertCoinSubRightMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, java.awt.event.InputEvent.SHIFT_MASK));
        insertCoinSubRightMenuItem.setText("Sub Right");
        insertCoinSubRightMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertCoinSubRightMenuItemActionPerformed(evt);
            }
        });
        insertCoinMenu.add(insertCoinSubRightMenuItem);

        machineMenu.add(insertCoinMenu);

        serviceButtonMenu.setMnemonic('v');
        serviceButtonMenu.setText("Press Service Button");

        serviceButtonMainMenuItem.setText("Main");
        serviceButtonMainMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serviceButtonMainMenuItemActionPerformed(evt);
            }
        });
        serviceButtonMenu.add(serviceButtonMainMenuItem);

        serviceButtonSubMenuItem.setText("Sub");
        serviceButtonSubMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serviceButtonSubMenuItemActionPerformed(evt);
            }
        });
        serviceButtonMenu.add(serviceButtonSubMenuItem);

        machineMenu.add(serviceButtonMenu);

        dipSwitchesMenuItem.setMnemonic('w');
        dipSwitchesMenuItem.setText("DIP Switches...");
        dipSwitchesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dipSwitchesMenuItemActionPerformed(evt);
            }
        });
        machineMenu.add(dipSwitchesMenuItem);
        machineMenu.add(jSeparator17);

        screamIntoMicrophoneMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        screamIntoMicrophoneMenuItem.setMnemonic('M');
        screamIntoMicrophoneMenuItem.setText("Scream into Microphone");
        screamIntoMicrophoneMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                screamIntoMicrophoneMenuItemActionPerformed(evt);
            }
        });
        machineMenu.add(screamIntoMicrophoneMenuItem);
        machineMenu.add(jSeparator22);

        eraseBatterySaveMenuItem.setMnemonic('a');
        eraseBatterySaveMenuItem.setText("Erase Battery Save");
        eraseBatterySaveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eraseBatterySaveMenuItemActionPerformed(evt);
            }
        });
        machineMenu.add(eraseBatterySaveMenuItem);

        hotSwapMenuItem.setMnemonic('H');
        hotSwapMenuItem.setText("Hot Swap...");
        hotSwapMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hotSwapMenuItemActionPerformed(evt);
            }
        });
        machineMenu.add(hotSwapMenuItem);

        glitchMenuItem.setMnemonic('G');
        glitchMenuItem.setText("Glitch");
        glitchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                glitchMenuItemActionPerformed(evt);
            }
        });
        machineMenu.add(glitchMenuItem);

        menuBar.add(machineMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText("View");
        viewMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                viewMenuMenuSelected(evt);
            }
        });

        fileInfoMenuItem.setMnemonic('I');
        fileInfoMenuItem.setText("File Info...");
        fileInfoMenuItem.setEnabled(false);
        fileInfoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileInfoMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(fileInfoMenuItem);
        viewMenu.add(separator15);

        barcodeBattlerMenuItem.setMnemonic('B');
        barcodeBattlerMenuItem.setText("Barcode Battler II...");
        barcodeBattlerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                barcodeBattlerMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(barcodeBattlerMenuItem);

        famicom3dGlassesMenuItem.setMnemonic('G');
        famicom3dGlassesMenuItem.setText("Famicom 3D Glasses...");
        famicom3dGlassesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                famicom3dGlassesMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(famicom3dGlassesMenuItem);

        robMenuItem.setMnemonic('R');
        robMenuItem.setText("R.O.B...");
        robMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                robMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(robMenuItem);

        subMonitorMenuItem.setMnemonic('n');
        subMonitorMenuItem.setText("Sub Monitor...");
        subMonitorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subMonitorMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(subMonitorMenuItem);
        viewMenu.add(separator5);

        showMenu.setMnemonic('w');
        showMenu.setText("Show");
        showMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                showMenuMenuSelected(evt);
            }
        });

        statusMessagesCheckBoxMenuItem.setMnemonic('M');
        statusMessagesCheckBoxMenuItem.setSelected(true);
        statusMessagesCheckBoxMenuItem.setText("Status Messages");
        statusMessagesCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusMessagesCheckBoxMenuItemActionPerformed(evt);
            }
        });
        showMenu.add(statusMessagesCheckBoxMenuItem);

        fpsCheckBoxMenuItem.setMnemonic('F');
        fpsCheckBoxMenuItem.setSelected(true);
        fpsCheckBoxMenuItem.setText("Displayed/Generated FPS");
        fpsCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fpsCheckBoxMenuItemActionPerformed(evt);
            }
        });
        showMenu.add(fpsCheckBoxMenuItem);

        inputDevicesCheckBoxMenuItem.setMnemonic('I');
        inputDevicesCheckBoxMenuItem.setText("Input Devices");
        inputDevicesCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputDevicesCheckBoxMenuItemActionPerformed(evt);
            }
        });
        showMenu.add(inputDevicesCheckBoxMenuItem);
        showMenu.add(jSeparator19);

        backgroundCheckBoxMenuItem.setMnemonic('B');
        backgroundCheckBoxMenuItem.setSelected(true);
        backgroundCheckBoxMenuItem.setText("Background");
        backgroundCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundCheckBoxMenuItemActionPerformed(evt);
            }
        });
        showMenu.add(backgroundCheckBoxMenuItem);

        spritesCheckBoxMenuItem.setMnemonic('S');
        spritesCheckBoxMenuItem.setSelected(true);
        spritesCheckBoxMenuItem.setText("Sprites");
        spritesCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spritesCheckBoxMenuItemActionPerformed(evt);
            }
        });
        showMenu.add(spritesCheckBoxMenuItem);

        spriteBoxesCheckBoxMenuItem.setMnemonic('x');
        spriteBoxesCheckBoxMenuItem.setText("Sprite Bounding Boxes");
        spriteBoxesCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spriteBoxesCheckBoxMenuItemActionPerformed(evt);
            }
        });
        showMenu.add(spriteBoxesCheckBoxMenuItem);

        noSpriteLimitCheckBoxMenuItem.setMnemonic('8');
        noSpriteLimitCheckBoxMenuItem.setSelected(true);
        noSpriteLimitCheckBoxMenuItem.setText("More than 8 sprites per scanline");
        noSpriteLimitCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noSpriteLimitCheckBoxMenuItemActionPerformed(evt);
            }
        });
        showMenu.add(noSpriteLimitCheckBoxMenuItem);

        viewMenu.add(showMenu);

        screenSizeMenu.setMnemonic('S');
        screenSizeMenu.setText("Screen Size");

        screenSize1XMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.ALT_MASK));
        screenSize1XMenuItem.setMnemonic('1');
        screenSize1XMenuItem.setText("1X");
        screenSize1XMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                screenSize1XMenuItemActionPerformed(evt);
            }
        });
        screenSizeMenu.add(screenSize1XMenuItem);

        screenSize2XMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.ALT_MASK));
        screenSize2XMenuItem.setMnemonic('2');
        screenSize2XMenuItem.setText("2X");
        screenSize2XMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                screenSize2XMenuItemActionPerformed(evt);
            }
        });
        screenSizeMenu.add(screenSize2XMenuItem);

        screenSize3XMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.ALT_MASK));
        screenSize3XMenuItem.setMnemonic('3');
        screenSize3XMenuItem.setText("3X");
        screenSize3XMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                screenSize3XMenuItemActionPerformed(evt);
            }
        });
        screenSizeMenu.add(screenSize3XMenuItem);

        screenSize4XMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, java.awt.event.InputEvent.ALT_MASK));
        screenSize4XMenuItem.setMnemonic('4');
        screenSize4XMenuItem.setText("4X");
        screenSize4XMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                screenSize4XMenuItemActionPerformed(evt);
            }
        });
        screenSizeMenu.add(screenSize4XMenuItem);

        screenSize5XMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_5, java.awt.event.InputEvent.ALT_MASK));
        screenSize5XMenuItem.setMnemonic('5');
        screenSize5XMenuItem.setText("5X");
        screenSize5XMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                screenSize5XMenuItemActionPerformed(evt);
            }
        });
        screenSizeMenu.add(screenSize5XMenuItem);

        screenSizeMaxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, java.awt.event.InputEvent.ALT_MASK));
        screenSizeMaxMenuItem.setMnemonic('M');
        screenSizeMaxMenuItem.setText("Max");
        screenSizeMaxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                screenSizeMaxMenuItemActionPerformed(evt);
            }
        });
        screenSizeMenu.add(screenSizeMaxMenuItem);
        screenSizeMenu.add(separator16);

        tvAspectCheckBoxMenuItem.setMnemonic('T');
        tvAspectCheckBoxMenuItem.setText("TV Aspect");
        tvAspectCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tvAspectCheckBoxMenuItemActionPerformed(evt);
            }
        });
        screenSizeMenu.add(tvAspectCheckBoxMenuItem);

        smoothScalingCheckBoxMenuItem.setMnemonic('S');
        smoothScalingCheckBoxMenuItem.setText("Smooth Scaling");
        smoothScalingCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smoothScalingCheckBoxMenuItemActionPerformed(evt);
            }
        });
        screenSizeMenu.add(smoothScalingCheckBoxMenuItem);

        uniformPixelScalingCheckBoxMenuItem.setMnemonic('p');
        uniformPixelScalingCheckBoxMenuItem.setText("Uniform-pixel Scaling");
        uniformPixelScalingCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uniformPixelScalingCheckBoxMenuItemActionPerformed(evt);
            }
        });
        screenSizeMenu.add(uniformPixelScalingCheckBoxMenuItem);

        underscanCheckBoxMenuItem.setMnemonic('u');
        underscanCheckBoxMenuItem.setText("Underscan");
        underscanCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                underscanCheckBoxMenuItemActionPerformed(evt);
            }
        });
        screenSizeMenu.add(underscanCheckBoxMenuItem);

        viewMenu.add(screenSizeMenu);

        videoFilterMenu.setMnemonic('V');
        videoFilterMenu.setText("Video Filter");
        viewMenu.add(videoFilterMenu);

        paletteMenu.setMnemonic('P');
        paletteMenu.setText("Palette");
        viewMenu.add(paletteMenu);

        lookAndFeelMenu.setMnemonic('L');
        lookAndFeelMenu.setText("Look and Feel");
        viewMenu.add(lookAndFeelMenu);

        fontSizeMenuItem.setMnemonic('o');
        fontSizeMenuItem.setText("Font Size...");
        fontSizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(fontSizeMenuItem);
        viewMenu.add(separator7);

        hideMenuBarMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0));
        hideMenuBarMenuItem.setMnemonic('H');
        hideMenuBarMenuItem.setText("Hide Menu Bar");
        hideMenuBarMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideMenuBarMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(hideMenuBarMenuItem);

        fullscreenMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, java.awt.event.InputEvent.ALT_MASK));
        fullscreenMenuItem.setMnemonic('F');
        fullscreenMenuItem.setText("Fullscreen");
        fullscreenMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullscreenMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(fullscreenMenuItem);

        menuBar.add(viewMenu);

        optionsMenu.setMnemonic('O');
        optionsMenu.setText("Options");
        optionsMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                optionsMenuMenuSelected(evt);
            }
        });

        buttonMappingMenuItem.setMnemonic('B');
        buttonMappingMenuItem.setText("Button Mapping...");
        buttonMappingMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMappingMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(buttonMappingMenuItem);

        portsMenuItem.setMnemonic('P');
        portsMenuItem.setText("Ports...");
        portsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(portsMenuItem);

        inputSettingsMenuItem.setMnemonic('I');
        inputSettingsMenuItem.setText("Input Settings...");
        inputSettingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputSettingsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(inputSettingsMenuItem);
        optionsMenu.add(jSeparator12);

        soundMenuItem.setMnemonic('S');
        soundMenuItem.setText("Sound...");
        soundMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                soundMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(soundMenuItem);

        volumeMixerMenuItem.setMnemonic('V');
        volumeMixerMenuItem.setText("Volume Mixer...");
        volumeMixerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                volumeMixerMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(volumeMixerMenuItem);

        nsfOptionsMenuItem.setMnemonic('N');
        nsfOptionsMenuItem.setText("NSF...");
        nsfOptionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nsfOptionsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(nsfOptionsMenuItem);
        optionsMenu.add(jSeparator15);

        contentDirectoryMenuItem.setMnemonic('C');
        contentDirectoryMenuItem.setText("Content Directory...");
        contentDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contentDirectoryMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(contentDirectoryMenuItem);

        archiveFileOptionsMenuItem.setMnemonic('A');
        archiveFileOptionsMenuItem.setText("Archive Files...");
        archiveFileOptionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                archiveFileOptionsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(archiveFileOptionsMenuItem);

        famicomDiskSystemOptionsMenuItem.setMnemonic('F');
        famicomDiskSystemOptionsMenuItem.setText("Famicom Disk System...");
        famicomDiskSystemOptionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                famicomDiskSystemOptionsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(famicomDiskSystemOptionsMenuItem);

        familyBasicOptionsMenuItem.setMnemonic('m');
        familyBasicOptionsMenuItem.setText("Family BASIC...");
        familyBasicOptionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                familyBasicOptionsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(familyBasicOptionsMenuItem);

        traceLoggerOptionsMenuItem.setMnemonic('T');
        traceLoggerOptionsMenuItem.setText("Trace Logger...");
        traceLoggerOptionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                traceLoggerOptionsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(traceLoggerOptionsMenuItem);
        optionsMenu.add(jSeparator18);

        userInterfaceMenuItem.setMnemonic('U');
        userInterfaceMenuItem.setText("User Interface...");
        userInterfaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userInterfaceMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(userInterfaceMenuItem);

        screenshotOptionsMenuItem.setMnemonic('c');
        screenshotOptionsMenuItem.setText("Screenshots...");
        screenshotOptionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                screenshotOptionsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(screenshotOptionsMenuItem);

        overscanMenuItem.setMnemonic('O');
        overscanMenuItem.setText("Overscan...");
        overscanMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overscanMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(overscanMenuItem);

        palettesMenuItem.setMnemonic('P');
        palettesMenuItem.setText("Palettes...");
        palettesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                palettesMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(palettesMenuItem);

        menuBar.add(optionsMenu);

        toolsMenu.setMnemonic('T');
        toolsMenu.setText("Tools");
        toolsMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                toolsMenuMenuSelected(evt);
            }
        });

        startNetplayServerMenuItem.setMnemonic('N');
        startNetplayServerMenuItem.setText("Start Netplay Server...");
        startNetplayServerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startNetplayServerMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(startNetplayServerMenuItem);

        connectToNetplayServerMenuItem.setMnemonic('C');
        connectToNetplayServerMenuItem.setText("Connect to Netplay Server...");
        connectToNetplayServerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectToNetplayServerMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(connectToNetplayServerMenuItem);
        toolsMenu.add(jSeparator4);

        mapMakerMenuItem.setMnemonic('M');
        mapMakerMenuItem.setText("Map Maker...");
        mapMakerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapMakerMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(mapMakerMenuItem);

        spriteSaverMenuItem.setMnemonic('v');
        spriteSaverMenuItem.setText("Sprite Saver...");
        spriteSaverMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spriteSaverMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(spriteSaverMenuItem);
        toolsMenu.add(jSeparator11);

        runProgramMenuItem.setMnemonic('R');
        runProgramMenuItem.setText("Run Program...");
        runProgramMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runProgramMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(runProgramMenuItem);

        startProgramServerMenuItem.setMnemonic('P');
        startProgramServerMenuItem.setText("Start Program Server...");
        startProgramServerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startProgramServerMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(startProgramServerMenuItem);
        toolsMenu.add(jSeparator14);

        manageCheatsMenuItem.setMnemonic('g');
        manageCheatsMenuItem.setText("Manage Cheats...");
        manageCheatsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageCheatsMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(manageCheatsMenuItem);

        searchCheatsMenuItem.setMnemonic('S');
        searchCheatsMenuItem.setText("Cheat Search...");
        searchCheatsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchCheatsMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(searchCheatsMenuItem);
        toolsMenu.add(jSeparator2);

        editFileHeaderMenuItem.setMnemonic('H');
        editFileHeaderMenuItem.setText("Edit File Header...");
        editFileHeaderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editFileHeaderMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(editFileHeaderMenuItem);
        toolsMenu.add(jSeparator16);

        saveScreenshotMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0));
        saveScreenshotMenuItem.setText("Save Screenshot");
        saveScreenshotMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveScreenshotMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(saveScreenshotMenuItem);
        toolsMenu.add(jSeparator10);

        applyIPSMenuItem.setText("Apply IPS Patch...");
        applyIPSMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyIPSMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(applyIPSMenuItem);

        createIPSMenuItem.setText("Create IPS Patch...");
        createIPSMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createIPSMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(createIPSMenuItem);
        toolsMenu.add(jSeparator21);

        trackHistoryCheckBoxMenuItem.setMnemonic('H');
        trackHistoryCheckBoxMenuItem.setText("Track History");
        trackHistoryCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackHistoryCheckBoxMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(trackHistoryCheckBoxMenuItem);

        watchHistoryMenuItem.setMnemonic('W');
        watchHistoryMenuItem.setText("Watch History...");
        watchHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                watchHistoryMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(watchHistoryMenuItem);

        exportVideoAudioMenuItem.setMnemonic('V');
        exportVideoAudioMenuItem.setText("Export Video/Audio...");
        exportVideoAudioMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportVideoAudioMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(exportVideoAudioMenuItem);

        editHistoryMenuItem.setMnemonic('E');
        editHistoryMenuItem.setText("Edit History...");
        editHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editHistoryMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(editHistoryMenuItem);

        clearHistoryMenuItem.setMnemonic('a');
        clearHistoryMenuItem.setText("Clear History");
        clearHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearHistoryMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(clearHistoryMenuItem);

        importHistoryMenuItem.setMnemonic('I');
        importHistoryMenuItem.setText("Import History...");
        importHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importHistoryMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(importHistoryMenuItem);

        exportHistoryMenuItem.setMnemonic('x');
        exportHistoryMenuItem.setText("Export History...");
        exportHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportHistoryMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(exportHistoryMenuItem);

        rewindTimeCheckBoxMenuItem.setMnemonic('T');
        rewindTimeCheckBoxMenuItem.setText("Rewind Time");
        rewindTimeCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rewindTimeCheckBoxMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(rewindTimeCheckBoxMenuItem);

        menuBar.add(toolsMenu);

        debugMenu.setMnemonic('D');
        debugMenu.setText("Debug");
        debugMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                debugMenuMenuSelected(evt);
            }
        });

        debuggerMenuItem.setMnemonic('D');
        debuggerMenuItem.setText("Debugger...");
        debuggerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debuggerMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(debuggerMenuItem);

        oamDataMenuItem.setMnemonic('O');
        oamDataMenuItem.setText("OAM Data...");
        oamDataMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                oamDataMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(oamDataMenuItem);

        patternTablesMenuItem.setMnemonic('P');
        patternTablesMenuItem.setText("Pattern Tables...");
        patternTablesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                patternTablesMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(patternTablesMenuItem);

        nametablesMenuItem.setMnemonic('N');
        nametablesMenuItem.setText("Nametables...");
        nametablesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nametablesMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(nametablesMenuItem);

        hexEditorMenuItem.setMnemonic('H');
        hexEditorMenuItem.setText("Hex Editor...");
        hexEditorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hexEditorMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(hexEditorMenuItem);

        asmDasmMenuItem.setMnemonic('A');
        asmDasmMenuItem.setText("Assembler-Disassembler...");
        asmDasmMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                asmDasmMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(asmDasmMenuItem);

        ramSearchMenuItem.setMnemonic('R');
        ramSearchMenuItem.setText("RAM Search...");
        ramSearchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ramSearchMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(ramSearchMenuItem);

        ramWatchMenuItem.setMnemonic('W');
        ramWatchMenuItem.setText("RAM Watch...");
        ramWatchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ramWatchMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(ramWatchMenuItem);
        debugMenu.add(jSeparator23);

        startTraceLoggerMenuItem.setMnemonic('L');
        startTraceLoggerMenuItem.setText("Start Trace Logger");
        startTraceLoggerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTraceLoggerMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(startTraceLoggerMenuItem);

        menuBar.add(debugMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");

        helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        helpMenuItem.setMnemonic('H');
        helpMenuItem.setText("Help...");
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuItem);

        licenseMenuItem.setMnemonic('L');
        licenseMenuItem.setText("License...");
        licenseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                licenseMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(licenseMenuItem);
        helpMenu.add(separator2);

        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.setText("About...");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 279, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        final AppMode appMode = App.getAppMode();
        if (appMode == AppMode.Default || appMode == AppMode.NetplayServer) {
            openFile(null, false, null);
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        if (machine != null || timeRewinding
                || App.getAppMode() == AppMode.WatchHistory) {
            App.close();
            if (App.getAppMode() == AppMode.NetplayClient) {
                App.destroyNetplayClientFrame();
            }
        }
    }//GEN-LAST:event_closeMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        exit();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        exit();
    }//GEN-LAST:event_formWindowClosing

    private void recentFilesClearMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentFilesClearMenuItemActionPerformed
        AppPrefs.getInstance().getPaths().clearRecentFiles();
        createRecentFilesMenu();
        AppPrefs.save();
    }//GEN-LAST:event_recentFilesClearMenuItemActionPerformed

    private void recentFilesLockCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentFilesLockCheckBoxMenuItemActionPerformed
        AppPrefs.getInstance().getPaths().setLockRecentFiles(
                recentFilesLockCheckBoxMenuItem.isSelected());
        AppPrefs.save();
    }//GEN-LAST:event_recentFilesLockCheckBoxMenuItemActionPerformed

    private void recentDirectoriesLockCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentDirectoriesLockCheckBoxMenuItemActionPerformed
        AppPrefs.getInstance().getPaths().setLockRecentDirectories(
                recentDirectoriesLockCheckBoxMenuItem.isSelected());
        AppPrefs.save();
    }//GEN-LAST:event_recentDirectoriesLockCheckBoxMenuItemActionPerformed

    private void recentDirectoriesClearMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentDirectoriesClearMenuItemActionPerformed
        AppPrefs.getInstance().getPaths().clearRecentDirectories();
        AppPrefs.save();
    }//GEN-LAST:event_recentDirectoriesClearMenuItemActionPerformed

    private void saveStateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveStateMenuItemActionPerformed
        if (isSaveable() && App.getAppMode() != AppMode.NetplayClient) {
            saveOrOpenState(JFileChooser.SAVE_DIALOG);
        }
    }//GEN-LAST:event_saveStateMenuItemActionPerformed

    private void loadStateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadStateMenuItemActionPerformed
        if (isSaveable() && App.getAppMode() != AppMode.NetplayClient) {
            saveOrOpenState(JFileChooser.OPEN_DIALOG);
        }
    }//GEN-LAST:event_loadStateMenuItemActionPerformed

    private void fileInfoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileInfoMenuItemActionPerformed
        App.setNoStepPause(true);
        new TextDialog(this, fileInfo, "File Information").setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_fileInfoMenuItemActionPerformed

    private void fullscreenMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullscreenMenuItemActionPerformed
        toggleFullscreenMode();
    }//GEN-LAST:event_fullscreenMenuItemActionPerformed

    private void editFileHeaderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editFileHeaderMenuItemActionPerformed
        openFile(null, true, null);
    }//GEN-LAST:event_editFileHeaderMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        App.setNoStepPause(true);
        AboutDialog aboutDialog = new AboutDialog(this, true);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void fontSizeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeMenuItemActionPerformed
        App.setNoStepPause(true);
        final View view = AppPrefs.getInstance().getView();
        final FontSizeDialog fontSizeDialog = new FontSizeDialog(this, true);
        fontSizeDialog.setFontScale(view.getFontScale());
        fontSizeDialog.setVisible(true);
        if (fontSizeDialog.isFontScaleChanged()) {
            view.setFontScale(fontSizeDialog.getFontScale());
            updateFrameStyles();
            AppPrefs.save();
        }
        App.setNoStepPause(false);
    }//GEN-LAST:event_fontSizeMenuItemActionPerformed

    private void buttonMappingMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMappingMenuItemActionPerformed
        App.setNoStepPause(true);
        new ButtonMappingDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_buttonMappingMenuItemActionPerformed

    private void hexEditorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hexEditorMenuItemActionPerformed
        App.createHexEditorFrame();
    }//GEN-LAST:event_hexEditorMenuItemActionPerformed

    private void manageCheatsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageCheatsMenuItemActionPerformed
        App.setNoStepPause(true);
        new CheatsDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_manageCheatsMenuItemActionPerformed

    private void searchCheatsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchCheatsMenuItemActionPerformed
        App.createCheatSearchFrame();
    }//GEN-LAST:event_searchCheatsMenuItemActionPerformed

    private void pauseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseMenuItemActionPerformed
        if (isPauseable()) {
            pause();
        }
    }//GEN-LAST:event_pauseMenuItemActionPerformed

    private void nextFrameMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextFrameMenuItemActionPerformed
        final AppMode appMode = App.getAppMode();
        if (machine != null && displayingImagePane
                && appMode != AppMode.WatchHistory && appMode != AppMode.HistoryEditor
                && appMode != AppMode.NetplayClient && pauseMenuItem.isSelected()) {
            nextFrame();
        }
    }//GEN-LAST:event_nextFrameMenuItemActionPerformed

    private void resetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetMenuItemActionPerformed
        if (machine != null && displayingImagePane
                && App.getAppMode() != AppMode.WatchHistory) {
            if (AppPrefs.getInstance().getUserInterfacePrefs().isConfirmReset()) {
                App.setNoStepPause(true);
                final YesNoDialog yesNoDialog = new YesNoDialog(this,
                        "Do you really want to reset?", "Confirm Reset");
                yesNoDialog.setVisible(true);
                App.setNoStepPause(false);
                if (yesNoDialog.isNo()) {
                    return;
                }
            }
            reset();
        }
    }//GEN-LAST:event_resetMenuItemActionPerformed

    private void powerCycleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_powerCycleMenuItemActionPerformed
        if (AppPrefs.getInstance().getUserInterfacePrefs().isConfirmReset()) {
            App.setNoStepPause(true);
            final YesNoDialog yesNoDialog = new YesNoDialog(this,
                    "Do you really want to power cycle?", "Confirm Power Cycle");
            yesNoDialog.setVisible(true);
            App.setNoStepPause(false);
            if (yesNoDialog.isNo()) {
                return;
            }
        }
        App.powerCycle();
    }//GEN-LAST:event_powerCycleMenuItemActionPerformed

    private void patternTablesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_patternTablesMenuItemActionPerformed
        App.createPatternTablesFrame();
    }//GEN-LAST:event_patternTablesMenuItemActionPerformed

    private void nametablesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nametablesMenuItemActionPerformed
        App.createNametablesFrame();
    }//GEN-LAST:event_nametablesMenuItemActionPerformed

    private void mapMakerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mapMakerMenuItemActionPerformed
        App.createMapMakerFrame();
    }//GEN-LAST:event_mapMakerMenuItemActionPerformed

    private void spriteSaverMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spriteSaverMenuItemActionPerformed
        App.createSpriteSaverFrame();
    }//GEN-LAST:event_spriteSaverMenuItemActionPerformed

    private void ramSearchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ramSearchMenuItemActionPerformed
        App.createRamSearchFrame();
    }//GEN-LAST:event_ramSearchMenuItemActionPerformed

    private void ramWatchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ramWatchMenuItemActionPerformed
        App.createRamWatchFrame();
    }//GEN-LAST:event_ramWatchMenuItemActionPerformed

    private void debuggerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debuggerMenuItemActionPerformed
        App.createDebuggerFrame();
    }//GEN-LAST:event_debuggerMenuItemActionPerformed

    private void asmDasmMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_asmDasmMenuItemActionPerformed
        App.createAsmDasmFrame();
    }//GEN-LAST:event_asmDasmMenuItemActionPerformed

    private void trackHistoryCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackHistoryCheckBoxMenuItemActionPerformed
        final boolean trackHistory = trackHistoryCheckBoxMenuItem.isSelected();
        AppPrefs.getInstance().getHistoryPrefs().setTrackHistory(trackHistory);
        AppPrefs.save();
        if (!trackHistory) {
            App.destroyWatchHistoryFrame();
        }
        App.setTrackHistory(trackHistory);
    }//GEN-LAST:event_trackHistoryCheckBoxMenuItemActionPerformed

    private void editHistoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editHistoryMenuItemActionPerformed
        App.createHistoryEditorFrame();
    }//GEN-LAST:event_editHistoryMenuItemActionPerformed

    private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_fileMenuMenuSelected
        final boolean machineExists = machine != null;
        final AppMode appMode = App.getAppMode();
        final boolean openable = appMode == AppMode.Default
                || appMode == AppMode.NetplayServer;
        final boolean saveable = isSaveable();
        final Paths paths = AppPrefs.getInstance().getPaths();

        openMenuItem.setEnabled(openable);
        openRecentFileMenu.setEnabled(!paths.getRecentFiles().isEmpty()
                && openable);
        openRecentArchiveMenu.setEnabled(!paths.getRecentArchives().isEmpty()
                && openable);
        openRecentDirectoryMenu.setEnabled(!paths.getRecentDirectories().isEmpty()
                && openable);

        quickLoadStateMenu.setEnabled(saveable);
        quickSaveStateMenu.setEnabled(saveable);
        loadStateMenuItem.setEnabled(saveable && appMode != AppMode.NetplayClient);
        saveStateMenuItem.setEnabled(saveable && appMode != AppMode.NetplayClient);
        closeMenuItem.setEnabled(machineExists || timeRewinding
                || appMode == AppMode.WatchHistory);
    }//GEN-LAST:event_fileMenuMenuSelected

    private void openRecentDirectoryMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_openRecentDirectoryMenuMenuSelected
        createRecentDirectoriesMenu();
    }//GEN-LAST:event_openRecentDirectoryMenuMenuSelected

    private void portsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portsMenuItemActionPerformed
        App.setNoStepPause(true);
        new PortsDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_portsMenuItemActionPerformed

    private void inputSettingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputSettingsMenuItemActionPerformed
        App.setNoStepPause(true);
        new InputSettingsDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_inputSettingsMenuItemActionPerformed

    private void rewindTimeCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rewindTimeCheckBoxMenuItemActionPerformed
        App.requestRewindTime(4, rewindTimeCheckBoxMenuItem.isSelected());
    }//GEN-LAST:event_rewindTimeCheckBoxMenuItemActionPerformed

    private void startNetplayServerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startNetplayServerMenuItemActionPerformed
        App.createNetplayServerFrame();
    }//GEN-LAST:event_startNetplayServerMenuItemActionPerformed

    private void connectToNetplayServerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectToNetplayServerMenuItemActionPerformed
        App.createNetplayClientFrame();
    }//GEN-LAST:event_connectToNetplayServerMenuItemActionPerformed

    private void familyBasicTypePasteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicTypePasteMenuItemActionPerformed
        InputUtil.addOtherInput(new TypePaste(getClipboardString()));
    }//GEN-LAST:event_familyBasicTypePasteMenuItemActionPerformed

    private void familyBasicPasteProgramMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicPasteProgramMenuItemActionPerformed
        InputUtil.addOtherInput(new PasteProgram(getClipboardString()));
    }//GEN-LAST:event_familyBasicPasteProgramMenuItemActionPerformed

    private void familyBasicCopyProgramMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicCopyProgramMenuItemActionPerformed
        App.setNoStepPause(true);
        setClipboardString(FamilyBasicUtil.copyProgram());
        App.setNoStepPause(false);
    }//GEN-LAST:event_familyBasicCopyProgramMenuItemActionPerformed

    private void familyBasicLoadProgramMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicLoadProgramMenuItemActionPerformed
        App.setNoStepPause(true);
        final JFileChooser chooser = createFileChooser("Load BASIC Program",
                AppPrefs.getInstance().getPaths().getBasicDir(),
                BasicFileExtensionFilters);
        if (showOpenDialog(this, chooser, (p, d) -> p.setBasicDir(d))
                == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = chooser.getSelectedFile();
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> loadBasicProgram(selectedFile, pleaseWaitDialog))
                    .start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }//GEN-LAST:event_familyBasicLoadProgramMenuItemActionPerformed

    private void familyBasicSaveProgramMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicSaveProgramMenuItemActionPerformed
        App.setNoStepPause(true);

        final AppPrefs prefs = AppPrefs.getInstance();
        final Paths paths = prefs.getPaths();
        final File file = showSaveAsDialog(this, paths.getBasicDir(),
                basicProgramFile == null ? null : basicProgramFile.getName(), "bas",
                BasicFileExtensionFilters[0], true);
        if (file != null) {
            final String dir = file.getParent();
            paths.addRecentDirectory(dir);
            paths.setBasicDir(dir);
            AppPrefs.save();

            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> saveBasicProgram(file, pleaseWaitDialog)).start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }//GEN-LAST:event_familyBasicSaveProgramMenuItemActionPerformed

    private void familyBasicLoadBackgroundMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicLoadBackgroundMenuItemActionPerformed
        BackgroundEditorFrame.open(this);
    }//GEN-LAST:event_familyBasicLoadBackgroundMenuItemActionPerformed

    private void familyBasicSaveBackgroundMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicSaveBackgroundMenuItemActionPerformed
        BackgroundEditorFrame.saveAs(this);
    }//GEN-LAST:event_familyBasicSaveBackgroundMenuItemActionPerformed

    private void familyBasicEditBackgroundMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicEditBackgroundMenuItemActionPerformed
        App.createBackgroundEditorFrame();
    }//GEN-LAST:event_familyBasicEditBackgroundMenuItemActionPerformed

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        setKeyEventsEnabled(keyEventsEnabled);
        DualAPU.setMainUpdateEnabled(true);
    }//GEN-LAST:event_formWindowGainedFocus

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        enableKeyEvents();
    }//GEN-LAST:event_formWindowLostFocus

    private void familyBasicPlayTapeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicPlayTapeMenuItemActionPerformed
        InputUtil.addOtherInput(new SetDataRecorderMode(DataRecorderMode.Play));
    }//GEN-LAST:event_familyBasicPlayTapeMenuItemActionPerformed

    private void familyBasicRecordTapeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicRecordTapeMenuItemActionPerformed
        InputUtil.addOtherInput(new SetDataRecorderMode(DataRecorderMode.Record));
    }//GEN-LAST:event_familyBasicRecordTapeMenuItemActionPerformed

    private void familyBasicStopTapeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicStopTapeMenuItemActionPerformed
        InputUtil.addOtherInput(new SetDataRecorderMode(DataRecorderMode.Stop));
    }//GEN-LAST:event_familyBasicStopTapeMenuItemActionPerformed

    private void familyBasicLoadTapeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicLoadTapeMenuItemActionPerformed
        App.setNoStepPause(true);
        final JFileChooser chooser = createFileChooser("Load Tape",
                AppPrefs.getInstance().getPaths().getTapeDir(),
                TapeFileExtensionFilters);
        if (showOpenDialog(this, chooser, (p, d) -> p.setTapeDir(d))
                == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = chooser.getSelectedFile();
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> loadTape(selectedFile, pleaseWaitDialog))
                    .start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }//GEN-LAST:event_familyBasicLoadTapeMenuItemActionPerformed

    private void familyBasicSaveTapeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicSaveTapeMenuItemActionPerformed
        App.setNoStepPause(true);

        final AppPrefs prefs = AppPrefs.getInstance();
        final Paths paths = prefs.getPaths();
        final File file = showSaveAsDialog(this, paths.getTapeDir(),
                tapeFile == null ? null : tapeFile.getName(), "tape",
                TapeFileExtensionFilters[0], true);
        if (file != null) {
            final String dir = file.getParent();
            paths.addRecentDirectory(dir);
            paths.setTapeDir(dir);
            AppPrefs.save();

            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> saveTape(file, pleaseWaitDialog)).start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }//GEN-LAST:event_familyBasicSaveTapeMenuItemActionPerformed

    private void familyBasicEraseTapeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicEraseTapeMenuItemActionPerformed
        InputUtil.addOtherInput(new SetDataRecorderMode(DataRecorderMode.Erase));
    }//GEN-LAST:event_familyBasicEraseTapeMenuItemActionPerformed

    private void dipSwitchesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dipSwitchesMenuItemActionPerformed
        if (App.isVsSystem() && App.showDipSwitchesDialog() && AppPrefs
                .getInstance().getDipSwitchesAppPrefs().isResetMachine()) {
            App.reset();
        }
    }//GEN-LAST:event_dipSwitchesMenuItemActionPerformed

    private void runProgramMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runProgramMenuItemActionPerformed
        App.createProgramFrame();
    }//GEN-LAST:event_runProgramMenuItemActionPerformed

    private void startProgramServerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startProgramServerMenuItemActionPerformed
        App.createProgramServerFrame();
    }//GEN-LAST:event_startProgramServerMenuItemActionPerformed

    private void recentArchivesLockCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentArchivesLockCheckBoxMenuItemActionPerformed
        AppPrefs.getInstance().getPaths().setLockRecentArchives(
                recentArchivesLockCheckBoxMenuItem.isSelected());
        AppPrefs.save();
    }//GEN-LAST:event_recentArchivesLockCheckBoxMenuItemActionPerformed

    private void recentArchivesClearMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentArchivesClearMenuItemActionPerformed
        AppPrefs.getInstance().getPaths().clearRecentArchives();
        createRecentArchivesMenu();
        AppPrefs.save();
    }//GEN-LAST:event_recentArchivesClearMenuItemActionPerformed

    private void famicomDiskSystemOptionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_famicomDiskSystemOptionsMenuItemActionPerformed
        App.setNoStepPause(true);
        new FamicomDiskSystemOptionsDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_famicomDiskSystemOptionsMenuItemActionPerformed

    private void flipDiskSideMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flipDiskSideMenuItemActionPerformed
        final Machine mac = machine;
        if (mac != null) {
            final Mapper m = mac.getMapper();
            if (m != null && m.getDiskSideCount() > 1) {
                flipDisk();
            }
        }
    }//GEN-LAST:event_flipDiskSideMenuItemActionPerformed

    private void ejectDiskMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ejectDiskMenuItemActionPerformed
        InputUtil.addOtherInput(new EjectDisk());
    }//GEN-LAST:event_ejectDiskMenuItemActionPerformed

    private void machineMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_machineMenuMenuSelected
        final AppMode appMode = App.getAppMode();
        final Machine mac = machine;
        final boolean machineExists = mac != null && displayingImagePane
                && appMode != AppMode.WatchHistory;
        final boolean notNetworkClient = appMode != AppMode.NetplayClient;
        final boolean enabled = machineExists && appMode != AppMode.HistoryEditor
                && notNetworkClient;

        powerCycleMenuItem.setEnabled(enabled);
        pauseMenuItem.setEnabled(enabled);
        nextFrameMenuItem.setEnabled(enabled && pauseMenuItem.isSelected());
        hotSwapMenuItem.setEnabled(enabled && !mac.isVsDualSystem());
        eraseBatterySaveMenuItem.setEnabled(enabled
                && mac.getMapper().isNonVolatilePrgRamPresent());

        resetMenuItem.setEnabled(machineExists);
        speedMenu.setEnabled((App.isFileLoaded() && displayingImagePane)
                && notNetworkClient);
        tvSystemMenu.setEnabled(machineExists);
        glitchMenuItem.setEnabled(machineExists);
        screamIntoMicrophoneMenuItem.setEnabled(machineExists);

        final Mapper m = machineExists ? mac.getMapper() : null;
        final int sides = m != null ? m.getDiskSideCount() : 0;
        insertDiskMenu.setEnabled(sides > 0);
        flipDiskSideMenuItem.setEnabled(sides > 1);
        ejectDiskMenuItem.setEnabled(sides > 0);
        if (sides != diskSideMenuItems.length) {
            insertDiskMenu.removeAll();
            diskSideButtonGroup = new ButtonGroup();
            diskSideMenuItems = new JCheckBoxMenuItem[sides];
            for (int i = 0; i < sides; i++) {
                diskSideMenuItems[i] = new JCheckBoxMenuItem(String.format(
                        "Disk %d Side %s", (i >> 1) + 1, (i & 1) == 0 ? "A" : "B"));
                final int side = i;
                diskSideMenuItems[i].addActionListener(e -> InputUtil.addOtherInput(
                        new SetDiskSide(side)));
                diskSideButtonGroup.add(diskSideMenuItems[i]);
                insertDiskMenu.add(diskSideMenuItems[i]);
            }
        }
        if (sides > 0) {
            final int currentSide = m.getDiskSide();
            if (currentSide >= 0) {
                diskSideMenuItems[currentSide].setSelected(true);
            } else {
                diskSideButtonGroup.clearSelection();
                flipDiskSideMenuItem.setEnabled(false);
            }
        }

        boolean usesFamilyKeyboard = false;
        boolean usesDataRecorder = false;
        boolean vsSystem = false;
        boolean vsDualSystem = false;
        if (machineExists) {
            vsSystem = App.isVsSystem();
            vsDualSystem = App.isVsDualSystem();
            final DeviceMapper[] deviceMappers = m.getDeviceMappers();
            if (notNetworkClient && deviceMappers != null) {
                for (int i = deviceMappers.length - 1; i >= 0; i--) {
                    final int inputDevice = deviceMappers[i].getInputDevice();
                    if (inputDevice == InputDevices.Keyboard) {
                        usesFamilyKeyboard = true;
                    } else if (inputDevice == InputDevices.DataRecorder) {
                        usesDataRecorder = true;
                    }
                }
            }
        }
        familyBasicMenu.setEnabled(usesFamilyKeyboard || usesDataRecorder);
        familyBasicCopyProgramMenuItem.setEnabled(usesFamilyKeyboard);
        familyBasicPasteProgramMenuItem.setEnabled(usesFamilyKeyboard);
        familyBasicTypePasteMenuItem.setEnabled(usesFamilyKeyboard);
        familyBasicLoadProgramMenuItem.setEnabled(usesFamilyKeyboard);
        familyBasicSaveProgramMenuItem.setEnabled(usesFamilyKeyboard);
        familyBasicEditBackgroundMenuItem.setEnabled(usesFamilyKeyboard);
        familyBasicLoadBackgroundMenuItem.setEnabled(usesFamilyKeyboard);
        familyBasicSaveBackgroundMenuItem.setEnabled(usesFamilyKeyboard);
        insertCoinMenu.setEnabled(vsSystem);
        insertCoinMainLeftMenuItem.setEnabled(vsSystem);
        insertCoinMainRightMenuItem.setEnabled(vsSystem);
        insertCoinSubLeftMenuItem.setEnabled(vsDualSystem);
        insertCoinSubRightMenuItem.setEnabled(vsDualSystem);
        serviceButtonMenu.setEnabled(vsSystem);
        serviceButtonMainMenuItem.setEnabled(vsSystem);
        serviceButtonSubMenuItem.setEnabled(vsDualSystem);
        dipSwitchesMenuItem.setEnabled(vsSystem && notNetworkClient);
    }//GEN-LAST:event_machineMenuMenuSelected

    private void barcodeBattlerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barcodeBattlerMenuItemActionPerformed
        App.createBarcodeBattlerFrame();
    }//GEN-LAST:event_barcodeBattlerMenuItemActionPerformed

    private void screenSize1XMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_screenSize1XMenuItemActionPerformed
        setScreenScale(1);
    }//GEN-LAST:event_screenSize1XMenuItemActionPerformed

    private void screenSize2XMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_screenSize2XMenuItemActionPerformed
        setScreenScale(2);
    }//GEN-LAST:event_screenSize2XMenuItemActionPerformed

    private void screenSize3XMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_screenSize3XMenuItemActionPerformed
        setScreenScale(3);
    }//GEN-LAST:event_screenSize3XMenuItemActionPerformed

    private void screenSize4XMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_screenSize4XMenuItemActionPerformed
        setScreenScale(4);
    }//GEN-LAST:event_screenSize4XMenuItemActionPerformed

    private void screenSizeMaxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_screenSizeMaxMenuItemActionPerformed
        App.runSubMonitorFrame(f -> maximize(f));
        maximize(this);
    }//GEN-LAST:event_screenSizeMaxMenuItemActionPerformed

    private void tvAspectCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tvAspectCheckBoxMenuItemActionPerformed
        useTvAspectRatio = tvAspectCheckBoxMenuItem.isSelected();
        AppPrefs.getInstance().getView().setTvAspect(useTvAspectRatio);
        AppPrefs.save();
        setUseTvAspectRatio(useTvAspectRatio);
    }//GEN-LAST:event_tvAspectCheckBoxMenuItemActionPerformed

    private void screenSize5XMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_screenSize5XMenuItemActionPerformed
        setScreenScale(5);
    }//GEN-LAST:event_screenSize5XMenuItemActionPerformed

    private void hideMenuBarMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideMenuBarMenuItemActionPerformed
        final SubMonitorFrame subMonitorFrame = App.getSubMonitorFrame();
        if (subMonitorFrame != null
                && subMonitorFrame.getImagePane().getBufferStrategy() != null) {
            exitFullscreenModeTime = System.currentTimeMillis();
            subMonitorFrame.exitFullscreenMode();
        } else if (evt.getModifiers() != 0 || (System.currentTimeMillis()
                - exitFullscreenModeTime) > HIDE_MENU_BAR_DELAY) {
            exitFullscreenModeTime = 0L;
            setMenuBarVisible(false);
        }
    }//GEN-LAST:event_hideMenuBarMenuItemActionPerformed

    private void smoothScalingCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smoothScalingCheckBoxMenuItemActionPerformed
        smoothScaling = smoothScalingCheckBoxMenuItem.isSelected();
        AppPrefs.getInstance().getView().setSmoothScaling(smoothScaling);
        AppPrefs.save();
        imagePane.setSmoothScaling(smoothScaling);
    }//GEN-LAST:event_smoothScalingCheckBoxMenuItemActionPerformed

    private void soundMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_soundMenuItemActionPerformed
        App.setNoStepPause(true);
        new SoundOptionsDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_soundMenuItemActionPerformed

    private void volumeMixerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_volumeMixerMenuItemActionPerformed
        App.createVolumeMixerFrame();
    }//GEN-LAST:event_volumeMixerMenuItemActionPerformed

    private void watchHistoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_watchHistoryMenuItemActionPerformed
        showWatchHistoryFrame(false);
    }//GEN-LAST:event_watchHistoryMenuItemActionPerformed

    private void exportVideoAudioMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportVideoAudioMenuItemActionPerformed
        showWatchHistoryFrame(true);
    }//GEN-LAST:event_exportVideoAudioMenuItemActionPerformed

    private void toolsMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_toolsMenuMenuSelected

        final AppMode appMode = App.getAppMode();
        final boolean notNetplay = !(appMode == AppMode.NetplayClient
                || appMode == AppMode.NetplayServer);
        if (notNetplay) {
            startNetplayServerMenuItem.setText("Start Netplay Server...");
            connectToNetplayServerMenuItem.setText("Connect to Netplay Server...");
        } else {
            startNetplayServerMenuItem.setText("Netplay Server Controls...");
            connectToNetplayServerMenuItem.setText("Netplay Client Controls...");
        }

        final boolean machineExists = machine != null;
        if (App.getProgramFrame() != null) {
            runProgramMenuItem.setText("Program Controls...");
            startProgramServerMenuItem.setEnabled(false);
        } else {
            runProgramMenuItem.setText("Run Program...");
            startProgramServerMenuItem.setEnabled(notNetplay);
        }
        if (App.getProgramServerFrame() != null) {
            startProgramServerMenuItem.setText("Program Server Controls...");
            runProgramMenuItem.setEnabled(false);
        } else {
            startProgramServerMenuItem.setText("Start Program Server...");
            runProgramMenuItem.setEnabled(notNetplay);
        }

        mapMakerMenuItem.setEnabled(machineExists);
        spriteSaverMenuItem.setEnabled(machineExists);
        manageCheatsMenuItem.setEnabled(machineExists
                && appMode != AppMode.NetplayClient);
        searchCheatsMenuItem.setEnabled(machineExists
                && appMode != AppMode.NetplayClient);
        saveScreenshotMenuItem.setEnabled(machineExists);

        startNetplayServerMenuItem.setEnabled(appMode == AppMode.Default
                || appMode == AppMode.NetplayServer);
        connectToNetplayServerMenuItem.setEnabled(appMode == AppMode.Default
                || appMode == AppMode.NetplayClient);

        trackHistoryCheckBoxMenuItem.setEnabled(machine != null
                && appMode == AppMode.Default);
        rewindTimeCheckBoxMenuItem.setEnabled((machine != null || timeRewinding)
                && appMode != AppMode.WatchHistory
                && appMode != AppMode.NetplayClient);
        rewindTimeCheckBoxMenuItem.setSelected(timeRewinding
                && rewindTimeCheckBoxMenuItem.isEnabled());

        final boolean enabled = historyTracking && !timeRewinding
                && (appMode == AppMode.Default || appMode == AppMode.NetplayServer);
        clearHistoryMenuItem.setEnabled(enabled);
        exportHistoryMenuItem.setEnabled(enabled);

        editHistoryMenuItem.setEnabled(enabled && notNetplay
                && appMode != AppMode.WatchHistory);

        final boolean watchHistoryEnabled = appMode == AppMode.WatchHistory
                || (historyTracking && !timeRewinding && notNetplay
                && (appMode == AppMode.Default
                || appMode == AppMode.NetplayServer));
        watchHistoryMenuItem.setEnabled(watchHistoryEnabled);
        importHistoryMenuItem.setEnabled(watchHistoryEnabled);
        exportVideoAudioMenuItem.setEnabled(watchHistoryEnabled);
    }//GEN-LAST:event_toolsMenuMenuSelected

    private void exportHistoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportHistoryMenuItemActionPerformed
        final WatchHistoryFrame watchHistoryFrame = App.getWatchHistoryFrame();
        if (watchHistoryFrame == null) {
            final MachineRunner r = App.getMachineRunner();
            if (r != null) {
                App.exportHistory(r.getMovie());
            }
        } else {
            final WatchHistoryPanel watchHistoryPanel = watchHistoryFrame
                    .getWatchHistoryPanel();
            watchHistoryPanel.pause();
            App.exportHistory(watchHistoryPanel.getMovie());
        }
    }//GEN-LAST:event_exportHistoryMenuItemActionPerformed

    private void importHistoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importHistoryMenuItemActionPerformed
        App.importHistory();
    }//GEN-LAST:event_importHistoryMenuItemActionPerformed

    private void clearHistoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearHistoryMenuItemActionPerformed
        final WatchHistoryFrame watchHistoryFrame = App.getWatchHistoryFrame();
        if (watchHistoryFrame != null) {
            watchHistoryFrame.getWatchHistoryPanel().resume(true);
        } else {
            App.setNoStepPause(true);
            final MachineRunner r = App.getMachineRunner();
            if (r != null) {
                final Movie movie = new Movie(App.isVsDualSystem());
                r.setMovie(movie);
                r.getMapper().updateButtons(0);
                SystemAudioProcessor.setMovie(movie);
            }
            App.setNoStepPause(false);
        }
    }//GEN-LAST:event_clearHistoryMenuItemActionPerformed

    private void saveScreenshotMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveScreenshotMenuItemActionPerformed
        if (machine != null) {
            imagePane.requestScreenshot();
        }
    }//GEN-LAST:event_saveScreenshotMenuItemActionPerformed

    private void palettesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_palettesMenuItemActionPerformed
        App.setNoStepPause(true);
        new PaletteOptionsDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_palettesMenuItemActionPerformed

    private void ntscRegionRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ntscRegionRadioButtonMenuItemActionPerformed
        setTVSystem(NTSC);
    }//GEN-LAST:event_ntscRegionRadioButtonMenuItemActionPerformed

    private void palRegionRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_palRegionRadioButtonMenuItemActionPerformed
        setTVSystem(PAL);
    }//GEN-LAST:event_palRegionRadioButtonMenuItemActionPerformed

    private void dendyRegionRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dendyRegionRadioButtonMenuItemActionPerformed
        setTVSystem(Dendy);
    }//GEN-LAST:event_dendyRegionRadioButtonMenuItemActionPerformed

    private void tvSystemMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_tvSystemMenuMenuSelected
        final Machine m = App.getMachine();
        if (m != null) {
            switch (m.getMapper().getTVSystem()) {
                case NTSC:
                    ntscRegionRadioButtonMenuItem.setSelected(true);
                    break;
                case PAL:
                    palRegionRadioButtonMenuItem.setSelected(true);
                    break;
                case Dendy:
                    dendyRegionRadioButtonMenuItem.setSelected(true);
                    break;
            }
        }
    }//GEN-LAST:event_tvSystemMenuMenuSelected

    private void overscanMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overscanMenuItemActionPerformed
        App.setNoStepPause(true);
        new OverscanDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_overscanMenuItemActionPerformed

    private void licenseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_licenseMenuItemActionPerformed
        App.setNoStepPause(true);
        new TextDialog(this, App.getLicense(), "License").setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_licenseMenuItemActionPerformed

    private void screenshotOptionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_screenshotOptionsMenuItemActionPerformed
        App.setNoStepPause(true);
        new ScreenshotOptionsDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_screenshotOptionsMenuItemActionPerformed

    private void archiveFileOptionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_archiveFileOptionsMenuItemActionPerformed
        App.setNoStepPause(true);
        new ArchiveOptionsDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_archiveFileOptionsMenuItemActionPerformed

    private void backgroundCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundCheckBoxMenuItemActionPerformed
        final boolean backgroundEnabled = backgroundCheckBoxMenuItem.isSelected();
        PPU.setBackgroundEnabled(backgroundEnabled);
        AppPrefs.getInstance().getView().setBackgroundEnabled(backgroundEnabled);
        AppPrefs.save();
    }//GEN-LAST:event_backgroundCheckBoxMenuItemActionPerformed

    private void spritesCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spritesCheckBoxMenuItemActionPerformed
        final boolean spritesEnabled = spritesCheckBoxMenuItem.isSelected();
        PPU.setSpritesEnabled(spritesEnabled);
        AppPrefs.getInstance().getView().setSpritesEnabled(spritesEnabled);
        AppPrefs.save();
    }//GEN-LAST:event_spritesCheckBoxMenuItemActionPerformed

    private void fpsCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fpsCheckBoxMenuItemActionPerformed
        final boolean showFPS = fpsCheckBoxMenuItem.isSelected();
        imagePane.setShowFPS(showFPS);
        AppPrefs.getInstance().getView().setShowFPS(showFPS);
        AppPrefs.save();
    }//GEN-LAST:event_fpsCheckBoxMenuItemActionPerformed

    private void inputDevicesCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputDevicesCheckBoxMenuItemActionPerformed
        final boolean showInputDevices = inputDevicesCheckBoxMenuItem.isSelected();
        PPU.setShowInputDevices(showInputDevices);
        AppPrefs.getInstance().getView().setShowInputDevices(showInputDevices);
        AppPrefs.save();
    }//GEN-LAST:event_inputDevicesCheckBoxMenuItemActionPerformed

    private void statusMessagesCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusMessagesCheckBoxMenuItemActionPerformed
        final boolean showStatusMessages = statusMessagesCheckBoxMenuItem
                .isSelected();
        imagePane.setShowStatusMessages(showStatusMessages);
        AppPrefs.getInstance().getView().setShowStatusMessages(showStatusMessages);
        AppPrefs.save();
    }//GEN-LAST:event_statusMessagesCheckBoxMenuItemActionPerformed

    private void userInterfaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userInterfaceMenuItemActionPerformed
        App.setNoStepPause(true);
        final UserInterfaceDialog dialog = new UserInterfaceDialog(this);
        dialog.setVisible(true);
        if (dialog.isOk()) {
            AppPrefs.getInstance().getUserInterfacePrefs().apply();
        }
        App.setNoStepPause(false);
    }//GEN-LAST:event_userInterfaceMenuItemActionPerformed

    private void applyIPSMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyIPSMenuItemActionPerformed
        App.createApplyIpsPatchFrame();
    }//GEN-LAST:event_applyIPSMenuItemActionPerformed

    private void createIPSMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createIPSMenuItemActionPerformed
        App.createCreateIpsPatchFrame();
    }//GEN-LAST:event_createIPSMenuItemActionPerformed

    private void spriteBoxesCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spriteBoxesCheckBoxMenuItemActionPerformed
        final boolean spriteBoxesEnabled = spriteBoxesCheckBoxMenuItem.isSelected();
        PPU.setSpriteBoxesEnabled(spriteBoxesEnabled);
        AppPrefs.getInstance().getView().setSpriteBoxesEnabled(spriteBoxesEnabled);
        AppPrefs.save();
    }//GEN-LAST:event_spriteBoxesCheckBoxMenuItemActionPerformed

    private void oamDataMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oamDataMenuItemActionPerformed
        App.createOamDataFrame();
    }//GEN-LAST:event_oamDataMenuItemActionPerformed

    private void nsfOptionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nsfOptionsMenuItemActionPerformed
        App.setNoStepPause(true);
        new NsfOptionsDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_nsfOptionsMenuItemActionPerformed

    private void glitchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_glitchMenuItemActionPerformed
        InputUtil.addOtherInput(new Glitch());
    }//GEN-LAST:event_glitchMenuItemActionPerformed

    private void contentDirectoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentDirectoryMenuItemActionPerformed
        App.setNoStepPause(true);
        new ContentDirectoryDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_contentDirectoryMenuItemActionPerformed

    private void hotSwapMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hotSwapMenuItemActionPerformed
        final Machine ejectedMachine = machine;
        if (ejectedMachine == null) {
            return;
        }
        App.setNoStepPause(true);
        boolean hotSwap = true;
        if (AppPrefs.getInstance().getUserInterfacePrefs().isConfirmHotSwap()) {
            final YesNoDialog dialog = new YesNoDialog(this, "<html><p>Hot Swap "
                    + "changes cartridges without turning off the power. The second game"
                    + "<br/>will begin executing with the memory state of the first game."
                    + "</p><br/><p>Proceed with Hot Swap?</p></html>",
                    "Confirm Hot Swap");
            dialog.setVisible(true);
            hotSwap = dialog.isYes();
        }
        if (hotSwap) {
            App.close();
            openFile(null, false, ejectedMachine);
        } else {
            App.setNoStepPause(false);
        }
    }//GEN-LAST:event_hotSwapMenuItemActionPerformed

    private void noSpriteLimitCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noSpriteLimitCheckBoxMenuItemActionPerformed
        final boolean noSpriteLimit = noSpriteLimitCheckBoxMenuItem
                .isSelected();
        InputUtil.addOtherInput(new SetNoSpriteLimit(noSpriteLimit));
        AppPrefs.getInstance().getView().setNoSpriteLimit(noSpriteLimit);
        AppPrefs.save();
    }//GEN-LAST:event_noSpriteLimitCheckBoxMenuItemActionPerformed

    private void familyBasicOptionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_familyBasicOptionsMenuItemActionPerformed
        App.setNoStepPause(true);
        new FamilyBasicOptionsDialog(this).setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_familyBasicOptionsMenuItemActionPerformed

    private void familyBasicMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_familyBasicMenuMenuSelected
        int mode = DataRecorderMode.Stop;
        final Machine m = machine;
        if (m != null) {
            final DataRecorderMapper d = m.getMapper().getDataRecorder();
            if (d != null) {
                mode = d.getDataRecorderMode();
            }
        }
        final boolean filesEnabled = mode == DataRecorderMode.Stop;
        familyBasicLoadTapeMenuItem.setEnabled(filesEnabled);
        familyBasicSaveTapeMenuItem.setEnabled(filesEnabled);
        familyBasicEraseTapeMenuItem.setEnabled(filesEnabled);
        switch (mode) {
            case DataRecorderMode.Play:
                familyBasicPlayTapeMenuItem.setSelected(true);
                break;
            case DataRecorderMode.Record:
                familyBasicRecordTapeMenuItem.setSelected(true);
                break;
            default:
                familyBasicStopTapeMenuItem.setSelected(true);
                break;
        }
    }//GEN-LAST:event_familyBasicMenuMenuSelected

    private void famicom3dGlassesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_famicom3dGlassesMenuItemActionPerformed
        App.createGlassesFrame();
    }//GEN-LAST:event_famicom3dGlassesMenuItemActionPerformed

    private void viewMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_viewMenuMenuSelected
        boolean barcodeBattlerEnabled = false;
        boolean famicom3dGlassesEnabled = false;
        final Machine mac = machine;
        final Mapper m = mac == null ? null : mac.getMapper();
        if (m != null) {
            barcodeBattlerEnabled = m.hasDeviceMapper(InputDevices.BarcodeBattler);
            famicom3dGlassesEnabled = m.hasDeviceMapper(InputDevices.Glasses);
        }
        fileInfoMenuItem.setEnabled(mac != null && fileInfo != null);
        barcodeBattlerMenuItem.setEnabled(barcodeBattlerEnabled);
        famicom3dGlassesMenuItem.setEnabled(famicom3dGlassesEnabled);
        robMenuItem.setEnabled(App.isRobGame());
        subMonitorMenuItem.setEnabled(App.isVsDualSystem());
    }//GEN-LAST:event_viewMenuMenuSelected

    private void speedMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_speedMenuMenuSelected
        final int speed = App.getSpeed();
        int bestIndex = 0;
        int delta = Integer.MAX_VALUE;
        for (int i = SpeedValues.length - 1; i >= 0; i--) {
            final int d = Math.abs(SpeedValues[i] - speed);
            if (d < delta) {
                delta = d;
                bestIndex = i;
            }
        }
        speedMenuItems[bestIndex].setSelected(true);
    }//GEN-LAST:event_speedMenuMenuSelected

    private void helpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuItemActionPerformed
        BrowserUtil.openBrowser("http://nintaco.com/faq.html");
    }//GEN-LAST:event_helpMenuItemActionPerformed

    private void screamIntoMicrophoneMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_screamIntoMicrophoneMenuItemActionPerformed
        if (machine != null && displayingImagePane
                && App.getAppMode() != AppMode.WatchHistory) {
            InputUtil.addOtherInput(new ScreamIntoMicrophone());
        }
    }//GEN-LAST:event_screamIntoMicrophoneMenuItemActionPerformed

    private void traceLoggerOptionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_traceLoggerOptionsMenuItemActionPerformed
        App.disposeTraceLogger();
        new LoggerDialog(this).setVisible(true);
    }//GEN-LAST:event_traceLoggerOptionsMenuItemActionPerformed

    private void startTraceLoggerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTraceLoggerMenuItemActionPerformed
        if (App.isTraceLoggerRunning()) {
            App.disposeTraceLogger();
        } else {
            App.startTraceLogger();
        }
    }//GEN-LAST:event_startTraceLoggerMenuItemActionPerformed

    private void debugMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_debugMenuMenuSelected
        final boolean machineExists = App.isFileLoaded();

        debuggerMenuItem.setEnabled(machineExists);
        oamDataMenuItem.setEnabled(machineExists);
        patternTablesMenuItem.setEnabled(machineExists);
        nametablesMenuItem.setEnabled(machineExists);
        hexEditorMenuItem.setEnabled(machineExists);
        asmDasmMenuItem.setEnabled(machineExists);
        ramSearchMenuItem.setEnabled(machineExists);
        ramWatchMenuItem.setEnabled(machineExists);

        if (App.isTraceLoggerRunning() && machineExists) {
            startTraceLoggerMenuItem.setText("Stop Trace Logger");
        } else {
            startTraceLoggerMenuItem.setText("Start Trace Logger");
        }
        startTraceLoggerMenuItem.setEnabled(machineExists);
    }//GEN-LAST:event_debugMenuMenuSelected

    private void optionsMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_optionsMenuMenuSelected
        portsMenuItem.setEnabled(App.getAppMode() != AppMode.NetplayClient);
    }//GEN-LAST:event_optionsMenuMenuSelected

    private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
        EDT.async(() -> {
            imagePane.requestRepaint();
            invalidate();
            validate();
            repaint();
        });
    }//GEN-LAST:event_formWindowStateChanged

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        EDT.async(() -> {
            createVideoFiltersMenu();
            initShowMenu();
            initScreenSizeMenu();
            if (AppPrefs.getInstance().getUserInterfacePrefs().isLaunchFileOpen()) {
                openFile(null, false, null);
            }
        });
    }//GEN-LAST:event_formWindowOpened

    private void robMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_robMenuItemActionPerformed
        final Machine m = machine;
        if (m != null) {
            final RobController rob = m.getPPU().getRob();
            if (rob != null) {
                new SetupROB(rob).run(m);
            }
        }
    }//GEN-LAST:event_robMenuItemActionPerformed

    private void showMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_showMenuMenuSelected
        final Machine m = machine;
        noSpriteLimitCheckBoxMenuItem.setSelected((m == null)
                ? AppPrefs.getInstance().getView().isNoSpriteLimit()
                : m.getPPU().isNoSpriteLimit());
    }//GEN-LAST:event_showMenuMenuSelected

    private void subMonitorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subMonitorMenuItemActionPerformed
        App.createSubMonitorFrame();
    }//GEN-LAST:event_subMonitorMenuItemActionPerformed

    private void insertCoinMainLeftMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertCoinMainLeftMenuItemActionPerformed
        if (machine != null && App.isVsSystem()) {
            insertCoin(VsSystem.Main, CoinSlot.Left);
        }
    }//GEN-LAST:event_insertCoinMainLeftMenuItemActionPerformed

    private void insertCoinMainRightMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertCoinMainRightMenuItemActionPerformed
        if (machine != null && App.isVsSystem()) {
            insertCoin(VsSystem.Main, CoinSlot.Right);
        }
    }//GEN-LAST:event_insertCoinMainRightMenuItemActionPerformed

    private void serviceButtonMainMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serviceButtonMainMenuItemActionPerformed
        if (machine != null && App.isVsSystem()) {
            InputUtil.addOtherInput(new PressServiceButton(VsSystem.Main));
        }
    }//GEN-LAST:event_serviceButtonMainMenuItemActionPerformed

    private void serviceButtonSubMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serviceButtonSubMenuItemActionPerformed
        if (machine != null && App.isVsDualSystem()) {
            InputUtil.addOtherInput(new PressServiceButton(VsSystem.Sub));
        }
    }//GEN-LAST:event_serviceButtonSubMenuItemActionPerformed

    private void insertCoinSubLeftMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertCoinSubLeftMenuItemActionPerformed
        if (machine != null && App.isVsDualSystem()) {
            insertCoin(VsSystem.Sub, CoinSlot.Left);
        }
    }//GEN-LAST:event_insertCoinSubLeftMenuItemActionPerformed

    private void insertCoinSubRightMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertCoinSubRightMenuItemActionPerformed
        if (machine != null && App.isVsDualSystem()) {
            insertCoin(VsSystem.Sub, CoinSlot.Right);
        }
    }//GEN-LAST:event_insertCoinSubRightMenuItemActionPerformed

    private void eraseBatterySaveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eraseBatterySaveMenuItemActionPerformed
        App.setNoStepPause(true);
        final YesNoDialog yesNoDialog = new YesNoDialog(this,
                "<html><p>This will erase all battery-backed data and restart the "
                        + "currently running file.</p><br/><p>Proceed with erase?</p></html>",
                "Confirm Erase Battery Save");
        yesNoDialog.setVisible(true);
        App.setNoStepPause(false);
        if (yesNoDialog.isYes()) {
            App.eraseBatterySave();
        }
    }//GEN-LAST:event_eraseBatterySaveMenuItemActionPerformed

    private void underscanCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_underscanCheckBoxMenuItemActionPerformed
        AppPrefs.getInstance().getView().setUnderscan(underscanCheckBoxMenuItem
                .isSelected());
        imagePane.updateScreenBorders();
        AppPrefs.save();
    }//GEN-LAST:event_underscanCheckBoxMenuItemActionPerformed

    private void uniformPixelScalingCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uniformPixelScalingCheckBoxMenuItemActionPerformed
        final boolean uniformPixelScaling = uniformPixelScalingCheckBoxMenuItem
                .isSelected();
        AppPrefs.getInstance().getView()
                .setUniformPixelScaling(uniformPixelScaling);
        imagePane.setUniformPixelScaling(uniformPixelScaling);
        AppPrefs.save();
    }//GEN-LAST:event_uniformPixelScalingCheckBoxMenuItemActionPerformed

    // End of variables declaration//GEN-END:variables
}
