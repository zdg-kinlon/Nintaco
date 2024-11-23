package nintaco.input.zapper;

import nintaco.Machine;
import nintaco.input.DeviceMapper;
import nintaco.input.Ports;
import nintaco.input.icons.InputIcons;
import nintaco.mappers.Mapper;
import nintaco.tv.TVSystem;

import java.io.Serializable;

import static nintaco.input.InputDevices.Zapper;

public class ZapperMapper extends DeviceMapper implements Serializable {

    protected static final int PHOTO_SENSOR_SCANLINES = 26;
    private static final long serialVersionUID = 0;
    protected final int shift;
    protected final int portIndex;

    protected Mapper mapper;
    protected TVSystem tvSystem;
    protected int triggerScanlines;

    protected boolean triggerPulled;
    protected boolean triggerReleased = true;
    protected boolean offscreen;
    protected int coordinates;
    protected int photoSensor;
    protected int trigger;
    protected int portValue;
    protected boolean expansionPort;

    public ZapperMapper(final int portIndex) {
        if (portIndex == Ports.ExpansionPort) {
            expansionPort = true;
        }
        if (portIndex == 0) {
            this.shift = 0;
            this.portIndex = 0;
        } else {
            this.shift = 8;
            this.portIndex = 1;
        }
    }

    @Override
    public void setMachine(final Machine machine) {
        mapper = machine.getMapper();
        machine.getPPU().setZapper(this);
    }

    @Override
    public int getInputDevice() {
        return Zapper;
    }

    public int getShift() {
        return shift;
    }

    public void handleLightDetected() {
        photoSensor = PHOTO_SENSOR_SCANLINES;
        updatePortValue();
    }

    public void handleScanline() {
        if (trigger > 0 && --trigger == 0) {
            offscreen = false;
        }
        if (photoSensor > 0) {
            --photoSensor;
        }
        updatePortValue();
    }

    protected void updatePortValue() {
        portValue = 0;
        if (trigger != 0) {
            portValue |= 0x10;
        }
        if (photoSensor == 0 && !offscreen) {
            portValue |= 0x08;
        }
    }

    @Override
    public void update(final int buttons) {

        triggerPulled = ((buttons >> shift) & 0x04) != 0;
        coordinates = (buttons >> 16) & 0xFFFF;

        if (triggerPulled) {

            final TVSystem system = mapper.getTVSystem();
            if (tvSystem != system) {
                tvSystem = system;
                triggerScanlines = (int) Math.round(system.getScanlineCount()
                        / (10.0 * system.getSecondsPerFrame()));
            }

            if (triggerReleased) {
                triggerReleased = false;
                trigger = triggerScanlines;
                offscreen = (coordinates == 0xFFFF);
            }

        } else {
            triggerReleased = true;
        }

        if (offscreen) {
            coordinates = 0xFFFF;
        }

        updatePortValue();
    }

    public boolean isTrigger() {
        return trigger > 0;
    }

    public int getCoordinates() {
        return coordinates;
    }

    @Override
    public void writePort(final int value) {
    }

    @Override
    public int readPort(final int portIndex) {
        return this.portIndex == portIndex ? portValue : 0;
    }

    @Override
    public int peekPort(final int portIndex) {
        return readPort(portIndex);
    }

    @Override
    public void render(final int[] screen) {
        final int x = expansionPort ? 169 : 8 + (portIndex << 6);
        final int y = 205;
        InputIcons.Zapper.render(screen, x, y);
        if (triggerPulled) {
            InputIcons.ZapperTrigger.render(screen, x + 14, y + 8);
        }
        final int X = coordinates & 0xFF;
        final int Y = (coordinates >> 8) & 0xFF;
        if (Y < 240) {
            InputIcons.ZapperTarget.renderSafe(screen, X - 7, Y - 7);
        }
    }
}