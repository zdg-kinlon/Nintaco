package nintaco.gui.netplay.client;

import nintaco.App;
import nintaco.palettes.PalettePPU;
import nintaco.palettes.PaletteUtil;
import nintaco.preferences.AppPrefs;

import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Arrays;

import static java.lang.Integer.max;
import static nintaco.gui.netplay.client.ClientStatus.OFFLINE;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.isBlank;
import static nintaco.util.StringUtil.parseInt;

public class NetplayClientFrame extends javax.swing.JFrame {

    private static final Color GREEN = new Color(0x008000);
    private static final Color YELLOW = new Color(0x808000);

    private DocumentListener hostListener;
    private DocumentListener portListener;
    private DocumentListener passwordListener;
    private char[] password;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activityLabel;
    private javax.swing.JScrollPane activityScrollPane;
    private javax.swing.JTextArea activityTextArea;
    private javax.swing.JButton clearActivityButton;
    private javax.swing.JToggleButton connectButton;
    private javax.swing.JComboBox controllerComboBox;
    private javax.swing.JLabel controllerLabel;
    private javax.swing.JButton hideWindowButton;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JCheckBox passwordCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JComboBox playerComboBox;
    private javax.swing.JLabel playerLabel;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JLabel statusLabel;

    public NetplayClientFrame() {
        initComponents();
        initTextFields();
        loadFields();
        setClientStatus(OFFLINE);
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
    }

    private void initTextFields() {
        addLoseFocusListener(this, hostTextField);
        addLoseFocusListener(this, portTextField);
        addLoseFocusListener(this, passwordField);
        hostListener = createDocumentListener(this::enableComponents);
        hostTextField.getDocument().addDocumentListener(hostListener);
        portListener = createDocumentListener(this::enableComponents);
        portTextField.getDocument().addDocumentListener(portListener);
        passwordListener = createDocumentListener(
                () -> password = passwordField.getPassword());
        passwordField.getDocument().addDocumentListener(passwordListener);
        makeMonospaced(activityTextArea);
        activityTextArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        enableAutoscroll(activityTextArea);
    }

    public void setPassword(final char[] password) {
        this.password = Arrays.copyOf(password, password.length);
        setPassword(password.length);
    }

    private void setPassword(final int passwordLength) {
        final StringBuilder sb = new StringBuilder();
        for (int i = passwordLength - 1; i >= 0; i--) {
            sb.append('*');
        }
        setPassword(sb.toString());
    }

    private void setPassword(final String password) {
        passwordField.getDocument().removeDocumentListener(passwordListener);
        passwordField.setText(password);
        passwordField.getDocument().addDocumentListener(passwordListener);
    }

    private void setPort(final int port) {
        setPort(Integer.toString(port));
    }

    private void setPort(final String port) {
        portTextField.getDocument().removeDocumentListener(portListener);
        portTextField.setText(port);
        portTextField.getDocument().addDocumentListener(portListener);
    }

    private void setHost(final String host) {
        hostTextField.getDocument().removeDocumentListener(hostListener);
        hostTextField.setText(host);
        hostTextField.getDocument().addDocumentListener(hostListener);
    }

    public void destroy() {
        App.stopClient();
        saveFields();
        if (PaletteUtil.setPalettePPU(PalettePPU._2C02)) {
            App.getImageFrame().createPaletteMenu();
        }
        dispose();
    }

    private void closeFrame() {
        if (connectButton.isSelected()) {
            setVisible(false);
        } else {
            App.destroyNetplayClientFrame();
        }
    }

    public void loadFields() {
        final NetplayClientPrefs prefs = AppPrefs.getInstance()
                .getNetplayClientPrefs();
        setHost(prefs.getHost());
        controllerComboBox.setSelectedIndex(prefs.getInputDevice());
        passwordCheckBox.setSelected(prefs.isRememberPassword());
        if (prefs.isRememberPassword()) {
            setPassword(prefs.getPasswordLength());
        }
        playerComboBox.setSelectedIndex(prefs.getPlayer());
        setPort(prefs.getPort());
    }

    private void saveFields() {
        final NetplayClientPrefs prefs = AppPrefs.getInstance()
                .getNetplayClientPrefs();

        prefs.setHost(hostTextField.getText());
        prefs.setInputDevice(max(0, controllerComboBox.getSelectedIndex()));
        prefs.setPlayer(max(0, playerComboBox.getSelectedIndex()));
        prefs.setPort(parseInt(portTextField.getText(),
                NetplayClientPrefs.DEFAULT_PORT));
        prefs.setRememberPassword(passwordCheckBox.isSelected());
        if (password != null) {
            prefs.setPasswordLength(passwordCheckBox.isSelected()
                    ? password.length : 0);
        }

        AppPrefs.save();
    }

    private void enableComponents() {

        final boolean clientDown = !connectButton.isSelected();

        hostLabel.setEnabled(clientDown);
        hostTextField.setEnabled(clientDown);
        portLabel.setEnabled(clientDown);
        portTextField.setEnabled(clientDown);
        passwordLabel.setEnabled(clientDown);
        passwordField.setEnabled(clientDown);
        passwordCheckBox.setEnabled(clientDown);
        playerLabel.setEnabled(clientDown);
        playerComboBox.setEnabled(clientDown);

        final boolean controllerEnabled = clientDown
                && playerComboBox.getSelectedIndex() != 4;
        controllerLabel.setEnabled(controllerEnabled);
        controllerComboBox.setEnabled(controllerEnabled);

        connectButton.setEnabled(!clientDown || (!isBlank(hostTextField.getText())
                && parseInt(portTextField.getText(), -1) >= 0));
        connectButton.setText(clientDown ? "Connect" : "Stop");
    }

    public void addActivity(final String activity, final Object... params) {
        if (EventQueue.isDispatchThread()) {
            activityTextArea.append(String.format(activity, params) + "\n");
        } else {
            EventQueue.invokeLater(() -> addActivity(activity, params));
        }
    }

    public void setClientStatus(final ClientStatus status) {
        if (EventQueue.isDispatchThread()) {
            connectButton.setSelected(status != OFFLINE);
            switch (status) {
                case OFFLINE:
                    statusLabel.setBackground(Color.RED);
                    statusLabel.setText("<html><b>OFFLINE</b></html>");
                    break;
                case CONNECTING:
                    statusLabel.setBackground(YELLOW);
                    statusLabel.setText("<html><b>CONNECTING</b></html>");
                    break;
                case ONLINE:
                    statusLabel.setBackground(GREEN);
                    statusLabel.setText("<html><b>ONLINE</b></html>");
                    break;
            }
            enableComponents();
        } else {
            EventQueue.invokeLater(() -> setClientStatus(status));
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

        hostLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();
        playerLabel = new javax.swing.JLabel();
        playerComboBox = new javax.swing.JComboBox();
        controllerLabel = new javax.swing.JLabel();
        controllerComboBox = new javax.swing.JComboBox();
        activityLabel = new javax.swing.JLabel();
        connectButton = new javax.swing.JToggleButton();
        clearActivityButton = new javax.swing.JButton();
        hideWindowButton = new javax.swing.JButton();
        passwordLabel = new javax.swing.JLabel();
        passwordCheckBox = new javax.swing.JCheckBox();
        passwordField = new javax.swing.JPasswordField();
        activityScrollPane = new javax.swing.JScrollPane();
        activityTextArea = new javax.swing.JTextArea();
        statusLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Connect to Server");
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        hostLabel.setText("Host:");
        hostLabel.setMaximumSize(null);
        hostLabel.setMinimumSize(null);
        hostLabel.setPreferredSize(null);

        hostTextField.setColumns(50);
        hostTextField.setMaximumSize(null);
        hostTextField.setMinimumSize(null);

        portLabel.setText("Port:");
        portLabel.setMaximumSize(null);
        portLabel.setMinimumSize(null);
        portLabel.setPreferredSize(null);

        portTextField.setColumns(6);
        portTextField.setMaximumSize(null);
        portTextField.setMinimumSize(null);
        portTextField.setPreferredSize(null);

        playerLabel.setText("Player:");
        playerLabel.setMaximumSize(null);
        playerLabel.setMinimumSize(null);
        playerLabel.setPreferredSize(null);

        playerComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"1", "2", "3", "4", "Spectator"}));
        playerComboBox.setFocusable(false);
        playerComboBox.setMaximumSize(null);
        playerComboBox.setMinimumSize(null);
        playerComboBox.setPreferredSize(null);
        playerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playerComboBoxActionPerformed(evt);
            }
        });

        controllerLabel.setText("Controller:");
        controllerLabel.setMaximumSize(null);
        controllerLabel.setMinimumSize(null);
        controllerLabel.setPreferredSize(null);

        controllerComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Gamepad 1", "Gamepad 2", "Gamepad 3", "Gamepad 4"}));
        controllerComboBox.setFocusable(false);
        controllerComboBox.setMaximumSize(null);
        controllerComboBox.setMinimumSize(null);
        controllerComboBox.setPreferredSize(null);

        activityLabel.setText("Activity");
        activityLabel.setMaximumSize(null);
        activityLabel.setMinimumSize(null);
        activityLabel.setPreferredSize(null);

        connectButton.setMnemonic('C');
        connectButton.setText("Connect");
        connectButton.setFocusPainted(false);
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectButtonActionPerformed(evt);
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

        passwordLabel.setText("Password (optional):");

        passwordCheckBox.setText("Remember password");
        passwordCheckBox.setFocusPainted(false);

        passwordField.setColumns(16);
        passwordField.setMaximumSize(null);
        passwordField.setMinimumSize(null);

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
        statusLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        statusLabel.setOpaque(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(statusLabel)
                                                .addGap(18, 18, Short.MAX_VALUE)
                                                .addComponent(connectButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(clearActivityButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(hideWindowButton))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(hostLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(portLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(hostTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(playerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(playerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(controllerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(controllerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(activityLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(passwordLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(9, 9, 9)
                                                .addComponent(passwordCheckBox))
                                        .addComponent(activityScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, clearActivityButton, connectButton, hideWindowButton, statusLabel);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(hostLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(portLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(passwordLabel)
                                        .addComponent(passwordCheckBox)
                                        .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(playerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(playerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(controllerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(controllerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(activityLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(activityScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(connectButton)
                                        .addComponent(clearActivityButton)
                                        .addComponent(hideWindowButton)
                                        .addComponent(statusLabel))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, clearActivityButton, connectButton, hideWindowButton, statusLabel);

    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
        if (connectButton.isSelected()) {
            saveFields();
            App.startClient(password);
            if (!passwordCheckBox.isSelected()) {
                setPassword("");
                password = null;
            }
            setTitle("Netplay Client Controls");
        } else {
            App.stopClient();
            setTitle("Connect to Server");
        }
        enableComponents();
    }//GEN-LAST:event_connectButtonActionPerformed

    private void clearActivityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActivityButtonActionPerformed
        activityTextArea.setText("");
    }//GEN-LAST:event_clearActivityButtonActionPerformed

    private void hideWindowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideWindowButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_hideWindowButtonActionPerformed

    private void playerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playerComboBoxActionPerformed
        enableComponents();
    }//GEN-LAST:event_playerComboBoxActionPerformed
    // End of variables declaration//GEN-END:variables
}
