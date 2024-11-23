package nintaco.gui.exportmedia.preferences;

import nintaco.gui.image.filters.VideoFilterDescriptor;
import nintaco.palettes.PaletteNames;
import nintaco.preferences.AppPrefs;

import java.io.Serializable;
import java.util.Objects;

public class ExportMediaFilePrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private MediaType mediaType;
    private Integer fileType;
    private Integer scale;
    private String fileName;
    private VideoFilterDescriptor videoFilter;
    private FramesOption framesOption;
    private Boolean recordAudio;
    private Boolean cropBorders;
    private Boolean smoothScaling;
    private Boolean useTvAspectRatio;
    private String suffix;
    private String palette;

    public ExportMediaFilePrefs() {
    }

    public ExportMediaFilePrefs(final ExportMediaFilePrefs prefs) {
        synchronized (AppPrefs.class) {
            mediaType = prefs.getMediaType();
            fileType = prefs.getFileType();
            scale = prefs.getScale();
            fileName = prefs.getFileName();
            videoFilter = prefs.getVideoFilter();
            framesOption = prefs.getFramesOption();
            recordAudio = prefs.isRecordAudio();
            cropBorders = prefs.isCropBorders();
            smoothScaling = prefs.isSmoothScaling();
            useTvAspectRatio = prefs.isUseTvAspectRatio();
            suffix = prefs.getSuffix();
        }
    }

    @Override
    public boolean equals(final Object obj) {
        synchronized (AppPrefs.class) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ExportMediaFilePrefs other = (ExportMediaFilePrefs) obj;
            if (!Objects.equals(this.fileName, other.fileName)) {
                return false;
            }
            if (!Objects.equals(this.suffix, other.suffix)) {
                return false;
            }
            if (this.mediaType != other.mediaType) {
                return false;
            }
            if (!Objects.equals(this.fileType, other.fileType)) {
                return false;
            }
            if (!Objects.equals(this.scale, other.scale)) {
                return false;
            }
            if (this.videoFilter != other.videoFilter) {
                return false;
            }
            if (!Objects.equals(this.palette, other.palette)) {
                return false;
            }
            if (this.framesOption != other.framesOption) {
                return false;
            }
            if (!Objects.equals(this.recordAudio, other.recordAudio)) {
                return false;
            }
            if (!Objects.equals(this.cropBorders, other.cropBorders)) {
                return false;
            }
            if (!Objects.equals(this.smoothScaling, other.smoothScaling)) {
                return false;
            }
            return Objects.equals(this.useTvAspectRatio, other.useTvAspectRatio);
        }
    }

    public MediaType getMediaType() {
        synchronized (AppPrefs.class) {
            if (mediaType == null) {
                mediaType = MediaType.Video;
            }
            return mediaType;
        }
    }

    public void setMediaType(final MediaType mediaType) {
        synchronized (AppPrefs.class) {
            this.mediaType = mediaType;
        }
    }

    public int getFileType() {
        synchronized (AppPrefs.class) {
            if (fileType == null) {
                fileType = VideoType.AVI;
            }
            return fileType;
        }
    }

    public void setFileType(final int fileType) {
        synchronized (AppPrefs.class) {
            this.fileType = fileType;
        }
    }

    public String getFileName() {
        synchronized (AppPrefs.class) {
            return fileName;
        }
    }

    public void setFileName(final String fileName) {
        synchronized (AppPrefs.class) {
            this.fileName = fileName;
        }
    }

    public boolean isRecordAudio() {
        synchronized (AppPrefs.class) {
            if (recordAudio == null) {
                recordAudio = true;
            }
            return recordAudio;
        }
    }

    public void setRecordAudio(final boolean recordAudio) {
        synchronized (AppPrefs.class) {
            this.recordAudio = recordAudio;
        }
    }

    public VideoFilterDescriptor getVideoFilter() {
        synchronized (AppPrefs.class) {
            if (videoFilter == null) {
                videoFilter = VideoFilterDescriptor.NoFilter;
            }
            return videoFilter;
        }
    }

    public void setVideoFilter(final VideoFilterDescriptor videoFilter) {
        synchronized (AppPrefs.class) {
            this.videoFilter = videoFilter;
        }
    }

    public int getScale() {
        synchronized (AppPrefs.class) {
            if (scale == null) {
                scale = 1;
            }
            return scale;
        }
    }

    public void setScale(final int scale) {
        synchronized (AppPrefs.class) {
            this.scale = scale;
        }
    }

    public boolean isCropBorders() {
        synchronized (AppPrefs.class) {
            if (cropBorders == null) {
                cropBorders = true;
            }
            return cropBorders;
        }
    }

    public void setCropBorders(final boolean cropBorders) {
        synchronized (AppPrefs.class) {
            this.cropBorders = cropBorders;
        }
    }

    public boolean isSmoothScaling() {
        synchronized (AppPrefs.class) {
            if (smoothScaling == null) {
                smoothScaling = false;
            }
            return smoothScaling;
        }
    }

    public void setSmoothScaling(final boolean smoothScaling) {
        synchronized (AppPrefs.class) {
            this.smoothScaling = smoothScaling;
        }
    }

    public boolean isUseTvAspectRatio() {
        synchronized (AppPrefs.class) {
            if (useTvAspectRatio == null) {
                useTvAspectRatio = false;
            }
            return useTvAspectRatio;
        }
    }

    public void setUseTvAspectRatio(final boolean useTvAspectRatio) {
        synchronized (AppPrefs.class) {
            this.useTvAspectRatio = useTvAspectRatio;
        }
    }

    public FramesOption getFramesOption() {
        synchronized (AppPrefs.class) {
            if (framesOption == null) {
                framesOption = FramesOption.SaveAll;
            }
            return framesOption;
        }
    }

    public void setFramesOption(final FramesOption framesOption) {
        synchronized (AppPrefs.class) {
            this.framesOption = framesOption;
        }
    }

    public String getSuffix() {
        synchronized (AppPrefs.class) {
            if (suffix == null) {
                suffix = "-%03d";
            }
            return suffix;
        }
    }

    public void setSuffix(final String suffix) {
        synchronized (AppPrefs.class) {
            this.suffix = suffix;
        }
    }

    public String getPalette() {
        synchronized (AppPrefs.class) {
            if (palette == null) {
                palette = PaletteNames.CURRENT;
            }
            return palette;
        }
    }

    public void setPalette(String palette) {
        synchronized (AppPrefs.class) {
            this.palette = palette;
        }
    }
}