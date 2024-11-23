package nintaco.gui.screenshots;

import nintaco.App;
import nintaco.gui.exportmedia.preferences.ExportMediaFilePrefs;
import nintaco.gui.image.filters.VideoFilterDescriptor;
import nintaco.gui.image.preferences.Paths;
import nintaco.palettes.PaletteNames;
import nintaco.preferences.AppPrefs;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static nintaco.files.FileUtil.getDirectoryPath;
import static nintaco.files.FileUtil.isDirectory;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.isBlank;

public class ScreenshotOptionsDialog extends javax.swing.JDialog {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox cropBordersCheckBox;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.JTextField directoryTextField;
    private javax.swing.JComboBox<String> filterComboBox;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox<String> paletteComboBox;
    private javax.swing.JLabel paletteLabel;
    private javax.swing.JComboBox<String> scaleComboBox;
    private javax.swing.JLabel scaleLabel;
    private javax.swing.JCheckBox smoothScalingCheckBox;
    private javax.swing.JLabel suffixLabel;
    private javax.swing.JTextField suffixTextField;
    private javax.swing.JCheckBox tvAspectCheckBox;
    private javax.swing.JComboBox<String> typeComboBox;
    private javax.swing.JLabel typeLabel;
    public ScreenshotOptionsDialog(final Window parent) {
        super(parent);
        setModal(true);
        initComponents();
        initTypeComboBox();
        initPaletteComboBox();
        loadFields();
        addLoseFocusListener(this, directoryTextField);
        addLoseFocusListener(this, suffixTextField);
        addTextFieldEditListener(directoryTextField, this::updateOkButton);
        addTextFieldEditListener(suffixTextField, this::updateOkButton);
        scaleFonts(this);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initPaletteComboBox() {
        final List<String> names = new ArrayList<>();
        AppPrefs.getInstance().getPalettes().getPaletteNames(names);
        names.add(0, PaletteNames.CURRENT);
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (final String name : names) {
            model.addElement(name);
        }
        paletteComboBox.setModel(model);
    }

    private void initTypeComboBox() {
        int selectedIndex = 0;
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (final String format : getWritableImageFileFormats()) {
            if ("png".equalsIgnoreCase(format)) {
                selectedIndex = model.getSize();
            }
            model.addElement(format);
        }
        typeComboBox.setModel(model);
        typeComboBox.setSelectedIndex(selectedIndex);
    }

    private void loadFields() {
        directoryTextField.setText(AppPrefs.getInstance().getPaths()
                .getScreenshotsDir());

        final ExportMediaFilePrefs prefs = AppPrefs.getInstance()
                .getScreenshotPrefs();
        typeComboBox.setSelectedIndex(prefs.getFileType());
        cropBordersCheckBox.setSelected(prefs.isCropBorders());
        filterComboBox.setSelectedItem(prefs.getVideoFilter());
        paletteComboBox.setSelectedItem(prefs.getPalette());
        scaleComboBox.setSelectedIndex(prefs.getScale() - 1);
        smoothScalingCheckBox.setSelected(prefs.isSmoothScaling());
        tvAspectCheckBox.setSelected(prefs.isUseTvAspectRatio());
        suffixTextField.setText(prefs.getSuffix());
        updateOkButton();
    }

    private ExportMediaFilePrefs saveFields() {
        final String dir = getDirectoryPath(directoryTextField.getText().trim());
        if (!isBlank(dir)) {
            final Paths paths = AppPrefs.getInstance().getPaths();
            paths.setScreenshotsDir(dir);
            paths.addRecentDirectory(dir);
        }
        final ExportMediaFilePrefs prefs = AppPrefs.getInstance()
                .getScreenshotPrefs();
        prefs.setFileType(typeComboBox.getSelectedIndex());
        prefs.setCropBorders(cropBordersCheckBox.isSelected());
        prefs.setVideoFilter((VideoFilterDescriptor) filterComboBox
                .getSelectedItem());
        prefs.setPalette((String) paletteComboBox.getSelectedItem());
        prefs.setScale(scaleComboBox.getSelectedIndex() + 1);
        prefs.setSmoothScaling(smoothScalingCheckBox.isSelected());
        prefs.setUseTvAspectRatio(tvAspectCheckBox.isSelected());
        prefs.setSuffix(suffixTextField.getText().trim());
        AppPrefs.save();
        return prefs;
    }

    private void closeDialog() {
        dispose();
    }

    private void updateOkButton() {
        final String suffix = suffixTextField.getText().trim();
        if (!suffix.isEmpty()) {
            try {
                String.format(suffix, 123);
            } catch (final Throwable t) {
                okButton.setEnabled(false);
                return;
            }
        }
        final String directory = directoryTextField.getText().trim();
        okButton.setEnabled(!directory.isEmpty() && isDirectory(directory));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox<>();
        directoryLabel = new javax.swing.JLabel();
        directoryTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        filterLabel = new javax.swing.JLabel();
        filterComboBox = new JComboBox(VideoFilterDescriptor.values());
        scaleLabel = new javax.swing.JLabel();
        scaleComboBox = new javax.swing.JComboBox<>();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cropBordersCheckBox = new javax.swing.JCheckBox();
        smoothScalingCheckBox = new javax.swing.JCheckBox();
        tvAspectCheckBox = new javax.swing.JCheckBox();
        suffixLabel = new javax.swing.JLabel();
        suffixTextField = new javax.swing.JTextField();
        paletteLabel = new javax.swing.JLabel();
        paletteComboBox = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Screenshot Options");
        setPreferredSize(null);

        typeLabel.setText("Type:");

        typeComboBox.setFocusable(false);

        directoryLabel.setText("Directory:");

        directoryTextField.setColumns(50);
        directoryTextField.setText(" ");
        directoryTextField.setMaximumSize(null);
        directoryTextField.setMinimumSize(null);
        directoryTextField.setPreferredSize(null);

        browseButton.setText("Browse...");
        browseButton.setFocusPainted(false);

        filterLabel.setText("Filter:");

        filterComboBox.setFocusable(false);
        filterComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterComboBoxActionPerformed(evt);
            }
        });

        scaleLabel.setText("Scale:");

        scaleComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"1x", "2x", "3x", "4x", "5x"}));
        scaleComboBox.setFocusable(false);

        cancelButton.setMnemonic('C');
        cancelButton.setText(" Cancel ");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setMnemonic('O');
        okButton.setText("OK");
        okButton.setEnabled(false);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cropBordersCheckBox.setSelected(true);
        cropBordersCheckBox.setText("Crop borders");
        cropBordersCheckBox.setFocusPainted(false);

        smoothScalingCheckBox.setText("Smooth scaling");
        smoothScalingCheckBox.setFocusPainted(false);

        tvAspectCheckBox.setText("TV aspect");
        tvAspectCheckBox.setFocusPainted(false);

        suffixLabel.setText("Suffix:");

        suffixTextField.setColumns(15);
        suffixTextField.setText("-03d%");

        paletteLabel.setText("Palette:");

        paletteComboBox.setFocusable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(directoryLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(directoryTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(browseButton))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(okButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(cropBordersCheckBox)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(smoothScalingCheckBox)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(tvAspectCheckBox))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(typeLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(filterLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(paletteLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(paletteComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(scaleLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(scaleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(suffixLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(suffixTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(directoryLabel)
                                        .addComponent(directoryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(browseButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(scaleLabel)
                                                .addComponent(scaleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(typeLabel)
                                                .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(filterLabel)
                                                .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(paletteLabel)
                                                .addComponent(paletteComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(suffixLabel)
                                                .addComponent(suffixTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cropBordersCheckBox)
                                        .addComponent(smoothScalingCheckBox)
                                        .addComponent(tvAspectCheckBox))
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton)
                                        .addComponent(okButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        saveFields();
        closeDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void filterComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterComboBoxActionPerformed
        VideoFilterDescriptor filter = (VideoFilterDescriptor) filterComboBox
                .getSelectedItem();
        if (filter == VideoFilterDescriptor.Current) {
            scaleLabel.setEnabled(false);
            scaleComboBox.setEnabled(false);
            filter = App.getImageFrame().getImagePane().getVideoFilterDescriptor();
            if (filter == null) {
                filter = VideoFilterDescriptor.NoFilter;
            }
        } else {
            scaleLabel.setEnabled(true);
            scaleComboBox.setEnabled(true);
        }
        scaleComboBox.setSelectedIndex(filter == VideoFilterDescriptor.NoFilter
                ? 0 : filter.getScale() - 1);
        smoothScalingCheckBox.setSelected(filter.isSmoothScaling());
        tvAspectCheckBox.setSelected(filter.isUseTvAspectRatio());
    }//GEN-LAST:event_filterComboBoxActionPerformed
    // End of variables declaration//GEN-END:variables
}
