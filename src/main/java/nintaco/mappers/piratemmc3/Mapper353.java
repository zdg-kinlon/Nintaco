package nintaco.mappers.piratemmc3;

// TODO ADD FDS AUDIO SWITCH IN CONFIGURATION

import nintaco.files.*;
import nintaco.mappers.nintendo.*;
import nintaco.mappers.nintendo.fds.*;

import static nintaco.util.BitUtil.*;

public class Mapper353 extends MMC3 {

    private static final long serialVersionUID = 0;

    private final FdsAudio audio = new FdsAudio();

    private int outer;
    private int mirroring;

    public Mapper353(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        mirroring = 0;
        writeOuterBankRegister(0);
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public int readMemory(final int address) {
        final int value = audio.readRegister(address);
        return (value >= 0) ? value : super.readMemory(address);
    }

    private void writeOuterBankRegister(final int address) {
        outer = (address >> 13) & 3;
        updateState();
    }

    private void updateState() {
        if (outer == 2) {
            setPrgBlock((outer << 5) | ((R[0] & 0x80) >> 3), 0x0F);
        } else {
            setPrgBlock(outer << 5, 0x1F);
        }
        if (outer == 2 && getBitBool(R[0], 7)) {
            chrRamPresent = true;
        } else {
            chrRamPresent = false;
            setChrBlock(outer << 7, 0x7F);
        }
        updateBanks();
    }

    @Override
    public void writeMemory(final int address, int value) {
        if (!audio.writeRegister(address, value) && (address & 0x8080) == 0x8080) {
            writeOuterBankRegister(address);
        }
        super.writeMemory(address, value);
    }

    @Override
    protected void writeMirroring(final int value) {
        mirroring = value;
        if (outer != 0) {
            super.writeMirroring(value);
        }
    }

    @Override
    protected void updatePrgBanks() {
        super.updatePrgBanks();
        if (outer == 3 && !getBitBool(R[0], 7)) {
            setPrgBank(6, 0x70 | R[6]);
            setPrgBank(7, 0x70 | R[7]);
        }
    }

    @Override
    protected void updateChrBanks() {
        super.updateChrBanks();
        if (outer == 0) {
            if (chrMode) {
                setNametable(0, R[2]);
                setNametable(1, R[3]);
                setNametable(2, R[4]);
                setNametable(3, R[5]);
            } else {
                setNametable(0, R[0]);
                setNametable(1, R[0]);
                setNametable(2, R[1]);
                setNametable(3, R[1]);
            }
        } else {
            super.writeMirroring(mirroring);
        }
    }

    @Override
    public void setNametable(final int index, final int value) {
        nametableMappings[index] = 0x2000 | ((value & 0x80) << 3);
    }

    @Override
    public void update() {
        audio.update();
    }

    @Override
    public float getAudioSample() {
        return audio.getAudioSample();
    }

    @Override
    public int getAudioMixerScale() {
        return audio.getAudioMixerScale();
    }
}