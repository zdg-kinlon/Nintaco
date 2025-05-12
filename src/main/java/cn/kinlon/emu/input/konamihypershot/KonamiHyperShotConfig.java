package cn.kinlon.emu.input.konamihypershot;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.KonamiHyperShot;

public class KonamiHyperShotConfig
        extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public KonamiHyperShotConfig() {
        super(KonamiHyperShot);
    }

    public KonamiHyperShotConfig(
            final KonamiHyperShotConfig konamiHyperShotConfig) {
        super(konamiHyperShotConfig);
    }

    @Override
    public KonamiHyperShotConfig copy() {
        return new KonamiHyperShotConfig(this);
    }
}
