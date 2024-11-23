package nintaco.mappers.colordreams;

import nintaco.files.NesFile;
import nintaco.mappers.nintendo.GxROM;

public class RumbleStation extends GxROM {

    private static final long serialVersionUID = 0;

    public RumbleStation(NesFile nesFile) {
        super(nesFile);
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address < 0x6000) {
            memory[address] = value;
        } else if (address < 0x8000) {
            writeBanksHigh(value);
        } else {
            writeBanksLow(value);
        }
    }

    private void writeBanksHigh(int value) {
        prgBanks[1] = (prgBanks[1] & 0x08000) | ((value & 0x0F) << 16);
        chrBanks[0] = (chrBanks[0] & 0x0E000) | ((value & 0xF0) << 12);
    }

    private void writeBanksLow(int value) {
        prgBanks[1] = (prgBanks[1] & 0xF0000) | ((value & 0x01) << 15);
        chrBanks[0] = (chrBanks[0] & 0xF0000) | ((value & 0x70) << 9);
    }
}
