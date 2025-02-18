package nintaco.gui.nsf;

import nintaco.input.InputUtil;
import nintaco.input.other.SetNsfOptions;
import nintaco.preferences.AppPrefs;

import javax.swing.*;
import java.awt.*;

import static nintaco.util.GuiUtil.addLoseFocusListener;
import static nintaco.util.GuiUtil.scaleFonts;

public class NsfOptionsDialog extends javax.swing.JDialog {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton defaultsButton;
    private javax.swing.JLabel minutesLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox playInBackgroundCheckBox;
    private javax.swing.JLabel secondsLabel;
    private javax.swing.JCheckBox silenceCheckBox;
    private javax.swing.JSpinner silenceSpinner;
    private javax.swing.JCheckBox trackLengthCheckBox;
    private javax.swing.JSpinner trackLengthSpinner;

    public NsfOptionsDialog(final Window parent) {
        super(parent);
        setModal(true);
        initComponents();
        initSpinners();
        loadFields();
        scaleFonts(this);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initSpinners() {
        silenceSpinner.setModel(new SpinnerNumberModel(1, 1, 99, 1));
        silenceSpinner.setEditor(new JSpinner.NumberEditor(silenceSpinner, "#"));
        trackLengthSpinner.setModel(new SpinnerNumberModel(1, 1, 99, 1));
        trackLengthSpinner.setEditor(new JSpinner.NumberEditor(trackLengthSpinner,
                "#"));
        addLoseFocusListener(this, silenceSpinner);
        addLoseFocusListener(this, trackLengthSpinner);
    }

    private void loadFields() {
        loadFields(AppPrefs.getInstance().getNsfPrefs());
    }

    private void loadFields(final NsfPrefs prefs) {
        silenceSpinner.setValue(prefs.getSilenceSeconds());
        trackLengthSpinner.setValue(prefs.getTrackLengthMinutes());
        silenceCheckBox.setSelected(prefs.isAutomaticallyAdvanceTrack());
        trackLengthCheckBox.setSelected(prefs.isDefaultTrackLength());
        playInBackgroundCheckBox.setSelected(prefs.isPlayInBackground());
        enableComponents();
    }

    private void saveFields() {
        final NsfPrefs prefs = AppPrefs.getInstance().getNsfPrefs();
        prefs.setSilenceSeconds((int) silenceSpinner.getValue());
        prefs.setTrackLengthMinutes((int) trackLengthSpinner.getValue());
        prefs.setAutomaticallyAdvanceTrack(silenceCheckBox.isSelected());
        prefs.setDefaultTrackLength(trackLengthCheckBox.isSelected());
        prefs.setPlayInBackground(playInBackgroundCheckBox.isSelected());
        InputUtil.addOtherInput(new SetNsfOptions(
                prefs.isAutomaticallyAdvanceTrack(), prefs.getSilenceSeconds(),
                prefs.isDefaultTrackLength(), prefs.getTrackLengthMinutes()));
        AppPrefs.save();
    }

    private void enableComponents() {
        silenceSpinner.setEnabled(silenceCheckBox.isSelected());
        trackLengthSpinner.setEnabled(trackLengthCheckBox.isSelected());
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

        trackLengthSpinner = new javax.swing.JSpinner();
        silenceCheckBox = new javax.swing.JCheckBox();
        playInBackgroundCheckBox = new javax.swing.JCheckBox();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        silenceSpinner = new javax.swing.JSpinner();
        secondsLabel = new javax.swing.JLabel();
        trackLengthCheckBox = new javax.swing.JCheckBox();
        minutesLabel = new javax.swing.JLabel();
        defaultsButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("NSF Options");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        trackLengthSpinner.setModel(new javax.swing.SpinnerNumberModel());
        trackLengthSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(trackLengthSpinner, ""));

        silenceCheckBox.setText("Automatically advance track after");
        silenceCheckBox.setFocusPainted(false);
        silenceCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                silenceCheckBoxActionPerformed(evt);
            }
        });

        playInBackgroundCheckBox.setText("Play in background");
        playInBackgroundCheckBox.setFocusPainted(false);

        cancelButton.setMnemonic('C');
        cancelButton.setText(" Cancel ");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setMnemonic('O');
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        secondsLabel.setText("seconds of silence");

        trackLengthCheckBox.setText("Default track length to");
        trackLengthCheckBox.setFocusPainted(false);
        trackLengthCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackLengthCheckBoxActionPerformed(evt);
            }
        });

        minutesLabel.setText("minutes");

        defaultsButton.setMnemonic('D');
        defaultsButton.setText("Defaults");
        defaultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(defaultsButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(okButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton)
                                .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(trackLengthCheckBox)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(trackLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(minutesLabel))
                                        .addComponent(playInBackgroundCheckBox)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(silenceCheckBox)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(silenceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(secondsLabel)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, defaultsButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(playInBackgroundCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(silenceCheckBox)
                                        .addComponent(silenceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(secondsLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(trackLengthCheckBox)
                                        .addComponent(trackLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(minutesLabel))
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton)
                                        .addComponent(okButton)
                                        .addComponent(defaultsButton))
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

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void silenceCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_silenceCheckBoxActionPerformed
        enableComponents();
    }//GEN-LAST:event_silenceCheckBoxActionPerformed

    private void trackLengthCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackLengthCheckBoxActionPerformed
        enableComponents();
    }//GEN-LAST:event_trackLengthCheckBoxActionPerformed

    private void defaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultsButtonActionPerformed
        loadFields(new NsfPrefs());
    }//GEN-LAST:event_defaultsButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
