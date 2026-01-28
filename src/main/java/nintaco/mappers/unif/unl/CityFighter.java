package nintaco.mappers.unif.unl;

import nintaco.files.*;
import nintaco.mappers.*;

import static nintaco.util.BitUtil.*;

public class CityFighter extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] chrRegs = new int[8];

    private int prgReg;
    private int mirroring;
    private int irqCounter;
    private boolean irqEnabled;
    private boolean prgMode;

    public CityFighter(final CartFile cartFile) {
        super(cartFile, 8, 8);
    }

    @Override
    public void init() {
        prgReg = 0;
        updateBanks();
    }

    private void updateBanks() {
        setPrgBanks(4, 4, prgReg & 0xFC);
        if (!prgMode) {
            setPrgBank(6, prgReg);
        }
        for (int i = 7; i >= 0; i--) {
            setChrBank(i, chrRegs[i]);
        }
        setNametableMirroring(mirroring);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF00C) {
            case 0x9000:
                prgReg = value & 0xC;
                mirroring = value & 3;
                break;
            case 0x9004:
            case 0x9008:
            case 0x900C:
                if (getBitBool(address, 11)) {
                    apu.dmc.writeDirectLoad((value & 0x0F) << 3);
                } else {
                    prgReg = value & 0xC;
                }
                break;
            case 0xC000:
            case 0xC004:
            case 0xC008:
            case 0xC00C:
                prgMode = toBitBool(value);
                break;
            case 0xD000:
                chrRegs[0] = (chrRegs[0] & 0xF0) | (value & 0x0F);
                break;
            case 0xD004:
                chrRegs[0] = (chrRegs[0] & 0x0F) | (value << 4);
                break;
            case 0xD008:
                chrRegs[1] = (chrRegs[1] & 0xF0) | (value & 0x0F);
                break;
            case 0xD00C:
                chrRegs[1] = (chrRegs[1] & 0x0F) | (value << 4);
                break;
            case 0xA000:
                chrRegs[2] = (chrRegs[2] & 0xF0) | (value & 0x0F);
                break;
            case 0xA004:
                chrRegs[2] = (chrRegs[2] & 0x0F) | (value << 4);
                break;
            case 0xA008:
                chrRegs[3] = (chrRegs[3] & 0xF0) | (value & 0x0F);
                break;
            case 0xA00C:
                chrRegs[3] = (chrRegs[3] & 0x0F) | (value << 4);
                break;
            case 0xB000:
                chrRegs[4] = (chrRegs[4] & 0xF0) | (value & 0x0F);
                break;
            case 0xB004:
                chrRegs[4] = (chrRegs[4] & 0x0F) | (value << 4);
                break;
            case 0xB008:
                chrRegs[5] = (chrRegs[5] & 0xF0) | (value & 0x0F);
                break;
            case 0xB00C:
                chrRegs[5] = (chrRegs[5] & 0x0F) | (value << 4);
                break;
            case 0xE000:
                chrRegs[6] = (chrRegs[6] & 0xF0) | (value & 0x0F);
                break;
            case 0xE004:
                chrRegs[6] = (chrRegs[6] & 0x0F) | (value << 4);
                break;
            case 0xE008:
                chrRegs[7] = (chrRegs[7] & 0xF0) | (value & 0x0F);
                break;
            case 0xE00C:
                chrRegs[7] = (chrRegs[7] & 0x0F) | (value << 4);
                break;
            case 0xF000:
                irqCounter = ((irqCounter & 0x1E0) | ((value & 0xF) << 1));
                break;
            case 0xF004:
                irqCounter = ((irqCounter & 0x1E) | ((value & 0xF) << 5));
                break;
            case 0xF008:
                irqEnabled = getBitBool(value, 1);
                cpu.interrupt().setMapperIrq(false);
                break;
        }
        updateBanks();
    }

    @Override
    public void update() {
        if (irqEnabled && --irqCounter <= 0) {
            cpu.interrupt().setMapperIrq(true);
        }
    }
}