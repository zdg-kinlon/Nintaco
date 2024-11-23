package nintaco.gui.cheats.search;

import nintaco.App;
import nintaco.Machine;
import nintaco.PauseStepType;
import nintaco.cheats.Cheat;
import nintaco.cheats.GameCheats;
import nintaco.files.CartFile;
import nintaco.gui.ToolTipsTable;
import nintaco.gui.image.ImageFrame;
import nintaco.gui.image.QuickSaveListener;
import nintaco.gui.image.QuickSaveStateInfo;
import nintaco.mappers.Mapper;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.ParseErrors.EMPTY;
import static nintaco.util.StringUtil.parseInt;

public class CheatSearchFrame extends javax.swing.JFrame {

    private final ImageFrame imageFrame;
    private final int[][] internalRAM = new int[2][0x0800];
    private final int[][] NVRAM = new int[2][0x2000];
    private final JRadioButton[] radioButtons;
    private List<QuickSaveStateInfo> quickSaveStateInfos;
    private CheatSearchTableModel cheatSearchTableModel;
    private RamTableModel ramTableModel;
    private int aValue = -1;
    private int bValue = -1;
    private final Filter[] filters = {
            (r0, r1) -> true,
            (r0, r1) -> r0 != r1,
            (r0, r1) -> r0 < r1,
            (r0, r1) -> r0 > r1,
            (r0, r1) -> r0 == aValue && r1 == bValue,
            (r0, r1) -> r0 == aValue && r0 - r1 == bValue,
            (r0, r1) -> r0 - r1 == bValue,
    };
    private Filter filter = filters[0];
    private volatile int requestR = -1;
    private volatile boolean requestNVRAM;
    private volatile boolean requestPause;
    private volatile Mapper mapper;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aLabel;
    private javax.swing.JTextField aTextField;
    private javax.swing.JRadioButton abDeltaRadioButton;
    private javax.swing.JRadioButton abRadioButton;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextField addressTextField;
    private javax.swing.JButton applyButton;
    private javax.swing.JLabel bLabel;
    private javax.swing.JTextField bTextField;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel cheatsPanel;
    private javax.swing.JScrollPane cheatsScrollPane;
    private javax.swing.JTable cheatsTable;
    private javax.swing.JLabel compareLabel;
    private javax.swing.JTextField compareTextField;
    private javax.swing.JButton deleteButton;
    private javax.swing.JRadioButton deltaRadioButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField descriptionTextField;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JRadioButton gtRadioButton;
    private javax.swing.JButton insertButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton loadButton;
    private javax.swing.JRadioButton ltRadioButton;
    private javax.swing.JRadioButton neRadioButton;
    private javax.swing.JButton nextFrameButton;
    private javax.swing.JRadioButton noRadioButton;
    private javax.swing.JCheckBox nvramCheckBox;
    private javax.swing.JButton okButton;
    private javax.swing.JToggleButton pauseButton;
    private javax.swing.JCheckBox pauseCheckBox;
    private javax.swing.JScrollPane ramScrollPane;
    private javax.swing.JTable ramTable;
    private javax.swing.JButton saveButton;
    private javax.swing.JComboBox saveSlotComboBox;
    private final QuickSaveListener quickSaveListener = this::onQuickSaveChanged;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JCheckBox showHexCheckBox;
    private javax.swing.JCheckBox snapAfterFrameCheckBox;
    private javax.swing.JButton snapR0Button;
    private javax.swing.JButton snapR1Button;
    private javax.swing.JButton updateButton;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JTextField valueTextField;
    private final DocumentListener cheatDocListener = createDocumentListener(
            this::updateCheatsButtons);

    public CheatSearchFrame(final Machine machine) {
        initComponents();

        radioButtons = new JRadioButton[]{
                noRadioButton,
                neRadioButton,
                ltRadioButton,
                gtRadioButton,
                abRadioButton,
                abDeltaRadioButton,
                deltaRadioButton,
        };
        for (JRadioButton radioButton : radioButtons) {
            radioButton.addActionListener(e -> updateRamTableModel());
        }

        imageFrame = App.getImageFrame();
        imageFrame.addQuickSaveListener(quickSaveListener);

        scaleFonts(this);
        initDocumentListeners();
        initCheatsTable();
        initRamTable();

        setMachine(machine);

        getRootPane().setDefaultButton(okButton);
        pack();
        moveToImageFrameMonitor(this);
    }

    private boolean tableHasBeenReset() {
        for (int i = cheatSearchTableModel.getRowCount() - 1; i >= 0; i--) {
            if (cheatSearchTableModel.getRow(i).isEnabled()) {
                return false;
            }
        }
        final List<Cheat> cheats = GameCheats.queryCheatsDB();
        if (cheats == null || cheats.size() != cheatSearchTableModel.getRowCount()) {
            return false;
        }
        for (int i = cheats.size() - 1; i >= 0; i--) {
            if (!cheats.get(i).equals(cheatSearchTableModel.getRow(i))) {
                return false;
            }
        }
        return true;
    }

    private void resetIfEmpty() {
        if (cheatSearchTableModel.getRowCount() == 0) {
            resetCheats();
        }
    }

    private void resetCheats() {
        cheatSearchTableModel.clear();
        final List<Cheat> cheats = GameCheats.queryCheatsDB();
        if (cheats != null) {
            cheatSearchTableModel.add(cheats, false);
            cheatSearchTableModel.setModified(false);
        }
        updateCheatsButtons();
    }

    public List<Cheat> getCheats() {
        return cheatSearchTableModel.getCheatsCopy();
    }

    public void setCheats(final List<Cheat> cheats) {
        cheatSearchTableModel.setCheats(cheats);
        resetIfEmpty();
    }

    public final void setMachine(final Machine machine) {
        if (machine == null) {
            mapper = null;
        } else {
            mapper = machine.getMapper();
        }
        updateR(0, mapper);
        updateR(1, mapper);
        EventQueue.invokeLater(() -> {
            final CartFile cartFile = App.getCartFile();
            if (cartFile != null) {
                nvramCheckBox.setSelected(cartFile.isNonVolatilePrgRamPresent());
            }
            activateButtons();
            final Mapper m = mapper;
            if (m == null) {
                cheatSearchTableModel.clear();
            } else {
                setCheats(GameCheats.getCopy());
            }
            cheatTableSelectionChanged();
        });
    }

    private void updateLoadButton() {
        final int index = saveSlotComboBox.getSelectedIndex();
        if (quickSaveStateInfos != null && index < quickSaveStateInfos.size()) {
            final QuickSaveStateInfo info = quickSaveStateInfos.get(index);
            loadButton.setEnabled(info.getFile().exists());
        } else {
            loadButton.setEnabled(false);
        }
    }

    private void initDocumentListeners() {
        aTextField.getDocument().addDocumentListener(createDocumentListener(
                this::updateRamTableModel));
        bTextField.getDocument().addDocumentListener(createDocumentListener(
                this::updateRamTableModel));
        addCheatDocumentListeners();
    }

    private void addCheatDocumentListeners() {
        descriptionTextField.getDocument().addDocumentListener(cheatDocListener);
        addressTextField.getDocument().addDocumentListener(cheatDocListener);
        valueTextField.getDocument().addDocumentListener(cheatDocListener);
        compareTextField.getDocument().addDocumentListener(cheatDocListener);
    }

    private void removeCheatDocumentListeners() {
        descriptionTextField.getDocument().removeDocumentListener(cheatDocListener);
        addressTextField.getDocument().removeDocumentListener(cheatDocListener);
        valueTextField.getDocument().removeDocumentListener(cheatDocListener);
        compareTextField.getDocument().removeDocumentListener(cheatDocListener);
    }

    private void initCheatsTable() {
        cheatsScrollPane.setPreferredSize(null);
        cheatSearchTableModel = new CheatSearchTableModel(GameCheats.getCopy());
        resetIfEmpty();
        cheatsTable.setModel(cheatSearchTableModel);
        ((ToolTipsTable) cheatsTable).setColumnToolTips("Enabled", "Description");
        disableCellBorder(cheatsTable);
        forceNoClearRowSelect(cheatsTable);
        cheatsTable.getSelectionModel().addListSelectionListener(
                e -> cheatTableSelectionChanged());
        resizeCellSizes(cheatsTable, true, 20, false);
        cheatsTable.getColumnModel().getColumn(1).setPreferredWidth(1 << 20);
        cheatTableSelectionChanged();
    }

    private void initRamTable() {
        ramTableModel = new RamTableModel(new ArrayList<>());
        ramTable.setModel(ramTableModel);
        resizeCellSizes(ramTable, true, 10, false, "BBBBB", "BBBB", "BBBB");
        disableCellBorder(ramTable);
        forceNoClearRowSelect(ramTable);
    }

    private void cheatTableSelectionChanged() {
        removeCheatDocumentListeners();
        final int index = cheatsTable.getSelectedRow();
        if (index >= 0 && index < cheatSearchTableModel.getRowCount()) {
            final boolean showHex = showHexCheckBox.isSelected();
            final Cheat cheat = cheatSearchTableModel.getRow(index);
            descriptionTextField.setText(cheat.getDescription());
            addressTextField.setText(String.format("%04X", cheat.getAddress()));
            valueTextField.setText(String.format(showHex ? "%02X" : "%d",
                    cheat.getDataValue()));
            if (cheat.hasCompareValue()) {
                compareTextField.setText(String.format(showHex ? "%02X" : "%d",
                        cheat.getCompareValue()));
            } else {
                compareTextField.setText("");
            }
        }
        addCheatDocumentListeners();
        updateCheatsButtons();
    }

    private Cheat createCheat() {
        final boolean showHex = showHexCheckBox.isSelected();
        final int address = parseInt(addressTextField.getText(), true, 0xFFFF);
        final int value = parseInt(valueTextField.getText(), showHex, 0xFF);
        final int compare = parseInt(compareTextField.getText(), showHex, 0xFF);
        if (address < 0 || value < 0 || compare < EMPTY) {
            return null;
        }
        final Cheat cheat = new Cheat(address, value, compare);
        final String description = descriptionTextField.getText().trim();
        if (description.isEmpty()) {
            cheat.generateDescription();
        } else {
            cheat.setDescription(description);
        }
        return cheat;
    }

    private void aEdited() {
        aValue = parseInt(aTextField.getText(), showHexCheckBox.isSelected(), 0xFF);
        updateRadioButtons();
    }

    private void bEdited() {
        bValue = parseInt(bTextField.getText(), showHexCheckBox.isSelected(), 0xFF);
        updateRadioButtons();
    }

    private void updateRadioButtons() {
        abRadioButton.setEnabled(aValue >= 0 && bValue >= 0);
        abDeltaRadioButton.setEnabled(aValue >= 0 && bValue >= 0);
        deltaRadioButton.setEnabled(bValue >= 0);
    }

    private void activateButtons() {
        updateCheatsButtons();
        final boolean enabled = mapper != null;
        snapR0Button.setEnabled(enabled);
        snapR1Button.setEnabled(enabled);
        pauseButton.setEnabled(enabled);
        nextFrameButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        loadButton.setEnabled(enabled);
        applyButton.setEnabled(enabled);
        okButton.setEnabled(enabled);
    }

    private void updateCheatsButtons() {

        if (mapper == null) {
            insertButton.setEnabled(false);
            deleteButton.setEnabled(false);
            updateButton.setEnabled(false);
            return;
        }

        deleteButton.setEnabled(cheatSearchTableModel.getRowCount() > 0
                && cheatsTable.getSelectedRow() >= 0 && cheatsTable.getSelectedRow()
                < cheatSearchTableModel.getRowCount());

        final boolean showHex = showHexCheckBox.isSelected();
        final int address = parseInt(addressTextField.getText(), true, 0xFFFF);
        int value = EMPTY;
        int compare = EMPTY;
        if (address >= 0) {
            value = parseInt(valueTextField.getText(), showHex, 0xFF);
            if (value >= 0) {
                compare = parseInt(compareTextField.getText(), showHex, 0xFF);
                insertButton.setEnabled(compare >= EMPTY);
            } else {
                insertButton.setEnabled(false);
            }
        } else {
            insertButton.setEnabled(false);
        }

        if (deleteButton.isEnabled() && insertButton.isEnabled()) {
            final Cheat cheat = cheatSearchTableModel.getRow(
                    cheatsTable.getSelectedRow());
            updateButton.setEnabled(!(cheat.getAddress() == address
                    && cheat.getDataValue() == value
                    && ((!cheat.hasCompareValue() && compare == EMPTY)
                    || cheat.getCompareValue() == compare)
                    && cheat.getDescription().equals(
                    descriptionTextField.getText().trim())));
        } else {
            updateButton.setEnabled(false);
        }
    }

    public void destroy() {
        imageFrame.removeQuickSaveListener(quickSaveListener);
        if (pauseButton.isSelected()) {
            App.setStepPause(false);
        }
        dispose();
    }

    private void closeFrame() {
        App.destroyCheatSearchFrame();
    }

    public void update() {
        if (requestR >= 0) {
            updateR(requestR, mapper);
            requestR = -1;
        }
    }

    private void updateR(final int r, final Mapper mapper) {
        final int[] ram = internalRAM[r];
        for (int i = 0x07FF; i >= 0; i--) {
            ram[i] = mapper == null ? 0 : mapper.peekCpuMemory(i);
        }
        if (requestNVRAM) {
            final int[] nvram = NVRAM[r];
            for (int i = 0x1FFF; i >= 0; i--) {
                nvram[i] = mapper == null ? 0 : mapper.peekCpuMemory(0x6000 | i);
            }
        }
        rUpdated();
    }

    private void rUpdated() {
        if (EventQueue.isDispatchThread()) {
            if (requestPause) {
                App.setStepPause(true);
                requestPause = false;
            }
            updateRamTableModel();
        } else {
            EventQueue.invokeLater(this::rUpdated);
        }
    }

    private void updateRamTableModel() {

        updateFilter();

        int firstAddress = 0;
        final int firstIndex = getFirstVisibleRowIndex(ramTable);

        if (firstIndex >= 0 && firstIndex < ramTableModel.getRowCount()) {
            final RamTableRow row = ramTableModel.getRow(firstIndex);
            firstAddress = row.getAddress();
        }

        int bestIndex = 0;
        int diff = Integer.MAX_VALUE;
        List<RamTableRow> rows = new ArrayList<>();
        for (int i = 0; i < 0x0800; i++) {
            final int r0 = internalRAM[0][i];
            final int r1 = internalRAM[1][i];
            if (filter.accept(r0, r1)) {
                rows.add(new RamTableRow(i, r0, r1));
                final int d = Math.abs(i - firstAddress);
                if (d < diff) {
                    diff = d;
                    bestIndex = rows.size() - 1;
                }
            }
        }
        if (nvramCheckBox.isSelected()) {
            for (int i = 0; i < 0x2000; i++) {
                final int r0 = NVRAM[0][i];
                final int r1 = NVRAM[1][i];
                if (filter.accept(r0, r1)) {
                    final int address = 0x6000 | i;
                    rows.add(new RamTableRow(address, r0, r1));
                    final int d = Math.abs(address - firstAddress);
                    if (d < diff) {
                        diff = d;
                        bestIndex = rows.size() - 1;
                    }
                }
            }
        }
        ramTableModel.setRows(rows);

        scrollToRowIndex(ramTable, bestIndex);
    }

    private void updateFilter() {
        aEdited();
        bEdited();
        filter = filters[0];
        for (int i = radioButtons.length - 1; i >= 0; i--) {
            if (radioButtons[i].isSelected()) {
                filter = filters[i];
                break;
            }
        }
    }

    private void requestSnap(int index) {
        requestNVRAM = nvramCheckBox.isSelected();
        if (pauseButton.isSelected()) {
            updateR(index, mapper);
        } else if (requestR < 0) {
            requestPause = pauseCheckBox.isSelected() && !pauseButton.isSelected();
            requestR = index;
        }
    }

    private void applyCheats() {
        if (!cheatSearchTableModel.isModified() || tableHasBeenReset()) {
            cheatSearchTableModel.clear();
        }
        GameCheats.setCheats(cheatSearchTableModel.getCheatsCopy());
        GameCheats.save();
        GameCheats.updateMachine();
    }

    public void onStepPausedChanged(boolean paused) {
        pauseButton.setText(paused ? "Resume" : "Pause");
        pauseButton.setSelected(paused);
        nextFrameButton.setEnabled(paused);
    }

    private void onQuickSaveChanged(
            List<QuickSaveStateInfo> quickSaveStateInfos) {
        this.quickSaveStateInfos = quickSaveStateInfos;
        updateLoadButton();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        cheatsPanel = new javax.swing.JPanel();
        cheatsScrollPane = new javax.swing.JScrollPane();
        cheatsTable = new ToolTipsTable();
        insertButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        descriptionTextField = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        addressLabel = new javax.swing.JLabel();
        addressTextField = new javax.swing.JTextField();
        valueLabel = new javax.swing.JLabel();
        valueTextField = new javax.swing.JTextField();
        compareLabel = new javax.swing.JLabel();
        compareTextField = new javax.swing.JTextField();
        searchPanel = new javax.swing.JPanel();
        ramScrollPane = new javax.swing.JScrollPane();
        ramTable = new ToolTipsTable();
        filterPanel = new javax.swing.JPanel();
        noRadioButton = new javax.swing.JRadioButton();
        saveButton = new javax.swing.JButton();
        gtRadioButton = new javax.swing.JRadioButton();
        aLabel = new javax.swing.JLabel();
        saveSlotComboBox = new javax.swing.JComboBox();
        snapR1Button = new javax.swing.JButton();
        aTextField = new javax.swing.JTextField();
        nvramCheckBox = new javax.swing.JCheckBox();
        pauseCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        abRadioButton = new javax.swing.JRadioButton();
        deltaRadioButton = new javax.swing.JRadioButton();
        abDeltaRadioButton = new javax.swing.JRadioButton();
        neRadioButton = new javax.swing.JRadioButton();
        ltRadioButton = new javax.swing.JRadioButton();
        loadButton = new javax.swing.JButton();
        bLabel = new javax.swing.JLabel();
        snapR0Button = new javax.swing.JButton();
        bTextField = new javax.swing.JTextField();
        pauseButton = new javax.swing.JToggleButton();
        nextFrameButton = new javax.swing.JButton();
        snapAfterFrameCheckBox = new javax.swing.JCheckBox();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();
        showHexCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Cheat Search");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cheatsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Cheats"));

        cheatsScrollPane.setMaximumSize(null);
        cheatsScrollPane.setMinimumSize(null);

        cheatsTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{

                }
        ));
        cheatsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        cheatsScrollPane.setViewportView(cheatsTable);

        insertButton.setMnemonic('I');
        insertButton.setText("Insert");
        insertButton.setFocusPainted(false);
        insertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertButtonActionPerformed(evt);
            }
        });

        updateButton.setMnemonic('U');
        updateButton.setText("Update");
        updateButton.setFocusPainted(false);
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        deleteButton.setMnemonic('D');
        deleteButton.setText("Delete");
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        descriptionTextField.setMinimumSize(null);
        descriptionTextField.setPreferredSize(null);

        descriptionLabel.setText("Description:");

        addressLabel.setText("Address:");

        addressTextField.setColumns(7);

        valueLabel.setText("Value:");

        valueTextField.setColumns(5);
        valueTextField.setPreferredSize(null);

        compareLabel.setText("Compare:");

        compareTextField.setColumns(5);
        compareTextField.setPreferredSize(null);

        javax.swing.GroupLayout cheatsPanelLayout = new javax.swing.GroupLayout(cheatsPanel);
        cheatsPanel.setLayout(cheatsPanelLayout);
        cheatsPanelLayout.setHorizontalGroup(
                cheatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(cheatsPanelLayout.createSequentialGroup()
                                .addGroup(cheatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(cheatsPanelLayout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addComponent(insertButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(updateButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(deleteButton)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(cheatsPanelLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(cheatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(cheatsPanelLayout.createSequentialGroup()
                                                                .addGroup(cheatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(addressLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(valueLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(compareLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(cheatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(descriptionTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                        .addGroup(cheatsPanelLayout.createSequentialGroup()
                                                                                .addGroup(cheatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(addressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addComponent(valueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addComponent(compareTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                .addGap(0, 0, Short.MAX_VALUE))))
                                                        .addComponent(cheatsScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addContainerGap())
        );

        cheatsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, deleteButton, insertButton, updateButton);

        cheatsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, compareTextField, valueTextField);

        cheatsPanelLayout.setVerticalGroup(
                cheatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(cheatsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(cheatsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addGroup(cheatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(descriptionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(descriptionLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(cheatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(addressLabel)
                                        .addComponent(addressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(cheatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(valueLabel)
                                        .addComponent(valueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(cheatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(compareLabel)
                                        .addComponent(compareTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(cheatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(insertButton)
                                        .addComponent(updateButton)
                                        .addComponent(deleteButton))
                                .addContainerGap())
        );

        searchPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Search"));

        ramScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        ramScrollPane.setMinimumSize(null);

        ramTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{

                }
        ));
        ramScrollPane.setViewportView(ramTable);

        buttonGroup.add(noRadioButton);
        noRadioButton.setSelected(true);
        noRadioButton.setText("No search filter");
        noRadioButton.setFocusPainted(false);

        saveButton.setMnemonic('S');
        saveButton.setText("Save");
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        buttonGroup.add(gtRadioButton);
        gtRadioButton.setText("<html>R0 &gt; R1</html>");
        gtRadioButton.setFocusPainted(false);

        aLabel.setText("A:");

        saveSlotComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"}));
        saveSlotComboBox.setFocusable(false);
        saveSlotComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSlotComboBoxActionPerformed(evt);
            }
        });

        snapR1Button.setMnemonic('1');
        snapR1Button.setText("Snap R1");
        snapR1Button.setFocusPainted(false);
        snapR1Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snapR1ButtonActionPerformed(evt);
            }
        });

        aTextField.setColumns(5);
        aTextField.setMinimumSize(null);
        aTextField.setPreferredSize(null);

        nvramCheckBox.setText("View NVRAM");
        nvramCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nvramCheckBoxActionPerformed(evt);
            }
        });

        pauseCheckBox.setSelected(true);
        pauseCheckBox.setText("Pause after snap");

        jLabel1.setText("Save slot:");
        jLabel1.setToolTipText("");
        jLabel1.setMaximumSize(null);
        jLabel1.setMinimumSize(null);
        jLabel1.setPreferredSize(null);

        buttonGroup.add(abRadioButton);
        abRadioButton.setText("<html>R0 = A, R1 = B</html>");
        abRadioButton.setEnabled(false);
        abRadioButton.setFocusPainted(false);

        buttonGroup.add(deltaRadioButton);
        deltaRadioButton.setText("<html>R0 &minus; R1 = B</html>");
        deltaRadioButton.setEnabled(false);
        deltaRadioButton.setFocusPainted(false);

        buttonGroup.add(abDeltaRadioButton);
        abDeltaRadioButton.setText("<html>R0 = A, R0 &minus; R1 = B</html>");
        abDeltaRadioButton.setEnabled(false);
        abDeltaRadioButton.setFocusPainted(false);

        buttonGroup.add(neRadioButton);
        neRadioButton.setText("<html>R0 &ne; R1</html>");
        neRadioButton.setFocusPainted(false);

        buttonGroup.add(ltRadioButton);
        ltRadioButton.setText("<html>R0 &lt; R1</html>");
        ltRadioButton.setFocusPainted(false);

        loadButton.setMnemonic('L');
        loadButton.setText("Load");
        loadButton.setFocusPainted(false);
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        bLabel.setText("B:");

        snapR0Button.setMnemonic('0');
        snapR0Button.setText("Snap R0");
        snapR0Button.setFocusPainted(false);
        snapR0Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snapR0ButtonActionPerformed(evt);
            }
        });

        bTextField.setColumns(5);
        bTextField.setMinimumSize(null);
        bTextField.setPreferredSize(null);

        pauseButton.setMnemonic('P');
        pauseButton.setText("Pause");
        pauseButton.setFocusPainted(false);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        nextFrameButton.setMnemonic('F');
        nextFrameButton.setText("Frame+1");
        nextFrameButton.setFocusPainted(false);
        nextFrameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextFrameButtonActionPerformed(evt);
            }
        });

        snapAfterFrameCheckBox.setText("Snap R1 after Frame+1");

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
                filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(filterPanelLayout.createSequentialGroup()
                                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(filterPanelLayout.createSequentialGroup()
                                                .addComponent(pauseButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(nextFrameButton))
                                        .addGroup(filterPanelLayout.createSequentialGroup()
                                                .addComponent(snapR0Button)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(snapR1Button))
                                        .addComponent(deltaRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(abDeltaRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(abRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(gtRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ltRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(neRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(filterPanelLayout.createSequentialGroup()
                                                .addComponent(aLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(aTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(bLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(bTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(noRadioButton)
                                        .addComponent(nvramCheckBox)
                                        .addGroup(filterPanelLayout.createSequentialGroup()
                                                .addComponent(saveButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(loadButton))
                                        .addGroup(filterPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(saveSlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(pauseCheckBox)
                                        .addComponent(snapAfterFrameCheckBox))
                                .addContainerGap())
        );

        filterPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, loadButton, nextFrameButton, pauseButton, saveButton, snapR0Button, snapR1Button);

        filterPanelLayout.setVerticalGroup(
                filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(filterPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(aLabel)
                                        .addComponent(aTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(bLabel)
                                        .addComponent(bTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(noRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(neRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ltRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(gtRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(abRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(abDeltaRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deltaRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(snapR0Button)
                                        .addComponent(snapR1Button))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(pauseButton)
                                        .addComponent(nextFrameButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(pauseCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(snapAfterFrameCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nvramCheckBox)
                                .addGap(18, 18, 18)
                                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(saveButton)
                                        .addComponent(loadButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(saveSlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
                searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(searchPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(ramScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        searchPanelLayout.setVerticalGroup(
                searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(searchPanelLayout.createSequentialGroup()
                                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(ramScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(searchPanelLayout.createSequentialGroup()
                                                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );

        cancelButton.setMnemonic('C');
        cancelButton.setText("   Cancel   ");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setMnemonic('O');
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        applyButton.setMnemonic('A');
        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        showHexCheckBox.setText("Show hex");
        showHexCheckBox.setFocusPainted(false);
        showHexCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHexCheckBoxActionPerformed(evt);
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
                                                .addComponent(cheatsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(showHexCheckBox)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(okButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(applyButton)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, applyButton, cancelButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(cheatsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton)
                                        .addComponent(okButton)
                                        .addComponent(applyButton)
                                        .addComponent(showHexCheckBox))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void snapR0ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snapR0ButtonActionPerformed
        requestSnap(0);
    }//GEN-LAST:event_snapR0ButtonActionPerformed

    private void snapR1ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snapR1ButtonActionPerformed
        requestSnap(1);
    }//GEN-LAST:event_snapR1ButtonActionPerformed

    private void showHexCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showHexCheckBoxActionPerformed
        ramTableModel.setShowHex(showHexCheckBox.isSelected());
        cheatTableSelectionChanged();
    }//GEN-LAST:event_showHexCheckBoxActionPerformed

    private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertButtonActionPerformed
        final int index = cheatSearchTableModel.add(createCheat());
        cheatsTable.getSelectionModel().setSelectionInterval(index, index);
        updateCheatsButtons();
    }//GEN-LAST:event_insertButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        cheatSearchTableModel.update(cheatsTable.getSelectedRow(), createCheat());
        updateCheatsButtons();
    }//GEN-LAST:event_updateButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        if (cheatSearchTableModel.isEmpty()) {
            return;
        }
        final int index = cheatsTable.getSelectedRow();
        if (index >= 0 && index < cheatSearchTableModel.getRowCount()) {
            cheatSearchTableModel.removeRow(index);
        }
        updateCheatsButtons();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        applyCheats();
        closeFrame();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeFrame();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        applyCheats();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void nextFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextFrameButtonActionPerformed
        if (snapAfterFrameCheckBox.isSelected()) {
            requestSnap(1);
        }
        App.step(PauseStepType.Frame);
    }//GEN-LAST:event_nextFrameButtonActionPerformed

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        App.setStepPause(pauseButton.isSelected());
    }//GEN-LAST:event_pauseButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        imageFrame.quickSaveState(saveSlotComboBox.getSelectedIndex() + 1);
    }//GEN-LAST:event_saveButtonActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        imageFrame.quickLoadState(saveSlotComboBox.getSelectedIndex() + 1);
    }//GEN-LAST:event_loadButtonActionPerformed

    private void saveSlotComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSlotComboBoxActionPerformed
        updateLoadButton();
    }//GEN-LAST:event_saveSlotComboBoxActionPerformed

    private void nvramCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nvramCheckBoxActionPerformed
        updateRamTableModel();
    }//GEN-LAST:event_nvramCheckBoxActionPerformed
    // End of variables declaration//GEN-END:variables
}
