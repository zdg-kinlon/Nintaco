package cn.kinlon.emu.gui.hexeditor;

import cn.kinlon.emu.App;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.MessageException;
import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.files.FdsFile;
import cn.kinlon.emu.gui.PleaseWaitDialog;
import cn.kinlon.emu.gui.StyleListener;
import cn.kinlon.emu.gui.hexeditor.preferences.Bookmark;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.preferences.GamePrefs;
import cn.kinlon.emu.utils.EDT;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

import static cn.kinlon.emu.files.FileUtil.getFileName;
import static cn.kinlon.emu.gui.hexeditor.DataSource.*;
import static cn.kinlon.emu.utils.GuiUtil.*;

public class HexEditorFrame
        extends javax.swing.JFrame implements StyleListener {

    private final HexEditorView hexEditorView = new HexEditorView();
    private final HexEditorColumnHeader hexEditorColumnHeader
            = new HexEditorColumnHeader();

    private int fileIndex;
    private String entryFileName;
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
    private javax.swing.JMenuItem editAddressLabelMenuItem;
    private javax.swing.JMenuItem editBreakpointMenuItem;
    private javax.swing.JMenuItem editCheatsMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JRadioButtonMenuItem fileContentsMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem findMenuItem;
    private javax.swing.JMenuItem goToAddressMenuItem;
    private javax.swing.JMenuItem goToFileIndexMenuItem;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
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

        if (enabled) {
            final CartFile cartFile = App.getCartFile();
            final FdsFile fdsFile = App.getFdsFile();
            lastSavedFile = null;
            if (cartFile != null) {
                if (cartFile.getArchiveFileName() == null) {
                    lastSavedFile = new File(cartFile.getEntryFileName());
                }
                entryFileName = getFileName(cartFile.getEntryFileName());
            } else if (fdsFile != null) {
                if (fdsFile.getArchiveFileName() == null) {
                    lastSavedFile = new File(fdsFile.getEntryFileName());
                }
                entryFileName = getFileName(fdsFile.getEntryFileName());
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
        App.setNoStepPause(false);
    }
    
    private void goToFileContents() {
        goToAddress(FileContents, fileIndex);
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
        saveCPUMenuItem = new javax.swing.JMenuItem();
        savePPUMenuItem = new javax.swing.JMenuItem();
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
        addBreakpointMenuItem = new javax.swing.JMenuItem();
        editBreakpointMenuItem = new javax.swing.JMenuItem();
        addAddressLabelMenuItem = new javax.swing.JMenuItem();
        editAddressLabelMenuItem = new javax.swing.JMenuItem();
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

    private void copySpacedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copySpacedMenuItemActionPerformed
        hexEditorView.copy(true);
    }//GEN-LAST:event_copySpacedMenuItemActionPerformed

    private void toggleBookmarkMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleBookmarkMenuItemActionPerformed
        hexEditorView.toggleBookmark();
    }//GEN-LAST:event_toggleBookmarkMenuItemActionPerformed

    private void removeAllBookmarksMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllBookmarksMenuItemActionPerformed
        hexEditorView.removeAllBookmarks();
    }//GEN-LAST:event_removeAllBookmarksMenuItemActionPerformed

    private void goToFileIndexMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goToFileIndexMenuItemActionPerformed
        goToFileContents();
    }//GEN-LAST:event_goToFileIndexMenuItemActionPerformed

    // End of variables declaration//GEN-END:variables
}
