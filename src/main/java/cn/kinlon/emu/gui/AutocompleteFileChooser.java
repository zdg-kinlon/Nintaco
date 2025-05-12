package cn.kinlon.emu.gui;

import cn.kinlon.emu.gui.archive.EntryElement;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.task.TaskScheduler;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import static java.awt.event.KeyEvent.*;
import static cn.kinlon.emu.files.FileUtil.isDirectory;
import static cn.kinlon.emu.utils.GuiUtil.*;

public class AutocompleteFileChooser extends JFileChooser {

    private static final KeyStroke ESCAPE_STROKE
            = KeyStroke.getKeyStroke(VK_ESCAPE, 0);
    private final TaskScheduler scheduler = new TaskScheduler();
    private Popup popup;
    private final KeyEventDispatcher escDispatcher = e -> {
        if (e.getID() == KEY_PRESSED && e.getKeyCode() == VK_ESCAPE) {
            if (popup != null) {
                hidePopup();
            } else {
                cancelSelection();
            }
        }
        return false;
    };
    private JTextField textField;
    private JList<EntryElement> list;
    private String searchText = "";
    private File oldDir;
    public AutocompleteFileChooser(
            final String title,
            final String directory,
            final FileFilter... fileFilters) {
        this(title, directory == null ? null : new File(directory), fileFilters);
    }

    public AutocompleteFileChooser(
            final String title,
            final File directory,
            final FileFilter... fileFilters) {

        setDialogTitle(title);
        setAcceptAllFileFilterUsed(false);
        setMultiSelectionEnabled(false);
        if (fileFilters != null) {
            for (final FileFilter filter : fileFilters) {
                addChoosableFileFilter(filter);
            }
        }
        if (directory != null && directory.exists()
                && isDirectory(directory.getPath())) {
            setCurrentDirectory(directory);
        }
        if (fileFilters != null && fileFilters.length > 0) {
            setFileFilter(fileFilters[0]);
        }

        scaleFonts(this);

        try {
            final ActionMap actionMap = getActionMap();
            if (actionMap != null) {
                final Action details = actionMap.get("viewTypeDetails");
                if (details != null) {
                    details.actionPerformed(null);
                }
            }
        } catch (final Throwable t) {
        }

        Dimension size = AppPrefs.getInstance().getView().getFileChooserSize();
        if (size == null) {
            size = getPreferredSize();
            if (size != null) {
                size.width *= 1.25;
                size.height *= 1.5;
            }
        }
        if (size != null) {
            setPreferredSize(size);
        }

        final List<JTable> tables = findComponents(JTable.class, this);
        if (!tables.isEmpty()) {
            final JTable table = tables.get(0);
            Container parent = table.getParent();
            if (parent instanceof JViewport viewport) {
                parent = viewport.getParent();
                if (parent instanceof JScrollPane) {
                    ((JScrollPane) parent).setVerticalScrollBarPolicy(JScrollPane
                            .VERTICAL_SCROLLBAR_ALWAYS);
                }
            }
            table.addPropertyChangeListener("columnModel",
                    new JFileChooserColumnWidthAdjuster(table));
            final TableModel model = table.getModel();
            model.addTableModelListener(e -> {
                if (oldDir != null && e.getFirstRow() >= 0 && e.getLastRow() >= 0) {
                    EventQueue.invokeLater(() -> {
                        for (int row = model.getRowCount() - 1; row >= 0; --row) {
                            if (oldDir.equals(model.getValueAt(row, 0))) {
                                scrollToCenter(table, row);
                                final ListSelectionModel selectionModel
                                        = table.getSelectionModel();
                                if (selectionModel != null) {
                                    table.requestFocusInWindow();
                                    selectionModel.setSelectionInterval(row, row);
                                }
                                break;
                            }
                        }
                        oldDir = null;
                    });
                }
            });
        }

        final List<JTextField> textFields = findComponents(JTextField.class, this);
        if (!textFields.isEmpty()) {
            textField = textFields.get(textFields.size() - 1);
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(final FocusEvent e) {
                    hidePopup();
                }
            });
            textField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(final KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case VK_UP:
                            if (popup != null && list != null
                                    && list.getModel().getSize() > 0) {
                                if (list.getSelectedIndex() < 0) {
                                    list.setSelectedIndex(list.getModel().getSize() - 1);
                                } else if (list.getSelectedIndex() == 0) {
                                    list.clearSelection();
                                } else {
                                    list.setSelectedIndex(list.getSelectedIndex() - 1);
                                }
                                final EntryElement element = list.getSelectedValue();
                                if (element == null) {
                                    textField.setText(searchText);
                                } else {
                                    textField.setText(element.getEntry());
                                    list.ensureIndexIsVisible(list.getSelectedIndex());
                                }
                            }
                            break;
                        case VK_DOWN:
                            if (popup != null && list != null
                                    && list.getModel().getSize() > 0) {
                                if (list.getSelectedIndex() < 0) {
                                    list.setSelectedIndex(0);
                                } else if (list.getSelectedIndex()
                                        == list.getModel().getSize() - 1) {
                                    list.clearSelection();
                                } else {
                                    list.setSelectedIndex(list.getSelectedIndex() + 1);
                                }
                                final EntryElement element = list.getSelectedValue();
                                if (element == null) {
                                    textField.setText(searchText);
                                } else {
                                    textField.setText(element.getEntry());
                                    list.ensureIndexIsVisible(list.getSelectedIndex());
                                }
                            }
                            break;
                    }
                }

                @Override
                public void keyReleased(final KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case VK_UP:
                        case VK_DOWN:
                        case VK_ESCAPE:
                            return;
                    }
                    scheduler.cancelAll();
                    searchText = textField.getText().trim();
                    if (searchText.isEmpty()) {
                        if (popup != null) {
                            popup.hide();
                            popup = null;
                        }
                        return;
                    }
                    popup = null;
                }
            });
        }

        addPropertyChangeListener(e -> {
            if (DIRECTORY_CHANGED_PROPERTY.equals(e.getPropertyName())) {
                final File dir = (File) e.getOldValue();
                if (dir != null && dir.getParentFile().equals(e.getNewValue())) {
                    oldDir = dir;
                }
            }
        });
    }

    private void hidePopup() {
        scheduler.cancelAll();
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }

    private void disposeScheduler() {
        scheduler.dispose();
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }

    @Override
    public int showDialog(final Component parent, final String approveButtonText)
            throws HeadlessException {
        final KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager
                .getCurrentKeyboardFocusManager();
        Object binding = null;
        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        try {
            if (inputMap != null && inputMap.getParent() != null) {
                inputMap = inputMap.getParent();
                binding = inputMap.get(ESCAPE_STROKE);
                inputMap.remove(ESCAPE_STROKE);
            }
            keyboardFocusManager.addKeyEventDispatcher(escDispatcher);
            return super.showDialog(parent, approveButtonText);
        } finally {
            disposeScheduler();
            keyboardFocusManager.removeKeyEventDispatcher(escDispatcher);
            if (inputMap != null && binding != null) {
                inputMap.put(ESCAPE_STROKE, binding);
            }
        }
    }

    @Override
    public int showSaveDialog(final Component parent) throws HeadlessException {
        final KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager
                .getCurrentKeyboardFocusManager();
        Object binding = null;
        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        try {
            if (inputMap != null && inputMap.getParent() != null) {
                inputMap = inputMap.getParent();
                binding = inputMap.get(ESCAPE_STROKE);
                inputMap.remove(ESCAPE_STROKE);
            }
            keyboardFocusManager.addKeyEventDispatcher(escDispatcher);
            return super.showSaveDialog(parent);
        } finally {
            disposeScheduler();
            keyboardFocusManager.removeKeyEventDispatcher(escDispatcher);
            if (inputMap != null && binding != null) {
                inputMap.put(ESCAPE_STROKE, binding);
            }
        }
    }

    @Override
    public int showOpenDialog(final Component parent) throws HeadlessException {
        try {
            return super.showOpenDialog(parent);
        } finally {
            disposeScheduler();
        }
    }

    private static class JFileChooserColumnWidthAdjuster
            implements PropertyChangeListener {

        private final JTable table;

        public JFileChooserColumnWidthAdjuster(final JTable table) {
            this.table = table;
        }

        @Override
        public void propertyChange(final PropertyChangeEvent e) {
            table.removePropertyChangeListener("columnModel", this);
            table.setColumnModel((TableColumnModel) e.getOldValue());
            table.addPropertyChangeListener("columnModel", this);
        }
    }
}