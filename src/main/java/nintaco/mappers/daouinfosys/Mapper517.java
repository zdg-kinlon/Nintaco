package nintaco.mappers.daouinfosys;

import nintaco.files.CartFile;
import nintaco.mappers.Mapper;

public class Mapper517 extends Mapper {

    private static final long serialVersionUID = 0;

    private int adcData;
    private int adcHigh;
    private int adcLow;
    private int state;

    public Mapper517(final CartFile cartFile) {
        super(cartFile, 4, 1);
    }

    @Override
    public void init() {
        setPrgBank(3, -1);
        adcData = state = 0;
    }

    @Override
    public void resetting() {
        init();
    }

    @Override
    public int readMemory(final int address) {
        if ((address & 0xF000) == 0x6000) {
            if (address == 0) {
                switch (state) {
                    case 0:
                        ++state;
                        return 0;
                    case 1:
                        ++state;
                        return 1;
                    default:
                        if (adcLow > 0) {
                            --adcLow;
                            return 1;
                        } else {
                            state = 0;
                            return 0;
                        }
                }
            } else {
                return (adcHigh-- > 0) ? 0 : 1;
            }
        } else {
            return super.readMemory(address);
        }
    }

    private int readMicrophone() {
        return 0; // TODO IMPLEMENT MICROPHONE
    }

    @Override
    protected void writeRegister(final int address, final int value) {
        if ((address & 0xF000) == 0x8000) {
            adcData = (int) (readMicrophone() * 63.0);
            adcHigh = adcData >> 2;
            adcLow = 0x40 - adcHigh - ((adcData & 3) << 2);
            state = 0;
            setPrgBank(2, value);
        }
    }
}