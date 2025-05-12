package cn.kinlon.emu.mappers.txc;

import cn.kinlon.emu.files.CartFile;


import static cn.kinlon.emu.mappers.NametableMirroring.*;

public class Txc22211B extends JV001 {

    private static final long serialVersionUID = 0;

    public Txc22211B(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }

    @Override
    protected void updateState() {
        setPrgBank(0);
        setChrBank(output);
        setNametableMirroring(X ? HORIZONTAL : VERTICAL);
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xE000) == 0x4000) {
            final int v = readJV001(address);
            return ((v & 0x01) << 5) | ((v & 0x02) << 3) | ((v & 0x04) << 1)
                    | ((v & 0x08) >> 1) | ((v & 0x10) >> 3) | ((v & 0x20) >> 5) | 0xC0;
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int v) {
        if (address >= 0x4000) {
            writeJV001(address, ((v & 0x01) << 5) | ((v & 0x02) << 3)
                    | ((v & 0x04) << 1) | ((v & 0x08) >> 1) | ((v & 0x10) >> 3)
                    | ((v & 0x20) >> 5));
        } else {
            memory[address] = v;
        }
    }
}