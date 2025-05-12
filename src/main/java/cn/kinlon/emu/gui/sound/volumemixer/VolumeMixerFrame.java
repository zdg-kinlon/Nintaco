package cn.kinlon.emu.gui.sound.volumemixer;

import cn.kinlon.emu.App;
import cn.kinlon.emu.apu.APU;
import cn.kinlon.emu.apu.SystemAudioProcessor;
import cn.kinlon.emu.mappers.konami.vrc6.VRC6Audio;
import cn.kinlon.emu.mappers.konami.vrc7.VRC7Audio;
import cn.kinlon.emu.mappers.namco.Namco163Audio;
import cn.kinlon.emu.mappers.nintendo.fds.FdsAudio;
import cn.kinlon.emu.mappers.nintendo.mmc5.MMC5Audio;
import cn.kinlon.emu.mappers.sunsoft.fme7.Sunsoft5BAudio;
import cn.kinlon.emu.preferences.AppPrefs;

import static cn.kinlon.emu.utils.GuiUtil.moveToImageFrameMonitor;
import static cn.kinlon.emu.utils.GuiUtil.scaleFonts;

public class VolumeMixerFrame extends javax.swing.JFrame {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel channelsPanel;
    private javax.swing.JLabel dmcLabel;
    private javax.swing.JPanel dmcPanel;
    private javax.swing.JSlider dmcSlider;
    private javax.swing.JLabel dmcVolumeLabel;
    private javax.swing.JLabel fdsLabel;
    private javax.swing.JPanel fdsPanel;
    private javax.swing.JSlider fdsSlider;
    private javax.swing.JLabel fdsVolumeLabel;
    private javax.swing.JLabel masterLabel;
    private javax.swing.JPanel masterPanel;
    private javax.swing.JSlider masterSlider;
    private javax.swing.JLabel masterVolumeLabel;
    private javax.swing.JLabel mmc5Label;
    private javax.swing.JPanel mmc5Panel;
    private javax.swing.JSlider mmc5Slider;
    private javax.swing.JLabel mmc5VolumeLabel;
    private javax.swing.JLabel n163Label;
    private javax.swing.JPanel n163Panel;
    private javax.swing.JSlider n163Slider;
    private javax.swing.JLabel n163VolumeLabel;
    private javax.swing.JLabel noiseLabel;
    private javax.swing.JPanel noisePanel;
    private javax.swing.JSlider noiseSlider;
    private javax.swing.JLabel noiseVolumeLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel s5bLabel;
    private javax.swing.JPanel s5bPanel;
    private javax.swing.JSlider s5bSlider;
    private javax.swing.JLabel s5bVolumeLabel;
    private javax.swing.JCheckBox smoothDmcCheckBox;
    private javax.swing.JCheckBox soundEnabledCheckBox;
    private javax.swing.JLabel square1Label;
    private javax.swing.JPanel square1Panel;
    private javax.swing.JSlider square1Slider;
    private javax.swing.JLabel square1VolumeLabel;
    private javax.swing.JLabel square2Label;
    private javax.swing.JPanel square2Panel;
    private javax.swing.JSlider square2Slider;
    private javax.swing.JLabel square2VolumeLabel;
    private javax.swing.JLabel triangleLabel;
    private javax.swing.JPanel trianglePanel;
    private javax.swing.JSlider triangleSlider;
    private javax.swing.JLabel triangleVolumeLabel;
    private javax.swing.JLabel vrc6Label;
    private javax.swing.JPanel vrc6Panel;
    private javax.swing.JSlider vrc6Slider;
    private javax.swing.JLabel vrc6VolumeLabel;
    private javax.swing.JLabel vrc7Label;
    private javax.swing.JPanel vrc7Panel;
    private javax.swing.JSlider vrc7Slider;
    private javax.swing.JLabel vrc7VolumeLabel;
    public VolumeMixerFrame() {
        initComponents();
        loadFields();
        scaleFonts(this);
        pack();
        moveToImageFrameMonitor(this);
    }

    private void loadFields() {
        setVolumeMixerPrefs(AppPrefs.getInstance().getVolumeMixerPrefs());
    }

    private void setVolumeMixerPrefs(final VolumeMixerPrefs prefs) {
        soundEnabledCheckBox.setSelected(prefs.isSoundEnabled());
        smoothDmcCheckBox.setSelected(prefs.isSmoothDMC());
        masterSlider.setValue(prefs.getMasterVolume());
        square1Slider.setValue(prefs.getSquare1Volume());
        square2Slider.setValue(prefs.getSquare2Volume());
        triangleSlider.setValue(prefs.getTriangleVolume());
        noiseSlider.setValue(prefs.getNoiseVolume());
        dmcSlider.setValue(prefs.getDmcVolume());
        fdsSlider.setValue(prefs.getFdsVolume());
        mmc5Slider.setValue(prefs.getMmc5Volume());
        vrc6Slider.setValue(prefs.getVrc6Volume());
        vrc7Slider.setValue(prefs.getVrc7Volume());
        n163Slider.setValue(prefs.getN163Volume());
        s5bSlider.setValue(prefs.getS5bVolume());
    }

    private void saveFields() {
        final VolumeMixerPrefs prefs = AppPrefs.getInstance().getVolumeMixerPrefs();
        prefs.setSoundEnabled(soundEnabledCheckBox.isSelected());
        prefs.setSmoothDMC(smoothDmcCheckBox.isSelected());
        prefs.setMasterVolume(masterSlider.getValue());
        prefs.setSquare1Volume(square1Slider.getValue());
        prefs.setSquare2Volume(square2Slider.getValue());
        prefs.setTriangleVolume(triangleSlider.getValue());
        prefs.setNoiseVolume(noiseSlider.getValue());
        prefs.setDmcVolume(dmcSlider.getValue());
        prefs.setFdsVolume(fdsSlider.getValue());
        prefs.setMmc5Volume(mmc5Slider.getValue());
        prefs.setVrc6Volume(vrc6Slider.getValue());
        prefs.setVrc7Volume(vrc7Slider.getValue());
        prefs.setN163Volume(n163Slider.getValue());
        prefs.setS5bVolume(s5bSlider.getValue());
        AppPrefs.save();
    }

    private void closeFrame() {
        App.destroyVolumeMixerFrame();
    }

    public void destroy() {
        dispose();
    }

    private void cancel() {
        final VolumeMixerPrefs prefs = AppPrefs.getInstance().getVolumeMixerPrefs();
        setMasterVolume(prefs.getMasterVolume());
        APU.setVolumeMixerPrefs(prefs);
        closeFrame();
    }

    public void setMasterVolume(final int volume) {
        if (masterSlider.getValue() != volume) {
            masterSlider.setValue(volume);
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

        channelsPanel = new javax.swing.JPanel();
        masterPanel = new javax.swing.JPanel();
        masterVolumeLabel = new javax.swing.JLabel();
        masterLabel = new javax.swing.JLabel();
        masterSlider = new javax.swing.JSlider();
        square1Panel = new javax.swing.JPanel();
        square1VolumeLabel = new javax.swing.JLabel();
        square1Label = new javax.swing.JLabel();
        square1Slider = new javax.swing.JSlider();
        square2Panel = new javax.swing.JPanel();
        square2VolumeLabel = new javax.swing.JLabel();
        square2Label = new javax.swing.JLabel();
        square2Slider = new javax.swing.JSlider();
        trianglePanel = new javax.swing.JPanel();
        triangleVolumeLabel = new javax.swing.JLabel();
        triangleLabel = new javax.swing.JLabel();
        triangleSlider = new javax.swing.JSlider();
        noisePanel = new javax.swing.JPanel();
        noiseVolumeLabel = new javax.swing.JLabel();
        noiseLabel = new javax.swing.JLabel();
        noiseSlider = new javax.swing.JSlider();
        dmcPanel = new javax.swing.JPanel();
        dmcVolumeLabel = new javax.swing.JLabel();
        dmcLabel = new javax.swing.JLabel();
        dmcSlider = new javax.swing.JSlider();
        fdsPanel = new javax.swing.JPanel();
        fdsVolumeLabel = new javax.swing.JLabel();
        fdsLabel = new javax.swing.JLabel();
        fdsSlider = new javax.swing.JSlider();
        mmc5Panel = new javax.swing.JPanel();
        mmc5VolumeLabel = new javax.swing.JLabel();
        mmc5Label = new javax.swing.JLabel();
        mmc5Slider = new javax.swing.JSlider();
        vrc6Panel = new javax.swing.JPanel();
        vrc6VolumeLabel = new javax.swing.JLabel();
        vrc6Label = new javax.swing.JLabel();
        vrc6Slider = new javax.swing.JSlider();
        vrc7Panel = new javax.swing.JPanel();
        vrc7VolumeLabel = new javax.swing.JLabel();
        vrc7Label = new javax.swing.JLabel();
        vrc7Slider = new javax.swing.JSlider();
        n163Panel = new javax.swing.JPanel();
        n163VolumeLabel = new javax.swing.JLabel();
        n163Label = new javax.swing.JLabel();
        n163Slider = new javax.swing.JSlider();
        s5bPanel = new javax.swing.JPanel();
        s5bVolumeLabel = new javax.swing.JLabel();
        s5bLabel = new javax.swing.JLabel();
        s5bSlider = new javax.swing.JSlider();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        soundEnabledCheckBox = new javax.swing.JCheckBox();
        smoothDmcCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Volume Mixer");
        setMaximumSize(null);
        setMinimumSize(null);
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        masterVolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        masterVolumeLabel.setText("100");

        masterLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        masterLabel.setText("Master");

        masterSlider.setMajorTickSpacing(10);
        masterSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        masterSlider.setPaintTicks(true);
        masterSlider.setValue(100);
        masterSlider.setFocusable(false);
        masterSlider.setPreferredSize(null);
        masterSlider.setRequestFocusEnabled(false);
        masterSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                masterSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout masterPanelLayout = new javax.swing.GroupLayout(masterPanel);
        masterPanel.setLayout(masterPanelLayout);
        masterPanelLayout.setHorizontalGroup(
                masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(masterPanelLayout.createSequentialGroup()
                                .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(masterVolumeLabel)
                                        .addComponent(masterSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(masterLabel))
                                .addGap(0, 0, 0))
        );
        masterPanelLayout.setVerticalGroup(
                masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(masterPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(masterLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(masterSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(masterVolumeLabel)
                                .addGap(0, 0, 0))
        );

        square1VolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        square1VolumeLabel.setText("100");

        square1Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        square1Label.setText("Square 1");

        square1Slider.setMajorTickSpacing(10);
        square1Slider.setOrientation(javax.swing.JSlider.VERTICAL);
        square1Slider.setPaintTicks(true);
        square1Slider.setValue(100);
        square1Slider.setFocusable(false);
        square1Slider.setPreferredSize(null);
        square1Slider.setRequestFocusEnabled(false);
        square1Slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                square1SliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout square1PanelLayout = new javax.swing.GroupLayout(square1Panel);
        square1Panel.setLayout(square1PanelLayout);
        square1PanelLayout.setHorizontalGroup(
                square1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(square1PanelLayout.createSequentialGroup()
                                .addGroup(square1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(square1Label)
                                        .addComponent(square1Slider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(square1VolumeLabel))
                                .addGap(0, 0, 0))
        );
        square1PanelLayout.setVerticalGroup(
                square1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(square1PanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(square1Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(square1Slider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(square1VolumeLabel)
                                .addGap(0, 0, 0))
        );

        square2VolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        square2VolumeLabel.setText("100");

        square2Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        square2Label.setText("Square 2");

        square2Slider.setMajorTickSpacing(10);
        square2Slider.setOrientation(javax.swing.JSlider.VERTICAL);
        square2Slider.setPaintTicks(true);
        square2Slider.setValue(100);
        square2Slider.setFocusable(false);
        square2Slider.setPreferredSize(null);
        square2Slider.setRequestFocusEnabled(false);
        square2Slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                square2SliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout square2PanelLayout = new javax.swing.GroupLayout(square2Panel);
        square2Panel.setLayout(square2PanelLayout);
        square2PanelLayout.setHorizontalGroup(
                square2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(square2PanelLayout.createSequentialGroup()
                                .addGroup(square2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(square2Label)
                                        .addComponent(square2Slider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(square2VolumeLabel))
                                .addGap(0, 0, 0))
        );
        square2PanelLayout.setVerticalGroup(
                square2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(square2PanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(square2Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(square2Slider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(square2VolumeLabel)
                                .addGap(0, 0, 0))
        );

        triangleVolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        triangleVolumeLabel.setText("100");

        triangleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        triangleLabel.setText("Triangle");

        triangleSlider.setMajorTickSpacing(10);
        triangleSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        triangleSlider.setPaintTicks(true);
        triangleSlider.setValue(100);
        triangleSlider.setFocusable(false);
        triangleSlider.setPreferredSize(null);
        triangleSlider.setRequestFocusEnabled(false);
        triangleSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                triangleSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout trianglePanelLayout = new javax.swing.GroupLayout(trianglePanel);
        trianglePanel.setLayout(trianglePanelLayout);
        trianglePanelLayout.setHorizontalGroup(
                trianglePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(trianglePanelLayout.createSequentialGroup()
                                .addGroup(trianglePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(triangleLabel)
                                        .addComponent(triangleSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(triangleVolumeLabel))
                                .addGap(0, 0, 0))
        );
        trianglePanelLayout.setVerticalGroup(
                trianglePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(trianglePanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(triangleLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(triangleSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(triangleVolumeLabel)
                                .addGap(0, 0, 0))
        );

        noiseVolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noiseVolumeLabel.setText("100");

        noiseLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noiseLabel.setText("Noise");

        noiseSlider.setMajorTickSpacing(10);
        noiseSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        noiseSlider.setPaintTicks(true);
        noiseSlider.setValue(100);
        noiseSlider.setFocusable(false);
        noiseSlider.setPreferredSize(null);
        noiseSlider.setRequestFocusEnabled(false);
        noiseSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                noiseSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout noisePanelLayout = new javax.swing.GroupLayout(noisePanel);
        noisePanel.setLayout(noisePanelLayout);
        noisePanelLayout.setHorizontalGroup(
                noisePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(noisePanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(noisePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(noiseLabel)
                                        .addComponent(noiseSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                                        .addComponent(noiseVolumeLabel))
                                .addGap(0, 0, 0))
        );
        noisePanelLayout.setVerticalGroup(
                noisePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(noisePanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(noiseLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(noiseSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(noiseVolumeLabel)
                                .addGap(0, 0, 0))
        );

        dmcVolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dmcVolumeLabel.setText("100");

        dmcLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dmcLabel.setText("DMC");

        dmcSlider.setMajorTickSpacing(10);
        dmcSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        dmcSlider.setPaintTicks(true);
        dmcSlider.setValue(100);
        dmcSlider.setFocusable(false);
        dmcSlider.setPreferredSize(null);
        dmcSlider.setRequestFocusEnabled(false);
        dmcSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                dmcSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout dmcPanelLayout = new javax.swing.GroupLayout(dmcPanel);
        dmcPanel.setLayout(dmcPanelLayout);
        dmcPanelLayout.setHorizontalGroup(
                dmcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(dmcPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(dmcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(dmcLabel)
                                        .addComponent(dmcSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                                        .addComponent(dmcVolumeLabel))
                                .addGap(0, 0, 0))
        );
        dmcPanelLayout.setVerticalGroup(
                dmcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(dmcPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(dmcLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dmcSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(dmcVolumeLabel)
                                .addGap(0, 0, 0))
        );

        fdsVolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fdsVolumeLabel.setText("100");

        fdsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fdsLabel.setText("FDS");

        fdsSlider.setMajorTickSpacing(10);
        fdsSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        fdsSlider.setPaintTicks(true);
        fdsSlider.setValue(100);
        fdsSlider.setFocusable(false);
        fdsSlider.setPreferredSize(null);
        fdsSlider.setRequestFocusEnabled(false);
        fdsSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fdsSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout fdsPanelLayout = new javax.swing.GroupLayout(fdsPanel);
        fdsPanel.setLayout(fdsPanelLayout);
        fdsPanelLayout.setHorizontalGroup(
                fdsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(fdsPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(fdsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(fdsLabel)
                                        .addComponent(fdsSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                                        .addComponent(fdsVolumeLabel))
                                .addGap(0, 0, 0))
        );
        fdsPanelLayout.setVerticalGroup(
                fdsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(fdsPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(fdsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fdsSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(fdsVolumeLabel)
                                .addGap(0, 0, 0))
        );

        mmc5VolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mmc5VolumeLabel.setText("100");

        mmc5Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mmc5Label.setText("MMC5");

        mmc5Slider.setMajorTickSpacing(10);
        mmc5Slider.setOrientation(javax.swing.JSlider.VERTICAL);
        mmc5Slider.setPaintTicks(true);
        mmc5Slider.setValue(100);
        mmc5Slider.setFocusable(false);
        mmc5Slider.setPreferredSize(null);
        mmc5Slider.setRequestFocusEnabled(false);
        mmc5Slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mmc5SliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout mmc5PanelLayout = new javax.swing.GroupLayout(mmc5Panel);
        mmc5Panel.setLayout(mmc5PanelLayout);
        mmc5PanelLayout.setHorizontalGroup(
                mmc5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mmc5PanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(mmc5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(mmc5Label)
                                        .addComponent(mmc5Slider, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                                        .addComponent(mmc5VolumeLabel))
                                .addGap(0, 0, 0))
        );
        mmc5PanelLayout.setVerticalGroup(
                mmc5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mmc5PanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(mmc5Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mmc5Slider, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(mmc5VolumeLabel)
                                .addGap(0, 0, 0))
        );

        vrc6VolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vrc6VolumeLabel.setText("100");

        vrc6Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vrc6Label.setText("VRC6");

        vrc6Slider.setMajorTickSpacing(10);
        vrc6Slider.setOrientation(javax.swing.JSlider.VERTICAL);
        vrc6Slider.setPaintTicks(true);
        vrc6Slider.setValue(100);
        vrc6Slider.setFocusable(false);
        vrc6Slider.setPreferredSize(null);
        vrc6Slider.setRequestFocusEnabled(false);
        vrc6Slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                vrc6SliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout vrc6PanelLayout = new javax.swing.GroupLayout(vrc6Panel);
        vrc6Panel.setLayout(vrc6PanelLayout);
        vrc6PanelLayout.setHorizontalGroup(
                vrc6PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(vrc6PanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(vrc6PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(vrc6Label)
                                        .addComponent(vrc6Slider, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                                        .addComponent(vrc6VolumeLabel))
                                .addGap(0, 0, 0))
        );
        vrc6PanelLayout.setVerticalGroup(
                vrc6PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(vrc6PanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(vrc6Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(vrc6Slider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(vrc6VolumeLabel)
                                .addGap(0, 0, 0))
        );

        vrc7VolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vrc7VolumeLabel.setText("100");

        vrc7Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vrc7Label.setText("VRC7");

        vrc7Slider.setMajorTickSpacing(10);
        vrc7Slider.setOrientation(javax.swing.JSlider.VERTICAL);
        vrc7Slider.setPaintTicks(true);
        vrc7Slider.setValue(100);
        vrc7Slider.setFocusable(false);
        vrc7Slider.setPreferredSize(null);
        vrc7Slider.setRequestFocusEnabled(false);
        vrc7Slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                vrc7SliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout vrc7PanelLayout = new javax.swing.GroupLayout(vrc7Panel);
        vrc7Panel.setLayout(vrc7PanelLayout);
        vrc7PanelLayout.setHorizontalGroup(
                vrc7PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(vrc7PanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(vrc7PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(vrc7Label)
                                        .addComponent(vrc7Slider, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                                        .addComponent(vrc7VolumeLabel))
                                .addGap(0, 0, 0))
        );
        vrc7PanelLayout.setVerticalGroup(
                vrc7PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(vrc7PanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(vrc7Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(vrc7Slider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(vrc7VolumeLabel)
                                .addGap(0, 0, 0))
        );

        n163VolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        n163VolumeLabel.setText("100");

        n163Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        n163Label.setText("N163");

        n163Slider.setMajorTickSpacing(10);
        n163Slider.setOrientation(javax.swing.JSlider.VERTICAL);
        n163Slider.setPaintTicks(true);
        n163Slider.setValue(100);
        n163Slider.setFocusable(false);
        n163Slider.setPreferredSize(null);
        n163Slider.setRequestFocusEnabled(false);
        n163Slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                n163SliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout n163PanelLayout = new javax.swing.GroupLayout(n163Panel);
        n163Panel.setLayout(n163PanelLayout);
        n163PanelLayout.setHorizontalGroup(
                n163PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(n163PanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(n163PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(n163Label)
                                        .addComponent(n163Slider, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                                        .addComponent(n163VolumeLabel))
                                .addGap(0, 0, 0))
        );
        n163PanelLayout.setVerticalGroup(
                n163PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(n163PanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(n163Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(n163Slider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(n163VolumeLabel)
                                .addGap(0, 0, 0))
        );

        s5bVolumeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        s5bVolumeLabel.setText("100");

        s5bLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        s5bLabel.setText("S5B");

        s5bSlider.setMajorTickSpacing(10);
        s5bSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        s5bSlider.setPaintTicks(true);
        s5bSlider.setValue(100);
        s5bSlider.setFocusable(false);
        s5bSlider.setPreferredSize(null);
        s5bSlider.setRequestFocusEnabled(false);
        s5bSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                s5bSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout s5bPanelLayout = new javax.swing.GroupLayout(s5bPanel);
        s5bPanel.setLayout(s5bPanelLayout);
        s5bPanelLayout.setHorizontalGroup(
                s5bPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(s5bPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(s5bPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(s5bLabel)
                                        .addComponent(s5bSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                                        .addComponent(s5bVolumeLabel))
                                .addGap(0, 0, 0))
        );
        s5bPanelLayout.setVerticalGroup(
                s5bPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(s5bPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(s5bLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(s5bSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(s5bVolumeLabel)
                                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout channelsPanelLayout = new javax.swing.GroupLayout(channelsPanel);
        channelsPanel.setLayout(channelsPanelLayout);
        channelsPanelLayout.setHorizontalGroup(
                channelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(channelsPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(channelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(channelsPanelLayout.createSequentialGroup()
                                                .addComponent(masterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(square1Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(square2Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(trianglePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(noisePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(dmcPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(channelsPanelLayout.createSequentialGroup()
                                                .addComponent(fdsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(mmc5Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(vrc6Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(vrc7Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(n163Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(s5bPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        channelsPanelLayout.setVerticalGroup(
                channelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(channelsPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(channelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(masterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(square1Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(square2Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(trianglePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(noisePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(dmcPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(channelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(mmc5Panel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(fdsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(n163Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(s5bPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(vrc6Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(vrc7Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 0, 0))
        );

        cancelButton.setMnemonic('C');
        cancelButton.setText(" Cancel ");
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setMnemonic('O');
        okButton.setText("OK");
        okButton.setFocusPainted(false);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        resetButton.setMnemonic('R');
        resetButton.setText("Reset");
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        soundEnabledCheckBox.setSelected(true);
        soundEnabledCheckBox.setText("Sound Enabled");
        soundEnabledCheckBox.setFocusPainted(false);
        soundEnabledCheckBox.setFocusable(false);
        soundEnabledCheckBox.setMaximumSize(null);
        soundEnabledCheckBox.setMinimumSize(null);
        soundEnabledCheckBox.setPreferredSize(null);
        soundEnabledCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                soundEnabledCheckBoxActionPerformed(evt);
            }
        });

        smoothDmcCheckBox.setSelected(true);
        smoothDmcCheckBox.setText("Smooth DMC");
        smoothDmcCheckBox.setFocusPainted(false);
        smoothDmcCheckBox.setFocusable(false);
        smoothDmcCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smoothDmcCheckBoxActionPerformed(evt);
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
                                                .addComponent(resetButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(okButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(soundEnabledCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(smoothDmcCheckBox)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(channelsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton, resetButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(soundEnabledCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(smoothDmcCheckBox))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(channelsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton)
                                        .addComponent(okButton)
                                        .addComponent(resetButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void masterSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_masterSliderStateChanged
        masterVolumeLabel.setText(Integer.toString(masterSlider.getValue()));
        APU.setMasterVolume(masterSlider.getValue());
        SystemAudioProcessor.setMasterVolume(masterSlider.getValue());
        App.getImageFrame().getNsfPanel().setVolume(masterSlider.getValue());
    }//GEN-LAST:event_masterSliderStateChanged

    private void square1SliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_square1SliderStateChanged
        square1VolumeLabel.setText(Integer.toString(square1Slider.getValue()));
        APU.setPulse1Volume(square1Slider.getValue());
    }//GEN-LAST:event_square1SliderStateChanged

    private void square2SliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_square2SliderStateChanged
        square2VolumeLabel.setText(Integer.toString(square2Slider.getValue()));
        APU.setPulse2Volume(square2Slider.getValue());
    }//GEN-LAST:event_square2SliderStateChanged

    private void triangleSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_triangleSliderStateChanged
        triangleVolumeLabel.setText(Integer.toString(triangleSlider.getValue()));
        APU.setTriangleVolume(triangleSlider.getValue());
    }//GEN-LAST:event_triangleSliderStateChanged

    private void noiseSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_noiseSliderStateChanged
        noiseVolumeLabel.setText(Integer.toString(noiseSlider.getValue()));
        APU.setNoiseVolume(noiseSlider.getValue());
    }//GEN-LAST:event_noiseSliderStateChanged

    private void dmcSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_dmcSliderStateChanged
        dmcVolumeLabel.setText(Integer.toString(dmcSlider.getValue()));
        APU.setDmcVolume(dmcSlider.getValue());
    }//GEN-LAST:event_dmcSliderStateChanged

    private void fdsSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fdsSliderStateChanged
        fdsVolumeLabel.setText(Integer.toString(fdsSlider.getValue()));
        FdsAudio.setVolume(fdsSlider.getValue());
    }//GEN-LAST:event_fdsSliderStateChanged

    private void mmc5SliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mmc5SliderStateChanged
        mmc5VolumeLabel.setText(Integer.toString(mmc5Slider.getValue()));
        MMC5Audio.setVolume(mmc5Slider.getValue());
    }//GEN-LAST:event_mmc5SliderStateChanged

    private void vrc6SliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_vrc6SliderStateChanged
        vrc6VolumeLabel.setText(Integer.toString(vrc6Slider.getValue()));
        VRC6Audio.setVolume(vrc6Slider.getValue());
    }//GEN-LAST:event_vrc6SliderStateChanged

    private void vrc7SliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_vrc7SliderStateChanged
        vrc7VolumeLabel.setText(Integer.toString(vrc7Slider.getValue()));
        VRC7Audio.setVolume(vrc7Slider.getValue());
    }//GEN-LAST:event_vrc7SliderStateChanged

    private void n163SliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_n163SliderStateChanged
        n163VolumeLabel.setText(Integer.toString(n163Slider.getValue()));
        Namco163Audio.setVolume(n163Slider.getValue());
    }//GEN-LAST:event_n163SliderStateChanged

    private void s5bSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_s5bSliderStateChanged
        s5bVolumeLabel.setText(Integer.toString(s5bSlider.getValue()));
        Sunsoft5BAudio.setVolume(s5bSlider.getValue());
    }//GEN-LAST:event_s5bSliderStateChanged

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        setVolumeMixerPrefs(new VolumeMixerPrefs());
    }//GEN-LAST:event_resetButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancel();
    }//GEN-LAST:event_formWindowClosing

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancel();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        saveFields();
        closeFrame();
    }//GEN-LAST:event_okButtonActionPerformed

    private void soundEnabledCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_soundEnabledCheckBoxActionPerformed
        APU.setSoundEnabled(soundEnabledCheckBox.isSelected());
    }//GEN-LAST:event_soundEnabledCheckBoxActionPerformed

    private void smoothDmcCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smoothDmcCheckBoxActionPerformed
        APU.setSmoothDMC(smoothDmcCheckBox.isSelected());
    }//GEN-LAST:event_smoothDmcCheckBoxActionPerformed
    // End of variables declaration//GEN-END:variables
}
