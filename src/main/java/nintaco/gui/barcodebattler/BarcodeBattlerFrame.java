package nintaco.gui.barcodebattler;

import nintaco.App;
import nintaco.input.InputUtil;
import nintaco.input.other.TransferBarcode;

import java.util.Random;

import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.removeWhitespaces;

public class BarcodeBattlerFrame extends javax.swing.JFrame {

    private final Random random = new Random();
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel digitsLabel;
    private javax.swing.JTextField digitsTextField;
    private javax.swing.JButton randomizeButton;
    private javax.swing.JButton transferButton;

    public BarcodeBattlerFrame() {
        initComponents();
        addTextFieldEditListener(digitsTextField, this::digitsTextFieldEdited);
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
    }

    private void closeFrame() {
        App.destroyBarcodeBattlerFrame();
    }

    public void destroy() {
        dispose();
    }

    private void digitsTextFieldEdited() {
        final String value = removeWhitespaces(digitsTextField.getText());
        if (value.length() != 8 && value.length() != 13) {
            transferButton.setEnabled(false);
            return;
        }
        int sum = 0;
        boolean three = false;
        for (int i = value.length() - 1; i >= 0; i--) {
            final char ch = value.charAt(i);
            if (ch < '0' || ch > '9') {
                transferButton.setEnabled(false);
                return;
            } else {
                final int digit = ch - '0';
                if (three) {
                    three = false;
                    sum += 3 * digit;
                } else {
                    three = true;
                    sum += digit;
                }
            }
        }
        transferButton.setEnabled(sum % 10 == 0);
    }

    private void transferBarcode(final String barcode) {
        if (App.getMachineRunner() != null) {
            InputUtil.addOtherInput(new TransferBarcode(barcode));
        } else {
            InputUtil.setBarcode(null);
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

        digitsLabel = new javax.swing.JLabel();
        digitsTextField = new javax.swing.JTextField();
        transferButton = new javax.swing.JButton();
        randomizeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Barcode Battler II");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        digitsLabel.setText("Digits");
        digitsLabel.setMaximumSize(null);
        digitsLabel.setMinimumSize(null);
        digitsLabel.setPreferredSize(null);

        digitsTextField.setColumns(32);
        digitsTextField.setText(" ");
        digitsTextField.setMaximumSize(null);
        digitsTextField.setMinimumSize(null);
        digitsTextField.setPreferredSize(null);

        transferButton.setMnemonic('T');
        transferButton.setText("Transfer");
        transferButton.setEnabled(false);
        transferButton.setFocusPainted(false);
        transferButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transferButtonActionPerformed(evt);
            }
        });

        randomizeButton.setMnemonic('R');
        randomizeButton.setText("Randomize");
        randomizeButton.setFocusPainted(false);
        randomizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                randomizeButtonActionPerformed(evt);
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
                                                .addComponent(digitsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(digitsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 66, Short.MAX_VALUE)
                                                .addComponent(randomizeButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(transferButton)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, randomizeButton, transferButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(digitsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(digitsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(transferButton)
                                        .addComponent(randomizeButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void randomizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_randomizeButtonActionPerformed
        final StringBuilder sb = new StringBuilder();
        int sum = 0;
        boolean three = random.nextBoolean();
        for (int i = three ? 6 : 11; i >= 0; i--) {
            final int digit = random.nextInt(10);
            sb.append((char) ('0' + digit));
            if (three) {
                three = false;
                sum += 3 * digit;
            } else {
                three = true;
                sum += digit;
            }
        }
        sb.append((char) ('0' + (10 - sum % 10) % 10));
        digitsTextField.setText(sb.toString());
    }//GEN-LAST:event_randomizeButtonActionPerformed

    private void transferButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transferButtonActionPerformed
        transferBarcode(removeWhitespaces(digitsTextField.getText()));
    }//GEN-LAST:event_transferButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing
    // End of variables declaration//GEN-END:variables
}
