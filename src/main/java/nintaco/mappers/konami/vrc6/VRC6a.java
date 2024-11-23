package nintaco.mappers.konami.vrc6;

import nintaco.files.CartFile;
import nintaco.mappers.konami.VrcIrq;

import static nintaco.mappers.NametableMirroring.*;
import static nintaco.util.BitUtil.getBitBool;

public class VRC6a extends VrcIrq {

    private static final long serialVersionUID = 0;

    protected final int[] R = new int[8];
    protected final VRC6Audio audio = new VRC6Audio();

    protected int B003;
    protected boolean chrRomNametables;
    protected boolean chrA10;
    protected boolean prgRamEnabled;

    public VRC6a(final CartFile cartFile) {
        super(cartFile, 8, 12);
        setPrgBank(7, -1);
    }

    @Override
    public int readVRAM(int address) {
        if (address < 0x3000) {
            if (address < 0x2000) {
                if (chrRamPresent) {
                    return vram[address];
                } else {
                    return chrROM[chrBanks[address >> 10] | (address & 0x03FF)];
                }
            } else {
                if (chrRomNametables) {
                    if (chrRamPresent) {
                        return vram[address];
                    } else {
                        return chrROM[chrBanks[address >> 10] | (address & 0x03FF)];
                    }
                } else {
                    return vram[address];
                }
            }
        } else {
            return vram[address];
        }
    }

    protected int adjustAddress(int address) {
        return address & 0xF003;
    }

    @Override
    public void writeMemory(int address, int value) {
        if (address < 0x6000 || address >= 0x8000 || prgRamEnabled) {
            memory[address] = value;
        }
        if (address >= 0x8000) {
            writeRegister(address, value);
        }
    }

    @Override
    protected void writeRegister(int address, int value) {
        if (audio.writeRegister(address, value)) {
            return;
        }
        switch (adjustAddress(address)) {
            case 0x8000:
            case 0x8001:
            case 0x8002:
            case 0x8003:
                write16KPrgSelect(value);
                break;

            case 0xB003:
                writePPUBankingStyle(value);
                break;

            case 0xC000:
            case 0xC001:
            case 0xC002:
            case 0xC003:
                write8KPrgSelect(value);
                break;

            case 0xD000:
                writeChrSelect(0, value);
                break;
            case 0xD001:
                writeChrSelect(1, value);
                break;
            case 0xD002:
                writeChrSelect(2, value);
                break;
            case 0xD003:
                writeChrSelect(3, value);
                break;
            case 0xE000:
                writeChrSelect(4, value);
                break;
            case 0xE001:
                writeChrSelect(5, value);
                break;
            case 0xE002:
                writeChrSelect(6, value);
                break;
            case 0xE003:
                writeChrSelect(7, value);
                break;

            case 0xF000:
                writeIrqLatch(value);
                break;
            case 0xF001:
                writeIrqControl(value);
                break;
            case 0xF002:
                writeIrqAcknowledge();
                break;
        }
    }

    protected void write16KPrgSelect(int value) {
        value = (value & 0x0F) << 1;
        setPrgBank(4, value);
        setPrgBank(5, value | 1);
    }

    protected void write8KPrgSelect(int value) {
        setPrgBank(6, value & 0x1F);
    }

    protected void writePPUBankingStyle(int value) {
        B003 = value;
        chrRomNametables = getBitBool(value, 4);
        chrA10 = getBitBool(value, 5);
        prgRamEnabled = getBitBool(value, 7);
        updateChrBanks();
        updateNametables();
    }

    protected void writeChrSelect(int register, int value) {
        R[register] = value;
        updateChrBanks();
    }

    protected void setChrBank1K(int bank, int register) {
        chrBanks[bank] = register << 10;
    }

    protected void setChrBank2K(int bank, int register) {
        int value = R[register];
        if (chrA10) {
            value &= 0xFE;
        } else {
            value <<= 1;
        }
        setChrBank1K(bank, value);
        setChrBank1K(bank + 1, value | 1);
    }

    protected void updateChrBanks() {
        switch (B003 & 3) {
            case 0:
                for (int i = 7; i >= 0; i--) {
                    setChrBank1K(i, R[i]);
                }
                break;
            case 1:
                for (int i = 3; i >= 0; i--) {
                    setChrBank2K(i << 1, i);
                }
                break;
            case 2:
            case 3:
                for (int i = 3; i >= 0; i--) {
                    setChrBank1K(i, R[i]);
                }
                setChrBank2K(4, 4);
                setChrBank2K(6, 5);
                break;
        }
    }

    protected void updateNametables() {
        switch (B003 & 7) {
            case 0:
            case 6:
            case 7:
                setChrBank1K(8, 6);
                setChrBank1K(9, 6);
                setChrBank1K(10, 7);
                setChrBank1K(11, 7);
                break;
            case 1:
            case 5:
                setChrBank1K(8, 4);
                setChrBank1K(9, 5);
                setChrBank1K(10, 6);
                setChrBank1K(11, 7);
                break;
            case 2:
            case 3:
            case 4:
                setChrBank1K(8, 6);
                setChrBank1K(9, 7);
                setChrBank1K(10, 6);
                setChrBank1K(11, 7);
                break;
        }
        if (chrA10) {
            switch (B003 & 0x0F) {
                case 0x00:
                case 0x07:
                    setNametableMirroring(VERTICAL);
                    break;
                case 0x03:
                case 0x04:
                    setNametableMirroring(HORIZONTAL);
                    break;
                case 0x08:
                case 0x0F:
                    setNametableMirroring(ONE_SCREEN_A);
                    break;
                case 0x0B:
                case 0x0C:
                    setNametableMirroring(ONE_SCREEN_B);
                    break;
                default:
                    setNametableMirroring(FOUR_SCREEN);
                    break;
            }
        } else {
            setNametableMirroring(FOUR_SCREEN);
        }
    }

    @Override
    public float getAudioSample() {
        return audio.getAudioSample();
    }

    @Override
    public int getAudioMixerScale() {
        return audio.getAudioMixerScale();
    }

    @Override
    public void update() {
        super.update();
        audio.update();
    }
}
