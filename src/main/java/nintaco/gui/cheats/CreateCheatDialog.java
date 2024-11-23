package nintaco.gui.cheats;

import nintaco.cheats.Cheat;
import nintaco.cheats.GameGenie;
import nintaco.cheats.ProActionRocky;

import javax.swing.event.DocumentListener;
import java.awt.*;

import static nintaco.util.GuiUtil.createDocumentListener;
import static nintaco.util.GuiUtil.scaleFonts;
import static nintaco.util.StringUtil.ParseErrors.EMPTY;
import static nintaco.util.StringUtil.parseInt;
import static nintaco.util.StringUtil.replaceBlank;

public class CreateCheatDialog extends javax.swing.JDialog {

    private static final String[] CARDS
            = {"gameGenieCard", "proActionRockyCard", "rawCard"};
    private String lastGameGenieCode;    private final DocumentListener gameGenieDocListener = createDocumentListener(
            this::updateGameGenieOkButton);
    private String lastProActionRockyCode;    private final DocumentListener proActionRockyDocListener
            = createDocumentListener(this::updateProActionRockyOkButton);
    private Cheat result;    private final DocumentListener rawDocListener = createDocumentListener(
            this::updateRawOkButton);
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextField addressTextField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel cardPanel;
    private javax.swing.JLabel compareLabel;
    private javax.swing.JTextField compareTextField;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField descriptionTextField;
    private javax.swing.JCheckBox enabledCheckBox;
    private javax.swing.JLabel gameGenieCodeLabel;
    private javax.swing.JPanel gameGeniePanel;
    private javax.swing.JTextField gameGenieTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel proActionRockCodeLabel;
    private javax.swing.JPanel proActionRockyPanel;
    private javax.swing.JTextField proActionRockyTextField;
    private javax.swing.JPanel rawPanel;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JPanel typePanel;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JTextField valueTextField;

    public CreateCheatDialog(Window parent) {
        super(parent);
        setModal(true);
        initComponents();
        addDocumentListeners();
        scaleFonts(this);
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, CARDS[0]);
        pack();
        setLocationRelativeTo(parent);
    }

    public Cheat getCheat() {
        return result;
    }

    public void setCheat(final Cheat cheat) {
        setCheat(cheat, CheatCards.GameGenie);
    }

    private void addDocumentListeners() {
        gameGenieTextField.getDocument().addDocumentListener(gameGenieDocListener);
        proActionRockyTextField.getDocument().addDocumentListener(
                proActionRockyDocListener);
        addressTextField.getDocument().addDocumentListener(rawDocListener);
        valueTextField.getDocument().addDocumentListener(rawDocListener);
        compareTextField.getDocument().addDocumentListener(rawDocListener);
    }

    private void removeDocumentListeners() {
        gameGenieTextField.getDocument().removeDocumentListener(
                gameGenieDocListener);
        proActionRockyTextField.getDocument().removeDocumentListener(
                proActionRockyDocListener);
        addressTextField.getDocument().removeDocumentListener(rawDocListener);
        valueTextField.getDocument().removeDocumentListener(rawDocListener);
        compareTextField.getDocument().removeDocumentListener(rawDocListener);
    }

    public void setCheat(final Cheat cheat, final int cheatCard) {
        if (cheat == null) {
            descriptionTextField.setText("");
            enabledCheckBox.setSelected(true);
        } else {
            descriptionTextField.setText(cheat.getDescription());
            enabledCheckBox.setSelected(cheat.isEnabled());
        }
        updateGameGenieTextField(cheat);
        updateProActionRockyTextField(cheat);
        updateRawTextFields(cheat);
        updateOkButton();
        typeComboBox.setSelectedIndex(cheatCard);
        switch (cheatCard) {
            case CheatCards.GameGenie:
                gameGenieTextField.requestFocus();
                gameGenieTextField.selectAll();
                break;
            case CheatCards.ProActionRocky:
                proActionRockyTextField.requestFocus();
                proActionRockyTextField.selectAll();
                break;
            case CheatCards.Raw:
                valueTextField.requestFocus();
                valueTextField.selectAll();
                break;
        }
    }

    private void updateGameGenieTextField(final Cheat cheat) {
        removeDocumentListeners();
        gameGenieTextField.setText(replaceBlank(GameGenie.convert(cheat), ""));
        lastGameGenieCode = gameGenieTextField.getText();
        addDocumentListeners();
    }

    private void updateProActionRockyTextField(Cheat cheat) {
        removeDocumentListeners();
        proActionRockyTextField.setText(replaceBlank(ProActionRocky.convert(cheat),
                ""));
        lastProActionRockyCode = proActionRockyTextField.getText();
        addDocumentListeners();
    }

    private void updateRawTextFields(Cheat cheat) {
        removeDocumentListeners();
        if (cheat == null) {
            addressTextField.setText("");
            valueTextField.setText("");
            compareTextField.setText("");
        } else {
            addressTextField.setText(String.format("$%04X", cheat.getAddress()));
            valueTextField.setText(Integer.toString(cheat.getDataValue()));
            compareTextField.setText(cheat.hasCompareValue()
                    ? Integer.toString(cheat.getCompareValue()) : "");
        }
        addDocumentListeners();
    }

    private void closeDialog() {
        if (result != null) {
            result.setEnabled(enabledCheckBox.isSelected());
            result.setDescription(descriptionTextField.getText().trim());
        }
        dispose();
    }

    private void updateOkButton() {
        switch (typeComboBox.getSelectedIndex()) {
            case CheatCards.GameGenie:
                updateGameGenieOkButton();
                break;
            case CheatCards.ProActionRocky:
                updateProActionRockyOkButton();
                break;
            case CheatCards.Raw:
                updateRawOkButton();
                break;
        }
    }

    private void updateGameGenieOkButton() {
        final String code = gameGenieTextField.getText().trim();
        final Cheat cheat = GameGenie.convert(code);
        okButton.setEnabled(cheat != null);
        if (cheat != null && !code.equals(lastGameGenieCode)) {
            lastGameGenieCode = code;
            updateProActionRockyTextField(cheat);
            updateRawTextFields(cheat);
        }
    }

    private void updateProActionRockyOkButton() {
        final String code = proActionRockyTextField.getText().trim();
        final Cheat cheat = ProActionRocky.convert(code);
        okButton.setEnabled(cheat != null);
        if (cheat != null && !code.equals(lastProActionRockyCode)) {
            lastProActionRockyCode = code;
            updateGameGenieTextField(cheat);
            updateRawTextFields(cheat);
        }
    }

    private void updateRawOkButton() {
        final int address = parseInt(addressTextField.getText(), true, 0xFFFF);
        final int value = parseInt(valueTextField.getText(), false, 0xFF);
        final int compare = parseInt(compareTextField.getText(), false, 0xFF);
        if (address >= 0 && value >= 0 && compare >= EMPTY) {
            okButton.setEnabled(true);
            final Cheat cheat = new Cheat(address, value, compare);
            updateGameGenieTextField(cheat);
            updateProActionRockyTextField(cheat);
        } else {
            okButton.setEnabled(false);
        }
    }

    private void okGameGenie() {
        final String code = gameGenieTextField.getText().trim();
        result = GameGenie.convert(code);
        if (descriptionTextField.getText().trim().isEmpty()) {
            descriptionTextField.setText(code);
        }
        closeDialog();
    }

    private void okProActionRocky() {
        final String code = proActionRockyTextField.getText().trim();
        result = ProActionRocky.convert(code);
        if (descriptionTextField.getText().trim().isEmpty()) {
            descriptionTextField.setText(code);
        }
        closeDialog();
    }

    private void okRaw() {
        result = new Cheat(parseInt(addressTextField.getText(), true, 0xFFFF),
                parseInt(valueTextField.getText(), false, 0xFF),
                parseInt(compareTextField.getText(), false, 0xFF));
        if (descriptionTextField.getText().trim().isEmpty()) {
            result.generateDescription();
            descriptionTextField.setText(result.getDescription());
        }
        closeDialog();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cardPanel = new javax.swing.JPanel();
        gameGeniePanel = new javax.swing.JPanel();
        gameGenieCodeLabel = new javax.swing.JLabel();
        gameGenieTextField = new javax.swing.JTextField();
        proActionRockyPanel = new javax.swing.JPanel();
        proActionRockCodeLabel = new javax.swing.JLabel();
        proActionRockyTextField = new javax.swing.JTextField();
        rawPanel = new javax.swing.JPanel();
        addressLabel = new javax.swing.JLabel();
        addressTextField = new javax.swing.JTextField();
        valueLabel = new javax.swing.JLabel();
        valueTextField = new javax.swing.JTextField();
        compareLabel = new javax.swing.JLabel();
        compareTextField = new javax.swing.JTextField();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        typePanel = new javax.swing.JPanel();
        typeComboBox = new javax.swing.JComboBox();
        typeLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTextField = new javax.swing.JTextField();
        enabledCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cardPanel.setLayout(new java.awt.CardLayout());

        gameGenieCodeLabel.setText("Code:");

        gameGenieTextField.setColumns(12);
        gameGenieTextField.setPreferredSize(null);

        javax.swing.GroupLayout gameGeniePanelLayout = new javax.swing.GroupLayout(gameGeniePanel);
        gameGeniePanel.setLayout(gameGeniePanelLayout);
        gameGeniePanelLayout.setHorizontalGroup(
                gameGeniePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(gameGeniePanelLayout.createSequentialGroup()
                                .addComponent(gameGenieCodeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(gameGenieTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
        );
        gameGeniePanelLayout.setVerticalGroup(
                gameGeniePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(gameGeniePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(gameGeniePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(gameGenieCodeLabel)
                                        .addComponent(gameGenieTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
        );

        cardPanel.add(gameGeniePanel, "gameGenieCard");

        proActionRockCodeLabel.setText("Code:");

        proActionRockyTextField.setColumns(12);
        proActionRockyTextField.setPreferredSize(null);

        javax.swing.GroupLayout proActionRockyPanelLayout = new javax.swing.GroupLayout(proActionRockyPanel);
        proActionRockyPanel.setLayout(proActionRockyPanelLayout);
        proActionRockyPanelLayout.setHorizontalGroup(
                proActionRockyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(proActionRockyPanelLayout.createSequentialGroup()
                                .addComponent(proActionRockCodeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(proActionRockyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
        );
        proActionRockyPanelLayout.setVerticalGroup(
                proActionRockyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(proActionRockyPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(proActionRockyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(proActionRockCodeLabel)
                                        .addComponent(proActionRockyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cardPanel.add(proActionRockyPanel, "proActionRockyCard");

        addressLabel.setText("Address:");

        addressTextField.setColumns(7);
        addressTextField.setPreferredSize(null);

        valueLabel.setText("Value:");

        valueTextField.setColumns(5);
        valueTextField.setPreferredSize(null);

        compareLabel.setText("Compare:");

        compareTextField.setColumns(5);
        compareTextField.setPreferredSize(null);

        javax.swing.GroupLayout rawPanelLayout = new javax.swing.GroupLayout(rawPanel);
        rawPanel.setLayout(rawPanelLayout);
        rawPanelLayout.setHorizontalGroup(
                rawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(rawPanelLayout.createSequentialGroup()
                                .addComponent(addressLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(valueLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(valueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(compareLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(compareTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
        );
        rawPanelLayout.setVerticalGroup(
                rawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(rawPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(rawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(addressLabel)
                                        .addComponent(addressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(valueLabel)
                                        .addComponent(valueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(compareLabel)
                                        .addComponent(compareTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cardPanel.add(rawPanel, "rawCard");

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

        typeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Game Genie", "Pro Action Rocky", "Raw"}));
        typeComboBox.setFocusable(false);
        typeComboBox.setMinimumSize(null);
        typeComboBox.setPreferredSize(null);
        typeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboBoxActionPerformed(evt);
            }
        });

        typeLabel.setText("Type:");

        descriptionLabel.setText("Description:");

        descriptionTextField.setColumns(40);

        javax.swing.GroupLayout typePanelLayout = new javax.swing.GroupLayout(typePanel);
        typePanel.setLayout(typePanelLayout);
        typePanelLayout.setHorizontalGroup(
                typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(typePanelLayout.createSequentialGroup()
                                .addComponent(descriptionLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(descriptionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGroup(typePanelLayout.createSequentialGroup()
                                .addComponent(typeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
        );
        typePanelLayout.setVerticalGroup(
                typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(typePanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(descriptionLabel)
                                        .addComponent(descriptionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(typeLabel)
                                        .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, 0))
        );

        enabledCheckBox.setText("Enabled");
        enabledCheckBox.setFocusPainted(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(cardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(enabledCheckBox)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(okButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton))
                                        .addComponent(typePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(typePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(enabledCheckBox)
                                        .addComponent(okButton)
                                        .addComponent(cancelButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void typeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboBoxActionPerformed
        ((CardLayout) cardPanel.getLayout()).show(cardPanel,
                CARDS[typeComboBox.getSelectedIndex()]);
        updateOkButton();
    }//GEN-LAST:event_typeComboBoxActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        switch (typeComboBox.getSelectedIndex()) {
            case CheatCards.GameGenie:
                okGameGenie();
                break;
            case CheatCards.ProActionRocky:
                okProActionRocky();
                break;
            case CheatCards.Raw:
                okRaw();
                break;
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing



    // End of variables declaration//GEN-END:variables

}
