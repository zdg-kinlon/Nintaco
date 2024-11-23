package nintaco.gui;

import nintaco.gui.archive.EntryElement;
import nintaco.gui.archive.SearchTask;
import nintaco.preferences.AppPrefs;
import nintaco.task.TaskScheduler;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.awt.event.KeyEvent.*;
import static java.lang.Math.min;
import static javax.swing.SwingUtilities.getWindowAncestor;
import static nintaco.files.FileUtil.isDirectory;
import static nintaco.util.GuiUtil.*;

public class AutocompleteFileChooser extends JFileChooser {

    private static final KeyStroke ESCAPE_STROKE
            = KeyStroke.getKeyStroke(VK_ESCAPE, 0);
    private final PopupFactory popupFactory = PopupFactory.getSharedInstance();
    private final TaskScheduler scheduler = new TaskScheduler();
    private Window window;
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
                    scheduler.add(new AutocompleteSearchTask(popup, getCurrentDirectory(),
                            getFileFilter(), searchText));
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

    private class AutocompleteSearchTask extends SearchTask {

        private final File directory;
        private final FileFilter fileFilter;

        private Popup popup;

        public AutocompleteSearchTask(
                final Popup popup,
                final File directory,
                final FileFilter fileFilter,
                final String searchStr) {
            super(new JList<>(), new ArrayList<>(), searchStr);
            this.directory = directory;
            this.fileFilter = fileFilter;
            this.popup = popup;
        }

        @Override
        public void loop() {
            if (directory != null) {
                final File[] files = directory.listFiles();
                for (int i = files.length - 1; i >= 0; --i) {
                    final File file = files[i];
                    if (fileFilter == null || fileFilter.accept(file)) {
                        entries.add(file.getName());
                    }
                }
                Collections.sort(entries, String.CASE_INSENSITIVE_ORDER);
            }
            super.loop();
            if (canceled) {
                EventQueue.invokeLater(() -> {
                    if (popup != null) {
                        popup.hide();
                        popup = null;
                    }
                });
            }
        }

        @Override
        protected void updateModel(final DefaultListModel<EntryElement> model) {
            if (canceled || AutocompleteFileChooser.this.popup != null) {
                if (popup != null) {
                    popup.hide();
                    popup = null;
                }
                return;
            }
            super.updateModel(model);
            if (model.getSize() == 0) {
                if (popup != null) {
                    popup.hide();
                    popup = null;
                }
                return;
            }
            list.clearSelection();

            final Window window = getWindowAncestor(AutocompleteFileChooser.this);
            if (window != null && AutocompleteFileChooser.this.window == null) {
                AutocompleteFileChooser.this.window = window;
                window.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowLostFocus(final WindowEvent e) {
                        hidePopup();
                    }

                    @Override
                    public void windowDeactivated(final WindowEvent e) {
                        hidePopup();
                    }

                    @Override
                    public void windowIconified(final WindowEvent e) {
                        hidePopup();
                    }

                    @Override
                    public void windowClosing(final WindowEvent e) {
                        hidePopup();
                    }
                });
                window.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentHidden(final ComponentEvent e) {
                        hidePopup();
                    }

                    @Override
                    public void componentMoved(final ComponentEvent e) {
                        hidePopup();
                    }

                    @Override
                    public void componentResized(final ComponentEvent e) {
                        hidePopup();
                    }
                });
            }

            list.setVisibleRowCount(min(15, model.getSize()));
            final JScrollPane scrollPane = new JScrollPane(list);
            scaleFonts(scrollPane);

            final Rectangle workingArea = getScreenWorkingArea(window);
            final Dimension size = scrollPane.getPreferredSize();
            final Point origin = textField.getLocationOnScreen();
            int y = origin.y + textField.getPreferredSize().height - 1;
            if (y + size.height > workingArea.y + workingArea.height) {
                y = origin.y - size.height + 1;
            }

            if (popup != null) {
                popup.hide();
                popup = null;
            }
            if (canceled) {
                return;
            }

            popup = AutocompleteFileChooser.this.popup = popupFactory.getPopup(
                    AutocompleteFileChooser.this, scrollPane, origin.x, y);
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(final MouseEvent e) {
                    final int index = list.locationToIndex(e.getPoint());
                    if (index < 0) {
                        list.clearSelection();
                        return;
                    }
                    list.setSelectedIndex(index);
                    final EntryElement element = list.getSelectedValue();
                    if (element != null) {
                        hidePopup();
                        textField.setText(element.getEntry());
                        textField.setSelectionStart(0);
                        textField.setSelectionEnd(element.getEntry().length());
                    }
                }

                @Override
                public void mouseExited(final MouseEvent e) {
                    list.clearSelection();
                }
            });
            list.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(final MouseEvent e) {
                    final int index = list.locationToIndex(e.getPoint());
                    if (index < 0) {
                        list.clearSelection();
                        return;
                    }
                    list.setSelectedIndex(index);
                }
            });
            AutocompleteFileChooser.this.list = list;
            popup.show();
        }
    }
}