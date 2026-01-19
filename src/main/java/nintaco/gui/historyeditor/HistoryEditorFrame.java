package nintaco.gui.historyeditor;

import nintaco.App;
import nintaco.MachineRunner;
import nintaco.files.CartFile;
import nintaco.gui.*;
import nintaco.gui.historyeditor.change.*;
import nintaco.gui.historyeditor.preferences.HistoryEditorPrefs;
import nintaco.gui.historyeditor.tasks.*;
import nintaco.gui.image.QuickSaveStateInfo;
import nintaco.gui.image.preferences.Paths;
import nintaco.movie.Movie;
import nintaco.preferences.AppPrefs;
import nintaco.task.Task;
import nintaco.task.TaskScheduler;
import nintaco.util.EDT;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.lang.Math.max;
import static nintaco.files.FileUtil.*;
import static nintaco.movie.Movie.BLOCK_SIZE;
import static nintaco.util.CollectionsUtil.toIntArray;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.MathUtil.clamp;
import static nintaco.util.StreamUtil.toByteArrayOutputStream;
import static nintaco.util.StringUtil.isBlank;
import static nintaco.util.StringUtil.parseInt;

public class HistoryEditorFrame extends javax.swing.JFrame
        implements FramePlayedListener {

    public static final String LINE_SEPARATOR = "\n";
    public static final FileExtensionFilter historyFileExtensionFilter
            = new FileExtensionFilter(0, "History files (*.history)", "history");
    private static final SimpleDateFormat dateFormatter
            = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final BookmarksModel bookmarksModel = new BookmarksModel();
    private final ChangeListModel changeListModel = new ChangeListModel();
    private final HistoryTableModel historyTableModel
            = new HistoryTableModel(changeListModel, bookmarksModel);
    private final TaskScheduler taskScheduler = new TaskScheduler();
    private final HistoryTableRowRenderer historyTableRowRenderer
            = new HistoryTableRowRenderer();
    private final List<QuickSaveStateInfo> quickSaveStateInfos
            = new ArrayList<>();

    private volatile Movie movie;

    private int lastClickedRowIndex;
    private RecordTask recordTask;
    private PlayMovieTask playMovieTask;
    private boolean executing;
    private int priorRowCount;
    private int priorHeadIndex;
    private String projectName;
    private boolean disking;
    private File priorSaveFile;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBookmarkButton;
    private javax.swing.JMenuItem appendCloneMenuItem;
    private javax.swing.JMenuItem appendFrameMenuItem;
    private javax.swing.JMenuItem appendFramesMenuItem;
    private javax.swing.JMenuItem appendPasteMenuItem;
    private javax.swing.JList bookmarksList;
    private javax.swing.JPanel bookmarksPanel;
    private javax.swing.JScrollPane bookmarksScrollPane;
    private javax.swing.JButton cancelButton;
    private javax.swing.JList changesList;
    private javax.swing.JScrollPane changesScrollPane;
    private javax.swing.JButton clearButton;
    private javax.swing.JMenuItem clearMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JButton deleteBookmarkButton;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem deselectMenuItem;
    private javax.swing.JButton editBookmarkButton;
    private javax.swing.JMenu editMenu;
    private javax.swing.JCheckBox fastGenerationCheckBox;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton headButton;
    private javax.swing.JPanel historyPanel;
    private javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JTable historyTable;
    private javax.swing.JMenuItem insertCloneMenuItem;
    private javax.swing.JMenuItem insertFrameMenuItem;
    private javax.swing.JMenuItem insertFramesMenuItem;
    private javax.swing.JMenuItem insertPasteMenuItem;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton loadButton;
    private javax.swing.JMenuItem loadMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JCheckBox mergeButtonsCheckBox;
    private javax.swing.JMenuItem mergeMenuItem;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem newestSlotMenuItem;
    private javax.swing.JButton nextBookmarkButton;
    private javax.swing.JButton nextFrameButton;
    private javax.swing.JMenuItem oldestSlotMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JToggleButton pauseToggleButton;
    private javax.swing.JButton previousBookmarkButton;
    private javax.swing.JButton previousFrameButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenu quickLoadProjectMenu;
    private javax.swing.JPopupMenu.Separator quickLoadSeparator;
    private javax.swing.JMenu quickSaveProjectMenu;
    private javax.swing.JPopupMenu.Separator quickSaveSeparator;
    private javax.swing.JMenuItem recentDirectoriesClearMenuItem;
    private javax.swing.JCheckBoxMenuItem recentDirectoriesLockMenuItem;
    private javax.swing.JMenu recentDirectoriesMenu;
    private javax.swing.JPopupMenu.Separator recentDirectoriesSeparator;
    private javax.swing.JMenuItem recentProjectsClearMenuItem;
    private javax.swing.JCheckBoxMenuItem recentProjectsLockMenuItem;
    private javax.swing.JMenu recentProjectsMenu;
    private javax.swing.JPopupMenu.Separator recentProjectsSeparator;
    private javax.swing.JCheckBox record1PCheckBox;
    private javax.swing.JCheckBox record2PCheckBox;
    private javax.swing.JCheckBox record3PCheckBox;
    private javax.swing.JCheckBox record4PCheckBox;
    private javax.swing.JCheckBox recordOtherCheckBox;
    private javax.swing.JPanel recordPanel;
    private javax.swing.JButton redoButton;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JCheckBox restorePositionCheckBox;
    private javax.swing.JPanel runPanel;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JComboBox saveSlotsComboBox;
    private javax.swing.JButton seekBookmarkButton;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem selectBetweenBookmarksMenuItem;
    private javax.swing.JPopupMenu.Separator separator;
    private javax.swing.JPopupMenu.Separator separator1;
    private javax.swing.JPopupMenu.Separator separator2;
    private javax.swing.JPopupMenu.Separator separator3;
    private javax.swing.JPopupMenu.Separator separator4;
    private javax.swing.JPopupMenu.Separator separator5;
    private javax.swing.JPopupMenu.Separator separator6;
    private javax.swing.JPopupMenu.Separator separator7;
    private javax.swing.JPopupMenu.Separator separator8;
    private javax.swing.JPopupMenu.Separator separator9;
    private javax.swing.JPanel statesPanel;
    private javax.swing.JCheckBox trackCursorCheckBox;
    private javax.swing.JMenuItem trimBottomMenuItem;
    private javax.swing.JMenuItem trimTopMenuItem;
    private javax.swing.JButton undoButton;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JCheckBox view1PCheckBox;
    private javax.swing.JCheckBox view2PCheckBox;
    private javax.swing.JCheckBox view3PCheckBox;
    private javax.swing.JCheckBox view4PCheckBox;
    private javax.swing.JPanel viewPanel;

    public HistoryEditorFrame(final MachineRunner machineRunner) {
        initComponents();
        loadFields();
        initHistoryTable();
        initChangeList();
        initBookmarksList();
        enableComponents();
        scaleFonts(this);
        initPreferredSizes();
        pack();
        moveToImageFrameMonitor(this);

        setMachineRunner(machineRunner);
    }

    private void initPreferredSizes() {
        runPanel.setPreferredSize(null);
        viewPanel.setPreferredSize(null);
        recordPanel.setPreferredSize(null);
        statesPanel.setPreferredSize(null);
        bookmarksPanel.setPreferredSize(null);
        historyPanel.setPreferredSize(null);
    }

    private void generateDefaultProjectName() {
        final String gameName = getFileNameWithoutExtension(App.getEntryFileName());
        if (gameName != null) {
            setProjectName(String.format("%s-%s", gameName,
                    dateFormatter.format(new Date())));
        }
    }

    public void setMachineRunner(final MachineRunner machineRunner) {
        EDT.async(() -> {
            if (machineRunner == null || machineRunner.getMachine() == null) {
                closeProject();
            } else {
                cancelAllTasks();

                movie = machineRunner.getMovie();
                if (movie == null) {
                    movie = new Movie(false);
                    historyTableModel.setMovie(movie);
                    historyTableModel.clear(toByteArrayOutputStream(
                            machineRunner.getMachine()).toByteArray());
                } else {
                    historyTableModel.setMovie(movie);
                }

                machineRunner.dispose();
                App.updateFrames(null);
                App.setMachineRunner(null);
                priorSaveFile = null;

                resizeHistoryTableColumns();
                lastClickedRowIndex = historyTableModel.getHeadIndex();
                setExecuting(false);
                generateDefaultProjectName();
                createQuickMenus();
                createRecentProjectsMenu();
                resetProgressBar();
                saveSlotsComboBox.setSelectedIndex(0);
            }
        });
    }

    private void initHistoryTable() {
        removeTransferActions(historyTable);
        historyTable.getSelectionModel()
                .addListSelectionListener(e -> historyTableSelectionChanged());
        historyTable.getTableHeader().setReorderingAllowed(false);
        addTableCellClickedListener(historyTable, this::historyTableMouseClicked);
        historyTable.setShowGrid(true);
        historyTable.setGridColor(new Color(0xF0F0F0));
        historyTable.setDefaultRenderer(String.class, historyTableRowRenderer);
        historyTable.setModel(historyTableModel);
        centerTableHeaders(historyTable);
        resizeHistoryTableColumns();
        historyTableSelectionChanged();
    }

    private void initChangeList() {
        changesList.setCellRenderer(new ChangeListCellRenderer());
        changeListModel.addListDataListener(new ListChangedAdapter(
                this::changeListUpdated));
        changeListModel.addChange(new InitializationChange());
        changesList.setModel(changeListModel);
    }

    private void initBookmarksList() {
        bookmarksModel.addListDataListener(new ListChangedAdapter(
                this::enableBookmarkButtons));
        bookmarksList.setCellRenderer(new NoBorderListCellRenderer());
        bookmarksList.setModel(bookmarksModel);
    }

    private void createQuickMenus() {
        quickLoadProjectMenu.removeAll();
        quickSaveProjectMenu.removeAll();
        quickSaveStateInfos.clear();

        final String historiesDir = AppPrefs.getInstance().getPaths()
                .getHistoriesDir();
        mkdir(historiesDir);

        for (int i = 1; i <= 9; i++) {
            final int index = i - 1;
            final File file = createQuickSaveFile(historiesDir, projectName, i);
            final boolean fileExists = file.exists();
            final String name = fileExists ? getFileTimestamp(i, file)
                    : String.format("%d  ...                     ", i);
            final char key = Character.forDigit(i, 10);

            final JMenuItem quickLoadMenuItem = new JMenuItem(name, key);
            scaleMenuItemFont(quickLoadMenuItem);
            quickLoadMenuItem.setAccelerator(KeyStroke.getKeyStroke(key));
            quickLoadMenuItem.setEnabled(fileExists);
            quickLoadMenuItem.addActionListener(e -> quickLoadProject(index));
            quickLoadProjectMenu.add(quickLoadMenuItem);

            final JMenuItem quickSaveMenuItem = new JMenuItem(name, key);
            scaleMenuItemFont(quickSaveMenuItem);
            quickSaveMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.getExtendedKeyCodeForChar(key), InputEvent.SHIFT_MASK));
            quickSaveMenuItem.addActionListener(e -> quickSaveProject(index));
            quickSaveProjectMenu.add(quickSaveMenuItem);

            quickSaveStateInfos.add(new QuickSaveStateInfo(i, file,
                    file.lastModified(), quickLoadMenuItem, quickSaveMenuItem));
        }

        quickLoadProjectMenu.add(quickLoadSeparator);
        quickLoadProjectMenu.add(newestSlotMenuItem);
        quickSaveProjectMenu.add(quickSaveSeparator);
        quickSaveProjectMenu.add(oldestSlotMenuItem);

        findNewestAndOldestSaveSlots();
        enableSaveButtons();
    }

    private void createRecentProjectsMenu() {
        recentProjectsMenu.removeAll();
        final Paths paths = AppPrefs.getInstance().getPaths();
        java.util.List<String> fileNames = paths.getRecentHistoryProjects();
        if (fileNames.isEmpty()) {
            recentProjectsMenu.setEnabled(false);
        } else {
            recentProjectsMenu.setEnabled(true);
            synchronized (AppPrefs.class) {
                for (final String fileName : fileNames) {
                    final JMenuItem menuItem = new JMenuItem(getFileName(fileName));
                    scaleMenuItemFont(menuItem);
                    menuItem.addActionListener(
                            e -> recentProjectMenuItemPressed(fileName));
                    recentProjectsMenu.add(menuItem);
                }
            }
            recentProjectsMenu.add(recentProjectsSeparator);
            recentProjectsMenu.add(recentProjectsLockMenuItem);
            recentProjectsMenu.add(recentProjectsClearMenuItem);
        }
        recentProjectsLockMenuItem.setSelected(paths.isLockRecentHistoryProjects());
    }

    private void enableRecentDirectoriesMenu() {
        recentDirectoriesMenu.setEnabled(!AppPrefs.getInstance().getPaths()
                .getRecentDirectories().isEmpty());
    }

    private void createRecentDirectoriesMenu() {
        recentDirectoriesMenu.removeAll();
        final Paths paths = AppPrefs.getInstance().getPaths();
        final java.util.List<String> directories = paths.getRecentDirectories();
        if (!directories.isEmpty()) {
            synchronized (AppPrefs.class) {
                for (final String directory : directories) {
                    final JMenuItem menuItem = new JMenuItem(directory);
                    scaleMenuItemFont(menuItem);
                    menuItem.addActionListener(e -> load(directory));
                    recentDirectoriesMenu.add(menuItem);
                }
            }
            recentDirectoriesMenu.add(recentDirectoriesSeparator);
            recentDirectoriesMenu.add(recentDirectoriesLockMenuItem);
            recentDirectoriesMenu.add(recentDirectoriesClearMenuItem);
        }
        recentDirectoriesLockMenuItem.setSelected(
                paths.isLockRecentDirectories());
    }

    private void recentProjectMenuItemPressed(final String fileName) {
        quickLoadProject(new File(fileName), App.getEntryFileName(), false);
    }

    private File createQuickSaveFile(final String historiesDir,
                                     final String projectName, final int index) {
        return new File(historiesDir, String.format("%s.history-%d",
                projectName, index));
    }

    private void renameQuickSaveFiles(final String newProjectName) {

        final String historiesDir = AppPrefs.getInstance().getPaths()
                .getHistoriesDir();
        mkdir(historiesDir);

        for (int i = quickSaveStateInfos.size() - 1; i >= 0; i--) {

            final QuickSaveStateInfo info = quickSaveStateInfos.get(i);
            final File oldFile = info.getFile();
            final File newFile = createQuickSaveFile(historiesDir, newProjectName,
                    info.getSlot());

            if (oldFile.exists() && !oldFile.equals(newFile)) {
                oldFile.renameTo(newFile);
            }

            quickSaveStateInfos.set(i, new QuickSaveStateInfo(info.getSlot(),
                    newFile, info.getModifiedTime(), info.getLoadMenuItem(),
                    info.getSaveMenuItem()));
        }
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
            newestSlotMenuItem.addActionListener(
                    e -> quickLoadProject(info.getSlot() - 1));
        } else {
            newestSlotMenuItem.setEnabled(false);
        }

        removeAllActionListeners(oldestSlotMenuItem);
        final QuickSaveStateInfo info = oldest;
        oldestSlotMenuItem.addActionListener(
                e -> quickSaveProject(info.getSlot() - 1));
    }

    private void removeAllActionListeners(final AbstractButton button) {
        for (final ActionListener listener : button.getActionListeners()) {
            button.removeActionListener(listener);
        }
    }

    private void resizeHistoryTableColumns() {
        final Object[] prototypes
                = new Object[(historyTableModel.getPlayers() << 3) + 2];
        prototypes[0] = "\u25A0\u25BA\u25BA";
        prototypes[1] = "MMMMMMM";
        Arrays.fill(prototypes, 2, prototypes.length, "M");
        resizeCellSizes(historyTable, false, true, 16, false, prototypes);
    }

    private boolean isBusy() {
        return executing || disking;
    }

    private void loadFields() {
        applyPreferences(AppPrefs.getInstance().getHistoryEditorPrefs());
    }

    private void applyPreferences(final HistoryEditorPrefs prefs) {
        fastGenerationCheckBox.setSelected(prefs.getFastGeneration());
        mergeButtonsCheckBox.setSelected(prefs.getMerge());
        restorePositionCheckBox.setSelected(prefs.getRestorePosition());
        trackCursorCheckBox.setSelected(prefs.getTrackCursor());

        final boolean[] recordPlayers = prefs.getRecordPlayers();
        record1PCheckBox.setSelected(recordPlayers[0]);
        record2PCheckBox.setSelected(recordPlayers[1]);
        record3PCheckBox.setSelected(recordPlayers[2]);
        record4PCheckBox.setSelected(recordPlayers[3]);

        final boolean[] viewPlayers = prefs.getViewPlayers();
        view1PCheckBox.setSelected(viewPlayers[0]);
        view2PCheckBox.setSelected(viewPlayers[1]);
        view3PCheckBox.setSelected(viewPlayers[2]);
        view4PCheckBox.setSelected(viewPlayers[3]);
        historyTableModel.setViewPlayers(viewPlayers);
    }

    private void saveFields() {
        capturePreferences(AppPrefs.getInstance().getHistoryEditorPrefs());
        AppPrefs.save();
    }

    private void closeProject() {
        cancelAllTasks();
        final int startIndex = historyTableModel.getTailIndex();
        final int endIndex = historyTableModel.getHeadIndex();
        resetProgressBar(startIndex, endIndex);
        taskScheduler.add(new NewProjectTask(movie, startIndex, endIndex,
                historyTableModel, this));
    }

    public void createNewProject(final byte[] saveState) {
        historyTableModel.clear(saveState);
        priorSaveFile = null;
        generateDefaultProjectName();
        resetProgressBar();
        saveSlotsComboBox.setSelectedIndex(0);
        createQuickMenus();
    }

    private HistoryEditorPrefs createHistoryEditorPrefs() {
        final HistoryEditorPrefs prefs = new HistoryEditorPrefs();
        capturePreferences(prefs);
        return prefs;
    }

    private void capturePreferences(final HistoryEditorPrefs prefs) {
        prefs.setFastGeneration(fastGenerationCheckBox.isSelected());
        prefs.setMerge(mergeButtonsCheckBox.isSelected());
        prefs.setRestorePosition(restorePositionCheckBox.isSelected());
        prefs.setTrackCursor(trackCursorCheckBox.isSelected());
        prefs.setRecordPlayers(getRecordPlayers());
        prefs.setViewPlayers(getViewPlayers());
    }

    private HistoryProject createHistoryProject() {
        final HistoryProject project = new HistoryProject();
        captureHistoryProject(project);
        return project;
    }

    private void captureHistoryProject(final HistoryProject project) {
        project.setEntryFileName(App.getEntryFileName());
        final CartFile cartFile = App.getCartFile();
        project.setEntryFileCRC(cartFile != null ? cartFile.getFileCRC() : 0);
        project.setBookmarks(bookmarksModel.getBookmarks());
        project.setChanges(changeListModel.getChanges());
        project.setChangesIndex(changeListModel.getChangesIndex());
        project.setHistoryEditorPrefs(createHistoryEditorPrefs());
        project.setLastClickedRowIndex(lastClickedRowIndex);
        project.setMovie(movie);
        project.setHeadIndex(historyTableModel.getHeadIndex());
        project.setLastIndex(historyTableModel.getLastIndex());
        project.setTailIndex(historyTableModel.getTailIndex());
        project.setHistoryScrollValues(getScrollValues(historyScrollPane));
        project.setBookmarksScrollValues(getScrollValues(bookmarksScrollPane));
        project.setChangesScrollValues(getScrollValues(changesScrollPane));
    }

    private void applyHistoryProject(final HistoryProject project) {
        lastClickedRowIndex = project.getLastClickedRowIndex();
        this.movie = project.getMovie();
        changeListModel.setChanges(project.getChanges(), project.getChangesIndex());
        bookmarksModel.setBookmarks(project.getBookmarks());
        historyTableModel.setMovie(project.getMovie());
        historyTableModel.setHeadIndex(project.getHeadIndex());
        historyTableModel.setLastIndex(project.getLastIndex());
        historyTableModel.setTailIndex(project.getTailIndex());
        historyTableModel.setBookmarks(bookmarksModel.getBookmarkedRows());
        applyPreferences(project.getHistoryEditorPrefs());
        resizeHistoryTableColumns();
        setScrollValues(historyScrollPane, project.getHistoryScrollValues());
        setScrollValues(bookmarksScrollPane, project.getBookmarksScrollValues());
        setScrollValues(changesScrollPane, project.getChangesScrollValues());
    }

    public void destroy() {
        saveFields();
        taskScheduler.dispose();
        dispose();
    }

    public void closeFrame() {
        App.destroyHistoryEditorFrame();
    }

    public void movieUpdated(final int frameIndex) {
        EDT.async(() -> historyTableModel.fireTableRowsUpdated(frameIndex, frameIndex));
    }

    private void updateViewPlayers() {
        final boolean[] viewPlayers = new boolean[4];
        viewPlayers[0] = view1PCheckBox.isSelected();
        viewPlayers[1] = view2PCheckBox.isSelected();
        viewPlayers[2] = view3PCheckBox.isSelected();
        viewPlayers[3] = view4PCheckBox.isSelected();
        historyTableModel.setViewPlayers(viewPlayers);
        resizeHistoryTableColumns();
    }

    private void historyTableMouseClicked(final int rowIndex,
                                          final int columnIndex) {

        if (isBusy()) {
            return;
        }

        if (columnIndex == 0) {
            moveHeadToRow(rowIndex);
        } else if (columnIndex >= 2) {
            cancelAllTasks();
            historyTableModel.toggleButton(rowIndex, columnIndex);
            runToLastClickedRow(rowIndex);
        }
    }

    private void moveHeadToRow(final int rowIndex) {

        if (isBusy()) {
            return;
        }

        cancelAllTasks();

        lastClickedRowIndex = rowIndex;
        if (rowIndex > historyTableModel.getTailIndex()) {
            resetProgressBar(historyTableModel.getTailIndex(), rowIndex);
            playMovieTask = new PlayMovieTask(movie, historyTableModel.getTailIndex(),
                    rowIndex, !fastGenerationCheckBox.isSelected(), this);
            taskScheduler.add(playMovieTask);
        }
        taskScheduler.add(new RenderScreenTask(movie, rowIndex, true));
        taskScheduler.add(new RenderScreenTask(movie, rowIndex - BLOCK_SIZE,
                false));
        historyTableModel.setHeadIndex(rowIndex);
    }

    public void runToLastClickedRow(int modifiedRowIndex) {
        if (lastClickedRowIndex < 0
                || lastClickedRowIndex >= historyTableModel.getRowCount()) {
            lastClickedRowIndex = historyTableModel.getRowCount() - 1;
        }
        if (modifiedRowIndex < 0
                || modifiedRowIndex >= historyTableModel.getRowCount()) {
            modifiedRowIndex = historyTableModel.getRowCount() - 1;
        }
        if (historyTableModel.isEmpty()) {
            resetProgressBar();
        } else if (modifiedRowIndex <= lastClickedRowIndex) {
            historyTableModel.setLastIndex(lastClickedRowIndex);
            final int lastRowIndex = restorePositionCheckBox.isSelected()
                    ? lastClickedRowIndex : modifiedRowIndex;
            resetProgressBar(historyTableModel.getTailIndex(), lastRowIndex);
            playMovieTask = new PlayMovieTask(movie, historyTableModel.getTailIndex(),
                    lastRowIndex, !fastGenerationCheckBox.isSelected(), this);
            taskScheduler.add(playMovieTask);
            taskScheduler.add(new RenderScreenTask(movie, lastRowIndex, true));
            taskScheduler.add(new RenderScreenTask(movie, lastRowIndex - BLOCK_SIZE,
                    false));
        }
    }

    private void cancelAllTasks() {
        taskScheduler.cancelAll();
        taskScheduler.waitForReady();
        resetProgressBar();
    }

    private void resetProgressBar() {
        resetProgressBar(0, 100);
    }

    private void resetProgressBar(final int min, final int max) {
        progressBar.setMinimum(max(0, min));
        progressBar.setMaximum(max(0, max));
        progressBar.setValue(max(0, min));
    }

    public void setProgressBar(final Task task, final int value) {
        EDT.async(() -> {
            if (!task.isCanceled()) {
                progressBar.setValue(value);
            }
        });
    }

    @Override
    public void framePlayed(final Task task, final int frameIndex,
                            final MachineRunner machineRunner) {
        EDT.async(() -> {
            if (!task.isCanceled()) {
                progressBar.setValue(frameIndex);
                historyTableModel.setHeadIndex(frameIndex);
                if (historyTableModel.getTailIndex() < frameIndex) {
                    historyTableModel.setTailIndex(frameIndex);
                }
            }
        });
    }

    public void handleRewoundFrame(final Task task, final int frameIndex) {
        EDT.async(() -> {
            if (!task.isCanceled()) {
                historyTableModel.setHeadIndex(frameIndex);
                scrollTo(frameIndex);
            }
        });
    }

    public void handleRecordedFrame(final Task task, final int frameIndex,
                                    final boolean newFrame) {
        EDT.async(() -> {
            if (!task.isCanceled()) {
                if (newFrame) {
                    historyTableModel.fireTableRowsInserted(frameIndex, frameIndex);
                }
                historyTableModel.setHeadIndex(frameIndex);
                historyTableModel.setTailIndex(frameIndex);
                scrollTo(frameIndex);
            }
        });
    }

    private void toggleRecording() {
        cancelAllTasks();
        lastClickedRowIndex = -1;
        setExecuting(!executing);
        enableComponents();
        if (executing) {
            priorRowCount = historyTableModel.getRowCount();
            priorHeadIndex = historyTableModel.getHeadIndex();
            recordTask = new RecordTask(movie, priorHeadIndex, this);
            recordTask.setRecordOptions(getRecordPlayers(),
                    recordOtherCheckBox.isSelected(), mergeButtonsCheckBox.isSelected());
            taskScheduler.add(recordTask);
            App.getImageFrame().requestFocus();
        } else {
            recordTask = null;
        }
    }

    public void handleEndRecord(final int[] priorButtons) {
        EDT.async(() -> {
            historyTableModel.handleEndRecord(priorButtons, priorRowCount,
                    priorHeadIndex);
            priorRowCount = historyTableModel.getRowCount();
            priorHeadIndex = historyTableModel.getHeadIndex();
        });
    }

    private boolean[] getRecordPlayers() {
        final boolean[] recordPlayers = new boolean[4];
        recordPlayers[0] = record1PCheckBox.isSelected();
        recordPlayers[1] = record2PCheckBox.isSelected();
        recordPlayers[2] = record3PCheckBox.isSelected();
        recordPlayers[3] = record4PCheckBox.isSelected();
        return recordPlayers;
    }

    private boolean[] getViewPlayers() {
        final boolean[] viewPlayers = new boolean[4];
        viewPlayers[0] = view1PCheckBox.isSelected();
        viewPlayers[1] = view2PCheckBox.isSelected();
        viewPlayers[2] = view3PCheckBox.isSelected();
        viewPlayers[3] = view4PCheckBox.isSelected();
        return viewPlayers;
    }

    private void enableComponents() {
        final boolean notExecuting = !isBusy();
        final boolean notEmpty = !historyTableModel.isEmpty();
        final boolean notExecutingEmpty = notExecuting && notEmpty;

        cancelButton.setEnabled(notExecuting);
        progressBar.setEnabled(notExecuting);

        previousFrameButton.setEnabled(notExecutingEmpty);
        nextFrameButton.setEnabled(notExecutingEmpty);
        headButton.setEnabled(notExecutingEmpty);

        menuBar.setEnabled(notExecuting);

        enableFileMenuItems();
        enableEditMenuItems();
        enableBookmarkButtons();
        enableChangeButtons();
        enableSaveButtons();
    }

    private void enableFileMenuItems() {

        final boolean notExecuting = !isBusy();
        final boolean notEmpty = !historyTableModel.isEmpty();
        final boolean notExecutingEmpty = notExecuting && notEmpty;

        fileMenu.setEnabled(notExecuting);

        loadMenuItem.setEnabled(notExecuting);
        saveMenuItem.setEnabled(notExecutingEmpty);
        saveAsMenuItem.setEnabled(notExecutingEmpty);

        enableRecentDirectoriesMenu();
    }

    private void enableEditMenuItems() {

        final boolean notExecuting = !isBusy();
        final boolean notEmpty = !historyTableModel.isEmpty();
        final boolean notExecutingEmpty = notExecuting && notEmpty;
        final boolean rowsSelected = historyTable.getSelectedRow() >= 0;

        final String value = getClipboardString();
        final boolean copyEnabled = notExecutingEmpty && rowsSelected;
        final boolean pasteEnabled = notExecutingEmpty
                && value != null && value.length() >= 32;

        editMenu.setEnabled(notExecuting);

        insertFrameMenuItem.setEnabled(notExecuting);
        insertFramesMenuItem.setEnabled(notExecuting);
        appendFrameMenuItem.setEnabled(notExecuting);
        appendFramesMenuItem.setEnabled(notExecuting);

        selectAllMenuItem.setEnabled(notExecutingEmpty);
        selectBetweenBookmarksMenuItem.setEnabled(notExecutingEmpty);

        deselectMenuItem.setEnabled(copyEnabled);
        trimTopMenuItem.setEnabled(copyEnabled);
        trimBottomMenuItem.setEnabled(copyEnabled);
        copyMenuItem.setEnabled(copyEnabled);
        cutMenuItem.setEnabled(copyEnabled);
        clearMenuItem.setEnabled(copyEnabled);
        deleteMenuItem.setEnabled(copyEnabled);
        insertCloneMenuItem.setEnabled(copyEnabled);
        appendCloneMenuItem.setEnabled(copyEnabled);

        pasteMenuItem.setEnabled(pasteEnabled);
        mergeMenuItem.setEnabled(pasteEnabled);
        insertPasteMenuItem.setEnabled(pasteEnabled);
        appendPasteMenuItem.setEnabled(pasteEnabled);
    }

    private void enableChangeButtons() {
        final boolean notExecuting = !isBusy();
        final boolean notEmpty = !historyTableModel.isEmpty();
        final boolean notExecutingEmpty = notExecuting && notEmpty;

        final int changesIndex = changeListModel.getChangesIndex();
        final boolean undoEnabled = changesIndex > 1 && notExecutingEmpty;
        final boolean redoEnabled = changesIndex < changeListModel.getSize()
                && notExecutingEmpty;
        historyPanel.setEnabled(notExecutingEmpty);
        changesList.setEnabled(notExecutingEmpty);
        clearButton.setEnabled(notExecutingEmpty && changeListModel.getSize() > 1);
        undoButton.setEnabled(undoEnabled);
        undoMenuItem.setEnabled(undoEnabled);
        redoButton.setEnabled(redoEnabled);
        redoMenuItem.setEnabled(redoEnabled);
    }

    private void enableSaveButtons() {
        final boolean notExecuting = !isBusy();
        final boolean notEmpty = !historyTableModel.isEmpty();
        final boolean notExecutingEmpty = notExecuting && notEmpty;

        statesPanel.setEnabled(notExecutingEmpty);
        saveSlotsComboBox.setEnabled(notExecutingEmpty);
        saveButton.setEnabled(notExecutingEmpty);

        boolean fileExists = false;
        final int index = saveSlotsComboBox.getSelectedIndex();
        if (index >= 0 && index < quickSaveStateInfos.size()) {
            fileExists = quickSaveStateInfos.get(index).getFile().exists();
        }
        loadButton.setEnabled(notExecutingEmpty && fileExists);
    }

    private void enableBookmarkButtons() {
        final boolean notExecuting = !isBusy();
        final boolean notEmpty = !historyTableModel.isEmpty();
        final boolean notExecutingEmpty = notExecuting && notEmpty;

        final int index = bookmarksList.getSelectedIndex();
        final boolean hasBookmarks = !bookmarksModel.isEmpty() && notExecutingEmpty;
        final boolean bookmarkSelected = index >= 0 && notExecutingEmpty;
        previousBookmarkButton.setEnabled(hasBookmarks);
        nextBookmarkButton.setEnabled(hasBookmarks);
        seekBookmarkButton.setEnabled(bookmarkSelected);
        addBookmarkButton.setEnabled(notExecutingEmpty);
        editBookmarkButton.setEnabled(bookmarkSelected);
        deleteBookmarkButton.setEnabled(bookmarkSelected);
        bookmarksList.setEnabled(notExecutingEmpty);
        bookmarksPanel.setEnabled(notExecutingEmpty);
    }

    private void setExecuting(final boolean executing) {
        this.executing = executing;
        pauseToggleButton.setSelected(executing);
        pauseToggleButton.setText(executing ? "Pause" : "Resume");
        enableComponents();
    }

    private void modifyHeadIndex(final int delta) {

        if (isBusy()) {
            return;
        }

        final int rowCount = historyTableModel.getRowCount();
        if (rowCount == 0) {
            return;
        }

        final int rowIndex = clamp(historyTableModel.getHeadIndex() + delta, 0,
                rowCount - 1);
        historyTableMouseClicked(rowIndex, 0);
        scrollTo(rowIndex);
    }

    private void scrollTo(final int rowIndex) {
        if (trackCursorCheckBox.isSelected()) {
            scrollToVisible(historyTable, rowIndex);
        }
    }

    private void historyTableSelectionChanged() {
        final boolean rowsSelected = historyTable.getSelectedRowCount() > 0;
        cutMenuItem.setEnabled(rowsSelected);
        copyMenuItem.setEnabled(rowsSelected);
    }

    private void copySelectedRows() {

        if (isBusy()) {
            return;
        }

        final int[] rows = historyTable.getSelectedRows();
        if (rows.length > 0) {
            Arrays.sort(rows);
            final int minRow = rows[0];
            final int maxRow = rows[rows.length - 1];
            final StringBuilder sb = new StringBuilder();
            for (int i = minRow, r = 0; i <= maxRow; i++) {
                if (i == rows[r]) {
                    int buttons = historyTableModel.getButtons(i);
                    for (int j = 0; j < 32; j++) {
                        sb.append((buttons & 1) == 1 ? HistoryTableModel.BUTTON_NAMES[j & 7]
                                : "-");
                        buttons >>= 1;
                    }
                    r++;
                }
                sb.append(LINE_SEPARATOR);
            }
            setClipboardString(sb);
        }
    }

    private void clearSelectedRows() {

        if (isBusy()) {
            return;
        }

        final int rowIndex = historyTable.getSelectedRow();
        if (rowIndex >= 0) {
            cancelAllTasks();
            historyTableModel.clearButtons(historyTable.getSelectedRows());
            runToLastClickedRow(rowIndex);
        }
    }

    private String[] getClipboardLines() {
        final String value = getClipboardString();
        if (value == null) {
            return null;
        }

        final String[] lines = value.split("\\s");
        for (int i = lines.length - 1; i >= 0; i--) {
            final String line = lines[i];
            if (!line.isEmpty() && line.length() != 32) {
                return null;
            }
        }

        return lines;
    }

    private int convertClipboardLine(final String line) {
        int b = 0;
        for (int j = 31; j >= 0; j--) {
            b = (b << 1) | (line.charAt(j) != '-' ? 1 : 0);
        }
        return b;
    }

    private int[] getIntClipboardButtons() {
        final String[] lines = getClipboardLines();
        final int[] buttons = new int[lines.length];
        for (int i = buttons.length - 1; i >= 0; i--) {
            final String line = lines[i];
            if (line.isEmpty()) {
                buttons[i] = 0;
            } else {
                buttons[i] = convertClipboardLine(line);
            }
        }

        return buttons;
    }

    private Integer[] getClipboardButtons() {
        final String[] lines = getClipboardLines();
        if (lines == null) {
            return null;
        }

        final Integer[] buttons = new Integer[lines.length];
        for (int i = buttons.length - 1; i >= 0; i--) {
            final String line = lines[i];
            if (line.isEmpty()) {
                buttons[i] = null;
            } else {
                buttons[i] = convertClipboardLine(line);
            }
        }

        return buttons;
    }

    private void pasteRows(final boolean merge) {

        if (isBusy()) {
            return;
        }

        final int rowIndex = historyTable.getSelectedRow();
        if (rowIndex < 0 || rowIndex >= historyTableModel.getRowCount()) {
            return;
        }

        final Integer[] buttons = getClipboardButtons();
        if (buttons == null) {
            return;
        }

        cancelAllTasks();
        historyTableModel.pasteButtons(rowIndex, buttons, merge);
        selectIntervals(rowIndex, buttons);
        runToLastClickedRow(rowIndex);
    }

    private void insertRows(int rowIndex, final int rowCount) {

        if (isBusy() || rowCount == 0) {
            return;
        }

        if (rowIndex < 0) {
            rowIndex = historyTable.getRowCount();
        }

        cancelAllTasks();
        historyTableModel.insertButtons(rowIndex, rowCount);
        final int lastRowIndex = rowIndex + rowCount - 1;
        historyTable.getSelectionModel().setSelectionInterval(rowIndex,
                lastRowIndex);
        EDT.async(() -> scrollToVisible(historyTable, lastRowIndex));
        runToLastClickedRow(rowIndex);
    }

    private void insertRows() {
        insertRows(historyTable.getSelectedRow());
    }

    private void insertRows(int rowIndex) {

        if (isBusy()) {
            return;
        }

        if (rowIndex < 0) {
            rowIndex = historyTable.getRowCount();
        }

        final Integer[] buttons = getClipboardButtons();
        if (buttons == null) {
            return;
        }

        cancelAllTasks();
        historyTableModel.insertButtons(rowIndex, toIntArray(buttons));
        selectIntervals(rowIndex, buttons);
        final int lastRowIndex = rowIndex + buttons.length - 1;
        EDT.async(() -> scrollToVisible(historyTable, lastRowIndex));
        runToLastClickedRow(rowIndex);
    }

    private void selectInterval(final int rowIndex, final int length) {
        final ListSelectionModel selectionModel = historyTable.getSelectionModel();
        selectionModel.clearSelection();
        if (length > 0) {
            selectionModel.addSelectionInterval(rowIndex, rowIndex + length - 1);
        }
    }

    private void selectIntervals(final int rowIndex, final Integer[] buttons) {
        final ListSelectionModel selectionModel = historyTable.getSelectionModel();
        selectionModel.clearSelection();

        int startIndex = -1;
        for (int i = 0; i <= buttons.length; i++) {
            if (i == buttons.length || buttons[i] == null) {
                if (startIndex >= 0) {
                    selectionModel.addSelectionInterval(rowIndex + startIndex,
                            rowIndex + i - 1);
                    startIndex = -1;
                }
            } else if (startIndex < 0) {
                startIndex = i;
            }
        }
    }

    private void deleteSelectedRows() {

        if (isBusy()) {
            return;
        }

        final int rowIndex = historyTable.getSelectedRow();
        if (rowIndex < 0) {
            return;
        }

        cancelAllTasks();
        final DeleteChange deleteChange = historyTableModel.deleteButtons(
                historyTable.getSelectedRows());
        historyTable.clearSelection();
        if (deleteChange == null) {
            runToLastClickedRow(rowIndex);
        } else {
            resetProgressBar(deleteChange.getRowIndex(),
                    deleteChange.getRowCount() - 1);
            final int tailIndex = historyTableModel.getTailIndex();
            historyTableModel.setHeadIndex(-1);
            historyTableModel.setTailIndex(-1);
            taskScheduler.add(new TrimTopTask(movie, tailIndex,
                    deleteChange.getRowCount() - 1, historyTableModel, this));
        }
    }

    private void trimTop() {

        if (isBusy()) {
            return;
        }

        final int endFrameIndex = historyTable.getSelectedRow();
        if (endFrameIndex < 0) {
            return;
        }

        cancelAllTasks();
        historyTable.clearSelection();
        final TrimTopTask task = new TrimTopTask(movie,
                historyTableModel.getTailIndex(), endFrameIndex, historyTableModel,
                this);
        resetProgressBar(task.getFrameIndex(), endFrameIndex);
        historyTableModel.setHeadIndex(-1);
        historyTableModel.setTailIndex(-1);
        taskScheduler.add(task);
    }

    private void trimBottom() {

        if (isBusy()) {
            return;
        }

        final int rowIndex = historyTable.getSelectedRow();
        if (rowIndex < 0) {
            return;
        }

        cancelAllTasks();
        historyTableModel.trimBottom(rowIndex);
        historyTable.clearSelection();
        runToLastClickedRow(rowIndex);
    }

    private void insertFrames(final boolean insert) {

        if (isBusy()) {
            return;
        }

        while (true) {
            final InputDialog dialog = new InputDialog(this,
                    "Enter the number of frames to " + (insert ? "insert" : "append")
                            + ".", (insert ? "Insert" : "Append") + " Frames");
            dialog.setVisible(true);
            if (dialog.isOk()) {
                final int rowCount = parseInt(dialog.getInput());
                if (rowCount <= 0) {
                    displayError(this, "The number of frames entered is invalid.");
                } else {
                    insertRows(insert ? historyTable.getSelectedRow() : -1, rowCount);
                    break;
                }
            } else {
                break;
            }
        }
    }

    private void insertClone(final boolean insert) {

        if (isBusy()) {
            return;
        }

        final int[] rows = historyTable.getSelectedRows();
        if (rows.length == 0) {
            return;
        }

        Arrays.sort(rows);
        final int minRow = rows[0];
        final int maxRow = rows[rows.length - 1];
        final Integer[] buttons = new Integer[maxRow - minRow + 1];
        for (int i = minRow, r = 0; i <= maxRow; i++) {
            if (i == rows[r]) {
                buttons[i - minRow] = historyTableModel.getButtons(i);
                r++;
            }
        }

        cancelAllTasks();
        final int rowIndex = insert ? minRow : historyTableModel.getRowCount();
        historyTableModel.insertButtons(rowIndex, toIntArray(buttons));
        selectIntervals(rowIndex, buttons);
        final int lastRowIndex = rowIndex + buttons.length - 1;
        EDT.async(() -> scrollToVisible(historyTable, lastRowIndex));
        runToLastClickedRow(rowIndex);
    }

    private void undo(final int undos) {

        if (isBusy() || undos < 1) {
            return;
        }

        cancelAllTasks();
        int rowIndex = Integer.MAX_VALUE;
        for (int i = 0; i < undos; i++) {
            final int row = historyTableModel.undo();
            if (row >= 0 && row < rowIndex) {
                rowIndex = row;
            }
        }
        if (rowIndex >= 0) {
            historyTable.clearSelection();
            runToLastClickedRow(rowIndex);
        }
    }

    private void redo(final int redos) {

        if (isBusy() || redos < 1) {
            return;
        }

        cancelAllTasks();
        int rowIndex = Integer.MAX_VALUE;
        for (int i = 0; i < redos; i++) {
            final int row = historyTableModel.redo();
            if (row >= 0 && row < rowIndex) {
                rowIndex = row;
            }
        }
        if (rowIndex >= 0) {
            historyTable.clearSelection();
            runToLastClickedRow(rowIndex);
        }
    }

    private void changeListUpdated() {
        enableChangeButtons();
        final int changesIndex = changeListModel.getChangesIndex();
        scrollToVisible(changesList, changesIndex - 1);
    }

    private void goToSelectedBookmark() {

        if (isBusy()) {
            return;
        }

        final int index = bookmarksList.getSelectedIndex();
        if (index >= 0 && index < bookmarksModel.getSize()) {
            goToBookmark(bookmarksModel.getElementAt(index));
        }
    }

    private void selectBookmark(final int index) {
        bookmarksList.getSelectionModel().setSelectionInterval(index, index);
    }

    private void goToBookmark(final HistoryBookmark bookmark) {

        if (isBusy() || bookmark == null) {
            return;
        }

        final int rowIndex = bookmark.getFrame();
        if (rowIndex >= 0 && rowIndex < historyTableModel.getRowCount()) {
            scrollToCenter(historyTable, rowIndex);
            moveHeadToRow(rowIndex);
        }
    }

    private void goToNextBookmark() {

        if (isBusy() || bookmarksModel.isEmpty()) {
            return;
        }

        final int size = bookmarksModel.getSize();
        if (size == 1) {
            selectBookmark(0);
            return;
        }

        final int headIndex = historyTableModel.getHeadIndex();
        int minFrame = Integer.MAX_VALUE;
        int minIndex = Integer.MIN_VALUE;
        int nextIndex = Integer.MIN_VALUE;
        int distance = Integer.MAX_VALUE;
        for (int i = size - 1; i >= 0; i--) {
            final HistoryBookmark bookmark = bookmarksModel.getElementAt(i);
            final int frame = bookmark.getFrame();
            if (frame < minFrame) {
                minIndex = i;
                minFrame = frame;
            }
            if (frame > headIndex && frame - headIndex < distance) {
                nextIndex = i;
                distance = frame - headIndex;
            }
        }
        if (nextIndex >= 0) {
            selectBookmark(nextIndex);
        } else if (minIndex >= 0) {
            selectBookmark(minIndex);
        }
    }

    private void goToPreviousBookmark() {

        if (isBusy() || bookmarksModel.isEmpty()) {
            return;
        }

        final int size = bookmarksModel.getSize();
        if (size == 1) {
            selectBookmark(0);
            return;
        }

        final int headIndex = historyTableModel.getHeadIndex();
        int maxFrame = Integer.MIN_VALUE;
        int maxIndex = Integer.MIN_VALUE;
        int previousIndex = Integer.MIN_VALUE;
        int distance = Integer.MAX_VALUE;
        for (int i = size - 1; i >= 0; i--) {
            final HistoryBookmark bookmark = bookmarksModel.getElementAt(i);
            final int frame = bookmark.getFrame();
            if (frame > maxFrame) {
                maxIndex = i;
                maxFrame = frame;
            }
            if (frame < headIndex && headIndex - frame < distance) {
                previousIndex = i;
                distance = headIndex - frame;
            }
        }
        if (previousIndex >= 0) {
            selectBookmark(previousIndex);
        } else if (maxIndex >= 0) {
            selectBookmark(maxIndex);
        }
    }

    private void recordOptionsChanged() {
        if (isBusy() && recordTask != null && !recordTask.isCanceled()) {
            recordTask.setRecordOptions(getRecordPlayers(),
                    recordOtherCheckBox.isSelected(),
                    mergeButtonsCheckBox.isSelected());
        }
    }

    private void clearChanges() {
        historyTableModel.clearChanges();
    }

    private void setProjectName(final String projectName) {
        if (!isBlank(projectName)) {
            this.projectName = projectName;
        }
    }

    private void saveHistoryProjectFile(final PleaseWaitDialog pleaseWaitDialog,
                                        final File file, final HistoryProject project,
                                        final boolean quickSave, final Runnable runnable) {

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
                priorSaveFile = file;
                setDisking(false);
                if (showError) {
                    displayError(this, "Failed to save history file.");
                } else {
                    if (!quickSave) {
                        AppPrefs.getInstance().getPaths()
                                .addRecentHistoryProject(file.getPath());
                        AppPrefs.save();
                    }
                    createRecentProjectsMenu();
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
        }
    }

    private void loadHistoryProjectFile(final PleaseWaitDialog pleaseWaitDialog,
                                        final File file, final String entryFileName, final boolean quickLoad) {

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
                priorSaveFile = file;
                setDisking(false);
                if (showError) {
                    displayError(this, "Failed to load history file.");
                } else {
                    createRecentProjectsMenu();
                    restoreHistoryProject(p, entryFileName,
                            removeExtension(file.getName()), file, quickLoad);
                }
            });
        }
    }

    private void quickLoadProject(final int index) {
        saveSlotsComboBox.setSelectedIndex(index);
        quickLoadProject(quickSaveStateInfos.get(index).getFile(),
                App.getEntryFileName(), true);
    }

    private void quickLoadProject(final File file, final String entryFileName,
                                  final boolean quickLoad) {

        if (isBusy()) {
            return;
        }

        cancelAllTasks();
        resetProgressBar();
        final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
        pleaseWaitDialog.setMessage("Loading history file...");
        setDisking(true);
        new Thread(() -> loadHistoryProjectFile(pleaseWaitDialog, file,
                entryFileName, quickLoad)).start();
        pleaseWaitDialog.showAfterDelay();
    }

    private void quickSaveProject(final int index) {

        if (isBusy()) {
            return;
        }

        saveSlotsComboBox.setSelectedIndex(index);
        final QuickSaveStateInfo info = quickSaveStateInfos.get(index);
        save(info.getFile(), true, () -> {
            final Date date = new java.util.Date();
            final String name = getFileTimestamp(info.getSlot(), date);
            final JMenuItem quickLoadMenuItem = info.getLoadMenuItem();
            final JMenuItem quickSaveMenuItem = info.getSaveMenuItem();
            quickLoadMenuItem.setEnabled(true);
            quickLoadMenuItem.setText(name);
            quickSaveMenuItem.setText(name);

            quickSaveStateInfos.set(index, new QuickSaveStateInfo(info.getSlot(),
                    info.getFile(), date.getTime(), quickLoadMenuItem,
                    quickSaveMenuItem));
            findNewestAndOldestSaveSlots();
        });
    }

    private void restoreHistoryProject(final HistoryProject project,
                                       final String entryFileName, final String loadedProjectName,
                                       final File file, final boolean quickLoad) {

        final CartFile cartFile = App.getCartFile();
        if ((cartFile != null && cartFile.getFileCRC() != project.getEntryFileCRC())
                || (cartFile == null && project.getEntryFileCRC() != 0)
                || !entryFileName.equalsIgnoreCase(project.getEntryFileName())) {
            final YesNoDialog dialog = new YesNoDialog(this, String.format(
                    "History game file: <pre>%s</pre><br/>"
                            + "Current game file: <pre>%s</pre><br/>"
                            + "Load history anyway?", project.getEntryFileName(),
                    entryFileName), "Game File Mismatch");
            dialog.setVisible(true);
            if (!dialog.isYes()) {
                return;
            }
        }

        if (!quickLoad) {
            AppPrefs.getInstance().getPaths().addRecentHistoryProject(file.getPath());
            AppPrefs.save();
        }

        setProjectName(loadedProjectName);
        createQuickMenus();
        createRecentProjectsMenu();
        applyHistoryProject(project);
    }

    private void setDisking(final boolean disking) {
        this.disking = disking;
        enableComponents();
    }

    public void saveAs() {
        if (isBusy()) {
            return;
        }

        final Paths paths = AppPrefs.getInstance().getPaths();
        final String fileName = projectName + ".history";
        final String historiesDir = paths.getHistoriesDir();

        mkdir(historiesDir);
        final File file = showSaveAsDialog(this, historiesDir,
                fileName, "history", historyFileExtensionFilter, true,
                "Save History File");
        if (file != null) {
            final String dir = file.getParent();
            paths.addRecentDirectory(dir);
            paths.setHistoriesDir(dir);
            AppPrefs.save();

            final String newProjectName = removeExtension(file.getName());
            setProjectName(newProjectName);
            renameQuickSaveFiles(newProjectName);
            save(file);
        }
    }

    private void save(final File file) {
        save(file, false, null);
    }

    private void save(final File file, final boolean quickSave,
                      final Runnable runnable) {

        if (file == null) {
            return;
        }

        final HistoryProject project = createHistoryProject();
        final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
        pleaseWaitDialog.setMessage("Saving history file...");
        setDisking(true);
        new Thread(() -> saveHistoryProjectFile(pleaseWaitDialog, file, project,
                quickSave, runnable)).start();
        pleaseWaitDialog.showAfterDelay();
    }

    public void load() {
        load(null);
    }

    private void load(final String directory) {

        if (isBusy()) {
            return;
        }

        final String entryFileName = App.getEntryFileName();
        if (isBlank(entryFileName)) {
            return;
        }

        final Paths paths = AppPrefs.getInstance().getPaths();
        final String historiesDir = paths.getHistoriesDir();
        mkdir(historiesDir);
        final JFileChooser chooser = createFileChooser("Load History File",
                directory != null ? directory : paths.getHistoriesDir(),
                historyFileExtensionFilter);
        if (showOpenDialog(this, chooser, (p, d) -> p.setHistoriesDir(d))
                == JFileChooser.APPROVE_OPTION) {
            quickLoadProject(chooser.getSelectedFile(), entryFileName, false);
        }
    }

    private void selectBetweenBookmarks() {

        if (isBusy()) {
            return;
        }

        if (bookmarksModel.isEmpty()) {
            historyTable.selectAll();
            return;
        }

        int selectedRowIndex = historyTable.getSelectedRow();
        if (selectedRowIndex < 0
                || selectedRowIndex >= historyTableModel.getRowCount()) {
            selectedRowIndex = historyTableModel.getHeadIndex();
            if (selectedRowIndex < 0
                    || selectedRowIndex >= historyTableModel.getRowCount()) {
                return;
            }
        }

        int aboveIndex = -1;
        int belowIndex = -1;
        int aboveDistance = Integer.MAX_VALUE;
        int belowDistance = Integer.MAX_VALUE;

        final int size = bookmarksModel.getSize();
        for (int i = size - 1; i >= -2; i--) {
            final int rowIndex;
            switch (i) {
                case -1:
                    rowIndex = historyTableModel.getRowCount();
                    break;
                case -2:
                    rowIndex = 0;
                    break;
                default:
                    rowIndex = bookmarksModel.getElementAt(i).getFrame();
                    break;
            }
            final int distance = selectedRowIndex - rowIndex;
            if (distance >= 0 && distance < aboveDistance) {
                aboveDistance = distance;
                aboveIndex = rowIndex;
            }
            if (distance < 0 && -distance < belowDistance) {
                belowDistance = -distance;
                belowIndex = rowIndex;
            }
        }

        historyTable.getSelectionModel().setSelectionInterval(aboveIndex,
                belowIndex - 1);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        historyScrollPane = new javax.swing.JScrollPane();
        historyTable = new javax.swing.JTable();
        viewPanel = new javax.swing.JPanel();
        view1PCheckBox = new javax.swing.JCheckBox();
        view2PCheckBox = new javax.swing.JCheckBox();
        view3PCheckBox = new javax.swing.JCheckBox();
        view4PCheckBox = new javax.swing.JCheckBox();
        headButton = new javax.swing.JButton();
        recordPanel = new javax.swing.JPanel();
        record1PCheckBox = new javax.swing.JCheckBox();
        record2PCheckBox = new javax.swing.JCheckBox();
        record3PCheckBox = new javax.swing.JCheckBox();
        record4PCheckBox = new javax.swing.JCheckBox();
        mergeButtonsCheckBox = new javax.swing.JCheckBox();
        recordOtherCheckBox = new javax.swing.JCheckBox();
        runPanel = new javax.swing.JPanel();
        nextFrameButton = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        cancelButton = new javax.swing.JButton();
        pauseToggleButton = new javax.swing.JToggleButton();
        previousFrameButton = new javax.swing.JButton();
        trackCursorCheckBox = new javax.swing.JCheckBox();
        fastGenerationCheckBox = new javax.swing.JCheckBox();
        restorePositionCheckBox = new javax.swing.JCheckBox();
        statesPanel = new javax.swing.JPanel();
        saveSlotsComboBox = new javax.swing.JComboBox();
        saveButton = new javax.swing.JButton();
        loadButton = new javax.swing.JButton();
        bookmarksPanel = new javax.swing.JPanel();
        bookmarksScrollPane = new javax.swing.JScrollPane();
        bookmarksList = new javax.swing.JList();
        addBookmarkButton = new javax.swing.JButton();
        editBookmarkButton = new javax.swing.JButton();
        deleteBookmarkButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        previousBookmarkButton = new javax.swing.JButton();
        nextBookmarkButton = new javax.swing.JButton();
        seekBookmarkButton = new javax.swing.JButton();
        historyPanel = new javax.swing.JPanel();
        changesScrollPane = new javax.swing.JScrollPane();
        changesList = new javax.swing.JList();
        undoButton = new javax.swing.JButton();
        redoButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        separator8 = new javax.swing.JPopupMenu.Separator();
        loadMenuItem = new javax.swing.JMenuItem();
        separator7 = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        separator4 = new javax.swing.JPopupMenu.Separator();
        quickLoadProjectMenu = new javax.swing.JMenu();
        quickLoadSeparator = new javax.swing.JPopupMenu.Separator();
        newestSlotMenuItem = new javax.swing.JMenuItem();
        quickSaveProjectMenu = new javax.swing.JMenu();
        quickSaveSeparator = new javax.swing.JPopupMenu.Separator();
        oldestSlotMenuItem = new javax.swing.JMenuItem();
        separator1 = new javax.swing.JPopupMenu.Separator();
        recentProjectsMenu = new javax.swing.JMenu();
        recentProjectsSeparator = new javax.swing.JPopupMenu.Separator();
        recentProjectsLockMenuItem = new javax.swing.JCheckBoxMenuItem();
        recentProjectsClearMenuItem = new javax.swing.JMenuItem();
        recentDirectoriesMenu = new javax.swing.JMenu();
        recentDirectoriesSeparator = new javax.swing.JPopupMenu.Separator();
        recentDirectoriesLockMenuItem = new javax.swing.JCheckBoxMenuItem();
        recentDirectoriesClearMenuItem = new javax.swing.JMenuItem();
        separator9 = new javax.swing.JPopupMenu.Separator();
        closeMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        separator2 = new javax.swing.JPopupMenu.Separator();
        deselectMenuItem = new javax.swing.JMenuItem();
        selectAllMenuItem = new javax.swing.JMenuItem();
        selectBetweenBookmarksMenuItem = new javax.swing.JMenuItem();
        separator3 = new javax.swing.JPopupMenu.Separator();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        mergeMenuItem = new javax.swing.JMenuItem();
        separator = new javax.swing.JPopupMenu.Separator();
        insertPasteMenuItem = new javax.swing.JMenuItem();
        insertFrameMenuItem = new javax.swing.JMenuItem();
        insertFramesMenuItem = new javax.swing.JMenuItem();
        insertCloneMenuItem = new javax.swing.JMenuItem();
        separator5 = new javax.swing.JPopupMenu.Separator();
        appendPasteMenuItem = new javax.swing.JMenuItem();
        appendFrameMenuItem = new javax.swing.JMenuItem();
        appendFramesMenuItem = new javax.swing.JMenuItem();
        appendCloneMenuItem = new javax.swing.JMenuItem();
        separator6 = new javax.swing.JPopupMenu.Separator();
        clearMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        trimTopMenuItem = new javax.swing.JMenuItem();
        trimBottomMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("History Editor");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        historyScrollPane.setMaximumSize(null);
        historyScrollPane.setMinimumSize(null);

        historyTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{

                }
        ));
        historyTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        historyScrollPane.setViewportView(historyTable);

        viewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("View"));

        view1PCheckBox.setSelected(true);
        view1PCheckBox.setText("1P");
        view1PCheckBox.setFocusPainted(false);
        view1PCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                view1PCheckBoxActionPerformed(evt);
            }
        });

        view2PCheckBox.setSelected(true);
        view2PCheckBox.setText("2P");
        view2PCheckBox.setFocusPainted(false);
        view2PCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                view2PCheckBoxActionPerformed(evt);
            }
        });

        view3PCheckBox.setText("3P");
        view3PCheckBox.setFocusPainted(false);
        view3PCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                view3PCheckBoxActionPerformed(evt);
            }
        });

        view4PCheckBox.setText("4P");
        view4PCheckBox.setFocusPainted(false);
        view4PCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                view4PCheckBoxActionPerformed(evt);
            }
        });

        headButton.setMnemonic('H');
        headButton.setText("Head");
        headButton.setFocusPainted(false);
        headButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout viewPanelLayout = new javax.swing.GroupLayout(viewPanel);
        viewPanel.setLayout(viewPanelLayout);
        viewPanelLayout.setHorizontalGroup(
                viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(viewPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(view1PCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(view2PCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(view3PCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(view4PCheckBox)
                                .addGap(18, 18, 18)
                                .addComponent(headButton)
                                .addContainerGap(95, Short.MAX_VALUE))
        );
        viewPanelLayout.setVerticalGroup(
                viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(view1PCheckBox)
                                .addComponent(view2PCheckBox)
                                .addComponent(view3PCheckBox)
                                .addComponent(view4PCheckBox)
                                .addComponent(headButton))
        );

        recordPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Record"));

        record1PCheckBox.setSelected(true);
        record1PCheckBox.setText("1P");
        record1PCheckBox.setFocusPainted(false);
        record1PCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                record1PCheckBoxActionPerformed(evt);
            }
        });

        record2PCheckBox.setSelected(true);
        record2PCheckBox.setText("2P");
        record2PCheckBox.setFocusPainted(false);
        record2PCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                record2PCheckBoxActionPerformed(evt);
            }
        });

        record3PCheckBox.setText("3P");
        record3PCheckBox.setFocusPainted(false);
        record3PCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                record3PCheckBoxActionPerformed(evt);
            }
        });

        record4PCheckBox.setText("4P");
        record4PCheckBox.setFocusPainted(false);
        record4PCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                record4PCheckBoxActionPerformed(evt);
            }
        });

        mergeButtonsCheckBox.setText("Merge");
        mergeButtonsCheckBox.setFocusPainted(false);
        mergeButtonsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeButtonsCheckBoxActionPerformed(evt);
            }
        });

        recordOtherCheckBox.setText("Other");
        recordOtherCheckBox.setFocusPainted(false);
        recordOtherCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recordOtherCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout recordPanelLayout = new javax.swing.GroupLayout(recordPanel);
        recordPanel.setLayout(recordPanelLayout);
        recordPanelLayout.setHorizontalGroup(
                recordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(recordPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(record1PCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(record2PCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(record3PCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(record4PCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(recordOtherCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                                .addComponent(mergeButtonsCheckBox)
                                .addContainerGap())
        );
        recordPanelLayout.setVerticalGroup(
                recordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(recordPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(recordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(record1PCheckBox)
                                        .addComponent(record2PCheckBox)
                                        .addComponent(record3PCheckBox)
                                        .addComponent(record4PCheckBox)
                                        .addComponent(mergeButtonsCheckBox)
                                        .addComponent(recordOtherCheckBox))
                                .addContainerGap())
        );

        runPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Run"));
        runPanel.setMaximumSize(null);

        nextFrameButton.setText("Frame+1");
        nextFrameButton.setFocusPainted(false);
        nextFrameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextFrameButtonActionPerformed(evt);
            }
        });

        progressBar.setMaximumSize(null);
        progressBar.setMinimumSize(null);
        progressBar.setPreferredSize(null);

        cancelButton.setMnemonic('C');
        cancelButton.setText("Cancel");
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        pauseToggleButton.setText("Pause");
        pauseToggleButton.setFocusPainted(false);
        pauseToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseToggleButtonActionPerformed(evt);
            }
        });

        previousFrameButton.setText("Frame-1");
        previousFrameButton.setFocusPainted(false);
        previousFrameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousFrameButtonActionPerformed(evt);
            }
        });

        trackCursorCheckBox.setSelected(true);
        trackCursorCheckBox.setText("Track cursor");
        trackCursorCheckBox.setFocusPainted(false);

        fastGenerationCheckBox.setSelected(true);
        fastGenerationCheckBox.setText("Fast generation");
        fastGenerationCheckBox.setFocusPainted(false);
        fastGenerationCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastGenerationCheckBoxActionPerformed(evt);
            }
        });

        restorePositionCheckBox.setSelected(true);
        restorePositionCheckBox.setText("Restore position");
        restorePositionCheckBox.setFocusPainted(false);

        javax.swing.GroupLayout runPanelLayout = new javax.swing.GroupLayout(runPanel);
        runPanel.setLayout(runPanelLayout);
        runPanelLayout.setHorizontalGroup(
                runPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(runPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(runPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(runPanelLayout.createSequentialGroup()
                                                .addComponent(previousFrameButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(pauseToggleButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(nextFrameButton))
                                        .addGroup(runPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addGroup(runPanelLayout.createSequentialGroup()
                                                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(cancelButton))
                                                .addGroup(runPanelLayout.createSequentialGroup()
                                                        .addComponent(trackCursorCheckBox)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(fastGenerationCheckBox)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(restorePositionCheckBox))))
                                .addContainerGap(28, Short.MAX_VALUE))
        );

        runPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, nextFrameButton, pauseToggleButton, previousFrameButton);

        runPanelLayout.setVerticalGroup(
                runPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(runPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(runPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(previousFrameButton)
                                        .addComponent(pauseToggleButton)
                                        .addComponent(nextFrameButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(runPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(trackCursorCheckBox)
                                        .addComponent(fastGenerationCheckBox)
                                        .addComponent(restorePositionCheckBox))
                                .addGroup(runPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(runPanelLayout.createSequentialGroup()
                                                .addGap(12, 12, 12)
                                                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, runPanelLayout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton)))
                                .addContainerGap())
        );

        statesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Slots"));

        saveSlotsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"}));
        saveSlotsComboBox.setFocusable(false);
        saveSlotsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSlotsComboBoxActionPerformed(evt);
            }
        });

        saveButton.setMnemonic('S');
        saveButton.setText("Save");
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        loadButton.setMnemonic('L');
        loadButton.setText("Load");
        loadButton.setFocusPainted(false);
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout statesPanelLayout = new javax.swing.GroupLayout(statesPanel);
        statesPanel.setLayout(statesPanelLayout);
        statesPanelLayout.setHorizontalGroup(
                statesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(statesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(saveSlotsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(loadButton)
                                .addContainerGap(161, Short.MAX_VALUE))
        );

        statesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, loadButton, saveButton);

        statesPanelLayout.setVerticalGroup(
                statesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(statesPanelLayout.createSequentialGroup()
                                .addGroup(statesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(saveSlotsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(saveButton)
                                        .addComponent(loadButton))
                                .addGap(0, 0, Short.MAX_VALUE))
        );

        bookmarksPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Bookmarks"));
        bookmarksPanel.setMaximumSize(null);

        bookmarksScrollPane.setMaximumSize(null);
        bookmarksScrollPane.setMinimumSize(null);
        bookmarksScrollPane.setPreferredSize(null);

        bookmarksList.setPreferredSize(null);
        bookmarksList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                bookmarksListValueChanged(evt);
            }
        });
        bookmarksScrollPane.setViewportView(bookmarksList);

        addBookmarkButton.setMnemonic('A');
        addBookmarkButton.setText("Add...");
        addBookmarkButton.setFocusPainted(false);
        addBookmarkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBookmarkButtonActionPerformed(evt);
            }
        });

        editBookmarkButton.setMnemonic('E');
        editBookmarkButton.setText("Edit...");
        editBookmarkButton.setToolTipText("");
        editBookmarkButton.setFocusPainted(false);
        editBookmarkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editBookmarkButtonActionPerformed(evt);
            }
        });

        deleteBookmarkButton.setMnemonic('D');
        deleteBookmarkButton.setText("Delete");
        deleteBookmarkButton.setFocusPainted(false);
        deleteBookmarkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteBookmarkButtonActionPerformed(evt);
            }
        });

        previousBookmarkButton.setMnemonic('P');
        previousBookmarkButton.setText("Previous");
        previousBookmarkButton.setFocusPainted(false);
        previousBookmarkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousBookmarkButtonActionPerformed(evt);
            }
        });

        nextBookmarkButton.setMnemonic('N');
        nextBookmarkButton.setText("Next");
        nextBookmarkButton.setFocusPainted(false);
        nextBookmarkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextBookmarkButtonActionPerformed(evt);
            }
        });

        seekBookmarkButton.setMnemonic('k');
        seekBookmarkButton.setText("Seek");
        seekBookmarkButton.setFocusPainted(false);
        seekBookmarkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seekBookmarkButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(previousBookmarkButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(seekBookmarkButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nextBookmarkButton)
                                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(previousBookmarkButton)
                                        .addComponent(nextBookmarkButton)
                                        .addComponent(seekBookmarkButton))
                                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout bookmarksPanelLayout = new javax.swing.GroupLayout(bookmarksPanel);
        bookmarksPanel.setLayout(bookmarksPanelLayout);
        bookmarksPanelLayout.setHorizontalGroup(
                bookmarksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(bookmarksPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(bookmarksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(bookmarksPanelLayout.createSequentialGroup()
                                                .addComponent(bookmarksScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(bookmarksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(addBookmarkButton)
                                                        .addComponent(editBookmarkButton)
                                                        .addComponent(deleteBookmarkButton)))
                                        .addGroup(bookmarksPanelLayout.createSequentialGroup()
                                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );

        bookmarksPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, addBookmarkButton, deleteBookmarkButton, editBookmarkButton);

        bookmarksPanelLayout.setVerticalGroup(
                bookmarksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bookmarksPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(bookmarksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(bookmarksPanelLayout.createSequentialGroup()
                                                .addComponent(addBookmarkButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(editBookmarkButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(deleteBookmarkButton)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(bookmarksPanelLayout.createSequentialGroup()
                                                .addComponent(bookmarksScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGap(6, 6, 6)))
                                .addContainerGap())
        );

        historyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("History"));

        changesScrollPane.setPreferredSize(null);

        changesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        changesList.setPreferredSize(null);
        changesList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                changesListMousePressed(evt);
            }
        });
        changesScrollPane.setViewportView(changesList);

        undoButton.setMnemonic('U');
        undoButton.setText("Undo");
        undoButton.setFocusPainted(false);
        undoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoButtonActionPerformed(evt);
            }
        });

        redoButton.setMnemonic('R');
        redoButton.setText("Redo");
        redoButton.setFocusPainted(false);
        redoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoButtonActionPerformed(evt);
            }
        });

        clearButton.setMnemonic('r');
        clearButton.setText("Clear");
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout historyPanelLayout = new javax.swing.GroupLayout(historyPanel);
        historyPanel.setLayout(historyPanelLayout);
        historyPanelLayout.setHorizontalGroup(
                historyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(historyPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(changesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(historyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(undoButton)
                                        .addComponent(redoButton)
                                        .addComponent(clearButton))
                                .addContainerGap())
        );

        historyPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, clearButton, redoButton, undoButton);

        historyPanelLayout.setVerticalGroup(
                historyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(historyPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(historyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(historyPanelLayout.createSequentialGroup()
                                                .addComponent(undoButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(redoButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(clearButton))
                                        .addComponent(changesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");
        fileMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                fileMenuMenuSelected(evt);
            }
        });

        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newMenuItem.setMnemonic('N');
        newMenuItem.setText("New Project");
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newMenuItem);
        fileMenu.add(separator8);

        loadMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        loadMenuItem.setMnemonic('L');
        loadMenuItem.setText("Load Project...");
        loadMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadMenuItem);
        fileMenu.add(separator7);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setText("Save Project");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setMnemonic('A');
        saveAsMenuItem.setText("Save Project As...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(separator4);

        quickLoadProjectMenu.setMnemonic('o');
        quickLoadProjectMenu.setText("Quick Load Project");
        quickLoadProjectMenu.add(quickLoadSeparator);

        newestSlotMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, 0));
        newestSlotMenuItem.setMnemonic('N');
        newestSlotMenuItem.setText("Newest Slot");
        quickLoadProjectMenu.add(newestSlotMenuItem);

        fileMenu.add(quickLoadProjectMenu);

        quickSaveProjectMenu.setMnemonic('v');
        quickSaveProjectMenu.setText("Quick Save Project");
        quickSaveProjectMenu.add(quickSaveSeparator);

        oldestSlotMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, java.awt.event.InputEvent.SHIFT_MASK));
        oldestSlotMenuItem.setMnemonic('O');
        oldestSlotMenuItem.setText("Oldest Slot");
        quickSaveProjectMenu.add(oldestSlotMenuItem);

        fileMenu.add(quickSaveProjectMenu);
        fileMenu.add(separator1);

        recentProjectsMenu.setMnemonic('P');
        recentProjectsMenu.setText("Recent Projects");
        recentProjectsMenu.add(recentProjectsSeparator);

        recentProjectsLockMenuItem.setMnemonic('L');
        recentProjectsLockMenuItem.setText("Lock");
        recentProjectsLockMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentProjectsLockMenuItemActionPerformed(evt);
            }
        });
        recentProjectsMenu.add(recentProjectsLockMenuItem);

        recentProjectsClearMenuItem.setMnemonic('C');
        recentProjectsClearMenuItem.setText("Clear");
        recentProjectsClearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentProjectsClearMenuItemActionPerformed(evt);
            }
        });
        recentProjectsMenu.add(recentProjectsClearMenuItem);

        fileMenu.add(recentProjectsMenu);

        recentDirectoriesMenu.setMnemonic('D');
        recentDirectoriesMenu.setText("Recent Directories");
        recentDirectoriesMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                recentDirectoriesMenuMenuSelected(evt);
            }
        });
        recentDirectoriesMenu.add(recentDirectoriesSeparator);

        recentDirectoriesLockMenuItem.setMnemonic('L');
        recentDirectoriesLockMenuItem.setText("Lock");
        recentDirectoriesLockMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentDirectoriesLockMenuItemActionPerformed(evt);
            }
        });
        recentDirectoriesMenu.add(recentDirectoriesLockMenuItem);

        recentDirectoriesClearMenuItem.setMnemonic('C');
        recentDirectoriesClearMenuItem.setText("Clear");
        recentDirectoriesClearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentDirectoriesClearMenuItemActionPerformed(evt);
            }
        });
        recentDirectoriesMenu.add(recentDirectoriesClearMenuItem);

        fileMenu.add(recentDirectoriesMenu);
        fileMenu.add(separator9);

        closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        closeMenuItem.setMnemonic('C');
        closeMenuItem.setText("Close");
        fileMenu.add(closeMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('E');
        editMenu.setText("Edit");
        editMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                editMenuMenuSelected(evt);
            }
        });

        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setMnemonic('U');
        undoMenuItem.setText("Undo");
        undoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(undoMenuItem);

        redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        redoMenuItem.setMnemonic('R');
        redoMenuItem.setText("Redo");
        redoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(redoMenuItem);
        editMenu.add(separator2);

        deselectMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        deselectMenuItem.setMnemonic('D');
        deselectMenuItem.setText("Deselect");
        deselectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(deselectMenuItem);

        selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAllMenuItem.setMnemonic('A');
        selectAllMenuItem.setText("Select All");
        selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(selectAllMenuItem);

        selectBetweenBookmarksMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        selectBetweenBookmarksMenuItem.setText("Select Between Bookmarks");
        selectBetweenBookmarksMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectBetweenBookmarksMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(selectBetweenBookmarksMenuItem);
        editMenu.add(separator3);

        cutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText("Cut");
        cutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(cutMenuItem);

        copyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        copyMenuItem.setMnemonic('y');
        copyMenuItem.setText("Copy");
        copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(copyMenuItem);

        pasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        pasteMenuItem.setMnemonic('P');
        pasteMenuItem.setText("Paste");
        pasteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(pasteMenuItem);

        mergeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        mergeMenuItem.setMnemonic('M');
        mergeMenuItem.setText("Merge");
        mergeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(mergeMenuItem);
        editMenu.add(separator);

        insertPasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        insertPasteMenuItem.setMnemonic('I');
        insertPasteMenuItem.setText("Insert Paste");
        insertPasteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertPasteMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(insertPasteMenuItem);

        insertFrameMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_INSERT, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        insertFrameMenuItem.setMnemonic('F');
        insertFrameMenuItem.setText("Insert Frame");
        insertFrameMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertFrameMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(insertFrameMenuItem);

        insertFramesMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_INSERT, 0));
        insertFramesMenuItem.setMnemonic('n');
        insertFramesMenuItem.setText("Insert Frames...");
        insertFramesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertFramesMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(insertFramesMenuItem);

        insertCloneMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_INSERT, java.awt.event.InputEvent.CTRL_MASK));
        insertCloneMenuItem.setMnemonic('C');
        insertCloneMenuItem.setText("Insert Clone");
        insertCloneMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertCloneMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(insertCloneMenuItem);
        editMenu.add(separator5);

        appendPasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        appendPasteMenuItem.setText("Append Paste");
        appendPasteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appendPasteMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(appendPasteMenuItem);

        appendFrameMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        appendFrameMenuItem.setText("Append Frame");
        appendFrameMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appendFrameMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(appendFrameMenuItem);

        appendFramesMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        appendFramesMenuItem.setText("Append Frames...");
        appendFramesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appendFramesMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(appendFramesMenuItem);

        appendCloneMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        appendCloneMenuItem.setText("Append Clone");
        appendCloneMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appendCloneMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(appendCloneMenuItem);
        editMenu.add(separator6);

        clearMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        clearMenuItem.setMnemonic('l');
        clearMenuItem.setText("Clear");
        clearMenuItem.setToolTipText("");
        clearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(clearMenuItem);

        deleteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, java.awt.event.InputEvent.CTRL_MASK));
        deleteMenuItem.setMnemonic('e');
        deleteMenuItem.setText("Delete");
        deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(deleteMenuItem);

        trimTopMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        trimTopMenuItem.setMnemonic('o');
        trimTopMenuItem.setText("Trim Top");
        trimTopMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trimTopMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(trimTopMenuItem);

        trimBottomMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        trimBottomMenuItem.setMnemonic('B');
        trimBottomMenuItem.setText("Trim Bottom");
        trimBottomMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trimBottomMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(trimBottomMenuItem);

        menuBar.add(editMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(historyScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(recordPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(historyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(runPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(viewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(statesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(bookmarksPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, bookmarksPanel, historyPanel, recordPanel, runPanel, statesPanel, viewPanel);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(runPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(viewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(recordPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(statesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(bookmarksPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(historyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(historyScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelAllTasks();
        taskScheduler.add(new DisposeTask(movie, historyTableModel.getTailIndex(),
                historyTableModel.getHeadIndex(), historyTableModel, this));
    }//GEN-LAST:event_formWindowClosing

    private void view1PCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_view1PCheckBoxActionPerformed
        updateViewPlayers();
    }//GEN-LAST:event_view1PCheckBoxActionPerformed

    private void view2PCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_view2PCheckBoxActionPerformed
        updateViewPlayers();
    }//GEN-LAST:event_view2PCheckBoxActionPerformed

    private void view3PCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_view3PCheckBoxActionPerformed
        updateViewPlayers();
    }//GEN-LAST:event_view3PCheckBoxActionPerformed

    private void view4PCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_view4PCheckBoxActionPerformed
        updateViewPlayers();
    }//GEN-LAST:event_view4PCheckBoxActionPerformed

    private void pauseToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseToggleButtonActionPerformed
        toggleRecording();
    }//GEN-LAST:event_pauseToggleButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelAllTasks();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void previousFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousFrameButtonActionPerformed
        modifyHeadIndex(-1);
    }//GEN-LAST:event_previousFrameButtonActionPerformed

    private void nextFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextFrameButtonActionPerformed
        modifyHeadIndex(1);
    }//GEN-LAST:event_nextFrameButtonActionPerformed

    private void headButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headButtonActionPerformed
        scrollToCenter(historyTable, historyTableModel.getHeadIndex());
    }//GEN-LAST:event_headButtonActionPerformed

    private void cutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutMenuItemActionPerformed
        copySelectedRows();
        clearSelectedRows();
    }//GEN-LAST:event_cutMenuItemActionPerformed

    private void copyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyMenuItemActionPerformed
        copySelectedRows();
    }//GEN-LAST:event_copyMenuItemActionPerformed

    private void pasteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasteMenuItemActionPerformed
        pasteRows(false);
    }//GEN-LAST:event_pasteMenuItemActionPerformed

    private void editMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_editMenuMenuSelected
        enableEditMenuItems();
    }//GEN-LAST:event_editMenuMenuSelected

    private void mergeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeMenuItemActionPerformed
        pasteRows(true);
    }//GEN-LAST:event_mergeMenuItemActionPerformed

    private void clearMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearMenuItemActionPerformed
        clearSelectedRows();
    }//GEN-LAST:event_clearMenuItemActionPerformed

    private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllMenuItemActionPerformed
        historyTable.selectAll();
    }//GEN-LAST:event_selectAllMenuItemActionPerformed

    private void deselectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectMenuItemActionPerformed
        historyTable.clearSelection();
    }//GEN-LAST:event_deselectMenuItemActionPerformed

    private void insertPasteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertPasteMenuItemActionPerformed
        insertRows();
    }//GEN-LAST:event_insertPasteMenuItemActionPerformed

    private void deleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMenuItemActionPerformed
        deleteSelectedRows();
    }//GEN-LAST:event_deleteMenuItemActionPerformed

    private void trimBottomMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trimBottomMenuItemActionPerformed
        trimBottom();
    }//GEN-LAST:event_trimBottomMenuItemActionPerformed

    private void insertFrameMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertFrameMenuItemActionPerformed
        insertRows(historyTable.getSelectedRow(), 1);
    }//GEN-LAST:event_insertFrameMenuItemActionPerformed

    private void insertFramesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertFramesMenuItemActionPerformed
        insertFrames(true);
    }//GEN-LAST:event_insertFramesMenuItemActionPerformed

    private void appendPasteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appendPasteMenuItemActionPerformed
        insertRows(-1);
    }//GEN-LAST:event_appendPasteMenuItemActionPerformed

    private void appendFrameMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appendFrameMenuItemActionPerformed
        insertRows(-1, 1);
    }//GEN-LAST:event_appendFrameMenuItemActionPerformed

    private void appendFramesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appendFramesMenuItemActionPerformed
        insertFrames(false);
    }//GEN-LAST:event_appendFramesMenuItemActionPerformed

    private void insertCloneMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertCloneMenuItemActionPerformed
        insertClone(true);
    }//GEN-LAST:event_insertCloneMenuItemActionPerformed

    private void appendCloneMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appendCloneMenuItemActionPerformed
        insertClone(false);
    }//GEN-LAST:event_appendCloneMenuItemActionPerformed

    private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoMenuItemActionPerformed
        undo(1);
    }//GEN-LAST:event_undoMenuItemActionPerformed

    private void redoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoMenuItemActionPerformed
        redo(1);
    }//GEN-LAST:event_redoMenuItemActionPerformed

    private void changesListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_changesListMousePressed

        final int delta = changesList.locationToIndex(evt.getPoint()) + 1
                - changeListModel.getChangesIndex();
        if (delta > 0) {
            redo(delta);
        } else if (delta < 0) {
            undo(-delta);
        }
    }//GEN-LAST:event_changesListMousePressed

    private void undoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoButtonActionPerformed
        undo(1);
    }//GEN-LAST:event_undoButtonActionPerformed

    private void redoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoButtonActionPerformed
        redo(1);
    }//GEN-LAST:event_redoButtonActionPerformed

    private void addBookmarkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBookmarkButtonActionPerformed
        final HistoryBookmarkEditor dialog = new HistoryBookmarkEditor(this,
                historyTableModel);
        dialog.setMode(false);
        final int frame = historyTable.getSelectedRow();
        if (frame >= 0) {
            final HistoryBookmark bookmark = bookmarksModel.findBookmark(frame);
            if (bookmark == null) {
                dialog.setHistoryBookmark("", frame);
            } else {
                dialog.setMode(true);
                dialog.setHistoryBookmark(bookmark);
            }
        }
        dialog.setVisible(true);
        if (dialog.isOk()) {
            final HistoryBookmark bookmark = dialog.getBookmark();
            final int index = bookmarksModel.indexOf(bookmark);
            if (index >= 0) {
                historyTableModel.addChange(new BookmarkEditedChange(
                        bookmarksModel.getElementAt(index), bookmark));
            } else {
                historyTableModel.addChange(new BookmarkAddedChange(bookmark));
            }
            scrollToVisible(bookmarksList, bookmarksModel.indexOf(bookmark));
        }
    }//GEN-LAST:event_addBookmarkButtonActionPerformed

    private void editBookmarkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBookmarkButtonActionPerformed
        final int index = bookmarksList.getSelectedIndex();
        if (index >= 0 && index < bookmarksModel.getSize()) {
            final HistoryBookmarkEditor dialog = new HistoryBookmarkEditor(this,
                    historyTableModel);
            dialog.setMode(true);
            final HistoryBookmark priorBookmark = bookmarksModel.getElementAt(index);
            dialog.setHistoryBookmark(priorBookmark);
            dialog.setVisible(true);
            if (dialog.isOk()) {
                final HistoryBookmark newBookmark = dialog.getBookmark();
                historyTableModel.addChange(new BookmarkEditedChange(priorBookmark,
                        newBookmark));
                scrollToVisible(bookmarksList, bookmarksModel.indexOf(newBookmark));
            }
        }
    }//GEN-LAST:event_editBookmarkButtonActionPerformed

    private void deleteBookmarkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteBookmarkButtonActionPerformed
        final int index = bookmarksList.getSelectedIndex();
        if (index >= 0 && index < bookmarksModel.getSize()) {
            bookmarksList.clearSelection();
            historyTableModel.addChange(new BookmarkDeletedChange(
                    bookmarksModel.getElementAt(index)));
        }
    }//GEN-LAST:event_deleteBookmarkButtonActionPerformed

    private void bookmarksListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_bookmarksListValueChanged
        enableBookmarkButtons();
        goToSelectedBookmark();
    }//GEN-LAST:event_bookmarksListValueChanged

    private void seekBookmarkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seekBookmarkButtonActionPerformed
        goToSelectedBookmark();
    }//GEN-LAST:event_seekBookmarkButtonActionPerformed

    private void previousBookmarkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousBookmarkButtonActionPerformed
        goToPreviousBookmark();
    }//GEN-LAST:event_previousBookmarkButtonActionPerformed

    private void nextBookmarkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextBookmarkButtonActionPerformed
        goToNextBookmark();
    }//GEN-LAST:event_nextBookmarkButtonActionPerformed

    private void trimTopMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trimTopMenuItemActionPerformed
        trimTop();
    }//GEN-LAST:event_trimTopMenuItemActionPerformed

    private void record1PCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_record1PCheckBoxActionPerformed
        recordOptionsChanged();
    }//GEN-LAST:event_record1PCheckBoxActionPerformed

    private void record2PCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_record2PCheckBoxActionPerformed
        recordOptionsChanged();
    }//GEN-LAST:event_record2PCheckBoxActionPerformed

    private void record3PCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_record3PCheckBoxActionPerformed
        recordOptionsChanged();
    }//GEN-LAST:event_record3PCheckBoxActionPerformed

    private void record4PCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_record4PCheckBoxActionPerformed
        recordOptionsChanged();
    }//GEN-LAST:event_record4PCheckBoxActionPerformed

    private void mergeButtonsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeButtonsCheckBoxActionPerformed
        recordOptionsChanged();
    }//GEN-LAST:event_mergeButtonsCheckBoxActionPerformed

    private void fastGenerationCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastGenerationCheckBoxActionPerformed
        if (playMovieTask != null && !playMovieTask.isCanceled()) {
            playMovieTask.setRealtime(!fastGenerationCheckBox.isSelected());
        }
    }//GEN-LAST:event_fastGenerationCheckBoxActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clearChanges();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        saveAs();
    }//GEN-LAST:event_saveAsMenuItemActionPerformed

    private void loadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadMenuItemActionPerformed
        load();
    }//GEN-LAST:event_loadMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        if (priorSaveFile == null) {
            saveAs();
        } else {
            save(priorSaveFile);
        }
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_fileMenuMenuSelected
        enableFileMenuItems();
    }//GEN-LAST:event_fileMenuMenuSelected

    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMenuItemActionPerformed
        closeProject();
    }//GEN-LAST:event_newMenuItemActionPerformed

    private void saveSlotsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSlotsComboBoxActionPerformed
        enableSaveButtons();
    }//GEN-LAST:event_saveSlotsComboBoxActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        final int index = saveSlotsComboBox.getSelectedIndex();
        if (index >= 0) {
            quickSaveProject(index);
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        final int index = saveSlotsComboBox.getSelectedIndex();
        if (index >= 0) {
            quickLoadProject(index);
        }
    }//GEN-LAST:event_loadButtonActionPerformed

    private void recentProjectsClearMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentProjectsClearMenuItemActionPerformed
        AppPrefs.getInstance().getPaths().clearRecentHistoryProjects();
        createRecentProjectsMenu();
        AppPrefs.save();
    }//GEN-LAST:event_recentProjectsClearMenuItemActionPerformed

    private void recentProjectsLockMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentProjectsLockMenuItemActionPerformed
        AppPrefs.getInstance().getPaths().setLockRecentHistoryProjects(
                recentProjectsLockMenuItem.isSelected());
        AppPrefs.save();
    }//GEN-LAST:event_recentProjectsLockMenuItemActionPerformed

    private void recentDirectoriesMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_recentDirectoriesMenuMenuSelected
        createRecentDirectoriesMenu();
    }//GEN-LAST:event_recentDirectoriesMenuMenuSelected

    private void recentDirectoriesLockMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentDirectoriesLockMenuItemActionPerformed
        AppPrefs.getInstance().getPaths().setLockRecentDirectories(
                recentDirectoriesLockMenuItem.isSelected());
        AppPrefs.save();
    }//GEN-LAST:event_recentDirectoriesLockMenuItemActionPerformed

    private void recentDirectoriesClearMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentDirectoriesClearMenuItemActionPerformed
        AppPrefs.getInstance().getPaths().clearRecentDirectories();
        AppPrefs.save();
    }//GEN-LAST:event_recentDirectoriesClearMenuItemActionPerformed

    private void selectBetweenBookmarksMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectBetweenBookmarksMenuItemActionPerformed
        selectBetweenBookmarks();
    }//GEN-LAST:event_selectBetweenBookmarksMenuItemActionPerformed

    private void recordOtherCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordOtherCheckBoxActionPerformed
        recordOptionsChanged();
    }//GEN-LAST:event_recordOtherCheckBoxActionPerformed
    // End of variables declaration//GEN-END:variables
}
