package nintaco.mappers.jy;

// TODO ADD FDS AUDIO SWITCH IN CONFIGURATION
// TODO 1997 Super HiK 8-in-1 (JY-097).nes MALFUNCTIONING

import nintaco.files.CartFile;
import nintaco.mappers.nintendo.fds.FdsAudio;

public class Mapper295 extends JY {

    private static final long serialVersionUID = 0;

    private final FdsAudio audio = new FdsAudio();

    protected int prgBlockOffset;
    protected int chrBlockOffset;

    public Mapper295(final CartFile cartFile) {
        super(cartFile, 295);
    }

    @Override
    protected void setPrgBank(final int bank, final int value) {
        super.setPrgBank(bank, prgBlockOffset | (value & 0x0F));
    }

    @Override
    protected void setChrBank(final int bank, final int value) {
        super.setChrBank(bank, chrBlockOffset | (value & 0x7F));
    }

    @Override
    public int readMemory(final int address) {
        final int value = audio.readRegister(address);
        return (value >= 0) ? value : super.readMemory(address);
    }

    @Override
    public void writeMemory(final int address, int value) {
        if (!audio.writeRegister(address, value) && (address & 0xF003) == 0xD003) {
            value &= 7;
            prgBlockOffset = value << 4;
            chrBlockOffset = value << 7;
            updatePrgBanks();
            updateChrBanks();
        }
        super.writeMemory(address, value);
    }

    @Override
    public void update() {
        audio.update();
        super.update();
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
