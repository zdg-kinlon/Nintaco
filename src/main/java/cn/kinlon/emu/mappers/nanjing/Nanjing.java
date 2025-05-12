package cn.kinlon.emu.mappers.nanjing;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class Nanjing extends Mapper {

    private static final long serialVersionUID = 0;

    private final int[] regs = new int[4];

    private boolean lastStrobe;
    private boolean trigger;

    public Nanjing(final CartFile cartFile) {
        super(cartFile, 2, 2);
    }

    @Override
    public void init() {
        lastStrobe = true;
        updateBanks();
    }

    private void updateBanks() {
        setPrgBank((regs[0] << 4) | (regs[1] & 0x0F));
        setChrBank(0, 0);
        setChrBank(1, 1);
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xF000) == 0x5000) {
            switch (address & 0x7700) {
                case 0x5100:
                    return regs[2] | regs[0] | regs[1] | (regs[3] ^ 0xFF);
                case 0x5500:
                    if (trigger) {
                        return regs[2] | regs[1];
                    } else {
                        return 0;
                    }
            }
            return 4;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {

        if (rendering && scanlineCycle == 256 && (regs[1] & 0x80) != 0) {
            if (scanline == 239) {
                setChrBank(0, 0);
                setChrBank(1, 0);
            } else if (scanline == 127) {
                setChrBank(0, 1);
                setChrBank(1, 1);
            }
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        if ((address & 0xF000) == 0x5000) {
            if (address == 0x5101) {
                if (lastStrobe && value == 0) {
                    trigger ^= true;
                }
                lastStrobe = value != 0;
            } else if (address == 0x5100 && value == 6) {
                setPrgBank(3);
            } else {
                switch (address & 0x7300) {
                    case 0x5200:
                        regs[0] = value;
                        updateBanks();
                        break;
                    case 0x5000:
                        regs[1] = value;
                        updateBanks();
                        if ((regs[1] & 0x80) == 0 && (ppu.getScanline() < 128)) {
                            setChrBank(0, 0);
                            setChrBank(1, 1);
                        }
                        break;
                    case 0x5300:
                        regs[2] = value;
                        break;
                    case 0x5100:
                        regs[3] = value;
                        updateBanks();
                        break;
                }
            }
        }
    }
}