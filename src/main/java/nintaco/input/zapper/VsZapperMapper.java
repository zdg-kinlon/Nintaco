package nintaco.input.zapper;

import java.io.Serializable;

public class VsZapperMapper extends ZapperMapper implements Serializable {

    private static final long serialVersionUID = 0;

    private int shiftRegister;
    private boolean strobe;

    public VsZapperMapper(final int portIndex) {
        super(portIndex);
    }

    @Override
    protected void updatePortValue() {
        portValue = 0x10;
        if (trigger != 0) {
            portValue |= 0x80;
        }
        if (photoSensor != 0) {
            portValue |= 0x40;
        }
    }

    @Override
    public void writePort(final int value) {
        strobe = (value & 1) == 1;
        if (strobe) {
            shiftRegister = portValue;
        }
    }

    @Override
    public int readPort(final int portIndex) {
        if (this.portIndex == portIndex) {
            final int value = shiftRegister & 1;
            if (!strobe) {
                shiftRegister >>= 1;
            }
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public int peekPort(final int portIndex) {
        return this.portIndex == portIndex ? (shiftRegister & 1) : 0;
    }
}