package nintaco.gui.api.server;

import nintaco.App;
import nintaco.gui.LocalIPAddressRenderer;
import nintaco.preferences.AppPrefs;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.InetAddress;
import java.util.List;

import static nintaco.util.GuiUtil.*;
import static nintaco.util.NetworkUtil.*;
import static nintaco.util.StringUtil.parseInt;

public class ProgramServerFrame extends javax.swing.JFrame {

    private static final Color GREEN = new Color(0x008000);

    private DocumentListener portListener;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activityLabel;
    private javax.swing.JScrollPane activityScrollPane;
    private javax.swing.JTextArea activityTextArea;
    private javax.swing.JButton clearActivityButton;
    private javax.swing.JButton hideWindowButton;
    private javax.swing.JComboBox localIPComboBox;
    private javax.swing.JLabel localIPLabel;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JCheckBox runInBackgroundCheckBox;
    private javax.swing.JToggleButton startServerButton;
    private javax.swing.JLabel statusLabel;

    public ProgramServerFrame() {
        initComponents();
        initLocalIPComboBox();
        initTextFields();
        loadFields();
        setServerStatus(false);
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
    }

    private void initLocalIPComboBox() {

        localIPComboBox.setRenderer(new LocalIPAddressRenderer());

        final List<InetAddress> localIPAddresses
                = sortAddresses(getNetworkInterfaces());
        localIPAddresses.add(0, null);
        localIPComboBox.setModel(new DefaultComboBoxModel<>(
                toArray(localIPAddresses)));
    }

    private void initTextFields() {
        portListener = createDocumentListener(this::enableComponents);
        portTextField.getDocument().addDocumentListener(portListener);
        makeMonospaced(activityTextArea);
        activityTextArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        enableAutoscroll(activityTextArea);
    }

    private void setPort(final int port) {
        setPort(Integer.toString(port));
    }

    private void setPort(final String port) {
        portTextField.getDocument().removeDocumentListener(portListener);
        portTextField.setText(port);
        portTextField.getDocument().addDocumentListener(portListener);
    }

    private void loadFields() {
        final ProgramServerPrefs prefs = AppPrefs.getInstance()
                .getProgramServerPrefs();
        localIPComboBox.setSelectedItem(prefs.getLocalIPAddress());
        setPort(prefs.getPort());
        runInBackgroundCheckBox.setSelected(prefs.isRunInBackground());
    }

    private void saveFields() {
        final ProgramServerPrefs prefs = AppPrefs.getInstance()
                .getProgramServerPrefs();
        prefs.setLocalIPAddress((InetAddress) localIPComboBox.getSelectedItem());
        prefs.setPort(parseInt(portTextField.getText(),
                ProgramServerPrefs.DEFAULT_PORT));
        prefs.setRunInBackground(runInBackgroundCheckBox.isSelected());
        AppPrefs.save();
    }

    public void enableComponents() {

        final boolean serverDown = !startServerButton.isSelected();

        localIPLabel.setEnabled(serverDown);
        localIPComboBox.setEnabled(serverDown);
        portLabel.setEnabled(serverDown);
        portTextField.setEnabled(serverDown);
        runInBackgroundCheckBox.setEnabled(serverDown);

        startServerButton.setEnabled(!serverDown
                || parseInt(portTextField.getText(), -1) >= 0);
        startServerButton.setText((serverDown ? "Start" : "Stop") + " Server");
    }

    public void destroy() {
        saveFields();
        dispose();
    }

    private void closeFrame() {
        if (startServerButton.isSelected()) {
            setVisible(false);
        } else {
            App.destroyProgramServerFrame();
        }
    }

    public void addActivity(final String activity, final Object... params) {
        if (EventQueue.isDispatchThread()) {
            activityTextArea.append(String.format(activity, params) + "\n");
        } else {
            EventQueue.invokeLater(() -> addActivity(activity, params));
        }
    }

    public void setServerStatus(final boolean serverUp) {
        if (EventQueue.isDispatchThread()) {
            startServerButton.setSelected(serverUp);
            if (serverUp) {
                statusLabel.setBackground(GREEN);
                statusLabel.setText("<html><b>RUNNING</b></html>");
            } else {
                statusLabel.setBackground(Color.RED);
                statusLabel.setText("<html><b>DOWN</b></html>");
            }
            enableComponents();
        } else {
            EventQueue.invokeLater(() -> setServerStatus(serverUp));
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

        localIPLabel = new javax.swing.JLabel();
        localIPComboBox = new javax.swing.JComboBox();
        portLabel = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();
        activityLabel = new javax.swing.JLabel();
        activityScrollPane = new javax.swing.JScrollPane();
        activityTextArea = new javax.swing.JTextArea();
        statusLabel = new javax.swing.JLabel();
        startServerButton = new javax.swing.JToggleButton();
        clearActivityButton = new javax.swing.JButton();
        hideWindowButton = new javax.swing.JButton();
        runInBackgroundCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Start Program Server");
        setMaximumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        localIPLabel.setText("Local IP address:");

        localIPComboBox.setFocusable(false);

        portLabel.setText("Port:");

        portTextField.setColumns(6);

        activityLabel.setText("Activity");

        activityScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        activityScrollPane.setMaximumSize(null);
        activityScrollPane.setMinimumSize(null);
        activityScrollPane.setPreferredSize(null);

        activityTextArea.setEditable(false);
        activityTextArea.setColumns(60);
        activityTextArea.setRows(8);
        activityTextArea.setMaximumSize(null);
        activityTextArea.setMinimumSize(null);
        activityTextArea.setPreferredSize(null);
        activityScrollPane.setViewportView(activityTextArea);

        statusLabel.setForeground(java.awt.Color.white);
        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        statusLabel.setText(" ");
        statusLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        statusLabel.setMaximumSize(null);
        statusLabel.setMinimumSize(null);
        statusLabel.setOpaque(true);
        statusLabel.setPreferredSize(null);

        startServerButton.setMnemonic('S');
        startServerButton.setText("Start Server");
        startServerButton.setFocusPainted(false);
        startServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startServerButtonActionPerformed(evt);
            }
        });

        clearActivityButton.setMnemonic('A');
        clearActivityButton.setText("Clear Activity");
        clearActivityButton.setFocusPainted(false);
        clearActivityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActivityButtonActionPerformed(evt);
            }
        });

        hideWindowButton.setMnemonic('H');
        hideWindowButton.setText("Hide Window");
        hideWindowButton.setFocusPainted(false);
        hideWindowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideWindowButtonActionPerformed(evt);
            }
        });

        runInBackgroundCheckBox.setText("Continue emulation when not in focus");
        runInBackgroundCheckBox.setFocusPainted(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(activityScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(startServerButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(clearActivityButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(hideWindowButton))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(localIPLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(localIPComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(portLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(activityLabel)
                                                        .addComponent(runInBackgroundCheckBox))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, clearActivityButton, hideWindowButton, startServerButton, statusLabel);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(localIPLabel)
                                        .addComponent(localIPComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(portLabel)
                                        .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(runInBackgroundCheckBox)
                                .addGap(18, 18, 18)
                                .addComponent(activityLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(activityScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(startServerButton)
                                        .addComponent(hideWindowButton)
                                        .addComponent(clearActivityButton)
                                        .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, clearActivityButton, hideWindowButton, startServerButton, statusLabel);

    }// </editor-fold>//GEN-END:initComponents

    private void startServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startServerButtonActionPerformed
        if (startServerButton.isSelected()) {
            saveFields();
            setTitle("Program Server Controls");
            App.startProgramServer();
        } else {
            setTitle("Start Program Server");
            App.stopProgramServer();
        }
        enableComponents();
    }//GEN-LAST:event_startServerButtonActionPerformed

    private void clearActivityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActivityButtonActionPerformed
        activityTextArea.setText("");
    }//GEN-LAST:event_clearActivityButtonActionPerformed

    private void hideWindowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideWindowButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_hideWindowButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing
    // End of variables declaration//GEN-END:variables
}
