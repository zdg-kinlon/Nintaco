package nintaco.gui.nametables;

import nintaco.App;
import nintaco.Machine;
import nintaco.MachineRunner;
import nintaco.PPU;
import nintaco.gui.ImagePanel;
import nintaco.gui.MaxLengthDocument;
import nintaco.gui.hexeditor.DataSource;
import nintaco.gui.hexeditor.HexEditorFrame;
import nintaco.palettes.PaletteUtil;
import nintaco.preferences.AppPrefs;

import javax.swing.event.DocumentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import static nintaco.tv.TVSystem.PAL;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.MathUtil.clamp;

public class NametablesFrame extends javax.swing.JFrame {

    private final int[] screen = new int[512 * 480];
    private final int[][] colors = new int[4][4];
    private final int[][][] attributes = new int[32][32][];
    private volatile Machine machine;
    private volatile PPU ppu;
    private volatile int updateScanline;
    private volatile int framesPerUpdate;
    private volatile int frames;
    private volatile int scrollType;
    private volatile int scrollXValue;
    private volatile int scrollYValue;
    private volatile int scanlineValue;
    private volatile int mouseBoxX = -1;
    private volatile int mouseBoxY = -1;
    private volatile int selectionStartAddress = -1;
    private volatile int selectionEndAddress = -1;
    private volatile boolean updateOnSprite0Hit;
    private volatile boolean showTileGrid;
    private volatile boolean showAttributeGrid;
    private volatile boolean dragging;
    private int mouseX;
    private int mouseY;
    private int lastAddress = -1;
    private int lastTile = -1;
    private int sprite0Hits;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addressLabel;
    private javax.swing.JLabel addressNameLabel;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton copyButton;
    private javax.swing.JButton copySpacedButton;
    private javax.swing.JButton defaultsButton;
    private javax.swing.JComboBox framesPerUpdateComboBox;
    private javax.swing.JLabel framesPerUpdateLabel;
    private javax.swing.JPanel nametablesPanel;
    private javax.swing.JButton resizeButton;
    private javax.swing.JLabel scanlineLabel;
    private javax.swing.JTextField scanlineTextField;
    private final DocumentListener scanlineListener
            = createDocumentListener(this::scanlineUpdated);
    private javax.swing.JComboBox scrollComboBox;
    private javax.swing.JLabel scrollLabel;
    private javax.swing.JButton searchButton;
    private javax.swing.JLabel selectionLabel;
    private javax.swing.JLabel selectionNameLabel;
    private javax.swing.JCheckBox showAttributeGridCheckBox;
    private javax.swing.JCheckBox showTileGridCheckBox;
    private javax.swing.JCheckBox sprite0CheckBox;
    private javax.swing.JLabel tileLabel;
    private javax.swing.JLabel tileNameLabel;

    public NametablesFrame(final MachineRunner machineRunner) {
        initComponents();
        scanlineTextField.setDocument(new MaxLengthDocument(3, scanlineListener));
        loadFields();
        setMachineRunner(machineRunner);
        nametablesPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseUpdated(e.getX(), e.getY(), false);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseUpdated(e.getX(), e.getY(), true);
            }
        });
        nametablesPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseUpdated(e.getX(), e.getY(), false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseUpdated(e.getX(), e.getY(), true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseUpdated(e.getX(), e.getY(), false);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                mouseUpdated(e.getX(), e.getY(), false);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseOutOfBounds();
            }
        });
        makeMonospaced(addressLabel);
        makeMonospaced(tileLabel);
        makeMonospaced(selectionLabel);
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
        resizeButton.requestFocus();
    }

    private void mouseUpdated(int x, int y, final boolean dragged) {

        mouseX = x;
        mouseY = y;

        if (x < 0 || y < 0 || x >= 512 || y >= 480) {
            mouseOutOfBounds();
            return;
        }

        x >>= 3;
        y >>= 3;

        final int address;
        if (x < 32) {
            if (y < 30) {
                address = 0x2000 | (y << 5) | x;
            } else {
                address = 0x2800 | ((y - 30) << 5) | x;
            }
        } else {
            if (y < 30) {
                address = 0x2400 | (y << 5) | (x - 32);
            } else {
                address = 0x2C00 | ((y - 30) << 5) | (x - 32);
            }
        }
        if (lastAddress != address || (dragged && !dragging)) {
            if (dragged) {
                if (!dragging) {
                    dragging = true;
                    selectionStartAddress = selectionEndAddress = address;
                } else {
                    selectionEndAddress = clamp(address, selectionStartAddress & 0xFC00,
                            (selectionStartAddress & 0xFC00) | 0x03BF);
                }
            } else {
                dragging = false;
            }
            addressLabel.setText(String.format("$%04X", address));
            lastAddress = address;
            mouseBoxX = x << 3;
            mouseBoxY = y << 3;
            render();
        }

        final PPU p = ppu;
        if (p != null) {
            final int tile = (p.getBackgroundPatternTableAddress() >> 4)
                    | p.peekVRAM(address);
            if (lastTile != tile) {
                tileLabel.setText(String.format("$%03X", tile));
                lastTile = tile;
            }
        } else {
            if (lastTile != -1) {
                tileLabel.setText("-   ");
                lastTile = -1;
            }
        }
        if (selectionStartAddress >= 0 && selectionEndAddress >= 0) {
            int start = selectionStartAddress;
            int end = selectionEndAddress;
            if (start > end) {
                final int t = start;
                start = end;
                end = t;
            }
            selectionLabel.setText(String.format("$%04X-$%04X", start, end));
        } else {
            selectionLabel.setText("-");
        }
    }

    private void mouseOutOfBounds() {
        mouseX = -1;
        mouseY = -1;
        if (mouseBoxX >= 0 || mouseBoxY >= 0) {
            mouseBoxX = -1;
            mouseBoxY = -1;
            render();
        }
        if (lastAddress != -1) {
            addressLabel.setText("-    ");
            lastAddress = -1;
        }
        if (lastTile != -1) {
            tileLabel.setText("-   ");
            lastTile = -1;
        }
    }

    private void setScanlineTextFieldText(String text) {
        scanlineTextField.getDocument().removeDocumentListener(scanlineListener);
        scanlineTextField.setText(text);
        scanlineTextField.getDocument().addDocumentListener(scanlineListener);
    }

    private void scanlineUpdated() {
        updateScanline = 0;
        try {
            final int value = Integer.parseInt(scanlineTextField.getText().trim());
            final Machine m = machine;
            if (value >= -1 && value < (m == null ? PAL.getScanlineCount()
                    : m.getMapper().getTVSystem().getScanlineCount()) - 1) {
                updateScanline = value;
            }
        } catch (Throwable t) {
        }
    }

    public void destroy() {
        saveFields();
        dispose();
    }

    private void closeFrame() {
        App.destroyNametablesFrame();
    }

    private void loadFields() {
        loadFields(AppPrefs.getInstance().getNametablesPrefs());
    }

    private void loadFields(final NametablesPrefs prefs) {
        updateScanline = prefs.getUpdateScanline();
        framesPerUpdate = prefs.getFramesPerUpdate();
        scrollType = prefs.getScrollType();
        updateOnSprite0Hit = prefs.isUpdateOnSprite0Hit();
        showTileGrid = prefs.isShowTileGrid();
        showAttributeGrid = prefs.isShowAttributeGrid();

        setScanlineTextFieldText(Integer.toString(updateScanline));
        scrollComboBox.setSelectedIndex(scrollType);
        framesPerUpdateComboBox.setSelectedItem(Integer.toString(framesPerUpdate));
        sprite0CheckBox.setSelected(updateOnSprite0Hit);
        showTileGridCheckBox.setSelected(showTileGrid);
        showAttributeGridCheckBox.setSelected(showAttributeGrid);
        updateScanlineComponents();
    }

    private void saveFields() {
        final NametablesPrefs prefs = AppPrefs.getInstance().getNametablesPrefs();
        prefs.setUpdateScanline(updateScanline);
        prefs.setScrollType(scrollType);
        prefs.setFramesPerUpdate(framesPerUpdate);
        prefs.setUpdateOnSprite0Hit(updateOnSprite0Hit);
        prefs.setShowTileGrid(showTileGrid);
        prefs.setShowAttributeGrid(showAttributeGrid);
        AppPrefs.save();
    }

    private void updateScanlineComponents() {
        final boolean enabled = !sprite0CheckBox.isSelected();
        scanlineLabel.setEnabled(enabled);
        scanlineTextField.setEnabled(enabled);
    }

    public final void setMachineRunner(final MachineRunner machineRunner) {
        if (machineRunner != null) {
            machine = machineRunner.getMachine();
        } else {
            machine = null;
        }
        if (machine == null) {
            ppu = null;
            ((ImagePanel) nametablesPanel).clearScreen();
        } else {
            ppu = machine.getPPU();
            if (machineRunner.isPaused()) {
                update(machine, ppu, updateScanline);
            }
        }
    }

    public synchronized void update(final int scanline) {
        final PPU p = ppu;
        final Machine m = machine;
        if (m == null || p == null) {
            return;
        } else if (updateOnSprite0Hit) {
            if (!p.isSprite0Hit()) {
                sprite0Hits = 0;
                return;
            } else if (++sprite0Hits != 2) {
                return;
            }
        } else if (scanline != updateScanline) {
            return;
        }
        if (--frames > 0) {
            return;
        }
        frames = framesPerUpdate;

        update(m, p, scanline);
    }

    private void update(final Machine m, final PPU p, final int scanline) {

        final int[] palette = PaletteUtil.getExtendedPalette(m);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                colors[i][j] = palette[p.getPaletteRamValue((i << 2) | j)];
            }
        }
        final int backgroundPatternTableAddress
                = p.getBackgroundPatternTableAddress();

        drawNametable(p, 0x2000, backgroundPatternTableAddress, screen, 0, 0);
        drawNametable(p, 0x2400, backgroundPatternTableAddress, screen, 256, 0);
        drawNametable(p, 0x2800, backgroundPatternTableAddress, screen, 0, 240);
        drawNametable(p, 0x2C00, backgroundPatternTableAddress, screen, 256, 240);

        scrollXValue = p.getScrollX();
        scrollYValue = p.getScrollY();
        scanlineValue = scanline;

        render();
    }

    private synchronized void render() {

        final ImagePanel imagePanel = (ImagePanel) nametablesPanel;
        final int[] scr = imagePanel.getScreen();
        System.arraycopy(this.screen, 0, scr, 0, scr.length);

        if (showTileGrid) {
            for (int y = 0; y < 480; y += 2) {
                final int offset = y << 9;
                for (int x = 7; x < 512; x += 8) {
                    scr[offset | x] = 0x712F38;
                }
            }
            for (int y = 7; y < 480; y += 8) {
                final int offset = y << 9;
                for (int x = 0; x < 512; x += 2) {
                    scr[offset | x] = 0x712F38;
                }
            }
        }

        if (showAttributeGrid) {
            for (int y = 1; y < 480; y += 2) {
                final int offset = y << 9;
                for (int x = 15; x < 512; x += 16) {
                    scr[offset | x] = 0x385CB0;
                }
            }
            for (int y = 15; y < 480; y += 16) {
                final int offset = y << 9;
                for (int x = 1; x < 512; x += 2) {
                    scr[offset | x] = 0x385CB0;
                }
            }
        }

        switch (scrollType) {
            case ScrollType.AbsoluteCrosshairs:
            case ScrollType.OffsetCrosshairs: {
                final int scrollX = scrollXValue;
                int scrollY = scrollYValue;
                if (scrollType == ScrollType.OffsetCrosshairs) {
                    scrollY -= scanlineValue + 1;
                }
                if (scrollY >= 480) {
                    scrollY -= 480;
                } else if (scrollY < 0) {
                    scrollY += 480;
                }
                final int row = scrollY << 9;
                for (int i = 0; i < 512; i++) {
                    scr[row | i] ^= 0xFFFFFF;
                }
                for (int i = 0; i < 480; i++) {
                    scr[(i << 9) | scrollX] ^= 0xFFFFFF;
                }
                break;
            }

            case ScrollType.Window: {
                final int x0 = scrollXValue;
                int y0 = scrollYValue - scanlineValue - 1;
                if (y0 >= 480) {
                    y0 -= 480;
                } else if (y0 < 0) {
                    y0 += 480;
                }
                final int x1 = (x0 + 255) & 0x1FF;
                int y1 = (y0 + 239);
                if (y1 >= 480) {
                    y1 -= 480;
                } else if (y1 < 0) {
                    y1 += 480;
                }
                final int row0 = y0 << 9;
                final int row1 = y1 << 9;
                final int x2 = (x1 + 1) & 0x1FF;
                for (int i = x0; i != x2; i = (i + 1) & 0x1FF) {
                    scr[row0 | i] ^= 0xFFFFFF;
                    scr[row1 | i] ^= 0xFFFFFF;
                }
                if (++y0 == 480) {
                    y0 = 0;
                }
                while (y0 != y1) {
                    final int i9 = y0 << 9;
                    scr[i9 | x0] ^= 0xFFFFFF;
                    scr[i9 | x1] ^= 0xFFFFFF;
                    if (++y0 == 480) {
                        y0 = 0;
                    }
                }
                break;
            }
        }

        int start = selectionStartAddress;
        int end = selectionEndAddress;
        if (end < start) {
            final int t = start;
            start = end;
            end = t;
        }
        if (start >= 0 && end >= 0) {
            final int offset = ((start & 0x0800) == 0 ? 0 : 0x1E000)
                    + ((start & 0x0400) >> 2);
            for (int i = start; i <= end; i++) {
                final int x = i & 0x001F;
                final int o = offset + ((i & 0x03E0) << 7) + ((i & 0x001F) << 3);
                int y0 = 0;
                int y7 = 7;
                if (i - 32 < start) {
                    y0 = 1;
                    for (int j = 7; j >= 0; j--) {
                        scr[o + j] ^= 0xFFFFFF;
                    }
                } else if (x > 0 && i - 33 < start) {
                    scr[o] ^= 0xFFFFFF;
                }
                if (i + 32 > end) {
                    y7 = 6;
                    final int o7 = o | 0x0E00;
                    for (int j = 7; j >= 0; j--) {
                        scr[o7 + j] ^= 0xFFFFFF;
                    }
                } else if (x < 31 && i + 33 > end) {
                    scr[o + 0x0E07] ^= 0xFFFFFF;
                }
                if (x == 0 || i == start) {
                    for (int j = y7; j >= y0; j--) {
                        scr[o + (j << 9)] ^= 0xFFFFFF;
                    }
                }
                if (x == 31 || i == end) {
                    final int o7 = o | 7;
                    for (int j = y7; j >= y0; j--) {
                        scr[o7 + (j << 9)] ^= 0xFFFFFF;
                    }
                }
            }
        }

        if (mouseBoxX >= 0 && mouseBoxY >= 0 && !dragging) {
            for (int y = 0; y < 8; y++) {
                final int offset = ((mouseBoxY | y) << 9) | mouseBoxX;
                scr[offset] ^= 0xFFFFFF;
                scr[offset + 7] ^= 0xFFFFFF;
            }
            final int offset0 = (mouseBoxY << 9) | mouseBoxX;
            final int offset7 = ((mouseBoxY | 7) << 9) | mouseBoxX;
            for (int x = 1; x < 7; x++) {
                scr[offset0 | x] ^= 0xFFFFFF;
                scr[offset7 | x] ^= 0xFFFFFF;
            }
        }

        imagePanel.render();
    }

    private void drawNametable(final PPU ppu, int address,
                               final int backgroundPatternTableAddress, final int[] screen,
                               final int screenX, final int screenY) {

        for (int tileY = 0, attributeAddress = address | 0x03C0; tileY < 32;
             tileY += 4) {
            for (int tileX = 0; tileX < 32; tileX += 4, attributeAddress++) {
                int attribute = ppu.peekVRAM(attributeAddress);
                for (int y = 0; y < 4; y += 2) {
                    final int yOffset = tileY | y;
                    for (int x = 0; x < 4; x += 2) {
                        final int xOffset = tileX | x;
                        final int[] cols = colors[attribute & 0x03];
                        attribute >>= 2;
                        for (int i = 0; i < 2; i++) {
                            for (int j = 0; j < 2; j++) {
                                attributes[yOffset | i][xOffset | j] = cols;
                            }
                        }
                    }
                }
            }
        }

        for (int tileY = 0; tileY < 30; tileY++) {
            final int yOffset = screenY + (tileY << 3);
            for (int tileX = 0; tileX < 32; tileX++, address++) {
                final int xOffset = screenX + (tileX << 3);
                final int[] cols = attributes[tileY][tileX];
                final int address0 = backgroundPatternTableAddress
                        | (ppu.peekVRAM(address) << 4);
                final int address1 = address0 + 8;
                for (int y = 0; y < 8; y++) {
                    final int yOff = (yOffset + y) << 9;
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

    private String createCopyString(final boolean spaced) {
        final PPU p = ppu;
        int start = selectionStartAddress;
        int end = selectionEndAddress;
        if (p == null || start < 0 || end < 0) {
            return null;
        } else if (end < start) {
            final int t = start;
            start = end;
            end = t;
        }
        final StringBuilder sb = new StringBuilder();
        for (int address = start; address <= end; address++) {
            if (spaced && sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(String.format("%02X", p.peekVRAM(address)));
        }
        return sb.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nametablesPanel = new ImagePanel(512, 480);
        resizeButton = new javax.swing.JButton();
        scanlineLabel = new javax.swing.JLabel();
        scanlineTextField = new javax.swing.JTextField();
        framesPerUpdateLabel = new javax.swing.JLabel();
        framesPerUpdateComboBox = new javax.swing.JComboBox();
        addressNameLabel = new javax.swing.JLabel();
        addressLabel = new javax.swing.JLabel();
        tileNameLabel = new javax.swing.JLabel();
        tileLabel = new javax.swing.JLabel();
        scrollLabel = new javax.swing.JLabel();
        scrollComboBox = new javax.swing.JComboBox();
        sprite0CheckBox = new javax.swing.JCheckBox();
        closeButton = new javax.swing.JButton();
        showTileGridCheckBox = new javax.swing.JCheckBox();
        showAttributeGridCheckBox = new javax.swing.JCheckBox();
        selectionNameLabel = new javax.swing.JLabel();
        selectionLabel = new javax.swing.JLabel();
        copyButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        copySpacedButton = new javax.swing.JButton();
        defaultsButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Nametables");
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        nametablesPanel.setMaximumSize(null);

        javax.swing.GroupLayout nametablesPanelLayout = new javax.swing.GroupLayout(nametablesPanel);
        nametablesPanel.setLayout(nametablesPanelLayout);
        nametablesPanelLayout.setHorizontalGroup(
                nametablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        nametablesPanelLayout.setVerticalGroup(
                nametablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        resizeButton.setMnemonic('R');
        resizeButton.setText("Resize");
        resizeButton.setFocusPainted(false);
        resizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resizeButtonActionPerformed(evt);
            }
        });

        scanlineLabel.setText("Update on scanline:");
        scanlineLabel.setMaximumSize(null);
        scanlineLabel.setMinimumSize(null);
        scanlineLabel.setPreferredSize(null);

        scanlineTextField.setColumns(4);
        scanlineTextField.setText("0");
        scanlineTextField.setMaximumSize(null);
        scanlineTextField.setMinimumSize(null);
        scanlineTextField.setPreferredSize(null);
        scanlineTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanlineTextFieldActionPerformed(evt);
            }
        });

        framesPerUpdateLabel.setText("Frames per update:");
        framesPerUpdateLabel.setMaximumSize(null);
        framesPerUpdateLabel.setMinimumSize(null);
        framesPerUpdateLabel.setPreferredSize(null);

        framesPerUpdateComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"1", "5", "15", "30", "60"}));
        framesPerUpdateComboBox.setFocusable(false);
        framesPerUpdateComboBox.setMaximumSize(null);
        framesPerUpdateComboBox.setMinimumSize(null);
        framesPerUpdateComboBox.setPreferredSize(null);
        framesPerUpdateComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                framesPerUpdateComboBoxActionPerformed(evt);
            }
        });

        addressNameLabel.setText("Address:");
        addressNameLabel.setMaximumSize(null);
        addressNameLabel.setMinimumSize(null);
        addressNameLabel.setPreferredSize(null);

        addressLabel.setText("-    ");
        addressLabel.setMaximumSize(null);
        addressLabel.setMinimumSize(null);
        addressLabel.setPreferredSize(null);

        tileNameLabel.setText("Tile:");
        tileNameLabel.setMaximumSize(null);
        tileNameLabel.setMinimumSize(null);
        tileNameLabel.setPreferredSize(null);

        tileLabel.setText("-   ");
        tileLabel.setMaximumSize(null);
        tileLabel.setMinimumSize(null);
        tileLabel.setPreferredSize(null);

        scrollLabel.setText("Scroll:");
        scrollLabel.setMaximumSize(null);
        scrollLabel.setMinimumSize(null);
        scrollLabel.setPreferredSize(null);

        scrollComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Disabled", "Absolute crosshairs", "Offset crosshairs", "Window"}));
        scrollComboBox.setFocusable(false);
        scrollComboBox.setMaximumSize(null);
        scrollComboBox.setMinimumSize(null);
        scrollComboBox.setName(""); // NOI18N
        scrollComboBox.setPreferredSize(null);
        scrollComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scrollComboBoxActionPerformed(evt);
            }
        });

        sprite0CheckBox.setText("Update on sprite 0 hit");
        sprite0CheckBox.setFocusPainted(false);
        sprite0CheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sprite0CheckBoxActionPerformed(evt);
            }
        });

        closeButton.setMnemonic('C');
        closeButton.setText(" Close ");
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        showTileGridCheckBox.setText("Show tile grid");
        showTileGridCheckBox.setFocusPainted(false);
        showTileGridCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTileGridCheckBoxActionPerformed(evt);
            }
        });

        showAttributeGridCheckBox.setText("Show attribute grid");
        showAttributeGridCheckBox.setFocusPainted(false);
        showAttributeGridCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAttributeGridCheckBoxActionPerformed(evt);
            }
        });

        selectionNameLabel.setText("Selection:");

        selectionLabel.setText("-");

        copyButton.setMnemonic('C');
        copyButton.setText("Copy");
        copyButton.setFocusPainted(false);
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
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

        copySpacedButton.setMnemonic('d');
        copySpacedButton.setText("Copy Spaced");
        copySpacedButton.setFocusPainted(false);
        copySpacedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copySpacedButtonActionPerformed(evt);
            }
        });

        defaultsButton.setMnemonic('D');
        defaultsButton.setText("Defaults");
        defaultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(addressNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(addressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(18, 18, 18)
                                                                                .addComponent(tileNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(tileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(18, 18, 18)
                                                                                .addComponent(selectionNameLabel)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(selectionLabel))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(scrollLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(scrollComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(19, 19, 19)
                                                                                .addComponent(showTileGridCheckBox)
                                                                                .addGap(18, 18, 18)
                                                                                .addComponent(showAttributeGridCheckBox))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(framesPerUpdateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(framesPerUpdateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(18, 18, 18)
                                                                                .addComponent(sprite0CheckBox)
                                                                                .addGap(15, 15, 15)
                                                                                .addComponent(scanlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(scanlineTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addComponent(nametablesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(defaultsButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(copyButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(copySpacedButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(searchButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(resizeButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(closeButton)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(nametablesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(addressNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tileNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(selectionNameLabel)
                                        .addComponent(selectionLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(scrollLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scrollComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(showTileGridCheckBox)
                                        .addComponent(showAttributeGridCheckBox))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(framesPerUpdateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(framesPerUpdateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scanlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scanlineTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(sprite0CheckBox))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(resizeButton)
                                        .addComponent(closeButton)
                                        .addComponent(copyButton)
                                        .addComponent(searchButton)
                                        .addComponent(copySpacedButton)
                                        .addComponent(defaultsButton))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, closeButton, copyButton, copySpacedButton, resizeButton, searchButton);

    }// </editor-fold>//GEN-END:initComponents

    private void resizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resizeButtonActionPerformed
        pack();
    }//GEN-LAST:event_resizeButtonActionPerformed

    private void framesPerUpdateComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_framesPerUpdateComboBoxActionPerformed
        final Object item = framesPerUpdateComboBox.getSelectedItem();
        if (item != null) {
            try {
                framesPerUpdate = Integer.parseInt(item.toString().trim());
            } catch (Throwable t) {
            }
        }
    }//GEN-LAST:event_framesPerUpdateComboBoxActionPerformed

    private void scanlineTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanlineTextFieldActionPerformed
        requestFocusInWindow();
    }//GEN-LAST:event_scanlineTextFieldActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void scrollComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollComboBoxActionPerformed
        scrollType = scrollComboBox.getSelectedIndex();
        render();
    }//GEN-LAST:event_scrollComboBoxActionPerformed

    private void sprite0CheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sprite0CheckBoxActionPerformed
        updateOnSprite0Hit = sprite0CheckBox.isSelected();
        updateScanlineComponents();
    }//GEN-LAST:event_sprite0CheckBoxActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        closeFrame();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void showTileGridCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTileGridCheckBoxActionPerformed
        showTileGrid = showTileGridCheckBox.isSelected();
        render();
    }//GEN-LAST:event_showTileGridCheckBoxActionPerformed

    private void showAttributeGridCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAttributeGridCheckBoxActionPerformed
        showAttributeGrid = showAttributeGridCheckBox.isSelected();
        render();
    }//GEN-LAST:event_showAttributeGridCheckBoxActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        App.createHexEditorFrame();
        final HexEditorFrame frame = App.getHexEditorFrame();
        frame.setDataSource(DataSource.FileContents);
        frame.getHexEditorView().showSearchDialog(false, createCopyString(true));
    }//GEN-LAST:event_searchButtonActionPerformed

    private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
        setClipboardString(createCopyString(false));
    }//GEN-LAST:event_copyButtonActionPerformed

    private void copySpacedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copySpacedButtonActionPerformed
        setClipboardString(createCopyString(true));
    }//GEN-LAST:event_copySpacedButtonActionPerformed

    private void defaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultsButtonActionPerformed
        loadFields(new NametablesPrefs());
    }//GEN-LAST:event_defaultsButtonActionPerformed
    public interface ScrollType {
        int Disabled = 0;
        int AbsoluteCrosshairs = 1;
        int OffsetCrosshairs = 2;
        int Window = 3;
    }
    // End of variables declaration//GEN-END:variables
}
