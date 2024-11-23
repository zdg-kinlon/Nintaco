package nintaco.gui.screenshots;

import nintaco.App;
import nintaco.gui.exportmedia.ImageConverter;
import nintaco.gui.exportmedia.preferences.ExportMediaFilePrefs;
import nintaco.preferences.AppPrefs;
import nintaco.tv.TVSystem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static nintaco.files.FileUtil.*;
import static nintaco.gui.image.ImagePane.IMAGE_HEIGHT;
import static nintaco.gui.image.ImagePane.IMAGE_WIDTH;
import static nintaco.tv.TVSystem.NTSC;
import static nintaco.util.GuiUtil.getWritableImageFileFormats;
import static nintaco.util.StringUtil.isBlank;

public final class ScreenshotSaver {

    private static final List<QueueElement> queue = new ArrayList<>();
    private static Thread thread;
    private static WeakReference<ImageConverter> weakConverter;

    private ScreenshotSaver() {
    }

    public static void save(final int[] screen, final TVSystem tvSystem) {
        final BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT,
                BufferedImage.TYPE_INT_RGB);
        final int[] data = ((DataBufferInt) image.getRaster().getDataBuffer())
                .getData();
        System.arraycopy(screen, 0, data, 0, screen.length);
        synchronized (queue) {
            queue.add(new QueueElement(image, tvSystem));
            if (thread == null) {
                thread = new Thread(ScreenshotSaver::processQueue,
                        "Screenshot Saver Thread");
                thread.start();
            }
        }
    }

    private static void processQueue() {
        while (true) {
            final QueueElement element;
            synchronized (queue) {
                if (queue.isEmpty()) {
                    thread = null;
                    return;
                } else {
                    element = queue.remove(0);
                }
            }
            try {
                processQueueElement(element);
            } catch (final Throwable t) {
//        t.printStackTrace();
            }
        }
    }

    private static void processQueueElement(final QueueElement element)
            throws Throwable {
        final String dir = AppPrefs.getInstance().getPaths().getScreenshotsDir();
        mkdir(dir);

        final String base = getFileNameWithoutExtension(App.getEntryFileName());
        final String fileBase = isBlank(base) ? "images" : base;
        final ExportMediaFilePrefs prefs = AppPrefs.getInstance()
                .getScreenshotPrefs();
        final String extension = getWritableImageFileFormats()[prefs.getFileType()];
        final File file = createUniqueFile(dir, fileBase, extension, true,
                prefs.getSuffix());

        ImageConverter imageConverter = null;
        if (weakConverter != null) {
            imageConverter = weakConverter.get();
        }
        if (imageConverter != null && !(imageConverter.getPrefs().equals(prefs)
                && imageConverter.getTvSystem() == element.getTvSystem())) {
            imageConverter.dispose();
            imageConverter = null;
        }
        if (imageConverter == null) {
            imageConverter = new ImageConverter(prefs, element.getTvSystem(), false);
        }
        imageConverter.setImage(element.getImage());
        final BufferedImage result = imageConverter.convert();
        weakConverter = new WeakReference<>(imageConverter);

        ImageIO.write(result, extension, file);
    }

    private static final class QueueElement {
        private final BufferedImage image;
        private final TVSystem tvSystem;

        public QueueElement(final BufferedImage image, final TVSystem tvSystem) {
            this.image = image;
            this.tvSystem = tvSystem == null ? NTSC : tvSystem;
        }

        public BufferedImage getImage() {
            return image;
        }

        public TVSystem getTvSystem() {
            return tvSystem;
        }
    }
}