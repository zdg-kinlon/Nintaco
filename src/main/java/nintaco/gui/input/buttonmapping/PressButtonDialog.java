package nintaco.gui.input.buttonmapping;


import nintaco.input.ButtonID;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nintaco.input.InputUtil.clearEventQueues;
import static nintaco.input.InputUtil.pollControllers;
import static nintaco.util.GuiUtil.scaleFonts;

public class PressButtonDialog extends javax.swing.JDialog {

    private final List<ButtonID> buttonIds = new ArrayList<>();
    private final Set<ButtonID> heldButtons = new HashSet<>();
    private final List<ButtonID> pressedButtons = new ArrayList<>();
    private boolean canceled;    private final Timer timer = new Timer(25, this::onTimerEvent);
    private boolean skipped;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel pressLabel;
    private javax.swing.JButton skipButton;

    public PressButtonDialog(final java.awt.Window parent,
                             final boolean showSkipButton) {
        super(parent);
        setModal(true);
        initComponents();
        skipButton.setVisible(showSkipButton);
        scaleFonts(this);
        pack();
        setLocationRelativeTo(parent);
        clearEventQueues();
        timer.start();
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean isSkipped() {
        return skipped;
    }

    private void onTimerEvent(ActionEvent e) {
        if (canceled || skipped) {
            return;
        }
        pollControllers(buttonIds);
        for (final ButtonID buttonID : buttonIds) {
            processButtonID(buttonID);
        }
    }

    private void processButtonID(final ButtonID buttonID) {
        if (buttonID.getValue() == 0) {
            final String buttonName = buttonID.getName();
            if (buttonName.isEmpty()) {
                heldButtons.remove(new ButtonID(buttonID.getDevice(), "x", -1));
                heldButtons.remove(new ButtonID(buttonID.getDevice(), "x", 1));
                heldButtons.remove(new ButtonID(buttonID.getDevice(), "y", -1));
                heldButtons.remove(new ButtonID(buttonID.getDevice(), "y", 1));
            } else {
                heldButtons.remove(new ButtonID(buttonID.getDevice(), buttonName, -1));
                heldButtons.remove(new ButtonID(buttonID.getDevice(), buttonName, 1));
            }
        } else {
            heldButtons.add(buttonID);
            if (!pressedButtons.contains(buttonID)) {
                pressedButtons.add(buttonID);
            }
        }

        if (!pressedButtons.isEmpty()) {
            EventQueue.invokeLater(() -> {
                if (!(canceled || skipped)) {
                    updatePressLabel();
                    if (heldButtons.isEmpty()) {
                        closeDialog();
                    }
                }
            });
        }
    }

    private void updatePressLabel() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html><center>");
        for (int i = 0; i < pressedButtons.size(); i++) {
            final ButtonID buttonID = pressedButtons.get(i);
            if (i > 0) {
                sb.append(" + ");
            }
            sb.append("<b>").append(buttonID).append("</b>");
        }
        sb.append("</center></html>");
        pressLabel.setText(sb.toString());
    }

    public void setButtonName(String buttonName) {
        pressLabel.setText("<html><center>Press <b>" + buttonName + "</b><br/><br/>"
                + "Hold down multiple to create button combos.</center></html>");
    }

    private void cancel() {
        canceled = true;
        pressedButtons.clear();
        pressLabel.setText("");
    }

    private void skip() {
        skipped = true;
        pressedButtons.clear();
        pressLabel.setText("");
    }

    private void cancelDialog() {
        cancel();
        closeDialog();
    }

    private void closeDialog() {
        timer.stop();
        clearEventQueues();
        EventQueue.invokeLater(this::dispose);
    }

    public ButtonID[] getButtonIds() {
        if (canceled || skipped) {
            return new ButtonID[0];
        }
        final ButtonID[] bs = new ButtonID[pressedButtons.size()];
        pressedButtons.toArray(bs);
        return bs;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pressLabel = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        skipButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Map");
        setMaximumSize(null);
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        pressLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pressLabel.setText(" ");
        pressLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(32, 32, 32, 32));
        pressLabel.setMaximumSize(null);
        pressLabel.setMinimumSize(null);
        pressLabel.setPreferredSize(null);

        cancelButton.setMnemonic('C');
        cancelButton.setText("Cancel");
        cancelButton.setFocusPainted(false);
        cancelButton.setFocusable(false);
        cancelButton.setMaximumSize(null);
        cancelButton.setMinimumSize(null);
        cancelButton.setPreferredSize(null);
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                cancelButtonMousePressed(evt);
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        skipButton.setMnemonic('S');
        skipButton.setText("Skip");
        skipButton.setFocusPainted(false);
        skipButton.setFocusable(false);
        skipButton.setMaximumSize(null);
        skipButton.setMinimumSize(null);
        skipButton.setPreferredSize(null);
        skipButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                skipButtonMousePressed(evt);
            }
        });
        skipButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skipButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(skipButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                        .addComponent(pressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, skipButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(pressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(skipButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelDialog();
    }//GEN-LAST:event_formWindowClosing

    private void cancelButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMousePressed
        cancel();
    }//GEN-LAST:event_cancelButtonMousePressed

    private void skipButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skipButtonActionPerformed
        skip();
        closeDialog();
    }//GEN-LAST:event_skipButtonActionPerformed

    private void skipButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_skipButtonMousePressed
        skip();
    }//GEN-LAST:event_skipButtonMousePressed

    // End of variables declaration//GEN-END:variables
}
