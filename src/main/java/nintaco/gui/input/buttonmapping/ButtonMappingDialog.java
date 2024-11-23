package nintaco.gui.input.buttonmapping;

import nintaco.gui.WideButtonsDialog;
import nintaco.input.*;
import nintaco.input.uforce.UForceDescriptor;
import nintaco.preferences.AppPrefs;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static nintaco.input.ConsoleType.NES;
import static nintaco.input.ConsoleType.VsDualSystem;
import static nintaco.input.InputDevices.DevicesCount;
import static nintaco.input.InputDevices.UForce;
import static nintaco.input.Ports.*;
import static nintaco.util.GuiUtil.*;

public class ButtonMappingDialog extends javax.swing.JDialog {

    private final List<DeviceConfig> deviceConfigs;

    private boolean okPressed;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton clearAllButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JComboBox deviceComboBox;
    private javax.swing.JButton okButton;
    private javax.swing.JButton resetAllButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton setAllButton;
    private javax.swing.JButton setButton;
    private javax.swing.JTable table;

    public ButtonMappingDialog(final Window parent) {
        super(parent);
        setModal(true);
        initComponents();
        deviceConfigs = AppPrefs.getInstance().getInputs().copyDeviceConfigs();
        initDeviceComboBox();
        initTable();
        selectPlayer1Device();
        scaleFonts(this);
        pack();
        setLocationRelativeTo(parent);
    }

    public void setInputDevice(final int inputDevice) {
        deviceComboBox.setSelectedIndex(inputDevice);
    }

    private void initDeviceComboBox() {
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (int i = 0; i < DevicesCount; i++) {
            model.addElement(DeviceDescriptor.getDescriptor(i).getDeviceName());
        }
        deviceComboBox.setModel(model);
    }

    private void initTable() {
        scrollPane.setPreferredSize(null);
        disableCellBorder(table);
        forceNoClearRowSelect(table);
    }

    private void selectPlayer1Device() {
        final Ports ports = AppPrefs.getInstance().getInputs().getPorts();
        if (ports.isMultitap()) {
            setInputDevice(ports.getDevice(Tap1));
        } else if (ports.getConsoleType() == VsDualSystem) {
            setInputDevice(ports.getDevice(Main1));
        } else if (ports.getConsoleType() == NES) {
            setInputDevice(ports.getDevice(Port1));
        } else {
            final Integer device = ports.getDevice(ExpansionPort);
            if (device == null || device < 0) {
                setInputDevice(ports.getDevice(Port1));
            } else {
                setInputDevice(device);
            }
        }
    }

    private void displayDevice(final int inputDevice) {
        table.setModel(new ButtonMappingTableModel(deviceConfigs.get(inputDevice)
                .getButtonMappings()));
        resizeCellSizes(table, true, 13, false, DeviceDescriptor
                .getDescriptor(UForce).getButtonName(
                        UForceDescriptor.BottomFieldLowerRight), "MMMMMMMMMMMMMMMM");
    }

    private void closeDialog() {
        dispose();
        if (okPressed) {
            AppPrefs.getInstance().getInputs().setDeviceConfigs(deviceConfigs);
            AppPrefs.save();
            InputUtil.handleSettingsChange();
        }
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    private int captureRewindTimeSetting() {

        final WideButtonsDialog dialog = new WideButtonsDialog(this,
                "How should the button behave?", "Rewind Time Setup",
                "Tapping the button toggles the direction of time.",
                "Time rewinds as long as the button is held down.");
        dialog.setVisible(true);
        return dialog.getButtonIndex();
    }

    private int captureHighSpeedSetting() {

        final WideButtonsDialog dialog = new WideButtonsDialog(this,
                "How should the button behave?", "High-speed Setup",
                "Tapping the button toggles between normal and high speed.",
                "Maintain high-speed as long as the button is held down.");
        dialog.setVisible(true);
        return dialog.getButtonIndex();
    }

    private boolean capturePressedButton(final boolean showSkipButton) {
        final int inputDevice = deviceComboBox.getSelectedIndex();
        if (inputDevice < 0) {
            return false;
        }
        final DeviceDescriptor descriptor = DeviceDescriptor
                .getDescriptor(inputDevice);
        final int button = table.getSelectedRow();
        final List<ButtonMapping> list = deviceConfigs.get(inputDevice)
                .getButtonMappings();
        final ButtonMapping mapping = list.get(button);

        final PressButtonDialog dialog = new PressButtonDialog(this,
                showSkipButton);
        dialog.setButtonName(mapping.getButtonName());
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        final ButtonID[] buttonIds = dialog.getButtonIds();
        if (dialog.isSkipped() || buttonIds.length > 0) {
            if (!dialog.isSkipped()) {
                final ButtonMapping buttonMapping = descriptor.getButtonMapping(button,
                        buttonIds);
                if (button == descriptor.getRewindTimeButton()) {
                    final int buttonIndex = captureRewindTimeSetting();
                    if (buttonIndex < 0) {
                        return false;
                    }
                    ((HoldDownOrToggleButtonMapping) buttonMapping).setHoldDown(
                            buttonIndex == 1);
                } else if (button == descriptor.getHighSpeedButton()) {
                    final int buttonIndex = captureHighSpeedSetting();
                    if (buttonIndex < 0) {
                        return false;
                    }
                    ((HoldDownOrToggleButtonMapping) buttonMapping).setHoldDown(
                            buttonIndex == 1);
                }

                deviceConfigs.get(inputDevice).getButtonMappings()
                        .set(button, buttonMapping);
            }

            displayDevice(inputDevice);
            final int nextButton = button + 1;
            if (nextButton == list.size()) {
                return false;
            }
            table.getSelectionModel().setSelectionInterval(nextButton, nextButton);
            return true;
        } else {
            return !dialog.isCanceled();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        deviceComboBox = new javax.swing.JComboBox();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        setButton = new javax.swing.JButton();
        setAllButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        resetAllButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        clearAllButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Button Mapping");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        deviceComboBox.setFocusable(false);
        deviceComboBox.setMinimumSize(null);
        deviceComboBox.setPreferredSize(null);
        deviceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deviceComboBoxActionPerformed(evt);
            }
        });

        cancelButton.setMnemonic('C');
        cancelButton.setText("   Cancel   ");
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

        scrollPane.setMaximumSize(null);
        scrollPane.setName(""); // NOI18N

        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setMaximumSize(null);
        table.setMinimumSize(null);
        table.setPreferredSize(null);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(table);

        setButton.setMnemonic('S');
        setButton.setText("Set");
        setButton.setFocusPainted(false);
        setButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setButtonActionPerformed(evt);
            }
        });

        setAllButton.setMnemonic('A');
        setAllButton.setText("Set All");
        setAllButton.setFocusPainted(false);
        setAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setAllButtonActionPerformed(evt);
            }
        });

        resetButton.setMnemonic('R');
        resetButton.setText("Reset");
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        resetAllButton.setMnemonic('t');
        resetAllButton.setText("Reset All");
        resetAllButton.setFocusPainted(false);
        resetAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAllButtonActionPerformed(evt);
            }
        });

        clearButton.setMnemonic('l');
        clearButton.setText("Clear");
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        clearAllButton.setMnemonic('e');
        clearAllButton.setText("Clear All");
        clearAllButton.setFocusPainted(false);
        clearAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAllButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(setButton)
                                        .addComponent(setAllButton)
                                        .addComponent(resetButton)
                                        .addComponent(resetAllButton)
                                        .addComponent(clearButton)
                                        .addComponent(clearAllButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(okButton)
                                                .addGap(8, 8, 8)
                                                .addComponent(cancelButton))
                                        .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(deviceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton);

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, clearAllButton, clearButton, resetAllButton, resetButton, setAllButton, setButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(deviceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(setButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(setAllButton)
                                                .addGap(18, 18, 18)
                                                .addComponent(resetButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(resetAllButton)
                                                .addGap(18, 18, 18)
                                                .addComponent(clearButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(clearAllButton)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(okButton)
                                        .addComponent(cancelButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void setButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setButtonActionPerformed
        capturePressedButton(false);
    }//GEN-LAST:event_setButtonActionPerformed

    private void setAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setAllButtonActionPerformed
        table.getSelectionModel().setSelectionInterval(0, 0);
        while (capturePressedButton(true)) ;
    }//GEN-LAST:event_setAllButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        final int device = deviceComboBox.getSelectedIndex();
        final int button = table.getSelectedRow();
        Inputs.resetDeviceConfig(device, button, deviceConfigs);
        displayDevice(device);
    }//GEN-LAST:event_resetButtonActionPerformed

    private void resetAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAllButtonActionPerformed
        final int device = deviceComboBox.getSelectedIndex();
        Inputs.resetDeviceConfigs(device, deviceConfigs);
        displayDevice(device);
    }//GEN-LAST:event_resetAllButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        final int device = deviceComboBox.getSelectedIndex();
        final int button = table.getSelectedRow();
        Inputs.clearButtonMapping(device, button, deviceConfigs);
        displayDevice(device);
    }//GEN-LAST:event_clearButtonActionPerformed

    private void clearAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAllButtonActionPerformed
        final int device = deviceComboBox.getSelectedIndex();
        Inputs.clearDeviceConfig(device, deviceConfigs);
        displayDevice(device);
    }//GEN-LAST:event_clearAllButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        okPressed = true;
        AppPrefs.getInstance().getInputs().setDeviceConfigs(deviceConfigs);
        AppPrefs.save();
        closeDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void deviceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deviceComboBoxActionPerformed
        displayDevice(deviceComboBox.getSelectedIndex());
    }//GEN-LAST:event_deviceComboBoxActionPerformed
    // End of variables declaration//GEN-END:variables
}
