package nintaco.gui;

import nintaco.files.FilePath;
import nintaco.files.FileUtil;
import nintaco.gui.archive.ArchiveFileChooser;
import nintaco.gui.image.preferences.Paths;
import nintaco.util.EDT;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.function.BiConsumer;

import static nintaco.files.ArchiveEntry.toNames;
import static nintaco.files.FileUtil.*;
import static nintaco.util.GuiUtil.*;

public class BrowsePanel extends javax.swing.JPanel {

    private Window window;
    private String fileChooserTitle;
    private String defaultDirectory;
    private String fileExtension;
    private boolean showOpenFileChooser;
    private BiConsumer<Paths, String> saveDirectoryConsumer;
    private FileFilter[] fileFilters;
    private Runnable browseButtonListener;
    private Runnable fileNameSetListener;
    private int fileSelectionMode = JFileChooser.FILES_ONLY;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField fileTextField;

    public BrowsePanel(final Window window) {
        this();
        setWindow(window);
    }

    // Default constructor required by Netbeans GUI Builder
    public BrowsePanel() {
        initComponents();
        setPreferredSize(null);
        fileTextField.setColumns(60);
        addTextFieldEditListener(fileTextField, this::textFieldEdited);
    }

    private void textFieldEdited() {
        if (fileNameSetListener != null) {
            fileNameSetListener.run();
        }
    }

    public void setFileSelectionMode(final int fileSelectionMode) {
        this.fileSelectionMode = fileSelectionMode;
    }

    public void setWindow(final Window window) {
        this.window = window;
        addLoseFocusListener(window, fileTextField);
    }

    public String getFileName() {
        return fileTextField.getText().trim();
    }

    public void setFileName(final String fileName) {
        fileTextField.setText(fileName);
    }

    public Runnable getBrowseButtonListener() {
        return browseButtonListener;
    }

    public void setBrowseButtonListener(final Runnable browseButtonListener) {
        this.browseButtonListener = browseButtonListener;
    }

    public Runnable getFileNameSetListener() {
        return fileNameSetListener;
    }

    public void setFileNameSetListener(final Runnable fileNameSetListener) {
        this.fileNameSetListener = fileNameSetListener;
    }

    public void addFileTextFieldEditedListener(final Runnable runnable) {
        addTextFieldEditListener(fileTextField, runnable);
    }

    public FileFilter[] getFileFilters() {
        return fileFilters;
    }

    public void setFileFilters(final FileFilter... fileFilters) {
        this.fileFilters = fileFilters;
    }

    public BiConsumer<Paths, String> getSaveDirectoryConsumer() {
        return saveDirectoryConsumer;
    }

    public void setSaveDirectoryConsumer(
            final BiConsumer<Paths, String> saveDirectoryConsumer) {
        this.saveDirectoryConsumer = saveDirectoryConsumer;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(final String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileChooserTitle() {
        return fileChooserTitle;
    }

    public void setFileChooserTitle(final String fileChooserTitle) {
        this.fileChooserTitle = fileChooserTitle;
    }

    public String getDefaultDirectory() {
        return defaultDirectory;
    }

    public void setDefaultDirectory(final String defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
    }

    public boolean isShowOpenFileChooser() {
        return showOpenFileChooser;
    }

    public void setShowOpenFileChooser(final boolean showOpenFileChooser) {
        this.showOpenFileChooser = showOpenFileChooser;
    }

    private void displaySaveAsFileChooser(final String enteredDir,
                                          final String enteredFile) {
        final File file = showSaveAsDialog(window, enteredDir, enteredFile,
                fileExtension, fileFilters == null ? null : fileFilters[0],
                false, fileChooserTitle);
        if (file != null) {
            fileTextField.setText(file.getPath());
            if (browseButtonListener != null) {
                browseButtonListener.run();
            }
            if (fileNameSetListener != null) {
                fileNameSetListener.run();
            }
        }
    }

    private void displayOpenFileChooser(final String enteredDir) {

        final JFileChooser chooser = createFileChooser(fileChooserTitle,
                enteredDir, fileFilters);
        chooser.setFileSelectionMode(fileSelectionMode);
        if (showOpenDialog(this, chooser, saveDirectoryConsumer)
                == JFileChooser.APPROVE_OPTION) {
            final String fileName = chooser.getSelectedFile().toString();
            if (fileSelectionMode != JFileChooser.DIRECTORIES_ONLY
                    && !isDirectory(fileName) && isArchiveFile(fileName)) {
                final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(window);
                pleaseWaitDialog.setMessage("Opening archive file...");
                new Thread(() -> openArchiveFile(fileName, pleaseWaitDialog)).start();
                pleaseWaitDialog.showAfterDelay();
            } else {
                fileTextField.setText(fileName);
                if (browseButtonListener != null) {
                    browseButtonListener.run();
                }
                if (fileNameSetListener != null) {
                    fileNameSetListener.run();
                }
            }
        }
    }

    private void openArchiveFile(final String archiveFileName,
                                 final PleaseWaitDialog pleaseWaitDialog) {

        java.util.List<String> entries = null;
        try {
            entries = toNames(getArchiveEntries(archiveFileName, null));
        } catch (Throwable t) {
            //t.printStackTrace();
        }
        final java.util.List<String> files = entries;
        pleaseWaitDialog.dispose();

        if (files == null) {
            displayError(window, "Failed to open archive file.");
        } else {
            switch (files.size()) {
                case 0:
                    displayError(window, "The archive does not contain any files.");
                    break;
                case 1:
                    EDT.async(() -> {
                        fileTextField.setText(new FilePath(files.get(0), archiveFileName)
                                .toLongString());
                        if (browseButtonListener != null) {
                            browseButtonListener.run();
                        }
                        if (fileNameSetListener != null) {
                            fileNameSetListener.run();
                        }
                    });
                    break;
                default:
                    EDT.async(() -> showArchiveFileChooser(archiveFileName,
                            files));
                    break;
            }
        }
    }

    private void showArchiveFileChooser(final String archiveFileName,
                                        final java.util.List<String> files) {
        final ArchiveFileChooser archiveChooser = new ArchiveFileChooser(window,
                files, -1);
        archiveChooser.setVisible(true);

        final String entryFileName = archiveChooser.getSelectedFile();
        if (entryFileName != null) {
            fileTextField.setText(new FilePath(entryFileName, archiveFileName)
                    .toLongString());
            if (browseButtonListener != null) {
                browseButtonListener.run();
            }
            if (fileNameSetListener != null) {
                fileNameSetListener.run();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(fileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(browseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed

        String enteredDir;
        String enteredFile = "";
        String enteredFilePath = fileTextField.getText().trim();
        if (!enteredFilePath.isEmpty()) {
            final int lt = enteredFilePath.indexOf('<');
            if (lt >= 0) {
                final int gt = enteredFilePath.indexOf('>', lt + 1);
                if (gt >= 0) {
                    enteredFile = enteredFilePath.substring(lt + 1, gt);
                } else {
                    enteredFile = enteredFilePath.substring(lt + 1);
                }
                enteredFilePath = enteredFilePath.substring(0, lt);
            } else if (!isDirectory(enteredFilePath)) {
                enteredFile = FileUtil.getFileName(enteredFilePath);
            }
            enteredDir = getDirectoryPath(enteredFilePath);
        } else {
            enteredDir = null;
        }
        if (enteredDir == null) {
            enteredDir = defaultDirectory;
        }

        if (showOpenFileChooser) {
            displayOpenFileChooser(enteredDir);
        } else {
            displaySaveAsFileChooser(enteredDir, enteredFile);
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
