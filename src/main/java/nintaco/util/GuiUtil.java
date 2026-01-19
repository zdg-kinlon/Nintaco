package nintaco.util;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;
import javax.swing.table.*;
import javax.swing.text.*;

import nintaco.*;
import nintaco.files.FileUtil;
import nintaco.gui.*;
import nintaco.gui.image.*;
import nintaco.gui.image.preferences.*;
import nintaco.preferences.*;

import static java.lang.Math.*;
import static javax.swing.SwingConstants.CENTER;
import static nintaco.files.FileUtil.*;
import static nintaco.util.MathUtil.*;
import static nintaco.util.StringUtil.*;

public final class GuiUtil {

    private static final String[] imageFileFormats
            = {"bmp", "gif", "jpg", "png"};

    private static final int SCREENSAVER_FRAMES = 1000;

    private static final Robot robot;
    private static int screensaverCounter;
    private static volatile boolean disableScreensaver;

    static {
        Robot r = null;
        try {
            r = new Robot();
        } catch (final Throwable t) {
            //t.printStackTrace();
        }
        robot = r;
    }

    private static final List<Image> icons = new ArrayList<>();

    static {
        for (final int i : new int[]{16, 20, 24, 32, 40, 48, 64, 96, 128, 256}) {
            icons.add(loadImage(String.format("/icons/icon-%d.png", i)));
        }
    }

    private static Map<Class, String> fontKeys = new HashMap<>();

    static {
        fontKeys.put(BasicArrowButton.class, "ArrowButton.font");
        fontKeys.put(JButton.class, "Button.font");
        fontKeys.put(JCheckBox.class, "CheckBox.font");
        fontKeys.put(JCheckBoxMenuItem.class, "CheckBoxMenuItem.font");
        fontKeys.put(JColorChooser.class, "ColorChooser.font");
        fontKeys.put(JComboBox.class, "ComboBox.font");
        fontKeys.put(JInternalFrame.JDesktopIcon.class, "DesktopIcon.font");
        fontKeys.put(JDesktopPane.class, "DesktopPane.font");
        fontKeys.put(JEditorPane.class, "EditorPane.font");
        fontKeys.put(JFileChooser.class, "FileChooser.font");
        fontKeys.put(JFormattedTextField.class, "FormattedTextField.font");
        fontKeys.put(JInternalFrame.class, "InternalFrame.font");
        fontKeys.put(BasicInternalFrameTitlePane.class,
                "InternalFrameTitlePane.font");
        fontKeys.put(JLabel.class, "Label.font");
        fontKeys.put(JList.class, "List.font");
        fontKeys.put(JMenu.class, "Menu.font");
        fontKeys.put(JMenuBar.class, "MenuBar.font");
        fontKeys.put(JMenuItem.class, "MenuItem.font");
        fontKeys.put(JOptionPane.class, "OptionPane.font");
        fontKeys.put(JPanel.class, "Panel.font");
        fontKeys.put(JPasswordField.class, "PasswordField.font");
        fontKeys.put(JPopupMenu.class, "PopupMenu.font");
        fontKeys.put(JPopupMenu.Separator.class, "PopupMenuSeparator.font");
        fontKeys.put(JProgressBar.class, "ProgressBar.font");
        fontKeys.put(JRadioButton.class, "RadioButton.font");
        fontKeys.put(JRadioButtonMenuItem.class, "RadioButtonMenuItem.font");
        fontKeys.put(JRootPane.class, "RootPane.font");
        fontKeys.put(JScrollBar.class, "ScrollBar.font");
        fontKeys.put(JScrollPane.class, "ScrollPane.font");
        fontKeys.put(JSeparator.class, "Separator.font");
        fontKeys.put(JSlider.class, "Slider.font");
        fontKeys.put(JSpinner.class, "Spinner.font");
        fontKeys.put(JSplitPane.class, "SplitPane.font");
        fontKeys.put(JTabbedPane.class, "TabbedPane.font");
        fontKeys.put(JTable.class, "Table.font");
        fontKeys.put(JTableHeader.class, "TableHeader.font");
        fontKeys.put(JTextArea.class, "TextArea.font");
        fontKeys.put(JTextField.class, "TextField.font");
        fontKeys.put(JTextPane.class, "TextPane.font");
        fontKeys.put(TitledBorder.class, "TitledBorder.font");
        fontKeys.put(JToggleButton.class, "ToggleButton.font");
        fontKeys.put(JToolBar.class, "ToolBar.font");
        fontKeys.put(JToolTip.class, "ToolTip.font");
        fontKeys.put(JTree.class, "Tree.font");
        fontKeys.put(JViewport.class, "Viewport.font");
    }

    public static class TableMouseAdapter extends MouseAdapter {

        private final JTable table;
        private final TableCellClickedListener listener;

        private int lastRowIndex;
        private int lastColumnIndex;

        public TableMouseAdapter(final JTable table,
                                 final TableCellClickedListener listener) {
            this.table = table;
            this.listener = listener;
        }

        private void fireMouseClicked(final int rowIndex, final int columnIndex) {
            listener.mouseClicked(table.convertRowIndexToModel(rowIndex),
                    table.convertColumnIndexToModel(columnIndex));
        }

        @Override
        public void mousePressed(final MouseEvent evt) {
            final int rowIndex = table.rowAtPoint(evt.getPoint());
            final int columnIndex = table.columnAtPoint(evt.getPoint());
            if (rowIndex >= 0 && columnIndex >= 0) {
                lastRowIndex = rowIndex;
                lastColumnIndex = columnIndex;
                fireMouseClicked(rowIndex, columnIndex);
            }
        }

        @Override
        public void mouseDragged(final MouseEvent evt) {
            final int rowIndex = table.rowAtPoint(evt.getPoint());
            final int columnIndex = table.columnAtPoint(evt.getPoint());
            if (rowIndex >= 0 && columnIndex >= 0 && (rowIndex != lastRowIndex
                    || columnIndex != lastColumnIndex)) {
                lastRowIndex = rowIndex;
                lastColumnIndex = columnIndex;
                fireMouseClicked(rowIndex, columnIndex);
            }
        }
    }

//  public static class CenteredHeaderRenderer implements TableCellRenderer {
//
//    private DefaultTableCellRenderer renderer;
//
//    public CenteredHeaderRenderer(final JTable table) {
//      renderer = (DefaultTableCellRenderer)table.getTableHeader()
//          .getDefaultRenderer();
//      renderer.setHorizontalAlignment(JLabel.CENTER);
//    }
//
//    @Override
//    public Component getTableCellRendererComponent(
//        final JTable table, final Object value, final boolean isSelected,
//            final boolean hasFocus, final int rowIndex, final int columnIndex) {
//      return renderer.getTableCellRendererComponent(
//          table, value, isSelected, hasFocus, rowIndex, columnIndex);
//    }
//  }

    public static class TableCellComboBoxRenderer extends JComboBox<String>
            implements TableCellRenderer {

        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public TableCellComboBoxRenderer(final String[] items) {
            super(items);
        }

        public TableCellComboBoxRenderer(final ComboBoxModel<String> model) {
            super(model);
        }

        @Override
        public Component getTableCellRendererComponent(final JTable table,
                                                       final Object value, final boolean isSelected, final boolean hasFocus,
                                                       final int rowIndex, final int columnIndex) {
            setSelectedItem(value);
            setBorder(noFocusBorder);
            return this;
        }
    }

    public static class RightLabelRenderer extends DefaultTableCellRenderer {

        public RightLabelRenderer() {
            setHorizontalAlignment(RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            setBorder(noFocusBorder);
            return this;
        }
    }

    ;

    public static class CenteredLabelRenderer extends DefaultTableCellRenderer {

        public CenteredLabelRenderer() {
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            setBorder(noFocusBorder);
            return this;
        }
    }

    ;

    public static class NoBorderLabelRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(final JTable table,
                                                       final Object value, final boolean isSelected, final boolean hasFocus,
                                                       final int row, final int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            setBorder(noFocusBorder);
            return this;
        }
    }

    ;

    public static class NoBorderListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(final JList<?> list,
                                                      final Object value, final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, value, index, isSelected,
                    false);
        }
    }

    ;

    public static class NoBorderMonospacedRenderer
            extends DefaultTableCellRenderer {

        private Font monospacedFont;

        @Override
        public Component getTableCellRendererComponent(final JTable table,
                                                       final Object value, final boolean isSelected, final boolean hasFocus,
                                                       final int rowIndex, final int columnIndex) {

            super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, rowIndex, columnIndex);
            final Font tableFont = table.getFont();
            if (monospacedFont == null
                    || monospacedFont.getSize() != tableFont.getSize()) {
                monospacedFont = new Font(Font.MONOSPACED, Font.PLAIN,
                        tableFont.getSize());
            }
            setFont(monospacedFont);
            setBorder(noFocusBorder);
            return this;
        }
    }

    public static class NoBorderBooleanRenderer extends JCheckBox
            implements TableCellRenderer {

        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public NoBorderBooleanRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setSelected(value != null && (boolean) value);
            setBorder(noFocusBorder);
            return this;
        }
    }

    private static class NoClearSelectionModel extends DefaultListSelectionModel {
        public NoClearSelectionModel() {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setSelectionInterval(0, 0);
        }

        @Override
        public void clearSelection() {
        }

        @Override
        public void removeSelectionInterval(int index0, int index1) {
        }
    }

    public static final NoBorderLabelRenderer NO_BORDER_LABEL_RENDERER
            = new NoBorderLabelRenderer();
    public static final NoBorderBooleanRenderer NO_BORDER_BOOLEAN_RENDERER
            = new NoBorderBooleanRenderer();
    public static final NoBorderMonospacedRenderer NO_BORDER_MONOSPACED_RENDERER
            = new NoBorderMonospacedRenderer();
    public static final CenteredLabelRenderer CENTERED_LABEL_RENDERER
            = new CenteredLabelRenderer();
    public static final RightLabelRenderer RIGHT_LABEL_RENDERER
            = new RightLabelRenderer();

    private GuiUtil() {
    }

    public static String[] getWritableImageFileFormats() {
        return imageFileFormats;
    }

    public static boolean isAlphaFormat(String fileFormat) {
        if (isBlank(fileFormat)) {
            return false;
        }
        fileFormat = fileFormat.trim().toLowerCase();
        return "png".equals(fileFormat) || "gif".equals(fileFormat);
    }

    public static void displayError(final Window parent,
                                    final String message, final Object... parameters) {
        displayError("Error", parent, message, parameters);
    }

    public static void displayError(final String title, final Window parent,
                                    final String message, final Object... parameters) {
        displayError(title, parent, String.format(message, parameters));
    }

    public static void displayError(final Window parent, final String message) {
        displayError("Error", parent, message);
    }

    public static void displayError(final String title, final Window parent,
                                    final String message) {
        displayMessage(title, parent, message, InformationDialog.IconType.ERROR);
    }

    public static void displayInformation(final String title, final Window parent,
                                          final String message) {
        displayMessage(title, parent, message,
                InformationDialog.IconType.INFORMATION);
    }

    public static void displayMessage(final String title, final Window parent,
                                      final String message, final InformationDialog.IconType iconType) {
        EDT.async(() -> {
            if (!isBlank(message)) {
                App.setNoStepPause(true);
                InformationDialog dialog = new InformationDialog(parent, message, title,
                        iconType);
                dialog.setVisible(true);
                App.setNoStepPause(false);
            }
        });
    }

    public static <T extends Component> List<T> findComponents(
            final Class<T> c, final Component component) {
        return findComponents(new ArrayList<>(), c, component);
    }

    public static <T extends Component> List<T> findComponents(
            final List<T> result, final Class<T> c, final Component component) {

        if (c.isAssignableFrom(component.getClass())) {
            result.add(c.cast(component));
        }

        if (component instanceof Container) {
            for (final Component child : ((Container) component).getComponents()) {
                findComponents(result, c, child);
            }
        }

        return result;
    }

    public static Font getDefaultFont(final Component component) {
        String key = null;
        Class<?> c = component.getClass();
        while (true) {
            key = fontKeys.get(c);
            if (key != null) {
                break;
            }
            c = c.getSuperclass();
            if (c == null || c == Component.class) {
                break;
            }
        }

        if (key == null) {
            final Font font = component.getFont();
            return font == null ? null : font.deriveFont(12f);
        } else {
            Font font = UIManager.getFont(key);
            if (font != null) {
                if (font.getSize() > 12) {
                    font = font.deriveFont(12f);
                }
                if (isMonospaced(component)) {
                    font = new Font(Font.MONOSPACED, font.getStyle(), font.getSize());
                }
            }
            return font;
        }
    }

    public static void scaleMenuItemFont(JMenuItem menuItem) {
        Font font = getDefaultFont(menuItem);
        if (font != null) {
            menuItem.setFont(font.deriveFont(font.getSize2D()
                    * AppPrefs.getInstance().getView().getFontScale()));
        }
    }

    public static void scaleFonts(final Component component) {
        if (component != null) {
            scaleFonts(component, AppPrefs.getInstance().getView().getFontScale());
        }
    }

    public static void scaleFonts(final Component component, final float scale) {
        if (component != null) {
            if (component instanceof Window) {
                ((Window) component).setIconImages(icons);
            }
            final Font font = getDefaultFont(component);
            if (font != null) {
                component.setFont(scaleFont(font, scale));
            }
            if (component instanceof JMenu) {
                scaleFonts(((JMenu) component).getPopupMenu(), scale);
            } else if (component instanceof JTable) {
                scaleFonts(((JTable) component).getTableHeader(), scale);
            }
            if (component instanceof JComponent) {
                final Border border = ((JComponent) component).getBorder();
                if (border instanceof TitledBorder) {
                    final Font f = UIManager.getFont("TitledBorder.font");
                    if (f != null) {
                        ((TitledBorder) border).setTitleFont(scaleFont(f, scale));
                    }
                }
            }
            if (component instanceof Container) {
                for (final Component comp : ((Container) component).getComponents()) {
                    scaleFonts(comp, scale);
                }
            }
        }
    }

    public static Font scaleFont(final Font font) {
        return scaleFont(font, AppPrefs.getInstance().getView().getFontScale());
    }

    public static Font scaleFont(final Font font, final float scale) {
        return font.deriveFont(font.getSize2D() * scale);
    }

    public static void resizeFonts(Component component, float size) {
        if (component != null) {
            Font font = getDefaultFont(component);
            if (font != null) {
                component.setFont(font.deriveFont(size));
            }
            if (component instanceof JMenu) {
                resizeFonts(((JMenu) component).getPopupMenu(), size);
            } else if (component instanceof JTable) {
                resizeFonts(((JTable) component).getTableHeader(), size);
            }
            if (component instanceof Container) {
                for (Component comp : ((Container) component).getComponents()) {
                    resizeFonts(comp, size);
                }
            }
        }
    }

    public static void repaint(final Component component) {
        if (component != null) {
            component.repaint();
            if (component instanceof JMenu) {
                repaint(((JMenu) component).getPopupMenu());
            } else if (component instanceof JTable) {
                repaint(((JTable) component).getTableHeader());
            }
            if (component instanceof Container) {
                for (Component comp : ((Container) component).getComponents()) {
                    repaint(comp);
                }
            }
        }
    }

    public static void resizeCellSizes(JTable table,
                                       boolean includeVerticleScrollBarWidth,
                                       int minimumVisibleRows, boolean limitVisibleRowsToModelSize,
                                       Object... prototypeValues) {
        resizeCellSizes(table, true, includeVerticleScrollBarWidth,
                minimumVisibleRows, limitVisibleRowsToModelSize, prototypeValues);
    }

    public static void resizeCellSizes(JTable table,
                                       boolean includeHeaderIcons, boolean includeVerticleScrollBarWidth,
                                       int minimumVisibleRows, boolean limitVisibleRowsToModelSize,
                                       Object... prototypeValues) {
        final Icon icon = UIManager.getIcon("Table.ascendingSortIcon");
        final TableColumnModel columnModel = table.getColumnModel();
        int height = 0;
        int totalWidth = 0;
        for (int column = 0; column < table.getColumnCount(); column++) {
            final TableColumn col = columnModel.getColumn(column);
            int width = 0;
            if (column < prototypeValues.length) {
                TableCellRenderer cellRenderer = col.getCellRenderer();
                if (cellRenderer == null) {
                    cellRenderer = table.getDefaultRenderer(
                            table.getModel().getColumnClass(column));
                }
                final JComponent comp = (JComponent) cellRenderer
                        .getTableCellRendererComponent(table, prototypeValues[column],
                                false, false, 0, column);
                scaleFonts(comp);
                Dimension size = comp.getPreferredSize();
                width = size.width;
                height = Math.max(size.height, height);
            }
            TableCellRenderer renderer = col.getHeaderRenderer();
            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
            final Component comp = renderer.getTableCellRendererComponent(table,
                    col.getHeaderValue(), false, false, 0, 0);
            if (includeHeaderIcons && comp instanceof JLabel) {
                ((JLabel) comp).setIcon(icon);
            }
            scaleFonts(comp);
            final Dimension size = comp.getPreferredSize();
            width = Math.max(size.width, width) + 4;
            col.setPreferredWidth(width);
            col.setMinWidth(width);
            col.setWidth(width);
            totalWidth += width;
        }
        if (table.getAutoResizeMode() == JTable.AUTO_RESIZE_LAST_COLUMN) {
            columnModel.getColumn(columnModel.getColumnCount() - 1)
                    .setPreferredWidth(100000);
        }
        table.setRowHeight(height);
        if (includeVerticleScrollBarWidth) {
            JViewport parent = (JViewport) table.getParent();
            JScrollPane scrollPane = (JScrollPane) parent.getParent();
            totalWidth += scrollPane.getVerticalScrollBar().getPreferredSize().width;
        }
        table.setPreferredScrollableViewportSize(new Dimension(
                totalWidth, (limitVisibleRowsToModelSize ? Math.min(minimumVisibleRows,
                table.getRowCount()) : minimumVisibleRows) * height));
    }

    public static void resizeCellSizes(final JTable table) {
        resizeCellSizes(table, false, 10, true);
    }

    // assumes JTextArea is within a JScrollPane
    public static void setTextAreaSize(final JTextArea textArea,
                                       final int rows, final int columns) {
        final JScrollPane scrollPane = (JScrollPane) ((JViewport) textArea
                .getParent()).getParent();
        final Insets i1 = scrollPane.getInsets();
        final Insets i2 = textArea.getInsets();
        final FontMetrics metrics = textArea.getFontMetrics(textArea.getFont());
        scrollPane.setPreferredSize(new Dimension(metrics.charWidth('M') * columns
                + scrollPane.getVerticalScrollBar().getPreferredSize().width
                + i1.left + i1.right + i2.left + i2.right,
                metrics.getHeight() * rows + scrollPane.getHorizontalScrollBar()
                        .getPreferredSize().height + i1.top + i1.bottom + i2.top
                        + i2.bottom));
    }

    // assumes Component is within a JScrollPane that is within a JPanel
    public static void setPanelPreferredSize(final Component component) {
        final Dimension size = component.getPreferredSize();
        final JScrollPane scrollPane = (JScrollPane) ((JViewport) component
                .getParent()).getParent();
        final JPanel panel = (JPanel) scrollPane.getParent();
        panel.setPreferredSize(new Dimension(5 + size.width + scrollPane
                .getVerticalScrollBar().getPreferredSize().width, 5 + size.height
                + scrollPane.getHorizontalScrollBar().getPreferredSize().height));
    }

    public static void resizeCellSizes(final JTable table,
                                       final boolean includeVerticleScrollBarWidth, final int minimumVisibleRows,
                                       final boolean limitVisibleRowsToModelSize) {
        if (table.getRowCount() == 0) {
            return;
        }
        final Icon icon = UIManager.getIcon("Table.ascendingSortIcon");
        final TableColumnModel columnModel = table.getColumnModel();
        int height = 0;
        int totalWidth = 0;
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 0;
            for (int row = table.getRowCount() - 1; row >= 0; row--) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                scaleFonts(comp);
                Dimension size = comp.getPreferredSize();
                width = Math.max(size.width, width);
                height = Math.max(size.height, height);
            }
            TableColumn col = columnModel.getColumn(column);
            TableCellRenderer renderer = col.getHeaderRenderer();
            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
            Component comp = renderer.getTableCellRendererComponent(table,
                    col.getHeaderValue(), false, false, 0, 0);
            if (comp instanceof JLabel) {
                ((JLabel) comp).setIcon(icon);
            }
            scaleFonts(comp);
            Dimension size = comp.getPreferredSize();
            width = Math.max(size.width, width);
            col.setPreferredWidth(width + 4);
            col.setMinWidth(width + 4);
            col.setWidth(width + 4);
            totalWidth += width;
        }
        table.setRowHeight(height);
        if (includeVerticleScrollBarWidth) {
            JViewport parent = (JViewport) table.getParent();
            JScrollPane scrollPane = (JScrollPane) parent.getParent();
            totalWidth += scrollPane.getVerticalScrollBar().getPreferredSize().width;
        }
        table.setPreferredScrollableViewportSize(new Dimension(
                totalWidth, (limitVisibleRowsToModelSize ? Math.min(minimumVisibleRows,
                table.getRowCount()) : minimumVisibleRows) * height));
    }

    public static void disableCellBorder(JTable table) {
        table.setDefaultRenderer(String.class, NO_BORDER_LABEL_RENDERER);
        table.setDefaultRenderer(Boolean.class, NO_BORDER_BOOLEAN_RENDERER);
    }

    public static void forceNoClearRowSelect(JTable table) {
        table.setSelectionModel(new NoClearSelectionModel());
    }

    public static boolean executeMessageTask(final Window window,
                                             final PleaseWaitDialog pleaseWaitDialog, final MessageTask task) {
        String errorMessage = null;
        try {
            task.execute();
        } catch (final MessageException e) {
            errorMessage = e.getMessage();
        } finally {
            pleaseWaitDialog.dispose();
            App.setNoStepPause(false);
        }
        if (errorMessage != null) {
            displayError(window, errorMessage);
            return false;
        } else {
            return true;
        }
    }

    public static void toFront(final JDialog dialog) {
        EDT.async(() -> {
            dialog.setVisible(true);
            dialog.setAlwaysOnTop(true);
            dialog.toFront();
            dialog.requestFocus();
            dialog.setAlwaysOnTop(false);
        });
    }

    public static void toFront(final JFrame frame) {
        EDT.async(() -> {
            frame.setVisible(true);
            frame.setExtendedState(frame.getExtendedState() & ~JFrame.ICONIFIED);
            frame.setAlwaysOnTop(true);
            frame.toFront();
            frame.requestFocus();
            frame.setAlwaysOnTop(false);
        });
    }

    public static void setClipboardString(final Object value) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(value.toString()), null);
    }

    public static String getClipboardString() {
        String result = null;
        final Clipboard clipboard = Toolkit.getDefaultToolkit()
                .getSystemClipboard();
        final Transferable contents = clipboard.getContents(null);
        if (contents != null && contents.isDataFlavorSupported(
                DataFlavor.stringFlavor)) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (final Throwable t) {
            }
        }
        return result;
    }

    public static int showSaveDialog(
            final Component parent,
            final JFileChooser chooser) {
        return showSaveDialog(parent, chooser, null);
    }

    public static int showOpenDialog(
            final Component parent,
            final JFileChooser chooser) {
        return showOpenDialog(parent, chooser, null);
    }

    public static int showSaveDialog(
            final Component parent,
            final JFileChooser chooser,
            final BiConsumer<Paths, String> saveDirectoryConsumer) {
        return showFileChooser(parent, chooser, saveDirectoryConsumer,
                JFileChooser.SAVE_DIALOG);
    }

    public static int showOpenDialog(
            final Component parent,
            final JFileChooser chooser,
            final BiConsumer<Paths, String> saveDirectoryConsumer) {
        return showFileChooser(parent, chooser, saveDirectoryConsumer,
                JFileChooser.OPEN_DIALOG);
    }

    public static int showFileChooser(
            final Component parent,
            final JFileChooser chooser,
            final BiConsumer<Paths, String> saveDirectoryConsumer,
            final int dialogType) {
        chooser.setDialogType(dialogType);
        final int result = chooser.showDialog(parent, null);
        final AppPrefs prefs = AppPrefs.getInstance();
        prefs.getView().setFileChooserSize(chooser.getSize());
        if (result == JFileChooser.APPROVE_OPTION) {
            final Paths paths = prefs.getPaths();
            final File selectedFile = chooser.getSelectedFile();
            if (selectedFile != null) {
                final String dir = selectedFile.getParent();
                if (dir != null && saveDirectoryConsumer != null) {
                    paths.addRecentDirectory(dir);
                    saveDirectoryConsumer.accept(paths, dir);
                }
            }
        }
        AppPrefs.save();
        return result;
    }

    public static JFileChooser createFileChooser(
            final String title,
            final String directory,
            final FileFilter... fileFilters) {
        return new AutocompleteFileChooser(title, directory, fileFilters);
    }

    public static JFileChooser createFileChooser(
            final String title,
            final File directory,
            final FileFilter... fileFilters) {
        return new AutocompleteFileChooser(title, directory, fileFilters);
    }

    public static File showSaveAsDialog(final Window parent,
                                        final String saveDirectory, final String fileName,
                                        final String fileExtension, final FileFilter fileFilter,
                                        final boolean promptOverwrite) {
        return showSaveAsDialog(parent, saveDirectory, fileName, fileExtension,
                fileFilter, promptOverwrite, "Save As");
    }

    public static File showSaveAsDialog(final Window parent,
                                        final String saveDirectory, String fileName, final String fileExtension,
                                        final FileFilter fileFilter, final boolean promptOverwrite,
                                        final String title) {
        File saveDir = new File(saveDirectory);
        File file = null;
        while (true) {

            final JFileChooser chooser = new AutocompleteFileChooser(title, saveDir,
                    fileFilter);

            if (fileFilter == null) {
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.addChoosableFileFilter(new FileExtensionFilter(0,
                        "All directories"));
            }
            if (fileName == null) {
                chooser.setSelectedFile(new File(saveDirectory));
            } else {
                chooser.setSelectedFile(getFile(saveDir, fileName));
            }

            final int result = chooser.showSaveDialog(parent);
            AppPrefs.getInstance().getView().setFileChooserSize(chooser.getSize());
            AppPrefs.save();
            if (result == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
                fileName = file.getName();
                saveDir = file.getParentFile();
                if (!file.exists() && getFileExtension(file).trim().isEmpty()) {
                    String name = file.getPath();
                    if (!name.endsWith(".")) {
                        name += ".";
                    }
                    file = new File(name + fileExtension);
                }
                final boolean showPrompt = promptOverwrite && file.exists()
                        && fileFilter != null;
                YesNoDialog yesNoDialog = null;
                if (showPrompt) {
                    yesNoDialog = new YesNoDialog(parent, "Overwrite existing file?",
                            "Confirm File Replace");
                    yesNoDialog.setVisible(true);
                }
                if (!showPrompt || yesNoDialog.isYes()) {
                    return file;
                }
            } else {
                break;
            }
        }
        return null;
    }

    public static boolean confirmOverwrite(final Window parent, String fileName) {
        if (fileName == null) {
            return false;
        }
        fileName = fileName.trim();
        if (fileName.length() == 0) {
            return false;
        }
        if (new File(fileName).exists()) {
            final YesNoDialog yesNoDialog = new YesNoDialog(parent,
                    "Overwrite existing file?", "Confirm File Replace");
            yesNoDialog.setVisible(true);
            if (yesNoDialog.isNo()) {
                return false;
            }
        }
        return true;
    }

    public static void addLoseFocusListener(final Window window,
                                            final JTextField textField) {
        textField.addActionListener(e -> window.requestFocusInWindow());
    }

    public static void addLoseFocusListener(final Window window,
                                            final JSpinner spinner) {
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField()
                .addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            window.requestFocusInWindow();
                        }
                    }
                });
    }

    public static DocumentListener createDocumentListener(
            final Runnable runnable) {
        return new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                runnable.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                runnable.run();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                runnable.run();
            }
        };
    }

    public static void addTextFieldEditListener(final JTextField textField,
                                                final Runnable runnable) {
        textField.getDocument().addDocumentListener(
                createDocumentListener(runnable));
    }

    public static void addSpinnerEditListener(final JSpinner spinner,
                                              final Runnable runnable) {
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().getDocument()
                .addDocumentListener(createDocumentListener(runnable));
    }

    public static int getFirstVisibleRowIndex(final JTable table) {
        final TableModel model = table.getModel();
        if (model.getRowCount() == 0 || model.getColumnCount() == 0
                || !(table.getParent() instanceof JViewport)) {
            return 0;
        }
        final JViewport viewport = (JViewport) table.getParent();
        final Point p = viewport.getViewPosition();
        final Rectangle r = table.getCellRect(0, 0, true);
        if (r.height == 0) {
            return 0;
        }
        return MathUtil.roundUpDivision(p.y, r.height);
    }

    private static void scrollTo(final int y, final int rowCount,
                                 final JViewport viewport, final Rectangle r, final Rectangle viewRect) {
        viewport.setViewPosition(new Point(0,
                clamp(y, 0, max(0, rowCount * r.height - viewRect.height))));
    }

    public static void scrollToCenter(final JTable table, final int rowIndex) {
        final TableModel model = table.getModel();
        final int rowCount = model.getRowCount();
        if (rowCount == 0 || model.getColumnCount() == 0
                || !(table.getParent() instanceof JViewport)) {
            return;
        }
        final JViewport viewport = (JViewport) table.getParent();
        final Rectangle r = table.getCellRect(0, 0, true);
        final Point p = new Point(0, rowIndex * r.height);
        final Rectangle viewRect = viewport.getViewRect();

        scrollTo(p.y + (r.height - viewRect.height) / 2, rowCount, viewport, r,
                viewRect);
    }

    public static void scrollToCenter(final JList list, final int rowIndex) {
        final ListModel model = list.getModel();
        final int rowCount = model.getSize();
        if (rowCount == 0 || !(list.getParent() instanceof JViewport)) {
            return;
        }
        final JViewport viewport = (JViewport) list.getParent();
        final Rectangle r = list.getCellBounds(0, 0);
        final Point p = new Point(0, rowIndex * r.height);
        final Rectangle viewRect = viewport.getViewRect();

        scrollTo(p.y + (r.height - viewRect.height) / 2, rowCount, viewport, r,
                viewRect);
    }

    public static void scrollToVisible(final JTable table, final int rowIndex) {
        final TableModel model = table.getModel();
        final int rowCount = model.getRowCount();
        if (rowCount == 0 || model.getColumnCount() == 0
                || !(table.getParent() instanceof JViewport)) {
            return;
        }
        final JViewport viewport = (JViewport) table.getParent();
        final Rectangle r = table.getCellRect(0, 0, true);
        final Point p = new Point(0, rowIndex * r.height);
        final Rectangle viewRect = viewport.getViewRect();

        if (p.y < viewRect.y) {
            scrollTo(p.y, rowCount, viewport, r, viewRect);
        } else {
            final int py1 = p.y + r.height;
            final int vy1 = viewRect.y + viewRect.height;
            if (py1 > vy1) {
                scrollTo(py1 - viewRect.height, rowCount, viewport, r, viewRect);
            }
        }
    }

    // Similar to JList::ensureIndexIsVisible
    public static void scrollToVisible(final JList list, final int rowIndex) {
        final ListModel model = list.getModel();
        final int rowCount = model.getSize();
        if (rowCount == 0 || !(list.getParent() instanceof JViewport)) {
            return;
        }
        final JViewport viewport = (JViewport) list.getParent();
        final Rectangle r = list.getCellBounds(0, 0);
        final Point p = new Point(0, rowIndex * r.height);
        final Rectangle viewRect = viewport.getViewRect();

        if (p.y < viewRect.y) {
            scrollTo(p.y, rowCount, viewport, r, viewRect);
        } else {
            final int py1 = p.y + r.height;
            final int vy1 = viewRect.y + viewRect.height;
            if (py1 > vy1) {
                scrollTo(py1 - viewRect.height, rowCount, viewport, r, viewRect);
            }
        }
    }

    public static void scrollToRowIndex(final JTable table, final int rowIndex) {
        final TableModel model = table.getModel();
        final int rowCount = model.getRowCount();
        if (rowCount == 0 || model.getColumnCount() == 0
                || !(table.getParent() instanceof JViewport)) {
            return;
        }
        final JViewport viewport = (JViewport) table.getParent();
        final Rectangle r = table.getCellRect(0, 0, true);
        final Rectangle viewRect = viewport.getViewRect();
        scrollTo(rowIndex * r.height, rowCount, viewport, r, viewRect);
    }

    public static void makeMonospaced(final Component component) {
        component.setName(Font.MONOSPACED);
        component.setFont(new Font(Font.MONOSPACED, Font.PLAIN,
                getDefaultFont(component).getSize()));
    }

    public static boolean isMonospaced(final Component component) {
        final Font font = component.getFont();
        if (font == null) {
            return false;
        } else {
            return Font.MONOSPACED.equals(font.getFamily())
                    || Font.MONOSPACED.equals(component.getName());
        }
    }

    public static void limitTextFieldLength(final JTextField textField,
                                            final int maxLength) {
        textField.setDocument(new PlainDocument() {
            @Override
            public void insertString(final int offset, final String str,
                                     final AttributeSet attr) throws BadLocationException {
                if (str != null && getLength() + str.length() <= maxLength) {
                    super.insertString(offset, str, attr);
                }
            }
        });
    }

    public static int parseTextField(final JTextField textField,
                                     final int emptyValue, final int minValue, final int maxValue) {
        int value;
        try {
            value = Integer.parseInt(textField.getText().trim());
        } catch (Throwable t) {
            value = emptyValue;
        }
        if (value < minValue) {
            value = minValue;
        } else if (value > maxValue) {
            value = maxValue;
        }
        textField.setText(Integer.toString(value));
        return value;
    }

    public static int getCaretRow(final JTextArea textArea) {
        try {
            return textArea.getLineOfOffset(textArea.getCaretPosition());
        } catch (Throwable t) {
            return -1;
        }
    }

    public static int getCaretColumn(final JTextArea textArea, final int row) {
        try {
            return textArea.getCaretPosition() - textArea.getLineStartOffset(row);
        } catch (Throwable t) {
            return -1;
        }
    }

    public static void showCursor(final JTextArea textArea) {
        final Caret caret = textArea.getCaret();
        caret.setVisible(true);
        caret.setSelectionVisible(true);
    }

    public static void hideCursor(final JTextArea textArea) {
        textArea.getCaret().setVisible(false);
    }

    public static void setCellRenderer(final JTable table,
                                       final int startColumnIndex, final int endColumnIndex,
                                       final TableCellRenderer renderer) {
        for (int i = startColumnIndex; i <= endColumnIndex; i++) {
            setCellRenderer(table, i, renderer);
        }
    }

    public static void setCellRenderer(final JTable table, final int columnIndex,
                                       final TableCellRenderer renderer) {
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);
    }

    public static void createComboBoxCellEditorAndRenderer(final JTable table,
                                                           final int columnIndex, final String[] items) {
        final TableColumn column = table.getColumnModel().getColumn(columnIndex);
        column.setCellEditor(new DefaultCellEditor(new JComboBox<>(items)));
        column.setCellRenderer(new TableCellComboBoxRenderer(items));
    }

    public static void centerTableHeaders(final JTable table) {
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(JLabel.CENTER);
    }

    public static void addTableCellClickedListener(final JTable table,
                                                   final TableCellClickedListener listener) {
        final TableMouseAdapter adapter = new TableMouseAdapter(table, listener);
        table.addMouseListener(adapter);
        table.addMouseMotionListener(adapter);
    }

    public static void removeTransferActions(final JComponent component) {
        component.setTransferHandler(null);
    }

    public static IntPoint getScrollValues(final JScrollPane scrollPane) {
        return new IntPoint(scrollPane.getHorizontalScrollBar().getValue(),
                scrollPane.getVerticalScrollBar().getValue());
    }

    public static void setScrollValues(final JScrollPane scrollPane,
                                       final IntPoint values) {
        scrollPane.getHorizontalScrollBar().setValue(values.getX());
        scrollPane.getVerticalScrollBar().setValue(values.getY());
    }

    public static void enableAutoscroll(final JTextArea textArea) {
        ((DefaultCaret) textArea.getCaret()).setUpdatePolicy(
                DefaultCaret.ALWAYS_UPDATE);
    }

    public static <T extends Enum<T>> void populateComboBox(
            final JComboBox comboBox, final Class<T> c) {
        final DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
        for (final T enumValue : c.getEnumConstants()) {
            model.addElement(enumValue);
        }
        comboBox.setModel(model);
    }

    /*
     * Returns the insets of the screen, which are defined by any taskbars that
     * have been set up by the user. This function accounts for multi-monitor
     * setups. If a window is supplied, then the the monitor that contains the
     * window will be used. If a window is not supplied, then the primary monitor
     * will be used.
     */
    public static Insets getScreenInsets(final Window windowOrNull) {
        final Insets insets;
        if (windowOrNull == null) {
            insets = Toolkit.getDefaultToolkit().getScreenInsets(GraphicsEnvironment
                    .getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration());
        } else {
            insets = windowOrNull.getToolkit().getScreenInsets(
                    windowOrNull.getGraphicsConfiguration());
        }
        return insets;
    }

    /*
     * Returns the working area of the screen. (The working area excludes any
     * taskbars.) This function accounts for multi-monitor setups. If a window is
     * supplied, then the the monitor that contains the window will be used. If a
     * window is not supplied, then the primary monitor will be used.
     */
    public static Rectangle getScreenWorkingArea(final Window windowOrNull) {
        final Insets insets;
        final Rectangle bounds;
        if (windowOrNull == null) {
            final GraphicsEnvironment ge = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();
            insets = Toolkit.getDefaultToolkit().getScreenInsets(
                    ge.getDefaultScreenDevice().getDefaultConfiguration());
            bounds = ge.getDefaultScreenDevice().getDefaultConfiguration()
                    .getBounds();
        } else {
            GraphicsConfiguration gc = windowOrNull.getGraphicsConfiguration();
            insets = windowOrNull.getToolkit().getScreenInsets(gc);
            bounds = gc.getBounds();
        }
        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= (insets.left + insets.right);
        bounds.height -= (insets.top + insets.bottom);
        return bounds;
    }

    /*
     * Returns the total area of the screen. (The total area includes any
     * taskbars.) This function accounts for multi-monitor setups. If a window is
     * supplied, then the the monitor that contains the window will be used. If a
     * window is not supplied, then the primary monitor will be used.
     */
    public static Rectangle getScreenTotalArea(final Window windowOrNull) {
        final Rectangle bounds;
        if (windowOrNull == null) {
            final GraphicsEnvironment ge = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();
            bounds = ge.getDefaultScreenDevice().getDefaultConfiguration()
                    .getBounds();
        } else {
            final GraphicsConfiguration gc = windowOrNull.getGraphicsConfiguration();
            bounds = gc.getBounds();
        }
        return bounds;
    }

    public static void setDisableScreensaver(final boolean disableScreensaver) {
        GuiUtil.disableScreensaver = disableScreensaver;
    }

    public static void suppressScreensaver() {
        if (--screensaverCounter <= 0) {
            screensaverCounter = SCREENSAVER_FRAMES;
            if (disableScreensaver) {
                robot.mouseWheel(0);
            }
        }
    }

    public static void normalize(final Frame frame) {
        frame.setExtendedState(frame.getExtendedState() & ~Frame.MAXIMIZED_BOTH);
        EDT.async(() -> {
            frame.invalidate();
            frame.validate();
            frame.repaint();
        });
    }

    public static void maxipack(final Frame frame) {
        if (isMaximized(frame)) {
            frame.setExtendedState(JFrame.NORMAL);
            maximize(frame);
        } else {
            frame.pack();
        }
        EDT.async(() -> {
            frame.invalidate();
            frame.validate();
            frame.repaint();
        });
    }

    public static void maximize(final Frame frame) {
        frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
        EDT.async(() -> {
            frame.invalidate();
            frame.validate();
            frame.repaint();
        });
    }

    public static boolean isMaximized(final Frame frame) {
        return (frame.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
    }

    public static void scrollToBottom(final JTextArea textArea) {
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    public static void scrollToBottom(final JScrollPane scrollPane) {
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    public static void showAllRows(final JTable table) {
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
    }

    public static Cursor getCursor(final String name, final Point hotSpot) {
        return Toolkit.getDefaultToolkit().createCustomCursor(getImageIcon(name)
                .getImage(), hotSpot, name);
    }

    public static ImageIcon getImageIcon(final String name) {
        return new ImageIcon(FileUtil.getResourceAsURL(name));
    }

    public static BufferedImage loadImage(final String name) {
        try {
            return ImageIO.read(FileUtil.getResourceAsURL(name));
        } catch (final Throwable t) {
            //t.printStackTrace();
            return null;
        }
    }

    public static void setMetalLookAndFeel(final MetalTheme theme) {
        try {
            final Dimension imagePaneSize = App.getImageFrame().getImagePane()
                    .getPreferredSize();
            MetalLookAndFeel.setCurrentTheme(theme);
            UIManager.setLookAndFeel(new MetalLookAndFeel());
            final nintaco.gui.image.preferences.View view = AppPrefs.getInstance()
                    .getView();
            view.setLookAndFeelClassName(MetalLookAndFeel.class.getCanonicalName());
            view.setThemeClassName(theme.getClass().getCanonicalName());
            enableTableGridlines();
            updateFrameStyles();
            AppPrefs.save();
            resizeImagePane(imagePaneSize);
        } catch (final Throwable t) {
            //t.printStackTrace();
        }
    }

    public static void setLookAndFeel(final String className) {
        try {
            final Dimension imagePaneSize = App.getImageFrame().getImagePane()
                    .getPreferredSize();
            UIManager.setLookAndFeel(className);
            final nintaco.gui.image.preferences.View view = AppPrefs.getInstance()
                    .getView();
            view.setLookAndFeelClassName(className);
            view.setThemeClassName(null);
            enableTableGridlines();
            updateFrameStyles();
            AppPrefs.save();
            resizeImagePane(imagePaneSize);
        } catch (final Throwable t) {
            //t.printStackTrace();
        }
    }

    private static void resizeImagePane(final Dimension imagePaneSize) {
        EDT.async(() -> {
            final ImageFrame imageFrame = App.getImageFrame();
            final ImagePane imagePane = imageFrame.getImagePane();
            imagePane.setSize(imagePaneSize);
            imagePane.paneResized();
            imageFrame.pack();
        });
    }

    private static void enableTableGridlines() {
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        defaults.put("Table.disabled", false);
        defaults.put("Table.showGrid", true);
        defaults.put("Table.intercellSpacing", new Dimension(1, 1));
    }

    public static void updateFrameStyles() {
        for (final Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            scaleFonts(window);
            if (window instanceof StyleListener) {
                ((StyleListener) window).styleChanged();
            }
            window.pack();
            window.setSize(window.getSize());
            window.invalidate();
            window.validate();
            window.repaint();
        }
    }

    public static void requestVsync(final JFrame frame,
                                    final boolean enableVsync) {
        EDT.async(() -> {
            final boolean frameDisplayable = frame.isDisplayable();
            if (frameDisplayable) {
                frame.dispose();
            }
            try {
                final Class<?> c = Class.forName("com.sun.java.swing.SwingUtilities3");
                final Method m = c.getMethod("setVsyncRequested", Container.class,
                        boolean.class);
                m.invoke(c, frame, Boolean.valueOf(AppPrefs.getInstance()
                        .getUserInterfacePrefs().isUseVsync() ? enableVsync : false));
            } catch (final Throwable t) {
                //t.printStackTrace();
            }
            if (frameDisplayable) {
                toFront(frame);
            }
        });
    }

    public static void drawRect(final int[] screen, final int x, final int y,
                                final int width, final int height, final int color) {

        final int x2 = x + width - 1;
        final int y2 = y + height - 1;

        drawHorizontalLine(screen, x, x2, y, color);
        drawHorizontalLine(screen, x, x2, y2, color);
        if (height >= 2) {
            drawVerticalLine(screen, x, y + 1, y2 - 1, color);
            drawVerticalLine(screen, x2, y + 1, y2 - 1, color);
        }
    }

    public static void drawHorizontalLine(final int[] screen, int x1, int x2,
                                          int y, final int color) {

        if (y < 0 || y > 239) {
            return;
        }
        y <<= 8;
        if (x1 > x2) {
            final int t = x1;
            x1 = x2;
            x2 = t;
        }
        if (x2 < 0 || x1 > 255) {
            return;
        }
        final int xMin = x1 < 0 ? 0 : x1;
        final int xMax = x2 > 255 ? 255 : x2;
        for (int x = xMin; x <= xMax; x++) {
            screen[y | x] = color;
        }
    }

    public static void drawVerticalLine(final int[] screen, int x, int y1,
                                        int y2, final int color) {

        if (x < 0 || x > 255) {
            return;
        }
        if (y1 > y2) {
            final int t = y1;
            y1 = y2;
            y2 = t;
        }
        if (y2 < 0 || y1 > 239) {
            return;
        }
        final int yMin = y1 < 0 ? 0 : y1;
        final int yMax = y2 > 239 ? 239 : y2;
        for (int y = yMin; y <= yMax; y++) {
            screen[(y << 8) | x] = color;
        }
    }

    public static boolean sharesGraphicsDevice(final Component c1,
                                               final Component c2) {
        return c1 != null && c2 != null && Objects.equals(c1
                .getGraphicsConfiguration().getDevice(), c2.getGraphicsConfiguration()
                .getDevice());
    }

    public static void moveToImageFrameMonitor(final Window window) {
        final ImageFrame imageFrame = App.getImageFrame();
        if (imageFrame != null) {
            final GraphicsDevice screenDevice = imageFrame.getGraphicsConfiguration()
                    .getDevice();
            if (screenDevice != null) {
                final Rectangle screenBounds = screenDevice.getDefaultConfiguration()
                        .getBounds();
                window.setLocation(screenBounds.x, screenBounds.y);
            }
        }
    }

    public static GraphicsDevice getNextGraphicsDevice(final Window window) {

        // The array returned by getScreenDevices() is shared, hence the need to
        // copy it before sorting.
        final GraphicsDevice[] sds = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getScreenDevices();
        final GraphicsDevice[] screenDevices = new GraphicsDevice[sds.length];
        System.arraycopy(sds, 0, screenDevices, 0, sds.length);
        Arrays.sort(screenDevices, (a, b) -> a.getIDstring().compareToIgnoreCase(
                b.getIDstring()));

        final GraphicsDevice graphicsDevice = window.getGraphicsConfiguration()
                .getDevice();
        int index = -1;
        for (int i = screenDevices.length - 1; i >= 0; --i) {
            if (Objects.equals(screenDevices[i], graphicsDevice)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            return null;
        }

        for (int i = 1; i < screenDevices.length; ++i) {
            final GraphicsDevice device = screenDevices[(index + i)
                    % screenDevices.length];
            if (device.isFullScreenSupported()) {
                return device;
            }
        }

        return null;
    }

    public static GraphicsDevice moveToGraphicsDevice(final Window window,
                                                      final GraphicsDevice device) {

        if (window == null || device == null) {
            return null;
        }

        final GraphicsDevice windowDevice = window.getGraphicsConfiguration()
                .getDevice();
        if (Objects.equals(windowDevice, device)) {
            if (!window.isVisible()) {
                window.setVisible(true);
            }
            return windowDevice;
        }

        final Rectangle windowDeviceBounds = windowDevice.getDefaultConfiguration()
                .getBounds();
        final Rectangle devicebounds = device.getDefaultConfiguration().getBounds();
        final Point p = window.getLocation();

        window.dispose();
        window.setLocation(
                (int) round(devicebounds.getX() + ((p.getX() - windowDeviceBounds.getX())
                        * devicebounds.width) / windowDeviceBounds.width),
                (int) round(devicebounds.getY() + ((p.getY() - windowDeviceBounds.getY())
                        * devicebounds.height) / windowDeviceBounds.height));
        window.setVisible(true);

        return windowDevice;
    }

    public static void forwardKeyEvents(final Component from,
                                        final Component to) {
        from.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(final KeyEvent e) {
                e.setSource(to);
                to.dispatchEvent(e);
            }

            @Override
            public void keyPressed(final KeyEvent e) {
                e.setSource(to);
                to.dispatchEvent(e);
            }

            @Override
            public void keyReleased(final KeyEvent e) {
                e.setSource(to);
                to.dispatchEvent(e);
            }
        });
    }
}