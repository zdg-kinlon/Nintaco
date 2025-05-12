package cn.kinlon.emu.gui.userinterface;

import cn.kinlon.emu.preferences.AppPrefs;

import javax.swing.*;

import static cn.kinlon.emu.utils.GuiUtil.addLoseFocusListener;
import static cn.kinlon.emu.utils.GuiUtil.scaleFonts;

public class UserInterfaceDialog extends javax.swing.JDialog {

    private boolean ok;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox acceptBackgroundInputCheckBox;
    private javax.swing.JCheckBox allowMultipleInstancesCheckBox;
    private javax.swing.JCheckBox applyIpsPatchesCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox confirmExitCheckBox;
    private javax.swing.JCheckBox confirmHotSwapCheckBox;
    private javax.swing.JCheckBox confirmResetCheckBox;
    private javax.swing.JButton defaultsButton;
    private javax.swing.JCheckBox disableScreensaverCheckBox;
    private javax.swing.JCheckBox enterFullscreenCheckBox;
    private javax.swing.JCheckBox hideMenuBarCheckBox;
    private javax.swing.JComboBox<String> initialRamStateComboBox;
    private javax.swing.JLabel initialRamStateLabel;
    private javax.swing.JComboBox<String> interframeDelayComboBox;
    private javax.swing.JLabel interframeDelayLabel;
    private javax.swing.JCheckBox launchFileOpenCheckBox;
    private javax.swing.JLabel maxLagFramesLabel;
    private javax.swing.JSpinner maxLagFramesSpinner;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox pauseMenuCheckBox;
    private javax.swing.JCheckBox referenceDatabaseCheckBox;
    private javax.swing.JCheckBox runInBackgroundCheckBox;
    private javax.swing.JCheckBox useMulticoreFilteringCheckBox;
    private javax.swing.JCheckBox useVsyncCheckBox;

    private void initSpinner() {
        maxLagFramesSpinner.setModel(new SpinnerNumberModel(1, 1, 999, 1));
        maxLagFramesSpinner.setEditor(new JSpinner.NumberEditor(maxLagFramesSpinner,
                "#"));
        addLoseFocusListener(this, maxLagFramesSpinner);
    }

    private void loadFields() {
        loadFields(AppPrefs.getInstance().getUserInterfacePrefs());
    }

    private void loadFields(final UserInterfacePrefs prefs) {
        launchFileOpenCheckBox.setSelected(prefs.isLaunchFileOpen());
        referenceDatabaseCheckBox.setSelected(prefs.isReferenceDatabase());
        applyIpsPatchesCheckBox.setSelected(prefs.isApplyIpsPatches());
        hideMenuBarCheckBox.setSelected(prefs.isHideMenuBar());
        enterFullscreenCheckBox.setSelected(prefs.isEnterFullscreen());
        pauseMenuCheckBox.setSelected(prefs.isPauseMenu());
        confirmResetCheckBox.setSelected(prefs.isConfirmReset());
        confirmExitCheckBox.setSelected(prefs.isConfirmExit());
        confirmHotSwapCheckBox.setSelected(prefs.isConfirmHotSwap());
        disableScreensaverCheckBox.setSelected(prefs.isDisableScreensaver());
        allowMultipleInstancesCheckBox.setSelected(prefs
                .isAllowMultipleInstances());
        runInBackgroundCheckBox.setSelected(prefs.isRunInBackground());
        acceptBackgroundInputCheckBox.setSelected(prefs.isAcceptBackgroundInput());
        useVsyncCheckBox.setSelected(prefs.isUseVsync());
        useMulticoreFilteringCheckBox.setSelected(prefs.isUseMulticoreFiltering());
        maxLagFramesSpinner.setValue(prefs.getMaxLagFrames());
        interframeDelayComboBox.setSelectedItem(prefs.getInterframeDelay());
        initialRamStateComboBox.setSelectedItem(prefs.getInitialRamState());
    }

    private void captureFields() {
        final UserInterfacePrefs prefs = AppPrefs.getInstance()
                .getUserInterfacePrefs();
        prefs.setLaunchFileOpen(launchFileOpenCheckBox.isSelected());
        prefs.setReferenceDatabase(referenceDatabaseCheckBox.isSelected());
        prefs.setApplyIpsPatches(applyIpsPatchesCheckBox.isSelected());
        prefs.setHideMenuBar(hideMenuBarCheckBox.isSelected());
        prefs.setEnterFullscreen(enterFullscreenCheckBox.isSelected());
        prefs.setPauseMenu(pauseMenuCheckBox.isSelected());
        prefs.setConfirmReset(confirmResetCheckBox.isSelected());
        prefs.setConfirmExit(confirmExitCheckBox.isSelected());
        prefs.setConfirmHotSwap(confirmHotSwapCheckBox.isSelected());
        prefs.setDisableScreensaver(disableScreensaverCheckBox.isSelected());
        prefs.setAllowMultipleInstances(allowMultipleInstancesCheckBox
                .isSelected());
        prefs.setRunInBackground(runInBackgroundCheckBox.isSelected());
        prefs.setAcceptBackgroundInput(acceptBackgroundInputCheckBox.isSelected());
        prefs.setUseVsync(useVsyncCheckBox.isSelected());
        prefs.setUseMulticoreFiltering(useMulticoreFilteringCheckBox.isSelected());
        prefs.setMaxLagFrames((int) maxLagFramesSpinner.getValue());
        prefs.setInterframeDelay((InterframeDelay) interframeDelayComboBox
                .getSelectedItem());
        prefs.setInitialRamState((InitialRamState) initialRamStateComboBox
                .getSelectedItem());
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

        launchFileOpenCheckBox = new javax.swing.JCheckBox();
        referenceDatabaseCheckBox = new javax.swing.JCheckBox();
        hideMenuBarCheckBox = new javax.swing.JCheckBox();
        enterFullscreenCheckBox = new javax.swing.JCheckBox();
        pauseMenuCheckBox = new javax.swing.JCheckBox();
        confirmResetCheckBox = new javax.swing.JCheckBox();
        confirmExitCheckBox = new javax.swing.JCheckBox();
        disableScreensaverCheckBox = new javax.swing.JCheckBox();
        allowMultipleInstancesCheckBox = new javax.swing.JCheckBox();
        runInBackgroundCheckBox = new javax.swing.JCheckBox();
        acceptBackgroundInputCheckBox = new javax.swing.JCheckBox();
        useVsyncCheckBox = new javax.swing.JCheckBox();
        useMulticoreFilteringCheckBox = new javax.swing.JCheckBox();
        interframeDelayLabel = new javax.swing.JLabel();
        interframeDelayComboBox = new JComboBox(InterframeDelay.values());
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        defaultsButton = new javax.swing.JButton();
        initialRamStateLabel = new javax.swing.JLabel();
        initialRamStateComboBox = new JComboBox(InitialRamState.values());
        confirmHotSwapCheckBox = new javax.swing.JCheckBox();
        applyIpsPatchesCheckBox = new javax.swing.JCheckBox();
        maxLagFramesLabel = new javax.swing.JLabel();
        maxLagFramesSpinner = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("User Interface Options");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        launchFileOpenCheckBox.setText("Launch File | Open... on startup");
        launchFileOpenCheckBox.setFocusPainted(false);
        launchFileOpenCheckBox.setPreferredSize(null);

        referenceDatabaseCheckBox.setText("Soft patch file header on load");
        referenceDatabaseCheckBox.setFocusPainted(false);

        hideMenuBarCheckBox.setText("Hide menu bar on load");
        hideMenuBarCheckBox.setFocusPainted(false);
        hideMenuBarCheckBox.setPreferredSize(null);

        enterFullscreenCheckBox.setText("Enter fullscreen mode on load");
        enterFullscreenCheckBox.setFocusPainted(false);
        enterFullscreenCheckBox.setPreferredSize(null);

        pauseMenuCheckBox.setText("Pause during menu access");
        pauseMenuCheckBox.setFocusPainted(false);
        pauseMenuCheckBox.setPreferredSize(null);

        confirmResetCheckBox.setText("Confirm reset / power cycle");
        confirmResetCheckBox.setFocusPainted(false);
        confirmResetCheckBox.setPreferredSize(null);

        confirmExitCheckBox.setText("Confirm exit");
        confirmExitCheckBox.setFocusPainted(false);
        confirmExitCheckBox.setPreferredSize(null);

        disableScreensaverCheckBox.setText("Disable screensaver");
        disableScreensaverCheckBox.setFocusPainted(false);
        disableScreensaverCheckBox.setMaximumSize(null);
        disableScreensaverCheckBox.setMinimumSize(null);
        disableScreensaverCheckBox.setPreferredSize(null);

        allowMultipleInstancesCheckBox.setText("Allow multiple application instances");
        allowMultipleInstancesCheckBox.setFocusPainted(false);
        allowMultipleInstancesCheckBox.setPreferredSize(null);

        runInBackgroundCheckBox.setText("Run in background");
        runInBackgroundCheckBox.setFocusPainted(false);
        runInBackgroundCheckBox.setPreferredSize(null);

        acceptBackgroundInputCheckBox.setText("Accept background input");
        acceptBackgroundInputCheckBox.setFocusPainted(false);
        acceptBackgroundInputCheckBox.setPreferredSize(null);

        useVsyncCheckBox.setText("Use VSync in window mode");
        useVsyncCheckBox.setFocusPainted(false);
        useVsyncCheckBox.setPreferredSize(null);

        useMulticoreFilteringCheckBox.setText("Use multiple cores for video filtering");
        useMulticoreFilteringCheckBox.setFocusPainted(false);
        useMulticoreFilteringCheckBox.setPreferredSize(null);

        interframeDelayLabel.setText("Inter-frame delay:");
        interframeDelayLabel.setPreferredSize(null);

        interframeDelayComboBox.setFocusable(false);
        interframeDelayComboBox.setPreferredSize(null);

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

        defaultsButton.setMnemonic('D');
        defaultsButton.setText("Defaults");
        defaultsButton.setFocusPainted(false);
        defaultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultsButtonActionPerformed(evt);
            }
        });

        initialRamStateLabel.setText("Initial RAM state:");
        initialRamStateLabel.setPreferredSize(null);

        initialRamStateComboBox.setFocusable(false);
        initialRamStateComboBox.setPreferredSize(null);

        confirmHotSwapCheckBox.setText("Confirm hot swap");
        confirmHotSwapCheckBox.setFocusPainted(false);
        confirmHotSwapCheckBox.setMaximumSize(null);
        confirmHotSwapCheckBox.setMinimumSize(null);
        confirmHotSwapCheckBox.setPreferredSize(null);

        applyIpsPatchesCheckBox.setText("Apply IPS patches on load");

        maxLagFramesLabel.setText("Max lag frames:");
        maxLagFramesLabel.setMaximumSize(null);
        maxLagFramesLabel.setPreferredSize(null);

        maxLagFramesSpinner.setMaximumSize(null);
        maxLagFramesSpinner.setMinimumSize(null);
        maxLagFramesSpinner.setPreferredSize(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 13, Short.MAX_VALUE)
                                                .addComponent(defaultsButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(okButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(interframeDelayLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(initialRamStateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(maxLagFramesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(interframeDelayComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(maxLagFramesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(initialRamStateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                        .addComponent(confirmHotSwapCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(confirmExitCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(disableScreensaverCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(launchFileOpenCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(referenceDatabaseCheckBox)
                                                        .addComponent(hideMenuBarCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(enterFullscreenCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(pauseMenuCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(confirmResetCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(allowMultipleInstancesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(runInBackgroundCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(acceptBackgroundInputCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(useVsyncCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(useMulticoreFilteringCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(applyIpsPatchesCheckBox))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, defaultsButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(launchFileOpenCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(referenceDatabaseCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(applyIpsPatchesCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(hideMenuBarCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(enterFullscreenCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pauseMenuCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(confirmResetCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(confirmExitCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(confirmHotSwapCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(disableScreensaverCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(allowMultipleInstancesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(runInBackgroundCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(acceptBackgroundInputCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(useVsyncCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(useMulticoreFilteringCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(maxLagFramesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(maxLagFramesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(interframeDelayLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(interframeDelayComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(initialRamStateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(initialRamStateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton)
                                        .addComponent(okButton)
                                        .addComponent(defaultsButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void defaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultsButtonActionPerformed
        loadFields(new UserInterfacePrefs());
    }//GEN-LAST:event_defaultsButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        ok = true;
        captureFields();
        AppPrefs.save();
        closeDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing
    // End of variables declaration//GEN-END:variables
}
