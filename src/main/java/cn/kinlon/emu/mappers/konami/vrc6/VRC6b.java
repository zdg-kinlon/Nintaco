package cn.kinlon.emu.mappers.konami.vrc6;

import cn.kinlon.emu.files.NesFile;

public class VRC6b extends VRC6a {

    private static final long serialVersionUID = 0;

    private static final int[] ADDRESS_MAP = {0, 2, 1, 3};

    public VRC6b(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    protected int adjustAddress(int address) {
        return (address & 0xF000) | ADDRESS_MAP[address & 0x0003];
    }
}
