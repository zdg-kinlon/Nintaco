package nintaco.gui.input.ports;

import nintaco.App;
import nintaco.gui.input.buttonmapping.ButtonMappingDialog;
import nintaco.input.DeviceDescriptor;
import nintaco.input.InputUtil;
import nintaco.input.Inputs;
import nintaco.input.Ports;
import nintaco.input.other.SetPorts;
import nintaco.preferences.AppPrefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import static nintaco.input.ConsoleType.NES;
import static nintaco.input.ConsoleType.VsDualSystem;
import static nintaco.input.InputDevices.*;
import static nintaco.input.Ports.*;
import static nintaco.util.GuiUtil.scaleFonts;
import static nintaco.util.MathUtil.clamp;

public class PortsDialog extends javax.swing.JDialog {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoConfigureCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel cardPanel;
    private javax.swing.JComboBox consoleComboBox;
    private javax.swing.JButton famicomConfigureExpansionPortButton;
    private javax.swing.JButton famicomConfigurePort1Button;
    private javax.swing.JButton famicomConfigurePort2Button;
    private javax.swing.JButton famicomConfigureTap1Button;
    private javax.swing.JButton famicomConfigureTap2Button;
    private javax.swing.JButton famicomConfigureTap3Button;
    private javax.swing.JButton famicomConfigureTap4Button;
    private javax.swing.JComboBox famicomExpansionPortComboBox;
    private javax.swing.JLabel famicomExpansionPortLabel;
    private javax.swing.JPanel famicomMultitapPanel;
    private javax.swing.JPanel famicomPanel;
    private javax.swing.JComboBox famicomPort1ComboBox;
    private javax.swing.JLabel famicomPort1Label;
    private javax.swing.JComboBox famicomPort2ComboBox;
    private javax.swing.JLabel famicomPort2Label;
    private javax.swing.JComboBox famicomTap1ComboBox;
    private javax.swing.JLabel famicomTap1Label;
    private javax.swing.JComboBox famicomTap2ComboBox;
    private javax.swing.JLabel famicomTap2Label;
    private javax.swing.JComboBox famicomTap3ComboBox;
    private javax.swing.JLabel famicomTap3Label;
    private javax.swing.JComboBox famicomTap4ComboBox;
    private javax.swing.JLabel famicomTap4Label;
    private javax.swing.JCheckBox multitapCheckBox;
    private javax.swing.JButton nesConfigurePort1Button;
    private javax.swing.JButton nesConfigurePort2Button;
    private javax.swing.JButton nesConfigureTap1Button;
    private javax.swing.JButton nesConfigureTap2Button;
    private javax.swing.JButton nesConfigureTap3Button;
    private javax.swing.JButton nesConfigureTap4Button;
    private javax.swing.JPanel nesMultitapPanel;
    private javax.swing.JPanel nesPanel;
    private javax.swing.JComboBox nesPort1ComboBox;
    private javax.swing.JLabel nesPort1Label;
    private javax.swing.JComboBox nesPort2ComboBox;
    private javax.swing.JLabel nesPort2Label;
    private javax.swing.JComboBox nesTap1ComboBox;
    private javax.swing.JLabel nesTap1Label;
    private javax.swing.JComboBox nesTap2ComboBox;
    private javax.swing.JLabel nesTap2Label;
    private javax.swing.JComboBox nesTap3ComboBox;
    private javax.swing.JLabel nesTap3Label;
    private javax.swing.JComboBox nesTap4ComboBox;
    private javax.swing.JLabel nesTap4Label;
    private javax.swing.JButton okButton;
    private javax.swing.JButton vsDualConfigureMain1Button;
    private javax.swing.JButton vsDualConfigureMain2Button;
    private javax.swing.JButton vsDualConfigureSub1Button;
    private javax.swing.JButton vsDualConfigureSub2Button;
    private javax.swing.JComboBox vsDualMain1ComboBox;
    private javax.swing.JLabel vsDualMain1Label;
    private javax.swing.JComboBox vsDualMain2ComboBox;
    private javax.swing.JLabel vsDualMain2Label;
    private javax.swing.JComboBox vsDualSub1ComboBox;
    private javax.swing.JLabel vsDualSub1Label;
    private javax.swing.JComboBox vsDualSub2ComboBox;
    private final ActionListener componentsListener = e -> enableComponents();
    private javax.swing.JLabel vsDualSub2Label;
    private javax.swing.JPanel vsDualSystemPanel;
    public PortsDialog(final Window parent) {
        super(parent);
        setModal(true);
        initComponents();
        initComboBoxes();
        initConfigureButtons();
        loadFields();
        enableComponents();
        scaleFonts(this);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComboBoxes() {
        addDevices(nesPort1ComboBox, Gamepad1,
                None, Gamepad1, Gamepad2, Gamepad3, Gamepad4, CrazyClimberLeft,
                MiraclePiano, PowerGlove, RacerMate1, RacerMate2, UForce);
        addDevices(nesPort2ComboBox, Gamepad2,
                None, Gamepad1, Gamepad2, Gamepad3, Gamepad4, Zapper, Arkanoid,
                CrazyClimberRight, PowerPad, RacerMate1, RacerMate2, SnesMouse,
                Subor, TransformerKeyboard);

        addDevices(famicomPort1ComboBox, Gamepad1,
                Gamepad1, Gamepad2, Gamepad3, Gamepad4, CrazyClimberLeft);
        addDevices(famicomPort2ComboBox, Gamepad2,
                Gamepad1, Gamepad2, Gamepad3, Gamepad4, CrazyClimberRight);
        addDevices(famicomExpansionPortComboBox, None,
                None, Zapper, Arkanoid, BandaiHyperShot, BarcodeBattler, BattleBox,
                DataRecorder, DongdaPEC586Keyboard, DoremikkoKeyboard,
                ExcitingBoxing, Glasses, FamilyTrainerMat, Keyboard,
                KonamiHyperShot, HoriTrack, Mahjong, OekaKids, Pachinko,
                PartyTap, TapTapMat, TopRiderBike, TurboFile);

        addDevices(nesTap1ComboBox, Gamepad1,
                None, Gamepad1, Gamepad2, Gamepad3, Gamepad4);
        addDevices(nesTap2ComboBox, Gamepad2,
                None, Gamepad1, Gamepad2, Gamepad3, Gamepad4);
        addDevices(nesTap3ComboBox, Gamepad3,
                None, Gamepad1, Gamepad2, Gamepad3, Gamepad4);
        addDevices(nesTap4ComboBox, Gamepad4,
                None, Gamepad1, Gamepad2, Gamepad3, Gamepad4);

        addDevices(famicomTap1ComboBox, Gamepad1,
                Gamepad1, Gamepad2, Gamepad3, Gamepad4);
        addDevices(famicomTap2ComboBox, Gamepad2,
                Gamepad1, Gamepad2, Gamepad3, Gamepad4);
        addDevices(famicomTap3ComboBox, Gamepad3,
                Gamepad1, Gamepad2, Gamepad3, Gamepad4);
        addDevices(famicomTap4ComboBox, Gamepad4,
                Gamepad1, Gamepad2, Gamepad3, Gamepad4);

        addDevices(vsDualMain1ComboBox, Gamepad1,
                None, Gamepad1, Gamepad2, Gamepad3, Gamepad4);
        addDevices(vsDualMain2ComboBox, Gamepad2,
                None, Gamepad1, Gamepad2, Gamepad3, Gamepad4);
        addDevices(vsDualSub1ComboBox, Gamepad3,
                None, Gamepad1, Gamepad2, Gamepad3, Gamepad4);
        addDevices(vsDualSub2ComboBox, Gamepad4,
                None, Gamepad1, Gamepad2, Gamepad3, Gamepad4);
    }

    private void addDevices(final JComboBox<DeviceDescriptor> comboBox,
                            final int defaultInputDevice, final int... inputDevices) {
        comboBox.removeActionListener(componentsListener);
        final DefaultComboBoxModel<DeviceDescriptor> model
                = new DefaultComboBoxModel<>();
        int targetIndex = 0;
        for (final int inputDevice : inputDevices) {
            final DeviceDescriptor element = DeviceDescriptor
                    .getDescriptor(inputDevice);
            if (inputDevice == defaultInputDevice) {
                targetIndex = model.getSize();
            }
            model.addElement(element);
        }
        comboBox.setModel(model);
        comboBox.setSelectedIndex(targetIndex);
        comboBox.addActionListener(componentsListener);
    }

    private void initConfigureButtons() {
        addConfigListener(vsDualMain1ComboBox, vsDualConfigureMain1Button);
        addConfigListener(vsDualMain2ComboBox, vsDualConfigureMain2Button);
        addConfigListener(vsDualSub1ComboBox, vsDualConfigureSub1Button);
        addConfigListener(vsDualSub2ComboBox, vsDualConfigureSub2Button);

        addConfigListener(nesTap1ComboBox, nesConfigureTap1Button);
        addConfigListener(nesTap2ComboBox, nesConfigureTap2Button);
        addConfigListener(nesTap3ComboBox, nesConfigureTap3Button);
        addConfigListener(nesTap4ComboBox, nesConfigureTap4Button);

        addConfigListener(famicomTap1ComboBox, famicomConfigureTap1Button);
        addConfigListener(famicomTap2ComboBox, famicomConfigureTap2Button);
        addConfigListener(famicomTap3ComboBox, famicomConfigureTap3Button);
        addConfigListener(famicomTap4ComboBox, famicomConfigureTap4Button);

        addConfigListener(nesPort1ComboBox, nesConfigurePort1Button);
        addConfigListener(nesPort2ComboBox, nesConfigurePort2Button);

        addConfigListener(famicomPort1ComboBox, famicomConfigurePort1Button);
        addConfigListener(famicomPort2ComboBox, famicomConfigurePort2Button);
        addConfigListener(famicomExpansionPortComboBox,
                famicomConfigureExpansionPortButton);
    }

    private void addConfigListener(final JComboBox<DeviceDescriptor> comboBox,
                                   final JButton configButton) {
        configButton.addActionListener(e -> {
            final int inputDevice = ((DeviceDescriptor) comboBox.getSelectedItem())
                    .getInputDevice();
            if (inputDevice != None) {
                final ButtonMappingDialog dialog = new ButtonMappingDialog(this);
                dialog.setInputDevice(inputDevice);
                dialog.setVisible(true);
            }
        });
    }

    private void loadFields() {
        final Inputs inputs = AppPrefs.getInstance().getInputs();
        autoConfigureCheckBox.setSelected(inputs.isAutoConfigure());

        final Ports ports = inputs.getPorts();
        if (ports.getConsoleType() == VsDualSystem) {
            setComboBoxValue(vsDualMain1ComboBox, ports.getDevice(Main1));
            setComboBoxValue(vsDualMain2ComboBox, ports.getDevice(Main2));
            setComboBoxValue(vsDualSub1ComboBox, ports.getDevice(Sub1));
            setComboBoxValue(vsDualSub2ComboBox, ports.getDevice(Sub2));
        } else if (ports.isMultitap()) {
            if (ports.getConsoleType() == NES) {
                setComboBoxValue(nesTap1ComboBox, ports.getDevice(Tap1));
                setComboBoxValue(nesTap2ComboBox, ports.getDevice(Tap2));
                setComboBoxValue(nesTap3ComboBox, ports.getDevice(Tap3));
                setComboBoxValue(nesTap4ComboBox, ports.getDevice(Tap4));
            } else {
                setComboBoxValue(famicomTap1ComboBox, ports.getDevice(Tap1));
                setComboBoxValue(famicomTap2ComboBox, ports.getDevice(Tap2));
                setComboBoxValue(famicomTap3ComboBox, ports.getDevice(Tap3));
                setComboBoxValue(famicomTap4ComboBox, ports.getDevice(Tap4));
            }
        } else if (ports.getConsoleType() == NES) {
            setComboBoxValue(nesPort1ComboBox, ports.getDevice(Port1));
            setComboBoxValue(nesPort2ComboBox, ports.getDevice(Port2));
        } else {
            setComboBoxValue(famicomPort1ComboBox, ports.getDevice(Port1));
            setComboBoxValue(famicomPort2ComboBox, ports.getDevice(Port2));
            setComboBoxValue(famicomExpansionPortComboBox,
                    ports.getDevice(ExpansionPort));
        }

        setComboBoxIndex(consoleComboBox, ports.getConsoleType());

        multitapCheckBox.removeActionListener(componentsListener);
        multitapCheckBox.setSelected(ports.isMultitap());
        multitapCheckBox.addActionListener(componentsListener);

        enableComponents();
    }

    private void setCard(final String cardName) {
        final CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, cardName);
    }

    private void setComboBoxIndex(final JComboBox comboBox, final int index) {
        comboBox.removeActionListener(componentsListener);
        comboBox.setSelectedIndex(index);
        comboBox.addActionListener(componentsListener);
    }

    private void setComboBoxValue(final JComboBox<DeviceDescriptor> comboBox,
                                  final int inputDevice) {
        setComboBoxValue(comboBox, DeviceDescriptor.getDescriptor(inputDevice));
    }

    private void setComboBoxValue(final JComboBox<DeviceDescriptor> comboBox,
                                  final DeviceDescriptor device) {
        comboBox.removeActionListener(componentsListener);
        comboBox.setSelectedItem(device);
        comboBox.addActionListener(componentsListener);
    }

    private void enableComponents() {
        multitapCheckBox.setVisible(consoleComboBox.getSelectedIndex()
                != VsDualSystem);
        if (consoleComboBox.getSelectedIndex() == VsDualSystem) {
            setCard("vsDualSystem");
            okButton.setEnabled(isUnique(vsDualMain1ComboBox, vsDualMain2ComboBox,
                    vsDualSub1ComboBox, vsDualSub2ComboBox));
            enableButton(vsDualMain1ComboBox, vsDualConfigureMain1Button);
            enableButton(vsDualMain2ComboBox, vsDualConfigureMain2Button);
            enableButton(vsDualSub1ComboBox, vsDualConfigureSub1Button);
            enableButton(vsDualSub2ComboBox, vsDualConfigureSub2Button);
        } else if (multitapCheckBox.isSelected()) {
            if (consoleComboBox.getSelectedIndex() == NES) {
                setCard("nesMultitap");
                okButton.setEnabled(isUnique(nesTap1ComboBox, nesTap2ComboBox,
                        nesTap3ComboBox, nesTap4ComboBox));
                enableButton(nesTap1ComboBox, nesConfigureTap1Button);
                enableButton(nesTap2ComboBox, nesConfigureTap2Button);
                enableButton(nesTap3ComboBox, nesConfigureTap3Button);
                enableButton(nesTap4ComboBox, nesConfigureTap4Button);
            } else {
                setCard("famicomMultitap");
                okButton.setEnabled(isUnique(famicomTap1ComboBox, famicomTap2ComboBox,
                        famicomTap3ComboBox, famicomTap4ComboBox));
                enableButton(famicomTap1ComboBox, famicomConfigureTap1Button);
                enableButton(famicomTap2ComboBox, famicomConfigureTap2Button);
                enableButton(famicomTap3ComboBox, famicomConfigureTap3Button);
                enableButton(famicomTap4ComboBox, famicomConfigureTap4Button);
            }
        } else if (consoleComboBox.getSelectedIndex() == NES) {
            setCard("nes");
            okButton.setEnabled(isUnique(nesPort1ComboBox, nesPort2ComboBox));
            enableButton(nesPort1ComboBox, nesConfigurePort1Button);
            enableButton(nesPort2ComboBox, nesConfigurePort2Button);
        } else {
            setCard("famicom");
            okButton.setEnabled(isUnique(famicomPort1ComboBox, famicomPort2ComboBox,
                    famicomExpansionPortComboBox));
            enableButton(famicomPort1ComboBox, famicomConfigurePort1Button);
            enableButton(famicomPort2ComboBox, famicomConfigurePort2Button);
            enableButton(famicomExpansionPortComboBox,
                    famicomConfigureExpansionPortButton);
        }
    }

    private void enableButton(final JComboBox<DeviceDescriptor> comboBox,
                              final JButton button) {
        button.setEnabled(((DeviceDescriptor) comboBox.getSelectedItem())
                .getInputDevice() != None);
    }

    private boolean isUnique(final JComboBox<DeviceDescriptor>... comboBoxes) {
        final Set<DeviceDescriptor> descriptors = new HashSet<>();
        boolean deviceFound = false;
        for (final JComboBox<DeviceDescriptor> comboBox : comboBoxes) {
            final DeviceDescriptor descriptor = (DeviceDescriptor) comboBox
                    .getSelectedItem();
            if (descriptor.getInputDevice() != None) {
                deviceFound = true;
                if (descriptors.contains(descriptor)) {
                    return false;
                }
                descriptors.add(descriptor);
            }
        }
        return deviceFound;
    }

    private void saveChanges() {
        final int[][] portDevices;
        if (consoleComboBox.getSelectedIndex() == VsDualSystem) {
            portDevices = new int[][]{
                    {Main1, getDevice(vsDualMain1ComboBox)},
                    {Main2, getDevice(vsDualMain2ComboBox)},
                    {Sub1, getDevice(vsDualSub1ComboBox)},
                    {Sub2, getDevice(vsDualSub2ComboBox)},
            };
        } else if (multitapCheckBox.isSelected()) {
            if (consoleComboBox.getSelectedIndex() == NES) {
                portDevices = new int[][]{
                        {Tap1, getDevice(nesTap1ComboBox)},
                        {Tap2, getDevice(nesTap2ComboBox)},
                        {Tap3, getDevice(nesTap3ComboBox)},
                        {Tap4, getDevice(nesTap4ComboBox)},
                };
            } else {
                portDevices = new int[][]{
                        {Tap1, getDevice(famicomTap1ComboBox)},
                        {Tap2, getDevice(famicomTap2ComboBox)},
                        {Tap3, getDevice(famicomTap3ComboBox)},
                        {Tap4, getDevice(famicomTap4ComboBox)},
                };
            }
        } else if (consoleComboBox.getSelectedIndex() == NES) {
            portDevices = new int[][]{
                    {Port1, getDevice(nesPort1ComboBox)},
                    {Port2, getDevice(nesPort2ComboBox)},
            };
        } else {
            portDevices = new int[][]{
                    {Port1, getDevice(famicomPort1ComboBox)},
                    {Port2, getDevice(famicomPort2ComboBox)},
                    {ExpansionPort, getDevice(famicomExpansionPortComboBox)},
            };
        }

        final SetPorts setPorts = new SetPorts(new Ports(portDevices,
                multitapCheckBox.isSelected(), clamp(consoleComboBox.getSelectedIndex(),
                0, 2)));
        if (App.getMachineRunner() == null) {
            setPorts.run(null);
        } else {
            InputUtil.addOtherInput(setPorts);
        }

        final Inputs inputs = AppPrefs.getInstance().getInputs();
        inputs.setAutoConfigure(autoConfigureCheckBox.isSelected());
        AppPrefs.save();
    }

    private int getDevice(final JComboBox<DeviceDescriptor> comboBox) {
        return ((DeviceDescriptor) comboBox.getSelectedItem()).getInputDevice();
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
        autoConfigureCheckBox = new javax.swing.JCheckBox();
        cardPanel = new javax.swing.JPanel();
        famicomPanel = new javax.swing.JPanel();
        famicomExpansionPortLabel = new javax.swing.JLabel();
        famicomExpansionPortComboBox = new JComboBox<DeviceDescriptor>();
        famicomConfigureExpansionPortButton = new javax.swing.JButton();
        famicomPort1Label = new javax.swing.JLabel();
        famicomPort2Label = new javax.swing.JLabel();
        famicomPort1ComboBox = new JComboBox<DeviceDescriptor>();
        famicomPort2ComboBox = new JComboBox<DeviceDescriptor>();
        famicomConfigurePort1Button = new javax.swing.JButton();
        famicomConfigurePort2Button = new javax.swing.JButton();
        nesMultitapPanel = new javax.swing.JPanel();
        nesTap4ComboBox = new JComboBox<DeviceDescriptor>();
        nesConfigureTap3Button = new javax.swing.JButton();
        nesConfigureTap4Button = new javax.swing.JButton();
        nesTap1Label = new javax.swing.JLabel();
        nesTap1ComboBox = new JComboBox<DeviceDescriptor>();
        nesConfigureTap1Button = new javax.swing.JButton();
        nesTap2Label = new javax.swing.JLabel();
        nesTap2ComboBox = new JComboBox<DeviceDescriptor>();
        nesConfigureTap2Button = new javax.swing.JButton();
        nesTap3Label = new javax.swing.JLabel();
        nesTap4Label = new javax.swing.JLabel();
        nesTap3ComboBox = new JComboBox<DeviceDescriptor>();
        nesPanel = new javax.swing.JPanel();
        nesPort1Label = new javax.swing.JLabel();
        nesPort1ComboBox = new JComboBox<DeviceDescriptor>();
        nesPort2ComboBox = new JComboBox<DeviceDescriptor>();
        nesPort2Label = new javax.swing.JLabel();
        nesConfigurePort2Button = new javax.swing.JButton();
        nesConfigurePort1Button = new javax.swing.JButton();
        famicomMultitapPanel = new javax.swing.JPanel();
        famicomTap4ComboBox = new JComboBox<DeviceDescriptor>();
        famicomConfigureTap3Button = new javax.swing.JButton();
        famicomConfigureTap4Button = new javax.swing.JButton();
        famicomTap1Label = new javax.swing.JLabel();
        famicomTap1ComboBox = new JComboBox<DeviceDescriptor>();
        famicomConfigureTap1Button = new javax.swing.JButton();
        famicomTap2Label = new javax.swing.JLabel();
        famicomTap2ComboBox = new JComboBox<DeviceDescriptor>();
        famicomConfigureTap2Button = new javax.swing.JButton();
        famicomTap3Label = new javax.swing.JLabel();
        famicomTap4Label = new javax.swing.JLabel();
        famicomTap3ComboBox = new JComboBox<DeviceDescriptor>();
        vsDualSystemPanel = new javax.swing.JPanel();
        vsDualMain1Label = new javax.swing.JLabel();
        vsDualMain1ComboBox = new JComboBox<DeviceDescriptor>();
        vsDualConfigureMain1Button = new javax.swing.JButton();
        vsDualMain2Label = new javax.swing.JLabel();
        vsDualMain2ComboBox = new JComboBox<DeviceDescriptor>();
        vsDualConfigureMain2Button = new javax.swing.JButton();
        vsDualSub1Label = new javax.swing.JLabel();
        vsDualSub1ComboBox = new JComboBox<DeviceDescriptor>();
        vsDualConfigureSub1Button = new javax.swing.JButton();
        vsDualSub2Label = new javax.swing.JLabel();
        vsDualSub2ComboBox = new JComboBox<DeviceDescriptor>();
        vsDualConfigureSub2Button = new javax.swing.JButton();
        multitapCheckBox = new javax.swing.JCheckBox();
        consoleComboBox = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Ports");
        setMinimumSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cancelButton.setMnemonic('C');
        cancelButton.setText("Cancel");
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(null);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setMnemonic('O');
        okButton.setText("OK");
        okButton.setFocusPainted(false);
        okButton.setPreferredSize(null);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        autoConfigureCheckBox.setSelected(true);
        autoConfigureCheckBox.setText("Auto-configure on open");
        autoConfigureCheckBox.setFocusPainted(false);
        autoConfigureCheckBox.setPreferredSize(null);

        cardPanel.setLayout(new java.awt.CardLayout());

        famicomPanel.setName(""); // NOI18N

        famicomExpansionPortLabel.setText("Expansion:");
        famicomExpansionPortLabel.setMaximumSize(null);
        famicomExpansionPortLabel.setMinimumSize(null);
        famicomExpansionPortLabel.setPreferredSize(null);

        famicomExpansionPortComboBox.setFocusable(false);
        famicomExpansionPortComboBox.setMaximumSize(null);
        famicomExpansionPortComboBox.setMinimumSize(null);
        famicomExpansionPortComboBox.setPreferredSize(null);

        famicomConfigureExpansionPortButton.setText("Configure...");
        famicomConfigureExpansionPortButton.setFocusPainted(false);
        famicomConfigureExpansionPortButton.setPreferredSize(null);

        famicomPort1Label.setText("Port 1:");
        famicomPort1Label.setMaximumSize(null);
        famicomPort1Label.setMinimumSize(null);
        famicomPort1Label.setPreferredSize(null);

        famicomPort2Label.setText("Port 2:");
        famicomPort2Label.setMaximumSize(null);
        famicomPort2Label.setMinimumSize(null);
        famicomPort2Label.setPreferredSize(null);

        famicomPort1ComboBox.setFocusable(false);
        famicomPort1ComboBox.setMaximumSize(null);
        famicomPort1ComboBox.setMinimumSize(null);
        famicomPort1ComboBox.setPreferredSize(null);
        famicomPort1ComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                famicomPort1ComboBoxActionPerformed(evt);
            }
        });

        famicomPort2ComboBox.setFocusable(false);
        famicomPort2ComboBox.setMaximumSize(null);
        famicomPort2ComboBox.setMinimumSize(null);
        famicomPort2ComboBox.setPreferredSize(null);
        famicomPort2ComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                famicomPort2ComboBoxActionPerformed(evt);
            }
        });

        famicomConfigurePort1Button.setText("Configure...");
        famicomConfigurePort1Button.setFocusPainted(false);
        famicomConfigurePort1Button.setPreferredSize(null);

        famicomConfigurePort2Button.setText("Configure...");
        famicomConfigurePort2Button.setFocusPainted(false);
        famicomConfigurePort2Button.setPreferredSize(null);

        javax.swing.GroupLayout famicomPanelLayout = new javax.swing.GroupLayout(famicomPanel);
        famicomPanel.setLayout(famicomPanelLayout);
        famicomPanelLayout.setHorizontalGroup(
                famicomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(famicomPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(famicomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(famicomPort1Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomPort2Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomExpansionPortLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(famicomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(famicomPanelLayout.createSequentialGroup()
                                                .addGroup(famicomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(famicomPort1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(famicomPort2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(famicomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(famicomConfigurePort1Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(famicomConfigurePort2Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(famicomPanelLayout.createSequentialGroup()
                                                .addComponent(famicomExpansionPortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(famicomConfigureExpansionPortButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );

        famicomPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, famicomExpansionPortComboBox, famicomPort1ComboBox, famicomPort2ComboBox);

        famicomPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, famicomConfigureExpansionPortButton, famicomConfigurePort1Button, famicomConfigurePort2Button);

        famicomPanelLayout.setVerticalGroup(
                famicomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, famicomPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(famicomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(famicomPort1Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomPort1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomConfigurePort1Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(famicomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(famicomPort2Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomPort2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomConfigurePort2Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(famicomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(famicomExpansionPortLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomExpansionPortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomConfigureExpansionPortButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        cardPanel.add(famicomPanel, "famicom");

        nesMultitapPanel.setName(""); // NOI18N

        nesTap4ComboBox.setFocusable(false);
        nesTap4ComboBox.setMaximumSize(null);
        nesTap4ComboBox.setMinimumSize(null);
        nesTap4ComboBox.setPreferredSize(null);

        nesConfigureTap3Button.setText("Configure...");
        nesConfigureTap3Button.setFocusPainted(false);
        nesConfigureTap3Button.setFocusable(false);

        nesConfigureTap4Button.setText("Configure...");
        nesConfigureTap4Button.setFocusPainted(false);
        nesConfigureTap4Button.setFocusable(false);

        nesTap1Label.setText("Tap 1:");
        nesTap1Label.setMaximumSize(null);
        nesTap1Label.setMinimumSize(null);
        nesTap1Label.setPreferredSize(null);

        nesTap1ComboBox.setFocusable(false);
        nesTap1ComboBox.setMaximumSize(null);
        nesTap1ComboBox.setMinimumSize(null);
        nesTap1ComboBox.setPreferredSize(null);

        nesConfigureTap1Button.setText("Configure...");
        nesConfigureTap1Button.setFocusPainted(false);
        nesConfigureTap1Button.setPreferredSize(null);

        nesTap2Label.setText("Tap 2:");
        nesTap2Label.setMaximumSize(null);
        nesTap2Label.setMinimumSize(null);
        nesTap2Label.setPreferredSize(null);

        nesTap2ComboBox.setFocusable(false);
        nesTap2ComboBox.setMaximumSize(null);
        nesTap2ComboBox.setMinimumSize(null);
        nesTap2ComboBox.setPreferredSize(null);

        nesConfigureTap2Button.setText("Configure...");
        nesConfigureTap2Button.setFocusPainted(false);
        nesConfigureTap2Button.setPreferredSize(null);

        nesTap3Label.setText("Tap 3:");
        nesTap3Label.setMaximumSize(null);
        nesTap3Label.setMinimumSize(null);
        nesTap3Label.setPreferredSize(null);

        nesTap4Label.setText("Tap 4:");
        nesTap4Label.setMaximumSize(null);
        nesTap4Label.setMinimumSize(null);
        nesTap4Label.setPreferredSize(null);

        nesTap3ComboBox.setFocusable(false);
        nesTap3ComboBox.setMaximumSize(null);
        nesTap3ComboBox.setMinimumSize(null);
        nesTap3ComboBox.setPreferredSize(null);

        javax.swing.GroupLayout nesMultitapPanelLayout = new javax.swing.GroupLayout(nesMultitapPanel);
        nesMultitapPanel.setLayout(nesMultitapPanelLayout);
        nesMultitapPanelLayout.setHorizontalGroup(
                nesMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nesMultitapPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(nesMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(nesTap4Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesTap3Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesTap2Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesTap1Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(nesMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(nesMultitapPanelLayout.createSequentialGroup()
                                                .addComponent(nesTap1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(nesConfigureTap1Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(nesMultitapPanelLayout.createSequentialGroup()
                                                .addComponent(nesTap2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(nesConfigureTap2Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(nesMultitapPanelLayout.createSequentialGroup()
                                                .addComponent(nesTap3ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(nesConfigureTap3Button))
                                        .addGroup(nesMultitapPanelLayout.createSequentialGroup()
                                                .addComponent(nesTap4ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(nesConfigureTap4Button)))
                                .addContainerGap())
        );
        nesMultitapPanelLayout.setVerticalGroup(
                nesMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nesMultitapPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(nesMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(nesTap1Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesTap1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesConfigureTap1Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(nesMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(nesTap2Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesTap2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesConfigureTap2Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(nesMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(nesTap3Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesTap3ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesConfigureTap3Button))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(nesMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(nesTap4Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesTap4ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesConfigureTap4Button))
                                .addContainerGap())
        );

        cardPanel.add(nesMultitapPanel, "nesMultitap");

        nesPanel.setName(""); // NOI18N

        nesPort1Label.setText("Port 1:");
        nesPort1Label.setMaximumSize(null);
        nesPort1Label.setMinimumSize(null);
        nesPort1Label.setPreferredSize(null);

        nesPort1ComboBox.setFocusable(false);
        nesPort1ComboBox.setMaximumSize(null);
        nesPort1ComboBox.setMinimumSize(null);
        nesPort1ComboBox.setPreferredSize(null);
        nesPort1ComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nesPort1ComboBoxActionPerformed(evt);
            }
        });

        nesPort2ComboBox.setFocusable(false);
        nesPort2ComboBox.setMaximumSize(null);
        nesPort2ComboBox.setMinimumSize(null);
        nesPort2ComboBox.setPreferredSize(null);
        nesPort2ComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nesPort2ComboBoxActionPerformed(evt);
            }
        });

        nesPort2Label.setText("Port 2:");
        nesPort2Label.setMaximumSize(null);
        nesPort2Label.setMinimumSize(null);
        nesPort2Label.setPreferredSize(null);

        nesConfigurePort2Button.setText("Configure...");
        nesConfigurePort2Button.setFocusPainted(false);
        nesConfigurePort2Button.setPreferredSize(null);

        nesConfigurePort1Button.setText("Configure...");
        nesConfigurePort1Button.setFocusPainted(false);
        nesConfigurePort1Button.setPreferredSize(null);

        javax.swing.GroupLayout nesPanelLayout = new javax.swing.GroupLayout(nesPanel);
        nesPanel.setLayout(nesPanelLayout);
        nesPanelLayout.setHorizontalGroup(
                nesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(nesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(nesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(nesPort1Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesPort2Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(nesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(nesPort1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesPort2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(nesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(nesConfigurePort1Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesConfigurePort2Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        nesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, nesPort1ComboBox, nesPort2ComboBox);

        nesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, nesConfigurePort1Button, nesConfigurePort2Button);

        nesPanelLayout.setVerticalGroup(
                nesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(nesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(nesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(nesPort1Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesPort1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesConfigurePort1Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(nesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(nesPort2Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesPort2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(nesConfigurePort2Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        cardPanel.add(nesPanel, "nes");

        famicomMultitapPanel.setName(""); // NOI18N

        famicomTap4ComboBox.setFocusable(false);
        famicomTap4ComboBox.setMaximumSize(null);
        famicomTap4ComboBox.setMinimumSize(null);
        famicomTap4ComboBox.setPreferredSize(null);

        famicomConfigureTap3Button.setText("Configure...");
        famicomConfigureTap3Button.setFocusPainted(false);
        famicomConfigureTap3Button.setPreferredSize(null);

        famicomConfigureTap4Button.setText("Configure...");
        famicomConfigureTap4Button.setFocusPainted(false);
        famicomConfigureTap4Button.setPreferredSize(null);

        famicomTap1Label.setText("Tap 1:");
        famicomTap1Label.setMaximumSize(null);
        famicomTap1Label.setMinimumSize(null);
        famicomTap1Label.setPreferredSize(null);

        famicomTap1ComboBox.setFocusable(false);
        famicomTap1ComboBox.setMaximumSize(null);
        famicomTap1ComboBox.setMinimumSize(null);
        famicomTap1ComboBox.setPreferredSize(null);

        famicomConfigureTap1Button.setText("Configure...");
        famicomConfigureTap1Button.setFocusPainted(false);
        famicomConfigureTap1Button.setPreferredSize(null);

        famicomTap2Label.setText("Tap 2:");
        famicomTap2Label.setMaximumSize(null);
        famicomTap2Label.setMinimumSize(null);
        famicomTap2Label.setPreferredSize(null);

        famicomTap2ComboBox.setFocusable(false);
        famicomTap2ComboBox.setMaximumSize(null);
        famicomTap2ComboBox.setMinimumSize(null);
        famicomTap2ComboBox.setPreferredSize(null);

        famicomConfigureTap2Button.setText("Configure...");
        famicomConfigureTap2Button.setFocusPainted(false);
        famicomConfigureTap2Button.setPreferredSize(null);

        famicomTap3Label.setText("Tap 3:");
        famicomTap3Label.setMaximumSize(null);
        famicomTap3Label.setMinimumSize(null);
        famicomTap3Label.setPreferredSize(null);

        famicomTap4Label.setText("Tap 4:");
        famicomTap4Label.setMaximumSize(null);
        famicomTap4Label.setMinimumSize(null);
        famicomTap4Label.setPreferredSize(null);

        famicomTap3ComboBox.setFocusable(false);
        famicomTap3ComboBox.setMaximumSize(null);
        famicomTap3ComboBox.setMinimumSize(null);
        famicomTap3ComboBox.setPreferredSize(null);

        javax.swing.GroupLayout famicomMultitapPanelLayout = new javax.swing.GroupLayout(famicomMultitapPanel);
        famicomMultitapPanel.setLayout(famicomMultitapPanelLayout);
        famicomMultitapPanelLayout.setHorizontalGroup(
                famicomMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, famicomMultitapPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(famicomMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(famicomTap4Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomTap3Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomTap2Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomTap1Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(famicomMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(famicomMultitapPanelLayout.createSequentialGroup()
                                                .addComponent(famicomTap1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(famicomConfigureTap1Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(famicomMultitapPanelLayout.createSequentialGroup()
                                                .addComponent(famicomTap2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(famicomConfigureTap2Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(famicomMultitapPanelLayout.createSequentialGroup()
                                                .addComponent(famicomTap3ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(famicomConfigureTap3Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(famicomMultitapPanelLayout.createSequentialGroup()
                                                .addComponent(famicomTap4ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(famicomConfigureTap4Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );
        famicomMultitapPanelLayout.setVerticalGroup(
                famicomMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, famicomMultitapPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(famicomMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(famicomTap1Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomTap1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomConfigureTap1Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(famicomMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(famicomTap2Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomTap2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomConfigureTap2Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(famicomMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(famicomTap3Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomTap3ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomConfigureTap3Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(famicomMultitapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(famicomTap4Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomTap4ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(famicomConfigureTap4Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        cardPanel.add(famicomMultitapPanel, "famicomMultitap");

        vsDualSystemPanel.setName(""); // NOI18N

        vsDualMain1Label.setText("Main 1:");

        vsDualMain1ComboBox.setFocusable(false);
        vsDualMain1ComboBox.setPreferredSize(null);

        vsDualConfigureMain1Button.setText("Configure...");
        vsDualConfigureMain1Button.setFocusPainted(false);

        vsDualMain2Label.setText("Main 2:");

        vsDualMain2ComboBox.setFocusable(false);
        vsDualMain2ComboBox.setPreferredSize(null);

        vsDualConfigureMain2Button.setText("Configure...");
        vsDualConfigureMain2Button.setFocusPainted(false);

        vsDualSub1Label.setText("Sub 1:");

        vsDualSub1ComboBox.setFocusable(false);
        vsDualSub1ComboBox.setPreferredSize(null);

        vsDualConfigureSub1Button.setText("Configure...");
        vsDualConfigureSub1Button.setFocusPainted(false);

        vsDualSub2Label.setText("Sub 2:");

        vsDualSub2ComboBox.setFocusable(false);
        vsDualSub2ComboBox.setPreferredSize(null);

        vsDualConfigureSub2Button.setText("Configure...");
        vsDualConfigureSub2Button.setFocusPainted(false);

        javax.swing.GroupLayout vsDualSystemPanelLayout = new javax.swing.GroupLayout(vsDualSystemPanel);
        vsDualSystemPanel.setLayout(vsDualSystemPanelLayout);
        vsDualSystemPanelLayout.setHorizontalGroup(
                vsDualSystemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(vsDualSystemPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(vsDualSystemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(vsDualMain1Label, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(vsDualMain2Label, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(vsDualSub1Label, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(vsDualSub2Label, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(vsDualSystemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(vsDualMain1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(vsDualMain2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(vsDualSub1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(vsDualSub2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(vsDualSystemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(vsDualConfigureMain1Button)
                                        .addComponent(vsDualConfigureMain2Button)
                                        .addComponent(vsDualConfigureSub1Button)
                                        .addComponent(vsDualConfigureSub2Button))
                                .addContainerGap())
        );
        vsDualSystemPanelLayout.setVerticalGroup(
                vsDualSystemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(vsDualSystemPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(vsDualSystemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(vsDualMain1Label)
                                        .addComponent(vsDualMain1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(vsDualConfigureMain1Button))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(vsDualSystemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(vsDualMain2Label)
                                        .addComponent(vsDualMain2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(vsDualConfigureMain2Button))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(vsDualSystemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(vsDualSub1Label)
                                        .addComponent(vsDualSub1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(vsDualConfigureSub1Button))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(vsDualSystemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(vsDualSub2Label)
                                        .addComponent(vsDualSub2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(vsDualConfigureSub2Button))
                                .addContainerGap())
        );

        cardPanel.add(vsDualSystemPanel, "vsDualSystem");

        multitapCheckBox.setText("Multitap");
        multitapCheckBox.setFocusPainted(false);
        multitapCheckBox.setPreferredSize(null);

        consoleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"NES", "Famicom", "VS. DualSystem"}));
        consoleComboBox.setFocusable(false);
        consoleComboBox.setPreferredSize(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10))
                        .addComponent(cardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(consoleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(multitapCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(autoConfigureCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(autoConfigureCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(consoleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(multitapCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, cancelButton, okButton);

    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        saveChanges();
        closeDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing

    private void nesPort1ComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nesPort1ComboBoxActionPerformed
        if (getDevice(nesPort1ComboBox) == CrazyClimberLeft) {
            setComboBoxValue(nesPort2ComboBox, CrazyClimberRight);
        }
    }//GEN-LAST:event_nesPort1ComboBoxActionPerformed

    private void nesPort2ComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nesPort2ComboBoxActionPerformed
        if (getDevice(nesPort2ComboBox) == CrazyClimberRight) {
            setComboBoxValue(nesPort1ComboBox, CrazyClimberLeft);
        }
    }//GEN-LAST:event_nesPort2ComboBoxActionPerformed

    private void famicomPort1ComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_famicomPort1ComboBoxActionPerformed
        if (getDevice(famicomPort1ComboBox) == CrazyClimberLeft) {
            setComboBoxValue(famicomPort2ComboBox, CrazyClimberRight);
        }
    }//GEN-LAST:event_famicomPort1ComboBoxActionPerformed

    private void famicomPort2ComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_famicomPort2ComboBoxActionPerformed
        if (getDevice(famicomPort2ComboBox) == CrazyClimberRight) {
            setComboBoxValue(famicomPort1ComboBox, CrazyClimberLeft);
        }
    }//GEN-LAST:event_famicomPort2ComboBoxActionPerformed
    // End of variables declaration//GEN-END:variables
}
