package cn.kinlon.emu.mappers.ntdec;

// TODO WIP CARD GAME NOT WORKING
// TODO FIGURE OUT WHERE THE AUDIO SAMPLES COME INTO PLAY

import cn.kinlon.emu.files.CartFile;
import cn.kinlon.emu.mappers.nintendo.MMC3;

import static cn.kinlon.emu.tv.TVSystem.NTSC;
import static cn.kinlon.emu.utils.StreamUtil.readByteArray;

public class PC95KO01 extends MMC3 {

    private static final double MAX_SAMPLE_COUNT = NTSC.cyclesPerSecond()
            / 4000.0;

    private transient int[] queue = new int[0x1000];
    private int queueSize;
    private int head;
    private int tail;

    private int outer0;
    private int outer1;
    private int keyboardRow;
    private int bits;
    private int shift;
    private int sample;
    private boolean SCL;
    private boolean SDA;
    private boolean state;
    private double sampleCount;

    public PC95KO01(final CartFile cartFile) {
        super(cartFile);
    }

    @Override
    public void init() {
        outer0 = outer1 = 0xE0;
        head = tail = sample = queueSize = bits = 0;
        sampleCount = 0.0;
        SDA = SCL = state = false;
        super.init();
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    protected void updatePrgBanks() {
        if (prgMode) {
            setPrgBank(4, outer0 | 0x1E);
            setPrgBank(5, outer1 | (R[7] & 0x1F));
            setPrgBank(6, 0xE0 | (R[6] & 0x1F));
            setPrgBank(7, -1);
        } else {
            setPrgBank(4, outer0 | (R[6] & 0x1F));
            setPrgBank(5, outer1 | (R[7] & 0x1F));
            setPrgBank(6, -2);
            setPrgBank(7, -1);
        }
    }

    private void enqueue(final int value) {
        if (queueSize < 0x1000) {
            ++queueSize;
            queue[head] = value;
            ++head;
            head &= 0xFFF;
        }
    }

    private int readKeyboardButtonState() {
        int state = 0;
        writeOutputPort(0x05);
        for (int i = 0; i <= keyboardRow; ++i) {
            writeOutputPort(0x04);
            state = (readInputPort(1) >> 1) & 0x0F;
            writeOutputPort(0x06);
            state |= (readInputPort(1) << 3) & 0xF0;
        }
        return state;
    }

    @Override
    public int readMemory(final int address) {
        switch (address) {
            case 0x4906:
                return readKeyboardButtonState();
            case 0x4C03:
                return 0x00;
            default:
                return super.readMemory(address);
        }
    }

    @Override
    public void writeMemory(final int address, final int value) {
        switch (address) {
            case 0x4904:
                keyboardRow = value;
                break;
            case 0x4C00:
                outer0 = value & 0xE0;
                updatePrgBanks();
                break;
            case 0x4C01:
                outer1 = value & 0xE0;
                updatePrgBanks();
                break;
            case 0x4C04:
                SCL = false;
                break;
            case 0x4C05:
                if (!SCL && state) {
                    shift = (bits < 0) ? 0 : ((shift << 1) | (SDA ? 1 : 0));
                    ++bits;
                }
                SCL = true;
                break;
            case 0x4C06:
                if (SCL && SDA && !state) {
                    bits = -1;
                    shift = 0;
                    state = true;
                }
                SDA = false;
                break;
            case 0x4C07: {
                int command = 0;
                int param = 0;
                int paramBits = 0;
                if (SCL && !SDA && state) {
                    for (int i = 0; i < 4; ++i) {
                        command = (command << 1) | (shift & 1);
                        shift >>= 1;
                        --bits;
                    }
                    paramBits = bits;
                    for (int i = 0; i < paramBits; ++i) {
                        param = (param << 1) | (shift & 1);
                        shift >>= 1;
                        --bits;
                    }
                    enqueue(param & 0xF);
                    enqueue((param >> 4) & 0xF);
                    enqueue((param >> 8) & 0xF);
                    state = false;
                }
                break;
            }
            default:
                super.writeMemory(address, value);
                break;
        }
    }

    @Override
    public float getAudioSample() {
        if (++sampleCount >= MAX_SAMPLE_COUNT) {
            sampleCount -= MAX_SAMPLE_COUNT;
            if (queueSize > 0) {
                --queueSize;
                sample = queue[tail] << 8;
                System.out.println(sample);
                ++tail;
                tail &= 0xFFF;
            } else {
                sample = 0;
            }
        }
        return sample;
    }

    @Override
    public int getAudioMixerScale() {
        return 0x7FFF; // TODO EXPERIMENT
    }
}