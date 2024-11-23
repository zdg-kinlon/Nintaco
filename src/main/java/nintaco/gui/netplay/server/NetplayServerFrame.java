package nintaco.gui.netplay.server;

import nintaco.App;
import nintaco.gui.LocalIPAddressRenderer;
import nintaco.input.Ports;
import nintaco.preferences.AppPrefs;
import nintaco.util.PasswordUtil;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import static nintaco.input.ConsoleType.VsDualSystem;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.NetworkUtil.*;
import static nintaco.util.StringUtil.parseInt;

public class NetplayServerFrame extends javax.swing.JFrame {

    private static final Color GREEN = new Color(0x008000);

    private JCheckBox[] playerCheckBoxes;
    private DocumentListener passwordListener;
    private DocumentListener portListener;
    private boolean passwordEdited;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activityLabel;
    private javax.swing.JScrollPane activityScrollPane;
    private javax.swing.JTextArea activityTextArea;
    private javax.swing.JButton clearActivityButton;
    private javax.swing.JButton hideWindowButton;
    private javax.swing.JCheckBox highSpeedCheckBox;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JComboBox localIPComboBox;
    private javax.swing.JLabel localIPLabel;
    private javax.swing.JLabel localPlayersLabel;
    private javax.swing.JCheckBox passwordCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JCheckBox player1CheckBox;
    private javax.swing.JCheckBox player2CheckBox;
    private javax.swing.JCheckBox player3CheckBox;
    private javax.swing.JCheckBox player4CheckBox;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JCheckBox quickSavesCheckBox;
    private javax.swing.JCheckBox rewindTimeCheckBox;
    private javax.swing.JCheckBox spectatorsCheckBox;
    private javax.swing.JToggleButton startServerButton;
    private javax.swing.JLabel statusLabel;
    public NetplayServerFrame() {
        initComponents();
        initPlayerCheckBoxes();
        initLocalIPComboBox();
        initTextFields();
        loadFields();
        setServerStatus(false);
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
    }

    private void initPlayerCheckBoxes() {
        playerCheckBoxes = new JCheckBox[]{
                player1CheckBox,
                player2CheckBox,
                player3CheckBox,
                player4CheckBox,
        };
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
        addLoseFocusListener(this, portTextField);
        addLoseFocusListener(this, passwordField);
        passwordListener = createDocumentListener(() -> passwordEdited = true);
        passwordField.getDocument().addDocumentListener(passwordListener);
        portListener = createDocumentListener(this::enableComponents);
        portTextField.getDocument().addDocumentListener(portListener);
        makeMonospaced(activityTextArea);
        activityTextArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        enableAutoscroll(activityTextArea);
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

    private void loadFields() {
        final NetplayServerPrefs prefs = AppPrefs.getInstance()
                .getNetplayServerPrefs();
        localIPComboBox.setSelectedItem(prefs.getLocalIPAddress());
        setPort(prefs.getPort());
        final boolean[] localPlayers = prefs.getLocalPlayers();
        for (int i = localPlayers.length - 1; i >= 0; i--) {
            playerCheckBoxes[i].setSelected(localPlayers[i]);
        }
        passwordCheckBox.setSelected(prefs.isEnablePassword());
        setPassword(prefs.getPasswordLength());
        rewindTimeCheckBox.setSelected(prefs.isAllowRewindTime());
        highSpeedCheckBox.setSelected(prefs.isAllowHighSpeed());
        spectatorsCheckBox.setSelected(prefs.isAllowSpectators());
        quickSavesCheckBox.setSelected(prefs.isAllowQuickSaves());
    }

    private void saveFields() {
        final NetplayServerPrefs prefs = AppPrefs.getInstance()
                .getNetplayServerPrefs();
        prefs.setAllowRewindTime(rewindTimeCheckBox.isSelected());
        prefs.setAllowHighSpeed(highSpeedCheckBox.isSelected());
        prefs.setAllowSpectators(spectatorsCheckBox.isSelected());
        prefs.setAllowQuickSaves(quickSavesCheckBox.isSelected());
        prefs.setLocalIPAddress((InetAddress) localIPComboBox.getSelectedItem());
        final boolean[] localPlayers = new boolean[4];
        for (int i = localPlayers.length - 1; i >= 0; i--) {
            localPlayers[i] = playerCheckBoxes[i].isSelected();
        }
        prefs.setLocalPlayers(localPlayers);
        prefs.setPort(parseInt(portTextField.getText(),
                NetplayServerPrefs.DEFAULT_PORT));

        prefs.setEnablePassword(passwordCheckBox.isSelected());
        if (passwordCheckBox.isSelected()) {
            if (passwordEdited) {
                final char[] password = passwordField.getPassword();
                final byte[] salt = PasswordUtil.createSalt();
                prefs.setPasswordLength(password.length);
                prefs.setPasswordSalt(salt);
                prefs.setPasswordHash(PasswordUtil.createHash(password, salt));
                Arrays.fill(password, '0');
            }
        } else {
            prefs.setPasswordSalt(null);
            prefs.setPasswordHash(null);
            prefs.setPasswordLength(0);
        }

        AppPrefs.save();
    }

    public void enableComponents() {

        final boolean serverDown = !startServerButton.isSelected();
        final boolean passwordEnabled = passwordCheckBox.isSelected();

        final Ports ports = AppPrefs.getInstance().getInputs().getPorts();
        final boolean fourPlayers = ports.isMultitap()
                || ports.getConsoleType() == VsDualSystem;

        localIPLabel.setEnabled(serverDown);
        localIPComboBox.setEnabled(serverDown);
        portLabel.setEnabled(serverDown);
        portTextField.setEnabled(serverDown);
        localPlayersLabel.setEnabled(serverDown);
        player1CheckBox.setEnabled(serverDown);
        player2CheckBox.setEnabled(serverDown);
        player3CheckBox.setEnabled(serverDown && fourPlayers);
        player4CheckBox.setEnabled(serverDown && fourPlayers);
        passwordCheckBox.setEnabled(serverDown);
        passwordLabel.setEnabled(serverDown && passwordEnabled);
        passwordField.setEnabled(serverDown && passwordEnabled);
        spectatorsCheckBox.setEnabled(serverDown);
        rewindTimeCheckBox.setEnabled(serverDown);
        highSpeedCheckBox.setEnabled(serverDown);
        quickSavesCheckBox.setEnabled(serverDown);

        startServerButton.setEnabled(!serverDown
                || parseInt(portTextField.getText(), -1) >= 0);
        startServerButton.setText((serverDown ? "Start" : "Stop") + " Server");
    }

    public void destroy() {
        App.stopNetplayServer();
        saveFields();
        dispose();
    }

    private void closeFrame() {
        if (startServerButton.isSelected()) {
            setVisible(false);
        } else {
            App.destroyNetplayServerFrame();
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

        jTextField1 = new javax.swing.JTextField();
        startServerButton = new javax.swing.JToggleButton();
        localIPLabel = new javax.swing.JLabel();
        localIPComboBox = new javax.swing.JComboBox();
        portLabel = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();
        localPlayersLabel = new javax.swing.JLabel();
        player1CheckBox = new javax.swing.JCheckBox();
        player2CheckBox = new javax.swing.JCheckBox();
        player3CheckBox = new javax.swing.JCheckBox();
        player4CheckBox = new javax.swing.JCheckBox();
        spectatorsCheckBox = new javax.swing.JCheckBox();
        passwordLabel = new javax.swing.JLabel();
        passwordCheckBox = new javax.swing.JCheckBox();
        passwordField = new javax.swing.JPasswordField();
        hideWindowButton = new javax.swing.JButton();
        activityLabel = new javax.swing.JLabel();
        clearActivityButton = new javax.swing.JButton();
        activityScrollPane = new javax.swing.JScrollPane();
        activityTextArea = new javax.swing.JTextArea();
        statusLabel = new javax.swing.JLabel();
        rewindTimeCheckBox = new javax.swing.JCheckBox();
        quickSavesCheckBox = new javax.swing.JCheckBox();
        highSpeedCheckBox = new javax.swing.JCheckBox();

        jTextField1.setText("jTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Start Netplay Server");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        startServerButton.setMnemonic('S');
        startServerButton.setText("Start Server");
        startServerButton.setFocusPainted(false);
        startServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startServerButtonActionPerformed(evt);
            }
        });

        localIPLabel.setText("Local IP address:");

        localIPComboBox.setFocusable(false);

        portLabel.setText("Port:");

        portTextField.setColumns(6);

        localPlayersLabel.setText("Local players:");

        player1CheckBox.setText("1P");
        player1CheckBox.setFocusPainted(false);

        player2CheckBox.setText("2P");
        player2CheckBox.setFocusPainted(false);

        player3CheckBox.setText("3P");
        player3CheckBox.setFocusPainted(false);

        player4CheckBox.setText("4P");
        player4CheckBox.setFocusPainted(false);

        spectatorsCheckBox.setText("Allow spectators");
        spectatorsCheckBox.setFocusPainted(false);

        passwordLabel.setText("Password:");

        passwordCheckBox.setText("Enable password");
        passwordCheckBox.setFocusPainted(false);
        passwordCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordCheckBoxActionPerformed(evt);
            }
        });

        passwordField.setColumns(16);

        hideWindowButton.setMnemonic('H');
        hideWindowButton.setText("Hide Window");
        hideWindowButton.setFocusPainted(false);
        hideWindowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideWindowButtonActionPerformed(evt);
            }
        });

        activityLabel.setText("Activity");

        clearActivityButton.setMnemonic('A');
        clearActivityButton.setText("Clear Activity");
        clearActivityButton.setFocusPainted(false);
        clearActivityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActivityButtonActionPerformed(evt);
            }
        });

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

        rewindTimeCheckBox.setText("Allow remote players to rewind time");
        rewindTimeCheckBox.setFocusPainted(false);

        quickSavesCheckBox.setText("Allow remote players to access quick saves");
        quickSavesCheckBox.setFocusPainted(false);

        highSpeedCheckBox.setText("Allow remote players to control high-speed ");
        highSpeedCheckBox.setFocusPainted(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(startServerButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(clearActivityButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(hideWindowButton))
                                        .addComponent(activityScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(localPlayersLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(player1CheckBox)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(player2CheckBox)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(player3CheckBox)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(player4CheckBox))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(passwordCheckBox)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(passwordLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(activityLabel)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(rewindTimeCheckBox)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(highSpeedCheckBox))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(spectatorsCheckBox)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(quickSavesCheckBox)))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, clearActivityButton, hideWindowButton, startServerButton, statusLabel);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(localIPLabel)
                                        .addComponent(localIPComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(portLabel)
                                        .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(localPlayersLabel)
                                        .addComponent(player1CheckBox)
                                        .addComponent(player2CheckBox)
                                        .addComponent(player3CheckBox)
                                        .addComponent(player4CheckBox))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(passwordCheckBox)
                                        .addComponent(passwordLabel)
                                        .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(spectatorsCheckBox)
                                        .addComponent(quickSavesCheckBox))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(highSpeedCheckBox)
                                        .addComponent(rewindTimeCheckBox))
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

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void passwordCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordCheckBoxActionPerformed
        enableComponents();
    }//GEN-LAST:event_passwordCheckBoxActionPerformed

    private void hideWindowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideWindowButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_hideWindowButtonActionPerformed

    private void clearActivityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActivityButtonActionPerformed
        activityTextArea.setText("");
    }//GEN-LAST:event_clearActivityButtonActionPerformed

    private void startServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startServerButtonActionPerformed
        if (startServerButton.isSelected()) {
            saveFields();
            App.startNetplayServer();
            setTitle("Netplay Server Controls");
        } else {
            App.stopNetplayServer();
            setTitle("Start Netplay Server");
        }
        enableComponents();
    }//GEN-LAST:event_startServerButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
