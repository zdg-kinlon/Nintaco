package cn.kinlon.emu.gui.hexeditor;

import cn.kinlon.emu.App;
import cn.kinlon.emu.Machine;
import cn.kinlon.emu.files.FdsFile;
import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.files.UnifFile;
import cn.kinlon.emu.gui.StyleListener;
import cn.kinlon.emu.gui.hexeditor.preferences.Bookmark;
import cn.kinlon.emu.gui.hexeditor.preferences.HexEditorAppPrefs;
import cn.kinlon.emu.gui.hexeditor.preferences.HexEditorGamePrefs;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.preferences.GamePrefs;
import cn.kinlon.emu.utils.EDT;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashSet;
import java.util.Set;

import static cn.kinlon.emu.gui.hexeditor.DataSource.*;
import static cn.kinlon.emu.utils.GuiUtil.*;
import static cn.kinlon.emu.utils.MathUtil.roundUpDivision;

public class HexEditorView extends JComponent implements StyleListener {

    static final int MARGIN = 1;

    private final DataSource[] dataSources = new DataSource[3];
    private final Rectangle clip = new Rectangle();

    private Font font;
    private HexEditorFrame hexEditorFrame;
    private Machine machine;
    private Color[] changedColors;
    private Color[] selectedColors;
    private FontMetrics metrics;
    private int charWidth = 1;
    private int charHeight = 1;
    private int charAscent;
    private int addressMaxX;
    private Dimension preferredSize = new Dimension(0, 0);
    private boolean selecting;
    private boolean editing;
    private boolean selectedText;
    private int writeValue;

    private volatile int startViewAddress;
    private volatile int endViewAddress;

    private DataSource dataSource;
    private CharTable charTable = new CharTable();
    private int fadeColor;
    private int editColor;
    private int bookmarkColor;
    private int editBookmarkColor;

    public HexEditorView() {

        createFadeColors();
        styleChanged();

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent e) {
                if (selecting) {
                    int address = convertCoordinatesIntoAddress(e.getX(), e.getY());
                    final DataSource source = dataSource;
                    if (address >= 0 && address != source.getEndSelectedAddress()) {
                        source.setEndSelectedAddress(address);
                        repaint();
                    }
                }
            }
        });
    }

    @Override
    public final void styleChanged() {
        font = scaleFont(new Font(Font.MONOSPACED, Font.PLAIN,
                getDefaultFont(new JTextArea("M")).getSize()));
        setFont(font);
        metrics = null;
    }

    public final void createFadeColors() {
        HexEditorAppPrefs prefs = AppPrefs.getInstance().getHexEditorPrefs();
        editColor = prefs.getFadeFrames();
        bookmarkColor = editColor + 1;
        editBookmarkColor = bookmarkColor + 1;
        fadeColor = editColor - 1;
        changedColors = new Color[editBookmarkColor + 1];
        selectedColors = new Color[changedColors.length];
        for (int i = fadeColor; i >= 0; i--) {
            int value = 255 * i / fadeColor;
            changedColors[i] = new Color(value / 2, value, value);
            value = 255 - 127 * i / fadeColor;
            selectedColors[i] = new Color(value, 255, 255);
        }
        selectedColors[editColor] = changedColors[editColor] = Color.RED;
        selectedColors[bookmarkColor] = changedColors[bookmarkColor]
                = selectedColors[editBookmarkColor] = changedColors[editBookmarkColor]
                = new Color(0xE57A00);
        repaint();
    }

    public void setHexEditorFrame(HexEditorFrame hexEditorFrame) {
        this.hexEditorFrame = hexEditorFrame;
    }

    public void keyPressed(final KeyEvent e) {
        if (selecting || (e.getModifiers() & (InputEvent.ALT_MASK
                | InputEvent.CTRL_MASK | InputEvent.META_MASK)) != 0) {
            return;
        }
        final DataSource source = dataSource;
        final int cacheSize = source.getSize();
        final int code = e.getExtendedKeyCode();
        final boolean shiftPressed
                = (e.getModifiers() & InputEvent.SHIFT_MASK) != 0;

        if (code == KeyEvent.VK_BACK_SPACE) {
            undo();
            return;
        }

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN
                || code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT) {

            int address = shiftPressed ? source.getEndSelectedAddress()
                    : source.getStartSelectedAddress();

            switch (code) {
                case KeyEvent.VK_UP:
                    if (address >= 16) {
                        address -= 16;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    address += 16;
                    break;
                case KeyEvent.VK_LEFT:
                    if (address > 0) {
                        address--;
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    address++;
                    break;
            }

            source.setEndSelectedAddress(address);
            if (!shiftPressed) {
                source.setStartSelectedAddress(address);
            }

            repaint();
            return;
        }

        if (selectedText) {
            char c = e.getKeyChar();
            if (shiftPressed) {
                c = Character.toUpperCase(c);
            }
            final int value = charTable.getValue(c);
            if (value >= 0) {
                write(source.getStartSelectedAddress(), value, source);
                source.setStartSelectedAddress(source.getStartSelectedAddress() + 1);
                if (source.getStartSelectedAddress() >= cacheSize) {
                    source.setStartSelectedAddress(cacheSize - 1);
                }
                source.setEndSelectedAddress(source.getStartSelectedAddress());
                repaint();
            }
        } else {
            int value = -1;
            if (code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9) {
                value = code - KeyEvent.VK_0;
            } else if (code >= KeyEvent.VK_NUMPAD0 && code <= KeyEvent.VK_NUMPAD9) {
                value = code - KeyEvent.VK_NUMPAD0;
            } else if (code >= KeyEvent.VK_A && code <= KeyEvent.VK_F) {
                value = code - (KeyEvent.VK_A - 10);
            }
            if (value >= 0) {
                if (editing) {
                    editing = false;
                    write(source.getStartSelectedAddress(),
                            (writeValue << 4) | value, source);
                    source.setStartSelectedAddress(source.getStartSelectedAddress() + 1);
                    if (source.getStartSelectedAddress() >= cacheSize) {
                        source.setStartSelectedAddress(cacheSize - 1);
                    }
                } else {
                    editing = true;
                    writeValue = value;
                }
                source.setEndSelectedAddress(source.getStartSelectedAddress());
                repaint();
            } else if (editing || code == KeyEvent.VK_ESCAPE) {
                editing = false;
                repaint();
            }
        }
    }

    public void update() {

        final DataSource source = dataSource;
        if (source == null || source.getIndex() == FileContents
                || source.getSize() == 0) {
            return;
        }

        int startAddress = startViewAddress;
        int endAddress = endViewAddress;
        if (startAddress > endAddress) {
            return;
        }
        final int[] cache = source.getCache();
        if (startAddress >= cache.length) {
            startAddress = cache.length - 1;
        } else if (startAddress < 0) {
            startAddress = 0;
        }
        if (endAddress >= cache.length) {
            endAddress = cache.length - 1;
        } else if (endAddress < 0) {
            endAddress = 0;
        }

        for (int i = startAddress; i <= endAddress; i++) {
            final int lastValue = cache[i] & 0xFF;
            final int color = (cache[i] >> 8) & 0xFF;
            final int value = source.peek(i);
            boolean changed = false;
            if (value != lastValue && color <= editColor) {
                cache[i] = (cache[i] & 0xFFFF0000) | (fadeColor << 8) | value;
                changed = true;
            } else if (color > 0 && color <= fadeColor) {
                cache[i] = (cache[i] & 0xFFFF00FF) | ((color - 1) << 8);
                changed = true;
            }
            if (changed) {
                final int nibble = i & 0x0F;
                final int x = MARGIN + (9 + 3 * nibble) * charWidth;
                final int y = MARGIN + (i >> 4) * charHeight;
                final int x2 = MARGIN + (60 + nibble) * charWidth;
                EDT.async(() -> repaint(x, y, charWidth << 1, charHeight));
                EDT.async(() -> repaint(x2, y, charWidth, charHeight));
            }
        }
    }

    private String getSelectedText(final boolean spaced) {
        final DataSource source = dataSource;
        int start = source.getStartSelectedAddress();
        int end = source.getEndSelectedAddress();
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        StringBuilder sb = new StringBuilder();
        final int[] cache = source.getCache();
        if (selectedText) {
            for (int i = start; i <= end && i < cache.length; i++) {
                if (i > start && spaced) {
                    sb.append(' ');
                }
                sb.append(charTable.getChar(cache[i] & 0xFF));
            }
        } else {
            for (int i = start; i <= end && i < cache.length; i++) {
                if (i > start && spaced) {
                    sb.append(' ');
                }
                sb.append(String.format("%02X", cache[i] & 0xFF));
            }
        }
        return sb.toString();
    }

    public void copy(final boolean spaced) {
        setClipboardString(getSelectedText(spaced));
    }

    public void paste() {
        final DataSource source = dataSource;
        if (source.getSize() == 0) {
            return;
        }
        paste(source, source.getStartSelectedAddress(), getClipboardString(),
                selectedText, true);
    }

    public void paste(final DataSource source, final int start, String text,
                      final boolean pasteText, final boolean modifySelection) {
        int[] values = null;
        boolean edited = false;
        if (pasteText) {
            values = new int[text.length()];
            for (int i = values.length - 1; i >= 0; i--) {
                final int address = start + i;
                final int value = charTable.getValue(text.charAt(i));
                final int cachedValue = source.readCache(address);
                final int color = (cachedValue & 0xFF00);
                final int priorValue = source.peek(address);
                values[i] = color | priorValue;
                if (value >= 0) {
                    source.write(address, value);
                    if (priorValue != source.peek(address)) {
                        edited = true;
                        source.writeCache(address, markEdited(value));
                    } else {
                        source.writeCache(address, color | (value & 0xFF));
                    }
                }
            }
        } else {
            text = text.replaceAll("\\s", "");
            values = new int[text.length() >> 1];
            for (int i = values.length - 1; i >= 0; i--) {
                final int address = start + i;
                final int offset = i << 1;
                int value = 0;
                try {
                    value = Integer.parseInt(text.substring(offset, offset + 2), 16);
                } catch (Throwable t) {
                }
                final int cachedValue = source.readCache(address);
                final int color = (cachedValue & 0xFF00);
                final int priorValue = source.peek(address);
                values[i] = color | priorValue;
                source.write(address, value);
                if (priorValue != source.peek(address)) {
                    edited = true;
                    source.writeCache(address, markEdited(value));
                } else {
                    source.writeCache(address, color | (value & 0xFF));
                }
            }
        }
        if (edited) {
            source.addEdit(new Edit(start, values));
        }
        if (modifySelection) {
            int address = start + values.length;
            if (address >= source.getSize()) {
                address = source.getSize() - 1;
            }
            if (address < 0) {
                address = 0;
            }
            source.setSelection(address);
        }
        repaint();
    }

    public void undo() {
        final DataSource source = dataSource;
        final int editIndex = source.getEditIndex() - 1;
        if (editIndex >= 0) {
            makeEdit(source, editIndex, false);
            source.setEditIndex(editIndex);
        }
    }

    public void redo() {
        final DataSource source = dataSource;
        final int editIndex = source.getEditIndex();
        if (editIndex < source.getEdits().size()) {
            makeEdit(source, editIndex, true);
            source.setEditIndex(editIndex + 1);
        }
    }

    private void makeEdit(final DataSource source,
                          final int editIndex, final boolean redo) {
        final Edit edit = source.getEdits().get(editIndex);
        final int start = edit.getAddress();
        final int[] values = edit.getValues();
        for (int i = values.length - 1; i >= 0; i--) {
            final int address = start + i;
            final int value = source.readColored(address);
            source.write(address, values[i]);
            source.writeCache(address, values[i]);
            values[i] = value;
        }
        int address = edit.getAddress();
        if (redo) {
            address += values.length;
        }
        if (address >= source.getSize()) {
            address = source.getSize() - 1;
        }
        if (address < 0) {
            address = 0;
        }
        source.setStartSelectedAddress(address);
        source.setEndSelectedAddress(address);
        centerAddressIfNotVisible(source, address);
        repaint();
    }

    public void selectAll() {
        final DataSource source = dataSource;
        source.setStartSelectedAddress(0);
        source.setEndSelectedAddress(source.getSize() - 1);
        repaint();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(final int index) {
        setDataSource(dataSources[index]);
    }

    private void setDataSource(final DataSource dataSource) {
        EDT.async(() -> {
            dataSource.refreshCache();
            this.dataSource = dataSource;
            metrics = null;
            editing = false;
            selecting = false;
            repaint();
        });
    }

    public void setCharTable(final CharTable charTable) {
        EDT.async(() -> {
            this.charTable = charTable;
            repaint();
        });
    }

    public void toggleBookmark() {
        final DataSource source = dataSource;
        if (source.isEmpty()) {
            return;
        }
        final int address = source.getSelectedAddress();
        final HexEditorGamePrefs prefs = GamePrefs.getInstance()
                .getHexEditorGamePrefs();
        if (prefs.containsBookmark(dataSource.getIndex(), address)) {
            prefs.removeBookmark(dataSource.getIndex(), address);
            bookmarksUpdated();
            return;
        }
    }

    private void bookmarksUpdated() {
        GamePrefs.save();
        hexEditorFrame.updateBookmarksMenu();
        colorBookmarks();
        repaint();
    }

    public void colorBookmarks() {
        Set<Integer>[] addresses = new Set[3];
        for (int i = addresses.length - 1; i >= 0; i--) {
            addresses[i] = new HashSet<>();
        }
        synchronized (GamePrefs.class) {
            for (Bookmark bookmark : GamePrefs.getInstance().getHexEditorGamePrefs()
                    .getBookmarks()) {
                addresses[bookmark.getDataSourceIndex()].add(bookmark.getAddress());
            }
        }

        for (int i = dataSources.length - 1; i >= 0; i--) {
            final int[] cache = dataSources[i].getCache();
            final Set<Integer> as = addresses[i];
            for (int j = cache.length - 1; j >= 0; j--) {
                int color = (cache[j] >> 8) & 0xFF;
                if (as.contains(j)) {
                    if (color <= editColor) {
                        color = (color == editColor) ? editBookmarkColor : bookmarkColor;
                    }
                } else if (color == editBookmarkColor) {
                    color = editColor;
                } else if (color == bookmarkColor) {
                    color = 0;
                }
                cache[j] = (color << 8) | (cache[j] & 0xFFFF00FF);
            }
        }
    }

    public void removeAllBookmarks() {
        GamePrefs.getInstance().getHexEditorGamePrefs().removeAllBookmarks();
        bookmarksUpdated();
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(final Machine machine) {

        this.machine = machine;
        if (machine == null) {
            for (int i = dataSources.length - 1; i >= 0; i--) {
                dataSources[i] = new NoDataSource(i);
            }
        } else {
            dataSources[CpuMemory] = new CpuDataSource(machine.getMapper());
            dataSources[PpuMemory] = new PpuDataSource(machine.getPPU());
            final NesFile nesFile = App.getNesFile();
            final UnifFile unifFile = App.getUnifFile();
            final FdsFile fdsFile = App.getFdsFile();
            if (nesFile != null) {
                dataSources[FileContents] = new FileDataSource(nesFile,
                        machine.getMapper());
            } else if (unifFile != null) {
                dataSources[FileContents] = new FileDataSource(unifFile);
            } else if (fdsFile != null) {
                dataSources[FileContents] = new FileDataSource(fdsFile);
            } else {
                dataSources[FileContents] = new NoDataSource(FileContents);
            }
        }
        setDataSource(dataSources[dataSource == null ? CpuMemory
                : dataSource.getIndex()]);
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    private int convertCoordinatesIntoAddress(int x, int y) {

        final int column = (x - MARGIN) / charWidth;
        final int row = (y - MARGIN) / charHeight;

        if (column >= 60 && column <= 75) {
            selectedText = true;
            return (row << 4) | (column - 60);
        } else if (column >= 9 && column <= 56) {
            selectedText = false;
            return (row << 4) | ((column - 9) / 3);
        }

        return -1;
    }

    public int getFileIndex() {
        final DataSource source = dataSource;
        if (source.getIndex() == FileContents || source.isEmpty()) {
            return -1;
        }
        final int index = App.getFileIndex(source.getSelectedAddress(),
                source.getIndex() == CpuMemory);
        final DataSource fileSource = dataSources[FileContents];
        if (index < 0 || index >= fileSource.getSize()) {
            return -1;
        }
        return index;
    }

    public void goToBookmark(final Bookmark bookmark) {
        goToAddress(bookmark.getDataSourceIndex(), bookmark.getAddress());
    }

    public boolean goToAddress(final int dataSourceIndex, final int address) {
        final DataSource source = dataSources[dataSourceIndex];
        if (address < 0 || address >= source.getSize()) {
            return false;
        }
        source.setSelection(address);
        if (dataSourceIndex != dataSource.getIndex()) {
            setDataSource(dataSourceIndex);
        }
        goToAddress(address);
        return true;
    }

    public void goToAddress(int address) {
        centerAddressIfNotVisible(dataSource, address);
        repaint();
    }

    private void centerAddressIfNotVisible(DataSource source, int address) {
        final JViewport viewport = (JViewport) getParent();
        final JScrollPane scrollPane = (JScrollPane) viewport.getParent();
        final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        final Rectangle r = viewport.getViewRect();
        final int startAddress = (roundUpDivision(r.y, charHeight)) << 4;
        final int endAddress = ((r.y + r.height) / charHeight) << 4;
        if (address < startAddress || address > endAddress) {
            address -= ((r.height >> 1) / charHeight) << 4;
            source.setScrollY(MARGIN + charHeight * (address >> 4));
            scrollBar.setValue(source.getScrollY());
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        g.getClipBounds(clip);
        final DataSource source = dataSource;
        final JViewport viewport = (JViewport) getParent();
        final JScrollPane scrollPane = (JScrollPane) viewport.getParent();
        final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();

        if (metrics == null) {
            metrics = g.getFontMetrics(font);
            charWidth = metrics.getWidths()['M'];
            charHeight = metrics.getHeight();
            charAscent = metrics.getAscent();
            addressMaxX = MARGIN + 6 * charWidth;
            preferredSize = new Dimension((MARGIN << 1) + 76 * charWidth,
                    (MARGIN << 1) + (source.getSize() >> 4) * charHeight);

            scrollBar.setUnitIncrement(charHeight);
            scrollBar.setBlockIncrement(16 * charHeight);
            EDT.async(() -> {
                scrollPane.setViewportView(null);
                scrollPane.setViewportView(this);
                scrollPane.updateUI();
                scrollBar.setValue(source.getScrollY());
            });
        } else {
            source.setScrollY(scrollBar.getValue());
        }

        final Rectangle r = viewport.getViewRect();
        startViewAddress = ((r.y - MARGIN) / charHeight) << 4;
        endViewAddress = ((r.y + r.height - MARGIN) / charHeight) << 4;

        g.setColor(Color.WHITE);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        final int startRow = (clip.y - MARGIN) / charHeight;
        final int startY = MARGIN + startRow * charHeight + charAscent;
        final int startAddress = startRow << 4;
        final int startColumn = (clip.x - MARGIN) / charWidth;
        final int endColumn = roundUpDivision(clip.x + clip.width - MARGIN,
                charWidth);
        final int endRow = roundUpDivision(clip.y + clip.height - MARGIN,
                charHeight);
        final int endY = MARGIN + endRow * charHeight + charAscent;

        int startSelected = source.getStartSelectedAddress();
        int endSelected = source.getEndSelectedAddress();
        if (startSelected > endSelected) {
            int temp = endSelected;
            endSelected = startSelected;
            startSelected = temp;
        }
        if (startSelected < 0) {
            startSelected = 0;
        }
        if (endSelected < 0) {
            endSelected = 0;
        }

        g.setColor(Color.BLACK);
        if (startColumn <= 5) {
            for (int y = startY, address = startAddress; y <= endY; y += charHeight,
                    address += 16) {
                g.drawString(String.format("%06X", address), MARGIN, y);
            }
        }
        if (startColumn <= 6 && endColumn >= 6) {
            for (int y = startY; y <= endY; y += charHeight) {
                g.drawString(":", addressMaxX, y);
            }
        }
        if (startColumn <= 56 && endColumn >= 9) {
            int startOffset = (startColumn - 9) / 3;
            int endOffset = (endColumn - 9) / 3;
            if (startOffset < 0) {
                startOffset = 0;
            }
            if (endOffset > 15) {
                endOffset = 15;
            }
            for (int y = startY, lineAddress = startAddress; y <= endY;
                 y += charHeight, lineAddress += 16) {
                for (int i = startOffset; i <= endOffset; i++) {
                    final int address = lineAddress | i;
                    final int x = MARGIN + (9 + i * 3) * charWidth;
                    final boolean selected = startSelected <= address
                            && address <= endSelected;
                    final int v = source.readCache(address);
                    if (selected) {
                        g.setColor(Color.BLACK);
                        g.fillRect(x, y - charAscent, charWidth << 1, charHeight);
                        g.setColor(selectedColors[(v >> 8) & 0xFF]);
                    } else {
                        g.setColor(changedColors[(v >> 8) & 0xFF]);
                    }
                    g.drawString(String.format("%02X", v & 0xFF), x, y);
                    if (selected && editing) {
                        g.setColor(Color.WHITE);
                        g.fillRect(x, y - charAscent, charWidth, charHeight);
                        g.setColor(Color.RED);
                        g.drawString(String.format("%X", writeValue), x, y);
                    }
                }
            }
        }
        g.setColor(Color.BLACK);
        if (startColumn <= 58 && endColumn >= 58) {
            final int x = MARGIN + 58 * charWidth;
            for (int y = startY; y <= endY; y += charHeight) {
                g.drawString(":", x, y);
            }
        }
        if (startColumn <= 75 && endColumn >= 60) {
            int startOffset = startColumn - 60;
            int endOffset = endColumn - 60;
            if (startOffset < 0) {
                startOffset = 0;
            }
            if (endOffset > 15) {
                endOffset = 15;
            }
            for (int y = startY, lineAddress = startAddress; y <= endY;
                 y += charHeight, lineAddress += 16) {
                for (int i = startOffset; i <= endOffset; i++) {
                    final int address = lineAddress | i;
                    final int v = source.readCache(address);
                    final int c = v & 0xFF;
                    final int x = MARGIN + (60 + i) * charWidth;
                    final boolean selected = startSelected <= address
                            && address <= endSelected;
                    if (selected) {
                        g.setColor(Color.BLACK);
                        g.fillRect(x, y - charAscent, charWidth, charHeight);
                        g.setColor(selectedColors[(v >> 8) & 0xFF]);
                    } else {
                        g.setColor(changedColors[(v >> 8) & 0xFF]);
                    }
                    g.drawString(charTable.getString(c), x, y);
                }
            }
        }
    }

    private int markEdited(int value) {
        return (editColor << 8) | (value & 0xFF);
    }

    private void write(final int address, final int value,
                       final DataSource source) {
        if (address < 0 || address >= source.getSize()) {
            return;
        }
        final int cachedValue = source.readCache(address);
        final int color = (cachedValue & 0xFF00);
        final int priorValue = source.peek(address);
        source.write(address, value);
        if (priorValue != source.peek(address)) {
            source.addEdit(new Edit(address, color | priorValue));
            source.writeCache(address, markEdited(value));
        } else {
            source.writeCache(address, color | (value & 0xFF));
        }
    }
}
