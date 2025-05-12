package cn.kinlon.emu.mappers.rare;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.waixing.Mapper191;



public class TQROM extends Mapper191 {

    private static final long serialVersionUID = 0;

    public TQROM(final CartFile cartFile) {
        super(cartFile, 16, 0x1FFF);
    }
}

