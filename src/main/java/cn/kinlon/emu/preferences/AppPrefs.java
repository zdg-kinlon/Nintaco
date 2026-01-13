package cn.kinlon.emu.preferences;

import cn.kinlon.emu.gui.fds.FamicomDiskSystemPrefs;
import cn.kinlon.emu.gui.hexeditor.preferences.HexEditorAppPrefs;
import cn.kinlon.emu.gui.image.preferences.HistoryPrefs;
import cn.kinlon.emu.gui.image.preferences.Paths;
import cn.kinlon.emu.gui.image.preferences.View;
import cn.kinlon.emu.gui.nsf.NsfPrefs;
import cn.kinlon.emu.gui.overscan.OverscanPrefs;
import cn.kinlon.emu.gui.sound.SoundPrefs;
import cn.kinlon.emu.gui.sound.volumemixer.VolumeMixerPrefs;
import cn.kinlon.emu.gui.userinterface.UserInterfacePrefs;
import cn.kinlon.emu.input.Inputs;
import cn.kinlon.emu.palettes.Palettes;
import cn.kinlon.emu.utils.PathUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.kinlon.emu.files.FileUtil.getWorkingDirectory;
import static cn.kinlon.emu.utils.ThreadUtil.joinAll;

public final class AppPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private static final File PREFERENCES_FILE = new File(
            getWorkingDirectory("nintaco.preferences"));

    private static AppPrefs instance;
    private static final List<Thread> threads = Collections.synchronizedList(
            new ArrayList<>());

    private Paths paths;
    private View view;
    private Inputs inputs;
    private Palettes palettes;
    private HexEditorAppPrefs hexEditorPrefs;
    private FamicomDiskSystemPrefs famicomDiskSystemPrefs;
    private SoundPrefs soundPrefs;
    private VolumeMixerPrefs volumeMixerPrefs;
    private HistoryPrefs historyPrefs;
    private OverscanPrefs overscanPrefs;
    private UserInterfacePrefs userInterfacePrefs;
    private NsfPrefs nsfPrefs;

    public synchronized NsfPrefs getNsfPrefs() {
        if (nsfPrefs == null) {
            nsfPrefs = new NsfPrefs();
        }
        return nsfPrefs;
    }

    public synchronized UserInterfacePrefs getUserInterfacePrefs() {
        if (userInterfacePrefs == null) {
            userInterfacePrefs = new UserInterfacePrefs();
        }
        return userInterfacePrefs;
    }

    public synchronized OverscanPrefs getOverscanPrefs() {
        if (overscanPrefs == null) {
            overscanPrefs = new OverscanPrefs();
        }
        return overscanPrefs;
    }

    public synchronized Palettes getPalettes() {
        if (palettes == null) {
            palettes = new Palettes();
        }
        return palettes;
    }

    public synchronized HistoryPrefs getHistoryPrefs() {
        if (historyPrefs == null) {
            historyPrefs = new HistoryPrefs();
        }
        return historyPrefs;
    }

    public synchronized VolumeMixerPrefs getVolumeMixerPrefs() {
        if (volumeMixerPrefs == null) {
            volumeMixerPrefs = new VolumeMixerPrefs();
        }
        return volumeMixerPrefs;
    }

    public synchronized SoundPrefs getSoundPrefs() {
        if (soundPrefs == null) {
            soundPrefs = new SoundPrefs();
        }
        return soundPrefs;
    }

    public synchronized FamicomDiskSystemPrefs getFamicomDiskSystemPrefs() {
        if (famicomDiskSystemPrefs == null) {
            famicomDiskSystemPrefs = new FamicomDiskSystemPrefs();
        }
        return famicomDiskSystemPrefs;
    }

    public synchronized HexEditorAppPrefs getHexEditorPrefs() {
        if (hexEditorPrefs == null) {
            hexEditorPrefs = new HexEditorAppPrefs();
        }
        return hexEditorPrefs;
    }

    public synchronized Paths getPaths() {
        if (paths == null) {
            paths = new Paths();
        }
        return paths;
    }

    public synchronized View getView() {
        if (view == null) {
            view = new View();
        }
        return view;
    }

    public synchronized Inputs getInputs() {
        if (inputs == null) {
            inputs = new Inputs();
        }
        return inputs;
    }

    public static synchronized AppPrefs getInstance() {
        return instance;
    }

    public static void flush() {
        joinAll(threads);
    }

    private static void dispose() {
        joinAll(threads);
        synchronized (AppPrefs.class) {
            instance = null;
        }
    }

    public static void load() {
        dispose();
        synchronized (AppPrefs.class) {
            try (final ObjectInputStream in = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(PREFERENCES_FILE)))) {
                instance = (AppPrefs) in.readObject();
            } catch (final Throwable t) {
            }
            if (instance == null) {
                instance = new AppPrefs();
            }
            instance.getPaths().init();
        }
    }

    public static void save() {
        final AppPrefs prefs;
        synchronized (AppPrefs.class) {
            if (instance == null) {
                return;
            }
            prefs = instance;
        }

        final Thread thread = new Thread(() -> {
            synchronized (AppPrefs.class) {
                try (final ObjectOutputStream out = new ObjectOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(PREFERENCES_FILE)))) {
                    out.writeObject(prefs);
                } catch (Throwable t) {
                    //t.printStackTrace();
                }
            }
            threads.remove(Thread.currentThread());
        }, "AppPrefs Save Thread");

        synchronized (threads) {
            thread.start();
            threads.add(thread);
        }
    }

    private AppPrefs() {
    }
}