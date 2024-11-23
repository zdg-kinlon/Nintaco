package nintaco.gui.dipswitches;

import nintaco.App;
import nintaco.input.dipswitches.DipSwitch;
import nintaco.input.dipswitches.DipSwitchValue;
import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;

import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

import static nintaco.util.GuiUtil.scaleFonts;
import static nintaco.util.MathUtil.clamp;

public class DipSwitchesDialog extends javax.swing.JDialog {

    private final List<DipSwitch> dipSwitches;
    private JLabel[] dipSwitchLabels;
    private JComboBox[] dipSwitchComboBoxes;
    private boolean ok;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton defaultsButton;
    private final ActionListener comboBoxesListener = e -> selectionChanged();
    private javax.swing.JPanel dipSwichesPanel;
    private javax.swing.JCheckBox loadCheckBox;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox resetCheckBox;

    public DipSwitchesDialog(final Window parent,
                             final List<DipSwitch> dipSwitches) {

        super(parent);
        this.dipSwitches = dipSwitches;

        setModal(true);
        initComponents();
        initDipSwitchComponents();
        loadFields();
        scaleFonts(this);
        boldAllLabels();
        getRootPane().setDefaultButton(okButton);
        pack();
        setLocationRelativeTo(parent);
        selectionChanged();
    }

    private void initDipSwitchComponents() {

        dipSwitchLabels = new JLabel[dipSwitches.size()];
        dipSwitchComboBoxes = new JComboBox[dipSwitches.size()];

        final GroupLayout dipSwichesPanelLayout = new GroupLayout(dipSwichesPanel);
        dipSwichesPanel.setLayout(dipSwichesPanelLayout);
        final ParallelGroup parallelGroup1 = dipSwichesPanelLayout
                .createParallelGroup(GroupLayout.Alignment.LEADING);
        final ParallelGroup parallelGroup2 = dipSwichesPanelLayout
                .createParallelGroup(GroupLayout.Alignment.LEADING);
        final SequentialGroup sequentialGroup = dipSwichesPanelLayout
                .createSequentialGroup().addContainerGap();

        for (int i = 0; i < dipSwitchLabels.length; i++) {

            final DipSwitch dipSwitch = dipSwitches.get(i);
            final JLabel label = dipSwitchLabels[i]
                    = new JLabel(dipSwitch.getName() + ":");

            final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            for (final DipSwitchValue value : dipSwitch.getValues()) {
                model.addElement(value.getName());
            }
            final JComboBox comboBox = dipSwitchComboBoxes[i] = new JComboBox(model);
            comboBox.addActionListener(comboBoxesListener);
            comboBox.setFocusable(false);

            parallelGroup1.addComponent(label);
            parallelGroup2.addComponent(comboBox);
            sequentialGroup.addGroup(dipSwichesPanelLayout.createParallelGroup(
                            GroupLayout.Alignment.BASELINE)
                    .addComponent(label)
                    .addComponent(comboBox));
            if (i == dipSwitchLabels.length - 1) {
                sequentialGroup.addContainerGap();
            } else {
                sequentialGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
            }
        }

        dipSwichesPanelLayout.setHorizontalGroup(
                dipSwichesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(dipSwichesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(parallelGroup1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(parallelGroup2)
                                .addContainerGap()));
        dipSwichesPanelLayout.setVerticalGroup(dipSwichesPanelLayout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(sequentialGroup));
    }

    private void loadFields() {
        final int[] dipSwitchValues = GamePrefs.getInstance()
                .getDipSwitchesGamePrefs().getDipSwitchValues();
        for (int i = dipSwitchComboBoxes.length - 1; i >= 0; i--) {
            final JComboBox comboBox = dipSwitchComboBoxes[i];
            comboBox.setSelectedIndex(clamp((i < dipSwitchValues.length)
                            ? dipSwitchValues[i] : dipSwitches.get(i).getDefaultValue(), 0,
                    comboBox.getItemCount()));
        }
        final DipSwitchesAppPrefs appPrefs = AppPrefs.getInstance()
                .getDipSwitchesAppPrefs();
        loadCheckBox.setSelected(appPrefs.isDisplayDialogOnLoad());
        resetCheckBox.setSelected(appPrefs.isResetMachine());
    }

    private void saveFields() {
        final int[] dipSwitchValues = new int[dipSwitchComboBoxes.length];
        for (int i = dipSwitchComboBoxes.length - 1; i >= 0; i--) {
            dipSwitchValues[i] = dipSwitchComboBoxes[i].getSelectedIndex();
        }
        GamePrefs.getInstance().getDipSwitchesGamePrefs().setDipSwitchValues(
                dipSwitchValues);
        GamePrefs.save();
        final DipSwitchesAppPrefs appPrefs = AppPrefs.getInstance()
                .getDipSwitchesAppPrefs();
        appPrefs.setDisplayDialogOnLoad(loadCheckBox.isSelected());
        appPrefs.setResetMachine(resetCheckBox.isSelected());
        AppPrefs.save();
    }

    private void closeDialog() {
        dispose();
    }

    private void boldAllLabels() {
        if (dipSwitchComboBoxes.length == 0) {
            return;
        }
        final Font boldFont = dipSwitchLabels[0].getFont().deriveFont(Font.BOLD);
        for (int i = dipSwitchLabels.length - 1; i >= 0; i--) {
            dipSwitchLabels[i].setFont(boldFont);
        }
    }

    private void selectionChanged() {
        if (dipSwitchComboBoxes.length == 0) {
            return;
        }
        boolean resetButtonEnabled = false;
        final Font plainFont = dipSwitchLabels[0].getFont().deriveFont(Font.PLAIN);
        final Font boldFont = dipSwitchLabels[0].getFont().deriveFont(Font.BOLD);
        for (int i = dipSwitchComboBoxes.length - 1; i >= 0; i--) {
            final boolean notDefault = dipSwitchComboBoxes[i].getSelectedIndex()
                    != dipSwitches.get(i).getDefaultValue();
            resetButtonEnabled |= notDefault;
            dipSwitchLabels[i].setFont(notDefault ? boldFont : plainFont);
        }
        defaultsButton.setEnabled(resetButtonEnabled);
    }

    public boolean isOK() {
        return ok;
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
        dipSwichesPanel = new javax.swing.JPanel();
        loadCheckBox = new javax.swing.JCheckBox();
        resetCheckBox = new javax.swing.JCheckBox();
        defaultsButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("DIP Switches");
        setMaximumSize(null);
        setMinimumSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cancelButton.setMnemonic('C');
        cancelButton.setText("Cancel");
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

        dipSwichesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("DIP Switches"));
        dipSwichesPanel.setMaximumSize(null);
        dipSwichesPanel.setName(""); // NOI18N

        javax.swing.GroupLayout dipSwichesPanelLayout = new javax.swing.GroupLayout(dipSwichesPanel);
        dipSwichesPanel.setLayout(dipSwichesPanelLayout);
        dipSwichesPanelLayout.setHorizontalGroup(
                dipSwichesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        dipSwichesPanelLayout.setVerticalGroup(
                dipSwichesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        loadCheckBox.setSelected(true);
        loadCheckBox.setText("Display this dialog on file load");
        loadCheckBox.setFocusPainted(false);
        loadCheckBox.setFocusable(false);

        resetCheckBox.setSelected(true);
        resetCheckBox.setText("Reset machine after pressing OK");
        resetCheckBox.setFocusPainted(false);
        resetCheckBox.setFocusable(false);

        defaultsButton.setMnemonic('D');
        defaultsButton.setText("Defaults");
        defaultsButton.setFocusPainted(false);
        defaultsButton.setFocusable(false);
        defaultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(defaultsButton)
                                                .addGap(18, 18, Short.MAX_VALUE)
                                                .addComponent(okButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton))
                                        .addComponent(dipSwichesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(resetCheckBox)
                                                        .addComponent(loadCheckBox))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, defaultsButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(dipSwichesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(loadCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(resetCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton)
                                        .addComponent(okButton)
                                        .addComponent(defaultsButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        saveFields();
        App.handleDipSwitchChange(dipSwitches);
        ok = true;
        closeDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void defaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultsButtonActionPerformed
        if (dipSwitchComboBoxes.length == 0) {
            return;
        }
        final Font plainFont = dipSwitchLabels[0].getFont().deriveFont(Font.PLAIN);
        for (int i = dipSwitchComboBoxes.length - 1; i >= 0; i--) {
            final JComboBox comboBox = dipSwitchComboBoxes[i];
            comboBox.removeActionListener(comboBoxesListener);
            comboBox.setSelectedIndex(dipSwitches.get(i).getDefaultValue());
            comboBox.addActionListener(comboBoxesListener);
            dipSwitchLabels[i].setFont(plainFont);
        }
        defaultsButton.setEnabled(false);
    }//GEN-LAST:event_defaultsButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
