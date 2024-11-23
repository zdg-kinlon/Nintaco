package nintaco.mappers.american;

import nintaco.files.NesFile;
import nintaco.mappers.colordreams.ColorDreams;

public class DeathRace extends ColorDreams {

    private static final long serialVersionUID = 0;

    public DeathRace(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    protected void writeRegister(int address, int value) {
        if (address != 0x8000) {
            super.writeRegister(address, value);
        }
    }
}
