package cn.kinlon.emu.mappers.nintendo;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;

public class NROM extends Mapper {

    private static final long serialVersionUID = 0;

    public NROM(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }
}
