package nintaco.gui;

import java.io.File;
import java.util.Locale;

import static nintaco.files.FileUtil.getFileExtension;
import static nintaco.files.FileUtil.isDirectory;

public class FileExtensionFilter extends javax.swing.filechooser.FileFilter {

    private final String description;
    private final int index;

    private String[] extensions;

    public FileExtensionFilter(final int index,
                               String... descriptionAndExtensions) {

        if (descriptionAndExtensions == null
                || descriptionAndExtensions.length == 0) {
            descriptionAndExtensions = new String[]{""};
        }

        this.index = index;
        this.description = descriptionAndExtensions[0];
        this.extensions = new String[descriptionAndExtensions.length - 1];
        for (int i = extensions.length - 1; i >= 0; i--) {
            extensions[i] = descriptionAndExtensions[i + 1]
                    .toLowerCase(Locale.ENGLISH);
        }
    }

    public String[] getExtensions() {
        return extensions;
    }

    public void setExtensions(final String[] extensions) {
        this.extensions = extensions;
    }

    @Override
    public boolean accept(final File file) {
        if (file != null) {
            if (isDirectory(file) || extensions.length == 0) {
                return true;
            }

            final String extension = getFileExtension(file)
                    .toLowerCase(Locale.ENGLISH);
            for (int i = extensions.length - 1; i >= 0; i--) {
                if (extensions[i].equals(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public int getIndex() {
        return index;
    }
}
