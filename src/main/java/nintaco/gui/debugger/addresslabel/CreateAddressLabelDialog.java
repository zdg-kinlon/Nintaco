package nintaco.gui.debugger.addresslabel;

import nintaco.disassembler.AddressLabel;

import javax.swing.event.DocumentListener;
import java.awt.*;

import static nintaco.util.GuiUtil.createDocumentListener;
import static nintaco.util.GuiUtil.scaleFonts;
import static nintaco.util.StringUtil.isBlank;
import static nintaco.util.StringUtil.parseInt;

public class CreateAddressLabelDialog extends javax.swing.JDialog {

    private DocumentListener bankListener;
    private DocumentListener addressListener;
    private DocumentListener labelListener;
    private DocumentListener commentListener;
    private AddressLabel result;
    private boolean ok;

    private int bank = -1;
    private int address;
    private String label;
    private String comment;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextField addressTextField;
    private javax.swing.JLabel bankLabel;
    private javax.swing.JTextField bankTextField;
    private javax.swing.JCheckBox bookmarkCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox codeCheckBox;
    private javax.swing.JLabel commentLabel;
    private javax.swing.JTextArea commentTextArea;
    private javax.swing.JLabel labelLabel;
    private javax.swing.JTextField labelTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JScrollPane scrollPane;

    public CreateAddressLabelDialog(final Window parent) {
        super(parent);
        setModal(true);
        initComponents();
        createDocumentListeners();
        addDocumentListeners();
        updateOkButton();
        scaleFonts(this);
        commentTextArea.setFont(labelTextField.getFont());
        pack();
        setLocationRelativeTo(parent);
    }

    public void selectAddress() {
        addressTextField.requestFocus();
        addressTextField.selectAll();
    }

    public void selectLabel() {
        labelTextField.requestFocus();
        labelTextField.selectAll();
    }

    private void createDocumentListeners() {
        bankListener = createDocumentListener(this::bankEdited);
        addressListener = createDocumentListener(this::addressEdited);
        labelListener = createDocumentListener(this::labelEdited);
        commentListener = createDocumentListener(this::commentEdited);
    }

    private void addDocumentListeners() {
        bankTextField.getDocument().addDocumentListener(bankListener);
        addressTextField.getDocument().addDocumentListener(addressListener);
        labelTextField.getDocument().addDocumentListener(labelListener);
        commentTextArea.getDocument().addDocumentListener(commentListener);
    }

    private void removeDocumentListeners() {
        bankTextField.getDocument().removeDocumentListener(bankListener);
        addressTextField.getDocument().removeDocumentListener(addressListener);
        labelTextField.getDocument().removeDocumentListener(labelListener);
        commentTextArea.getDocument().removeDocumentListener(commentListener);
    }

    private void bankEdited() {
        bank = parseInt(bankTextField.getText(), true, 0xFF);
        if (bank < 0) {
            bank = -1;
        }
        updateOkButton();
    }

    private void addressEdited() {
        address = parseInt(addressTextField.getText(), true, 0xFFFF);
        updateOkButton();
    }

    private void labelEdited() {
        label = labelTextField.getText().trim();
        if (label.isEmpty()) {
            label = null;
        }
        updateOkButton();
    }

    private void commentEdited() {
        comment = commentTextArea.getText().trim();
        if (comment.isEmpty()) {
            comment = null;
        }
        updateOkButton();
    }

    private void updateOkButton() {
        final boolean labelBlank = isBlank(label);
        okButton.setEnabled(address >= 0 && !(labelBlank && isBlank(comment))
                && !(labelBlank && bookmarkCheckBox.isSelected()));
    }

    private void closeDialog() {
        dispose();
    }

    public boolean isOK() {
        return ok;
    }

    public AddressLabel getAddressLabel() {
        return result;
    }

    public void setAddressLabel(final AddressLabel a) {
        removeDocumentListeners();

        bank = a.getBank();
        address = a.getAddress();
        label = a.getLabel();
        comment = a.getComment();

        if (bank >= 0x00 && bank <= 0xFF) {
            bankTextField.setText(String.format("$%02X", bank));
        } else {
            bankTextField.setText("");
            bank = -1;
        }
        if (address >= 0x0000 && address <= 0xFFFF) {
            addressTextField.setText(String.format("$%04X", address));
        } else {
            addressTextField.setText("");
            address = -1;
        }
        if (!isBlank(label)) {
            label = label.trim();
            labelTextField.setText(label);
        } else {
            label = null;
            labelTextField.setText("");
        }
        if (!isBlank(comment)) {
            comment = comment.trim();
            commentTextArea.setText(comment);
        } else {
            comment = null;
            commentTextArea.setText("");
        }
        codeCheckBox.setSelected(a.isCode());
        bookmarkCheckBox.setSelected(a.isBookmark());

        updateOkButton();
        addDocumentListeners();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addressLabel = new javax.swing.JLabel();
        addressTextField = new javax.swing.JTextField();
        labelLabel = new javax.swing.JLabel();
        labelTextField = new javax.swing.JTextField();
        commentLabel = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        codeCheckBox = new javax.swing.JCheckBox();
        scrollPane = new javax.swing.JScrollPane();
        commentTextArea = new javax.swing.JTextArea();
        bankLabel = new javax.swing.JLabel();
        bankTextField = new javax.swing.JTextField();
        bookmarkCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Address Label");
        setMaximumSize(null);
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        addressLabel.setText("Address:");
        addressLabel.setMaximumSize(null);
        addressLabel.setMinimumSize(null);
        addressLabel.setPreferredSize(null);

        addressTextField.setColumns(6);
        addressTextField.setText("$0000");
        addressTextField.setMaximumSize(null);
        addressTextField.setMinimumSize(null);
        addressTextField.setPreferredSize(null);

        labelLabel.setText("Label:");
        labelLabel.setMaximumSize(null);
        labelLabel.setMinimumSize(null);
        labelLabel.setPreferredSize(null);

        labelTextField.setColumns(15);
        labelTextField.setMaximumSize(null);
        labelTextField.setMinimumSize(null);

        commentLabel.setText("Comment:");
        commentLabel.setMaximumSize(null);
        commentLabel.setMinimumSize(null);
        commentLabel.setPreferredSize(null);

        cancelButton.setMnemonic('C');
        cancelButton.setText("  Cancel  ");
        cancelButton.setMaximumSize(null);
        cancelButton.setMinimumSize(null);
        cancelButton.setPreferredSize(null);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setMnemonic('O');
        okButton.setText("OK");
        okButton.setMaximumSize(null);
        okButton.setMinimumSize(null);
        okButton.setPreferredSize(null);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        codeCheckBox.setSelected(true);
        codeCheckBox.setText("Code");
        codeCheckBox.setMaximumSize(null);
        codeCheckBox.setMinimumSize(null);
        codeCheckBox.setPreferredSize(null);

        scrollPane.setMaximumSize(null);
        scrollPane.setMinimumSize(null);

        commentTextArea.setColumns(15);
        commentTextArea.setRows(5);
        commentTextArea.setMaximumSize(null);
        commentTextArea.setMinimumSize(null);
        scrollPane.setViewportView(commentTextArea);

        bankLabel.setText("Bank:");
        bankLabel.setMaximumSize(null);
        bankLabel.setMinimumSize(null);
        bankLabel.setPreferredSize(null);

        bankTextField.setColumns(4);
        bankTextField.setMaximumSize(null);
        bankTextField.setMinimumSize(null);
        bankTextField.setPreferredSize(null);

        bookmarkCheckBox.setSelected(true);
        bookmarkCheckBox.setText("Bookmark");
        bookmarkCheckBox.setMaximumSize(null);
        bookmarkCheckBox.setMinimumSize(null);
        bookmarkCheckBox.setPreferredSize(null);
        bookmarkCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookmarkCheckBoxActionPerformed(evt);
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
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(bankLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(labelLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(labelTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(bankTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(addressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(addressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(codeCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(bookmarkCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 129, Short.MAX_VALUE)))
                                                .addContainerGap())
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(10, 10, 10))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addContainerGap())
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(commentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(addressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(bankLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(bankTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(codeCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(bookmarkCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(labelLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(labelTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(commentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        ok = true;
        result = new AddressLabel(bank, address, label, comment,
                codeCheckBox.isSelected(), bookmarkCheckBox.isSelected());
        closeDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void bookmarkCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookmarkCheckBoxActionPerformed
        updateOkButton();
    }//GEN-LAST:event_bookmarkCheckBoxActionPerformed
    // End of variables declaration//GEN-END:variables
}
