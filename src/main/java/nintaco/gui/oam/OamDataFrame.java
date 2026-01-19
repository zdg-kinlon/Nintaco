package nintaco.gui.oam;

import nintaco.App;
import nintaco.Machine;
import nintaco.MachineRunner;
import nintaco.PPU;
import nintaco.gui.ImagePanel;
import nintaco.gui.MaxLengthDocument;
import nintaco.palettes.PaletteUtil;
import nintaco.preferences.AppPrefs;
import nintaco.util.EDT;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import static nintaco.tv.TVSystem.PAL;
import static nintaco.util.BitUtil.getBitBool;
import static nintaco.util.BitUtil.reverseBits;
import static nintaco.util.GuiUtil.*;

public class OamDataFrame extends javax.swing.JFrame {

    public static final int IMAGE_WIDTH = 147;
    public static final int IMAGE_HEIGHT = 71;
    private static final int[] GRAY_PALETTE = {
            0x000000,
            0xFFFFFF,
            0xB2B2B2,
            0x757575,
    };
    private static final int ROWS_17 = 17 * IMAGE_WIDTH;
    private volatile MachineRunner machineRunner;
    private volatile Machine machine;
    private volatile PPU ppu;
    private volatile boolean updateOnSprite0Hit;
    private volatile boolean colorSprites;
    private volatile boolean showHiddenSprites;
    private volatile boolean highlightSelectedSprite;
    private volatile int updateScanline;
    private volatile int sprite0Hits;
    private volatile int framesPerUpdate;
    private volatile int frames;
    private volatile int spriteIndex;
    private volatile int hoverSpriteIndex;
    private int mouseX;
    private int mouseY;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel attributeLabel;
    private javax.swing.JTextField attributeTextField;
    private javax.swing.JButton closeButton;
    private javax.swing.JCheckBox colorCheckBox;
    private javax.swing.JButton defaultsButton;
    private javax.swing.JPanel displayPanel;
    private javax.swing.JComboBox<String> framesPerUpdateComboBox;
    private javax.swing.JLabel framesPerUpdateLabel;
    private javax.swing.JCheckBox highlightCheckBox;
    private javax.swing.JLabel oamAddressLabel;
    private javax.swing.JTextField oamAddressTextField;
    private javax.swing.JPanel oamDataPanel;
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JTextField priorityTextField;
    private javax.swing.JButton resizeButton;
    private javax.swing.JLabel scanlineLabel;
    private javax.swing.JTextField scanlineTextField;
    private final DocumentListener scanlineListener
            = createDocumentListener(this::scanlineUpdated);
    private javax.swing.JCheckBox showHiddenSpritesCheckBox;
    private javax.swing.JCheckBox sprite0CheckBox;
    private javax.swing.JComboBox<String> spriteIndexComboBox;
    private javax.swing.JLabel spriteIndexLabel;
    private javax.swing.JPanel spritePanel;
    private javax.swing.JLabel tileAddressLabel;
    private javax.swing.JTextField tileAddressTextField;
    private javax.swing.JLabel tileLabel;
    private javax.swing.JTextField tileTextField;
    private javax.swing.JCheckBox xFlipCheckBox;
    private javax.swing.JLabel xLabel;
    private javax.swing.JTextField xTextField;
    private javax.swing.JCheckBox yFlipCheckBox;
    private javax.swing.JLabel yLabel;
    private javax.swing.JTextField yTextField;

    public OamDataFrame(final MachineRunner machineRunner) {
        initComponents();
        initSpriteIndexComboBox();
        scanlineTextField.setDocument(new MaxLengthDocument(3, scanlineListener));
        loadFields();
        setMachineRunner(machineRunner);
        oamDataPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(final MouseEvent e) {
                mouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(final MouseEvent e) {
                mouseUpdated(e.getX(), e.getY());
            }
        });
        oamDataPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                mouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                mouseUpdated(e.getX(), e.getY());
                mouseButtonPressed(e.getX(), e.getY(), e.isControlDown());
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                mouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
                mouseUpdated(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                mouseOutOfBounds();
            }
        });
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
    }

    private void initSpriteIndexComboBox() {
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("None");
        for (int i = 0; i < 64; i++) {
            model.addElement(Integer.toString(i));
        }
        spriteIndexComboBox.setModel(model);
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
        PPU.clearHighlightedSprite();
        saveFields();
        dispose();
    }

    private void closeFrame() {
        App.destroyOamDataFrame();
    }

    private void loadFields() {
        loadFields(AppPrefs.getInstance().getOamDataPrefs());
    }

    private void loadFields(final OamDataPrefs prefs) {
        updateScanline = prefs.getUpdateScanline();
        framesPerUpdate = prefs.getFramesPerUpdate();
        updateOnSprite0Hit = prefs.isUpdateOnSprite0Hit();
        spriteIndex = prefs.getSpriteIndex();
        colorSprites = prefs.isColorSprites();
        showHiddenSprites = prefs.isShowHiddenSprites();
        highlightSelectedSprite = prefs.isHighlightSelectedSprite();

        setScanlineTextFieldText(Integer.toString(updateScanline));
        framesPerUpdateComboBox.setSelectedItem(Integer.toString(framesPerUpdate));
        sprite0CheckBox.setSelected(updateOnSprite0Hit);
        spriteIndexComboBox.setSelectedIndex(spriteIndex + 1);
        colorCheckBox.setSelected(colorSprites);
        showHiddenSpritesCheckBox.setSelected(showHiddenSprites);
        highlightCheckBox.setSelected(highlightSelectedSprite);
        updateScanlineComponents();
    }

    private void saveFields() {
        final OamDataPrefs prefs = AppPrefs.getInstance().getOamDataPrefs();
        prefs.setUpdateScanline(updateScanline);
        prefs.setFramesPerUpdate(framesPerUpdate);
        prefs.setUpdateOnSprite0Hit(updateOnSprite0Hit);
        prefs.setSpriteIndex(spriteIndex);
        prefs.setColorSprites(colorSprites);
        prefs.setShowHiddenSprites(showHiddenSprites);
        prefs.setHighlightSelectedSprite(highlightSelectedSprite);
        AppPrefs.save();
    }

    public final void setMachineRunner(final MachineRunner machineRunner) {
        this.machineRunner = machineRunner;
        if (machineRunner == null) {
            machine = null;
            ppu = null;
            ((ImagePanel) oamDataPanel).clearScreen();
        } else {
            machine = machineRunner.getMachine();
            ppu = machine.getPPU();
            if (machineRunner.isPaused()) {
                update(machine, ppu);
            }
        }
        final boolean enabled = machine != null;
        EDT.async(() -> enableComponents(enabled));
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
        if (--frames > 0 /*&& lastPaletteBoxX == paletteBoxX*/) {
            return;
        }
        frames = framesPerUpdate;

        update(m, p);
    }

    public void update(final Machine m, final PPU p) {

        final ImagePanel imagePanel = (ImagePanel) oamDataPanel;
        if (m == null || p == null) {
            imagePanel.clearScreen();
            return;
        }

        final int[] screen = imagePanel.getScreen();
        for (int i = screen.length - 1; i >= 0; i--) {
            screen[i] = 0;
        }

        for (int i = 4; i >= 0; i--) {
            final int so = IMAGE_WIDTH + i * ROWS_17;
            for (int x = IMAGE_WIDTH - 3; x >= 0; x--) {
                screen[1 + so + x] = 0x3F3F3F;
            }
        }
        for (int so = (IMAGE_HEIGHT - 2) * IMAGE_WIDTH; so >= IMAGE_WIDTH;
             so -= IMAGE_WIDTH) {
            for (int x = 1; x < IMAGE_WIDTH; x += 9) {
                screen[so + x] = 0x3F3F3F;
            }
        }
        if (spriteIndex >= 0) {
            final int X = 1 + (spriteIndex & 0x0F) * 9;
            final int Y = IMAGE_WIDTH + (spriteIndex >> 4) * ROWS_17;
            for (int x = 9; x >= 0; x--) {
                final int offset = Y + X + x;
                screen[offset + ROWS_17] = screen[offset] = 0xFFFFFF;
            }
            for (int y = 16, offset = Y + X + IMAGE_WIDTH; y >= 1; y--,
                    offset += IMAGE_WIDTH) {
                screen[offset + 9] = screen[offset] = 0xFFFFFF;
            }
        }
        if (hoverSpriteIndex >= 0) {
            final int X = 1 + (hoverSpriteIndex & 0x0F) * 9;
            final int Y = IMAGE_WIDTH + (hoverSpriteIndex >> 4) * ROWS_17;
            for (int x = 9; x >= 0; x--) {
                final int offset = Y + X + x;
                screen[offset + ROWS_17] = screen[offset] = 0xFFFF00;
            }
            for (int y = 16, offset = Y + X + IMAGE_WIDTH; y >= 1; y--,
                    offset += IMAGE_WIDTH) {
                screen[offset + 9] = screen[offset] = 0xFFFF00;
            }
        }

        renderSpriteTiles(m, p, screen);

        imagePanel.render();

        final int[] OAM = p.getOAM();
        final int x;
        final int y;
        final int tile;
        final int attribute;
        final int oamAddress;
        final int tileAddress;
        final boolean priority;
        final boolean xFlip;
        final boolean yFlip;
        if (spriteIndex >= 0) {
            final int index = spriteIndex << 2;
            y = OAM[index];
            tile = OAM[index + 1];
            attribute = OAM[index + 2];
            x = OAM[index + 3];
            oamAddress = index;
            tileAddress = ppu.isSpriteSize8x16() ? ((tile & 1) << 12)
                    | ((tile & 0xFE) << 4) : ppu.getSpritePatternTableAddress()
                    | (tile << 4);
            priority = getBitBool(attribute, 5);
            xFlip = getBitBool(attribute, 6);
            yFlip = getBitBool(attribute, 7);
            if (highlightSelectedSprite) {
                PPU.setHighlightedSprite(x, y);
            }
        } else {
            x = y = tile = attribute = oamAddress = tileAddress = -1;
            priority = xFlip = yFlip = false;
            PPU.clearHighlightedSprite();
        }
        EDT.async(() -> {
            if (x >= 0) {
                updateSpriteFields(x, y, tile, attribute, oamAddress, tileAddress,
                        priority, xFlip, yFlip);
            }
            mouseUpdated(mouseX, mouseY);
        });
    }

    private void renderSpriteTiles(final Machine machine, final PPU ppu,
                                   final int[] screen) {

        final int[] OAM = ppu.getOAM();
        final int[] palette = PaletteUtil.getExtendedPalette(machine);
        final int[] paletteRAM = ppu.getPaletteRAM();
        final int patternTableAddress = ppu.getSpritePatternTableAddress();
        final boolean size8x16 = ppu.isSpriteSize8x16();
        for (int i = 0xFC; i >= 0; i -= 4) {
            final int x = 2 + ((i >> 2) & 0x0F) * 9;
            final int y = 2 + ((i >> 6) & 0x03) * 17;
            final int spriteY = OAM[i];
            final boolean hidden = spriteY >= 0xEF;
            if (hidden && !showHiddenSprites) {
                continue;
            }
            final int tileIndex = OAM[i + 1];
            final int attribute = OAM[i + 2];
            final int paletteIndex = 0x10 | ((attribute & 0x03) << 2);
            final boolean flipHorizontally = getBitBool(attribute, 6);
            final boolean flipVertically = getBitBool(attribute, 7);

            if (size8x16) {
                int address0 = ((tileIndex & 1) << 12) | ((tileIndex & 0xFE) << 4);
                int address1 = address0 | 0x0010;
                if (flipVertically) {
                    final int temp = address0;
                    address0 = address1;
                    address1 = temp;
                }
                renderTile(ppu, screen, address0, paletteIndex, paletteRAM, palette, x,
                        y, flipHorizontally, flipVertically);
                renderTile(ppu, screen, address1, paletteIndex, paletteRAM, palette, x,
                        y + 8, flipHorizontally, flipVertically);
            } else {
                renderTile(ppu, screen, patternTableAddress | (tileIndex << 4),
                        paletteIndex, paletteRAM, palette, x, y, flipHorizontally,
                        flipVertically);
            }

            if (hidden) {
                for (int j = 15; j >= 0; j--) {
                    screen[(y + j) * IMAGE_WIDTH + x + (j >> 1)] = 0xFF0000;
                }
            }
        }
    }

    private void renderTile(final PPU ppu, final int[] screen,
                            final int patternTableAddress, final int paletteIndex,
                            final int[] paletteRAM, final int[] palette, final int x, final int y,
                            final boolean flipHorizontally, final boolean flipVertically) {
        for (int i = 7; i >= 0; i--) {
            final int address = patternTableAddress | i;
            int b0 = ppu.peekVRAM(address);
            int b1 = ppu.peekVRAM(address | 0x08);
            if (flipHorizontally) {
                b0 = reverseBits(b0);
                b1 = reverseBits(b1);
            }
            final int rowIndex = (y + (flipVertically ? (7 - i) : i)) * IMAGE_WIDTH;
            for (int j = 7; j >= 0; j--) {
                final int v = ((b1 & 1) << 1) | (b0 & 1);
                b0 >>= 1;
                b1 >>= 1;
                final int index = paletteIndex | v;
                if ((index & 0x03) != 0) {
                    screen[rowIndex + x + j] = colorSprites
                            ? palette[paletteRAM[index & 0x1F]] : GRAY_PALETTE[index & 0x03];
                }
            }
        }
    }

    private void updateSpriteFields(final int x, final int y, final int tile,
                                    final int attribute, final int oamAddress, final int tileAddress,
                                    final boolean priority, final boolean xFlip, final boolean yFlip) {
        xTextField.setText(Integer.toString(x));
        yTextField.setText(Integer.toString(y));
        tileTextField.setText(String.format("%02X", tile));
        attributeTextField.setText(String.format("%02X", attribute));
        oamAddressTextField.setText(String.format("%02X", oamAddress));
        tileAddressTextField.setText(String.format("%04X", tileAddress));
        priorityTextField.setText(priority ? "Behind" : "In Front");
        xFlipCheckBox.setSelected(xFlip);
        yFlipCheckBox.setSelected(yFlip);
    }

    private void updateScanlineComponents() {
        final boolean enabled = !sprite0CheckBox.isSelected();
        scanlineLabel.setEnabled(enabled);
        scanlineTextField.setEnabled(enabled);
    }

    private void mouseUpdated(final int x, final int y) {
        if (mouseX == x && mouseY == y) {
            return;
        }
        mouseX = x;
        mouseY = y;
        hoverSpriteIndex = getSpriteIndex(x, y);

        final MachineRunner r = machineRunner;
        if (r != null && r.isPaused()) {
            update(machine, ppu);
        }
    }

    private void mouseOutOfBounds() {
        mouseX = -1;
        mouseY = -1;
        hoverSpriteIndex = -1;
        final MachineRunner r = machineRunner;
        if (r != null && r.isPaused()) {
            update(machine, ppu);
        }
    }

    private void mouseButtonPressed(final int x, final int y,
                                    final boolean controlDown) {
        final int index = getSpriteIndex(x, y);
        spriteIndexComboBox.setSelectedIndex(index == spriteIndex && controlDown
                ? 0 : index + 1);
        final MachineRunner r = machineRunner;
        if (r != null && r.isPaused()) {
            update(machine, ppu);
        }
    }

    private int getSpriteIndex(final int x, final int y) {
        if (x >= 1 && y >= 1 && x <= IMAGE_WIDTH - 3 && y <= IMAGE_HEIGHT - 3) {
            return (x - 1) / 9 + (((y - 1) / 17) << 4);
        } else {
            return -1;
        }
    }

    private void enableComponents(final boolean enabled) {
        framesPerUpdateLabel.setEnabled(enabled);
        framesPerUpdateComboBox.setEnabled(enabled);
        colorCheckBox.setEnabled(enabled);
        showHiddenSpritesCheckBox.setEnabled(enabled);
        sprite0CheckBox.setEnabled(enabled);
        scanlineLabel.setEnabled(enabled);
        scanlineTextField.setEnabled(enabled);
        spriteIndexLabel.setEnabled(enabled);
        spriteIndexComboBox.setEnabled(enabled);
        highlightCheckBox.setEnabled(enabled);
        defaultsButton.setEnabled(enabled);
        enableSpriteComponents(enabled);
        if (!enabled) {
            spriteIndexComboBox.setSelectedIndex(0);
        }
    }

    private void enableSpriteComponents(boolean enabled) {
        enabled &= spriteIndex >= 0;
        xLabel.setEnabled(enabled);
        xTextField.setEnabled(enabled);
        yLabel.setEnabled(enabled);
        yTextField.setEnabled(enabled);
        tileLabel.setEnabled(enabled);
        tileTextField.setEnabled(enabled);
        attributeLabel.setEnabled(enabled);
        attributeTextField.setEnabled(enabled);
        oamAddressLabel.setEnabled(enabled);
        oamAddressTextField.setEnabled(enabled);
        tileAddressLabel.setEnabled(enabled);
        tileAddressTextField.setEnabled(enabled);
        xFlipCheckBox.setEnabled(enabled);
        yFlipCheckBox.setEnabled(enabled);
        priorityLabel.setEnabled(enabled);
        priorityTextField.setEnabled(enabled);
        if (!enabled) {
            xTextField.setText("");
            yTextField.setText("");
            tileTextField.setText("");
            attributeTextField.setText("");
            oamAddressTextField.setText("");
            tileAddressTextField.setText("");
            xFlipCheckBox.setSelected(false);
            yFlipCheckBox.setSelected(false);
            priorityTextField.setText("");
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

        closeButton = new javax.swing.JButton();
        resizeButton = new javax.swing.JButton();
        oamDataPanel = new ImagePanel(IMAGE_WIDTH, IMAGE_HEIGHT, 3);
        displayPanel = new javax.swing.JPanel();
        scanlineLabel = new javax.swing.JLabel();
        framesPerUpdateComboBox = new javax.swing.JComboBox<>();
        framesPerUpdateLabel = new javax.swing.JLabel();
        scanlineTextField = new javax.swing.JTextField();
        colorCheckBox = new javax.swing.JCheckBox();
        sprite0CheckBox = new javax.swing.JCheckBox();
        showHiddenSpritesCheckBox = new javax.swing.JCheckBox();
        highlightCheckBox = new javax.swing.JCheckBox();
        spritePanel = new javax.swing.JPanel();
        spriteIndexLabel = new javax.swing.JLabel();
        spriteIndexComboBox = new javax.swing.JComboBox<>();
        xLabel = new javax.swing.JLabel();
        xTextField = new javax.swing.JTextField();
        yLabel = new javax.swing.JLabel();
        yTextField = new javax.swing.JTextField();
        tileLabel = new javax.swing.JLabel();
        tileTextField = new javax.swing.JTextField();
        attributeLabel = new javax.swing.JLabel();
        attributeTextField = new javax.swing.JTextField();
        oamAddressLabel = new javax.swing.JLabel();
        oamAddressTextField = new javax.swing.JTextField();
        tileAddressLabel = new javax.swing.JLabel();
        tileAddressTextField = new javax.swing.JTextField();
        priorityLabel = new javax.swing.JLabel();
        priorityTextField = new javax.swing.JTextField();
        xFlipCheckBox = new javax.swing.JCheckBox();
        yFlipCheckBox = new javax.swing.JCheckBox();
        defaultsButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("OAM Data");
        setMaximumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
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

        resizeButton.setMnemonic('R');
        resizeButton.setText("Resize");
        resizeButton.setFocusPainted(false);
        resizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resizeButtonActionPerformed(evt);
            }
        });

        oamDataPanel.setMaximumSize(null);

        javax.swing.GroupLayout oamDataPanelLayout = new javax.swing.GroupLayout(oamDataPanel);
        oamDataPanel.setLayout(oamDataPanelLayout);
        oamDataPanelLayout.setHorizontalGroup(
                oamDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        oamDataPanelLayout.setVerticalGroup(
                oamDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 154, Short.MAX_VALUE)
        );

        displayPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Display"));
        displayPanel.setMaximumSize(null);

        scanlineLabel.setText("Update on scanline:");
        scanlineLabel.setMaximumSize(null);
        scanlineLabel.setMinimumSize(null);
        scanlineLabel.setPreferredSize(null);

        framesPerUpdateComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"1", "5", "15", "30", "60"}));
        framesPerUpdateComboBox.setFocusable(false);
        framesPerUpdateComboBox.setMaximumSize(null);
        framesPerUpdateComboBox.setMinimumSize(null);
        framesPerUpdateComboBox.setPreferredSize(null);
        framesPerUpdateComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                framesPerUpdateComboBoxActionPerformed(evt);
            }
        });

        framesPerUpdateLabel.setText("Frames per update:");
        framesPerUpdateLabel.setMaximumSize(null);
        framesPerUpdateLabel.setMinimumSize(null);
        framesPerUpdateLabel.setPreferredSize(null);

        scanlineTextField.setColumns(4);
        scanlineTextField.setMaximumSize(null);
        scanlineTextField.setMinimumSize(null);
        scanlineTextField.setPreferredSize(null);
        scanlineTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanlineTextFieldActionPerformed(evt);
            }
        });

        colorCheckBox.setSelected(true);
        colorCheckBox.setText("Color");
        colorCheckBox.setFocusPainted(false);
        colorCheckBox.setMaximumSize(null);
        colorCheckBox.setMinimumSize(null);
        colorCheckBox.setPreferredSize(null);
        colorCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorCheckBoxActionPerformed(evt);
            }
        });

        sprite0CheckBox.setText("Update on sprite 0 hit");
        sprite0CheckBox.setFocusPainted(false);
        sprite0CheckBox.setMaximumSize(null);
        sprite0CheckBox.setMinimumSize(null);
        sprite0CheckBox.setPreferredSize(null);
        sprite0CheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sprite0CheckBoxActionPerformed(evt);
            }
        });

        showHiddenSpritesCheckBox.setText("Show hidden sprites");
        showHiddenSpritesCheckBox.setFocusPainted(false);
        showHiddenSpritesCheckBox.setMaximumSize(null);
        showHiddenSpritesCheckBox.setMinimumSize(null);
        showHiddenSpritesCheckBox.setPreferredSize(null);
        showHiddenSpritesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHiddenSpritesCheckBoxActionPerformed(evt);
            }
        });

        highlightCheckBox.setText("Highlight selected sprite");
        highlightCheckBox.setFocusPainted(false);
        highlightCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                highlightCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout displayPanelLayout = new javax.swing.GroupLayout(displayPanel);
        displayPanel.setLayout(displayPanelLayout);
        displayPanelLayout.setHorizontalGroup(
                displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(displayPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(highlightCheckBox)
                                        .addGroup(displayPanelLayout.createSequentialGroup()
                                                .addComponent(framesPerUpdateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(framesPerUpdateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(colorCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(showHiddenSpritesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(displayPanelLayout.createSequentialGroup()
                                                .addComponent(sprite0CheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(scanlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(scanlineTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        displayPanelLayout.setVerticalGroup(
                displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(displayPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(framesPerUpdateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(framesPerUpdateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(colorCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(showHiddenSpritesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(sprite0CheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scanlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scanlineTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(highlightCheckBox)
                                .addContainerGap())
        );

        spritePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sprite"));
        spritePanel.setMaximumSize(null);

        spriteIndexLabel.setText("Index:");
        spriteIndexLabel.setMaximumSize(null);
        spriteIndexLabel.setMinimumSize(null);
        spriteIndexLabel.setPreferredSize(null);

        spriteIndexComboBox.setFocusable(false);
        spriteIndexComboBox.setMaximumSize(null);
        spriteIndexComboBox.setMinimumSize(null);
        spriteIndexComboBox.setPreferredSize(null);
        spriteIndexComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spriteIndexComboBoxActionPerformed(evt);
            }
        });

        xLabel.setText("X:");
        xLabel.setMaximumSize(null);
        xLabel.setMinimumSize(null);
        xLabel.setPreferredSize(null);

        xTextField.setEditable(false);
        xTextField.setColumns(3);
        xTextField.setFocusable(false);
        xTextField.setMaximumSize(null);
        xTextField.setMinimumSize(null);
        xTextField.setPreferredSize(null);

        yLabel.setText("Y:");
        yLabel.setMaximumSize(null);
        yLabel.setMinimumSize(null);
        yLabel.setPreferredSize(null);

        yTextField.setEditable(false);
        yTextField.setColumns(3);
        yTextField.setFocusable(false);
        yTextField.setMaximumSize(null);
        yTextField.setMinimumSize(null);
        yTextField.setPreferredSize(null);

        tileLabel.setText("Tile:");
        tileLabel.setMaximumSize(null);
        tileLabel.setMinimumSize(null);
        tileLabel.setPreferredSize(null);

        tileTextField.setEditable(false);
        tileTextField.setColumns(2);
        tileTextField.setFocusable(false);
        tileTextField.setMaximumSize(null);
        tileTextField.setMinimumSize(null);
        tileTextField.setPreferredSize(null);

        attributeLabel.setText("Attribute:");
        attributeLabel.setMaximumSize(null);
        attributeLabel.setMinimumSize(null);
        attributeLabel.setPreferredSize(null);

        attributeTextField.setEditable(false);
        attributeTextField.setColumns(2);
        attributeTextField.setFocusable(false);
        attributeTextField.setMaximumSize(null);
        attributeTextField.setMinimumSize(null);
        attributeTextField.setPreferredSize(null);

        oamAddressLabel.setText("OAM address:");
        oamAddressLabel.setMaximumSize(null);
        oamAddressLabel.setMinimumSize(null);
        oamAddressLabel.setPreferredSize(null);

        oamAddressTextField.setEditable(false);
        oamAddressTextField.setColumns(2);
        oamAddressTextField.setFocusable(false);
        oamAddressTextField.setMaximumSize(null);
        oamAddressTextField.setMinimumSize(null);
        oamAddressTextField.setPreferredSize(null);

        tileAddressLabel.setText("Tile address:");
        tileAddressLabel.setMaximumSize(null);
        tileAddressLabel.setMinimumSize(null);
        tileAddressLabel.setPreferredSize(null);

        tileAddressTextField.setEditable(false);
        tileAddressTextField.setColumns(4);
        tileAddressTextField.setFocusable(false);
        tileAddressTextField.setMaximumSize(null);
        tileAddressTextField.setMinimumSize(null);
        tileAddressTextField.setPreferredSize(null);

        priorityLabel.setText("Priority:");
        priorityLabel.setMaximumSize(null);
        priorityLabel.setMinimumSize(null);
        priorityLabel.setPreferredSize(null);

        priorityTextField.setEditable(false);
        priorityTextField.setColumns(8);
        priorityTextField.setFocusable(false);
        priorityTextField.setMaximumSize(null);
        priorityTextField.setMinimumSize(null);
        priorityTextField.setPreferredSize(null);

        xFlipCheckBox.setText("X-flip");
        xFlipCheckBox.setFocusPainted(false);
        xFlipCheckBox.setFocusable(false);
        xFlipCheckBox.setMaximumSize(null);
        xFlipCheckBox.setMinimumSize(null);
        xFlipCheckBox.setPreferredSize(null);

        yFlipCheckBox.setText("Y-flip");
        yFlipCheckBox.setFocusPainted(false);
        yFlipCheckBox.setFocusable(false);
        yFlipCheckBox.setMaximumSize(null);
        yFlipCheckBox.setMinimumSize(null);
        yFlipCheckBox.setPreferredSize(null);

        javax.swing.GroupLayout spritePanelLayout = new javax.swing.GroupLayout(spritePanel);
        spritePanel.setLayout(spritePanelLayout);
        spritePanelLayout.setHorizontalGroup(
                spritePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(spritePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(spritePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(spritePanelLayout.createSequentialGroup()
                                                .addComponent(xFlipCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(yFlipCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(spritePanelLayout.createSequentialGroup()
                                                .addComponent(spriteIndexLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(spriteIndexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(xLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(xTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(yLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(yTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(tileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(tileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(attributeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(attributeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(spritePanelLayout.createSequentialGroup()
                                                .addComponent(oamAddressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(oamAddressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(tileAddressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(tileAddressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(priorityLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(priorityTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        spritePanelLayout.setVerticalGroup(
                spritePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(spritePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(spritePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(spriteIndexLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(spriteIndexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(xLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(xTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(yLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(yTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(attributeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(attributeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(spritePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(oamAddressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(oamAddressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tileAddressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tileAddressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(priorityLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(priorityTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(spritePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(xFlipCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(yFlipCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        defaultsButton.setMnemonic('D');
        defaultsButton.setText("Defaults");
        defaultsButton.setFocusPainted(false);
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
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(defaultsButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(resizeButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(closeButton))
                                        .addComponent(oamDataPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(displayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(spritePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, closeButton, defaultsButton, resizeButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(oamDataPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(displayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spritePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(closeButton)
                                        .addComponent(resizeButton)
                                        .addComponent(defaultsButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        closeFrame();
    }//GEN-LAST:event_closeButtonActionPerformed

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

    private void sprite0CheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sprite0CheckBoxActionPerformed
        updateOnSprite0Hit = sprite0CheckBox.isSelected();
        updateScanlineComponents();
    }//GEN-LAST:event_sprite0CheckBoxActionPerformed

    private void spriteIndexComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spriteIndexComboBoxActionPerformed
        spriteIndex = spriteIndexComboBox.getSelectedIndex() - 1;
        enableSpriteComponents(machine != null);
    }//GEN-LAST:event_spriteIndexComboBoxActionPerformed

    private void colorCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorCheckBoxActionPerformed
        colorSprites = colorCheckBox.isSelected();
        final MachineRunner r = machineRunner;
        if (r != null && r.isPaused()) {
            update(machine, ppu);
        }
    }//GEN-LAST:event_colorCheckBoxActionPerformed

    private void showHiddenSpritesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showHiddenSpritesCheckBoxActionPerformed
        showHiddenSprites = showHiddenSpritesCheckBox.isSelected();
        final MachineRunner r = machineRunner;
        if (r != null && r.isPaused()) {
            update(machine, ppu);
        }
    }//GEN-LAST:event_showHiddenSpritesCheckBoxActionPerformed

    private void highlightCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highlightCheckBoxActionPerformed
        highlightSelectedSprite = highlightCheckBox.isSelected();
        if (!highlightSelectedSprite) {
            PPU.clearHighlightedSprite();
        }
    }//GEN-LAST:event_highlightCheckBoxActionPerformed

    private void defaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultsButtonActionPerformed
        loadFields(new OamDataPrefs());
    }//GEN-LAST:event_defaultsButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
