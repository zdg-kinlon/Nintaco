package cn.kinlon.emu.gui.image;


import cn.kinlon.emu.*;
import cn.kinlon.emu.files.FilePath;
import cn.kinlon.emu.files.IpsUtil;
import cn.kinlon.emu.files.NsfFile;
import cn.kinlon.emu.gui.*;
import cn.kinlon.emu.gui.about.AboutDialog;
import cn.kinlon.emu.gui.fds.FamicomDiskSystemOptionsDialog;
import cn.kinlon.emu.gui.image.filters.VideoFilterDescriptor;
import cn.kinlon.emu.gui.image.preferences.Paths;
import cn.kinlon.emu.gui.image.preferences.View;
import cn.kinlon.emu.gui.input.buttonmapping.ButtonMappingDialog;
import cn.kinlon.emu.gui.input.ports.PortsDialog;
import cn.kinlon.emu.gui.input.settings.InputSettingsDialog;
import cn.kinlon.emu.gui.nsf.NsfPanel;
import cn.kinlon.emu.gui.sound.SoundOptionsDialog;
import cn.kinlon.emu.gui.userinterface.UserInterfacePrefs;
import cn.kinlon.emu.input.DeviceMapper;
import cn.kinlon.emu.input.InputDevices;
import cn.kinlon.emu.input.InputUtil;
import cn.kinlon.emu.input.other.*;
import cn.kinlon.emu.mappers.Mapper;
import cn.kinlon.emu.mappers.nintendo.vs.CoinSlot;
import cn.kinlon.emu.mappers.nintendo.vs.DualAPU;
import cn.kinlon.emu.mappers.nintendo.vs.VsSystem;
import cn.kinlon.emu.palettes.PalettePPU;
import cn.kinlon.emu.palettes.PaletteUtil;
import cn.kinlon.emu.palettes.Palettes;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.tv.TVSystem;
import cn.kinlon.emu.utils.CollectionsUtil;
import cn.kinlon.emu.utils.GuiUtil;


import javax.swing.*;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

import static cn.kinlon.emu.files.FileUtil.*;
import static cn.kinlon.emu.tv.TVSystem.*;
import static cn.kinlon.emu.utils.GuiUtil.*;
import static cn.kinlon.emu.utils.StringUtils.isBlank;
import static java.awt.event.KeyEvent.*;
import static javax.swing.UIManager.getInstalledLookAndFeels;
import static javax.swing.UIManager.getSystemLookAndFeelClassName;

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
            = new JRadioButtonMenuItem[SpeedValues.length];
    private final KeyListener NoMenuBarKeyListener = new KeyAdapter() {
        @Override
        public void keyPressed(final KeyEvent e) {
            e.consume();
            final int code = e.getKeyCode();
            if (e.isShiftDown()) {
                if (!(e.isAltDown() || e.isControlDown())) {
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
                switch (code) {
                    case VK_F2:
                        insertCoinMainLeftMenuItemActionPerformed(null);
                        break;
                    case VK_F3:
                        insertCoinMainRightMenuItemActionPerformed(null);
                        break;
                    case VK_F12:
                        break;
                    case VK_ESCAPE:
                        setFullscreenMode(false);
                        setMenuBarVisible(true);
                        break;
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
    private boolean displayingImagePane;
    private Machine machine;
    private boolean historyTracking;
    private boolean timeRewinding;
    private boolean keyEventsEnabled;
    private boolean smoothScaling;
    private boolean useTvAspectRatio;
    private boolean uniformPixelScaling;
    private String saveFileName;
    private volatile String fileInfo;
    private ButtonGroup diskSideButtonGroup = new ButtonGroup();
    private JCheckBoxMenuItem[] diskSideMenuItems = new JCheckBoxMenuItem[0];
    private boolean wasMaximized;
    private long exitFullscreenModeTime;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem asmDasmMenuItem;
    private javax.swing.JCheckBoxMenuItem backgroundCheckBoxMenuItem;
    private javax.swing.JMenuItem barcodeBattlerMenuItem;
    private javax.swing.JMenuItem buttonMappingMenuItem;
    private javax.swing.JMenuItem clearHistoryMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem connectToNetplayServerMenuItem;
    private javax.swing.JMenu debugMenu;
    private javax.swing.JMenuItem debuggerMenuItem;
    private javax.swing.JRadioButtonMenuItem dendyRegionRadioButtonMenuItem;
    private javax.swing.JMenuItem dipSwitchesMenuItem;
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
    private javax.swing.JMenuItem familyBasicLoadBackgroundMenuItem;
    private javax.swing.JMenuItem familyBasicLoadProgramMenuItem;
    private javax.swing.JMenu familyBasicMenu;
    private javax.swing.JMenuItem familyBasicPasteProgramMenuItem;
    private javax.swing.JMenuItem familyBasicSaveBackgroundMenuItem;
    private javax.swing.JMenuItem familyBasicSaveProgramMenuItem;
    private javax.swing.JMenuItem familyBasicTypePasteMenuItem;
    private javax.swing.JMenuItem fileInfoMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem flipDiskSideMenuItem;
    private javax.swing.JMenuItem fontSizeMenuItem;
    private javax.swing.JCheckBoxMenuItem fpsCheckBoxMenuItem;
    private javax.swing.JMenuItem fullscreenMenuItem;
    private javax.swing.JMenuItem glitchMenuItem;
    private javax.swing.JMenu helpMenu;
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
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator20;
    private javax.swing.JPopupMenu.Separator jSeparator22;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.ButtonGroup lookAndFeelButtonGroup;
    private javax.swing.JMenu lookAndFeelMenu;
    private javax.swing.JMenu machineMenu;
    private javax.swing.JMenuItem manageCheatsMenuItem;
    private javax.swing.JMenuItem mapMakerMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem nametablesMenuItem;
    private javax.swing.JMenuItem nextFrameMenuItem;
    private javax.swing.JRadioButtonMenuItem ntscRegionRadioButtonMenuItem;
    private javax.swing.JMenuItem oamDataMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenu openRecentArchiveMenu;
    private javax.swing.JMenu openRecentDirectoryMenu;
    private javax.swing.JMenu openRecentFileMenu;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JRadioButtonMenuItem palRegionRadioButtonMenuItem;
    private javax.swing.JMenu paletteMenu;
    private javax.swing.JMenuItem patternTablesMenuItem;
    private javax.swing.JCheckBoxMenuItem pauseMenuItem;
    private javax.swing.JMenuItem portsMenuItem;
    private javax.swing.JMenuItem powerCycleMenuItem;
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
    private javax.swing.JMenuItem screamIntoMicrophoneMenuItem;
    private javax.swing.JMenuItem screenSize1XMenuItem;
    private javax.swing.JMenuItem screenSize2XMenuItem;
    private javax.swing.JMenuItem screenSize3XMenuItem;
    private javax.swing.JMenuItem screenSize4XMenuItem;
    private javax.swing.JMenuItem screenSize5XMenuItem;
    private javax.swing.JMenuItem screenSizeMaxMenuItem;
    private javax.swing.JMenu screenSizeMenu;
    private javax.swing.JMenuItem searchCheatsMenuItem;
    private javax.swing.JPopupMenu.Separator separator15;
    private javax.swing.JPopupMenu.Separator separator16;
    private javax.swing.JPopupMenu.Separator separator7;
    private javax.swing.JMenuItem serviceButtonMainMenuItem;
    private javax.swing.JMenu serviceButtonMenu;
    private javax.swing.JMenuItem serviceButtonSubMenuItem;
    private javax.swing.JCheckBoxMenuItem smoothScalingCheckBoxMenuItem;
    private javax.swing.JMenuItem soundMenuItem;
    private javax.swing.JMenu speedMenu;
    private javax.swing.JCheckBoxMenuItem spriteBoxesCheckBoxMenuItem;
    private javax.swing.JMenuItem spriteSaverMenuItem;
    private javax.swing.JCheckBoxMenuItem spritesCheckBoxMenuItem;
    private javax.swing.JMenuItem startNetplayServerMenuItem;
    private javax.swing.JMenuItem startProgramServerMenuItem;
    private javax.swing.JMenuItem startTraceLoggerMenuItem;
    private javax.swing.JMenuItem subMonitorMenuItem;
    private javax.swing.JCheckBoxMenuItem trackHistoryCheckBoxMenuItem;
    private javax.swing.JCheckBoxMenuItem tvAspectCheckBoxMenuItem;
    private javax.swing.JMenu tvSystemMenu;
    private javax.swing.JCheckBoxMenuItem underscanCheckBoxMenuItem;
    private javax.swing.JCheckBoxMenuItem uniformPixelScalingCheckBoxMenuItem;
    private javax.swing.JMenu videoFilterMenu;
    private javax.swing.JMenu viewMenu;
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

        EventQueue.invokeLater(() -> { // TODO REVIEW THIS
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

    public void setHistoryTracking(final boolean historyTracking) {
        if (EventQueue.isDispatchThread()) {
            this.historyTracking = historyTracking;
            trackHistoryCheckBoxMenuItem.setSelected(historyTracking);
            setTimeRewinding(timeRewinding && historyTracking);
        } else {
            EventQueue.invokeLater(()
                    -> setHistoryTracking(historyTracking));
        }
    }

    public void setTimeRewinding(final boolean timeRewinding) {
        if (EventQueue.isDispatchThread()) {
            this.timeRewinding = timeRewinding;
        } else {
            EventQueue.invokeLater(() -> setTimeRewinding(timeRewinding));
        }
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
        if (EventQueue.isDispatchThread()) {
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
        } else {
            EventQueue.invokeLater(this::createPaletteMenu);
        }
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

    public void createLookAndFeelMenu() {
        final View view = AppPrefs.getInstance().getView();
        final String lookAndFeelClassName = view.getLookAndFeelClassName() == null
                ? getSystemLookAndFeelClassName() : view.getLookAndFeelClassName();
        final String metalLookAndFeelClassName = MetalLookAndFeel.class
                .getCanonicalName();
        UIManager.LookAndFeelInfo[] lookAndFeelInfos = getInstalledLookAndFeels();
        Arrays.sort(lookAndFeelInfos,
                (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        for (final UIManager.LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos) {
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
            EventQueue.invokeLater(run);
        }
        menuItem.addActionListener(e -> EventQueue.invokeLater(run));
        lookAndFeelMenu.add(menuItem);
    }

    private void addLookAndFeelMenuItem(final UIManager.LookAndFeelInfo lookAndFeelInfo,
                                        final String lookAndFeelClassName) {
        final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(
                lookAndFeelInfo.getName());
        lookAndFeelButtonGroup.add(menuItem);
        if (lookAndFeelInfo.getClassName().equals(lookAndFeelClassName)) {
            menuItem.setSelected(true);
            EventQueue.invokeLater(
                    () -> GuiUtil.setLookAndFeel(lookAndFeelClassName));
        }
        menuItem.addActionListener(e -> EventQueue.invokeLater(
                () -> GuiUtil.setLookAndFeel(lookAndFeelInfo.getClassName())));
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
        if (EventQueue.isDispatchThread()) {
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
        } else {
            EventQueue.invokeLater(() -> updateContentPane(mapper, nsfFile));
        }
    }

    public boolean isDisplayingImagePane() {
        return displayingImagePane;
    }

    public void setMachine(final Machine machine) {
        this.machine = machine;
        nsfPanel.setMachine(machine);
        EventQueue.invokeLater(this::initTimeRewinding);
    }

    public void appModeChanged(final AppMode appMode) {
        if (EventQueue.isDispatchThread()) {
            nsfPanel.appModeChanged(appMode);
            updateMenus();
        } else {
            EventQueue.invokeLater(() -> appModeChanged(appMode));
        }
    }

    public ImagePane getImagePane() {
        return imagePane;
    }

    public NsfPanel getNsfPanel() {
        return nsfPanel;
    }

    public void adjustSize() {
        if (EventQueue.isDispatchThread()) {
            if (imagePane.getBufferStrategy() == null) {
                maxipack(this);
            }
        } else {
            invokeAndWait(this::adjustSize);
        }
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
            EventQueue.invokeLater(() -> setVisible(true));
            EventQueue.invokeLater(imagePane::redraw);
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
            EventQueue.invokeLater(() -> setVisible(true));
            EventQueue.invokeLater(imagePane::redraw);
        }
    }

    public void setFullscreenMode(final boolean fullscreenMode) {
        if (EventQueue.isDispatchThread()) {
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
        } else {
            EventQueue.invokeLater(() -> setFullscreenMode(fullscreenMode));
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

            App.setStepPause(false);
            final Mapper m = App.loadFile(in, handle.getFileSize(), fileName);

            fileInfo = App.createMachine(m, ejectedMachine);
            pleaseWaitDialog.dispose();

            AppPrefs.getInstance().getPaths().addRecentFile(fileName);
            AppPrefs.save();
            saveFileName = createSaveFile(fileName);
            EventQueue.invokeLater(this::finishLoad);
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
        }
    }

    private void finishLoad() {
        App.setSpeed(100);
        final UserInterfacePrefs prefs = AppPrefs.getInstance()
                .getUserInterfacePrefs();
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
        if (EventQueue.isDispatchThread()) {
            openFile(new File(fileName));
        } else {
            EventQueue.invokeLater(() -> openFile(fileName));
        }
    }

    public void openFile(final File file) {
        if (EventQueue.isDispatchThread()) {
            fileOpened(file, null, false, null, null);
        } else {
            EventQueue.invokeLater(() -> openFile(file));
        }
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
            App.setNoStepPause(false);
            return;
        }

        final File outerFile = new File(filePath.getOuterPath());
        if (!outerFile.exists()) {
            App.setNoStepPause(false);
            return;
        } else if (!outerFile.isFile()) {
            App.setNoStepPause(false);
            return;
        }

        final String archiveFileName;
        final String entryFileName;
        if (filePath.isArchivedEntry()) {
            if (!isArchiveFile(filePath.getArchivePath())) {
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
            pleaseWaitDialog.setMessage("Reading file...");
            new Thread(() -> loadDiskFile(entryFileName, pleaseWaitDialog, editHeader, ejectedMachine)).start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
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
        if (EventQueue.isDispatchThread()) {
            this.keyEventsEnabled = keyEventsEnabled;
            final KeyboardFocusManager manager = KeyboardFocusManager
                    .getCurrentKeyboardFocusManager();
            manager.removeKeyEventDispatcher(DisableKeyEventsDispatcher);
            if (!keyEventsEnabled) {
                manager.addKeyEventDispatcher(DisableKeyEventsDispatcher);
            }
        } else {
            EventQueue.invokeLater(() -> setKeyEventsEnabled(keyEventsEnabled));
        }
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
        lookAndFeelButtonGroup = new javax.swing.ButtonGroup();
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
        familyBasicMenu = new javax.swing.JMenu();
        familyBasicCopyProgramMenuItem = new javax.swing.JMenuItem();
        familyBasicPasteProgramMenuItem = new javax.swing.JMenuItem();
        familyBasicTypePasteMenuItem = new javax.swing.JMenuItem();
        familyBasicLoadProgramMenuItem = new javax.swing.JMenuItem();
        familyBasicSaveProgramMenuItem = new javax.swing.JMenuItem();
        familyBasicEditBackgroundMenuItem = new javax.swing.JMenuItem();
        familyBasicLoadBackgroundMenuItem = new javax.swing.JMenuItem();
        familyBasicSaveBackgroundMenuItem = new javax.swing.JMenuItem();
        insertCoinMenu = new javax.swing.JMenu();
        insertCoinMainLeftMenuItem = new javax.swing.JMenuItem();
        insertCoinMainRightMenuItem = new javax.swing.JMenuItem();
        insertCoinSubLeftMenuItem = new javax.swing.JMenuItem();
        insertCoinSubRightMenuItem = new javax.swing.JMenuItem();
        serviceButtonMenu = new javax.swing.JMenu();
        serviceButtonMainMenuItem = new javax.swing.JMenuItem();
        serviceButtonSubMenuItem = new javax.swing.JMenuItem();
        dipSwitchesMenuItem = new javax.swing.JMenuItem();
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
        fpsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        inputDevicesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        backgroundCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        spritesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        spriteBoxesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
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
        famicomDiskSystemOptionsMenuItem = new javax.swing.JMenuItem();
        startNetplayServerMenuItem = new javax.swing.JMenuItem();
        connectToNetplayServerMenuItem = new javax.swing.JMenuItem();
        mapMakerMenuItem = new javax.swing.JMenuItem();
        spriteSaverMenuItem = new javax.swing.JMenuItem();
        runProgramMenuItem = new javax.swing.JMenuItem();
        startProgramServerMenuItem = new javax.swing.JMenuItem();
        manageCheatsMenuItem = new javax.swing.JMenuItem();
        searchCheatsMenuItem = new javax.swing.JMenuItem();
        saveScreenshotMenuItem = new javax.swing.JMenuItem();
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
        startTraceLoggerMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Nintaco DEV");
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

        flipDiskSideMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(VK_B, java.awt.event.InputEvent.SHIFT_MASK));
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

        famicomDiskSystemOptionsMenuItem.setMnemonic('F');
        famicomDiskSystemOptionsMenuItem.setText("Famicom Disk System...");
        famicomDiskSystemOptionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                famicomDiskSystemOptionsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(famicomDiskSystemOptionsMenuItem);

        menuBar.add(optionsMenu);

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

        hexEditorMenuItem.setMnemonic('H');
        hexEditorMenuItem.setText("Hex Editor...");
        hexEditorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hexEditorMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(hexEditorMenuItem);

        menuBar.add(debugMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");

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


    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        exit();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        exit();
    }//GEN-LAST:event_formWindowClosing

    private void fileInfoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileInfoMenuItemActionPerformed
        App.setNoStepPause(true);
        new TextDialog(this, fileInfo, "File Information").setVisible(true);
        App.setNoStepPause(false);
    }//GEN-LAST:event_fileInfoMenuItemActionPerformed

    private void fullscreenMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullscreenMenuItemActionPerformed
        toggleFullscreenMode();
    }//GEN-LAST:event_fullscreenMenuItemActionPerformed

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

    private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_fileMenuMenuSelected
        final boolean machineExists = machine != null;
        final AppMode appMode = App.getAppMode();
        final boolean openable = appMode == AppMode.Default
                || appMode == AppMode.NetplayServer;
        final Paths paths = AppPrefs.getInstance().getPaths();

        openMenuItem.setEnabled(openable);
        openRecentFileMenu.setEnabled(!paths.getRecentFiles().isEmpty()
                && openable);
        openRecentArchiveMenu.setEnabled(!paths.getRecentArchives().isEmpty()
                && openable);
        openRecentDirectoryMenu.setEnabled(!paths.getRecentDirectories().isEmpty()
                && openable);
        closeMenuItem.setEnabled(machineExists || timeRewinding
                || appMode == AppMode.WatchHistory);
    }//GEN-LAST:event_fileMenuMenuSelected

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

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        setKeyEventsEnabled(keyEventsEnabled);
        DualAPU.setMainUpdateEnabled(true);
    }//GEN-LAST:event_formWindowGainedFocus

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        enableKeyEvents();
    }//GEN-LAST:event_formWindowLostFocus

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

        runProgramMenuItem.setText("Run Program...");
        startProgramServerMenuItem.setEnabled(notNetplay);

        startProgramServerMenuItem.setText("Start Program Server...");
        runProgramMenuItem.setEnabled(notNetplay);

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
            TVSystem tvSystem = m.getMapper().getTVSystem();
            if (tvSystem == NTSC) {
                ntscRegionRadioButtonMenuItem.setSelected(true);
            } else if (tvSystem == PAL) {
                palRegionRadioButtonMenuItem.setSelected(true);
            } else if (tvSystem == Dendy) {
                dendyRegionRadioButtonMenuItem.setSelected(true);
            }
        }
    }//GEN-LAST:event_tvSystemMenuMenuSelected

    private void glitchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_glitchMenuItemActionPerformed
        InputUtil.addOtherInput(new Glitch());
    }//GEN-LAST:event_glitchMenuItemActionPerformed

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

        startTraceLoggerMenuItem.setEnabled(machineExists);
    }//GEN-LAST:event_debugMenuMenuSelected

    private void optionsMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_optionsMenuMenuSelected
        portsMenuItem.setEnabled(App.getAppMode() != AppMode.NetplayClient);
    }//GEN-LAST:event_optionsMenuMenuSelected

    private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
        EventQueue.invokeLater(() -> {
            imagePane.requestRepaint();
            invalidate();
            validate();
            repaint();
        });
    }//GEN-LAST:event_formWindowStateChanged

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        EventQueue.invokeLater(() -> {
            createVideoFiltersMenu();
            initShowMenu();
            initScreenSizeMenu();
            if (AppPrefs.getInstance().getUserInterfacePrefs().isLaunchFileOpen()) {
                openFile(null, false, null);
            }
        });
    }//GEN-LAST:event_formWindowOpened

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
