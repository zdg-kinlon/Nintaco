package nintaco.gui.hexeditor;

import nintaco.App;
import nintaco.Machine;
import nintaco.MessageException;
import nintaco.files.CartFile;
import nintaco.files.FdsFile;
import nintaco.gui.*;
import nintaco.gui.hexeditor.preferences.Bookmark;
import nintaco.gui.hexeditor.preferences.HexEditorAppPrefs;
import nintaco.gui.hexeditor.preferences.HexEditorGamePrefs;
import nintaco.gui.image.ImageFrame;
import nintaco.gui.image.preferences.Paths;
import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;
import nintaco.util.EDT;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static nintaco.files.FileUtil.*;
import static nintaco.gui.hexeditor.DataSource.*;
import static nintaco.util.GuiUtil.*;

public class HexEditorFrame
        extends javax.swing.JFrame implements StyleListener {

    private final HexEditorView hexEditorView = new HexEditorView();
    private final HexEditorColumnHeader hexEditorColumnHeader
            = new HexEditorColumnHeader();

    private int fileIndex;
    private FileExtensionFilter fileFilter;
    private String entryFileName;
    private String fileExtension;
    private File lastSavedFile;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addAddressLabelMenuItem;
    private javax.swing.JMenuItem addBreakpointMenuItem;
    private javax.swing.JMenuItem addCheatMenuItem;
    private javax.swing.JMenu bookmarksMenu;
    private javax.swing.JPopupMenu.Separator bookmarksSeparator;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem copySpacedMenuItem;
    private javax.swing.JRadioButtonMenuItem cpuMemoryMenuItem;
    private javax.swing.JMenu debugMenu;
    private javax.swing.JMenuItem editAddressLabelMenuItem;
    private javax.swing.JMenuItem editBreakpointMenuItem;
    private javax.swing.JMenuItem editCheatsMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JRadioButtonMenuItem fileContentsMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem findMenuItem;
    private javax.swing.JMenuItem goToAddressMenuItem;
    private javax.swing.JMenuItem goToFileIndexMenuItem;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JMenuItem loadCharTableMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu navigateMenu;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JRadioButtonMenuItem ppuMemoryMenuItem;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JMenuItem removeAllBookmarksMenuItem;
    private javax.swing.JMenuItem replaceMenuItem;
    private javax.swing.JMenuItem saveCPUMenuItem;
    private javax.swing.JMenuItem saveFileAsMenuItem;
    private javax.swing.JMenuItem saveFileMenuItem;
    private javax.swing.JMenuItem savePPUMenuItem;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem toggleBookmarkMenuItem;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JMenuItem unloadCharTableMenuItem;
    private javax.swing.ButtonGroup viewButtonGroup;

    public HexEditorFrame() {
        initComponents();
        scaleFonts(this);
        hexEditorView.setHexEditorFrame(this);
        scrollPane.setViewportView(hexEditorView);
        scrollPane.setColumnHeaderView(hexEditorColumnHeader);
        navigateMenu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuSelected(MenuEvent e) {
                updateNavigationMenu();
            }
        });
        initScrollPane();
        pack();
        moveToImageFrameMonitor(this);
    }

    private void initScrollPane() {
        final FontMetrics metrics = hexEditorView.getFontMetrics(hexEditorView
                .getFont());
        scrollPane.setPreferredSize(new Dimension((HexEditorView.MARGIN << 1) + 76
                * metrics.getWidths()['M'] + (scrollPane.getVerticalScrollBar()
                .getPreferredSize().width << 1), (HexEditorView.MARGIN << 1) + 40
                * metrics.getHeight()));
    }

    @Override
    public void styleChanged() {
        hexEditorView.styleChanged();
        hexEditorColumnHeader.styleChanged();
        initScrollPane();
    }

    private void updateNavigationMenu() {
        setDataSource(hexEditorView.getDataSource().getIndex());
    }

    private void machineUpdated(final boolean enabled) {
        saveFileMenuItem.setEnabled(enabled);
        saveFileAsMenuItem.setEnabled(enabled);
        saveCPUMenuItem.setEnabled(enabled);
        savePPUMenuItem.setEnabled(enabled);
        undoMenuItem.setEnabled(enabled);
        redoMenuItem.setEnabled(enabled);
        copyMenuItem.setEnabled(enabled);
        copySpacedMenuItem.setEnabled(enabled);
        pasteMenuItem.setEnabled(enabled);
        selectAllMenuItem.setEnabled(enabled);
        findMenuItem.setEnabled(enabled);
        replaceMenuItem.setEnabled(enabled);
        goToAddressMenuItem.setEnabled(enabled);
        toggleBookmarkMenuItem.setEnabled(enabled);
        removeAllBookmarksMenuItem.setEnabled(enabled);

        fileFilter = null;
        entryFileName = null;
        fileExtension = null;
        if (enabled) {
            final CartFile cartFile = App.getCartFile();
            final FdsFile fdsFile = App.getFdsFile();
            lastSavedFile = null;
            if (cartFile != null) {
                if (cartFile.getArchiveFileName() == null) {
                    lastSavedFile = new File(cartFile.getEntryFileName());
                }
                fileFilter = ImageFrame.FileExtensionFilters[1];
                entryFileName = getFileName(cartFile.getEntryFileName());
                fileExtension = "nintaco";
            } else if (fdsFile != null) {
                if (fdsFile.getArchiveFileName() == null) {
                    lastSavedFile = new File(fdsFile.getEntryFileName());
                }
                fileFilter = ImageFrame.FileExtensionFilters[2];
                entryFileName = getFileName(fdsFile.getEntryFileName());
                fileExtension = "fds";
            }
            saveFileAsMenuItem.setEnabled(lastSavedFile != null);
        }
    }

    public void updateBookmarksMenu() {
        synchronized (GamePrefs.class) {
            final List<Bookmark> bookmarks = GamePrefs.getInstance()
                    .getHexEditorGamePrefs().getBookmarks();
            bookmarksMenu.removeAll();
            bookmarksMenu.add(toggleBookmarkMenuItem);
            bookmarksMenu.add(removeAllBookmarksMenuItem);
            removeAllBookmarksMenuItem.setEnabled(!bookmarks.isEmpty());
            if (!bookmarks.isEmpty()) {
                bookmarksMenu.add(bookmarksSeparator);
            }
            for (int i = 0; i < bookmarks.size(); i++) {
                final Bookmark bookmark = bookmarks.get(i);
                final JMenuItem menuItem = new JMenuItem(bookmark.getName());
                if (i < 10) {
                    menuItem.setAccelerator(KeyStroke.getKeyStroke(
                            KeyEvent.VK_0 + i, InputEvent.CTRL_MASK));
                }
                menuItem.addActionListener(e -> hexEditorView.goToBookmark(bookmark));
                bookmarksMenu.add(menuItem);
            }
        }
    }

    public void update() {
        hexEditorView.update();
    }

    public void destroy() {
        dispose();
    }

    private void closeFrame() {
        App.destroyHexEditorFrame();
    }

    public void setMachine(final Machine machine) {
        hexEditorView.setMachine(machine);
        EDT.async(() -> {
            machineUpdated(machine != null);
            updateBookmarksMenu();
            hexEditorView.colorBookmarks();
        });
    }

    public void setDataSource(final int index) {
        switch (index) {
            case CpuMemory:
                cpuMemoryMenuItem.setSelected(true);
                break;
            case PpuMemory:
                ppuMemoryMenuItem.setSelected(true);
                break;
            case FileContents:
                fileContentsMenuItem.setSelected(true);
                break;
        }
        hexEditorColumnHeader.setDataSource(index);
        hexEditorView.setDataSource(index);
        fileIndex = hexEditorView.getFileIndex();
        goToFileIndexMenuItem.setEnabled(fileIndex >= 0);
    }

    public void goToAddress(final int dataSourceIndex, final int address) {
        hexEditorView.goToAddress(dataSourceIndex, address);
        setDataSource(dataSourceIndex);
    }

    private void loadCharTable(final PleaseWaitDialog pleaseWaitDialog,
                               final File file) {
        String errorMessage = null;
        try {
            hexEditorView.setCharTable(new CharTable(file));
        } catch (MessageException e) {
            errorMessage = e.getMessage();
        } catch (Throwable t) {
            errorMessage = "Error loading character table.";
        }
        pleaseWaitDialog.dispose();
        if (errorMessage != null) {
            displayError(this, errorMessage);
        }
        App.setNoStepPause(false);
    }

    private void saveMemory(final int dataSourceIndex) {
        App.setNoStepPause(true);
        final HexEditorGamePrefs prefs = GamePrefs.getInstance()
                .getHexEditorGamePrefs();
        String name = dataSourceIndex == CpuMemory ? prefs.getCpuMemoryFile()
                : prefs.getPpuMemoryFile();
        if (name == null) {
            name = appendSeparator(AppPrefs.getInstance().getPaths()
                    .getSaveEditedNesFileDir())
                    + getFileNameWithoutExtension(entryFileName)
                    + (dataSourceIndex == CpuMemory ? "-cpu.bin" : "-ppu.bin");
        }
        final File defaultFile = new File(name);
        final File file = showSaveAsDialog(this,
                defaultFile.getParent(), defaultFile.getName(), "bin",
                new FileNameExtensionFilter("Binary files (*.bin)", "bin"), true);
        if (file != null) {
            if (dataSourceIndex == CpuMemory) {
                prefs.setCpuMemoryFile(file.getPath());
            } else {
                prefs.setPpuMemoryFile(file.getPath());
            }
            GamePrefs.save();
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> hexEditorView.saveFile(pleaseWaitDialog, file,
                    dataSourceIndex)).start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }

    private void goToFileContents() {
        goToAddress(FileContents, fileIndex);
    }

    public void goToFileContents(final int address) {
        goToAddress(CpuMemory, address);
        EDT.async(() -> {
            hexEditorView.goToAddress(FileContents, fileIndex);
            setDataSource(FileContents);
        });
    }

    public HexEditorView getHexEditorView() {
        return hexEditorView;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        viewButtonGroup = new javax.swing.ButtonGroup();
        scrollPane = new javax.swing.JScrollPane();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        saveFileMenuItem = new javax.swing.JMenuItem();
        saveFileAsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        saveCPUMenuItem = new javax.swing.JMenuItem();
        savePPUMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        loadCharTableMenuItem = new javax.swing.JMenuItem();
        unloadCharTableMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        closeMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        copyMenuItem = new javax.swing.JMenuItem();
        copySpacedMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        selectAllMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        findMenuItem = new javax.swing.JMenuItem();
        replaceMenuItem = new javax.swing.JMenuItem();
        navigateMenu = new javax.swing.JMenu();
        cpuMemoryMenuItem = new javax.swing.JRadioButtonMenuItem();
        ppuMemoryMenuItem = new javax.swing.JRadioButtonMenuItem();
        fileContentsMenuItem = new javax.swing.JRadioButtonMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        goToAddressMenuItem = new javax.swing.JMenuItem();
        goToFileIndexMenuItem = new javax.swing.JMenuItem();
        bookmarksMenu = new javax.swing.JMenu();
        toggleBookmarkMenuItem = new javax.swing.JMenuItem();
        removeAllBookmarksMenuItem = new javax.swing.JMenuItem();
        bookmarksSeparator = new javax.swing.JPopupMenu.Separator();
        debugMenu = new javax.swing.JMenu();
        addBreakpointMenuItem = new javax.swing.JMenuItem();
        editBreakpointMenuItem = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        addAddressLabelMenuItem = new javax.swing.JMenuItem();
        editAddressLabelMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        addCheatMenuItem = new javax.swing.JMenuItem();
        editCheatsMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Hex Editor");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        saveFileMenuItem.setMnemonic('S');
        saveFileMenuItem.setText("Save File");
        saveFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveFileMenuItem);

        saveFileAsMenuItem.setMnemonic('A');
        saveFileAsMenuItem.setText("Save File As...");
        saveFileAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveFileAsMenuItem);
        fileMenu.add(jSeparator1);

        saveCPUMenuItem.setMnemonic('M');
        saveCPUMenuItem.setText("Save CPU Memory...");
        saveCPUMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveCPUMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveCPUMenuItem);

        savePPUMenuItem.setMnemonic('P');
        savePPUMenuItem.setText("Save PPU Memory...");
        savePPUMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePPUMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(savePPUMenuItem);
        fileMenu.add(jSeparator3);

        loadCharTableMenuItem.setMnemonic('L');
        loadCharTableMenuItem.setText("Load Character Table...");
        loadCharTableMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadCharTableMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadCharTableMenuItem);

        unloadCharTableMenuItem.setMnemonic('U');
        unloadCharTableMenuItem.setText("Reset Character Table");
        unloadCharTableMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unloadCharTableMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(unloadCharTableMenuItem);
        fileMenu.add(jSeparator2);

        closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        closeMenuItem.setMnemonic('C');
        closeMenuItem.setText("Close");
        closeMenuItem.setToolTipText("");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('E');
        editMenu.setText("Edit");

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
        editMenu.add(jSeparator4);

        copyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        copyMenuItem.setMnemonic('C');
        copyMenuItem.setText("Copy");
        copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(copyMenuItem);

        copySpacedMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        copySpacedMenuItem.setMnemonic('S');
        copySpacedMenuItem.setText("Copy Spaced");
        copySpacedMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copySpacedMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(copySpacedMenuItem);

        pasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        pasteMenuItem.setMnemonic('P');
        pasteMenuItem.setText("Paste");
        pasteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(pasteMenuItem);

        selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAllMenuItem.setMnemonic('A');
        selectAllMenuItem.setText("Select All");
        selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(selectAllMenuItem);
        editMenu.add(jSeparator5);

        findMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        findMenuItem.setMnemonic('F');
        findMenuItem.setText("Find...");
        findMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(findMenuItem);

        replaceMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        replaceMenuItem.setMnemonic('e');
        replaceMenuItem.setText("Replace...");
        replaceMenuItem.setToolTipText("");
        replaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(replaceMenuItem);

        menuBar.add(editMenu);

        navigateMenu.setMnemonic('N');
        navigateMenu.setText("Navigate");

        cpuMemoryMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
        viewButtonGroup.add(cpuMemoryMenuItem);
        cpuMemoryMenuItem.setMnemonic('C');
        cpuMemoryMenuItem.setSelected(true);
        cpuMemoryMenuItem.setText("CPU Memory");
        cpuMemoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cpuMemoryMenuItemActionPerformed(evt);
            }
        });
        navigateMenu.add(cpuMemoryMenuItem);

        ppuMemoryMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK));
        viewButtonGroup.add(ppuMemoryMenuItem);
        ppuMemoryMenuItem.setMnemonic('P');
        ppuMemoryMenuItem.setText("PPU Memory");
        ppuMemoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppuMemoryMenuItemActionPerformed(evt);
            }
        });
        navigateMenu.add(ppuMemoryMenuItem);

        fileContentsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.ALT_MASK));
        viewButtonGroup.add(fileContentsMenuItem);
        fileContentsMenuItem.setMnemonic('F');
        fileContentsMenuItem.setText("File Contents");
        fileContentsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileContentsMenuItemActionPerformed(evt);
            }
        });
        navigateMenu.add(fileContentsMenuItem);
        navigateMenu.add(jSeparator6);

        goToAddressMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        goToAddressMenuItem.setMnemonic('G');
        goToAddressMenuItem.setText("Go to Address...");
        goToAddressMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goToAddressMenuItemActionPerformed(evt);
            }
        });
        navigateMenu.add(goToAddressMenuItem);

        goToFileIndexMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        goToFileIndexMenuItem.setMnemonic('I');
        goToFileIndexMenuItem.setText("Go to File Index");
        goToFileIndexMenuItem.setEnabled(false);
        goToFileIndexMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goToFileIndexMenuItemActionPerformed(evt);
            }
        });
        navigateMenu.add(goToFileIndexMenuItem);

        menuBar.add(navigateMenu);

        bookmarksMenu.setMnemonic('B');
        bookmarksMenu.setText("Bookmarks");

        toggleBookmarkMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        toggleBookmarkMenuItem.setMnemonic('T');
        toggleBookmarkMenuItem.setText("Toggle Bookmark");
        toggleBookmarkMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleBookmarkMenuItemActionPerformed(evt);
            }
        });
        bookmarksMenu.add(toggleBookmarkMenuItem);

        removeAllBookmarksMenuItem.setText("Remove All Bookmarks");
        removeAllBookmarksMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllBookmarksMenuItemActionPerformed(evt);
            }
        });
        bookmarksMenu.add(removeAllBookmarksMenuItem);
        bookmarksMenu.add(bookmarksSeparator);

        menuBar.add(bookmarksMenu);

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

        addBreakpointMenuItem.setText("Add Breakpoint...");
        addBreakpointMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBreakpointMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(addBreakpointMenuItem);

        editBreakpointMenuItem.setText("Edit Breakpoints...");
        editBreakpointMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editBreakpointMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(editBreakpointMenuItem);
        debugMenu.add(jSeparator7);

        addAddressLabelMenuItem.setText("Add Address Label...");
        addAddressLabelMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAddressLabelMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(addAddressLabelMenuItem);

        editAddressLabelMenuItem.setText("Edit Address Labels...");
        editAddressLabelMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editAddressLabelMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(editAddressLabelMenuItem);
        debugMenu.add(jSeparator8);

        addCheatMenuItem.setText("Add Cheat...");
        addCheatMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCheatMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(addCheatMenuItem);

        editCheatsMenuItem.setText("Edit Cheats...");
        editCheatsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editCheatsMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(editCheatsMenuItem);

        menuBar.add(debugMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(scrollPane)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(scrollPane)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        hexEditorView.keyPressed(evt);
    }//GEN-LAST:event_formKeyPressed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        closeFrame();
    }//GEN-LAST:event_closeMenuItemActionPerformed

    private void copyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyMenuItemActionPerformed
        hexEditorView.copy(false);
    }//GEN-LAST:event_copyMenuItemActionPerformed

    private void pasteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasteMenuItemActionPerformed
        hexEditorView.paste();
    }//GEN-LAST:event_pasteMenuItemActionPerformed

    private void cpuMemoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cpuMemoryMenuItemActionPerformed
        setDataSource(CpuMemory);
    }//GEN-LAST:event_cpuMemoryMenuItemActionPerformed

    private void ppuMemoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppuMemoryMenuItemActionPerformed
        setDataSource(PpuMemory);
    }//GEN-LAST:event_ppuMemoryMenuItemActionPerformed

    private void fileContentsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileContentsMenuItemActionPerformed
        setDataSource(FileContents);
    }//GEN-LAST:event_fileContentsMenuItemActionPerformed

    private void loadCharTableMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadCharTableMenuItemActionPerformed
        App.setNoStepPause(true);
        final JFileChooser chooser = createFileChooser("Load Character Table",
                AppPrefs.getInstance().getPaths().getLoadCharacterTableDir(),
                new FileNameExtensionFilter("Character Table (*.tbl)", "tbl"));
        if (showOpenDialog(this, chooser, (p, d) -> p.setLoadCharacterTableDir(d))
                == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = chooser.getSelectedFile();
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> loadCharTable(pleaseWaitDialog, selectedFile)).start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }//GEN-LAST:event_loadCharTableMenuItemActionPerformed

    private void unloadCharTableMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unloadCharTableMenuItemActionPerformed
        hexEditorView.setCharTable(new CharTable());
    }//GEN-LAST:event_unloadCharTableMenuItemActionPerformed

    private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoMenuItemActionPerformed
        hexEditorView.undo();
    }//GEN-LAST:event_undoMenuItemActionPerformed

    private void redoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoMenuItemActionPerformed
        hexEditorView.redo();
    }//GEN-LAST:event_redoMenuItemActionPerformed

    private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllMenuItemActionPerformed
        hexEditorView.selectAll();
    }//GEN-LAST:event_selectAllMenuItemActionPerformed

    private void findMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findMenuItemActionPerformed
        hexEditorView.showSearchDialog(false);
    }//GEN-LAST:event_findMenuItemActionPerformed

    private void replaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceMenuItemActionPerformed
        hexEditorView.showSearchDialog(true);
    }//GEN-LAST:event_replaceMenuItemActionPerformed

    private void copySpacedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copySpacedMenuItemActionPerformed
        hexEditorView.copy(true);
    }//GEN-LAST:event_copySpacedMenuItemActionPerformed

    private void goToAddressMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goToAddressMenuItemActionPerformed
        HexEditorAppPrefs prefs = AppPrefs.getInstance().getHexEditorPrefs();
        final List<Integer> addresses = prefs.getRecentAddresses();
        final List<String> items = new ArrayList<String>();
        String format = "%04X";
        for (final int address : addresses) {
            if (address > 0xFFFF) {
                format = "%06X";
                break;
            }
        }
        for (final int address : addresses) {
            items.add(String.format(format, address));
        }
        while (true) {
            final EditableComboBoxDialog dialog = new EditableComboBoxDialog(this,
                    "Address", "Go to Address", items);
            dialog.setOkButtonText("Go");
            dialog.setVisible(true);
            if (dialog.isOk()) {
                final String input = dialog.getInput().trim();
                if (input.isEmpty()) {
                    break;
                }
                int value;
                try {
                    value = Integer.parseInt(input, 16);
                } catch (Throwable t) {
                    final InformationDialog errorDialog = new InformationDialog(this,
                            "Invalid hexidecimal address.", "Bad Address",
                            InformationDialog.IconType.ERROR);
                    errorDialog.setVisible(true);
                    continue;
                }
                if (value < 0 || value > 0xFFFFFF) {
                    final InformationDialog errorDialog = new InformationDialog(this,
                            "Address out of range.<br/>Expected: 000000&mdash;FFFFFF",
                            "Bad Address", InformationDialog.IconType.ERROR);
                    errorDialog.setVisible(true);
                    continue;
                }
                prefs.addRecentAddress(value);
                hexEditorView.goToAddress(value);
                break;
            } else {
                break;
            }
        }
    }//GEN-LAST:event_goToAddressMenuItemActionPerformed

    private void toggleBookmarkMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleBookmarkMenuItemActionPerformed
        hexEditorView.toggleBookmark();
    }//GEN-LAST:event_toggleBookmarkMenuItemActionPerformed

    private void removeAllBookmarksMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllBookmarksMenuItemActionPerformed
        hexEditorView.removeAllBookmarks();
    }//GEN-LAST:event_removeAllBookmarksMenuItemActionPerformed

    private void goToFileIndexMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goToFileIndexMenuItemActionPerformed
        goToFileContents();
    }//GEN-LAST:event_goToFileIndexMenuItemActionPerformed

    private void saveFileAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileAsMenuItemActionPerformed

        if (entryFileName == null) {
            return;
        }

        App.setNoStepPause(true);
        final Paths paths = AppPrefs.getInstance().getPaths();
        final File file = showSaveAsDialog(this,
                paths.getSaveEditedNesFileDir(), entryFileName, fileExtension,
                fileFilter, true);
        if (file != null) {
            paths.setSaveEditedNesFileDir(file.getParentFile().getPath());
            AppPrefs.save();
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            entryFileName = file.getName();
            lastSavedFile = file;
            saveFileMenuItem.setEnabled(true);
            new Thread(() -> hexEditorView.saveFile(pleaseWaitDialog, file,
                    FileContents)).start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }//GEN-LAST:event_saveFileAsMenuItemActionPerformed

    private void saveFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileMenuItemActionPerformed

        if (lastSavedFile == null) {
            saveFileAsMenuItemActionPerformed(evt);
            return;
        } else if (entryFileName == null) {
            return;
        }

        App.setNoStepPause(true);
        final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
        new Thread(() -> hexEditorView.saveFile(pleaseWaitDialog, lastSavedFile,
                FileContents)).start();
        pleaseWaitDialog.showAfterDelay();
    }//GEN-LAST:event_saveFileMenuItemActionPerformed

    private void saveCPUMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveCPUMenuItemActionPerformed
        saveMemory(CpuMemory);
    }//GEN-LAST:event_saveCPUMenuItemActionPerformed

    private void savePPUMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePPUMenuItemActionPerformed
        saveMemory(PpuMemory);
    }//GEN-LAST:event_savePPUMenuItemActionPerformed

    private void addBreakpointMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBreakpointMenuItemActionPerformed
        hexEditorView.addBreakpoint();
    }//GEN-LAST:event_addBreakpointMenuItemActionPerformed

    private void editBreakpointMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBreakpointMenuItemActionPerformed
        hexEditorView.showBreakpointDialog();
    }//GEN-LAST:event_editBreakpointMenuItemActionPerformed

    private void addAddressLabelMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAddressLabelMenuItemActionPerformed
        hexEditorView.addAddressLabel();
    }//GEN-LAST:event_addAddressLabelMenuItemActionPerformed

    private void editAddressLabelMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editAddressLabelMenuItemActionPerformed
        hexEditorView.showAddressLabelsDialog();
    }//GEN-LAST:event_editAddressLabelMenuItemActionPerformed

    private void addCheatMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCheatMenuItemActionPerformed
        hexEditorView.addCheat();
    }//GEN-LAST:event_addCheatMenuItemActionPerformed

    private void editCheatsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editCheatsMenuItemActionPerformed
        hexEditorView.showCheatsDialog();
    }//GEN-LAST:event_editCheatsMenuItemActionPerformed

    private void debugMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_debugMenuMenuSelected
        final boolean enabled = hexEditorView.getMachine() != null
                && hexEditorView.getDataSource().getIndex() == DataSource.CpuMemory;
        addBreakpointMenuItem.setEnabled(enabled);
        editBreakpointMenuItem.setEnabled(enabled);
        addAddressLabelMenuItem.setEnabled(enabled);
        editAddressLabelMenuItem.setEnabled(enabled);
        addCheatMenuItem.setEnabled(enabled);
        editCheatsMenuItem.setEnabled(enabled);
    }//GEN-LAST:event_debugMenuMenuSelected
    // End of variables declaration//GEN-END:variables
}
