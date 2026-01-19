package nintaco.gui.watchhistory;

import nintaco.App;
import nintaco.MachineRunner;
import nintaco.apu.SystemAudioProcessor;
import nintaco.gui.exportmedia.ExportMediaFileDialog;
import nintaco.gui.historyeditor.tasks.PlayMovieTask;
import nintaco.gui.historyeditor.tasks.RenderScreenTask;
import nintaco.gui.image.SubMonitorFrame;
import nintaco.gui.nsf.PauseIcon;
import nintaco.gui.nsf.PlayIcon;
import nintaco.gui.nsf.SwitchTrackIcon;
import nintaco.mappers.nintendo.vs.MainCPU;
import nintaco.movie.Movie;
import nintaco.movie.MovieFrame;
import nintaco.task.Task;
import nintaco.task.TaskScheduler;
import nintaco.util.EDT;

import javax.swing.*;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import static nintaco.gui.image.ImagePane.IMAGE_HEIGHT;
import static nintaco.gui.image.ImagePane.IMAGE_WIDTH;
import static nintaco.util.GuiUtil.addLoseFocusListener;
import static nintaco.util.GuiUtil.addSpinnerEditListener;
import static nintaco.util.MathUtil.clamp;

public class WatchHistoryPanel extends javax.swing.JPanel {

    private final WatchHistoryFrame parent;

    private TaskScheduler taskScheduler;
    private TaskScheduler hoverScheduler;
    private PreviewPane previewPane;
    private PopupFactory popupFactory;

    private Task playMovieTask;
    private PlayMovieTask hoverMovieTask;
    private Movie movie;
    private Popup previewPopup;
    private int previewPopupX;
    private int previewPopupY;
    private int endPlayIndex;
    private boolean running;
    private boolean mouseEntered;
    private boolean playingMovie;
    private boolean wasPlayingWhenPressed;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner currentFrameSpinner;
    private javax.swing.JButton currentSeekButton;
    private javax.swing.JButton endCaptureButton;
    private javax.swing.JLabel endFrameLabel;
    private javax.swing.JSpinner endFrameSpinner;
    private javax.swing.JButton endSeekButton;
    private javax.swing.JButton exportButton;
    private javax.swing.JSlider historySlider;
    private javax.swing.JButton nextFrameButton;
    private javax.swing.JToggleButton playButton;
    private javax.swing.JButton previewButton;
    private javax.swing.JButton previousFrameButton;
    private javax.swing.JButton resumeButton;
    private javax.swing.JButton resumeHereButton;
    private javax.swing.JPanel savePanel;
    private javax.swing.JPanel sliderPanel;
    private javax.swing.JButton startCaptureButton;
    private javax.swing.JLabel startFrameLabel;
    private javax.swing.JSpinner startFrameSpinner;
    private javax.swing.JButton startSeekButton;

    public WatchHistoryPanel(final WatchHistoryFrame parent) {
        this.parent = parent;
        initComponents();
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(final Movie movie) {
        EDT.async(() -> {
            final MachineRunner machineRunner = App.getMachineRunner();
            Movie m = movie;
            if (machineRunner != null) {
                if (m == null) {
                    m = machineRunner.getMovie();
                }
                machineRunner.dispose();
                App.updateFrames(null);
                App.setMachineRunner(null);
            }
            if (m != null) {
                if (this.movie == m) {
                    return;
                }
                this.movie = m;
                endPlayIndex = m.frameCount - 1;
                historySlider.setMinimum(0);
                historySlider.setMaximum(endPlayIndex);
                historySlider.setValue(0);
                currentFrameSpinner.setModel(
                        new SpinnerNumberModel(0, 0, endPlayIndex, 1));
                currentFrameSpinner.setEditor(
                        new NumberEditor(currentFrameSpinner, "#"));
                startFrameSpinner.setModel(
                        new SpinnerNumberModel(0, 0, endPlayIndex, 1));
                startFrameSpinner.setEditor(
                        new NumberEditor(startFrameSpinner, "#"));
                endFrameSpinner.setModel(
                        new SpinnerNumberModel(endPlayIndex, 0, endPlayIndex, 1));
                endFrameSpinner.setEditor(
                        new NumberEditor(endFrameSpinner, "#"));
                ((JSpinner.DefaultEditor) currentFrameSpinner.getEditor()).getTextField()
                        .addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyPressed(KeyEvent e) {
                                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                                    requestFocusInWindow();
                                    EDT.async(() -> seek(currentFrameSpinner));
                                }
                            }
                        });
                addLoseFocusListener(parent, startFrameSpinner);
                addLoseFocusListener(parent, endFrameSpinner);
                addSpinnerEditListener(startFrameSpinner, this::updatePreviewAndSave);
                addSpinnerEditListener(endFrameSpinner, this::updatePreviewAndSave);
            }
        });
    }

    public void setShowSave(final boolean showSave) {
        EDT.async(() -> {
            parent.setTitle(showSave ? "Export Video/Audio" : "Watch History");
            savePanel.setVisible(showSave);
            if (showSave) {
                playingMovie = false;
                if (playMovieTask != null) {
                    playMovieTask.cancel();
                }
            } else {
                playMovie(false, false);
            }
            updatePlayButton();
        });
    }

    public void init() {
        EDT.async(() -> {
            setPreferredSize(null);
            running = true;
            if (taskScheduler == null) {
                taskScheduler = new TaskScheduler();
            }
            if (hoverScheduler == null) {
                hoverScheduler = new TaskScheduler();
            }
            if (previewPane == null) {
                previewPane = new PreviewPane();
            }
            popupFactory = PopupFactory.getSharedInstance();
            setComponentsEnabled(true);
        });
    }

    private void setComponentsEnabled(final boolean enabled) {
        historySlider.setEnabled(enabled);
        playButton.setEnabled(enabled);
        nextFrameButton.setEnabled(enabled);
        previousFrameButton.setEnabled(enabled);
        resumeHereButton.setEnabled(enabled);
        resumeButton.setEnabled(enabled);
        startFrameSpinner.setEnabled(enabled);
        endFrameSpinner.setEnabled(enabled);
        startCaptureButton.setEnabled(enabled);
        endCaptureButton.setEnabled(enabled);
        startSeekButton.setEnabled(enabled);
        endSeekButton.setEnabled(enabled);
        updatePreviewAndSave();
        resumeButton.requestFocusInWindow();
    }

    private void updatePreviewAndSave() {
        final Integer startFrameIndex = (Integer) startFrameSpinner.getValue();
        final Integer endFrameIndex = (Integer) endFrameSpinner.getValue();
        final boolean enabled = historySlider.isEnabled()
                && startFrameIndex != null && endFrameIndex != null
                && startFrameIndex <= endFrameIndex;
        previewButton.setEnabled(enabled);
        exportButton.setEnabled(enabled);
    }

    public void movieFramePlayed(final Task task, final int frameIndex,
                                 final MachineRunner machineRunner) {
        EDT.async(() -> {
            if (!task.isCanceled()) {
                historySlider.setValue(frameIndex);
                if (frameIndex >= endPlayIndex) {
                    playingMovie = false;
                    updatePlayButton();
                }
            }
        });
    }

    public void previewFramePlayed(final Task task, final int frameIndex,
                                   final MachineRunner machineRunner) {
        EDT.async(() -> {
            if (!task.isCanceled()) {
                hidePreviewPopup();
                if (mouseEntered && App.getImageFrame().isDisplayingImagePane()) {
                    previewPane.drawRectangle();
                    previewPopup = popupFactory.getPopup(this, previewPane, previewPopupX,
                            previewPopupY);
                    previewPopup.show();
                }  
            }
        });
    }

    private void mouseMoved(final MouseEvent e) {
        if (running) {
            mouseEntered = true;
            final SliderUI sliderUI = historySlider.getUI();
            if (sliderUI instanceof BasicSliderUI) {
                final Point p = new Point();
                SwingUtilities.convertPointToScreen(p, this);
                mouseHovered(((BasicSliderUI) sliderUI).valueForXPosition(e.getX()),
                        e.getXOnScreen(), p.y);
            }
        }
    }

    private void mouseHovered(final int frameIndex, final int screenX,
                              final int screenY) {
        if (running) {
            final PlayMovieTask hoverTask = hoverMovieTask;
            if (hoverTask != null) {
                hoverTask.cancel();
            }
            previewPopupX = screenX - (IMAGE_WIDTH >> 1);
            previewPopupY = screenY - IMAGE_HEIGHT;
            final int index = frameIndex & ~0x3F;
            hoverMovieTask = new PlayMovieTask(movie, index, index,
                    false, this::previewFramePlayed);
            hoverMovieTask.setFrameRenderer(previewPane);
            hoverMovieTask.setRenderingEnabled(true);
            hoverScheduler.add(hoverMovieTask);
        }
    }

    public void pause() {
        if (running) {
            hidePreviewPopup();
            cancelPlayTask();
        }
    }

    private void cancelPlayTask() {
        cancelPlayTask(true);
    }

    private void cancelPlayTask(final boolean updatePlayButton) {
        final Task playTask = playMovieTask;
        if (playTask != null) {
            playTask.cancel();
        }
        final TaskScheduler scheduler = taskScheduler;
        if (scheduler != null) {
            scheduler.waitForEmpty();
        }
        playingMovie = false;
        if (updatePlayButton) {
            updatePlayButton();
        }
        SystemAudioProcessor.flush();
    }

    private void playMovie(final boolean playButtonPressed,
                           final boolean singleFrame) {

        if (running) {
            endPlayIndex = (singleFrame || playingMovie)
                    ? historySlider.getValue() : historySlider.getMaximum();
            hidePreviewPopup();
            cancelPlayTask(false);
            EDT.async(() -> {
                if (playButtonPressed
                        && historySlider.getValue() == historySlider.getMaximum()) {
                    historySlider.setValue(0);
                    endPlayIndex = historySlider.getMaximum();
                }
                playMovieTask = new WatchMovieTask(movie, historySlider.getValue(),
                        endPlayIndex, this::movieFramePlayed);
                playingMovie = !singleFrame;
                updatePlayButton();
                taskScheduler.add(playMovieTask);
            });
        }
    }

    private void showFrame(final int offset) {
        if (running) {
            cancelPlayTask();
            EDT.async(() -> {
                endPlayIndex = clamp(historySlider.getValue() + offset, 0,
                        historySlider.getMaximum());
                playMovieTask = new RenderScreenTask(movie, endPlayIndex, (r, f) -> {
                    EDT.async(() -> historySlider.setValue(f.frameIndex));
                    RenderScreenTask.DEFAULT_RENDER_SCREEN_LISTENER
                            .completedRendering(r, f);
                });
                taskScheduler.add(playMovieTask);
            });
        }
    }

    private void hidePreviewPopup() {
        if (previewPopup != null) {
            previewPopup.hide();
        }
        App.getImageFrame().getImagePane().repaint();
        final SubMonitorFrame subMonitorFrame = App.getSubMonitorFrame();
        if (subMonitorFrame != null) {
            subMonitorFrame.getImagePane().repaint();
        }
    }

    private void resumeHere(final int frameIndex, final boolean clearHistory) {
        if (running) {
            cancelPlayTask();
            EDT.async(() -> {
                hidePreviewPopup();
                taskScheduler.cancelAll();
                hoverScheduler.cancelAll();
                setComponentsEnabled(false);
                running = false;
                endPlayIndex = frameIndex;
                final RenderScreenTask task = new RenderScreenTask(movie, frameIndex,
                        (r, f) -> resume(r, f, clearHistory));
                task.setDisposeEnabled(false);
                taskScheduler.add(task);
            });
        }
    }

    private void resume(final MachineRunner machineRunner,
                        final MovieFrame movieFrame, final boolean clearHistory) {
        taskScheduler.dispose();
        hoverScheduler.dispose();
        previewPane.dispose();
        taskScheduler = null;
        hoverScheduler = null;
        previewPane = null;
        popupFactory = null;
        final boolean vsDualSystem = movieFrame.isVsDualSystem();
        if (clearHistory) {
            movie = new Movie(vsDualSystem);
        } else {
            movie.frameIndex = movieFrame.frameIndex + 1;
            movie.truncate();
        }
        machineRunner.setMovie(movie);
        movie = null;
        machineRunner.getPPU().setScreenRenderer(App.getImageFrame()
                .getImagePane());
        if (vsDualSystem) {
            final SubMonitorFrame subMonitorFrame = App.getSubMonitorFrame();
            if (subMonitorFrame != null) {
                ((MainCPU) machineRunner.getCPU()).getSubPPU().setScreenRenderer(
                        subMonitorFrame.getImagePane());
            }
        }
        machineRunner.getAPU().setAudioProcessor(
                App.getSystemAudioProcessor());
        App.setMachineRunner(machineRunner);
        App.updateFrames(machineRunner);
        new Thread(machineRunner).start();
        App.destroyWatchHistoryFrame();
    }

    public void close() {
        cancelPlayTask();
        EDT.async(() -> {
            hidePreviewPopup();
            running = false;
            if (taskScheduler != null) {
                taskScheduler.dispose();
            }
            if (hoverScheduler != null) {
                hoverScheduler.dispose();
            }
            if (previewPane != null) {
                previewPane.dispose();
            }
            setComponentsEnabled(false);
            taskScheduler = null;
            hoverScheduler = null;
            previewPane = null;
            popupFactory = null;
            App.destroyWatchHistoryFrame();
        });
    }

    private void seek(final JSpinner spinner) {
        if (running) {
            final Integer value = (Integer) spinner.getValue();
            if (value != null) {
                historySlider.setValue(value);
                playMovie(false, true);
            }
        }
    }

    public void resume(final boolean clearHistory) {
        resumeHere(historySlider.getMaximum(), clearHistory);
    }

    private void updatePlayButton() {
        playButton.setSelected(playingMovie);
        currentSeekButton.setEnabled(!playButton.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        historySlider = new javax.swing.JSlider();
        nextFrameButton = new javax.swing.JButton();
        previousFrameButton = new javax.swing.JButton();
        playButton = new javax.swing.JToggleButton();
        resumeHereButton = new javax.swing.JButton();
        resumeButton = new javax.swing.JButton();
        savePanel = new javax.swing.JPanel();
        startFrameLabel = new javax.swing.JLabel();
        startFrameSpinner = new javax.swing.JSpinner();
        startCaptureButton = new javax.swing.JButton();
        endFrameLabel = new javax.swing.JLabel();
        endFrameSpinner = new javax.swing.JSpinner();
        endCaptureButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        startSeekButton = new javax.swing.JButton();
        endSeekButton = new javax.swing.JButton();
        previewButton = new javax.swing.JButton();
        sliderPanel = new javax.swing.JPanel();
        currentFrameSpinner = new javax.swing.JSpinner();
        currentSeekButton = new javax.swing.JButton();

        setMaximumSize(null);

        historySlider.setPaintTicks(true);
        historySlider.setFocusable(false);
        historySlider.setMaximumSize(null);
        historySlider.setMinimumSize(null);
        historySlider.setPreferredSize(null);
        historySlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                historySliderStateChanged(evt);
            }
        });
        historySlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                historySliderMouseDragged(evt);
            }

            public void mouseMoved(java.awt.event.MouseEvent evt) {
                historySliderMouseMoved(evt);
            }
        });
        historySlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                historySliderMouseEntered(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                historySliderMouseExited(evt);
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                historySliderMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                historySliderMouseReleased(evt);
            }
        });

        nextFrameButton.setIcon(new SwitchTrackIcon(false));
        nextFrameButton.setText(" ");
        nextFrameButton.setFocusPainted(false);
        nextFrameButton.setFocusable(false);
        nextFrameButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextFrameButton.setIconTextGap(0);
        nextFrameButton.setMargin(new java.awt.Insets(4, 4, 4, 4));
        nextFrameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextFrameButtonActionPerformed(evt);
            }
        });

        previousFrameButton.setIcon(new SwitchTrackIcon(true));
        previousFrameButton.setText(" ");
        previousFrameButton.setFocusPainted(false);
        previousFrameButton.setFocusable(false);
        previousFrameButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        previousFrameButton.setIconTextGap(0);
        previousFrameButton.setMargin(new java.awt.Insets(4, 4, 4, 4));
        previousFrameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousFrameButtonActionPerformed(evt);
            }
        });

        playButton.setIcon(new PlayIcon());
        playButton.setSelected(true);
        playButton.setText(" ");
        playButton.setFocusPainted(false);
        playButton.setFocusable(false);
        playButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        playButton.setIconTextGap(0);
        playButton.setMargin(new java.awt.Insets(6, 6, 6, 6));
        playButton.setRolloverIcon(new PlayIcon());
        playButton.setSelectedIcon(new PauseIcon());
        playButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playButtonActionPerformed(evt);
            }
        });

        resumeHereButton.setText("Resume Here");
        resumeHereButton.setFocusPainted(false);
        resumeHereButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resumeHereButtonActionPerformed(evt);
            }
        });

        resumeButton.setText("Resume");
        resumeButton.setFocusPainted(false);
        resumeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resumeButtonActionPerformed(evt);
            }
        });

        startFrameLabel.setText("Start Frame:");

        startFrameSpinner.setPreferredSize(null);
        startFrameSpinner.setRequestFocusEnabled(false);

        startCaptureButton.setText("Capture");
        startCaptureButton.setFocusPainted(false);
        startCaptureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startCaptureButtonActionPerformed(evt);
            }
        });

        endFrameLabel.setText("End Frame:");

        endFrameSpinner.setPreferredSize(null);
        endFrameSpinner.setRequestFocusEnabled(false);

        endCaptureButton.setText("Capture");
        endCaptureButton.setFocusPainted(false);
        endCaptureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endCaptureButtonActionPerformed(evt);
            }
        });

        exportButton.setText("Export...");
        exportButton.setFocusPainted(false);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        startSeekButton.setText("Seek");
        startSeekButton.setFocusPainted(false);
        startSeekButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startSeekButtonActionPerformed(evt);
            }
        });

        endSeekButton.setText("Seek");
        endSeekButton.setFocusPainted(false);
        endSeekButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endSeekButtonActionPerformed(evt);
            }
        });

        previewButton.setText("Preview");
        previewButton.setFocusPainted(false);
        previewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout savePanelLayout = new javax.swing.GroupLayout(savePanel);
        savePanel.setLayout(savePanelLayout);
        savePanelLayout.setHorizontalGroup(
                savePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(savePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(savePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(startFrameLabel)
                                        .addComponent(endFrameLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(savePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(startFrameSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(endFrameSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(savePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(savePanelLayout.createSequentialGroup()
                                                .addComponent(startCaptureButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(startSeekButton)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(savePanelLayout.createSequentialGroup()
                                                .addComponent(endCaptureButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(endSeekButton)
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(previewButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(exportButton)))
                                .addContainerGap())
        );

        savePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, exportButton, previewButton);

        savePanelLayout.setVerticalGroup(
                savePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(savePanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(savePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(startFrameLabel)
                                        .addComponent(startFrameSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(startCaptureButton)
                                        .addComponent(startSeekButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(savePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(endFrameLabel)
                                        .addComponent(endFrameSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(endCaptureButton)
                                        .addComponent(exportButton)
                                        .addComponent(endSeekButton)
                                        .addComponent(previewButton))
                                .addGap(0, 0, 0))
        );

        sliderPanel.setPreferredSize(new java.awt.Dimension(0, 5));

        javax.swing.GroupLayout sliderPanelLayout = new javax.swing.GroupLayout(sliderPanel);
        sliderPanel.setLayout(sliderPanelLayout);
        sliderPanelLayout.setHorizontalGroup(
                sliderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        sliderPanelLayout.setVerticalGroup(
                sliderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 5, Short.MAX_VALUE)
        );

        currentFrameSpinner.setPreferredSize(null);
        currentFrameSpinner.setRequestFocusEnabled(false);

        currentSeekButton.setText("Seek");
        currentSeekButton.setFocusPainted(false);
        currentSeekButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentSeekButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(historySlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(previousFrameButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(playButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(nextFrameButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(currentFrameSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(currentSeekButton)
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(resumeHereButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(resumeButton)))
                                .addContainerGap())
                        .addComponent(savePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(sliderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(historySlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(nextFrameButton)
                                        .addComponent(previousFrameButton)
                                        .addComponent(playButton)
                                        .addComponent(resumeHereButton)
                                        .addComponent(resumeButton)
                                        .addComponent(currentFrameSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(currentSeekButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sliderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(savePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void historySliderMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_historySliderMouseMoved
        mouseMoved(evt);
    }//GEN-LAST:event_historySliderMouseMoved

    private void historySliderMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_historySliderMouseExited
        mouseEntered = false;
        final PlayMovieTask hoverTask = hoverMovieTask;
        if (hoverTask != null) {
            hoverTask.cancel();
        }
        hidePreviewPopup();
    }//GEN-LAST:event_historySliderMouseExited

    private void historySliderMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_historySliderMousePressed
        if (running) {
            wasPlayingWhenPressed = playingMovie;
            mouseEntered = true;
            cancelPlayTask();
            EDT.async(() -> {
                final SliderUI sliderUI = historySlider.getUI();
                if (sliderUI instanceof BasicSliderUI) {
                    historySlider.setValue(((BasicSliderUI) sliderUI).valueForXPosition(
                            evt.getX()));
                }
            });
        }
    }//GEN-LAST:event_historySliderMousePressed

    private void playButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playButtonActionPerformed
        if (playingMovie) {
            cancelPlayTask();
        } else {
            playMovie(true, false);
        }
        currentSeekButton.setEnabled(!playButton.isSelected());
    }//GEN-LAST:event_playButtonActionPerformed

    private void historySliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_historySliderMouseReleased
        playMovie(false, !wasPlayingWhenPressed);
    }//GEN-LAST:event_historySliderMouseReleased

    private void nextFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextFrameButtonActionPerformed
        showFrame(1);
    }//GEN-LAST:event_nextFrameButtonActionPerformed

    private void previousFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousFrameButtonActionPerformed
        showFrame(-1);
    }//GEN-LAST:event_previousFrameButtonActionPerformed

    private void historySliderMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_historySliderMouseDragged
        mouseMoved(evt);
    }//GEN-LAST:event_historySliderMouseDragged

    private void resumeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resumeButtonActionPerformed
        resume(false);
    }//GEN-LAST:event_resumeButtonActionPerformed

    private void resumeHereButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resumeHereButtonActionPerformed
        resumeHere(historySlider.getValue(), false);
    }//GEN-LAST:event_resumeHereButtonActionPerformed

    private void startCaptureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startCaptureButtonActionPerformed
        startFrameSpinner.setValue(historySlider.getValue());
    }//GEN-LAST:event_startCaptureButtonActionPerformed

    private void endCaptureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endCaptureButtonActionPerformed
        endFrameSpinner.setValue(historySlider.getValue());
    }//GEN-LAST:event_endCaptureButtonActionPerformed

    private void startSeekButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startSeekButtonActionPerformed
        seek(startFrameSpinner);
    }//GEN-LAST:event_startSeekButtonActionPerformed

    private void endSeekButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endSeekButtonActionPerformed
        seek(endFrameSpinner);
    }//GEN-LAST:event_endSeekButtonActionPerformed

    private void previewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewButtonActionPerformed
        if (running) {
            final Integer startFrameIndex = (Integer) startFrameSpinner.getValue();
            final Integer endFrameIndex = (Integer) endFrameSpinner.getValue();
            if (startFrameIndex != null && endFrameIndex != null
                    && startFrameIndex <= endFrameIndex) {
                hidePreviewPopup();
                cancelPlayTask();
                EDT.async(() -> {
                    playingMovie = true;
                    endPlayIndex = endFrameIndex;
                    playMovieTask = new WatchMovieTask(movie, startFrameIndex,
                            endFrameIndex, (t, i, r) -> movieFramePlayed(t, i, r));
                    taskScheduler.add(playMovieTask);
                    updatePlayButton();
                });
            }
        }
    }//GEN-LAST:event_previewButtonActionPerformed

    private void historySliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_historySliderStateChanged
        currentFrameSpinner.setValue(historySlider.getValue());
    }//GEN-LAST:event_historySliderStateChanged

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        if (running) {
            final Integer startFrameIndex = (Integer) startFrameSpinner.getValue();
            final Integer endFrameIndex = (Integer) endFrameSpinner.getValue();
            if (startFrameIndex != null && endFrameIndex != null
                    && startFrameIndex <= endFrameIndex) {
                hidePreviewPopup();
                cancelPlayTask();
                EDT.async(() -> {
                    new ExportMediaFileDialog(App.getImageFrame(), movie, startFrameIndex,
                            endFrameIndex, previewPane.getImage(), previewPane.getScreen())
                            .setVisible(true);
                });
            }
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    private void currentSeekButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentSeekButtonActionPerformed
        seek(currentFrameSpinner);
    }//GEN-LAST:event_currentSeekButtonActionPerformed

    private void historySliderMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_historySliderMouseEntered
        mouseEntered = true;
    }//GEN-LAST:event_historySliderMouseEntered
    // End of variables declaration//GEN-END:variables
}
