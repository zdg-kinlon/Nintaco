package nintaco.gui.familybasic;

import nintaco.preferences.AppPrefs;

import javax.swing.*;
import java.awt.*;

import static nintaco.util.GuiUtil.addLoseFocusListener;
import static nintaco.util.GuiUtil.scaleFonts;

public class FamilyBasicOptionsDialog extends javax.swing.JDialog {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel cpuCyclesPerSecondLabel;
    private javax.swing.JPanel dataRecorderPanel;
    private javax.swing.JSpinner longDelaySpinner;
    private javax.swing.JLabel longLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel samplingPeriodLabel;
    private javax.swing.JSpinner samplingPeriodSpinner;
    private javax.swing.JSpinner shortDelaySpinner;
    private javax.swing.JLabel shortLabel;
    private javax.swing.JPanel typePastePanel;
    public FamilyBasicOptionsDialog(final Window parent) {
        super(parent);
        setModal(true);
        initComponents();
        initSpinners();
        loadFields();
        scaleFonts(this);
        pack();
        setLocationRelativeTo(parent);
    }

    private void closeDialog() {
        dispose();
    }

    private void initSpinners() {
        shortDelaySpinner.setModel(new SpinnerNumberModel(1, 1, 999, 1));
        shortDelaySpinner.setEditor(new JSpinner.NumberEditor(shortDelaySpinner,
                "#"));
        longDelaySpinner.setModel(new SpinnerNumberModel(20, 20, 999, 1));
        longDelaySpinner.setEditor(new JSpinner.NumberEditor(longDelaySpinner,
                "#"));
        samplingPeriodSpinner.setModel(new SpinnerNumberModel(88, 1, 999, 1));
        samplingPeriodSpinner.setEditor(new JSpinner.NumberEditor(
                samplingPeriodSpinner, "#"));
        addLoseFocusListener(this, shortDelaySpinner);
        addLoseFocusListener(this, longDelaySpinner);
        addLoseFocusListener(this, samplingPeriodSpinner);
    }

    private void loadFields() {
        loadFields(AppPrefs.getInstance().getFamilyBasicPrefs());
    }

    private void loadFields(final FamilyBasicPrefs prefs) {
        shortDelaySpinner.setValue(prefs.getTypePasteShortDelay());
        longDelaySpinner.setValue(prefs.getTypePasteLongDelay());
        samplingPeriodSpinner.setValue(prefs.getDataRecorderSamplingPeriod());
    }

    private void saveFields() {
        final FamilyBasicPrefs prefs = AppPrefs.getInstance().getFamilyBasicPrefs();
        prefs.setTypePasteShortDelay((int) shortDelaySpinner.getValue());
        prefs.setTypePasteLongDelay((int) longDelaySpinner.getValue());
        prefs.setDataRecorderSamplingPeriod((int) samplingPeriodSpinner.getValue());
        AppPrefs.save();
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
        typePastePanel = new javax.swing.JPanel();
        longDelaySpinner = new javax.swing.JSpinner();
        shortDelaySpinner = new javax.swing.JSpinner();
        shortLabel = new javax.swing.JLabel();
        longLabel = new javax.swing.JLabel();
        dataRecorderPanel = new javax.swing.JPanel();
        samplingPeriodLabel = new javax.swing.JLabel();
        samplingPeriodSpinner = new javax.swing.JSpinner();
        cpuCyclesPerSecondLabel = new javax.swing.JLabel();
        resetButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Family BASIC Options");
        setMaximumSize(null);
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cancelButton.setMnemonic('C');
        cancelButton.setText(" Cancel ");
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setMnemonic('O');
        okButton.setText("OK");
        okButton.setFocusPainted(false);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        typePastePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Type Paste"));
        typePastePanel.setMaximumSize(null);

        longDelaySpinner.setMaximumSize(null);
        longDelaySpinner.setMinimumSize(null);
        longDelaySpinner.setPreferredSize(null);

        shortDelaySpinner.setMaximumSize(null);
        shortDelaySpinner.setMinimumSize(null);
        shortDelaySpinner.setPreferredSize(null);

        shortLabel.setText("Key press delay:");

        longLabel.setText("End of line delay:");

        javax.swing.GroupLayout typePastePanelLayout = new javax.swing.GroupLayout(typePastePanel);
        typePastePanel.setLayout(typePastePanelLayout);
        typePastePanelLayout.setHorizontalGroup(
                typePastePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(typePastePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(typePastePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(shortLabel)
                                        .addComponent(longLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(typePastePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(shortDelaySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(longDelaySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
        typePastePanelLayout.setVerticalGroup(
                typePastePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(typePastePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(typePastePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(shortLabel)
                                        .addComponent(shortDelaySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(typePastePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(longLabel)
                                        .addComponent(longDelaySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        dataRecorderPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Data Recorder"));
        dataRecorderPanel.setMaximumSize(null);

        samplingPeriodLabel.setText("Sampling period:");

        cpuCyclesPerSecondLabel.setText("CPU cycles/sample");
        cpuCyclesPerSecondLabel.setMaximumSize(null);
        cpuCyclesPerSecondLabel.setMinimumSize(null);
        cpuCyclesPerSecondLabel.setPreferredSize(null);

        javax.swing.GroupLayout dataRecorderPanelLayout = new javax.swing.GroupLayout(dataRecorderPanel);
        dataRecorderPanel.setLayout(dataRecorderPanelLayout);
        dataRecorderPanelLayout.setHorizontalGroup(
                dataRecorderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(dataRecorderPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(samplingPeriodLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(samplingPeriodSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cpuCyclesPerSecondLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        dataRecorderPanelLayout.setVerticalGroup(
                dataRecorderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(dataRecorderPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(dataRecorderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(samplingPeriodLabel)
                                        .addComponent(samplingPeriodSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cpuCyclesPerSecondLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        resetButton.setMnemonic('R');
        resetButton.setText("Reset");
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(typePastePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(dataRecorderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(resetButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(okButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton, resetButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(typePastePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(dataRecorderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton)
                                        .addComponent(okButton)
                                        .addComponent(resetButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        saveFields();
        closeDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        loadFields(new FamilyBasicPrefs());
    }//GEN-LAST:event_resetButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
