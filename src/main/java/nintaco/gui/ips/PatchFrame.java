package nintaco.gui.ips;

import nintaco.App;
import nintaco.MessageException;
import nintaco.files.FilePath;
import nintaco.files.FileUtil;
import nintaco.files.IpsUtil;
import nintaco.gui.BrowsePanel;
import nintaco.gui.FileExtensionFilter;
import nintaco.gui.InformationDialog;
import nintaco.gui.PleaseWaitDialog;
import nintaco.gui.image.ImageFrame;
import nintaco.gui.image.preferences.Paths;
import nintaco.preferences.AppPrefs;
import nintaco.util.EDT;

import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;

import static nintaco.files.FileUtil.*;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.isBlank;

public class PatchFrame extends javax.swing.JFrame {

    private final boolean applyIPS;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private nintaco.gui.BrowsePanel browsePanel1;
    private nintaco.gui.BrowsePanel browsePanel2;
    private nintaco.gui.BrowsePanel browsePanel3;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;

    public PatchFrame(final boolean applyIPS) {
        this.applyIPS = applyIPS;
        initComponents();
        initBrowsePanels();
        setTitle(applyIPS ? "Apply IPS Patch" : "Create IPS Patch");
        enableOkButton();
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
    }

    public void destroy() {
        dispose();
    }

    private void closeFrame() {
        if (applyIPS) {
            App.destroyApplyIpsPatchFrame();
        } else {
            App.destroyCreateIpsPatchFrame();
        }
    }

    private void initBrowsePanels() {

        final Paths paths = AppPrefs.getInstance().getPaths();
        mkdir(paths.getPatchesDir());
        browsePanel1.setFileFilters(ImageFrame.FileExtensionFilters);
        browsePanel1.setFileChooserTitle("Select Original File");
        browsePanel1.setDefaultDirectory(paths.getFilesDir());
        browsePanel1.setSaveDirectoryConsumer((p, d) -> p.setFilesDir(d));
        browsePanel1.setShowOpenFileChooser(true);
        browsePanel2.setShowOpenFileChooser(true);
        browsePanel3.setShowOpenFileChooser(false);
        browsePanel1.addFileTextFieldEditedListener(this::enableOkButton);
        browsePanel2.addFileTextFieldEditedListener(this::enableOkButton);
        browsePanel3.addFileTextFieldEditedListener(this::enableOkButton);
        if (applyIPS) {
            final IpsPatchPrefs prefs = AppPrefs.getInstance()
                    .getApplyIpsPatchPrefs();
            browsePanel1.setFileName(prefs.getOriginal());
            browsePanel2.setFileName(prefs.getPatch());
            browsePanel3.setFileName(prefs.getModified());

            browsePanel1.setBrowseButtonListener(this::setApplyBrowsePanel3FileName);

            ((TitledBorder) browsePanel2.getBorder()).setTitle("Patch File (in)");
            browsePanel2.setFileChooserTitle("Select Patch File");
            browsePanel2.setFileFilters(new FileExtensionFilter(0,
                    "IPS files (*.ips)", "ips"));
            browsePanel2.setDefaultDirectory(paths.getPatchesDir());
            browsePanel2.setSaveDirectoryConsumer((p, d) -> p.setPatchesDir(d));

            ((TitledBorder) browsePanel3.getBorder()).setTitle("Modified File (out)");
            browsePanel3.setFileChooserTitle("Select Modified File");
            browsePanel3.setFileExtension(null);
            browsePanel3.setFileFilters(ImageFrame.FileExtensionFilters);
            browsePanel3.setDefaultDirectory(paths.getFilesDir());
            browsePanel3.setBrowseButtonListener(this::setApplyBrowsePanel3FileName);
            browsePanel3.setSaveDirectoryConsumer((p, d) -> p.setFilesDir(d));
        } else {
            final IpsPatchPrefs prefs = AppPrefs.getInstance()
                    .getCreateIpsPatchPrefs();
            browsePanel1.setFileName(prefs.getOriginal());
            browsePanel2.setFileName(prefs.getModified());
            browsePanel3.setFileName(prefs.getPatch());

            browsePanel1.setBrowseButtonListener(this::setCreateBrowsePanel3FileName);

            ((TitledBorder) browsePanel2.getBorder()).setTitle("Modified File (in)");
            browsePanel2.setFileChooserTitle("Select Modified File");
            browsePanel2.setFileFilters(ImageFrame.FileExtensionFilters);
            browsePanel2.setDefaultDirectory(paths.getFilesDir());
            browsePanel2.setSaveDirectoryConsumer((p, d) -> p.setFilesDir(d));

            ((TitledBorder) browsePanel3.getBorder()).setTitle("Patch File (out)");
            browsePanel3.setFileChooserTitle("Select Patch File");
            browsePanel3.setFileExtension("ips");
            browsePanel3.setFileFilters(new FileExtensionFilter(0,
                    "IPS files (*.ips)", "ips"));
            browsePanel3.setDefaultDirectory(paths.getPatchesDir());
            browsePanel2.setSaveDirectoryConsumer((p, d) -> p.setPatchesDir(d));
            browsePanel3.setBrowseButtonListener(this::setCreateBrowsePanel3FileName);
        }
    }

    private void setApplyBrowsePanel3FileName() {
        if (isBlank(browsePanel3.getFileName())) {
            String dir = null;
            String fileName = "modified.nes";
            if (!isBlank(browsePanel1.getFileName())) {
                final FilePath filePath = FilePath.fromLongString(browsePanel1
                        .getFileName());
                final String entryName = filePath.getEntryFileName();
                if (!isBlank(entryName)) {
                    fileName = FileUtil.getFileNameWithoutExtension(entryName)
                            + "-modified." + getFileExtension(entryName);
                }
                if (!isBlank(filePath.getArchivePath())) {
                    dir = getDirectoryPath(filePath.getArchivePath());
                } else if (!isBlank(filePath.getEntryPath())) {
                    dir = getDirectoryPath(filePath.getEntryPath());
                }
            }
            if (dir == null) {
                dir = AppPrefs.getInstance().getPaths().getFilesDir();
            }
            browsePanel3.setFileName(FileUtil.appendSeparator(dir) + fileName);
        }
    }

    private void setCreateBrowsePanel3FileName() {
        if (isBlank(browsePanel3.getFileName())) {
            String fileName = "out.ips";
            if (!isBlank(browsePanel1.getFileName())) {
                final FilePath filePath = FilePath.fromLongString(browsePanel1
                        .getFileName());
                final String entryName = filePath.getEntryFileName();
                if (!isBlank(entryName)) {
                    fileName = FileUtil.getFileNameWithoutExtension(entryName)
                            + ".ips";
                }
            }
            browsePanel3.setFileName(FileUtil.appendSeparator(
                    AppPrefs.getInstance().getPaths().getPatchesDir()) + fileName);
        }
    }

    private void enableOkButton() {
        okButton.setEnabled(!(isBlank(browsePanel1.getFileName())
                || isBlank(browsePanel2.getFileName())
                || isBlank(browsePanel3.getFileName())));
    }

    private void processFiles(final String fileName1, final String fileName2,
                              final String fileName3, final PleaseWaitDialog pleaseWaitDialog) {
        if (applyIPS) {
            applyIpsPatch(fileName1, fileName2, fileName3, pleaseWaitDialog);
        } else {
            createIpsPatch(fileName1, fileName2, fileName3, pleaseWaitDialog);
        }
    }

    private void applyIpsPatch(final String original, final String patch,
                               final String modified, final PleaseWaitDialog pleaseWaitDialog) {

        String errorMessage = null;
        try {
            IpsUtil.applyIPS(original, patch, modified);
        } catch (final MessageException m) {
            errorMessage = m.getMessage();
        } catch (final Throwable t) {
            errorMessage = "Failed to apply IPS patch.";
        }
        pleaseWaitDialog.dispose();
        if (errorMessage == null) {
            EDT.sync(() -> displayMessage("Patching Completed", this,
                    "The file was successfully patched.",
                    InformationDialog.IconType.INFORMATION));
            EDT.async(this::closeFrame);
        } else {
            displayError("Apply IPS Error", this, errorMessage);
        }
        App.setNoStepPause(false);
    }

    private void createIpsPatch(final String original, final String modified,
                                final String patch, final PleaseWaitDialog pleaseWaitDialog) {

        String errorMessage = null;
        try {
            IpsUtil.createIPS(original, modified, patch);
        } catch (final MessageException m) {
            errorMessage = m.getMessage();
        } catch (final Throwable t) {
            errorMessage = "Failed to create IPS patch.";
        }
        pleaseWaitDialog.dispose();
        if (errorMessage == null) {
            EDT.sync(() -> displayMessage("Patch Creation Completed", this,
                    "The IPS patch was successfully created.",
                    InformationDialog.IconType.INFORMATION));
            EDT.async(this::closeFrame);
        } else {
            displayError("Create IPS Error", this, errorMessage);
        }
        App.setNoStepPause(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        browsePanel1 = new BrowsePanel(this);
        browsePanel2 = new BrowsePanel(this);
        browsePanel3 = new BrowsePanel(this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Create IPS Patch");
        setMaximumSize(null);
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cancelButton.setText(" Cancel ");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        browsePanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Original File (in)"));

        browsePanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Modified File (in)"));

        browsePanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Patch File (out)"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(okButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton))
                                        .addComponent(browsePanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(browsePanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(browsePanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(browsePanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browsePanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browsePanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton)
                                        .addComponent(okButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        final String fileName1 = browsePanel1.getFileName();
        final String fileName2 = browsePanel2.getFileName();
        final String fileName3 = browsePanel3.getFileName();

        if (isBlank(fileName1)) {
            displayError("Missing Field", this,
                    "Please provide the original file name.");
            return;
        }
        if (isBlank(fileName2)) {
            displayError("Missing Field", this, "Please provide the %s file name.",
                    applyIPS ? "patch" : "modified");
            return;
        }
        if (isBlank(fileName3)) {
            displayError(this, "Please provide the %s file name.", applyIPS
                    ? "modified" : "patch");
            return;
        }

        final FilePath filePath1 = FilePath.fromLongString(fileName1);
        if (filePath1 == null) {
            displayError("Invalid File Name", this,
                    "The original file name is not valid.");
            return;
        } else if (!new File(filePath1.getOuterPath()).exists()) {
            displayError("File Not Found", this,
                    "Failed to locate the original file.");
            return;
        }

        final FilePath filePath2 = FilePath.fromLongString(fileName2);
        if (filePath2 == null) {
            displayError("Invalid File Name", this,
                    "The %s file name is not valid.", applyIPS ? "patch" : "modified");
        } else if (!new File(filePath2.getOuterPath()).exists()) {
            displayError("File Not Found", this,
                    "Failed to locate the %s file.", applyIPS ? "patch" : "modified");
            return;
        }

        if (!confirmOverwrite(this, fileName3)) {
            return;
        }

        if (applyIPS) {
            final IpsPatchPrefs prefs = AppPrefs.getInstance()
                    .getApplyIpsPatchPrefs();
            prefs.setOriginal(fileName1);
            prefs.setPatch(fileName2);
            prefs.setModified(fileName3);
        } else {
            final IpsPatchPrefs prefs = AppPrefs.getInstance()
                    .getCreateIpsPatchPrefs();
            prefs.setOriginal(fileName1);
            prefs.setModified(fileName2);
            prefs.setPatch(fileName3);
        }
        AppPrefs.save();

        App.setNoStepPause(true);
        final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
        new Thread(() -> processFiles(fileName1, fileName2, fileName3,
                pleaseWaitDialog)).start();
        pleaseWaitDialog.showAfterDelay();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeFrame();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing
    // End of variables declaration//GEN-END:variables
}
