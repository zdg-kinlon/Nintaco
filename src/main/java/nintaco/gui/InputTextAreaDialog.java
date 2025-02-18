package nintaco.gui;

import nintaco.util.GuiUtil;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;

import static nintaco.util.GuiUtil.createDocumentListener;
import static nintaco.util.GuiUtil.scaleFonts;

public class InputTextAreaDialog extends javax.swing.JDialog {

    private boolean ok;
    private String input;
    private boolean textRequired;
    private DocumentListener documentListener;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel promptLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextArea textArea;

    public InputTextAreaDialog(final Window parent, final String message,
                               final String title) {
        super(parent);
        setModal(true);
        initComponents();
        initDocumentListener();
        getRootPane().setDefaultButton(okButton);
        scaleFonts(this);
        setPrompt(message, title);
    }

    private void initDocumentListener() {
        documentListener = createDocumentListener(this::enableComponents);
        textArea.getDocument().addDocumentListener(documentListener);
    }

    @Override
    public void setVisible(final boolean visible) {
        pack();
        setLocationRelativeTo(getParent());
        super.setVisible(visible);
    }

    public void makeMonospaced() {
        GuiUtil.makeMonospaced(textArea);
    }

    public void setDimensions(final int columns, final int rows) {
        textArea.setColumns(columns);
        textArea.setRows(rows);
    }

    private void enableComponents() {
        okButton.setEnabled(!(textRequired && textArea.getText().isEmpty()));
    }

    public void setTextRequired() {
        textRequired = true;
        enableComponents();
    }

    public final void setPrompt(String message, final String title) {
        if (!message.toLowerCase().trim().startsWith("<html>")) {
            message = "<html>" + message + "</html>";
        }
        setTitle(title);
        promptLabel.setText(message);
        iconLabel.setIcon(UIManager.getIcon("OptionPane.questionIcon"));
        textArea.requestFocus();
    }

    public void setOkButtonMnemonic(final char mnemonic) {
        okButton.setMnemonic(mnemonic);
    }

    public void setOkButtonText(final String text) {
        okButton.setText(text);
    }

    public void setCancelButtonMnemonic(char mnemonic) {
        cancelButton.setMnemonic(mnemonic);
    }

    public void setCancelButtonText(final String text) {
        cancelButton.setText(text);
    }

    public String getInput() {
        return input;
    }

    public void setInput(final String input) {
        if (input == null) {
            return;
        }
        textArea.getDocument().removeDocumentListener(documentListener);
        textArea.setText(input);
        textArea.getDocument().addDocumentListener(documentListener);
        textArea.selectAll();
        enableComponents();
        textArea.requestFocus();
    }

    public boolean isOk() {
        return ok;
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

        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        inputPanel = new javax.swing.JPanel();
        promptLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        iconLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cancelButton.setMnemonic('C');
        cancelButton.setLabel("   Cancel   ");
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

        promptLabel.setText(" ");

        scrollPane.setMaximumSize(null);
        scrollPane.setMinimumSize(null);

        textArea.setColumns(28);
        textArea.setRows(5);
        textArea.setMaximumSize(null);
        textArea.setMinimumSize(null);
        textArea.setPreferredSize(null);
        scrollPane.setViewportView(textArea);

        javax.swing.GroupLayout inputPanelLayout = new javax.swing.GroupLayout(inputPanel);
        inputPanel.setLayout(inputPanelLayout);
        inputPanelLayout.setHorizontalGroup(
                inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inputPanelLayout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(promptLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(1, 1, 1))
        );
        inputPanelLayout.setVerticalGroup(
                inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(inputPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(promptLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );

        iconLabel.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(iconLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(inputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(15, 15, 15)
                                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(15, 15, 15))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(iconLabel)
                                        .addComponent(inputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(15, 15, 15))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        ok = true;
        input = textArea.getText();
        closeDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
