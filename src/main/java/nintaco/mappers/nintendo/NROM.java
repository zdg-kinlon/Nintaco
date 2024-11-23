package nintaco.mappers.nintendo;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class NROM extends Mapper {

    private static final long serialVersionUID = 0;

    public NROM(final CartFile cartFile) {
        super(cartFile, 2, 1);
    }
}
