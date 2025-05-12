package cn.kinlon.emu.gui;

import cn.kinlon.emu.App;
import cn.kinlon.emu.preferences.AppPrefs;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;

public final class PauseMenuListener implements MenuListener {

    private static final PauseMenuListener INSTANCE = new PauseMenuListener();

    private int selectedMenus;
    private boolean paused;

    private PauseMenuListener() {
    }

    public static void addPauseMenuListener(final JMenuBar menuBar) {
        for (final Component c : menuBar.getComponents()) {
            ((JMenu) c).addMenuListener(INSTANCE);
        }
    }

    @Override
    public void menuSelected(MenuEvent e) {
        menuSelectionChanged(1);
    }

    @Override
    public void menuDeselected(MenuEvent e) {
        EventQueue.invokeLater(() -> menuSelectionChanged(-1));
    }

    @Override
    public void menuCanceled(MenuEvent e) {
    }

    private void menuSelectionChanged(final int delta) {
        if (selectedMenus == 0 && delta > 0) {
            if (AppPrefs.getInstance().getUserInterfacePrefs().isPauseMenu()) {
                paused = true;
                App.setNoStepPause(true);
            }
        } else if (selectedMenus + delta == 0) {
            if (paused) {
                paused = false;
                App.setNoStepPause(false);
            }
        }
        selectedMenus += delta;
    }
}
