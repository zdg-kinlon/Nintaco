package cn.kinlon.emu.mappers.unif.bmc;

// TODO FDS AUDIO FLAG IN GUI
// TODO FDS AUDIO RESET SHOULD BE CALLED ELSEWHERE

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.Mapper;
import cn.kinlon.emu.mappers.nintendo.fds.FdsAudio;




import static cn.kinlon.emu.utils.BitUtil.*;

public class BMCSB5013 extends Mapper {

    private static final long serialVersionUID = 0;

    private static final int[] MASKS = {0x3F, 0x1F, 0x2F, 0x0F};

    private final FdsAudio audio = new FdsAudio();
    private final int[] prg = new int[4];

    private int offset;
    private int mask;
    private int irqLatch;
    private int irqLatency;
    private int irqCounter;
    private boolean irqReload;
    private boolean irqEnabled;
    private boolean PA12Mode;
    private boolean irqAutoEnable;

    public BMCSB5013(final CartFile cartFile) {
        super(cartFile, 8, 1, 0x8000, 0x6000);
    }

    @Override
    public void init() {
        prg[0] = 3;
        prg[1] = 0;
        prg[2] = 1;
        prg[3] = 2;
        mask = 0x3F;
        offset = irqCounter = 0;
        irqReload = irqEnabled = PA12Mode = irqAutoEnable = false;
        setNametableMirroring(0);
        cpu.setMapperIrq(false);
        audio.reset();
        updateState();
    }

    @Override
    public void resetting() {
        init();
    }

    private void updateState() {
        setPrgBank(3, offset | (mask & prg[0]));
        setPrgBank(4, offset | (mask & prg[1]));
        setPrgBank(5, offset | (mask & prg[2]));
        setPrgBank(6, offset | (mask & prg[3]));
        setPrgBank(7, offset | (mask & 0xFF));
    }

    private void writePrgBankSelect(final int index, final int value) {
        prg[index] = value;
        updateState();
    }

    private void writePrgBaseSelect(final int value) {
        offset = (value & 0x38) << 1;
        updateState();
    }

    private void writeOuterBankSizeSelect(final int value) {
        mask = MASKS[value & 3];
        updateState();
    }

    private void writeNametableMirroringTypeSelect(final int value) {
        setNametableMirroring(value & 3);
    }

    private void writeIrqCounterLowReload(final int value) {
        if (irqAutoEnable) {
            irqEnabled = false;
        }
        if (PA12Mode) {
            irqReload = true;
            irqCounter = 0;
        } else {
            irqCounter = (irqCounter & 0xFF00) | value;
        }
        cpu.setMapperIrq(false);
    }

    private void writeIrqCounterHighLoadLatch(final int value) {
        if (irqAutoEnable) {
            irqEnabled = true;
        }
        if (PA12Mode) {
            irqLatch = value;
        } else {
            irqCounter = (value << 8) | (irqCounter & 0x00FF);
        }
        cpu.setMapperIrq(false);
    }

    private void writeIrqMode(final int value) {
        irqAutoEnable = getBitBool(value, 2);
        PA12Mode = getBitBool(value, 1);
        irqEnabled = getBitBool(value, 0);
        cpu.setMapperIrq(false);
    }

    private void writeIrqDisableEnable(final int value) {
        irqEnabled = getBitBool(value, 0);
        cpu.setMapperIrq(false);
    }

    @Override
    public int readMemory(final int address) {
        final int value = audio.readRegister(address);
        return (value >= 0) ? value : super.readMemory(address);
    }

    @Override
    public void writeMemory(final int address, int value) {
        super.writeMemory(address, value);
        audio.writeRegister(address, value);
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        switch (address & 0xF003) {
            case 0x8000:
                writePrgBankSelect(1, value);
                break;
            case 0x8001:
                writePrgBankSelect(2, value);
                break;
            case 0x8002:
                writePrgBankSelect(3, value);
                break;
            case 0x8003:
                writePrgBankSelect(0, value);
                break;
            case 0x9000:
                writePrgBaseSelect(value);
                break;
            case 0x9001:
                writeOuterBankSizeSelect(value);
                break;
            case 0x9002:
                writeNametableMirroringTypeSelect(value);
                break;
            case 0xC000:
                writeIrqCounterLowReload(value);
                break;
            case 0xC001:
                writeIrqCounterHighLoadLatch(value);
                break;
            case 0xC002:
                writeIrqMode(value);
                break;
            case 0xC003:
                writeIrqDisableEnable(value);
                break;
        }
    }

    @Override
    public void handlePpuCycle(final int scanline, final int scanlineCycle,
                               final int address, final boolean rendering) {

        if (irqLatency != 0) {
            --irqLatency;
        }
        if ((address & 0x1000) != 0) {
            if (PA12Mode && irqLatency != 0) {
                if (irqReload || (irqCounter & 0x00FF) == 0) {
                    irqCounter = (irqCounter & 0xFF00) | irqLatch;
                    irqReload = false;
                } else {
                    irqCounter = (irqCounter & 0xFF00) | ((irqLatch - 1) & 0x00FF);
                }
                if (irqEnabled && (irqCounter & 0x00FF) == 0) {
                    cpu.setMapperIrq(true);
                }
            }
            irqLatency = 8;
        }
    }

    @Override
    public void update() {
        audio.update();
        if (irqEnabled && !PA12Mode) {
            --irqCounter;
            irqCounter &= 0xFFFF;
            if (irqCounter == 0) {
                cpu.setMapperIrq(true);
            }
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
}
