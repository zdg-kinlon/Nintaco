package nintaco.gui.api.local;

import nintaco.App;
import nintaco.api.local.JarClassLoader;
import nintaco.api.local.LocalAPI;
import nintaco.gui.FileExtensionFilter;
import nintaco.gui.PleaseWaitDialog;
import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;
import nintaco.util.CollectionsUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static nintaco.files.FileUtil.getDirectoryPath;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.isBlank;

public class ProgramFrame extends javax.swing.JFrame {

    public static final FileExtensionFilter[] FileExtensionFilters = {
            new FileExtensionFilter(0, "JAR files (*.jar)", "jar"),
            new FileExtensionFilter(1, "All files (*.*)"),
    };
    private static final PrintStream standardOutput = System.out;
    private static final PrintStream standardError = System.err;
    private final PrintStream textAreaPrintStream;
    private JarClassLoader classLoader;
    private String loadedJarFileName;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel argumentsLabel;
    private javax.swing.JTextField argumentsTextField;
    private javax.swing.JButton clearOutputButton;
    private javax.swing.JButton findJarButton;
    private javax.swing.JButton hideWindowButton;
    private javax.swing.JPanel jarFilePanel;
    private javax.swing.JLabel jarNameLabel;
    private javax.swing.JTextField jarNameTextField;
    private javax.swing.JButton loadJarButton;
    private javax.swing.JComboBox mainClassComboBox;
    private javax.swing.JLabel mainClassLabel;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JTextArea outputTextArea;
    private javax.swing.JPanel runConfigurationPanel;
    private javax.swing.JToggleButton runToggleButton;
    private javax.swing.JLabel statusLabel;

    public ProgramFrame() {
        initComponents();
        clearRunConfiguration();
        makeMonospaced(outputTextArea);
        outputTextArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        textAreaPrintStream = new PrintStream(new TextAreaOutputStream(
                outputTextArea));
        enableAutoscroll(outputTextArea);
        addTextFieldEditListener(jarNameTextField, this::jarNameTextFieldEdited);
        loadFields();
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
    }

    private void closeFrame() {
        saveFields();
        setVisible(false);
    }

    public void destroy() {
        saveFields();
        dispose();
    }

    private void loadFields() {
        String jarName = "";
        jarName = GamePrefs.getInstance().getProgramGamePrefs().getJar();
        if (isBlank(jarName)) {
            final String jarDir = AppPrefs.getInstance().getPaths().getJarDir();
            if (!isBlank(jarDir)) {
                jarName = jarDir;
            }
        }

        jarNameTextField.setText(jarName);
    }

    private void saveFields() {
        final String jarName = jarNameTextField.getText().trim();
        AppPrefs.getInstance().getPaths().setJarDir(isBlank(jarName) ? null
                : getDirectoryPath(jarName));

        final ProgramGamePrefs prefs = GamePrefs.getInstance()
                .getProgramGamePrefs();
        prefs.setJar(jarName);
        prefs.setMainClass((String) mainClassComboBox.getSelectedItem());
        GamePrefs.save();
    }

    private void jarNameTextFieldEdited() {
        updateLoadJarButton();
        clearRunConfiguration();
    }

    private void updateLoadJarButton() {
        final File file = new File(jarNameTextField.getText());
        loadJarButton.setEnabled(file.exists() && file.isFile());
    }

    private void loadJarFile(final String fileName,
                             final PleaseWaitDialog pleaseWaitDialog) {

        loadedJarFileName = null;
        final File file = new File(fileName);
        if (!file.isFile() || !file.exists()) {
            displayError(this, "JAR file not found.");
            pleaseWaitDialog.dispose();
            return;
        }

        String[] mainClassNames = null;
        boolean error = false;
        try (final JarClassLoader loader = new JarClassLoader(fileName)) {
            mainClassNames = loader.getMainClassNames();
        } catch (final Throwable t) {
            //t.printStackTrace();
            error = true;
        }

        pleaseWaitDialog.dispose();

        if (error) {
            displayError(this, "Failed to load JAR file.");
        } else if (CollectionsUtil.isBlank(mainClassNames)) {
            displayError(this, "The JAR file does not contain any main class files.");
        } else {
            loadedJarFileName = fileName;
            final String[] names = mainClassNames;
            EventQueue.invokeLater(() -> setRunConfiguration(names));
        }
    }

    private void setJarFileEnabled(final boolean enabled) {
        jarFilePanel.setEnabled(enabled);
        jarNameLabel.setEnabled(enabled);
        jarNameTextField.setEnabled(enabled);
        findJarButton.setEnabled(enabled);
        if (enabled) {
            updateLoadJarButton();
        } else {
            loadJarButton.setEnabled(false);
        }
    }

    private void setRunConfiguration(final String[] mainClassNames) {
        mainClassComboBox.setModel(new DefaultComboBoxModel<>(mainClassNames));

        final String mainClass = GamePrefs.getInstance().getProgramGamePrefs()
                .getMainClass();
        if (!isBlank(mainClass) && CollectionsUtil.contains(mainClassNames,
                mainClass)) {
            mainClassComboBox.setSelectedItem(mainClass);
        }

        runConfigurationPanel.setEnabled(true);
        mainClassLabel.setEnabled(true);
        mainClassComboBox.setEnabled(true);
        argumentsLabel.setEnabled(true);
        argumentsTextField.setEnabled(true);
        runToggleButton.setEnabled(true);

        argumentsTextField.requestFocusInWindow();
    }

    private void clearRunConfiguration() {
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("                                          ");
        mainClassComboBox.setModel(model);
        runConfigurationPanel.setEnabled(false);
        mainClassLabel.setEnabled(false);
        mainClassComboBox.setEnabled(false);
        argumentsLabel.setEnabled(false);
        argumentsTextField.setEnabled(false);
        runToggleButton.setEnabled(false);
    }

    private String[] parseArguments(final String text) {
        final List<String> args = new ArrayList<>();

        boolean insideString = false;
        final int length = text.length();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= length; i++) {
            if (i == length) {
                if (sb.length() > 0) {
                    args.add(sb.toString());
                }
            } else {
                final char c = text.charAt(i);
                if (insideString) {
                    if (c == '\\') {
                        if (i + 1 == length) {
                            sb.append(c);
                        } else {
                            char c1 = text.charAt(++i);
                            switch (c1) {
                                case '\'':
                                    sb.append('\'');
                                    break;
                                case '"':
                                    sb.append('"');
                                    break;
                                case '\\':
                                    sb.append('\\');
                                    break;
                                case '0':
                                case '1':
                                case '2':
                                case '3': {
                                    int value = c1 - '0';
                                    if (i + 1 != length) {
                                        final char c2 = text.charAt(i + 1);
                                        if (c2 >= '0' && c2 <= '7') {
                                            value = (value << 3) | (c2 - '0');
                                            i++;
                                            if (i + 1 != length) {
                                                final char c3 = text.charAt(i + 1);
                                                if (c3 >= '0' && c3 <= '7') {
                                                    value = (value << 3) | (c3 - '0');
                                                    i++;
                                                }
                                            }
                                        }
                                    }
                                    sb.append((char) value);
                                    break;
                                }
                                case 'b':
                                    sb.append('\b');
                                    break;
                                case 'n':
                                    sb.append('\n');
                                    break;
                                case 'r':
                                    sb.append('\r');
                                    break;
                                case 'f':
                                    sb.append('\f');
                                    break;
                                case 't':
                                    sb.append('\t');
                                    break;
                                case 'u':
                                    if (i + 4 < length) {
                                        try {
                                            sb.append(Character.toChars(Integer.parseInt(text
                                                    .substring(i + 1, i + 5), 16)));
                                            i += 4;
                                        } catch (final Throwable t) {
                                            sb.append(c);
                                            sb.append(c1);
                                        }
                                    } else {
                                        sb.append(c);
                                        sb.append(c1);
                                    }
                                    break;
                                default:
                                    sb.append(c);
                                    sb.append(c1);
                                    break;
                            }
                        }
                    } else if (c == '"') {
                        insideString = false;
                        if (sb.length() > 0) {
                            args.add(sb.toString());
                            sb.setLength(0);
                        }
                    } else {
                        sb.append(c);
                    }
                } else {
                    if (c == '"' && (i == 0
                            || Character.isWhitespace(text.charAt(i - 1)))) {
                        insideString = true;
                    } else if (Character.isWhitespace(c)) {
                        if (sb.length() > 0) {
                            args.add(sb.toString());
                            sb.setLength(0);
                        }
                    } else {
                        sb.append(c);
                    }
                }
            }
        }

        return CollectionsUtil.convertToArray(String.class, args, false);
    }

    private void runPressed(final String jarFileName, final String mainClassName,
                            final String[] arguments) {

        final File file = new File(jarFileName);
        if (!file.isFile() || !file.exists()) {
            displayError(this, "JAR file not found.");
            EventQueue.invokeLater(this::stopButtonPressed);
            return;
        }

        LocalAPI.setLocalAPI(new LocalAPI());

        System.setOut(textAreaPrintStream);
        System.setErr(textAreaPrintStream);
        try {
            classLoader = new JarClassLoader(file);
            classLoader.runMain(mainClassName, arguments);
        } catch (final Throwable t) {
            //t.printStackTrace();
        }
    }

    private void runButtonPressed() {

        final String jarFileName = loadedJarFileName;
        final String mainClassName = (String) mainClassComboBox.getSelectedItem();
        final String arguments = argumentsTextField.getText();

        mainClassLabel.setEnabled(false);
        mainClassComboBox.setEnabled(false);
        argumentsLabel.setEnabled(false);
        argumentsTextField.setEnabled(false);

        hideWindowButton.setEnabled(true);

        runToggleButton.setMnemonic('S');
        runToggleButton.setText("Stop");
        scrollToBottom(outputTextArea);
        setJarFileEnabled(false);
        setTitle("Program Controls");
        new Thread(() -> runPressed(jarFileName, mainClassName,
                parseArguments(arguments))).start();
    }

    private void stopPressed() {

        final LocalAPI api = LocalAPI.getLocalAPI();
        if (api != null) {
            LocalAPI.setLocalAPI(null);
            api.dispose();
        }

        System.setOut(standardOutput);
        System.setErr(standardError);
        final JarClassLoader loader = classLoader;
        if (loader != null) {
            try {
                loader.close();
            } catch (final Throwable t) {
                //t.printStackTrace();
            }
        }
    }

    private void stopButtonPressed() {
        runToggleButton.setMnemonic('R');
        runToggleButton.setText("Run");

        mainClassLabel.setEnabled(true);
        mainClassComboBox.setEnabled(true);
        argumentsLabel.setEnabled(true);
        argumentsTextField.setEnabled(true);

        hideWindowButton.setEnabled(false);

        setJarFileEnabled(true);
        setTitle("Run Program");
        new Thread(this::stopPressed).start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jarFilePanel = new javax.swing.JPanel();
        jarNameLabel = new javax.swing.JLabel();
        jarNameTextField = new javax.swing.JTextField();
        loadJarButton = new javax.swing.JButton();
        findJarButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        runConfigurationPanel = new javax.swing.JPanel();
        argumentsTextField = new javax.swing.JTextField();
        mainClassLabel = new javax.swing.JLabel();
        argumentsLabel = new javax.swing.JLabel();
        mainClassComboBox = new javax.swing.JComboBox();
        runToggleButton = new javax.swing.JToggleButton();
        outputPanel = new javax.swing.JPanel();
        clearOutputButton = new javax.swing.JButton();
        outputScrollPane = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();
        hideWindowButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Run Program");
        setMinimumSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jarFilePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("JAR File"));

        jarNameLabel.setText("JAR name:");

        jarNameTextField.setMaximumSize(null);
        jarNameTextField.setMinimumSize(null);
        jarNameTextField.setPreferredSize(null);

        loadJarButton.setMnemonic('L');
        loadJarButton.setText("Load JAR");
        loadJarButton.setEnabled(false);
        loadJarButton.setFocusPainted(false);
        loadJarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadJarButtonActionPerformed(evt);
            }
        });

        findJarButton.setMnemonic('F');
        findJarButton.setText("Find JAR...");
        findJarButton.setFocusPainted(false);
        findJarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findJarButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jarFilePanelLayout = new javax.swing.GroupLayout(jarFilePanel);
        jarFilePanel.setLayout(jarFilePanelLayout);
        jarFilePanelLayout.setHorizontalGroup(
                jarFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jarFilePanelLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(jarNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jarNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(findJarButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(loadJarButton)
                                .addGap(5, 5, 5))
        );
        jarFilePanelLayout.setVerticalGroup(
                jarFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jarFilePanelLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(jarFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jarNameLabel)
                                        .addComponent(findJarButton)
                                        .addComponent(jarNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(loadJarButton))
                                .addGap(5, 5, 5))
        );

        statusLabel.setText(" ");
        statusLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        statusLabel.setMaximumSize(null);
        statusLabel.setMinimumSize(null);
        statusLabel.setPreferredSize(null);

        runConfigurationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Run Configuration"));

        argumentsTextField.setMaximumSize(null);
        argumentsTextField.setMinimumSize(null);
        argumentsTextField.setPreferredSize(null);

        mainClassLabel.setText("Main class:");
        mainClassLabel.setMaximumSize(null);
        mainClassLabel.setMinimumSize(null);
        mainClassLabel.setPreferredSize(null);

        argumentsLabel.setText("Arguments:");
        argumentsLabel.setMaximumSize(null);
        argumentsLabel.setMinimumSize(null);
        argumentsLabel.setName(""); // NOI18N
        argumentsLabel.setPreferredSize(null);

        mainClassComboBox.setFocusable(false);
        mainClassComboBox.setMaximumSize(null);
        mainClassComboBox.setMinimumSize(null);
        mainClassComboBox.setPreferredSize(null);

        runToggleButton.setMnemonic('R');
        runToggleButton.setText("Run");
        runToggleButton.setFocusPainted(false);
        runToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runToggleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout runConfigurationPanelLayout = new javax.swing.GroupLayout(runConfigurationPanel);
        runConfigurationPanel.setLayout(runConfigurationPanelLayout);
        runConfigurationPanelLayout.setHorizontalGroup(
                runConfigurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(runConfigurationPanelLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(runConfigurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(mainClassLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(argumentsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(runConfigurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(mainClassComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(argumentsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(runToggleButton)
                                .addGap(5, 5, 5))
        );
        runConfigurationPanelLayout.setVerticalGroup(
                runConfigurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(runConfigurationPanelLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(runConfigurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(mainClassLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(mainClassComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(7, 7, 7)
                                .addGroup(runConfigurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(argumentsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(argumentsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(runToggleButton))
                                .addGap(5, 5, 5))
        );

        outputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));

        clearOutputButton.setMnemonic('C');
        clearOutputButton.setText("Clear Output");
        clearOutputButton.setFocusPainted(false);
        clearOutputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearOutputButtonActionPerformed(evt);
            }
        });

        outputScrollPane.setMaximumSize(null);
        outputScrollPane.setMinimumSize(null);
        outputScrollPane.setPreferredSize(null);

        outputTextArea.setEditable(false);
        outputTextArea.setColumns(80);
        outputTextArea.setRows(10);
        outputTextArea.setMaximumSize(null);
        outputTextArea.setMinimumSize(null);
        outputTextArea.setPreferredSize(null);
        outputTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                outputTextAreaFocusGained(evt);
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                outputTextAreaFocusLost(evt);
            }
        });
        outputScrollPane.setViewportView(outputTextArea);

        hideWindowButton.setMnemonic('H');
        hideWindowButton.setText("Hide Window");
        hideWindowButton.setEnabled(false);
        hideWindowButton.setFocusPainted(false);
        hideWindowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideWindowButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout outputPanelLayout = new javax.swing.GroupLayout(outputPanel);
        outputPanel.setLayout(outputPanelLayout);
        outputPanelLayout.setHorizontalGroup(
                outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(outputPanelLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(outputScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(outputPanelLayout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(clearOutputButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(hideWindowButton)))
                                .addGap(5, 5, 5))
        );
        outputPanelLayout.setVerticalGroup(
                outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(outputPanelLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(outputScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(clearOutputButton)
                                        .addComponent(hideWindowButton))
                                .addGap(4, 4, 4))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jarFilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(runConfigurationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(outputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jarFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(runConfigurationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(outputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (hideWindowButton.isEnabled()) {
            closeFrame();
        } else {
            App.destroyProgramFrame();
        }
    }//GEN-LAST:event_formWindowClosing

    private void findJarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findJarButtonActionPerformed

        final ProgramGamePrefs prefs = GamePrefs.getInstance()
                .getProgramGamePrefs();
        String directory = null;
        final String jarName = jarNameTextField.getText();
        if (!isBlank(jarName)) {
            directory = getDirectoryPath(jarName);
        }
        if (isBlank(directory)) {
            directory = getDirectoryPath(prefs.getJar());
        }
        if (isBlank(directory)) {
            directory = AppPrefs.getInstance().getPaths().getJarDir();
        }

        final JFileChooser chooser = createFileChooser("Select JAR File", directory,
                FileExtensionFilters);
        if (showOpenDialog(this, chooser) == JFileChooser.APPROVE_OPTION) {
            final String fileName = chooser.getSelectedFile().toString();
            jarNameTextField.setText(fileName);
            prefs.setJar(fileName);
            GamePrefs.save();
        }
    }//GEN-LAST:event_findJarButtonActionPerformed

    private void clearOutputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearOutputButtonActionPerformed
        outputTextArea.setText("");
    }//GEN-LAST:event_clearOutputButtonActionPerformed

    private void runToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runToggleButtonActionPerformed
        if (runToggleButton.isSelected()) {
            runButtonPressed();
        } else {
            stopButtonPressed();
        }
    }//GEN-LAST:event_runToggleButtonActionPerformed

    private void loadJarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadJarButtonActionPerformed

        final String fileName = jarNameTextField.getText();
        if (isBlank(fileName)) {
            displayError(this, "Enter a JAR file name.");
            return;
        }

        final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
        pleaseWaitDialog.setMessage("Loading JAR file...");
        clearRunConfiguration();
        new Thread(() -> loadJarFile(fileName, pleaseWaitDialog)).start();
        pleaseWaitDialog.showAfterDelay();
    }//GEN-LAST:event_loadJarButtonActionPerformed

    private void hideWindowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideWindowButtonActionPerformed
        closeFrame();
    }//GEN-LAST:event_hideWindowButtonActionPerformed

    private void outputTextAreaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_outputTextAreaFocusGained
        showCursor(outputTextArea);
    }//GEN-LAST:event_outputTextAreaFocusGained

    private void outputTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_outputTextAreaFocusLost
        hideCursor(outputTextArea);
    }//GEN-LAST:event_outputTextAreaFocusLost
    // End of variables declaration//GEN-END:variables
}
