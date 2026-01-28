package nintaco.mappers.unif.btl;

import nintaco.files.*;
import nintaco.mappers.konami.*;

public class BTL900218 extends VRC2And4 {

    private static final long serialVersionUID = 0;

    private int irqCounter;

    public BTL900218(final CartFile cartFile) {
        super(cartFile);
        prgHigh = 0x20;
        variant = VRC4e;
    }

    @Override
    public void resetting() {
        irqEnabled = false;
        irqCounter = 0;
        cpu.interrupt().setMapperIrq(false);
    }

    @Override
    public void update() {
        if (irqEnabled && (++irqCounter & 0x400) != 0) {
            cpu.interrupt().setMapperIrq(true);
        }
    }

    private void writeIRQ(final int address) {
        switch ((address & 0x000C)) {
            case 0x0008:
                irqEnabled = true;
                break;
            case 0x000C:
                irqEnabled = false;
                irqCounter = 0;
                cpu.interrupt().setMapperIrq(false);
                break;
        }
    }

    @Override
    protected void writeRegister(int address, final int value) {
        if ((address & 0xF000) == 0xF000) {
            writeIRQ(address);
        } else {
            super.writeRegister(address, value);
        }
    }
}
