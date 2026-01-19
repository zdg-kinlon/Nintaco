package nintaco.gui.ramwatch;

import nintaco.App;
import nintaco.Machine;
import nintaco.cheats.Cheat;
import nintaco.gui.FileExtensionFilter;
import nintaco.gui.PleaseWaitDialog;
import nintaco.gui.cheats.CheatsDialog;
import nintaco.gui.hexeditor.HexEditorFrame;
import nintaco.gui.image.preferences.Paths;
import nintaco.gui.ramsearch.RamSearchFrame;
import nintaco.mappers.Mapper;
import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;
import nintaco.util.EDT;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nintaco.files.FileUtil.getFileNameWithoutExtension;
import static nintaco.files.FileUtil.mkdir;
import static nintaco.gui.hexeditor.DataSource.CpuMemory;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.parseInt;

public class RamWatchFrame extends javax.swing.JFrame {

    private static final FileExtensionFilter[] FILE_FILTERS = {
            new FileExtensionFilter(0, "Watch list files (*.wch)", "wch"),
            new FileExtensionFilter(1, "All files (*.*)"),
    };

    private static final int[] SIZES = {1, 2, 4};
    private static final char[] WORD_SIZES = {'b', 'w', 'd'};
    private static final char[] FORMATS = {'s', 'u', 'h'};
    private static final Pattern pattern = Pattern.compile(
            "\\d+\\t(\\p{XDigit}+)\\t(\\D)\\t(\\D)\\t0\\t(.*)");

    private RamWatchTableModel tableModel;

    private volatile Mapper mapper;
    private volatile List<RamWatchRow> ramRows = new ArrayList<>();
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addCheatButton;
    private javax.swing.JMenuItem addCheatMenuItem;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JButton downButton;
    private javax.swing.JButton duplicateButton;
    private javax.swing.JMenuItem duplicateMenuItem;
    private javax.swing.JButton editButton;
    private javax.swing.JMenuItem editMenuItem;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton hexEditorButton;
    private javax.swing.JMenuItem hexEditorMenuItem;
    private javax.swing.JMenuItem importMenuItem;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem moveDownMenuItem;
    private javax.swing.JMenuItem moveUpMenuItem;
    private javax.swing.JButton newButton;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JMenuItem removeAllMenuItem;
    private javax.swing.JButton removeButton;
    private javax.swing.JMenuItem removeMenuItem;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton searchButton;
    private javax.swing.JMenuItem searchMenuItem;
    private javax.swing.JButton separatorButton;
    private javax.swing.JMenuItem separatorMenuItem;
    private javax.swing.JTable table;
    private javax.swing.JButton upButton;
    private javax.swing.JPanel upDownPanel;
    private javax.swing.JMenu watchesMenuItem;
    private javax.swing.JPopupMenu.Separator watchesSeparator1;
    private javax.swing.JPopupMenu.Separator watchesSeparator2;

    public RamWatchFrame(final Machine machine) {
        initComponents();
        initTable();
        setMachine(machine);
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
    }

    private void initTable() {
        table.getSelectionModel().addListSelectionListener(
                e -> handleTableSelectionChanged());
        tableModel = new RamWatchTableModel();
        table.setModel(tableModel);
        final RamWatchRowRenderer leftRenderer = new RamWatchRowRenderer(false);
        final RamWatchRowRenderer rightRenderer = new RamWatchRowRenderer(true);
        for (int i = 0; i < 3; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(
                    i == 2 ? leftRenderer : rightRenderer);
        }

        resizeCellSizes(table, true, 10, false, "BBBB", "BBBBBBBBBBB",
                "This is an example description.");
    }

    public void addRamWatch(final int address, final int wordSizeIndex,
                            final int valueFormat) {
        final EditRamWatchDialog dialog = new EditRamWatchDialog(this);
        final RamWatchRow row = new RamWatchRow();
        row.setAddress(address);
        row.setWordSizeIndex(wordSizeIndex);
        row.setValueFormat(valueFormat);
        dialog.setRamWatchRow(row);
        dialog.setTitle("New RAM Watch");
        dialog.setVisible(true);
        insertRow(dialog.getResult());
    }

    private void handleTableSelectionChanged() {
        updateButtons();
    }

    public void destroy() {
        dispose();
    }

    private void closeFrame() {
        App.destroyRamWatchFrame();
    }

    private void loadGamePrefs() {
        final List<RamWatchRow> rows = GamePrefs.getInstance()
                .getRamWatchGamePrefs().getRows();
        EDT.async(() -> {
            synchronized (GamePrefs.class) {
                tableModel.copyRows(rows);
            }
            tableModel.fireTableDataChanged();
            onTableRowsChanged();
        });
    }

    private void moveRowUp() {
        final int selectedIndex = table.getSelectedRow();
        if (selectedIndex > 0 && selectedIndex < tableModel.getRowCount()) {
            final List<RamWatchRow> rows = tableModel.getRows();
            final RamWatchRow r0 = rows.get(selectedIndex - 1);
            final RamWatchRow r1 = rows.get(selectedIndex);
            rows.set(selectedIndex - 1, r1);
            rows.set(selectedIndex, r0);
            tableModel.fireTableRowsUpdated(selectedIndex - 1, selectedIndex);
            table.setRowSelectionInterval(selectedIndex - 1, selectedIndex - 1);
            onTableRowsChanged();
        }
    }

    private void moveRowDown() {
        final int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0 && selectedIndex < tableModel.getRowCount() - 1) {
            final List<RamWatchRow> rows = tableModel.getRows();
            final RamWatchRow r0 = rows.get(selectedIndex);
            final RamWatchRow r1 = rows.get(selectedIndex + 1);
            rows.set(selectedIndex, r1);
            rows.set(selectedIndex + 1, r0);
            tableModel.fireTableRowsUpdated(selectedIndex, selectedIndex + 1);
            table.setRowSelectionInterval(selectedIndex + 1, selectedIndex + 1);
            onTableRowsChanged();
        }
    }

    private void editRow() {
        final int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0 && selectedIndex < tableModel.getRowCount()) {
            final EditRamWatchDialog dialog = new EditRamWatchDialog(this);
            dialog.setTitle("Edit RAM Watch");
            dialog.setRamWatchRow(tableModel.getRows().get(selectedIndex));
            dialog.setVisible(true);
            final RamWatchRow row = dialog.getResult();
            if (row != null) {
                final List<RamWatchRow> rows = tableModel.getRows();
                rows.set(selectedIndex, row);
                tableModel.fireTableRowsUpdated(selectedIndex, selectedIndex);
                onTableRowsChanged();
            }
        }
    }

    private void removeRow() {
        final int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0 && selectedIndex < tableModel.getRowCount()) {
            tableModel.getRows().remove(selectedIndex);
            tableModel.fireTableRowsDeleted(selectedIndex, selectedIndex);
            onTableRowsChanged();
        }
    }

    private void removeAllRows() {
        clearTable();
    }

    private void newRow() {
        final EditRamWatchDialog dialog = new EditRamWatchDialog(this);
        dialog.setTitle("New RAM Watch");
        dialog.setVisible(true);
        insertRow(dialog.getResult());
    }

    private void duplicateRow() {
        final int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0 && selectedIndex < tableModel.getRowCount()) {
            insertRow(new RamWatchRow(tableModel.getRow(selectedIndex)));
        }
    }

    private void addSeparator() {
        final RamWatchRow row = new RamWatchRow();
        row.setSeparator(true);
        insertRow(row);
    }

    private void search() {
        App.createRamSearchFrame();
        final RamSearchFrame frame = App.getRamSearchFrame();
        if (frame != null) {
            frame.showWatches();
        }
    }

    private void addCheat() {
        final int selectedRowIndex = table.getSelectedRow();
        if (selectedRowIndex >= 0 && selectedRowIndex < tableModel.getRowCount()) {
            final RamWatchRow row = tableModel.getRow(selectedRowIndex);
            final int value = row.getValue() & 0xFF;
            final Cheat cheat = new Cheat(row.getAddress(), value, value);
            cheat.generateDescription();
            App.setNoStepPause(true);
            final CheatsDialog dialog = new CheatsDialog(this);
            dialog.setNewCheat(cheat);
            dialog.setVisible(true);
            App.setNoStepPause(false);
        }
    }

    private void showHexEditor() {
        final int selectedRowIndex = table.getSelectedRow();
        if (selectedRowIndex >= 0 && selectedRowIndex < tableModel.getRowCount()) {
            final RamWatchRow row = tableModel.getRow(selectedRowIndex);
            App.createHexEditorFrame();
            final HexEditorFrame frame = App.getHexEditorFrame();
            frame.goToAddress(CpuMemory, row.getAddress());
        }
    }

    private void insertRow(final RamWatchRow row) {
        if (row != null) {
            final List<RamWatchRow> rows = tableModel.getRows();
            final int selectedIndex = table.getSelectedRow();
            int index;
            if (selectedIndex >= 0 && selectedIndex < rows.size()) {
                index = selectedIndex + 1;
                rows.add(index, row);
            } else {
                rows.add(row);
                index = rows.size() - 1;
            }
            tableModel.fireTableRowsInserted(index, index);
            table.setRowSelectionInterval(index, index);
            onTableRowsChanged();
        }
    }

    private void clearTable() {
        tableModel.getRows().clear();
        tableModel.fireTableDataChanged();
        onTableRowsChanged();
    }

    public final void setMachine(final Machine machine) {
        if (machine == null) {
            mapper = null;
            EDT.async(this::clearTable);
        } else {
            mapper = machine.getMapper();
            loadGamePrefs();
        }
        updateButtons();
    }

    public void update() {

        final Mapper m = mapper;
        if (m == null) {
            return;
        }

        final List<RamWatchRow> rows = ramRows;
        for (int i = rows.size() - 1; i >= 0; i--) {
            final RamWatchRow row = rows.get(i);
            if (!row.isSeparator()) {
                final int wordSize = SIZES[row.getWordSizeIndex()];
                int value;
                if (wordSize == 1) {
                    value = m.peekCpuMemory(row.getAddress());
                } else {
                    value = 0;
                    final int address = row.getAddress();
                    for (int j = wordSize - 1; j >= 0; j--) {
                        value <<= 8;
                        value |= m.peekCpuMemory(address + j);
                    }
                }
                if (row.getValue() != value) {
                    row.setValue(value);
                }
            }
        }
        EDT.async(this::updateTable);
    }

    private void updateTable() {
        final List<RamWatchRow> tableRows = tableModel.getRows();
        for (int i = tableRows.size() - 1; i >= 0; i--) {
            final RamWatchRow tableRow = tableRows.get(i);
            final RamWatchRow row = ramRows.get(i);
            if (!tableRow.isSeparator() && row.getValue() != tableRow.getValue()) {
                tableRow.setValue(row.getValue());
                tableModel.fireTableCellUpdated(i, 1);
            }
        }
    }

    private void onTableRowsChanged() {
        final List<RamWatchRow> tableRows = tableModel.getRows();
        final List<RamWatchRow> rows = new ArrayList<>();
        final List<RamWatchRow> prefRows = new ArrayList<>();
        for (final RamWatchRow row : tableRows) {
            rows.add(new RamWatchRow(row));
            prefRows.add(new RamWatchRow(row));
        }
        this.ramRows = rows;
        updateButtons();

        GamePrefs.getInstance().getRamWatchGamePrefs().setRows(prefRows);
        GamePrefs.save();
        final RamSearchFrame frame = App.getRamSearchFrame();
        if (frame != null) {
            frame.onWatchesUpdated();
        }
    }

    private boolean hasWatches() {
        for (final RamWatchRow row : tableModel.getRows()) {
            if (!row.isSeparator()) {
                return true;
            }
        }
        return false;
    }

    private void updateButtons() {
        EDT.async(() -> {
            final boolean enabled = mapper != null;
            final int selectedIndex = table.getSelectedRow();
            final boolean rowSelected = enabled && selectedIndex >= 0
                    && selectedIndex < tableModel.getRowCount();
            final boolean separatorSelected;
            if (rowSelected) {
                final RamWatchRow row = tableModel.getRow(selectedIndex);
                separatorSelected = row.isSeparator();
            } else {
                separatorSelected = false;
            }

            upButton.setEnabled(rowSelected && selectedIndex > 0);
            downButton.setEnabled(rowSelected
                    && selectedIndex < tableModel.getRowCount() - 1);
            newButton.setEnabled(enabled);
            editButton.setEnabled(rowSelected && !separatorSelected);
            removeButton.setEnabled(rowSelected);
            removeAllButton.setEnabled(enabled && tableModel.getRowCount() > 0);
            duplicateButton.setEnabled(rowSelected);
            separatorButton.setEnabled(enabled);
            searchButton.setEnabled(enabled && hasWatches());
            addCheatButton.setEnabled(rowSelected && !separatorSelected);
            hexEditorButton.setEnabled(rowSelected && !separatorSelected);

            moveUpMenuItem.setEnabled(upButton.isEnabled());
            moveDownMenuItem.setEnabled(downButton.isEnabled());
            newMenuItem.setEnabled(newButton.isEnabled());
            editMenuItem.setEnabled(editButton.isEnabled());
            removeMenuItem.setEnabled(removeButton.isEnabled());
            removeAllMenuItem.setEnabled(removeAllButton.isEnabled());
            duplicateMenuItem.setEnabled(duplicateButton.isEnabled());
            separatorMenuItem.setEnabled(separatorButton.isEnabled());
            searchMenuItem.setEnabled(searchButton.isEnabled());
            addCheatMenuItem.setEnabled(addCheatButton.isEnabled());
            hexEditorMenuItem.setEnabled(hexEditorButton.isEnabled());
        });
    }

    private void importWatchList(final PleaseWaitDialog pleaseWaitDialog,
                                 final File file) {

        boolean error = false;
        final List<RamWatchRow> rows = new ArrayList<>();
        try (final BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            final int rowCount = Integer.parseInt(br.readLine().trim());
            for (int i = 0; i < rowCount; i++) {
                final Matcher matcher = pattern.matcher(br.readLine());
                if (matcher.find()) {
                    final RamWatchRow row = new RamWatchRow();
                    final char wordSize = matcher.group(2).charAt(0);
                    final char format = matcher.group(3).charAt(0);
                    if (wordSize == 'S') {
                        row.setSeparator(true);
                    } else {
                        row.setAddress(parseInt(matcher.group(1), true, 0x0000, 0xFFFF));
                        switch (wordSize) {
                            case 'b':
                                row.setWordSizeIndex(0);
                                break;
                            case 'w':
                                row.setWordSizeIndex(1);
                                break;
                            case 'd':
                                row.setWordSizeIndex(2);
                                break;
                            default:
                                throw new RuntimeException("Invalid word size: " + wordSize);
                        }
                        switch (format) {
                            case 's':
                                row.setValueFormat(0);
                                break;
                            case 'u':
                                row.setValueFormat(1);
                                break;
                            case 'h':
                                row.setValueFormat(2);
                                break;
                            default:
                                throw new RuntimeException("Invalid value format: " + format);
                        }
                        row.setDescription(matcher.group(4));
                        rows.add(row);
                    }
                }
            }
        } catch (Throwable t) {
            //t.printStackTrace();
            error = true;
        }
        pleaseWaitDialog.dispose();
        if (error) {
            displayError(this, "Failed to import watch list.");
        } else {
            EDT.async(() -> {
                tableModel.setRows(rows);
                tableModel.fireTableDataChanged();
                onTableRowsChanged();
            });
        }
    }

    private void exportWatchList(final PleaseWaitDialog pleaseWaitDialog,
                                 final File file, final List<RamWatchRow> rows) {
        boolean error = false;
        try (final PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(file)))) {
            out.println();
            out.println(rows.size());
            for (int i = 0; i < rows.size(); i++) {
                final RamWatchRow row = rows.get(i);
                final char wordSize;
                final char format;
                final String description;
                if (row.isSeparator()) {
                    wordSize = 'S';
                    format = 'S';
                    description = "----------------------------";
                } else {
                    wordSize = WORD_SIZES[row.getWordSizeIndex()];
                    format = FORMATS[row.getValueFormat()];
                    description = row.getDescription();
                }
                out.format("%05d\t%04X\t%c\t%c\t0\t%s%n", i, row.getAddress(), wordSize,
                        format, description);
            }
        } catch (Throwable t) {
            error = true;
            //t.printStackTrace();
        }
        pleaseWaitDialog.dispose();
        if (error) {
            displayError(this, "Failed to export watch list.");
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

        scrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        buttonsPanel = new javax.swing.JPanel();
        hexEditorButton = new javax.swing.JButton();
        addCheatButton = new javax.swing.JButton();
        separatorButton = new javax.swing.JButton();
        duplicateButton = new javax.swing.JButton();
        newButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        upDownPanel = new javax.swing.JPanel();
        downButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        importMenuItem = new javax.swing.JMenuItem();
        exportMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        closeMenuItem = new javax.swing.JMenuItem();
        watchesMenuItem = new javax.swing.JMenu();
        moveUpMenuItem = new javax.swing.JMenuItem();
        moveDownMenuItem = new javax.swing.JMenuItem();
        watchesSeparator1 = new javax.swing.JPopupMenu.Separator();
        newMenuItem = new javax.swing.JMenuItem();
        editMenuItem = new javax.swing.JMenuItem();
        removeMenuItem = new javax.swing.JMenuItem();
        removeAllMenuItem = new javax.swing.JMenuItem();
        duplicateMenuItem = new javax.swing.JMenuItem();
        separatorMenuItem = new javax.swing.JMenuItem();
        watchesSeparator2 = new javax.swing.JPopupMenu.Separator();
        searchMenuItem = new javax.swing.JMenuItem();
        addCheatMenuItem = new javax.swing.JMenuItem();
        hexEditorMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("RAM Watch");
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

        hexEditorButton.setMnemonic('x');
        hexEditorButton.setText("Hex Editor");
        hexEditorButton.setFocusPainted(false);
        hexEditorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hexEditorButtonActionPerformed(evt);
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

        separatorButton.setMnemonic('t');
        separatorButton.setText("Separator");
        separatorButton.setFocusPainted(false);
        separatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                separatorButtonActionPerformed(evt);
            }
        });

        duplicateButton.setMnemonic('p');
        duplicateButton.setText("Duplicate");
        duplicateButton.setFocusPainted(false);
        duplicateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateButtonActionPerformed(evt);
            }
        });

        newButton.setMnemonic('N');
        newButton.setText("New");
        newButton.setFocusPainted(false);
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        removeButton.setMnemonic('R');
        removeButton.setText("Remove");
        removeButton.setFocusPainted(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        editButton.setMnemonic('E');
        editButton.setText("Edit");
        editButton.setFocusPainted(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        downButton.setText("<html>&#x25bc;</html>");
        downButton.setFocusPainted(false);
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        upButton.setText("<html>&#x25b2;</html>");
        upButton.setFocusPainted(false);
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout upDownPanelLayout = new javax.swing.GroupLayout(upDownPanel);
        upDownPanel.setLayout(upDownPanelLayout);
        upDownPanelLayout.setHorizontalGroup(
                upDownPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(upDownPanelLayout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(upDownPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(upButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(downButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        upDownPanelLayout.setVerticalGroup(
                upDownPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(upDownPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(upButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(downButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0))
        );

        removeAllButton.setMnemonic('A');
        removeAllButton.setText("Remove All");
        removeAllButton.setFocusPainted(false);
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });

        searchButton.setMnemonic('S');
        searchButton.setText("Search");
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonsPanelLayout = new javax.swing.GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        buttonsPanelLayout.setHorizontalGroup(
                buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(removeAllButton)
                        .addComponent(removeButton)
                        .addComponent(duplicateButton)
                        .addComponent(editButton)
                        .addComponent(newButton)
                        .addComponent(separatorButton)
                        .addComponent(searchButton)
                        .addComponent(addCheatButton)
                        .addComponent(hexEditorButton)
                        .addComponent(upDownPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        buttonsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, addCheatButton, duplicateButton, editButton, hexEditorButton, newButton, removeAllButton, removeButton, searchButton, separatorButton);

        buttonsPanelLayout.setVerticalGroup(
                buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(buttonsPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(upDownPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(newButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(editButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeAllButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(duplicateButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separatorButton)
                                .addGap(18, 18, 18)
                                .addComponent(searchButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addCheatButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(hexEditorButton)
                                .addGap(0, 0, 0))
        );

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");
        fileMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                fileMenuMenuSelected(evt);
            }
        });

        importMenuItem.setMnemonic('I');
        importMenuItem.setText("Import Watch List...");
        importMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(importMenuItem);

        exportMenuItem.setMnemonic('E');
        exportMenuItem.setText("Export Watch List...");
        exportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportMenuItem);
        fileMenu.add(jSeparator3);

        closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        closeMenuItem.setMnemonic('C');
        closeMenuItem.setText("Close");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeMenuItem);

        menuBar.add(fileMenu);

        watchesMenuItem.setMnemonic('W');
        watchesMenuItem.setText("Watches");

        moveUpMenuItem.setMnemonic('U');
        moveUpMenuItem.setText("Move Up");
        moveUpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpMenuItemActionPerformed(evt);
            }
        });
        watchesMenuItem.add(moveUpMenuItem);

        moveDownMenuItem.setMnemonic('D');
        moveDownMenuItem.setText("Move Down");
        moveDownMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownMenuItemActionPerformed(evt);
            }
        });
        watchesMenuItem.add(moveDownMenuItem);
        watchesMenuItem.add(watchesSeparator1);

        newMenuItem.setText("New...");
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuItemActionPerformed(evt);
            }
        });
        watchesMenuItem.add(newMenuItem);

        editMenuItem.setMnemonic('E');
        editMenuItem.setText("Edit...");
        editMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuItemActionPerformed(evt);
            }
        });
        watchesMenuItem.add(editMenuItem);

        removeMenuItem.setMnemonic('R');
        removeMenuItem.setText("Remove");
        removeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMenuItemActionPerformed(evt);
            }
        });
        watchesMenuItem.add(removeMenuItem);

        removeAllMenuItem.setMnemonic('A');
        removeAllMenuItem.setText("Remove All");
        removeAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllMenuItemActionPerformed(evt);
            }
        });
        watchesMenuItem.add(removeAllMenuItem);

        duplicateMenuItem.setMnemonic('p');
        duplicateMenuItem.setText("Duplicate");
        duplicateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateMenuItemActionPerformed(evt);
            }
        });
        watchesMenuItem.add(duplicateMenuItem);

        separatorMenuItem.setMnemonic('t');
        separatorMenuItem.setText("Separator");
        separatorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                separatorMenuItemActionPerformed(evt);
            }
        });
        watchesMenuItem.add(separatorMenuItem);
        watchesMenuItem.add(watchesSeparator2);

        searchMenuItem.setMnemonic('S');
        searchMenuItem.setText("Search...");
        searchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchMenuItemActionPerformed(evt);
            }
        });
        watchesMenuItem.add(searchMenuItem);

        addCheatMenuItem.setMnemonic('C');
        addCheatMenuItem.setText("Add Cheat...");
        addCheatMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCheatMenuItemActionPerformed(evt);
            }
        });
        watchesMenuItem.add(addCheatMenuItem);

        hexEditorMenuItem.setMnemonic('x');
        hexEditorMenuItem.setText("Hex Editor...");
        hexEditorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hexEditorMenuItemActionPerformed(evt);
            }
        });
        watchesMenuItem.add(hexEditorMenuItem);

        menuBar.add(watchesMenuItem);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(buttonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(buttonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        moveRowUp();
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        moveRowDown();
    }//GEN-LAST:event_downButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        editRow();
    }//GEN-LAST:event_editButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        removeRow();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        newRow();
    }//GEN-LAST:event_newButtonActionPerformed

    private void duplicateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateButtonActionPerformed
        duplicateRow();
    }//GEN-LAST:event_duplicateButtonActionPerformed

    private void separatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_separatorButtonActionPerformed
        addSeparator();
    }//GEN-LAST:event_separatorButtonActionPerformed

    private void addCheatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCheatButtonActionPerformed
        addCheat();
    }//GEN-LAST:event_addCheatButtonActionPerformed

    private void hexEditorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hexEditorButtonActionPerformed
        showHexEditor();
    }//GEN-LAST:event_hexEditorButtonActionPerformed

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        removeAllRows();
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void moveUpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpMenuItemActionPerformed
        moveRowUp();
    }//GEN-LAST:event_moveUpMenuItemActionPerformed

    private void moveDownMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownMenuItemActionPerformed
        moveRowDown();
    }//GEN-LAST:event_moveDownMenuItemActionPerformed

    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMenuItemActionPerformed
        newRow();
    }//GEN-LAST:event_newMenuItemActionPerformed

    private void editMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuItemActionPerformed
        editRow();
    }//GEN-LAST:event_editMenuItemActionPerformed

    private void removeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMenuItemActionPerformed
        removeRow();
    }//GEN-LAST:event_removeMenuItemActionPerformed

    private void removeAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllMenuItemActionPerformed
        removeAllRows();
    }//GEN-LAST:event_removeAllMenuItemActionPerformed

    private void duplicateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateMenuItemActionPerformed
        duplicateRow();
    }//GEN-LAST:event_duplicateMenuItemActionPerformed

    private void separatorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_separatorMenuItemActionPerformed
        addSeparator();
    }//GEN-LAST:event_separatorMenuItemActionPerformed

    private void addCheatMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCheatMenuItemActionPerformed
        addCheat();
    }//GEN-LAST:event_addCheatMenuItemActionPerformed

    private void hexEditorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hexEditorMenuItemActionPerformed
        showHexEditor();
    }//GEN-LAST:event_hexEditorMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        closeFrame();
    }//GEN-LAST:event_closeMenuItemActionPerformed

    private void importMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importMenuItemActionPerformed
        final Paths paths = AppPrefs.getInstance().getPaths();
        mkdir(paths.getWatchesDir());

        final JFileChooser chooser = createFileChooser("Import Watch List",
                paths.getWatchesDir(), FILE_FILTERS);
        if (showOpenDialog(this, chooser, (p, d) -> p.setWatchesDir(d))
                == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = chooser.getSelectedFile();
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> importWatchList(pleaseWaitDialog, selectedFile)).start();
            pleaseWaitDialog.showAfterDelay();
        }
    }//GEN-LAST:event_importMenuItemActionPerformed

    private void exportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMenuItemActionPerformed
        final Paths paths = AppPrefs.getInstance().getPaths();
        mkdir(paths.getWatchesDir());
        final File file = showSaveAsDialog(this, paths.getWatchesDir(),
                getFileNameWithoutExtension(App.getEntryFileName()) + ".wch", "wch",
                FILE_FILTERS[0], true, "Export Watch List");
        if (file != null) {
            final String dir = file.getParent();
            paths.addRecentDirectory(dir);
            paths.setWatchesDir(dir);
            AppPrefs.save();
            final List<RamWatchRow> rows = ramRows;
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> exportWatchList(pleaseWaitDialog, file, rows)).start();
            pleaseWaitDialog.showAfterDelay();
        }
    }//GEN-LAST:event_exportMenuItemActionPerformed

    private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_fileMenuMenuSelected
        final boolean enabled = mapper != null;
        importMenuItem.setEnabled(enabled);
        exportMenuItem.setEnabled(enabled && ramRows.size() > 0);
    }//GEN-LAST:event_fileMenuMenuSelected

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        search();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void searchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchMenuItemActionPerformed
        search();
    }//GEN-LAST:event_searchMenuItemActionPerformed
    // End of variables declaration//GEN-END:variables
}
