package nintaco.gui;

import javax.swing.*;
import java.awt.*;

import static nintaco.util.GuiUtil.scaleFonts;

public class InformationDialog extends javax.swing.JDialog {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel infoLabel;
    private javax.swing.JButton okButton;

    public InformationDialog(Window parent, String message, String title,
                             IconType iconType) {
        super(parent);
        setModal(true);
        initComponents();
        getRootPane().setDefaultButton(okButton);
        scaleFonts(this);
        setMessage(message, title, iconType);
    }

    public void setMessage(String message, String title, IconType iconType) {
        if (!message.toLowerCase().trim().startsWith("<html>")) {
            message = "<html>" + message + "</html>";
        }
        setTitle(title);
        infoLabel.setText(message);
        final String iconName;
        switch (iconType) {
            case ERROR:
                iconName = "OptionPane.errorIcon";
                break;
            case WARNING:
                iconName = "OptionPane.warningIcon";
                break;
            default:
                iconName = "OptionPane.informationIcon";
                break;
        }
        infoLabel.setIcon(UIManager.getIcon(iconName));
        pack();
        setLocationRelativeTo(getParent());
    }

    private void closeDialog() {
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        infoLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconImage(null);
        setIconImages(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        okButton.setMnemonic('O');
        okButton.setFocusPainted(false);
        okButton.setLabel("   OK   ");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        infoLabel.setText(" ");
        infoLabel.setIconTextGap(16);
        infoLabel.setMaximumSize(null);
        infoLabel.setMinimumSize(null);
        infoLabel.setPreferredSize(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(0, 162, Short.MAX_VALUE)
                                                .addComponent(okButton)))
                                .addGap(15, 15, 15))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(15, 15, 15)
                                .addComponent(okButton)
                                .addGap(15, 15, 15))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing
    public enum IconType {
        INFORMATION, ERROR, WARNING
    }
    // End of variables declaration//GEN-END:variables
}
