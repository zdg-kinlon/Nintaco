package nintaco.gui.familybasic;

import nintaco.App;
import nintaco.Machine;
import nintaco.PPU;
import nintaco.gui.FileExtensionFilter;
import nintaco.gui.ImagePanel;
import nintaco.gui.InputTextAreaDialog;
import nintaco.gui.PleaseWaitDialog;
import nintaco.gui.image.preferences.Paths;
import nintaco.input.familybasic.FamilyBasicUtil;
import nintaco.palettes.PaletteUtil;
import nintaco.preferences.AppPrefs;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static nintaco.input.familybasic.FamilyBasicUtil.charToHex;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.isBlank;
import static nintaco.util.StringUtil.replaceNewlines;

public class BackgroundEditorFrame extends javax.swing.JFrame {

    public static final FileExtensionFilter[] BackgroundFileExtensionFilters = {
            new FileExtensionFilter(0, "Background files (*.background)", "background"),
            new FileExtensionFilter(1, "All files (*.*)"),
    };
    private static final int NAMETABLE_ADDRESS = 0x2400;

    private static final int BACKGROUND_WIDTH = 224;

    private static final int BACKGROUND_HEIGHT = 168;
    private static File backgroundFile;
    private final DrawAndFillListener drawAndFillListener
            = new DrawAndFillListener();
    private final SelectAndCopyListener selectAndCopyListener
            = new SelectAndCopyListener();
    private final PasteListener pasteListener = new PasteListener();
    private final List<int[]> history = new ArrayList<>();
    private final int[][] colors = new int[4][4];
    private final int[][] attributes = new int[16][16];
    private final int[][] tiles = new int[32][32];
    private final int[][] attributesCopy = new int[16][16];
    private final int[][] tilesCopy = new int[32][32];
    private final Fill[][] fills = new Fill[32][32];
    private volatile Machine machine;
    private volatile PPU ppu;
    private int historyIndex;
    private int patternTableBoxX;
    private int patternTableBoxY;
    private boolean patternTableDrawBox;
    private int selectedPattern;
    private int paletteBoxY;
    private boolean paletteDrawBox;
    private int selectedPalette;
    private int backgroundBoxX;
    private int backgroundBoxY;
    private boolean backgroundDrawBox;
    private int selectX0 = -1;
    private int selectY0;
    private int selectX1;
    private int selectY1;
    private int copyImageWidth;
    private int copyImageHeight;
    private int copyOffsetX;
    private int copyOffsetY;
    private int copyImageX;
    private int copyImageY;
    private int[] copyScreen;
    private boolean drawCopy;
    private String copyMessage;
    private Mode mode;
    private MouseMotionListener mouseMotionListener;
    private MouseListener mouseListener;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JLabel coordinatesLabel;
    private javax.swing.JRadioButton drawRadioButton;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JRadioButton fillRadioButton;
    private javax.swing.JLabel graphicLabel;
    private javax.swing.JTextField graphicTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.ButtonGroup modeButtonGroup;
    private javax.swing.JLabel modeLabel;
    private javax.swing.JPanel modePanel;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JLabel paletteLabel;
    private javax.swing.JPanel palettePanel;
    private javax.swing.JRadioButton pasteRadioButton;
    private javax.swing.JLabel patternLabel;
    private javax.swing.JPanel patternPanel;
    private javax.swing.JLabel patternTableLabel;
    private javax.swing.JPanel patternTablePanel;
    private final DocumentListener graphicListener
            = createDocumentListener(this::graphicEdited);
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JButton resizeButton;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JRadioButton selectAndCopyRadioButton;
    private javax.swing.JButton textButton;
    private javax.swing.JMenuItem undoMenuItem;

    public BackgroundEditorFrame(final Machine machine) {
        initComponents();
        initImagePanels();
        setMachine(machine);
        addLoseFocusListener(this, graphicTextField);
        graphicTextField.getDocument().addDocumentListener(graphicListener);
        makeMonospaced(graphicTextField);
        makeMonospaced(coordinatesLabel);
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
    }

    private static void loadBackground(final Window parent, final File file,
                                       final PleaseWaitDialog pleaseWaitDialog) {
        if (executeMessageTask(parent, pleaseWaitDialog,
                () -> FamilyBasicUtil.loadBackground(file))) {
            backgroundFile = file;
            if (parent instanceof BackgroundEditorFrame frame) {
                frame.renderBackground();
                frame.saveState();
            }
        }
    }

    private static void saveBackground(final Window parent, final File file,
                                       final PleaseWaitDialog pleaseWaitDialog) {
        if (executeMessageTask(parent, pleaseWaitDialog,
                () -> FamilyBasicUtil.saveBackground(file))) {
            backgroundFile = file;
        }
    }

    public static void open(final Window parent) {
        open(parent, null);
    }

    public static void open(final Window parent, final Component source) {
        App.setNoStepPause(true);
        final JFileChooser chooser = createFileChooser("Load Background",
                AppPrefs.getInstance().getPaths().getBackgroundDir(),
                BackgroundFileExtensionFilters);
        if (showOpenDialog(parent, chooser, (p, d) -> p.setBackgroundDir(d))
                == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = chooser.getSelectedFile();
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(parent);
            new Thread(() -> loadBackground(parent, selectedFile, pleaseWaitDialog))
                    .start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }

    public static void saveAs(final Window parent) {
        saveAs(parent, null);
    }

    public static void saveAs(final Window parent, final Component source) {
        App.setNoStepPause(true);

        final AppPrefs prefs = AppPrefs.getInstance();
        final Paths paths = prefs.getPaths();
        final File file = showSaveAsDialog(parent, paths.getBackgroundDir(),
                backgroundFile == null ? null : backgroundFile.getName(),
                "background", BackgroundFileExtensionFilters[0], true);
        if (file != null) {
            final String dir = file.getParent();
            paths.addRecentDirectory(dir);
            paths.setBackgroundDir(dir);
            AppPrefs.save();

            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(parent);
            new Thread(() -> saveBackground(parent, file, pleaseWaitDialog)).start();
            pleaseWaitDialog.showAfterDelay();
        } else {
            App.setNoStepPause(false);
        }
    }

    private void initImagePanels() {
        initPatternPanel();
        initBackgroundPanel();
        initPalettePanel();
        initPatternTablePanel();
    }

    private void initPatternPanel() {
        final ImagePanel panel = ((ImagePanel) patternPanel);
        panel.setBlackBars(false);
        panel.setCentered(false);
    }

    private void initBackgroundPanel() {
        final ImagePanel panel = ((ImagePanel) backgroundPanel);
        panel.setBlackBars(false);
        panel.setCentered(false);
        setMode(Mode.Draw);
    }

    private void setMode(final Mode mode) {
        this.mode = mode;
        backgroundPanel.removeMouseMotionListener(mouseMotionListener);
        backgroundPanel.removeMouseListener(mouseListener);
        switch (mode) {
            case Draw:
                drawRadioButton.setSelected(true);
                mouseMotionListener = drawAndFillListener;
                mouseListener = drawAndFillListener;
                break;
            case Fill:
                fillRadioButton.setSelected(true);
                mouseMotionListener = drawAndFillListener;
                mouseListener = drawAndFillListener;
                break;
            case SelectAndCopy:
                selectAndCopyRadioButton.setSelected(true);
                selectX0 = -1;
                copyScreen = null;
                copyMessage = null;
                mouseMotionListener = selectAndCopyListener;
                mouseListener = selectAndCopyListener;
                pasteRadioButton.setEnabled(false);
                break;
            case Paste:
                pasteRadioButton.setSelected(true);
                mouseMotionListener = pasteListener;
                mouseListener = pasteListener;
                break;
        }
        backgroundPanel.addMouseMotionListener(mouseMotionListener);
        backgroundPanel.addMouseListener(mouseListener);
        renderBackground();
    }

    private void initPalettePanel() {
        final ImagePanel panel = ((ImagePanel) palettePanel);
        panel.setBlackBars(false);
        panel.setCentered(false);

        panel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                paletteMouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                paletteMousePressed(e.getX(), e.getY());
            }
        });
        panel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                paletteMouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                paletteMousePressed(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                paletteMouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                paletteMouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                paletteMouseOutOfBounds();
            }
        });
    }

    private void initPatternTablePanel() {
        final ImagePanel panel = ((ImagePanel) patternTablePanel);
        panel.setBlackBars(false);
        panel.setCentered(false);

        panel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                patternTableMouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                patternTableMousePressed(e.getX(), e.getY());
            }
        });
        panel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                patternTableMouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                patternTableMousePressed(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                patternTableMouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                patternTableMouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                patternTableMouseOutOfBounds();
            }
        });
    }

    public void destroy() {
        dispose();
    }

    private void closeFrame() {
        App.destroyBackgroundEditorFrame();
    }

    public final void setMachine(final Machine machine) {
        this.machine = machine;
        this.ppu = machine == null ? null : machine.getPPU();
        EventQueue.invokeLater(() -> {
            if (ppu == null) {
                ((ImagePanel) patternTablePanel).clearScreen();
                ((ImagePanel) backgroundPanel).clearScreen();
                ((ImagePanel) patternPanel).clearScreen();
                ((ImagePanel) palettePanel).clearScreen();
                return;
            } else {
                renderPatternTable();
                renderBackground();
                drawPattern();
                initHistory();
            }
            enableComponents();
        });
    }

    private void renderPalette() {
        final PPU p = ppu;
        if (p != null) {
            final ImagePanel panel = (ImagePanel) palettePanel;
            drawPalette(panel.getScreen());
            panel.render();
        }
    }

    private void renderPatternTable() {
        final Machine m = machine;
        final PPU p = ppu;
        if (m != null && p != null) {
            updateColors(m, p);
            final ImagePanel panel = (ImagePanel) patternTablePanel;
            drawPatternTable(p, panel.getScreen(), selectedPalette);
            panel.render();
        }
    }

    private void renderBackground() {
        final Machine m = machine;
        final PPU p = ppu;
        if (m != null && p != null) {
            updateColors(m, p);
            final ImagePanel panel = (ImagePanel) backgroundPanel;
            drawBackground(p, panel.getScreen());
            panel.render();
        }
    }

    private void updateColors(final Machine machine, final PPU ppu) {
        if (machine != null) {
            final int[] palette = PaletteUtil.getExtendedPalette(machine);
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    colors[i][j] = palette[ppu.getPaletteRamValue((i << 2) | j)];
                }
            }
            renderPalette();
        }
    }

    private void drawPattern() {

        final ImagePanel patternPan = ((ImagePanel) patternPanel);
        final int[] patternScreen = patternPan.getScreen();

        final ImagePanel patternTablePan = ((ImagePanel) patternTablePanel);
        final int[] patternTableScreen = patternTablePan.getScreen();

        final int tileX = (selectedPattern & 0x0F) << 3;
        final int tileY = (selectedPattern & 0xF0) >> 1;
        for (int y = 0; y < 8; y++) {
            System.arraycopy(patternTableScreen, ((tileY | y) << 7) | tileX,
                    patternScreen, y << 3, 8);
        }

        patternPan.render();
    }

    private void drawPalette(final int[] screen) {
        for (int i = 3; i >= 0; i--) {
            final int[] cols = colors[i];
            final int y = i << 8;
            for (int j = 3; j >= 0; j--) {
                final int color = cols[j];
                final int offset = y | (j << 3);
                for (int k = 0; k < 8; k++) {
                    screen[offset | k] = color;
                }
            }
            for (int j = 6; j >= 0; j--) {
                System.arraycopy(screen, y, screen, y | (j << 5), 32);
            }
        }
        if (paletteDrawBox) {
            final int y0 = paletteBoxY;
            final int y1 = y0 | 192;
            for (int i = 31; i >= 0; i--) {
                screen[y0 | i] ^= 0xFFFFFF;
                screen[y1 | i] ^= 0xFFFFFF;
            }
            for (int i = 5; i >= 1; i--) {
                final int y = y0 | (i << 5);
                screen[y] ^= 0xFFFFFF;
                screen[y | 31] ^= 0xFFFFFF;
            }
        }
    }

    private void drawPatternTable(final PPU ppu, final int[] screen,
                                  final int palette) {

        int address = 0x1000;
        for (int tileY = 0; tileY < 128; tileY += 8) {
            for (int tileX = 0; tileX < 128; tileX += 8, address += 16) {
                final int address1 = address | 0x0008;
                for (int y = 0; y < 8; y++) {
                    final int b0 = ppu.peekVRAM(address + y);
                    final int b1 = ppu.peekVRAM(address1 + y);
                    final int index = ((tileY | y) << 7) | tileX;
                    for (int x = 0; x < 8; x++) {
                        final int shift = 7 - x;
                        screen[index | x] = colors[palette][(((b1 >> shift) & 1) << 1)
                                | ((b0 >> shift) & 1)];
                    }
                }
            }
        }

        if (patternTableDrawBox) {
            final int boxX = patternTableBoxX;
            final int boxY = patternTableBoxY;
            final int y0 = boxY << 7;
            final int y1 = (boxY + 7) << 7;
            for (int i = 7; i >= 0; i--) {
                screen[y0 | boxX | i] ^= 0xFFFFFF;
                screen[y1 | boxX | i] ^= 0xFFFFFF;
            }
            for (int i = 6; i >= 1; i--) {
                final int y = (boxY + i) << 7;
                screen[y | boxX] ^= 0xFFFFFF;
                screen[y | boxX | 7] ^= 0xFFFFFF;
            }
        }
    }

    private int readAttribute(final PPU ppu, final int tileX, final int tileY) {
        return (ppu.peekVRAM(NAMETABLE_ADDRESS | 0x03C0 | ((tileY >> 2) << 3)
                | (tileX >> 2)) >> (((tileY & 2) | ((tileX >> 1) & 1)) << 1)) & 0x03;
    }

    private void writeAttribute(final PPU ppu, final int tileX, final int tileY,
                                final int attribute) {
        final int address = NAMETABLE_ADDRESS | 0x03C0 | ((tileY >> 2) << 3)
                | (tileX >> 2);
        final int shift = (((tileY & 2) | ((tileX >> 1) & 1)) << 1);
        ppu.writeVRAM(address, (ppu.peekVRAM(address) & ~(0x03 << shift))
                | ((attribute & 0x03) << shift));
    }

    private int readTile(final PPU ppu, final int tileX, final int tileY) {
        return ppu.peekVRAM(NAMETABLE_ADDRESS | (tileY << 5) | tileX);
    }

    private void writeTile(final PPU ppu, final int tileX, final int tileY,
                           final int value) {
        ppu.writeVRAM(NAMETABLE_ADDRESS | (tileY << 5) | tileX, value);
    }

    private void copy(final String message) {
        if (isBlank(message)) {
            copyMessage = null;
            return;
        }
        final String[] lines = replaceNewlines(message).split("\n");
        int width = 0;
        int height = min(lines.length, 30);
        for (int i = height - 1; i >= 0; i--) {
            lines[i] = lines[i].trim();
            width = max(width, lines[i].length());
        }
        width = min(width, 32);
        selectX0 = 0;
        selectY0 = 0;
        selectX1 = width - 1;
        selectY1 = height - 1;
        for (int i = tilesCopy.length - 1; i >= 0; i--) {
            for (int j = tilesCopy[i].length - 1; j >= 0; j--) {
                tilesCopy[i][j] = -1;
            }
        }

        copyOffsetX = 0;
        copyOffsetY = 0;
        copyImageWidth = width << 3;
        copyImageHeight = height << 3;
        copyScreen = new int[copyImageWidth * copyImageHeight];
        for (int i = copyScreen.length - 1; i >= 0; i--) {
            copyScreen[i] = -1;
        }
        renderPatternTable();
        final int[] screen = ((ImagePanel) patternTablePanel).getScreen();

        for (int i = height - 1; i >= 0; i--) {
            final int i2 = i >> 1;
            final int I = i << 3;
            final String line = lines[i];
            for (int j = min(width, line.length()) - 1; j >= 0; j--) {
                final int J = j << 3;
                attributesCopy[i2][j >> 1] = selectedPalette;
                final int value = charToHex(line.charAt(j));
                final int tile = tilesCopy[i][j] = value >= 0 ? value : 0;
                if (tile >= 0) {
                    final int X = (tile & 0x0F) << 3;
                    final int Y = (tile & 0xF0) >> 1;
                    for (int y = 7; y >= 0; y--) {
                        System.arraycopy(screen, ((Y | y) << 7) | X, copyScreen,
                                (I + y) * copyImageWidth + J, 8);
                    }
                }
            }
        }

        copyMessage = message;
        pasteRadioButton.setEnabled(true);
    }

    private void copy() {
        final PPU p = ppu;
        if (p == null) {
            return;
        }

        if (selectX0 > selectX1) {
            final int temp = selectX0;
            selectX0 = selectX1;
            selectX1 = temp;
            copyOffsetX = 0;
        } else {
            copyOffsetX = selectX0 - selectX1;
        }
        if (selectY0 > selectY1) {
            final int temp = selectY0;
            selectY0 = selectY1;
            selectY1 = temp;
            copyOffsetY = 0;
        } else {
            copyOffsetY = selectY0 - selectY1;
        }

        readNametable(p, attributesCopy, tilesCopy);

        copyImageWidth = (selectX1 - selectX0 + 1) << 3;
        copyImageHeight = (selectY1 - selectY0 + 1) << 3;
        copyScreen = new int[copyImageWidth * copyImageHeight];

        drawNametable(p, selectX0, selectY0, selectX1, selectY1, attributesCopy,
                tilesCopy, copyScreen, copyImageWidth);

        final int offsetY = (copyImageHeight - 1) * copyImageWidth;
        for (int i = copyImageWidth - 1; i >= 0; i--) {
            copyScreen[i] ^= 0xFFFFFF;
            copyScreen[offsetY + i] ^= 0xFFFFFF;
        }
        for (int i = copyImageHeight - 2; i >= 1; i--) {
            final int offset = copyImageWidth * i;
            copyScreen[offset] ^= 0xFFFFFF;
            copyScreen[offset + copyImageWidth - 1] ^= 0xFFFFFF;
        }

        copyMessage = null;
        pasteRadioButton.setEnabled(true);
    }

    private void paste(final int x, final int y) {
        final PPU p = ppu;
        if (p != null) {
            for (int i = selectY0; i <= selectY1; i++) {
                final int i2 = i >> 1;
                final int I = y + i;
                if (I >= 0 && I < 30) {
                    final int ai = I >> 1;
                    for (int j = selectX0; j <= selectX1; j++) {
                        final int J = x + j;
                        if (J >= 0 && J < 32) {
                            final int aj = J >> 1;
                            final int tile = tilesCopy[i][j];
                            if (tile >= 0) {
                                tiles[I][J] = tile;
                                attributes[ai][aj] = attributesCopy[i2][j >> 1];
                            }
                        }
                    }
                }
            }
            writeNametable(p);
            saveState();
        }
    }

    private void initHistory() {
        history.clear();
        historyIndex = 0;
        saveState();
    }

    private void saveState() {
        final PPU p = ppu;
        if (p != null) {
            final int[] nametable = new int[0x0400];
            for (int i = nametable.length - 1; i >= 0; i--) {
                nametable[i] = p.peekVRAM(NAMETABLE_ADDRESS | i);
            }
            while (history.size() > historyIndex) {
                history.remove(history.size() - 1);
            }
            history.add(nametable);
            historyIndex = history.size();
            enableComponents();
        }
    }

    private void undo() {
        final PPU p = ppu;
        if (p != null && historyIndex > 1) {
            historyIndex--;
            final int[] nametable = history.get(historyIndex - 1);
            for (int i = nametable.length - 1; i >= 0; i--) {
                p.writeVRAM(NAMETABLE_ADDRESS | i, nametable[i]);
            }
            enableComponents();
            renderBackground();
        }
    }

    private void redo() {
        final PPU p = ppu;
        if (p != null && historyIndex < history.size()) {
            historyIndex++;
            final int[] nametable = history.get(historyIndex - 1);
            for (int i = nametable.length - 1; i >= 0; i--) {
                p.writeVRAM(NAMETABLE_ADDRESS | i, nametable[i]);
            }
            enableComponents();
            renderBackground();
        }
    }

    private void enableComponents() {
        if (ppu == null) {
            undoMenuItem.setEnabled(false);
            redoMenuItem.setEnabled(false);
        } else {
            undoMenuItem.setEnabled(historyIndex > 1);
            redoMenuItem.setEnabled(historyIndex < history.size());
        }
    }

    private void readNametable(final PPU ppu) {
        readNametable(ppu, attributes, tiles);
    }

    private void readNametable(final PPU ppu, final int[][] attributes,
                               final int[][] tiles) {

        int address = NAMETABLE_ADDRESS;
        for (int tileY = 0, attributeAddress = address | 0x03C0; tileY < 16;
             tileY += 2) {
            for (int tileX = 0; tileX < 16; tileX += 2, attributeAddress++) {
                int attribute = ppu.peekVRAM(attributeAddress);
                for (int y = 0; y < 2; y++) {
                    final int yOffset = tileY | y;
                    for (int x = 0; x < 2; x++) {
                        attributes[yOffset][tileX | x] = attribute & 0x03;
                        attribute >>= 2;
                    }
                }
            }
        }

        address += 66;
        for (int tileY = 2; tileY < 23; tileY++) {
            for (int tileX = 2; tileX < 30; tileX++, address++) {
                tiles[tileY][tileX] = ppu.peekVRAM(address);
            }
            address += 4;
        }
    }

    private void drawNametable(final PPU ppu, final int x0, final int y0,
                               final int x1, final int y1, final int[][] attributes, final int[][] tiles,
                               final int[] screen, final int width) {
        final int backgroundPatternTableAddress
                = ppu.getBackgroundPatternTableAddress();
        for (int tileY = y0; tileY <= y1; tileY++) {
            final int tileY2 = tileY >> 1;
            final int yOffset = (tileY - y0) << 3;
            for (int tileX = x0; tileX <= x1; tileX++) {
                final int xOffset = (tileX - x0) << 3;
                final int[] cols = colors[attributes[tileY2][tileX >> 1]];
                final int address0 = backgroundPatternTableAddress
                        | (tiles[tileY][tileX] << 4);
                final int address1 = address0 + 8;
                for (int y = 0; y < 8; y++) {
                    final int yOff = width * (yOffset + y);
                    final int b0 = ppu.peekVRAM(address0 + y);
                    final int b1 = ppu.peekVRAM(address1 + y);
                    for (int x = 0; x < 8; x++) {
                        final int shift = 7 - x;
                        screen[yOff + xOffset + x] = cols[(((b1 >> shift) & 1) << 1)
                                | ((b0 >> shift) & 1)];
                    }
                }
            }
        }
    }

    private void writeNametable(final PPU ppu) {

        int address = NAMETABLE_ADDRESS;
        for (int tileY = 0, attributeAddress = address | 0x03C0; tileY < 16;
             tileY += 2) {
            for (int tileX = 0; tileX < 16; tileX += 2, attributeAddress++) {
                int attribute = 0;
                for (int y = 1; y >= 0; y--) {
                    for (int x = 1; x >= 0; x--) {
                        attribute = (attribute << 2) | attributes[tileY | y][tileX | x];
                    }
                }
                ppu.writeVRAM(attributeAddress, attribute);
            }
        }

        address += 66;
        for (int tileY = 2; tileY < 23; tileY++) {
            for (int tileX = 2; tileX < 30; tileX++, address++) {
                ppu.writeVRAM(address, tiles[tileY][tileX]);
            }
            address += 4;
        }
    }

    private void drawBackground(final PPU ppu, final int[] screen) {

        readNametable(ppu);
        drawNametable(ppu, 2, 2, 29, 22, attributes, tiles, screen,
                BACKGROUND_WIDTH);

        if (backgroundDrawBox) {
            final int boxX = backgroundBoxX;
            final int boxY = backgroundBoxY;
            final int y0 = boxY;
            final int y1 = boxY + 1568;
            for (int i = 7; i >= 0; i--) {
                screen[y0 + boxX + i] ^= 0xFFFFFF;
                screen[y1 + boxX + i] ^= 0xFFFFFF;
            }
            for (int i = 6; i >= 1; i--) {
                final int offset = boxY + BACKGROUND_WIDTH * i + boxX;
                screen[offset] ^= 0xFFFFFF;
                screen[offset + 7] ^= 0xFFFFFF;
            }
        }

        if (mode == Mode.SelectAndCopy && selectX0 >= 0) {

            int x0 = selectX0 - 2;
            int y0 = selectY0 - 2;
            int x1 = selectX1 - 2;
            int y1 = selectY1 - 2;
            if (x0 > x1) {
                final int temp = x0;
                x0 = x1;
                x1 = temp;
            }
            if (y0 > y1) {
                final int temp = y0;
                y0 = y1;
                y1 = temp;
            }
            x0 <<= 3;
            y0 <<= 3;
            x1 = (x1 << 3) + 7;
            y1 = (y1 << 3) + 7;

            final int Y0 = BACKGROUND_WIDTH * y0;
            final int Y1 = BACKGROUND_WIDTH * y1;
            for (int x = x0; x <= x1; x++) {
                screen[Y0 + x] ^= 0xFFFFFF;
                screen[Y1 + x] ^= 0xFFFFFF;
            }
            for (int y = y0 + 1; y < y1; y++) {
                final int Y = BACKGROUND_WIDTH * y;
                screen[Y + x0] ^= 0xFFFFFF;
                screen[Y + x1] ^= 0xFFFFFF;
            }
        } else if (mode == Mode.Paste && copyScreen != null && drawCopy) {
            for (int y = copyImageHeight - 1; y >= 0; y--) {
                final int Y = copyImageY + y;
                if (Y >= 0 && Y < BACKGROUND_HEIGHT) {
                    final int offsetY = BACKGROUND_WIDTH * Y;
                    final int oy = copyImageWidth * y;
                    for (int x = copyImageWidth - 1; x >= 0; x--) {
                        final int X = copyImageX + x;
                        if (X >= 0 && X < BACKGROUND_WIDTH) {
                            final int pixel = copyScreen[oy + x];
                            if (pixel >= 0) {
                                screen[offsetY + X] = pixel;
                            }
                        }
                    }
                }
            }
        }
    }

    private void setPatternTableDrawBox(final int x, final int y) {
        patternTableBoxX = x & 0x78;
        patternTableBoxY = y & 0x78;
        patternTableDrawBox = true;
        renderPatternTable();
    }

    private void patternTableMouseUpdated(final int x, final int y) {
        setPatternTableDrawBox(x, y);
    }

    private void patternTableMousePressed(final int x, final int y) {
        selectedPattern = ((y >> 3) << 4) | (x >> 3);
        patternTableDrawBox = false;
        renderPatternTable();
        drawPattern();
        setPatternTableDrawBox(x, y);
        updateGraphic();
    }

    private void patternTableMouseOutOfBounds() {
        patternTableDrawBox = false;
        renderPatternTable();
    }

    private void setPaletteDrawBox(final int y) {
        paletteBoxY = (y & 0x18) << 5;
        paletteDrawBox = true;
        renderPalette();
    }

    private void paletteMouseUpdated(final int x, final int y) {
        setPaletteDrawBox(y);
    }

    private void paletteMousePressed(final int x, final int y) {
        selectedPalette = y >> 3;
        patternTableDrawBox = false;
        renderPatternTable();
        drawPattern();
        setPaletteDrawBox(y);
        updateGraphic();
        if (copyMessage != null) {
            copy(copyMessage);
        }
    }

    private void paletteMouseOutOfBounds() {
        paletteDrawBox = false;
        renderPalette();
    }

    private void setBackgroundDrawBox(final int x, final int y) {
        backgroundBoxX = x & 0xF8;
        backgroundBoxY = BACKGROUND_WIDTH * (y & 0xF8);
        backgroundDrawBox = true;
        coordinatesLabel.setText(String.format("X: %02d, Y: %02d", x >> 3, y >> 3));
        renderBackground();
    }

    private void backgroundMouseUpdated(final int x, final int y) {
        setBackgroundDrawBox(x, y);
    }

    private void backgroundMousePressed(final int x, final int y) {
        final PPU p = ppu;
        if (p != null) {
            final int tileX = 2 + (x >> 3);
            final int tileY = 2 + (y >> 3);

            switch (mode) {
                case Draw:
                    draw(p, tileX, tileY);
                    break;
                case Fill:
                    fill(p, tileX, tileY);
                    break;
            }

            setBackgroundDrawBox(x, y);
        }
    }

    private void draw(final PPU ppu, final int tileX, final int tileY) {
        writeAttribute(ppu, tileX, tileY, selectedPalette);
        writeTile(ppu, tileX, tileY, selectedPattern);
        saveState();
    }

    private void fill(final PPU ppu, final int tileX, final int tileY) {

        readNametable(ppu);
        final int targetAttribute = attributes[tileY >> 1][tileX >> 1];
        final int targetTile = tiles[tileY][tileX];
        if (targetAttribute != selectedPalette || targetTile != selectedPattern) {
            for (int i = 31; i >= 0; i--) {
                final int i2 = i >> 1;
                for (int j = 31; j >= 0; j--) {
                    fills[i][j] = attributes[i2][j >> 1] == targetAttribute
                            && tiles[i][j] == targetTile ? Fill.Target : Fill.Empty;
                }
            }
            floodFill(ppu, tileX, tileY);
            for (int i = 31; i >= 0; i--) {
                final int i2 = i >> 1;
                for (int j = 31; j >= 0; j--) {
                    if (fills[i][j] == Fill.Filled) {
                        attributes[i2][j >> 1] = selectedPalette;
                        tiles[i][j] = selectedPattern;
                    }
                }
            }
            writeNametable(ppu);
            renderBackground();
            saveState();
        }
    }

    private void floodFill(final PPU ppu, final int tileX, final int tileY) {

        if (tileY < 0 || tileY > 29 || tileX < 0 || tileX > 31
                || fills[tileY][tileX] != Fill.Target) {
            return;
        }

        int x0 = tileX - 1;
        for (; x0 >= 0 && fills[tileY][x0] == Fill.Target; x0--) {
        }
        x0++;

        int x1 = tileX + 1;
        for (; x1 < 32 && fills[tileY][x1] == Fill.Target; x1++) {
        }

        for (int x = x0; x < x1; x++) {
            fills[tileY][x] = Fill.Filled;
        }
        for (int x = x0; x < x1; x++) {
            floodFill(ppu, x, tileY - 1);
        }
        for (int x = x0; x < x1; x++) {
            floodFill(ppu, x, tileY + 1);
        }
    }

    private void backgroundMouseOutOfBounds() {
        backgroundDrawBox = false;
        clearCoordinatesLabel();
        renderBackground();
    }

    private void clearCoordinatesLabel() {
        coordinatesLabel.setText("");
    }

    private void updateGraphic() {
        int graphic = -1;
        if (selectedPattern < 32) {
            graphic = selectedPattern;
        } else if (selectedPattern >= 184) {
            graphic = selectedPattern - 152;
        }
        if (graphic >= 0) {
            setGraphic(String.format("%c%c%c",
                    (char) ('A' + (graphic >> 3)),
                    (char) ('0' + (graphic & 7)),
                    (char) ('0' + selectedPalette)));
        } else {
            setGraphic("");
        }
    }

    private void setGraphic(final String graphic) {
        graphicTextField.getDocument().removeDocumentListener(graphicListener);
        graphicTextField.setText(graphic);
        graphicTextField.getDocument().addDocumentListener(graphicListener);
        requestFocusInWindow();
    }

    private void graphicEdited() {
        final String name = graphicTextField.getText().trim();
        if (name.length() == 3) {
            final char c0 = Character.toUpperCase(name.charAt(0));
            final char c1 = name.charAt(1);
            final char c2 = name.charAt(2);
            if (c0 >= 'A' && c0 <= 'M'
                    && c1 >= '0' && c1 <= '7'
                    && c2 >= '0' && c2 <= '3') {
                final int index = ((c0 - 'A') << 3) | (c1 - '0');
                selectedPattern = index < 32 ? index : index + 152;
                selectedPalette = c2 - '0';
                renderPatternTable();
                renderBackground();
                drawPattern();
            }
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

        modeButtonGroup = new javax.swing.ButtonGroup();
        backgroundPanel = new ImagePanel(224, 168, 2);
        patternTablePanel = new ImagePanel(128, 128, 4);
        backgroundLabel = new javax.swing.JLabel();
        patternTableLabel = new javax.swing.JLabel();
        patternLabel = new javax.swing.JLabel();
        patternPanel = new ImagePanel(8, 8, 16);
        paletteLabel = new javax.swing.JLabel();
        palettePanel = new ImagePanel(32, 32, 4);
        modePanel = new javax.swing.JPanel();
        drawRadioButton = new javax.swing.JRadioButton();
        fillRadioButton = new javax.swing.JRadioButton();
        selectAndCopyRadioButton = new javax.swing.JRadioButton();
        pasteRadioButton = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        resizeButton = new javax.swing.JButton();
        graphicLabel = new javax.swing.JLabel();
        graphicTextField = new javax.swing.JTextField();
        textButton = new javax.swing.JButton();
        coordinatesLabel = new javax.swing.JLabel();
        modeLabel = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        closeMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Family BASIC Background Editor");
        setMaximumSize(null);
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setMaximumSize(null);

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
                backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        backgroundPanelLayout.setVerticalGroup(
                backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 359, Short.MAX_VALUE)
        );

        patternTablePanel.setMaximumSize(null);
        patternTablePanel.setPreferredSize(new java.awt.Dimension(128, 512));

        javax.swing.GroupLayout patternTablePanelLayout = new javax.swing.GroupLayout(patternTablePanel);
        patternTablePanel.setLayout(patternTablePanelLayout);
        patternTablePanelLayout.setHorizontalGroup(
                patternTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 128, Short.MAX_VALUE)
        );
        patternTablePanelLayout.setVerticalGroup(
                patternTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        backgroundLabel.setText("Background");
        backgroundLabel.setMaximumSize(null);
        backgroundLabel.setMinimumSize(null);
        backgroundLabel.setPreferredSize(null);

        patternTableLabel.setText("Pattern Table");
        patternTableLabel.setMaximumSize(null);
        patternTableLabel.setMinimumSize(null);
        patternTableLabel.setPreferredSize(null);

        patternLabel.setText("Pattern");
        patternLabel.setMaximumSize(null);
        patternLabel.setMinimumSize(null);
        patternLabel.setPreferredSize(null);

        patternPanel.setMaximumSize(null);
        patternPanel.setPreferredSize(new java.awt.Dimension(128, 128));

        javax.swing.GroupLayout patternPanelLayout = new javax.swing.GroupLayout(patternPanel);
        patternPanel.setLayout(patternPanelLayout);
        patternPanelLayout.setHorizontalGroup(
                patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 128, Short.MAX_VALUE)
        );
        patternPanelLayout.setVerticalGroup(
                patternPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        paletteLabel.setText("Palette");
        paletteLabel.setMaximumSize(null);
        paletteLabel.setMinimumSize(null);
        paletteLabel.setPreferredSize(null);

        palettePanel.setMaximumSize(null);
        palettePanel.setPreferredSize(new java.awt.Dimension(128, 128));

        javax.swing.GroupLayout palettePanelLayout = new javax.swing.GroupLayout(palettePanel);
        palettePanel.setLayout(palettePanelLayout);
        palettePanelLayout.setHorizontalGroup(
                palettePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 128, Short.MAX_VALUE)
        );
        palettePanelLayout.setVerticalGroup(
                palettePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 127, Short.MAX_VALUE)
        );

        modeButtonGroup.add(drawRadioButton);
        drawRadioButton.setSelected(true);
        drawRadioButton.setText("Draw");
        drawRadioButton.setFocusPainted(false);
        drawRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drawRadioButtonActionPerformed(evt);
            }
        });

        modeButtonGroup.add(fillRadioButton);
        fillRadioButton.setText("Fill");
        fillRadioButton.setFocusPainted(false);
        fillRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fillRadioButtonActionPerformed(evt);
            }
        });

        modeButtonGroup.add(selectAndCopyRadioButton);
        selectAndCopyRadioButton.setText("Select & Copy");
        selectAndCopyRadioButton.setFocusPainted(false);
        selectAndCopyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAndCopyRadioButtonActionPerformed(evt);
            }
        });

        modeButtonGroup.add(pasteRadioButton);
        pasteRadioButton.setText("Paste");
        pasteRadioButton.setEnabled(false);
        pasteRadioButton.setFocusPainted(false);
        pasteRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout modePanelLayout = new javax.swing.GroupLayout(modePanel);
        modePanel.setLayout(modePanelLayout);
        modePanelLayout.setHorizontalGroup(
                modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(modePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(drawRadioButton)
                                        .addComponent(fillRadioButton)
                                        .addComponent(selectAndCopyRadioButton)
                                        .addComponent(pasteRadioButton))
                                .addContainerGap())
        );
        modePanelLayout.setVerticalGroup(
                modePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(modePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(drawRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fillRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(selectAndCopyRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pasteRadioButton)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setMaximumSize(null);

        closeButton.setMnemonic('C');
        closeButton.setText("Close");
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        resizeButton.setMnemonic('R');
        resizeButton.setText("Resize");
        resizeButton.setFocusPainted(false);
        resizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resizeButtonActionPerformed(evt);
            }
        });

        graphicLabel.setText("Graphic:");

        graphicTextField.setColumns(4);
        graphicTextField.setText("A00");

        textButton.setMnemonic('T');
        textButton.setText("Text");
        textButton.setFocusPainted(false);
        textButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textButtonActionPerformed(evt);
            }
        });

        coordinatesLabel.setText("             ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(graphicLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(graphicTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(coordinatesLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(textButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(resizeButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(closeButton)
                                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, closeButton, resizeButton, textButton);

        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(graphicLabel)
                                        .addComponent(graphicTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(resizeButton)
                                        .addComponent(closeButton)
                                        .addComponent(textButton)
                                        .addComponent(coordinatesLabel))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        modeLabel.setText("Mode:");

        fileMenu.setText("File");

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setMnemonic('O');
        openMenuItem.setText("Open...");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setText("Save");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        saveAsMenuItem.setMnemonic('A');
        saveAsMenuItem.setText("Save As...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(jSeparator1);

        closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        closeMenuItem.setMnemonic('C');
        closeMenuItem.setText("Close");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");

        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setMnemonic('U');
        undoMenuItem.setText("Undo");
        undoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(undoMenuItem);

        redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        redoMenuItem.setMnemonic('R');
        redoMenuItem.setText("Redo");
        redoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(redoMenuItem);

        menuBar.add(editMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(backgroundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(patternPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(patternLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(palettePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(paletteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(modeLabel)
                                                                        .addComponent(modePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(patternTableLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(patternTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(backgroundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(patternTableLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(6, 6, 6)
                                                .addComponent(patternTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(patternLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(paletteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(modeLabel))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(modePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                                .addComponent(palettePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                                                                .addComponent(patternPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void drawRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawRadioButtonActionPerformed
        setMode(Mode.Draw);
    }//GEN-LAST:event_drawRadioButtonActionPerformed

    private void fillRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fillRadioButtonActionPerformed
        setMode(Mode.Fill);
    }//GEN-LAST:event_fillRadioButtonActionPerformed

    private void selectAndCopyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAndCopyRadioButtonActionPerformed
        setMode(Mode.SelectAndCopy);
    }//GEN-LAST:event_selectAndCopyRadioButtonActionPerformed

    private void pasteRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasteRadioButtonActionPerformed
        setMode(Mode.Paste);
    }//GEN-LAST:event_pasteRadioButtonActionPerformed

    private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoMenuItemActionPerformed
        undo();
    }//GEN-LAST:event_undoMenuItemActionPerformed

    private void redoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoMenuItemActionPerformed
        redo();
    }//GEN-LAST:event_redoMenuItemActionPerformed

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        closeFrame();
    }//GEN-LAST:event_closeMenuItemActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        closeFrame();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void resizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resizeButtonActionPerformed
        pack();
    }//GEN-LAST:event_resizeButtonActionPerformed

    private void textButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textButtonActionPerformed
        final InputTextAreaDialog dialog = new InputTextAreaDialog(this,
                "Enter a message, press OK and then click on the background to paste.",
                "Text Entry");
        dialog.setTextRequired();
        dialog.setDimensions(28, 5);
        dialog.setVisible(true);
        if (dialog.isOk()) {
            copy(dialog.getInput());
            setMode(Mode.Paste);
        }
    }//GEN-LAST:event_textButtonActionPerformed

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        saveAs(this);
    }//GEN-LAST:event_saveAsMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        if (backgroundFile == null) {
            saveAs(this);
        } else {
            App.setNoStepPause(true);
            final PleaseWaitDialog pleaseWaitDialog = new PleaseWaitDialog(this);
            new Thread(() -> saveBackground(this, backgroundFile, pleaseWaitDialog))
                    .start();
            pleaseWaitDialog.showAfterDelay();
        }
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        open(this, menuBar);
    }//GEN-LAST:event_openMenuItemActionPerformed
    private enum Mode {Draw, Fill, SelectAndCopy, Paste}
    private enum Fill {Empty, Target, Filled}

    private class DrawAndFillListener
            implements MouseMotionListener, MouseListener {

        @Override
        public void mouseMoved(final MouseEvent e) {
            backgroundMouseUpdated(e.getX(), e.getY());
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
            backgroundMousePressed(e.getX(), e.getY());
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            backgroundMouseUpdated(e.getX(), e.getY());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            backgroundMousePressed(e.getX(), e.getY());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            backgroundMouseUpdated(e.getX(), e.getY());
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            backgroundMouseUpdated(e.getX(), e.getY());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            backgroundMouseOutOfBounds();
        }
    }

    private class SelectAndCopyListener
            implements MouseMotionListener, MouseListener {

        @Override
        public void mouseDragged(final MouseEvent e) {
            selectX1 = 2 + (e.getX() >> 3);
            selectY1 = 2 + (e.getY() >> 3);
            backgroundDrawBox = false;
            renderBackground();
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            setBackgroundDrawBox(e.getX(), e.getY());
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            selectX0 = selectX1 = 2 + (e.getX() >> 3);
            selectY0 = selectY1 = 2 + (e.getY() >> 3);
            backgroundDrawBox = false;
            renderBackground();
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            selectX1 = 2 + (e.getX() >> 3);
            selectY1 = 2 + (e.getY() >> 3);
            copy();
            setMode(Mode.Paste);
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
            setBackgroundDrawBox(e.getX(), e.getY());
        }

        @Override
        public void mouseExited(final MouseEvent e) {
            backgroundDrawBox = false;
            renderBackground();
        }
    }

    private class PasteListener implements MouseMotionListener, MouseListener {

        @Override
        public void mouseDragged(final MouseEvent e) {
            drawCopy = false;
            backgroundDrawBox = false;
            renderBackground();
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            drawCopy = true;
            backgroundDrawBox = false;
            copyImageX = ((e.getX() >> 3) + copyOffsetX) << 3;
            copyImageY = ((e.getY() >> 3) + copyOffsetY) << 3;
            renderBackground();
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            drawCopy = false;
            backgroundDrawBox = false;
            paste(2 + (e.getX() >> 3) + copyOffsetX - selectX0,
                    2 + (e.getY() >> 3) + copyOffsetY - selectY0);
            renderBackground();
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
        }

        @Override
        public void mouseExited(final MouseEvent e) {
        }
    }
    // End of variables declaration//GEN-END:variables
}
