package cn.kinlon.emu.mappers.unif.unl;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.pirate.Mapper083;



import static cn.kinlon.emu.utils.BitUtil.*;

public class YOKO extends Mapper083 {

    private static final long serialVersionUID = 0;

    public YOKO(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        resetBit = 3;
        super.init();
    }

    @Override
    public void resetting() {
        resetBit = (resetBit + 1) & 3;
        mode = 0;
        bank = 0;
        updateBanks();
    }

    @Override
    protected void updateBanks() {
        setNametableMirroring(mode & 1);
        setChrBanks(0, 2, regs[3] << 1);
        setChrBanks(2, 2, regs[4] << 1);
        setChrBanks(4, 2, regs[5] << 1);
        setChrBanks(6, 2, regs[6] << 1);
        if (getBitBool(mode, 4)) {
            final int base = (bank & 8) << 1;
            setPrgBank(4, base | (regs[0] & 0x0F));
            setPrgBank(5, base | (regs[1] & 0x0F));
            setPrgBank(6, base | (regs[2] & 0x0F));
            setPrgBank(7, base | 0x0F);
        } else {
            if (getBitBool(mode, 3)) {
                setPrgBanks(4, 4, (bank & 0xFE) << 1);
            } else {
                setPrgBanks(4, 2, bank << 1);
                setPrgBanks(6, 2, -2);
            }
        }
    }

    @Override
    public int readMemory(final int address) {
        if (address >= 0x5000 && address < 0x5400) {
            return resetBit;
        } else if (address >= 0x5400 && address < 0x6000) {
            return exRegs[address & 3];
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {

        memory[address] = value;

        if (address >= 0x5400 && address < 0x6000) {
            exRegs[address & 3] = value;
        } else if (address >= 0x8000) {
            switch (address & 0x8C17) {
                case 0x8000:
                    bank = value;
                    updateBanks();
                    break;
                case 0x8400:
                    mode = value;
                    updateBanks();
                    break;
                case 0x8800:
                    irqCounter = (irqCounter & 0xFF00) | value;
                    cpu.setMapperIrq(false);
                    break;
                case 0x8801:
                    irqEnabled = getBitBool(mode, 7);
                    irqCounter = (value << 8) | (irqCounter & 0x00FF);
                    break;
                case 0x8C00:
                    regs[0] = value;
                    updateBanks();
                    break;
                case 0x8C01:
                    regs[1] = value;
                    updateBanks();
                    break;
                case 0x8C02:
                    regs[2] = value;
                    updateBanks();
                    break;
                case 0x8C10:
                    regs[3] = value;
                    updateBanks();
                    break;
                case 0x8C11:
                    regs[4] = value;
                    updateBanks();
                    break;
                case 0x8C16:
                    regs[5] = value;
                    updateBanks();
                    break;
                case 0x8C17:
                    regs[6] = value;
                    updateBanks();
                    break;
            }
        }
    }
}