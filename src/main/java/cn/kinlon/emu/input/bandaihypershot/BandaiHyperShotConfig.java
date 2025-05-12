package cn.kinlon.emu.input.bandaihypershot;

import cn.kinlon.emu.input.DeviceConfig;

import java.io.Serializable;

import static cn.kinlon.emu.input.InputDevices.BandaiHyperShot;

public class BandaiHyperShotConfig
        extends DeviceConfig implements Serializable {

    private static final long serialVersionUID = 0;

    public BandaiHyperShotConfig() {
        super(BandaiHyperShot);
    }

    public BandaiHyperShotConfig(
            final BandaiHyperShotConfig bandaiHyperShotConfig) {
        super(bandaiHyperShotConfig);
    }

    @Override
    public BandaiHyperShotConfig copy() {
        return new BandaiHyperShotConfig(this);
    }
}
