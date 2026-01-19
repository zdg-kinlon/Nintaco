package nintaco.gui.exportmedia;

import nintaco.App;
import nintaco.MachineRunner;
import nintaco.apu.AudioProcessor;
import nintaco.apu.SystemAudioProcessor;
import nintaco.gui.FileExtensionFilter;
import nintaco.gui.InformationDialog;
import nintaco.gui.ProgressDialog;
import nintaco.gui.exportmedia.preferences.ExportMediaFilePrefs;
import nintaco.gui.exportmedia.preferences.FramesOption;
import nintaco.gui.exportmedia.preferences.MediaType;
import nintaco.gui.historyeditor.tasks.FramePlayedListener;
import nintaco.gui.historyeditor.tasks.PlayMovieTask;
import nintaco.gui.historyeditor.tasks.TaskTerminatedListener;
import nintaco.gui.image.filters.VideoFilterDescriptor;
import nintaco.gui.image.preferences.Paths;
import nintaco.movie.GifSequenceWriter;
import nintaco.movie.Movie;
import nintaco.movie.VideoFile;
import nintaco.palettes.PaletteNames;
import nintaco.preferences.AppPrefs;
import nintaco.tv.TVSystem;
import nintaco.util.EDT;
import nintaco.util.GuiUtil;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;

import static nintaco.files.FileUtil.*;
import static nintaco.gui.exportmedia.preferences.FramesOption.SaveAll;
import static nintaco.gui.exportmedia.preferences.MediaType.*;
import static nintaco.gui.exportmedia.preferences.VideoType.*;
import static nintaco.tv.TVSystem.PAL;
import static nintaco.util.GuiUtil.*;
import static nintaco.util.MathUtil.getDigits;
import static nintaco.util.StringUtil.isBlank;

public class ExportMediaFileDialog extends javax.swing.JDialog {

    private static final String ERROR_TITLE = "Export Media Error";

    private final String fileBase;
    private final Movie movie;
    private final int startFrameIndex;
    private final int endFrameIndex;
    private final BufferedImage image;
    private final int[] screen;

    private VideoFile videoFile;
    private ImageOutputStream imageOutputStream;
    private GifSequenceWriter gifSequenceWriter;
    private ImageConverter imageConverter;
    private TVSystem lastTVSystem;

    private volatile PlayMovieTask playMovieTask;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox cropBordersCheckBox;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JComboBox<FramesOption> framesComboBox;
    private javax.swing.JLabel framesLabel;
    private javax.swing.JComboBox<MediaType> mediaComboBox;
    private javax.swing.JLabel mediaLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox<String> paletteComboBox;
    private javax.swing.JLabel paletteLabel;
    private javax.swing.JCheckBox recordAudioCheckBox;
    private javax.swing.JComboBox<String> scaleComboBox;
    private javax.swing.JLabel scaleLabel;
    private javax.swing.JCheckBox smoothScalingCheckBox;
    private javax.swing.JCheckBox tvAspectCheckBox;
    private javax.swing.JComboBox<String> typeComboBox;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JComboBox<VideoFilterDescriptor> videoFiltersComboBox;

    public ExportMediaFileDialog(final Window parent, final Movie movie,
                                 final int startFrameIndex, final int endFrameIndex,
                                 final BufferedImage image, final int[] screen) {
        super(parent);
        final String base = getFileNameWithoutExtension(App.getEntryFileName());
        fileBase = isBlank(base) ? "media" : base;
        this.movie = movie;
        this.startFrameIndex = startFrameIndex;
        this.endFrameIndex = endFrameIndex;
        this.image = image;
        this.screen = screen;
        setModal(true);
        initComponents();
        initPaletteComboBox();
        addTextFieldEditListener(fileTextField, this::fileTextFieldEdited);
        updateTypeComboBox();
        loadFields();
        runNsfCheck();
        scaleFonts(this);
        pack();
        setLocationRelativeTo(parent);
    }

    private void runNsfCheck() {
        if (App.getNsfFile() != null) {
            mediaComboBox.setSelectedItem(MediaType.Audio);
            mediaComboBox.setEnabled(false);
            mediaLabel.setEnabled(false);
        }
    }

    private void initPaletteComboBox() {
        final java.util.List<String> names = new ArrayList<>();
        AppPrefs.getInstance().getPalettes().getPaletteNames(names);
        names.add(0, PaletteNames.CURRENT);
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (final String name : names) {
            model.addElement(name);
        }
        paletteComboBox.setModel(model);
    }

    private void loadFields() {
        fileTextField.setText(AppPrefs.getInstance().getPaths()
                .getMediaDir());

        final ExportMediaFilePrefs prefs = AppPrefs.getInstance()
                .getExportMediaFilePrefs();
        mediaComboBox.setSelectedItem(prefs.getMediaType());
        typeComboBox.setSelectedIndex(prefs.getFileType());
        recordAudioCheckBox.setSelected(prefs.isRecordAudio());
        cropBordersCheckBox.setSelected(prefs.isCropBorders());
        framesComboBox.setSelectedItem(prefs.getFramesOption());
        videoFiltersComboBox.setSelectedItem(prefs.getVideoFilter());
        paletteComboBox.setSelectedItem(prefs.getPalette());
        scaleComboBox.setSelectedIndex(prefs.getScale() - 1);
        smoothScalingCheckBox.setSelected(prefs.isSmoothScaling());
        tvAspectCheckBox.setSelected(prefs.isUseTvAspectRatio());
    }

    private ExportMediaFilePrefs saveFields() {
        final String dir = getDirectoryPath(fileTextField.getText().trim());
        if (dir != null) {
            final Paths paths = AppPrefs.getInstance().getPaths();
            paths.setMediaDir(dir);
            paths.addRecentDirectory(dir);
        }
        final ExportMediaFilePrefs prefs = AppPrefs.getInstance()
                .getExportMediaFilePrefs();
        prefs.setFileName(fileTextField.getText().trim());
        prefs.setMediaType((MediaType) mediaComboBox.getSelectedItem());
        prefs.setFileType(typeComboBox.getSelectedIndex());
        prefs.setRecordAudio(recordAudioCheckBox.isSelected());
        prefs.setCropBorders(cropBordersCheckBox.isSelected());
        prefs.setFramesOption((FramesOption) framesComboBox.getSelectedItem());
        prefs.setVideoFilter((VideoFilterDescriptor) videoFiltersComboBox
                .getSelectedItem());
        prefs.setPalette((String) paletteComboBox.getSelectedItem());
        prefs.setScale(scaleComboBox.getSelectedIndex() + 1);
        prefs.setSmoothScaling(smoothScalingCheckBox.isSelected());
        prefs.setUseTvAspectRatio(tvAspectCheckBox.isSelected());
        AppPrefs.save();
        return prefs;
    }

    private void fileTextFieldEdited() {
        okButton.setEnabled(!isBlank(fileTextField.getText()));
    }

    private String getExtension() {
        switch ((MediaType) mediaComboBox.getSelectedItem()) {
            case Video:
                return EXTENSIONS[typeComboBox.getSelectedIndex()];
            case Audio:
                return SystemAudioProcessor.getSupportedAudioFileTypes()
                        [typeComboBox.getSelectedIndex()].getExtension();
            default:
                return null;
        }
    }

    private void updateFileName() {
        String fileName = new File(fileTextField.getText().trim()).getPath();
        if (fileName.isEmpty()) {
            fileName = AppPrefs.getInstance().getPaths().getMediaDir();
        }

        boolean dir = false;
        String extension = null;
        switch ((MediaType) mediaComboBox.getSelectedItem()) {
            case Video:
                extension = EXTENSIONS[typeComboBox.getSelectedIndex()];
                break;
            case Audio:
                extension = SystemAudioProcessor.getSupportedAudioFileTypes()
                        [typeComboBox.getSelectedIndex()].getExtension();
                break;
            case Frames:
                dir = true;
                break;
        }

        final File file = new File(fileName);
        if (isDirectory(fileName)) {
            if (!dir) {
                if (fileBase.equals(file.getName())) {
                    fileName = file.getPath() + "." + extension;
                } else {
                    fileName = appendSeparator(fileName) + fileBase + "." + extension;
                }
            }
        } else if (dir) {
            final File parentFile = file.getParentFile();
            if (parentFile == null) {
                fileName = AppPrefs.getInstance().getPaths().getMediaDir();
                if (!fileBase.equals(new File(fileName).getName())) {
                    fileName = appendSeparator(fileName) + fileBase;
                }
            } else if (fileBase.equals(parentFile.getName())) {
                fileName = parentFile.getPath();
            } else {
                fileName = appendSeparator(parentFile.getPath()) + fileBase;
            }
        } else {
            fileName = removeExtension(fileName) + "." + extension;
        }

        fileTextField.setText(fileName);
    }

    private void closeDialog() {
        disposeImageConverter();
        dispose();
    }

    private FileExtensionFilter getFilterExtensionFilter() {
        final String extension = getExtension();
        if (extension == null) {
            return null;
        } else {
            return new FileExtensionFilter(0, String.format("%1$s files (*.%1$s)",
                    extension), extension);
        }
    }

    private void updateTypeComboBox() {
        int selectedIndex = 0;
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        switch ((MediaType) mediaComboBox.getSelectedItem()) {
            case Video:
                model.addElement("avi (TechSmith Screen Capture Codec)");
                model.addElement("mov (TechSmith Screen Capture Codec)");
                model.addElement("animated gif");
                break;
            case Audio:
                for (final AudioFileFormat.Type type
                        : SystemAudioProcessor.getSupportedAudioFileTypes()) {
                    final String extension = type.getExtension();
                    if ("wav".equalsIgnoreCase(extension)) {
                        selectedIndex = model.getSize();
                    }
                    model.addElement(extension);
                }
                break;
            case Frames:
                for (final String format : GuiUtil.getWritableImageFileFormats()) {
                    if ("png".equalsIgnoreCase(format)) {
                        selectedIndex = model.getSize();
                    }
                    model.addElement(format);
                }
                break;
        }
        typeComboBox.setModel(model);
        typeComboBox.setSelectedIndex(selectedIndex);
    }

    private void exportMedia(final ProgressDialog progressDialog) {
        final PlayMovieTask task = playMovieTask;
        if (task != null) {
            task.run();
        }
        progressDialog.dispose();
    }

    private void cancelExport() {
        final PlayMovieTask task = playMovieTask;
        if (task != null) {
            task.cancel();
        }
        disposeImageConverter();
    }

    private void writeImage(final String format, final String extension,
                            final int frameIndex, final MachineRunner machineRunner,
                            final ExportMediaFilePrefs prefs,
                            final ProgressDialog progressDialog) {
        try {
            progressDialog.setMessage("Frame: " + frameIndex);
            progressDialog.setValue(frameIndex);
            final TVSystem tvSystem = machineRunner.getMapper().getTVSystem();
            if (imageConverter == null || tvSystem != lastTVSystem) {
                lastTVSystem = tvSystem;
                disposeImageConverter();
                imageConverter = new ImageConverter(prefs, tvSystem, true);
                imageConverter.setImage(image);
            }
            final BufferedImage img = imageConverter.convert();
            if (img != null) {
                ImageIO.write(img, extension,
                        new File(String.format(format, frameIndex)));
            }
            if (frameIndex == endFrameIndex) {
                disposeImageConverter();
                progressDialog.dispose();
                EDT.async(this::displaySuccessMessage);
            }
        } catch (final Throwable t) {
            disposeImageConverter();
            progressDialog.dispose();
            final PlayMovieTask task = playMovieTask;
            if (task != null && !task.isCanceled()) {
                task.cancel();
                //t.printStackTrace();
                displayError(ERROR_TITLE, this, "Failed to write frame image.");
            }
        }
    }

    private void writeAnimatedGifFrame(final int frameIndex,
                                       final MachineRunner machineRunner, final ExportMediaFilePrefs prefs,
                                       final ProgressDialog progressDialog) {
        try {
            progressDialog.setMessage("Frame: " + frameIndex);
            progressDialog.setValue(frameIndex);
            final TVSystem tvSystem = machineRunner.getMapper().getTVSystem();
            if (imageConverter == null || tvSystem != lastTVSystem
                    || imageOutputStream == null || gifSequenceWriter == null) {
                lastTVSystem = tvSystem;
                disposeImageConverter();
                imageConverter = new ImageConverter(prefs, tvSystem, true);
                imageConverter.setImage(image);
                disposeGifSequenceWriter();
                imageOutputStream = new FileImageOutputStream(new File(
                        prefs.getFileName()));
                gifSequenceWriter = new GifSequenceWriter(imageOutputStream,
                        image.getType(), tvSystem == PAL ? 40 : 30, true);
            }
            if ((frameIndex & 1) == 0) {
                final BufferedImage img = imageConverter.convert();
                if (img != null) {
                    gifSequenceWriter.writeToSequence(img);
                }
            }
        } catch (final Throwable t) {
            closeAnimatedGif(progressDialog, t);
        }
    }

    private void disposeGifSequenceWriter() {
        try {
            gifSequenceWriter.close();
        } catch (final Throwable u) {
        }
        try {
            imageOutputStream.close();
        } catch (final Throwable u) {
        }
    }

    private void closeAnimatedGif(final ProgressDialog progressDialog,
                                  final Throwable t) {
        disposeGifSequenceWriter();
        progressDialog.dispose();
        final PlayMovieTask task = playMovieTask;
        if (task != null && !task.isCanceled()) {
            task.cancel();
            if (t == null) {
                EDT.async(this::displaySuccessMessage);
            } else {
                //t.printStackTrace();
                displayError(ERROR_TITLE, this, "Failed to write frame image.");
            }
        }
    }

    private void writeVideoFrame(final int frameIndex,
                                 final MachineRunner machineRunner, final ExportMediaFilePrefs prefs,
                                 final ProgressDialog progressDialog) {
        try {
            progressDialog.setMessage("Frame: " + frameIndex);
            progressDialog.setValue(frameIndex);
            final TVSystem tvSystem = machineRunner.getMapper().getTVSystem();
            if (imageConverter == null || tvSystem != lastTVSystem
                    || videoFile == null) {
                lastTVSystem = tvSystem;
                disposeImageConverter();
                imageConverter = new ImageConverter(prefs, tvSystem, true);
                imageConverter.setImage(image);
                disposeVideoFile();
                final double framesPerSecond = tvSystem.getFramesPerSecond();
                videoFile = new VideoFile(prefs.getFileName(),
                        prefs.getFileType() == AVI, prefs.getFramesOption() != SaveAll
                        ? framesPerSecond / 2.0 : framesPerSecond,
                        imageConverter.getWidth(), imageConverter.getHeight(),
                        prefs.isRecordAudio());
            }
            final BufferedImage img = imageConverter.convert();
            if (img != null) {
                videoFile.writeImage(img);
            }
        } catch (final Throwable t) {
            closeVideoFile(progressDialog, t);
        }
    }

    private void disposeVideoFile() {
        try {
            videoFile.close();
        } catch (final Throwable u) {
        }
    }

    private void closeVideoFile(final ProgressDialog progressDialog,
                                final Throwable t) {
        disposeVideoFile();
        progressDialog.dispose();
        final PlayMovieTask task = playMovieTask;
        if (task != null && !task.isCanceled()) {
            task.cancel();
            if (t == null) {
                EDT.async(this::displaySuccessMessage);
            } else {
                //t.printStackTrace();
                displayError(ERROR_TITLE, this, "Failed to write frame image.");
            }
        }
    }

    private void writeVideoAudioSample(final int sample) {
        if (videoFile != null) {
            try {
                videoFile.writeAudioSample(sample);
            } catch (final Throwable t) {
                disposeVideoFile();
                final PlayMovieTask task = playMovieTask;
                if (task != null && !task.isCanceled()) {
                    task.cancel();
                    //t.printStackTrace();
                    displayError(ERROR_TITLE, this, "Failed to write audio sample.");
                }
            }
        }
    }

    private void displaySuccessMessage() {
        new InformationDialog(this, "Export completed successfully.",
                "Media Export Finished", InformationDialog.IconType.INFORMATION)
                .setVisible(true);
        closeDialog();
    }

    private void disposeImageConverter() {
        if (imageConverter != null) {
            imageConverter.dispose();
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

        mediaLabel = new javax.swing.JLabel();
        mediaComboBox = new JComboBox(MediaType.values());
        fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox<>();
        recordAudioCheckBox = new javax.swing.JCheckBox();
        filterLabel = new javax.swing.JLabel();
        videoFiltersComboBox = new JComboBox(VideoFilterDescriptor.values());
        scaleLabel = new javax.swing.JLabel();
        scaleComboBox = new javax.swing.JComboBox<>();
        tvAspectCheckBox = new javax.swing.JCheckBox();
        cropBordersCheckBox = new javax.swing.JCheckBox();
        smoothScalingCheckBox = new javax.swing.JCheckBox();
        framesLabel = new javax.swing.JLabel();
        framesComboBox = new JComboBox(FramesOption.values());
        paletteLabel = new javax.swing.JLabel();
        paletteComboBox = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Export Media");
        setPreferredSize(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        mediaLabel.setText("Media:");

        mediaComboBox.setFocusable(false);
        mediaComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediaComboBoxActionPerformed(evt);
            }
        });

        fileLabel.setText("File:");

        fileTextField.setColumns(60);
        fileTextField.setText(" ");

        browseButton.setText("Browse...");
        browseButton.setFocusPainted(false);
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(" Cancel ");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        typeLabel.setText("Type:");

        typeComboBox.setFocusable(false);
        typeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboBoxActionPerformed(evt);
            }
        });

        recordAudioCheckBox.setSelected(true);
        recordAudioCheckBox.setText("Record audio");
        recordAudioCheckBox.setFocusPainted(false);

        filterLabel.setText("Filter:");

        videoFiltersComboBox.setFocusable(false);
        videoFiltersComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                videoFiltersComboBoxActionPerformed(evt);
            }
        });

        scaleLabel.setText("Scale:");

        scaleComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"1x", "2x", "3x", "4x", "5x"}));
        scaleComboBox.setFocusable(false);

        tvAspectCheckBox.setText("TV aspect");

        cropBordersCheckBox.setSelected(true);
        cropBordersCheckBox.setText("Crop borders");
        cropBordersCheckBox.setFocusPainted(false);

        smoothScalingCheckBox.setText("Smooth scaling");

        framesLabel.setText("Frames:");

        framesComboBox.setFocusable(false);

        paletteLabel.setText("Palette:");

        paletteComboBox.setFocusable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(fileLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(fileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(browseButton))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(okButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(mediaLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(mediaComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(typeLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(framesLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(framesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(filterLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(videoFiltersComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(paletteLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(paletteComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(scaleLabel)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(scaleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(cropBordersCheckBox)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(smoothScalingCheckBox)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(tvAspectCheckBox)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(recordAudioCheckBox)))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, cancelButton, okButton);

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(mediaLabel)
                                        .addComponent(mediaComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(typeLabel)
                                        .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(fileLabel)
                                        .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(browseButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(framesLabel)
                                        .addComponent(framesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(filterLabel)
                                        .addComponent(videoFiltersComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scaleLabel)
                                        .addComponent(scaleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(paletteLabel)
                                        .addComponent(paletteComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(recordAudioCheckBox)
                                        .addComponent(cropBordersCheckBox)
                                        .addComponent(smoothScalingCheckBox)
                                        .addComponent(tvAspectCheckBox))
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton)
                                        .addComponent(okButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        final ExportMediaFilePrefs prefs = saveFields();
        final ProgressDialog progressDialog = new ProgressDialog(this,
                this::cancelExport);

        if (isDirectory(prefs.getFileName())) {
            mkdir(prefs.getFileName());
        } else if (confirmOverwrite(this, prefs.getFileName())) {
            final String parent = new File(prefs.getFileName()).getParent();
            if (parent != null) {
                mkdir(parent);
            }
        } else {
            return;
        }

        disposeVideoFile();
        disposeGifSequenceWriter();
        disposeImageConverter();
        imageConverter = null;
        videoFile = null;
        imageOutputStream = null;
        gifSequenceWriter = null;
        lastTVSystem = null;

        FramePlayedListener taskListener = null;
        AudioProcessor audioProcessor = null;
        TaskTerminatedListener terminatedListener = null;

        progressDialog.setMinimum(startFrameIndex);
        progressDialog.setMaximum(endFrameIndex);
        progressDialog.setValue(startFrameIndex);
        switch (prefs.getMediaType()) {
            case Video:
                progressDialog.setTitle("Exporting Video...");
                if (prefs.getFileType() == ANIMATED_GIF) {
                    try {
                        taskListener = (t, f, m) -> writeAnimatedGifFrame(f, m, prefs,
                                progressDialog);
                        terminatedListener = t -> closeAnimatedGif(progressDialog, null);
                    } catch (final Throwable t) {
                        //t.printStackTrace();
                        displayError(ERROR_TITLE, this, "Failed to create animated GIF.");
                        return;
                    }
                } else {
                    try {
                        taskListener = (t, f, m) -> writeVideoFrame(f, m, prefs,
                                progressDialog);
                        terminatedListener = t -> closeVideoFile(progressDialog, null);
                        if (prefs.isRecordAudio()) {
                            audioProcessor = this::writeVideoAudioSample;
                        }
                    } catch (final Throwable t) {
                        //t.printStackTrace();
                        displayError(ERROR_TITLE, this, "Failed to create video file.");
                        return;
                    }
                }
                break;
            case Audio:
                progressDialog.setTitle("Exporting Audio...");
                try {
                    final PipedInputStream pipedIn = new PipedInputStream();
                    final PipedOutputStream pipedOut = new PipedOutputStream(pipedIn);
                    final AudioInputStream in = new AudioInputStream(pipedIn,
                            SystemAudioProcessor.AUDIO_FORMAT, AudioSystem.NOT_SPECIFIED);
                    final AudioFileFormat.Type fileType = SystemAudioProcessor
                            .getSupportedAudioFileTypes()[prefs.getFileType()];
                    final File file = new File(prefs.getFileName());
                    taskListener = (t, f, m) -> {
                        progressDialog.setMessage("Frame: " + f);
                        progressDialog.setValue(f);
                        if (f == endFrameIndex) {
                            progressDialog.dispose();
                            try {
                                in.close();
                            } catch (final Throwable u) {
                            }
                            try {
                                pipedIn.close();
                            } catch (final Throwable u) {
                            }
                            try {
                                pipedOut.close();
                            } catch (final Throwable u) {
                            }
                            EDT.async(this::displaySuccessMessage);
                        }
                    };
                    audioProcessor = s -> {
                        try {
                            pipedOut.write((byte) (s >> 8));
                            pipedOut.write((byte) s);
                        } catch (final Throwable t) {
                            //t.printStackTrace();
                            try {
                                in.close();
                            } catch (final Throwable u) {
                            }
                            try {
                                pipedIn.close();
                            } catch (final Throwable u) {
                            }
                            try {
                                pipedOut.close();
                            } catch (final Throwable u) {
                            }
                        }
                    };
                    new Thread(() -> {
                        try {
                            AudioSystem.write(in, fileType, file);
                        } catch (final Throwable t) {
                            //t.printStackTrace();
                            try {
                                in.close();
                            } catch (final Throwable u) {
                            }
                            try {
                                pipedIn.close();
                            } catch (final Throwable u) {
                            }
                            try {
                                pipedOut.close();
                            } catch (final Throwable u) {
                            }
                            progressDialog.dispose();
                            displayError(ERROR_TITLE, this, "Failed to create audio file.");
                        }
                    }).start();
                } catch (final Throwable t) {
                    //t.printStackTrace();
                    displayError(ERROR_TITLE, this, "Failed to create audio file.");
                    return;
                }
                break;
            case Frames: {
                progressDialog.setTitle("Exporting Frames...");
                final String extension = getWritableImageFileFormats()
                        [prefs.getFileType()];
                final String format = appendSeparator(prefs.getFileName()) + fileBase
                        + "-%0" + getDigits(endFrameIndex) + "d." + extension;
                taskListener = (t, f, m) -> writeImage(format, extension, f, m,
                        prefs, progressDialog);
                break;
            }
        }
        playMovieTask = new PlayMovieTask(movie, startFrameIndex, endFrameIndex,
                false, taskListener, () -> screen, null, audioProcessor,
                terminatedListener);
        new Thread(() -> exportMedia(progressDialog)).start();
        progressDialog.setVisible(true);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void mediaComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mediaComboBoxActionPerformed
        updateTypeComboBox();
        updateFileName();
    }//GEN-LAST:event_mediaComboBoxActionPerformed

    private void typeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboBoxActionPerformed
        updateFileName();
        recordAudioCheckBox.setEnabled(mediaComboBox.getSelectedItem() == Video
                && typeComboBox.getSelectedIndex() != ANIMATED_GIF);
        final boolean enabled = mediaComboBox.getSelectedItem() != Audio;
        cropBordersCheckBox.setEnabled(enabled);
        framesComboBox.setEnabled(enabled
                && typeComboBox.getSelectedIndex() != ANIMATED_GIF);
        framesLabel.setEnabled(framesComboBox.isEnabled());
        videoFiltersComboBox.setEnabled(enabled);
        filterLabel.setEnabled(videoFiltersComboBox.isEnabled());
        paletteComboBox.setEnabled(enabled);
        paletteLabel.setEnabled(paletteComboBox.isEnabled());
        scaleComboBox.setEnabled(enabled);
        scaleLabel.setEnabled(scaleComboBox.isEnabled());
        smoothScalingCheckBox.setEnabled(enabled);
        tvAspectCheckBox.setEnabled(enabled);
    }//GEN-LAST:event_typeComboBoxActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        updateFileName();
        final String title;
        final String path = fileTextField.getText().trim();
        String directory = null;
        String fileName = null;
        if (mediaComboBox.getSelectedItem() == Frames) {
            directory = path;
            title = "Select Output Directory";
        } else {
            final File file = new File(path);
            directory = file.getParent();
            fileName = file.getName();
            title = "Save Media File";
        }
        final File file = showSaveAsDialog(this, directory, fileName,
                getExtension(), getFilterExtensionFilter(), false, title);
        if (file != null) {
            fileTextField.setText(file.getPath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void videoFiltersComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_videoFiltersComboBoxActionPerformed
        VideoFilterDescriptor filter = (VideoFilterDescriptor) videoFiltersComboBox
                .getSelectedItem();
        if (filter == VideoFilterDescriptor.Current) {
            scaleLabel.setEnabled(false);
            scaleComboBox.setEnabled(false);
            filter = App.getImageFrame().getImagePane().getVideoFilterDescriptor();
            if (filter == null) {
                filter = VideoFilterDescriptor.NoFilter;
            }
        } else {
            scaleLabel.setEnabled(true);
            scaleComboBox.setEnabled(true);
        }
        scaleComboBox.setSelectedIndex(filter == VideoFilterDescriptor.NoFilter
                ? 0 : filter.getScale() - 1);
        smoothScalingCheckBox.setSelected(filter.isSmoothScaling());
        tvAspectCheckBox.setSelected(filter.isUseTvAspectRatio());
    }//GEN-LAST:event_videoFiltersComboBoxActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeDialog();
    }//GEN-LAST:event_formWindowClosing
    // End of variables declaration//GEN-END:variables
}
