package cn.kinlon.emu.gui.hexeditor;

import cn.kinlon.emu.ppu.PPU;

public class PpuDataSource extends DataSource {

    private final PPU ppu;

    public PpuDataSource(PPU ppu) {
        super(0x4000);
        this.ppu = ppu;
    }

    @Override
    public int peek(int address) {
        try {
            return address < 0 ? 0 : ppu.peekVRAM(address);
        } catch (Throwable t) {
            return 0;
        }
    }

    @Override
    public void write(int address, int value) {
        if (address >= 0) {
            ppu.writeVRAM(address, value & 0xFF);
        }
    }

    @Override
    public int getIndex() {
        return PpuMemory;
    }
}
