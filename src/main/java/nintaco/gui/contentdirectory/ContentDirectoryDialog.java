package nintaco.gui.contentdirectory;

import nintaco.preferences.AppPrefs;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static nintaco.files.FileUtil.getWorkingDirectory;
import static nintaco.files.FileUtil.mkdir;
import static nintaco.util.GuiUtil.displayError;
import static nintaco.util.GuiUtil.scaleFonts;

public class ContentDirectoryDialog extends javax.swing.JDialog {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private nintaco.gui.BrowsePanel browsePanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private javax.swing.JButton resetButton;

    public ContentDirectoryDialog(final Window parent) {
        super(parent);
        setModal(true);
        initComponents();
        initBrowsePanel();
        scaleFonts(this);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initBrowsePanel() {
        browsePanel.setFileName(AppPrefs.getInstance().getPaths()
                .getContentDirectory());
        browsePanel.setWindow(this);
        browsePanel.setShowOpenFileChooser(true);
        browsePanel.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        browsePanel.setFileNameSetListener(this::fileNameChanged);
        fileNameChanged();
    }

    private void fileNameChanged() {
        final String dir = browsePanel.getFileName().trim();
        okButton.setEnabled(!dir.isEmpty());
        resetButton.setEnabled(!getWorkingDirectory().equalsIgnoreCase(dir));
    }

    private void closeDialog() {
        dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        browsePanel = new nintaco.gui.BrowsePanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Change Generated Content Directory");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        browsePanel.setMaximumSize(null);
        browsePanel.setMinimumSize(null);
        browsePanel.setPreferredSize(null);

        cancelButton.setMnemonic('C');
        cancelButton.setText(" Cancel ");
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

        resetButton.setMnemonic('R');
        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(resetButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                        .addComponent(browsePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton, resetButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(browsePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(resetButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        final String directory = browsePanel.getFileName().trim();
        if (directory.isEmpty()) {
            return;
        }
        final File dir = mkdir(directory);
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            displayError("Invalid Directory", this,
                    "The specified path is not a valid directory.");
        } else {
            AppPrefs.getInstance().getPaths().setContentDirectory(dir.getPath());
            AppPrefs.save();
            closeDialog();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        browsePanel.setFileName(getWorkingDirectory());
    }//GEN-LAST:event_resetButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
