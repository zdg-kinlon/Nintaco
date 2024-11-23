package nintaco.gui.ramsearch;

import nintaco.App;
import nintaco.Machine;
import nintaco.PauseStepType;
import nintaco.cheats.Cheat;
import nintaco.gui.cheats.CheatsDialog;
import nintaco.gui.hexeditor.HexEditorFrame;
import nintaco.gui.image.ImageFrame;
import nintaco.gui.image.QuickSaveListener;
import nintaco.gui.image.QuickSaveStateInfo;
import nintaco.gui.ramwatch.RamWatchFrame;
import nintaco.gui.ramwatch.RamWatchRow;
import nintaco.mappers.Mapper;
import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

import static java.lang.Math.abs;
import static nintaco.gui.hexeditor.DataSource.CpuMemory;
import static nintaco.gui.ramsearch.ValueFormat.*;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.parseInt;

public class RamSearchFrame extends javax.swing.JFrame {

    private static final int[] SIZES = {1, 2, 4};
    private static final int[] MASKS = {0xFF, 0xFFFF, 0xFFFFFFFF};

    private static final MemoryRange[][] RANGES = {
            {new MemoryRange(0x0000, 0x07FF)},
            {new MemoryRange(0x0000, 0x07FF), new MemoryRange(0x6000, 0x7FFF)},
            {new MemoryRange(0x8000, 0xFFFF)},
    };

    private static final RamRowFilter[] FILTERS1 = {
            (c, p, s, a, A, B) -> (byte) (c - p) != 0,
            (c, p, s, a, A, B) -> (byte) (c - p) < 0,
            (c, p, s, a, A, B) -> (byte) (c - p) > 0,
            (c, p, s, a, A, B) -> (byte) (c - p) <= 0,
            (c, p, s, a, A, B) -> (byte) (c - p) >= 0,
            (c, p, s, a, A, B) -> (byte) (c - A) == 0,
            (c, p, s, a, A, B) -> (byte) (c - A) == 0 && (byte) (p - B) == 0,
            (c, p, s, a, A, B) -> (byte) (c - A) == 0 && (byte) (c - p - B) == 0,
            (c, p, s, a, A, B) -> (byte) (c - p - B) == 0,
            (c, p, s, a, A, B) -> (byte) (abs(c - p) - B) == 0,
            (c, p, s, a, A, B) -> A != 0 && (byte) (abs(c % A) - B) == 0,
            (c, p, s, a, A, B) -> a == A,
            (c, p, s, a, A, B) -> s >= A,
    };
    private static final RamRowFilter[] FILTERS2 = {
            (c, p, s, a, A, B) -> (short) (c - p) != 0,
            (c, p, s, a, A, B) -> (short) (c - p) < 0,
            (c, p, s, a, A, B) -> (short) (c - p) > 0,
            (c, p, s, a, A, B) -> (short) (c - p) <= 0,
            (c, p, s, a, A, B) -> (short) (c - p) >= 0,
            (c, p, s, a, A, B) -> (short) (c - A) == 0,
            (c, p, s, a, A, B) -> (short) (c - A) == 0 && (short) (p - B) == 0,
            (c, p, s, a, A, B) -> (short) (c - A) == 0 && (short) (c - p - B) == 0,
            (c, p, s, a, A, B) -> (short) (c - p - B) == 0,
            (c, p, s, a, A, B) -> (short) (abs(c - p) - B) == 0,
            (c, p, s, a, A, B) -> A != 0 && (short) (abs(c % A) - B) == 0,
            (c, p, s, a, A, B) -> a == A,
            (c, p, s, a, A, B) -> s >= A,
    };
    private static final RamRowFilter[] FILTERS4 = {
            (c, p, s, a, A, B) -> c - p != 0,
            (c, p, s, a, A, B) -> c - p < 0,
            (c, p, s, a, A, B) -> c - p > 0,
            (c, p, s, a, A, B) -> c - p <= 0,
            (c, p, s, a, A, B) -> c - p >= 0,
            (c, p, s, a, A, B) -> c - A == 0,
            (c, p, s, a, A, B) -> c - A == 0 && p - B == 0,
            (c, p, s, a, A, B) -> c - A == 0 && c - p - B == 0,
            (c, p, s, a, A, B) -> c - p - B == 0,
            (c, p, s, a, A, B) -> abs(c - p) - B == 0,
            (c, p, s, a, A, B) -> A != 0 && abs(c % A) - B == 0,
            (c, p, s, a, A, B) -> a == A,
            (c, p, s, a, A, B) -> s >= A,
    };
    private static final RamRowFilter[][] FILTERS
            = {FILTERS1, FILTERS2, FILTERS4};

    private static final RamRowFilter ALL_PASS_FILTER
            = (c, p, s, a, A, B) -> true;

    private final RamRow[] rows = new RamRow[0x10000];
    private final int[] memory = new int[0x10000];

    private final ImageFrame imageFrame;
    private List<QuickSaveStateInfo> quickSaveStateInfos;
    private RamSearchTableModel tableModel;
    private int selectedRowIndex = -1;
    private int saveSlotIndex;
    private int alignIndex;
    private boolean aValueAvailable;
    private boolean bValueAvailable;
    private boolean autofilter;
    private JRadioButton[] filterRadioButtons;
    private JRadioButton[] wordSizeRadioButtons;
    private JRadioButton[] formatRadioButtons;
    private JRadioButton[] memoryRangeRadioButtons;
    private volatile Mapper mapper;
    private volatile boolean resetCountsRequest;
    private volatile int memoryRange;
    private volatile int wordSizeIndex;
    private volatile int alignment = 1;
    private volatile int valueFormat;
    private volatile int aValue;
    private volatile int bValue;
    private volatile int filterIndex;
    private volatile RamRowFilter filter = ALL_PASS_FILTER;
    private volatile int[] watchAddresses;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aLabel;
    private javax.swing.JTextField aTextField;
    private javax.swing.JRadioButton abscModaEbRadioButton;
    private javax.swing.JRadioButton abscMpEbRadioButton;
    private javax.swing.JButton addCheatButton;
    private javax.swing.JRadioButton addressEaRadioButton;
    private javax.swing.JComboBox alignComboBox;
    private javax.swing.JLabel alignLabel;
    private javax.swing.JCheckBox autofilterCheckBox;
    private javax.swing.JLabel bLabel;
    private javax.swing.JTextField bTextField;
    private javax.swing.JRadioButton byte1RadioButton;
    private javax.swing.JRadioButton byte2RadioButton;
    private javax.swing.JRadioButton byte4RadioButton;
    private javax.swing.JRadioButton cEaRadioButton;
    private javax.swing.JRadioButton cEacMpEbRadioButton;
    private javax.swing.JRadioButton cEapEbRadioButton;
    private javax.swing.JRadioButton cGEpRadioButton;
    private javax.swing.JRadioButton cGTpRadioButton;
    private javax.swing.JRadioButton cLEpRadioButton;
    private javax.swing.JRadioButton cLTpRadioButton;
    private javax.swing.JRadioButton cMpEbRadioButton;
    private javax.swing.JRadioButton cNEpRadioButton;
    private javax.swing.JRadioButton changesGEaRadioButton;
    private javax.swing.JButton filterButton;
    private javax.swing.ButtonGroup filterCriteriaButtonGroup;
    private javax.swing.ButtonGroup formatButtonGroup;
    private javax.swing.JButton hexEditorButton;
    private javax.swing.JRadioButton hexRadioButton;
    private javax.swing.JButton hideRowButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton loadButton;
    private javax.swing.ButtonGroup memoryButtonGroup;
    private javax.swing.JButton nextFrameButton;
    private javax.swing.JToggleButton pauseButton;
    private javax.swing.JRadioButton ramNvramRadioButton;
    private javax.swing.JRadioButton ramRadioButton;
    private javax.swing.JButton resetCountsButton;
    private javax.swing.JRadioButton romRadioButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JComboBox saveSlotComboBox;
    private final QuickSaveListener quickSaveListener = this::onQuickSaveChanged;
    private javax.swing.JLabel saveSlotLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton showAllButton;
    private javax.swing.JButton showWatchesButton;
    private javax.swing.JRadioButton signedRadioButton;
    private javax.swing.JTable table;
    private javax.swing.JRadioButton unsignedRadioButton;
    private javax.swing.JButton watchButton;
    private javax.swing.ButtonGroup wordSizeButtonGroup;
    public RamSearchFrame(final Machine machine) {
        initComponents();
        initRadioButtons();
        initTable();
        loadFields();
        setMachine(machine);

        imageFrame = App.getImageFrame();
        imageFrame.addQuickSaveListener(quickSaveListener);

        initDocumentListeners();
        showAllRows();
        scaleFonts(this);
        updateButtons();
        pack();
        moveToImageFrameMonitor(this);
    }

    public void destroy() {
        imageFrame.removeQuickSaveListener(quickSaveListener);
        if (pauseButton.isSelected()) {
            App.setStepPause(false);
        }
        saveFields();
        dispose();
    }

    private void closeFrame() {
        App.destroyRamSearchFrame();
    }

    private void initRadioButtons() {
        filterRadioButtons = new JRadioButton[]{
                cNEpRadioButton,
                cLTpRadioButton,
                cGTpRadioButton,
                cLEpRadioButton,
                cGEpRadioButton,
                cEaRadioButton,
                cEapEbRadioButton,
                cEacMpEbRadioButton,
                cMpEbRadioButton,
                abscMpEbRadioButton,
                abscModaEbRadioButton,
                addressEaRadioButton,
                changesGEaRadioButton,
        };
        for (int i = filterRadioButtons.length - 1; i >= 0; i--) {
            final int index = i;
            filterRadioButtons[i].addActionListener(e -> setFilterIndex(index));
        }

        wordSizeRadioButtons = new JRadioButton[]{
                byte1RadioButton,
                byte2RadioButton,
                byte4RadioButton,
        };
        for (int i = wordSizeRadioButtons.length - 1; i >= 0; i--) {
            final int index = i;
            wordSizeRadioButtons[i].addActionListener(e -> setWordSizeIndex(index));
        }

        formatRadioButtons = new JRadioButton[]{
                signedRadioButton,
                unsignedRadioButton,
                hexRadioButton,
        };
        for (int i = formatRadioButtons.length - 1; i >= 0; i--) {
            final int format = i;
            formatRadioButtons[i].addActionListener(e -> setValueFormat(format));
        }

        memoryRangeRadioButtons = new JRadioButton[]{
                ramRadioButton,
                ramNvramRadioButton,
                romRadioButton,
        };
        for (int i = memoryRangeRadioButtons.length - 1; i >= 0; i--) {
            final int range = i;
            memoryRangeRadioButtons[i].addActionListener(e -> setMemoryRange(range));
        }
    }

    private void initDocumentListeners() {
        aTextField.getDocument().addDocumentListener(createDocumentListener(
                this::aTextFieldEdited));
        bTextField.getDocument().addDocumentListener(createDocumentListener(
                this::bTextFieldEdited));
        addLoseFocusListener(this, aTextField);
        addLoseFocusListener(this, bTextField);
    }

    private void initTable() {

        table.getSelectionModel().addListSelectionListener(
                e -> handleTableSelectionChanged());

        for (int i = rows.length - 1; i >= 0; i--) {
            rows[i] = new RamRow();
        }

        final List<RamSearchTableRow> rs = new ArrayList<>();
        for (int i = 0; i < 0x0800; i++) {
            rs.add(new RamSearchTableRow(i, 0, 0, 0, ValueFormat.Hex, 0));
        }
        tableModel = new RamSearchTableModel(rs);
        table.setModel(tableModel);
        final RamRowRenderer renderer = new RamRowRenderer();
        for (int i = 0; i < 4; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        resizeCellSizes(table, true, 10, false, "BBBB", "BBBBBBBBBBB",
                "BBBBBBBBBBB", "BBBBBBBBBBB");
    }

    private void handleTableSelectionChanged() {
        selectedRowIndex = table.getSelectedRow();
        updateButtons();
    }

    private void updateFilter() {
        if ((!aValueAvailable
                && (filterIndex >= 5 && filterIndex <= 7 || filterIndex >= 10))
                || (!bValueAvailable && (filterIndex >= 6 && filterIndex <= 10))) {
            filter = ALL_PASS_FILTER;
        } else {
            filter = FILTERS[wordSizeIndex][filterIndex];
        }
    }

    private void setFilterIndex(final int filterIndex) {
        this.filterIndex = filterIndex;
        updateFilter();
        updateAB();
    }

    private void setWordSizeIndex(final int wordSizeIndex) {
        this.wordSizeIndex = wordSizeIndex;
        updateFilter();
        updateAB();
        resizeTable();
    }

    private void setValueFormat(final int valueFormat) {
        this.valueFormat = valueFormat;
        updateAB();
        updateTableFormatting();
    }

    private void updateTableFormatting() {
        final List<RamSearchTableRow> rows = tableModel.getRows();
        for (int i = rows.size() - 1; i >= 0; i--) {
            final RamSearchTableRow row = rows.get(i);
            row.setWordSizeIndex(wordSizeIndex);
            row.setValueFormat(valueFormat);
        }
        tableModel.fireTableDataChanged();
    }

    private void setMemoryRange(final int memoryRange) {
        this.memoryRange = memoryRange;
        showAllRows();
    }

    private void updateAB() {
        aTextFieldEdited();
        bTextFieldEdited();
    }

    private void enableFilterRadioButtons() {
        filterRadioButtons[5].setEnabled(aValueAvailable);
        filterRadioButtons[6].setEnabled(aValueAvailable && bValueAvailable);
        filterRadioButtons[7].setEnabled(aValueAvailable && bValueAvailable);
        filterRadioButtons[8].setEnabled(bValueAvailable);
        filterRadioButtons[9].setEnabled(bValueAvailable);
        filterRadioButtons[10].setEnabled(aValueAvailable && bValueAvailable);
        filterRadioButtons[11].setEnabled(aValueAvailable);
        filterRadioButtons[12].setEnabled(aValueAvailable);
    }

    private void aTextFieldEdited() {
        final Integer value = parseTextField(aTextField);
        if (value == null) {
            aValueAvailable = false;
        } else {
            aValueAvailable = true;
            aValue = value;
        }
        enableFilterRadioButtons();
    }

    private void bTextFieldEdited() {
        final Integer value = parseTextField(bTextField);
        if (value == null) {
            bValueAvailable = false;
        } else {
            bValueAvailable = true;
            bValue = value;
        }
        enableFilterRadioButtons();
    }

    private void setTextField(JTextField textField, Integer value) {

        final String text;

        if (value == null) {
            text = "";
        } else {
            switch (filterIndex) {
                case 11:
                    text = String.format("%04X", value);
                    break;
                case 12:
                    text = Integer.toString(value);
                    break;
                default:
                    switch (wordSizeIndex) {
                        case 0:
                            switch (valueFormat) {
                                case Signed:
                                    text = Byte.toString((byte) (int) value);
                                    break;
                                case Unsigned:
                                    text = Integer.toString(value & 0xFF);
                                    break;
                                default:
                                    text = String.format("%02X", value & 0xFF);
                                    break;
                            }
                            break;
                        case 1:
                            switch (valueFormat) {
                                case Signed:
                                    text = Short.toString((short) (int) value);
                                    break;
                                case Unsigned:
                                    text = Integer.toString(value & 0xFFFF);
                                    break;
                                default:
                                    text = String.format("%04X", value & 0xFFFF);
                                    break;
                            }
                            break;
                        default:
                            switch (valueFormat) {
                                case Signed:
                                    text = Integer.toString(value);
                                    break;
                                case Unsigned:
                                    text = Integer.toUnsignedString(value);
                                    break;
                                default:
                                    text = String.format("%08X", value);
                                    break;
                            }
                            break;
                    }
                    break;
            }
        }

        textField.setText(text);
    }

    private Integer parseTextField(JTextField textField) {
        final long minValue;
        final long maxValue;
        switch (filterIndex) {
            case 11:
                minValue = 0x0000L;
                maxValue = 0xFFFFL;
                break;
            case 12:
                minValue = 0L;
                maxValue = Integer.MAX_VALUE;
                break;
            default:
                switch (wordSizeIndex) {
                    case 0:
                        if (valueFormat == Signed) {
                            minValue = Byte.MIN_VALUE;
                            maxValue = Byte.MAX_VALUE;
                        } else {
                            minValue = 0x00L;
                            maxValue = 0xFFL;
                        }
                        break;
                    case 1:
                        if (valueFormat == Signed) {
                            minValue = Short.MIN_VALUE;
                            maxValue = Short.MAX_VALUE;
                        } else {
                            minValue = 0x0000L;
                            maxValue = 0xFFFFL;
                        }
                        break;
                    default:
                        if (valueFormat == Signed) {
                            minValue = Integer.MIN_VALUE;
                            maxValue = Integer.MAX_VALUE;
                        } else {
                            minValue = 0x00000000L;
                            maxValue = 0xFFFFFFFFL;
                        }
                        break;
                }
                break;
        }
        return parseInt(textField.getText(),
                filterIndex == 11 || filterIndex != 12 && (valueFormat == Hex), minValue, maxValue);
    }

    private boolean hasWatches() {
        for (final RamWatchRow row : GamePrefs.getInstance().getRamWatchGamePrefs()
                .getRows()) {
            if (!row.isSeparator()) {
                return true;
            }
        }
        return false;
    }

    private void updateButtons() {
        if (EventQueue.isDispatchThread()) {
            final Mapper m = mapper;
            final boolean enabled = m != null;
            filterButton.setEnabled(enabled);
            showAllButton.setEnabled(enabled);
            showWatchesButton.setEnabled(enabled && hasWatches());
            resetCountsButton.setEnabled(enabled);
            autofilterCheckBox.setEnabled(enabled);
            saveSlotLabel.setEnabled(enabled);
            saveSlotComboBox.setEnabled(enabled);
            saveButton.setEnabled(enabled);
            pauseButton.setEnabled(enabled);
            final boolean rowButtonsEnabled = enabled && selectedRowIndex >= 0;
            hideRowButton.setEnabled(rowButtonsEnabled);
            watchButton.setEnabled(rowButtonsEnabled);
            addCheatButton.setEnabled(rowButtonsEnabled);
            hexEditorButton.setEnabled(rowButtonsEnabled);
            updateLoadButton();
            nextFrameButton.setEnabled(pauseButton.isEnabled()
                    && pauseButton.isSelected());
        } else {
            EventQueue.invokeLater(this::updateButtons);
        }
    }

    public final void setMachine(final Machine machine) {
        if (machine == null) {
            mapper = null;
            clearRows();
        } else {
            mapper = machine.getMapper();
            resetCountsRequest = true;
        }
        showAllRows();
        updateButtons();
    }

    private void loadFields() {
        final RamSearchPrefs prefs = AppPrefs.getInstance().getRamSearchAppPrefs();
        if (prefs.getAValue() == null) {
            aValueAvailable = false;
        } else {
            aValueAvailable = true;
            aValue = prefs.getAValue();
        }
        alignIndex = prefs.getAlignIndex();
        autofilter = prefs.getAutofilter();
        if (prefs.getBValue() == null) {
            bValueAvailable = false;
        } else {
            bValueAvailable = true;
            bValue = prefs.getBValue();
        }
        filterIndex = prefs.getFilterIndex();
        memoryRange = prefs.getMemoryRange();
        saveSlotIndex = prefs.getSaveSlotIndex();
        valueFormat = prefs.getValueFormat();
        wordSizeIndex = prefs.getWordSizeIndex();

        setTextField(aTextField, prefs.getAValue());
        alignComboBox.setSelectedIndex(alignIndex);
        autofilterCheckBox.setSelected(autofilter);
        setTextField(bTextField, prefs.getBValue());
        filterRadioButtons[filterIndex].setSelected(true);
        memoryRangeRadioButtons[memoryRange].setSelected(true);
        formatRadioButtons[valueFormat].setSelected(true);
        saveSlotComboBox.setSelectedIndex(saveSlotIndex);
        wordSizeRadioButtons[wordSizeIndex].setSelected(true);

        enableFilterRadioButtons();
    }

    private void saveFields() {
        final RamSearchPrefs prefs = AppPrefs.getInstance().getRamSearchAppPrefs();
        prefs.setAValue(aValueAvailable ? aValue : null);
        prefs.setAlignIndex(alignIndex);
        prefs.setAutofilter(autofilter);
        prefs.setBValue(bValueAvailable ? bValue : null);
        prefs.setFilterIndex(filterIndex);
        prefs.setMemoryRange(memoryRange);
        prefs.setSaveSlotIndex(saveSlotIndex);
        prefs.setValueFormat(valueFormat);
        prefs.setWordSizeIndex(wordSizeIndex);
        AppPrefs.save();
    }

    public void update() {

        final Mapper m = mapper;
        if (m == null) {
            return;
        }

        final int[] addresses = watchAddresses;
        final int wordSize = SIZES[wordSizeIndex];
        final int mask = MASKS[wordSizeIndex];
        if (addresses != null) {
            for (int i = addresses.length - 1; i >= 0; i--) {
                final int a = addresses[i];
                final RamRow row = rows[a];
                int value = 0;
                if (wordSize == 1) {
                    value = m.peekCpuMemory(a);
                } else {
                    for (int j = wordSize - 1; j >= 0; j--) {
                        value <<= 8;
                        value |= m.peekCpuMemory((a + j) & 0xFFFF);
                    }
                }
                if (row.current != value) {
                    row.prior = row.current;
                    row.current = value;
                    row.changes++;
                } else {
                    row.prior &= mask;
                }
                if (resetCountsRequest) {
                    row.changes = 0;
                }
                row.flagged = !filter.filter(row.current, row.prior, row.changes, a,
                        aValue, bValue);
            }
        } else {
            final MemoryRange[] range = RANGES[memoryRange];
            for (int i = range.length - 1; i >= 0; i--) {
                final int endAddress = range[i].endAddress;
                for (int a = range[i].startAddress; a <= endAddress; a++) {
                    memory[a] = m.peekCpuMemory(a);
                }
            }
            for (int i = range.length - 1; i >= 0; i--) {
                final int endAddress = range[i].endAddress;
                for (int a = range[i].startAddress; a <= endAddress; a += alignment) {
                    final RamRow row = rows[a];
                    int value = 0;
                    if (wordSize == 1) {
                        value = memory[a];
                    } else {
                        for (int j = wordSize - 1; j >= 0; j--) {
                            value <<= 8;
                            value |= memory[(a + j) & 0xFFFF];
                        }
                    }
                    if (row.current != value) {
                        row.prior = row.current;
                        row.current = value;
                        row.changes++;
                    } else {
                        row.prior &= mask;
                    }
                    if (resetCountsRequest) {
                        row.changes = 0;
                    }
                    row.flagged = !filter.filter(row.current, row.prior, row.changes, a,
                            aValue, bValue);
                }
            }
        }
        resetCountsRequest = false;

        EventQueue.invokeLater(this::updateTable);
    }

    private void updateTable() {
        final List<RamSearchTableRow> rs = tableModel.getRows();
        for (int i = rs.size() - 1; i >= 0; i--) {
            final RamSearchTableRow tableRow = rs.get(i);
            final RamRow ramRow = rows[tableRow.address];
            if (autofilter && ramRow.flagged) {
                rs.remove(i);
                tableModel.fireTableRowsDeleted(i, i);
                continue;
            }
            boolean rowChanged = false;
            if (tableRow.current != ramRow.current) {
                tableRow.setCurrent(ramRow.current);
                rowChanged = true;
            }
            if (tableRow.prior != ramRow.prior) {
                tableRow.setPrior(ramRow.prior);
                rowChanged = true;
            }
            if (tableRow.changes != ramRow.changes) {
                tableRow.setChanges(ramRow.changes);
                rowChanged = true;
            }
            if (tableRow.flagged != ramRow.flagged) {
                tableRow.setFlagged(ramRow.flagged);
                rowChanged = true;
            }
            if (rowChanged) {
                tableModel.fireTableRowsUpdated(i, i);
            }
        }
    }

    private void resizeTable() {
        final MemoryRange[] ranges = RANGES[memoryRange];
        final boolean[] addresses = new boolean[0x10000];
        final List<RamSearchTableRow> tableRows = tableModel.getRows();
        for (final RamSearchTableRow row : tableRows) {
            for (int i = 3; i >= 0; i--) {
                final int address = row.address + i;
                for (int j = ranges.length - 1; j >= 0; j--) {
                    if (ranges[j].startAddress <= address
                            && ranges[j].endAddress >= address) {
                        addresses[address] = true;
                        break;
                    }
                }
            }
        }

        final List<RamSearchTableRow> tableRows2 = new ArrayList<>();
        for (int address = 0x0000; address <= 0xFFFF; address += alignment) {
            if (addresses[address]) {
                final RamSearchTableRow row = new RamSearchTableRow(rows[address]);
                row.setAddress(address);
                row.setWordSizeIndex(wordSizeIndex);
                row.setValueFormat(valueFormat);
                tableRows2.add(row);
            }
        }
        tableModel.setRows(tableRows2);
        tableModel.fireTableDataChanged();
    }

    private void clearRows() {
        for (RamRow row : rows) {
            row.changes = 0;
            row.current = 0;
            row.flagged = false;
            row.prior = 0;
        }
    }

    private void showAllRows() {
        if (EventQueue.isDispatchThread()) {
            watchAddresses = null;
            final List<RamSearchTableRow> tableRows = new ArrayList<>();
            for (final MemoryRange range : RANGES[memoryRange]) {
                for (int address = range.startAddress; address <= range.endAddress;
                     address += alignment) {
                    final RamSearchTableRow row = new RamSearchTableRow(rows[address]);
                    row.setAddress(address);
                    row.setWordSizeIndex(wordSizeIndex);
                    row.setValueFormat(valueFormat);
                    tableRows.add(row);
                }
            }
            tableModel.setRows(tableRows);
            tableModel.fireTableDataChanged();
        } else {
            EventQueue.invokeLater(this::showAllRows);
        }
    }

    public void onStepPausedChanged(boolean paused) {
        pauseButton.setText(paused ? "Resume" : "Pause");
        pauseButton.setSelected(paused);
        nextFrameButton.setEnabled(paused && mapper != null);
    }

    private void onQuickSaveChanged(
            List<QuickSaveStateInfo> quickSaveStateInfos) {
        this.quickSaveStateInfos = quickSaveStateInfos;
        updateLoadButton();
    }

    private void updateLoadButton() {
        saveSlotIndex = saveSlotComboBox.getSelectedIndex();
        if (mapper != null && quickSaveStateInfos != null
                && saveSlotIndex < quickSaveStateInfos.size()) {
            final QuickSaveStateInfo info = quickSaveStateInfos.get(saveSlotIndex);
            loadButton.setEnabled(info.getFile().exists());
        } else {
            loadButton.setEnabled(false);
        }
    }

    public void onWatchesUpdated() {
        if (watchAddresses != null) {
            showWatches();
        }
        updateButtons();
    }

    public void showWatches() {
        final Set<Integer> addresses = new HashSet<>();
        for (final RamWatchRow row : GamePrefs.getInstance().getRamWatchGamePrefs()
                .getRows()) {
            if (!row.isSeparator()) {
                addresses.add(row.getAddress());
            }
        }
        if (addresses.isEmpty()) {
            showAllRows();
            return;
        }
        final int[] as = new int[addresses.size()];
        int index = 0;
        for (final Iterator<Integer> i = addresses.iterator(); i.hasNext(); ) {
            as[index++] = i.next();
        }
        Arrays.sort(as);

        final List<RamSearchTableRow> tableRows = new ArrayList<>();
        for (final int address : as) {
            final RamSearchTableRow row = new RamSearchTableRow(rows[address]);
            row.setAddress(address);
            row.setWordSizeIndex(wordSizeIndex);
            row.setValueFormat(valueFormat);
            tableRows.add(row);
        }
        tableModel.setRows(tableRows);
        tableModel.fireTableDataChanged();

        watchAddresses = as;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filterCriteriaButtonGroup = new javax.swing.ButtonGroup();
        wordSizeButtonGroup = new javax.swing.ButtonGroup();
        formatButtonGroup = new javax.swing.ButtonGroup();
        memoryButtonGroup = new javax.swing.ButtonGroup();
        scrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        cGTpRadioButton = new javax.swing.JRadioButton();
        addressEaRadioButton = new javax.swing.JRadioButton();
        cEaRadioButton = new javax.swing.JRadioButton();
        cLTpRadioButton = new javax.swing.JRadioButton();
        abscModaEbRadioButton = new javax.swing.JRadioButton();
        bLabel = new javax.swing.JLabel();
        changesGEaRadioButton = new javax.swing.JRadioButton();
        cMpEbRadioButton = new javax.swing.JRadioButton();
        bTextField = new javax.swing.JTextField();
        cEacMpEbRadioButton = new javax.swing.JRadioButton();
        abscMpEbRadioButton = new javax.swing.JRadioButton();
        aTextField = new javax.swing.JTextField();
        cGEpRadioButton = new javax.swing.JRadioButton();
        cEapEbRadioButton = new javax.swing.JRadioButton();
        cNEpRadioButton = new javax.swing.JRadioButton();
        aLabel = new javax.swing.JLabel();
        cLEpRadioButton = new javax.swing.JRadioButton();
        autofilterCheckBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        byte1RadioButton = new javax.swing.JRadioButton();
        byte2RadioButton = new javax.swing.JRadioButton();
        byte4RadioButton = new javax.swing.JRadioButton();
        alignLabel = new javax.swing.JLabel();
        alignComboBox = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        signedRadioButton = new javax.swing.JRadioButton();
        unsignedRadioButton = new javax.swing.JRadioButton();
        hexRadioButton = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        ramRadioButton = new javax.swing.JRadioButton();
        ramNvramRadioButton = new javax.swing.JRadioButton();
        romRadioButton = new javax.swing.JRadioButton();
        filterButton = new javax.swing.JButton();
        showAllButton = new javax.swing.JButton();
        resetCountsButton = new javax.swing.JButton();
        hideRowButton = new javax.swing.JButton();
        watchButton = new javax.swing.JButton();
        addCheatButton = new javax.swing.JButton();
        hexEditorButton = new javax.swing.JButton();
        loadButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        nextFrameButton = new javax.swing.JButton();
        saveSlotLabel = new javax.swing.JLabel();
        saveSlotComboBox = new javax.swing.JComboBox();
        pauseButton = new javax.swing.JToggleButton();
        showWatchesButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("RAM Search");
        setMaximumSize(null);
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        scrollPane.setMaximumSize(null);
        scrollPane.setMinimumSize(null);

        table.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{

                }
        ));
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(table);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter Criteria"));

        filterCriteriaButtonGroup.add(cGTpRadioButton);
        cGTpRadioButton.setText("<html>Current &gt; Prior</html>");
        cGTpRadioButton.setFocusPainted(false);

        filterCriteriaButtonGroup.add(addressEaRadioButton);
        addressEaRadioButton.setText("<html>Address = A</html>");
        addressEaRadioButton.setFocusPainted(false);

        filterCriteriaButtonGroup.add(cEaRadioButton);
        cEaRadioButton.setText("<html>Current = A</html>");
        cEaRadioButton.setFocusPainted(false);

        filterCriteriaButtonGroup.add(cLTpRadioButton);
        cLTpRadioButton.setText("<html>Current &lt; Prior</html>");
        cLTpRadioButton.setActionCommand("");
        cLTpRadioButton.setFocusPainted(false);

        filterCriteriaButtonGroup.add(abscModaEbRadioButton);
        abscModaEbRadioButton.setText("<html>| Current % A | = B</html>");
        abscModaEbRadioButton.setFocusPainted(false);

        bLabel.setText("B:");

        filterCriteriaButtonGroup.add(changesGEaRadioButton);
        changesGEaRadioButton.setText("<html>Changes &ge; A</html>");
        changesGEaRadioButton.setFocusPainted(false);

        filterCriteriaButtonGroup.add(cMpEbRadioButton);
        cMpEbRadioButton.setText("<html>Current &minus; Prior = B</html>");
        cMpEbRadioButton.setFocusPainted(false);

        bTextField.setColumns(6);

        filterCriteriaButtonGroup.add(cEacMpEbRadioButton);
        cEacMpEbRadioButton.setText("<html>Current = A, Current &minus; Prior = B</html>");
        cEacMpEbRadioButton.setFocusPainted(false);

        filterCriteriaButtonGroup.add(abscMpEbRadioButton);
        abscMpEbRadioButton.setText("<html>| Current &minus; Prior | = B</html>");
        abscMpEbRadioButton.setFocusPainted(false);

        aTextField.setColumns(6);

        filterCriteriaButtonGroup.add(cGEpRadioButton);
        cGEpRadioButton.setText("<html>Current &ge; Prior</html>");
        cGEpRadioButton.setFocusPainted(false);

        filterCriteriaButtonGroup.add(cEapEbRadioButton);
        cEapEbRadioButton.setText("<html>Current = A, Prior = B</html>");
        cEapEbRadioButton.setFocusPainted(false);

        filterCriteriaButtonGroup.add(cNEpRadioButton);
        cNEpRadioButton.setSelected(true);
        cNEpRadioButton.setText("<html>Current &ne; Prior</html>");
        cNEpRadioButton.setFocusPainted(false);

        aLabel.setText("A:");

        filterCriteriaButtonGroup.add(cLEpRadioButton);
        cLEpRadioButton.setText("<html>Current &le; Prior</html>");
        cLEpRadioButton.setFocusPainted(false);

        autofilterCheckBox.setText("Auto-filter");
        autofilterCheckBox.setFocusPainted(false);
        autofilterCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autofilterCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(autofilterCheckBox)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(aLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(aTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(bLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(bTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(cNEpRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cLTpRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cGTpRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cEaRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cEapEbRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cEacMpEbRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cMpEbRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(abscMpEbRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(abscModaEbRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cLEpRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cGEpRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addressEaRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(changesGEaRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(10, 10, 10))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(aLabel)
                                        .addComponent(aTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(bLabel)
                                        .addComponent(bTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cNEpRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cLTpRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cGTpRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cLEpRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cGEpRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cEaRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cEapEbRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cEacMpEbRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cMpEbRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(abscMpEbRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(abscModaEbRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addressEaRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(changesGEaRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(autofilterCheckBox)
                                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Word Size"));

        wordSizeButtonGroup.add(byte1RadioButton);
        byte1RadioButton.setSelected(true);
        byte1RadioButton.setText("1 byte");
        byte1RadioButton.setFocusPainted(false);

        wordSizeButtonGroup.add(byte2RadioButton);
        byte2RadioButton.setText("2 bytes");
        byte2RadioButton.setFocusPainted(false);

        wordSizeButtonGroup.add(byte4RadioButton);
        byte4RadioButton.setText("4 bytes");
        byte4RadioButton.setFocusPainted(false);

        alignLabel.setText("Align:");

        alignComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"1", "2", "4"}));
        alignComboBox.setFocusable(false);
        alignComboBox.setMaximumSize(null);
        alignComboBox.setMinimumSize(null);
        alignComboBox.setPreferredSize(null);
        alignComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alignComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(byte1RadioButton)
                                        .addComponent(byte2RadioButton)
                                        .addComponent(byte4RadioButton)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(alignLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(alignComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(byte1RadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(byte2RadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(byte4RadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(alignLabel)
                                        .addComponent(alignComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Format"));

        formatButtonGroup.add(signedRadioButton);
        signedRadioButton.setSelected(true);
        signedRadioButton.setText("Signed");
        signedRadioButton.setFocusPainted(false);

        formatButtonGroup.add(unsignedRadioButton);
        unsignedRadioButton.setText("Unsigned");
        unsignedRadioButton.setFocusPainted(false);

        formatButtonGroup.add(hexRadioButton);
        hexRadioButton.setText("Hex");
        hexRadioButton.setFocusPainted(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(signedRadioButton)
                                        .addComponent(unsignedRadioButton)
                                        .addComponent(hexRadioButton))
                                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(signedRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(unsignedRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(hexRadioButton)
                                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Memory"));

        memoryButtonGroup.add(ramRadioButton);
        ramRadioButton.setSelected(true);
        ramRadioButton.setText("RAM");
        ramRadioButton.setFocusPainted(false);

        memoryButtonGroup.add(ramNvramRadioButton);
        ramNvramRadioButton.setText("RAM, NVRAM");
        ramNvramRadioButton.setFocusPainted(false);

        memoryButtonGroup.add(romRadioButton);
        romRadioButton.setText("ROM");
        romRadioButton.setFocusPainted(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(ramRadioButton)
                                        .addComponent(ramNvramRadioButton)
                                        .addComponent(romRadioButton))
                                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(ramRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ramNvramRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(romRadioButton)
                                .addContainerGap())
        );

        filterButton.setMnemonic('F');
        filterButton.setText("Filter");
        filterButton.setFocusPainted(false);
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterButtonActionPerformed(evt);
            }
        });

        showAllButton.setMnemonic('A');
        showAllButton.setText("Show All");
        showAllButton.setFocusPainted(false);
        showAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAllButtonActionPerformed(evt);
            }
        });

        resetCountsButton.setMnemonic('R');
        resetCountsButton.setText("Reset Counts");
        resetCountsButton.setFocusPainted(false);
        resetCountsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetCountsButtonActionPerformed(evt);
            }
        });

        hideRowButton.setMnemonic('H');
        hideRowButton.setText("Hide Row");
        hideRowButton.setFocusPainted(false);
        hideRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideRowButtonActionPerformed(evt);
            }
        });

        watchButton.setMnemonic('W');
        watchButton.setText("Watch");
        watchButton.setFocusPainted(false);
        watchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                watchButtonActionPerformed(evt);
            }
        });

        addCheatButton.setMnemonic('C');
        addCheatButton.setText("Add Cheat");
        addCheatButton.setFocusPainted(false);
        addCheatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCheatButtonActionPerformed(evt);
            }
        });

        hexEditorButton.setMnemonic('x');
        hexEditorButton.setText("Hex Editor");
        hexEditorButton.setFocusPainted(false);
        hexEditorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hexEditorButtonActionPerformed(evt);
            }
        });

        loadButton.setMnemonic('L');
        loadButton.setText("Load");
        loadButton.setFocusPainted(false);
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        saveButton.setMnemonic('S');
        saveButton.setText("Save");
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        nextFrameButton.setMnemonic('m');
        nextFrameButton.setText("Frame+1");
        nextFrameButton.setFocusPainted(false);
        nextFrameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextFrameButtonActionPerformed(evt);
            }
        });

        saveSlotLabel.setText("Save slot:");
        saveSlotLabel.setMaximumSize(null);
        saveSlotLabel.setMinimumSize(null);
        saveSlotLabel.setPreferredSize(null);

        saveSlotComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"}));
        saveSlotComboBox.setFocusable(false);
        saveSlotComboBox.setMaximumSize(null);
        saveSlotComboBox.setMinimumSize(null);
        saveSlotComboBox.setPreferredSize(null);
        saveSlotComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSlotComboBoxActionPerformed(evt);
            }
        });

        pauseButton.setMnemonic('P');
        pauseButton.setText("Pause");
        pauseButton.setFocusPainted(false);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        showWatchesButton.setText("Show Watches");
        showWatchesButton.setFocusPainted(false);
        showWatchesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showWatchesButtonActionPerformed(evt);
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
                                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(saveSlotLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(saveSlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(pauseButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(nextFrameButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(filterButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(showAllButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(showWatchesButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(resetCountsButton))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(saveButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(loadButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(hideRowButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(watchButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(addCheatButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(hexEditorButton)))))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, jPanel2, jPanel3, jPanel4);

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, loadButton, nextFrameButton, pauseButton, saveButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(hideRowButton)
                                        .addComponent(watchButton)
                                        .addComponent(addCheatButton)
                                        .addComponent(hexEditorButton)
                                        .addComponent(loadButton)
                                        .addComponent(saveButton)
                                        .addComponent(saveSlotLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(saveSlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(nextFrameButton)
                                                .addComponent(pauseButton))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(filterButton)
                                                .addComponent(showAllButton)
                                                .addComponent(resetCountsButton)
                                                .addComponent(showWatchesButton)))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void alignComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alignComboBoxActionPerformed
        alignIndex = alignComboBox.getSelectedIndex();
        if (alignIndex >= 0) {
            alignment = SIZES[alignIndex];
        }
        resizeTable();
    }//GEN-LAST:event_alignComboBoxActionPerformed

    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterButtonActionPerformed
        final List<RamSearchTableRow> rs = tableModel.getRows();
        for (int i = rs.size() - 1; i >= 0; i--) {
            final RamSearchTableRow row = rs.get(i);
            if (row.isFlagged()) {
                rs.remove(i);
                tableModel.fireTableRowsDeleted(i, i);
            }
        }
    }//GEN-LAST:event_filterButtonActionPerformed

    private void showAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAllButtonActionPerformed
        showAllRows();
    }//GEN-LAST:event_showAllButtonActionPerformed

    private void resetCountsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetCountsButtonActionPerformed
        resetCountsRequest = true;
    }//GEN-LAST:event_resetCountsButtonActionPerformed

    private void hideRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideRowButtonActionPerformed
        if (selectedRowIndex >= 0 && selectedRowIndex < tableModel.getRowCount()) {
            tableModel.getRows().remove(selectedRowIndex);
            tableModel.fireTableRowsDeleted(selectedRowIndex, selectedRowIndex);
        }
    }//GEN-LAST:event_hideRowButtonActionPerformed

    private void watchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_watchButtonActionPerformed
        if (selectedRowIndex >= 0 && selectedRowIndex < tableModel.getRowCount()) {
            final RamSearchTableRow row = tableModel.getRow(selectedRowIndex);
            App.createRamWatchFrame();
            final RamWatchFrame frame = App.getRamWatchFrame();
            if (frame != null) {
                frame.addRamWatch(row.getAddress(), row.getWordSizeIndex(),
                        row.getValueFormat());
            }
        }
    }//GEN-LAST:event_watchButtonActionPerformed

    private void addCheatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCheatButtonActionPerformed
        if (selectedRowIndex >= 0 && selectedRowIndex < tableModel.getRowCount()) {
            final RamSearchTableRow row = tableModel.getRow(selectedRowIndex);
            final int value = row.current & 0xFF;
            final Cheat cheat = new Cheat(row.address, value, value);
            cheat.generateDescription();
            App.setNoStepPause(true);
            final CheatsDialog dialog = new CheatsDialog(this);
            dialog.setNewCheat(cheat);
            dialog.setVisible(true);
            App.setNoStepPause(false);
        }
    }//GEN-LAST:event_addCheatButtonActionPerformed

    private void hexEditorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hexEditorButtonActionPerformed
        if (selectedRowIndex >= 0 && selectedRowIndex < tableModel.getRowCount()) {
            final RamSearchTableRow row = tableModel.getRow(selectedRowIndex);
            App.createHexEditorFrame();
            final HexEditorFrame frame = App.getHexEditorFrame();
            frame.goToAddress(CpuMemory, row.address);
        }
    }//GEN-LAST:event_hexEditorButtonActionPerformed

    private void autofilterCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autofilterCheckBoxActionPerformed
        autofilter = autofilterCheckBox.isSelected();
    }//GEN-LAST:event_autofilterCheckBoxActionPerformed

    private void saveSlotComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSlotComboBoxActionPerformed
        updateLoadButton();
    }//GEN-LAST:event_saveSlotComboBoxActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        saveSlotIndex = saveSlotComboBox.getSelectedIndex();
        imageFrame.quickSaveState(saveSlotIndex + 1);
    }//GEN-LAST:event_saveButtonActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        saveSlotIndex = saveSlotComboBox.getSelectedIndex();
        imageFrame.quickLoadState(saveSlotIndex + 1);
    }//GEN-LAST:event_loadButtonActionPerformed

    private void nextFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextFrameButtonActionPerformed
        App.step(PauseStepType.Frame);
    }//GEN-LAST:event_nextFrameButtonActionPerformed

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        App.setStepPause(pauseButton.isSelected());
    }//GEN-LAST:event_pauseButtonActionPerformed

    private void showWatchesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showWatchesButtonActionPerformed
        showWatches();
    }//GEN-LAST:event_showWatchesButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
