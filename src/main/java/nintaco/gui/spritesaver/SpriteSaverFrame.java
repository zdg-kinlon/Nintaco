package nintaco.gui.spritesaver;

import nintaco.App;
import nintaco.Machine;
import nintaco.PPU;
import nintaco.preferences.AppPrefs;
import nintaco.preferences.GamePrefs;
import nintaco.util.EDT;
import nintaco.util.GuiUtil;

import javax.swing.*;
import java.io.File;

import static nintaco.files.FileUtil.*;
import static nintaco.tv.TVSystem.PAL;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.StringUtil.isBlank;

public class SpriteSaverFrame extends javax.swing.JFrame {

    private final SpriteSearcher spriteSearcher = new SpriteSearcher();

    private int totalSpritesFound;
    private int fileIndex;
    private String fileFormat;
    private String outputDir;
    private String filePrefix;

    private int sprite0Hits;

    private volatile Machine machine;
    private volatile PPU ppu;
    private volatile boolean running;
    private volatile int updateScanline;
    private volatile int minOccurrences;
    private volatile int withinSeconds;
    private volatile int edgeMargin;
    private volatile int imageScale;
    private volatile boolean updateOnSprite0Hit;
    private volatile boolean saving;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JButton defaultsButton;
    private javax.swing.JLabel edgeMarginLabel;
    private javax.swing.JTextField edgeMarginTextField;
    private javax.swing.JComboBox fileFormatComboBox;
    private javax.swing.JLabel fileFormatLabel;
    private javax.swing.JLabel filePrefixLabel;
    private javax.swing.JTextField filePrefixTextField;
    private javax.swing.JLabel imageScaleLabel;
    private javax.swing.JTextField imageScaleTextField;
    private javax.swing.JLabel minOccurrencesLabel;
    private javax.swing.JTextField minOccurrencesTextField;
    private javax.swing.JLabel outputDirLabel;
    private javax.swing.JTextField outputDirTextField;
    private javax.swing.JLabel scanlineLabel;
    private javax.swing.JTextField scanlineTextField;
    private javax.swing.JCheckBox sprite0CheckBox;
    private javax.swing.JLabel startIndexLabel;
    private javax.swing.JTextField startIndexTextField;
    private javax.swing.JToggleButton startToggleButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel withinSecondsLabel;
    private javax.swing.JTextField withinSecondsTextField;

    public SpriteSaverFrame(final Machine machine) {
        initComponents();
        initFileFormatComboBox();
        initLoseFocusListeners();
        loadFields();
        setMachine(machine);
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
    }

    private void initFileFormatComboBox() {
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (String format : GuiUtil.getWritableImageFileFormats()) {
            model.addElement(format);
        }
        fileFormatComboBox.setModel(model);
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
        addLoseFocusListener(this, minOccurrencesTextField);
        addLoseFocusListener(this, scanlineTextField);
        addLoseFocusListener(this, withinSecondsTextField);
        addLoseFocusListener(this, edgeMarginTextField);
        addLoseFocusListener(this, imageScaleTextField);
    }

    public void destroy() {
        saveFields();
        dispose();
    }

    private void closeFrame() {
        App.destroySpriteSaverFrame();
    }

    private void loadFields() {
        loadFields(AppPrefs.getInstance().getSpriteSaverAppPrefs());
    }

    private void loadFields(final SpriteSaverAppPrefs prefs) {
        outputDir = AppPrefs.getInstance().getPaths().getSpritesDir();
        fileFormat = prefs.getFileFormat();
        imageScale = prefs.getImageScale();

        outputDirTextField.setText(outputDir);
        fileFormatComboBox.setSelectedItem(fileFormat);
        imageScaleTextField.setText(Integer.toString(imageScale));
        updateStartIndex();
    }

    private void saveFields() {
        if (running) {
            setRunning(false);
            flush();
        }
        EDT.sync(this::captureFields);
        saveGamePrefs();

        final SpriteSaverAppPrefs prefs = AppPrefs.getInstance()
                .getSpriteSaverAppPrefs();
        AppPrefs.getInstance().getPaths().setSpritesDir(outputDir);
        prefs.setFileFormat(fileFormat);
        prefs.setImageScale(imageScale);
        AppPrefs.save();
    }

    private void loadGamePrefs() {
        loadGamePrefs(GamePrefs.getInstance().getSpriteSaverGamePrefs());
    }

    private void loadGamePrefs(final SpriteSaverGamePrefs prefs) {

        filePrefix = isBlank(prefs.getFilePrefix()) ? getFileNameWithoutExtension(
                App.getEntryFileName()) : prefs.getFilePrefix();
        updateScanline = prefs.getUpdateScanline();
        updateOnSprite0Hit = prefs.isUpdateOnSprite0Hit();
        edgeMargin = prefs.getEdgeMargin();
        minOccurrences = prefs.getMinOccurrences();
        withinSeconds = prefs.getWithinSeconds();

        EDT.async(() -> {
            filePrefixTextField.setText(filePrefix);
            scanlineTextField.setText(Integer.toString(updateScanline));
            sprite0CheckBox.setSelected(updateOnSprite0Hit);
            edgeMarginTextField.setText(Integer.toString(edgeMargin));
            minOccurrencesTextField.setText(Integer.toString(minOccurrences));
            withinSecondsTextField.setText(Integer.toString(withinSeconds));
            updateScanlineComponents();
            updateStartIndex();
        });
    }

    private void saveGamePrefs() {
        final SpriteSaverGamePrefs prefs = GamePrefs.getInstance()
                .getSpriteSaverGamePrefs();

        prefs.setFilePrefix(filePrefix);
        prefs.setUpdateScanline(updateScanline);
        prefs.setUpdateOnSprite0Hit(updateOnSprite0Hit);
        prefs.setEdgeMargin(edgeMargin);
        prefs.setMinOccurrences(minOccurrences);
        prefs.setWithinSeconds(withinSeconds);

        GamePrefs.save();
    }

    private void enableComponents() {
        enableComponents(ppu, saving);
    }

    private void enableComponents(final PPU ppu, final boolean saving) {
        final boolean run = ppu != null && running;

        startToggleButton.setEnabled(ppu != null && !saving);
        startToggleButton.setSelected(run);
        startToggleButton.setText(run ? "Stop" : "Start");

        final boolean enabled = !(ppu == null || run);
        outputDirLabel.setEnabled(enabled);
        outputDirTextField.setEnabled(enabled);
        browseButton.setEnabled(enabled);
        filePrefixLabel.setEnabled(enabled);
        filePrefixTextField.setEnabled(enabled);
        startIndexLabel.setEnabled(enabled);
        startIndexTextField.setEnabled(enabled);
        fileFormatLabel.setEnabled(enabled);
        fileFormatComboBox.setEnabled(enabled);
        minOccurrencesLabel.setEnabled(enabled);
        minOccurrencesTextField.setEnabled(enabled);
        withinSecondsLabel.setEnabled(enabled);
        withinSecondsTextField.setEnabled(enabled);
        edgeMarginLabel.setEnabled(enabled);
        edgeMarginTextField.setEnabled(enabled);
        imageScaleLabel.setEnabled(enabled);
        imageScaleTextField.setEnabled(enabled);
        sprite0CheckBox.setEnabled(enabled);
        scanlineLabel.setEnabled(enabled);
        scanlineTextField.setEnabled(enabled);
        defaultsButton.setEnabled(enabled);

        statusLabel.setText(" ");
        if (ppu == null) {
            filePrefixTextField.setText("");
        }
    }

    private void setRunning(final boolean running) {
        EDT.async(() -> {
            this.running = running;
            enableComponents();
        });
    }

    private void updateScanlineComponents() {
        final boolean enabled = !sprite0CheckBox.isSelected();
        scanlineLabel.setEnabled(enabled);
        scanlineTextField.setEnabled(enabled);
    }

    public final void setMachine(final Machine machine) {
        if (running) {
            flush();
        }
        setRunning(false);
        this.machine = machine;
        if (machine == null) {
            ppu = null;
        } else {
            ppu = machine.getPPU();
            loadGamePrefs();
        }
        EDT.async(() -> {
            enableComponents();
            totalSpritesFound = 0;
        });
    }

    public void update(final int scanline) {

        if (!running) {
            return;
        }

        final PPU pp = ppu;
        if (pp == null) {
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

        final int spritesFound = spriteSearcher.search(pp);
        if (spritesFound > 0) {
            handleSpritesFound(spritesFound, false);
        }
    }

    private void handleSpritesFound(final int spritesFound, final boolean total) {
        EDT.async(() -> {
            if (total) {
                totalSpritesFound = spritesFound;
                statusLabel.setText(String.format("Saved %d sprite%s.",
                        totalSpritesFound, totalSpritesFound == 1 ? "" : "s"));
            } else {
                totalSpritesFound += spritesFound;
                statusLabel.setText(String.format("Sprites found: %d",
                        totalSpritesFound));
            }
        });
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
        updateOnSprite0Hit = sprite0CheckBox.isSelected();
        final Machine m = machine;
        updateScanline = parseTextField(scanlineTextField, 0, -1, (m == null
                ? PAL.getScanlineCount() : m.getMapper().getTVSystem()
                .getScanlineCount()) - 1);
        minOccurrences = parseTextField(minOccurrencesTextField, 3, 1, 999);
        withinSeconds = parseTextField(withinSecondsTextField, 10, 1, 300);
        edgeMargin = parseTextField(edgeMarginTextField, 16, 0, 119);
        imageScale = parseTextField(imageScaleTextField, 1, 1, 16);
    }

    private synchronized void flush() {
        final Machine m = machine;
        final PPU pp = ppu;
        final String outDir = outputDir;
        final String prefix = filePrefix;
        final String format = fileFormat;
        final int scale = imageScale;
        final int startIndex = fileIndex;
        if (m == null || pp == null) {
            return;
        }
        saving = true;
        EDT.async(() -> statusLabel.setText("Saving sprites..."));
        enableComponents(pp, saving);
        new Thread(() -> {
            handleSpritesFound(spriteSearcher.save(m, outDir, prefix, format, scale,
                    startIndex), true);
            saving = false;
            updateStartIndex();
            enableComponents(pp, saving);
        }).start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        outputDirLabel = new javax.swing.JLabel();
        outputDirTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        filePrefixLabel = new javax.swing.JLabel();
        filePrefixTextField = new javax.swing.JTextField();
        startIndexLabel = new javax.swing.JLabel();
        startIndexTextField = new javax.swing.JTextField();
        fileFormatLabel = new javax.swing.JLabel();
        fileFormatComboBox = new javax.swing.JComboBox();
        sprite0CheckBox = new javax.swing.JCheckBox();
        scanlineLabel = new javax.swing.JLabel();
        scanlineTextField = new javax.swing.JTextField();
        edgeMarginLabel = new javax.swing.JLabel();
        edgeMarginTextField = new javax.swing.JTextField();
        minOccurrencesLabel = new javax.swing.JLabel();
        minOccurrencesTextField = new javax.swing.JTextField();
        withinSecondsLabel = new javax.swing.JLabel();
        withinSecondsTextField = new javax.swing.JTextField();
        startToggleButton = new javax.swing.JToggleButton();
        imageScaleLabel = new javax.swing.JLabel();
        imageScaleTextField = new javax.swing.JTextField();
        statusLabel = new javax.swing.JLabel();
        defaultsButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Sprite Saver");
        setMaximumSize(null);
        setMinimumSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        outputDirLabel.setText("Output directory:");
        outputDirLabel.setMaximumSize(null);
        outputDirLabel.setMinimumSize(null);
        outputDirLabel.setPreferredSize(null);

        outputDirTextField.setMaximumSize(null);
        outputDirTextField.setMinimumSize(null);
        outputDirTextField.setPreferredSize(null);

        browseButton.setMnemonic('B');
        browseButton.setText("Browse");
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
        startIndexLabel.setMaximumSize(null);
        startIndexLabel.setMinimumSize(null);
        startIndexLabel.setPreferredSize(null);

        startIndexTextField.setColumns(5);
        startIndexTextField.setText("0");
        startIndexTextField.setMaximumSize(null);
        startIndexTextField.setMinimumSize(null);

        fileFormatLabel.setText("File format:");
        fileFormatLabel.setMaximumSize(null);
        fileFormatLabel.setMinimumSize(null);
        fileFormatLabel.setPreferredSize(null);

        fileFormatComboBox.setFocusable(false);
        fileFormatComboBox.setMaximumSize(null);
        fileFormatComboBox.setMinimumSize(null);
        fileFormatComboBox.setPreferredSize(null);

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

        edgeMarginLabel.setText("Edge margin:");
        edgeMarginLabel.setMaximumSize(null);
        edgeMarginLabel.setMinimumSize(null);
        edgeMarginLabel.setPreferredSize(null);

        edgeMarginTextField.setColumns(4);
        edgeMarginTextField.setText("16");
        edgeMarginTextField.setToolTipText("Sprites around the edges of the frame are ignored.");
        edgeMarginTextField.setMaximumSize(null);
        edgeMarginTextField.setMinimumSize(null);
        edgeMarginTextField.setPreferredSize(null);

        minOccurrencesLabel.setText("Min occurrences:");
        minOccurrencesLabel.setMaximumSize(null);
        minOccurrencesLabel.setMinimumSize(null);
        minOccurrencesLabel.setPreferredSize(null);

        minOccurrencesTextField.setColumns(4);
        minOccurrencesTextField.setText("3");
        minOccurrencesTextField.setToolTipText("The meta-sprite must appear at least this many times and within the specified number of seconds before it is saved.");
        minOccurrencesTextField.setMaximumSize(null);
        minOccurrencesTextField.setMinimumSize(null);
        minOccurrencesTextField.setPreferredSize(null);

        withinSecondsLabel.setText("Within seconds:");
        withinSecondsLabel.setMaximumSize(null);
        withinSecondsLabel.setMinimumSize(null);
        withinSecondsLabel.setPreferredSize(null);

        withinSecondsTextField.setColumns(4);
        withinSecondsTextField.setText("10");
        withinSecondsTextField.setToolTipText("Within this time window, the meta-sprite must appear for the specified minimum number of occurances before it is saved.");
        withinSecondsTextField.setMaximumSize(null);
        withinSecondsTextField.setMinimumSize(null);
        withinSecondsTextField.setPreferredSize(null);

        startToggleButton.setText("Start");
        startToggleButton.setEnabled(false);
        startToggleButton.setFocusPainted(false);
        startToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startToggleButtonActionPerformed(evt);
            }
        });

        imageScaleLabel.setText("Image scale:");

        imageScaleTextField.setColumns(4);
        imageScaleTextField.setText("1");

        statusLabel.setText(" ");

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
                                                .addComponent(outputDirLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(outputDirTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(browseButton))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(filePrefixLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(filePrefixTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGap(18, 18, 18)
                                                .addComponent(startIndexLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(startIndexTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(fileFormatLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(fileFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(statusLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(defaultsButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(startToggleButton))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(sprite0CheckBox)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(scanlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(scanlineTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(minOccurrencesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(minOccurrencesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(withinSecondsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(withinSecondsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(edgeMarginLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(edgeMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(imageScaleLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(imageScaleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, browseButton, startToggleButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(outputDirLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(outputDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(browseButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(filePrefixLabel)
                                        .addComponent(filePrefixTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(startIndexLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(startIndexTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fileFormatLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fileFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(edgeMarginLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(edgeMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(minOccurrencesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(minOccurrencesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(withinSecondsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(withinSecondsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(imageScaleLabel)
                                        .addComponent(imageScaleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(sprite0CheckBox)
                                        .addComponent(scanlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scanlineTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(startToggleButton)
                                        .addComponent(statusLabel)
                                        .addComponent(defaultsButton))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeFrame();
    }//GEN-LAST:event_formWindowClosing

    private void startToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startToggleButtonActionPerformed
        if (startToggleButton.isSelected()) {
            statusLabel.setText(" ");
            totalSpritesFound = 0;
            saveFields();
            spriteSearcher.setMinOccurrences(minOccurrences);
            spriteSearcher.setSweepSeconds(withinSeconds);
            spriteSearcher.setEdgeMargin(edgeMargin);
            setRunning(true);
        } else {
            flush();
            setRunning(false);
        }
    }//GEN-LAST:event_startToggleButtonActionPerformed

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

    private void sprite0CheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sprite0CheckBoxActionPerformed
        updateOnSprite0Hit = sprite0CheckBox.isSelected();
        updateScanlineComponents();
    }//GEN-LAST:event_sprite0CheckBoxActionPerformed

    private void defaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultsButtonActionPerformed
        loadFields(new SpriteSaverAppPrefs());
        loadGamePrefs(new SpriteSaverGamePrefs());
    }//GEN-LAST:event_defaultsButtonActionPerformed
    // End of variables declaration//GEN-END:variables
}
