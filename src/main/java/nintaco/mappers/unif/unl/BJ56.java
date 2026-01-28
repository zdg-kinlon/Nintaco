package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.mappers.NametableMirroring.*;

public class BJ56 extends Mapper {

    private static final long serialVersionUID = 0;

    private int irqCounter;

    public BJ56(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        setNametableMirroring(VERTICAL);
        set4PrgBanks(4, 0xFC);
    }

    private void writeChrRomBankSelect(final int address, final int value) {
        setChrBank(address & 7, value);
    }

    private void writePrgRomBankSelect(final int address, final int value) {
        setPrgBank(4 | (address & 3), value);
    }

    private void resetIrqCounter() {
        irqCounter = 0;
    }

    private void acknowledgeIrq() {
        cpu.interrupt().setMapperIrq(false);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address < 0x8008) {
            writeChrRomBankSelect(address, value);
        } else if (address < 0x800C) {
            writePrgRomBankSelect(address, value);
        } else if (address == 0x800D) {
            resetIrqCounter();
        } else if (address == 0x800F) {
            acknowledgeIrq();
        }
    }

    @Override
    public void update() {
        if ((++irqCounter & 0x1000) != 0) {
            cpu.interrupt().setMapperIrq(true);
        }
    }
}