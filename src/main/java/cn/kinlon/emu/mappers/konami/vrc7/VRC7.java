package cn.kinlon.emu.mappers.konami.vrc7;

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.konami.VrcIrq;

import static cn.kinlon.emu.utils.BitUtil.getBitBool;

public class VRC7 extends VrcIrq {

    private static final long serialVersionUID = 0;

    private final VRC7Audio audio = new VRC7Audio();
    private boolean wramEnabled; // unused

    public VRC7(final CartFile cartFile) {
        super(cartFile);
        xram = new int[0x2000];
        setPrgBank(7, -1);
        audio.init();
    }

    @Override
    public void writeVRAM(int address, int value) {
        if (chrRamPresent && address < 0x2000) {
            xram[chrBanks[address >> 10] | (address & 0x03FF)] = value;
        } else {
            vram[address] = value;
        }
    }

    @Override
    public int readVRAM(int address) {
        if (address < 0x2000) {
            return (chrRamPresent ? xram : chrROM)[chrBanks[address >> 10]
                    | (address & 0x03FF)];
        } else {
            return vram[address];
        }
    }

    @Override
    protected void writeRegister(int address, int value) {
        if (audio.writeRegister(address, value)) {
            return;
        }
        switch (address) {
            case 0x8000:
                writePrgBank(4, value);
                break;
            case 0x8008:
            case 0x8010:
                writePrgBank(5, value);
                break;
            case 0x9000:
                writePrgBank(6, value);
                break;
            case 0xA000:
                writeChrBank(0, value);
                break;
            case 0xA008:
            case 0xA010:
                writeChrBank(1, value);
                break;
            case 0xB000:
                writeChrBank(2, value);
                break;
            case 0xB008:
            case 0xB010:
                writeChrBank(3, value);
                break;
            case 0xC000:
                writeChrBank(4, value);
                break;
            case 0xC008:
            case 0xC010:
                writeChrBank(5, value);
                break;
            case 0xD000:
                writeChrBank(6, value);
                break;
            case 0xD008:
            case 0xD010:
                writeChrBank(7, value);
                break;
            case 0xE000:
                writeMirroring(value);
                break;
            case 0xE008:
            case 0xE010:
                writeIrqLatch(value);
                break;
            case 0xF000:
                writeIrqControl(value);
                break;
            case 0xF008:
            case 0xF010:
                writeIrqAcknowledge();
                break;
        }
    }

    private void writePrgBank(int bank, int value) {
        setPrgBank(bank, value & 0x3F);
    }

    private void writeChrBank(int bank, int value) {
        chrBanks[bank] = value << 10;
    }

    void writeMirroring(int value) {
        writeNametableMirroring(value);
        wramEnabled = getBitBool(value, 7);
    }

    private void writeNametableMirroring(int nametableMirroring) {
        nametableMirroring = nametableMirroring & 3;
        setNametableMirroring(nametableMirroring);
    }

    @Override
    public void update() {
        super.update();
        audio.update();
    }

    @Override
    public int getAudioMixerScale() {
        return audio.getAudioMixerScale();
    }

    @Override
    public float getAudioSample() {
        return audio.getAudioSample();
    }
}
