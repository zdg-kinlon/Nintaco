package cn.kinlon.emu.gui.fds;

import cn.kinlon.emu.App;
import cn.kinlon.emu.files.FilePath;
import cn.kinlon.emu.gui.FileExtensionFilter;
import cn.kinlon.emu.gui.PleaseWaitDialog;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.utils.EDT;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;

import static cn.kinlon.emu.files.FileUtil.*;
import static cn.kinlon.emu.gui.fds.DiskActivityIndicator.*;
import static cn.kinlon.emu.utils.GuiUtil.*;
import static cn.kinlon.emu.utils.StringUtils.isBlank;
import static cn.kinlon.emu.utils.ThreadUtils.async_io;

public class FamicomDiskSystemOptionsDialog extends javax.swing.JDialog {

    private boolean loadingsBIOS;
    private FilePath biosFilePath;
    private DiskActivityIndicator diskActivityIndicator;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel biosFilePanel;
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton capsLockRadioButton;
    private javax.swing.JPanel driveActivityIndicatorPanel;
    private javax.swing.JCheckBox fastForwardCheckBox;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JRadioButton kanaLockRadioButton;
    private javax.swing.JRadioButton noneRadioButton;
    private javax.swing.JRadioButton numLockRadioButton;
    private javax.swing.JButton okButton;
    private javax.swing.JRadioButton scrollLockRadioButton;

    public FamicomDiskSystemOptionsDialog(final Window parent) {
        super(parent);
        setModal(true);
        initComponents();
        initTextField();
        loadFields();
        scaleFonts(this);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initTextField() {
        biosFilePanel.setPreferredSize(null);
        driveActivityIndicatorPanel.setPreferredSize(null);
        fileTextField.setColumns(60);
        addTextFieldEditListener(fileTextField, this::fileTextFieldEdited);
        addLoseFocusListener(this, fileTextField);
    }

    private void closeDialog() {
        if (!loadingsBIOS) {
            dispose();
        }
    }

    private void loadFields() {
        final FamicomDiskSystemPrefs prefs = AppPrefs.getInstance()
                .getFamicomDiskSystemPrefs();
        final FilePath biosFile = prefs.getBiosFile();
        if (biosFile != null) {
            fileTextField.setText(biosFile.toLongString());
        }
        switch (prefs.getDiskActivityIndicator()) {
            case NONE:
                noneRadioButton.setSelected(true);
                break;
            case NUM_LOCK:
                numLockRadioButton.setSelected(true);
                break;
            case CAPS_LOCK:
                capsLockRadioButton.setSelected(true);
                break;
            case SCROLL_LOCK:
                scrollLockRadioButton.setSelected(true);
                break;
            case KANA_LOCK:
                kanaLockRadioButton.setSelected(true);
                break;
        }
        fastForwardCheckBox.setSelected(prefs.isFastForwardDuringDiskAccess());
    }

    private void saveFields() {
        final FamicomDiskSystemPrefs prefs = AppPrefs.getInstance()
                .getFamicomDiskSystemPrefs();
        prefs.setBiosFile(biosFilePath);
        prefs.setDiskActivityIndicator(diskActivityIndicator);
        prefs.setFastForwardDuringDiskAccess(fastForwardCheckBox.isSelected());
        AppPrefs.save();
    }

    private void fileTextFieldEdited() {
        okButton.setEnabled(!isBlank(fileTextField.getText()));
    }

    private void loadBIOS(final FilePath filePath,
                          final PleaseWaitDialog pleaseWaitDialog) {

        try {
            getInputStream(filePath, (in, length) -> {
                if (length != 0x2000) {
                    displayLoadBiosError(pleaseWaitDialog,
                            "<p><b>Invalid BIOS file.</b></p>"
                                    + "<p>The size of the provided file is not 8192 bytes.</p>");
                    return;
                }
                try {
                    App.loadFdsBIOS(in, length);
                } catch (final Throwable t) {
                    displayLoadBiosError(pleaseWaitDialog);
                    return;
                }
                pleaseWaitDialog.dispose();
                EDT.async(this::biosLoadCompleted);
            });
        } catch (final FileNotFoundException f) {
            displayLoadBiosError(pleaseWaitDialog, "BIOS file not found.");
        } catch (final Throwable t) {
            displayLoadBiosError(pleaseWaitDialog);
        } finally {
            pleaseWaitDialog.dispose();
            EDT.async(() -> loadingsBIOS = false);
        }
    }

    private void biosLoadCompleted() {
        loadingsBIOS = false;
        saveFields();
        App.fireDiskActivityIndicatorChanged();
        dispose();
    }

    private void displayLoadBiosError(final PleaseWaitDialog pleaseWaitDialog) {
        displayLoadBiosError(pleaseWaitDialog, "Failed to load BIOS file.");
    }

    private void displayLoadBiosError(final PleaseWaitDialog pleaseWaitDialog,
                                      final String errorMessage) {
        pleaseWaitDialog.dispose();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        biosFilePanel = new javax.swing.JPanel();
        fileTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        driveActivityIndicatorPanel = new javax.swing.JPanel();
        noneRadioButton = new javax.swing.JRadioButton();
        numLockRadioButton = new javax.swing.JRadioButton();
        capsLockRadioButton = new javax.swing.JRadioButton();
        scrollLockRadioButton = new javax.swing.JRadioButton();
        kanaLockRadioButton = new javax.swing.JRadioButton();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        fastForwardCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Famicom Disk System Options");
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        biosFilePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("BIOS File"));
        biosFilePanel.setMaximumSize(null);

        fileTextField.setColumns(60);
        fileTextField.setMaximumSize(null);
        fileTextField.setMinimumSize(null);
        fileTextField.setPreferredSize(null);

        browseButton.setText("Browse...");
        browseButton.setMaximumSize(null);
        browseButton.setMinimumSize(null);
        browseButton.setPreferredSize(null);
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout biosFilePanelLayout = new javax.swing.GroupLayout(biosFilePanel);
        biosFilePanel.setLayout(biosFilePanelLayout);
        biosFilePanelLayout.setHorizontalGroup(
                biosFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(biosFilePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(fileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        biosFilePanelLayout.setVerticalGroup(
                biosFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(biosFilePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(biosFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(browseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        biosFilePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, browseButton, fileTextField);

        driveActivityIndicatorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Drive Activity Indicator"));
        driveActivityIndicatorPanel.setMaximumSize(null);

        buttonGroup.add(noneRadioButton);
        noneRadioButton.setText("None");
        noneRadioButton.setPreferredSize(null);

        buttonGroup.add(numLockRadioButton);
        numLockRadioButton.setText("Num Lock");
        numLockRadioButton.setPreferredSize(null);

        buttonGroup.add(capsLockRadioButton);
        capsLockRadioButton.setText("Caps Lock");
        capsLockRadioButton.setPreferredSize(null);

        buttonGroup.add(scrollLockRadioButton);
        scrollLockRadioButton.setText("Scroll Lock");
        scrollLockRadioButton.setPreferredSize(null);

        buttonGroup.add(kanaLockRadioButton);
        kanaLockRadioButton.setText("Kana Lock");
        kanaLockRadioButton.setPreferredSize(null);

        javax.swing.GroupLayout driveActivityIndicatorPanelLayout = new javax.swing.GroupLayout(driveActivityIndicatorPanel);
        driveActivityIndicatorPanel.setLayout(driveActivityIndicatorPanelLayout);
        driveActivityIndicatorPanelLayout.setHorizontalGroup(
                driveActivityIndicatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(driveActivityIndicatorPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(noneRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(numLockRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(capsLockRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(scrollLockRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(kanaLockRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        driveActivityIndicatorPanelLayout.setVerticalGroup(
                driveActivityIndicatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(driveActivityIndicatorPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(driveActivityIndicatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(noneRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(numLockRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(capsLockRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scrollLockRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(kanaLockRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        cancelButton.setMnemonic('C');
        cancelButton.setText("  Cancel  ");
        cancelButton.setMaximumSize(null);
        cancelButton.setMinimumSize(null);
        cancelButton.setPreferredSize(null);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setMnemonic('O');
        okButton.setText("OK");
        okButton.setMaximumSize(null);
        okButton.setMinimumSize(null);
        okButton.setPreferredSize(null);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        fastForwardCheckBox.setSelected(true);
        fastForwardCheckBox.setText("Fast forward during disk access");
        fastForwardCheckBox.setPreferredSize(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(fastForwardCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(driveActivityIndicatorPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(biosFilePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(biosFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(driveActivityIndicatorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(fastForwardCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed

        final String enteredDir;
        String enteredFilePath = fileTextField.getText().trim();
        if (!enteredFilePath.isEmpty()) {
            final int lt = enteredFilePath.indexOf('<');
            if (lt >= 0) {
                enteredFilePath = enteredFilePath.substring(0, lt);
            }
            enteredDir = getDirectoryPath(enteredFilePath);
        } else {
            enteredDir = null;
        }

        final JFileChooser chooser = createFileChooser("Open BIOS File",
                enteredDir != null ? enteredDir : AppPrefs.getInstance().getPaths()
                        .getFdsBiosDir(), new FileExtensionFilter(0, "All files (*.*)"));
        if (showOpenDialog(this, chooser, (p, d) -> p.setFdsBiosDir(d))
                == JFileChooser.APPROVE_OPTION) {
            final String fileName = chooser.getSelectedFile().toString();
            fileTextField.setText(fileName);
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (loadingsBIOS) {
            return;
        }
        final String file = fileTextField.getText().trim();
        if (file.isEmpty()) {
            return;
        }
        biosFilePath = FilePath.fromLongString(file);
        if (biosFilePath == null) {
            return;
        }
        if (numLockRadioButton.isSelected()) {
            diskActivityIndicator = NUM_LOCK;
        } else if (capsLockRadioButton.isSelected()) {
            diskActivityIndicator = CAPS_LOCK;
        } else if (scrollLockRadioButton.isSelected()) {
            diskActivityIndicator = SCROLL_LOCK;
        } else if (kanaLockRadioButton.isSelected()) {
            diskActivityIndicator = KANA_LOCK;
        } else {
            diskActivityIndicator = NONE;
        }
        loadingsBIOS = true;
        final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
        pleaseWaitDialog.setMessage("Loading BIOS file...");
        async_io(() -> loadBIOS(biosFilePath, pleaseWaitDialog));
        pleaseWaitDialog.showAfterDelay();
    }//GEN-LAST:event_okButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
