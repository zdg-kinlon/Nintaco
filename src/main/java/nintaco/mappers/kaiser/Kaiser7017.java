package nintaco.mappers.kaiser;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

import static nintaco.util.BitUtil.toBit;

public class Kaiser7017 extends Mapper {

    private static final long serialVersionUID = 0;

    private int bank2;
    private int mirroring;
    private int irqCounter;
    private boolean irqEnabled;

    public Kaiser7017(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        updateState();
        setChrBank(0);
    }

    private void updateState() {
        setPrgBank(2, bank2);
        setPrgBank(3, 2);
        setNametableMirroring(mirroring);
    }

    @Override
    public int readMemory(final int address) {
        if (address == 0x4030) {
            final int value = toBit(cpu.getMapperIrq());
            cpu.setMapperIrq(false);
            return value;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if (address >= 0x4020 && address < 0x6000) {
            if ((address & 0xFF00) == 0x4A00) {
                bank2 = ((address >> 2) & 3) | ((address >> 4) & 4);
            } else if ((address & 0xFF00) == 0x5100) {
                updateState();
            } else if (address == 0x4020) {
                cpu.setMapperIrq(false);
                irqCounter = (irqCounter & 0xFF00) | value;
            } else if (address == 0x4021) {
                cpu.setMapperIrq(false);
                irqCounter = (value << 8) | (irqCounter & 0x00FF);
                irqEnabled = true;
            } else if (address == 0x4025) {
                mirroring = (value & 8) >> 3;
            }
        }
    }

    @Override
    public void update() {
        if (irqEnabled && --irqCounter <= 0) {
            irqEnabled = false;
            cpu.setMapperIrq(true);
        }
    }
}