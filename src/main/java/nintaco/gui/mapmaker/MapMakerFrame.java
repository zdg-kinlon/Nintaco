package nintaco.gui.mapmaker;

import nintaco.App;
import nintaco.Machine;
import nintaco.PPU;
import nintaco.gui.IntPoint;
import nintaco.mappers.Mapper;
import nintaco.palettes.PaletteUtil;
import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;
import nintaco.util.EDT;
import nintaco.util.GuiUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.List;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static nintaco.files.FileUtil.*;
import static nintaco.gui.mapmaker.CaptureType.*;
import static nintaco.tv.TVSystem.PAL;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.isBlank;

public class MapMakerFrame extends javax.swing.JFrame {

    private static final IntPoint[][] ORIGINS = new IntPoint[5][];

    static {
        for (int i = ORIGINS.length - 1; i >= 0; i--) {
            ORIGINS[i] = createOrigins(i + 1);
        }
    }

    private final int[][] patternTableAddresses = new int[60][64];
    private final int[][] paletteRamIndices = new int[60][64];

    private final IntPoint key = new IntPoint();
    private final IntPoint position = new IntPoint();

    private int[][] screenA = new int[30][32];
    private int[][] screenB = new int[30][32];

    private Map<IntPoint, MapTile> tiles = new HashMap<>();
    private int fileIndex;
    private String fileFormat;
    private String outputDir;
    private String filePrefix;

    private boolean capturedScreenA;
    private int sprite0Hits;

    private volatile Machine machine;
    private volatile Mapper mapper;
    private volatile PPU ppu;
    private volatile boolean running;
    private volatile int updateScanline;
    private volatile int captureType;
    private volatile int maxDifferences;
    private volatile int trackingSize;
    private volatile int flushDelay;
    private volatile int flushTimer;
    private volatile int startTileRow;
    private volatile int endTileRow;
    private volatile boolean lastStart;
    private volatile boolean paused;
    private volatile boolean resumeRequested;
    private volatile boolean updateOnSprite0Hit;
    private volatile boolean autoFlush;
    private volatile boolean autoPause;
    private volatile IntPoint[] origins;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoFlushCheckBox;
    private javax.swing.JCheckBox autoPauseCheckBox;
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox captureComboBox;
    private javax.swing.JLabel captureLabel;
    private javax.swing.JButton defaultsButton;
    private javax.swing.JLabel endTileRowLabel;
    private javax.swing.JSpinner endTileRowSpinner;
    private javax.swing.JComboBox fileFormatComboBox;
    private javax.swing.JLabel fileFormatLabel;
    private javax.swing.JLabel filePrefixLabel;
    private javax.swing.JTextField filePrefixTextField;
    private javax.swing.JButton flushButton;
    private javax.swing.JLabel flushDelayLabel;
    private javax.swing.JTextField flushDelayTextField;
    private javax.swing.JLabel maxDiffLabel;
    private javax.swing.JTextField maxDiffTextField;
    private javax.swing.JLabel outputDirLabel;
    private javax.swing.JTextField outputDirTextField;
    private javax.swing.JToggleButton pauseToggleButton;
    private javax.swing.JLabel scanlineLabel;
    private javax.swing.JTextField scanlineTextField;
    private javax.swing.JCheckBox sprite0CheckBox;
    private javax.swing.JLabel startIndexLabel;
    private javax.swing.JTextField startIndexTextField;
    private javax.swing.JLabel startTileRowLabel;
    private javax.swing.JSpinner startTileRowSpinner;
    private javax.swing.JToggleButton startToggleButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JComboBox trackingSizeComboBox;
    private javax.swing.JLabel trackingSizeLabel;

    public MapMakerFrame(final Machine machine) {
        initComponents();
        initFileFormatComboBox();
        initLoseFocusListeners();
        initSpinners();
        loadFields();
        setMachine(machine);
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
    }

    private static IntPoint[] createOrigins(final int size) {

        final List<IntPoint> qs = new ArrayList<>();
        for (int i = -size; i <= size; i++) {
            for (int j = -size; j <= size; j++) {
                if (i != 0 && j != 0) {
                    qs.add(new IntPoint(i, j));
                }
            }
        }
        Collections.sort(qs,
                (a, b) -> (a.x * a.x + a.y * a.y) - (b.x * b.x + b.y * b.y));

        final List<IntPoint> ps = new ArrayList<>();
        ps.add(new IntPoint(0, 0));
        for (int i = 1; i <= size; i++) {
            ps.add(new IntPoint(i, 0));
            ps.add(new IntPoint(-i, 0));
            ps.add(new IntPoint(0, i));
            ps.add(new IntPoint(0, -i));
        }
        ps.addAll(qs);

        final IntPoint[] os = new IntPoint[ps.size()];
        ps.toArray(os);
        return os;
    }

    private void initSpinners() {
        startTileRowSpinner.setModel(new SpinnerNumberModel(0, 0, 29, 1));
        startTileRowSpinner.setEditor(new JSpinner.NumberEditor(startTileRowSpinner,
                "#"));
        endTileRowSpinner.setModel(new SpinnerNumberModel(29, 0, 29, 1));
        endTileRowSpinner.setEditor(new JSpinner.NumberEditor(endTileRowSpinner,
                "#"));
        addLoseFocusListener(this, startTileRowSpinner);
        addLoseFocusListener(this, endTileRowSpinner);
    }

    private synchronized void flush() {
        final PPU pp = ppu;
        if (pp == null) {
            return;
        }
        final Map<IntPoint, MapTile> ts = tiles;
        final int index = fileIndex;
        new Thread(() -> {
            saveImage(pp, ts, index);
        }).start();
        tiles = new HashMap<>();
        fileIndex++;
        capturedScreenA = false;
        flushTimer = flushDelay;
        for (int i = 29; i >= 0; i--) {
            for (int j = 31; j >= 0; j--) {
                screenA[i][j] = screenB[i][j] = -1;
            }
        }
    }

    private void updateStartIndex() {
        EDT.async(() -> {
            filePrefix = filePrefixTextField.getText().trim();
            outputDir = outputDirTextField.getText().trim();
            final String prefix = filePrefix;
            final String outDir = outputDir;
            if (!(isBlank(prefix) || isBlank(outDir))) {
                new Thread(() -> {
                    final int index = getSuggestedStartIndex(prefix, outDir);
                    EDT.async(() -> {
                        fileIndex = index;
                        startIndexTextField.setText(Integer.toString(fileIndex));
                    });
                }).start();
            }
        });
    }

    private void saveImage(final PPU ppu, final Map<IntPoint, MapTile> tiles,
                           final int fileIndex) {

        File file = null;
        try {
            final Machine m = machine;
            if (m == null) {
                return;
            }
            final int[] palette = PaletteUtil.getExtendedPalette(m);
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (final IntPoint p : tiles.keySet()) {
                minX = min(minX, p.x);
                maxX = max(maxX, p.x);
                minY = min(minY, p.y);
                maxY = max(maxY, p.y);
            }
            final int width = (maxX - minX + 1) << 3;
            final int height = (maxY - minY + 1) << 3;

            mkdir(outputDir);

            final String fileName = String.format("%s-%03d.%s", filePrefix, fileIndex,
                    fileFormat);
            file = new File(appendSeparator(outputDir) + fileName);
            final BufferedImage image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer())
                    .getData();
            for (final Map.Entry<IntPoint, MapTile> entry : tiles.entrySet()) {
                final IntPoint p = entry.getKey();
                final byte[] paletteIndices = entry.getValue().getPaletteIndices();
                final int tx = (p.x - minX) << 3;
                final int ty = (p.y - minY) << 3;
                for (int y = 7; y >= 0; y--) {
                    final int offset = (ty | y) * width + tx;
                    final int row = y << 3;
                    for (int x = 7; x >= 0; x--) {
                        pixels[offset + x] = palette[paletteIndices[row | x] & 0xFF];
                    }
                }
            }
            ImageIO.write(image, fileFormat, file);
            EDT.async(() -> {
                if (running) {
                    statusLabel.setText("Saved: " + fileName);
                }
            });
        } catch (Throwable t) {
            setRunning(false);
            if (file == null) {
                displayError(this, "Failed to save image file.");
            } else {
                displayError(this, "Failed to save '" + file.getPath() + "'.");
            }
        }
    }

    private int compare(final IntPoint[] origins, final int[][] a,
                        final int[][] b) {

        int bestDifferences = 0x1000;
        int bestIndex = 0;
        for (int i = 0; i < origins.length; i++) {
            final IntPoint o = origins[i];
            final int differences = compare(a, b, o.x, o.y, bestDifferences);
            if (differences < bestDifferences) {
                bestIndex = i;
                bestDifferences = differences;
                if (differences == 0) {
                    break;
                }
            }
        }

        if (autoFlush && bestDifferences >= maxDifferences) {
            return -1;
        }

        return bestIndex;
    }

    private int compare(final int[][] a, final int[][] b, final int bx,
                        final int by, final int maxDifferences) {

        final int x0 = max(0, bx);
        final int y0 = max(0, by);
        final int x1 = min(32, bx + 32);
        final int y1 = min(30, by + 30);

        int differences = 0;
        for (int y = y0; y < y1; y++) {
            final int[] A = a[y];
            final int[] B = b[y - by];
            for (int x = x0; x < x1; x++) {
                if (A[x] != B[x - bx] && ++differences >= maxDifferences) {
                    return differences;
                }
            }
        }

        return differences;
    }

    public void destroy() {
        saveFields();
        dispose();
    }

    private void closeFrame() {
        App.destroyMapMakerFrame();
    }

    private void initLoseFocusListeners() {
        outputDirTextField.addActionListener(e -> {
            updateStartIndex();
            requestFocusInWindow();
        });
        filePrefixTextField.addActionListener(e -> {
            updateStartIndex();
            requestFocusInWindow();
        });
        addLoseFocusListener(this, startIndexTextField);
        addLoseFocusListener(this, maxDiffTextField);
        addLoseFocusListener(this, scanlineTextField);
        addLoseFocusListener(this, flushDelayTextField);
    }

    private void initFileFormatComboBox() {
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (final String format : GuiUtil.getWritableImageFileFormats()) {
            model.addElement(format);
        }
        fileFormatComboBox.setModel(model);
    }

    private void loadFields() {
        loadFields(AppPrefs.getInstance().getMapMakerAppPrefs());
    }

    private void loadFields(final MapMakerAppPrefs prefs) {
        outputDir = AppPrefs.getInstance().getPaths().getMapsDir();
        fileFormat = prefs.getFileFormat();
        flushDelay = prefs.getFlushDelay();

        outputDirTextField.setText(outputDir);
        fileFormatComboBox.setSelectedItem(fileFormat);
        flushDelayTextField.setText(Integer.toString(flushDelay));
        updateStartIndex();
    }

    private void saveFields() {
        if (running) {
            setRunning(false);
            flush();
        }

        EDT.sync(this::captureFields);
        saveGamePrefs();

        final MapMakerAppPrefs prefs = AppPrefs.getInstance().getMapMakerAppPrefs();
        AppPrefs.getInstance().getPaths().setMapsDir(outputDir);
        prefs.setFileFormat(fileFormat);
        prefs.setFlushDelay(flushDelay);
        AppPrefs.save();
    }

    private void resetFields() {
        loadFields(new MapMakerAppPrefs());
    }

    private void setSpinnersEnabled(final boolean enabled) {
        final boolean e = enabled
                && captureComboBox.getSelectedIndex() != VerticalBand;
        startTileRowLabel.setEnabled(e);
        startTileRowSpinner.setEnabled(e);
        endTileRowLabel.setEnabled(e);
        endTileRowSpinner.setEnabled(e);
    }

    private void loadGamePrefs() {
        loadGamePrefs(GamePrefs.getInstance().getMapMakerGamePrefs());
    }

    private void loadGamePrefs(final MapMakerGamePrefs prefs) {
        setAutoFlush(prefs.isAutoFlush());
        autoPause = prefs.isAutoPause();
        captureType = prefs.getCaptureType();
        filePrefix = isBlank(prefs.getFilePrefix()) ? getFileNameWithoutExtension(
                App.getEntryFileName()) : prefs.getFilePrefix();
        maxDifferences = prefs.getMaxDifferences();
        trackingSize = prefs.getTrackingSize();
        updateScanline = prefs.getUpdateScanline();
        updateOnSprite0Hit = prefs.isUpdateOnSprite0Hit();
        startTileRow = prefs.getStartTileRow();
        endTileRow = prefs.getEndTileRow();

        EDT.async(() -> {
            filePrefixTextField.setText(filePrefix);
            captureComboBox.setSelectedIndex(captureType);
            maxDiffTextField.setText(Integer.toString(maxDifferences));
            trackingSizeComboBox.setSelectedIndex(trackingSize - 1);
            scanlineTextField.setText(Integer.toString(updateScanline));
            sprite0CheckBox.setSelected(updateOnSprite0Hit);
            startTileRowSpinner.setValue(startTileRow);
            endTileRowSpinner.setValue(endTileRow);
            setScanlineComponentsEnabled(true);
            setSpinnersEnabled(true);
            updateStartIndex();
        });
    }

    private void resetGamePrefs() {
        loadGamePrefs(new MapMakerGamePrefs());
    }

    private void saveGamePrefs() {
        final MapMakerGamePrefs prefs = GamePrefs.getInstance()
                .getMapMakerGamePrefs();

        prefs.setAutoFlush(autoFlush);
        prefs.setAutoPause(autoPause);
        prefs.setCaptureType(captureType);
        prefs.setFilePrefix(filePrefix);
        prefs.setMaxDifferences(maxDifferences);
        prefs.setTrackingSize(trackingSize);
        prefs.setUpdateScanline(updateScanline);
        prefs.setUpdateOnSprite0Hit(updateOnSprite0Hit);
        prefs.setStartTileRow(startTileRow);
        prefs.setEndTileRow(endTileRow);

        GamePrefs.save();
    }

    private void setAutoFlush(final boolean autoFlush) {
        EDT.async(() -> {
            this.autoFlush = autoFlush;
            autoFlushCheckBox.setSelected(autoFlush);
            maxDiffLabel.setEnabled(autoFlush);
            maxDiffTextField.setEnabled(autoFlush);
        });
    }

    private void captureFields() {
        outputDir = outputDirTextField.getText().trim();
        filePrefix = filePrefixTextField.getText().trim();
        fileIndex = parseTextField(startIndexTextField, 0, 0, 999);
        final Object format = fileFormatComboBox.getSelectedItem();
        if (format == null) {
            fileFormat = "png";
        } else {
            fileFormat = format.toString();
        }
        captureType = captureComboBox.getSelectedIndex();
        if (captureType < 0) {
            captureType = VisibleWindow;
        }
        trackingSize = trackingSizeComboBox.getSelectedIndex() + 1;
        if (trackingSize == 0) {
            trackingSize = 1;
        }
        origins = ORIGINS[trackingSize - 1];
        autoFlush = autoFlushCheckBox.isSelected();
        autoPause = autoPauseCheckBox.isSelected();
        maxDifferences = parseTextField(maxDiffTextField, 160, 1, 960);
        updateOnSprite0Hit = sprite0CheckBox.isSelected();
        final Machine m = machine;
        updateScanline = parseTextField(scanlineTextField, 0, -1, (m == null
                ? PAL.getScanlineCount() : m.getMapper().getTVSystem()
                .getScanlineCount()) - 1);
        flushDelay = parseTextField(flushDelayTextField, 60, 0, 600);
        startTileRow = (int) startTileRowSpinner.getValue();
        endTileRow = (int) endTileRowSpinner.getValue();
        if (endTileRow < startTileRow) {
            final int temp = startTileRow;
            startTileRow = endTileRow;
            endTileRow = temp;
            startTileRowSpinner.setValue(startTileRow);
            endTileRowSpinner.setValue(endTileRow);
        }
    }

    private void enableComponents() {
        final Machine m = machine;
        final boolean run = m != null && running;

        startToggleButton.setEnabled(m != null);
        startToggleButton.setSelected(run);
        startToggleButton.setText(run ? "Stop" : "Start");
        flushButton.setEnabled(run);
        pauseToggleButton.setEnabled(run);

        final boolean enabled = !(m == null || run);
        outputDirLabel.setEnabled(enabled);
        outputDirTextField.setEnabled(enabled);
        browseButton.setEnabled(enabled);
        filePrefixLabel.setEnabled(enabled);
        filePrefixTextField.setEnabled(enabled);
        startIndexLabel.setEnabled(enabled);
        startIndexTextField.setEnabled(enabled);
        fileFormatLabel.setEnabled(enabled);
        fileFormatComboBox.setEnabled(enabled);
        captureLabel.setEnabled(enabled);
        captureComboBox.setEnabled(enabled);
        trackingSizeLabel.setEnabled(enabled);
        trackingSizeComboBox.setEnabled(enabled);
        autoFlushCheckBox.setEnabled(enabled);
        maxDiffLabel.setEnabled(enabled);
        maxDiffTextField.setEnabled(enabled);
        sprite0CheckBox.setEnabled(enabled);
        flushDelayLabel.setEnabled(enabled);
        flushDelayTextField.setEnabled(enabled);
        autoPauseCheckBox.setEnabled(enabled);
        defaultsButton.setEnabled(enabled);
        setScanlineComponentsEnabled(enabled);
        setSpinnersEnabled(enabled);

        statusLabel.setText(" ");
        if (m == null) {
            filePrefixTextField.setText("");
        }
    }

    private void setRunning(final boolean running) {
        EDT.async(() -> {
            if (!running && autoFlush) {
                flush();
            }
            if (!running) {
                setPaused(false);
                lastStart = false;
                startIndexTextField.setText(Integer.toString(fileIndex));
            }

            flushTimer = 0;
            this.running = running;
            enableComponents();
        });
    }

    private void setScanlineComponentsEnabled(final boolean enabled) {
        final boolean e = enabled && !sprite0CheckBox.isSelected();
        scanlineLabel.setEnabled(e);
        scanlineTextField.setEnabled(e);
    }

    public final void setMachine(final Machine machine) {
        if (running) {
            setRunning(false);
            flush();
        }
        this.machine = machine;
        if (machine == null) {
            ppu = null;
            mapper = null;
        } else {
            ppu = machine.getPPU();
            mapper = machine.getMapper();
            loadGamePrefs();
        }
        EDT.async(this::enableComponents);
    }

    public void update(final int scanline) {

        if (!running) {
            return;
        }

        final PPU pp = ppu;
        final Mapper m = mapper;
        if (m == null || pp == null) {
            return;
        } else if (updateOnSprite0Hit) {
            if (!pp.isSprite0Hit()) {
                sprite0Hits = 0;
                return;
            } else if (++sprite0Hits != 2) {
                return;
            }
        } else if (scanline != updateScanline) {
            return;
        }

        if (autoPause) {
            final boolean start = (m.getButtons() & 0x08080808) != 0;
            if (!lastStart && start) {
                if (paused) {
                    resumeRequested = true;
                } else {
                    setPaused(true);
                }
            }
            lastStart = start;
        }
        if (flushTimer > 0) {
            flushTimer--;
            return;
        }

        final int backgroundPatternTableAddress
                = pp.getBackgroundPatternTableAddress();
        readNametable(pp, 0x2000, backgroundPatternTableAddress, 0, 0);
        readNametable(pp, 0x2400, backgroundPatternTableAddress, 32, 0);
        readNametable(pp, 0x2800, backgroundPatternTableAddress, 0, 30);
        readNametable(pp, 0x2C00, backgroundPatternTableAddress, 32, 30);

        final int tileX = pp.getScrollX() >> 3;
        int scrollY = pp.getScrollY() - scanline;
        if (scrollY >= 480) {
            scrollY -= 480;
        } else if (scrollY < 0) {
            scrollY += 480;
        }
        final int tileY = scrollY >> 3;

        if (capturedScreenA) {
            copyNametables(tileX, tileY, screenB);
            final IntPoint[] orgs = origins;
            final int index = compare(orgs, screenA, screenB);
            if (paused && resumeRequested && index >= 0) {
                resumeRequested = false;
                setPaused(false);
            }
            if (!paused) {
                if (index > 0) {
                    final int[][] temp = screenA;
                    screenA = screenB;
                    screenB = temp;
                    final IntPoint delta = orgs[index];
                    position.translate(delta);
                    snapshotPatternTables(pp, tileX, tileY, position);
                } else if (index < 0) {
                    flush();
                }
            }
        } else {
            position.setX(0);
            position.setY(0);
            copyNametables(tileX, tileY, screenA);
            snapshotPatternTables(pp, tileX, tileY, position);
            capturedScreenA = true;
        }
    }

    private void setPaused(final boolean paused) {
        this.paused = paused;
        if (paused) {
            EDT.async(() -> {
                pauseToggleButton.setSelected(true);
                pauseToggleButton.setMnemonic('R');
                pauseToggleButton.setText("Resume");
            });
        } else {
            EDT.async(() -> {
                pauseToggleButton.setSelected(false);
                pauseToggleButton.setMnemonic('P');
                pauseToggleButton.setText("Pause");
            });
        }
    }

    private void copyNametables(final int tileX, final int tileY,
                                final int[][] copy) {

        for (int y = 29; y >= 0; y--) {
            final int[] c = copy[y];
            int Y = tileY + y;
            if (Y >= 60) {
                Y -= 60;
            }
            final int[] addresses = patternTableAddresses[Y];
            for (int x = 31; x >= 0; x--) {
                int X = tileX + x;
                if (X >= 64) {
                    X -= 64;
                }
                c[x] = addresses[X];
            }
        }
    }

    private void readNametable(final PPU ppu, int address,
                               final int backgroundPatternTableAddress, final int px, final int py) {

        for (int tileY = 0, attributeAddress = address | 0x03C0; tileY < 30;
             tileY += 4) {
            for (int tileX = 0; tileX < 32; tileX += 4, attributeAddress++) {
                int attribute = ppu.peekVRAM(attributeAddress);
                for (int y = 0; y < 4; y += 2) {
                    final int yOffset = tileY | y;
                    for (int x = 0; x < 4; x += 2) {
                        final int xOffset = tileX | x;
                        final int paletteRamIndex = (attribute & 0x03) << 2;
                        attribute >>= 2;
                        for (int i = 0; i < 2; i++) {
                            int Y = yOffset | i;
                            if (Y >= 30) {
                                break;
                            }
                            final int[] indices = paletteRamIndices[Y + py];
                            for (int j = 0; j < 2; j++) {
                                indices[px + (xOffset | j)] = paletteRamIndex;
                            }
                        }
                    }
                }
            }
        }

        for (int tileY = 0; tileY < 30; tileY++) {
            final int[] addresses = patternTableAddresses[py + tileY];
            for (int tileX = 0; tileX < 32; tileX++, address++) {
                addresses[px + tileX] = backgroundPatternTableAddress
                        | (ppu.peekVRAM(address) << 4);
            }
        }
    }

    private void snapshotPatternTables(final PPU ppu, int tileX,
                                       int tileY, final IntPoint position) {

        int Px = position.x;
        int Py = position.y;
        if ((tileX & 1) == 1) {
            tileX--;
            Px--;
        }
        if ((tileY & 1) == 1) {
            tileY--;
            Py--;
        }

        int yMin = 0, yMax = 0, xMin = 0, xMax = 0;
        int edgeMinY = 0, edgeMaxY = 0, edgeMinX = 0, edgeMaxX = 0;
        switch (captureType) {
            case VisibleWindow:
                xMin = tileX;
                xMax = tileX + 32;
                yMin = tileY + startTileRow;
                yMax = tileY + endTileRow + 1;
                edgeMinX = tileX + 2;
                edgeMaxX = tileX + 29;
                edgeMinY = tileY + 2;
                edgeMaxY = tileY + 27;
                break;
            case HorizontalBand:
                xMin = 0;
                xMax = 64;
                yMin = tileY + startTileRow;
                yMax = tileY + endTileRow + 1;
                edgeMinX = 0;
                edgeMaxX = 64;
                edgeMinY = tileY + 2;
                edgeMaxY = tileY + 27;
                break;
            case VerticalBand:
                xMin = tileX;
                xMax = tileX + 32;
                yMin = 0;
                yMax = 60;
                edgeMinX = tileX + 2;
                edgeMaxX = tileX + 29;
                edgeMinY = 0;
                edgeMaxY = 60;
                break;
        }

        for (int i = yMin; i < yMax; i++) {
            final boolean edgeTileY = i < edgeMinY || i > edgeMaxY;
            int Y = i;
            if (Y >= 60) {
                Y -= 60;
            }
            final int[] addresses = patternTableAddresses[Y];
            final int[] indices = paletteRamIndices[Y];
            final int py = Py + i - tileY;
            for (int j = xMin; j < xMax; j++) {
                final boolean edgeTile = edgeTileY || j < edgeMinX || j > edgeMaxX;
                key.x = Px + j - tileX;
                key.y = py;
                MapTile tile = tiles.get(key);
                if (tile == null || (!edgeTile && tile.isEdgeTile())) {
                    final byte[] paletteIndices;
                    if (tile == null) {
                        tile = new MapTile();
                        paletteIndices = new byte[64];
                        tile.setPaletteIndices(paletteIndices);
                    } else {
                        paletteIndices = tile.getPaletteIndices();
                    }
                    tile.setEdgeTile(edgeTile);
                    tiles.put(new IntPoint(key), tile);

                    int X = j;
                    if (X >= 64) {
                        X -= 64;
                    }
                    final int address0 = addresses[X];
                    final int address1 = address0 + 8;
                    final int index = indices[X];
                    for (int y = 0; y < 8; y++) {
                        final int row = y << 3;
                        final int b0 = ppu.peekVRAM(address0 + y);
                        final int b1 = ppu.peekVRAM(address1 + y);
                        for (int x = 0; x < 8; x++) {
                            final int shift = 7 - x;
                            paletteIndices[row | x] = (byte) ppu.getPaletteRamValue(index
                                    | (((b1 >> shift) & 1) << 1) | ((b0 >> shift) & 1));
                        }
                    }
                }
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

        startToggleButton = new javax.swing.JToggleButton();
        sprite0CheckBox = new javax.swing.JCheckBox();
        scanlineLabel = new javax.swing.JLabel();
        scanlineTextField = new javax.swing.JTextField();
        captureLabel = new javax.swing.JLabel();
        captureComboBox = new javax.swing.JComboBox();
        trackingSizeLabel = new javax.swing.JLabel();
        trackingSizeComboBox = new javax.swing.JComboBox();
        fileFormatLabel = new javax.swing.JLabel();
        fileFormatComboBox = new javax.swing.JComboBox();
        outputDirLabel = new javax.swing.JLabel();
        outputDirTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        filePrefixLabel = new javax.swing.JLabel();
        filePrefixTextField = new javax.swing.JTextField();
        startIndexLabel = new javax.swing.JLabel();
        startIndexTextField = new javax.swing.JTextField();
        flushButton = new javax.swing.JButton();
        maxDiffLabel = new javax.swing.JLabel();
        maxDiffTextField = new javax.swing.JTextField();
        autoFlushCheckBox = new javax.swing.JCheckBox();
        statusLabel = new javax.swing.JLabel();
        flushDelayLabel = new javax.swing.JLabel();
        flushDelayTextField = new javax.swing.JTextField();
        pauseToggleButton = new javax.swing.JToggleButton();
        autoPauseCheckBox = new javax.swing.JCheckBox();
        startTileRowLabel = new javax.swing.JLabel();
        startTileRowSpinner = new javax.swing.JSpinner();
        endTileRowLabel = new javax.swing.JLabel();
        endTileRowSpinner = new javax.swing.JSpinner();
        defaultsButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Map Maker");
        setMaximumSize(null);
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        startToggleButton.setMnemonic('S');
        startToggleButton.setText("Start");
        startToggleButton.setEnabled(false);
        startToggleButton.setFocusPainted(false);
        startToggleButton.setName(""); // NOI18N
        startToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startToggleButtonActionPerformed(evt);
            }
        });

        sprite0CheckBox.setText("Update on sprite 0 hit");
        sprite0CheckBox.setFocusPainted(false);
        sprite0CheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sprite0CheckBoxActionPerformed(evt);
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

        captureLabel.setText("Capture:");
        captureLabel.setMaximumSize(null);
        captureLabel.setMinimumSize(null);
        captureLabel.setPreferredSize(null);

        captureComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Visible Window", "Horizontal Band", "Vertical Band"}));
        captureComboBox.setFocusable(false);
        captureComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                captureComboBoxActionPerformed(evt);
            }
        });

        trackingSizeLabel.setText("Tracking size:");

        trackingSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"1", "2", "3", "4", "5"}));
        trackingSizeComboBox.setSelectedIndex(1);
        trackingSizeComboBox.setFocusable(false);
        trackingSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackingSizeComboBoxActionPerformed(evt);
            }
        });

        fileFormatLabel.setText("File format:");
        fileFormatLabel.setMaximumSize(null);
        fileFormatLabel.setMinimumSize(null);
        fileFormatLabel.setPreferredSize(null);

        fileFormatComboBox.setFocusable(false);
        fileFormatComboBox.setMaximumSize(null);
        fileFormatComboBox.setMinimumSize(null);
        fileFormatComboBox.setPreferredSize(null);

        outputDirLabel.setText("Output directory:");
        outputDirLabel.setMaximumSize(null);
        outputDirLabel.setMinimumSize(null);
        outputDirLabel.setPreferredSize(null);

        outputDirTextField.setMaximumSize(null);
        outputDirTextField.setMinimumSize(null);
        outputDirTextField.setPreferredSize(null);

        browseButton.setMnemonic('B');
        browseButton.setText("Browse...");
        browseButton.setFocusPainted(false);
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        filePrefixLabel.setText("File prefix:");

        filePrefixTextField.setMaximumSize(null);
        filePrefixTextField.setMinimumSize(null);
        filePrefixTextField.setPreferredSize(null);

        startIndexLabel.setText("Start index:");

        startIndexTextField.setColumns(4);
        startIndexTextField.setText("0");

        flushButton.setMnemonic('F');
        flushButton.setText("   Flush   ");
        flushButton.setEnabled(false);
        flushButton.setFocusPainted(false);
        flushButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flushButtonActionPerformed(evt);
            }
        });

        maxDiffLabel.setText("Max differences:");

        maxDiffTextField.setColumns(4);
        maxDiffTextField.setText("160");

        autoFlushCheckBox.setSelected(true);
        autoFlushCheckBox.setText("Auto flush");
        autoFlushCheckBox.setFocusPainted(false);
        autoFlushCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoFlushCheckBoxActionPerformed(evt);
            }
        });

        statusLabel.setText(" ");

        flushDelayLabel.setText("Post flush frames:");

        flushDelayTextField.setColumns(4);
        flushDelayTextField.setText("60");
        flushDelayTextField.setMaximumSize(null);
        flushDelayTextField.setMinimumSize(null);
        flushDelayTextField.setPreferredSize(null);

        pauseToggleButton.setText("Pause");
        pauseToggleButton.setEnabled(false);
        pauseToggleButton.setFocusPainted(false);
        pauseToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseToggleButtonActionPerformed(evt);
            }
        });

        autoPauseCheckBox.setSelected(true);
        autoPauseCheckBox.setText("Auto pause");
        autoPauseCheckBox.setFocusPainted(false);
        autoPauseCheckBox.setMaximumSize(null);
        autoPauseCheckBox.setMinimumSize(null);
        autoPauseCheckBox.setPreferredSize(null);
        autoPauseCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoPauseCheckBoxActionPerformed(evt);
            }
        });

        startTileRowLabel.setText("Start tile row:");
        startTileRowLabel.setMaximumSize(null);
        startTileRowLabel.setMinimumSize(null);
        startTileRowLabel.setPreferredSize(null);

        startTileRowSpinner.setMaximumSize(null);
        startTileRowSpinner.setMinimumSize(null);
        startTileRowSpinner.setPreferredSize(null);

        endTileRowLabel.setText("End tile row:");
        endTileRowLabel.setMaximumSize(null);
        endTileRowLabel.setMinimumSize(null);
        endTileRowLabel.setPreferredSize(null);

        endTileRowSpinner.setMaximumSize(null);
        endTileRowSpinner.setMinimumSize(null);
        endTileRowSpinner.setPreferredSize(null);

        defaultsButton.setText("Defaults");
        defaultsButton.setFocusPainted(false);
        defaultsButton.setMaximumSize(null);
        defaultsButton.setMinimumSize(null);
        defaultsButton.setPreferredSize(null);
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
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(statusLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(defaultsButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(pauseToggleButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(flushButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(startToggleButton))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(outputDirLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(outputDirTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(browseButton))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(filePrefixLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(filePrefixTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGap(18, 18, 18)
                                                .addComponent(startIndexLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(startIndexTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(fileFormatLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(fileFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(captureLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(captureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(startTileRowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(startTileRowSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(endTileRowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(endTileRowSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(trackingSizeLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(trackingSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(maxDiffLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(maxDiffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(sprite0CheckBox)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(scanlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(scanlineTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(autoPauseCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(autoFlushCheckBox)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(flushDelayLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(flushDelayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, defaultsButton, flushButton, pauseToggleButton, startToggleButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                                .addComponent(outputDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(browseButton))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(4, 4, 4)
                                                .addComponent(outputDirLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(filePrefixLabel)
                                        .addComponent(filePrefixTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(startIndexLabel)
                                        .addComponent(startIndexTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fileFormatLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fileFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(captureLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(captureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(startTileRowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(startTileRowSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(endTileRowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(endTileRowSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(trackingSizeLabel)
                                        .addComponent(trackingSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(maxDiffLabel)
                                        .addComponent(maxDiffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(sprite0CheckBox)
                                        .addComponent(scanlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scanlineTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(autoFlushCheckBox)
                                        .addComponent(flushDelayLabel)
                                        .addComponent(flushDelayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(autoPauseCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(statusLabel)
                                        .addComponent(flushButton)
                                        .addComponent(startToggleButton)
                                        .addComponent(pauseToggleButton)
                                        .addComponent(defaultsButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void startToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startToggleButtonActionPerformed
        if (startToggleButton.isSelected()) {
            statusLabel.setText(" ");
            saveFields();
            setRunning(true);
        } else {
            setRunning(false);
        }
    }//GEN-LAST:event_startToggleButtonActionPerformed

    private void sprite0CheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sprite0CheckBoxActionPerformed
        updateOnSprite0Hit = sprite0CheckBox.isSelected();
        setScanlineComponentsEnabled(true);
    }//GEN-LAST:event_sprite0CheckBoxActionPerformed

    private void captureComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_captureComboBoxActionPerformed
        final int index = captureComboBox.getSelectedIndex();
        if (index >= 0) {
            captureType = index;
        }
        setSpinnersEnabled(true);
    }//GEN-LAST:event_captureComboBoxActionPerformed

    private void flushButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flushButtonActionPerformed
        flush();
    }//GEN-LAST:event_flushButtonActionPerformed

    private void autoFlushCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoFlushCheckBoxActionPerformed
        setAutoFlush(autoFlushCheckBox.isSelected());
    }//GEN-LAST:event_autoFlushCheckBoxActionPerformed

    private void trackingSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackingSizeComboBoxActionPerformed
        final int index = trackingSizeComboBox.getSelectedIndex();
        if (index >= 0) {
            trackingSize = index + 1;
            origins = ORIGINS[index];
        }
    }//GEN-LAST:event_trackingSizeComboBoxActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String outDir = outputDirTextField.getText().trim();
        if (isBlank(outDir)) {
            outDir = outputDir;
        }
        File dir = findExistingParent(outDir);
        if (dir == null) {
            dir = new File(".");
        }

        final JFileChooser chooser = createFileChooser("Choose Output Directory",
                dir);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (showOpenDialog(this, chooser) == JFileChooser.APPROVE_OPTION) {
            final File file = chooser.getSelectedFile();
            if (file != null) {
                outputDir = file.getPath();
                outputDirTextField.setText(outputDir);
                updateStartIndex();
            }
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void pauseToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseToggleButtonActionPerformed
        setPaused(pauseToggleButton.isSelected());
    }//GEN-LAST:event_pauseToggleButtonActionPerformed

    private void autoPauseCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoPauseCheckBoxActionPerformed
        autoPause = autoPauseCheckBox.isSelected();
    }//GEN-LAST:event_autoPauseCheckBoxActionPerformed

    private void defaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultsButtonActionPerformed
        resetFields();
        resetGamePrefs();
    }//GEN-LAST:event_defaultsButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
