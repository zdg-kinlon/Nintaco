package nintaco.gui.palettes;

import nintaco.App;
import nintaco.gui.FileExtensionFilter;
import nintaco.gui.InputDialog;
import nintaco.gui.image.preferences.Paths;
import nintaco.palettes.PalettePPU;
import nintaco.palettes.PaletteUtil;
import nintaco.palettes.Palettes;
import nintaco.preferences.AppPrefs;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

import static nintaco.files.FileUtil.mkdir;
import static nintaco.palettes.PaletteUtil.*;
import static nintaco.util.GuiUtil.*;

public class PaletteOptionsDialog extends javax.swing.JDialog {

    public static final FileExtensionFilter palExtensionFilter
            = new FileExtensionFilter(0, "Palette files (*.pal)", "pal");
    private static final int MAX_PALETTE_NAME_LENGTH = 15;
    private final static PalettePPU[] palettePpuValues = {
            PalettePPU._2C02,
            PalettePPU._2C03_2C05,
            PalettePPU.RP2C04_0001,
            PalettePPU.RP2C04_0002,
            PalettePPU.RP2C04_0003,
            PalettePPU.RP2C04_0004,
    };

    private final Map<PalettePPU, String> ppuPaletteMapping = new HashMap<>();
    private final Map<String, int[]> palettes = new HashMap<>();
    private final Map<String, int[]> originalPalettes = new HashMap<>();
    private final List<String> paletteNames = new ArrayList<>();

    private final JComboBox[] comboBoxes;
    private final JComboBox[] ppuMappingComboBoxes;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> RP2C04_0001ComboBox;
    private javax.swing.JLabel RP2C04_0001Label;
    private javax.swing.JComboBox<String> RP2C04_0002ComboBox;
    private javax.swing.JLabel RP2C04_0002Label;
    private javax.swing.JComboBox<String> RP2C04_0003ComboBox;
    private javax.swing.JLabel RP2C04_0003Label;
    private javax.swing.JComboBox<String> RP2C04_0004ComboBox;
    private javax.swing.JLabel RP2C04_0004Label;
    private javax.swing.JComboBox<String> _2C02ComboBox;
    private javax.swing.JLabel _2C02Label;
    private javax.swing.JComboBox<String> _2C03and2C05ComboBox;
    private javax.swing.JLabel _2C03and2C05Label;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel colorLabel;
    private javax.swing.JLabel colorValueLabel;
    private javax.swing.JButton defaultsButton;
    private javax.swing.JButton duplicateButton;
    private javax.swing.JPanel editPanel;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton importButton;
    private javax.swing.JLabel indexLabel;
    private javax.swing.JLabel indexValueLabel;
    private javax.swing.JPanel mappingPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox<String> paletteComboBox;
    private javax.swing.JLabel paletteLabel;
    private javax.swing.JPanel palettePanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton renameButton;
    private javax.swing.JButton resetButton;
    public PaletteOptionsDialog(final Window parent) {
        super(parent);
        setModal(true);
        initComponents();

        comboBoxes = new JComboBox[]{
                paletteComboBox,
                _2C02ComboBox,
                _2C03and2C05ComboBox,
                RP2C04_0001ComboBox,
                RP2C04_0002ComboBox,
                RP2C04_0003ComboBox,
                RP2C04_0004ComboBox,
        };
        ppuMappingComboBoxes = new JComboBox[]{
                _2C02ComboBox,
                _2C03and2C05ComboBox,
                RP2C04_0001ComboBox,
                RP2C04_0002ComboBox,
                RP2C04_0003ComboBox,
                RP2C04_0004ComboBox,
        };

        initDataStructures();
        populateComboBoxes();
        setPpuMappingComboBoxes();
        makeMonospaced(indexValueLabel);
        makeMonospaced(colorValueLabel);
        scaleFonts(this);
        getRootPane().setDefaultButton(okButton);
        paletteSelected();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initDataStructures() {
        final Palettes prefs = AppPrefs.getInstance().getPalettes();
        prefs.getPpuPaletteMapping(ppuPaletteMapping);
        prefs.getPalettes(palettes);
        prefs.getPaletteNames(paletteNames);
    }

    private void populateComboBoxes() {
        for (final JComboBox comboBox : comboBoxes) {
            final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            for (final String name : paletteNames) {
                model.addElement(name);
            }
            comboBox.setModel(model);
        }
    }

    private void setPpuMappingComboBoxes() {
        for (int i = ppuMappingComboBoxes.length - 1; i >= 0; i--) {
            ppuMappingComboBoxes[i].setSelectedItem(ppuPaletteMapping
                    .get(palettePpuValues[i]));
        }
    }

    public void paletteSelectionChanged(final int index, final int color) {
        if (index < 0) {
            indexValueLabel.setText("-  ");
            colorValueLabel.setText("-      ");
        } else {
            indexValueLabel.setText(String.format("$%02X", index));
            colorValueLabel.setText(String.format("$%06X", color));
        }
    }

    private void paletteSelected() {
        final String paletteName = (String) paletteComboBox.getSelectedItem();
        ((PalettePanel) palettePanel).setPalette(palettes.get(paletteName));
        final boolean enabled = !PaletteUtil.isStandardPaletteName(paletteName);
        removeButton.setEnabled(enabled);
        renameButton.setEnabled(enabled);
    }

    private boolean validateName(String name) {
        name = name.trim();
        return name.length() > 0 && name.length() <= MAX_PALETTE_NAME_LENGTH
                && isUniqueName(name);
    }

    private boolean isUniqueName(final String name) {
        for (int i = paletteNames.size() - 1; i >= 0; i--) {
            if (paletteNames.get(i).equalsIgnoreCase(name)) {
                return false;
            }
        }
        return true;
    }

    private void saveFields() {
        final Palettes prefs = AppPrefs.getInstance().getPalettes();
        prefs.setPalettes(palettes);
        prefs.setPpuPaletteMapping(ppuPaletteMapping);
        AppPrefs.save();
    }

    private void closeDialog() {
        dispose();
    }

    private void mappingChanged(final PalettePPU palettePPU,
                                final JComboBox comboBox) {
        final String paletteName = (String) comboBox.getSelectedItem();
        ppuPaletteMapping.put(palettePPU, paletteName);
        paletteComboBox.setSelectedItem(paletteName);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mappingPanel = new javax.swing.JPanel();
        paletteLabel = new javax.swing.JLabel();
        resetButton = new javax.swing.JButton();
        paletteComboBox = new javax.swing.JComboBox<>();
        colorLabel = new javax.swing.JLabel();
        importButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        colorValueLabel = new javax.swing.JLabel();
        renameButton = new javax.swing.JButton();
        indexLabel = new javax.swing.JLabel();
        palettePanel = new PalettePanel(this);
        indexValueLabel = new javax.swing.JLabel();
        exportButton = new javax.swing.JButton();
        duplicateButton = new javax.swing.JButton();
        editPanel = new javax.swing.JPanel();
        _2C02Label = new javax.swing.JLabel();
        _2C03and2C05Label = new javax.swing.JLabel();
        RP2C04_0001Label = new javax.swing.JLabel();
        RP2C04_0002Label = new javax.swing.JLabel();
        RP2C04_0003Label = new javax.swing.JLabel();
        RP2C04_0004Label = new javax.swing.JLabel();
        _2C02ComboBox = new javax.swing.JComboBox<>();
        _2C03and2C05ComboBox = new javax.swing.JComboBox<>();
        RP2C04_0001ComboBox = new javax.swing.JComboBox<>();
        RP2C04_0002ComboBox = new javax.swing.JComboBox<>();
        RP2C04_0003ComboBox = new javax.swing.JComboBox<>();
        RP2C04_0004ComboBox = new javax.swing.JComboBox<>();
        defaultsButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Palette Options");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        mappingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Edit Palette"));

        paletteLabel.setText("Palette:");

        resetButton.setMnemonic('s');
        resetButton.setText("Reset");
        resetButton.setFocusPainted(false);
        resetButton.setRequestFocusEnabled(false);
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        paletteComboBox.setFocusable(false);
        paletteComboBox.setRequestFocusEnabled(false);
        paletteComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paletteComboBoxActionPerformed(evt);
            }
        });

        colorLabel.setText("Color:");

        importButton.setMnemonic('I');
        importButton.setText("Import...");
        importButton.setFocusPainted(false);
        importButton.setRequestFocusEnabled(false);
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        removeButton.setMnemonic('R');
        removeButton.setText("Remove");
        removeButton.setFocusPainted(false);
        removeButton.setRequestFocusEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        colorValueLabel.setText("-      ");

        renameButton.setMnemonic('n');
        renameButton.setText("Rename");
        renameButton.setFocusPainted(false);
        renameButton.setRequestFocusEnabled(false);
        renameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameButtonActionPerformed(evt);
            }
        });

        indexLabel.setText("Index:");

        palettePanel.setRequestFocusEnabled(false);

        javax.swing.GroupLayout palettePanelLayout = new javax.swing.GroupLayout(palettePanel);
        palettePanel.setLayout(palettePanelLayout);
        palettePanelLayout.setHorizontalGroup(
                palettePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        palettePanelLayout.setVerticalGroup(
                palettePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        indexValueLabel.setText("-  ");

        exportButton.setMnemonic('x');
        exportButton.setText("Export...");
        exportButton.setFocusPainted(false);
        exportButton.setRequestFocusEnabled(false);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        duplicateButton.setText("Duplicate...");
        duplicateButton.setFocusPainted(false);
        duplicateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mappingPanelLayout = new javax.swing.GroupLayout(mappingPanel);
        mappingPanel.setLayout(mappingPanelLayout);
        mappingPanelLayout.setHorizontalGroup(
                mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mappingPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(palettePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(mappingPanelLayout.createSequentialGroup()
                                                .addComponent(paletteLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(paletteComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(removeButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(renameButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(resetButton)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(mappingPanelLayout.createSequentialGroup()
                                                .addComponent(indexLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(indexValueLabel)
                                                .addGap(18, 18, 18)
                                                .addComponent(colorLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(colorValueLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(duplicateButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(importButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(exportButton)))
                                .addContainerGap())
        );

        mappingPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, duplicateButton, exportButton, importButton);

        mappingPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, removeButton, renameButton, resetButton);

        mappingPanelLayout.setVerticalGroup(
                mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mappingPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(paletteLabel)
                                        .addComponent(paletteComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(removeButton)
                                        .addComponent(renameButton)
                                        .addComponent(resetButton))
                                .addGap(18, 18, 18)
                                .addComponent(palettePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(importButton)
                                                .addComponent(exportButton)
                                                .addComponent(duplicateButton))
                                        .addGroup(mappingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(indexLabel)
                                                .addComponent(indexValueLabel)
                                                .addComponent(colorLabel)
                                                .addComponent(colorValueLabel)))
                                .addContainerGap())
        );

        editPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PPU Palette Mapping"));

        _2C02Label.setText("2C02:");

        _2C03and2C05Label.setText("2C03/2C05:");

        RP2C04_0001Label.setText("RP2C04-0001:");

        RP2C04_0002Label.setText("RP2C04-0002:");

        RP2C04_0003Label.setText("RP2C04-0003:");

        RP2C04_0004Label.setText("RP2C04-0004:");

        _2C02ComboBox.setFocusable(false);
        _2C02ComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _2C02ComboBoxActionPerformed(evt);
            }
        });

        _2C03and2C05ComboBox.setFocusable(false);
        _2C03and2C05ComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _2C03and2C05ComboBoxActionPerformed(evt);
            }
        });

        RP2C04_0001ComboBox.setFocusable(false);
        RP2C04_0001ComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RP2C04_0001ComboBoxActionPerformed(evt);
            }
        });

        RP2C04_0002ComboBox.setFocusable(false);
        RP2C04_0002ComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RP2C04_0002ComboBoxActionPerformed(evt);
            }
        });

        RP2C04_0003ComboBox.setFocusable(false);
        RP2C04_0003ComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RP2C04_0003ComboBoxActionPerformed(evt);
            }
        });

        RP2C04_0004ComboBox.setFocusable(false);
        RP2C04_0004ComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RP2C04_0004ComboBoxActionPerformed(evt);
            }
        });

        defaultsButton.setMnemonic('D');
        defaultsButton.setText("Restore Defaults");
        defaultsButton.setFocusPainted(false);
        defaultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout editPanelLayout = new javax.swing.GroupLayout(editPanel);
        editPanel.setLayout(editPanelLayout);
        editPanelLayout.setHorizontalGroup(
                editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(editPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(_2C02Label)
                                        .addComponent(_2C03and2C05Label)
                                        .addComponent(RP2C04_0001Label)
                                        .addComponent(RP2C04_0002Label)
                                        .addComponent(RP2C04_0003Label)
                                        .addComponent(RP2C04_0004Label))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(_2C02ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(_2C03and2C05ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(RP2C04_0001ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(RP2C04_0002ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(RP2C04_0003ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(RP2C04_0004ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editPanelLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(defaultsButton))
        );
        editPanelLayout.setVerticalGroup(
                editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(editPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(_2C02Label)
                                        .addComponent(_2C02ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(_2C03and2C05Label)
                                        .addComponent(_2C03and2C05ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(RP2C04_0001Label)
                                        .addComponent(RP2C04_0001ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(RP2C04_0002Label)
                                        .addComponent(RP2C04_0002ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(RP2C04_0003Label)
                                        .addComponent(RP2C04_0003ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(RP2C04_0004Label)
                                        .addComponent(RP2C04_0004ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addComponent(defaultsButton)
                                .addContainerGap())
        );

        okButton.setMnemonic('O');
        okButton.setText("OK");
        okButton.setFocusPainted(false);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setMnemonic('C');
        cancelButton.setText(" Cancel ");
        cancelButton.setFocusPainted(false);
        cancelButton.setRequestFocusEnabled(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(editPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(mappingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(okButton)
                                                .addGap(8, 8, 8)
                                                .addComponent(cancelButton)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(editPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(mappingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(okButton)
                                        .addComponent(cancelButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void paletteComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paletteComboBoxActionPerformed
        paletteSelected();
    }//GEN-LAST:event_paletteComboBoxActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        final String paletteName = (String) paletteComboBox.getSelectedItem();
        final int[] pal = palettes.get(paletteName);
        if (!AppPrefs.getInstance().getPalettes().getOriginalPalette(paletteName,
                pal)) {
            final int[] palette = originalPalettes.get(paletteName);
            if (palette != null) {
                System.arraycopy(palette, 0, pal, 0, 64);
            }
        }
        paletteSelected();
        repaint();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void defaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultsButtonActionPerformed
        getDefaultMapping(ppuPaletteMapping);
        setPpuMappingComboBoxes();
    }//GEN-LAST:event_defaultsButtonActionPerformed

    private void duplicateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateButtonActionPerformed
        final InputDialog dialog = new InputDialog(this,
                "Please provide a name for the new palette:", "Duplicate Palette");
        dialog.setTextRequired();
        dialog.setInputValidator(this::validateName);
        dialog.setMaxInputLength(MAX_PALETTE_NAME_LENGTH);
        dialog.setVisible(true);
        if (dialog.isOk()) {
            final String name = dialog.getInput();
            {
                final int[] palette = new int[64];
                System.arraycopy(palettes.get((String) paletteComboBox
                        .getSelectedItem()), 0, palette, 0, 64);
                palettes.put(name, palette);
            }
            {
                final int[] palette = new int[64];
                System.arraycopy(palettes.get((String) paletteComboBox
                        .getSelectedItem()), 0, palette, 0, 64);
                originalPalettes.put(name, palette);
            }
            paletteNames.add(name);
            Collections.sort(paletteNames, String.CASE_INSENSITIVE_ORDER);
            populateComboBoxes();
            setPpuMappingComboBoxes();
            paletteComboBox.setSelectedItem(name);
            paletteSelected();
        }
    }//GEN-LAST:event_duplicateButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        final String name = (String) paletteComboBox.getSelectedItem();
        if (isStandardPaletteName(name)) {
            return;
        }
        for (final PalettePPU palettePPU : PalettePPU.values()) {
            if (ppuPaletteMapping.get(palettePPU).equals(name)) {
                ppuPaletteMapping.put(palettePPU, getDefaultName(palettePPU));
            }
        }
        paletteNames.remove(name);
        palettes.remove(name);
        populateComboBoxes();
        setPpuMappingComboBoxes();
        paletteSelected();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void _2C02ComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__2C02ComboBoxActionPerformed
        mappingChanged(PalettePPU._2C02, _2C02ComboBox);
    }//GEN-LAST:event__2C02ComboBoxActionPerformed

    private void _2C03and2C05ComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__2C03and2C05ComboBoxActionPerformed
        mappingChanged(PalettePPU._2C03_2C05, _2C03and2C05ComboBox);
    }//GEN-LAST:event__2C03and2C05ComboBoxActionPerformed

    private void RP2C04_0001ComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RP2C04_0001ComboBoxActionPerformed
        mappingChanged(PalettePPU.RP2C04_0001, RP2C04_0001ComboBox);
    }//GEN-LAST:event_RP2C04_0001ComboBoxActionPerformed

    private void RP2C04_0002ComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RP2C04_0002ComboBoxActionPerformed
        mappingChanged(PalettePPU.RP2C04_0002, RP2C04_0002ComboBox);
    }//GEN-LAST:event_RP2C04_0002ComboBoxActionPerformed

    private void RP2C04_0003ComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RP2C04_0003ComboBoxActionPerformed
        mappingChanged(PalettePPU.RP2C04_0003, RP2C04_0003ComboBox);
    }//GEN-LAST:event_RP2C04_0003ComboBoxActionPerformed

    private void RP2C04_0004ComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RP2C04_0004ComboBoxActionPerformed
        mappingChanged(PalettePPU.RP2C04_0004, RP2C04_0004ComboBox);
    }//GEN-LAST:event_RP2C04_0004ComboBoxActionPerformed

    private void renameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameButtonActionPerformed
        final InputDialog dialog = new InputDialog(this,
                "Please provide a new name for this palette:", "Rename Palette");
        dialog.setTextRequired();
        dialog.setInputValidator(this::validateName);
        dialog.setMaxInputLength(MAX_PALETTE_NAME_LENGTH);
        dialog.setVisible(true);
        if (dialog.isOk()) {
            final String oldName = (String) paletteComboBox.getSelectedItem();
            final String newName = dialog.getInput();
            paletteNames.remove(oldName);
            paletteNames.add(newName);
            Collections.sort(paletteNames, String.CASE_INSENSITIVE_ORDER);
            for (final PalettePPU palettePPU : PalettePPU.values()) {
                if (ppuPaletteMapping.get(palettePPU).equals(oldName)) {
                    ppuPaletteMapping.put(palettePPU, newName);
                }
            }
            palettes.put(newName, palettes.remove(oldName));
            populateComboBoxes();
            setPpuMappingComboBoxes();
            paletteComboBox.setSelectedItem(newName);
            paletteSelected();
        }
    }//GEN-LAST:event_renameButtonActionPerformed

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        final Paths paths = AppPrefs.getInstance().getPaths();
        final String palettesDir = paths.getPalettesDir();
        mkdir(palettesDir);

        final JFileChooser chooser = createFileChooser("Import Palette",
                palettesDir, palExtensionFilter);
        if (showOpenDialog(this, chooser, (p, d) -> p.setPalettesDir(d))
                == JFileChooser.APPROVE_OPTION) {
            final int[] loadedPalette;
            try {
                loadedPalette = loadPalette(chooser.getSelectedFile());
            } catch (final Throwable t) {
                //t.printStackTrace();
                displayError(this, "Failed to read palette file.");
                return;
            }

            final InputDialog dialog = new InputDialog(this,
                    "Please provide a name for the new palette:", "Import Palette");
            dialog.setTextRequired();
            dialog.setInputValidator(this::validateName);
            dialog.setMaxInputLength(MAX_PALETTE_NAME_LENGTH);
            dialog.setVisible(true);
            if (dialog.isOk()) {
                final String name = dialog.getInput();
                palettes.put(name, loadedPalette);
                {
                    final int[] palette = new int[64];
                    System.arraycopy(loadedPalette, 0, palette, 0, 64);
                    originalPalettes.put(name, palette);
                }
                paletteNames.add(name);
                Collections.sort(paletteNames, String.CASE_INSENSITIVE_ORDER);
                populateComboBoxes();
                setPpuMappingComboBoxes();
                paletteComboBox.setSelectedItem(name);
                paletteSelected();
            }
        }
    }//GEN-LAST:event_importButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        final String name = (String) paletteComboBox.getSelectedItem();
        final Paths paths = AppPrefs.getInstance().getPaths();
        final String fileName = name + ".pal";
        final String palettesDir = paths.getPalettesDir();

        mkdir(palettesDir);
        final File file = showSaveAsDialog(this, palettesDir,
                fileName, "pal", palExtensionFilter, true, "Export Palette File");
        if (file != null) {
            final String dir = file.getParent();
            paths.addRecentDirectory(dir);
            paths.setPalettesDir(dir);
            AppPrefs.save();

            try {
                savePalette(palettes.get(name), file);
            } catch (final Throwable t) {
                //t.printStackTrace();
                displayError(this, "Failed to write palette file.");
            }
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        saveFields();
        PaletteUtil.update();
        App.getImageFrame().createPaletteMenu();
        closeDialog();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing
    // End of variables declaration//GEN-END:variables
}
