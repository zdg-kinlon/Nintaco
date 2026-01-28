package nintaco.gui.debugger.logger;

import nintaco.App;
import nintaco.disassembler.LogPrefs;
import nintaco.gui.FileExtensionFilter;
import nintaco.gui.image.preferences.Paths;
import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;

import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static nintaco.PPU.*;
import static nintaco.apu.APU.REG_APU_STATUS;
import static nintaco.files.FileUtil.getFileNameWithoutExtension;
import static nintaco.files.FileUtil.mkdir;
import static nintaco.mappers.Mapper.REG_INPUT_PORT_1;
import static nintaco.mappers.Mapper.REG_OUTPUT_PORT;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.*;

public class LoggerDialog extends javax.swing.JDialog {

    private boolean ok;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox aCheckBox;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JCheckBox addressLabelsCheckBox;
    private javax.swing.JTextField addressesTextField;
    private javax.swing.JCheckBox apuStatusCheckBox;
    private javax.swing.JCheckBox bankCheckBox;
    private javax.swing.JComboBox branchesComboBox;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox controller1CheckBox;
    private javax.swing.JCheckBox controller2CheckBox;
    private javax.swing.JPanel countersPanel;
    private javax.swing.JCheckBox cpuCounterCheckBox;
    private javax.swing.JPanel cpuRegistersPanel;
    private javax.swing.JCheckBox dotCheckBox;
    private javax.swing.JPanel executionPanel;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JCheckBox fineXCheckBox;
    private javax.swing.JPanel formattingPanel;
    private javax.swing.JCheckBox frameCounterCheckBox;
    private javax.swing.JCheckBox inspectionsCheckBox;
    private javax.swing.JCheckBox instructionCheckBox;
    private javax.swing.JCheckBox instructionCounterCheckBox;
    private javax.swing.JComboBox logOrderComboBox;
    private javax.swing.JCheckBox machineCodeCheckBox;
    private javax.swing.JLabel maxLinesLabel;
    private javax.swing.JTextField maxLinesTextField;
    private javax.swing.JCheckBox oamDataCheckBox;
    private javax.swing.JButton okButton;
    private final DocumentListener textFieldListener = createDocumentListener(
            this::updateOkButton);
    private javax.swing.JPanel otherRegistersPanel;
    private javax.swing.JComboBox pComboBox;
    private javax.swing.JComboBox pcComboBox;
    private javax.swing.JCheckBox ppuDataCheckBox;
    private javax.swing.JPanel ppuRegistersPanel;
    private javax.swing.JCheckBox ppuStatusCheckBox;
    private javax.swing.JCheckBox scanlineCheckBox;
    private javax.swing.JComboBox spComboBox;
    private javax.swing.JCheckBox tCheckBox;
    private javax.swing.JCheckBox tabBySpCheckBox;
    private javax.swing.JCheckBox vCheckBox;
    private javax.swing.JCheckBox wCheckBox;
    private javax.swing.JCheckBox xCheckBox;
    private javax.swing.JCheckBox yCheckBox;
    public LoggerDialog(final Window parent) {
        super(parent);
        setModal(true);
        initComponents();
        loadFields();
        initTextFields();
        scaleFonts(this);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initTextFields() {
        addLoseFocusListener(this, fileTextField);
        addLoseFocusListener(this, maxLinesTextField);
        addLoseFocusListener(this, addressesTextField);
        fileTextField.getDocument().addDocumentListener(textFieldListener);
        maxLinesTextField.getDocument().addDocumentListener(textFieldListener);
    }

    public boolean isOk() {
        return ok;
    }

    private void closeDialog() {
        dispose();
    }

    private void loadFields() {
        final LoggerAppPrefs appPrefs = AppPrefs.getInstance().getLoggerAppPrefs();

        maxLinesTextField.setText(Integer.toString(appPrefs.getMaxLines()));

        final LogPrefs prefs = appPrefs.getLogPrefs();

        frameCounterCheckBox.setSelected(prefs.frameCounter);
        cpuCounterCheckBox.setSelected(prefs.cpuCounter);
        instructionCounterCheckBox.setSelected(prefs.instructionCounter);
        scanlineCheckBox.setSelected(prefs.scanline);
        dotCheckBox.setSelected(prefs.dot);

        logOrderComboBox.setSelectedIndex(prefs.logBeforeExecute ? 0 : 1);
        bankCheckBox.setSelected(prefs.bank);
        machineCodeCheckBox.setSelected(prefs.machineCode);
        instructionCheckBox.setSelected(prefs.instruction);
        inspectionsCheckBox.setSelected(prefs.inspections);

        pcComboBox.setSelectedIndex(prefs.logPCType);
        aCheckBox.setSelected(prefs.A);
        xCheckBox.setSelected(prefs.X);
        yCheckBox.setSelected(prefs.Y);
        pComboBox.setSelectedIndex(prefs.logPType);
        spComboBox.setSelectedIndex(prefs.logSType);

        vCheckBox.setSelected(prefs.v);
        tCheckBox.setSelected(prefs.t);
        fineXCheckBox.setSelected(prefs.x);
        wCheckBox.setSelected(prefs.w);

        addressLabelsCheckBox.setSelected(prefs.addressLabels);
        tabBySpCheckBox.setSelected(prefs.tabBySP);
        branchesComboBox.setSelectedIndex(prefs.branchesType);

        final Set<Integer> addresses = new HashSet<>();
        if (prefs.addresses != null) {
            for (final int address : prefs.addresses) {
                addresses.add(address);
            }
        }
        ppuStatusCheckBox.setSelected(addresses.contains(REG_PPU_STATUS));
        oamDataCheckBox.setSelected(addresses.contains(REG_OAM_DATA));
        ppuDataCheckBox.setSelected(addresses.contains(REG_PPU_DATA));
        apuStatusCheckBox.setSelected(addresses.contains(REG_APU_STATUS));
        controller1CheckBox.setSelected(addresses.contains(REG_OUTPUT_PORT));
        controller2CheckBox.setSelected(addresses.contains(REG_INPUT_PORT_1));
        addresses.remove(REG_PPU_STATUS);
        addresses.remove(REG_OAM_DATA);
        addresses.remove(REG_PPU_DATA);
        addresses.remove(REG_APU_STATUS);
        addresses.remove(REG_OUTPUT_PORT);
        addresses.remove(REG_INPUT_PORT_1);

        final int[] as = new int[addresses.size()];
        int i = 0;
        for (final int address : addresses) {
            as[i++] = address;
        }
        Arrays.sort(as);
        final StringBuilder sb = new StringBuilder();
        for (final int address : as) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            append(sb, "%04X", address);
        }
        addressesTextField.setText(sb.toString());
        fileTextField.setText(GamePrefs.getInstance().getLoggerGamePrefs()
                .getFileName());
    }

    private void saveFields() {

        final LoggerAppPrefs appPrefs = AppPrefs.getInstance().getLoggerAppPrefs();

        appPrefs.setMaxLines(parseMaxLinesTextField());

        final LogPrefs prefs = appPrefs.getLogPrefs();

        prefs.frameCounter = frameCounterCheckBox.isSelected();
        prefs.cpuCounter = cpuCounterCheckBox.isSelected();
        prefs.instructionCounter = instructionCounterCheckBox.isSelected();
        prefs.scanline = scanlineCheckBox.isSelected();
        prefs.dot = dotCheckBox.isSelected();

        prefs.logBeforeExecute = logOrderComboBox.getSelectedIndex() == 0;
        prefs.bank = bankCheckBox.isSelected();
        prefs.machineCode = machineCodeCheckBox.isSelected();
        prefs.instruction = instructionCheckBox.isSelected();
        prefs.inspections = inspectionsCheckBox.isSelected();

        prefs.logPCType = pcComboBox.getSelectedIndex();
        prefs.A = aCheckBox.isSelected();
        prefs.X = xCheckBox.isSelected();
        prefs.Y = yCheckBox.isSelected();
        prefs.logPType = pComboBox.getSelectedIndex();
        prefs.logSType = spComboBox.getSelectedIndex();

        prefs.v = vCheckBox.isSelected();
        prefs.t = tCheckBox.isSelected();
        prefs.x = fineXCheckBox.isSelected();
        prefs.w = wCheckBox.isSelected();

        prefs.addressLabels = addressLabelsCheckBox.isSelected();
        prefs.tabBySP = tabBySpCheckBox.isSelected();
        prefs.branchesType = branchesComboBox.getSelectedIndex();

        final Set<Integer> addresses = new HashSet<>();
        if (ppuStatusCheckBox.isSelected()) {
            addresses.add(REG_PPU_STATUS);
        }
        if (oamDataCheckBox.isSelected()) {
            addresses.add(REG_OAM_DATA);
        }
        if (ppuDataCheckBox.isSelected()) {
            addresses.add(REG_PPU_DATA);
        }
        if (apuStatusCheckBox.isSelected()) {
            addresses.add(REG_APU_STATUS);
        }
        if (controller1CheckBox.isSelected()) {
            addresses.add(REG_OUTPUT_PORT);
        }
        if (controller2CheckBox.isSelected()) {
            addresses.add(REG_INPUT_PORT_1);
        }

        final String addressesText = addressesTextField.getText().trim();
        if (!addressesText.isEmpty()) {
            for (final String s : addressesText.split("[^0-9a-fA-F]+")) {
                final int address = parseInt(s, true, 0xFFFF);
                if (address >= 0) {
                    addresses.add(address);
                }
            }
        }
        final int[] as = new int[addresses.size()];
        int i = 0;
        for (final int address : addresses) {
            as[i++] = address;
        }
        Arrays.sort(as);
        if (as.length > 0) {
            prefs.addresses = as;
        } else {
            prefs.addresses = null;
        }
        AppPrefs.save();

        GamePrefs.getInstance().getLoggerGamePrefs().setFileName(fileTextField
                .getText().trim());
        GamePrefs.save();
    }

    private int parseMaxLinesTextField() {
        final Integer value = parseInt(maxLinesTextField.getText(), false, 1,
                10_000_000);
        return value == null ? -1 : value;
    }

    private void updateOkButton() {
        okButton.setEnabled(!isBlank(fileTextField.getText())
                && parseMaxLinesTextField() >= 1);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileLabel = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        fileTextField = new javax.swing.JTextField();
        countersPanel = new javax.swing.JPanel();
        frameCounterCheckBox = new javax.swing.JCheckBox();
        cpuCounterCheckBox = new javax.swing.JCheckBox();
        scanlineCheckBox = new javax.swing.JCheckBox();
        dotCheckBox = new javax.swing.JCheckBox();
        instructionCounterCheckBox = new javax.swing.JCheckBox();
        executionPanel = new javax.swing.JPanel();
        machineCodeCheckBox = new javax.swing.JCheckBox();
        instructionCheckBox = new javax.swing.JCheckBox();
        inspectionsCheckBox = new javax.swing.JCheckBox();
        bankCheckBox = new javax.swing.JCheckBox();
        logOrderComboBox = new javax.swing.JComboBox();
        cpuRegistersPanel = new javax.swing.JPanel();
        aCheckBox = new javax.swing.JCheckBox();
        xCheckBox = new javax.swing.JCheckBox();
        yCheckBox = new javax.swing.JCheckBox();
        pComboBox = new javax.swing.JComboBox();
        spComboBox = new javax.swing.JComboBox();
        pcComboBox = new javax.swing.JComboBox();
        formattingPanel = new javax.swing.JPanel();
        addressLabelsCheckBox = new javax.swing.JCheckBox();
        tabBySpCheckBox = new javax.swing.JCheckBox();
        branchesComboBox = new javax.swing.JComboBox();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        maxLinesLabel = new javax.swing.JLabel();
        maxLinesTextField = new javax.swing.JTextField();
        otherRegistersPanel = new javax.swing.JPanel();
        apuStatusCheckBox = new javax.swing.JCheckBox();
        controller1CheckBox = new javax.swing.JCheckBox();
        controller2CheckBox = new javax.swing.JCheckBox();
        addressLabel = new javax.swing.JLabel();
        addressesTextField = new javax.swing.JTextField();
        ppuRegistersPanel = new javax.swing.JPanel();
        ppuStatusCheckBox = new javax.swing.JCheckBox();
        oamDataCheckBox = new javax.swing.JCheckBox();
        ppuDataCheckBox = new javax.swing.JCheckBox();
        vCheckBox = new javax.swing.JCheckBox();
        tCheckBox = new javax.swing.JCheckBox();
        fineXCheckBox = new javax.swing.JCheckBox();
        wCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Logger Configuration");
        setMaximumSize(null);
        setMinimumSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        fileLabel.setText("File:");
        fileLabel.setMaximumSize(null);
        fileLabel.setMinimumSize(null);
        fileLabel.setPreferredSize(null);

        browseButton.setText("Browse...");
        browseButton.setMaximumSize(null);
        browseButton.setMinimumSize(null);
        browseButton.setPreferredSize(null);
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        fileTextField.setMaximumSize(null);
        fileTextField.setMinimumSize(null);
        fileTextField.setPreferredSize(null);

        countersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Counters"));
        countersPanel.setMaximumSize(null);

        frameCounterCheckBox.setText("Frames");
        frameCounterCheckBox.setMaximumSize(null);
        frameCounterCheckBox.setMinimumSize(null);
        frameCounterCheckBox.setPreferredSize(null);

        cpuCounterCheckBox.setText("CPU Cycles");
        cpuCounterCheckBox.setMaximumSize(null);
        cpuCounterCheckBox.setMinimumSize(null);
        cpuCounterCheckBox.setPreferredSize(null);

        scanlineCheckBox.setText("Scanline");
        scanlineCheckBox.setMaximumSize(null);
        scanlineCheckBox.setMinimumSize(null);
        scanlineCheckBox.setPreferredSize(null);

        dotCheckBox.setText("Dot");
        dotCheckBox.setMaximumSize(null);
        dotCheckBox.setMinimumSize(null);
        dotCheckBox.setPreferredSize(null);

        instructionCounterCheckBox.setText("Instructions");
        instructionCounterCheckBox.setMaximumSize(null);
        instructionCounterCheckBox.setMinimumSize(null);
        instructionCounterCheckBox.setPreferredSize(null);

        javax.swing.GroupLayout countersPanelLayout = new javax.swing.GroupLayout(countersPanel);
        countersPanel.setLayout(countersPanelLayout);
        countersPanelLayout.setHorizontalGroup(
                countersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(countersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(countersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(frameCounterCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cpuCounterCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(instructionCounterCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scanlineCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(dotCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        countersPanelLayout.setVerticalGroup(
                countersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(countersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(frameCounterCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(instructionCounterCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cpuCounterCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scanlineCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dotCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(54, Short.MAX_VALUE))
        );

        executionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Execution"));
        executionPanel.setMaximumSize(null);

        machineCodeCheckBox.setText("Machine code");
        machineCodeCheckBox.setMaximumSize(null);
        machineCodeCheckBox.setMinimumSize(null);
        machineCodeCheckBox.setPreferredSize(null);

        instructionCheckBox.setText("Instruction");
        instructionCheckBox.setMaximumSize(null);
        instructionCheckBox.setMinimumSize(null);
        instructionCheckBox.setPreferredSize(null);

        inspectionsCheckBox.setText("Inspections");

        bankCheckBox.setText("Bank");
        bankCheckBox.setMaximumSize(null);
        bankCheckBox.setMinimumSize(null);
        bankCheckBox.setPreferredSize(null);

        logOrderComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Log before execute", "Log after execute"}));
        logOrderComboBox.setMaximumSize(null);
        logOrderComboBox.setMinimumSize(null);
        logOrderComboBox.setPreferredSize(null);

        javax.swing.GroupLayout executionPanelLayout = new javax.swing.GroupLayout(executionPanel);
        executionPanel.setLayout(executionPanelLayout);
        executionPanelLayout.setHorizontalGroup(
                executionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(executionPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(executionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(machineCodeCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(instructionCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(inspectionsCheckBox)
                                        .addComponent(bankCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(logOrderComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
        executionPanelLayout.setVerticalGroup(
                executionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(executionPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(logOrderComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bankCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(machineCodeCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(instructionCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(inspectionsCheckBox)
                                .addContainerGap(51, Short.MAX_VALUE))
        );

        cpuRegistersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("CPU Registers"));
        cpuRegistersPanel.setMaximumSize(null);

        aCheckBox.setText("A");
        aCheckBox.setMaximumSize(null);
        aCheckBox.setMinimumSize(null);
        aCheckBox.setPreferredSize(null);

        xCheckBox.setText("X");
        xCheckBox.setMaximumSize(null);
        xCheckBox.setMinimumSize(null);
        xCheckBox.setPreferredSize(null);

        yCheckBox.setText("Y");
        yCheckBox.setMaximumSize(null);
        yCheckBox.setMinimumSize(null);
        yCheckBox.setPreferredSize(null);

        pComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"P:HH", "P:NVUBDIZC", "No P"}));
        pComboBox.setMaximumSize(null);
        pComboBox.setMinimumSize(null);
        pComboBox.setPreferredSize(null);

        spComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"S", "SP", "No SP"}));
        spComboBox.setMaximumSize(null);
        spComboBox.setMinimumSize(null);
        spComboBox.setPreferredSize(null);

        pcComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"PC", "$PC", "No PC"}));
        pcComboBox.setMaximumSize(null);
        pcComboBox.setMinimumSize(null);
        pcComboBox.setPreferredSize(null);

        javax.swing.GroupLayout cpuRegistersPanelLayout = new javax.swing.GroupLayout(cpuRegistersPanel);
        cpuRegistersPanel.setLayout(cpuRegistersPanelLayout);
        cpuRegistersPanelLayout.setHorizontalGroup(
                cpuRegistersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(cpuRegistersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(cpuRegistersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(spComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(pComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(yCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(xCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(aCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(pcComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
        cpuRegistersPanelLayout.setVerticalGroup(
                cpuRegistersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(cpuRegistersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(pcComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(aCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(xCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(26, Short.MAX_VALUE))
        );

        formattingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Formatting"));
        formattingPanel.setMaximumSize(null);

        addressLabelsCheckBox.setText("Address labels");
        addressLabelsCheckBox.setMaximumSize(null);
        addressLabelsCheckBox.setMinimumSize(null);
        addressLabelsCheckBox.setPreferredSize(null);

        tabBySpCheckBox.setText("Tab by SP");
        tabBySpCheckBox.setMaximumSize(null);
        tabBySpCheckBox.setMinimumSize(null);
        tabBySpCheckBox.setPreferredSize(null);

        branchesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Absolute branches", "Hex branches", "Decimal branches"}));
        branchesComboBox.setMaximumSize(null);
        branchesComboBox.setMinimumSize(null);
        branchesComboBox.setPreferredSize(null);

        javax.swing.GroupLayout formattingPanelLayout = new javax.swing.GroupLayout(formattingPanel);
        formattingPanel.setLayout(formattingPanelLayout);
        formattingPanelLayout.setHorizontalGroup(
                formattingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(formattingPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(formattingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(addressLabelsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tabBySpCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(branchesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
        formattingPanelLayout.setVerticalGroup(
                formattingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(formattingPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(addressLabelsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tabBySpCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(branchesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(101, Short.MAX_VALUE))
        );

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

        maxLinesLabel.setText("Max lines:");
        maxLinesLabel.setMaximumSize(null);
        maxLinesLabel.setMinimumSize(null);
        maxLinesLabel.setPreferredSize(null);

        maxLinesTextField.setColumns(8);
        maxLinesTextField.setMaximumSize(null);
        maxLinesTextField.setMinimumSize(null);
        maxLinesTextField.setPreferredSize(null);

        otherRegistersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Other Registers"));
        otherRegistersPanel.setMaximumSize(null);

        apuStatusCheckBox.setText("APUSTATUS ($4015)");
        apuStatusCheckBox.setMaximumSize(null);
        apuStatusCheckBox.setMinimumSize(null);
        apuStatusCheckBox.setPreferredSize(null);

        controller1CheckBox.setText("CONTROLLER1 ($4016)");
        controller1CheckBox.setMaximumSize(null);
        controller1CheckBox.setMinimumSize(null);
        controller1CheckBox.setPreferredSize(null);

        controller2CheckBox.setText("CONTROLLER2 ($4017)");
        controller2CheckBox.setMaximumSize(null);
        controller2CheckBox.setMinimumSize(null);
        controller2CheckBox.setPreferredSize(null);

        javax.swing.GroupLayout otherRegistersPanelLayout = new javax.swing.GroupLayout(otherRegistersPanel);
        otherRegistersPanel.setLayout(otherRegistersPanelLayout);
        otherRegistersPanelLayout.setHorizontalGroup(
                otherRegistersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(otherRegistersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(otherRegistersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(apuStatusCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(controller1CheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(controller2CheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        otherRegistersPanelLayout.setVerticalGroup(
                otherRegistersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(otherRegistersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(apuStatusCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(controller1CheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(controller2CheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(100, Short.MAX_VALUE))
        );

        addressLabel.setText("Addresses:");
        addressLabel.setMaximumSize(null);
        addressLabel.setMinimumSize(null);
        addressLabel.setPreferredSize(null);

        addressesTextField.setMaximumSize(null);
        addressesTextField.setMinimumSize(null);
        addressesTextField.setPreferredSize(null);

        ppuRegistersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PPU Registers"));

        ppuStatusCheckBox.setText("PPUSTATUS ($2002)");
        ppuStatusCheckBox.setMaximumSize(null);
        ppuStatusCheckBox.setMinimumSize(null);
        ppuStatusCheckBox.setPreferredSize(null);

        oamDataCheckBox.setText("OAMDATA ($2004)");
        oamDataCheckBox.setMaximumSize(null);
        oamDataCheckBox.setMinimumSize(null);
        oamDataCheckBox.setPreferredSize(null);

        ppuDataCheckBox.setText("PPUDATA ($2007)");
        ppuDataCheckBox.setMaximumSize(null);
        ppuDataCheckBox.setMinimumSize(null);
        ppuDataCheckBox.setPreferredSize(null);

        vCheckBox.setText("VRAM address (v)");
        vCheckBox.setMaximumSize(null);
        vCheckBox.setMinimumSize(null);
        vCheckBox.setPreferredSize(null);

        tCheckBox.setText("Temp VRAM addr (t)");
        tCheckBox.setMaximumSize(null);
        tCheckBox.setMinimumSize(null);
        tCheckBox.setPreferredSize(null);

        fineXCheckBox.setText("Fine X scroll (x)");
        fineXCheckBox.setMaximumSize(null);
        fineXCheckBox.setMinimumSize(null);
        fineXCheckBox.setPreferredSize(null);

        wCheckBox.setText("Write toggle (w)");
        wCheckBox.setMaximumSize(null);
        wCheckBox.setMinimumSize(null);
        wCheckBox.setPreferredSize(null);

        javax.swing.GroupLayout ppuRegistersPanelLayout = new javax.swing.GroupLayout(ppuRegistersPanel);
        ppuRegistersPanel.setLayout(ppuRegistersPanelLayout);
        ppuRegistersPanelLayout.setHorizontalGroup(
                ppuRegistersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ppuRegistersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(ppuRegistersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(ppuStatusCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(oamDataCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ppuDataCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(vCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fineXCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(wCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ppuRegistersPanelLayout.setVerticalGroup(
                ppuRegistersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ppuRegistersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(vCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fineXCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(wCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ppuStatusCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(oamDataCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ppuDataCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(8, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(fileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(fileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(browseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(maxLinesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(maxLinesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(addressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(addressesTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(countersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(executionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cpuRegistersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(ppuRegistersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(otherRegistersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(formattingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(fileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(browseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(maxLinesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(maxLinesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addressesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(countersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(executionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(formattingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(cpuRegistersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(otherRegistersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ppuRegistersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, countersPanel, cpuRegistersPanel, executionPanel, formattingPanel, otherRegistersPanel, ppuRegistersPanel);

    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (confirmOverwrite(this, fileTextField.getText())) {
            ok = true;
            saveFields();
            closeDialog();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed

        final Paths paths = AppPrefs.getInstance().getPaths();
        String fileName = fileTextField.getText().trim();
        final String inputFileName;
        String logsDir;
        if (fileName.isEmpty()) {
            final String entryFileName = App.getEntryFileName();
            inputFileName = (isBlank(entryFileName) ? "trace"
                    : getFileNameWithoutExtension(entryFileName)) + ".log";
            logsDir = paths.getLogsDir();
        } else {
            final File f = new File(fileName);
            inputFileName = f.getName();
            logsDir = f.getParent();
            if (isBlank(logsDir)) {
                logsDir = paths.getLogsDir();
            }
        }

        mkdir(logsDir);
        final File file = showSaveAsDialog(this, logsDir,
                inputFileName, "log", new FileExtensionFilter(0,
                        "Log files (*.log)", "log"), false, "Choose Log File");
        if (file != null) {
            final String dir = file.getParent();
            paths.addRecentDirectory(dir);
            paths.setLogsDir(dir);
            AppPrefs.save();

            fileTextField.setText(file.getPath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
