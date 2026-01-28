package nintaco.mappers.racermate;

import nintaco.files.*;
import nintaco.mappers.*;
import nintaco.preferences.*;

public class RacerMate extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int CLOCKS_PER_IRQ = 1024;

    private int irqCounter = CLOCKS_PER_IRQ;
    private boolean irqEnabled;

    public RacerMate(final CartFile cartFile) {
        super(cartFile, 4, 2);
    }

    @Override
    protected int getChrRamSize(final CartFile cartFile) {
        return 0x10000; // 64K of battery-backed CHR RAM
    }

    @Override
    public void init() {
        setPrgBank(3, -1);
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if (address < 0xC000) {
            writeBankSelect(value);
        } else {
            writeIrqAcknowledge(address);
        }
    }

    private void writeBankSelect(final int value) {
        setPrgBank(2, value >> 6);
        setChrBank(1, value & 0x0F);
    }

    private void writeIrqAcknowledge(final int address) {
        if (address == 0xF080) {
            irqEnabled = false;
            irqCounter = CLOCKS_PER_IRQ;
            cpu.interrupt().setMapperIrq(false);
        } else if (address == 0xF000) {
            irqEnabled = true;
        }
    }

    @Override
    public void update() {
        if (irqEnabled && --irqCounter == 0) {
            cpu.interrupt().setMapperIrq(true);
        }
    }

    // 64K of battery-backed CHR RAM
    @Override
    public void loadNonVolatilePrgRam() {
        synchronized (GamePrefs.class) {
            final int[] sram = GamePrefs.getInstance().getNonVolatilePrgRam();
            if (sram != null && sram.length == 0x10000) {
                System.arraycopy(sram, 0x0000, xChrRam, 0x0000, 0x10000);
            }
        }
    }

    // 64K of battery-backed CHR RAM
    @Override
    protected void saveNonVolatilePrgRam() {
        synchronized (GamePrefs.class) {
            final GamePrefs prefs = GamePrefs.getInstance();
            int[] sram = prefs.getNonVolatilePrgRam();
            if (sram == null || sram.length != 0x10000) {
                sram = new int[0x10000];
            }
            System.arraycopy(xChrRam, 0x0000, sram, 0x0000, 0x10000);
            prefs.setNonVolatilePrgRam(sram);
        }
        GamePrefs.save();
    }
}