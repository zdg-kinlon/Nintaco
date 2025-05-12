package cn.kinlon.emu.mappers.american;

import cn.kinlon.emu.files.NesFile;
import cn.kinlon.emu.mappers.colordreams.ColorDreams;

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
