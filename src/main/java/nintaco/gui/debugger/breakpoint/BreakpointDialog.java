package nintaco.gui.debugger.breakpoint;

import nintaco.App;
import nintaco.Breakpoint;
import nintaco.BreakpointType;
import nintaco.gui.PleaseWaitDialog;
import nintaco.gui.ToolTipsTable;
import nintaco.gui.debugger.addresslabel.AddressLabelDialog;
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
import static nintaco.gui.debugger.breakpoint.BreakpointColumns.*;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.parseInt;

public class BreakpointDialog extends TableDialog {

    private BreakpointTableModel tableModel;
    private Breakpoint breakpoint;
    private boolean ok;

    public BreakpointDialog(final Window parent) {
        super(parent);
    }

    @Override
    protected void initialize() {
        setTitle("Breakpoints");
        tableModel = new BreakpointTableModel();
        tableModel.setRowsCopy(GamePrefs.getInstance().getDebuggerGamePrefs()
                .getBreakpoints());
        table.setModel(tableModel);
        table.setAutoCreateRowSorter(true);
        table.getRowSorter().toggleSortOrder(START_ADDRESS);
        setCellRenderer(table, ENABLED, NO_BORDER_BOOLEAN_RENDERER);
        createComboBoxCellEditorAndRenderer(table, TYPE,
                BreakpointTableModel.TYPE_NAMES);
        setCellRenderer(table, BANK, NO_BORDER_MONOSPACED_RENDERER);
        setCellRenderer(table, START_ADDRESS, NO_BORDER_MONOSPACED_RENDERER);
        setCellRenderer(table, END_ADDRESS, NO_BORDER_MONOSPACED_RENDERER);
        resizeCellSizes(table, true, 8, false, Boolean.TRUE, BreakpointTableModel
                .TYPE_NAMES[BreakpointType.Execute], "MMMM", "MMMMMM", "MMMMMM");
        ((ToolTipsTable) table).setColumnToolTips("Enabled", "Type", "Bank", "Start",
                "End");
    }

    public boolean isOk() {
        return ok;
    }

    public void setBreakpoint(final Breakpoint breakpoint) {
        this.breakpoint = breakpoint;
    }

    @Override
    protected void handleShown() {
        if (breakpoint != null) {
            final List<Breakpoint> rows = tableModel.getRows();
            for (int i = rows.size() - 1; i >= 0; i--) {
                final Breakpoint row = rows.get(i);
                if (row.equals(breakpoint)) {
                    editButtonPressed(i);
                    return;
                }
            }
            final CreateBreakpointDialog dialog = new CreateBreakpointDialog(this);
            dialog.setTitle("New Breakpoint");
            dialog.setBreakpoint(new Breakpoint(breakpoint));
            dialog.setVisible(true);
            if (dialog.isOk()) {
                setSelectedRow(tableModel.add(dialog.getBreakpoint()));
            }
        }
    }

    @Override
    protected void okButtonPressed() {
        GamePrefs.getInstance().getDebuggerGamePrefs().setBreakpoints(tableModel
                .getRows());
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
        final String recentDir = paths.getBreakpointsDir();
        mkdir(recentDir);
        final File file = showSaveAsDialog(this, recentDir,
                getFileNameWithoutExtension(App.getEntryFileName()) + ".csv", "csv",
                AddressLabelDialog.FILE_FILTERS[0], true);
        if (file != null) {
            final String dir = file.getParent();
            paths.addRecentDirectory(dir);
            paths.setBreakpointsDir(dir);
            AppPrefs.save();
            final List<Breakpoint> breakpoints = tableModel.getRows();
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> exportFile(pleaseWaitDialog, file, breakpoints))
                    .start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }

    private void exportFile(final PleaseWaitDialog pleaseWaitDialog,
                            final File file, final List<Breakpoint> breakpoints) {

        final List<List<String>> rows = new ArrayList<>();
        for (final Breakpoint breakpoint : breakpoints) {
            final List<String> row = new ArrayList<>();
            rows.add(row);
            row.add(Boolean.toString(breakpoint.isEnabled()));
            row.add(BreakpointTableModel.TYPE_NAMES[breakpoint.getType()]);
            row.add(String.format("%02X", breakpoint.getBank()));
            row.add(String.format("%04X", breakpoint.getStartAddress()));
            row.add(String.format("%04X", breakpoint.getEndAddress()));
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
            displayError(this, "Failed to export breakpoints.");
        }
        App.setNoStepPause(false);
    }

    @Override
    protected void importButtonPressed() {
        App.setNoStepPause(true);
        final JFileChooser chooser = createFileChooser("Import Breakpoints",
                AppPrefs.getInstance().getPaths().getBreakpointsDir(),
                AddressLabelDialog.FILE_FILTERS);
        if (showOpenDialog(this, chooser, (p, d) -> p.setBreakpointsDir(d))
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

        final List<Breakpoint> rows = new ArrayList<>();
        List<List<String>> values = null;
        try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
            values = CsvUtil.read(br);
            for (final List<String> list : values) {
                if (list.size() < 5) {
                    continue;
                }
                final boolean enabled = Boolean.parseBoolean(list.get(ENABLED));
                final int type = BreakpointTableModel.getType(list.get(TYPE));
                final int bank = parseInt(list.get(BANK), true, 0xFF);
                final int startAddress = parseInt(list.get(START_ADDRESS), true,
                        0xFFFF);
                final int endAddress = parseInt(list.get(END_ADDRESS), true, 0xFFFF);
                if (startAddress >= 0 && type >= 0) {
                    rows.add(new Breakpoint(type, bank, startAddress, endAddress,
                            enabled));
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
    protected void deleteButtonPressed(int index) {
        tableModel.delete(index);
    }

    @Override
    protected void editButtonPressed(int index) {
        final CreateBreakpointDialog dialog = new CreateBreakpointDialog(this);
        dialog.setTitle("Edit Breakpoint");
        dialog.setBreakpoint(tableModel.getRows().get(index));
        dialog.setVisible(true);
        if (dialog.isOk()) {
            tableModel.delete(index);
            setSelectedRow(tableModel.add(dialog.getBreakpoint()));
        }
    }

    @Override
    protected void newButtonPressed() {
        final CreateBreakpointDialog dialog = new CreateBreakpointDialog(this);
        dialog.setTitle("New Breakpoint");
        dialog.setVisible(true);
        if (dialog.isOk()) {
            setSelectedRow(tableModel.add(dialog.getBreakpoint()));
        }
    }
}