package nintaco.gui.debugger.addresslabel;

import nintaco.App;
import nintaco.disassembler.AddressLabel;
import nintaco.disassembler.AddressTextRange;
import nintaco.gui.FileExtensionFilter;
import nintaco.gui.PleaseWaitDialog;
import nintaco.gui.ToolTipsTable;
import nintaco.gui.image.preferences.Paths;
import nintaco.gui.table.TableDialog;
import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;
import nintaco.util.CsvUtil;
import nintaco.util.EDT;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static nintaco.files.FileUtil.getFileNameWithoutExtension;
import static nintaco.files.FileUtil.mkdir;
import static nintaco.gui.debugger.addresslabel.AddressLabelColumns.*;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.isBlank;
import static nintaco.util.StringUtil.parseInt;

public class AddressLabelDialog extends TableDialog {

    public static final FileExtensionFilter[] FILE_FILTERS = {
            new FileExtensionFilter(0, "Comma-separated values files (*.csv)", "csv"),
            new FileExtensionFilter(1, "All files (*.*)"),
    };

    private AddressLabelTableModel tableModel;
    private boolean ok;
    private AddressTextRange range;

    public AddressLabelDialog(final Window parent) {
        super(parent);
    }

    @Override
    protected void initialize() {
        setTitle("Address Labels");
        tableModel = new AddressLabelTableModel();
        tableModel.setRowsCopy(GamePrefs.getInstance().getDebuggerGamePrefs()
                .getAddressLabels());
        table.setModel(tableModel);
        setCellRenderer(table, CODE, NO_BORDER_BOOLEAN_RENDERER);
        setCellRenderer(table, BOOKMARK, NO_BORDER_BOOLEAN_RENDERER);
        setCellRenderer(table, BANK, NO_BORDER_MONOSPACED_RENDERER);
        setCellRenderer(table, ADDRESS, NO_BORDER_MONOSPACED_RENDERER);
        setCellRenderer(table, LABEL, NO_BORDER_LABEL_RENDERER);
        setCellRenderer(table, COMMENT, NO_BORDER_LABEL_RENDERER);
        table.setAutoCreateRowSorter(true);
        table.getRowSorter().toggleSortOrder(ADDRESS);
        resizeCellSizes(table, true, 8, false, true, true, "MMM", "MMMMM",
                "MMMMMMMMMMMMMMMM", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
        ((ToolTipsTable) table).setColumnToolTips("Code", "Bookmark", "Bank",
                "Address", "Label", "Comment");
    }

    public void setRange(final AddressTextRange range) {
        this.range = range;
    }

    public boolean isOk() {
        return ok;
    }

    @Override
    protected void handleShown() {
        if (range != null) {
            final List<AddressLabel> rows = tableModel.getRows();
            for (int i = rows.size() - 1; i >= 0; i--) {
                final AddressLabel row = rows.get(i);
                if (row.getBank() == range.getBank()
                        && row.getAddress() == range.getAddress()) {
                    editButtonPressed(i);
                    return;
                }
            }
            final CreateAddressLabelDialog dialog
                    = new CreateAddressLabelDialog(this);
            dialog.setTitle("New Address Label");
            dialog.setAddressLabel(new AddressLabel(range.getBank(),
                    range.getAddress(), "", "", true, true));
            dialog.selectLabel();
            dialog.setVisible(true);
            if (dialog.isOK()) {
                setSelectedRow(tableModel.add(dialog.getAddressLabel()));
            }
        }
    }

    @Override
    protected void okButtonPressed() {
        GamePrefs.getInstance().getDebuggerGamePrefs().setAddressLabels(
                tableModel.getRows());
        GamePrefs.save();
        ok = true;
        closeDialog();
    }

    @Override
    protected void cancelButtonPressed() {
        closeDialog();
    }

    @Override
    protected void clearButtonPressed() {
        tableModel.clear();
    }

    @Override
    protected void exportButtonPressed() {
        App.setNoStepPause(true);
        final Paths paths = AppPrefs.getInstance().getPaths();
        final String recentDir = paths.getAddressLabelsDir();
        mkdir(recentDir);
        final File file = showSaveAsDialog(this, recentDir,
                getFileNameWithoutExtension(App.getEntryFileName()) + ".csv", "csv",
                FILE_FILTERS[0], true);
        if (file != null) {
            final String dir = file.getParent();
            paths.addRecentDirectory(dir);
            paths.setAddressLabelsDir(dir);
            AppPrefs.save();
            final List<AddressLabel> addressLabels = tableModel.getRows();
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> exportFile(pleaseWaitDialog, file, addressLabels))
                    .start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }

    private void exportFile(final PleaseWaitDialog pleaseWaitDialog,
                            final File file, final List<AddressLabel> addressLabels) {

        final List<List<String>> rows = new ArrayList<>();
        for (final AddressLabel addressLabel : addressLabels) {
            final List<String> row = new ArrayList<>();
            rows.add(row);
            row.add(Boolean.toString(addressLabel.isCode()));
            row.add(Boolean.toString(addressLabel.isBookmark()));
            row.add(String.format("%02X", addressLabel.getBank()));
            row.add(String.format("%04X", addressLabel.getAddress()));
            row.add(addressLabel.getLabel());
            row.add(addressLabel.getComment());
        }

        boolean error = false;
        try (PrintStream out = new PrintStream(new BufferedOutputStream(
                new FileOutputStream(file)))) {
            CsvUtil.write(out, rows);
        } catch (Throwable t) {
            error = true;
            //t.printStackTrace();
        }
        pleaseWaitDialog.dispose();
        if (error) {
            displayError(this, "Failed to export address labels.");
        }
        App.setNoStepPause(false);
    }

    @Override
    protected void importButtonPressed() {
        App.setNoStepPause(true);
        final JFileChooser chooser = createFileChooser("Import Address Labels",
                AppPrefs.getInstance().getPaths().getAddressLabelsDir(), FILE_FILTERS);
        if (showOpenDialog(this, chooser, (p, d) -> p.setAddressLabelsDir(d))
                == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = chooser.getSelectedFile();
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> importFile(pleaseWaitDialog, selectedFile)).start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }

    private void importFile(final PleaseWaitDialog pleaseWaitDialog,
                            final File selectedFile) {

        final List<AddressLabel> rows = new ArrayList<>();
        List<List<String>> values = null;
        try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
            values = CsvUtil.read(br);
            for (final List<String> list : values) {
                if (list.size() < 6) {
                    continue;
                }
                final boolean code = Boolean.parseBoolean(list.get(CODE));
                final boolean bookmark = Boolean.parseBoolean(list.get(BOOKMARK));
                final int bank = parseInt(list.get(BANK), true, 0xFF);
                final int address = parseInt(list.get(ADDRESS), true, 0xFFFF);
                final String label = list.get(LABEL);
                final String comment = list.get(COMMENT);
                if (address >= 0 && !(isBlank(label) && isBlank(comment))) {
                    rows.add(new AddressLabel(bank, address, label, comment, code,
                            bookmark));
                }
            }
        } catch (Throwable t) {
            //t.printStackTrace();
        }
        pleaseWaitDialog.dispose();
        if (values == null) {
            displayError(this, "Failed to import address labels file.");
        } else if (rows.isEmpty()) {
            displayError(this, "The file does not contain any valid address labels.");
        } else {
            EDT.async(() -> {
                tableModel.setRows(rows);
                selectionChanged();
                updateClearButton();
            });
        }
        App.setNoStepPause(false);
    }

    @Override
    protected void deleteButtonPressed(final int index) {
        tableModel.delete(index);
    }

    @Override
    protected void editButtonPressed(final int index) {
        final CreateAddressLabelDialog dialog = new CreateAddressLabelDialog(this);
        dialog.setTitle("Edit Address Label");
        dialog.setAddressLabel(tableModel.getRows().get(index));
        dialog.selectLabel();
        dialog.setVisible(true);
        if (dialog.isOK()) {
            tableModel.delete(index);
            setSelectedRow(tableModel.add(dialog.getAddressLabel()));
        }
    }

    @Override
    protected void newButtonPressed() {
        final CreateAddressLabelDialog dialog = new CreateAddressLabelDialog(this);
        dialog.setTitle("New Address Label");
        dialog.selectAddress();
        dialog.setVisible(true);
        if (dialog.isOK()) {
            setSelectedRow(tableModel.add(dialog.getAddressLabel()));
        }
    }
}
