package cn.kinlon.emu.mappers.daouinfosys;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;
import cn.kinlon.emu.mappers.konami.vrc7.VRC7Audio;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class Mapper515 extends Mapper {

    private static final long serialVersionUID = 0;

    private final VRC7Audio audio = new VRC7Audio();

    private int adcData;

    public Mapper515(final CartFile cartFile) {
        super(cartFile, 4, 1);
        audio.init();
    }

    @Override
    public void init() {
        setPrgBank(3, -1);
    }

    private int readMicrophone() {
        return 0; // TODO IMPLEMENT MICROPHONE
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xE003) == 0x6003) {
            final int value = adcData & 0x80;
            adcData <<= 1;
            adcData &= 0xFF;
            return value;
        } else {
            return super.readMemory(address);
        }
    }

    private void write6(final int address, final int value) {
        switch (address & 1) {
            case 0:
                audio.writeRegister(0x9010, value);
                break;
            case 1:
                audio.writeRegister(0x9030, value);
                break;
        }
    }

    private void write8(final int value) {
        // The last 1 MiB of PRG-ROM are the main cartridge, the first (total 
        // PRG-ROM size minus 1 MiB) are the expansion cartridge.
        int bank = value & 0x3F;
        if (getBitBool(value, 7)) {
            bank -= 64;
        }
        setPrgBank(2, bank);
    }

    private void writeC(final int address) {
        if ((address & 3) == 2) {
            adcData = (int) (readMicrophone() * 64.0);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        memory[address] = value;
        switch (address & 0xE000) {
            case 0x6000:
                write6(address, value);
                break;
            case 0x8000:
                write8(value);
                break;
            case 0xC000:
                writeC(address);
                break;
        }
    }

    @Override
    public void update() {
        audio.update();
    }

    @Override
    public int getAudioMixerScale() {
        return 0; // board does not allow 2A03 audio to be mixed with YM2413 output
    }

    @Override
    public float getAudioSample() {
        return audio.getAudioSample();
    }
}