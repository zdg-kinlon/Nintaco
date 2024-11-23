package nintaco.gui.cheats;

import nintaco.App;
import nintaco.cheats.Cheat;
import nintaco.cheats.GameCheats;
import nintaco.files.CartFile;
import nintaco.gui.FileExtensionFilter;
import nintaco.gui.PleaseWaitDialog;
import nintaco.gui.RadioSelectionDialog;
import nintaco.gui.ToolTipsTable;
import nintaco.gui.cheats.search.CheatSearchFrame;
import nintaco.gui.image.preferences.Paths;
import nintaco.preferences.AppPrefs;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static nintaco.files.FileUtil.getFileNameWithoutExtension;
import static nintaco.util.GuiUtil.*;

public class CheatsDialog extends javax.swing.JDialog {

    private static final FileExtensionFilter[] FILE_FILTERS = {
            new FileExtensionFilter(0, "All supported cheat files", "cht", "xml"),
            new FileExtensionFilter(1, "Cheat text files (*.cht)", "cht"),
            new FileExtensionFilter(2, "Cheat XML files (*.xml)", "xml"),
            new FileExtensionFilter(3, "All files (*.*)"),
    };

    final CheatPrefs cheatPrefs;

    private CheatsTableModel tableModel;
    private Cheat newCheat;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane cheatsScrollPane;
    private javax.swing.JTable cheatsTable;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton importButton;
    private javax.swing.JButton newButton;
    private javax.swing.JButton okButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JCheckBox showHexCheckBox;
    private javax.swing.JPanel tableButtonsPanel;

    public CheatsDialog(final Window parent) {
        super(parent);
        setModal(true);
        initComponents();
        initTable();
        getRootPane().setDefaultButton(okButton);
        cheatPrefs = AppPrefs.getInstance().getCheatPrefs();
        showHexCheckBox.setSelected(cheatPrefs.isShowHex());
        resetIfEmpty();
        scaleFonts(this);
        pack();
        setLocationRelativeTo(parent);
    }

    public void setNewCheat(final Cheat newCheat) {
        this.newCheat = newCheat;
    }

    private void initTable() {

        final CheatSearchFrame cheatSearch = App.getCheatSearchFrame();
        List<Cheat> cheats = cheatSearch == null ? GameCheats.getCheats()
                : cheatSearch.getCheats();
        if (cheats == null) {
            cheats = new ArrayList<>();
        }
        tableModel = new CheatsTableModel(cheats);
        cheatsTable.setModel(tableModel);

        cheatsScrollPane.setPreferredSize(null);
        disableCellBorder(cheatsTable);
        forceNoClearRowSelect(cheatsTable);
        cheatsTable.getSelectionModel().addListSelectionListener(
                e -> updateButtons());
        cheatsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resizeCellSizes(cheatsTable, false, 15, false, Boolean.TRUE,
                "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM",
                "$BBBB", "$BB", "$BB", "MMMMMMMM", "MMMMMMMM");
        ((ToolTipsTable) cheatsTable).setColumnToolTips("Enabled", "Description",
                "Address", "Data Value", "Compare Value", "Game Genie Code",
                "Pro Action Rocky Code");

        updateButtons();
    }

    private void updateButtons() {
        final boolean rowSelected = cheatsTable.getSelectedRowCount() > 0;
        final boolean enabled = tableModel.getRowCount() > 0;
        editButton.setEnabled(enabled && rowSelected);
        deleteButton.setEnabled(enabled && rowSelected);
        exportButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
    }

    private void closeDialog() {
        dispose();
    }

    private void importFile(final PleaseWaitDialog pleaseWaitDialog,
                            final File file) {
        List<Cheat> cheats = null;
        try {
            if (file.getName().trim().toLowerCase().endsWith("xml")) {
                cheats = GameCheats.loadXML(file);
            } else {
                cheats = GameCheats.loadCHT(file);
            }
        } catch (Throwable t) {
            //t.printStackTrace();
        }
        pleaseWaitDialog.dispose();
        if (cheats == null) {
            displayError(this, "Failed to import cheats file.");
        } else if (cheats.isEmpty()) {
            displayError(this, "The file does not contain any valid cheats.");
        } else {
            final List<Cheat> cs = cheats;
            EventQueue.invokeLater(() -> {
                tableModel.addCheats(cs, true);
                updateButtons();
            });
        }
        App.setNoStepPause(false);
    }

    private void exportFile(final PleaseWaitDialog pleaseWaitDialog,
                            final File file, final boolean chtFile, final List<Cheat> cheats) {
        boolean error = false;
        try {
            if (chtFile) {
                GameCheats.saveCHT(file, cheats);
            } else {
                final CartFile cartFile = App.getCartFile();
                if (cartFile == null) {
                    GameCheats.saveXML(file, cheats);
                } else {
                    GameCheats.saveXML(file, cheats, true, cartFile.getFileCRC());
                }
            }
        } catch (Throwable t) {
            error = true;
            //t.printStackTrace();   
        }
        pleaseWaitDialog.dispose();
        if (error) {
            displayError(this, "Failed to save cheats file.");
        }
        App.setNoStepPause(false);
    }

    private void resetIfEmpty() {
        if (tableModel.getRowCount() == 0) {
            resetCheats();
        }
    }

    private boolean tableHasBeenReset() {
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            if ((boolean) tableModel.getValueAt(i, CheatsTableModel.Indices.ENABLED)) {
                return false;
            }
        }
        final List<Cheat> cheats = GameCheats.queryCheatsDB();
        if (cheats == null || cheats.size() != tableModel.getRowCount()) {
            return false;
        }
        for (int i = cheats.size() - 1; i >= 0; i--) {
            if (!cheats.get(i).equals(tableModel.getRow(i).getCheat())) {
                return false;
            }
        }
        return true;
    }

    private void resetCheats() {
        tableModel.clear();
        final List<Cheat> cheats = GameCheats.queryCheatsDB();
        if (cheats != null) {
            tableModel.addCheats(cheats, false);
            tableModel.setModified(false);
        }
        updateButtons();
    }

    private void addCheat(final Cheat cheat) {
        if (cheat != null) {
            tableModel.addCheat(cheat);
            EventQueue.invokeLater(() -> {
                scrollToBottom(cheatsScrollPane);
                cheatsTable.getSelectionModel().setSelectionInterval(tableModel
                        .getRowCount() - 1, tableModel.getRowCount() - 1);
            });
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cheatsScrollPane = new javax.swing.JScrollPane();
        cheatsTable = new ToolTipsTable();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        tableButtonsPanel = new javax.swing.JPanel();
        editButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        newButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        showHexCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Cheats");
        setMaximumSize(null);
        setMinimumSize(null);
        setPreferredSize(null);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cheatsScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        cheatsScrollPane.setMaximumSize(null);
        cheatsScrollPane.setMinimumSize(null);

        cheatsTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{

                }
        ));
        cheatsTable.setPreferredSize(null);
        cheatsScrollPane.setViewportView(cheatsTable);

        cancelButton.setMnemonic('C');
        cancelButton.setText("   Cancel   ");
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setMnemonic('O');
        okButton.setText("OK");
        okButton.setFocusable(false);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        editButton.setMnemonic('E');
        editButton.setText("Edit...");
        editButton.setFocusable(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        importButton.setMnemonic('I');
        importButton.setText("Import...");
        importButton.setFocusable(false);
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        exportButton.setMnemonic('x');
        exportButton.setText("Export...");
        exportButton.setFocusable(false);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        deleteButton.setMnemonic('D');
        deleteButton.setText("Delete");
        deleteButton.setFocusable(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        newButton.setMnemonic('N');
        newButton.setText("New...");
        newButton.setFocusable(false);
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        resetButton.setMnemonic('R');
        resetButton.setFocusable(false);
        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        clearButton.setMnemonic('r');
        clearButton.setText("Clear");
        clearButton.setFocusable(false);
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tableButtonsPanelLayout = new javax.swing.GroupLayout(tableButtonsPanel);
        tableButtonsPanel.setLayout(tableButtonsPanelLayout);
        tableButtonsPanelLayout.setHorizontalGroup(
                tableButtonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(tableButtonsPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(newButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(editButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(importButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(exportButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(resetButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearButton)
                                .addGap(0, 0, Short.MAX_VALUE))
        );

        tableButtonsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, clearButton, deleteButton, editButton, exportButton, importButton, newButton, resetButton);

        tableButtonsPanelLayout.setVerticalGroup(
                tableButtonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(tableButtonsPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(tableButtonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(tableButtonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(resetButton)
                                                .addComponent(clearButton))
                                        .addGroup(tableButtonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(exportButton)
                                                .addComponent(importButton)
                                                .addComponent(deleteButton)
                                                .addComponent(editButton)
                                                .addComponent(newButton)))
                                .addGap(0, 0, 0))
        );

        showHexCheckBox.setText("Show hex");
        showHexCheckBox.setFocusPainted(false);
        showHexCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHexCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(cheatsScrollPane, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(tableButtonsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(showHexCheckBox)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(okButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(cheatsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tableButtonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(11, 11, 11)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(showHexCheckBox)
                                        .addComponent(okButton)
                                        .addComponent(cancelButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        final CreateCheatDialog dialog = new CreateCheatDialog(this);
        dialog.setTitle("New Cheat");
        dialog.setCheat(null);
        dialog.setVisible(true);
        addCheat(dialog.getCheat());
        updateButtons();
    }//GEN-LAST:event_newButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        final int rowIndex = cheatsTable.getSelectedRow();
        if (rowIndex >= 0 && rowIndex < tableModel.getRowCount()) {
            final CreateCheatDialog dialog = new CreateCheatDialog(this);
            dialog.setTitle("Edit Cheat");
            dialog.setCheat(tableModel.getCheat(rowIndex));
            dialog.setVisible(true);
            final Cheat cheat = dialog.getCheat();
            if (cheat != null) {
                tableModel.updateCheat(cheat, rowIndex);
            }
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        final int rowIndex = cheatsTable.getSelectedRow();
        if (rowIndex >= 0 && rowIndex < tableModel.getRowCount()) {
            tableModel.deleteCheat(rowIndex);
        }
        updateButtons();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        App.setNoStepPause(true);
        final JFileChooser chooser = createFileChooser("Import Cheats",
                AppPrefs.getInstance().getPaths().getCheatsDir(), FILE_FILTERS);
        if (showOpenDialog(this, chooser, (p, d) -> p.setCheatsDir(d))
                == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = chooser.getSelectedFile();
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> importFile(pleaseWaitDialog, selectedFile)).start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }//GEN-LAST:event_importButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        App.setNoStepPause(true);
        final Paths paths = AppPrefs.getInstance().getPaths();

        final RadioSelectionDialog dialog = new RadioSelectionDialog(this,
                "Select file type:", "Export File", cheatPrefs.getExportFileType(),
                "Cheat text file (.cht)", "Cheat XML file (.xml)");
        dialog.setOkButtonText("Next >");
        dialog.setOkButtonMnemonic('N');
        dialog.setVisible(true);
        if (dialog.isOk()) {
            final String extension = dialog.getSelectedIndex() == 0 ? "cht" : "xml";
            final File file = showSaveAsDialog(this, paths.getCheatsDir(),
                    getFileNameWithoutExtension(App.getEntryFileName())
                            + "." + extension, extension,
                    FILE_FILTERS[dialog.getSelectedIndex() + 1], true);
            if (file != null) {
                final String dir = file.getParent();
                paths.addRecentDirectory(dir);
                paths.setCheatsDir(dir);
                AppPrefs.save();
                final List<Cheat> cheats = tableModel.getCheats();
                final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
                cheatPrefs.setExportFileType(dialog.getSelectedIndex());
                new Thread(() -> exportFile(pleaseWaitDialog, file,
                        dialog.getSelectedIndex() == 0, cheats)).start();
                pleaseWaitDialog.showAfterDelay();
            } else {
                App.setNoStepPause(false);
            }
        } else {
            App.setNoStepPause(false);
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        tableModel.clear();
        updateButtons();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (cheatsTable.isEditing()) {
            cheatsTable.getCellEditor().stopCellEditing();
        }
        if (!tableModel.isModified() || tableHasBeenReset()) {
            tableModel.clear();
        }
        GameCheats.setCheats(tableModel.getCheats());
        GameCheats.save();
        GameCheats.updateMachine();
        cheatPrefs.setShowHex(showHexCheckBox.isSelected());
        AppPrefs.save();
        final CheatSearchFrame cheatSearch = App.getCheatSearchFrame();
        if (cheatSearch != null) {
            cheatSearch.setCheats(tableModel.getCheats());
        }
        closeDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void showHexCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showHexCheckBoxActionPerformed
        tableModel.setShowHex(showHexCheckBox.isSelected());
    }//GEN-LAST:event_showHexCheckBoxActionPerformed

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        if (newCheat != null) {
            final CreateCheatDialog dialog = new CreateCheatDialog(this);
            dialog.setTitle("New Cheat");
            dialog.setCheat(newCheat, CheatCards.Raw);
            dialog.setVisible(true);
            addCheat(dialog.getCheat());
            updateButtons();
        }
    }//GEN-LAST:event_formComponentShown

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        resetCheats();
    }//GEN-LAST:event_resetButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
