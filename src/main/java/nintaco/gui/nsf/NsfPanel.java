package nintaco.gui.nsf;

import nintaco.App;
import nintaco.AppMode;
import nintaco.Machine;
import nintaco.apu.APU;
import nintaco.apu.SystemAudioProcessor;
import nintaco.files.NsfFile;
import nintaco.gui.sound.volumemixer.VolumeMixerFrame;
import nintaco.input.InputUtil;
import nintaco.input.other.RequestSong;
import nintaco.input.other.SetSongPaused;
import nintaco.mappers.Mapper;
import nintaco.mappers.nsf.NsfMapper;
import nintaco.preferences.AppPrefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

import static java.lang.Math.min;

public class NsfPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 0L;

    private static final String DEFAULT_SONG_LENGTH_STR
            = " / \u2013\u2013:\u2013\u2013";
    private String songLengthStr = DEFAULT_SONG_LENGTH_STR;
    private volatile NsfMapper mapper;
    private volatile NsfFile nsfFile;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel artistLabel;
    private javax.swing.JLabel artistTextLabel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JLabel copyrightLabel;
    private javax.swing.JLabel copyrightTextLabel;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JButton nextTrackButton;
    private javax.swing.JToggleButton playToggleButton;
    private javax.swing.JButton previousTrackButton;
    private javax.swing.JLabel soundChipsLabel;
    private javax.swing.JLabel soundChipsTextLabel;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel titleTextLabel;
    private javax.swing.JComboBox<String> trackComboBox;
    private final ActionListener trackListener = e -> handleTrackChange();
    private final Timer timer = new Timer(500, e -> updateClock());
    private javax.swing.JPanel trackPanel;
    private javax.swing.JLabel tracksCountLabel;
    private javax.swing.JSlider volumeSlider;

    public NsfPanel() {
        initComponents();
        trackComboBox.addActionListener(trackListener);
        setVolume(AppPrefs.getInstance().getVolumeMixerPrefs().getMasterVolume());
    }

    public void init(final Mapper mapper, final NsfFile nsfFile) {
        if (mapper != null && nsfFile != null && mapper.isNsfMapper()) {
            this.nsfFile = nsfFile;
            this.mapper = (NsfMapper) mapper;
            final String[] albumInfo = nsfFile.getAlbumInfo();
            titleLabel.setText(albumInfo[0]);
            artistLabel.setText(albumInfo[1]);
            copyrightLabel.setText(albumInfo[2]);
            setSoundChips(nsfFile);
            setTracks(nsfFile);
            timer.start();
        } else {
            close();
        }
    }

    public void setMachine(final Machine machine) {
        if (machine != null && machine.getMapper().isNsfMapper()) {
            this.mapper = (NsfMapper) machine.getMapper();
        }
    }

    public void appModeChanged(final AppMode appMode) {
        final boolean enabled = appMode == AppMode.Default;
        trackComboBox.setEnabled(enabled);
        previousTrackButton.setEnabled(enabled);
        playToggleButton.setEnabled(enabled);
        nextTrackButton.setEnabled(enabled);
    }

    private void close() {
        if (EventQueue.isDispatchThread()) {
            mapper = null;
            nsfFile = null;
            songLengthStr = DEFAULT_SONG_LENGTH_STR;
            timer.stop();
        } else {
            EventQueue.invokeLater(this::close);
        }
    }

    public void updateClock() {
        final NsfMapper m = mapper;
        if (m != null) {
            playToggleButton.setSelected(!m.isSongPaused());
            if (App.getImageFrame().isFocused()) {
                playToggleButton.requestFocus();
            }
            if (m.getRequestedSong() != trackComboBox.getSelectedIndex()) {
                fireTrackChanged(m.getRequestedSong());
            }
            if (!(m.isAudioActive() || App.getNetplayClient().isRunning())) {
                switchTracks(1);
            } else {
                timeLabel.setText(String.format("%s%s",
                        toMMSS((int) (m.getSongCpuCycles()
                                / m.getTVSystem().getCyclesPerSecond())), songLengthStr));
            }
        }
    }

    private long getAdjustedTrackMillis(final NsfFile nsfFile,
                                        final int trackIndex) {
        long fadeLength = nsfFile.getFadeMillis()[trackIndex];
        if (fadeLength < 0) {
            fadeLength = NsfMapper.DEFAULT_FADE_SECONDS * 1000L;
        }
        long trackLength = nsfFile.getTrackMillis()[trackIndex];
        return trackLength >= 0 ? (trackLength + fadeLength) : -1;
    }

    private void setTracks(final NsfFile nsfFile) {
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        final String[] labels = nsfFile.getTrackLabels();
        for (int i = 0; i < labels.length; i++) {
            final StringBuilder sb = new StringBuilder();
            sb.append(i + 1);
            if (!NsfFile.DEFAULT_TEXT.equals(labels[i])) {
                sb.append(" - ").append(labels[i]);
            }
            final long songMillis = getAdjustedTrackMillis(nsfFile, i);
            if (songMillis > 0) {
                sb.append(" (").append(toMMSS((int) (songMillis / 1000L))).append(")");
            }
            model.addElement(sb.toString());
        }
        trackComboBox.setModel(model);
        tracksCountLabel.setText(String.format("/ %d", labels.length));
        if (!App.getNetplayClient().isRunning()) {
            trackComboBox.setSelectedIndex(nsfFile.getStartingSong());
        }
    }

    private String toMMSS(final int seconds) {
        return String.format("%02d:%02d", min(99, seconds / 60), seconds % 60);
    }

    private void setSoundChips(final NsfFile nsfFile) {
        if (nsfFile.getChipCount() == 0) {
            soundChipsLabel.setText("<standard>");
        } else {
            final StringBuilder sb = new StringBuilder();
            appendSoundChip(sb, nsfFile.isUsesFdsAudio(), "FDS");
            appendSoundChip(sb, nsfFile.isUsesMMC5Audio(), "MMC5");
            appendSoundChip(sb, nsfFile.isUsesNamco163Audio(), "Namco 163");
            appendSoundChip(sb, nsfFile.isUsesSunsoft5BAudio(), "Sunsoft 5B");
            appendSoundChip(sb, nsfFile.isUsesVRC6Audio(), "VRC6");
            appendSoundChip(sb, nsfFile.isUsesVRC7Audio(), "VRC7");
            soundChipsLabel.setText(sb.toString());
        }
    }

    private void appendSoundChip(final StringBuilder sb, final boolean test,
                                 final String name) {
        if (test) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
        }
    }

    private void switchTracks(final int offset) {
        final int songIndex = trackComboBox.getSelectedIndex();
        if (songIndex >= 0) {
            trackComboBox.setSelectedIndex((songIndex + offset)
                    % trackComboBox.getItemCount());
        }
    }

    private void handleTrackChange() {
        final int songIndex = trackComboBox.getSelectedIndex();
        if (songIndex >= 0) {
            InputUtil.addOtherInput(new RequestSong(songIndex));
        }
    }

    public void fireTrackChanged(final int songIndex) {

        final NsfFile f = nsfFile;
        final NsfMapper m = mapper;
        if (m != null && f != null) {
            trackComboBox.removeActionListener(trackListener);
            trackComboBox.setSelectedIndex(songIndex);
            trackComboBox.addActionListener(trackListener);
            playToggleButton.setSelected(!m.isSongPaused());
            if (m.isSongPaused()) {
                InputUtil.addOtherInput(new SetSongPaused(false));
            }
            final long songMillis = getAdjustedTrackMillis(f, songIndex);
            if (songMillis > 0) {
                songLengthStr = " / " + toMMSS((int) (songMillis / 1000L));
            } else {
                songLengthStr = DEFAULT_SONG_LENGTH_STR;
            }
            updateClock();
        }
    }

    public void setVolume(final int volume) {
        if (volumeSlider.getValue() != volume) {
            volumeSlider.setValue(volume);
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

        timeLabel = new javax.swing.JLabel();
        infoPanel = new javax.swing.JPanel();
        soundChipsTextLabel = new javax.swing.JLabel();
        soundChipsLabel = new javax.swing.JLabel();
        artistLabel = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        copyrightLabel = new javax.swing.JLabel();
        titleTextLabel = new javax.swing.JLabel();
        copyrightTextLabel = new javax.swing.JLabel();
        artistTextLabel = new javax.swing.JLabel();
        trackPanel = new javax.swing.JPanel();
        tracksCountLabel = new javax.swing.JLabel();
        trackComboBox = new javax.swing.JComboBox<>();
        buttonsPanel = new javax.swing.JPanel();
        previousTrackButton = new javax.swing.JButton();
        nextTrackButton = new javax.swing.JButton();
        playToggleButton = new javax.swing.JToggleButton();
        volumeSlider = new javax.swing.JSlider();

        setMaximumSize(null);

        timeLabel.setText("00:00 / 00:00");

        soundChipsTextLabel.setText("Sound chips:");

        soundChipsLabel.setText("[sound chips]");

        artistLabel.setText("[artist]");

        titleLabel.setText("[title]");

        copyrightLabel.setText("[copyright]");

        titleTextLabel.setText("Title:");

        copyrightTextLabel.setText("Copyright:");

        artistTextLabel.setText("Artist:");

        javax.swing.GroupLayout infoPanelLayout = new javax.swing.GroupLayout(infoPanel);
        infoPanel.setLayout(infoPanelLayout);
        infoPanelLayout.setHorizontalGroup(
                infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(infoPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(soundChipsTextLabel)
                                        .addComponent(copyrightTextLabel)
                                        .addComponent(artistTextLabel)
                                        .addComponent(titleTextLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(titleLabel)
                                        .addComponent(artistLabel)
                                        .addComponent(copyrightLabel)
                                        .addComponent(soundChipsLabel))
                                .addGap(0, 0, 0))
        );

        infoPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, artistTextLabel, copyrightTextLabel, soundChipsTextLabel, titleTextLabel);

        infoPanelLayout.setVerticalGroup(
                infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(infoPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(titleTextLabel)
                                        .addComponent(titleLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(artistTextLabel)
                                        .addComponent(artistLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(copyrightTextLabel)
                                        .addComponent(copyrightLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(soundChipsTextLabel)
                                        .addComponent(soundChipsLabel))
                                .addGap(0, 0, 0))
        );

        tracksCountLabel.setText("/ X");

        trackComboBox.setFocusable(false);

        javax.swing.GroupLayout trackPanelLayout = new javax.swing.GroupLayout(trackPanel);
        trackPanel.setLayout(trackPanelLayout);
        trackPanelLayout.setHorizontalGroup(
                trackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(trackPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(trackComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tracksCountLabel)
                                .addContainerGap())
        );
        trackPanelLayout.setVerticalGroup(
                trackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(trackPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(trackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(trackComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tracksCountLabel))
                                .addGap(0, 0, 0))
        );

        previousTrackButton.setIcon(new SwitchTrackIcon(true));
        previousTrackButton.setText(" ");
        previousTrackButton.setFocusPainted(false);
        previousTrackButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        previousTrackButton.setIconTextGap(0);
        previousTrackButton.setMargin(new java.awt.Insets(4, 4, 4, 4));
        previousTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousTrackButtonActionPerformed(evt);
            }
        });

        nextTrackButton.setIcon(new SwitchTrackIcon(false));
        nextTrackButton.setText(" ");
        nextTrackButton.setFocusPainted(false);
        nextTrackButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextTrackButton.setIconTextGap(0);
        nextTrackButton.setMargin(new java.awt.Insets(4, 4, 4, 4));
        nextTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextTrackButtonActionPerformed(evt);
            }
        });

        playToggleButton.setIcon(new PlayIcon());
        playToggleButton.setText(" ");
        playToggleButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        playToggleButton.setFocusPainted(false);
        playToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        playToggleButton.setIconTextGap(0);
        playToggleButton.setMargin(new java.awt.Insets(6, 6, 6, 6));
        playToggleButton.setRolloverIcon(new PlayIcon());
        playToggleButton.setSelectedIcon(new PauseIcon());
        playToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playToggleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonsPanelLayout = new javax.swing.GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        buttonsPanelLayout.setHorizontalGroup(
                buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(buttonsPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(previousTrackButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(playToggleButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nextTrackButton)
                                .addGap(0, 0, 0))
        );
        buttonsPanelLayout.setVerticalGroup(
                buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(buttonsPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(playToggleButton)
                                        .addComponent(previousTrackButton)
                                        .addComponent(nextTrackButton))
                                .addGap(0, 0, 0))
        );

        volumeSlider.setPaintTicks(true);
        volumeSlider.setFocusable(false);
        volumeSlider.setPreferredSize(new Dimension(infoPanel.getPreferredSize().width, volumeSlider.getPreferredSize().height));
        volumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                volumeSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(infoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(buttonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(timeLabel)
                                        .addComponent(volumeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(trackPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(15, Short.MAX_VALUE)
                                .addComponent(infoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                                .addComponent(trackPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(buttonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(volumeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(15, 15, 15))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void playToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playToggleButtonActionPerformed
        InputUtil.addOtherInput(new SetSongPaused(!playToggleButton.isSelected()));
    }//GEN-LAST:event_playToggleButtonActionPerformed

    private void nextTrackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextTrackButtonActionPerformed
        switchTracks(1);
    }//GEN-LAST:event_nextTrackButtonActionPerformed

    private void previousTrackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousTrackButtonActionPerformed
        switchTracks(-1);
    }//GEN-LAST:event_previousTrackButtonActionPerformed

    private void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSliderStateChanged
        final VolumeMixerFrame frame = App.getVolumeMixerFrame();
        if (frame != null) {
            frame.setMasterVolume(volumeSlider.getValue());
        }
        AppPrefs.getInstance().getVolumeMixerPrefs()
                .setMasterVolume(volumeSlider.getValue());
        APU.setMasterVolume(volumeSlider.getValue());
        SystemAudioProcessor.setMasterVolume(volumeSlider.getValue());
    }//GEN-LAST:event_volumeSliderStateChanged
    // End of variables declaration//GEN-END:variables
}
