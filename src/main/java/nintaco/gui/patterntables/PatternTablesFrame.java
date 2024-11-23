package nintaco.gui.patterntables;

import nintaco.App;
import nintaco.Machine;
import nintaco.MachineRunner;
import nintaco.PPU;
import nintaco.gui.ImagePanel;
import nintaco.gui.MaxLengthDocument;
import nintaco.palettes.PaletteUtil;
import nintaco.preferences.AppPrefs;

import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import static nintaco.tv.TVSystem.PAL;
import static nintaco.util.GuiUtil.*;

public class PatternTablesFrame extends javax.swing.JFrame {

    private static final int[] GRAY_PALETTE = {
            0x000000,
            0xFFFFFF,
            0xB2B2B2,
            0x757575,
    };

    private final int[] colorSet = new int[4];
    private volatile MachineRunner machineRunner;
    private volatile Machine machine;
    private volatile PPU ppu;
    private volatile int sprite0Hits;
    private volatile int updateScanline;
    private volatile int colorSetIndex;
    private volatile int framesPerUpdate;
    private volatile int frames;
    private volatile int paletteBoxX;
    private volatile int lastPaletteBoxX = -1;
    private volatile int tileBoxX;
    private volatile int tileBoxY;
    private volatile int lastTileBoxX = -1;
    private volatile int lastTileBoxY = -1;
    private volatile boolean updateOnSprite0Hit;
    private int mouseX;
    private int mouseY;
    private int lastTile = -1;
    private int lastPalette = -1;
    private int lastColor = -1;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel colorLabel;
    private javax.swing.JLabel colorNameLabel;
    private javax.swing.JComboBox colorSetComboBox;
    private javax.swing.JLabel colorSetLabel;
    private javax.swing.JButton defaultsButton;
    private javax.swing.JComboBox framesPerUpdateComboBox;
    private javax.swing.JLabel framesPerUpdateLabel;
    private javax.swing.JLabel paletteLabel;
    private javax.swing.JLabel paletteNameLabel;
    private javax.swing.JPanel patternTablesPanel;
    private javax.swing.JButton resizeButton;
    private javax.swing.JLabel scanlineLabel;
    private javax.swing.JTextField scanlineTextField;
    private final DocumentListener scanlineListener
            = createDocumentListener(this::scanlineUpdated);
    private javax.swing.JCheckBox sprite0CheckBox;
    private javax.swing.JLabel tileLabel;
    private javax.swing.JLabel tileNameLabel;

    public PatternTablesFrame(final MachineRunner machineRunner) {
        initComponents();
        scanlineTextField.setDocument(new MaxLengthDocument(3, scanlineListener));
        loadFields();
        setMachineRunner(machineRunner);
        patternTablesPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseUpdated(e.getX(), e.getY());
            }
        });
        patternTablesPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseUpdated(e.getX(), e.getY());
                mouseButtonPressed(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                mouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseOutOfBounds();
            }
        });
        makeMonospaced(tileLabel);
        makeMonospaced(paletteLabel);
        makeMonospaced(colorLabel);
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
        resizeButton.requestFocus();
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

    private void mouseButtonPressed(final int x, final int y) {
        if (y >= 136 && y < 152 && x >= 0 && x < 256) {
            colorSetComboBox.setSelectedIndex((x >> 6) + 1);
            final MachineRunner r = machineRunner;
            if (r != null && r.isPaused()) {
                update(machine, ppu);
            }
        }
    }

    private void mouseUpdated(final int x, final int y) {

        if (mouseX == x && mouseY == y) {
            return;
        }
        mouseX = x;
        mouseY = y;
        final PPU p = ppu;
        final Machine m = machine;

        if (y >= 0 && y < 128) {
            tileBoxX = x & 0xF8;
            tileBoxY = y & 0x78;
            if (x >= 0 && x < 128) {
                final int tile = ((y >> 3) << 4) | (x >> 3);
                if (lastTile != tile) {
                    tileLabel.setText(String.format("$0%02X", tile));
                    lastTile = tile;
                }
            } else if (x > 128 && x < 256) {
                final int tile = ((y >> 3) << 4) | ((x - 129) >> 3);
                if (lastTile != tile) {
                    tileLabel.setText(String.format("$1%02X", tile));
                    lastTile = tile;
                }
            } else {
                if (lastTile != -1) {
                    tileLabel.setText("-   ");
                    lastTile = -1;
                }
            }
            paletteBoxX = -1;
        } else {
            if (lastTile != -1) {
                tileLabel.setText("-   ");
                lastTile = -1;
            }
            if (y >= 136 && y < 168 && x >= 0 && x < 256) {
                final int palette = ((y - 136) & 0xF0) | (x >> 4);
                if (lastPalette != palette) {
                    paletteLabel.setText(String.format("$%02X", palette));
                    lastPalette = palette;
                }

                if (p == null) {
                    if (lastColor != -1) {
                        colorLabel.setText("-  ");
                        lastColor = -1;
                    }
                } else {
                    final int color = p.getPaletteRAM()[palette];
                    if (lastColor != color) {
                        colorLabel.setText(String.format("$%02X", color));
                        lastColor = color;
                    }
                }
                paletteBoxX = (y < 152) ? (x & 0xC0) : -1;
            } else {
                if (lastPalette != -1) {
                    paletteLabel.setText("-  ");
                    lastPalette = -1;
                }
                if (lastColor != -1) {
                    colorLabel.setText("-  ");
                    lastColor = -1;
                }
                paletteBoxX = -1;
            }
            tileBoxX = tileBoxY = -1;
        }

        final MachineRunner r = machineRunner;
        if (r != null && r.isPaused()) {
            update(m, p);
        }
    }

    private void mouseOutOfBounds() {
        mouseX = -1;
        mouseY = -1;
        if (lastTile != -1) {
            tileLabel.setText("-   ");
            lastTile = -1;
        }
        if (lastPalette != -1) {
            paletteLabel.setText("-  ");
            lastPalette = -1;
        }
        if (lastColor != -1) {
            colorLabel.setText("-  ");
            lastColor = -1;
        }
        tileBoxX = tileBoxY = paletteBoxX = -1;
        final MachineRunner r = machineRunner;
        if (r != null && r.isPaused()) {
            update(machine, ppu);
        }
    }

    public void destroy() {
        saveFields();
        dispose();
    }

    private void closeFrame() {
        App.destroyPatternTablesFrame();
    }

    private void loadFields() {
        loadFields(AppPrefs.getInstance().getPatternTablesPrefs());
    }

    private void loadFields(final PatternTablesPrefs prefs) {
        updateScanline = prefs.getUpdateScanline();
        colorSetIndex = prefs.getColorSetIndex();
        framesPerUpdate = prefs.getFramesPerUpdate();
        updateOnSprite0Hit = prefs.isUpdateOnSprite0Hit();

        setScanlineTextFieldText(Integer.toString(updateScanline));
        colorSetComboBox.setSelectedIndex(colorSetIndex + 1);
        framesPerUpdateComboBox.setSelectedItem(Integer.toString(framesPerUpdate));
        sprite0CheckBox.setSelected(updateOnSprite0Hit);
        updateScanlineComponents();
    }

    private void saveFields() {
        final PatternTablesPrefs prefs = AppPrefs.getInstance()
                .getPatternTablesPrefs();
        prefs.setUpdateScanline(updateScanline);
        prefs.setColorSetIndex(colorSetIndex);
        prefs.setFramesPerUpdate(framesPerUpdate);
        prefs.setUpdateOnSprite0Hit(updateOnSprite0Hit);
        AppPrefs.save();
    }

    private void updateScanlineComponents() {
        final boolean enabled = !sprite0CheckBox.isSelected();
        scanlineLabel.setEnabled(enabled);
        scanlineTextField.setEnabled(enabled);
    }

    public final void setMachineRunner(final MachineRunner machineRunner) {
        this.machineRunner = machineRunner;
        if (machineRunner != null) {
            machine = machineRunner.getMachine();
        } else {
            machine = null;
        }
        if (machine == null) {
            ppu = null;
            ((ImagePanel) patternTablesPanel).clearScreen();
        } else {
            ppu = machine.getPPU();
            if (machineRunner.isPaused()) {
                update(machine, ppu);
            }
        }
    }

    public void update(final int scanline) {
        final Machine m = machine;
        final PPU p = ppu;
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
        if (--frames > 0 && lastPaletteBoxX == paletteBoxX
                && lastTileBoxX == tileBoxX && lastTileBoxY == tileBoxY) {
            return;
        }
        frames = framesPerUpdate;
        update(m, p);
    }

    private synchronized void update(final Machine m, final PPU p) {

        final ImagePanel imagePanel = (ImagePanel) patternTablesPanel;
        if (m == null || p == null) {
            imagePanel.clearScreen();
            return;
        }

        final int[] palette = PaletteUtil.getExtendedPalette(m);
        final int[] colors;
        if (colorSetIndex < 0) {
            colors = GRAY_PALETTE;
        } else {
            colors = colorSet;
            final int offset = colorSetIndex << 2;
            for (int i = 3; i >= 0; i--) {
                colors[i] = palette[p.getPaletteRamValue(offset | i)];
            }
        }

        final int[] screen = imagePanel.getScreen();
        for (int i = 0; i < 2; i++) {
            final int xOffset = i == 0 ? 0x00 : 0x80;
            int address = i == 0 ? 0x0000 : 0x1000;
            for (int tileY = 0; tileY < 128; tileY += 8) {
                for (int tileX = 0; tileX < 128; tileX += 8, address += 16) {
                    final int address1 = address | 0x0008;
                    final int screenX = xOffset | tileX;
                    for (int y = 0; y < 8; y++) {
                        final int b0 = p.peekVRAM(address + y);
                        final int b1 = p.peekVRAM(address1 + y);
                        final int index = ((tileY | y) << 8) | screenX;
                        for (int x = 0; x < 8; x++) {
                            final int shift = 7 - x;
                            screen[index | x] = colors[(((b1 >> shift) & 1) << 1)
                                    | ((b0 >> shift) & 1)];
                        }
                    }
                }
            }
        }
        for (int i = 16; i >= 0; i -= 16) {
            final int yOffset = 136 + i;
            for (int j = 15; j >= 0; j--) {
                final int xStart = (j & 3) == 3 ? 14 : 15;
                final int color = palette[p.getPaletteRamValue(i | j)];
                final int xOffset = j << 4;
                for (int y = 15; y >= 0; y--) {
                    final int rowOffset = ((yOffset + y) << 8) | xOffset;
                    for (int x = xStart; x >= 0; x--) {
                        screen[rowOffset | x] = color;
                    }
                }
            }
        }
        final int boxX = paletteBoxX;
        lastPaletteBoxX = boxX;
        if (boxX >= 0) {
            final int row0 = (136 << 8) | boxX;
            final int row1 = (151 << 8) | boxX;
            for (int i = 62; i >= 0; i--) {
                screen[row0 | i] ^= 0xFFFFFF;
                screen[row1 | i] ^= 0xFFFFFF;
            }
            for (int i = 14; i >= 1; i--) {
                final int index = ((136 + i) << 8) | boxX;
                screen[index] ^= 0xFFFFFF;
                screen[index + 62] ^= 0xFFFFFF;
            }
        }
        final int tx = tileBoxX;
        final int ty = tileBoxY;
        lastTileBoxX = tx;
        lastTileBoxY = ty;
        if (tx >= 0 && ty >= 0) {
            final int ox0 = (ty << 8) | tx;
            final int ox7 = ((ty + 7) << 8) | tx;
            for (int i = 7; i >= 0; i--) {
                screen[ox0 + i] ^= 0xFFFFFF;
                screen[ox7 + i] ^= 0xFFFFFF;
            }
            for (int i = 6; i > 0; i--) {
                final int offset = ((ty + i) << 8) | tx;
                screen[offset] ^= 0xFFFFFF;
                screen[offset | 7] ^= 0xFFFFFF;
            }
        }
        imagePanel.render();

        EventQueue.invokeLater(() -> mouseUpdated(mouseX, mouseY));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        patternTablesPanel = new ImagePanel(256, 168, 2);
        paletteNameLabel = new javax.swing.JLabel();
        paletteLabel = new javax.swing.JLabel();
        tileNameLabel = new javax.swing.JLabel();
        tileLabel = new javax.swing.JLabel();
        scanlineLabel = new javax.swing.JLabel();
        scanlineTextField = new javax.swing.JTextField();
        framesPerUpdateLabel = new javax.swing.JLabel();
        framesPerUpdateComboBox = new javax.swing.JComboBox();
        resizeButton = new javax.swing.JButton();
        colorSetLabel = new javax.swing.JLabel();
        colorSetComboBox = new javax.swing.JComboBox();
        colorNameLabel = new javax.swing.JLabel();
        colorLabel = new javax.swing.JLabel();
        sprite0CheckBox = new javax.swing.JCheckBox();
        closeButton = new javax.swing.JButton();
        defaultsButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Pattern Tables");
        setMinimumSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        patternTablesPanel.setMaximumSize(null);

        javax.swing.GroupLayout patternTablesPanelLayout = new javax.swing.GroupLayout(patternTablesPanel);
        patternTablesPanel.setLayout(patternTablesPanelLayout);
        patternTablesPanelLayout.setHorizontalGroup(
                patternTablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        patternTablesPanelLayout.setVerticalGroup(
                patternTablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 111, Short.MAX_VALUE)
        );

        paletteNameLabel.setText("Palette:");
        paletteNameLabel.setMaximumSize(null);
        paletteNameLabel.setMinimumSize(null);
        paletteNameLabel.setPreferredSize(null);

        paletteLabel.setText("-");
        paletteLabel.setToolTipText("");
        paletteLabel.setMaximumSize(null);
        paletteLabel.setMinimumSize(null);
        paletteLabel.setPreferredSize(null);

        tileNameLabel.setText("Tile:");
        tileNameLabel.setMaximumSize(null);
        tileNameLabel.setMinimumSize(null);
        tileNameLabel.setPreferredSize(null);

        tileLabel.setText("-   ");
        tileLabel.setMaximumSize(null);
        tileLabel.setMinimumSize(null);
        tileLabel.setPreferredSize(null);

        scanlineLabel.setText("Update on scanline:");
        scanlineLabel.setToolTipText("");
        scanlineLabel.setMaximumSize(null);
        scanlineLabel.setMinimumSize(null);
        scanlineLabel.setPreferredSize(null);

        scanlineTextField.setColumns(4);
        scanlineTextField.setText("0");
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
        framesPerUpdateComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                framesPerUpdateComboBoxActionPerformed(evt);
            }
        });

        resizeButton.setMnemonic('R');
        resizeButton.setText("Resize");
        resizeButton.setPreferredSize(null);
        resizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resizeButtonActionPerformed(evt);
            }
        });

        colorSetLabel.setText("Color set:");
        colorSetLabel.setMaximumSize(null);
        colorSetLabel.setMinimumSize(null);
        colorSetLabel.setPreferredSize(null);

        colorSetComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"None", "0", "1", "2", "3"}));
        colorSetComboBox.setFocusable(false);
        colorSetComboBox.setMaximumSize(null);
        colorSetComboBox.setMinimumSize(null);
        colorSetComboBox.setPreferredSize(null);
        colorSetComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorSetComboBoxActionPerformed(evt);
            }
        });

        colorNameLabel.setText("Color:");
        colorNameLabel.setMaximumSize(null);
        colorNameLabel.setMinimumSize(null);
        colorNameLabel.setPreferredSize(null);

        colorLabel.setText("-  ");
        colorLabel.setMaximumSize(null);
        colorLabel.setMinimumSize(null);
        colorLabel.setPreferredSize(null);

        sprite0CheckBox.setText("Update on sprite 0 hit");
        sprite0CheckBox.setMaximumSize(null);
        sprite0CheckBox.setMinimumSize(null);
        sprite0CheckBox.setPreferredSize(null);
        sprite0CheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sprite0CheckBoxActionPerformed(evt);
            }
        });

        closeButton.setMnemonic('C');
        closeButton.setText("Close");
        closeButton.setPreferredSize(null);
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
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
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(patternTablesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(tileNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(tileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(paletteNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(paletteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(colorNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(colorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(colorSetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(colorSetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(framesPerUpdateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(framesPerUpdateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(sprite0CheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(scanlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(scanlineTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(defaultsButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(resizeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, closeButton, defaultsButton, resizeButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(patternTablesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(paletteNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(paletteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tileNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(colorNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(colorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(framesPerUpdateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(framesPerUpdateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(colorSetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(colorSetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(scanlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scanlineTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(sprite0CheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(resizeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(defaultsButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void resizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resizeButtonActionPerformed
        pack();
    }//GEN-LAST:event_resizeButtonActionPerformed

    private void colorSetComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorSetComboBoxActionPerformed
        final int value = colorSetComboBox.getSelectedIndex();
        colorSetIndex = value >= 0 ? value - 1 : -1;
        frames = 1;
    }//GEN-LAST:event_colorSetComboBoxActionPerformed

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

    private void sprite0CheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sprite0CheckBoxActionPerformed
        updateOnSprite0Hit = sprite0CheckBox.isSelected();
        updateScanlineComponents();
    }//GEN-LAST:event_sprite0CheckBoxActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        closeFrame();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void defaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultsButtonActionPerformed
        loadFields(new PatternTablesPrefs());
    }//GEN-LAST:event_defaultsButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
