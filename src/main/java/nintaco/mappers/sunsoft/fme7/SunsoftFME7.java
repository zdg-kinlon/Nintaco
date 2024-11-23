package nintaco.mappers.sunsoft.fme7;

import nintaco.files.*;
import nintaco.mappers.*;
import nintaco.tv.*;

import static nintaco.util.BitUtil.*;

public class SunsoftFME7 extends Mapper {

    private static final long serialVersionUID = 0;

    protected final Sunsoft5BAudio audio = new Sunsoft5BAudio();

    protected int command;
    protected boolean prgRamEnabled;
    protected boolean usePrgRam;

    protected int irqCounter;
    protected boolean irqEnabled;
    protected boolean irqCounterEnabled;

    public SunsoftFME7(final CartFile cartFile) {
        super(cartFile, 8, 8);
        setPrgBank(7, -1);
        audio.init();
    }

    @Override
    public void setTVSystem(TVSystem tvSystem) {
        super.setTVSystem(tvSystem);
        audio.setTVSystem(tvSystem);
    }

    @Override
    public int readMemory(final int address) {
        if (address < 0x8000) {
            if (address >= 0x6000) {
                if (usePrgRam) {
                    return prgRamEnabled ? memory[address] : 0;
                } else {
                    return prgROM[(prgBanks[3] | (address & 0x1FFF)) & prgRomSizeMask];
                }
            } else {
                return memory[address];
            }
        } else {
            return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        if (audio.writeRegister(address, value)) {
            return;
        }
        switch (address & 0xE000) {
            case 0x8000:
                writeCommand(value);
                break;
            case 0xA000:
                writeParameter(value);
                break;
            default:
                if (address < 0x6000 || (prgRamEnabled && usePrgRam)) {
                    memory[address] = value;
                }
                break;
        }
    }

    private void writeCommand(final int value) {
        command = 0x0F & value;
    }

    private void writeParameter(final int value) {
        switch (command) {
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
                setChrBank(command, value);
                break;
            case 0x08:
            case 0x09:
            case 0x0A:
            case 0x0B:
                writePrgBank(command - 0x05, value);
                break;
            case 0x0C:
                setNametableMirroring(value & 3);
                break;
            case 0x0D:
                writeIrqControl(value);
                break;
            case 0x0E:
                writeIrqCounterLow(value);
                break;
            case 0x0F:
                writeIrqCounterHigh(value);
                break;
        }
    }

    private void writeIrqCounterLow(final int value) {
        irqCounter = (irqCounter & 0xFF00) | value;
    }

    private void writeIrqCounterHigh(final int value) {
        irqCounter = (irqCounter & 0x00FF) | (value << 8);
    }

    private void writeIrqControl(final int value) {
        irqEnabled = getBitBool(value, 0);
        irqCounterEnabled = getBitBool(value, 7);
        cpu.setMapperIrq(false);
    }

    private void writePrgBank(final int bank, final int value) {
        setPrgBank(bank, value & 0x3F);
        if (bank == 3) {
            usePrgRam = getBitBool(value, 6);
            prgRamEnabled = getBitBool(value, 7);
        }
    }

    @Override
    public void update() {
        if (irqCounterEnabled) {
            irqCounter = (irqCounter - 1) & 0xFFFF;
            if (irqCounter == 0xFFFF && irqEnabled) {
                cpu.setMapperIrq(true);
            }
        }
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