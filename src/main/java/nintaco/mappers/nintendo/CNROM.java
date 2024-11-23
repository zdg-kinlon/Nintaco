package nintaco.mappers.nintendo;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class CNROM extends Mapper {

    private static final long serialVersionUID = 0;

    public CNROM(final CartFile cartFile) {
        super(cartFile, 2, 1);
        memory[0x0011] = 0xFF; // Minna no Taabou no Nakayoshi Daisakusen
        java.util.Arrays.fill(vram, 0xFF);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        setChrBank(value);
    }
}
