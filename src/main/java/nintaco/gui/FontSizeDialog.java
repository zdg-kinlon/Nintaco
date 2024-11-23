package nintaco.gui;

import java.awt.*;

import static nintaco.util.GuiUtil.scaleFonts;

public class FontSizeDialog extends javax.swing.JDialog {

    private final Font defaultFont;

    private float fontScale = 1f;
    private boolean fontScaleChanged;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private javax.swing.JButton restoreDefaultButton;
    private javax.swing.JSlider sizeSlider;
    private javax.swing.JLabel textLabel;

    public FontSizeDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        getRootPane().setDefaultButton(okButton);
        defaultFont = textLabel.getFont();
        initTextLabel();
        scaleFonts(this);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initTextLabel() {
        final FontMetrics metrics = textLabel.getFontMetrics(textLabel.getFont());
        textLabel.setPreferredSize(new Dimension(100 * metrics.charWidth('M'),
                metrics.getHeight() * 10));
    }

    private void closeDialog() {
        dispose();
    }

    public boolean isFontScaleChanged() {
        return fontScaleChanged;
    }

    public float getFontScale() {
        return fontScale;
    }

    public void setFontScale(final float fontScale) {
        this.fontScale = fontScale;
        sizeSlider.setValue((int) (1000f * fontScale));
        updateTextLabel();
    }

    private void updateTextLabel() {
        textLabel.setText(String.format("<html><center>Adjust the slider below to "
                + "change the font size.<br/>(%d%%)</center></html>", Math.round(
                100f * fontScale)));
        textLabel.setFont(defaultFont.deriveFont(defaultFont.getSize2D()
                * fontScale));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textLabel = new javax.swing.JLabel();
        sizeSlider = new javax.swing.JSlider();
        cancelButton = new javax.swing.JButton();
        restoreDefaultButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Font Size");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        textLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        textLabel.setText("<html>Adjust the slider below to change the font size.</html>");
        textLabel.setMaximumSize(null);
        textLabel.setMinimumSize(null);
        textLabel.setPreferredSize(null);

        sizeSlider.setMajorTickSpacing(200);
        sizeSlider.setMaximum(3000);
        sizeSlider.setMinimum(1000);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setFocusable(false);
        sizeSlider.setMaximumSize(null);
        sizeSlider.setMinimumSize(null);
        sizeSlider.setPreferredSize(null);
        sizeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sizeSliderStateChanged(evt);
            }
        });

        cancelButton.setMnemonic('C');
        cancelButton.setText("   Cancel   ");
        cancelButton.setFocusPainted(false);
        cancelButton.setMaximumSize(null);
        cancelButton.setMinimumSize(null);
        cancelButton.setPreferredSize(null);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        restoreDefaultButton.setMnemonic('R');
        restoreDefaultButton.setText("Reset");
        restoreDefaultButton.setFocusPainted(false);
        restoreDefaultButton.setMaximumSize(null);
        restoreDefaultButton.setMinimumSize(null);
        restoreDefaultButton.setPreferredSize(null);
        restoreDefaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreDefaultButtonActionPerformed(evt);
            }
        });

        okButton.setMnemonic('O');
        okButton.setText("OK");
        okButton.setFocusPainted(false);
        okButton.setMaximumSize(null);
        okButton.setMinimumSize(null);
        okButton.setPreferredSize(null);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(textLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(sizeSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(restoreDefaultButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton, restoreDefaultButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(textLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(sizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(restoreDefaultButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sizeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sizeSliderStateChanged
        fontScale = sizeSlider.getValue() / 1000f;
        updateTextLabel();
    }//GEN-LAST:event_sizeSliderStateChanged

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        fontScaleChanged = true;
        closeDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    private void restoreDefaultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreDefaultButtonActionPerformed
        sizeSlider.setValue(1000);
        fontScale = 1f;
    }//GEN-LAST:event_restoreDefaultButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing
    // End of variables declaration//GEN-END:variables
}
