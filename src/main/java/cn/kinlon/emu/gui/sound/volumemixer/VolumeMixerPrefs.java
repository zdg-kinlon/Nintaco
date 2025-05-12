package cn.kinlon.emu.gui.sound.volumemixer;

import cn.kinlon.emu.preferences.AppPrefs;

import java.io.Serializable;

public class VolumeMixerPrefs implements Serializable {

    private static final long serialVersionUID = 0;

    private Boolean soundEnabled;
    private Boolean smoothDMC;
    private Integer masterVolume;
    private Integer square1Volume;
    private Integer square2Volume;
    private Integer triangleVolume;
    private Integer noiseVolume;
    private Integer dmcVolume;
    private Integer fdsVolume;
    private Integer mmc5Volume;
    private Integer vrc6Volume;
    private Integer vrc7Volume;
    private Integer n163Volume;
    private Integer s5bVolume;

    public VolumeMixerPrefs() {
    }

    public boolean isSoundEnabled() {
        synchronized (AppPrefs.class) {
            if (soundEnabled == null) {
                soundEnabled = true;
            }
            return soundEnabled;
        }
    }

    public void setSoundEnabled(final boolean soundEnabled) {
        synchronized (AppPrefs.class) {
            this.soundEnabled = soundEnabled;
        }
    }

    public boolean isSmoothDMC() {
        synchronized (AppPrefs.class) {
            if (smoothDMC == null) {
                smoothDMC = true;
            }
            return smoothDMC;
        }
    }

    public void setSmoothDMC(final boolean smoothDMC) {
        synchronized (AppPrefs.class) {
            this.smoothDMC = smoothDMC;
        }
    }

    public int getMasterVolume() {
        synchronized (AppPrefs.class) {
            if (masterVolume == null) {
                masterVolume = 10;
            }
            return masterVolume;
        }
    }

    public void setMasterVolume(final int masterVolume) {
        synchronized (AppPrefs.class) {
            this.masterVolume = masterVolume;
        }
    }

    public int getSquare1Volume() {
        synchronized (AppPrefs.class) {
            if (square1Volume == null) {
                square1Volume = 100;
            }
            return square1Volume;
        }
    }

    public void setSquare1Volume(final int square1Volume) {
        synchronized (AppPrefs.class) {
            this.square1Volume = square1Volume;
        }
    }

    public int getSquare2Volume() {
        synchronized (AppPrefs.class) {
            if (square2Volume == null) {
                square2Volume = 100;
            }
            return square2Volume;
        }
    }

    public void setSquare2Volume(final int square2Volume) {
        synchronized (AppPrefs.class) {
            this.square2Volume = square2Volume;
        }
    }

    public int getTriangleVolume() {
        synchronized (AppPrefs.class) {
            if (triangleVolume == null) {
                triangleVolume = 100;
            }
            return triangleVolume;
        }
    }

    public void setTriangleVolume(final int triangleVolume) {
        synchronized (AppPrefs.class) {
            this.triangleVolume = triangleVolume;
        }
    }

    public int getNoiseVolume() {
        synchronized (AppPrefs.class) {
            if (noiseVolume == null) {
                noiseVolume = 100;
            }
            return noiseVolume;
        }
    }

    public void setNoiseVolume(final int noiseVolume) {
        synchronized (AppPrefs.class) {
            this.noiseVolume = noiseVolume;
        }
    }

    public int getDmcVolume() {
        synchronized (AppPrefs.class) {
            if (dmcVolume == null) {
                dmcVolume = 100;
            }
            return dmcVolume;
        }
    }

    public void setDmcVolume(final int dmcVolume) {
        synchronized (AppPrefs.class) {
            this.dmcVolume = dmcVolume;
        }
    }

    public int getFdsVolume() {
        synchronized (AppPrefs.class) {
            if (fdsVolume == null) {
                fdsVolume = 100;
            }
            return fdsVolume;
        }
    }

    public void setFdsVolume(final int fdsVolume) {
        synchronized (AppPrefs.class) {
            this.fdsVolume = fdsVolume;
        }
    }

    public int getMmc5Volume() {
        synchronized (AppPrefs.class) {
            if (mmc5Volume == null) {
                mmc5Volume = 100;
            }
            return mmc5Volume;
        }
    }

    public void setMmc5Volume(final int mmc5Volume) {
        synchronized (AppPrefs.class) {
            this.mmc5Volume = mmc5Volume;
        }
    }

    public int getVrc6Volume() {
        synchronized (AppPrefs.class) {
            if (vrc6Volume == null) {
                vrc6Volume = 100;
            }
            return vrc6Volume;
        }
    }

    public void setVrc6Volume(final int vrc6Volume) {
        synchronized (AppPrefs.class) {
            this.vrc6Volume = vrc6Volume;
        }
    }

    public int getVrc7Volume() {
        synchronized (AppPrefs.class) {
            if (vrc7Volume == null) {
                vrc7Volume = 100;
            }
            return vrc7Volume;
        }
    }

    public void setVrc7Volume(final int vrc7Volume) {
        synchronized (AppPrefs.class) {
            this.vrc7Volume = vrc7Volume;
        }
    }

    public int getN163Volume() {
        synchronized (AppPrefs.class) {
            if (n163Volume == null) {
                n163Volume = 100;
            }
            return n163Volume;
        }
    }

    public void setN163Volume(final int n163Volume) {
        synchronized (AppPrefs.class) {
            this.n163Volume = n163Volume;
        }
    }

    public int getS5bVolume() {
        synchronized (AppPrefs.class) {
            if (s5bVolume == null) {
                s5bVolume = 100;
            }
            return s5bVolume;
        }
    }

    public void setS5bVolume(final int s5bVolume) {
        synchronized (AppPrefs.class) {
            this.s5bVolume = s5bVolume;
        }
    }
}
