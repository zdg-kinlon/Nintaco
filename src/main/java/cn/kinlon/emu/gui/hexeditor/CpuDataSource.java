package cn.kinlon.emu.gui.hexeditor;

import cn.kinlon.emu.mappers.Mapper;

public class CpuDataSource extends DataSource {

    private final Mapper mapper;

    public CpuDataSource(final Mapper mapper) {
        super(0x10000);
        this.mapper = mapper;
    }

    @Override
    public int peek(final int address) {
        try {
            return address < 0 ? 0 : mapper.peekCpuMemory(address);
        } catch (Throwable t) {
            return 0;
        }
    }

    @Override
    public void write(final int address, final int value) {
        if (address >= 0) {
            mapper.writeMemory(address, value & 0xFF);
        }
    }

    @Override
    public int getIndex() {
        return CpuMemory;
    }
}
