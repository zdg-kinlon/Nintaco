package nintaco.gui;

import nintaco.files.Console;
import nintaco.files.ExtendedConsole;
import nintaco.files.MutableNesFile;
import nintaco.files.NesFile;
import nintaco.files.NesFile.CpuPpuTiming;
import nintaco.files.NesFile.DefaultExpansionDevice;
import nintaco.gui.image.ImageFrame;
import nintaco.gui.image.preferences.Paths;
import nintaco.mappers.nintendo.vs.VsHardware;
import nintaco.mappers.nintendo.vs.VsPPU;
import nintaco.preferences.AppPrefs;
import nintaco.util.EDT;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import static java.lang.Math.min;
import static nintaco.mappers.NametableMirroring.*;
import static nintaco.util.BitUtil.ceilBase2;
import static nintaco.util.BitUtil.log2;
import static nintaco.util.GuiUtil.scaleFonts;
import static nintaco.util.GuiUtil.showSaveAsDialog;
import static nintaco.util.MathUtil.clamp;
import static nintaco.util.StringUtil.parseInt;

public class EditNesHeaderDialog extends javax.swing.JDialog {

    private static final String[] MEM_SIZES_16 = {"None", "16K", "32K", "64K",
            "128K", "256K", "512K", "1M", "2M", "4M", "8M", "16M", "32M", "64M"};
    private static final String[] MEM_SIZES_8 = {"None", "8K", "16K", "32K",
            "64K", "128K", "256K", "512K", "1M", "2M", "4M", "8M", "16M", "32M"};
    private static final String[] RAM_SIZES = {"None", "128", "256", "512", "1K",
            "2K", "4K", "8K", "16K", "32K", "64K", "128K", "256K", "512K", "1M"};
    private MutableNesFile originalNesFile;    private final ItemListener itemListener = this::selectionChanged;
    private MutableNesFile nesFile;
    private File saveFile;
    private String entryFileName;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CancelButton;
    private javax.swing.JButton RestoreAllButton;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JComboBox chrNVRamComboBox;
    private javax.swing.JLabel chrNVRamLabel;
    private javax.swing.JComboBox chrRamComboBox;
    private javax.swing.JLabel chrRamLabel;
    private javax.swing.JComboBox chrRomComboBox;
    private javax.swing.JLabel chrRomLabel;
    private javax.swing.JComboBox<String> consoleComboBox;
    private javax.swing.JLabel consoleLabel;
    private javax.swing.ButtonGroup fileFormatButtonGroup;
    private javax.swing.JLabel fileFormatLabel;
    private javax.swing.JRadioButton fourScreenRadioButton;
    private javax.swing.JRadioButton horizontalRadioButton;
    private javax.swing.JRadioButton iNESRadioButton;
    private javax.swing.JPanel leftPanel;
    private javax.swing.ButtonGroup machineButtonGroup;
    private javax.swing.JLabel mapperLabel;
    private javax.swing.JTextField mapperTextField;
    private javax.swing.JPanel middlePanel;
    private javax.swing.ButtonGroup mirroringButtonGroup;
    private javax.swing.JLabel mirroringLabel;
    private javax.swing.JComboBox<String> miscROMsComboBox;
    private javax.swing.JLabel miscROMsLabel;
    private javax.swing.JRadioButton nes20RadioButton;
    private javax.swing.JComboBox<String> peripheralComboBox;
    private javax.swing.JLabel peripheralLabel;
    private javax.swing.JComboBox prgNVRamComboBox;
    private javax.swing.JLabel prgNVRamLabel;
    private javax.swing.JComboBox prgRamComboBox;
    private javax.swing.JLabel prgRamLabel;
    private javax.swing.JComboBox prgRomComboBox;
    private javax.swing.JLabel prgRomLabel;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JButton saveAsButton;
    private javax.swing.JLabel submapperLabel;
    private javax.swing.JTextField submapperTextField;
    private javax.swing.JPanel topPanel;
    private javax.swing.JCheckBox trainerCheckBox;
    private javax.swing.ButtonGroup tvSystemButtonGroup;
    private javax.swing.JComboBox<String> tvSystemComboBox;
    private javax.swing.JLabel tvSystemLabel;
    private javax.swing.JRadioButton verticalRadioButton;
    private javax.swing.JComboBox vsHardwareComboBox;
    private javax.swing.JLabel vsHardwareLabel;
    private javax.swing.JComboBox vsPPUComboBox;
    private javax.swing.JLabel vsPPULabel;
    public EditNesHeaderDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        getRootPane().setDefaultButton(saveAsButton);

        prgRomComboBox.setPrototypeDisplayValue("None ");
        prgRamComboBox.setPrototypeDisplayValue("None ");
        prgNVRamComboBox.setPrototypeDisplayValue("Disabled ");

        chrRomComboBox.setPrototypeDisplayValue("None ");
        chrRamComboBox.setPrototypeDisplayValue("None ");
        chrNVRamComboBox.setPrototypeDisplayValue("None ");

        miscROMsComboBox.setPrototypeDisplayValue("0 ");

        tvSystemComboBox.setPrototypeDisplayValue("Multi-region ");
        consoleComboBox.setPrototypeDisplayValue(
                ExtendedConsole.toString(ExtendedConsole.VT01_MONOCHROME) + " ");
        peripheralComboBox.setPrototypeDisplayValue(DefaultExpansionDevice.toString(
                DefaultExpansionDevice.SUBOR_KEYBOARD_WITH_MOUSE_3X8_BIT_PROTOCOL)
                + " ");
        vsPPUComboBox.setPrototypeDisplayValue(
                VsPPU.toString(VsPPU.RP2C04_0004) + " ");
        vsHardwareComboBox.setPrototypeDisplayValue(VsHardware.toString(
                VsHardware.VS_DUALSYSTEM_RAID_ON_BUNGELING_BAY) + " ");
        scaleFonts(this);
        pack();
    }

    public void setEntryFileName(final String entryFileName) {
        this.entryFileName = entryFileName;
    }

    public File getSaveFile() {
        return saveFile;
    }

    public MutableNesFile getNesFile() {
        return nesFile;
    }

    public void setNesFile(final MutableNesFile nesFile) {
        this.originalNesFile = nesFile;
        restoreAllFields();
    }

    private void restoreAllFields() {
        this.nesFile = originalNesFile.copy();
        setAllFields();
    }

    private void captureAllFields() {
        nesFile.setNes20Format(nes20RadioButton.isSelected());
        nesFile.setMapperNumber(clamp(parseInt(mapperTextField.getText()),
                0, 4095));
        nesFile.setSubmapperNumber(clamp(parseInt(submapperTextField.getText()),
                0, 15));
        if (horizontalRadioButton.isSelected()) {
            nesFile.setMirroring(HORIZONTAL);
        } else if (verticalRadioButton.isSelected()) {
            nesFile.setMirroring(VERTICAL);
        } else if (fourScreenRadioButton.isSelected()) {
            nesFile.setMirroring(FOUR_SCREEN);
        }

        if (prgRomComboBox.getSelectedIndex() == 0) {
            nesFile.setPrgRomPages(0);
        } else {
            nesFile.setPrgRomPages(clamp(1 << (prgRomComboBox.getSelectedIndex() - 1),
                    0, nes20RadioButton.isSelected() ? 0xEFF : 0xFF));
        }
        if (chrRomComboBox.getSelectedIndex() == 0) {
            nesFile.setChrRomPages(0);
        } else {
            nesFile.setChrRomPages(clamp(1 << (chrRomComboBox.getSelectedIndex() - 1),
                    0, nes20RadioButton.isSelected() ? 0xEFF : 0xFF));
        }
        if (prgNVRamComboBox.getModel().getSize() == 2) {
            nesFile.setNonVolatilePrgRamPresent(
                    prgNVRamComboBox.getSelectedIndex() == 1);
            nesFile.setNonVolatilePrgRamSize(
                    nesFile.isNonVolatilePrgRamPresent() ? 0x2000 : 0);
        } else if (prgNVRamComboBox.getSelectedIndex() == 0) {
            nesFile.setNonVolatilePrgRamSize(0);
            nesFile.setNonVolatilePrgRamPresent(false);
        } else {
            nesFile.setNonVolatilePrgRamSize(
                    1 << (prgNVRamComboBox.getSelectedIndex() + 6));
            nesFile.setNonVolatilePrgRamPresent(true);
        }
        if (chrNVRamComboBox.getModel().getSize() > 0) {
            if (chrNVRamComboBox.getSelectedIndex() == 0) {
                nesFile.setNonVolatileChrRamSize(0);
            } else {
                nesFile.setNonVolatileChrRamSize(
                        1 << (chrNVRamComboBox.getSelectedIndex() + 6));
            }
        }
        if (chrRamComboBox.getModel().getSize() > 0) {
            if (chrRamComboBox.getSelectedIndex() == 0) {
                nesFile.setChrRamSize(0);
            } else {
                nesFile.setChrRamSize(1 << (chrRamComboBox.getSelectedIndex() + 6));
            }
        }

        if (prgRamComboBox.getSelectedIndex() == 0) {
            nesFile.setPrgRamPages(0);
            nesFile.setPrgRamSize(0);
        } else if (prgRamComboBox.getModel().getSize() == 10) {
            nesFile.setPrgRamPages(1 << (prgRamComboBox.getSelectedIndex() - 1));
            nesFile.setPrgRamSize(
                    nesFile.getPrgRamPages() * NesFile.PRG_RAM_PAGE_SIZE);
        } else {
            nesFile.setPrgRamSize(1 << (prgRamComboBox.getSelectedIndex() + 6));
            nesFile.setPrgRamPages(nesFile.getPrgRamSize()
                    / NesFile.PRG_RAM_PAGE_SIZE);
        }

        if (miscROMsComboBox.getModel().getSize() > 0) {
            nesFile.setMiscellaneousROMs(miscROMsComboBox.getSelectedIndex());
        }

        switch (tvSystemComboBox.getSelectedIndex()) {
            case 1:
                nesFile.setCpuPpuTiming(CpuPpuTiming.PAL);
                break;
            case 2:
                nesFile.setCpuPpuTiming(CpuPpuTiming.DENDY);
                break;
            case 3:
                nesFile.setCpuPpuTiming(CpuPpuTiming.MULTI_REGION);
                break;
            default:
                nesFile.setCpuPpuTiming(CpuPpuTiming.NTSC);
                break;
        }
        nesFile.setConsole(min(consoleComboBox.getSelectedIndex(),
                Console.EXTENDED));
        nesFile.setExtendedConsole(consoleComboBox.getSelectedIndex());
        if (peripheralComboBox.getModel().getSize() > 0) {
            nesFile.setDefaultExpansionDevice(peripheralComboBox.getSelectedIndex());
        }

        if (vsHardwareComboBox.getModel().getSize() > 0) {
            nesFile.setVsHardware(vsHardwareComboBox.getSelectedIndex());
        }
        if (vsPPUComboBox.getModel().getSize() > 0) {
            nesFile.setVsPPU(vsPPUComboBox.getSelectedIndex());
        }

        nesFile.setTrainerPresent(trainerCheckBox.isSelected());
    }

    private void setMemSizes(final JComboBox comboBox, final String[] romSizes,
                             final int romLength, final int origin) {
        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (int i = 0, len = nesFile.isNes20Format() ? 14 : 10; i < len; i++) {
            model.addElement(romSizes[i]);
        }
        comboBox.setModel(model);
        comboBox.setSelectedIndex(clamp(log2(ceilBase2(romLength)) - origin,
                0, model.getSize() - 1));
    }

    private void setMemSizes(final JComboBox comboBox, final int size) {
        final DefaultComboBoxModel model = new DefaultComboBoxModel(RAM_SIZES);
        comboBox.setModel(model);
        comboBox.setSelectedIndex(clamp(size == 0 ? 0 : log2(size) - 6, 0,
                model.getSize() - 1));
    }

    private void setAllFields() {

        nes20RadioButton.removeItemListener(itemListener);
        iNESRadioButton.removeItemListener(itemListener);
        consoleComboBox.removeItemListener(itemListener);

        setMemSizes(prgRomComboBox, MEM_SIZES_16, nesFile.getPrgRomLength(), 13);
        setMemSizes(chrRomComboBox, MEM_SIZES_8, nesFile.getChrRomLength(), 12);

        if (nesFile.isNes20Format() && nesFile.isVsSystem()) {
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (int i = 0; i < 4; i++) {
                model.addElement(VsHardware.toString(i));
            }
            vsHardwareComboBox.setEnabled(true);
            vsHardwareComboBox.setModel(model);
            setSelectedIndex(vsHardwareComboBox, nesFile.getVsHardware());

            model = new DefaultComboBoxModel();
            for (int i = 0; i < 13; i++) {
                model.addElement(VsPPU.toString(i));
            }
            vsPPUComboBox.setEnabled(true);
            vsPPUComboBox.setModel(model);
            setSelectedIndex(vsPPUComboBox, nesFile.getVsPPU());
        } else {
            vsHardwareComboBox.setEnabled(false);
            vsHardwareComboBox.setModel(new DefaultComboBoxModel());
            vsPPUComboBox.setEnabled(false);
            vsPPUComboBox.setModel(new DefaultComboBoxModel());
        }

        if (nesFile.isNes20Format()) {
            nes20RadioButton.setSelected(true);
            submapperTextField.setText(
                    Integer.toString(nesFile.getSubmapperNumber()));
            submapperTextField.setEnabled(true);
            chrRamComboBox.setEnabled(true);
            chrNVRamComboBox.setEnabled(true);
            setMemSizes(prgNVRamComboBox, nesFile.getNonVolatilePrgRamSize());
            setMemSizes(chrRamComboBox, nesFile.getChrRamSize());
            setMemSizes(chrNVRamComboBox, nesFile.getNonVolatileChrRamSize());
            setMemSizes(prgRamComboBox, nesFile.getPrgRamSize());
            tvSystemComboBox.setModel(new DefaultComboBoxModel(
                    new String[]{"NTSC", "PAL", "Dendy", "Multi-region"}));
            switch (nesFile.getCpuPpuTiming()) {
                case CpuPpuTiming.NTSC:
                    tvSystemComboBox.setSelectedIndex(0);
                    break;
                case CpuPpuTiming.PAL:
                    tvSystemComboBox.setSelectedIndex(1);
                    break;
                case CpuPpuTiming.DENDY:
                    tvSystemComboBox.setSelectedIndex(2);
                    break;
                case CpuPpuTiming.MULTI_REGION:
                    tvSystemComboBox.setSelectedIndex(3);
                    break;
            }
            consoleComboBox.setModel(new DefaultComboBoxModel(ExtendedConsole.NAMES));
            setSelectedIndex(consoleComboBox, (nesFile.getConsole()
                    == Console.EXTENDED) ? nesFile.getExtendedConsole()
                    : nesFile.getConsole());
            peripheralComboBox.setEnabled(true);
            peripheralComboBox.setModel(new DefaultComboBoxModel(
                    DefaultExpansionDevice.NAMES));
            setSelectedIndex(peripheralComboBox, nesFile.getDefaultExpansionDevice());
            miscROMsComboBox.setEnabled(true);
            miscROMsComboBox.setModel(new DefaultComboBoxModel(
                    new String[]{"0", "1", "2", "3"}));
            setSelectedIndex(miscROMsComboBox, nesFile.getMiscellaneousROMs());
        } else {
            iNESRadioButton.setSelected(true);
            submapperTextField.setText("");
            submapperTextField.setEnabled(false);
            chrRamComboBox.setEnabled(false);
            chrNVRamComboBox.setEnabled(false);
            chrRamComboBox.setModel(new DefaultComboBoxModel());
            chrNVRamComboBox.setModel(new DefaultComboBoxModel());
            prgNVRamComboBox.setModel(new DefaultComboBoxModel(
                    new String[]{"Disabled", "Enabled"}));
            setSelectedIndex(prgNVRamComboBox,
                    nesFile.isNonVolatilePrgRamPresent() ? 1 : 0);
            setMemSizes(prgRamComboBox, MEM_SIZES_8, nesFile.getPrgRamSize(), 12);
            tvSystemComboBox.setModel(new DefaultComboBoxModel(
                    new String[]{"NTSC", "PAL"}));
            setSelectedIndex(tvSystemComboBox,
                    nesFile.getCpuPpuTiming() == CpuPpuTiming.PAL ? 1 : 0);
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (int i = 0; i < 3; ++i) {
                model.addElement(Console.NAMES[i]);
            }
            consoleComboBox.setModel(model);
            setSelectedIndex(consoleComboBox, (nesFile.getConsole()
                    == Console.EXTENDED) ? 0 : nesFile.getConsole());
            peripheralComboBox.setEnabled(false);
            peripheralComboBox.setModel(new DefaultComboBoxModel());
            miscROMsComboBox.setEnabled(false);
            miscROMsComboBox.setModel(new DefaultComboBoxModel());
        }
        mapperTextField.setText(Integer.toString(nesFile.getMapperNumber()));
        switch (nesFile.getMirroring()) {
            case HORIZONTAL:
                horizontalRadioButton.setSelected(true);
                break;
            case VERTICAL:
                verticalRadioButton.setSelected(true);
                break;
            case FOUR_SCREEN:
                fourScreenRadioButton.setSelected(true);
                break;
        }
        trainerCheckBox.setSelected(nesFile.isTrainerPresent());
        nes20RadioButton.addItemListener(itemListener);
        iNESRadioButton.addItemListener(itemListener);
        consoleComboBox.addItemListener(itemListener);
    }

    private void setSelectedIndex(final JComboBox comboBox, final int index) {
        comboBox.setSelectedIndex((index < 0 || index >= comboBox.getModel()
                .getSize()) ? 0 : index);
    }

    private void selectionChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            EDT.async(() -> {
                captureAllFields();
                setAllFields();
            });
        }
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
        java.awt.GridBagConstraints gridBagConstraints;

        fileFormatButtonGroup = new javax.swing.ButtonGroup();
        mirroringButtonGroup = new javax.swing.ButtonGroup();
        tvSystemButtonGroup = new javax.swing.ButtonGroup();
        machineButtonGroup = new javax.swing.ButtonGroup();
        bottomPanel = new javax.swing.JPanel();
        CancelButton = new javax.swing.JButton();
        RestoreAllButton = new javax.swing.JButton();
        saveAsButton = new javax.swing.JButton();
        topPanel = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        mirroringLabel = new javax.swing.JLabel();
        fourScreenRadioButton = new javax.swing.JRadioButton();
        iNESRadioButton = new javax.swing.JRadioButton();
        fileFormatLabel = new javax.swing.JLabel();
        nes20RadioButton = new javax.swing.JRadioButton();
        horizontalRadioButton = new javax.swing.JRadioButton();
        mapperTextField = new javax.swing.JTextField();
        submapperLabel = new javax.swing.JLabel();
        verticalRadioButton = new javax.swing.JRadioButton();
        mapperLabel = new javax.swing.JLabel();
        submapperTextField = new javax.swing.JTextField();
        middlePanel = new javax.swing.JPanel();
        prgRomLabel = new javax.swing.JLabel();
        prgRomComboBox = new javax.swing.JComboBox();
        prgRamLabel = new javax.swing.JLabel();
        prgRamComboBox = new javax.swing.JComboBox();
        prgNVRamLabel = new javax.swing.JLabel();
        prgNVRamComboBox = new javax.swing.JComboBox();
        chrRomLabel = new javax.swing.JLabel();
        chrRamLabel = new javax.swing.JLabel();
        chrNVRamLabel = new javax.swing.JLabel();
        chrNVRamComboBox = new javax.swing.JComboBox();
        chrRamComboBox = new javax.swing.JComboBox();
        chrRomComboBox = new javax.swing.JComboBox();
        miscROMsLabel = new javax.swing.JLabel();
        miscROMsComboBox = new javax.swing.JComboBox<>();
        rightPanel = new javax.swing.JPanel();
        consoleLabel = new javax.swing.JLabel();
        vsPPULabel = new javax.swing.JLabel();
        vsPPUComboBox = new javax.swing.JComboBox();
        vsHardwareLabel = new javax.swing.JLabel();
        vsHardwareComboBox = new javax.swing.JComboBox();
        tvSystemLabel = new javax.swing.JLabel();
        tvSystemComboBox = new javax.swing.JComboBox<>();
        consoleComboBox = new javax.swing.JComboBox<>();
        peripheralLabel = new javax.swing.JLabel();
        peripheralComboBox = new javax.swing.JComboBox<>();
        trainerCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("iNES File Header Editor");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        bottomPanel.setPreferredSize(null);

        CancelButton.setMnemonic('C');
        CancelButton.setText("Cancel");
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });

        RestoreAllButton.setMnemonic('R');
        RestoreAllButton.setText("Restore All");
        RestoreAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RestoreAllButtonActionPerformed(evt);
            }
        });

        saveAsButton.setMnemonic('S');
        saveAsButton.setText("Save As...");
        saveAsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
                bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPanelLayout.createSequentialGroup()
                                .addGap(0, 167, Short.MAX_VALUE)
                                .addComponent(saveAsButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(RestoreAllButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(CancelButton)
                                .addGap(0, 0, 0))
        );

        bottomPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, CancelButton, RestoreAllButton, saveAsButton);

        bottomPanelLayout.setVerticalGroup(
                bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPanelLayout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(CancelButton)
                                        .addComponent(RestoreAllButton)
                                        .addComponent(saveAsButton))
                                .addContainerGap())
        );

        topPanel.setPreferredSize(null);

        leftPanel.setPreferredSize(null);
        leftPanel.setLayout(new java.awt.GridBagLayout());

        mirroringLabel.setText("Mirroring:");
        mirroringLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(22, 10, 0, 0);
        leftPanel.add(mirroringLabel, gridBagConstraints);

        mirroringButtonGroup.add(fourScreenRadioButton);
        fourScreenRadioButton.setText("Four-screen");
        fourScreenRadioButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 12, 7);
        leftPanel.add(fourScreenRadioButton, gridBagConstraints);

        fileFormatButtonGroup.add(iNESRadioButton);
        iNESRadioButton.setSelected(true);
        iNESRadioButton.setText("iNES");
        iNESRadioButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 10, 0, 0);
        leftPanel.add(iNESRadioButton, gridBagConstraints);

        fileFormatLabel.setText("File Format:");
        fileFormatLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
        leftPanel.add(fileFormatLabel, gridBagConstraints);

        fileFormatButtonGroup.add(nes20RadioButton);
        nes20RadioButton.setText("NES 2.0");
        nes20RadioButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        leftPanel.add(nes20RadioButton, gridBagConstraints);

        mirroringButtonGroup.add(horizontalRadioButton);
        horizontalRadioButton.setSelected(true);
        horizontalRadioButton.setText("Horizontal");
        horizontalRadioButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 10, 0, 0);
        leftPanel.add(horizontalRadioButton, gridBagConstraints);

        mapperTextField.setColumns(4);
        mapperTextField.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 10, 0, 0);
        leftPanel.add(mapperTextField, gridBagConstraints);

        submapperLabel.setText("Submapper:");
        submapperLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        leftPanel.add(submapperLabel, gridBagConstraints);

        mirroringButtonGroup.add(verticalRadioButton);
        verticalRadioButton.setText("Vertical");
        verticalRadioButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        leftPanel.add(verticalRadioButton, gridBagConstraints);

        mapperLabel.setText("Mapper:");
        mapperLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(21, 10, 0, 0);
        leftPanel.add(mapperLabel, gridBagConstraints);

        submapperTextField.setColumns(4);
        submapperTextField.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        leftPanel.add(submapperTextField, gridBagConstraints);

        middlePanel.setPreferredSize(null);
        middlePanel.setLayout(new java.awt.GridBagLayout());

        prgRomLabel.setText("PRG ROM:");
        prgRomLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 10, 0, 0);
        middlePanel.add(prgRomLabel, gridBagConstraints);

        prgRomComboBox.setFocusable(false);
        prgRomComboBox.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 4, 0, 10);
        middlePanel.add(prgRomComboBox, gridBagConstraints);

        prgRamLabel.setText("PRG RAM:");
        prgRamLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 10, 0, 0);
        middlePanel.add(prgRamLabel, gridBagConstraints);

        prgRamComboBox.setFocusable(false);
        prgRamComboBox.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 10);
        middlePanel.add(prgRamComboBox, gridBagConstraints);

        prgNVRamLabel.setText("PRG NVRAM:");
        prgNVRamLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 10, 0, 0);
        middlePanel.add(prgNVRamLabel, gridBagConstraints);

        prgNVRamComboBox.setFocusable(false);
        prgNVRamComboBox.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 10);
        middlePanel.add(prgNVRamComboBox, gridBagConstraints);

        chrRomLabel.setText("CHR ROM:");
        chrRomLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(21, 10, 0, 0);
        middlePanel.add(chrRomLabel, gridBagConstraints);

        chrRamLabel.setText("CHR RAM:");
        chrRamLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 10, 0, 0);
        middlePanel.add(chrRamLabel, gridBagConstraints);

        chrNVRamLabel.setText("CHR NVRAM:");
        chrNVRamLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 10, 0, 0);
        middlePanel.add(chrNVRamLabel, gridBagConstraints);

        chrNVRamComboBox.setFocusable(false);
        chrNVRamComboBox.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 10);
        middlePanel.add(chrNVRamComboBox, gridBagConstraints);

        chrRamComboBox.setFocusable(false);
        chrRamComboBox.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 10);
        middlePanel.add(chrRamComboBox, gridBagConstraints);

        chrRomComboBox.setFocusable(false);
        chrRomComboBox.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 4, 0, 10);
        middlePanel.add(chrRomComboBox, gridBagConstraints);

        miscROMsLabel.setText("Misc. ROMs:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(21, 10, 0, 0);
        middlePanel.add(miscROMsLabel, gridBagConstraints);

        miscROMsComboBox.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 4, 0, 10);
        middlePanel.add(miscROMsComboBox, gridBagConstraints);

        rightPanel.setPreferredSize(null);
        rightPanel.setLayout(new java.awt.GridBagLayout());

        consoleLabel.setText("Console:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 10, 0, 0);
        rightPanel.add(consoleLabel, gridBagConstraints);

        vsPPULabel.setText("VS PPU:");
        vsPPULabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(21, 10, 0, 0);
        rightPanel.add(vsPPULabel, gridBagConstraints);

        vsPPUComboBox.setFocusable(false);
        vsPPUComboBox.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 4, 0, 0);
        rightPanel.add(vsPPUComboBox, gridBagConstraints);

        vsHardwareLabel.setText("VS Hardware:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 10, 0, 0);
        rightPanel.add(vsHardwareLabel, gridBagConstraints);

        vsHardwareComboBox.setFocusable(false);
        vsHardwareComboBox.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        rightPanel.add(vsHardwareComboBox, gridBagConstraints);

        tvSystemLabel.setText("TV System:");
        tvSystemLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(14, 10, 0, 0);
        rightPanel.add(tvSystemLabel, gridBagConstraints);

        tvSystemComboBox.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 4, 0, 0);
        rightPanel.add(tvSystemComboBox, gridBagConstraints);

        consoleComboBox.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        rightPanel.add(consoleComboBox, gridBagConstraints);

        peripheralLabel.setText("Peripheral:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 10, 0, 0);
        rightPanel.add(peripheralLabel, gridBagConstraints);

        peripheralComboBox.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        rightPanel.add(peripheralComboBox, gridBagConstraints);

        trainerCheckBox.setText("Trainer");
        trainerCheckBox.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 10, 24, 0);
        rightPanel.add(trainerCheckBox, gridBagConstraints);

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
                topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(topPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(leftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(middlePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(0, 0, 0))
        );
        topPanelLayout.setVerticalGroup(
                topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(topPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(leftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                                        .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(middlePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void RestoreAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RestoreAllButtonActionPerformed
        restoreAllFields();
    }//GEN-LAST:event_RestoreAllButtonActionPerformed

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_CancelButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void saveAsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsButtonActionPerformed
        final Paths paths = AppPrefs.getInstance().getPaths();
        final File file = showSaveAsDialog(this,
                paths.getSaveEditedNesFileDir(), entryFileName, "nes",
                ImageFrame.FileExtensionFilters[1], true);
        if (file != null) {
            captureAllFields();
            paths.setSaveEditedNesFileDir(file.getParentFile().getPath());
            AppPrefs.save();
            saveFile = file;
            closeDialog();
        }
    }//GEN-LAST:event_saveAsButtonActionPerformed

    // End of variables declaration//GEN-END:variables
}
