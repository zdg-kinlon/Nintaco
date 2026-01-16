package cn.kinlon.emu.utils;

import cn.kinlon.emu.gui.image.preferences.View;
import cn.kinlon.emu.App;
import cn.kinlon.emu.files.FileUtil;
import cn.kinlon.emu.gui.AutocompleteFileChooser;
import cn.kinlon.emu.gui.StyleListener;
import cn.kinlon.emu.gui.image.ImageFrame;
import cn.kinlon.emu.gui.image.ImagePane;
import cn.kinlon.emu.gui.image.preferences.Paths;
import cn.kinlon.emu.preferences.AppPrefs;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

import static java.lang.Math.max;
import static java.lang.Math.round;
import static cn.kinlon.emu.utils.MathUtil.clamp;

public final class GuiUtil {

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

    private static final Map<Class, String> fontKeys = new HashMap<>();

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

    private GuiUtil() {
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

    public static void disableCellBorder(JTable table) {
        table.setDefaultRenderer(String.class, NO_BORDER_LABEL_RENDERER);
        table.setDefaultRenderer(Boolean.class, NO_BORDER_BOOLEAN_RENDERER);
    }

    public static void forceNoClearRowSelect(JTable table) {
        table.setSelectionModel(new NoClearSelectionModel());
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
        StringSelection stringSelection = new StringSelection(value.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
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

    public static DocumentListener createDocumentListener(final Runnable runnable) {
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

    public static void addTextFieldEditListener(final JTextField textField, final Runnable runnable) {
        textField.getDocument().addDocumentListener(createDocumentListener(runnable));
    }

    private static void scrollTo(final int y, final int rowCount, final JViewport viewport, final Rectangle r, final Rectangle viewRect) {
        int pY = clamp(y, 0, max(0, rowCount * r.height - viewRect.height));
        Point p = new Point(0, pY);
        viewport.setViewPosition(p);
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

    public static boolean isMonospaced(final Component component) {
        final Font font = component.getFont();
        if (font == null) {
            return false;
        } else {
            return Font.MONOSPACED.equals(font.getFamily())
                    || Font.MONOSPACED.equals(component.getName());
        }
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
            final View view = AppPrefs.getInstance()
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
            final View view = AppPrefs.getInstance()
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