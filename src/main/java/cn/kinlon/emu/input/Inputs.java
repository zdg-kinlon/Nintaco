package cn.kinlon.emu.input;

import cn.kinlon.emu.App;
import cn.kinlon.emu.ppu.PPU;
import cn.kinlon.emu.cartdb.Cart;
import cn.kinlon.emu.cartdb.CartDB;
import cn.kinlon.emu.cartdb.CartDevices;
import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.gui.image.ImagePane;
import cn.kinlon.emu.input.other.SetPorts;
import cn.kinlon.emu.preferences.AppPrefs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static cn.kinlon.emu.input.ConsoleType.*;
import static cn.kinlon.emu.input.InputDevices.*;
import static cn.kinlon.emu.input.Ports.*;

public class Inputs implements Serializable {

    private static final long serialVersionUID = 0;

    private Ports ports;
    private List<DeviceConfig> deviceConfigs;
    private Boolean autoConfigure;
    private Integer autofireRate;
    private Integer highSpeedRate;
    private Boolean allowImpossibleInput;
    private Boolean showZapperCrosshairs;
    private Boolean hideMouseCursor;
    private Boolean hideInactiveMouse;
    private Boolean hideFullscreenMouse;
    private Boolean exchangeGlasses;
    private Boolean disableKeyboardRewindTime;
    private Integer zapperLightDetectionMargin;

    public Inputs() {
        init();
    }

    public static void clearDeviceConfig(final int inputDevice,
                                         final List<DeviceConfig> configs) {
        configs.get(inputDevice).clear();
    }

    public static void clearButtonMapping(final int inputDevice,
                                          final int button, final List<DeviceConfig> configs) {
        final DeviceConfig config = configs.get(inputDevice);
        config.getButtonMappings().set(button,
                config.getDeviceDescriptor().getNoButtonMapping(button));
    }

    public static ButtonMapping getButtonMapping(final int inputDevice,
                                                 final int button, final List<DeviceConfig> configs) {
        return configs.get(inputDevice).getButtonMappings().get(button);
    }

    public static void resetDeviceConfig(final int inputDevice, final int button,
                                         final List<DeviceConfig> configs) {
        final DeviceConfig config = configs.get(inputDevice);
        if (config == null) {
            configs.set(inputDevice, newDeviceConfig(inputDevice));
        } else {
            config.getButtonMappings().set(button,
                    config.getDeviceDescriptor().getDefaultButtonMapping(button));
        }
    }

    public static void resetDeviceConfigs(final int inputDevice,
                                          final List<DeviceConfig> configs) {
        configs.set(inputDevice, newDeviceConfig(inputDevice));
    }

    private void init() {
        synchronized (AppPrefs.class) {
            if (deviceConfigs == null) {
                deviceConfigs = new ArrayList<>();
            }
            while (deviceConfigs.size() < DevicesCount) {
                deviceConfigs.add(null);
            }
            for (int i = DevicesCount - 1; i >= 0; i--) {
                if (deviceConfigs.get(i) == null) {
                    deviceConfigs.set(i, newDeviceConfig(i));
                }
            }
        }
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }

    public int getAutofireRate() {
        synchronized (AppPrefs.class) {
            if (autofireRate == null) {
                autofireRate = 4;
            }
            return autofireRate;
        }
    }

    public void setAutofireRate(final int autofireRate) {
        synchronized (AppPrefs.class) {
            this.autofireRate = autofireRate;
        }
    }

    public int getHighSpeedRate() {
        synchronized (AppPrefs.class) {
            if (highSpeedRate == null) {
                highSpeedRate = 0;
            }
            return highSpeedRate;
        }
    }

    public void setHighSpeedRate(final int highSpeedRate) {
        synchronized (AppPrefs.class) {
            this.highSpeedRate = highSpeedRate;
        }
    }

    public boolean isAutoConfigure() {
        synchronized (AppPrefs.class) {
            if (autoConfigure == null) {
                autoConfigure = true;
            }
            return autoConfigure;
        }
    }

    public void setAutoConfigure(final boolean autoConfigure) {
        synchronized (AppPrefs.class) {
            this.autoConfigure = autoConfigure;
        }
    }

    public boolean isAllowImpossibleInput() {
        synchronized (AppPrefs.class) {
            if (allowImpossibleInput == null) {
                allowImpossibleInput = false;
            }
            return allowImpossibleInput;
        }
    }

    public void setAllowImpossibleInput(final boolean allowImpossibleInput) {
        synchronized (AppPrefs.class) {
            this.allowImpossibleInput = allowImpossibleInput;
        }
    }

    public boolean isShowZapperCrosshairs() {
        synchronized (AppPrefs.class) {
            if (showZapperCrosshairs == null) {
                showZapperCrosshairs = true;
            }
            return showZapperCrosshairs;
        }
    }

    public void setShowZapperCrosshairs(final boolean showZapperCrosshairs) {
        synchronized (AppPrefs.class) {
            this.showZapperCrosshairs = showZapperCrosshairs;
        }
    }

    public boolean isHideMouseCursor() {
        synchronized (AppPrefs.class) {
            if (hideMouseCursor == null) {
                hideMouseCursor = true;
            }
            return hideMouseCursor;
        }
    }

    public boolean isHideInactiveMouse() {
        synchronized (AppPrefs.class) {
            if (hideInactiveMouse == null) {
                hideInactiveMouse = true;
            }
            return hideInactiveMouse;
        }
    }

    public void setHideInactiveMouse(final boolean hideInactiveMouse) {
        synchronized (AppPrefs.class) {
            this.hideInactiveMouse = hideInactiveMouse;
        }
    }

    public boolean isHideFullscreenMouse() {
        synchronized (AppPrefs.class) {
            if (hideFullscreenMouse == null) {
                hideFullscreenMouse = true;
            }
            return hideFullscreenMouse;
        }
    }

    public void setHideFullscreenMouse(final boolean hideFullscreenMouse) {
        synchronized (AppPrefs.class) {
            this.hideFullscreenMouse = hideFullscreenMouse;
        }
    }

    public boolean isExchangeGlasses() {
        synchronized (AppPrefs.class) {
            if (exchangeGlasses == null) {
                exchangeGlasses = false;
            }
            return exchangeGlasses;
        }
    }

    public void setExchangeGlasses(final boolean exchangeGlasses) {
        synchronized (AppPrefs.class) {
            this.exchangeGlasses = exchangeGlasses;
        }
    }

    public boolean isDisableKeyboardRewindTime() {
        synchronized (AppPrefs.class) {
            if (disableKeyboardRewindTime == null) {
                disableKeyboardRewindTime = true;
            }
            return disableKeyboardRewindTime;
        }
    }

    public void setDisableKeyboardRewindTime(
            final boolean disableKeyboardRewindTime) {
        synchronized (AppPrefs.class) {
            this.disableKeyboardRewindTime = disableKeyboardRewindTime;
        }
    }

    public int getZapperLightDetectionMargin() {
        synchronized (AppPrefs.class) {
            if (zapperLightDetectionMargin == null) {
                zapperLightDetectionMargin = 3;
            }
            return zapperLightDetectionMargin;
        }
    }

    public void setZapperLightDetectionMargin(
            final int zapperLightDetectionMargin) {
        synchronized (AppPrefs.class) {
            this.zapperLightDetectionMargin = zapperLightDetectionMargin;
        }
    }

    public List<DeviceConfig> getDeviceConfigs() {
        return deviceConfigs;
    }

    public void setDeviceConfigs(final List<DeviceConfig> deviceConfigs) {
        synchronized (AppPrefs.class) {
            this.deviceConfigs = deviceConfigs;
        }
    }

    public Ports getPorts() {
        synchronized (AppPrefs.class) {
            if (ports == null) {
                ports = Ports.DEFAULTS;
            }
            return ports;
        }
    }

    public void setPorts(final Ports ports) {
        synchronized (AppPrefs.class) {
            this.ports = ports;
        }
    }

    public List<DeviceConfig> copyDeviceConfigs() {
        synchronized (AppPrefs.class) {
            final List<DeviceConfig> configs = new ArrayList<>();
            for (final DeviceConfig config : deviceConfigs) {
                configs.add(config.copy());
            }
            return configs;
        }
    }

    public void autoConfigure() {
        autoConfigure(null);
    }

    public void autoConfigure(final CartFile cartFile) {
        synchronized (AppPrefs.class) {
            if (isAutoConfigure()) {
                final int[][] portDevices;
                int consoleType = NES;
                boolean multitap = false;
                if (cartFile != null && cartFile.isVsDualSystem()) {
                    consoleType = VsDualSystem;
                    portDevices = setPortDevices(Gamepad1, Gamepad2, Gamepad3, Gamepad4);
                } else {
                    final Cart cart = (cartFile != null && CartDB.isEnabled())
                            ? cartFile.getCart() : null;
                    switch (cart == null ? -1 : cart.getDevice()) {
                        case CartDevices.FourPlayer:
                            multitap = true;
                            portDevices = setPortDevices(Gamepad1, Gamepad2, Gamepad3,
                                    Gamepad4);
                            break;
                        case CartDevices.Zapper:
                            portDevices = setPortDevices(Gamepad1, Zapper);
                            break;
                        case CartDevices._3DGlasses:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(Glasses);
                            break;
                        case CartDevices.Arkanoid:
                            portDevices = setPortDevices(Gamepad1, Arkanoid);
                            break;
                        case CartDevices.BandaiHyperShot:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(BandaiHyperShot);
                            break;
                        case CartDevices.BarcodeWorld:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(BarcodeBattler);
                            break;
                        case CartDevices.BattleBox:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(BattleBox);
                            break;
                        case CartDevices.CrazyClimber:
                            consoleType = Famicom;
                            portDevices = new int[][]{
                                    {Port1, CrazyClimberLeft},
                                    {Port2, CrazyClimberRight},
                                    {ExpansionPort, None}
                            };
                            break;
                        case CartDevices.DataRecorder:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(DataRecorder);
                            break;
                        case CartDevices.DongdaPEC586:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(DongdaPEC586Keyboard);
                            break;
                        case CartDevices.ExcitingBoxing:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(ExcitingBoxing);
                            break;
                        case CartDevices.FamilyKeyboard:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(Keyboard);
                            break;
                        case CartDevices.FamilyTrainer:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(FamilyTrainerMat);
                            break;
                        case CartDevices.KonamiHyperShot:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(KonamiHyperShot);
                            break;
                        case CartDevices.Mahjong:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(Mahjong);
                            break;
                        case CartDevices.MiraclePiano:
                            portDevices = setPortDevices(MiraclePiano, Gamepad1);
                            break;
                        case CartDevices.OekaKids:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(OekaKids);
                            break;
                        case CartDevices.Pachinko:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(Pachinko);
                            break;
                        case CartDevices.PartyTap:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(PartyTap);
                            break;
                        case CartDevices.PowerGlove:
                            portDevices = setPortDevices(PowerGlove, Gamepad2);
                            break;
                        case CartDevices.PowerPad:
                            portDevices = setPortDevices(Gamepad1, PowerPad);
                            break;
                        case CartDevices.RacerMate:
                            portDevices = setPortDevices(RacerMate1, RacerMate2);
                            break;
                        case CartDevices.Subor:
                            portDevices = setPortDevices(Gamepad1, Subor);
                            break;
                        case CartDevices.TapTapMat:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(TapTapMat);
                            break;
                        case CartDevices.TopRiderBike:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(TopRiderBike);
                            break;
                        case CartDevices.TurboFile:
                            consoleType = Famicom;
                            portDevices = setExpansionPortDevice(TurboFile);
                            break;
                        case CartDevices.Transformer:
                            portDevices = setPortDevices(Gamepad1, TransformerKeyboard);
                            break;
                        case CartDevices.UForce:
                            portDevices = setPortDevices(UForce, None);
                            break;
                        default:
                            portDevices = setPortDevices(Gamepad1, Gamepad2);
                            break;
                    }
                }

                new SetPorts(new Ports(portDevices, multitap, consoleType)).run(null);
            }
        }
    }

    private int[][] setExpansionPortDevice(final int device) {
        return new int[][]{
                {Port1, Gamepad1},
                {Port2, Gamepad2},
                {ExpansionPort, device}
        };
    }

    private int[][] setPortDevices(final int... devices) {
        final int[][] portDevices = new int[devices.length][2];
        for (int i = 0; i < devices.length; i++) {
            portDevices[i][0] = i;
            portDevices[i][1] = devices[i];
        }
        return portDevices;
    }

    public ButtonMapping getButtonMapping(final int inputDevice,
                                          final int button) {
        synchronized (AppPrefs.class) {
            return getButtonMapping(inputDevice, button, deviceConfigs);
        }
    }

    public void apply() {
        synchronized (AppPrefs.class) {
            final ImagePane imagePane = App.getImageFrame().getImagePane();
            imagePane.setHideInactiveMouseCursor(isHideInactiveMouse());
            imagePane.setHideFullscreenMouseCursor(isHideFullscreenMouse());
            
            PPU.setZapperLightDetectionMargin(getZapperLightDetectionMargin());
        }
    }
}
