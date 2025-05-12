package cn.kinlon.emu.gui.userinterface;

import cn.kinlon.emu.App;
import cn.kinlon.emu.cartdb.CartDB;
import cn.kinlon.emu.gui.image.ImagePane;
import cn.kinlon.emu.preferences.AppPrefs;
import cn.kinlon.emu.utils.GuiUtil;
import cn.kinlon.emu.utils.TimeUtil;

import java.io.Serializable;

import static cn.kinlon.emu.utils.GuiUtil.requestVsync;

public class UserInterfacePrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Boolean launchFileOpen;
    private Boolean referenceDatabase;
    private Boolean applyIpsPatches;
    private Boolean hideMenuBar;
    private Boolean enterFullscreen;
    private Boolean pauseMenu;
    private Boolean confirmReset;
    private Boolean confirmExit;
    private Boolean confirmHotSwap;
    private Boolean disableScreensaver;
    private Boolean allowMultipleInstances;
    private Boolean runInBackground;
    private Boolean acceptBackgroundInput;
    private Boolean useVsync;
    private Boolean useMulticoreFiltering;
    private Integer maxLagFrames;
    private InterframeDelay interframeDelay;
    private InitialRamState initialRamState;

    public boolean isLaunchFileOpen() {
        synchronized (AppPrefs.class) {
            if (launchFileOpen == null) {
                launchFileOpen = false;
            }
            return launchFileOpen;
        }
    }

    public void setLaunchFileOpen(final boolean launchFileOpen) {
        synchronized (AppPrefs.class) {
            this.launchFileOpen = launchFileOpen;
        }
    }

    public boolean isReferenceDatabase() {
        synchronized (AppPrefs.class) {
            if (referenceDatabase == null) {
                referenceDatabase = true;
            }
            return referenceDatabase;
        }
    }

    public void setReferenceDatabase(final boolean referenceDatabase) {
        synchronized (AppPrefs.class) {
            this.referenceDatabase = referenceDatabase;
        }
    }

    public boolean isApplyIpsPatches() {
        synchronized (AppPrefs.class) {
            if (applyIpsPatches == null) {
                applyIpsPatches = true;
            }
            return applyIpsPatches;
        }
    }

    public void setApplyIpsPatches(final boolean applyIpsPatches) {
        synchronized (AppPrefs.class) {
            this.applyIpsPatches = applyIpsPatches;
        }
    }

    public boolean isHideMenuBar() {
        synchronized (AppPrefs.class) {
            if (hideMenuBar == null) {
                hideMenuBar = false;
            }
            return hideMenuBar;
        }
    }

    public void setHideMenuBar(final boolean hideMenuBar) {
        synchronized (AppPrefs.class) {
            this.hideMenuBar = hideMenuBar;
        }
    }

    public boolean isEnterFullscreen() {
        synchronized (AppPrefs.class) {
            if (enterFullscreen == null) {
                enterFullscreen = false;
            }
            return enterFullscreen;
        }
    }

    public void setEnterFullscreen(final boolean enterFullscreen) {
        synchronized (AppPrefs.class) {
            this.enterFullscreen = enterFullscreen;
        }
    }

    public boolean isPauseMenu() {
        synchronized (AppPrefs.class) {
            if (pauseMenu == null) {
                pauseMenu = true;
            }
            return pauseMenu;
        }
    }

    public void setPauseMenu(final boolean pauseMenu) {
        synchronized (AppPrefs.class) {
            this.pauseMenu = pauseMenu;
        }
    }

    public boolean isConfirmReset() {
        synchronized (AppPrefs.class) {
            if (confirmReset == null) {
                confirmReset = false;
            }
            return confirmReset;
        }
    }

    public void setConfirmReset(final boolean confirmReset) {
        synchronized (AppPrefs.class) {
            this.confirmReset = confirmReset;
        }
    }

    public boolean isConfirmExit() {
        synchronized (AppPrefs.class) {
            if (confirmExit == null) {
                confirmExit = true;
            }
            return confirmExit;
        }
    }

    public void setConfirmExit(final boolean confirmExit) {
        synchronized (AppPrefs.class) {
            this.confirmExit = confirmExit;
        }
    }

    public boolean isConfirmHotSwap() {
        synchronized (AppPrefs.class) {
            if (confirmHotSwap == null) {
                confirmHotSwap = true;
            }
            return confirmHotSwap;
        }
    }

    public void setConfirmHotSwap(final boolean confirmHotSwap) {
        synchronized (AppPrefs.class) {
            this.confirmHotSwap = confirmHotSwap;
        }
    }

    public boolean isDisableScreensaver() {
        synchronized (AppPrefs.class) {
            if (disableScreensaver == null) {
                disableScreensaver = true;
            }
            return disableScreensaver;
        }
    }

    public void setDisableScreensaver(final boolean disableScreensaver) {
        synchronized (AppPrefs.class) {
            this.disableScreensaver = disableScreensaver;
        }
    }

    public boolean isAllowMultipleInstances() {
        synchronized (AppPrefs.class) {
            if (allowMultipleInstances == null) {
                allowMultipleInstances = false;
            }
            return allowMultipleInstances;
        }
    }

    public void setAllowMultipleInstances(final boolean allowMultipleInstances) {
        synchronized (AppPrefs.class) {
            this.allowMultipleInstances = allowMultipleInstances;
        }
    }

    public boolean isRunInBackground() {
        synchronized (AppPrefs.class) {
            if (runInBackground == null) {
                runInBackground = false;
            }
            return runInBackground;
        }
    }

    public void setRunInBackground(final boolean runInBackground) {
        synchronized (AppPrefs.class) {
            this.runInBackground = runInBackground;
        }
    }

    public boolean isAcceptBackgroundInput() {
        synchronized (AppPrefs.class) {
            if (acceptBackgroundInput == null) {
                acceptBackgroundInput = false;
            }
            return acceptBackgroundInput;
        }
    }

    public void setAcceptBackgroundInput(final boolean acceptBackgroundInput) {
        synchronized (AppPrefs.class) {
            this.acceptBackgroundInput = acceptBackgroundInput;
        }
    }

    public boolean isUseVsync() {
        synchronized (AppPrefs.class) {
            if (useVsync == null) {
                useVsync = true;
            }
            return useVsync;
        }
    }

    public void setUseVsync(final boolean useVsync) {
        synchronized (AppPrefs.class) {
            this.useVsync = useVsync;
        }
    }

    public boolean isUseMulticoreFiltering() {
        synchronized (AppPrefs.class) {
            if (useMulticoreFiltering == null) {
                useMulticoreFiltering = true;
            }
            return useMulticoreFiltering;
        }
    }

    public void setUseMulticoreFiltering(final boolean useMulticoreFiltering) {
        synchronized (AppPrefs.class) {
            this.useMulticoreFiltering = useMulticoreFiltering;
        }
    }

    public int getMaxLagFrames() {
        synchronized (AppPrefs.class) {
            if (maxLagFrames == null) {
                maxLagFrames = 60;
            }
            return maxLagFrames;
        }
    }

    public void setMaxLagFrames(final int maxLagFrames) {
        synchronized (AppPrefs.class) {
            this.maxLagFrames = maxLagFrames;
        }
    }

    public InterframeDelay getInterframeDelay() {
        synchronized (AppPrefs.class) {
            if (interframeDelay == null) {
                interframeDelay = InterframeDelay.Sleep;
            }
            return interframeDelay;
        }
    }

    public void setInterframeDelay(final InterframeDelay interframeDelay) {
        synchronized (AppPrefs.class) {
            this.interframeDelay = interframeDelay;
        }
    }

    public InitialRamState getInitialRamState() {
        synchronized (AppPrefs.class) {
            if (initialRamState == null) {
                initialRamState = InitialRamState.All00;
            }
            return initialRamState;
        }
    }

    public void setInitialRamState(InitialRamState initialRamState) {
        synchronized (AppPrefs.class) {
            this.initialRamState = initialRamState;
        }
    }

    public void apply() {
        synchronized (AppPrefs.class) {
            final ImagePane imagePane = App.getImageFrame().getImagePane();
            imagePane.createVideoFilterThreads();
            TimeUtil.setMaxLagFrames(getMaxLagFrames());
            TimeUtil.setInterframeDelay(getInterframeDelay());
            CartDB.setEnabled(isReferenceDatabase());
            GuiUtil.setDisableScreensaver(isDisableScreensaver());
            requestVsync(App.getImageFrame(), isUseVsync());
        }
    }
}